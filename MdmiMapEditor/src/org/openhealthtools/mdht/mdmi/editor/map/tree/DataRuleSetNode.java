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

import org.openhealthtools.mdht.mdmi.model.DataRule;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;

/** Node for a collection of DataRules */
public class DataRuleSetNode extends EditableObjectNode {
	private static final String s_display = s_res.getString("DataRuleSetNode.dataRules");

	private Collection<NewObjectInfo> m_newObjectInfo = null;
	
	public DataRuleSetNode(SemanticElement semanticElement) {
		super(semanticElement);
		setDisplayType(s_display);
		setNodeIcon(TreeNodeIcon.DataRuleSetIcon);
		
		loadChildren(semanticElement);
	}
	
	private void loadChildren(SemanticElement semanticElement) {
		for (DataRule dataRule : semanticElement.getDataRules()) {
			DataRuleNode dataRuleNode = new DataRuleNode(dataRule);
			addSorted(dataRuleNode);
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

	@Override
	public boolean canDrop(EditableObjectNode newChild) {
		// can drop a DataRule
		return (newChild.getUserObject() instanceof DataRule);
	}

	@Override
	public void deleteChild(MutableTreeNode child) {
		// remove from parent semantic element
		SemanticElement semanticElement = (SemanticElement)getUserObject();
		DataRule dataRule = (DataRule)((DefaultMutableTreeNode)child).getUserObject();
		semanticElement.getDataRules().remove(dataRule);
		
		// remove from message group too
		MessageGroup group = getMessageGroup();
		group.getDataRules().remove(dataRule);
		
		super.remove(child);
	}

	/** What new items can be created */
	@Override
	public Collection<NewObjectInfo> getNewObjectInformation(boolean changeType) {
		if (m_newObjectInfo == null) {
			m_newObjectInfo = super.getNewObjectInformation(changeType);
			m_newObjectInfo.add(new NewDataRule());
		}
		
		return m_newObjectInfo;
	}
	

	///////////////////////////////////////////////////////////////
	//    Custom Classes
	//////////////////////////////////////////////////////////////
	
	public class NewDataRule extends NewObjectInfo {
		public NewDataRule() {
		}

		@Override
		public EditableObjectNode addNewChild(Object childObject) {
			SemanticElement semanticElement = (SemanticElement)getUserObject();
			DataRule dataRule = (DataRule)childObject;
			MessageGroup group = getMessageGroup();
			
			// add to semantic element
			semanticElement.addDataRule(dataRule);
			dataRule.setSemanticElement(semanticElement);
			
			// add to message group
			group.addDataRule(dataRule);
			dataRule.setScope( group );
			
			EditableObjectNode treeNode = new DataRuleNode(dataRule);
			return treeNode;
		}

		@Override
		public Class<?> getChildClass() {
			return DataRule.class;
		}

		@Override
		public String getChildName(Object childObject) {
			return ((DataRule)childObject).getName();
		}

		@Override
		public void setChildName(Object childObject, String newName) {
			((DataRule)childObject).setName(newName);
		}
	}
}
