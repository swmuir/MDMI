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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.URI;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MdmiModelTree;
import org.openhealthtools.mdht.mdmi.model.DTExternal;

/** A dialog used for selecting a MessageGroup, MessageModel, and one or more
 * Semantic Elements within that message model
 * @author Conway
 *
 */
public class GenerateTypeSpecDialog extends BaseDialog implements DocumentListener {

	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");
	
	private static final String NAME_REQUIRED = "<Enter Name>";

	private DTExternal m_datatype = null;

	private JTextField  m_name = new JTextField();
	private JTextField  m_valueSet = new JTextField();
	private JTextField  m_domainName = new JTextField();
	private JLabel      m_typeSpec = new JLabel(" ");

	public static final String[][] urlEscapes = {
		{ " ", "%20" },
		{ "$", "%24" },
		{ "&", "%26" },
		{ "`", "%60" },
		{ ":", "%3A" },
		{ "<", "%3C" },
		{ ">", "%3E" },
		{ "[", "%5B" },
		{ "]", "%5D" },
		{ "{", "%7B" },
		{ "}", "%7D" },
		{ "\"", "%22" },
		{ "+", "%2B" },
		{ "#", "%23" },
		{ "%", "%25" },
		{ "@", "%40" },
		{ "/", "%2F" },
		{ ";", "%3B" },
		{ "=", "%3D" },
		{ "?", "%3F" },
		{ "\\", "%5C" },
		{ "^", "%5E" },
		{ "|", "%7C" },
		{ "~", "%7E" },
		{ "'", "%27" },
		{ ",", "%2C" },
	};
	
	// fix URL string by encoding special characters
	public static String normalizeURL(String name) {
		StringBuilder newName = new StringBuilder();
		for (int i=0; i<name.length(); i++) {
			char c = name.charAt(i);
			// check if c is in list
			boolean isSpecial = false;
			for (String[] pair : urlEscapes) {
				if (c == pair[0].charAt(0)) {
					isSpecial = true;
					newName.append(pair[1]);
					break;
				}
			}
			if (!isSpecial) {
				newName.append(c);
			}
		}
		
		return newName.toString();
	}

	public GenerateTypeSpecDialog(Frame owner, DTExternal dataType) {
		super(owner, BaseDialog.OK_CANCEL_OPTION);
		m_datatype = dataType;
		buildUI();
		setTitle(s_res.getString("GenerateTypeSpecDialog.title"));
		pack(new Dimension(500,300));
	}

	
	private void buildUI() {
		// 
		// Datatype Name:  [__________________]
		// Value Set:      [__________________]
		// Domain Name:    [__________________]
		//  -- Type Spec -----------------------------------------------------------
		// | mdmi://datatypes/deferredEnum?type=&valueSet=vvvvvv&domainName=ddddddd |
		//  ------------------------------------------------------------------------ 
		
		
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = Standards.getInsets();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 0;
		gbc.weighty = 0;

		// Datatype Name: 
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("GenerateTypeSpecDialog.datatypeName")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.left = 0;
		if (m_datatype.getName() != null) {
			m_name.setText(m_datatype.getName());
		} else {
			m_name.setText(NAME_REQUIRED);
		}
		mainPanel.add(m_name, gbc);
		gbc.insets.left = Standards.LEFT_INSET;

		// Value Set: 
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("GenerateTypeSpecDialog.valueSet")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.left = 0;
		mainPanel.add(m_valueSet, gbc);
		gbc.insets.left = Standards.LEFT_INSET;

		// Domain Name
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("GenerateTypeSpecDialog.domainName")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.left = 0;
		mainPanel.add(m_domainName, gbc);
		gbc.insets.left = Standards.LEFT_INSET;
		
		
		//GenerateTypeSpecDialog.typeSpec
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(BorderLayout.CENTER, m_typeSpec);
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
				s_res.getString("GenerateTypeSpecDialog.typeSpec")));

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 1;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.left = Standards.LEFT_INSET;
		gbc.insets.right = Standards.LEFT_INSET;
		mainPanel.add(panel, gbc);
		
		
		// add listeners
		m_name.getDocument().addDocumentListener(this);
		m_valueSet.getDocument().addDocumentListener(this);
		m_domainName.getDocument().addDocumentListener(this);
		m_typeSpec.setForeground(Color.blue);
		
		setDirty(true);	// allow OK button
		
		getContentPane().add(mainPanel, BorderLayout.CENTER);
	}
	



	@Override
	public void dispose() {
		m_name.getDocument().removeDocumentListener(this);
		m_valueSet.getDocument().removeDocumentListener(this);
		m_domainName.getDocument().removeDocumentListener(this);
		super.dispose();
	}
	
	public String getDatatypeName() {
		String theName = m_name.getText().trim();
		if (NAME_REQUIRED.equals(theName)) {
			theName = "";
		}
		return theName;
	}

	@Override
	public boolean isDataValid() {
		// must have a name
		if (getDatatypeName().length() == 0) {
			return false;
		}
		// must have a domain or value
		if (m_valueSet.getText().trim().length() == 0 &&
				m_domainName.getText().trim().length() == 0) {
			return false;
		}

		return true;
	}
	
	
	@Override
	protected void okButtonAction() {
		String typeSpecString = createTypeSpecString(getDatatypeName(),
				m_valueSet.getText().trim(), m_domainName.getText().trim());
		try {
			URI typeSpec = URI.create(typeSpecString);
			m_datatype.setTypeSpec(typeSpec);
			m_datatype.setTypeName(getDatatypeName());
			
			// refresh 
			// if open, save and close
			SelectionManager.getInstance().getEntityEditor().stopEditing(m_datatype);

			MdmiModelTree entitySelector = SelectionManager.getInstance().getEntitySelector();
			DefaultMutableTreeNode treeNode = entitySelector.findNode(m_datatype);
			
			//open it
			if (treeNode != null) {
				entitySelector.selectNode(treeNode);
				SelectionManager.getInstance().editItem(treeNode);
			}
						
		} catch (IllegalArgumentException ex) {
			String message = "Unable to create URI  \"" + typeSpecString + "\".";
			JOptionPane.showMessageDialog(this, message,
					s_res.getString("GenerateTypeSpecDialog.title"),
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		super.okButtonAction();
	}

	
	// create TypeSpec string
	public static String createTypeSpecString(String datatypeName, String valueSet, String domainName) {
		if (datatypeName == null) {
			datatypeName = "<unknown>";
		}
		StringBuilder buf = new StringBuilder();
		buf.append("mdmi://datatypes/deferredEnum?type=").append(datatypeName);
		if (!valueSet.isEmpty()) {
			buf.append("&valueSet=").append(normalizeURL(valueSet));
		}
		if (!domainName.isEmpty()) {
			buf.append("&domainName=").append(normalizeURL(domainName));
		}
		
		return buf.toString();
	}
	
	private void dataEntered()
	{
		setDirty(true);
		// set text
		String text = "  ";
		if (isDataValid()) {
			text = createTypeSpecString(getDatatypeName(), m_valueSet.getText().trim(), m_domainName.getText().trim());
		}
		m_typeSpec.setText(text);
		m_typeSpec.invalidate();
	}
	
	///////////////////////////////////////////
	//  Document Listener Methods

	@Override
	public void insertUpdate(DocumentEvent e) {
		dataEntered();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		dataEntered();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		dataEntered();
	}


}
