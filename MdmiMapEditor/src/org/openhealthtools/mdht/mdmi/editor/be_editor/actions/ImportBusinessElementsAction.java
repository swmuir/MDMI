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

import org.openhealthtools.mdht.mdmi.editor.be_editor.BEDisplayPanel;
import org.openhealthtools.mdht.mdmi.editor.be_editor.BEEditor;
import org.openhealthtools.mdht.mdmi.editor.be_editor.actions.LeaveOrReplaceDialog.LeaveReplaceOption;
import org.openhealthtools.mdht.mdmi.editor.be_editor.tables.MdmiTableModel;
import org.openhealthtools.mdht.mdmi.editor.be_editor.tables.TableEntry;
import org.openhealthtools.mdht.mdmi.editor.common.actions.AbstractMenuAction;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ModelIOUtilities;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;

public class ImportBusinessElementsAction extends AbstractMenuAction {
	private static final long serialVersionUID = -1;

	@Override
	public void execute(ActionEvent actionEvent) {
		// prompt for input
		final BEEditor editor = BEEditor.getInstance();
		final BEDisplayPanel displayPanel = editor.getBEDisplayPanel();

		ArrayList<MessageGroup> groups = ModelIOUtilities.promptAndReadModel();
		if (groups == null) {
			return;
		}


		List<MdmiBusinessElementReference> newBERList = new ArrayList<MdmiBusinessElementReference>();
		for (MessageGroup group : groups) {
			newBERList.addAll(group.getDomainDictionary().getBusinessElements());
		}
		
		MdmiTableModel tableModel = displayPanel.getTableModel();
		tableModel.setDefaultReplaceOption(LeaveReplaceOption.Unknown);	// clear response

		// Keep track of Data Types - we'll add if we don't have them
		ArrayList<MdmiDatatype> dataTypesNeeded = new ArrayList<MdmiDatatype>();

		for (MdmiBusinessElementReference ber : newBERList) {

			ber.setDomainDictionaryReference(null);	// set the dictionary reference to null so it looks new
			
			// We have three possibilities when replacing.
			// 1. Simple case - it's a new BER
			// 2. BER already exists:
			//    a. User says to replace it.
			//    b. User says to keep it.

			// does it already exist?
			TableEntry existing = tableModel.findEntityWithName(ber);

			// easy case
			if (existing == null) {
				displayPanel.getTableModel().addNewEntry(ber);
				displayPanel.tableSizeChanged();

			} else {
				if (!handleDuplicateBERs(tableModel, newBERList, existing, ber)) {
					return;
				}
			}
			

			// keep track of the dataTypes
			MdmiDatatype referenceDatatype = ber.getReferenceDatatype();
			if (referenceDatatype != null && !dataTypesNeeded.contains(referenceDatatype)) {
				dataTypesNeeded.add(referenceDatatype);
			}
		}

		// now add data types
		ImportDatatypesAction.addDataTypes(dataTypesNeeded);

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				displayPanel.tableSizeChanged();
				displayPanel.getTableSorter().sortByColumn(1);	// name column
				displayPanel.getTable().revalidate();
				displayPanel.enableFields();
				// bring tab to the front
				editor.setActive(displayPanel);
			}
		});

	}

	
	private static boolean handleDuplicateBERs(MdmiTableModel tableModel,
			List<MdmiBusinessElementReference> replacementList,
			TableEntry existingEntry, MdmiBusinessElementReference newBER) {
		
		LeaveReplaceOption replaceOption = tableModel.getDefaultReplaceOption();
		
		if (replaceOption == LeaveReplaceOption.LeaveAll) {
			// don't need to prompt
			return leaveDuplicateDataType(tableModel, replacementList, existingEntry, newBER);
			
		} else if (replaceOption == LeaveReplaceOption.ReplaceAll) {
			// don't need to prompt
			return replaceDuplicateDataType(tableModel, replacementList, existingEntry, newBER);
			
		} else {
			// prompt
			LeaveReplaceOption status = LeaveOrReplaceDialog.ShowDialog(BEEditor.getInstance(), 
					tableModel.getObjectTypeName(existingEntry.getUserObject()), 
					tableModel.getObjectName(existingEntry.getUserObject()));
			
			if (status == LeaveReplaceOption.Cancel) {
				return false;	// user quits
				
			} else if (status == LeaveReplaceOption.Leave) {
				// leave it
				return leaveDuplicateDataType(tableModel, replacementList, existingEntry, newBER);
				
			} else if (status == LeaveReplaceOption.LeaveAll) {
				// leave it - don't prompt again
				tableModel.setDefaultReplaceOption(status);	// save response
				return leaveDuplicateDataType(tableModel, replacementList, existingEntry, newBER);
				
			} else if (status == LeaveReplaceOption.Replace) {
				// replace it
				return replaceDuplicateDataType(tableModel, replacementList, existingEntry, newBER);
				
			} else if (status == LeaveReplaceOption.ReplaceAll) {
				// replace it - don't prompt again
				tableModel.setDefaultReplaceOption(status);	// save response
				return replaceDuplicateDataType(tableModel, replacementList, existingEntry, newBER);
			} 
		}
		
		return true;
	}

	private static boolean leaveDuplicateDataType(MdmiTableModel tableModel, List<MdmiBusinessElementReference> replacementList,
			TableEntry existingEntry, MdmiBusinessElementReference importedBER) {

		// don't need to do anything - there are no references to the BER to update
		return true;
	}

	private static boolean replaceDuplicateDataType(MdmiTableModel tableModel, List<MdmiBusinessElementReference> replacementList,
			TableEntry existingEntry, MdmiBusinessElementReference importedBER) {
		
		// replace the BER in the table
		existingEntry.setUserObject(importedBER);
		
		return true;
	}
}
