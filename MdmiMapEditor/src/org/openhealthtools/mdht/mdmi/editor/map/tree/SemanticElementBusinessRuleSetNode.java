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

import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.SemanticElementBusinessRule;

/** Node for a collection of SemanticElementBusinessRules */
public class SemanticElementBusinessRuleSetNode extends EditableObjectNode {
	private static final String s_display = s_res.getString("SemanticElementBusinessRuleSetNode.rules");

	private Collection<NewObjectInfo> m_newObjectInfo = null;

	private SemanticElement m_element = null;
	
	public SemanticElementBusinessRuleSetNode(SemanticElement elem) {
		super(elem);
		setDisplayType(s_display);
		m_element = elem;
		setNodeIcon(TreeNodeIcon.SemanticElementBusinessRuleSetIcon);
		
		loadChildren(elem);
	}

	private void loadChildren(SemanticElement elem) {
		// add BusinessRules
		for (SemanticElementBusinessRule rule : elem.getBusinessRules()) {
			EditableObjectNode relNode = new SemanticElementBusinessRuleNode(rule);
			addSorted(relNode);
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
		// remove rule from semantic element
		SemanticElementBusinessRule rel = (SemanticElementBusinessRule)((DefaultMutableTreeNode)child).getUserObject();
		m_element.getBusinessRules().remove(rel);
		
		super.remove(child);
	}
	
	
	/** What new items can be created */
	@Override
	public Collection<NewObjectInfo> getNewObjectInformation(boolean changeType) {
		if (m_newObjectInfo == null) {
			m_newObjectInfo = super.getNewObjectInformation(changeType);
			m_newObjectInfo.add(new NewSemanticElementBusinessRule());
		}
		
		return m_newObjectInfo;
	}
	
	///////////////////////////////////////////////////////////////
	//    Custom Classes
	//////////////////////////////////////////////////////////////
	
	public class NewSemanticElementBusinessRule extends NewObjectInfo {

		@Override
		public EditableObjectNode addNewChild(Object childObject) {
			SemanticElementBusinessRule rel = (SemanticElementBusinessRule)childObject;
			m_element.addBusinessRule(rel);
			rel.setSemanticElement(m_element);
			
			return new SemanticElementBusinessRuleNode(rel);
		}

		@Override
		public Class<?> getChildClass() {
			return SemanticElementBusinessRule.class;
		}

		@Override
		public String getChildName(Object childObject) {
			return ((SemanticElementBusinessRule)childObject).getName();
		}

		@Override
		public void setChildName(Object childObject, String newName) {
			((SemanticElementBusinessRule)childObject).setName(newName);
		}
	}

}
