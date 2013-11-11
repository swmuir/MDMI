package org.openhealthtools.mdht.mdmi.editor.map.editor;

import java.awt.Color;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.openhealthtools.mdht.mdmi.editor.common.components.CustomTextArea;

/** A JTextPane, using a DefaultStyledDocument, that includes some parsing/decorating */
public class RuleTextPane extends CustomTextArea {

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
	

	private static final String [] s_KeyWords = {
		"AND", "OR", "IF", "THEN", "ELSE", "SET",
		"=", "<>"
	};

	
	public RuleTextPane() {
		super(new DefaultStyledDocument());
	}
	
	/** Check for known key words */
	public static boolean isKeyWord(String word) {
		for (String keyWord: s_KeyWords) {
			if (keyWord.equalsIgnoreCase(word)) {
				return true;
			}
		}
		return false;
	}
	
	
	/** Parse and re-decorate text. The Document will be changed,
	 * so listeners must be removed/reset */
	public DefaultStyledDocument parseText() {
		String text = getText();

		int dot = getCaretPosition(); //m_textPane.getCaret().getDot();
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
		
		// replace document
		setDocument(doc);
		if (dot >= 0 && dot < doc.getLength() ) {
			setCaretPosition(dot);
		}
		
		return doc;
	}

}
