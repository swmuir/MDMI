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

import java.util.ArrayList;
import java.util.List;

import org.openhealthtools.mdht.mdmi.editor.map.editor.AbstractComponentEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.RuleField;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.SemanticElementRelationship;
import org.openhealthtools.mdht.mdmi.model.validate.ModelInfo;

/** Node for a collection of SemanticElementRelationships */
public class SemanticElementRelationshipNode extends EditableObjectNode {

	public SemanticElementRelationshipNode(SemanticElementRelationship relationship) {
		super(relationship);
		setNodeIcon(TreeNodeIcon.SemanticElementRelationshipIcon);
	}

	@Override
	public String getDisplayName(Object userObject) {
		SemanticElementRelationship relationship = (SemanticElementRelationship)userObject;
		String name = relationship.getName();
		if (name != null && name.length() > 0) {
			return name;
		}
		// use rule
		String rule = relationship.getRule();
		if (rule == null) {
			rule = s_res.getString("EditableObjectNode.unNamedNodeDisplay");
		}
		if (rule.length() > 33) {
			rule = rule.substring(0,30) + "...";
		}
		return rule;
	}

	@Override
	public String getToolTipText() {
		return ((SemanticElementRelationship)getUserObject()).getDescription();
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
	public String getRulesExpressionLanguage() {
		String language = ((SemanticElementRelationship)userObject).getRuleExpressionLanguage();
		if (language == null || language.length() == 0) {
			language = getDefaultRulesExpressionLanguage();
		}
		return language;
	}

	public String getDefaultRulesExpressionLanguage() {
		String language = super.getRulesExpressionLanguage();
		return language;
	}

	@Override
	public AbstractComponentEditor getEditorForNode() {
		return new CustomEditor(getMessageGroup(), getUserObject().getClass());
	}

	//////////////////////////////////////////////////////////////////
	//    Custom Classes
	//////////////////////////////////////////////////////////////
	public class CustomEditor extends AbstractRuleEditor {
		
		public CustomEditor(MessageGroup group, Class<?> objectClass) {
			super(group, objectClass);
		}

		/** Determine if this field should be shown read-only */
		@Override
		public boolean isReadOnlyFields(String fieldName) {
			// Context is read-only
			return "Context".equalsIgnoreCase(fieldName);
		}

		@Override
		public String getDefaultRuleLanguage() {
			return getDefaultRulesExpressionLanguage();
		}

		@Override
		public String getRuleLanguage() {
			return getConstraintExpressionLanguage();
		}

		@Override
		protected List<ModelInfo> validateRuleField(RuleField ruleField) {
			List<ModelInfo> errors = new ArrayList<ModelInfo>();
			SemanticElementRelationship relationship = (SemanticElementRelationship)getEditObject();
			SemanticElement semanticElement = relationship.getRelatedSemanticElement();
			if (semanticElement != null) {
				errors = ruleField.validateConstraintRule(semanticElement);
			}
			return errors;
		}

	}
}
