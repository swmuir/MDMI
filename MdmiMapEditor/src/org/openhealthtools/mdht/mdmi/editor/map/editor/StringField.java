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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;

/** An IEditorField that shows String values as a text area */
public class StringField extends JPanel implements IEditorField, DocumentListener {

	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.editor.Local");

	private GenericEditor m_parentEditor;
	private JTextArea     m_textArea;
	private JLabel			 m_defaultValueDisplay = new JLabel("");

	private TextSearcher  m_searcher = null;
	
	public StringField(GenericEditor parentEditor, int rows, int columns) {
		setLayout(new BorderLayout());
		m_textArea = new CustomTextArea(rows, columns);
		
		m_parentEditor = parentEditor;

		m_textArea.setFont(Standards.DEFAULT_FONT);
		m_textArea.setLineWrap(true);
		m_textArea.setWrapStyleWord(true);
//		use TextField's border, since the TextArea doesn't have one
		m_textArea.setBorder(UIManager.getBorder("TextField.border"));
		add(m_textArea, BorderLayout.CENTER);
		
		add(m_defaultValueDisplay, BorderLayout.EAST);
		m_defaultValueDisplay.setForeground(Color.darkGray);
		m_defaultValueDisplay.setVisible(false);
	}
	
	private class CustomTextArea extends JTextArea {
		public CustomTextArea(int rows, int columns) {
			super(rows, columns);
		}

		/** ignore Tab key as text entry - use it to move focus to next field */
		@Override
		protected void processComponentKeyEvent( KeyEvent e ) {  
			if (e.getID() == KeyEvent.KEY_PRESSED &&  e.getKeyCode() == KeyEvent.VK_TAB ) {
				e.consume();
				if (e.isShiftDown()) {
					transferFocusBackward();
				} else {
					transferFocus();
				}
			}  
			else {  
				super.processComponentKeyEvent( e );  
			}  
		} 
	}
	
	@Override
	public JComponent getComponent() {
		return this;
	}

	/** return the data as a String */
	@Override
	public Object getValue()  {
		return m_textArea.getText().trim();
	}

	@Override
	public void setDisplayValue(Object value) {
		m_textArea.setText(value == null ? "" : value.toString());
	}
	
	@Override
	public void setReadOnly() {
		m_textArea.setEditable(false);
	}
	
	@Override
	public void highlightText(String text, Color highlightColor) {
		// use highlighters to highlight some of text
		if (m_searcher != null) {
			// clear old ones
			m_searcher.clearHighlights();
		}
		m_searcher = new TextSearcher(m_textArea, 
				new TextHighlighter.TextHighlighterPainter(highlightColor));
		m_searcher.search(text);
	}

	@Override
	public void addNotify() {
		super.addNotify();
		m_textArea.getDocument().addDocumentListener(this);
	}

	@Override
	public void removeNotify() {
		m_textArea.getDocument().removeDocumentListener(this);
		super.removeNotify();
	}
	
	/////////////////////////////////////////
	//    Document Listener Methods        //
	/////////////////////////////////////////

	@Override
	public void changedUpdate(DocumentEvent e) {
		documentChanged();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		documentChanged();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		documentChanged();
	}
	
	private void documentChanged() {
		m_parentEditor.setModified(true);
		if (m_searcher != null) {
			m_searcher.clearHighlights();
		}
	}
}
