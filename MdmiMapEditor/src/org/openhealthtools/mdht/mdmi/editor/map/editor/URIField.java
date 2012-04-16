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
import java.net.URI;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** An IEditorField that shows a URI value as a TextField */
public class URIField extends JTextField implements IEditorField, DocumentListener {
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.editor.Local");

	private GenericEditor m_parentEditor;

	private TextSearcher  m_searcher = null;
	
	public URIField(GenericEditor parentEditor) {
		super();
		
		m_parentEditor = parentEditor;
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	/** return the data as a String */
	@Override
	public Object getValue() throws DataFormatException {
		String textValue = getText().trim();
		try {
			URI value = (textValue == null || textValue.length() == 0) ? null :
				URI.create(textValue);
			return value;
		} catch (IllegalArgumentException ex) {
			throw new DataFormatException("The text '" + textValue + "' is not a valid URI");
		}
	}

	@Override
	public void setDisplayValue(Object value) throws DataFormatException {
		setText(value == null ? "" : value.toString());
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
	
	@Override
	public void setReadOnly() {
		setEditable(false);
	}

	@Override
	public void highlightText(String text, Color highlightColor) {
		// use highlighters to highlight some of text
		if (m_searcher != null) {
			// clear old ones
			m_searcher.clearHighlights();
		}
		m_searcher = new TextSearcher(this, 
				new TextHighlighter.TextHighlighterPainter(highlightColor));
		m_searcher.search(text);
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
