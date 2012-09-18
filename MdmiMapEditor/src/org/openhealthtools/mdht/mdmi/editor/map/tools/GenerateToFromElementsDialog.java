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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.editor.AdvancedSelectionField;
import org.openhealthtools.mdht.mdmi.editor.map.tree.ConversionRuleNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MdmiModelTree;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SemanticElementNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.ToBusinessElementNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.ToBusinessElementSetNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.ToMessageElementNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.ToMessageElementSetNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.TreeNodeIcon;
import org.openhealthtools.mdht.mdmi.model.ConversionRule;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.ToBusinessElement;
import org.openhealthtools.mdht.mdmi.model.ToMessageElement;

/** A dialog used for selecting a MessageGroup, MessageModel, and one or more
 * Semantic Elements within that message model
 * @author Conway
 *
 */
public class GenerateToFromElementsDialog extends BaseDialog {
	private static final String UNDEFINED_TYPE = " - ";

	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");

	private SemanticElement m_semanticElement = null;

	private JComboBox m_businessElementSelector  = new JComboBox();
	private JComboBox m_SEfieldNameSelector  = new JComboBox();
	private JComboBox m_BEfieldNameSelector  = new JComboBox();

	private JRadioButton m_isoButton     = new JRadioButton(s_res.getString("GenerateToFromElementsDialog.isomorphic"));
	private JRadioButton m_fromBERButton = new JRadioButton(s_res.getString("GenerateToFromElementsDialog.toMdmi"));
	private JRadioButton m_toBERButton   = new JRadioButton(s_res.getString("GenerateToFromElementsDialog.toBE"));

	private ButtonGroup  m_directionGroup = new ButtonGroup();
	
	private JLabel		m_beDatatype = new JLabel(UNDEFINED_TYPE);
	private JTextField  m_name = new JTextField();

	private ModelRenderers.BusinessElementRenderer m_businessElementRenderer = new ModelRenderers.BusinessElementRenderer();

	private FieldNameComboBoxItemRenderer m_fieldNameRenderer = new FieldNameComboBoxItemRenderer();
	
	private ActionListener m_businessElementListener = new BusinessElementListener();
	private ActionListener m_directionListener = new DirectionListener();
	private ActionListener m_fieldNameListener = new FieldNameListener();
	
	private JRadioButton m_prevButton = null;

	public GenerateToFromElementsDialog(Frame owner, SemanticElement semanticElement) {
		super(owner, BaseDialog.OK_CANCEL_APPLY_OPTION);
		m_semanticElement = semanticElement;
		buildUI();
		setTitle(s_res.getString("GenerateToFromElementsDialog.title"));
		pack(new Dimension(400,300));
	}

	
	private void buildUI() {
		// 
		//                             
		// Direction:            [ ] Iso   ( ) From   ( ) To
		// Business Element Ref: [__________________|v]
		// Name:                 [                    ]
		//  -- Semantic Element ------------------------
		// | Data Type:         text                    |
		// | Field Name:       [__________________|v]   |
		//  --------------------------------------------
		//  -- Business Element Ref --------------------
		// | Data Type:         text                    |
		// | Field Name:       [__________________|v]   |
		//  --------------------------------------------
		
		
		MdmiDatatype dataType = m_semanticElement.getDatatype();

		m_isoButton.setSelected(true);
		m_fromBERButton.setSelected(false);
		m_toBERButton.setSelected(false);
		m_prevButton = m_isoButton;
		
		m_directionGroup.add(m_isoButton);
		m_directionGroup.add(m_fromBERButton);
		m_directionGroup.add(m_toBERButton);

		// get all fields in the SE's datatype
		m_SEfieldNameSelector.addItem(AdvancedSelectionField.BLANK_ENTRY);
		populateFieldNames(m_SEfieldNameSelector, dataType, null);

		// get all business elements in the group
		populateBusinessElements();
		
		
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = Standards.getInsets();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 0;
		gbc.weighty = 0;

		// Direction 
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("GenerateToFromElementsDialog.direction")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.left = 0;
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		buttons.add(m_isoButton);
		buttons.add(m_fromBERButton);
		buttons.add(m_toBERButton);
		mainPanel.add(buttons, gbc);
		gbc.insets.left = Standards.LEFT_INSET;

		// Business Element
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("GenerateToFromElementsDialog.businessElement")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.left = 0;
		mainPanel.add(m_businessElementSelector, gbc);
		gbc.insets.left = Standards.LEFT_INSET;
		
		
		// Name
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("GenerateToFromElementsDialog.name")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.left = 0;
		mainPanel.add(m_name, gbc);
		gbc.insets.left = Standards.LEFT_INSET;
		
		//////////////////////////////////////////
		//  Semantic Element Data
		//////////////////////////////////////////
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 2;

		JLabel seDatatypeLabel = new JLabel(UNDEFINED_TYPE);
		if (dataType != null) {
			seDatatypeLabel.setText(dataType.getName());
		}
		JPanel pSE = createDataTypePanel(s_res.getString("GenerateToFromElementsDialog.semanticElement"),
				seDatatypeLabel, m_SEfieldNameSelector);
		mainPanel.add(pSE, gbc);
		gbc.gridwidth = 1;

		
		//////////////////////////////////////////
		//  Business Element Data
		//////////////////////////////////////////
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 2;
		JPanel pBE = createDataTypePanel(s_res.getString("GenerateToFromElementsDialog.businessElement"),
				m_beDatatype, m_BEfieldNameSelector);
		mainPanel.add(pBE, gbc);
		gbc.gridwidth = 1;

		
		// Set up listeners and renderers
		m_isoButton.addActionListener(m_directionListener);
		m_fromBERButton.addActionListener(m_directionListener);
		m_toBERButton.addActionListener(m_directionListener);

		m_SEfieldNameSelector.addActionListener(m_fieldNameListener);
		m_SEfieldNameSelector.setRenderer(m_fieldNameRenderer);
		m_BEfieldNameSelector.addActionListener(m_fieldNameListener);
		m_BEfieldNameSelector.setRenderer(m_fieldNameRenderer);
		
		m_businessElementSelector.setRenderer(m_businessElementRenderer);
		m_businessElementSelector.addActionListener(m_businessElementListener);
		
		
		setDirty(true);	// allow OK button
		
		getContentPane().add(mainPanel, BorderLayout.CENTER);
	}


	private void populateBusinessElements() {
		m_businessElementSelector.removeAllItems();
		
		m_businessElementSelector.addItem(AdvancedSelectionField.BLANK_ENTRY);
		MessageGroup group =  m_semanticElement.getElementSet().getModel().getGroup();
		for (MdmiBusinessElementReference bizElem : group.getDomainDictionary().getBusinessElements()) {
			
			if (m_isoButton.isSelected()) {
				// only add BERs with same datatype as SE
				if (bizElem.getReferenceDatatype() == m_semanticElement.getDatatype()) {
					m_businessElementSelector.addItem(bizElem);
				}
			} else {
				m_businessElementSelector.addItem(bizElem);
			}
		}
		
		// we can pre-fill the datatype
		if (m_isoButton.isSelected()) {
			selectDataType(m_semanticElement.getDatatype());
		}
	}
	
	
	
	private JPanel createDataTypePanel(String label, JLabel datatypeLabel, JComboBox fieldSelector) {
		//  -- label ---------------------
		// |  Datatype:   xxxxxx          |
		// |  Field Name: [           |v] |
		//  ------------------------------
		//
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = Standards.getInsets();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 0;
		gbc.weighty = 0;
		
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
				label));
		

		// Data type
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		panel.add(new JLabel(s_res.getString("GenerateToFromElementsDialog.dataType")), gbc);
		gbc.gridx++;
		gbc.insets.left = 0;
		panel.add(datatypeLabel, gbc);
		gbc.insets.left = Standards.LEFT_INSET;
		
		// Field Name 
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		panel.add(new JLabel(s_res.getString("GenerateToFromElementsDialog.fieldName")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.left = 0;
		panel.add(fieldSelector, gbc);
		gbc.insets.left = Standards.LEFT_INSET;
		
		return panel;
	}



	@Override
	public void dispose() {
		m_businessElementSelector.removeActionListener(m_businessElementListener);
		m_isoButton.removeActionListener(m_directionListener);
		m_toBERButton.removeActionListener(m_directionListener);
		m_fromBERButton.removeActionListener(m_directionListener);
		m_BEfieldNameSelector.removeActionListener(m_fieldNameListener);
		m_SEfieldNameSelector.removeActionListener(m_fieldNameListener);
		
		m_businessElementSelector.setRenderer(null);
		m_BEfieldNameSelector.setRenderer(null);
		m_SEfieldNameSelector.setRenderer(null);
		super.dispose();
	}

	@Override
	public boolean isDataValid() {
		// must have a name
		if (m_name.getText().trim().length() == 0) {
			return false;
		}
		//must have a BE with a datatype
		MdmiBusinessElementReference businessElement = getMdmiBusinessElementReference();
		if (businessElement != null &&
				businessElement.getReferenceDatatype() != null) {
			return true;
		}
		return false;
	}
	
	// get the selected business element
	public MdmiBusinessElementReference getMdmiBusinessElementReference() {
		Object item = m_businessElementSelector.getSelectedItem();
		if (item instanceof MdmiBusinessElementReference) {
			return (MdmiBusinessElementReference)item;
		}
		return null;
	}
	
	@Override
	protected void applyButtonAction() {
		if (createToFromElements()) {
			super.applyButtonAction();
			
		} else {
			return;
		}
	}
	
	@Override
	protected void okButtonAction() {
		if (createToFromElements()) {
			super.okButtonAction();
			
		} else {
			return;
		}
	}

	/** Create the To and/or From Elements */
	private boolean createToFromElements() {
		boolean created = false;
		String ruleName = m_name.getText().trim();
		
		if (m_isoButton.isSelected()) {
			// both
			created = createToFromElements(true, "To_" + ruleName) &&
					  createToFromElements(false, "From_" + ruleName);
		} else {
			created = createToFromElements(m_toBERButton.isSelected(), ruleName);
		}
		return created;
	}
	
	
	/** Create the To and/or From Elements */
	private boolean createToFromElements(boolean toBER, String ruleName) {
		// create data
		MdmiBusinessElementReference businessElement = getMdmiBusinessElementReference();
		if (businessElement == null) {
			// shouldn't be here anyway
			return false;
		}
		

		MdmiModelTree entitySelector = SelectionManager.getInstance().getEntitySelector();
		DefaultMutableTreeNode treeNode = entitySelector.findNode(m_semanticElement);
		if (!(treeNode instanceof SemanticElementNode))
		{
			// shouldn't get here
			return false;
		}
		SemanticElementNode seNode = (SemanticElementNode)treeNode;
		

		// parent of the node we'll be creating
		EditableObjectNode parentNode = null;
		ConversionRuleNode childNode = null;
		
		boolean existingRule = false;
		
		String beFieldName = m_BEfieldNameSelector.getSelectedItem().toString().trim();
		String seFieldName = m_SEfieldNameSelector.getSelectedItem().toString().trim();
		
		ConversionRule conversionRule = null;
		
		if (toBER) {
			// check if it exists already
			for (ToBusinessElement existing : m_semanticElement.getFromMdmi()) {
				if (ruleName.equalsIgnoreCase(existing.getName())) {
					existingRule = true;
					conversionRule = existing;
					break;
				}
			}
			
			if (conversionRule == null) {
				// Create a new ToBusinessElement
				conversionRule = new ToBusinessElement();
				((ToBusinessElement) conversionRule).setBusinessElement(businessElement);
				m_semanticElement.addFromMdmi((ToBusinessElement) conversionRule);
				conversionRule.setOwner(m_semanticElement);
				conversionRule.setName(ruleName);	// need to set name before creating node
				
				// create the tree node
				childNode = new ToBusinessElementNode((ToBusinessElement) conversionRule);
				
				// Find the parent node
				for (int i=0; i<seNode.getChildCount(); i++) {
					if (seNode.getChildAt(i) instanceof ToBusinessElementSetNode) {
						parentNode = (ToBusinessElementSetNode)seNode.getChildAt(i);
						break;
					}
				}
			}
						
		} else {
			// check if it exists already
			for (ToMessageElement existing : m_semanticElement.getToMdmi()) {
				if (ruleName.equalsIgnoreCase(existing.getName())) {
					existingRule = true;
					conversionRule = existing;
					break;
				}
			}

			if (conversionRule == null) {
				conversionRule = new ToMessageElement();
				((ToMessageElement) conversionRule).setBusinessElement(businessElement);
				m_semanticElement.addToMdmi((ToMessageElement) conversionRule);
				conversionRule.setOwner(m_semanticElement);
				conversionRule.setName(ruleName);	// need to set name before creating node

				// create the tree node
				childNode = new ToMessageElementNode((ToMessageElement) conversionRule);
				
				// Find the parent node
				for (int i=0; i<seNode.getChildCount(); i++) {
					if (seNode.getChildAt(i) instanceof ToMessageElementSetNode) {
						parentNode = (ToMessageElementSetNode)seNode.getChildAt(i);
						break;
					}
				}
			}
			
		}

		// if Isomorphic, the rule should be left blank
		if (!m_isoButton.isSelected()) {
			// Rule will be of the form: Set <target> to <source>
			String newRule =  generateRuleText(toBER, ruleName, seFieldName, beFieldName);

			// append new rule to existing rule
			String rule = conversionRule.getRule();
			if (rule != null && rule.length() > 0) {
				StringBuilder buf = new StringBuilder(rule);
				buf.append("\r\n").append(newRule);
				newRule = buf.toString();
			}
			conversionRule.setRule(newRule);
		}
		

		
		if (!existingRule) {
			entitySelector.insertAndOpen(parentNode, childNode);
		} else {
			// if open - close
			SelectionManager.getInstance().getEntityEditor().stopEditing(conversionRule);
			
			//open it
			childNode = (ConversionRuleNode) entitySelector.findNode(conversionRule);

			entitySelector.selectNode(childNode);
			SelectionManager.getInstance().editItem(childNode);
		}
		
		return true;
	}
	
	public static String generateRuleText(boolean toBER, String ruleName, String seFieldName, String beFieldName) {
		// Rule will be of the form: Set <target> to <source>
		StringBuilder newRule = new StringBuilder();
		String target = "value";
		String source = "value";
		
		if (toBER) {
			//  Set <ruleName>[.BEfieldName] to value
			// or
			//  Set <ruleName>[.BEfieldName] to <seFieldName>
			target = ruleName;
			if (beFieldName.length() > 0) {
				target += "." + beFieldName;
			}
			if (seFieldName.length() > 0) {
				source = seFieldName;
			}
			
		} else {
			//  Set value to <ruleName>[.BEfieldName]
			// or
			//  Set <SEfieldName> to <ruleName>[.BEfieldName]
			if (seFieldName.length() > 0) {
				target = seFieldName;
			}
			
			source = ruleName;
			if (beFieldName.length() > 0) {
				source += "." + beFieldName;
			}
			
		}
		
		
		// Set <target> to <source>
		newRule.append("set ").append(target).append(" to ").append(source);
		
		return newRule.toString();
	}

	
	// auto-fill the name
	private void fillInName() {
		String name = "";
		MdmiBusinessElementReference businessElement = getMdmiBusinessElementReference();
		if (businessElement != null) {
			String beRefName = businessElement.getName();
			if (m_isoButton.isSelected()) {
				name = beRefName;
			} else if (m_toBERButton.isSelected()) {
				name = "To_" + beRefName;
			} else {
				name = "From_" + beRefName;
			}
		}
		m_name.setText(name);
	}
	
	/** Fill in the Data Type and Field Type fields */
	private void selectDataType(MdmiDatatype dataType) {
		// empty field list
		m_BEfieldNameSelector.removeAllItems();
		m_BEfieldNameSelector.addItem(AdvancedSelectionField.BLANK_ENTRY);
		
		if (dataType != null) {
			m_beDatatype.setText(dataType.getName());
		} else {
			m_beDatatype.setText(UNDEFINED_TYPE);
		}

		// fill fields (not for iso)
		if (!m_isoButton.isSelected()) {
			populateFieldNames(m_BEfieldNameSelector, dataType, null);
		}
	}

	/** object inside a combo box of field names */
	public static class FieldNameComboBoxItem {
		private String m_path = null;
		private Field m_field = null;
		public FieldNameComboBoxItem(String path, Field field) {
			m_path = path;
			m_field = field;
		}
		
		public Field getField() {
			return m_field;
		}
		
		/** return <path>.fieldName */
		public String getPath() {
			StringBuilder fieldPath = new StringBuilder();
			if (m_path != null && !m_path.isEmpty()) {
				fieldPath.append(m_path).append('.');
			}
			fieldPath.append(m_field.getName());
			return fieldPath.toString();
		}
		
		/** return the path */
		@Override
		public String toString() {
			return getPath();
		}
		
	}

	/** populate the combo box with the fields from the datatype */
	public static void populateFieldNames(JComboBox comboBox, MdmiDatatype dataType, String path) {
		if (dataType instanceof DTComplex) {
			for (Field field : ((DTComplex)dataType).getFields()) {
				FieldNameComboBoxItem item = new FieldNameComboBoxItem(path, field);
				comboBox.addItem(item);
				
				// if field is a complex type, go further
				MdmiDatatype fieldDataType = field.getDatatype();
				populateFieldNames(comboBox, fieldDataType, item.getPath());
			}
		}
	}

	////////////////////////////////
	
	// Isomorphic/To BER/From BER change - change name text
	private class DirectionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			fillInName();
			
			// if Iso button was checked or un-checked, we need to repopulate
			if (e.getSource() == m_isoButton || m_prevButton == m_isoButton) {
				populateBusinessElements();
			}
			
			m_prevButton = (JRadioButton) e.getSource();
			setDirty(true);
		}
	}
	
	// Field Name changed - enable Apply/Save
	private class FieldNameListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			setDirty(true);
		}
	}

	private class BusinessElementListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			fillInName();
			
			// fill data type name
			MdmiDatatype dataType = null;
			MdmiBusinessElementReference businessElement = getMdmiBusinessElementReference();
			if (businessElement != null) {
				dataType = businessElement.getReferenceDatatype();
			}
			
			selectDataType(dataType);
			
			setDirty(true);	// enable/disable OK button
		}
	}
	
	/** FieldNameComboBoxItem renderer */
	public static class FieldNameComboBoxItemRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			
			Icon icon = null;
			
			// append datatype
			if (value instanceof FieldNameComboBoxItem) {
				FieldNameComboBoxItem item = (FieldNameComboBoxItem)value;
				StringBuilder text = new StringBuilder(item.getPath());
				// show as "<path> : <datatype>"
				Field field = item.getField();
				if (field.getDatatype() != null) {
					text.append(" : ").append(field.getDatatype().getName());
					icon = TreeNodeIcon.getTreeIcon(field.getDatatype().getClass());
				}
				value = text;
				
			}

			Component comp = super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);

			
			setIcon(icon);
			return comp;
		}
	}

}
