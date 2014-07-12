package org.openhealthtools.mdht.mdmi.editor.map.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.UserPreferences;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.common.components.WrappingDisplayText;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.editor.MdmiDatatypeField;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MdmiModelTree;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MessageGroupNode;
import org.openhealthtools.mdht.mdmi.model.DTSEnumerated;
import org.openhealthtools.mdht.mdmi.model.EnumerationLiteral;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;

/** A dialog to create and populate an enumerated datatype from a CSV file.
 * The CSV file will have a row for each enumerated value, with the data as:
 *     Code, Name, (Description)
 * @author Sally Conway
 *
 */
public class EnumeratedDataTypeCreation extends BaseDialog {
	
	private DTSEnumerated m_dataType = null;

	private JComboBox<MessageGroup> m_messageGroupSelector  = new JComboBox<MessageGroup>();
	private JComboBox<String> m_dataTypeSelector = new JComboBox<String>();
	private JButton m_browseButton = new JButton("...");
	private JTextField m_fileName = new JTextField(12);
	
	// Preview Table
	private static final String[] m_previewColumns = {"Code", "Name", "Description"};
	private static final String[][] s_emptyData = {{"", "", ""},{"", "", ""}};
	private JScrollPane m_previewScroller = new JScrollPane(new JTable(s_emptyData, m_previewColumns));

	// Listener for data changes
	private DataChangedListener m_listener = new DataChangedListener();

	
	private ModelRenderers.MessageGroupRenderer m_messageGroupRenderer = new ModelRenderers.MessageGroupRenderer();

	public EnumeratedDataTypeCreation(Frame owner) {
		super(owner);
		setTitle("Create Enumerated Data Type");
		
		buildUI();
		pack(new Dimension(500, 100));
	}


	// Convert a CSV File with fields:
	//      code, name, description(optional)
	// into an Enumerated Data Type
	// 
	//  Message Group: [__________________|v]
	//  CSV File:  [_______________________] [...]
	//  Datatype:  [_____________________|v] 
	//
	//   -- Preview --------------------------
	//  |                                      |
	//  |                                      |
	//  |                                      |
	//  |                                      |
	//   --------------------------------------
	private void buildUI() {
		JPanel mainPanel = new JPanel(new GridBagLayout());
		add(mainPanel, BorderLayout.CENTER);

		List<MessageGroup> messageGroups = SelectionManager.getInstance().getEntitySelector().getMessageGroups();
		for (MessageGroup group : messageGroups) {
			m_messageGroupSelector.addItem(group);
		}
		m_dataTypeSelector.setEditable(true);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = Standards.getInsets();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.WEST;
		

		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.BOTH;
		WrappingDisplayText textMessage = new WrappingDisplayText("Convert a CSV file with fields: " +
		" Code, Name, Description(optional)\n" + "into an enumerated Data Type");

		mainPanel.add(textMessage, gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;

		
		// Message Group
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		JLabel msgGroupLabel = new JLabel("Message Group:");
		mainPanel.add(msgGroupLabel, gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(m_messageGroupSelector, gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		
		// File
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel("CSV File:"), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(m_fileName, gbc);
		gbc.insets.left = 0;	// snug it up
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		gbc.gridx++;
		mainPanel.add(m_browseButton, gbc);
		gbc.insets.left = Standards.LEFT_INSET;	// restore
		
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		
		// Data Type
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel("Data Type:"), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(m_dataTypeSelector, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		
		// Preview
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 3;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		mainPanel.add(m_previewScroller, gbc);
		m_previewScroller.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Preview"));
		
		// fill data types from first group
		fillDataTypes();

		// add listeners
		m_messageGroupSelector.addActionListener(m_listener);
		m_messageGroupSelector.setRenderer(m_messageGroupRenderer);
		
		m_dataTypeSelector.addActionListener(m_listener);
		((JTextField)m_dataTypeSelector.getEditor().getEditorComponent()).getDocument().addDocumentListener(m_listener);
		m_fileName.getDocument().addDocumentListener(m_listener);
		
		m_browseButton.addActionListener(m_listener);
	}


	/** Fill in the data type selector from this group */
	private void fillDataTypes() {
		MessageGroup group = (MessageGroup)m_messageGroupSelector.getSelectedItem();
		
		List<MdmiDatatype> types = new ArrayList<MdmiDatatype>();
		types.addAll(group.getDatatypes());
		// Sort by type name
		Collections.sort(types, MdmiDatatypeField.getDatatypeComparator());
		
		m_dataTypeSelector.removeActionListener(m_listener);
		m_dataTypeSelector.removeAllItems();
		m_dataTypeSelector.addItem("");	// make first entry blank
		for (MdmiDatatype datatype : types) {
			if (datatype instanceof DTSEnumerated) {
				m_dataTypeSelector.addItem(datatype.getTypeName());
			}
		}
		m_dataTypeSelector.addActionListener(m_listener);
	}
	
	private static final String LAST_ENUM_FILE = "LastEnumFile";

	private void browseForFile() {
		// remember last setting
		UserPreferences preferences = UserPreferences.getInstance(SystemContext.getApplicationName(), null);

		JFileChooser chooser = CSVFileReader.getCSVFileChooser(preferences.getValue(LAST_ENUM_FILE, "."));
		chooser.setDialogTitle("Load Enumerated Data Type");

		int opt = chooser.showOpenDialog(this);
		if (opt == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			
			String lastFileName = file.getAbsolutePath();
			m_fileName.setText(lastFileName);
			
			// Preview
			previewFile(lastFileName);
			
			// save it for next time
			preferences.putValue(LAST_ENUM_FILE, lastFileName);
			setDirty(true);
		}
	}
	
	private void previewFile(String fileName) {
		
		try {
			List<EnumerationLiteral> enumerations = readEnumerationLiterals(new File(fileName));
			String [][] previewData = new String[enumerations.size()][m_previewColumns.length];
			for (int i=0; i<enumerations.size(); i++) {
				EnumerationLiteral literal = enumerations.get(i);
				previewData[i][0] = literal.getCode();
				previewData[i][1] = literal.getName();
				previewData[i][2] = literal.getDescription();
			}
			JTable previewTable = new JTable(previewData, m_previewColumns);
			m_previewScroller.setViewportView(previewTable);
			m_previewScroller.invalidate();
			
		} catch (IOException e) {
			String message = "Unable to read '" + fileName + "'.";
			if (!e.getLocalizedMessage().isEmpty()) {
				message += " Error message is: " + e.getLocalizedMessage();
			}
			JOptionPane.showMessageDialog(this, message, "Invalid Data", JOptionPane.WARNING_MESSAGE);
		}
	}


	@Override
	public void dispose() {
		super.dispose();
		
		// remove all listeners
		m_messageGroupSelector.removeActionListener(m_listener);
		m_messageGroupSelector.setRenderer(null);
		m_dataTypeSelector.removeActionListener(m_listener);
		((JTextField)m_dataTypeSelector.getEditor().getEditorComponent()).getDocument().removeDocumentListener(m_listener);
		m_fileName.getDocument().removeDocumentListener(m_listener);
		
		m_browseButton.removeActionListener(m_listener);
	}

	/** read EnumerationLiterals from a CSV file. The file will be in the form:
	 *        Code, Display, (Description)
	 * @throws IOException 
	 */
	public static List<EnumerationLiteral> readEnumerationLiterals(File csvFile) throws IOException {
		List<EnumerationLiteral> enumerations = new ArrayList<EnumerationLiteral>();
		
		CSVFileReader reader = new CSVFileReader(csvFile);

		// Read File Line By Line
		List<String> stringList = null;

		// Fields
		String code = null;
		String display = null;
		String description = null;
		
		int column = 0;
		while ((stringList = reader.getNextLine()) != null) {
			// skip empty lines
			if (CSVFileReader.isEmptyList(stringList)) {
				continue;
			}

			// read each line 
			column = 0;
			code = CSVFileReader.getString(stringList, column++);
			display = CSVFileReader.getString(stringList, column++);
			description = CSVFileReader.getString(stringList, column++);
			
			if (!code.isEmpty()) {
				// Code is required
			}
			
			
			// create a literal
			EnumerationLiteral literal = new EnumerationLiteral();
			literal.setCode(code);
			literal.setName(display);
			literal.setDescription(description);
			
			enumerations.add(literal);
		}
		
		return enumerations;
	}
	
	
	
	@Override
	public boolean isDataValid() {
		return (!getDataTypeName().isEmpty() && !getFileName().isEmpty());
	}

	public MdmiDatatype findDataType(String typeName) {

		MessageGroup group = (MessageGroup)m_messageGroupSelector.getSelectedItem();
		MdmiDatatype found = null;
		for (MdmiDatatype dt : group.getDatatypes()) {
			if (dt.getTypeName().equalsIgnoreCase(typeName)) {
				found = dt;
				break;
			}
		}
		return  found;
	}

	@Override
	protected void okButtonAction() {
		String dataTypeName = getDataTypeName();
		String fileName = getFileName();
		MessageGroup group = (MessageGroup)m_messageGroupSelector.getSelectedItem();
		
		// find data type - 
		MdmiDatatype dataType = findDataType(dataTypeName);
		if (dataType != null) {

			if (dataType instanceof DTSEnumerated) {
				//  wipe it out

				String message = "A dataType named '" + dataTypeName + "' already exists\n";
				message += "Do you want to replace it.";
				int opt = JOptionPane.showConfirmDialog(this, message, "Existing Data", JOptionPane.YES_NO_OPTION);
				if (opt != JOptionPane.YES_OPTION) {
					return;
				}
				
				m_dataType = (DTSEnumerated)dataType;
				m_dataType.getLiterals().clear();
				
			} else {
				//   give an error if it exists, but isn't the right time
				String message = "A dataType named '" + dataTypeName + "' exists, but is no an Enumerated Type\n";
				message += "Please choose a different name.";
				JOptionPane.showMessageDialog(this, message, "Invalid Data", JOptionPane.WARNING_MESSAGE);
				m_dataTypeSelector.requestFocus();
				return;
			}
			
		} else {
			m_dataType = new DTSEnumerated();
			m_dataType.setTypeName(dataTypeName);
		}
		
		// fill it
		try {
			
			for (EnumerationLiteral literal : readEnumerationLiterals(new File(fileName))) {
				m_dataType.addLiteral(literal);
			}
			
			// update tree
			MdmiModelTree entitySelector = SelectionManager.getInstance().getEntitySelector();
			if (m_dataType.getOwner() == null) {
				// new item - add it to the group
				MessageGroupNode groupNode = (MessageGroupNode) entitySelector.findNode(group);
				groupNode.addDatatype(m_dataType);
			} else {
				// existing
				EditableObjectNode dataTypeNode = (EditableObjectNode)entitySelector.findNode(m_dataType);

				entitySelector.refreshNode(dataTypeNode);
			}
			
		} catch (IOException e) {
			String message = "Unable to read '" + fileName + "'.";
			if (!e.getLocalizedMessage().isEmpty()) {
				message += " Error message is: " + e.getLocalizedMessage();
			}
			JOptionPane.showMessageDialog(this, message, "Invalid Data", JOptionPane.WARNING_MESSAGE);
		}
		
		super.okButtonAction();
	}
	
	public String getDataTypeName() {
		JTextField ed =  (JTextField)m_dataTypeSelector.getEditor().getEditorComponent();
		return ed.getText().trim();
	}
	
	public String getFileName() {
		return m_fileName.getText().trim();
	}


	private class DataChangedListener implements DocumentListener, ActionListener {

		@Override
		public void insertUpdate(DocumentEvent e) {
			documentChanged(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			documentChanged(e);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			documentChanged(e);
		}
		
		private void documentChanged(DocumentEvent e) {
			setDirty(true);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == m_browseButton) {
				browseForFile();

			} else if (e.getSource() == m_messageGroupSelector) {
				fillDataTypes();
				setDirty(true);
				
			} else {
				setDirty(true);
			}
		}
		
	}

}
