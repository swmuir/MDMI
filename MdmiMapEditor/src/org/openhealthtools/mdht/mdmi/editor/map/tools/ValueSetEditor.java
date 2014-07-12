package org.openhealthtools.mdht.mdmi.editor.map.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import org.openhealthtools.mdht.mdmi.MdmiValueSet;
import org.openhealthtools.mdht.mdmi.MdmiValueSet.Value;
import org.openhealthtools.mdht.mdmi.MdmiValueSetMap;
import org.openhealthtools.mdht.mdmi.MdmiValueSetMap.Mapping;
import org.openhealthtools.mdht.mdmi.MdmiValueSetsHandler;
import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.common.components.ExceptionDetailsDialog;
import org.openhealthtools.mdht.mdmi.editor.common.tables.TableSorter;
import org.openhealthtools.mdht.mdmi.editor.map.editor.AdvancedSelectionField;

/** A Dialog for creating the mappings between value sets.
 * The value sets can be initialized from CSV files if necessary.
 * @author Sally Conway
 *
 */
public class ValueSetEditor extends BaseDialog {
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");

	
	private JSplitPane m_mainPanel;
	private JPanel m_topPart;
	private JSplitPane m_tableSplitter;

	private MappingTablePanel m_srcToTargetTable = null;
	private MappingTablePanel m_targetToSrcTable = null;
	
	// Top
	private JLabel m_nameField = new JLabel("Mappings");
	
	// Tabs
	private static final String[] s_columnLabels = {"Source Code", "Source Description", "Target Code", "Target Description"};
	private static final int SOURCE_CODE_COL = 0;
	private static final int SOURCE_DESCRIPTION_COL = 1;
	private static final int TARGET_CODE_COL = 2;
	private static final int TARGET_DESCRIPTION_COL = 3;
	
	private ActionListener m_importBtnListener = new ImportButtonListener();


	public ValueSetEditor(Frame owner, MdmiValueSetMap mapSrcToTarget, MdmiValueSetMap mapTargetToSrc) {
		super(owner, BaseDialog.OK_CANCEL_OPTION);
		setTitle("Value Set Editor");
		
		buildUI();
		setValueSetMap( mapSrcToTarget,  mapTargetToSrc);
		pack(new Dimension(1000, 700));
	}

	public ValueSetEditor(Dialog owner, MdmiValueSetMap mapSrcToTarget, MdmiValueSetMap mapTargetToSrc) {
		super(owner, BaseDialog.OK_CANCEL_OPTION);
		setTitle("Value Set Editor");
		
		buildUI();
		setValueSetMap( mapSrcToTarget,  mapTargetToSrc);
		pack(new Dimension(1000, 700));
	}
	
	/** use the maps to build the tables */
	private void setValueSetMap(MdmiValueSetMap mapSrcToTarget, MdmiValueSetMap mapTargetToSrc) {
		
		// update label
		m_nameField.setText("<html>Mappings between <b>" + mapSrcToTarget.getSourceSet().getName()
				+ "</b> and <b>" + mapSrcToTarget.getTargetSet().getName() + "</b></html>");
		
		// set up the tables
		m_srcToTargetTable = new MappingTablePanel(mapSrcToTarget);
		m_tableSplitter.setLeftComponent(m_srcToTargetTable);
		m_srcToTargetTable.addBtnListener(m_importBtnListener);

		// other table is reversed
		m_targetToSrcTable = new MappingTablePanel(mapTargetToSrc);
		m_tableSplitter.setRightComponent(m_targetToSrcTable);
		m_targetToSrcTable.addBtnListener(m_importBtnListener);
		
		setDirty(true);	// allow OK button
	}
	
	@Override
	public void dispose() {
		if (m_srcToTargetTable != null) {
			m_srcToTargetTable.removeBtnListener(m_importBtnListener);
		}
		if (m_targetToSrcTable != null) {
			m_targetToSrcTable.removeBtnListener(m_importBtnListener);
		}
		super.dispose();
	}
	
	/** Load mapping from a CSV file. The file will be in the form:
	 *        From_Code, From_Description, To_Code, To_Descro[topm
	 * @throws IOException 
	 */

	static String s_lastFileName = null;
	private void loadFromCSVFile(MappingTablePanel mappingPanel) throws IOException {
		
		if (mappingPanel == null) {
			return;
		}
		
		MdmiValueSetMap valueSetMap = mappingPanel.m_tableModel.valueSetMap;
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
						message.append("From code");
						if (toCode.isEmpty()) {
							message.append(" and a ");
						}
					}
					if (toCode.isEmpty()) {
						message.append("To code");
					}
					JOptionPane.showMessageDialog(ValueSetEditor.this,
							message, "Invalid Format", JOptionPane.ERROR_MESSAGE);
					return;
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
		}
		mappingPanel.resetTable();
		
		// update the other table's selector
		MappingTablePanel otherPanel = null;
		if (mappingPanel == m_srcToTargetTable) {
			otherPanel = m_targetToSrcTable;
		} else {
			otherPanel = m_srcToTargetTable;
		}
		otherPanel.populateCodeSelector(sourceSet);
	}
	
	
	//  Value Set
	//  Mappings between A and B
	//  =============================================================================
	//  Mapping from  A to B          [CSV]  ||  Mapping from B to A            [CSV]
	//  -----------------------------------  ||  -----------------------------------
	// | Code | Descr | To Code | To Descr | || | Code | Descr | To Code | To Descr | 
	// |------|-------|---------|----------| || |------|-------|---------|----------|
	// |______|_______|_________|__________| || |______|_______|_________|__________| 
	// |______|_______|_________|__________| || |______|_______|_________|__________| 
	// |______|_______|_________|__________| || |______|_______|_________|__________| 
	//                                       || |______|_______|_________|__________| 
	//
	//
	void buildUI() {
		// create the main parts (top, bottom)
		m_mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		m_tableSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
		m_topPart = new JPanel();
		m_mainPanel.setTopComponent(m_topPart);
		m_mainPanel.setBottomComponent(m_tableSplitter);
		
		
		// Top
		buildTopPanel(m_topPart);
		Dimension pref = m_topPart.getPreferredSize();
		m_mainPanel.setDividerLocation(pref.height + Standards.BOTTOM_INSET + Standards.TOP_INSET);
		
		// Bottom
		m_tableSplitter.setDividerLocation(500);
		
		add(m_mainPanel, BorderLayout.CENTER);
	}

	
	void buildTopPanel(JPanel topPanel) {
		topPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.insets = Standards.getInsets();

		//  Mappings between X and Y
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 0;
		gbc.gridy = 0;

		gbc.weightx = 0;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		topPanel.add(m_nameField, gbc);
	}


	@Override
	public boolean isDataValid() {
		return true;
	}

	@Override
	protected void okButtonAction() {

		super.okButtonAction();
	}
	
	/** A Sub-panel that contains a table that shows the mappings in an MdmiValueSetMap. */

	//  Mapping from  A to B          [CSV]  
	//  -----------------------------------  
	// | Code | Descr | To Code | To Descr | 
	// |------|-------|---------|----------| 
	// |______|_______|_________|__________| 
	// |______|_______|_________|__________| 
	// |______|_______|_________|__________| 
	//                                      
	private class MappingTablePanel extends JPanel {
		
		MappingTableModel m_tableModel = new MappingTableModel();
		TableSorter m_sorter = new TableSorter( m_tableModel );
		JTable m_table= new JTable();

		JButton m_openCSVFile = new JButton("Import from CSV...");

		public MappingTablePanel(MdmiValueSetMap map) {
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
			populateCodeSelector(map.getTargetSet());
			
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
		
		public void resetTable() {
			m_tableModel.rowCount = m_tableModel.valueSetMap.getSourceSet().getValues().size();
			m_sorter.reallocateIndexes();
			
			m_table.revalidate();
		}

		/** Re populate the combo box that selects the target */
		private void populateCodeSelector(MdmiValueSet targetSet) {
			// create a combo-box editor for the "Target Mapping" column
			CodeSelector selector = new CodeSelector(targetSet);

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
			north.add(m_openCSVFile, gbc);

			add(north, BorderLayout.NORTH);

			m_table.setColumnSelectionAllowed(true);	// allow single item selection
			m_table.setRowHeight(18);	// a bit bigger
			
			add(new JScrollPane(m_table), BorderLayout.CENTER);
		}

		void addBtnListener(ActionListener listener) {
			m_openCSVFile.addActionListener(listener);
		}
		void removeBtnListener(ActionListener listener) {
			m_openCSVFile.removeActionListener(listener);
		}
		
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
			}
			if (value != null) {
				strValue = value.toString();
			}
			
			if (columnIndex == SOURCE_CODE_COL) {
				srcValue.setCode(strValue);
				
			} else if (columnIndex == SOURCE_DESCRIPTION_COL) {
				srcValue.setDescription(strValue);

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
				} else {
					// create new mapping
					valueSetMap.addMapping(srcCode, targetCode);
				}
				// changing the target code will also impact the target description
				repaint();
				
			} else if (columnIndex == TARGET_DESCRIPTION_COL) {
				String srcCode = srcValue.getCode();
				// find the mapping for the source
				Mapping mapping = findMapping(srcCode);
				if (mapping != null) {
					// set description
					mapping.getTarget().setDescription(strValue);
				}
			}
		}
		
	}

	
	// Editor for selecting a mapping
	public static class CodeSelector extends AbstractCellEditor implements TableCellEditor {
	    // This is the component that will handle the editing of the cell value
		private JComboBox<String> m_comboBox = new JComboBox<String>();
		
		private boolean m_ignoreStateChange = false;
		
		public CodeSelector(MdmiValueSet valueSet) {

			// first item is blank
			m_comboBox.removeAllItems();
			m_comboBox.addItem(AdvancedSelectionField.BLANK_ENTRY);
			for (Value value : valueSet.getValues()) {
				m_comboBox.addItem(value.getCode());
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
				value = AdvancedSelectionField.BLANK_ENTRY;
			}
	        
	        // select the value
	        m_ignoreStateChange = true;
			m_comboBox.setSelectedItem(value);
	        m_ignoreStateChange = false;

	        // Return the configured component
	        return m_comboBox;
	    }
		
	}
	
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
	
	private class ImportButtonListener implements ActionListener {

		@Override
		public void actionPerformed(final ActionEvent e) {
			MappingTablePanel mappingPanel = null;
			if (e.getSource() ==  m_srcToTargetTable.m_openCSVFile) {
				mappingPanel = m_srcToTargetTable;
			} else if (e.getSource() == m_targetToSrcTable.m_openCSVFile) {
				mappingPanel = m_targetToSrcTable;
			}
			
			try {
				loadFromCSVFile(mappingPanel);
			} catch (Exception ex) {
				ExceptionDetailsDialog.showException(ValueSetEditor.this, ex);
			}
		}

	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			String lAndF = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(lAndF);
		} catch (Exception e) {
		}
		
		JFrame frame = new JFrame();
		frame.setVisible(true);
		
		MdmiValueSetsHandler handler = ValueSetIdentifierDialog.createDummyHandler();
		// need two value sets with maps to/from each other
		Collection<MdmiValueSet> allValueSets = handler.getAllValueSets();
		
		MdmiValueSetMap map1 = null;
		MdmiValueSetMap map2 = null;
		
		MdmiValueSet vs1 = null;
		for (MdmiValueSet vs : allValueSets) {
			if (vs1 == null) {
				vs1 = vs;
			} else {
				// check for maps in both directions
				map1 = handler.getValueSetMap(vs1.getName() + "." + vs.getName());
				map2 = handler.getValueSetMap(vs.getName() + "." + vs1.getName());

				if (map1 != null && map2 != null) {
					break;
				}
			}
		}

		if (map1 != null && map2 != null) {
			ValueSetEditor testDialog = new ValueSetEditor(frame, map1, map2);

			// show it
			testDialog.center();
			@SuppressWarnings("unused")
			int val = testDialog.display();
		}
		
		System.exit(0);

	}

}
