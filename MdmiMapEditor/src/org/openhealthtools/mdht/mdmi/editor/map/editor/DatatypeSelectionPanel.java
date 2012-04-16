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
package org.openhealthtools.mdht.mdmi.editor.map.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;

import org.openhealthtools.mdht.mdmi.editor.common.components.CheckBoxListPanel;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;

/** A collection of MdmiDatatypes displayed as check boxes in a list.
 * Any one, or more of the datatypes can be selected.
 * 
 * Changes to the selections are noted via a property change event using the
 * NUM_SELECTIONS_PROPERTY property name.
 * @author Conway
 *
 */
public class DatatypeSelectionPanel extends CheckBoxListPanel {

	public DatatypeSelectionPanel(Collection <MdmiDatatype> datatypes) {
		super();
		fillListModel(datatypes);
	}
	
	/** Fill the list model from this collection of datatypes */
	public void fillListModel(Collection <MdmiDatatype> datatypes) {
		DefaultListModel listModel = getModel();
		listModel.removeAllElements();
		
      for (MdmiDatatype MdmiDatatype : datatypes) {
         addCheckBox(new DataTypeCheckBox(MdmiDatatype));
      }
	}

	/** Find the checkbox that contains this datatype */
	public DataTypeCheckBox findCheckBox(MdmiDatatype datatype) {
		for (JCheckBox checkbox : getCheckBoxes()) {
			if (checkbox instanceof DataTypeCheckBox &&
					((DataTypeCheckBox)checkbox).datatype == datatype) {
				return (DataTypeCheckBox)checkbox;
			}
		}
		return null;
	}

	/** Get all selected items in the list */
	public List<MdmiDatatype> getSelectedTypes() {
		ArrayList<MdmiDatatype> types = new ArrayList<MdmiDatatype>();
		
		for (JCheckBox checkbox : getCheckBoxes()) {
      	if (checkbox.isSelected()) {
            types.add(((DataTypeCheckBox)checkbox).datatype);
         }
      }
      
		return types;
	}
	
	/** Select all items specified */
	public void setSelectedTypes(Collection<MdmiDatatype> types) {
		selectAll(false);

		// select items in list
		for (MdmiDatatype datatype : types) {
			JCheckBox checkbox = findCheckBox(datatype);
			checkbox.setSelected(true);
		}
	}

	////////////////////////////////////////////////////////////////

	/** Wrapper for JCheckBox created from a datatype */
	public static class DataTypeCheckBox extends JCheckBox {
		public MdmiDatatype datatype;
		public DataTypeCheckBox(MdmiDatatype datatype) {
			super.setText(datatype.getTypeName());
			this.datatype = datatype;
		}
	}

}
