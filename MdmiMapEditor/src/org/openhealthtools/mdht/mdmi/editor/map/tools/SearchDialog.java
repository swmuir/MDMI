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
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.StatusPanel;
import org.openhealthtools.mdht.mdmi.editor.map.console.TextMatchLink;
import org.openhealthtools.mdht.mdmi.editor.map.editor.TextSearcher;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNode;
import org.openhealthtools.mdht.mdmi.model.Bag;
import org.openhealthtools.mdht.mdmi.model.Choice;
import org.openhealthtools.mdht.mdmi.model.ConversionRule;
import org.openhealthtools.mdht.mdmi.model.DTCChoice;
import org.openhealthtools.mdht.mdmi.model.DTCStructured;
import org.openhealthtools.mdht.mdmi.model.DTExternal;
import org.openhealthtools.mdht.mdmi.model.DTSDerived;
import org.openhealthtools.mdht.mdmi.model.DTSEnumerated;
import org.openhealthtools.mdht.mdmi.model.DTSPrimitive;
import org.openhealthtools.mdht.mdmi.model.DataRule;
import org.openhealthtools.mdht.mdmi.model.EnumerationLiteral;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.LeafSyntaxTranslator;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MdmiDomainDictionaryReference;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.MessageModel;
import org.openhealthtools.mdht.mdmi.model.MessageSyntaxModel;
import org.openhealthtools.mdht.mdmi.model.Node;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.SemanticElementRelationship;
import org.openhealthtools.mdht.mdmi.model.SemanticElementSet;
import org.openhealthtools.mdht.mdmi.model.ToBusinessElement;
import org.openhealthtools.mdht.mdmi.model.ToMessageElement;

/** Display a non-modal dialog enabling the user to search for elements that match
 * the entered text.
 * @author Conway
 *
 */
public class SearchDialog extends BaseDialog {
	/** Resource for localization */
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");
	
	// remembered data
	private static String s_lastText = "";
	private static List<String> s_checkedBoxes = new ArrayList<String>();
	private static String  s_searchField = s_res.getString("SearchDialog.name");
	private static boolean s_caseSensitive = false;
	
	
	private JTextField m_searchText = new JTextField();
	
	private ButtonGroup  m_fieldGroup;
	private JRadioButton m_nameField = new JRadioButton(s_res.getString("SearchDialog.name"));
	private JRadioButton m_descriptionField = new JRadioButton(s_res.getString("SearchDialog.description"));
	private JRadioButton m_ruleField = new JRadioButton(s_res.getString("SearchDialog.rule"));
	
	private JCheckBox m_caseSensitiveBox = new JCheckBox(s_res.getString("SearchDialog.caseSensitive"), 
			s_caseSensitive);
	
	private List<JCheckBox> m_checkBoxes = new ArrayList<JCheckBox>();
	private Map<JCheckBox, List<JCheckBox>> m_links = new HashMap<JCheckBox, List<JCheckBox>>();
	
	// Listeners
	private ButtonListener m_buttonListener = new ButtonListener();
	private TextListener	  m_textListener = new TextListener();

	public SearchDialog(Frame owner) {
		super(owner, BaseDialog.OK_CANCEL_OPTION);
		buildUI();
		setModal(false);
	}
	
	
	@Override
	public void addNotify() {
		super.addNotify();

		m_searchText.getDocument().addDocumentListener(m_textListener);
		for (JCheckBox checkBox: m_checkBoxes) {
			checkBox.addActionListener(m_buttonListener);
		}
	}
	
	@Override
	public void removeNotify() {
		m_searchText.getDocument().removeDocumentListener(m_textListener);
		for (JCheckBox checkBox: m_checkBoxes) {
			checkBox.removeActionListener(m_buttonListener);
		}
		
		super.removeNotify();
	}
	
	private void buildUI() {
		setTitle(s_res.getString("SearchDialog.title"));
		
		JPanel mainPanel = new JPanel(new GridBagLayout());
		//  Search Text:
		//  [________________________________] [x] Case sensitive
		//  (* = any string)    
		//
		//   -Search In ----------------------------------            
		//  | (o) Name     ( ) Description                |
		//   ---------------------------------------------
		//
		//   -Search Element Types ----------------------
		//  | [x] Message Group      [x] Semantic Element |
		//  | .....                                       |
		//   ---------------------------------------------
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets  = Standards.getInsets();
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		
		// Search text widget
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(getSearchTextPanel(), gbc);
		
		// Case Sensitive button
		gbc.gridx++;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		mainPanel.add(m_caseSensitiveBox, gbc);
		
		// Name/Description choice
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 1;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(getNameAndDescriptionButtons(), gbc);
		
		// Search In
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1;
		gbc.weightx = 1;
		mainPanel.add(getSearchElementsPanel(), gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		
		pack();
	}
	
	private JPanel getSearchTextPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets.top = Standards.TOP_INSET;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		
		
		// Search Text:
		panel.add(new JLabel(s_res.getString("SearchDialog.searchTextLabel")), gbc);
		gbc.gridy++;
		
		// text entry
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		m_searchText.setText(s_lastText);
		panel.add(m_searchText, gbc);
		gbc.gridy++;
		
		// (* = any string) 
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		panel.add(new JLabel(s_res.getString("SearchDialog.searchHint")), gbc);
		
		return panel;
	}

	/** return a panel with the Name and Description radio buttons */
	private JPanel getNameAndDescriptionButtons() {
		m_fieldGroup = new ButtonGroup();
		m_fieldGroup.add(m_nameField);
		m_fieldGroup.add(m_descriptionField);
		m_fieldGroup.add(m_ruleField);
		
		// pre-select
		for (Enumeration<AbstractButton> buttons=m_fieldGroup.getElements(); buttons.hasMoreElements(); ) {
			AbstractButton button = buttons.nextElement();
			if (s_searchField.equals(button.getText())) {
				button.setSelected(true);
				setDirty(true);
				break;
			}
		}
		
		JPanel panel = new JPanel(new GridLayout(1,0));
		panel.add(m_nameField);
		panel.add(m_descriptionField);
		panel.add(m_ruleField);

		// Search In
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				s_res.getString("SearchDialog.searchInLabel")));
		return panel;
	}
	
	/** return a panel with check boxes for all possible element types */
	private JPanel getSearchElementsPanel() {

      // [ ] Message Group	  [ ] Semantic Element Set	  [ ] Message Syntax Model
      //
      // [ ] Data Types		  [ ] Semantic Element		  [ ] Syntax Node
      //   [ ] DTCchoice											 [ ] Bag
      //   [ ] DTSDerived	  [ ] Semantic Element Rel		 [ ] Choice
      //   [ ] DTCStructured										 [ ] Leaf
      //   [ ] ...			  [ ] Conversion Rule					
      //   [ ] ...			    [ ] To Message Element	  [ ] Referent Index Ref
      //							    [ ] To Business Element
      // [ ] Message Model										  [ ] Business Element Ref

		JPanel column1 = new JPanel(new GridBagLayout());
		JPanel column2 = new JPanel(new GridBagLayout());
		JPanel column3 = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 1;
		gbc.weighty = 1;
		
		JPanel panel = new JPanel(new GridBagLayout());
		panel.add(column1, gbc);
		gbc.gridx++;
		panel.add(column2, gbc);
		gbc.gridx++;
		panel.add(column3, gbc);

		int subItemInset = 4;
		
		// Column 1
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets.top = 0;
		addCheckBox(column1, gbc, MessageGroup.class);

		gbc.insets.top = Standards.TOP_INSET;
		JCheckBox parent = addCheckBox(column1, gbc, MdmiDatatype.class);
		gbc.insets.top = 0;
		linkWithParent(parent, addCheckBox(column1, gbc, DTCChoice.class) );
		gbc.insets.top = subItemInset;
		linkWithParent(parent, addCheckBox(column1, gbc, DTCStructured.class) );
		linkWithParent(parent, addCheckBox(column1, gbc, DTExternal.class) );
		linkWithParent(parent, addCheckBox(column1, gbc, DTSDerived.class) );
		linkWithParent(parent, addCheckBox(column1, gbc, DTSEnumerated.class) );
		linkWithParent(parent, addCheckBox(column1, gbc, DTSPrimitive.class) );

		gbc.insets.top = Standards.TOP_INSET;
		addCheckBox(column1, gbc, Field.class);
		addCheckBox(column1, gbc, EnumerationLiteral.class);
		
		// Column 2
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets.top = 0;
		addCheckBox(column2, gbc, DataRule.class);

		gbc.insets.top = Standards.TOP_INSET;
		addCheckBox(column2, gbc, MessageModel.class);
		
		addCheckBox(column2, gbc, SemanticElementSet.class);
		
		addCheckBox(column2, gbc, SemanticElement.class);
		
		addCheckBox(column2, gbc, SemanticElementRelationship.class);
		
		parent = addCheckBox(column2, gbc, ConversionRule.class);
		gbc.insets.top = 0;
		linkWithParent(parent, addCheckBox(column2, gbc, ToBusinessElement.class) );
		gbc.insets.top = subItemInset;
		linkWithParent(parent, addCheckBox(column2, gbc, ToMessageElement.class) );
		
		// Column 3
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets.top = 0;
		addCheckBox(column3, gbc, MessageSyntaxModel.class);

		gbc.insets.top = Standards.TOP_INSET;
		parent = addCheckBox(column3, gbc, Node.class);
		gbc.insets.top = 0;
		linkWithParent(parent, addCheckBox(column3, gbc, Bag.class) );
		gbc.insets.top = subItemInset;
		linkWithParent(parent, addCheckBox(column3, gbc, Choice.class) );
		linkWithParent(parent, addCheckBox(column3, gbc, LeafSyntaxTranslator.class) );

		gbc.insets.top = Standards.TOP_INSET;
		addCheckBox(column3, gbc, MdmiDomainDictionaryReference.class);

		addCheckBox(column3, gbc, MdmiBusinessElementReference.class);
				
		// Search Element Types
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				s_res.getString("SearchDialog.searchTypesLabel")));
		return panel;
	}


	/**
	 * @param panel
	 */
	private JCheckBox addCheckBox(JPanel panel, GridBagConstraints gbc, Class<?> objectType) {
		String text = ClassUtil.beautifyName(objectType);

		JCheckBox checkBox = new JCheckBox(text);
		m_checkBoxes.add(checkBox);
		if (s_checkedBoxes.contains(text)) {
			checkBox.setSelected(true);	// pre-set
			setDirty(true);
		}
		panel.add(checkBox, gbc);
		gbc.gridy++;
		return checkBox;
	}

	
	/** Link this check box to the parent, so that when the parent is selected or de-selected,
	 * this check box is too
	 * @param child
	 */
	private void linkWithParent(JCheckBox parent, JCheckBox child) {
		// build link
		List<JCheckBox> list = m_links.get(parent);
		if (list == null) {
			// first link
			list = new ArrayList<JCheckBox>();
			m_links.put(parent, list);
		}
		list.add(child);
		
		// show child indented
		child.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
	}


	@Override
	public boolean isDataValid() {
		boolean valid = false;

		// need text
		String textToMatch = m_searchText.getText().trim();
		valid = (textToMatch.length() > 0);

		// at least one checkbox needs to be selected as well
		if (valid) {
			valid = false;
			for (JCheckBox checkBox : m_checkBoxes) {
				if (checkBox.isSelected()) {
					valid = true;
					break;
				}
			}
		}
		return valid;
	}
	
	@Override
	protected void okButtonAction() {
		
		// save settings
		saveSettings();
		

		String searchText = m_searchText.getText().trim();
		int flags = Pattern.CASE_INSENSITIVE;
		if (m_caseSensitiveBox.isSelected()) {
			flags = 0;
		}
		Pattern pattern = Pattern.compile(toRegex(searchText), flags);
		
		String fieldName = getSearchFieldName();	// Name, Description or Rule
		
		List<EditableObjectNode> matchingNodes = new ArrayList<EditableObjectNode>();
		
		// perform search
		JTree tree = SelectionManager.getInstance().getEntitySelector().getMessageElementsTree();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
		for (Enumeration<?> en = root.preorderEnumeration(); en != null && en.hasMoreElements();) {
			TreeNode node = (TreeNode)en.nextElement();
			if (node instanceof EditableObjectNode) {
				EditableObjectNode editNode = (EditableObjectNode)node;
				if (patternMatches(pattern, editNode, fieldName)) {
					matchingNodes.add(editNode);
				}
			}
		}

		showResults(searchText, matchingNodes);
		
		super.okButtonAction();
	}
	
	private boolean patternMatches(Pattern pattern, EditableObjectNode node, String fieldName) {
		// only care about the ones that can be written to
		if (node.isEditable()) {
			Object userObject = node.getUserObject();
			String elementType = ClassUtil.beautifyName(userObject.getClass());
			// is this type selected
			if (isCheckBoxSelected(elementType)) {

				//we need to get the value from the user object
				String fieldValue = getFieldValue(fieldName, userObject);
				
				// check if it matches the user expression
				if (fieldValue != null && pattern.matcher(fieldValue).matches()) {
					return true;
				}

			}
		}
		return false;
	}

	/** Invoke the getFieldName() method on this user object (assuming it returns a string),
	 * and return the result.
	 * @param fieldName	Attribute name
	 * @param userObject
	 * @return
	 */
	private String getFieldValue(String fieldName, Object userObject) {
		String value = null;
		
		//we need to get the value from the user object by calling the "getFieldName()" method
		try {
			String methodName = "get" + Character.toUpperCase(fieldName.charAt(0))
				+ fieldName.substring(1);
			Method getMethod = null;
			try {
				getMethod = userObject.getClass().getMethod(methodName);
			} catch (Exception e) {
				return null;  // no getXXX() method
			}
			
			if (getMethod != null) {
				Object descr = getMethod.invoke(userObject);
				value = (descr == null ? null : descr.toString());
				
				// special handling
				if (value != null) {
					value = value.replaceAll("\\r", "");
					value = value.replaceAll("\\n", "");
				}
			}
		} catch (Exception e) {
			SelectionManager.getInstance().getStatusPanel().writeException(e);
		}
		return value;
	}

	/** Display the results of the search in the console window
	 * @param searchText
	 * @param matchingNodes
	 */
	private void showResults(String searchText,
			List<EditableObjectNode> matchingNodes) {
		
		StatusPanel statusPanel = SelectionManager.getInstance().getStatusPanel();
		statusPanel.clearConsole();
		String statusMsg;
		if (matchingNodes.size() == 0) {
			// No elements with 'text' in the Name were found
			statusMsg = MessageFormat.format(s_res.getString("SearchDialog.noMatchFound"),
					searchText, getSearchFieldName());
		} else {
			// Found {0} matches for 'text'
			statusMsg = MessageFormat.format(s_res.getString("SearchDialog.matchesFound"),
					matchingNodes.size(), searchText);
		}
		statusPanel.writeConsole(statusMsg);
		
			
		for (EditableObjectNode referenceNode : matchingNodes) {
			// - Type_of_Object <LINK>
			statusMsg = "   - " + referenceNode.getDisplayType();
			TextMatchLink link = new TextMatchLink(referenceNode.getUserObject(),
					referenceNode.getDisplayName(), getSearchFieldName(), searchText);
			statusPanel.writeConsoleLink(statusMsg, link, "");
		}
	}

	/** get the name of the field we're searching (i.e. "Name" or "Description")
	 * @return
	 */
	private String getSearchFieldName() {
		String searchField = m_nameField.getText();
		for (Enumeration<AbstractButton> buttons=m_fieldGroup.getElements(); buttons.hasMoreElements(); ) {
			AbstractButton button = buttons.nextElement();
			if (button.isSelected()) {
				searchField = button.getText();
				break;
			}
		}
		return searchField;
	}


	/** Save current settings for next time the dialog is displayed
	 * @param textToMatch
	 */
	private void saveSettings() {
		s_lastText = m_searchText.getText().trim();
		s_caseSensitive = m_caseSensitiveBox.isSelected();
		
		for (Enumeration<AbstractButton> buttons=m_fieldGroup.getElements(); buttons.hasMoreElements(); ) {
			AbstractButton button = buttons.nextElement();
			if (button.isSelected()) {
				s_searchField = button.getText();
				break;
			}
		}
		
		s_checkedBoxes.clear();
		for (JCheckBox checkBox : m_checkBoxes) {
			if (checkBox.isSelected()) {
				s_checkedBoxes.add(checkBox.getText());
			}
		}
	}
	
	/** Convert the user-entered text into a regular expression */
	private static String toRegex(String text) {
		StringBuilder buf = new StringBuilder();
		// start with the results from TextSearcher
		buf.append(TextSearcher.toRegex(text));

		// begin and end with ".*",
		if (!buf.toString().startsWith(".*")) {
			buf.insert(0, ".*");
		}
		if (!buf.toString().endsWith(".*")) {
			buf.append(".*");
		}
		return buf.toString();
	}
	
	/** Determine if the checkbox with the provided text is selected */
	private boolean isCheckBoxSelected(String text) {
		for (JCheckBox checkBox : m_checkBoxes) {
			if (text.equals(checkBox.getText())) {
				return checkBox.isSelected();
			}
		}
		return false;
	}
	

	////////////////////////////////////////////////////
	private class TextListener implements DocumentListener {
		@Override
		public void changedUpdate(DocumentEvent e) {
			textChanged();
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			textChanged();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			textChanged();
		}
		
		private void textChanged() {
			setDirty(true);
		}
	}
	
	private class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JCheckBox parent = (JCheckBox)e.getSource();
			
			List<JCheckBox> linkedBoxes = m_links.get(parent);
			if (linkedBoxes != null) {
				// select all linked check boxes
				boolean state = parent.isSelected();
				for (JCheckBox checkBox : linkedBoxes) {
					checkBox.setSelected(state);
				}
			}
			setDirty(true);
		}
	}
	
}
