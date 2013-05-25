package org.openhealthtools.mdht.mdmi.editor.be_editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;

import org.openhealthtools.mdht.mdmi.editor.be_editor.tables.TableEntry;
import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.map.tools.Comparators;
import org.openhealthtools.mdht.mdmi.model.DTSPrimitive;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;

/** Select a datatype from the list */
public class DataTypeSelector extends JPanel implements ActionListener {
	public static final String BLANK_ENTRY = "     ";

	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.be_editor.Local");

	private JComboBox m_comboBox = new JComboBox();
	private JButton   m_refreshBtn = new JButton("Refresh");
	
	private Class<? extends MdmiDatatype> m_allowedClass = null;
	private List<MdmiDatatype> m_excludedItems = new ArrayList<MdmiDatatype>();

	public DataTypeSelector(Class<? extends MdmiDatatype> dataClass) {
		setLayout(new FlowLayout(FlowLayout.LEFT, Standards.LEFT_INSET, 0));

		URL imageURL = getClass().getResource(s_res.getString("DataTypeSelector.refreshIcon"));
		if (imageURL != null) {
			ImageIcon icon =  new ImageIcon(imageURL);
			m_refreshBtn.setText(null);
			m_refreshBtn.setIcon(icon);
		}
		
		add(m_comboBox);
		add(m_refreshBtn);
		
		m_allowedClass = dataClass;
		fillSelector();
	}

	@Override
	public void addNotify() {
		super.addNotify();
		m_refreshBtn.addActionListener(this);
		m_refreshBtn.setToolTipText(s_res.getString("DataTypeSelector.refreshToolTip"));

		m_comboBox.setRenderer(new ComboBoxRenderer());
	}
	
	@Override
	public void removeNotify() {
		m_refreshBtn.removeActionListener(this);
		m_refreshBtn.setToolTipText(null);

		m_comboBox.setRenderer(null);

		super.removeNotify();
	}
	
	/** Add an action listener to the combo box */
	public void addActionListener(ActionListener listener) {
		m_comboBox.addActionListener(listener);
	}
	
	/** Remove an action listener from the combo box */
	public void removeActionListener(ActionListener listener) {
		m_comboBox.removeActionListener(listener);
	}
	
	// add a datatype to the list of ones to exclude from the list
	public void excludeDatatype(MdmiDatatype datatype) {
		m_excludedItems.add(datatype);
	}
	
	// Fill with all datatypes that meet the provided class
	public void fillSelector() {
		BEEditor beEditor = (BEEditor)SystemContext.getApplicationFrame();
		DataTypeDisplayPanel dataTypeDisplayPanel = beEditor.getDataTypeDisplayPanel();

		ArrayList<MdmiDatatype> dataTypes = new ArrayList<MdmiDatatype>();
		// add primitives
		if (m_allowedClass.isAssignableFrom(DTSPrimitive.class)) {
			for (DTSPrimitive primitive : DTSPrimitive.ALL_PRIMITIVES) {
				if (!m_excludedItems.contains(primitive)) {
					dataTypes.add(primitive);
				}
			}
		}
		
		// search locally
		List <TableEntry> allDatatypes = dataTypeDisplayPanel.getTableModel().getAllEntries();
		for (TableEntry entry : allDatatypes) {
			Object userObject = entry.getUserObject();

			// check type
			if (m_allowedClass.isAssignableFrom(userObject.getClass())) {
				// skip ones that don't have a typeName - they are a result of an un-accepted edit
				if (((MdmiDatatype)userObject).getTypeName() == null) {
					continue;
				}
				if (!m_excludedItems.contains(userObject)) {
					dataTypes.add((MdmiDatatype) userObject);
				}
			}
		}
		

		// Sort by name
		Collections.sort(dataTypes, new Comparators.DataTypeComparator());

		// add to selector
		m_comboBox.removeAllItems();
		// make first one blank
		m_comboBox.addItem(BLANK_ENTRY);
		
		for (MdmiDatatype dataType : dataTypes) {
			m_comboBox.addItem(dataType);
		}
	}
	
	/** Set the  data type */
	public  void setDataType(MdmiDatatype datatype) {
		if (datatype == null) {
			m_comboBox.setSelectedIndex(0);
		} else {
			m_comboBox.setSelectedItem(datatype);
		}
	}

	
	/** Get the selected data type */
	public MdmiDatatype getDataType() {
		Object selectedItem = m_comboBox.getSelectedItem();
		if (selectedItem instanceof MdmiDatatype) {
			return (MdmiDatatype)selectedItem;
		}
		return null;
	}
	


	/** Renderer for the ComboBox */
	protected class ComboBoxRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			Color foreground = isSelected ? list.getSelectionForeground() : list.getForeground();
			Color background = isSelected ? list.getSelectionBackground() : list.getBackground();
			
			String displayValue = "";
			String toolTip = null;

			if (value == null) {
				displayValue = "";
			} else if (value instanceof DTSPrimitive) {
				displayValue = ((DTSPrimitive)value).getDescription();
			} else if (value instanceof MdmiDatatype) {
				displayValue = ((MdmiDatatype)value).getName();
			} else {
				displayValue = value.toString();
			}
	
			
			Component c = super.getListCellRendererComponent(list, displayValue, index, isSelected,
					cellHasFocus);
			c.setBackground(background);
			c.setForeground(foreground);
			setToolTipText(toolTip);
			return c;
		}

	}
	
	//////////////////////////////////////
	// ActionListener
	//////////////////////////////////////
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == m_refreshBtn) {
			fillSelector();
		}
		
	}

}
