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
package org.openhealthtools.mdht.mdmi.editor.map.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.map.editor.GenericEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.RuleLanguageSelector;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MdmiExpression;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;

/** A dialog used for generating ComputedIn Rules for a selected Semantic Element
 * @author Conway
 *
 */
public class GenerateComputedInValuesDialog extends BaseDialog implements PropertyChangeListener {
	public static final String CR_LF = "\r\n";
	public static final String SPACES = "    ";

	private static final String UNDEFINED_TYPE = " - ";

	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");

	private SemanticElement m_semanticElement = null;
	private MdmiExpression m_computedInValue = null;
	
	private RuleLanguageSelector m_ruleLanguageSelector = new RuleLanguageSelector();
	private FieldList m_fieldList;
	
	private GenericEditor m_computedInValueEditor = null;

	public GenerateComputedInValuesDialog(Frame owner, SemanticElement semanticElement, MdmiExpression computedInValue) {
		super(owner, BaseDialog.OK_CANCEL_APPLY_OPTION);
		m_semanticElement = semanticElement;
		m_computedInValue = computedInValue;
		
		buildUI();
		setTitle(s_res.getString("GenerateComputedInValuesDialog.title"));
		pack(new Dimension(400,250));
	}

	// define the editor, so we can populate it when done
	public void setComputedInEditor(GenericEditor editor) {
		m_computedInValueEditor = editor;
	}
	
	private void buildUI() {
		// Select Language:      (o) Java Script   ( ) NRL 
		//                             
		// Semantic Element:     text
		// Data Type:            text
		// 
		//  - Fields ----------------------------
		// | Field Name    Value                 |
		// |  ---------------------------------  |
		// | | Field1:   [__________________]  | |
		// | | Field2:   [__________________]  | |
		// | | Field3:   [__________________]  | |
		// | | ...                             | |
		// | | FieldN:   [__________________]  | |
		// |  ---------------------------------  |
		//  -------------------------------------
		
		// Populate Controls before laying out
		MdmiDatatype dataType = m_semanticElement.getDatatype();
		
//		// for now - don't use group's language
//		MessageGroup group =  m_semanticElement.getElementSet().getModel().getGroup();
//		m_ruleLanguageSelector.setLanguage(group.getDefaultRuleExprLang());
		
		m_fieldList = new FieldList(dataType);
		
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = Standards.getInsets();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 0;
		gbc.weighty = 0;
		
		// Language
		gbc.weightx = 0;
		// add extra bottom insets
		gbc.insets.bottom = 2*Standards.BOTTOM_INSET;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("GenerateComputedInValuesDialog.selectLanguage")), gbc);
		
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.left = 0;
		gbc.insets.right = 0;	// don't need insets since the next component has FlowLayout padding
		mainPanel.add(m_ruleLanguageSelector, gbc);
		gbc.insets.left = Standards.LEFT_INSET;

		// Semantic Element: name
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.insets.bottom = 0;
		gbc.insets.right = Standards.RIGHT_INSET;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("GenerateComputedInValuesDialog.semanticElement")), gbc);
		gbc.gridx++;

		gbc.insets.left = 0;
		mainPanel.add(new JLabel(m_semanticElement.getName()), gbc);
		gbc.insets.left = Standards.LEFT_INSET;

		// Data Type: name
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.insets.bottom = 2*Standards.BOTTOM_INSET;
		gbc.insets.right = Standards.RIGHT_INSET;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("GenerateComputedInValuesDialog.dataType")), gbc);
		gbc.gridx++;

		gbc.insets.left = 0;
		JLabel datatypeLabel = new JLabel(UNDEFINED_TYPE);
		if (dataType != null) {
			datatypeLabel.setText(dataType.getName());
		}
		mainPanel.add(datatypeLabel, gbc);
		gbc.insets.left = Standards.LEFT_INSET;
		

		// Fields List
		gbc.gridx = 0;
		gbc.gridy++;
		
		if (dataType instanceof DTComplex) {
			// wrap in a scroll pane
			// Field Names/Values
			JScrollPane scroller = new JScrollPane(m_fieldList);
			scroller.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
					s_res.getString("GenerateComputedInValuesDialog.fieldNamesValues")));

			gbc.weightx = 1;
			gbc.gridwidth = 2;
			gbc.weighty = 1;
			gbc.fill = GridBagConstraints.BOTH;
			mainPanel.add(scroller, gbc);
		} else {
			// Value: [________]
			gbc.insets.right = 0;
			gbc.weightx = 0;
			gbc.fill = GridBagConstraints.NONE;
			mainPanel.add(new JLabel(s_res.getString("GenerateComputedInValuesDialog.fieldValue")), gbc);
			
			gbc.gridx++;
			gbc.weightx = 1;
			gbc.insets.left = 0;
			gbc.insets.right = Standards.RIGHT_INSET;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			mainPanel.add(m_fieldList, gbc);
		}
		
		// Set up listeners
		m_ruleLanguageSelector.addPropertyChangeListener(this);
		
		
		setDirty(true);	// allow OK button
		
		getContentPane().add(mainPanel, BorderLayout.CENTER);
	}



	@Override
	public void dispose() {
		m_ruleLanguageSelector.removePropertyChangeListener(this);
		super.dispose();
	}

	@Override
	public boolean isDataValid() {
		// must have a data type
		if (m_semanticElement.getDatatype() == null) {
			return false;
		}

		return true;
	}
	
	
	@Override
	protected void applyButtonAction() {
		if (fillComputedInValue()) {
			super.applyButtonAction();
			
		} else {
			return;
		}
	}
	
	@Override
	protected void okButtonAction() {
		if (fillComputedInValue()) {
			super.okButtonAction();
			
		} else {
			return;
		}
	}

	/** populate the ComputedIn value */
	private boolean fillComputedInValue() {
		// Get user data
		MdmiDatatype datatype = m_semanticElement.getDatatype();
		if (datatype == null) {
			return false;
		}

		Collection<FieldValue> fieldValues = new ArrayList<FieldValue>();
		for (FieldListRow row : m_fieldList.m_rows) {
			FieldValue fieldValue = new FieldValue();
			fieldValue.attrName = row.fieldName;
			fieldValue.inputValue = row.textField.getText().trim();

			fieldValues.add(fieldValue);
		}

		// language
		String language = m_ruleLanguageSelector.getLanguage();
		m_computedInValue.setLanguage(language);
		
		// rule
		String rule = generateComputedInRule(language, datatype, fieldValues);
		m_computedInValue.setExpression(rule);	// replace what's there
		
		
		// tell the object editor to fill in the data
		if (m_computedInValueEditor != null) {
			m_computedInValueEditor.populateUI(m_computedInValue);
		}
		return true;
	}

	// class for generating rules. For a complex type, there will be one of these per field.
	// For a simple type, there will be just one - with the attrName set to the dataType name
	public static class FieldValue {
		String attrName;
		String inputValue;
	}
	
	/**
	 * Generate rules. 
	 * @param language	NRL or Java Script (default)
	 * @param dataType	The dataType
	 * @param fieldValues	A list of name/value pairs. If the dataType is simple, this will contain one item, with
	 * 						the fieldName set to the dataType name, and the inputValue set to the desired value.
	 * 						If the dataType is complex, there will be an entry for every field in the dataType
	 * @return
	 */
	public static String generateComputedInRule(String language, MdmiDatatype dataType, Collection<FieldValue> fieldValues) {

		String ruleText = null;
		if (RuleLanguageSelector.NRL.equals(language)) {
			ruleText = generateNRLRuleText(dataType, fieldValues);
		} else {
			// default is Java Script
			ruleText = generateJSRuleText(dataType, fieldValues);
		}
		
		return ruleText;
	}
	
	public static String generateNRLRuleText(MdmiDatatype dataType, Collection<FieldValue> fieldValues) {
		// Per Ken:
		// for each field of a complex type
		//    set <attrName> to '<inputValue>';
		// for simple data type
		//    set value to '<inputValue>'
		StringBuilder buf = new StringBuilder();
		if (fieldValues != null) {
			for (FieldValue fieldValue : fieldValues) {
				if (fieldValue.inputValue == null ||
						fieldValue.inputValue.isEmpty()) continue;

				if (buf.length() != 0) {
					buf.append(CR_LF);
				}

				if (dataType instanceof DTComplex) {
					buf.append("set ").append(fieldValue.attrName).append(" to '");
					buf.append(fieldValue.inputValue).append("';");
				} else {
					buf.append("set value to '");
					buf.append(fieldValue.inputValue).append("';");
				}
			}
		}
		//set value to 'field-input'
		
		return buf.toString();
	}
	
	public static String generateJSRuleText(MdmiDatatype dataType, Collection<FieldValue> fieldValues) {
		StringBuilder buf = new StringBuilder();
		// Per Ken: 
		// for each field of a complex type
		//    value.value().setValue('<attrName>', '<inputValue>');
		// for simple data type
		//    value.getXValue().setValue('<inputValue>');
		for (FieldValue fieldValue : fieldValues) {
			if (fieldValue.inputValue == null ||
					fieldValue.inputValue.isEmpty()) continue;

			if (buf.length() != 0) {
				buf.append(CR_LF);
			}

			if (dataType instanceof DTComplex) {
				buf.append("value.value().setValue('").append(fieldValue.attrName).append("', '");
				buf.append(fieldValue.inputValue).append("');");
			} else {
				buf.append("value.getXValue().setValue('");
				buf.append(fieldValue.inputValue).append("');");
			}
		}

		return buf.toString();
	}
	

	// General purpose propertyChangeListener - enable Apply/Save
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		setDirty(true);	// enable/disable OK button
	}
	
	/** Class for contents of fieldList */
	public static class FieldListRow {
		String fieldName = "";
		JTextField textField = new JTextField(20);
	}

	//   
	//  Field1:   [__________________]
	//  Field2:   [__________________]
	//  Field3:   [__________________]
	//   ...                            
	//  FieldN:   [__________________]
	private class FieldList extends JPanel implements DocumentListener {
		ArrayList<FieldListRow> m_rows = new ArrayList<FieldListRow>();
		
		FieldList(MdmiDatatype dataType) {
			super(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.weighty = 0;
			
			if (dataType == null) {
				// blank label
				JLabel label = new JLabel("...No DataType...");
				label.setEnabled(false);	// turn it gray
				add(label, gbc);
				
			} else if (dataType instanceof DTComplex) {
				// add fields
				ArrayList<Field> fields = ((DTComplex)dataType).getFields();
				for (int i=0; i<fields.size(); i++) {
					Field field = fields.get(i);
					// create a FieldListRow
					FieldListRow row = new FieldListRow();
					row.fieldName = field.getName();
					m_rows.add(row);

					// last one gets all the weight
					if (i == fields.size()-1) {
						gbc.weighty = 1;
					}

					// Field Name
					gbc.gridx = 0;
					gbc.insets.left = Standards.LEFT_INSET;
					gbc.insets.right = 0;
					gbc.fill = GridBagConstraints.NONE;
					gbc.weightx = 0;
					add(new JLabel(row.fieldName), gbc);
					
					// Value
					gbc.gridx++;
					gbc.insets.right = Standards.RIGHT_INSET;
					gbc.fill = GridBagConstraints.HORIZONTAL;
					gbc.weightx = 1;
					add(row.textField, gbc);

					// set up for next one
					gbc.gridy++;
				}
				
			} else {
				// one FieldListRow
				FieldListRow row = new FieldListRow();
				row.fieldName = dataType.getName();
				m_rows.add(row);

				// Just text field
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.weightx = 1;
				//gbc.weighty = 1;
				add(row.textField, gbc);
			}
		}

		@Override
		public void addNotify() {
			// add document listener
			super.addNotify();
			for (FieldListRow row : m_rows) {
				row.textField.getDocument().addDocumentListener(this);
			}
		}

		@Override
		public void removeNotify() {
			// remove document listener
			for (FieldListRow row : m_rows) {
				row.textField.getDocument().removeDocumentListener(this);
			}
			super.removeNotify();
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			setDirty(true);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			setDirty(true);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			setDirty(true);
		}
		
	}

}
