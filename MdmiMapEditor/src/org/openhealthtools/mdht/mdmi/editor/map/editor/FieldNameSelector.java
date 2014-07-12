package org.openhealthtools.mdht.mdmi.editor.map.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openhealthtools.mdht.mdmi.editor.map.tools.Comparators;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SyntaxNodeNode;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;

/** A combo box that displays field names */
public class FieldNameSelector extends AdvancedSelectionField {

	private MdmiDatatype m_datatype;
	
	public FieldNameSelector(GenericEditor parentEditor, MdmiDatatype datatype) {
		super(parentEditor);
		m_datatype = datatype;
		loadComboBox();
	}

	@Override
	public Class<?> getDataClass() {
		return Field.class;
	}
	
	@Override
	protected Collection<? extends Object> getComboBoxData() {
		// Find all the allowed field names, based on the parent's type
		ArrayList<Field> elements = new ArrayList<Field>();

		if (m_datatype instanceof DTComplex) {
			for (Field field : ((DTComplex)m_datatype).getFields()) {
				// skip fields without valid names
				if (field.getName() != null && field.getName().length() > 0) {
					elements.add(field);
				}
			}

			// Sort by name
			Collections.sort(elements, new Comparators.FieldComparator());
		}
	
		List<Object> data = new ArrayList<Object>();
		data.addAll(elements);
		// make first item blank
		data.add(0, BLANK_ENTRY);
		return data;
	}

	@Override
	protected String toString(Object listObject) {
		if (listObject instanceof Field) {
			Field field = (Field)listObject;
			if (field.getDatatype() != null) {
				// name : Type
				return SyntaxNodeNode.showNameAndType(field.getName(), field.getDatatype().getTypeName());
			}
			return field.getName();
		}
		return listObject.toString();
	}

	@Override
	protected String getToolTipText(Object listObject) {
		if (listObject instanceof Field) {
			Field element = (Field)listObject;
			return element.getDescription();
		}
		return null;
	}


	@Override
	public Object getValue() {
		// return name only
		Object value = super.getValue();
		if (value instanceof Field) {
			value = ((Field)value).getName();
		}
		return value;
	}

	@Override
	public void setDisplayValue(Object value) {
		// value will be a string, but our list contains Fields
		if ("".equals(value)) {
			value = BLANK_ENTRY;
		} else if (value instanceof String) {
			// find Field with this name
			for (int i = 0; i < getComboBox().getItemCount(); i++) {
				Object item = getComboBox().getItemAt(i);
				if (item instanceof Field && value.equals(((Field)item).getName())) {
					value = item;
					break;
				}
			}
		}
		super.setDisplayValue(value);
	}

}
