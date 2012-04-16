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

import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDomainDictionaryReference;

public class DomainDictionaryReferenceNode extends EditableObjectNode {
	private Collection<NewObjectInfo> m_newObjectInfo = null;

	public DomainDictionaryReferenceNode(MdmiDomainDictionaryReference dictionary) {
		super(dictionary);
		setNodeIcon(TreeNodeIcon.DomainDictionaryReferenceIcon);
		
		loadChildren(dictionary);
	}
	
	private void loadChildren(MdmiDomainDictionaryReference dictionary) {
		Collection<MdmiBusinessElementReference> bizElems = dictionary.getBusinessElements();
		for (MdmiBusinessElementReference bizElemRef : bizElems) {
			EditableObjectNode bizElemNode = new BusinessElementReferenceNode(bizElemRef);
			addSorted(bizElemNode);
		}
	}

	@Override
	public String getDisplayName(Object userObject) {
		return s_res.getString("DomainDictionaryReferenceNode.displayName");
	}

	@Override
	public String getToolTipText() {
		return ((MdmiDomainDictionaryReference)getUserObject()).getDescription();
	}


	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public boolean isRemovable() {
		return false;
	}


	@Override
	public void deleteChild(MutableTreeNode child) {
		// remove from parent model
		MdmiBusinessElementReference model = (MdmiBusinessElementReference)((DefaultMutableTreeNode)child).getUserObject();
		((MdmiDomainDictionaryReference)getUserObject()).getBusinessElements().remove(model);
		
		super.remove(child);
	}


	/** What new items can be created */
	@Override
	public Collection<NewObjectInfo> getNewObjectInformation(boolean changeType) {
		if (m_newObjectInfo == null) {
			m_newObjectInfo = super.getNewObjectInformation(changeType);
			m_newObjectInfo.add(new NewBusinessElementRef());
		}

		return m_newObjectInfo;
	}

	//////////////////////////////////////////////////////////////////
	//    Custom Classes
	//////////////////////////////////////////////////////////////

	public class NewBusinessElementRef extends NewObjectInfo {
		public NewBusinessElementRef() {
			super(s_res.getString("DomainDictionaryReferenceNode.businessRef"));
		}

		@Override
		public EditableObjectNode addNewChild(Object childObject) {
			MdmiDomainDictionaryReference parent = (MdmiDomainDictionaryReference)getUserObject();
			MdmiBusinessElementReference element = 
				(MdmiBusinessElementReference)childObject;
			parent.addBusinessElement(element);
			element.setDomainDictionaryReference(parent);

			BusinessElementReferenceNode treeNode = new BusinessElementReferenceNode(element);
			return treeNode;
		}

		@Override
		public Class<?> getChildClass() {
			return MdmiBusinessElementReference.class;
		}

		@Override
		public String getChildName(Object childObject) {
			return ((MdmiBusinessElementReference)childObject).getName();
		}

		@Override
		public void setChildName(Object childObject, String newName) {
			((MdmiBusinessElementReference)childObject).setName(newName);
		}
	}
}
