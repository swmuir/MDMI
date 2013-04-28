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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

/** Search and highlight text within a TextComponent */
public class TextSearcher {

	private JTextComponent m_textComponent;

	private Highlighter.HighlightPainter m_painter;
	private Pattern m_pattern;

	/** Highlight in pale yellow */
	public TextSearcher(JTextComponent comp) {
		this.m_textComponent = comp;
		this.m_painter = new TextHighlighter.TextHighlighterPainter(new Color(0xffffaa));
	}

	/** Highlight with the provided highlighter */
	public TextSearcher(JTextComponent comp, Highlighter.HighlightPainter painter) {
		this.m_textComponent = comp;
		this.m_painter = painter;
	}

	/** Search in the text component for this expression. Return the index of where the first
	 * match is found */
	public int search(String searchExpr) {
		int flags = Pattern.CASE_INSENSITIVE;
		m_pattern = Pattern.compile(toRegex(searchExpr), flags);
		return searchAgain();
	}

	/** repeat search using same search expression */
	public int searchAgain() {
		clearHighlights();

		if (m_pattern == null) {
			return -1;
		}

		// Look for the word we are given - insensitive search
		String content = null;
		try {
			Document d = m_textComponent.getDocument();
			content = d.getText(0, d.getLength()).toLowerCase();
		} catch (BadLocationException e) {
			// Cannot happen
			return -1;
		}

		// find and highlight them
		Highlighter highlighter = m_textComponent.getHighlighter();
		
		int firstIdx = -1;
		int startIdx = -1;
		int endIdx = -1;
		Matcher matcher = m_pattern.matcher(content);
		while(matcher.find()) {
			startIdx = matcher.start();
			endIdx = matcher.end();

			try {
				highlighter.addHighlight(startIdx, endIdx, m_painter);
			} catch (BadLocationException e) {
				// Nothing to do
			}

			if (firstIdx != -1) {
				firstIdx = startIdx;
			}
		}

		return firstIdx;
	}

	/** highlight the text at this offset and length */
	public void highlightText(int offset, int length) {
		Highlighter highlighter = m_textComponent.getHighlighter();
		try {
			highlighter.addHighlight(offset, offset + length, m_painter);
		} catch (BadLocationException e) {
			// Nothing to do
		}
	}

	/** Clear the highlights we've added
	 */
	public void clearHighlights() {
		Highlighter highlighter = m_textComponent.getHighlighter();
		
		// Remove any existing highlights from last word
		Highlighter.Highlight[] highlights = highlighter.getHighlights();
		for (int i = 0; i < highlights.length; i++) {
			Highlighter.Highlight h = highlights[i];
			if (h.getPainter() == m_painter) {
				highlighter.removeHighlight(h);
			}
		}
	}



	/** Convert the user-entered text into a regular expression */
	public static String toRegex(String text) {
		StringBuilder buf = new StringBuilder();

		// convert "*" to ".*"
		for (int i=0; i<text.length(); i++) {
			char c = text.charAt(i);
			if (c == '*') {
				// avoid multiple '*'s
				if (i > 0 && text.charAt(i-1) == '*') {
					continue;
				}
				buf.append('.');
			}
			buf.append(c);
		}

		//		// begin and end with ".*",
		//		if (!buf.toString().startsWith(".*")) {
		//			buf.insert(0, ".*");
		//		}
		//		if (!buf.toString().endsWith(".*")) {
		//			buf.append(".*");
		//		}
		return buf.toString();
	}


	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception evt) {}

		JFrame frame = new JFrame("Highlight example");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		final JTextPane textPane = new JTextPane();

		JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout());
		pane.add(new JLabel("Enter word: "), "West");
		final JTextField searchTextField = new JTextField();
		pane.add(searchTextField, "Center");
		frame.getContentPane().add(pane, "South");
		frame.getContentPane().add(new JScrollPane(textPane), "Center");


		String text = "The time has come the walrus said\n" +
		"To talk of many things\n" +
		"Of shoes and ships and sealing wax\n" +
		"And cabbages and kings\n" +
		"And why the sea is boiling hot\n" +
		"And whether pigs have wings.";
		textPane.setText(text);
		final TextSearcher searcher = new TextSearcher(textPane);

		searchTextField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				String word = searchTextField.getText().trim();
				int offset = searcher.search(word);
				if (offset != -1) {
					try {
						textPane.scrollRectToVisible(textPane
								.modelToView(offset));
					} catch (BadLocationException e) {
					}
				}
			}
		});

		textPane.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent evt) {
				searcher.searchAgain();
			}

			@Override
			public void removeUpdate(DocumentEvent evt) {
				searcher.searchAgain();
			}

			@Override
			public void changedUpdate(DocumentEvent evt) {
			}
		});

		frame.setSize(400, 400);
		frame.setVisible(true);
	}
}