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
package org.openhealthtools.mdht.mdmi.editor.common.treetable;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

/**
 * This is a wrapper class takes a TreeTableModel and implements 
 * the table model interface. The implementation is trivial, with 
 * all of the event dispatching support provided by the superclass: 
 * the AbstractTableModel. 
 *
 * @version %I% %G%
 *
 * @author Philip Milne
 * @author Scott Violet
 */


public class TreeTableModelAdapter extends AbstractTableModel
{
	JTree tree;
	TreeTableModel treeTableModel;

	public TreeTableModelAdapter(TreeTableModel treeTableModel, JTree tree) {
		this.tree = tree;
		this.treeTableModel = treeTableModel;

		tree.addTreeExpansionListener(new TreeExpansionListener() {
			// Don't use fireTableRowsInserted() here; 
			// the selection model would get  updated twice. 
			public void treeExpanded(TreeExpansionEvent event) {  
				fireTableDataChanged(); 
			}
			public void treeCollapsed(TreeExpansionEvent event) {  
				fireTableDataChanged(); 
			}
		});


		// Install a TreeModelListener that can update the table when
		// tree changes. We use delayedFireTableDataChanged as we can
		// not be guaranteed the tree will have finished processing
		// the event before us.
		treeTableModel.addTreeModelListener(new TreeModelListener() {
			public void treeNodesChanged(TreeModelEvent e) {
				delayedFireTableDataChanged();
			}

			public void treeNodesInserted(TreeModelEvent e) {
				final int [] indices = e.getChildIndices();
				if (indices.length > 0) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							fireTableRowsInserted(indices[0], indices[indices.length-1]);                         
						}
					});
				}
			}

			public void treeNodesRemoved(TreeModelEvent e) {
				delayedFireTableDataChanged();
			}

			public void treeStructureChanged(TreeModelEvent e) {
				fireTableStructureChanged();
			}
		});
	}


	// Wrappers, implementing TableModel interface. 

	public int getColumnCount() {
		return treeTableModel.getColumnCount();
	}

	@Override
	public String getColumnName(int column) {
		return treeTableModel.getColumnName(column);
	}

	@Override
	public Class<?> getColumnClass(int column) {
		return treeTableModel.getColumnClass(column);
	}

	public int getRowCount() {
		return tree.getRowCount();
	}

	protected Object nodeForRow(int row) {
		TreePath treePath = tree.getPathForRow(row);
		if (treePath != null) {
			return treePath.getLastPathComponent();    
		}
		return null;
	}

	public Object getValueAt(int row, int column) {
		return treeTableModel.getValueAt(nodeForRow(row), column);
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return treeTableModel.isCellEditable(nodeForRow(row), column); 
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		treeTableModel.setValueAt(value, nodeForRow(row), column);
	}

	/**
	 * Invokes fireTableDataChanged after all the pending events have been
	 * processed. SwingUtilities.invokeLater is used to handle this.
	 */
	protected void delayedFireTableDataChanged() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				fireTableDataChanged();
			}
		});
	}

}


