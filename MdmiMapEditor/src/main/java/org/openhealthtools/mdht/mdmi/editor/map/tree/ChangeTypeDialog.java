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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;

/** Present a dialog allowing the user to change the supplied object
 * from one class type to another
 * @author Conway
 *
 */
public class ChangeTypeDialog extends BaseDialog {

	/** Resource for localization */
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tree.Local");

	private EditableObjectNode m_node; 
	
	private JList m_listOfTypes;
	
	private TypeSelectionListener m_listListener = new TypeSelectionListener();
	private ClassNameRenderer m_listRenderer = new ClassNameRenderer();

	public ChangeTypeDialog(Frame owner, EditableObjectNode node) {
		super(owner, OK_CANCEL_OPTION);
		m_node = node;
		
		buildUI();
		pack(new Dimension(300, 100));
	}
	
	@Override
	public void dispose() {
		m_listOfTypes.setCellRenderer(null);
		m_listOfTypes.removeListSelectionListener(m_listListener);
		
		super.dispose();
	}
	
	/** Can this node have its type changed. */
	public static boolean canChangeType(EditableObjectNode node) {
		if (node.getParent() instanceof EditableObjectNode) {
			EditableObjectNode parent = (EditableObjectNode)node.getParent();
			Collection<NewObjectInfo> newObjects = parent.getNewObjectInformation(true);
			if (newObjects != null && newObjects.size() > 1) {
				return true;
			}
		}
		return false;
	}

	private void buildUI() {
		JPanel main = new JPanel(new GridBagLayout());
		
		setTitle(s_res.getString("ChangeTypeDialog.title"));
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = Standards.getInsets();
		
		// Change this <objectType> to a new type:
		//  -------------------------------------
		// | New Type 1                        | |
		// | New Type 2                        | |
		// | New Type 3                        | |
		//  -------------------------------------
		//
		Object userObject = m_node.getUserObject();
		String displayText = MessageFormat.format(s_res.getString("ChangeTypeDialog.displayFormat"), 
			ClassUtil.beautifyName(userObject.getClass()));
		main.add(new JLabel(displayText), gbc);
		
		gbc.gridy++;
		
		// fill list 
		DefaultListModel listModel = new DefaultListModel();
		
		EditableObjectNode parent = (EditableObjectNode)m_node.getParent();
		Collection<NewObjectInfo> newObjects = parent.getNewObjectInformation(true);
		for (NewObjectInfo newObject : newObjects) {
			Class<?> childClass = newObject.getChildClass();
			if (!childClass.equals(userObject.getClass())) {
				listModel.addElement(childClass);
			}
		}
		m_listOfTypes = new JList(listModel);
		JScrollPane scroller = new JScrollPane(m_listOfTypes);
		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1;
		main.add(scroller, gbc);

		m_listOfTypes.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_listOfTypes.setCellRenderer(m_listRenderer);
		m_listOfTypes.addListSelectionListener(m_listListener);
		getContentPane().add(main, BorderLayout.CENTER);
	}

	@Override
	protected void okButtonAction() {
		// Create a new object of the selected type
		Class<?> selectedType = (Class<?>)m_listOfTypes.getSelectedValue();
		
		// confirm
		// Are you sure you want to change '<name>' from a <oldType> to a <newType>?
		String confirmMsg = MessageFormat.format(s_res.getString("ChangeTypeDialog.confirmFormat"),
				m_node.toString(), m_node.getDisplayType(), ClassUtil.beautifyName(selectedType));
		int opt = JOptionPane.showConfirmDialog(this, confirmMsg,
				getTitle(), JOptionPane.YES_NO_OPTION);
		if (opt != JOptionPane.YES_OPTION) {
			return;
		}
		
		if (changeClassType(m_node,selectedType) != null) {
			// all is good

		} else {
			// Unable to create a new <type>
			showError(getTitle(), 
					MessageFormat.format(s_res.getString("ChangeTypeDialog.errorFormat"),
							selectedType));
		}

		super.okButtonAction();
	}

	/** Change the user-object in this node to a new class.
	 * All fields from the existing user-object will be copied to the new object.
	 * @return	The tree node containing the new object, or null */
	public static EditableObjectNode changeClassType(EditableObjectNode node, Class<?> newClass) {
		EditableObjectNode parent = (EditableObjectNode)node.getParent();
		Object userObject = node.getUserObject();

		// find a NewObjectInfo to use
		NewObjectInfo objectInfo = parent.getNewObjectInformationForClass(newClass);
		if (objectInfo != null) {
			MdmiModelTree selectionTree = SelectionManager.getInstance().getEntitySelector();
			try {
				// remember what was expanded
				List<DefaultMutableTreeNode> expandedNodes = selectionTree.getExpandedChildren(parent);
				
				// Create a new child for this parent
				Object newObject = objectInfo.createChildObject();

				// Copy data from old user object to the new one
				ClassUtil.copyData(userObject, newObject);
				
				// Create a new node for this child
				EditableObjectNode newNode = objectInfo.addNewChild(newObject);
				// copy children of old node into new node
				copyChildren(expandedNodes, node, newNode);

				// Remove old node
				int index = parent.getIndex(node);
				parent.deleteChild(node);
				
				// Insert new node at same location (it has same name)
				parent.insert(newNode, index);

				// Replace references to old object with the new one
				updateReferences(node, userObject, newObject);

				// reset expansion indicator since node has been replaced
				int idx = expandedNodes.indexOf(node);
				if (idx != -1) {
					expandedNodes.remove(idx);
					expandedNodes.add(idx, newNode);
				}

				// Fire event
				DefaultTreeModel treeModel = (DefaultTreeModel)selectionTree.getMessageElementsTree().getModel();
				treeModel.nodeStructureChanged(parent);

				// mark model as changed
				SelectionManager.getInstance().setUpdatesPending();

				// re-expand
				selectionTree.expandNodes(expandedNodes);
				
				// if open - close
				SelectionManager.getInstance().getEntityEditor().stopEditing(userObject);

				selectionTree.selectNode(node);
				SelectionManager.getInstance().editItem(newNode);

				return newNode;

			} catch (Exception e) {
				SelectionManager.getInstance().getStatusPanel().writeException(e);
			}

		}
		return null;
	}

	/** Copy children from old node to the new one
	 * @param oldNode
	 * @param newNode
	 */
	private static void copyChildren(List<DefaultMutableTreeNode> expandedNodes,
			EditableObjectNode oldNode, EditableObjectNode newNode) {
		// Copy children from old object to the new one
		for (int c=0; c<oldNode.getChildCount(); c++) {
			if (!(oldNode.getChildAt(c) instanceof EditableObjectNode)) {
				continue;
			}
			
			EditableObjectNode oldChild = (EditableObjectNode)oldNode.getChildAt(c);
			Object childUserObject = oldChild.getUserObject();

			EditableObjectNode newChild = null;

			// if open - close
			SelectionManager.getInstance().getEntityEditor().stopEditing(childUserObject);
			
			// check if there's already the correct child from copying the model
			if (newNode.getChildCount() > c && newNode.getChildAt(c) instanceof EditableObjectNode) {
				newChild = (EditableObjectNode)newNode.getChildAt(c);
				if (newChild.toString().equals(oldChild.toString())) {
					continue;
				}
			}

			// do we know how to add this child to the new node
			NewObjectInfo childInfo = newNode.getNewObjectInformationForClass(childUserObject.getClass());
			if (childInfo != null) {
				// create a new child node for the child user object
				newChild = childInfo.addNewChild(childUserObject);
				// add new child to new node
				newNode.add( newChild );

				// reset expansion indicator since node has been replaced
				int idx = expandedNodes.indexOf(oldChild);
				if (idx != -1) {
					expandedNodes.remove(idx);
					expandedNodes.add(idx, newChild);
				}
			}
		}
	}

	/**
	 * @param node
	 * @param oldObject
	 * @param newObject
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private static void updateReferences(EditableObjectNode node,
			Object oldObject, Object newObject) {

		List<EditableObjectNode> references = SelectionManager.getInstance().getEntitySelector().findReferences(node);
		
		for (EditableObjectNode referringNode : references) {
			Object referringObject = referringNode.getUserObject();
			List<Method[]> getSetPairs = ClassUtil.getMethodPairs(referringObject.getClass(),
					oldObject.getClass());
			if (getSetPairs.size() != 0) {
				// call the getXXX() method on referring object. If it returns the oldObject,
				//  replace with the newObject by calling the setXXX( newObject ) method
				for (Method [] methodPair : getSetPairs) {
					Method getMethod = methodPair[0];
					Method setMethod = methodPair[1];
					try {
						Object objectInChild = getMethod.invoke(referringObject);
						if (oldObject == objectInChild) {
							// replace old with new
							setMethod.invoke(referringObject, newObject);
						}
					}catch (Exception e) {
						String msg = "Unable to update reference to " + node.getDisplayType() + " '" +
							node.toString() + "' in class " + ClassUtil.beautifyName(referringObject.getClass()) +
							", method " + setMethod.getName() + ", object:"  + referringObject.toString();
						SelectionManager.getInstance().getStatusPanel().writeException(msg, e);
					}
				}
			}
		}
	}

	@Override
	public boolean isDataValid() {
		return m_listOfTypes.getSelectedValue() != null;
	}
	
	private class TypeSelectionListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting()) {
				return;
			}
			setDirty(true);
		}
		
	}
	
	private class ClassNameRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			Icon icon = null;
			if (value instanceof Class<?>) {
				Class<?> clazz = (Class<?>)value;
				value = ClassUtil.beautifyName(clazz);
				// use node icon
				icon = TreeNodeIcon.getTreeIcon(clazz);
			}
			Component c = super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			setIcon(icon);
			return c;
		}
	}

}
