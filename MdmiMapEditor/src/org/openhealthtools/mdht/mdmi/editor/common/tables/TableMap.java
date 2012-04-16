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

import javax.swing.table.*;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;

public class TableMap extends DefaultTableModel implements TableModelListener
{
    protected TableModel model;

    public TableModel  getModel() {
        return model;
    }

    public void  setModel(TableModel model) {
        this.model = model;
        model.addTableModelListener(this);
    }

    // By default, Implement TableModel by forwarding all messages
    // to the model.

    @Override
   public Object getValueAt(int aRow, int aColumn) {
        return model.getValueAt(aRow, aColumn);
    }

    @Override
   public void setValueAt(Object aValue, int aRow, int aColumn) {
        model.setValueAt(aValue, aRow, aColumn);
    }

    @Override
   public int getRowCount() {
        return (model == null) ? 0 : model.getRowCount();
    }

    @Override
   public int getColumnCount() {
        return (model == null) ? 0 : model.getColumnCount();
    }

    @Override
   public String getColumnName(int aColumn) {
        return model.getColumnName(aColumn);
    }

    @Override
   public Class <?> getColumnClass(int aColumn) {
        return model.getColumnClass(aColumn);
    }

    @Override
   public boolean isCellEditable(int row, int column) {
         return model.isCellEditable(row, column);
    }
//
// Implementation of the TableModelListener interface,
//

    // By default forward all events to all the listeners.
    public void tableChanged(TableModelEvent e) {
        fireTableChanged(e);
    }
}
