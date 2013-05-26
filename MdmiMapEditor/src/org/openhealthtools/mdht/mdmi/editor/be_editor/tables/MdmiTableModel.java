package org.openhealthtools.mdht.mdmi.editor.be_editor.tables;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.openhealthtools.mdht.mdmi.editor.be_editor.actions.LeaveOrReplaceDialog.LeaveReplaceOption;

/** A TableModel of TableEntry items */
public abstract class MdmiTableModel extends AbstractTableModel {

	private String m_columnNames[];
	
	protected ArrayList <TableEntry> m_elementList = new ArrayList<TableEntry>();
	protected boolean m_hasChanges = false;
	
	protected LeaveReplaceOption m_defaultOption = LeaveReplaceOption.Unknown;
	
	public LeaveReplaceOption getDefaultReplaceOption() {
		return m_defaultOption;
	}
	public void setDefaultReplaceOption(LeaveReplaceOption option) {
		m_defaultOption = option;
	}

	/** Define display columns (excluding first column which shows modification state) */
	public MdmiTableModel(String [] addlColumns) {
		m_columnNames = new String[1+addlColumns.length];

		m_columnNames[0] = "";	// first column
		
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
	
	// add an existing entry to table
	public TableEntry addExistingEntry(Object obj) {
		// wrap
		TableEntry entry = new TableEntry(obj);
		m_elementList.add(entry);
		return entry;
	}
	
	// add a new entry
	public TableEntry addNewEntry(Object obj) {
		// wrap
		TableEntry entry = new TableEntry(obj);
		m_elementList.add(entry);
		// commit needed
		m_hasChanges = true;
		return entry;
	}
	
	public TableEntry findEntityWithName(Object userObj) {
		for (TableEntry entry : m_elementList) {
			if (objectsMatch(entry.getUserObject(), userObj)) {
				return entry;
			}
		}
		return null;
	}
	

	public boolean objectsMatch(Object obj1, Object obj2) {
		String name1 = getObjectName(obj1);
		String name2 = getObjectName(obj2);

		if (name1 == null) name1 = "";
		if (name2 == null) name2 = "";

		return name1.compareToIgnoreCase(name2) == 0;
	}

	public abstract String getObjectTypeName(Object obj);
	public abstract String getObjectName(Object obj);
	public abstract boolean isNew(Object obj);

	// get an entry at a specific index
	public TableEntry getEntry(int idx) {
		if (idx < 0 || idx >= getRowCount()) {
			return null;
		}
		TableEntry entry = m_elementList.get(idx);
		return entry;
	}

	// physically remove an entry
	public void removeEntry(int idx) {
		m_elementList.remove(idx);
	}

	// physically remove an entry
	public void removeEntry(TableEntry entry) {
		m_elementList.remove(entry);
	}

	// delete an entry (by marking it as deleted)
	public void markDeleted(TableEntry entry) {
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
			if (isNew(entry.getUserObject())) {
				value = "  new  ";
			} else if (entry.isDeleted()) {
				value = "deleted";
			} else if (entry.isDirty()) {
				value = " dirty ";
			}
		}

		if (value == null) {
			value = "";
		}
		return value;
	}

}
