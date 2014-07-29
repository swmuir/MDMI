package org.openhealthtools.mdht.mdmi.editor.map.editor;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;


/** A component to pick Field names for value sets.
 * A property change event will be fired when the selection changes. */
public class ValueSetData extends JPanel implements IEditorField, ActionListener {
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.editor.Local");

	//  This is the value set
	public static final String ENUM_VALUE_SET         = "EnumValueSet";
	
	// These use the Field
	public static final String ENUM_VALUE_FIELD       = "EnumValueField";
	public static final String ENUM_VALUE_SET_FIELD   = "EnumValueSetField";
	public static final String ENUM_VALUE_DESCR_FIELD = "EnumValueDescrField";
	

	// simply provide a String editor, and 3 field combo boxes in this order
	private ValueSetSelector m_valueSetName;
	private String[] m_valueFieldNames = {
			ENUM_VALUE_FIELD, 
			ENUM_VALUE_SET_FIELD, 
			ENUM_VALUE_DESCR_FIELD
			};
	private FieldNameSelector[] m_allFields;
	
	private GenericEditor m_parentEditor = null;
	private MessageGroup m_group = null;
	private MdmiDatatype m_datatype = null;
	private JComboBox<?> m_datatypeCombo = null;

	public ValueSetData(GenericEditor parentEditor, MessageGroup group, MdmiDatatype datatype) {
		m_parentEditor = parentEditor;
		m_group = group;
		m_datatype = datatype;
		
		// save the data type editor field, so we can listen for changes
		MdmiDatatypeField datatypeField = findDatatypeField();
		if (datatypeField != null) {
			m_datatypeCombo = datatypeField.getComboBox();
		}
		
		buildUI();
	}
	
	/** Find the field that supports editing the data type */
	private MdmiDatatypeField findDatatypeField() {

		for (DataEntryFieldInfo fieldInfo : m_parentEditor.getDataEntryFieldList()) {
			if (fieldInfo.getEditComponent() instanceof MdmiDatatypeField) {
				return (MdmiDatatypeField)fieldInfo.getEditComponent();
			}
		}
		return null;
	}
	
	//
	//   Value Set:         [________]
	//
	//   Value Field:       [_______v]
	//   Value Set Field:   [_______v]
	//   Value Description: [_______v]
	private void buildUI() {
		// create the data
		m_valueSetName = new ValueSetSelector(m_parentEditor, m_group);
		m_allFields = new FieldNameSelector[m_valueFieldNames.length];
		for (int i=0; i<m_allFields.length; i++) {
			m_allFields[i] = new FieldNameSelector(m_parentEditor, m_datatype);
		}
		
		// disable value set name if no fields
		if (m_allFields[0].getComboBox().getItemCount() <= 1) {
			m_valueSetName.setReadOnly();
		}

		// layout the components
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.insets = Standards.getInsets();

		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 0;
		gbc.weighty = 0;	// all the same
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		String fieldName = ClassUtil.beautifyName(ENUM_VALUE_SET);
		add(new JLabel(fieldName + ":"), gbc);
		
		gbc.gridx++;
		
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(m_valueSetName, gbc);

		gbc.gridx = 0;
		gbc.gridy++; 
		
		// adjust padding
		gbc.insets.bottom = 0;
		
		for (int i=0; i<m_allFields.length; i++) {

			gbc.weightx = 0;
			gbc.fill = GridBagConstraints.NONE;
			fieldName = ClassUtil.beautifyName(m_valueFieldNames[i]);
			add(new JLabel(fieldName + ":"), gbc);
			
			gbc.gridx++;
			
			gbc.weightx = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			add(m_allFields[i], gbc);

			gbc.gridx = 0;
			gbc.gridy++;
		}
		
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Value Set Data"));
	}
	
	@Override
	public void highlightText(String text, Color highlightColor) {
		// Name
		if (text.equals(m_valueSetName.getValue())) {
			m_valueSetName.highlightText(text, highlightColor);
		}
		// Fields
		for (FieldNameSelector valueField : m_allFields) {
			if (text.equals(valueField.getValue())) {
				valueField.highlightText(text, highlightColor);
			}
		}
	}
	
	@Override
	public void addNotify() {
		// add listener
		if (m_datatypeCombo != null) {
			m_datatypeCombo.addActionListener(this);
		}
		super.addNotify();
	}
	
	@Override
	public void removeNotify() {
		super.removeNotify();
		// cleanup listener
		if (m_datatypeCombo != null) {
			m_datatypeCombo.removeActionListener(this);
		}
	}

	/** Get the values as an array of 4 strings */
	@Override
	public Object getValue() throws DataFormatException {
		Collection<String> valueList = new ArrayList<String>();
		
		String value = (String) m_valueSetName.getValue();
		valueList.add( ensureNonNull(value) );
		
		for (FieldNameSelector valueField : m_allFields) {
			value = (String) valueField.getValue();
			valueList.add( ensureNonNull(value) );
		}
		return valueList;
	}

	/** Set the values as an array of 4 strings */
	@Override
	public void setDisplayValue(Object value) throws DataFormatException {
		if (value instanceof Collection<?>) {
			Collection<?> valueList = (Collection<?>)value;
			int i=0;
			for (Object v : valueList) {
				if (i == 0) {
					m_valueSetName.setDisplayValue(v);
				} else {
					// i=1,2,3
					m_allFields[i-1].setDisplayValue(v);
				}
				i++;
			}
		}
	}


	public void highlightFieldWithError(String fieldName) {

		if (fieldName.equalsIgnoreCase(ENUM_VALUE_SET)) {
			DataEntryFieldInfo.createErrorBorder(m_valueSetName);
			return;
		}
		
		for (int i=0; i<m_valueFieldNames.length; i++) {
			if (fieldName.equalsIgnoreCase(m_valueFieldNames[i])) {
				DataEntryFieldInfo.createErrorBorder(m_allFields[i]);
				break;
			}
		}
	}

	public void clearFieldsWithError() {
		DataEntryFieldInfo.clearErrorBorder(m_valueSetName);
		for (int i=0; i<m_allFields.length; i++) {
			DataEntryFieldInfo.clearErrorBorder(m_allFields[i]);
		}
	}

	@Override
	public void setReadOnly() {
		m_valueSetName.setReadOnly();
		for (int i=0; i<m_allFields.length; i++) {
			m_allFields[i].setReadOnly();
		}
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	public DataEntryFieldInfo getDataEntryFieldInfo() {
		ValueSetFieldInfo fieldInfo = new ValueSetFieldInfo();
		fieldInfo.setEditComponent(this);
		return fieldInfo;
	}
	
	public static String ensureNonNull(String str) {
		if (str == null) {
			str = "";
		}
		return str;
	}
	
	public class ValueSetFieldInfo extends DataEntryFieldInfo {
		public ValueSetFieldInfo() {
			// we don't care about the Method attributes, since we'll use our own gets and sets
			super("ValueSet", null, null, null);
		}

		@Override
		public Object getValueFromModel(Object model)
				throws IllegalArgumentException, IllegalAccessException,
				InvocationTargetException {

			Collection<String> valueList = new ArrayList<String>();
			if (model instanceof SemanticElement) {
				SemanticElement se = (SemanticElement)model;
				valueList.add( ensureNonNull(se.getEnumValueSet()) );
				valueList.add( ensureNonNull(se.getEnumValueField()) );
				valueList.add( ensureNonNull(se.getEnumValueSetField()) );
				valueList.add( ensureNonNull(se.getEnumValueDescrField()) );
			} else if (model instanceof MdmiBusinessElementReference) {
				MdmiBusinessElementReference ber = (MdmiBusinessElementReference)model;
				valueList.add( ensureNonNull(ber.getEnumValueSet()) );
				valueList.add( ensureNonNull(ber.getEnumValueField()) );
				valueList.add( ensureNonNull(ber.getEnumValueSetField()) );
				valueList.add( ensureNonNull(ber.getEnumValueDescrField()) );
			}
			return valueList;
		}

		/** Override setValueInModel method because there is no single set method
		 */
		@Override
		public void setValueInModel(Object model, Object value)
				throws IllegalArgumentException, IllegalAccessException,
				InvocationTargetException {
			 
			// clear all error highlights
			clearFieldsWithError();

			if (value instanceof Collection<?>) {
				Collection<?> valueList = (Collection<?>)value;
				int i=0;
				for (Object v : valueList) {
					String vs = (v == null) ? "" : v.toString();
					if (model instanceof SemanticElement) {
						SemanticElement se = (SemanticElement)model;
						if (i == 0)
							se.setEnumValueSet(vs);
						else if (i == 1)
							se.setEnumValueField(vs);
						else if (i == 2)
							se.setEnumValueSetField(vs);
						else if (i == 3)
							se.setEnumValueDescrField(vs);
					} else if (model instanceof MdmiBusinessElementReference) {
						MdmiBusinessElementReference ber = (MdmiBusinessElementReference)model;
						if (i == 0)
							ber.setEnumValueSet(vs);
						else if (i == 1)
							ber.setEnumValueField(vs);
						else if (i == 2)
							ber.setEnumValueSetField(vs);
						else if (i == 3)
							ber.setEnumValueDescrField(vs);
					}
					i++;
				}
				return;
			}
			
			super.setValueInModel(model, value);
		}
		
		
	}

	///////////////////////////////////////////
	// Action Listener
	/////////////////////////////////////////////
	@Override
	public void actionPerformed(ActionEvent e) {
		// Check if the Data Type has changed - if so, we need to rebuild 
		if (e.getSource() == m_datatypeCombo) {
			Object selected = m_datatypeCombo.getSelectedItem();
			if (selected instanceof MdmiDatatype && selected != m_datatype) {
				// data type changed - we need to reload
				m_datatype = (MdmiDatatype)selected;
				
				removeAll();
				buildUI();
			}
		}
	}

}
