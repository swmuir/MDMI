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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.editor.AbstractComponentEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.AdvancedSelectionField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.RuleTextPane;
import org.openhealthtools.mdht.mdmi.editor.map.tree.ConversionRuleNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MdmiModelTree;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SemanticElementNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.ToBusinessElementNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.ToBusinessElementSetNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.ToMessageElementNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.ToMessageElementSetNode;
import org.openhealthtools.mdht.mdmi.model.ConversionRule;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.ToBusinessElement;
import org.openhealthtools.mdht.mdmi.model.ToMessageElement;

/** A dialog used for editing the data that links a Semantic Element with a
 * Business Element Reference
 * @author Conway
 *
 */
public class SemanticElementBERLinkDialog extends BaseDialog {
	// direction flags for methods
	public static final boolean TO_BER = true;
	public static final boolean TO_SE  = false;

	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");

	private SemanticElement m_semanticElement = null;
	private MdmiBusinessElementReference m_businessElement = null;
	
	// structure is the same for ToBusinessElement and ToMessageElement
	private class ConversionRuleInformation {
		boolean        toBER = false;
		ConversionRule convRule = null;

		JPanel       rulePanel = null;
		JComboBox    fieldNameSelector = new JComboBox();
		RuleTextPane ruleTextPane = new RuleTextPane();
		JButton      addDeleteButton = new JButton();	// text and icon will be filled in later
		
		public ConversionRuleInformation(boolean toBusinessElementRef) {
			toBER = toBusinessElementRef;
		}
	}

	
	private JButton   m_generateRuleBtn = new JButton(s_res.getString("SemanticElementBERLinkDialog.generateRule"));

	private ConversionRuleInformation m_SeToBerInfo = new ConversionRuleInformation(TO_BER);	
	private ConversionRuleInformation m_BerToSeInfo = new ConversionRuleInformation(TO_SE);	
	


	private static Icon s_addIcon = AbstractComponentEditor.getIcon(SemanticElementBERLinkDialog.class,
			s_res.getString("SemanticElementBERLinkDialog.addIcon"));
	private static Icon s_deleteIcon = AbstractComponentEditor.getIcon(SemanticElementBERLinkDialog.class,
			s_res.getString("SemanticElementBERLinkDialog.deleteIcon"));
	
	private ButtonActionListener m_actionListener = new ButtonActionListener();
	
	public SemanticElementBERLinkDialog(Frame owner, SemanticElement semanticElement,
			MdmiBusinessElementReference businessElement) {
		super(owner, BaseDialog.OK_CANCEL_OPTION);
		m_semanticElement = semanticElement;
		m_businessElement = businessElement;
		
		// identify From/To rules
		m_BerToSeInfo.convRule = findToMessageElement(m_semanticElement, m_businessElement);
		m_SeToBerInfo.convRule = findToBusinessElement(m_semanticElement, m_businessElement);
		
		buildUI();
		setTitle(s_res.getString("SemanticElementBERLinkDialog.title"));
		pack(new Dimension(400,300));
	}
	
	/** find the ToMessageElement with this BER */
	public static ToMessageElement findToMessageElement(SemanticElement se, MdmiBusinessElementReference ber) {
		ToMessageElement found = null;
		
		for (ToMessageElement toMdmi : se.getToMdmi()) {
			if (toMdmi.getBusinessElement() == ber) {
				found = toMdmi;
				break;
			}
		}
		
		return found;
	}

	/** find the ToMessageElement with this BER */
	public static ToBusinessElement findToBusinessElement(SemanticElement se, MdmiBusinessElementReference ber) {
		ToBusinessElement found = null;
		
		for (ToBusinessElement fromMdmi : se.getFromMdmi()) {
			if (fromMdmi.getBusinessElement() == ber) {
				found = fromMdmi;
				break;
			}
		}
		
		return found;
	}

	
	private void buildUI() {
		// 
		//  -- Semantic Element ------   -- Business Element Ref --
		// | Name:       text         | | Name:       text         |
		// | Data Type:  text         | | Data Type:  text         |  
		// | Field Name: [_________|v]| | Field Name: [_________|v]|
		//  --------------------------   --------------------------
		//
		// [ Generate Rule ]                               
		//
		//  -- BER to SE ----------------------------------
		// | Name:     text                                |
		// |  --- Rule --------------------------------    |
		// | |                                         |   |
		// |  -----------------------------------------    |
		//  -----------------------------------------------
		//
		//  -- BER to SE ----------------------------------
		// | Name:     text                                |
		// |  --- Rule --------------------------------    |
		// | |                                         |   |
		// |  -----------------------------------------    |
		//  -----------------------------------------------
		//
		
		// add renderer to combo box
		GenerateToFromElementsDialog.FieldNameComboBoxItemRenderer fieldNameRenderer =
				new GenerateToFromElementsDialog.FieldNameComboBoxItemRenderer();
		
		m_SeToBerInfo.fieldNameSelector.setRenderer(fieldNameRenderer);
		m_BerToSeInfo.fieldNameSelector.setRenderer(fieldNameRenderer);
				
		// get all fields in the SE's datatype
		m_SeToBerInfo.fieldNameSelector.addItem(AdvancedSelectionField.BLANK_ENTRY);
		GenerateToFromElementsDialog.populateFieldNames(m_SeToBerInfo.fieldNameSelector, m_semanticElement.getDatatype(), null);

		// get all fields in the BER's datatype
		m_BerToSeInfo.fieldNameSelector.addItem(AdvancedSelectionField.BLANK_ENTRY);
		GenerateToFromElementsDialog.populateFieldNames(m_BerToSeInfo.fieldNameSelector, m_businessElement.getReferenceDatatype(), null);
		
		// icon to generate rules
		m_generateRuleBtn.setIcon(AbstractComponentEditor.getIcon(this.getClass(),
				s_res.getString("SemanticElementBERLinkDialog.generateRuleIcon")));
		m_generateRuleBtn.setEnabled(m_BerToSeInfo.convRule != null || m_SeToBerInfo.convRule != null);
		m_generateRuleBtn.addActionListener(m_actionListener);

		// Add/Delete buttons (text and icon get added later)
		m_BerToSeInfo.addDeleteButton.addActionListener(m_actionListener);
		m_SeToBerInfo.addDeleteButton.addActionListener(m_actionListener);
		
		
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = Standards.getInsets();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 0;
		gbc.weighty = 1;

		
		//////////////////////////////////////////
		//  Semantic Element Data
		//////////////////////////////////////////
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.BOTH;

		JPanel pSE = createDataTypePanel(ClassUtil.beautifyName(SemanticElement.class), m_semanticElement.getName(),
				m_semanticElement.getDatatype(), m_SeToBerInfo.fieldNameSelector);
		mainPanel.add(pSE, gbc);

		
		//////////////////////////////////////////
		//  Business Element Data
		//////////////////////////////////////////
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.BOTH;
		JPanel pBE = createDataTypePanel(ClassUtil.beautifyName(MdmiBusinessElementReference.class), m_businessElement.getName(),
				m_businessElement.getReferenceDatatype(), m_BerToSeInfo.fieldNameSelector);
		mainPanel.add(pBE, gbc);
		
		// Generate Button
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(m_generateRuleBtn, gbc);
		gbc.gridwidth = 1;

		gbc.gridwidth = 2;
		
		///////////////////////////
		// SE to BER
		///////////////////////////
		ConversionRuleInformation ruleInfo = m_SeToBerInfo;
		formatButton(ruleInfo.addDeleteButton, ruleInfo.convRule, ToBusinessElement.class);
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(ruleInfo.addDeleteButton, gbc);
		
		ruleInfo.rulePanel = createRulePanel(ruleInfo);
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.BOTH;
		mainPanel.add(m_SeToBerInfo.rulePanel, gbc);
		if (m_SeToBerInfo.convRule == null) {
			m_SeToBerInfo.rulePanel.setVisible(false);
		}
		
		///////////////////////////
		// BER to SE
		///////////////////////////
		ruleInfo = m_BerToSeInfo;
		formatButton(ruleInfo.addDeleteButton, ruleInfo.convRule, ToMessageElement.class);
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(ruleInfo.addDeleteButton, gbc);
		
		ruleInfo.rulePanel = createRulePanel(ruleInfo);
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.BOTH;
		mainPanel.add(ruleInfo.rulePanel, gbc);
		if (ruleInfo.convRule == null) {
			ruleInfo.rulePanel.setVisible(false);
		}
		
		
		setDirty(true);	// allow OK button
		
		getContentPane().add(mainPanel, BorderLayout.CENTER);
	}
	
	
	
	private JPanel createDataTypePanel(String label, String name, MdmiDatatype datatype, JComboBox fieldSelector) {
		//  -- label --------------------------------------
		// | Name:          text                           |
		// | Data Type:     text                           |
		// | Field Name:    [_______________________|v]    |
		//  -----------------------------------------------
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

		// Name
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		panel.add(new JLabel(s_res.getString("SemanticElementBERLinkDialog.name")), gbc);
		gbc.gridx++;
		gbc.insets.left = 0;
		panel.add(new JLabel(name), gbc);
		gbc.insets.left = Standards.LEFT_INSET;

		// Data type
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;;
		gbc.fill = GridBagConstraints.NONE;
		panel.add(new JLabel(s_res.getString("SemanticElementBERLinkDialog.dataType")), gbc);
		gbc.gridx++;
		gbc.insets.left = 0;
		String datatypeName = "undefined";
		if (datatype != null) {
			datatypeName = datatype.getName();
		}
		panel.add(new JLabel(datatypeName), gbc);
		gbc.insets.left = Standards.LEFT_INSET;
		
		// Field Name 
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		panel.add(new JLabel(s_res.getString("SemanticElementBERLinkDialog.fieldName")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.left = 0;
		panel.add(fieldSelector, gbc);
		gbc.insets.left = Standards.LEFT_INSET;
		
		return panel;
	}

	private String createDefaultConversionRuleName(boolean toBER) {
		String defaultRuleName;
		String beRefName = m_businessElement.getName();
		if (toBER) {
			defaultRuleName = "To_" + beRefName;
		} else {
			defaultRuleName = "From_" + beRefName;
		}
		return defaultRuleName;
	}
	
	//createRulePanel(ruleInfo.toBER, ruleInfo.convRule, ruleInfo.ruleTextPane);
	private JPanel createRulePanel(ConversionRuleInformation ruleInfo) {
		
		//  -----------------------------------------------
		// | Name:          text                           |
		// |  - Rule ------------------------------------  |
		// | |                                           | |
		// |  -------------------------------------------  |
		//  -----------------------------------------------
		//
		boolean toBER = ruleInfo.toBER;
		ConversionRule convRule = ruleInfo.convRule;
		RuleTextPane ruleField = ruleInfo.ruleTextPane;
		
		
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = Standards.getInsets();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 0;
		gbc.weighty = 0;
		
		panel.setBorder(BorderFactory.createEtchedBorder());

		// Name
		JLabel nameLabel = new JLabel();
		if (convRule != null) {
			nameLabel.setText( convRule.getName() );
		} else {
			nameLabel.setText(createDefaultConversionRuleName(toBER));
		}
		
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		panel.add(new JLabel(s_res.getString("SemanticElementBERLinkDialog.name")), gbc);
		gbc.gridx++;
		gbc.insets.left = 0;
		panel.add(nameLabel, gbc);
		gbc.insets.left = Standards.LEFT_INSET;

		// Rule Text
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 1;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		if (convRule != null && convRule.getRule() != null) {
			ruleField.setText(convRule.getRule());
			ruleField.parseText();
		}
		JScrollPane scroller = new JScrollPane(ruleField);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		scroller.setPreferredSize( new Dimension(120, 96) );
		scroller.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
				s_res.getString("SemanticElementBERLinkDialog.rule")));
		panel.add(scroller, gbc);
		
		return panel;
	}

	/** set the Add/Delete button text and icon */
	private void formatButton(JButton addDelButton, Object model, Class<?> modelClass) {
		Icon icon;
		String text;
		String className = ClassUtil.beautifyName(modelClass);
		if (model == null) {
			// Add button
			icon = s_addIcon;
			// Add <class>
			text = MessageFormat.format(s_res.getString("SemanticElementBERLinkDialog.addFormat"), className);
		} else {
			// Delete button
			icon = s_deleteIcon;
			// Delete <class>
			text = MessageFormat.format(s_res.getString("SemanticElementBERLinkDialog.deleteFormat"), className);
		}
		addDelButton.setIcon(icon);
		addDelButton.setText(text);
	}

	@Override
	public void dispose() {
		m_generateRuleBtn.removeActionListener(m_actionListener);
		m_BerToSeInfo.addDeleteButton.removeActionListener(m_buttonActionListener);
		m_SeToBerInfo.addDeleteButton.removeActionListener(m_buttonActionListener);
		m_BerToSeInfo.fieldNameSelector.setRenderer(null);
		m_SeToBerInfo.fieldNameSelector.setRenderer(null);
		super.dispose();
	}

	@Override
	public boolean isDataValid() {
		return true;
	}
	
	@Override
	protected void okButtonAction() {
		
		// create or delete as necessary
		updateConversionRuleElement(TO_SE, m_BerToSeInfo.convRule);
		updateConversionRuleElement(TO_BER, m_SeToBerInfo.convRule);
		
		// update To/From element text
		if (m_BerToSeInfo.convRule != null) {
			m_BerToSeInfo.convRule.setRule(m_BerToSeInfo.ruleTextPane.getText());
			openInEditor(m_BerToSeInfo.convRule);
		}
		if (m_SeToBerInfo.convRule != null) {
			m_SeToBerInfo.convRule.setRule(m_SeToBerInfo.ruleTextPane.getText());
			openInEditor(m_SeToBerInfo.convRule);
		}
		super.okButtonAction();
	}

	/** Add/Delete ToMessageElement or ToBusinessElement */
	private void updateConversionRuleElement(boolean toBER, ConversionRule convRule) {
		
		MdmiModelTree entitySelector = SelectionManager.getInstance().getEntitySelector();
		DefaultMutableTreeNode treeNode = entitySelector.findNode(m_semanticElement);
		SemanticElementNode seNode = (SemanticElementNode)treeNode;

		// Find the appropriate place for the ConversionRule
		EditableObjectNode parentNode = null;	// either a ToMessageElementSetNode or a ToBusinessElementSetNode
		for (int i=0; i<seNode.getChildCount(); i++) {
			TreeNode childNode = seNode.getChildAt(i);
			if ((!toBER && childNode instanceof ToMessageElementSetNode) ||
					(toBER && childNode instanceof ToBusinessElementSetNode)) {
				parentNode = (EditableObjectNode)childNode;
				break;
			}
		}

		// check for add/deletes
		ConversionRule oldOne = (toBER ? findToBusinessElement(m_semanticElement, m_businessElement)
				: findToMessageElement(m_semanticElement, m_businessElement));
		if (convRule != null) {
			// is it new (does it contain a BER)
			MdmiBusinessElementReference ber = (toBER ? ((ToBusinessElement)convRule).getBusinessElement() :
				 ((ToMessageElement)convRule).getBusinessElement());
			if (ber == null)  {	
				// create the tree node
				EditableObjectNode childNode;
				// new Element - configure it
				if (toBER) {
					((ToBusinessElement)convRule).setBusinessElement(m_businessElement);
					m_semanticElement.addFromMdmi((ToBusinessElement)convRule);
					childNode = new ToBusinessElementNode((ToBusinessElement)convRule);
				} else {
					((ToMessageElement)convRule).setBusinessElement(m_businessElement);
					m_semanticElement.addToMdmi((ToMessageElement)convRule);
					childNode = new ToMessageElementNode((ToMessageElement)convRule);
				}
				
				convRule.setOwner(m_semanticElement);

				// add the tree node
				entitySelector.insertNewNode( parentNode, childNode);				
			}
		}

		// was one deleted
		if (oldOne != null && oldOne != convRule) {
			// deleted ToMessageElement - remove it
			DefaultMutableTreeNode childNode = entitySelector.findNode(parentNode, oldOne);
			
			// clean it up
			if (toBER) {
				((ToBusinessElement)oldOne).setBusinessElement(null);
			} else {
				 ((ToMessageElement)oldOne).setBusinessElement(null);
			}
			m_semanticElement.getToMdmi().remove(oldOne);
			oldOne.setOwner(null);

			// delete the tree node
			if (childNode != null) {
				entitySelector.deleteNode(childNode, false);	// don't prompt
			}
		}
	}
	
	// open in editor
	private void openInEditor(ConversionRule conversionRule) {
		// if open - close
		SelectionManager.getInstance().getEntityEditor().stopEditing(conversionRule);
		
		//open it
		MdmiModelTree entitySelector = SelectionManager.getInstance().getEntitySelector();
		DefaultMutableTreeNode childNode = (ConversionRuleNode) entitySelector.findNode(conversionRule);

		entitySelector.selectNode(childNode);
		SelectionManager.getInstance().editItem(childNode);
	}
	
	private void addOrDeleteElement(ConversionRuleInformation ruleInfo) {
		if (ruleInfo.convRule == null) {
			// create a new one
			ruleInfo.convRule = (ruleInfo.toBER) ? new ToBusinessElement() : new ToMessageElement();
			ruleInfo.convRule.setName(createDefaultConversionRuleName(ruleInfo.toBER));
			ruleInfo.rulePanel.setVisible(true);
		} else {
			// delete old
			ruleInfo.convRule = null;
			ruleInfo.rulePanel.setVisible(false);
		}
		
		// change label on button
		formatButton(ruleInfo.addDeleteButton, ruleInfo.convRule, 
				(ruleInfo.toBER) ? ToBusinessElement.class :  ToMessageElement.class);
		
		// enable/disable generate button
		m_generateRuleBtn.setEnabled(m_BerToSeInfo.convRule != null || m_SeToBerInfo.convRule != null);
		invalidate();
		pack();
	}
	
	
	
	/** Fill in the rule text for both BER to SE and SE to BER */
	private void generateRuleText() {
		String beFieldName = m_BerToSeInfo.fieldNameSelector.getSelectedItem().toString().trim();
		String seFieldName = m_SeToBerInfo.fieldNameSelector.getSelectedItem().toString().trim();

		if (m_SeToBerInfo.convRule != null) {
			String newRule = GenerateToFromElementsDialog.generateRuleText(true, 
					m_SeToBerInfo.convRule.getName(), seFieldName, beFieldName);

			// append new rule to existing rule
			appendRuleText(m_SeToBerInfo.ruleTextPane, newRule);

		}
		if (m_BerToSeInfo.convRule != null) {
			String newRule = GenerateToFromElementsDialog.generateRuleText(false, 
					m_BerToSeInfo.convRule.getName(), seFieldName, beFieldName);

			// append new rule to existing rule
			appendRuleText(m_BerToSeInfo.ruleTextPane, newRule);
		}
	}

	
	private void appendRuleText(RuleTextPane textArea, String ruleText) {
		// append new rule to existing rule
		String existingRule = textArea.getText();
		if (existingRule != null && existingRule.length() > 0) {
			StringBuilder buf = new StringBuilder(existingRule);
			buf.append("\r\n").append(ruleText);
			ruleText = buf.toString();
		}
		
		textArea.setText(ruleText);
		textArea.parseText();
	}
	
	private class ButtonActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			if (e.getSource() == m_generateRuleBtn) {
				generateRuleText();
			} else if (e.getSource() == m_BerToSeInfo.addDeleteButton) {
				addOrDeleteElement(m_BerToSeInfo);
			} else if (e.getSource() == m_SeToBerInfo.addDeleteButton) {
				addOrDeleteElement(m_SeToBerInfo);
			}
		}
		
	}
}
