package org.openhealthtools.mdht.mdmi.editor.be_editor.tables;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/** A TableModel of TableEntry items */
public abstract class MdmiTableModel extends AbstractTableModel {

	private String m_columnNames[];
	
	ArrayList <TableEntry> m_elementList = new ArrayList<TableEntry>();
	boolean m_hasChanges = false;
	

	/** Define display columns (excluding first column which shows modification state) */
	public MdmiTableModel(String [] addlColumns) {
		m_columnNames = new String[1+addlColumns.length];

		m_columnNames[0] = "Modified";	// first column
		
		// add additional columns
		for (int i=0; i<addlColumns.length; i++) {
			m_columnNames[i+1] = addlColumns[i];
		}
	}

	public boolean hasChanges() {
		return m_hasChanges;
	}

	public void clearChanges() {
		this.m_hasChanges = false;
	}

	// return a (copied) list of all elements
	public List <TableEntry> getAllEntries() {
		ArrayList <TableEntry> elementList = new ArrayList<TableEntry>();
		elementList.addAll(m_elementList);
		return elementList;
	}
	
	// add an entry
	public void addEntry(Object obj) {
		// wrap
		TableEntry entry = new TableEntry(obj);
		entry.setDirty(true);
		m_elementList.add(entry);
		m_hasChanges = true;	// commit needed
	}

	// get an entry at a specific index
	public TableEntry getEntry(int idx) {
		if (idx < 0 || idx >= getRowCount()) {
			return null;
		}
		TableEntry entry = m_elementList.get(idx);
		return entry;
	}

	// delete an entry (by marking it as deleted)
	public void deleteEntry(TableEntry entry) {
		entry.setDeleted(true);
		setDirty(entry);
	}

	// mark an entry 
	public void setDirty(TableEntry entry) {
		entry.setDirty(true);
		m_hasChanges = true;	// commit needed
	}


	@Override
	public int getRowCount() {
		return m_elementList.size();
	}

	@Override
	public int getColumnCount() {
		return m_columnNames.length;
	}

	@Override
	public String getColumnName(int column) {
		return m_columnNames[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		String value = null;

		TableEntry entry = m_elementList.get(rowIndex);

		if (columnIndex == 0) {
			if (entry.isDeleted()) {
				value = "deleted";
			} else if (entry.isDirty()) {
				value = "  *  ";
			}
		}

		if (value == null) {
			value = "";
		}
		return value;
	}

}
