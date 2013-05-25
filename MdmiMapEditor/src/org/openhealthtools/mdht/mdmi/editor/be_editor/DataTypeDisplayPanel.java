package org.openhealthtools.mdht.mdmi.editor.be_editor;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.openhealthtools.mdht.mdmi.MdmiException;
import org.openhealthtools.mdht.mdmi.editor.be_editor.ServerInterface.RetrievePosition;
import org.openhealthtools.mdht.mdmi.editor.be_editor.tables.MdmiTableModel;
import org.openhealthtools.mdht.mdmi.editor.be_editor.tables.TableEntry;
import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.model.DTCStructured;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.DTExternal;
import org.openhealthtools.mdht.mdmi.model.DTSDerived;
import org.openhealthtools.mdht.mdmi.model.DTSEnumerated;
import org.openhealthtools.mdht.mdmi.model.Field;
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
			int width =  (c == 1 ? 200 : 80);
			setColumnWidth(c, width);
		}
	}

	//////////////////////////////////////
	// Abstract Methods
	/////////////////////////////////////

	// find the item with this name on the server
	@Override
	protected Object getObjectFromService(ServerInterface service, String name)
	{
		return service.getDatatype(name);
	}

	@Override
	protected boolean findItems(String searchText, RetrievePosition retrievePosition) {
		ServerInterface service = ServerInterface.getInstance();
		
		MdmiDatatype[] datatypes = service.getAllDatatypes(retrievePosition, searchText);
		
		// add them
		MdmiTableModel tableModel = getTableModel();
		for (MdmiDatatype datatype : datatypes) {
			if (tableModel.findEntityWithName(datatype) == null) {
				tableModel.addExistingEntry(datatype);
			}
		}
		
		return true;
	}

	@Override
	protected Object createNewItem() {
		// TODO: which type
		// create a new DataType
		MdmiDatatype datatype = null;
		Frame top = SystemContext.getApplicationFrame();

		EditDatatypeDlg dlg = null;
		
		// prompt for type
		String [] datatypeTypes = { "Complex", "External", "Derived", "Enumerated" };
		
		String sel = (String)JOptionPane.showInputDialog(top,
		                    "What type of Datatype do you want to create?", "Datatype Selection",
		                    JOptionPane.QUESTION_MESSAGE, null,
		                    datatypeTypes, datatypeTypes[0]);
		
		
		
		if (sel.equals(datatypeTypes[0])) {
			dlg = new EditComplexDatatypeDlg(top, null);
		} else if (sel.equals(datatypeTypes[1])) {
			dlg = new EditExternalDatatypeDlg(top, null);
		} else if (sel.equals(datatypeTypes[2])) {
			dlg = new EditDerivedDatatypeDlg(top, null);
		} else if (sel.equals(datatypeTypes[3])) {
			dlg = new EditEnumDatatypeDlg(top, null);
		}
		
		if (dlg == null) {
			return null;
		}
		
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
		Frame top = SystemContext.getApplicationFrame();
		EditDatatypeDlg dlg = null;
		if (datatype instanceof DTComplex) {
			dlg = new EditComplexDatatypeDlg(top, (DTComplex)datatype);
		} else if (datatype instanceof DTExternal) {
			dlg = new EditExternalDatatypeDlg(top, (DTExternal)datatype);
		} else if (datatype instanceof DTSDerived) {
			dlg = new EditDerivedDatatypeDlg(top, (DTSDerived)datatype);
		} else if (datatype instanceof DTSEnumerated) {
			dlg = new EditEnumDatatypeDlg(top, (DTSEnumerated)datatype);
		}

		if (dlg != null) {
			int rc = dlg.display(top);
			if (rc == BaseDialog.OK_BUTTON_OPTION) {
				return true;
			}
		}
		
		return false;
	}


	@Override
	protected boolean commitChanges() {
		boolean status = true;
		
		StringBuilder addErrors = new StringBuilder();
		StringBuilder updateErrors = new StringBuilder();
		StringBuilder deleteErrors = new StringBuilder();
		
		ServerInterface service = ServerInterface.getInstance();
		
		String datatypeName = null;
		StringBuilder errors = null;

		// update each changed item (work backwards so delete doesn't affect order)
		MdmiTableModel tableModel = getTableModel();
		for (int row = tableModel.getRowCount()-1; row >= 0; row--) {
			
			TableEntry entry = getEntry(row);
			MdmiDatatype datatype = (MdmiDatatype)entry.getUserObject();
			datatypeName = datatype.getTypeName();
			
			boolean isNew = tableModel.isNew(datatype);
			boolean isDeleted = entry.isDeleted();
			
			if (isNew || entry.isDirty()) {

				try {
					if (isDeleted) {
						// delete it (if we need to)
						if (!isNew) {
							errors = deleteErrors;
							service.deleteDatatype(datatype);
						}
						// remove it from table
						tableModel.removeEntry(row);

					} else {
						// Add or Update
						errors = addErrors;
						
						// check referenced datatypes 
						if (datatype instanceof DTSDerived) {
							MdmiDatatype basetype = ((DTSDerived)datatype).getBaseType();

							if (basetype == null) {
								throw new MdmiException("Base Datatype is not specified");
							}
						} else if (datatype instanceof DTCStructured) {
							for (Field field : ((DTCStructured)datatype).getFields()) {
								MdmiDatatype fieldDatatype = field.getDatatype();
								if (fieldDatatype == null) {
									throw new MdmiException("Field Datatype is not specified for " +
											datatype.getTypeName() + "." + field.getName());
								}
							}
						}
						
						// now add or update it
						if (isNew) {
							// it's new, so set the group information
							MessageGroup group = service.getMessageGroup();
							datatype.setOwner(group);

							// first check if it already exists
							MdmiDatatype existing = service.getDatatype(datatypeName);
							if (existing != null) {
								// not new - we'll update it next
								isNew = false;
							} else {
								service.addDatatype(datatype);
							}
						}

						if (!isNew){
							// update an existing one
							errors = updateErrors;
							service.updateDatatype(datatype);
						}
					}

					// mark as clean
					entry.setDirty(false);
					
				} catch (MdmiException ex) {
					if (isNew) {
						// reset
						datatype.setOwner(null);
					}

					appendErrorText(errors, datatypeName, ex);
					
					// keep going
					status = false;
				}
			}
			
		}
		
		// show errors
		if (status == false) {
			errors = new StringBuilder();
			errors.append("<html><font size=\"-1\">");
			if (addErrors.length() != 0) {
				errors.append("<font color=red><b>Unable to add the following datatypes:</b></font>").append(addErrors);
			}

			if (updateErrors.length() != 0) {
				if (errors.length() > 0) errors.append("<br><br>");
				errors.append("<font color=red><b>Unable to update the following datatypes:</b></font>").append(updateErrors);
			}

			if (deleteErrors.length() != 0) {
				if (errors.length() > 0) errors.append("<br><br>");
				errors.append("<font color=red><b>Unable to delete the following datatypes:</b></font>").append(deleteErrors);
			}
			errors.append("</font></html>");

			Frame top = SystemContext.getApplicationFrame();
			JEditorPane display = new JEditorPane();
			display.setContentType("text/html");
			display.setText(errors.toString());
			JScrollPane scroller = new JScrollPane(display);
			scroller.getViewport().setPreferredSize(new Dimension(500, 300));
			JOptionPane.showMessageDialog(top, scroller, 
					"Error Committing Datatypes", JOptionPane.ERROR_MESSAGE);
		}
		
		
		return status;
	}
	
	// Return all Datatypes in the table
	public Collection<MdmiDatatype> getAllDatatypes() {
		ArrayList<MdmiDatatype> allDatatypes = new ArrayList<MdmiDatatype>();
		
		for (int row = 0 ; row < getTableModel().getRowCount(); row++) {
			TableEntry entry = getEntry(row);
			MdmiDatatype datatype = (MdmiDatatype)entry.getUserObject(); 
			allDatatypes.add(datatype);
		}
		
		return allDatatypes;
	}

	
	////////////////////////////////////////////////////////////
	//    Table Model
	////////////////////////////////////////////////////////////
	
	private class DatatypeTableModel extends MdmiTableModel  {
		
		public DatatypeTableModel() {
			super(new String[]{
						s_res.getString("DatatypeDisplayPanel.nameCol"),	// Name
						s_res.getString("DatatypeDisplayPanel.typeCol")		// Type
						});
		}


		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object value = "";
			
			TableEntry entry = getEntry(rowIndex);
			MdmiDatatype datatype = (MdmiDatatype)entry.getUserObject();
			
			if (columnIndex == 1) {
				// Name
				value = datatype.getTypeName();

			} else if (columnIndex == 2) {
				// Type
				String type = ClassUtil.beautifyName(datatype.getClass());
				// strip off the word "datatype"
				int idx = type.indexOf("datatype");
				if (idx != -1) {
					type = type.substring(0, idx).trim();
				}
				value = type;
				
			} else {
				value = super.getValueAt(rowIndex, columnIndex);
			}
			
			return value;
		}


		@Override
		public String getObjectName(Object obj) {
			if (obj instanceof MdmiDatatype) {
				String name = ((MdmiDatatype)obj).getTypeName();
				return name;
			}

			return null;
		}


		@Override
		public String getObjectTypeName(Object obj) {
			String type = ClassUtil.beautifyName(obj.getClass());
			return type;
		}
		
		// a New datatype has a null owner
		@Override
		public boolean isNew(Object obj)
		{
			if (obj instanceof MdmiDatatype && ((MdmiDatatype)obj).getOwner() == null) {
				return true;
			}
			return false;
		}

	}
	
}
