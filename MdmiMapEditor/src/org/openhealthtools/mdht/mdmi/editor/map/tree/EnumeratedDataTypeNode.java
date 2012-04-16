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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.openhealthtools.mdht.mdmi.model.DTSEnumerated;
import org.openhealthtools.mdht.mdmi.model.EnumerationLiteral;

public class EnumeratedDataTypeNode extends DataTypeNode {
	private Collection<NewObjectInfo> m_newObjectInfo = null;

	public EnumeratedDataTypeNode(DTSEnumerated datatype) {
		super(datatype);
		
		loadChildren(datatype);
	}
	
	private void loadChildren(DTSEnumerated datatype) {
		// Add enumerations
		for (EnumerationLiteral literal : datatype.getLiterals()) {
			EditableObjectNode modelNode = new EnumerationNode(literal);
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
			EnumerationLiteral child = (EnumerationLiteral)childNode.getUserObject();
			// change model
			DTSEnumerated parentType = (DTSEnumerated)getUserObject();
			parentType.getLiterals().remove(oldIdx);
			parentType.getLiterals().add(newIdx, child);
		}
		
		return newIdx;
	}

	@Override
	public boolean isRemovable() {
		return true;
	}


	@Override
	public void deleteChild(MutableTreeNode childNode) {
		// remove from parent model
		Object child = ((DefaultMutableTreeNode)childNode).getUserObject();
		EnumerationLiteral literal = (EnumerationLiteral)child;
		((DTSEnumerated)getUserObject()).getLiterals().remove(literal);

		super.remove(childNode);
	}

	
	/** What new items can be created */
	@Override
	public Collection<NewObjectInfo> getNewObjectInformation(boolean changeType) {
		if (m_newObjectInfo == null) {
			m_newObjectInfo = super.getNewObjectInformation(changeType);
			// add new EnumerationLiterals
			m_newObjectInfo.add(new NewEnumeration());
		}
		
		return m_newObjectInfo;
	}
	
	///////////////////////////////////////////////////////////////
	//    Custom Classes
	//////////////////////////////////////////////////////////////
	
	public class NewEnumeration extends NewObjectInfo {
		@Override
		public Class<?> getChildClass() {
			return EnumerationLiteral.class;
		}
		
		@Override
		public EditableObjectNode addNewChild(Object childObject) {
			DTSEnumerated parent = (DTSEnumerated)getUserObject();
			EnumerationLiteral literal = (EnumerationLiteral)childObject;
			
			parent.addLiteral(literal);
			
			return new EnumerationNode(literal);
		}

		@Override
		public String getChildName(Object childObject) {
			return ((EnumerationLiteral)childObject).getName();
		}

		@Override
		public void setChildName(Object childObject, String newName) {
			((EnumerationLiteral)childObject).setName(newName);
		}
	}
}
