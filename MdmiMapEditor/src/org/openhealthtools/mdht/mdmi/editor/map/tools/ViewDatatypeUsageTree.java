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

import java.awt.Component;
import java.awt.Dimension;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.JScrollPane;

import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;

/** Show a datatype, and all fields, in a graphical view */
public class ViewDatatypeUsageTree extends PrintableView {
	
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");

	/** Minimum/maximum dimensions */
	protected Dimension m_min = new Dimension(500, 300);
	protected Dimension m_max = new Dimension(1000, 730);

	
	private DatatypeUsageTree   m_tree;
	
	public ViewDatatypeUsageTree(MdmiDatatype datatype) {
		// View Usage of Datatype 'foo'
		setTitle( MessageFormat.format(s_res.getString("ViewDatatypeUsageTree.title"), 
				ClassUtil.beautifyName(datatype.getClass()), datatype.getTypeName()) );

		// create tree
		m_tree = new DatatypeUsageTree(datatype);
		setCenterComponent(new JScrollPane(m_tree));
		
		// expand all
		m_tree.expandAll(m_tree.getRoot());
		
		initFrame();
	}
	
	public ViewDatatypeUsageTree(Collection<MdmiDatatype> datatypes) {
		// View Datatypes
		setTitle( s_res.getString("ViewDatatypeUsageTree.viewDatatypes") );

		// create tree
		m_tree = new DatatypeUsageTree(datatypes);
		setCenterComponent(new JScrollPane(m_tree));
		
		// expand top only
		m_tree.expandRow(0);
		
		initFrame();	
	}
	
	@Override
	protected Component getPrintComponent() {
		return m_tree;
	}

	private void initFrame() {		
		// set visible row count
		int visRow = m_tree.getVisibleRowCount();
		int rowCount = m_tree.getRowCount();
		// adjust to show at least 50 rows
		if (rowCount > visRow) {
			visRow = Math.min(rowCount, 50);
			m_tree.setVisibleRowCount(visRow);
		}
		pack(m_min, m_max);
		BaseDialog.centerOnScreen(this);
	}

	@Override
	public void dispose() {	
		super.dispose();
	}
	
	/** Get the Datatype tree */
	public DatatypeUsageTree getTree() {
		return m_tree;
	}
	
	
}
