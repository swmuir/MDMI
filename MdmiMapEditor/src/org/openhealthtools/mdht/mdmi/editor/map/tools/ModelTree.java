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
package org.openhealthtools.mdht.mdmi.editor.map.tools;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.openhealthtools.mdht.mdmi.editor.common.components.CursorManager;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNodeRenderer;

/** A Tree view of some model element*/
public abstract class ModelTree extends JTree {
	
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");

	private DefaultMutableTreeNode m_root;
	private DefaultTreeModel       m_model;

	private MouseClickListener m_mouseListener = new MouseClickListener();
	
	/** Create the tree for this node */
	protected ModelTree(Object model) {
		setBackground(UIManager.getColor("Viewport.background"));
		loadTree(model);
	}
	

	/** Load the tree from this node */
	public void loadTree(Object model) {
		// create the tree with the model as the root
		createTree( createRootNode(model) );
		
		m_model.nodeStructureChanged(m_root);
	}
	
	/** build the root node for this model */
	protected abstract DefaultMutableTreeNode createRootNode(Object model);

	
	/** Define tree parameters, and set the model */
	private void createTree(DefaultMutableTreeNode root) {
		m_root = root;
		m_model = new DefaultTreeModel(m_root);
		setModel(m_model);
		setRowHeight(18);
	}
	
	@Override
	public void addNotify() {
		super.addNotify();

		addMouseListener(m_mouseListener);
		
		DefaultTreeCellRenderer renderer = createTreeCellRenderer();
		// set renderer background to match tree
		renderer.setBackgroundNonSelectionColor(getBackground());
		
		setCellRenderer(renderer);
		
		ToolTipManager.sharedInstance().registerComponent(this);
	}

	
	@Override
	public void removeNotify() {
		removeMouseListener(m_mouseListener);
		setCellRenderer(null);
		ToolTipManager.sharedInstance().unregisterComponent(this);
		
		super.removeNotify();
	}

	/** Create the tree cell renderer */
	public DefaultTreeCellRenderer createTreeCellRenderer() {
		return new EditableObjectNodeRenderer();
	}
	
	/** get the root node */
	public DefaultMutableTreeNode getRoot() {
		return m_root;
	}

	
	/** Expand this node, and all children */
	public void expandAll(DefaultMutableTreeNode node) {
		for (Enumeration<?> en = node.depthFirstEnumeration(); en != null && en.hasMoreElements();) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode)en.nextElement();
			expandPath(new TreePath(child.getPath()));
		}
	}
	
	/** Get the selected node */
	public DefaultMutableTreeNode getSelection() {
		TreePath selectedPath = getSelectionPath();
		if (selectedPath != null) {
			Object selection = selectedPath.getLastPathComponent();
			if (selection instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)selection;
				return selectedNode;
			}
		}
		return null;
	}
	

	/** Process a double-click by opening the selected node */
	public void handleDoubleClick() {
		openSelection();
	}

	/** Open (edit) the selected item */
	public void openSelection() {
		DefaultMutableTreeNode selection = getSelection();
		if (selection != null) {
			openSelection(selection.getUserObject());
		}
	}

	/** Open (edit) the supplied model */
	protected void openSelection(Object model) {
		openUserObject(this, model);
	}

	/** Open (edit) the supplied model, where the model
	 * is an user object of one of the tree nodes in the editor tree.
	 */
	public static void openUserObject(Component parent, Object userObject) {
		if (userObject != null) {
			CursorManager cm = CursorManager.getInstance(parent);
			try {
				cm.setWaitCursor();
				//find model in editor tree
				SelectionManager selectionManager = SelectionManager.getInstance();
				DefaultMutableTreeNode treeNode =
					selectionManager.getEntitySelector().findNode(userObject);
				if (treeNode != null) {
					selectionManager.getEntitySelector().selectNode(treeNode);
					selectionManager.editItem(treeNode);
					
					// reset focus
					parent.requestFocus();
				}
			} finally {
				cm.restoreCursor();
			}
		}
	}
	
	/////////////////////////////////////////
	//   Menu Handling
	////////////////////////////////////////
	/** Creates the popup menu based on the selection
	 * @return
	 */
	protected JPopupMenu createPopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();
		DefaultMutableTreeNode selection = getSelection();
		if (selection != null) {
			// Open node
			if (selection instanceof EditableObjectNode) {
				popupMenu.add(new OpenSelectionAction(selection));
			}
			
			// Expand node
			if (!selection.isLeaf()) {
				if (popupMenu.getComponentCount() > 0) {
					popupMenu.addSeparator();
				}
				popupMenu.add(new ExpandSelectionAction());
			}
		}
		
		return popupMenu;
	}

	////////////////////////////////////////////////////
	// Popup menu handling
	//////////////////////////////////////////////////
	private class MouseClickListener extends MouseAdapter {
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
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopup(e);
			}
		}

		/** Show popup menu based on selection */
		private void showPopup(MouseEvent e) {
			JTree tree = (JTree)e.getSource();

			// select node if its not already selected
			TreePath pathForPoint = tree.getPathForLocation(e.getX(), e.getY());
			if (pathForPoint != null) {
				tree.setSelectionPath(pathForPoint);
			}
			
			JPopupMenu popupMenu = createPopupMenu();

			if (popupMenu != null) {
				popupMenu.show(tree, e.getX(), e.getY());
			}
		}
	}
	
	/////////////////////////////////////////
	//  Action Listener Interface
	////////////////////////
	public class OpenSelectionAction extends AbstractAction {
		public OpenSelectionAction(DefaultMutableTreeNode node) {
			super(MessageFormat.format(s_res.getString("ViewDataObject.openFormat"), 
					ClassUtil.beautifyName(node.getUserObject().getClass())));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			openSelection();
		}
		
	}

	public class ExpandSelectionAction extends AbstractAction {
		public ExpandSelectionAction() {
			super(s_res.getString("ModelTree.expandAll"));
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			DefaultMutableTreeNode selection = getSelection();
			if (selection != null) {
				expandAll(selection);
			}
		}
		
	}
}
