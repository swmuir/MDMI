package org.openhealthtools.mdht.mdmi.editor.be_editor;

import java.awt.Frame;
import java.util.ResourceBundle;

import org.openhealthtools.mdht.mdmi.editor.be_editor.ServerInterface.RetrievePosition;
import org.openhealthtools.mdht.mdmi.editor.be_editor.tables.MdmiTableModel;
import org.openhealthtools.mdht.mdmi.editor.be_editor.tables.TableEntry;
import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;

public class DataTypeDisplayPanel extends AbstractDisplayPanel {
	
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.be_editor.Local");

	public DataTypeDisplayPanel() {
		super(s_res.getString("DatatypeDisplayPanel.objType"));
		setTableModel(new DatatypeTableModel());
	}

	// set the column widths
	@Override
	protected void setTableColumnWidths() {
		super.setTableColumnWidths();	// sets first column
		for (int c=1; c<getTable().getColumnCount(); c++) {
			int width =  (c == 1 ? 300 : 100);
			setColumnWidth(c, width);
		}
	}

	//////////////////////////////////////
	// Abstract Methods
	/////////////////////////////////////


	@Override
	protected boolean findItems(String searchText) {
		ServerInterface service = ServerInterface.getInstance();
		
		// start from the beginning
		RetrievePosition pos = new RetrievePosition();
		MdmiDatatype[] datatypes = service.getAllDatatypes(pos, searchText);
		
		// add them
		for (MdmiDatatype datatype : datatypes) {
			getTableModel().addEntry(datatype);
		}
		
		return true;
	}

	@Override
	protected Object createNewItem() {
		// create a new DataType
		MdmiDatatype datatype = null;
		Frame top = SystemContext.getApplicationFrame();
		EditComplexDatatypeDlg dlg = new EditComplexDatatypeDlg(top, null);
		
		// TODO - make non-modal
		int rc = dlg.display(top);
		if (rc == BaseDialog.OK_BUTTON_OPTION) {
			datatype = dlg.getDatatype();
		}
		
		return datatype;
	}

	@Override
	protected boolean modifyItem(TableEntry entry) {

		if (entry == null) {
			return false;
		}
		
		MdmiDatatype datatype = (MdmiDatatype)entry.getUserObject(); 
		if (datatype instanceof DTComplex) {

			Frame top = SystemContext.getApplicationFrame();
			EditComplexDatatypeDlg dlg = new EditComplexDatatypeDlg(top, (DTComplex)datatype);

			// TODO - make non-modal
			int rc = dlg.display(top);
			if (rc == BaseDialog.OK_BUTTON_OPTION) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	protected boolean undoChanges(TableEntry entry) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	protected boolean commitChanges() {
		ServerInterface service = ServerInterface.getInstance();
		
		// update each changed item
		for (int row = 0 ; row < getTableModel().getRowCount(); row++) {
			TableEntry entry = getTableModel().getEntry(row);
			if (entry.isDirty()) {
				MdmiDatatype datatype = (MdmiDatatype)entry.getUserObject();

				boolean isNew = (datatype.getOwner() == null);
				if (entry.isDeleted()) {
					// delete it (if we need to)
					if (!isNew) {
						service.deleteDatatype(datatype);
					}
					
				} else if (isNew) {
					// it's new, so set the group information
					MessageGroup group = service.getMessageGroup();
					datatype.setOwner(group);
					service.addDatatype(datatype);
					
				} else {
					// update an existing one
					service.updateDatatype(datatype);
				}
				
			}
			
		}
		
		return true;
	}
	
	

	
	////////////////////////////////////////////////////////////
	//    Table Model
	////////////////////////////////////////////////////////////
	
	private class DatatypeTableModel extends MdmiTableModel  {
		
		public DatatypeTableModel() {
			super(new String[]{
						s_res.getString("DatatypeDisplayPanel.nameCol")	// Name
						});
		}


		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object value = "";
			
			TableEntry entry = getEntry(rowIndex);
			MdmiDatatype datatype = (MdmiDatatype)entry.getUserObject();
			
			if (columnIndex == 1) {
				// Name
				value = datatype.getName();
				
			} else {
				value = super.getValueAt(rowIndex, columnIndex);
			}
			
			return value;
		}
		
	}

	
}
