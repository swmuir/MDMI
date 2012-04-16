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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openhealthtools.mdht.mdmi.editor.map.tools.Comparators;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;

/** An IEditorField that shows MdmiDatatype values in a ComboBox */
public class MdmiDatatypeField extends AdvancedSelectionField {
	Class<? extends MdmiDatatype> m_dataClass;
	
	public MdmiDatatypeField(GenericEditor parentEditor, Class<? extends MdmiDatatype> clazz) {
		super();
		m_dataClass = clazz;	// we need to set this before building the UI
		buildUI(parentEditor);
	}

	/** Create a ComboBox and fill it with DataTypes from the MessageGroup
	 * @param parentEditor
	 */
	@Override
	protected Collection<? extends Object> getComboBoxData() {
		
		// Get all DataTypes from the message group
		List<MdmiDatatype> dataTypes = getAllDatatypes(getParentEditor().getMessageGroup(), m_dataClass);
		
		List<Object> data = new ArrayList<Object>();
		data.addAll(dataTypes);
		// make first item blank
		data.add(0, BLANK_ENTRY);
		return data;
		
	}
	
	/** Get a list of all dataypes (of a particular type) from the message group,
	 * sorted by name.
	 * @param group
	 * @param dataClass	allow filtering of a specific class
	 * @return
	 */
	public static List<MdmiDatatype> getAllDatatypes(MessageGroup group, Class<? extends MdmiDatatype> dataClass) {

		ArrayList<MdmiDatatype> dataTypes = new ArrayList<MdmiDatatype>();
		for (MdmiDatatype dataType : group.getDatatypes()) {
			// skip nulls
			if (dataType == null) {
				continue;
			}
			// check type
			if (!dataClass.isAssignableFrom(dataType.getClass())) {
				continue;
			}
			// skip ones that don't have a typeName - they are a result of an unaccepted edit
			if (dataType.getTypeName() == null) {
				continue;
			}
			dataTypes.add(dataType);
		}

		// Sort by name
		Collections.sort(dataTypes, getDatatypeComparator());
		
		return dataTypes;
	}

	/** Get a comparator to sort MdmiDatatypes by TypeName */
	public static Comparator<MdmiDatatype> getDatatypeComparator() {
		return new Comparators.DataTypeComparator();
	}
	

	@Override
	public void addNotify() {
		super.addNotify();
		getViewButton().setToolTipText(s_res.getString("MdmiDatatypeField.viewToolTip"));
	}

	@Override
	public void removeNotify() {
		getViewButton().setToolTipText(null);
		super.removeNotify();
	}
	

	@Override
	public Class<?> getDataClass() {
		return MdmiDatatype.class;
	}
	
	/** Convert an object in the list to a string */
	@Override
	protected String toString(Object listObject) {
		if (listObject instanceof MdmiDatatype) {
			MdmiDatatype datatype = (MdmiDatatype)listObject;
			return datatype.getTypeName();
		}
		return listObject.toString();
	}
	
	/** Get a tooltip for an item in the list */
	@Override
	protected String getToolTipText(Object listObject) {
		if (listObject instanceof MdmiDatatype) {
			MdmiDatatype element = (MdmiDatatype)listObject;
			return element.getDescription();
		}
		return null;
	}
}
