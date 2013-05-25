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
package org.openhealthtools.mdht.mdmi.editor.be_editor.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.openhealthtools.mdht.mdmi.editor.be_editor.BEEditor;
import org.openhealthtools.mdht.mdmi.editor.be_editor.DataTypeDisplayPanel;
import org.openhealthtools.mdht.mdmi.editor.be_editor.actions.LeaveOrReplaceDialog.LeaveReplaceOption;
import org.openhealthtools.mdht.mdmi.editor.be_editor.tables.MdmiTableModel;
import org.openhealthtools.mdht.mdmi.editor.be_editor.tables.TableEntry;
import org.openhealthtools.mdht.mdmi.editor.be_editor.tools.DatatypeReplacement;
import org.openhealthtools.mdht.mdmi.editor.common.actions.AbstractMenuAction;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ModelIOUtilities;
import org.openhealthtools.mdht.mdmi.model.DTSPrimitive;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;

public class ImportDatatypesAction extends AbstractMenuAction {
	private static final long serialVersionUID = -1;

	@Override
	public void execute(ActionEvent actionEvent) {

		// prompt for input
		ArrayList<MessageGroup> groups = ModelIOUtilities.promptAndReadModel();

		if (groups != null) {
			List<MdmiDatatype> dataTypes = new ArrayList<MdmiDatatype>();
			for (MessageGroup group : groups) {
				dataTypes.addAll(group.getDatatypes());
			}
			
			addDataTypes(dataTypes);
		}
  
	}

	public static void addDataTypes(List<MdmiDatatype> newDataTypesList) {
		// prompt for input
		final BEEditor editor = BEEditor.getInstance();
		final DataTypeDisplayPanel displayPanel = editor.getDataTypeDisplayPanel();


		MdmiTableModel tableModel = displayPanel.getTableModel();
		tableModel.setDefaultReplaceOption(LeaveReplaceOption.Unknown);	// clear response
		
		for (MdmiDatatype datatype : newDataTypesList) {
			// add all non-primitive data types
			if (datatype instanceof DTSPrimitive) {
				continue;
			}

			datatype.setOwner(null);	// set the owner to null so it looks new
			
			// We have three possibilities when replacing.
			// 1. Simple case - it's a new datatype
			// 2. Datatype already exists:
			//    a. User says to replace it.
			//       + We'll need to replace all references to the old type in the application 
			//         with the new type
			//    b. User says to keep it.
			//       + We'll need to replace all references to the new type in the application 
			//         with the old type
			//       + We'll need to replace all references to the new type in our import list 
			//         with the old type
			//      

			// does it already exist?
			TableEntry existing = tableModel.findEntityWithName(datatype);
			
			// easy case
			if (existing == null) {
				tableModel.addNewEntry(datatype);
				displayPanel.tableSizeChanged();
				
			} else {
				if (!handleDuplicateDataType(tableModel, newDataTypesList, existing, datatype)) {
					return;
				}
			}
			
		}


		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				displayPanel.getTableSorter().sortByColumn(1);	// name column
				displayPanel.getTable().revalidate();
				displayPanel.enableFields();
				// bring tab to the front
				editor.setActive(displayPanel);
			}
		});
	}
	
	private static boolean handleDuplicateDataType(MdmiTableModel tableModel, List<MdmiDatatype> replacementList,
			TableEntry existingEntry, MdmiDatatype newType) {
		
		LeaveReplaceOption replaceOption = tableModel.getDefaultReplaceOption();
		
		if (replaceOption == LeaveReplaceOption.LeaveAll) {
			// don't need to prompt
			return leaveDuplicateDataType(tableModel, replacementList, existingEntry, newType);
			
		} else if (replaceOption == LeaveReplaceOption.ReplaceAll) {
			// don't need to prompt
			return replaceDuplicateDataType(tableModel, replacementList, existingEntry, newType);
			
		} else {
			// prompt
			LeaveReplaceOption status = LeaveOrReplaceDialog.ShowDialog(BEEditor.getInstance(), 
					tableModel.getObjectTypeName(existingEntry.getUserObject()), 
					tableModel.getObjectName(existingEntry.getUserObject()));
			
			if (status == LeaveReplaceOption.Cancel) {
				return false;	// user quits
				
			} else if (status == LeaveReplaceOption.Leave) {
				// leave it
				return leaveDuplicateDataType(tableModel, replacementList, existingEntry, newType);
				
			} else if (status == LeaveReplaceOption.LeaveAll) {
				// leave it - don't prompt again
				tableModel.setDefaultReplaceOption(status);	// save response
				return leaveDuplicateDataType(tableModel, replacementList, existingEntry, newType);
				
			} else if (status == LeaveReplaceOption.Replace) {
				// replace it
				return replaceDuplicateDataType(tableModel, replacementList, existingEntry, newType);
				
			} else if (status == LeaveReplaceOption.ReplaceAll) {
				// replace it - don't prompt again
				tableModel.setDefaultReplaceOption(status);	// save response
				return replaceDuplicateDataType(tableModel, replacementList, existingEntry, newType);
			} 
		}
		
		return true;
	}

	// leave the duplicated datatype in the table
	private static boolean leaveDuplicateDataType(MdmiTableModel tableModel, List<MdmiDatatype> replacementList,
			TableEntry existingEntry, MdmiDatatype importedDataType) {
		MdmiDatatype appDataType = (MdmiDatatype)existingEntry.getUserObject();

		// replace all references to the imported datatype in the model
		//   (just in case something was added earlier that references it) 
		DatatypeReplacement.replaceDataType(importedDataType, appDataType);

		// replace all references to the imported datatype in the import list
		//   (for future adds)
		DatatypeReplacement.replaceAllDatatypes(replacementList, importedDataType, appDataType);
		return true;
	}

	// replace all occurrences of the duplicated datatype  with the new one
	private static boolean replaceDuplicateDataType(MdmiTableModel tableModel, List<MdmiDatatype> replacementList,
			TableEntry existingEntry, MdmiDatatype importedDataType) {
		MdmiDatatype appDataType = (MdmiDatatype)existingEntry.getUserObject();
		
		// replace the datatype in the table
		existingEntry.setUserObject(importedDataType);
		
		// replace all references to the datatype in the model with the imported datatype
		DatatypeReplacement.replaceDataType(appDataType, importedDataType);
		
		return true;
	}

}
