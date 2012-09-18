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
package org.openhealthtools.mdht.mdmi.editor.map.editor;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;

import org.openhealthtools.mdht.mdmi.editor.common.IntegerDocumentFilter;
import org.openhealthtools.mdht.mdmi.editor.common.Standards;

/** An IEdtorField that uses a TextField with a number filter to display a integer.
 * There is an optional checkbox to set the field to "unbounded" (e.g. MAX_INTEGET) */
public class IntegerField extends JPanel implements IEditorField, DocumentListener, ActionListener {
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.editor.Local");

	private GenericEditor m_parentEditor;

	private IntegerDocumentFilter m_integerFilter = new IntegerDocumentFilter();
	private JTextField m_editField = null;
	private JCheckBox  m_unboundedBox;
	
	public IntegerField(GenericEditor parentEditor, int rows) {
		setLayout(new FlowLayout(FlowLayout.LEADING, Standards.LEFT_INSET, 0));
		
		m_editField = new JTextField(rows);
		m_unboundedBox = new JCheckBox("Unbounded");
		
		add(m_editField);
		
		m_parentEditor = parentEditor;

		setMinimumSize(getPreferredSize());	// keep from resizing
		((AbstractDocument)m_editField.getDocument()).setDocumentFilter(m_integerFilter);
	}
	
	public void addUnboundedBox() {
		add(m_unboundedBox);
		revalidate();
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	/** return the data as an integer */
	@Override
	public Object getValue() throws DataFormatException {

		if (m_unboundedBox.isSelected()) {
			return Integer.MAX_VALUE;
		}
		
		String textValue = m_editField.getText().trim();
		try {
			int value = (textValue == null || textValue.length() == 0) ? 0 :
				Integer.parseInt(textValue);
			return value;
		} catch (NumberFormatException ex) {
			// '{0}' is not a {1}.
			throw new DataFormatException(MessageFormat.format(s_res.getString("GenericEditor.dataFormatExceptionFormat"),
					textValue, "integer"));
		}
	}

	@Override
	public void setDisplayValue(Object value) throws DataFormatException {
		// select Unbounded box if the value is MAX_VALUE, and the box is showing
		if (value instanceof Integer && m_unboundedBox.isVisible() &&
				Integer.MAX_VALUE == ((Integer)value).intValue()) {
			m_editField.setText("");
			m_editField.setEditable(false);
			m_unboundedBox.setSelected(true);
		} else {
			m_unboundedBox.setSelected(false);
			m_editField.setText(value == null ? "" : value.toString());
		}
	}
	
	@Override
	public void setReadOnly() {
		m_editField.setEditable(false);
		m_unboundedBox.setEnabled(false);
	}
	
	
	@Override
	public void highlightText(String text, Color highlightColor) {
		if (highlightColor == null) {
			highlightColor = UIManager.getColor("TextArea.background");	// restore
		}
		m_editField.setBackground(highlightColor);
	}

	@Override
	public void addNotify() {
		super.addNotify();
		m_editField.getDocument().addDocumentListener(this);
		m_unboundedBox.addActionListener(this);
	}

	@Override
	public void removeNotify() {
		m_editField.getDocument().removeDocumentListener(this);
		m_unboundedBox.removeActionListener(this);
		super.removeNotify();
	}

	
	/////////////////////////////////////////
	//    Document Listener Methods        //
	/////////////////////////////////////////

	@Override
	public void changedUpdate(DocumentEvent e) {
		m_parentEditor.setModified(true);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		m_parentEditor.setModified(true);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		m_parentEditor.setModified(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// enable/disable edit field
		if (m_unboundedBox.isSelected()) {
			m_editField.setText("");
			m_editField.setEditable(false);
		} else {
			m_editField.setEditable(true);
		}
		m_parentEditor.setModified(true);
		
	}
}
