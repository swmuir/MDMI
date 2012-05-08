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

import org.openhealthtools.mdht.mdmi.editor.map.editor.AbstractComponentEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.GenericEditor;
import org.openhealthtools.mdht.mdmi.model.MessageModel;
import org.openhealthtools.mdht.mdmi.model.MessageSyntaxModel;
import org.openhealthtools.mdht.mdmi.model.SemanticElementSet;

public class MessageModelNode extends EditableObjectNode {

	public MessageModelNode(MessageModel model) {
		super(model);
		setNodeIcon(TreeNodeIcon.MessageModelIcon);
		
		loadChildren(model);
	}
	
	protected void loadChildren(MessageModel model) {
		
		// Semantic Element Set
		SemanticElementSet elemSet = model.getElementSet();
		if (elemSet == null) {
			elemSet = new SemanticElementSet();
			model.setElementSet(elemSet);
		}
		EditableObjectNode elemSetNode = new SemanticElementSetNode(elemSet);
		add(elemSetNode);
		
		// Message Syntax Model
		MessageSyntaxModel syntaxModel = model.getSyntaxModel();
		if (syntaxModel == null) {
			syntaxModel = new MessageSyntaxModel();
			model.setSyntaxModel(syntaxModel);
		}
		EditableObjectNode syntaxNode = new MessageSyntaxModelNode(syntaxModel);
		add(syntaxNode);
		
	}

	@Override
	public String getDisplayName(Object userObject) {
		String displayName = ((MessageModel)userObject).getMessageModelName();
		if (displayName == null || displayName.length() == 0) {
			displayName = s_res.getString("EditableObjectNode.unNamedNodeDisplay");
		}
		return displayName;
//		if (displayName == null) {
//			displayName = s_res.getString("EditableObjectNode.unNamedNodeDisplay");
//		}
//		return MessageFormat.format(s_res.getString("MessageModelNode.displayNameFormat"),
//				displayName);
	}

	@Override
	public String getToolTipText() {
		return ((MessageModel)getUserObject()).getDescription();
	}


	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public boolean isRemovable() {
		return true;
	}


	/** get the editor */
	@Override
	public AbstractComponentEditor getEditorForNode() {
		return new CustomEditor();
	}

	//////////////////////////////////////////////////////////////////
	
	private class CustomEditor extends GenericEditor {
		protected CustomEditor() {
			super(MessageModelNode.this.getMessageGroup(), MessageModel.class);
		}
		
		@Override
		public String getModelName( Object model ) {
			if (model instanceof MessageModel) {
				return getDisplayName(model);
			}
			return super.getModelName(model);
		}
	}

}
