package org.openhealthtools.mdht.mdmi.editor.be_editor.tables;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.openhealthtools.mdht.mdmi.model.DTSEnumerated;
import org.openhealthtools.mdht.mdmi.model.EnumerationLiteral;


/** A table for editing the EnumLiteral fields of an Enumerated datatype */
public class EnumLiteralTable extends JTable {

    private static String[] s_columnNames = {"Name",
                                    "Code",
                                    "Description"};

    protected EditableTableModel m_tableModel;
    
    public EnumLiteralTable() {
    	m_tableModel = new EditableTableModel();
    	setModel(m_tableModel);

    	// set table props
    	setVisibleRowCount(20);
    	setFillsViewportHeight(true);
        setCellSelectionEnabled(true);
        
        // set column widths
        for (int i=0; i<m_tableModel.getColumnCount(); i++) {
        	int width = 60;
        	if (i == 1) width = 30;
        	else if (i == 2) width = 200;
    		TableColumn column = getColumnModel().getColumn(i);
    		column.setWidth(width);
    		column.setPreferredWidth(width);
    	
        }
    }

	public void setVisibleRowCount(int rows){ 
	    setPreferredScrollableViewportSize(new Dimension( 
	   		 getPreferredScrollableViewportSize().width, 
	            rows*getRowHeight() 
	    )); 
	} 
    
    // populate view from data type
    public void initialize(DTSEnumerated dataType)
    {
    	m_tableModel.initialize(dataType);
    	
    	// add blanks to make the table at least 18 rows
    	int blankRows = 18;
    	if (dataType !=  null && dataType.getLiterals() != null) {
    		blankRows -= dataType.getLiterals().size();
    	}
    	blankRows = Math.max(blankRows, 5);	// at least 5 blanks
    	m_tableModel.addBlankEntries(blankRows);
    }
    
    // populate list
    public boolean populateModel(DTSEnumerated dataType) {
    	// TODO validate
    	
    	dataType.getLiterals().clear();
    	for (EnumLiteralData data : m_tableModel.getData()) {
    		EnumerationLiteral literal = data.makeEnumerationLiteral();
    		
    		dataType.getLiterals().add(literal);
    	}
    	
    	return true;
    }
    
	// Data in a row of the table
	protected class EnumLiteralData {
		String name = "";
		String code = "";
		String description = "";
		
		EnumLiteralData() {
			// default
		}
		
		EnumLiteralData(EnumerationLiteral literal) {
			name = literal.getName();
			code = literal.getCode();
			description = literal.getDescription();
		}
		

		EnumerationLiteral makeEnumerationLiteral() {
			EnumerationLiteral literal = new EnumerationLiteral();
			literal.setName(name);
			literal.setCode(code);
			literal.setDescription(description);
			return literal;
		}
		
	}
	
	// The model will grow so that there are blank entries
	protected class EditableTableModel extends AbstractTableModel {
		
		ArrayList<EnumLiteralData> m_data = new ArrayList<EnumLiteralData>();

		public EditableTableModel() {
			super();
		}
		
		// Initialize from a datatype
		public void initialize(DTSEnumerated datatype) {
			if (datatype == null || datatype.getLiterals() == null) {
				return;
			}
			
			ArrayList<EnumerationLiteral> literals = datatype.getLiterals();
			for (EnumerationLiteral literal : literals) {
				EnumLiteralData data = new EnumLiteralData(literal);
				m_data.add(data);
			}
		}
		
		public void addBlankEntries(int n) {
			int lastRow = m_data.size();
			for (int i=0; i<n; i++) {
				m_data.add(new EnumLiteralData());
			}
        	fireTableRowsInserted(lastRow, lastRow+n-1);
		}
		
		public List<EnumLiteralData> getData() {
			return m_data;
		}

		@Override
		public int getRowCount() {
			return m_data.size();
		}

		@Override
		public int getColumnCount() {
			return s_columnNames.length;
		}

		@Override
		public String getColumnName(int column) {
			return s_columnNames[column];
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = null;
			EnumLiteralData data = m_data.get(row);
			if (col == 0) {
				value = data.name;
			} else if (col == 1) {
				value = data.code;
			} else if (col == 2) {
				value = data.description;
			}
			return value;
		}

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  
         */
		@Override
        public Class<?> getColumnClass(int c) {
            return String.class;
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
		@Override
        public boolean isCellEditable(int row, int col) {
			return true;
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
		@Override
        public void setValueAt(Object value, int row, int col) {
			if (value == null) {
				value = "";
			}
			EnumLiteralData data = m_data.get(row);
			if (col == 0) {
				data.name = value.toString();
			} else if (col == 1) {
				data.code = value.toString();
			} else if (col == 2) {
				data.description = value.toString();
			}

            fireTableCellUpdated(row, col);
            
            // if we've added data to the last row, add more rows
            if (row == m_data.size()-1) {
            	addBlankEntries(5);
            }
        }

	}
}
