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
import javax.swing.tree.TreeNode;

import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MdmiDomainDictionaryReference;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.MessageModel;
import org.openhealthtools.mdht.mdmi.model.MessageSyntaxModel;
import org.openhealthtools.mdht.mdmi.model.SemanticElementSet;

public class MessageGroupNode extends EditableObjectNode {
	private Collection<NewObjectInfo> m_newObjectInfo = null;

	public MessageGroupNode(MessageGroup group) {
		super(group);
		setNodeIcon(TreeNodeIcon.MessageGroupIcon);
		
		loadChildren(group);
	}
	
	private void loadChildren(MessageGroup group) {
		// Data Types
		DefaultMutableTreeNode dataTypesNode = new DataTypeSetNode(group);
		add(dataTypesNode);
		
		// Message Models
		Collection<MessageModel> models = group.getModels();
		if (models.size() == 0) {
			MessageModel blankMessageModel = createEmptyMessageModel(group);
			models.add(blankMessageModel);
		}
		
		for (MessageModel model : models) {
			EditableObjectNode modelNode = new MessageModelNode(model);
			addSorted(modelNode);
		}
		
		// Domain Dictionary
		MdmiDomainDictionaryReference dictionary = group.getDomainDictionary();
		if (dictionary == null) {
			// create one if it doesn't exist
			dictionary = new MdmiDomainDictionaryReference();
			group.setDomainDictionary(dictionary);
			dictionary.setMessageGroup(group);
		}
		EditableObjectNode dictionaryNode = new DomainDictionaryReferenceNode(dictionary);
		add(dictionaryNode);
	}

	/** Create a MessageModel with no name, and with an un-named SemanticElementSet
	 * and un-named MessageSyntaxModel
	 * @param group
	 * @return
	 */
	private MessageModel createEmptyMessageModel(MessageGroup group) {
		// add a blank message model
		MessageModel blankMessageModel = new MessageModel();
		blankMessageModel.setMessageModelName("");
		blankMessageModel.setGroup(group);
		
		// add a blank semantic element set
		SemanticElementSet blankSet = new SemanticElementSet();
		blankSet.setName("");
		blankSet.setModel(blankMessageModel);
		blankMessageModel.setElementSet(blankSet);
		
		// add a blank syntax model
		MessageSyntaxModel blankSyntaxModel = new MessageSyntaxModel();
		blankSyntaxModel.setName("");
		blankSyntaxModel.setElementSet(blankSet);
		blankSyntaxModel.setModel(blankMessageModel);
		blankMessageModel.setSyntaxModel(blankSyntaxModel);
		return blankMessageModel;
	}
	
	/** Add a new datatype */
	public void addDatatype(MdmiDatatype datatype) {
		// find DatatypeSet
		for (int i=0; i<getChildCount(); i++) {
			TreeNode nodeAt_i = getChildAt(i);
			if (nodeAt_i instanceof DataTypeSetNode) {
				DataTypeSetNode setNode = (DataTypeSetNode)nodeAt_i;
				NewObjectInfo info = setNode.getNewObjectInformationForClass(datatype.getClass());
				if (info == null) {
					SelectionManager.getInstance().getStatusPanel().writeErrorText("Unable to add a " 
							+ datatype.getClass().getName() + " datatype");
				} else {
					EditableObjectNode childNode = info.addNewChild(datatype);
					SelectionManager.getInstance().getEntitySelector().insertNewNode(setNode, childNode);
				}
				return;
			}
		}
	}

	
	/** Add the child node to this parent, maintaining a sorted order - the node will be added at
	 * position i, such that childNode < node[i]
	 * @param childNode
	 * @return	returns the highest node in the tree that was added
	 */
	@Override
	public DefaultMutableTreeNode addSorted(DefaultMutableTreeNode childNode) {
		if (!(childNode instanceof MessageModelNode)) {
			return super.addSorted(childNode);
		}
		
		int idx = getChildCount();
		String childName = childNode.toString();
		for (int i=0; i<getChildCount(); i++) {
			TreeNode nodeAt_i = getChildAt(i);
			// add Message Models after DataTypes, and before Domain Dictionary
			if (nodeAt_i instanceof DataTypeSetNode) {
				continue;
			} else if (nodeAt_i instanceof DomainDictionaryReferenceNode) {
				idx = i;
				break;
			}
			
			String nodeName = nodeAt_i.toString();
			if (childName.compareToIgnoreCase(nodeName) < 0) {
				idx = i;
				break;
			}
		}
		
		insert(childNode, idx);
		return childNode;
	}
	
	@Override
	public String getDisplayName(Object userObject) {
		return ((MessageGroup)userObject).getName();
	}

	@Override
	public String getToolTipText() {
		return ((MessageGroup)getUserObject()).getDescription();
	}


	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public boolean isRemovable() {
		return true;
	}

	@Override
	public void deleteChild(MutableTreeNode child) {
		// remove from parent model
		MessageModel model = (MessageModel)((DefaultMutableTreeNode)child).getUserObject();
		((MessageGroup)getUserObject()).getModels().remove(model);
		
		super.remove(child);
	}

	
	@Override
	public String getConstraintExpressionLanguage() {
		return ((MessageGroup)userObject).getDefaultConstraintExprLang();
	}

	@Override
	public String getFormatExpressionLanguage() {
//		return ((MessageGroup)userObject).getFormatExprLang();
		return null;	// not yet implemented
	}

	@Override
	public String getLocationExpressionLanguage() {
		return ((MessageGroup)userObject).getDefaultLocationExprLang();
	}

	@Override
	public String getRulesExpressionLanguage() {
		return ((MessageGroup)userObject).getDefaultRuleExprLang();
	}

	/** What new items can be created */
	@Override
	public Collection<NewObjectInfo> getNewObjectInformation(boolean changeType) {
		if (m_newObjectInfo == null) {
			m_newObjectInfo = super.getNewObjectInformation(changeType);
			m_newObjectInfo.add(new NewMessageModel());
		}
		
		return m_newObjectInfo;
	}
	
	///////////////////////////////////////////////////////////////
	//    Custom Classes
	//////////////////////////////////////////////////////////////
	
	public class NewMessageModel extends NewObjectInfo {
		
		@Override
		public Class<?> getChildClass() {
			return MessageModel.class;
		}
		
		@Override
		public EditableObjectNode addNewChild(Object childObject) {
			MessageGroup parent = (MessageGroup)getUserObject();
			MessageModel model = (MessageModel)childObject;

			parent.addModel(model);
			model.setGroup(parent);
			
			return new MessageModelNode(model);
		}

		@Override
		public String getChildName(Object childObject) {
			return ((MessageModel)childObject).getMessageModelName();
		}

		@Override
		public void setChildName(Object childObject, String newName) {
			((MessageModel)childObject).setMessageModelName(newName);
		}
	}
}
