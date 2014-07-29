package org.openhealthtools.mdht.mdmi.editor.map.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import org.openhealthtools.mdht.mdmi.MdmiValueSet;
import org.openhealthtools.mdht.mdmi.MdmiValueSet.Value;
import org.openhealthtools.mdht.mdmi.MdmiValueSetMap;
import org.openhealthtools.mdht.mdmi.MdmiValueSetMap.Mapping;
import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.tables.TableSorter;

/** A Sub-panel that contains a table that shows the mappings in an MdmiValueSetMap. 
 * An "Import from CSV" button is provided. The calling class must add a listener to the button.
 * A property change event (SOURCE_DESCRIPTION/TARGET_CODE/TARGET_DESCRIPTION) will be sent to 
 * listeners if the data is changed.
 * */
public class ValueSetMapPanel extends JPanel {
	
	public static final String SOURCE_CODE = "Source Code";
	public static final String SOURCE_DESCRIPTION = "Source Description";
	public static final String TARGET_CODE = "Target Code";
	public static final String TARGET_DESCRIPTION = "Target Description";

	static String s_lastFileName = null;
	
	/** Column names */
	private static final String[] s_columnLabels = {
		SOURCE_CODE, 
		SOURCE_DESCRIPTION, 
		TARGET_CODE, 
		TARGET_DESCRIPTION
		};
	
	// column values
	private static final int SOURCE_CODE_COL = 0;
	private static final int SOURCE_DESCRIPTION_COL = 1;
	private static final int TARGET_CODE_COL = 2;
	private static final int TARGET_DESCRIPTION_COL = 3;
	

	//  Mapping from  A to B          [CSV]  
	//  -----------------------------------  
	// | Code | Descr | To Code | To Descr | 
	// |------|-------|---------|----------| 
	// |______|_______|_________|__________| 
	// |______|_______|_________|__________| 
	// |______|_______|_________|__________| 
	//                                      

	private MappingTableModel m_tableModel = new MappingTableModel();
	private TableSorter m_sorter = new TableSorter( m_tableModel );
	private JTable m_table= new JTable();

	private JButton m_openCSVFile = new JButton("Import from CSV...");

	/** Create a panel from a map */
	public ValueSetMapPanel(MdmiValueSetMap map) {
		setLayout(new BorderLayout());

		// configure the model
		m_tableModel.setMdmiValueSetMap(map);

		// Reset the sorter
		m_sorter.reallocateIndexes();
		m_table.setModel(m_sorter);
		m_sorter.addMouseListenerToHeaderInTable(m_table);
		
		// don't allow sorting on "Target" field
		Vector<Integer> excludedCols = new Vector<Integer>();
		excludedCols.add(TARGET_CODE_COL);
		m_sorter.setExcludeColumns(excludedCols);
		
		// create a combo-box editor for the "Target Mapping" column
		populateTargetSelector();
		
		// create the target cell renderer
		ColorCellRenderer renderer = new ColorCellRenderer();

		// set renderer
		for (int c=0; c<m_table.getColumnCount(); c++) {
			TableColumn column = m_table.getColumnModel().getColumn(c);
			if (c == TARGET_CODE_COL || c == TARGET_DESCRIPTION_COL) {
				column.setCellRenderer(renderer);
			}
		}

		buildUI();
	}
	
	/** get the ValueSetMap associated with this item */
	public MdmiValueSetMap getValueSetMap() {
		return  m_tableModel.valueSetMap;
	}

	
	/** Prompt the user, and load mapping from a CSV file. The file will be in the form:
	 *        From_Code, From_Description, To_Code, To_Description
	 * @throws IOException 
	 */
	public boolean importFromCSV() throws IOException {
		
		MdmiValueSetMap valueSetMap = getValueSetMap();
		MdmiValueSet sourceSet = valueSetMap.getSourceSet();
		MdmiValueSet targetSet = valueSetMap.getTargetSet();

		// create a file chooser
		JFileChooser chooser = CSVFileReader.getCSVFileChooser(s_lastFileName);
		chooser.setDialogTitle("Open a CSV File with the mapping data (From Code, From Descr, To Code, To Descr)");

		// prompt for a CSV file
		int opt = chooser.showOpenDialog(this);
		if (opt == JFileChooser.APPROVE_OPTION) {
			File csvFile = chooser.getSelectedFile();
			s_lastFileName = csvFile.getAbsolutePath();
		
			CSVFileReader reader = new CSVFileReader(csvFile);

			// Read File Line By Line
			List<String> stringList = null;

			int lineNo = 0;
			// Fields
			String fromCode = null;
			String fromDescr = null;
			String toCode = null;
			String toDescr = null;

			int column = 0;
			while ((stringList = reader.getNextLine()) != null) {
				lineNo++;
				// skip empty lines
				if (CSVFileReader.isEmptyList(stringList)) {
					continue;
				}

				// read each field 
				column = 0;
				fromCode = CSVFileReader.getString(stringList, column++);
				fromDescr = CSVFileReader.getString(stringList, column++);
				toCode = CSVFileReader.getString(stringList, column++);
				toDescr = CSVFileReader.getString(stringList, column++);

				// must have a fromCode and a toCode (the rest are optional)
				if (fromCode.isEmpty() || toCode.isEmpty()) {
					StringBuilder message = new StringBuilder();
					message.append("Line ").append(lineNo).append(" is missing a ");
					if (fromCode.isEmpty()) {
						message.append("From Code");
						if (toCode.isEmpty()) {
							message.append(" and a ");
						}
					}
					if (toCode.isEmpty()) {
						message.append("To Code");
					}
					JOptionPane.showMessageDialog(this,
							message, "Invalid Format", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				
				// 1. Lookup From Code
				Value fromValue = sourceSet.getValue(fromCode);
				if (fromValue == null) {
					// add it
					sourceSet.addValue(fromCode, fromDescr);
					fromValue = sourceSet.getValue(fromCode);
				} else if (fromValue.getDescription() == null || fromValue.getDescription().isEmpty()){
					// update description
					fromValue.setDescription(fromDescr);
				}
				
				// 2. Lookup To Code
				Value toValue = targetSet.getValue(toCode);
				if (toValue == null) {
					// add it
					targetSet.addValue(toCode, toDescr);
					toValue = targetSet.getValue(toCode);
				} else if (toValue.getDescription() == null || toValue.getDescription().isEmpty()){
					// update description
					toValue.setDescription(toDescr);
				}
				
				// 3. Add mapping
				valueSetMap.addMapping(fromCode, toCode);

			}

			// reload the table
			resetTable();
			return true;
		}
		return false;
	}
	
	/** reload the table when the value set changes */
	public void resetTable() {
		m_tableModel.rowCount = m_tableModel.valueSetMap.getSourceSet().getValues().size();
		m_sorter.reallocateIndexes();

		// update the target selector too
		populateTargetSelector();
		
		m_table.revalidate();
	}

	/** (Re)populate the combo box that selects the target */
	private void populateTargetSelector() {
		MdmiValueSet targetSet = getValueSetMap().getTargetSet();
		// create a combo-box editor for the "Target Mapping" column
		ValueSetCodeSelector selector = new ValueSetCodeSelector(targetSet);

		// reset combo box for "Target" selection
		for (int c=0; c<m_table.getColumnCount(); c++) {
			TableColumn column = m_table.getColumnModel().getColumn(c);
			if (c == TARGET_CODE_COL) {
				column.setCellEditor(selector);
			}
		}
	}

	private void buildUI() {
		JPanel north = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = Standards.getInsets();
		gbc.gridx = 0;
		gbc.gridy = 0;

		String text = "<html>Mapping From <b>" + m_tableModel.valueSetMap.getSourceSet().getName()
				+ "</b> to <b>" + m_tableModel.valueSetMap.getTargetSet().getName() + "</b></html>";
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.weightx = 1;	// give label all the weight
		north.add(new JLabel(text), gbc);
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.weightx = 0;	// give label all the weight
		north.add(getImportButton(), gbc);

		add(north, BorderLayout.NORTH);

		m_table.setColumnSelectionAllowed(true);	// allow single item selection
		m_table.setRowHeight(18);	// a bit bigger
		
		add(new JScrollPane(m_table), BorderLayout.CENTER);
	}
	
	/** Access the "Open" button */
	public JButton getImportButton() {
		return m_openCSVFile;
	}

	/** Add listener to Open button */
	public void addOpenBtnListener(ActionListener listener) {
		getImportButton().addActionListener(listener);
	}
	/** Remove listener on Open button */
	public void removeOpenBtnListener(ActionListener listener) {
		getImportButton().removeActionListener(listener);
	}
	

	////////////////////////////////////
	// Table Model: 
	//  | Source Code | Source Description || Target Code | Target Description |
	////////////////////////////////////////////////////////////////////////////


	private class MappingTableModel extends AbstractTableModel {
		private MdmiValueSetMap valueSetMap;
		private int rowCount = 0;

		public void setMdmiValueSetMap(MdmiValueSetMap map) {
			valueSetMap = map;
			rowCount = valueSetMap.getSourceSet().getValues().size();
		}
		
		@Override
		public int getRowCount() {
			return rowCount;
		}

		@Override
		public int getColumnCount() {
			return s_columnLabels.length;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return s_columnLabels[columnIndex];
		}


		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			boolean editable = false;
			// only description and target columns are editable
			if (columnIndex == SOURCE_DESCRIPTION_COL || columnIndex == TARGET_CODE_COL || columnIndex == TARGET_CODE_COL) {
				editable = true;
			}
			return editable;
		}
		

		/** Find the mapping */
		private Mapping findMapping(String sourceCode) {
			Mapping found = null;
			for (Mapping mapping : valueSetMap.getMappings()) {
				// find the mapping for this source
				if (mapping.getSource().getCode() == sourceCode) {
					found = mapping;
					break;
				}
			}
			return found;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (valueSetMap == null)
				return "";
			
			Value srcValue = valueSetMap.getSourceSet().getValues().get(rowIndex);
			
			Object value = null;
			
			if (columnIndex == SOURCE_CODE_COL) {
				value = srcValue.getCode();
			} else if (columnIndex == SOURCE_DESCRIPTION_COL) {
				value = srcValue.getDescription();
				
			} else if (columnIndex == TARGET_CODE_COL) {
				value = "";
				// find the mapping for the source
				String srcCode = srcValue.getCode();
				Mapping mapping = findMapping(srcCode);
				if (mapping != null) {
					value = mapping.getTarget().getCode();
				}
			} else if (columnIndex == TARGET_DESCRIPTION_COL) {
				value = "";
				// find the mapping for the source
				String srcCode = srcValue.getCode();
				Mapping mapping = findMapping(srcCode);
				if (mapping != null) {
					value = mapping.getTarget().getDescription();
				}
			}
			
			return value;
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (valueSetMap == null)
				return;
			
			Value srcValue = valueSetMap.getSourceSet().getValues().get(rowIndex);
			
			String strValue = "";
			if (value instanceof JComboBox<?>) {
				value = ((JComboBox<?>)value).getSelectedItem();
				if (value instanceof ValueSetCodeSelectorItem) {
					// just use code part
					value = ((ValueSetCodeSelectorItem)value).code;
				}
			}
			
			if (value != null) {
				strValue = value.toString();
			}
			
			if (columnIndex == SOURCE_CODE_COL) {
				srcValue.setCode(strValue);
				
			} else if (columnIndex == SOURCE_DESCRIPTION_COL) {
				srcValue.setDescription(strValue);
			    firePropertyChange( SOURCE_DESCRIPTION, "", strValue );

			} else if (columnIndex == TARGET_CODE_COL) {
				String srcCode = srcValue.getCode();
				String targetCode = strValue;
				// find the mapping for the source
				Mapping mapping = findMapping(srcCode);
				if (mapping != null) {
					// set target based on the code
					Value targetValue = valueSetMap.getTargetSet().getValue(targetCode);
					if (targetValue == null) {
						// remove mapping
						valueSetMap.removeMapping(mapping);
					} else {
						mapping.setTarget(targetValue);
					}
				} else if (!targetCode.isEmpty()) {
					// create new mapping
					valueSetMap.addMapping(srcCode, targetCode);
				}
				
			    firePropertyChange( TARGET_CODE, "", strValue );
				// changing the target code will also impact the target description
				repaint();
				
			} else if (columnIndex == TARGET_DESCRIPTION_COL) {
				String srcCode = srcValue.getCode();
				// find the mapping for the source
				Mapping mapping = findMapping(srcCode);
				if (mapping != null) {
					// set description
					mapping.getTarget().setDescription(strValue);
				    firePropertyChange( TARGET_DESCRIPTION, "", strValue );
				}
			}
		}
		
	}
	
	/** Item in the ValueSetCodeSelector */
	public static class ValueSetCodeSelectorItem {
		public String code;
		public String description;

		public ValueSetCodeSelectorItem(String code, String description) {
			this.code = code;
			this.description = description;
		}
		
		/** Return as "Code (description)" */
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder(code);
			if (description != null && !description.isEmpty()) {
				buf.append(" (").append(description).append(")");
			}
			return buf.toString();
		}
		
		/** Compare */
		@Override
		public boolean equals(Object other) {
			if (other instanceof ValueSetCodeSelectorItem) {
				// compare codes
				return this.code.equals(((ValueSetCodeSelectorItem)other).code);
			} else {
				// compare as strings
				return this.code.equals(other.toString());
			}
		}
	}

	
	// A cell Editor that provides a combo-box for selecting the Code for a value set mapping
	public static class ValueSetCodeSelector extends AbstractCellEditor implements TableCellEditor {
		private static ValueSetCodeSelectorItem BLANK_ITEM = new ValueSetCodeSelectorItem("",null);
		
	    // This is the component that will handle the editing of the cell value
		private JComboBox<ValueSetCodeSelectorItem> m_comboBox = new JComboBox<ValueSetCodeSelectorItem>();
		
		private boolean m_ignoreStateChange = false;
		
		public ValueSetCodeSelector(MdmiValueSet valueSet) {

			// first item is blank
			m_comboBox.removeAllItems();
			m_comboBox.addItem(BLANK_ITEM);
			
			ArrayList<Value> sortedValues = new ArrayList<Value>();
			sortedValues.addAll(valueSet.getValues());
			// sort by code
			Collections.sort(sortedValues, new Comparator<Value>() {
				@Override
				public int compare(Value v1, Value v2) {
					return v1.getCode().compareTo(v2.getCode());
				}
			});
			
			
			for (Value value : sortedValues) {
				m_comboBox.addItem(new ValueSetCodeSelectorItem(value.getCode(), value.getDescription()));
			}
			
			m_comboBox.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (!m_ignoreStateChange) {
						// stop editing when a new selection is made
						if (e.getStateChange() == ItemEvent.SELECTED) {
							stopCellEditing();
						}
					}
					
				}
			});
			
		}

		@Override
		public Object getCellEditorValue() {
			return m_comboBox;
		}


		// This method is called when a cell value is edited (by clicking on it) by the user.
	    @Override
	    public Component getTableCellEditorComponent(JTable table, Object value,
	            boolean isSelected, int rowIndex, int colIndex) {
	        // 'value' is value contained in the cell located at (rowIndex, colIndex)
	    	
	        if (isSelected) {
	            // cell (and perhaps other cells) are selected
	        }

	        // Configure the component with the specified value
	        if (value == null) {
				value = BLANK_ITEM;
			}
	        
	        // select the value
	        m_ignoreStateChange = true;
			m_comboBox.setSelectedItem(value);
	        m_ignoreStateChange = false;

	        // Return the configured component
	        return m_comboBox;
	    }
		
	}
	
	/////////////////////////////////////////////////////
	/** Renderer for table cell - show in color */
	private static class ColorCellRenderer extends DefaultTableCellRenderer {
		Color selectedBackground = null;
		final Color paleGreen = new Color(192, 255, 192);
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
			if (column == TARGET_CODE_COL &&  "".equals(value)) {
				value = "<html><b>** Un-Mapped **</b></html>";
			}

			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);

			if (isSelected && selectedBackground == null) {
				// save it the first time
				selectedBackground = c.getBackground();
			}
			// set the background
			if (!isSelected) {
				c.setBackground(paleGreen);
			} else {
				c.setBackground(selectedBackground);
			}
			return c;
		}
	}
}
