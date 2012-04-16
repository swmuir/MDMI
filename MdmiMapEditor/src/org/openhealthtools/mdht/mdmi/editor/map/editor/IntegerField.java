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
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;

import org.openhealthtools.mdht.mdmi.editor.common.IntegerDocumentFilter;

/** An IEdtorField that uses a TextField with a number filter to display a integer */
public class IntegerField extends JTextField implements IEditorField, DocumentListener {
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.editor.Local");

	private GenericEditor m_parentEditor;

	private IntegerDocumentFilter m_integerFilter = new IntegerDocumentFilter();
	
	public IntegerField(GenericEditor parentEditor, int rows) {
		super(rows);
		
		m_parentEditor = parentEditor;

		setMinimumSize(getPreferredSize());	// keep from resizing
		((AbstractDocument)getDocument()).setDocumentFilter(m_integerFilter);
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	/** return the data as an integer */
	@Override
	public Object getValue() throws DataFormatException {
		String textValue = getText().trim();
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
		setText(value == null ? "" : value.toString());
	}
	
	@Override
	public void setReadOnly() {
		setEditable(false);
	}
	
	
	@Override
	public void highlightText(String text, Color highlightColor) {
		if (highlightColor == null) {
			highlightColor = UIManager.getColor("TextArea.background");	// restore
		}
		setBackground(highlightColor);
	}

	@Override
	public void addNotify() {
		super.addNotify();
		getDocument().addDocumentListener(this);
	}

	@Override
	public void removeNotify() {
		getDocument().removeDocumentListener(this);
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
}
