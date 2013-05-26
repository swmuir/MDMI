package org.openhealthtools.mdht.mdmi.editor.be_editor;

import java.awt.Dimension;
import java.awt.Frame;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

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
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
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

	// find the item with this name on the server
	@Override
	protected Object getObjectFromService(ServerInterface service, String name)
	{
		return service.getBusinessElementReference(name);
	}

	@Override
	protected boolean findItems(String searchText, RetrievePosition retrievePosition) {
		ServerInterface service = ServerInterface.getInstance();
		
		MdmiBusinessElementReference [] bers = service.getAllBusinessElementReferences(retrievePosition, searchText);
		
		// add them
		MdmiTableModel tableModel = getTableModel();
		for (MdmiBusinessElementReference ber : bers) {
			if (tableModel.findEntityWithName(ber) == null) {
				tableModel.addExistingEntry(ber);
			}
		}
		
		return true;
	}

	@Override
	protected Object createNewItem() {
		// create a new BER
		MdmiBusinessElementReference ber = null;
		Frame top = (Frame)getTopLevelAncestor();
		EditBusinessElementReferenceDlg dlg = new EditBusinessElementReferenceDlg(top);
		
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

		int rc = dlg.display(top);
		if (rc == BaseDialog.OK_BUTTON_OPTION) {
			return true;
		}
		
		return false;
	}


	@Override
	protected boolean commitChanges() {
		boolean status = true;

		Map<String, Exception> addErrorMap = new TreeMap<String, Exception>();
		Map<String, Exception> addDatatypeErrorMap = new TreeMap<String, Exception>();
		Map<String, Exception> updateErrorMap = new TreeMap<String, Exception>();
		Map<String, Exception> deleteErrorMap = new TreeMap<String, Exception>();
		
		ServerInterface service = ServerInterface.getInstance();
		
		String berName = null;
		
		Map<String, Exception> currErrorMap = null;

		// update each changed item (work backwards so delete doesn't affect order)
		MdmiTableModel tableModel = getTableModel();
		for (int row = tableModel.getRowCount()-1; row >= 0; row--) {
			
			TableEntry entry = getEntry(row);
			MdmiBusinessElementReference ber = (MdmiBusinessElementReference)entry.getUserObject();
			boolean isNew = tableModel.isNew(ber);
			boolean isDeleted = entry.isDeleted();
			
			if (isNew || entry.isDirty()) {
				berName = ber.getName();
				System.out.println("Committing " + berName);
				
				if (ber.getReference() == null) {
					URI uri = URI.create("http://dictionary.mdmi.org/bers/" + berName);
					ber.setReference(uri);
				}

				try {
					if (isDeleted) {
						// delete it (if we need to)
						if (!isNew) {
							currErrorMap = deleteErrorMap;
							service.deleteBusinessElementReference(ber);
						}
						// remove it from table
						tableModel.removeEntry(row);
					} else {
						// Add or Update
						currErrorMap = addErrorMap;
						
						// add referenced datatype first if needed
						MdmiDatatype refDatatype = ber.getReferenceDatatype();
						if (refDatatype == null) {
							throw new MdmiException("Reference Datatype is not specified");
						} else if (!addReferencedDatatype(refDatatype, addDatatypeErrorMap)) {
							status = false;
						}

						// now add or update it
						if (isNew) {
							// it's new, so set the group information
							MessageGroup group = service.getMessageGroup();
							ber.setDomainDictionaryReference(group.getDomainDictionary());

							// check if it already exists
							MdmiBusinessElementReference existing = service.getBusinessElementReference(berName);
							if (existing != null) {
								// not new - we'll update it next
								isNew = false;
							} else {
								service.addBusinessElementReference(ber);
							}

						}

						if (!isNew) {
							// update an existing one
							currErrorMap = updateErrorMap;
							service.updateBusinessElementReference(ber);
						}
					}

					// mark as clean
					entry.setDirty(false);
					
				} catch (MdmiException ex) {
					if (isNew) {
						// reset
						ber.setDomainDictionaryReference(null);
					}

					currErrorMap.put(berName, ex);
					
					// keep going
					status = false;
				}
			}
			
		}
		
		// show errors
		if (status == false) {
			StringBuilder errorMessage = new StringBuilder();
			errorMessage.append("<html><font size=\"-1\">");

			if (addErrorMap.size() != 0) {
				errorMessage.append("<font color=red><b>Unable to add the following business elements:</b></font><br>");
				errorMessage.append(createErrorMessage(addErrorMap));
			}

			if (updateErrorMap.size() != 0) {
				if (errorMessage.length() > 0) errorMessage.append("<br><br>");
				errorMessage.append("<font color=red><b>Unable to update the following business elements:</b></font>");
				errorMessage.append(createErrorMessage(updateErrorMap));
			}

			if (deleteErrorMap.size() != 0) {
				if (errorMessage.length() > 0) errorMessage.append("<br><br>");
				errorMessage.append("<font color=red><b>Unable to delete the following business elements:</b></font>");
				errorMessage.append(createErrorMessage(deleteErrorMap));
			}
			

			if (addDatatypeErrorMap.size() != 0) {
				if (errorMessage.length() > 0) errorMessage.append("<br><br>");
				errorMessage.append("<font color=red><b>Unable to add the following datatypes:</b></font>");
				errorMessage.append(createErrorMessage(addDatatypeErrorMap));
			}

			errorMessage.append("</font></html>");

			Frame top = SystemContext.getApplicationFrame();
			JEditorPane display = new JEditorPane();
			display.setContentType("text/html");
			display.setText(errorMessage.toString());
			JScrollPane scroller = new JScrollPane(display);
			scroller.getViewport().setPreferredSize(new Dimension(500, 300));
			JOptionPane.showMessageDialog(top, scroller, 
					"Error Committing Business Elements", JOptionPane.ERROR_MESSAGE);
		}
		
		
		return status;
	}

	
	// Return all Business Element References in the table
	public Collection<MdmiBusinessElementReference> getAllBusinessElementReferences() {
		ArrayList<MdmiBusinessElementReference> allBERs = new ArrayList<MdmiBusinessElementReference>();
		
		for (int row = 0 ; row < getTableModel().getRowCount(); row++) {
			TableEntry entry = getEntry(row);
			MdmiBusinessElementReference ber = (MdmiBusinessElementReference)entry.getUserObject(); 
			allBERs.add(ber);
		}
		
		return allBERs;
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
					value = ref.getReferenceDatatype().getTypeName();
				}
				
			} else {
				value = super.getValueAt(rowIndex, columnIndex);
			}
			
			return value;
		}


		@Override
		public String getObjectName(Object obj) {
			if (obj instanceof MdmiBusinessElementReference) {
				String name = ((MdmiBusinessElementReference)obj).getName();
				return name;
			}

			return null;
		}


		@Override
		public String getObjectTypeName(Object obj) {
			String type = ClassUtil.beautifyName(obj.getClass());
			return type;
		}

		// a New BER has a null dictionary
		@Override
		public boolean isNew(Object obj)
		{
			if (obj instanceof MdmiBusinessElementReference &&
					((MdmiBusinessElementReference)obj).getDomainDictionaryReference() == null) {
				return true;
			}
			return false;
		}
	}

	
}
