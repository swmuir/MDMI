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

import java.util.List;

import javax.swing.JComponent;

import org.openhealthtools.mdht.mdmi.editor.map.ModelChangeEvent;
import org.openhealthtools.mdht.mdmi.editor.map.ModelChangeListener;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.editor.DataEntryFieldInfo;
import org.openhealthtools.mdht.mdmi.editor.map.editor.DefaultTextField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.GenericEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.IEditorField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.RuleField;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.validate.ModelInfo;

/** An abstract editor for model elements that contain a rule (and rule-language) field */
public abstract class AbstractRuleEditor extends GenericEditor implements ModelChangeListener {

	private DefaultTextField m_rulesLanguageField;
	
	public AbstractRuleEditor(MessageGroup group, Class<?> objectClass) {
		super(group, objectClass);
	}

	@Override
	public void addNotify() {
		super.addNotify();
		SelectionManager.getInstance().addModelChangeListener(this);
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		SelectionManager.getInstance().removeModelChangeListener(this);
	}

	/** return the name of the field that defines the rule.
	 * The default is "Rule" */
	public String getRuleFieldName() {
		return "Rule";
	}

	/** return the name of the field that defines the rule language.
	 * The default is "RuleExpressionLanguage" */
	public String getRuleLanguageFieldName() {
		return "RuleExpressionLanguage";
	}

	/** Get the default rule language */
	public abstract String getDefaultRuleLanguage();
	
	/** Get the actual rule language */
	public abstract String getRuleLanguage();
	

	/** Call the validateConstraintRule or validateActionRule on this object */
	protected abstract List<ModelInfo> validateRuleField(RuleField ruleField);
	
	/** Use a custom field for language */
	@Override
	protected IEditorField createEditorField(DataEntryFieldInfo fieldInfo) {
		if (getRuleLanguageFieldName().equalsIgnoreCase(fieldInfo.getFieldName())) {
			// Use a special text field
			m_rulesLanguageField = new DefaultTextField(this, getDefaultRuleLanguage());
			return m_rulesLanguageField;
		}
		return super.createEditorField(fieldInfo);
	}


	@Override
	public List<ModelInfo> validateModel() {
		List<ModelInfo> errors = super.validateModel();
		
		// validate rule if NRL
		if ("NRL".equalsIgnoreCase(getRuleLanguage())) {
			JComponent component = getComponent(getRuleFieldName());
			if (component instanceof RuleField) {
				RuleField ruleField = (RuleField)component;
				errors.addAll( validateRuleField(ruleField) );
			}
		}
		
		return errors;
	}


	@Override
	public void modelChanged(ModelChangeEvent e) {
		if (e.getSource() != getEditObject()) {
			// Could be a change to the language - update value
			String defaultLanguage = getDefaultRuleLanguage();
			m_rulesLanguageField.setDefaultValue(defaultLanguage);
		}
	}

}
