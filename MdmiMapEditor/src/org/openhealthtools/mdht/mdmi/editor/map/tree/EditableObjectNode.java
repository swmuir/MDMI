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

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.editor.AbstractComponentEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.GenericEditor;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;

@SuppressWarnings("serial")
public abstract class EditableObjectNode extends DefaultMutableTreeNode {

	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tree.Local");
	
	private String m_displayName = "";
	private String m_displayType = null;
	
	private boolean m_objectHasError = false;
	private boolean m_imported = false;

	private Icon  m_icon = null;

	public EditableObjectNode(Object userObject) {
		super(userObject);
		setDisplayName(getDisplayName(userObject));
		setDisplayType(ClassUtil.beautifyName(userObject.getClass()));
	}

	@Override
	public void setUserObject(Object userObject) {
		super.setUserObject(userObject);
		setDisplayName(getDisplayName(userObject));
	}

	public abstract String getDisplayName(Object userObject);
	
	/** Define the string to be shown on the display */
	public void setDisplayName(String displayName) {
		if (displayName == null || displayName.length() == 0) {
			displayName = s_res.getString("EditableObjectNode.unNamedNodeDisplay");
		}
		m_displayName = displayName;
	}
	
	public String getDisplayName() {
		return m_displayName;
	}

	@Override
	public String toString() {
		String str = getDisplayName();
		return str != null ? str : "";
	}
	
	/** set the icon associated with this node type. A value
	 * of null indicates that the default tree icon is to be used.
	 */
	public void setNodeIcon(Icon icon) {
		m_icon = icon;
	}
	
	/** return an icon associated with this node type. A value
	 * of null indicates that the default tree icon is to be used.
	 */
	public Icon getNodeIcon() {
		Icon icon = m_icon;
		if (hasError() && m_icon != null) {
			icon = new ErrorIcon(m_icon);
			// adjust opacity if error is one of our child's, and not ours
			if (!m_objectHasError) {
				// error is in child
				Color color = ((ErrorIcon)icon).getErrorColor();
				// make new color transparent
				int alpha = 112;	// 0(clear) < alpha < 255(opaque)
				Color newColor = new Color(color.getRed(), color.getGreen(), 
						color.getBlue(), alpha);
				((ErrorIcon)icon).setErrorColor(newColor);
			}
		}
		
		if (icon != null && isImported()) {
			icon = new ImportedIcon(icon);
		}
		return icon;
	}
	
	/** set display type (which is also the tool tip) */
	public void setDisplayType(String text) {
		m_displayType = text;
	}

	/** get the display type (i.e. Message Model) */
	public String getDisplayType() {
		return m_displayType;
	}
	
	/** return tool tip text */
	public String getToolTipText() {
		return null;
	}
	
	/** Is there an error in the user object (or one of its children) */
	public boolean hasError() {
		if (childHasError()) {
			return true;
		}
		return m_objectHasError;
	}
	
	/** Indicate that there is an error in the user object */
	public void setHasError(boolean error) {
		m_objectHasError = error;
	}
	

	/** Does any child have and error */
	private boolean childHasError() {
		// return true if any child has an error
		for (int i=0; i<getChildCount(); i++) {
			if (getChildAt(i) instanceof EditableObjectNode &&
					((EditableObjectNode)getChildAt(i)).hasError()) {
				return true;
			}
		}
		return false;
	}
	
	// indicate object was imported (currently only applies to datatypes and business element refs
	public boolean isImported() {
		return m_imported;
	}
	
	public void setImported(boolean imported) {
		m_imported = imported;
		// set children too
		for (int i=0; i<getChildCount(); i++) {
			if (getChildAt(i) instanceof EditableObjectNode) {
				((EditableObjectNode)getChildAt(i)).setImported(imported);
			}
		}
	}
	
	////////////// Language //////////////////////////////////////////////
	
	/** Get the Location Expression Language from this node. If not defined, get from parent */
	public String getLocationExpressionLanguage() {
		String language = null;
		// get from parent
		TreeNode parentNode = this.getParent();
		if (parentNode instanceof EditableObjectNode) {
			language = ((EditableObjectNode)parentNode).getLocationExpressionLanguage();
		}
		return language;
	}
	
	public String getConstraintExpressionLanguage() {
		String language = null;
		// get from parent
		TreeNode parentNode = this.getParent();
		if (parentNode instanceof EditableObjectNode) {
			language = ((EditableObjectNode)parentNode).getConstraintExpressionLanguage();
		}
		return language;
	}
	
	public String getRulesExpressionLanguage() {
		String language = null;
		// get from parent
		TreeNode parentNode = this.getParent();
		if (parentNode instanceof EditableObjectNode) {
			language = ((EditableObjectNode)parentNode).getRulesExpressionLanguage();
		}
		return language;
	}
	
	public String getFormatExpressionLanguage() {
		String language = null;
		// get from parent
		TreeNode parentNode = this.getParent();
		if (parentNode instanceof EditableObjectNode) {
			language = ((EditableObjectNode)parentNode).getFormatExpressionLanguage();
		}
		return language;
	}
	
	////////////////////////////////////////////////////////
	
	/** Is this object editable */
	public boolean isEditable() {
		return false;
	}
	
	/** Can this object be deleted */
	public boolean isRemovable() {
		return false;
	}

	/** Can this object be copied */
	public boolean isCopyable() {
		return isRemovable();
	}
	
	/** Can this object's type be changed */
	public boolean canChangeType() {
		return ChangeTypeDialog.canChangeType(this);
	}
	
	/** Delete this child from the tree, and from it's model parent */
	public void deleteChild(MutableTreeNode child) {
		super.remove(child);
	}
	
	/** Can this childObject be pasted into this object*/
	public boolean canPaste(EditableObjectNode childNode) {
		if (childNode == null) {
			return false;
		}
		
		Object childObject = childNode.getUserObject();
		if (childObject == null) {
			return false;
		}
		
		// check if we can create a new one of this type - if so, we'll assume
		// we can also paste
		if (getNewObjectInformationForClass(childObject.getClass()) != null) {
			return true;
		}
		return false;
	}
	
	/** Can a node be dragged from its current position and dropped into this node */
	public boolean canDrop(EditableObjectNode newChild) {
		return false;
	}
	
	/** drop a new child into this node */
	public void reParent(EditableObjectNode child) {
		Object childObject = child.getUserObject();
		// remove from old parent model and tree node
		// add to new parent model and tree node
		
		NewObjectInfo newObjectInfo = getNewObjectInformationForClass(childObject.getClass());
		if (newObjectInfo != null) {
			// remove from old parent (tree and model)
			if (child.getParent() instanceof EditableObjectNode) {
				((EditableObjectNode)child.getParent()).deleteChild(child);
			}

			// rename if necessary
			newObjectInfo.giveChildUniqueName(this, childObject);
			
			// add to parent model
			newObjectInfo.addNewChild(childObject);
			
			// add to this node
			if (showChildrenSorted()) {
				addSorted(child);
			} else {
				// add to end, then move up to position 0
				// (this forces the model to be updated as well)
				add(child);
				int idx = getIndex(child);
				if (idx > 0) {
					moveChildNode(child, -idx);
				}
			}
		}
	}
	
	/** Can a new child of only one type be created */
	public boolean canCreateNewChild() {
		Collection<NewObjectInfo> newOperations = getNewObjectInformation(false);
		// need one, and only one, child
		if (newOperations.size() == 1) {
			return true;
		}
		return false;
	}
	
	public EditableObjectNode paste(Object childObject) {
		EditableObjectNode node = null;
		// figure out how to paste
		NewObjectInfo newObjectInfo = getNewObjectInformationForClass(childObject.getClass());
		if (newObjectInfo != null) {
			// rename if necessary
			newObjectInfo.giveChildUniqueName(this, childObject);
			// add it
			node = newObjectInfo.addNewChild(childObject);
		}
		return node;
	}
	
	/** What new items can be created.
	 * 
	 * @param changeType	show information for changing the object type
	 * @return
	 */
	public Collection<NewObjectInfo> getNewObjectInformation(boolean changeType) {
		return new ArrayList<NewObjectInfo>();
	}
	
	/** return a NewObjectInfo for the provided class */
	public NewObjectInfo getNewObjectInformationForClass(Class <?> clazz) {
		Collection<NewObjectInfo> newObjects = getNewObjectInformation(true);
		if (newObjects == null) {
			return null;
		}
		
		for (NewObjectInfo newObjectInfo : newObjects) {
			Class<?> childClass = newObjectInfo.getChildClass();

			if (childClass.equals(clazz)) {
				return newObjectInfo;
			}
		}
		return null;

	}
	
	/** get the editor */
	public AbstractComponentEditor getEditorForNode() {
		return new GenericEditor(getMessageGroup(), getUserObject().getClass());
	}

	/** Get the MessageGroup at the top of the tree */
	public MessageGroup getMessageGroup() {
		if (this instanceof MessageGroupNode) {
			return (MessageGroup)getUserObject();
		}
		// check parent
		if (getParent() instanceof EditableObjectNode) {
			EditableObjectNode parent = (EditableObjectNode)getParent();
			return parent.getMessageGroup();
		}
		
		return null;
	}


	/** Should the children of this node be sorted */
	public boolean showChildrenSorted() {
		return true;
	}
	
	/** Can this node be moved up relative to its siblings */
	public boolean canMoveUp() {
		if (getParent() instanceof EditableObjectNode) {
			EditableObjectNode parent = (EditableObjectNode)getParent();
			if (!parent.showChildrenSorted()) {
				int idx = parent.getIndex(this);
				// not first
				return idx > 0;
			}
		}
		return false;
	}

	
	/** Can this node be moved down relative to its siblings */
	public boolean canMoveDown() {
		if (getParent() instanceof EditableObjectNode) {
			EditableObjectNode parent = (EditableObjectNode)getParent();
			if (!parent.showChildrenSorted()) {
				int idx = parent.getIndex(this);
				// not last
				return idx < parent.getChildCount()-1;
			}
		}
		return false;
	}
	
	/** Move child node up or down by the given amount. For example, an amount of -1 will move
	 * the node up one position in its parent.
	 * @return	the new index of the node
	 */
	public int moveChildNode(EditableObjectNode childNode, int amt) {
		int childIdx = getIndex(childNode);
		
		int newIdx = childIdx + amt;
		if (newIdx >= 0 && newIdx < getChildCount()) {

			remove(childIdx);
			insert(childNode, newIdx);

			return newIdx;
		}
		return childIdx;
	}
	
	public List<JComponent> getAdditionalPopuMenus() {
		return null;
	}

	
	/** Add the child node to this parent, maintaining a sorted order if necessar
	 * the node will be added at position i, such that childNode < node[i]
	 * @param childNode
	 * @return	returns the highest node in the tree that was added
	 */
	public DefaultMutableTreeNode addSorted(DefaultMutableTreeNode childNode) {
		if (showChildrenSorted()) {
			MdmiModelTree.addSorted(this, childNode);
		} else {
			add(childNode);
		}
		return childNode;
	}
	
	/** Make a copy of the user object 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws IllegalArgumentException */
	public Object copyUserObject() throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Object userObjectCopy = ClassUtil.clone(getUserObject());
		return userObjectCopy;
	}
	
	/** Make a copy of this node. A copy will be made of the user object, as will all of the
	 * child nodes, and their user objects
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 */
	public EditableObjectNode copyNode() throws IllegalArgumentException, InstantiationException, 
				IllegalAccessException, InvocationTargetException, SecurityException, 
				NoSuchMethodException {
		
		Object userObjectCopy = copyUserObject();

		// create a new node for the new user object
		EditableObjectNode copy = newInstance(userObjectCopy);

		// copy children nodes, which will copy the model's children as well
		copyChildren(this, copy);

		return copy;
	}
	
	/** Create a new Node of the same type, for the provided user object 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws IllegalArgumentException */
	private EditableObjectNode newInstance(Object userObject) 
			throws IllegalArgumentException, InstantiationException, 
			IllegalAccessException, InvocationTargetException {
		
		Class<?> objectClass = userObject.getClass();
		Constructor<?> ctor = null;
		while (ctor == null) {
			try {
				ctor = getClass().getConstructor(objectClass);
			} catch (NoSuchMethodException e) {
				// try superclass
				objectClass = objectClass.getSuperclass();
			}
		}

		EditableObjectNode copy = 
			(EditableObjectNode)ctor.newInstance(userObject);
		
		return copy;
	}
	
	/** Recursively copy all children in the source node to the target node 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchMethodException 
	 * @throws SecurityException */
	private void copyChildren(EditableObjectNode sourceNode, EditableObjectNode targetNode)
				throws IllegalArgumentException, InstantiationException, 
						IllegalAccessException, InvocationTargetException, 
						SecurityException, NoSuchMethodException {
		for (int i=0; i<sourceNode.getChildCount(); i++) {
			if (!(sourceNode.getChildAt(i) instanceof EditableObjectNode)) {
				continue;
			}
			
			EditableObjectNode sourceChild = (EditableObjectNode)sourceNode.getChildAt(i);
			Object sourceChildUserObj = sourceChild.getUserObject();
			String childDisplayType = sourceChild.getDisplayType();

			EditableObjectNode targetChild = null;
			Object targetChildUserObj = null;
			// If there's already a child at this position, we need to investigate
			if (targetNode.getChildCount() > i &&
					targetNode.getChildAt(i) instanceof EditableObjectNode &&
					(((EditableObjectNode)targetNode.getChildAt(i)).getDisplayType().equals(childDisplayType))) {
				targetChild = (EditableObjectNode)targetNode.getChildAt(i);
				// This can happen for two reasons.
				// If it has the same user object as the source child, it was a result of a
				//   shallow copy of an array, which we need to correct
				// Otherwise, it could be a "set" node that was created as part of the node's 
				//   creation - we want to leave these
				targetChildUserObj = targetChild.getUserObject();
				if (sourceChildUserObj == targetChildUserObj) {
					// shallow copy side effect - discard this node
					targetNode.remove(i);
					targetChild = null;
				}
			
			}
			
			if (targetChild == null) {
				// clone the user object and add it using the
				// target node's NewObjectInfo method
				targetChildUserObj = sourceChild.copyUserObject();

				// use newObjectInfo to add the new userObject to the model
				NewObjectInfo newObjectInfo =
					targetNode.getNewObjectInformationForClass(sourceChildUserObj.getClass());
				if (newObjectInfo == null) {
					// just create a node of the specific type
					targetChild = sourceChild.newInstance(targetChildUserObj);
				} else {
					targetChild = newObjectInfo.addNewChild(targetChildUserObj);
				}

				// add child node to target (maintain the given order)
				targetNode.insert(targetChild, i);
			}
			
			// recurse through this node's children
			if (!sourceChild.isLeaf()) {
				copyChildren(sourceChild, targetChild);
			}
			
		}
		
	}
}
