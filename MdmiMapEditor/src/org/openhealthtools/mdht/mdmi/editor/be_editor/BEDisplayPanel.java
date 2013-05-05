package org.openhealthtools.mdht.mdmi.editor.be_editor;

import java.awt.Frame;
import java.util.ResourceBundle;

import org.openhealthtools.mdht.mdmi.editor.be_editor.ServerInterface.RetrievePosition;
import org.openhealthtools.mdht.mdmi.editor.be_editor.tables.MdmiTableModel;
import org.openhealthtools.mdht.mdmi.editor.be_editor.tables.TableEntry;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;

public class BEDisplayPanel extends AbstractDisplayPanel {
	
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.be_editor.Local");

	public BEDisplayPanel() {
		super(s_res.getString("BEDisplayPanel.objType"));
		setTableModel(new BETableModel());
	}

	// set the column widths
	@Override
	protected void setTableColumnWidths() {
		super.setTableColumnWidths();	// sets first column
		for (int c=1; c<getTable().getColumnCount(); c++) {
			int width =  (c == 1 ? 200 : 100);
			setColumnWidth(c, width);
		}
	}

	//////////////////////////////////////
	// Abstract Methods
	/////////////////////////////////////


	@Override
	protected boolean findItems(String searchText) {
		// TODO Auto-generated method stub
		ServerInterface service = ServerInterface.getInstance();
		
		// start from the beginning
		RetrievePosition pos = new RetrievePosition();
		MdmiBusinessElementReference [] bers = service.getAllBusinessElementReferences(pos, searchText);
		
		// add them
		for (MdmiBusinessElementReference ber : bers) {
			getTableModel().addEntry(ber);
		}
		
		return true;
	}

	@Override
	protected Object createNewItem() {
		// create a new BER
		MdmiBusinessElementReference ber = null;
		Frame top = (Frame)getTopLevelAncestor();
		EditBusinessElementReferenceDlg dlg = new EditBusinessElementReferenceDlg(top);
		
		// TODO - make non-modal
		int rc = dlg.display(top);
		if (rc == BaseDialog.OK_BUTTON_OPTION) {
			ber = dlg.getBusinessElementReference();
		}
		
		return ber;
	}

	@Override
	protected boolean modifyItem(TableEntry entry) {

		if (entry == null) {
			return false;
		}
		
		MdmiBusinessElementReference ber = (MdmiBusinessElementReference)entry.getUserObject(); 
		
		Frame top = (Frame)getTopLevelAncestor();
		EditBusinessElementReferenceDlg dlg = new EditBusinessElementReferenceDlg(top, ber);

		// TODO - make non-modal
		int rc = dlg.display(top);
		if (rc == BaseDialog.OK_BUTTON_OPTION) {
			return true;
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
				MdmiBusinessElementReference ber = (MdmiBusinessElementReference)entry.getUserObject();
				boolean isNew = (ber.getDomainDictionaryReference() == null);
				if (entry.isDeleted()) {
					// delete it (if we need to)
					if (!isNew) {
						service.deleteBusinessElementReference(ber);
					}
					
				} else if (isNew) {
					// it's new, so set the group information
					MessageGroup group = service.getMessageGroup();
					ber.setDomainDictionaryReference(group.getDomainDictionary());
					service.addBusinessElementReference(ber);
					
				} else {
					// update an existing one
					service.updateBusinessElementReference(ber);
				}
				
			}
			
		}
		
		return true;
	}
	
	

	
	////////////////////////////////////////////////////////////
	//    Table Model
	////////////////////////////////////////////////////////////
	
	private class BETableModel extends MdmiTableModel  {
		
		public BETableModel() {
			super(new String[]{
						s_res.getString("BEDisplayPanel.nameCol"),	// Name
						s_res.getString("BEDisplayPanel.datatypeCol")  // Datatype
						});
		}


		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object value = "";
			
			TableEntry entry = getEntry(rowIndex);
			MdmiBusinessElementReference ref = (MdmiBusinessElementReference)entry.getUserObject();
			
			if (columnIndex == 1) {
				// Name
				value = ref.getName();
				
			} else if (columnIndex == 2) {
				// DataType
				if (ref.getReferenceDatatype() != null) {
					value = ref.getReferenceDatatype().getName();
				}
				
			} else {
				value = super.getValueAt(rowIndex, columnIndex);
			}
			
			return value;
		}
		
	}

	
}
