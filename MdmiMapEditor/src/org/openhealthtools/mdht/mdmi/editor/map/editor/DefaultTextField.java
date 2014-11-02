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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;

/** An IEditorField that shows String values in a text field. The default value is
 * displayed in the field in gray, and will disappear when text is entered by the user */
public class DefaultTextField extends JTextField implements IEditorField, FocusListener, DocumentListener {

	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.editor.Local");

	private GenericEditor m_parentEditor;

	private TextSearcher  m_searcher = null;
	
	private String  m_defaultValue = "";
	private boolean m_showingDefault = false;
	
	public DefaultTextField(GenericEditor parentEditor, String defaultValue) {
		m_parentEditor = parentEditor;
		setDefaultValue(defaultValue);
		// initialize to default
		setText("");
	}
	 
   @Override
	protected Document createDefaultModel() {
	      return new DefaultDocument();
   }

	@Override
	public JComponent getComponent() {
		return this;
	}
	
	@Override
	public String getText() {
		if (m_showingDefault) {
			return "";
		}
		return super.getText();
	}

	/** return the data as a String */
	@Override
	public Object getValue()  {
		return getText().trim();
	}

	@Override
	public void setDisplayValue(Object value) {
		setText(value == null ? "" : value.toString());
	}
	
	@Override
	public void setReadOnly() {
		setEditable(false);
	}
	
	/** Return the default value that is displayed */
	public String getDefaultValue() {
		return m_defaultValue;
	}
	
	/** Set the default value that is displayed when no text is present */
	public void setDefaultValue(String defaultValue) {
		String oldValue = m_defaultValue;
		m_defaultValue = defaultValue == null ? "(default)" : (defaultValue+" (default)");
		if (m_showingDefault && !m_defaultValue.equals(oldValue)) {
			// replace default text if changed
			if (getDocument() instanceof DefaultDocument) {
				try {
					boolean modified = m_parentEditor.isModified();
					((DefaultDocument)getDocument()).showDefaultValue();	// this will set the dirty flag
					m_parentEditor.setModified(modified);
				} catch (BadLocationException ex) {
					SelectionManager.getInstance().getStatusPanel().writeException(ex);
				}
			}
		}
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

	@Override
	public void addNotify() {
		super.addNotify();
		this.getDocument().addDocumentListener(this);
		this.addFocusListener(this);
	}

	@Override
	public void removeNotify() {
		this.getDocument().removeDocumentListener(this);
		this.removeFocusListener(this);
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
	
	private class DefaultDocument extends PlainDocument {
		private Color m_normalColor  = UIManager.getColor("TextField.foreground");
		private Color m_defaultColor = UIManager.getColor("TextField.inactiveForeground");
		
		@Override
		public void replace(int offset, int length, String text,
				AttributeSet attrs) throws BadLocationException {
			super.replace(offset, length, text, attrs);
			
			if (text.length() == 0 && getLength() == 0) {
				// empty - show default data
				showDefaultValue();
			}
		}

		@Override
		public void insertString(int offset, String text, AttributeSet a)
				throws BadLocationException {
			if (m_showingDefault && text.length() > 0) {
				// user-entered text, remove display of default data
				removeDefaultValue();
			} else if (text.length() == 0 && getLength() == 0) {
				// empty - show default data
				showDefaultValue();
			}
			super.insertString(offset, text, a);
		}
		
		@Override
		public void remove(int offset, int length) throws BadLocationException {
			super.remove(offset, length);
			// if wiping out all text, use default
			if (getLength() == 0) {
				showDefaultValue();
			}
		}
		
		/** Show the default value as the display */
		private void showDefaultValue() throws BadLocationException {
			if (getLength() > 0) {
				super.remove(0, getLength());
			}
			m_showingDefault = true;
			setForeground(m_defaultColor);
			super.insertString(0, m_defaultValue, null);
			setCaretPosition(0);
		}

		/** Remove the default value from the display
		 */
		private void removeDefaultValue() throws BadLocationException {
			m_showingDefault = false;
			setForeground(m_normalColor);
			super.remove(0, getLength());
		}

	}
	
	/////////////////////////////////////////////
	//  Focus Listener Interface
	//////////////////////////////////////////////
	@Override
	public void focusGained(FocusEvent e) {
		 // if showing default text - highlight text
		if (m_showingDefault) {
			select(0, getDocument().getLength());
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
	}
}
