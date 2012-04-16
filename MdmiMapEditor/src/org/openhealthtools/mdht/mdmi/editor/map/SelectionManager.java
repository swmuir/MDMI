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
package org.openhealthtools.mdht.mdmi.editor.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.map.console.LinkedObject;
import org.openhealthtools.mdht.mdmi.editor.map.editor.EditorPanel;
import org.openhealthtools.mdht.mdmi.editor.map.editor.TabContents;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ModelIOUtilities;
import org.openhealthtools.mdht.mdmi.editor.map.tree.DataTypeNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MdmiModelTree;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SyntaxNodeNode;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;

/** This class responds to selections made on the EntitySelector to enable/disable menus,
 * invoke editors, etc.
 * @author Conway
 *
 */
public class SelectionManager {
	/** Resource for localization */
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.Local");
	
	private MdmiModelTree m_selector = null;
	private EditorPanel    m_editor   = null;
	private StatusPanel	  m_status   = null;
	
	// Listeners
	private TreeSelectionListener	m_itemSelectionListener = new ItemSelectionListener();
	private TreeModelListener    	m_itemDeletedListener   = new TreeStateListener();
	
	// Action enabled/disabled states
	private HashMap<String, Boolean> m_actionState = new HashMap<String, Boolean>();
	
	// State Changes -  
	private List<ModelChangeListener> m_modelChangeListeners = new ArrayList<ModelChangeListener>();
	private List<CollectionChangeListener> m_changeListeners = new ArrayList<CollectionChangeListener>();
	
	
	private boolean m_updatesPending = false;
	
	// single instance
	private static SelectionManager s_instance = null;
	
	public static SelectionManager getInstance() {
		if (s_instance == null) {
			s_instance = new SelectionManager();
		}
		return s_instance;
	}
	
	private SelectionManager() {
	}

	/** Begin managment of the three components */
	public void startManagement(MdmiModelTree selector, EditorPanel editor, StatusPanel status) {
		m_selector = selector;
		m_editor = editor;
		m_status = status;
		

		// Initialize states of actions we're interested in
		initializeActionStates();
		
		enableActionsForNodeSelection(null, false);	// start out with nothing selected

		if (m_selector != null) {
			m_selector.getMessageElementsTree().getSelectionModel().addTreeSelectionListener(m_itemSelectionListener);
			m_selector.getMessageElementsTree().getModel().addTreeModelListener(m_itemDeletedListener);
		}
		
	}

	/**
	 * Initialize the enabled/disabled state of all actions we care about
	 */
	private void initializeActionStates() {
		m_actionState.put(Actions.EDIT_ACTION, false);
		m_actionState.put(Actions.EXPAND_NODE_ACTION, false);
		m_actionState.put(Actions.COLLAPSE_NODE_ACTION, false);
		m_actionState.put(Actions.NEW_CHILD_ACTION, false);
		m_actionState.put(Actions.CUT_ACTION, false);
		m_actionState.put(Actions.COPY_ACTION, false);
		m_actionState.put(Actions.PASTE_ACTION, false);
		m_actionState.put(Actions.DELETE_ACTION, false);
		m_actionState.put(Actions.MOVE_UP_ACTION, false);
		m_actionState.put(Actions.MOVE_DOWN_ACTION, false);
		m_actionState.put(Actions.ACCEPT_ACTION, false);
		m_actionState.put(Actions.ACCEPT_ALL_ACTION, false);
		m_actionState.put(Actions.SAVE_ACTION, true); // always allowed
		m_actionState.put(Actions.FIND_REFERENCES_ACTION, false);
	}
	

	public MdmiModelTree getEntitySelector() {
		return m_selector;
	}
	
	public EditorPanel getEntityEditor() {
		return m_editor;
	}
	
	public StatusPanel getStatusPanel() {
		return m_status;
	}
	
	public UserPreferences getUserPreferences() {
		String appName = SystemContext.getApplicationName();
		UserPreferences preferences = UserPreferences.getInstance(appName, null);
		return preferences;
	}
	
	/** Finish managment of these components */
	public void endManagement() {
		if (m_selector != null) {
			m_selector.getMessageElementsTree().getSelectionModel().removeTreeSelectionListener(m_itemSelectionListener);
			m_selector.getMessageElementsTree().getModel().removeTreeModelListener(m_itemDeletedListener);
		}
	}
	
	/*
	 *  Menu Actions
	 */

	////////////////////////////////////////////
	//            Expand                      //
	////////////////////////////////////////////
	
	/** Expand all nodes at this selection */
	public void expandSelection() {
		TreeNode selectedNode = m_selector.getSelectedNode();
		expandSelection(selectedNode);
	}
	
	/** Expand this node and all children */
	public void expandSelection(TreeNode treeNode) {
		if (treeNode instanceof DefaultMutableTreeNode) {
			m_selector.expandNodeAndDescendents((DefaultMutableTreeNode)treeNode);
		}
	}

	////////////////////////////////////////////
	//            Collapse                      //
	////////////////////////////////////////////
	
	/** Expand all nodes at this selection */
	public void collapseSelection() {
		TreeNode selectedNode = m_selector.getSelectedNode();
		collapseSelection(selectedNode);
	}
	
	/** Collapse this node and all children */
	public void collapseSelection(TreeNode treeNode) {
		if (treeNode instanceof DefaultMutableTreeNode) {
			m_selector.collapseNodeAndDescendents((DefaultMutableTreeNode)treeNode);
		}
	}

	////////////////////////////////////////////
	//            New                        //
	////////////////////////////////////////////
	
	/** Create a new (empty) model */
	public void createNewModel() {
		// load with empty data
		List<MessageGroup> emptyGroup = new ArrayList<MessageGroup>();
		emptyGroup.add(new MessageGroup());
		m_selector.loadTree(emptyGroup);
		m_updatesPending = false;
	}

	
	/** Create a new instance */
	public void createNewInstance() {
		TreeNode selectedNode = m_selector.getSelectedNode();
		createNewInstance(selectedNode);
	}
	
	/** Create a new child item under this node */
	public void createNewInstance(TreeNode treeNode) {
		m_selector.createNewChild(treeNode);
	}
	

	////////////////////////////////////////////
	//            Cut                      //
	////////////////////////////////////////////
	
	/** Cut an instance */
	public void cutInstance() {
		List<TreeNode> selectedNodes = m_selector.getSelectedNodes();
		cutItems(selectedNodes);
	}
	
	/** Create a new item on this node */
	public void cutItems(List<TreeNode> treeNodes) {
		m_selector.cutItems(treeNodes);
	}
	

	////////////////////////////////////////////
	//            Copy                      //
	////////////////////////////////////////////
	
	/** Copy an instance */
	public void copyInstance() {
		List<TreeNode> selectedNodes = m_selector.getSelectedNodes();
		copyItems(selectedNodes);
	}
	
	/** Create a new item on this node */
	public void copyItems(List<TreeNode> treeNodes) {
		m_selector.copyItems(treeNodes);
	}

	
	////////////////////////////////////////////
	//         Find References                //
	////////////////////////////////////////////
	
	/** Show all references to the selected item */
	public void showReferencesToSelection() {
		List<TreeNode> selectedNodes = m_selector.getSelectedNodes();
		showReferences(selectedNodes);
	}
	
	/** Show all references to the user object in this node(s) */
	public void showReferences(List<TreeNode> treeNodes) {
		StatusPanel statusPanel = SelectionManager.getInstance().getStatusPanel();
		statusPanel.clearConsole();
		for (TreeNode treeNode : treeNodes) {
			if (treeNode  instanceof EditableObjectNode ) {
				m_selector.showReferences((EditableObjectNode)treeNode);
			}
		}
	}

	
	////////////////////////////////////////////
	//            Paste                      //
	////////////////////////////////////////////
	
	/** Paste a copy of the saved instances into the selected node */
	public void pasteIntoInstance() {
		TreeNode selectedNode = m_selector.getSelectedNode();
		pasteItemsIn(selectedNode);
	}
	
	/** Create a new item on this node */
	public void pasteItemsIn(TreeNode treeNode) {
		if (treeNode  instanceof EditableObjectNode ) {
			m_selector.pasteItemsIn((EditableObjectNode)treeNode);
		}
	}


	////////////////////////////////////////////
	//            Delete                      //
	////////////////////////////////////////////
	
	/** Delete the currently selected item */
	public void deleteSelection() {
		List<TreeNode> selectedNodes = m_selector.getSelectedNodes();
		deleteItem(selectedNodes);
	}
	
	/** Delete the item(s) at this node */
	public void deleteItem(List<TreeNode> treeNodes) {
		m_selector.deleteNodes(treeNodes);
	}


	////////////////////////////////////////////
	//              Edit                      //
	////////////////////////////////////////////
	/** Edit the currently selected items */
	public void editSelection() {
		List<TreeNode> selectedNodes = m_selector.getSelectedNodes();
		for (TreeNode node: selectedNodes) {
			editItem(node);
		}
	}
	
	/** Edit the item at this node */
	public TabContents editItem(TreeNode treeNode) {
		if (treeNode  instanceof EditableObjectNode ) {
			return m_editor.editEntity((EditableObjectNode)treeNode);
		}
		return null;
	}


	////////////////////////////////////////////
	//        Change Type                    //
	////////////////////////////////////////////
	
	/** Change the type (class) of an item */
	public void changeItemType() {
		TreeNode selectedNode = m_selector.getSelectedNode();
		changeItemType(selectedNode);
	}
	
	/** Change the type (class) of the selected item */
	public void changeItemType(TreeNode treeNode) {
		m_selector.changeItemType(treeNode);
	}



	////////////////////////////////////////////
	//            Move Up                    //
	////////////////////////////////////////////
	
	/** move the item up one position in tree */
	public void moveItemUp() {
		TreeNode selectedNode = m_selector.getSelectedNode();
		moveItemUp(selectedNode);
	}
	
	/** Move the node up one position in the tree */
	public void moveItemUp(TreeNode treeNode) {
		if (treeNode  instanceof EditableObjectNode ) {
			m_selector.moveUp((EditableObjectNode)treeNode);
		}
	}

	////////////////////////////////////////////
	//           Move Down                    //
	////////////////////////////////////////////
	
	/** move the item Down one position in tree */
	public void moveItemDown() {
		TreeNode selectedNode = m_selector.getSelectedNode();
		moveItemDown(selectedNode);
	}
	
	/** Move the node Down one position in the tree */
	public void moveItemDown(TreeNode treeNode) {
		if (treeNode  instanceof EditableObjectNode ) {
			m_selector.moveDown((EditableObjectNode)treeNode);
		}
	}


	////////////////////////////////////////////
	//              Accept                    //
	////////////////////////////////////////////
	/** Accept the currently selected items */
	public void acceptSelection() {
		List<TreeNode> selectedNodes = m_selector.getSelectedNodes();
		for (TreeNode node: selectedNodes) {
			acceptItem(node);
		}
	}
	
	/** accept the item at this node */
	public void acceptItem(TreeNode treeNode) {
		if (treeNode  instanceof EditableObjectNode ) {
			acceptEntity(((EditableObjectNode)treeNode).getUserObject());
		}
	}

	/** accept changes to the Object being edited */
	public boolean acceptEntity(Object entity) {
		return m_editor.acceptEdits(entity);
	}
	
	/** accept all open items */
	public boolean acceptAllEdits() {
		return m_editor.acceptAllEdits();
	}


	////////////////////////////////////////////
	//              Save                      //
	////////////////////////////////////////////

	/** Check if there have been updates to any items that need to be saved */
	public boolean hasPendingUpdates() {
		return m_updatesPending;
	}
	
	/** Indicate that there have been updates to any items that need to be saved */
	public void setUpdatesPending() {
		m_updatesPending = true;
	}

	/** Save pending updates */
	public boolean saveUpdates() {
		// warn on error
		boolean hasErrors = false;
		List<MessageGroup> messageGroups = m_selector.getMessageGroups();
		for (MessageGroup messageGroup : messageGroups) {
			EditableObjectNode msgGroupNode = (EditableObjectNode)m_selector.findNode(messageGroup);
			if (msgGroupNode.hasError()) {
				hasErrors = true;
				break;
			}
		}
		if (hasErrors) {
//			There are reference errors in one or more model elements.
//			If any elements are referencing items that have been deleted, those references
//			will be set to null.
//			Do you want to accept anyway?
			int opt = JOptionPane.showConfirmDialog(m_selector, 
					s_res.getString("SelectionManager.referenceErrors"), 
					s_res.getString("SelectionManager.errorInModel"), JOptionPane.YES_NO_OPTION);
			if (opt != JOptionPane.YES_OPTION) {
				return false;
			}
		}
		
		if (ModelIOUtilities.writeModelToFile()) {

			m_updatesPending = false;

			return true;
		}
		return false;
		
	}
	

	////////////////////////////////////////////


	/** check enabled state of the actions */
	private void readActionStates() {
		for (String key: m_actionState.keySet()) {
			boolean enabled = Actions.getActionInstance(key).isEnabled();
			m_actionState.put(key, enabled);
		}
	}
	
	/** Save the enabled state for this action */
	private void saveActionState(String key, boolean enabled) {
		m_actionState.put(key, enabled);
	}

	/** update all actions with the saved state */
	private void updateActionStates() {
		for (String key: m_actionState.keySet()) {
			boolean enabled = m_actionState.get(key);
			Actions.getActionInstance(key).setEnabled(enabled);
		}
	}
	
	public void enableActionsForSelection() {
		List<TreeNode> selectedNodes = m_selector.getSelectedNodes();
		enableActionsForNodeSelection(selectedNodes, true);
	}
	
	/** Called when a node is selected - enable/disable actions */
	private void enableActionsForNodeSelection(List<TreeNode> selectedNodes, boolean selected) {
		// Save current states
		readActionStates();
		
		if (selectedNodes == null || selectedNodes.size() == 0) {
			// select or deselect all actions
			for (String key: m_actionState.keySet()) {
				saveActionState(key, selected);
			}
			
		} else {
			boolean singleSelection = (selectedNodes.size() == 1);
			boolean canOpen = true;
			boolean canRemove = true;
			boolean canCut = true;
			boolean canAccept = false;
			boolean showRef = true;
			
			for (TreeNode node : selectedNodes) {
				if (node instanceof EditableObjectNode) {
					EditableObjectNode selectedNode = (EditableObjectNode)node;
					Object entity = selectedNode.getUserObject();
					
					// Open can apply to multiple selections if all are editable
					if (!selected || !selectedNode.isEditable()) {
						canOpen = false;
						showRef = false;
					}

					// Delete can apply to multiple selections if all are removable
					if (!selected || !selectedNode.isRemovable()) {
						canRemove = false;
					}
					
					// Accept can apply to multiple selections if any are open
					if (selected && m_editor.isOpen(entity)) {
						canAccept = true;
					}
					
					// new child will apply if there is only one allowed child type
					saveActionState(Actions.NEW_CHILD_ACTION, singleSelection && selected &&
							selectedNode.canCreateNewChild());
					
					// Cut can apply to multiple selections if all are removable and copyable
					if (!selected || !selectedNode.isRemovable() || !selectedNode.isCopyable()) {
						canCut = false;
					}

					// Paste - single selection, all nodes in list must be pastable
					boolean pasteAllowed = true;
					if (!singleSelection || !selected) {
						pasteAllowed = false;
					} else {
						for (EditableObjectNode copiedNode : getEntitySelector().getCopiedNodes())
						{
							if (!selectedNode.canPaste(copiedNode)) {
								pasteAllowed = false;
								break;
							}
						}
					}
					saveActionState(Actions.PASTE_ACTION, pasteAllowed);

					// move up/down
					saveActionState(Actions.MOVE_UP_ACTION, singleSelection && selected &&
							selectedNode.canMoveUp());
					saveActionState(Actions.MOVE_DOWN_ACTION, singleSelection && selected &&
							selectedNode.canMoveDown());
					
					// Expand/Collapse
					saveActionState(Actions.EXPAND_NODE_ACTION, singleSelection && selected);
					saveActionState(Actions.COLLAPSE_NODE_ACTION, singleSelection && selected);

				}
			}

			// Open, Copy, Delete and Save, References can apply to  multiples, so we'll handle separately
			saveActionState(Actions.EDIT_ACTION, canOpen);
			saveActionState(Actions.COPY_ACTION, selected && canCopyNodes(selectedNodes));
			saveActionState(Actions.CUT_ACTION, canCut);
			saveActionState(Actions.DELETE_ACTION, canRemove);
			saveActionState(Actions.ACCEPT_ACTION, canAccept);
			saveActionState(Actions.FIND_REFERENCES_ACTION, showRef);
		}
		
		// Accept All
		saveActionState(Actions.ACCEPT_ALL_ACTION, m_editor.isAnyEntityModified());
		
		// Save - always allowed
		saveActionState(Actions.SAVE_ACTION, true);
		
		// update the actions with the new state
		updateActionStates();
		
	}

	/** Determine whethere the nodes in this list can be copied */
	public boolean canCopyNodes(List<TreeNode> nodes) {
		boolean canCopy = true;
		EditableObjectNode firstNode = null;
		
   	for (TreeNode node : nodes) {
   		if (!(node instanceof EditableObjectNode)) {
   			return false;
   		}
   		EditableObjectNode edNode = (EditableObjectNode)node;
   		
   		// to copy, all selected nodes must be copyable.
   		if (!edNode.isCopyable()) {
   			canCopy = false;
   		} else if (firstNode == null) {
				// first one
				firstNode = edNode;
   		} else if (edNode.getParent() == firstNode.getParent()) {
   			// same parents - this is okay
			} else if (!sameBasicType(edNode, firstNode)) {
				// different varieties
				canCopy = false;
			} else if (!sameBasicType(edNode.getParent(), firstNode.getParent())) {
				// different parent varieties
				canCopy = false;
			}
   		
   		if (!canCopy) {
   			break;
   		}
   	}
   	return canCopy;
	}
	
	// same basic type
	private boolean sameBasicType(TreeNode node1, TreeNode node2)
	{
		if (node1 == null || node2 == null ||
				!(node1 instanceof EditableObjectNode) || !(node2 instanceof EditableObjectNode)) {
			return false;
		}
		
		// some types are the same basic type
		if ( (node1 instanceof DataTypeNode && node2 instanceof DataTypeNode)
			|| (node1 instanceof SyntaxNodeNode && node2 instanceof SyntaxNodeNode) ) {
			return true;
		}
		
		// otherwise check display type
		EditableObjectNode eNode1 = (EditableObjectNode)node1;
		EditableObjectNode eNode2 = (EditableObjectNode)node2;
		return (eNode1.getDisplayType().equals(eNode2.getDisplayType()));
	}
	
	//////////////////////////////////////////////////
	//   Console Activity
	//////////////////////////////////////////////////

	/** Update the console */
	public void writeToConsole(String text) {
		m_status.writeConsole(text);
	}
	
	/** Update the console with an error message */
	public void writeError(String textPre, LinkedObject entity, String textPost) {
		m_status.writeErrorLink(textPre, entity, textPost);
	}
	
	//////////////////////////////////////////////////////
	//  Change Listeners
	//////////////////////////////////////////////////////

	/** Add a Change listener for changes to a particular class of objects
	 */
	public void addCollectionChangeListener(CollectionChangeListener listener) {
		synchronized (m_changeListeners) {
			if (!(m_changeListeners.contains(listener))) {
				m_changeListeners.add(listener);
			}
		}
	}
	
	/** Remove a Change listener for changes to a particular class of objects
	 */
	public void removeCollectionChangeListener(Class<?> objectClass, CollectionChangeListener listener) {
		synchronized (m_changeListeners) {
			m_changeListeners.remove(listener);
		}
	}
	
	/** Notify class listeners when an object of this class type changes */
	public void notifyCollectionChangeListeners(Class<?> objectClass) {
		synchronized (m_changeListeners) {
			for (CollectionChangeListener listener : m_changeListeners) {
				Class<?> listenerClass = listener.getListenForClass();
				if (listenerClass.isAssignableFrom(objectClass)) {
					listener.contentsChanged(new CollectionChangeEvent(objectClass));
				}
			}
		}
	}
	
	/** Get all the collection listeners */
	public Collection<CollectionChangeListener> getChangeListeners() {
		return m_changeListeners;
	}
	

	/** Add a ModelChange listener for changes to a particular object
	 */
	public void addModelChangeListener(ModelChangeListener listener) {
		synchronized (m_modelChangeListeners) {
			if (!(m_modelChangeListeners.contains(listener))) {
				m_modelChangeListeners.add(listener);
			}
		}
	}
	
	/** Remove a ModelChange listener for changes to a particular object
	 */
	public void removeModelChangeListener(ModelChangeListener listener) {
		synchronized (m_modelChangeListeners) {
			m_modelChangeListeners.remove(listener);
		}
	}
	
	/** Notify model listeners when an object changes */
	public void notifyModelChangeListeners(Object model) {
		synchronized (m_modelChangeListeners) {
			for (ModelChangeListener listener : m_modelChangeListeners) {
				listener.modelChanged(new ModelChangeEvent(model));
			}
		}
	}
	

	/** Listener for Selection Tree */
	class ItemSelectionListener implements TreeSelectionListener {
		final boolean [] m_order = {false, true};
		
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			TreePath [] paths = e.getPaths();
			// first handle de-selections, then handle selections
			List<TreeNode> nodes = new ArrayList<TreeNode>();
			for (TreePath path : paths) {
				if (e.isAddedPath(path) == false) {
					TreeNode node = (TreeNode)path.getLastPathComponent();
					nodes.add(node);
				}
			}
			enableActionsForNodeSelection(nodes, false);
			
			// handle selections (if there are any)
			nodes = m_selector.getSelectedNodes();
			enableActionsForNodeSelection(nodes, true);
		}
	}

	class TreeStateListener implements TreeModelListener {

		@Override
		public void treeNodesChanged(TreeModelEvent event) {
//			for (Object child: event.getChildren()) {
//				System.out.println("Changed " + child);
//			}
		}

		@Override
		public void treeNodesInserted(TreeModelEvent event) {
//			for (Object child: event.getChildren()) {
//				System.out.println("Inserted " + child);
//			}
		}

		@Override
		public void treeNodesRemoved(TreeModelEvent event) {
			Object [] children = event.getChildren();
			for (Object childRemoved: children) {
				if (childRemoved instanceof TreeNode) {
					stopEditingEntity((TreeNode)childRemoved);
				}
			}
		}


		@Override
		public void treeStructureChanged(TreeModelEvent event) {
//			for (Object child: event.getChildren()) {
//				System.out.println("Structure Changed " + child);
//			}
		}

		private void stopEditingEntity(TreeNode node) {
			// process children
			for (int i=0; i<node.getChildCount(); i++) {
				TreeNode child = node.getChildAt(i);
				if (child instanceof TreeNode) {
					stopEditingEntity((TreeNode)child);
				}
			}
			// notify editor to close if entity is deleted
			if (node instanceof EditableObjectNode) {
				m_editor.stopEditing(((EditableObjectNode)node).getUserObject());
			}
		}
	}
}
