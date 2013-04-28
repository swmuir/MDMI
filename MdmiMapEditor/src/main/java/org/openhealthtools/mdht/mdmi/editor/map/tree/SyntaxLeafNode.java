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

import org.openhealthtools.mdht.mdmi.editor.map.ModelChangeEvent;
import org.openhealthtools.mdht.mdmi.editor.map.editor.AbstractComponentEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.DataEntryFieldInfo;
import org.openhealthtools.mdht.mdmi.editor.map.editor.DefaultTextField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.IEditorField;
import org.openhealthtools.mdht.mdmi.model.LeafSyntaxTranslator;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;

public class SyntaxLeafNode extends SyntaxNodeNode {

	public SyntaxLeafNode(LeafSyntaxTranslator leaf) {
		super(leaf);
		setNodeIcon(TreeNodeIcon.SyntaxLeafIcon);
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
	public AbstractComponentEditor getEditorForNode() {
		return new CustomEditor(getMessageGroup(), getUserObject().getClass());
	}

	@Override
	public String getFormatExpressionLanguage() {
		String language = ((LeafSyntaxTranslator)userObject).getFormatExpressionLanguage();
		if (language == null || language.length() == 0) {
			language = getDefaultFormatExpressionLanguage();
		}
		return language;
	}

	public String getDefaultFormatExpressionLanguage() {
		String language = super.getFormatExpressionLanguage();
		return language;
	}


	//////////////////////////////////////////////////////////////////
	//    Custom Classes
	//////////////////////////////////////////////////////////////
	public class CustomEditor extends SyntaxNodeNode.CustomEditor {
		private DefaultTextField m_formatLanguageField;
		
		public CustomEditor(MessageGroup group, Class<?> objectClass) {
			super(group, objectClass);
		}
		
		/** Use a custom field for language */
		@Override
		protected IEditorField createEditorField(DataEntryFieldInfo fieldInfo) {
			if ("FormatExpressionLanguage".equalsIgnoreCase(fieldInfo.getFieldName())) {
				// Use a special text field
				m_formatLanguageField = new DefaultTextField(this, 
						getDefaultFormatExpressionLanguage());
				return m_formatLanguageField;
			}
			return super.createEditorField(fieldInfo);
		}

		@Override
		public void modelChanged(ModelChangeEvent e) {
			if (e.getSource() != getUserObject()) {
				// Could be a change to the language - update value
				String defaultLanguage = getDefaultFormatExpressionLanguage();
				m_formatLanguageField.setDefaultValue(defaultLanguage);
			}
			
			super.modelChanged(e);
		}
	}
}
