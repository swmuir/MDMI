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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.editor.AdvancedSelectionField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.RuleLanguageSelector;
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
import org.openhealthtools.mdht.mdmi.model.DTSPrimitive;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.SemanticElementRelationship;
import org.openhealthtools.mdht.mdmi.model.ToBusinessElement;
import org.openhealthtools.mdht.mdmi.model.ToMessageElement;

/** A dialog used for generating To/From Rules for a relationship within selected Semantic Element
 * @author Conway
 *
 */
public class GenerateToFromRelationshipRuleDialog extends BaseDialog implements ActionListener,
											DocumentListener, PropertyChangeListener {
	public static final String CR_LF = "\r\n";
	public static final String SPACES = "    ";

	private static final String UNDEFINED_TYPE = " - ";

	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");

	private SemanticElement m_semanticElement = null;

	private final JComboBox<Object> m_businessElementSelector  = new JComboBox<Object>();
	private final JComboBox<Object> m_SEfieldNameSelector  = new JComboBox<Object>();
	private final JComboBox<Object> m_BEfieldNameSelector  = new JComboBox<Object>();

	private final RuleLanguageSelector m_ruleLanguageSelector = new RuleLanguageSelector();

	private final JRadioButton m_isoButton     = new JRadioButton(s_res.getString("GenerateToFromElementsDialog.isomorphic"));
	private final JRadioButton m_fromBERButton = new JRadioButton(s_res.getString("GenerateToFromElementsDialog.toMdmi"));
	private final JRadioButton m_toBERButton   = new JRadioButton(s_res.getString("GenerateToFromElementsDialog.toBE"));

	private final ButtonGroup  m_directionGroup = new ButtonGroup();

	private final JCheckBox m_filterByDatatypeButton   = new JCheckBox("Filter by Data Type");


	private final JLabel		m_beDatatype = new JLabel(UNDEFINED_TYPE);
	private final JTextField  m_name = new JTextField();

	private final FieldNameComboBoxItemRenderer m_fieldNameRenderer = new FieldNameComboBoxItemRenderer();

	private final ActionListener m_businessElementListener = new BusinessElementListener();
	private final ActionListener m_directionListener = new DirectionListener();

	private JRadioButton m_prevButton = null;
	///////////////////////////////////////
	private static boolean s_append = true;	// last state
	private JRadioButton m_appendBtn = new JRadioButton(s_res.getString("GenerateToFromElementsDialog.append"), true);
	private JRadioButton m_replaceBtn = new JRadioButton(s_res.getString("GenerateToFromElementsDialog.replace"), false);

	// these are only used if selectRelationship is set
	private boolean m_selectRelationship = false;
	private final JComboBox<Object> m_relationshipSelector  = new JComboBox<Object>();
	private final JTextField  m_relationshipValue = new JTextField();

	/** create a dialog to generate To/From rules with a relationship */
	public GenerateToFromRelationshipRuleDialog(Frame owner, SemanticElement semanticElement) {
		this(owner, semanticElement, true);
	}

	/** create a dialog to generate To/From rules with an optional relationship */
	protected GenerateToFromRelationshipRuleDialog(Frame owner, SemanticElement semanticElement, boolean selectRelationship) {
		super(owner, BaseDialog.OK_CANCEL_APPLY_OPTION);
		m_semanticElement = semanticElement;
		m_selectRelationship = selectRelationship;
		
		// set datatype on "Filter by Data Type"  button
		String filterText = MessageFormat.format(s_res.getString("GenerateToFromElementsDialog.filterByDataType"), 
				m_semanticElement.getDatatype() == null ? "UNK" : m_semanticElement.getDatatype().getTypeName());
		m_filterByDatatypeButton.setText(filterText);
		
		
		buildUI();
		String title = MessageFormat.format(s_res.getString("GenerateToFromElementsDialog.title"), 
				semanticElement.getName());
		setTitle(title);
		pack(new Dimension(550,300));
	}

	private void buildUI() {
		// Select Language:      (o) Java Script   ( ) NRL
		//
		// Direction:            ( ) Iso   ( ) From   ( ) To
		// Business Element:     [x] Filter by Data Type
		//                       [_______________________|v]
		// Name:                 [                    ]
		// Relationship:         [_________________|v]
		// (0) Append   ( ) Replace
		//  -- Semantic Element ------------------------
		// | Data Type:         text                    |
		// | Field Name:       [__________________|v]   |
		//  --------------------------------------------
		//  -- Business Element Ref --------------------
		// | Data Type:         text                    |
		// | Field Name:       [__________________|v]   |
		//  --------------------------------------------

		// Populate Controls before laying out
		MdmiDatatype dataType = m_semanticElement.getDatatype();

//		// for now - don't use group's language
//		MessageGroup group =  m_semanticElement.getElementSet().getModel().getGroup();
//		m_ruleLanguageSelector.setLanguage(group.getDefaultRuleExprLang());

		m_isoButton.setSelected(true);
		m_fromBERButton.setSelected(false);
		m_toBERButton.setSelected(false);
		m_filterByDatatypeButton.setSelected(true);	// start out checked
		m_prevButton = m_isoButton;

		m_directionGroup.add(m_isoButton);
		m_directionGroup.add(m_fromBERButton);
		m_directionGroup.add(m_toBERButton);
		
		// for Relationship - there's no Isomorphic case
		if (m_selectRelationship) {
			m_isoButton.setVisible(false);
			m_toBERButton.setSelected(true);
			m_filterByDatatypeButton.setSelected(false);
		}

		// get all fields in the SE's datatype
		m_SEfieldNameSelector.addItem(AdvancedSelectionField.BLANK_ENTRY);
		populateFieldNames(m_SEfieldNameSelector, dataType, null);

		// get all business elements in the group
		populateBusinessElements();


		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = Standards.getInsets();
		// add extra bottom insets
		gbc.insets.bottom = 2*Standards.BOTTOM_INSET;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 0;
		gbc.weighty = 0;

		// Language
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("GenerateToFromElementsDialog.selectLanguage")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.left = 0;
		gbc.insets.right = 0;	// don't need insets since the next component has FlowLayout padding
		mainPanel.add(m_ruleLanguageSelector, gbc);
		gbc.insets.left = Standards.LEFT_INSET;

		// Direction
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets.right = 0;	// don't need insets since the next component has FlowLayout padding
		mainPanel.add(new JLabel(s_res.getString("GenerateToFromElementsDialog.direction")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.left = 0;
		gbc.insets.right = Standards.RIGHT_INSET;
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, Standards.LEFT_INSET, 0));
		buttons.add(m_isoButton);
		buttons.add(m_fromBERButton);
		buttons.add(m_toBERButton);
		mainPanel.add(buttons, gbc);
		gbc.insets.left = Standards.LEFT_INSET;

		// Business element: [x] Filter by Datatype 
		//                   [___________________|v]
		// or
		//
		// Business element:  [___________________|v]
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.insets.bottom = 0;	// make this group closer to the next (BE List)
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("GenerateToFromElementsDialog.businessElementLabel")), gbc);

		if (!m_selectRelationship) {
			// Filter by Datatype checkbox
			gbc.gridx++;
			gbc.weightx = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets.left = 0;
			mainPanel.add(m_filterByDatatypeButton, gbc);


			// want Business Element List on new row
			gbc.gridx = 0;
			gbc.gridy++;
			gbc.insets.left = Standards.LEFT_INSET;
			gbc.insets.bottom = 2*Standards.BOTTOM_INSET;
			gbc.weightx = 0;
			gbc.fill = GridBagConstraints.NONE;
			mainPanel.add(new JLabel(" "), gbc);	// space
			gbc.insets.top = 0;
		}
		
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.left = 0;
		gbc.insets.bottom = 2*Standards.BOTTOM_INSET;
		mainPanel.add(m_businessElementSelector, gbc);
		gbc.insets.left = Standards.LEFT_INSET;
		gbc.insets.top = Standards.TOP_INSET;


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
		
		//////////////////////////////////////////////
		// Relationship Choice
		///////////////////////////////////////////////
		if (m_selectRelationship) {
			gbc.gridx = 0;
			gbc.gridy++;
			gbc.weightx = 0;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridwidth = 2;
			JPanel relPane = createRelationshipPanel();
			mainPanel.add(relPane, gbc);

			populateRelationships();
		}
		
		// (0) Append   ( ) Replace
		gbc.gridx = 0;
		gbc.gridy++;
		ButtonGroup group = new ButtonGroup();
		m_appendBtn.setSelected(s_append);	// use last state
		m_replaceBtn.setSelected(!m_appendBtn.isSelected());
		group.add(m_appendBtn);
		group.add(m_replaceBtn);
		JPanel appendReplace = new JPanel(new FlowLayout(FlowLayout.LEFT, Standards.LEFT_INSET, 0));
		appendReplace.add(m_appendBtn);
		appendReplace.add(m_replaceBtn);

		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("GenerateToFromElementsDialog.rule")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.left = 0;
		gbc.insets.right = 0;	// don't need insets since the next component has FlowLayout padding
		mainPanel.add(appendReplace, gbc);
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
			seDatatypeLabel.setText(dataType.getTypeName());
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
		m_ruleLanguageSelector.addPropertyChangeListener(this);
		m_isoButton.addActionListener(m_directionListener);
		m_fromBERButton.addActionListener(m_directionListener);
		m_toBERButton.addActionListener(m_directionListener);
		m_filterByDatatypeButton.addActionListener(m_directionListener);

		m_SEfieldNameSelector.addActionListener(this);
		m_SEfieldNameSelector.setRenderer(m_fieldNameRenderer);
		m_BEfieldNameSelector.addActionListener(this);
		m_BEfieldNameSelector.setRenderer(m_fieldNameRenderer);

//		m_businessElementSelector.setRenderer(m_businessElementRenderer);
		m_businessElementSelector.addActionListener(m_businessElementListener);

		m_relationshipSelector.addActionListener(this);
		m_relationshipValue.getDocument().addDocumentListener(this);

		setDirty(true);	// allow OK button

		getContentPane().add(mainPanel, BorderLayout.CENTER);
	}


	private void populateBusinessElements() {
		m_businessElementSelector.removeAllItems();

		// Find all the BusinessElementReferences
		ArrayList<MdmiBusinessElementReference> elements = new ArrayList<MdmiBusinessElementReference>();

		MessageGroup group =  m_semanticElement.getElementSet().getModel().getGroup();
		for (MdmiBusinessElementReference element : group.getDomainDictionary().getBusinessElements()) {
			if (element.getName() != null && element.getName().length() > 0) {

				if (m_filterByDatatypeButton.isSelected()) {
					// only add BERs with same datatype as SE
					if (element.getReferenceDatatype() == m_semanticElement.getDatatype()) {
						elements.add(element);
					}
				} else {
					elements.add(element);
				}
			}
		}

		// sort by name
		Collections.sort(elements, new Comparators.BusinessElementReferenceComparator());

		// wrap, and add to combo box
		m_businessElementSelector.addItem(AdvancedSelectionField.BLANK_ENTRY);
		for (MdmiBusinessElementReference bizElem : elements) {
			m_businessElementSelector.addItem(new BusinessElementReferenceWrapper(bizElem));
		}

		// we can pre-fill the datatype
		if (m_isoButton.isSelected()) {
			selectDataType(m_semanticElement.getDatatype());
		}
	}

	/** Wrapper for items in the combo box */
	public class BusinessElementReferenceWrapper {
		public MdmiBusinessElementReference m_ber;

		public BusinessElementReferenceWrapper(MdmiBusinessElementReference object) {
			m_ber = object;
		}

		@Override
		public String toString() {
			if (m_ber.getName() == null || "".equals(m_ber.getName())) {
				return ClassUtil.s_unNamedItem;
			}
			if (m_ber.getDescription() == null || m_ber.getDescription().isEmpty()) {
				return m_ber.getName();
			} else {
				return m_ber.getName() + " (" + m_ber.getDescription() + ")";
			}
		}

		@Override
		public boolean equals(Object otherObject) {
			if (otherObject instanceof BusinessElementReferenceWrapper) {
				return equals(((BusinessElementReferenceWrapper)otherObject).m_ber);
			}

			// compare objects
			return m_ber.equals(otherObject);
		}

	}



	private void populateRelationships() {
		m_relationshipSelector.removeAllItems();

		// Find all the SemanticElementRelationships
		ArrayList<SemanticElementRelationship> elements = new ArrayList<SemanticElementRelationship>();

		for (SemanticElementRelationship relationship : m_semanticElement.getRelationships()) {
			if (relationship.getName() != null && relationship.getName().length() > 0) {
				elements.add(relationship);
			}
		}

		// sort by name
		Collections.sort(elements, new Comparators.SemanticElementRelationshipComparator());

		// wrap, and add to combo box
		m_relationshipSelector.addItem(AdvancedSelectionField.BLANK_ENTRY);
		for (SemanticElementRelationship rels : elements) {
			m_relationshipSelector.addItem(new RelationshipWrapper(rels));
		}
	}

	/** Wrapper for items in the combo box */
	public class RelationshipWrapper {
		public SemanticElementRelationship m_relationship;

		public RelationshipWrapper(SemanticElementRelationship object) {
			m_relationship = object;
		}

		@Override
		public String toString() {
			if (m_relationship.getName() == null || m_relationship.getName().isEmpty()) {
				return ClassUtil.s_unNamedItem;
			}
			return m_relationship.getName();
		}

		@Override
		public boolean equals(Object otherObject) {
			if (otherObject instanceof RelationshipWrapper) {
				return equals(((RelationshipWrapper)otherObject).m_relationship);
			}

			// compare objects
			return m_relationship.equals(otherObject);
		}

	}

	private JPanel createDataTypePanel(String label, JLabel datatypeLabel, JComboBox<Object> fieldSelector) {
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


	private JPanel createRelationshipPanel() {
		//  -- Relationship Condition ------
		// |  Relationship: [          |v] |
		// |  Value:        [____________] |
		//  --------------------------------
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
				s_res.getString("GenerateToFromElementsDialog.relationshipLabel")));


		// Relationship
		gbc.fill = GridBagConstraints.NONE;
		panel.add(new JLabel(s_res.getString("GenerateToFromElementsDialog.relationship")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.left = 0;
		panel.add(m_relationshipSelector, gbc);
		gbc.insets.left = Standards.LEFT_INSET;

		// Condition
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		panel.add(new JLabel(s_res.getString("GenerateToFromElementsDialog.condition")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.left = 0;
		panel.add(m_relationshipValue, gbc);
		gbc.insets.left = Standards.LEFT_INSET;

		return panel;
	}



	@Override
	public void dispose() {
		m_ruleLanguageSelector.removePropertyChangeListener(this);
		m_businessElementSelector.removeActionListener(m_businessElementListener);
		m_isoButton.removeActionListener(m_directionListener);
		m_toBERButton.removeActionListener(m_directionListener);
		m_fromBERButton.removeActionListener(m_directionListener);
		m_filterByDatatypeButton.removeActionListener(m_directionListener);
		m_BEfieldNameSelector.removeActionListener(this);
		m_SEfieldNameSelector.removeActionListener(this);

//		m_businessElementSelector.setRenderer(null);
		m_BEfieldNameSelector.setRenderer(null);
		m_SEfieldNameSelector.setRenderer(null);
		
		m_relationshipSelector.removeActionListener(this);
		m_relationshipValue.getDocument().removeDocumentListener(this);
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
		if (businessElement == null ||
				businessElement.getReferenceDatatype() == null) {
			return false;
		}
		
		// must have a relationship (name and value)
		if (m_selectRelationship) {
			if (getSemanticElementRelationshpName() == null || getSemanticElementRelationshpName().isEmpty() ||
					getRelationshpValue().isEmpty()) {
				return false;
			}
		}
		return true;
	}

	// get the selected business element
	public MdmiBusinessElementReference getMdmiBusinessElementReference() {
		Object item = m_businessElementSelector.getSelectedItem();
		if (item instanceof BusinessElementReferenceWrapper) {
			return ((BusinessElementReferenceWrapper)item).m_ber;
		}
		return null;
	}

	// get the selected Relationship's name
	public String getSemanticElementRelationshpName() {
		Object item = m_relationshipSelector.getSelectedItem();
		if (item instanceof RelationshipWrapper) {
			return ((RelationshipWrapper)item).m_relationship.getName();
		}
		return null;
	}

	// get the Relationship value text
	public String getRelationshpValue() {
		return m_relationshipValue.getText().trim();
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
		
		//save button state for next time
		s_append = m_appendBtn.isSelected();

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
				parentNode = (EditableObjectNode)findNodeOfType(seNode, ToBusinessElementSetNode.class);
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

				// Find the parent ToMessageElementSetNode node
				parentNode = (EditableObjectNode)findNodeOfType(seNode, ToMessageElementSetNode.class);
			}

		}

		if (m_replaceBtn.isSelected()) {
			// wipe out existing rule
			conversionRule.setRule(new String());
		}
		String rule = conversionRule.getRule();

		// if Isomorphic, the rule should be left blank
		if (!m_isoButton.isSelected()) {
			String language = m_ruleLanguageSelector.getLanguage();
			// Rule will be of the form: Set <target> to <source>
			String newRule = generateRuleText(language, conversionRule, seFieldName, beFieldName,
					getSemanticElementRelationshpName(), getRelationshpValue());

			// append new rule to existing rule
			if (rule != null && rule.length() > 0) {
				StringBuilder buf = new StringBuilder(rule);
				buf.append(CR_LF).append(newRule);
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

	// find a child node of the specified type
	private TreeNode findNodeOfType(TreeNode node, Class<?> nodeType) {
		TreeNode foundNode = null;
		for (int i=0; i<node.getChildCount(); i++) {
			TreeNode child = node.getChildAt(i);
			if (nodeType.isAssignableFrom(child.getClass())) {
				foundNode = child;
			} else {
				foundNode = findNodeOfType(child, nodeType);
			}

			if (foundNode != null) break;
		}
		return foundNode;
	}

	public static String generateRuleText(String language, ConversionRule theRule, String seFieldName, String beFieldName,
			String relationshipName, String relationshipValue) {

		String ruleText = null;
		if (RuleLanguageSelector.NRL.equals(language)) {
			ruleText = generateNRLRuleText(theRule, seFieldName, beFieldName, relationshipName, relationshipValue);
		} else {
			// default is Java Script
			ruleText = generateJSRuleText(theRule, seFieldName, beFieldName, relationshipName, relationshipValue);
		}

		return ruleText;
	}

	// NRL Rule will be of the form: Set <target> to <source>
	public static String generateNRLRuleText(ConversionRule theRule, String seFieldName, String beFieldName,
			String relationshipName, String relationshipValue) {

		String ruleName = theRule.getName();

		StringBuilder newRule = new StringBuilder();
		String target = "value";
		String source = "value";

		if (theRule instanceof ToBusinessElement) {
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

	// Get indentation (4 spaces per level)
	public static String getIndent(int level) {
		StringBuilder buf = new StringBuilder();
		for (int l=0; l<level; l++) {
			buf.append(SPACES);
		}
		return buf.toString();
	}
	// From Jeffrey Klann
	//	Generating a BE to SE rule:
	//
	//		- Same datatypes: No code needed.
	//		- Both complex datatypes: Existing wizard works.
	//		- BE is a complex data type and SE is a simple type (e.g., String):
	//		var source = From_PatientID.getValue();
	//		var target = value.getXValue();
	//		target.setValue(source.getValue('name-of-field'));
	//
	//
	//		Generating SE to BE rule:
	//		- Same datatypes: No code needed.
	//		- Both complex datatypes: Existing wizard works.
	//		- BE is a complex type and SE is a simple type (e.g., String):
	//		var source = value.value();
	//		var target = To_ProblemCode.getValue();
	//		target.setValue('code', source);
	//
	// JSP Rule will be of the form:
	//          target.setValue('<targetAttr>', source.getValue('<srcAttribute>'))
	//  Assign source: s1.s2.s3 to target t1.t2.t3.t4:
	//		var s1 = source.getXValue('s1').getValue();
	//
	//		if (null != s1) {
	//			// work our way down
	//			var s2 = s1.getXValue('s2').getValue();
	//			if (null != s2) {
	//
	//				// target(s)
	//				var t1 = target.getXValue('t1').getValue();
	//				if (null == t1) {
	//					// create T1
	//				}
	//
	//				var t2 = t1.getXValue('t2').getValue();
	//				if (null == t2) {
	//					// create T2
	//				}
	//
	//				var t3 = t2.getXValue('t3').getValue();
	//				if (null == t3) {
	//					// create T3
	//				}
	//
	//				// finally
	//				t3.setValue('t4', s2.getValue('s3'));
	//			}
	//
	//		}
	public static String generateJSRuleText(ConversionRule theRule, String seFieldName, String beFieldName,
			String relationshipName, String relationshipValue) {

		String ruleName = theRule.getName();

		StringBuilder newRule = new StringBuilder();

		MdmiDatatype seDatatype = theRule.getOwner().getDatatype();
		MdmiDatatype beDatatype = null;

		String srcVarDefinition;	// var source=....
		String targetVarDefinition;	// var target=....

		String srcFieldPath;
		String targetFieldPath;
		
		MdmiDatatype srcDatatype = null;
//		MdmiDatatype targetDatatype = null;

		// create variables and fields
		//   To BER                                To SE
		//   var source = value.value();           var source = ruleName.getValue();
		//   var target = ruleName.getValue();     var target = value.value();
		//                                            or if BE is a complex data type and SE is a simple type
		//		                                   var target = value.getXValue();

		if (theRule instanceof ToBusinessElement) {
			beDatatype = ((ToBusinessElement)theRule).getBusinessElement().getReferenceDatatype();

			srcVarDefinition = "var source = value.value();";

			// if BE is a simple data type and SE is a complex type, just use name
			if (!isComplex(beDatatype) && isComplex(seDatatype)) {
				targetVarDefinition = "var target = " + ruleName + ";";
			} else {
				targetVarDefinition = "var target = " + ruleName + ".getValue();";
			}
			srcDatatype = seDatatype;
//			targetDatatype = beDatatype;
			srcFieldPath = seFieldName;
			targetFieldPath = beFieldName;

		} else {
			beDatatype = ((ToMessageElement)theRule).getBusinessElement().getReferenceDatatype();

			// if BE is a simple data type and SE is a complex type, just use name
			if (!isComplex(beDatatype) && isComplex(seDatatype)) {
				srcVarDefinition = "var source = " + ruleName + ";";
			} else {
				srcVarDefinition = "var source = " + ruleName + ".getValue();";
			}

			// if BE is a complex data type and SE is a simple type, use "getXValue"
			if (isComplex(beDatatype) && !isComplex(seDatatype)) {
				targetVarDefinition = "var target = value.getXValue();";
			} else {
				targetVarDefinition = "var target = value.value();";
			}

			srcDatatype = beDatatype;
//			targetDatatype = seDatatype;
			srcFieldPath = beFieldName;
			targetFieldPath = seFieldName;
		}

		String existingRule = theRule.getRule();
		
		int depth = 0;
		
		// create relationship test
		// e.g.
		// var key = value.getRelation("Key");
		// if (key != null && key.value() == "Fill" ) {
		//    ....
		// }
		boolean hasRelationship = false;
		if (relationshipName != null && !relationshipName.isEmpty() &&
				relationshipValue != null && !relationshipValue.isEmpty())
		{
			hasRelationship = true;
		}
		
		if (hasRelationship) {
			// start new scope
			newRule.append("var key = value.getRelation(\"").append(relationshipName).append("\");").append(CR_LF);
			newRule.append("if (key != null && key.value() == \"").append(relationshipValue).append("\") {").append(CR_LF);
			
			depth++;	// gets own scope
			existingRule = new String();	// do this so we don't check for the existence of the variables
			
		} else if (beDatatype == seDatatype) {
			// easy case - Same datatypes: No code needed.
			return newRule.toString();
		}
		
		// make sure rule is non-null
		if (existingRule == null) {
			existingRule = new String();
		}

		// 1. Create source and target variables (if they don't already exist)
		String indent = getIndent(depth);
		if (!existingRule.contains(srcVarDefinition)) {
			newRule.append(indent).append(srcVarDefinition).append(CR_LF);
		}
		if (beDatatype != seDatatype && !existingRule.contains(targetVarDefinition)) {
			// if isomorphic (and relationship), we don't need a target
			newRule.append(indent).append(targetVarDefinition).append(CR_LF);
		}

		// parse field names on "." separator
		// (an empty string will result in an array of length 1, with one empty value)
		String[] srcFieldNames = srcFieldPath.split("\\.");
		String[] targetFieldNames = targetFieldPath.split("\\.");

		String prevSrcVarName = "source";


		// make a new scope, so we don't duplicate variables
		if (!hasRelationship &&
				srcFieldNames.length > 1 || targetFieldNames.length > 1) {
			newRule.append("{").append(CR_LF);
			depth++;
		}

		indent = getIndent(depth);

		if (beDatatype == seDatatype) {
			// this can only happen within a relationship
			//		 {ruleName}.setValue(source);
			newRule.append(indent).append(ruleName).append(".setValue(source);").append(CR_LF);
			depth++;
		} else {
			// look at each field name in the hierarchy
			//  (e.g. user.address.street)
			for (int s=0; s<srcFieldNames.length; s++) {

				String srcFieldName = srcFieldNames[s];
				if (srcDatatype instanceof DTComplex && !srcFieldName.isEmpty()) {
					// get field's datatype
					Field field = ((DTComplex)srcDatatype).getField(srcFieldName);
					if (field != null) {
						srcDatatype = field.getDatatype();
					} else {
						srcDatatype = null;
					}
				}
				String srcVarName = "from_" + srcFieldName;

				// we need to walk intermediate fields
				if (s < srcFieldNames.length-1) {
					//		var s2 = s1.getXValue('s2').getValue();
					//	    if (null != s2) {
					if (s > 0) newRule.append(CR_LF);
					newRule.append(indent).append("var ").append(srcVarName).append(" = ")
					.append(prevSrcVarName).append(".getXValue('").append(srcFieldName).append("').getValue();")
					.append(CR_LF);
					newRule.append(indent).append("if (").append(srcVarName).append(" != null) {").append(CR_LF);


				} else {
					newRule.append( buildTargetJSInfo(depth, seDatatype, beDatatype,
							prevSrcVarName, srcDatatype, srcFieldName, targetFieldNames) );
				}


				depth++;
				prevSrcVarName = srcVarName;
			}
		}

		// close parentheses
		for (int l=depth-1; l>0; l--) {
			newRule.append(getIndent(l-1)).append("}").append(CR_LF);
		}

		// trim CR/LF at end of line
		String newRuleString = newRule.toString();
		if (newRule.toString().endsWith(CR_LF)) {
			newRule.setLength(newRule.length() - CR_LF.length());
			newRuleString = newRule.toString();
		}
		return newRuleString;
	}

	// JavaScript rule target assignment
	private static String buildTargetJSInfo(int indentLevel, MdmiDatatype seDatatype, MdmiDatatype beDatatype,
			String prevSrcVarName, MdmiDatatype srcDatatype, String srcFieldName, String[] targetFieldNames) {

		String indent = getIndent(indentLevel);
		StringBuilder targetRule = new StringBuilder();
		
		String getSrcMethod = "getValue";
		if (srcDatatype  == DTSPrimitive.DATETIME) {
			getSrcMethod = "getDate";
		}

		String prevTargetVarName = "target";
		// last field - do targets
		if (targetFieldNames.length == 0 || (targetFieldNames.length == 1 && targetFieldNames[0].length() == 0)) {
			// no target fields
			//		target.setValue(s2.getValue('s3'));
			targetRule.append(indent).append(prevTargetVarName).append(".setValue(").append(prevSrcVarName);
			targetRule.append(".").append(getSrcMethod);
			if (srcFieldName.isEmpty()) {
				// .getValue() or .getDate()
				targetRule.append("()");
			} else {
				// .getValue('srcField') or .getDate('srcField')
				targetRule.append("('").append(srcFieldName).append("')");
			}
			targetRule.append(");").append(CR_LF);

		} else {
			for (int t=0; t<targetFieldNames.length; t++) {
				String targetFieldName = targetFieldNames[t];
				String targetVarName = "to_" + targetFieldName;

				if (t < targetFieldNames.length-1) {
					// create intermediate variables
					//	var t2 = t1.getXValue('t2').getValue();
					//  if (t2 == null) {
					//     var t2x = t1.getXValue('t2');
					//     t2 = new XDataStruct(t2x);
					//     t2x.setValue(t2);
					//  }
					if (t > 0) targetRule.append(CR_LF);
					targetRule.append(indent).append("var ").append(targetVarName).append(" = ").append(prevTargetVarName)
								.append(".getXValue('").append(targetFieldName).append("').getValue();").append(CR_LF);
					targetRule.append(indent).append("if (").append(targetVarName).append(" == null) {").append(CR_LF);

					// code to create variable
					String indent2 = indent + SPACES;
					targetRule.append(indent2).append("var xValue = ").append(prevTargetVarName)
							  .append(".getXValue('").append(targetFieldName).append("');").append(CR_LF);
					targetRule.append(indent2).append(targetVarName).append(" = new XDataStruct(xValue);").append(CR_LF);
					targetRule.append(indent2).append("xValue.setValue(").append(targetVarName).append(");").append(CR_LF);

					targetRule.append(indent).append("}").append(CR_LF);


				} else {
					// finally - do the assignment
					//      t3.setValue('t4', s2); or
					//		t3.setValue('t4', s2.getValue()); or t3.setValue('t4', s2.getDate()); 
					//		t3.setValue('t4', s2.getValue('s3')); or t3.setValue('t4', s2.getDate('s3'));
					targetRule.append(indent).append(prevTargetVarName).append(".setValue('")
						.append(targetFieldName).append("', ").append(prevSrcVarName);

					// if BE is a complex data type and SE is a simple type, just use source
					if (isComplex(beDatatype) && !isComplex(seDatatype)) {
						// do nothing
					} else if (srcFieldName.isEmpty()) {
						// .getValue() or .getDate()
						targetRule.append(".").append(getSrcMethod).append("()");
					} else {
						// .getValue('srcField') or .getDate('srcField')
						targetRule.append(".").append(getSrcMethod).append("('").append(srcFieldName).append("')");
					}
					targetRule.append(");").append(CR_LF);
				}

				prevTargetVarName = targetVarName;
			}
		}

		return targetRule.toString();
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
			m_beDatatype.setText(dataType.getTypeName());
		} else {
			m_beDatatype.setText(UNDEFINED_TYPE);
		}

		// fill fields (not for filtered)
		if (!m_filterByDatatypeButton.isSelected()) {
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
	
	/** Is this data type complex for the purpose of creating the rule? DateTime is considered complex */
	public static boolean isComplex(MdmiDatatype dataType) {
		if (dataType instanceof DTComplex || dataType == DTSPrimitive.DATETIME) {
			return true;
		}
		return false;
	}

	/** populate the combo box with the fields from the datatype */
	public static void populateFieldNames(JComboBox<Object> comboBox, MdmiDatatype dataType, String path) {
		if (dataType instanceof DTComplex) {
			for (Field field : ((DTComplex)dataType).getFields()) {
				MdmiDatatype fieldDataType = field.getDatatype();

				// check for recursion
				if (fieldDataType instanceof DTComplex) {
					if (fieldsInCombobox(comboBox, (DTComplex)fieldDataType)) {
						// warn and continue
						String message = "The field '" + field.getName() + "' of data type '" + dataType.getTypeName() +
								"' is recursive. It cannot be used for this operation.";
						JOptionPane.showMessageDialog(SelectionManager.getInstance().getEntityEditor(), message,
								"Recursive Data Types",
								JOptionPane.WARNING_MESSAGE);

						continue;
					}
				}

				// add field to combo box
				FieldNameComboBoxItem item = new FieldNameComboBoxItem(path, field);
				comboBox.addItem(item);

				// if field is a complex type, go further
				if (fieldDataType instanceof DTComplex) {
					populateFieldNames(comboBox, fieldDataType, item.getPath());
				}

			}
		}
	}

	/** Look for recursion by checking if the fields of this data type are already in the combo box */
	private static boolean fieldsInCombobox(JComboBox<Object> fieldNameComboBox, DTComplex dataType) {

		for (Field field : dataType.getFields()) {
			for (int i=0; i<fieldNameComboBox.getItemCount(); i++) {
				Object item = fieldNameComboBox.getItemAt(i);
				if (item instanceof FieldNameComboBoxItem &&
						((FieldNameComboBoxItem)item).m_field == field) {
					return true;
				}
			}
		}
		return false;
	}

	////////////////////////////////

	// Isomorphic/To BER/From BER change - change name text
	private class DirectionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			fillInName();

			Object source = e.getSource();
			// if Iso button was checked or un-checked, we need to repopulate
			if (source instanceof JRadioButton) {

				// disable/deselect filterByDatatypeButton if not ISO
				m_filterByDatatypeButton.setEnabled(source == m_isoButton);
				m_filterByDatatypeButton.setSelected(source == m_isoButton);

				if (source == m_isoButton || m_prevButton == m_isoButton) {
					// filtering has changed, so repopulate
					populateBusinessElements();
				}

				m_prevButton = (JRadioButton) source;

			} else if (source == m_filterByDatatypeButton) {
				populateBusinessElements();
			}

			setDirty(true);
		}
	}

	// General purpose ActionListener - enable Apply/Save
	@Override
	public void actionPerformed(ActionEvent e) {
		setDirty(true);
	}


	// General purpose propertyChangeListener - enable Apply/Save
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		setDirty(true);	// enable/disable OK button
	}

	// General purpose document listener - enable Apply/Save
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
		public Component getListCellRendererComponent(JList<?> list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			Icon icon = null;

			// append datatype
			if (value instanceof FieldNameComboBoxItem) {
				FieldNameComboBoxItem item = (FieldNameComboBoxItem)value;
				StringBuilder text = new StringBuilder(item.getPath());
				// show as "<path> : <datatype>"
				Field field = item.getField();
				if (field.getDatatype() != null) {
					text.append(" : ").append(field.getDatatype().getTypeName());
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
