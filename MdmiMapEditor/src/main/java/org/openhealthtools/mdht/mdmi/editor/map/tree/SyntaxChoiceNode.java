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

import org.openhealthtools.mdht.mdmi.editor.map.ModelChangeEvent;
import org.openhealthtools.mdht.mdmi.editor.map.editor.AbstractComponentEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.DataEntryFieldInfo;
import org.openhealthtools.mdht.mdmi.editor.map.editor.DefaultTextField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.IEditorField;
import org.openhealthtools.mdht.mdmi.model.Bag;
import org.openhealthtools.mdht.mdmi.model.Choice;
import org.openhealthtools.mdht.mdmi.model.LeafSyntaxTranslator;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.Node;

public class SyntaxChoiceNode extends SyntaxNodeNode {
	private Collection<NewObjectInfo> m_newObjectInfo = null;

	public SyntaxChoiceNode(Choice choice) {
		super(choice);
		setNodeIcon(TreeNodeIcon.SyntaxChoiceIcon);
		loadChildren(choice);
	}

	private void loadChildren(Choice choice) {
		for (Node child : choice.getNodes()) {
			addSorted(createSyntaxNode(child));
		}	
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
		Node model = (Node)((DefaultMutableTreeNode)child).getUserObject();
		((Choice)getUserObject()).getNodes().remove(model);
		
		super.remove(child);
	}

	/** Can a node be dragged from its current position and dropped into this node */
	@Override
	public boolean canDrop(EditableObjectNode newChild) {
		return (newChild.getUserObject() instanceof Node);
	}	
	
	@Override
	public AbstractComponentEditor getEditorForNode() {
		return new CustomEditor(getMessageGroup(), getUserObject().getClass());
	}

	@Override
	public String getConstraintExpressionLanguage() {
		String language = ((Choice)userObject).getConstraintExpressionLanguage();
		if (language == null || language.length() == 0) {
			language = getDefaultConstraintExpressionLanguage();
		}
		return language;
	}

	public String getDefaultConstraintExpressionLanguage() {
		String language = super.getConstraintExpressionLanguage();
		return language;
	}


	/** What new items can be created */
	@Override
	public Collection<NewObjectInfo> getNewObjectInformation(boolean changeType) {
		if (m_newObjectInfo == null) {
			m_newObjectInfo = super.getNewObjectInformation(changeType);
			m_newObjectInfo.add(new NewBag());
			m_newObjectInfo.add(new NewChoice());
			m_newObjectInfo.add(new NewLeaf());
		}

		return m_newObjectInfo;
	}
 
	//////////////////////////////////////////////////////////////////
	//    Custom Classes
	//////////////////////////////////////////////////////////////
	public class CustomEditor extends SyntaxNodeNode.CustomEditor {
		private DefaultTextField m_constraintLanguageField;
		
		public CustomEditor(MessageGroup group, Class<?> objectClass) {
			super(group, objectClass);
		}
		
		/** Use a custom field for language */
		@Override
		protected IEditorField createEditorField(DataEntryFieldInfo fieldInfo) {
			if ("ConstraintExpressionLanguage".equalsIgnoreCase(fieldInfo.getFieldName())) {
				// Use a special text field
				m_constraintLanguageField = new DefaultTextField(this,
						getDefaultConstraintExpressionLanguage());
				return m_constraintLanguageField;
			}
			return super.createEditorField(fieldInfo);
		}


		@Override
		public void modelChanged(ModelChangeEvent e) {
			if (e.getSource() != getUserObject()) {
				// Could be a change to the language - update value
				String defaultLanguage = getDefaultConstraintExpressionLanguage();
				m_constraintLanguageField.setDefaultValue(defaultLanguage);
			}
			super.modelChanged(e);
		}

	}

	public class NewBag extends AbstractNewBag {
		@Override
		public void addNode(Bag node) {
			Choice parent = (Choice)getUserObject();
			parent.addNode(node);
			node.setParentNode(parent);
		}
	}
	
	public class NewChoice extends AbstractNewChoice {
		@Override
		public void addNode(Choice node) {
			Choice parent = (Choice)getUserObject();
			parent.addNode(node);
			node.setParentNode(parent);
		}
	}
	
	public class NewLeaf extends AbstractNewLeaf {
		@Override
		public void addNode(LeafSyntaxTranslator node) {
			Choice parent = (Choice)getUserObject();
			parent.addNode(node);
			node.setParentNode(parent);
		}
	}

}
