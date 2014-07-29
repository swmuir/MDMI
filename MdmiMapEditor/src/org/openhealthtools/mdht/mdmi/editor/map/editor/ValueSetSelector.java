package org.openhealthtools.mdht.mdmi.editor.map.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.openhealthtools.mdht.mdmi.Mdmi;
import org.openhealthtools.mdht.mdmi.MdmiResolver;
import org.openhealthtools.mdht.mdmi.MdmiValueSet;
import org.openhealthtools.mdht.mdmi.MdmiValueSet.Value;
import org.openhealthtools.mdht.mdmi.MdmiValueSetsHandler;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;

/** An editable combo box that displays value set names */
public class ValueSetSelector extends AdvancedSelectionField {

	private MdmiValueSetsHandler m_handler = null;
	
	public ValueSetSelector(GenericEditor parentEditor, MessageGroup group) {
		super(parentEditor);
		MdmiResolver resolver = Mdmi.INSTANCE.getResolver();
		if (resolver != null && group != null) {
			m_handler = resolver.getValueSetsHandler(group.getName());
		}

		// allow editing
		getComboBox().setEditable(true);
		loadComboBox();
	}

	@Override
	public void setReadOnly() {
		super.setReadOnly();
		getComboBox().setEditable(false);
	}
	
	@Override
	public Class<?> getDataClass() {
		return Field.class;
	}
	
	@Override
	protected Collection<? extends Object> getComboBoxData() {
		// Find all the value sets
		ArrayList<String> elements = new ArrayList<String>();
		
		if (m_handler != null) {
			// use value set name, since there's no "toString" method
			for (MdmiValueSet valueSet : m_handler.getAllValueSets()) {
				elements.add(valueSet.getName());
			}
		
			// Sort
			Collections.sort(elements);
		}
	
		List<Object> data = new ArrayList<Object>();
		data.addAll(elements);
		// make first item blank
		data.add(0, BLANK_ENTRY);
		return data;
	}

	@Override
	protected String toString(Object listObject) {
		return listObject.toString();
	}


	@Override
	public void setDisplayValue(Object value) {
		// value will be a string
		if ("".equals(value)) {
			value = BLANK_ENTRY;
		}
		super.setDisplayValue(value);
	}

	@Override
	protected String getToolTipText(Object listObject) {
		return null;
	}

	/** get the selected value set name */
	public String getValueSetName() {
		JTextField editField = (JTextField)getComboBox().getEditor().getEditorComponent();
		String value = editField.getText();
		return value.trim();
	}

	/** Open the selected item - it's not in the tree */
	@Override
	protected void openSelection() {
		String valueSetName = getValueSetName();
		if (!valueSetName.isEmpty()) {
			MdmiValueSet valueSet = m_handler.getValueSet(valueSetName);
			if (valueSet != null) {
				// show value set
				ViewValueSetDialog dlg = new ViewValueSetDialog((JFrame)getTopLevelAncestor(), valueSet);
				dlg.display(this);
			} else {
				JOptionPane.showMessageDialog(this, "The Value Set '" + valueSetName + "' does not exist ",
						valueSetName, JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
	
	/** Display the Value Set data in a table */
	private class ViewValueSetDialog extends BaseDialog {

		public ViewValueSetDialog(Frame owner, MdmiValueSet valueSet) {
			super(owner, BaseDialog.OK_BUTTON_OPTION);
			buildUI(valueSet);
			setTitle(valueSet.getName());
			pack(new Dimension(450, 400));
		}

		private void buildUI(MdmiValueSet valueSet) {
			// Create a table with two columns
			String [] colNames = new String[2];
			colNames[0] = "Code";
			colNames[1] = "Description";
			
			ArrayList<Value> values = valueSet.getValues();
			String[][] rowData = new String[values.size()][colNames.length];
			for (int i=0; i<values.size(); i++) {
				Value value = values.get(i);
				rowData[i][0] = value.getCode();
				rowData[i][1] = value.getDescription() == null ? "" : value.getDescription();
			}

			JTable table = new JTable(rowData, colNames);
			add(new JScrollPane(table), BorderLayout.CENTER);
			
			setDirty(true);	// enable OK button
		}

		@Override
		public boolean isDataValid() {
			return true;
		}
		
	}
}
