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

import java.awt.Cursor;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;


/** A special TableModel that provides support for sorting on a column.
 * A typical use would be:
 * <code>
 * 
 * TableSorter sorter = new TableSorter( tableModel );
 * JTable table = new JTable (sorter);
 * sorter.addMouseListenerToHeaderInTable(table);
 * 
 * </code>
 * Note that the sorting is performed via the view. The underlying table model is
 * never modified. To convert between the table row and the model index, use the
 * <code>getModelIndexFromRow(row)</code> and <code>getTableRowFromModel(index)</code>
 * methods. 
 *
 */
public class TableSorter extends TableMap {
   private int[]           m_indexes;
   private int[]           m_revIndexes;
   private Vector <Integer>         m_sortingColumns = new Vector<Integer>();
   private Vector <Boolean>         m_sortingColumnsAscending = new Vector<Boolean>();
   private MouseListener    m_listMouseListener;
   private boolean          m_ascending = true;
   private Vector <Integer>         m_excludeColumns = null;
   
   public TableSorter()
   {
      m_indexes = new int[0]; // For consistency.
      m_revIndexes = new int[0];
   }
   
   public TableSorter(TableModel model)
   {
      setModel(model);
   }
   
   public void setExcludeColumns( Vector<Integer> cols ) {
      m_excludeColumns = cols;
   }
   
   /** Is this column in the exclusion list? */
   public boolean isExcludedColumn( int column ) {
   	return (m_excludeColumns != null && m_excludeColumns.contains(column));
   }
   
   @Override
   public void setModel(TableModel model)
   {
      super.setModel(model);
      reallocateIndexes();
   }
   
   /** Compare the data in this column */
   public int compareRowsByColumn(int row1, int row2, int column)
   {
      TableModel data = model;
      
      Object o1 = data.getValueAt(row1, column);
      Object o2 = data.getValueAt(row2, column);
      
      int result = compare(o1, o2);
      
      return result;
   }

   /** Compare two objects for sorting */
   @SuppressWarnings("unchecked")
   protected int compare(Object o1, Object o2) {
      int result;
      if (o1 instanceof Enum) {   
         Enum e1 = (Enum)o1;
         o1 = e1.toString();
      }
      if (o2 instanceof Enum) {   
         Enum e2 = (Enum)o2;
         o2 = e2.toString();
      }

      // Treat null as an empty string
      if (o1 == null) {   
         o1 = "";
      }
      if (o2 == null) {   
         o2 = "";
      }
      
      /* We copy all returned values from the getValue call in case
       an optimised model is reusing one object to return many values.
       The Number subclasses in the JDK are immutable and so will not be used in
       this way but other subclasses of Number might want to do this to save
       space and avoid unnecessary heap allocation.
       */
      if (o1 instanceof String) {   
         String s1 = (String)o1;
         result = s1.compareToIgnoreCase(o2.toString());
         
         
      } else if (o1 instanceof Comparable) {   
         try {
            Comparable comp = (Comparable)o1;
            result = comp.compareTo(o2);
         } catch (ClassCastException e) {
            String s1 = o1.toString();
            String s2 = o2.toString();
            result = s1.compareTo(s2);
         }
         
      } else {
         String s1 = o1.toString();
         String s2 = o2.toString();
         result = s1.compareTo(s2);
      }
      return result;
   }
   
   public int compareRows(int row1, int row2)
   {
      for( int level = 0; level < m_sortingColumns.size(); level++ )
      {
         Integer column = m_sortingColumns.elementAt(level);
         int result = compareRowsByColumn(row1, row2, column.intValue());
         if ( result != 0 )
         {
            return (m_sortingColumnsAscending.elementAt(level).booleanValue() ? result : -result);
         }
      }
      
      return 0;
   }
   
   public void  reallocateIndexes()
   {
      int rowCount = model.getRowCount();
      
      // Set up a new array of indexes with the right number of elements
      // for the new data model.
      m_indexes = new int[rowCount];
      m_revIndexes = new int[rowCount];
      
      // Initialise with the identity mapping.
      for( int row = 0; row < rowCount; row++ ) {
         m_indexes[row] = row;
         m_revIndexes[row] = row;
      }
   }
   
   @Override
   public void tableChanged(TableModelEvent e)
   {
      reallocateIndexes();
      // Table data has changed.  Force a resort.
      sort(this);
      super.tableChanged(e);
   }
   
   public void checkModel()
   {
      if( m_indexes.length != model.getRowCount() )
      {
         System.err.println("Sorter not informed of a change in model.");
      }
   }
   
   public void sort(Object sender)
   {
      checkModel();
      synchronized (m_indexes) {
         if (m_sortingColumns.size() > 0) {
            List <Integer> indexList = new ArrayList<Integer>();
            for (int idx : m_indexes) {
               indexList.add(new Integer(idx));
            }
            Collections.sort(indexList, new Comparator<Integer>() {
               public int compare(Integer row0, Integer row1) {
                  int result = compareRows(row0.intValue(), row1.intValue());
                  return result;
               }
            });
            for (int i=0; i<indexList.size(); i++) {
               int idx = indexList.get(i).intValue();
               m_indexes[i] = idx;
               m_revIndexes[idx] = i;
            }
         }

      }
   }
   
   
   // The mapping only affects the contents of the data rows.
   // Pass all requests to these rows through the mapping array: "indexes".
   @Override
   public Object getValueAt(int aRow, int aColumn)
   {
      checkModel();
      return model.getValueAt(m_indexes[aRow], aColumn);
   }
   
   @Override
   public void setValueAt(Object newValue, int aRow, int aColumn)
   {
      checkModel();
      int rowIndex = m_indexes[aRow];
      Object oldValue = model.getValueAt(rowIndex, aColumn);
      model.setValueAt(newValue, rowIndex, aColumn);
      if ( (oldValue == null && newValue != null) ||
      		(oldValue != null && !oldValue.equals(newValue)) ) {
      	// value changed - re-sort
      	reallocateIndexes();
      	sort(this);
      }
   }
   
   /** Sort ascending by a single column */
   public void sortByColumn(int column)
   {
      sortByColumn(column, true);
   }
   
   /** Sort by a single column */
   public void sortByColumn(int column, boolean isAscending)
   {
      this.m_ascending = isAscending;
      m_sortingColumns.removeAllElements();
      m_sortingColumns.addElement(new Integer(column));
      m_sortingColumnsAscending.removeAllElements();
      m_sortingColumnsAscending.addElement(new Boolean(m_ascending));
      
      sort(this);
      super.tableChanged(new TableModelEvent(this));
   }
   
   /** Sort by multiple columns */
   public void complexSort(int[] columns, boolean[] ascendings)
   {
      //this.ascending = ascending;
      m_sortingColumns.removeAllElements();
      for (int i = 0; i<columns.length;i++)
      {
         m_sortingColumns.addElement(new Integer(columns[i]));
      }
      
      m_sortingColumnsAscending.removeAllElements();
      for (int i = 0; i<ascendings.length;i++)
      {
         m_sortingColumnsAscending.addElement(new Boolean(ascendings[i]));
      }
      
      sort(this);
      super.tableChanged(new TableModelEvent(this));
   }
   
   // Remove the mouse listener
   public void removeMouseListener(JTable table)
   {
      if (m_listMouseListener != null) {
         JTableHeader th = table.getTableHeader();
         th.removeMouseListener(m_listMouseListener);
      }
   }
   
   /** Set the table header renderer to be a new instance of a 
    * SortedTableHeaderRenderer.
    */
   public void setSortHeader( JTable table )
   {
      SortedTableHeaderRenderer rend = new SortedTableHeaderRenderer( this );
      table.getTableHeader().setDefaultRenderer(rend);
   }

   
   /** Add a listener to the table header so that the table will be sorted
    * when a column header is clicked. The table header renderer will be set
    * to the default (SortedTableHeaderRenderer)
    * @param table
    */
   public void addMouseListenerToHeaderInTable(JTable table)
   {
		addMouseListenerToHeaderInTable(table, new SortedTableHeaderRenderer( this ));
   }
   
   /** Add a renderer and default listener to the table header so that the table will be sorted
    * when a column header is clicked.
    * @param table  The table.
    * @param headerRenderer The renderer for the table header.
    */
   public void addMouseListenerToHeaderInTable(JTable table, TableCellRenderer headerRenderer)
   {
		addMouseListenerToHeaderInTable(table, new HeaderMouseListener(table, this), headerRenderer);
   }
   
   /** Add a renderer and listener to the table header so that the table will be sorted
    * when a column header is clicked. The renderer will show arrows to indicate sorting.
    * @param table  The table.
    * @param mouseListener	The mouse listener
    * @param headerRenderer The renderer for the table header.
    */
   public void addMouseListenerToHeaderInTable(JTable table, 
   		MouseListener mouseListener,
   		TableCellRenderer headerRenderer) {
   	JTableHeader th = table.getTableHeader();
      th.setDefaultRenderer(headerRenderer);
      table.setColumnSelectionAllowed(false);
      
      m_listMouseListener = mouseListener;
      th.addMouseListener(m_listMouseListener);
   }
   
   public boolean isSortColumn( int column )
   {
      return( m_sortingColumns.contains( new Integer( column ) ) );
   }
   
   public boolean isAsscending( int column )
   {
      int index = m_sortingColumns.indexOf( new Integer( column ) );
      return( ((Boolean)m_sortingColumnsAscending.elementAt(index)).booleanValue() );
   }
   
   public int [] getSortColumns() {
      int [] sortColumns = new int [m_sortingColumns.size()];
      for (int i=0; i<m_sortingColumns.size(); i++) {
         sortColumns[i] = m_sortingColumns.get(i).intValue();
      }
      
      return sortColumns;
   }
   
   public int[] getIndexes()
   {
      return( m_indexes );
   }
   
   /** Map the table row to the corresponding model index */
   public int getModelIndexFromRow(int row) {
      return m_indexes[row];
   }
   
   /** Map the model index to the current table row */
   public int getTableRowFromModel(int index) {
      return m_revIndexes[index];
   }
   
   
   /** Listener for selections in the table header.
    * By default, the mouseReleased event will cause the selected
    * column(s) to be sorted.
    * If the CTRL key is held down, multiple columns can be selected.
    */
   public static class HeaderMouseListener extends MouseAdapter {
      private JTable theTable;
      private TableSorter theSorter;
      private int startColumn = -1;
      
      public HeaderMouseListener(JTable table, TableSorter sorter) {
         theTable = table;
         theSorter = sorter;
      }
      
      public JTable getTable() {
      	return theTable;
      }
      
      public TableSorter getTableSorter() {
      	return theSorter;
      }
      
      @Override
      public void mousePressed(MouseEvent e)
      {
         TableColumnModel columnModel = theTable.getColumnModel();
         startColumn = columnModel.getColumnIndexAtX(e.getX());
         //startColumn = tableView.convertColumnIndexToModel(viewColumn);
      } 
      
      @Override
      public void mouseReleased( MouseEvent e )
      {
         if( theTable.getTableHeader().getCursor().getType() == Cursor.DEFAULT_CURSOR  )
         {
            TableColumnModel columnModel = theTable.getColumnModel();
            int viewColumn = columnModel.getColumnIndexAtX(e.getX());
            int column = theTable.convertColumnIndexToModel(viewColumn);
            if( viewColumn == startColumn 
                  && ! theSorter.isExcludedColumn(column) )
            {
               if( e.getClickCount()%2 == 1 && column != -1 )
               {
                  boolean ascending = true;
                  if( theSorter.isSortColumn(column) ) {
                     ascending = !theSorter.isAsscending(column);
                  } else if (e.isShiftDown()) {
                     ascending = false;
                  }
                  
                  // if Ctrl key is pressed, add to existing sorts
                  int [] existingSortColumns = theSorter.getSortColumns();
                  if ((e.getModifiers() & InputEvent.CTRL_MASK) != 0 && existingSortColumns.length > 0) {
                     Vector <Integer> sortingColumns = new Vector<Integer>();
                     Vector <Boolean> sortingAscendings = new Vector<Boolean>();
                     
                     // initialize sortingColumns vector with existing sorts
                     for (int i=0; i<existingSortColumns.length; i++) {
                        if (existingSortColumns[i] != column) {
                           sortingColumns.add(existingSortColumns[i]);
                           sortingAscendings.add( theSorter.isAsscending(existingSortColumns[i]) );
                        }
                     }
                     
                     // save new column
                     sortingColumns.add(new Integer(column));
                     sortingAscendings.add(new Boolean(ascending));
                     
                     int [] columns = new int[sortingColumns.size()];
                     boolean [] ascendings = new boolean[sortingColumns.size()];
                     for (int i=0; i<sortingColumns.size(); i++) {
                        columns[i] = sortingColumns.elementAt(i).intValue();
                        ascendings[i] = sortingAscendings.elementAt(i).booleanValue();
                     }
                     theSorter.complexSort(columns, ascendings);
                           
                  } else {
                     theSorter.sortByColumn(column, ascending);
                  }
               }
               theTable.getTableHeader().repaint();
            }
         }
         startColumn = -1;
      }
      
   }
   
}
