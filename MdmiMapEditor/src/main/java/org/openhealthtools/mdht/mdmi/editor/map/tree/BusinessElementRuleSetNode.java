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
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementRule;

/** Node for a collection of MdmiBusinessElementRules */
public class BusinessElementRuleSetNode extends EditableObjectNode {
	private static final String s_display = s_res.getString("BusinessElementRuleSetNode.rules");

	private Collection<NewObjectInfo> m_newObjectInfo = null;

	private MdmiBusinessElementReference m_element = null;
	
	public BusinessElementRuleSetNode(MdmiBusinessElementReference elem) {
		super(elem);
		setDisplayType(s_display);
		m_element = elem;
		setNodeIcon(TreeNodeIcon.BusinessElementRuleSetIcon);
		
		loadChildren(elem);
	}

	private void loadChildren(MdmiBusinessElementReference elem) {
		// add BusinessRules
		for (MdmiBusinessElementRule rule : elem.getBusinessRules()) {
			EditableObjectNode ruleNode = new BusinessElementRuleNode(rule);
			addSorted(ruleNode);
		}
	}

	@Override
	public String getDisplayName(Object userObject) {
		return s_display;
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public boolean isRemovable() {
		return false;
	}

	
	/** Delete this child from the tree, and from it's model parent */
	@Override
	public void deleteChild(MutableTreeNode child) {
		// remove rule from biz element
		MdmiBusinessElementRule rel = (MdmiBusinessElementRule)((DefaultMutableTreeNode)child).getUserObject();
		m_element.getBusinessRules().remove(rel);
		
		super.remove(child);
	}
	
	
	/** What new items can be created */
	@Override
	public Collection<NewObjectInfo> getNewObjectInformation(boolean changeType) {
		if (m_newObjectInfo == null) {
			m_newObjectInfo = super.getNewObjectInformation(changeType);
			m_newObjectInfo.add(new NewBusinessElementRule());
		}
		
		return m_newObjectInfo;
	}
	
	///////////////////////////////////////////////////////////////
	//    Custom Classes
	//////////////////////////////////////////////////////////////
	
	public class NewBusinessElementRule extends NewObjectInfo {

		@Override
		public EditableObjectNode addNewChild(Object childObject) {
			MdmiBusinessElementRule bizRule = (MdmiBusinessElementRule)childObject;
			m_element.addBusinessRule(bizRule);
			bizRule.setBusinessElement(m_element);
			
			return new BusinessElementRuleNode(bizRule);
		}

		@Override
		public Class<?> getChildClass() {
			return MdmiBusinessElementRule.class;
		}

		@Override
		public String getChildName(Object childObject) {
			return ((MdmiBusinessElementRule)childObject).getName();
		}

		@Override
		public void setChildName(Object childObject, String newName) {
			((MdmiBusinessElementRule)childObject).setName(newName);
		}
	}

}
