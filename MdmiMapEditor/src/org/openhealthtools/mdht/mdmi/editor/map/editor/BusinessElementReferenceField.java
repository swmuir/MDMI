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
import java.util.List;

import org.openhealthtools.mdht.mdmi.editor.map.tools.Comparators;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;

/** An IEditorField that shows MdmiBusinessElementReference values in a ComboBox */
public class BusinessElementReferenceField extends AdvancedSelectionField {

	public BusinessElementReferenceField(GenericEditor parentEditor) {
		super(parentEditor);		
	}

	@Override
	protected Collection<? extends Object> getComboBoxData() {
		// Find all the BusinessElementReferences
		ArrayList<MdmiBusinessElementReference> elements = new ArrayList<MdmiBusinessElementReference>();
		Collection<MdmiBusinessElementReference> refs =
			getParentEditor().getMessageGroup().getDomainDictionary().getBusinessElements();
		for (MdmiBusinessElementReference element : refs) {
			if (element.getName() != null && element.getName().length() > 0) {
				elements.add(element);
			}
		}

		// sort by name
		Collections.sort(elements, new Comparators.BusinessElementReferenceComparator());
		
		List<Object> data = new ArrayList<Object>();
		data.addAll(elements);
		// make first item blank
		data.add(0, BLANK_ENTRY);
		return data;
	}


	@Override
	public void addNotify() {
		super.addNotify();
		getViewButton().setToolTipText(s_res.getString("BusinessElementReferenceField.viewToolTip"));
	}

	@Override
	public void removeNotify() {
		getViewButton().setToolTipText(null);
		super.removeNotify();
	}


	@Override
	public Class<?> getDataClass() {
		return MdmiBusinessElementReference.class;
	}
	
	/** Convert an object in the list to a string */
	@Override
	protected String toString(Object listObject) {
		if (listObject instanceof MdmiBusinessElementReference) {
			MdmiBusinessElementReference element = (MdmiBusinessElementReference)listObject;
			return element.getName();
		}
		return listObject.toString();
	}
	
	/** Get a tooltip for an item in the list */
	@Override
	protected String getToolTipText(Object listObject) {
		if (listObject instanceof MdmiBusinessElementReference) {
			MdmiBusinessElementReference element = (MdmiBusinessElementReference)listObject;
			return element.getDescription();
		}
		return null;
	}
	

}
