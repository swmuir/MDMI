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
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import net.sourceforge.nrl.parser.NRLError;
import net.sourceforge.nrl.parser.model.*;

import org.openhealthtools.mdht.mdmi.IExpressionInterpreter;
import org.openhealthtools.mdht.mdmi.engine.NrlAdapter;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.validate.ModelInfo;

/** An IEditorField that shows String values as a text area */
public class RuleField extends JPanel implements IEditorField, DocumentListener, FocusListener {

	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.editor.Local");

	private static final SimpleAttributeSet s_normalAttr  = new SimpleAttributeSet();
	private static final SimpleAttributeSet s_textAttr  = new SimpleAttributeSet();
	private static final SimpleAttributeSet s_keyWordAttr = new SimpleAttributeSet();
	static {
		StyleConstants.setFontFamily(s_normalAttr, "Monospaced");
		StyleConstants.setFontSize(s_normalAttr, 12);

		StyleConstants.setFontFamily(s_textAttr, "Monospaced");
		StyleConstants.setFontSize(s_textAttr, 12);
		StyleConstants.setForeground(s_textAttr, Color.blue);
		
		StyleConstants.setFontFamily(s_keyWordAttr, "Monospaced");
		StyleConstants.setFontSize(s_keyWordAttr, 12);
		StyleConstants.setForeground(s_keyWordAttr, Color.red.darker());
		StyleConstants.setBold(s_keyWordAttr, true);
	}
	
	
	private GenericEditor m_parentEditor;
	private DefaultStyledDocument m_document;
	private JTextPane     m_textPane;
	private TextSearcher  m_searcher = null;


	private boolean m_isListening = false;
	
	private static final String [] s_KeyWords = {
		"AND", "OR", "IF", "THEN", "ELSE", "SET",
		"=", "<>"
	};
	
	private static boolean isKeyWord(String word) {
		for (String keyWord: s_KeyWords) {
			if (keyWord.equalsIgnoreCase(word)) {
				return true;
			}
		}
		return false;
	}
	
	public RuleField(GenericEditor parentEditor) {
		setLayout(new BorderLayout());
		m_document = new DefaultStyledDocument();
		m_textPane = new CustomTextPane(m_document);

		JScrollPane scroller = new JScrollPane( m_textPane );
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		scroller.setPreferredSize( new Dimension(120, 96) );
		
		m_parentEditor = parentEditor;

		add(scroller, BorderLayout.CENTER);
		
	}
	
	private class CustomTextPane extends JTextPane {
		public CustomTextPane(StyledDocument doc) {
			super(doc);
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
		return m_textPane.getText().trim();
	}

	@Override
	public void setDisplayValue(Object value) {
		m_document.removeDocumentListener(this);
		String text = (value == null ? "" : value.toString());
		m_textPane.setText(text);
		parseText();
		m_textPane.revalidate();
		if (m_isListening) {
			m_document.addDocumentListener(this);
		}
	}
	
	private void parseText() {
		String text = m_textPane.getText();

		int dot = m_textPane.getCaretPosition(); //m_textPane.getCaret().getDot();
		DefaultStyledDocument doc = new DefaultStyledDocument();	//RuleDocument();
		
		Pattern quotePattern = Pattern.compile("'[^']*'");

		// tokenize text to find keywords
		StringTokenizer tok = new StringTokenizer(text, " \t\n\r\f", true);
		while (tok.hasMoreElements()) {
			String token = tok.nextToken();
			SimpleAttributeSet attrS = s_normalAttr;
			if ("\r".equals(token)) {
				// strip these out - they mess up editing
				continue;
			}
			Matcher matcher = quotePattern.matcher(token);
			if (isKeyWord(token)) {
				attrS = s_keyWordAttr;
			}
			
			// look for text in quotes
			try {
				String subToken;
				int startIdx = 0;
				while (matcher.find()) {
					int matchStart = matcher.start();
					int matchEnd = matcher.end();
					
					// show everything up-to the match
					if (matchStart != startIdx) {
						subToken = token.substring(startIdx, matchStart);
						doc.insertString(doc.getLength(), subToken, attrS);
					}
					
					// show matching segment
					subToken = token.substring(matchStart, matchEnd);
					doc.insertString(doc.getLength(), subToken, s_textAttr);
					
					// adjust 
					startIdx = matchEnd;
				}
				
				// show everything after the match
				if (startIdx != token.length()) {
					subToken = token.substring(startIdx);
					doc.insertString(doc.getLength(), subToken, attrS);
				}
				
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		
		m_document.removeDocumentListener(this);
		
		// replace document
		m_textPane.setDocument(doc);
		m_textPane.setCaretPosition(dot);
		
		m_document = doc;
		if (m_isListening) {
			m_document.addDocumentListener(this);
		}
	}
	
	@Override
	public void setReadOnly() {
		m_textPane.setEditable(false);
	}
	
	
	@Override
	public void highlightText(String text, Color highlightColor) {
		// use highlighters to highlight some of text
		if (m_searcher != null) {
			// clear old ones
			m_searcher.clearHighlights();
		}
		m_searcher = new TextSearcher(m_textPane, 
				new TextHighlighter.TextHighlighterPainter(highlightColor));
		m_searcher.search(text);
	}

	@Override
	public void addNotify() {
		super.addNotify();
		m_textPane.addFocusListener(this);
		m_textPane.getDocument().addDocumentListener(this);
		m_isListening = true;
	}

	@Override
	public void removeNotify() {
		m_textPane.removeFocusListener(this);
		m_textPane.getDocument().removeDocumentListener(this);
		m_isListening = false;
		super.removeNotify();
	}
	
	// debugging
	@SuppressWarnings("unused")
	private String getTextAt(int offset, int length) {
		String text = "--error--";
		try {
			text = m_document.getText(offset, length);
			// look for special characters
			text = handleSpecial(text);
		} catch (BadLocationException e) {
		}
		return text;
	}
	
	// convert white-space characters to <>
	private static String handleSpecial(String text) {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<text.length(); i++) {
			char c = text.charAt(i);
			if (c == '\n') {
				buf.append("<\\n>");
			} else if (c == '\r') {
				buf.append("<\\r>");
			} else if (c == '\f') {
				buf.append("<\\f>");
			} else if (c == '\t') {
				buf.append("<\\t>");
			} else {
				buf.append(c);
			}
		}
		return buf.toString();
	}

	private List<ModelInfo> validateRule(SemanticElement semanticElement, boolean actionRule) {	
		List<ModelInfo> errors = new ArrayList<ModelInfo>();
		
		String ruleText = m_textPane.getText();
		if (ruleText == null || ruleText.length() == 0) {
			return errors;
		}
		// use highlighters to highlight error text
		if (m_searcher != null) {
			// clear old ones
			m_searcher.clearHighlights();
		}
		m_searcher = new TextSearcher(m_textPane, 
				new TextHighlighter.UnderlineHighlightPainter(Color.red));

		List<NRLError> nrlErrors;
        IExpressionInterpreter m_adapter = new NrlAdapter();
		if (actionRule)
			nrlErrors = m_adapter.compileAction(semanticElement,ruleText);
		else
			nrlErrors = m_adapter.compileAction(semanticElement,ruleText);

		/** convert a RuleFactoryException into a list of ModelInfo errors. HIghlight errors */
		for (NRLError nrlError : nrlErrors) {
			int lineNo = nrlError.getLine();
			int col = nrlError.getColumn();
			int length = nrlError.getLength();
			String message = nrlError.getMessage();

			// convert line and column to offset
			int offset = getOffsetFromNRL(ruleText, lineNo, col);
			m_searcher.highlightText(offset, length < 1 ? 1 : length);
			
			ModelInfo error = new ModelInfo(m_parentEditor.getEditObject(), "rule", message);
			errors.add(error);
		}
		
		return errors;
	}

	public List<ModelInfo> validateConstraintRule(SemanticElement semanticElement) {
		return validateRule(semanticElement, false);
	}

	public List<ModelInfo> validateActionRule(SemanticElement semanticElement) {
		return validateRule(semanticElement, true);
	}

	/**
	 * @param ruleText
	 * @param lineNo
	 * @param col
	 * @return
	 */
	private int getOffsetFromNRL(String ruleText, int lineNo, int col) {
		int offset = col-1;
		
		if (lineNo > 1) {
			// break text into lines
			int lineCount = 1;	// NRL parser starts lines at 1
			int colCount = 0;
			StringTokenizer tok = new StringTokenizer(ruleText, "\r\n");
			while (tok.hasMoreElements()) {
				String line = tok.nextToken();
				if (lineCount == lineNo) {
					offset = colCount + col - 1;
					break;
				}
				
				lineCount++;
				colCount += line.length() + 1;
			}
		}
		return offset;
	}
	
	/////////////////////////////////////////
	//    Document Listener Methods        //
	/////////////////////////////////////////

	@Override
	public void changedUpdate(DocumentEvent e) {
		// don't care about this one
//		documentChanged();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		documentChanged();

		int offset = e.getOffset();
		int length = e.getLength();
//		System.out.println("Inserted '" + getTextAt(offset, length) + "' at " + offset + " text length is " + m_document.getLength());
//		int dot = m_textPane.getCaret().getDot();
//		System.out.println("   dot at " + dot
//				+ ", mark at " + m_textPane.getCaret().getMark() 
//				+ ", caretPosition is " + m_textPane.getCaretPosition());
		try {
			String text = m_document.getText(offset, length);
			if (text.length() > 1 || Character.isWhitespace(text.charAt(0))) {
				parseText();
			}		
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}

	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		documentChanged();
//		parseText();
	}
	
	private void documentChanged() {
		m_parentEditor.setModified(true);
		if (m_searcher != null) {
			m_searcher.clearHighlights();
		}
	}
	
	///////////////////////////////////////////
	//  Focus Listener Methods
	//////////////////////////////////////////

	@Override
	public void focusGained(FocusEvent e) {
	}

	@Override
	public void focusLost(FocusEvent e) {
		parseText();
	}
}
