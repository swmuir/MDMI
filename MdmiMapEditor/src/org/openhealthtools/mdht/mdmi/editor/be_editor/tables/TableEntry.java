package org.openhealthtools.mdht.mdmi.editor.be_editor.tables;



/** Data in a table */
public class TableEntry {

	private Object m_userObject;
	private boolean m_dirty;
	private boolean m_deleted;
	
	public TableEntry(Object userObject) {
		m_userObject = userObject;
		m_dirty = false;
		m_deleted = false;
	}

	public Object getUserObject() {
		return m_userObject;
	}

	public void setUserObject(Object obj) {
		m_userObject = obj;
	}


	public boolean isDirty() {
		return m_dirty;
	}

	public void setDirty(boolean dirty) {
		this.m_dirty = dirty;
	}

	public boolean isDeleted() {
		return m_deleted;
	}

	public void setDeleted(boolean deleted) {
		this.m_deleted = deleted;
	}
	
}
