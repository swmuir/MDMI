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
package org.openhealthtools.mdht.mdmi.editor.map.tools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.common.components.CursorManager;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;

/** Show datatype and syntax node for this semantic element, and all sub-types */
public class ViewDatatypeSyntax extends PrintableView {

	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");
	
	private DatatypeToNodeTable m_treeTable;
	private JTextArea 	m_errorRegion = new JTextArea(6, 50);
	private JScrollPane  m_errorScroller;
	
	private JLabel		m_displayLabel;
	private JButton	m_generateBtn;
	private ActionListener m_generateBtnListener;
	
	/** Minimum/maximum dimensions */
	protected Dimension m_min = new Dimension(800, 300);
	protected Dimension m_max = new Dimension(1000, 750);

	public ViewDatatypeSyntax(SemanticElement semanticElement) {
		// title - Syntax elements for Semantic Element 'foo'
		setTitle( MessageFormat.format(s_res.getString("ViewDatatypeSyntax.title"), 
				ClassUtil.beautifyName(semanticElement.getClass()), semanticElement.getName()) );
		
		m_treeTable = new DatatypeToNodeTable(semanticElement);
		
		// label
		m_displayLabel.setText(ClassUtil.beautifyName(semanticElement.getClass()) + " " + semanticElement.getName());

		// tree table
		setCenterComponent(new JScrollPane(m_treeTable));
		
		// errors
		configureErrorDisplay();
		showTableErrors();
		
		initFrame();
	}

	public ViewDatatypeSyntax(Collection<SemanticElement> semanticElements) {
		// title - Semantic Element Syntax Elements
		setTitle( s_res.getString("ViewDatatypeSyntax.multiTitle") );
		
		m_treeTable = new DatatypeToNodeTable(semanticElements);

		// tree table
		setCenterComponent(new JScrollPane(m_treeTable));
		
		// errors
		configureErrorDisplay();
		showTableErrors();
		
		initFrame();
	}

	/**
	 * initialize the error region
	 */
	private void configureErrorDisplay() {
		m_errorRegion.setLineWrap(true);
		m_errorRegion.setWrapStyleWord(true);
		m_errorScroller = new JScrollPane(m_errorRegion);
		getContentPane().add(m_errorScroller, BorderLayout.SOUTH);
	}

	private void initFrame() {		
		// set visible row count
		int visRow = m_treeTable.getVisibleRowCount();
		int rowCount = m_treeTable.getRowCount();
		// adjust to show up to 35 rows
		if (rowCount > visRow) {
			visRow = Math.min(rowCount, 35);
			m_treeTable.setVisibleRowCount(visRow);
		}
		pack(m_min, m_max);
		BaseDialog.centerOnScreen(this);
	}

	
	@Override
	protected JPanel createButtonPanel() {
		JPanel panel = super.createButtonPanel();
		
		// add button to generate syntax elements
		m_generateBtn = new JButton(s_res.getString("ViewDatatypeSyntax.generateButton"));
		m_generateBtn.setToolTipText(s_res.getString("ViewDatatypeSyntax.generateToolTip"));
		
		m_generateBtnListener = new GenerateSyntaxAction();
		m_generateBtn.addActionListener(m_generateBtnListener);
		
		panel.add(m_generateBtn, 0);
		

		// Add label
		JPanel buttonPanel = new JPanel(new BorderLayout());
		m_displayLabel = new JLabel();
		m_displayLabel.setFont(m_displayLabel.getFont().deriveFont(12.0f));
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		labelPanel.add(m_displayLabel);
		
		buttonPanel.add(labelPanel, BorderLayout.CENTER);
		buttonPanel.add(panel, BorderLayout.EAST);
		
		return buttonPanel;
	}
	
	

	@Override
	public void dispose() {
		m_generateBtn.removeActionListener(m_generateBtnListener);
		
		super.dispose();
	}

	private void showTableErrors() {
		StringBuilder buf = new StringBuilder();
		int errorCount = 0;
		for (String error : m_treeTable.getValidationErrors()) {
			errorCount ++;
			if (buf.length() > 0) {
				buf.append("\n");
			}
			// add number
			buf.append(" ").append(errorCount).append(". ");
			buf.append(error);
		}
		m_errorRegion.setText(buf.toString());
		
		// show/hide
		m_errorScroller.setVisible(errorCount > 0);
	}
	
	@Override
	protected Component getPrintComponent() {
		return m_treeTable;
	}

	///////////////////////////////////////
	//  Action Listeners
	//////////////////////////////////////
	
	private class GenerateSyntaxAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			CursorManager cm = CursorManager.getInstance(ViewDatatypeSyntax.this);
			try {
				cm.setWaitCursor();
				m_treeTable.validateSyntaxNodes();
				showTableErrors();
				
				// bring to front
				ViewDatatypeSyntax.this.toFront();
			} finally {
				cm.restoreCursor();
			}
		}
	}
}
