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

import java.awt.GridBagConstraints;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.openhealthtools.mdht.mdmi.editor.map.editor.AbstractComponentEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.MdmiDatatypeField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.RuleField;
import org.openhealthtools.mdht.mdmi.model.ConversionRule;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.validate.ModelInfo;

/** Node for a ConversionRule */
public abstract class ConversionRuleNode extends EditableObjectNode {

	protected ConversionRuleNode(ConversionRule convRule) {
		super(convRule);
		setNodeIcon(TreeNodeIcon.ConversionRuleIcon);
	}

	@Override
	public String getDisplayName(Object userObject) {
		ConversionRule conversionRule = (ConversionRule)userObject;
		String name = conversionRule.getName();
		if (name != null && name.length() > 0) {
			return name;
		}
		// use rule
		String rule = conversionRule.getRule();
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
		return ((ConversionRule)getUserObject()).getDescription();
	}


//	@Override
//	public String getDisplayName(Object userObject) {
//		return ((ConversionRule)userObject).getName();
//	}

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

	/** Get the parent SemanticElement */
	public SemanticElement getSemanticElement() {
		// older model doesn't have parent - so we need to search the tree
		SemanticElement semanticElement = ((ConversionRule)getUserObject()).getOwner();
		if (semanticElement != null) {
			return semanticElement;
		}
		TreeNode parentNode = getParent();
		while (parentNode != null) {
			if (parentNode instanceof SemanticElementNode) {
				return (SemanticElement)((SemanticElementNode)parentNode).getUserObject();
			}
			parentNode = parentNode.getParent();
		}
		return null;
	}
	
	@Override
	public String getRulesExpressionLanguage() {
		String language = ((ConversionRule)userObject).getRuleExpressionLanguage();
		if (language == null || language.length() == 0) {
			language = getDefaultRulesExpressionLanguage();
		}
		return language;
	}

	public String getDefaultRulesExpressionLanguage() {
		String language = super.getRulesExpressionLanguage();
		return language;
	}


	//////////////////////////////////////////////////////////////////
	//    Custom Classes
	//////////////////////////////////////////////////////////////
	public class CustomEditor extends AbstractRuleEditor {
		private MdmiDatatypeField m_semanticElementDatatypeField;
		
		public CustomEditor(MessageGroup group, Class<?> objectClass) {
			super(group, objectClass);
		}

		/** Determine if this field should be shown read-only */
		@Override
		public boolean isReadOnlyFields(String fieldName) {
			// Owner is read-only
			return "Owner".equalsIgnoreCase(fieldName);
		}

		
		@Override
		protected void createDataEntryFields(List<Method[]> methodPairList) {
			super.createDataEntryFields(methodPairList);
			
			// Add a field to show Semantic Element's datatype
			m_semanticElementDatatypeField = new MdmiDatatypeField(this, MdmiDatatype.class);
			m_semanticElementDatatypeField.setReadOnly();
			addLabeledField("Owner Data Type",
					m_semanticElementDatatypeField, 0.0, GridBagConstraints.HORIZONTAL);
			if ( ((ConversionRule)getUserObject()).getOwner() != null) {
   			MdmiDatatype datatype = ((ConversionRule)getUserObject()).getOwner().getDatatype();
   			if (datatype != null) {
   				m_semanticElementDatatypeField.setDisplayValue(datatype);
   			}
			}
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
			SemanticElement semanticElement = getSemanticElement();
			if (semanticElement != null) {
				errors = ruleField.validateActionRule(semanticElement);
			}
			return errors;
		}

	}
}
