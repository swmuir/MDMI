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

import java.util.Collection;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.Field;

public class ComplexDataTypeNode extends DataTypeNode {
	private Collection<NewObjectInfo> m_newObjectInfo = null;

	public ComplexDataTypeNode(DTComplex datatype) {
		super(datatype);
		setNodeIcon(TreeNodeIcon.ComplexDataTypeIcon);
		
		loadChildren(datatype);
	}
	
	private void loadChildren(DTComplex datatype) {
		// Add fields (un-sorted)
		for (Field field : datatype.getFields()) {
			EditableObjectNode modelNode = new FieldNode(field);
			add(modelNode);
		}
	}

	/** children of this node are not sorted */
	@Override
	public boolean showChildrenSorted() {
		return false;
	}

	@Override
	public int moveChildNode(EditableObjectNode childNode, int amt) {
		int oldIdx = getIndex(childNode);
		int newIdx = super.moveChildNode(childNode, amt);
		
		if (newIdx != oldIdx) {
			Field child = (Field)childNode.getUserObject();
			// change model
			DTComplex parentType = (DTComplex)getUserObject();
			parentType.getFields().remove(oldIdx);
			parentType.getFields().add(newIdx, child);
		}
		
		return newIdx;
	}


	@Override
	public void setUserObject(Object userObject) {
		super.setUserObject(userObject);
		
		// find and update corresponding FieldNode(s),
		//  since the FieldNode shows the datatype as part of the name
		MdmiModelTree entitySelector = SelectionManager.getInstance().getEntitySelector();
		DefaultTreeModel model = (DefaultTreeModel)entitySelector.getMessageElementsTree().getModel();
		List <EditableObjectNode> references = entitySelector.findReferences(this);
		for (EditableObjectNode node : references) {
			if (node instanceof FieldNode) {
				if (((Field)node.getUserObject()).getDatatype() == userObject) {
					model.nodeChanged(node);
				}
			}
		}
	}

	@Override
	public boolean isRemovable() {
		return true;
	}


	@Override
	public void deleteChild(MutableTreeNode childNode) {
		// remove from parent model
		Object child = ((DefaultMutableTreeNode)childNode).getUserObject();
		Field field = (Field)child;
		((DTComplex)getUserObject()).getFields().remove(field);
		
		super.remove(childNode);
	}

	
	/** What new items can be created */
	@Override
	public Collection<NewObjectInfo> getNewObjectInformation(boolean changeType) {
		if (m_newObjectInfo == null) {
			m_newObjectInfo = super.getNewObjectInformation(changeType);
			// add a new Field
			m_newObjectInfo.add(new NewField());
		}
		
		return m_newObjectInfo;
	}
	
	///////////////////////////////////////////////////////////////
	//    Custom Classes
	//////////////////////////////////////////////////////////////
	
	public class NewField extends NewObjectInfo {
		@Override
		public Class<?> getChildClass() {
			return Field.class;
		}
		
		@Override
		public EditableObjectNode addNewChild(Object childObject) {
			DTComplex parent = (DTComplex)getUserObject();
			Field field = (Field)childObject;
			
			parent.getFields().add(field);
			field.setOwnerType(parent);
			
			return new FieldNode(field);
		}

		@Override
		public String getChildName(Object childObject) {
			return ((Field)childObject).getName();
		}

		@Override
		public void setChildName(Object childObject, String newName) {
			((Field)childObject).setName(newName);
		}
	}
}
