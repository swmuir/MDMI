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
package org.openhealthtools.mdht.mdmi.editor.map.console;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.openhealthtools.mdht.mdmi.editor.common.DefPopupListener;

/**
 * A console to display status text. 
 */
public class ConsolePanel extends JPanel {
	private static final long serialVersionUID = -1;

	private static final String LINKED_OBJECT = "LinkedObject";
	
	/** Resource for localization */
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.Local");
	
	public static final String NL = "\n";

	private SimpleAttributeSet m_defaultSet;
	
	private DefaultStyledDocument m_document;
	private JTextPane   m_display;
	private JPopupMenu  m_popupMenu;
	
	private MouseListener    m_popupListener;
	private ClearAction      m_clearAction  = new ClearAction();
	private DeleteAction     m_deleteAction = new DeleteAction();
	private FindEntityAction m_findEntityAction = new FindEntityAction();
	private OpenEntityAction m_openEntityAction = new OpenEntityAction();

	public ConsolePanel() {
		buildUI();
	}
	
	public SimpleAttributeSet getAttributeSet() {
		if (m_defaultSet == null) {
			m_defaultSet = new SimpleAttributeSet();
		}
		return m_defaultSet;
	}

	@Override
	public void addNotify() {
		super.addNotify();
		m_display.addMouseListener( m_popupListener );
	}

	@Override
	public void removeNotify() {
		m_display.removeMouseListener( m_popupListener );
		super.removeNotify();
	}

	/** Add text to the console without a New-Line character */
	public void write( String text ) {
		addString( text, getAttributeSet());
	}

	/** Add text to the console, followed by a new-line character */
	public void writeln( String text ) {
		addString( text + NL, getAttributeSet());
	}
	
	/** Add the text to the end of the document. Errors will be ignored */
	protected void addString(String text, AttributeSet attrs) {
		try {
			m_document.insertString( getLength(), text, attrs );
		} catch( Exception ignored ) {}
	}
	
	/** return the document length */
	public int getLength() {
		return m_document.getLength();
	}

	public boolean hasText() {
		return getLength() > 0;
	}
	
	public DefaultStyledDocument getDocument() {
		return m_document;
	}

	/** Clear the selected text */
	public void clearSelection() {
		int selStart = getSelectionStart();
		int selEnd = getSelectionEnd();
		try {
			m_document.remove( selStart, selEnd - selStart );
		} catch( Exception ignored ) {}
	}

	/** Clear the display */
	public void clear() {
		try {
			m_document.remove( 0, getLength() );
		} catch( Exception ignored ) {}
	}
	
	public int getSelectionStart() {
		return m_display.getSelectionStart();
	}
	
	public int getSelectionEnd() {
		return m_display.getSelectionEnd();
	}

	private void buildUI() {
		// add popup menu for clear/delete
		m_popupMenu = buildPopupMenu();
		m_popupListener = createMouseListener(m_popupMenu);

		// configure display
		m_document = new DefaultStyledDocument();
		m_display = new JTextPane(m_document);
		m_display.setPreferredSize( new Dimension(420, 120) );
		m_display.setEditable( false );
		JScrollPane scroller = new JScrollPane( m_display );

		setLayout( new BorderLayout() );
		
		add( scroller, BorderLayout.CENTER );
		
//		// TESTING
//		m_display.addFocusListener(new FocusListener() {
//
//			@Override
//			public void focusGained(FocusEvent e) {
//				System.out.println("Focus Gained on " + m_display);
//			}
//
//			@Override
//			public void focusLost(FocusEvent e) {
//				System.out.println("  Focus Lost on " + m_display);
//				
//			}
//			
//		});
	}
	
	/** Create a mouse listener. By default, this is the MousePopupListener */
	protected MouseListener createMouseListener(JPopupMenu popupMenu) {
		return new MousePopupListener(popupMenu);
	}
	
	protected JPopupMenu buildPopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();
		
		popupMenu.add(new JMenuItem(m_deleteAction));
		popupMenu.add(new JMenuItem(m_clearAction));

		popupMenu.addSeparator();
		
		popupMenu.add(new JMenuItem(m_findEntityAction));
		popupMenu.add(new JMenuItem(m_openEntityAction));
		
		return popupMenu;
	}
	
	/** Select the entire line of text containing this offset position */
	public void selectLine(int offset) {
		int startOfLine = offset;
		int endOfLine = offset;

		try {
			// find start of line by going backwards to find the new-line (or start of document)
			while (startOfLine > 0) {
				String s = m_document.getText(startOfLine, 1);
				if (NL.equals(s)) {
					// don't include leading NL
					startOfLine++;
					break;
				}
				
				startOfLine--;
			}
			// find end of line by going forward to find the new-line (or end of document)
			while (endOfLine < getLength()) {
				String s = m_document.getText(endOfLine, 1);
				if (NL.equals(s)) {
					// include trailing NL
					endOfLine++;
					break;
				}
				
				endOfLine++;
			}
			
		} catch( Exception ignored ) {}

		m_display.select(startOfLine, endOfLine);
	}
	
	/** Select the entire word of text containing this offset position */
	public void selectWord(int offset) {
		int startOfWord = offset;
		int endOfWord = offset;

		try {
			// find start of word by going backwards to find a white space (or start of document)
			while (startOfWord > 0) {
				String s = m_document.getText(startOfWord, 1);
				if (Character.isWhitespace(s.charAt(0))) {
					// don't include leading space
					startOfWord++;
					break;
				}
				
				startOfWord--;
			}
			// find end of line by going forward to find a white space (or end of document)
			while (endOfWord < getLength()) {
				String s = m_document.getText(endOfWord, 1);
				if (Character.isWhitespace(s.charAt(0))) {
					// include trailing NL
					endOfWord++;
					break;
				}
				
				endOfWord++;
			}
			
		} catch( Exception ignored ) {}

		m_display.select(startOfWord, endOfWord);
	}


	/** Add a "link" to an Object */
	public void writeLink( LinkedObject object, String objectName ) {
		SimpleAttributeSet attrS = new SimpleAttributeSet(getAttributeSet());
		StyleConstants.setUnderline(attrS, true);
		StyleConstants.setForeground(attrS, Color.blue);
		attrS.addAttribute(LINKED_OBJECT, object);
		addString( objectName, attrS );
	}

	/** find the first linked object in the selection range */
	private LinkedObject getLinkedObject(int selStart, int selEnd) {
		LinkedObject link = null;
		
		while (link == null && selStart <= selEnd) {
			Element element = getDocument().getCharacterElement(selStart);
			AttributeSet attrS = element.getAttributes();
			link = (LinkedObject)attrS.getAttribute(LINKED_OBJECT);
			if (link == null) {
				selStart = element.getEndOffset();
			}
		}
		
		return link;
	}

	/** find the Element containing this link */
	public Element getCharacterElement(LinkedObject link) {
		int offset = 0;
		while (offset < getDocument().getLength()) {
			Element element = getDocument().getCharacterElement(offset);
			AttributeSet attrS = element.getAttributes();
			Object linkedObject = attrS.getAttribute(LINKED_OBJECT);
			if (link == linkedObject) {
				return element;
			}
			offset = element.getEndOffset();
		}
		
		return null;
	}
	
	/** Find and select the node in the tree that is specified by the Link */
	private void findLink(boolean openToo) {
		int selStart = getSelectionStart();
		int selEnd = getSelectionEnd();
		LinkedObject link = getLinkedObject(selStart, selEnd);
		if (link != null) {
			if (openToo) {
				// find and open it
				link.openTarget();
			} else {
				// just find it in the tree
				link.findTarget();
			}
			
			// change color
			Element element = getCharacterElement(link);
			if (element != null) {
				SimpleAttributeSet attrS = new SimpleAttributeSet(element.getAttributes());
				StyleConstants.setForeground(attrS, Color.magenta);

				int start = element.getStartOffset();
				int length = (element.getEndOffset()-element.getStartOffset());
				getDocument().setCharacterAttributes(start, length, attrS, true);
			}
			
			// put focus back on console
			m_display.requestFocusInWindow();
			selectLine(selStart);
		}
	}


	/** Enable/Disable popup menus according to selection */
	protected void enablePopupActions(MouseEvent e) {
		int selStart = getSelectionStart();
		int selEnd = getSelectionEnd();
		
		// don't allow delete selection if nothing selected
		m_deleteAction.setEnabled(false);
		if (selStart < selEnd) {
			m_deleteAction.setEnabled(true);
		}
		
		// don't allow search if nothing selected
		m_findEntityAction.setEnabled(false);
		m_openEntityAction.setEnabled(false);
		if (getLinkedObject(selStart, selEnd) != null) {
			m_findEntityAction.setEnabled(true);
			m_openEntityAction.setEnabled(true);
		}
	}

	protected class MousePopupListener extends DefPopupListener {
		public MousePopupListener(JPopupMenu menu) {
			super(menu);
		}
		
		
		@Override
		public void mouseClicked(MouseEvent event) {
			Point p = event.getPoint();
			int offset = m_display.viewToModel(p);
			int clickCount = event.getClickCount();
			if (clickCount == 2) {
				// select word
				selectWord(offset);
			} else if (clickCount == 3) {
				// select line
				selectLine(offset);
			}
			super.mouseClicked(event);

			// if clicking in a link, find it
			int modifier = event.getModifiers();
			// don't link if right-mouse button
			if ((modifier & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {
				if (event.getClickCount() == 1) {
					findLink(true);
				}
			}
		}

		@Override
		protected void showPopup(MouseEvent e) {
			Point p = e.getPoint();
//			System.out.println("show popup on " + offset + ", caret is at " + m_display.getCaretPosition());
			String text = m_display.getSelectedText();
			if (text == null || text.length() == 0) {
				int offset = m_display.viewToModel(p);
				m_display.setCaretPosition(offset);
			}
			enablePopupActions( e );
			super.showPopup(e);
		}
		
	}
	
	public class ClearAction extends AbstractAction {
		ClearAction() {
			super(s_res.getString("ConsolePanel.clearAction"));
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			clear();
		}
	}
	
	public class DeleteAction extends AbstractAction {
		DeleteAction() {
			super(s_res.getString("ConsolePanel.deleteSelectionAction"));
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			clearSelection();
		}
	}
	
	public class FindEntityAction extends AbstractAction {
		FindEntityAction() {
			super(s_res.getString("ConsolePanel.findAction"));
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			findLink(false);
		}
	}
	
	public class OpenEntityAction extends AbstractAction {
		OpenEntityAction() {
			super(s_res.getString("ConsolePanel.openAction"));
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			findLink(true);
		}
	}
}
