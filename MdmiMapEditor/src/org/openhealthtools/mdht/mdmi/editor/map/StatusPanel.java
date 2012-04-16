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
package org.openhealthtools.mdht.mdmi.editor.map;

import java.awt.BorderLayout;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openhealthtools.mdht.mdmi.editor.map.console.ConsolePanel;
import org.openhealthtools.mdht.mdmi.editor.map.console.ErrorPanel;
import org.openhealthtools.mdht.mdmi.editor.map.console.LinkedObject;
import org.openhealthtools.mdht.mdmi.editor.map.console.ValidationErrorLink;
import org.openhealthtools.mdht.mdmi.model.validate.ModelInfo;

public class StatusPanel extends JPanel {

	private static final long serialVersionUID = -1;
	
	/** Resource for localization */
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.Local");

	private ConsolePanel m_console = new ConsolePanel();
	private ErrorPanel   m_errors  = new ErrorPanel();

	private int	m_consoleIndex;
	private int	m_errorsIndex;
	
	private JTabbedPane m_tabs = new JTabbedPane();
	
	private ErrorTextListener m_errorListener = new ErrorTextListener();
	
	public StatusPanel() {
		buildUI();
	}
	
	@Override
	public void addNotify() {
		super.addNotify();
		m_errors.getDocument().addDocumentListener(m_errorListener);
	}

	@Override
	public void removeNotify() {
		m_errors.getDocument().removeDocumentListener(m_errorListener);
		super.removeNotify();
	}

	private void buildUI() {
		setLayout(new BorderLayout());
		
		// Console
		String title = s_res.getString("StatusPanel.console");
		Icon icon = getIcon(s_res.getString("StatusPanel.consoleIcon"));
		m_consoleIndex = m_tabs.getComponentCount();
		m_tabs.addTab(title, icon, m_console);
		
		// Errors
		title = s_res.getString("StatusPanel.errors");
		icon = getIcon(s_res.getString("StatusPanel.errorsDisabledIcon"));
		m_errorsIndex = m_tabs.getComponentCount();
		m_tabs.addTab(title, icon, m_errors);
		
		
		add(m_tabs, BorderLayout.CENTER);
	}

	private Icon getIcon(String path) {
		Icon icon  = null;
		URL url  = getClass().getResource(path);
		if (url != null) {
			icon =  new ImageIcon(url);
		}
		return icon;
	}
	
	/** Clear the console */
	public void clearConsole() {
		m_console.clear();
	}
	
	/** Clear the error display */
	public void clearErrors() {
		m_errors.clear();
	}
	
	/** Write text to the console */
	public void writeConsole(String text) {
		m_console.writeln(text);
		m_tabs.setSelectedIndex(m_consoleIndex);
	}
	
	/** Write text containing a link */
	public void writeConsoleLink(String preText, LinkedObject object, String postText) {
		writeLink(m_console, preText, object, postText);
	}
	
	/** Write exception to the console */
	public void writeException(String msg, Exception ex) {
		m_errors.writeException(msg, ex);
		m_tabs.setSelectedIndex(m_errorsIndex);
	}
	
	/** Write exception to the console */
	public void writeException(Exception ex) {
		m_errors.writeException(ex);
		m_tabs.setSelectedIndex(m_errorsIndex);
	}
	
	/** Write error text to the console */
	public void writeErrorText(String text) {
		m_errors.writeln( text );
		m_tabs.setSelectedIndex(m_errorsIndex);
	}

	
	/** Write an errorMsg, showing the object as a hyper-link */
	public void writeValidationErrorMsg(String textPre, ModelInfo errorMsg) {
		LinkedObject object = new ValidationErrorLink(errorMsg);
		writeErrorLink(textPre, object, 
				": " + errorMsg.getMessage());
	}
	
	/** Write error text containing a link */
	public void writeErrorLink(String preText, LinkedObject object, String postText) {
		writeLink(m_errors, preText, object, postText);
	}
	

	
	/** Write text with a link to the supplied panel*/
	private void writeLink(ConsolePanel panel,
			String preText, LinkedObject object, String postText) {
		if (panel != null) {
			panel.write(preText);
			// add leading blank
			if (object.getName() != null) {
				panel.write(" ");
			}
		}
		if (object != null) {
			panel.writeLink(object, object.getName());
		}
		if (postText != null) {
			// add leading blank
			if (object.getName() != null) {
				panel.write(" ");
			}
			panel.write(postText);
		}
		panel.writeln("");
		
		m_tabs.setSelectedComponent(panel);
	}
	
	
	private class ErrorTextListener implements DocumentListener {
		private boolean m_hasText = false;
		@Override
		public void changedUpdate(DocumentEvent arg0) {
			changeIcon();
		}

		@Override
		public void insertUpdate(DocumentEvent arg0) {
			changeIcon();
		}

		@Override
		public void removeUpdate(DocumentEvent arg0) {
			changeIcon();
		}
		
		/** change tab icon if there are/are not errors */
		private void changeIcon() {
			Icon icon;
			boolean hasText = m_errors.hasText();
			if (hasText != m_hasText) {
				m_hasText = hasText;
				if (hasText) {
					icon = getIcon(s_res.getString("StatusPanel.errorsIcon"));
				} else {
					icon = getIcon(s_res.getString("StatusPanel.errorsDisabledIcon"));
				}
				m_tabs.setIconAt(m_errorsIndex, icon );
			}
		}
		
	}

}
