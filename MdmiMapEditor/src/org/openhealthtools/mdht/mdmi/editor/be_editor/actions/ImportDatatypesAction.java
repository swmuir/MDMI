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
import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;

import org.openhealthtools.mdht.mdmi.editor.be_editor.BEEditor;
import org.openhealthtools.mdht.mdmi.editor.be_editor.DataTypeDisplayPanel;
import org.openhealthtools.mdht.mdmi.editor.common.actions.AbstractMenuAction;
import org.openhealthtools.mdht.mdmi.editor.map.tools.Comparators;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ModelIOUtilities;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.DTSPrimitive;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;

public class ImportDatatypesAction extends AbstractMenuAction {
	private static final long serialVersionUID = -1;

	@Override
	public void execute(ActionEvent actionEvent) {
		// prompt for input
		BEEditor editor = BEEditor.getInstance();
		final DataTypeDisplayPanel datatypePanel = editor.getDataTypeDisplayPanel();
		
		ArrayList<MessageGroup> groups = ModelIOUtilities.promptAndReadModel();
		if (groups != null) {
			List<MdmiDatatype> dataTypes = new ArrayList<MdmiDatatype>();
			for (MessageGroup group : groups) {
				dataTypes.addAll(group.getDatatypes());
			}
			// Sort by name
			Collections.sort(dataTypes, new Comparators.DataTypeComparator());
			
			
			for (MdmiDatatype datatype : dataTypes) {
				// add all non-primitive data types
				if (datatype instanceof DTSPrimitive) {
					continue;
				}
				// we only support complex types right now
				if (datatype instanceof DTComplex) {
					datatype.setOwner(null);	// set the owner to null so it looks new
					datatypePanel.getTableModel().addEntry(datatype);
				}
			}
		}

		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				datatypePanel.getTable().revalidate();
			}
		});
        
	}
	
}
