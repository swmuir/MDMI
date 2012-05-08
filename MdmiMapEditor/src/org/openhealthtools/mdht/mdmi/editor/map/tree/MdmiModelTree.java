/*******************************************************************************
* Copyright (c) 2012 Firestar Software, Inc.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Firestar Software, Inc. - initial API and implementation
*
* Author:
*     Sally Conway
*
*******************************************************************************/
package org.openhealthtools.mdht.mdmi.editor.map.tree;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.map.Actions;
import org.openhealthtools.mdht.mdmi.editor.map.CollectionChangeListenerHelper;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.StatusPanel;
import org.openhealthtools.mdht.mdmi.editor.map.console.LinkedObject;
import org.openhealthtools.mdht.mdmi.editor.map.console.ReferenceLink;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;

/** Component presenting a tree of MessageGroup components */
public class MdmiModelTree extends JPanel {
	private static final long serialVersionUID = -1;

	private static final Map<KeyStroke, String> s_keystrokeMap = new HashMap<KeyStroke, String>();

	static {
		s_keystrokeMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				ActionEvent.CTRL_MASK), Actions.CUT_ACTION);
		s_keystrokeMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				ActionEvent.CTRL_MASK), Actions.COPY_ACTION);
		s_keystrokeMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V,
				ActionEvent.CTRL_MASK), Actions.PASTE_ACTION);
		s_keystrokeMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				ActionEvent.CTRL_MASK), Actions.NEW_CHILD_ACTION);
	}

	/** Resource for localization */
	private static ResourceBundle s_res = ResourceBundle
			.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tree.Local");

	// Components
	private JTree m_messageElementsTree;
	private DefaultMutableTreeNode m_treeRoot;
	private DefaultTreeModel m_treeModel;

	// Utility for handling inter-node references
	private TreeUtility m_treeUtility;

	// Listeners & renderers
	private EditableObjectNodeRenderer m_treeCellRenderer;
	private MouseClickListener m_mouseListener;

	// Items for Copy/Paste
	private ArrayList<EditableObjectNode> m_copyOfNodesList = new ArrayList<EditableObjectNode>();

	private EditableObjectNode m_draggingNode = null;
	private int m_droppingRow = -1;

	public MdmiModelTree() {
		setLayout(new BorderLayout());
		m_treeRoot = new MdmiModelNode();
		m_treeModel = new DefaultTreeModel(m_treeRoot);
		m_messageElementsTree = new JTree(m_treeModel);
		m_messageElementsTree.setRowHeight(18);
		m_messageElementsTree.setRootVisible(false);
		m_messageElementsTree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

		m_treeCellRenderer = new EditableObjectNodeRendererPlus();
		m_mouseListener = new MouseClickListener();
		m_treeUtility = new TreeUtility(m_treeRoot);

		JScrollPane scroller = new JScrollPane(m_messageElementsTree);
		add(scroller, BorderLayout.CENTER);

		// load with empty data
		List<MessageGroup> emptyGroup = new ArrayList<MessageGroup>();
		emptyGroup.add(new MessageGroup());
		loadTree(emptyGroup);
	}

	@Override
	public void addNotify() {
		super.addNotify();

		if (m_messageElementsTree != null) {
			ToolTipManager.sharedInstance().registerComponent(
					m_messageElementsTree);
			m_messageElementsTree.setCellRenderer(m_treeCellRenderer);
			m_messageElementsTree.addMouseListener(m_mouseListener);
			m_messageElementsTree.addMouseMotionListener(m_mouseListener);
		}

		// Add bindings for Copy (Ctrl C), Paste, etc.
		InputMap inputMap = m_messageElementsTree.getInputMap(WHEN_FOCUSED);
		ActionMap actionMap = m_messageElementsTree.getActionMap();

		for (KeyStroke keyStroke : s_keystrokeMap.keySet()) {
			String actionName = s_keystrokeMap.get(keyStroke);
			inputMap.put(keyStroke, actionName);
			actionMap.put(actionName, Actions.getActionInstance(actionName));
		}
	}

	@Override
	public void removeNotify() {

		if (m_messageElementsTree != null) {
			ToolTipManager.sharedInstance().unregisterComponent(
					m_messageElementsTree);
			m_messageElementsTree.setCellRenderer(null);
			m_messageElementsTree.removeMouseListener(m_mouseListener);
			m_messageElementsTree.removeMouseMotionListener(m_mouseListener);
		}

		// remove bindings for Copy (Ctrl C), Paste, etc.
		InputMap inputMap = m_messageElementsTree.getInputMap(WHEN_FOCUSED);
		ActionMap actionMap = m_messageElementsTree.getActionMap();

		for (KeyStroke keyStroke : s_keystrokeMap.keySet()) {
			String actionName = s_keystrokeMap.get(keyStroke);
			inputMap.remove(keyStroke);
			actionMap.remove(actionName);
		}

		super.removeNotify();
	}

	/** Return the selection tree */
	public JTree getMessageElementsTree() {
		return m_messageElementsTree;
	}

	/** Return all message groups, sorted as shown in the tree */
	public List<MessageGroup> getMessageGroups() {
		// We'll assume the message groups are the children of the root
		List<MessageGroup> groups = new ArrayList<MessageGroup>();
		for (int i = 0; i < m_treeRoot.getChildCount(); i++) {
			MessageGroupNode msgGroupNode = (MessageGroupNode) m_treeRoot
					.getChildAt(i);
			MessageGroup group = msgGroupNode.getMessageGroup();
			groups.add(group);
		}
		return groups;
	}

	/** Fill tree */
	public void loadTree(List<MessageGroup> groups) {
		// clear tree
		int[] childIndices = new int[m_treeRoot.getChildCount()];
		Object[] removedChildren = new Object[m_treeRoot.getChildCount()];
		for (int i = 0; i < childIndices.length; i++) {
			childIndices[i] = i;
			removedChildren[i] = m_treeRoot.getChildAt(i);
		}
		m_treeRoot.removeAllChildren();
		if (childIndices.length > 0) {
			m_treeModel
					.nodesWereRemoved(m_treeRoot, childIndices, removedChildren);
		}

		for (MessageGroup group : groups) {
			// Message Group - will load appropriate children
			EditableObjectNode msgGroupNode = new MessageGroupNode(group);
			m_treeRoot.add(msgGroupNode);
		}

		m_treeModel.nodeStructureChanged(m_treeRoot);
		m_messageElementsTree.setRootVisible(true);

		// expand MessageGroup and MessageModel nodes
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				m_messageElementsTree
						.expandPath(new TreePath(m_treeRoot.getPath()));
				for (int i = 0; i < m_treeRoot.getChildCount(); i++) {
					TreeNode child = m_treeRoot.getChildAt(i);
					m_messageElementsTree.expandPath(new TreePath(
							((DefaultMutableTreeNode) child).getPath()));
					for (int j = 0; j < child.getChildCount(); j++) {
						TreeNode grandChild = child.getChildAt(j);
						if (grandChild instanceof MessageModelNode) {
							m_messageElementsTree.expandPath(new TreePath(
									((MessageModelNode) grandChild).getPath()));
						}
					}
				}
			}
		});
	}

	/** Expand the specified node, and all nodes beneath it */
	public void expandNodeAndDescendents(DefaultMutableTreeNode top) {
		for (Enumeration<?> en = top.depthFirstEnumeration(); en != null
				&& en.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) en
					.nextElement();
			m_messageElementsTree.expandPath(new TreePath(node.getPath()));
		}
	}

	/** Collapse the specified node, and all nodes beneath it */
	public void collapseNodeAndDescendents(DefaultMutableTreeNode top) {
		for (Enumeration<?> en = top.depthFirstEnumeration(); en != null
				&& en.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) en
					.nextElement();
			m_messageElementsTree.collapsePath(new TreePath(node.getPath()));
		}
	}

	/** Return a list of all nodes of this node type */
	public List<DefaultMutableTreeNode> getNodesOfType(Class<?> nodeType) {
		List<DefaultMutableTreeNode> nodes = new ArrayList<DefaultMutableTreeNode>();
		for (Enumeration<?> en = m_treeRoot.depthFirstEnumeration(); en != null
				&& en.hasMoreElements();) {
			TreeNode node = (TreeNode) en.nextElement();
			// node.getClass() extends nodeType
			if (nodeType.isAssignableFrom(node.getClass())) {
				nodes.add((DefaultMutableTreeNode) node);
			}
		}
		return nodes;
	}

	/** Return a list of all nodes of this user type */
	public List<DefaultMutableTreeNode> getNodesOfUserObjectType(
			Class<?> userObjectType) {
		List<DefaultMutableTreeNode> nodes = new ArrayList<DefaultMutableTreeNode>();
		for (Enumeration<?> en = m_treeRoot.depthFirstEnumeration(); en != null
				&& en.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) en
					.nextElement();
			// node.getClass() extends nodeType
			if (userObjectType.isAssignableFrom(node.getUserObject().getClass())) {
				nodes.add((DefaultMutableTreeNode) node);
			}
		}
		return nodes;
	}

	public void refreshNode(EditableObjectNode node) {
		m_treeModel.nodeStructureChanged(node);
	}
	
	/** refresh the user object currently in the tree with the new one */
	public DefaultMutableTreeNode refreshUserObject(Object userObject) {
		DefaultMutableTreeNode node = findNode(userObject);
		if (node != null) {
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node
					.getParent();
			String oldName = new String(node.toString()); // need to make a copy

			node.setUserObject(userObject); // forces name to be updated
			((EditableObjectNode) node).setHasError(false); // clear error
			String newName = node.toString();

			// check if we need to change the order of this node is relative to its
			// parent
			if (parent.getChildCount() > 1 && !newName.equalsIgnoreCase(oldName)) {
				// Name has changes, so we need to remove it and re-insert it

				// remember what was expanded
				List<DefaultMutableTreeNode> expandedNodes = getExpandedChildren(parent);

				// remove and re-insert (which will cause node to be collapsed)
				node.removeFromParent();
				if (parent instanceof EditableObjectNode) {
					((EditableObjectNode) parent).addSorted(node);
				} else {
					addSorted(parent, node);
				}
				m_treeModel.nodeStructureChanged(parent);

				// re-expand and re-select
				expandNodes(expandedNodes);
				selectNode(node);

			} else {
				m_treeModel.nodeChanged(node);
			}
			repaint();
		}
		return node;
	}

	/**
	 * Expand each node in the path
	 * 
	 * @param expandedPaths
	 */
	public void expandNodes(List<DefaultMutableTreeNode> expandedNodes) {
		for (DefaultMutableTreeNode node : expandedNodes) {
			TreePath path = new TreePath(node.getPath());
			m_messageElementsTree.expandPath(path);
		}
	}

	/**
	 * Get the TreePath of this node and all children that are expanded
	 * 
	 * @param node
	 * @return
	 */
	public List<DefaultMutableTreeNode> getExpandedChildren(
			DefaultMutableTreeNode node) {
		List<DefaultMutableTreeNode> expandedNodes = new ArrayList<DefaultMutableTreeNode>();
		for (Enumeration<?> en = node.depthFirstEnumeration(); en != null
				&& en.hasMoreElements();) {
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) en
					.nextElement();
			TreePath path = new TreePath(treeNode.getPath());
			if (m_messageElementsTree.isExpanded(path)) {
				expandedNodes.add(treeNode);
			}
		}
		return expandedNodes;
	}

	/**
	 * Find an ancestor of this type (could be this node) user object.
	 * 
	 * @param node
	 * @param parentType
	 * @return
	 */
	public static DefaultMutableTreeNode findParent(DefaultMutableTreeNode node,
			Class<? extends DefaultMutableTreeNode> parentType) {
		TreeNode parent = node;
		while (parent != null) {
			if (parent.getClass().equals(parentType)) {
				return (DefaultMutableTreeNode) parent;
			}
			parent = parent.getParent();
		}

		return null;
	}

	/**
	 * Find any descendent node of the tree that has an equivalent (using
	 * equals()) user object.
	 * 
	 * @param parentNode
	 * @param theClass
	 * @return
	 */
	public DefaultMutableTreeNode findNode(Object userObject) {
		return findNode(m_treeRoot, userObject);
	}

	/**
	 * Find any descendent node of this parent that has an equivalent (using
	 * equals()) user object.
	 * 
	 * @param parentNode
	 * @param theClass
	 * @return
	 */
	public DefaultMutableTreeNode findNode(DefaultMutableTreeNode parentNode,
			Object userObject) {
		DefaultMutableTreeNode foundNode = null;

		for (int c = 0; c < parentNode.getChildCount() && foundNode == null; c++) {
			if (!(parentNode.getChildAt(c) instanceof DefaultMutableTreeNode)) {
				continue;
			}

			DefaultMutableTreeNode child = (DefaultMutableTreeNode) parentNode
					.getChildAt(c);
			if (child.getUserObject().equals(userObject)) {
				foundNode = child;

			} else {
				// search children
				foundNode = findNode(child, userObject);
			}
		}
		return foundNode;
	}

	/**
	 * Add the child node to the parent, maintaining a sorted order - the node
	 * will be added at position i, such that childNode < node[i]
	 * 
	 * @param parentNode
	 * @param childNode
	 */
	public static void addSorted(DefaultMutableTreeNode parentNode,
			DefaultMutableTreeNode childNode) {
		int idx = parentNode.getChildCount();
		String childName = childNode.toString();
		for (int i = 0; i < parentNode.getChildCount(); i++) {
			TreeNode nodeAt_i = parentNode.getChildAt(i);
			String nodeName = nodeAt_i.toString();
			if (childName.compareToIgnoreCase(nodeName) < 0) {
				idx = i;
				break;
			}
		}

		parentNode.insert(childNode, idx);

	}

	/**
	 * Creates the popup menu based on the selection
	 * 
	 * @return
	 */
	protected JPopupMenu createPopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();

		List<TreeNode> nodes = getSelectedNodes();
		if (nodes.size() == 0) {
			return null;
		}
		
		// Single Selection
		if (nodes.size() == 1) {
			TreeNode node = nodes.get(0);
			if (node instanceof EditableObjectNode) {
				EditableObjectNode edNode = (EditableObjectNode)node;

				// Open (edit)
				if (edNode.isEditable()) {
					addMenuItemFor(Actions.EDIT_ACTION, popupMenu, false);
				}

				// New
				Collection<NewObjectInfo> newOperations = edNode.getNewObjectInformation(false);
				if (newOperations.size() > 0) {
					addSeparator(popupMenu);
					JMenu newMenu = new JMenu(Actions.getActionInstance(Actions.NEW_MENU_ACTION));
					if (newOperations.size() > 1) {
						// use sub-menu if there are multiples
						popupMenu.add(newMenu);
					}
					// add a menu item for each type
					for (NewObjectInfo newObjectInfo: newOperations) {
						JMenuItem menuItem = new JMenuItem(new NewItemAction(newObjectInfo, edNode));
						if (newOperations.size() == 1) {
							// add directly to popup menu
							menuItem.setText(newMenu.getText() + " " + menuItem.getText());	// "New Item..."
							popupMenu.add(menuItem);
						} else {
							// add to sub-menu
							newMenu.add(menuItem);
						}
					}
				}

				// Cut
				if (edNode.isCopyable() && edNode.isRemovable()) {
					addMenuItemFor(Actions.CUT_ACTION, popupMenu, false);
				}
				// Copy
				if (edNode.isCopyable()) {
					addMenuItemFor(Actions.COPY_ACTION, popupMenu, false);
				}
				
				// Paste - all nodes in copy-list must be "pastable" into the selected node
				boolean canPaste = true;
				for (EditableObjectNode nodeInList : m_copyOfNodesList) {
   				if (!edNode.canPaste(nodeInList)) {
   					canPaste = false;
   					break;
   				}
				}
				if (canPaste) {
					addMenuItemFor(Actions.PASTE_ACTION, popupMenu, false);
				}

				// Delete node
				if (edNode.isRemovable()) {
					addMenuItemFor(Actions.DELETE_ACTION, popupMenu, true);
				}
				
				// Move up/down
				if (edNode.getParent() instanceof EditableObjectNode && 
						!((EditableObjectNode)edNode.getParent()).showChildrenSorted()) {
					addMenuItemFor(Actions.MOVE_UP_ACTION, popupMenu, true);
					addMenuItemFor(Actions.MOVE_DOWN_ACTION, popupMenu, false);
				}
				
				// Change type
				if (edNode.canChangeType()) {
					addMenuItemFor(Actions.CHANGE_TYPE_ACTION, popupMenu, true);
				}

				// Find references
				if (edNode.isEditable()) {
					addMenuItemFor(Actions.FIND_REFERENCES_ACTION, popupMenu, true);
				}
				
				// node-specific menus
				List<JComponent> nodeSpecificMenus = edNode.getAdditionalPopuMenus();
				if (nodeSpecificMenus != null && nodeSpecificMenus.size() > 0) {
					addSeparator(popupMenu);
					for (JComponent item : nodeSpecificMenus) {
						popupMenu.add(item);
					}
				}
			}

			// Expand/Collapse
			addMenuItemFor(Actions.EXPAND_NODE_ACTION, popupMenu, true);
			addMenuItemFor(Actions.COLLAPSE_NODE_ACTION, popupMenu, false);
			
			
		} else {
			// multiple selection - 
			//   Edit, Copy, Cut Remove are allowed
			boolean canEditAll = true;
			boolean canRemoveAll = true;
			boolean canCutAll = true;
		
			boolean canCopy = SelectionManager.getInstance().canCopyNodes(nodes);

			for (TreeNode node : nodes) {
				if (!(node instanceof EditableObjectNode)) {
					return null;
				}
				EditableObjectNode edNode = (EditableObjectNode)node;
				if (!edNode.isEditable()) {
					canEditAll = false;
				}
				if (!edNode.isRemovable()) {
					canRemoveAll = false;
					canCutAll = false;
				}
				if (!edNode.isCopyable()) {
					canCutAll = false;
				}
					
			}

			if (canEditAll) {
				addMenuItemFor(Actions.EDIT_ACTION, popupMenu, false);
			}

			// Cut
			if (canCutAll) {
				addMenuItemFor(Actions.CUT_ACTION, popupMenu, false);
			}

			// Copy
			if (canCopy) {
				addMenuItemFor(Actions.COPY_ACTION, popupMenu, false);
			}
			
			// Delete
			if (canRemoveAll) {
				addMenuItemFor(Actions.DELETE_ACTION, popupMenu, true);
			}
			
			if (canEditAll) {
				addMenuItemFor(Actions.FIND_REFERENCES_ACTION, popupMenu, true);
			}
		}
		
		return popupMenu;
	}

	/**
	 * Add a separator to the menu if it is not empty- this is typically called
	 * before adding a new menu item
	 */
	private static void addSeparator(JPopupMenu popupMenu) {
		if (popupMenu.getComponentCount() > 0) {
			popupMenu.addSeparator();
		}
	}

	/**
	 * Add a new menuItem to the supplied menu, where the menuItem implements the
	 * action
	 * 
	 * @param actionName
	 *           The name to pass to Actions.getActionInstance()
	 * @param menu
	 *           The parent menu
	 * @param addSeparator
	 *           flag to add a separator before this menu
	 * @return
	 */
	static JMenuItem addMenuItemFor(String actionName, JPopupMenu menu,
			boolean addSeparator) {

		if (addSeparator) {
			addSeparator(menu);
		}

		JMenuItem menuItem = new JMenuItem(Actions.getActionInstance(actionName));
		menu.add(menuItem);

		return menuItem;
	}

	/** Perform the default action when a node is double clicked */
	protected void handleDoubleClick() {
		TreeNode node = getSelectedNode();
		if (node instanceof EditableObjectNode
				&& ((EditableObjectNode) node).isEditable()) {
			// Open
			Actions.performAction(Actions.EDIT_ACTION);
		}
	}

	/**
	 * Create a new child (only applies if there is only one type of child
	 * allowed)
	 */
	public void createNewChild(TreeNode node) {
		if (node instanceof EditableObjectNode) {
			EditableObjectNode edNode = (EditableObjectNode) node;
			// New
			Collection<NewObjectInfo> newOperations = edNode
					.getNewObjectInformation(false);
			// need one, and only one, child
			if (newOperations.size() == 1) {
				for (NewObjectInfo newObjectInfo : newOperations) {
					EditableObjectNode newNode = newObjectInfo.createNewChild();
					insertAndOpen(edNode, newNode);
				}
			}
		}
	}

	/** Create a new child of a particular class */
	public void createNewChild(TreeNode node, Class<?> childClass) {
		if (node instanceof EditableObjectNode) {
			EditableObjectNode edNode = (EditableObjectNode) node;
			// New
			NewObjectInfo newOperation = edNode
					.getNewObjectInformationForClass(childClass);
			if (newOperation != null) {
				EditableObjectNode newNode = newOperation.createNewChild();
				insertAndOpen(edNode, newNode);
			}
		}
	}

	/** Remove items, but put on clipboard */
	public void cutItems(List<TreeNode> treeNodes) {
		m_copyOfNodesList.clear();
		for (TreeNode node : treeNodes) {
			if (node instanceof EditableObjectNode
					&& ((EditableObjectNode) node).isCopyable()
					&& ((EditableObjectNode) node).isRemovable()) {
				EditableObjectNode edNode = (EditableObjectNode) node;

				if (deleteNode(edNode)) {
					m_copyOfNodesList.add(edNode);
				}
			}
		}
	}

	/** Make a copy of the items in the specified node */
	public void copyItems(List<TreeNode> treeNodes) {
		m_copyOfNodesList.clear();
		for (TreeNode node : treeNodes) {
			// ignore other nodes
			if (node instanceof EditableObjectNode
					&& ((EditableObjectNode) node).isCopyable()) {
				EditableObjectNode edNode = (EditableObjectNode) node;
				try {
					m_copyOfNodesList.add(edNode.copyNode());
				} catch (Exception e) {
					SelectionManager.getInstance().getStatusPanel().writeException(
							"Error copying " + edNode.getDisplayType() + " '"
									+ edNode.getDisplayName() + "'", e);
				}
			}
		}
	}

	/** return the list of nodes that were copied from a Copy operation */
	 public List<EditableObjectNode> getCopiedNodes() {
		 return m_copyOfNodesList;
	 }

	/** paste the copied items into the specified node */
	public void pasteItemsIn(EditableObjectNode node) {
		for (EditableObjectNode nodeInList : m_copyOfNodesList) {
			if (node.canPaste(nodeInList)) {
				try {
					EditableObjectNode copy = nodeInList.copyNode();
					EditableObjectNode childNode = node.paste(copy.getUserObject());
					insertAndOpen(node, childNode);

				} catch (Exception e) {
					SelectionManager.getInstance().getStatusPanel().writeException(
							"Error pasting '" + nodeInList.getDisplayName()
									+ "' into '" + node.getDisplayType() + "'", e);
				}
			}
		}
	}

	/** Delete the specified node(s) */
	public boolean deleteNode(TreeNode node, boolean prompt) {
		List<TreeNode> nodes = new ArrayList<TreeNode>();
		nodes.add(node);
		return deleteNodes(nodes, prompt);
	}

	/** Prompt the user to delete the specified node(s) */
	public boolean deleteNode(TreeNode node) {
		List<TreeNode> nodes = new ArrayList<TreeNode>();
		nodes.add(node);
		return deleteNodes(nodes, true);	// prompt
	}


	/** Delete the specified node(s) in the tree, as well as in the model */
	public boolean deleteNodes(List<TreeNode> nodes, boolean prompt) {
		List<EditableObjectNode> editableObjNodes = new ArrayList<EditableObjectNode>();

		// check that all nodes are removeable
		for (TreeNode node : nodes) {
			if (!(node instanceof EditableObjectNode)
					|| !((EditableObjectNode) node).isRemovable()) {
				return false;
			}
			editableObjNodes.add((EditableObjectNode) node);
		}

		// keep track of affected nodes
		Map<EditableObjectNode, List<EditableObjectNode>> affectedNodes = m_treeUtility
				.findAffectedNodes(editableObjNodes);

		// Confirm
		// Do you really want to delete <type> <name>?
		// [Yes] [No]
		int confirm = JOptionPane.YES_OPTION;
		if (prompt) {
			confirm = confirmDelete(nodes, affectedNodes);
		}

		if (confirm == JOptionPane.YES_OPTION) {

			// keep track of the classes of nodes for later notification
			CollectionChangeListenerHelper helper = new CollectionChangeListenerHelper();

			StatusPanel statusPanel = SelectionManager.getInstance()
					.getStatusPanel();

			// remove nodes from tree
			for (TreeNode treeNode : nodes) {
				EditableObjectNode node = (EditableObjectNode) treeNode;

				// remove from tree
				EditableObjectNode parent = (EditableObjectNode) node.getParent();
				int index = parent.getIndex(node);
				// delete user object reference from parent
				parent.deleteChild(node);

				// mark model as changed
				SelectionManager.getInstance().setUpdatesPending();

				// note class types that are impacted
				helper.addAllNodes(node);

				// notify tree listeners
				m_treeModel.nodesWereRemoved(parent, new int[] { index },
						new Object[] { node });

				// show status on console
				String statusMsg = MessageFormat.format(s_res
						.getString("MdmiModelTree.deleteStatus"), node
						.getDisplayType(), node.getDisplayName());
				statusPanel.writeConsole(statusMsg);
			}

			// show reference errors
			if (affectedNodes.size() > 0) {
				// There are reference errors as a result of this operation:
				statusPanel.writeConsole(s_res
						.getString("MdmiModelTree.referenceErrors"));
				statusPanel.writeErrorText(s_res
						.getString("MdmiModelTree.referenceErrors"));
			}
			int count = 1;
			for (EditableObjectNode referenceNode : affectedNodes.keySet()) {
				referenceNode.setHasError(true); // mark node with an error

				// #. Missing references in Type_of_Object <LINK>
				String msg = MessageFormat.format(s_res
						.getString("MdmiModelTree.referenceErrorFormat"),
						formatItemNumber(count++), referenceNode.getDisplayType());

				ReferenceLink link = new ReferenceLink(referenceNode
						.getUserObject(), referenceNode.getDisplayName());
				// fill in refers-to list
				for (EditableObjectNode refersTo : affectedNodes.get(referenceNode)) {
					link.addReferredToObject(refersTo.getUserObject());
				}

				statusPanel.writeErrorLink(msg, link, "");
			}

			// notify change listeners
			helper.notifyListeners();

			return true;

		} else {
			return false;
		}
	}

	/**
	 * Confirm the deletion of these nodes. The user will be shown a list of
	 * other nodes that have references to the node(s) being deleted.
	 * 
	 * @param deletedNodes
	 * @param affectedNodes
	 * @return JOptionPane.YES_OPTION or JOPtionPane.NO_OPTION
	 */
	private int confirmDelete(List<TreeNode> deletedNodes,
			Map<EditableObjectNode, List<EditableObjectNode>> affectedNodes) {

		StringBuffer confirmMsg = new StringBuffer();

		// show each affected node
		if (affectedNodes.size() > 0) {
			// There are one or more elements that have dependencies on the items
			// you will be removing.
			confirmMsg.append(s_res.getString("MdmiModelTree.deleteDependencies"));
			int count = 1;
			for (EditableObjectNode referenceNode : affectedNodes.keySet()) {
				// #. Type_of_Object object_name
				confirmMsg.append("\n ");
				confirmMsg.append(MessageFormat.format(s_res
						.getString("MdmiModelTree.dependencyFormat"),
						formatItemNumber(count++), referenceNode.getDisplayType(),
						referenceNode.getDisplayName()));
			}
			confirmMsg.append("\n\n");
		}

		// Do you really want to delete this/these items?
		if (deletedNodes.size() == 1) {
			EditableObjectNode node = (EditableObjectNode) deletedNodes.get(0);
			confirmMsg.append(MessageFormat.format(s_res
					.getString("MdmiModelTree.deleteConfirmOne"), node
					.getDisplayType(), node.getDisplayName()));

		} else {
			confirmMsg.append(s_res.getString("MdmiModelTree.deleteConfirmMulti"));
		}

		String title = s_res.getString("MdmiModelTree.deleteTitle");
		int confirm = JOptionPane.showConfirmDialog(this, confirmMsg.toString(),
				title, JOptionPane.YES_NO_OPTION);
		return confirm;
	}

	/** Format a line-item numbering "###." */
	public static String formatItemNumber(int number) {
		return String.format("% 3d.", number);
	}

	/** Move this node one postion up relative to its siblings */
	public boolean moveUp(EditableObjectNode node) {
		if (node.canMoveUp()) {
			EditableObjectNode parentNode = (EditableObjectNode) node.getParent();
			int childIdx = parentNode.getIndex(node);

			// remember what was expanded
			List<DefaultMutableTreeNode> expandedNodes = getExpandedChildren(parentNode);

			// move it
			int newIdx = parentNode.moveChildNode(node, -1);

			// update tree
			if (newIdx != childIdx) {
				m_treeModel.nodeStructureChanged(parentNode);

				// re-expand
				expandNodes(expandedNodes);
				selectNode(node);
			}
		}

		return false;
	}

	/** Move this node down one postion relative to its siblings */
	public boolean moveDown(EditableObjectNode node) {
		if (node.canMoveDown()) {
			EditableObjectNode parentNode = (EditableObjectNode) node.getParent();
			int childIdx = parentNode.getIndex(node);

			// remember what was expanded
			List<DefaultMutableTreeNode> expandedNodes = getExpandedChildren(parentNode);

			// move it
			int newIdx = parentNode.moveChildNode(node, 1);

			// update tree
			if (newIdx != childIdx) {
				m_treeModel.nodeStructureChanged(parentNode);

				// re-expand
				expandNodes(expandedNodes);
				selectNode(node);
			}
		}

		return false;
	}

	/** Move node from its current parent into a new parent */
	private void moveNode(EditableObjectNode node, EditableObjectNode newParent) {
		// newParent cannot be a descendent of the node
		if (newParent != node && !node.isNodeDescendant(newParent)
				&& newParent.canDrop(node)) {
			CollectionChangeListenerHelper helper = new CollectionChangeListenerHelper();

			EditableObjectNode oldParent = (EditableObjectNode) node.getParent();
			helper.addAllNodes(oldParent);
			int oldIndex = oldParent.getIndex(node);

			// remember what was expanded
			List<DefaultMutableTreeNode> expandedNodes = getExpandedChildren(node);

			// Remove node from old parent, and add it to new parent
			newParent.reParent(node);

			// mark model as changed
			helper.addAllNodes(newParent);
			SelectionManager.getInstance().setUpdatesPending();

			// notify change listeners
			helper.notifyListeners();

			// notify tree listeners
			int newIndex = newParent.getIndex(m_draggingNode);
			m_treeModel.nodesWereRemoved(oldParent, new int[] { oldIndex },
					new Object[] { m_draggingNode });
			m_treeModel.nodesWereInserted(newParent, new int[] { newIndex });

			// re-expand
			expandNodes(expandedNodes);
			// re-select
			selectNode(node);
		}

	}

	/** Show all references of the user object at this node */
	public void showReferences(EditableObjectNode node) {
		List<EditableObjectNode> referenceNodes = findReferences(node);

		// Write References on console
		StatusPanel statusPanel = SelectionManager.getInstance().getStatusPanel();
		// statusPanel.clearConsole();
		String infoMsg;
		LinkedObject link = new LinkedObject(node.getUserObject(), node
				.getDisplayName());
		if (referenceNodes.size() == 0) {
			// No references to Type <LINK> found
			infoMsg = MessageFormat.format(s_res
					.getString("MdmiModelTree.noReferencesFoundPrefix"), node
					.getDisplayType());
			statusPanel.writeConsoleLink(infoMsg, link, s_res
					.getString("MdmiModelTree.noReferencesFoundSuffix"));
		} else {
			// Found {0} references to Type <LINK>:
			infoMsg = MessageFormat.format(s_res
					.getString("MdmiModelTree.referencesFoundPrefixFormat"),
					referenceNodes.size(), node.getDisplayType());
			statusPanel.writeConsoleLink(infoMsg, link, s_res
					.getString("MdmiModelTree.referencesFoundSuffix"));

			// show each reference
			int count = 1;
			for (EditableObjectNode referenceNode : referenceNodes) {
				// #. Type_of_Object <LINK>
				infoMsg = MessageFormat.format(s_res
						.getString("MdmiModelTree.referencePrefix"),
						formatItemNumber(count++), referenceNode.getDisplayType());
				// show each reference as a link
				ReferenceLink refLink = new ReferenceLink(referenceNode
						.getUserObject(), referenceNode.getDisplayName());
				refLink.addReferredToObject(node.getUserObject());
				statusPanel.writeConsoleLink(infoMsg, refLink, "");
			}
		}
	}

	/**
	 * Find all nodes that have a user-object with references to the user-object
	 * in this node
	 * 
	 * @param node
	 * @return
	 */
	public List<EditableObjectNode> findReferences(EditableObjectNode node) {
		return m_treeUtility.findReferences(node);
	}

	/**
	 * Expand the tree and select this node
	 * 
	 * @param node
	 */
	public void selectNode(DefaultMutableTreeNode node) {
		TreePath path = new TreePath(node.getPath());
		TreePath parentPath = path.getParentPath();

		m_messageElementsTree.expandPath(parentPath);
		m_messageElementsTree.scrollPathToVisible(path);

		// select node
		m_messageElementsTree.setSelectionPath(path);
	}

	/**
	 * Get the single selection. If there are multiple selections, or no
	 * selections, null will be returned
	 */
	public TreeNode getSelectedNode() {
		TreeNode treeNode = null;

		TreePath[] selectedPaths = m_messageElementsTree.getSelectionPaths();
		if (selectedPaths != null && selectedPaths.length == 1) {
			Object selection = selectedPaths[0].getLastPathComponent();
			if (selection instanceof TreeNode) {
				treeNode = (TreeNode) selection;
			}
		}

		return treeNode;
	}

	/** Get all selections */
	public List<TreeNode> getSelectedNodes() {
		List<TreeNode> treeNodes = new ArrayList<TreeNode>();

		TreePath[] selectedPaths = m_messageElementsTree.getSelectionPaths();
		if (selectedPaths != null) {
			for (TreePath path : selectedPaths) {
				Object selection = path.getLastPathComponent();
				if (selection instanceof TreeNode) {
					treeNodes.add((TreeNode) selection);
				}
			}
		}

		return treeNodes;
	}

	/**
	 * Insert a node (from "new" or "past") into the tree. All listeners will be
	 * notified, and the new node will be opened.
	 * 
	 * @param parentNode
	 * @param newNode
	 */
	public void insertAndOpen(EditableObjectNode parentNode,
			EditableObjectNode newNode) {
		insertNewNode(parentNode, newNode);

		// Select and edit
		selectNode(newNode);
		SelectionManager.getInstance().editItem(newNode);
	}

	/**
	 * Insert a newly added node (from "new" or "paste") into the tree. All
	 * appropriate listeners will be notified
	 * 
	 * @param newNode
	 */
	public void insertNewNode(EditableObjectNode parentNode,
			EditableObjectNode newNode) {
		if (newNode != null) {
			DefaultMutableTreeNode topNode = parentNode.addSorted(newNode);

			// mark model as changed
			SelectionManager.getInstance().setUpdatesPending();

			// notify change listeners
			CollectionChangeListenerHelper helper = new CollectionChangeListenerHelper();
			helper.addAllNodes(newNode);
			helper.notifyListeners();

			// parent where node was inserted may not be the node that was clicked
			TreeNode newParent = topNode.getParent();
			int nodeIndex = newParent.getIndex(topNode);
			m_treeModel.nodesWereInserted(newParent, new int[] { nodeIndex });

		}
	}

	/** Change the type of the UserObject under this node */
	public void changeItemType(TreeNode node) {
		if (node instanceof EditableObjectNode) {
			EditableObjectNode edNode = (EditableObjectNode) node;

			// make sure node (and any chldren) is not being edited
			if (okayToChange(edNode)) {
				ChangeTypeDialog dlg = new ChangeTypeDialog(SystemContext
						.getApplicationFrame(), edNode);
				dlg.display(this);
			}
		}
	}

	/**
	 * Make sure this node, and it's immediate children, do not have any unaccepted
	 * edits.
	 * 
	 * @param node
	 * @return
	 */
	private boolean okayToChange(EditableObjectNode node) {

		Object userObject = node.getUserObject();

		// make sure node (and any chldren) is not being edited
		if (SelectionManager.getInstance().getEntityEditor().isModified(
				userObject)) {
			SelectionManager.getInstance().editItem(node);
			// <node> has unaccepted changes - please accept or cancel the editing
			// session
			JOptionPane.showMessageDialog(this, MessageFormat.format(s_res
					.getString("MdmiModelTree.pleaseAcceptFormat"), node
					.getDisplayName()), s_res
					.getString("MdmiModelTree.pleaseAcceptTitle"),
					JOptionPane.WARNING_MESSAGE);
			return false;
		}

		// check children
		for (int i = 0; i < node.getChildCount(); i++) {
			if (node.getChildAt(i) instanceof EditableObjectNode) {
				EditableObjectNode childNode = (EditableObjectNode) node
						.getChildAt(i);
				userObject = childNode.getUserObject();
				if (SelectionManager.getInstance().getEntityEditor().isModified(
						userObject)) {
					SelectionManager.getInstance().editItem(childNode);
					// <node> has unaccepted changes - please accept or cancel the editing
					// session
					JOptionPane.showMessageDialog(this, MessageFormat.format(s_res
							.getString("MdmiModelTree.pleaseAcceptFormat"), childNode
							.getDisplayName()), s_res
							.getString("MdmiModelTree.pleaseAcceptTitle"),
							JOptionPane.WARNING_MESSAGE);
					return false;
				}
			}
		}
		return true;
	}
	

	
	/** Replace all occurences of the old user object with the new one */
	public EditableObjectNode replaceUserObject(Object oldObj, Object newObj)
	{
		// we need to be careful what gets copied
		try {
			EditableObjectNode treeNode = (EditableObjectNode)findNode(oldObj);
			
			if (treeNode != null) {
				List<EditableObjectNode> affectedNodes = findReferences(treeNode);

				// replace all references to the old object with the new one
				for (EditableObjectNode affectedNode : affectedNodes) {
					Object userObject = affectedNode.getUserObject();
					TreeUtility.replaceReferenceTo(userObject, oldObj, newObj);
				}
				
				// remove old node from tree (this will remove it from the model as well)
				EditableObjectNode parent = (EditableObjectNode) treeNode.getParent();
				int index = parent.getIndex(treeNode);
				parent.deleteChild(treeNode);

				// notify tree listeners
				m_treeModel.nodesWereRemoved(parent, new int[] { index },
						new Object[] { treeNode });
				
				// create a new node with the new object (this will add children as well)
				EditableObjectNode newNode = parent.paste(newObj);

				// insert and notify
				insertNewNode(parent, newNode);
				
				return newNode;
			}
			
		} catch (Exception e) {
			SelectionManager.getInstance().getStatusPanel().writeException(e);
		}
		
		return null;
	}

	// //////////////////////////////////////////////////////////////
	class EditableObjectNodeRendererPlus extends EditableObjectNodeRenderer {

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			// outline if target of drop
			if (m_droppingRow == row) {
				setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, UIManager
						.getColor("Tree.selectionBackground")));
			} else {
				setBorder(null);
			}
			Component c = super.getTreeCellRendererComponent(tree, value,
					selected, expanded, leaf, row, hasFocus);
			return c;
		}

	}

	class MouseClickListener extends MouseAdapter implements MouseMotionListener {
		DragNodeCursor m_dragCursor = null;

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopup(e);

			} else if (e.getClickCount() == 2) {
				handleDoubleClick();
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopup(e);
			} else {
				m_draggingNode = null;
				m_droppingRow = -1;
			}
		}

		// initialize cursor when drag has started
		private void beginDrag(MouseEvent e) {
			EditableObjectNode node = getEditableObjectNode(e);
			if (node != null) {
				m_draggingNode = node;
				m_dragCursor = null;

				// Try a dynamic icon that uses the node's image
				if (node.getNodeIcon() instanceof ImageIcon) {
					try {
						m_dragCursor = new DragNodeCursor((ImageIcon) node
								.getNodeIcon(), node.toString());

						m_dragCursor.setCursor(e.getComponent(), e.getPoint());
					} catch (Exception ex) {
						SelectionManager.getInstance().getStatusPanel()
								.writeException(ex);
					}
				}

				if (m_dragCursor == null) {
					Cursor cursor = Cursor
							.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
					// use static image
					ImageIcon icon = TreeNodeIcon.getIcon(s_res
							.getString("MdmiModelTree.cursorIcon"));
					if (icon != null) {
						cursor = Toolkit.getDefaultToolkit().createCustomCursor(
								icon.getImage(), new Point(0, 4), "node");
					}

					e.getComponent().setCursor(cursor);
				}
			}
		}

		/**
		 * Get an EditableObjectNode from the tree at the provided location. If
		 * the object at that location is not an EditableObjectNode, than null
		 * will be returned
		 */
		private EditableObjectNode getEditableObjectNode(MouseEvent e) {
			JTree tree = (JTree) e.getComponent();
			TreePath pathForPoint = tree.getPathForLocation(e.getX(), e.getY());
			if (pathForPoint != null) {
				Object comp = pathForPoint.getLastPathComponent();
				if (comp instanceof EditableObjectNode) {
					return (EditableObjectNode) comp;
				}
			}
			return null;
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			m_dragCursor = null;
			e.getComponent().setCursor(Cursor.getDefaultCursor());

			if (e.isPopupTrigger()) {
				showPopup(e);
			} else if (m_draggingNode != null) {
				// implement drop
				if (m_droppingRow != -1) {
					EditableObjectNode parentTarget = getEditableObjectNode(e);
					if (parentTarget != null) {
						moveNode(m_draggingNode, parentTarget);
					}
				}
			}
			m_draggingNode = null;
			repaintRow((JTree) e.getComponent(), m_droppingRow);
			m_droppingRow = -1;
		}

		private void repaintRow(JTree tree, int row) {
			if (row != -1) {
				tree.repaint(tree.getRowBounds(row));
			}
		}

		/** Show popup menu based on selection */
		private void showPopup(MouseEvent e) {
			JTree tree = (JTree) e.getSource();

			// select node if its not already selected
			TreePath pathForPoint = tree.getPathForLocation(e.getX(), e.getY());
			if (pathForPoint != null) {
				boolean selected = false;
				TreePath[] selections = tree.getSelectionPaths();
				if (selections != null) {
					for (TreePath selectedPath : selections) {
						if (pathForPoint.equals(selectedPath)) {
							selected = true;
						}
					}
				}
				if (!selected) {
					m_messageElementsTree.setSelectionPath(pathForPoint);
				}
			}

			JPopupMenu popupMenu = createPopupMenu();

			if (popupMenu != null) {
				popupMenu.show(tree, e.getX(), e.getY());
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (m_draggingNode == null) {
				// initialize cursor
				beginDrag(e);

			} else if (m_dragCursor != null) {
				// redraw cursor
				m_dragCursor.setCursor(e.getComponent(), e.getPoint());
			}

			if (m_draggingNode != null) {
				JTree tree = (JTree) e.getComponent();
				int row = tree.getRowForLocation(e.getX(), e.getY());
				if (row != m_droppingRow) {
					// repaint old and new
					repaintRow(tree, m_droppingRow);
					repaintRow(tree, row);
				}
				m_droppingRow = row;
			}

		}

		@Override
		public void mouseMoved(MouseEvent e) {
			// don't care
			m_draggingNode = null;
		}
	}

	/** Action handler for creating a new item */
	private class NewItemAction extends AbstractAction {
		private NewObjectInfo m_newObjectInfo;
		private EditableObjectNode m_parentNode;

		public NewItemAction(NewObjectInfo newObjectInfo,
				EditableObjectNode parentNode) {
			super(newObjectInfo.getDisplayName());
			m_parentNode = parentNode;
			m_newObjectInfo = newObjectInfo;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			EditableObjectNode newNode = m_newObjectInfo.createNewChild();
			insertAndOpen(m_parentNode, newNode);
		}
	}
}
