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
package org.openhealthtools.mdht.mdmi.editor.common.tables;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/** A simple DefaultTableModel that is not editable */
public class NonEditableTableModel extends DefaultTableModel {

	public NonEditableTableModel() {
		super();
	}

	public NonEditableTableModel(int rowCount, int columnCount) {
		super(rowCount, columnCount);
	}

	public NonEditableTableModel(Object[] columnNames, int rowCount) {
		super(columnNames, rowCount);
	}

	public NonEditableTableModel(Object[][] data, Object[] columnNames) {
		super(data, columnNames);
	}

	@SuppressWarnings("unchecked")
	public NonEditableTableModel(Vector columnNames, int rowCount) {
		super(columnNames, rowCount);
	}

	@SuppressWarnings("unchecked")
	public NonEditableTableModel(Vector data, Vector columnNames) {
		super(data, columnNames);
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

}
