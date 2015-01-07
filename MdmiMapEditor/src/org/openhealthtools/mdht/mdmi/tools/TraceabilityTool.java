package org.openhealthtools.mdht.mdmi.tools;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.openhealthtools.mdht.mdmi.editor.common.UserPreferences;
import org.openhealthtools.mdht.mdmi.editor.common.components.WrappingDisplayText;
import org.openhealthtools.mdht.mdmi.model.ConversionRule;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MdmiDomainDictionaryReference;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.MessageModel;
import org.openhealthtools.mdht.mdmi.model.Node;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.ToBusinessElement;
import org.openhealthtools.mdht.mdmi.model.ToMessageElement;
import org.openhealthtools.mdht.mdmi.model.validate.ModelValidationResults;
import org.openhealthtools.mdht.mdmi.model.xmi.direct.reader.MapBuilderXMIDirect;

/*
 * Tool to provide trace information
 */
public class TraceabilityTool extends JFrame implements ActionListener {

	/** Field separator for Absolute Name */
	private static final char NAME_SEPARATOR = '.';

	/** Field separator for Absolute Location */
	private static final char PATH_SEPARATOR = '/';
	
	private static final String XMI_EXTENSION = "xmi";
	private static final String CSV_EXTENSION = "csv";
	
	// persisted values
	private static final String LAST_FILE_OPENED = "LastFileOpened";
	private static final String LAST_FILE_WRITTEN = "LastFileWritten";

	// check box states
	private static final String UNIQUEID_STATE    = "UniqueIDState";
	private static final String DESCRIPTION_STATE = "DescriptionState";
	private static final String CODELLIST_STATE = "CodeListState";
	private static final String ABS_LOCATION_STATE = "AbsoluteLocationState";
	private static final String ABS_NAME_STATE = "AbsoluteNameState";
	private static final String CARDINALITY_STATE = "CardinalityState";
	private static final String COMPUTED_VALUE_STATE = "ComputedValueState";
	private static final String SOURCE_TO_TARGET_STATE = "SourceToTargetState";
	private static final String TARGET_TO_SOURCE_STATE = "TargetToSourceState";
	
	/** Resource for localization */
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.tools.Local");
	
	private JTextField m_sourceFileName = new JTextField(35);
	private JTextField m_targetFileName[] = new JTextField[] {new JTextField(35), new JTextField(35)};
	private JTextField m_outputFileName = new JTextField(35);
	private JButton    m_browseSourceBtn = new JButton("...");
	private JButton    m_browseTargetBtn[] = new JButton[] {new JButton("..."), new JButton("...")};
	private JButton    m_browseOutputBtn = new JButton("...");
	
	// Optional Fields
	private JCheckBox m_uniqueID = new JCheckBox(s_res.getString("TraceabilityTool.uniqueID"));
	private JCheckBox m_description = new JCheckBox(s_res.getString("TraceabilityTool.description"));
	private JCheckBox m_codeList = new JCheckBox(s_res.getString("TraceabilityTool.codeList"));
	private JCheckBox m_absoluteLocation = new JCheckBox(s_res.getString("TraceabilityTool.absolutePathLocation"));
	private JCheckBox m_absoluteName = new JCheckBox(s_res.getString("TraceabilityTool.absolutePathName"));
	private JCheckBox m_cardinality = new JCheckBox(s_res.getString("TraceabilityTool.cardinality"));
	private JCheckBox m_computedValue = new JCheckBox(s_res.getString("TraceabilityTool.computedValue"));
	
	// Direction buttons
	private JCheckBox m_sourceToTarget = new JCheckBox(s_res.getString("TraceabilityTool.sourceToTarget"));
	private JCheckBox m_targetToSource = new JCheckBox(s_res.getString("TraceabilityTool.targetToSource"));
	
	// Generate buttons
	private JButton    m_generateBtn = new JButton(s_res.getString("TraceabilityTool.generateBtn"));

	private File m_lastInputFile = null;
	private File m_lastOutputFile = null;
	
	// Found SEs
	ArrayList<SemanticElement> m_foundSourceSes = new ArrayList<SemanticElement>();
	ArrayList<SemanticElement> m_foundTarget1Ses = new ArrayList<SemanticElement>();
	ArrayList<SemanticElement> m_foundTarget2Ses = new ArrayList<SemanticElement>();
	
	private UserPreferences m_pref = UserPreferences.getInstance("TraceabilityTool", null);
	
	/** field separator */
	public static char SEPARATOR = ',';
	
	public TraceabilityTool() {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle(s_res.getString("TraceabilityTool.title"));
		buildUI();
		
		// read last file(s)
        String lastFileName = m_pref.getValue(LAST_FILE_OPENED, null);
        if (lastFileName != null) {
        	m_lastInputFile = new File(lastFileName);
        }
        lastFileName = m_pref.getValue(LAST_FILE_WRITTEN, null);
        if (lastFileName != null) {
        	m_lastOutputFile = new File(lastFileName);
        }
		pack();
	}
	
	private void buildUI() {
		getContentPane().setLayout(new BorderLayout());
		
		JPanel main = new JPanel(new GridBagLayout());
		
		// Source File:     [_______________________________] [...]
		// Target File(1):  [_______________________________] [...]
		// Target File(2):  [_______________________________] [...]
		// Output File:     [_______________________________] [...]
		//
		//  - Optional Fields-------------------------
		// | [x] Description     [x] Location         |
		//  ------------------------------------------
		//
		//  - Direction -------------------------------------
		// | [x] Source-to-Target    [x] Target-to-Source    |
		//  -------------------------------------------------
		//
		//            [Generate]
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 0;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.NONE;
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		// Generate traceability information between standards and the Referent Index
		WrappingDisplayText instructions = new WrappingDisplayText();
		instructions.setText(s_res.getString("TraceabilityTool.instructions"));
		main.add(instructions, gbc);
		gbc.gridwidth = 1;
		
		gbc.gridy++;

		// Source File: [__________________________][...]
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		main.add(new JLabel(s_res.getString("TraceabilityTool.sourceFile")), gbc);
		gbc.gridx++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		main.add(m_sourceFileName, gbc);
		gbc.gridx++;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		gbc.insets.left = 0;
		main.add(m_browseSourceBtn, gbc);
		gbc.insets.left = 10;
		
		gbc.insets.top = 0;
		
		for (int i=0; i<2; i++) {
			gbc.gridx = 0;
			gbc.gridy++;

			// Target File (1): [__________________________][...]
			gbc.weightx = 0;
			gbc.fill = GridBagConstraints.NONE;
			String textLabel = MessageFormat.format(s_res.getString("TraceabilityTool.targetFileI"), (i+1));
			main.add(new JLabel(textLabel), gbc);
			gbc.gridx++;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			main.add(m_targetFileName[i], gbc);
			gbc.gridx++;
			gbc.fill = GridBagConstraints.NONE;
			gbc.weightx = 0;
			gbc.insets.left = 0;
			main.add(m_browseTargetBtn[i], gbc);
			gbc.insets.left = 10;
		}
		
		gbc.insets.top = 10;
		gbc.gridx = 0;
		gbc.gridy++;

		// Output File: [__________________________][...]
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		main.add(new JLabel(s_res.getString("TraceabilityTool.outputFile")), gbc);
		gbc.gridx++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		main.add(m_outputFileName, gbc);
		gbc.gridx++;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		gbc.insets.left = 0;
		main.add(m_browseOutputBtn, gbc);
		gbc.insets.left = 10;
		
		gbc.insets.top = 10;
		gbc.gridx = 0;
		gbc.gridy++;
		
		// optional fields
		JPanel optionalFields = new JPanel(new GridLayout(0, 3, 10, 10));
		optionalFields.add(m_uniqueID);
		optionalFields.add(m_description);
		optionalFields.add(m_codeList);
		optionalFields.add(m_absoluteLocation);
		optionalFields.add(m_absoluteName);
		optionalFields.add(m_cardinality);
		optionalFields.add(m_computedValue);
		// set state
		m_uniqueID.setSelected(m_pref.getBooleanValue(UNIQUEID_STATE, true));
		m_description.setSelected(m_pref.getBooleanValue(DESCRIPTION_STATE, true));
		m_codeList.setSelected(m_pref.getBooleanValue(CODELLIST_STATE, true));
		m_absoluteLocation.setSelected(m_pref.getBooleanValue(ABS_LOCATION_STATE, true));
		m_absoluteName.setSelected(m_pref.getBooleanValue(ABS_NAME_STATE, true));
		m_cardinality.setSelected(m_pref.getBooleanValue(CARDINALITY_STATE, true));
		m_computedValue.setSelected(m_pref.getBooleanValue(COMPUTED_VALUE_STATE, true));

		optionalFields.setBorder( BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				s_res.getString("TraceabilityTool.options")));
		gbc.weightx = 1.0;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		main.add(optionalFields, gbc);
		gbc.gridwidth = 1;
		
		
		// Direction
		gbc.gridx = 0;
		gbc.gridy++;
		
		// optional fields
		JPanel directionFields = new JPanel(new GridLayout(0, 2, 10, 10));
		directionFields.add(m_sourceToTarget);
		directionFields.add(m_targetToSource);
		// set state
		m_sourceToTarget.setSelected(m_pref.getBooleanValue(SOURCE_TO_TARGET_STATE, true));
		m_targetToSource.setSelected(m_pref.getBooleanValue(TARGET_TO_SOURCE_STATE, true));

		directionFields.setBorder( BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				s_res.getString("TraceabilityTool.direction")));
		gbc.weightx = 1.0;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		main.add(directionFields, gbc);
		gbc.gridwidth = 1;
		
		getContentPane().add(main, BorderLayout.CENTER);
		
		//        [Generate]
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		buttons.add(m_generateBtn);
		// disable until input and output files defined
		m_generateBtn.setEnabled(false);
		getContentPane().add(buttons, BorderLayout.SOUTH);
		

    	m_browseSourceBtn.addActionListener(this);
    	m_browseTargetBtn[0].addActionListener(this);
    	m_browseTargetBtn[1].addActionListener(this);
    	m_browseOutputBtn.addActionListener(this);
    	
    	m_uniqueID.addActionListener(this);
		m_description.addActionListener(this);
		m_codeList.addActionListener(this);
		m_absoluteLocation.addActionListener(this);
		m_absoluteName.addActionListener(this);
		m_cardinality.addActionListener(this);
		m_computedValue.addActionListener(this);

		m_sourceToTarget.addActionListener(this);
		m_targetToSource.addActionListener(this);
		
    	m_generateBtn.addActionListener(this);
	}

 

    @Override
	public void dispose() {
    	// save
        if (m_lastInputFile != null) {
            m_pref.putValue(LAST_FILE_OPENED, m_lastInputFile.getAbsolutePath());
        }
        if (m_lastOutputFile != null) {
        	m_pref.putValue(LAST_FILE_WRITTEN, m_lastOutputFile.getAbsolutePath());
        }
        
		// cleanup
    	m_browseSourceBtn.removeActionListener(this);
    	m_browseTargetBtn[0].removeActionListener(this);
    	m_browseTargetBtn[1].removeActionListener(this);
    	m_browseOutputBtn.removeActionListener(this);
    	
    	m_uniqueID.removeActionListener(this);
		m_description.removeActionListener(this);
		m_codeList.removeActionListener(this);
		m_absoluteLocation.removeActionListener(this);
		m_absoluteName.removeActionListener(this);
		m_cardinality.removeActionListener(this);
		m_computedValue.removeActionListener(this);
		
		m_sourceToTarget.removeActionListener(this);
		m_targetToSource.removeActionListener(this);
		
    	m_generateBtn.removeActionListener(this);
    	
		super.dispose();
	}
    
    /** Browse for an input or output file */
    private File getFile(boolean read) {
    	File lastFile = null;
        if (read) {
        	lastFile = m_lastInputFile;
        } else {
        	lastFile = m_lastOutputFile;
        	// if not found, use input file with CSV extension
        	if (lastFile == null && m_lastInputFile != null) {
        		String inputFileName = m_lastInputFile.getAbsolutePath();
        		// replace .xmi with .csv
        		inputFileName = inputFileName.replace(".xmi", ".csv");
        		lastFile = new File(inputFileName);
        	}
        }
        
        // create a file chooser
        JFileChooser chooser = new JFileChooser(lastFile);
        if (read) {
        	// read XMI
            chooser.setFileFilter(new FileNameExtensionFilter("XMI file", XMI_EXTENSION));
        } else {
        	// write CSV
            chooser.setFileFilter(new FileNameExtensionFilter("CSV file", CSV_EXTENSION));
        }
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        // pre-set
        if (lastFile != null && lastFile.exists()) {
        	chooser.setSelectedFile(lastFile);
        }
        
        File file = null;

        // prompt for a file
        int opt;
        if (read) {
        	opt = chooser.showOpenDialog(this);
        } else {
        	opt = chooser.showSaveDialog(this);
        }
        
        if (opt == JFileChooser.APPROVE_OPTION) {
        	// save it
        	file = chooser.getSelectedFile();
        	if (read) {
        		m_lastInputFile = file;
        	} else {
        		m_lastOutputFile = file;
        	}
        }
        
        return file;
    }
    
    /** Open the input files and produce the output file */
    private void generateOutputData() {

    	String srcFileName = m_sourceFileName.getText().trim();
    	String targetFileName1 = m_targetFileName[0].getText().trim();
    	String targetFileName2 = m_targetFileName[1].getText().trim();
    	
    	try {
    		// check input files
    		int numFiles = 2;
    		File srcFile = new File(srcFileName);
    		File targetFile1 = new File(targetFileName1);
    		// File2 is optional
    		File targetFile2 = null;
    		if (!targetFileName2.isEmpty()) {
    			numFiles++;
    			targetFile2 = new File(targetFileName2);
    		}

    		for (int i=0; i<numFiles; i++) {
    			// validate file
    			String inFileName;
    			File inputFile;
    			if (i == 0) {
    				inFileName = srcFileName;
    				inputFile = srcFile;
    			} else if (i == 1) {
    				inFileName = targetFileName1;
    				inputFile = targetFile1;
    				
    			} else {
    				inFileName = targetFileName2;
    				inputFile = targetFile2;
    				
    			}
    			if (inFileName.isEmpty()) {
    				// No input file has been specified."
    				JOptionPane.showMessageDialog(this, s_res.getString("TraceabilityTool.noInputMessage"),
    						s_res.getString("TraceabilityTool.noInputTitle"), JOptionPane.ERROR_MESSAGE);
    				return;
    			}
    			if (!inputFile.exists()) {
    				// The file, fileName, does not exist.
    				// Please specify an existing file.
    				JOptionPane.showMessageDialog(this, 
    						MessageFormat.format(s_res.getString("TraceabilityTool.fileDoesntExistMessage"), inFileName),
    						s_res.getString("TraceabilityTool.fileDoesntExistTitle"),
    						JOptionPane.ERROR_MESSAGE);
    				return;
    			}
    		}
    		
    		// check for duplicate names
    		if (srcFile.equals(targetFile1) || srcFile.equals(targetFile2)) {
    			//The input files must be different.
    			JOptionPane.showMessageDialog(this, s_res.getString("TraceabilityTool.duplicateInputMessage"),
    					s_res.getString("TraceabilityTool.duplicateInputTitle"), JOptionPane.ERROR_MESSAGE);
    			return;
    		}

    		// check output file
    		String outFileName = m_outputFileName.getText().trim();
    		if (outFileName.isEmpty()) {
    			// No output file has been specified."
    			JOptionPane.showMessageDialog(this, s_res.getString("TraceabilityTool.noOutputMessage"),
    			s_res.getString("TraceabilityTool.noOutputTitle"), JOptionPane.ERROR_MESSAGE);
    			return;
    		}
    		if (!outFileName.endsWith(CSV_EXTENSION)) {
    			// tack on ".csv" extension
    			outFileName += "." + CSV_EXTENSION;
    		}
    		File outFile = new File(outFileName);
    		if (outFile.exists()) {
    			// The file, fileName, already exists.
    			// Are you sure you want to overwrite it?
    			int opt = JOptionPane.showConfirmDialog(this, 
    					MessageFormat.format(s_res.getString("TraceabilityTool.fileExistsMessage"), outFile.getName()),
    					s_res.getString("TraceabilityTool.fileExistsTitle"),
    					JOptionPane.YES_NO_OPTION);
    			
    			if (opt != JOptionPane.YES_OPTION) {
    				return;
    			}
    		}
    		

    		// read
    		ModelValidationResults results = new ModelValidationResults();
    		List<MessageGroup> srcGroups = MapBuilderXMIDirect.build(srcFile, results);
    		List<MessageGroup> targetGroups1 = MapBuilderXMIDirect.build(targetFile1, results);
    		List<MessageGroup> targetGroups2 = null;
    		if (targetFile2 != null) {
    			targetGroups2 = MapBuilderXMIDirect.build(targetFile2, results);
    		}
    		
    		// there should only be one message group
    		StringBuilder warning = new StringBuilder();
    		if (srcGroups.size() != 1) {
    			// The file, {0}, contains multiple message groups. Only the first will be used.
				warning.append(MessageFormat.format(s_res.getString("TraceabilityTool.multipleGroupsMessage"),
						srcFileName));
    		}
    		if (targetGroups1.size() != 1) {
    			// The file, {0}, contains multiple message groups. Only the first will be used.
    			if (warning.length() > 0) {
    				warning.append("\n");
    			}
				warning.append(MessageFormat.format(s_res.getString("TraceabilityTool.multipleGroupsMessage"),
						targetFileName1));
    		}
    		if (targetGroups2 != null && targetGroups2.size() != 1) {
    			// The file, {0}, contains multiple message groups. Only the first will be used.
    			if (warning.length() > 0) {
    				warning.append("\n");
    			}
				warning.append(MessageFormat.format(s_res.getString("TraceabilityTool.multipleGroupsMessage"),
						targetFileName2));
    		}
    		if (warning.length() > 0) {
    			JOptionPane.showMessageDialog(this, warning,
    					s_res.getString("TraceabilityTool.multipleGroupsTitle"),
    					JOptionPane.INFORMATION_MESSAGE);
    		}

    		//---------------------------------------
    		// open output file
    		//---------------------------------------
    		FileOutputStream fstream = new FileOutputStream(outFile);
    		DataOutputStream out = new DataOutputStream(fstream);
    		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
    		
    		//---------------------------------------
    		// Generate Source->Target
    		//---------------------------------------
    		int beCount = 0;
    		if (m_sourceToTarget.isSelected()) {
    			writer.write("Source: ");
    			writer.write(srcFile.getName());
    			writer.newLine();
    			if (targetFile2 != null) {
        			writer.write("Target 1: ");
        			writer.write(targetFile1.getName());
        			writer.newLine();
        			writer.flush();
        			writer.write("Target 2: ");
        			writer.write(targetFile2.getName());
        			writer.newLine();
        			writer.flush();
    				
    			} else {
        			writer.write("Target: ");
        			writer.write(targetFile1.getName());
        			writer.newLine();
        			writer.flush();
    			}

    			beCount = Math.max(beCount, 
    					generateOutput(srcGroups.get(0), targetGroups1.get(0), 
    							targetGroups2 == null ? null :targetGroups2.get(0), writer));
    		}

    		if (m_sourceToTarget.isSelected() && m_targetToSource.isSelected()) {
    			writer.newLine();
    		}
    		
    		//---------------------------------------
    		// Generate Target->Source
    		//---------------------------------------
    		if (m_targetToSource.isSelected()) {
    			writer.write("Source: ");
    			writer.write(targetFile1.getName());
    			writer.newLine();
    			writer.write("Target: ");
    			writer.write(srcFile.getName());
    			writer.newLine();
    			writer.flush();

    			beCount = Math.max(beCount, generateOutput(targetGroups1.get(0), srcGroups.get(0), null, writer));
    		}

    		//---------------------------------------
    		// Close
    		//---------------------------------------
    		writer.close();
    		out.close();
    		fstream.close();
    		
    		// Finished updating:
    		//  X Business Elements were identified. Would you like to open FILE to view the data
    		String message = MessageFormat.format(s_res.getString("TraceabilityTool.analysisMessage"), beCount,
    				outFile.getPath());

    		int option = JOptionPane.showConfirmDialog(this, message, s_res.getString("TraceabilityTool.analysisTitle"),
    				JOptionPane.YES_NO_OPTION);
    		if (option == JOptionPane.YES_OPTION) {
    			if (Desktop.isDesktopSupported()) {
    			    try {
    			        Desktop.getDesktop().open(outFile);
    			    } catch (IOException ex) {
    		    		JOptionPane.showMessageDialog(this, "Unable to open output file. Error is:\n\n" + ex.getMessage(),
    		    				"Error", JOptionPane.ERROR_MESSAGE);
    			    }
    			}
    		}
    		
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		String message = ex.getLocalizedMessage();
    		if (message == null || message.isEmpty()) {
    			message = ex.getClass().getSimpleName();
    		}
    		JOptionPane.showMessageDialog(this, message, "Unexpected Error", JOptionPane.ERROR_MESSAGE);
    	}
    }

    /** write the data from the two input groups 
     * @return the number of business elements mapped */
    
	private int generateOutput(MessageGroup sourceGroup, MessageGroup targetGroup1, 
			MessageGroup targetGroup2, // optional
			BufferedWriter writer)
			throws FileNotFoundException, IOException {

		//------------------------------------
		// 1. write header
		//------------------------------------
		// Business Element Data
		if (m_uniqueID.isSelected()) {
			writer.write("Unique Id");						// Column 1
			writer.write(SEPARATOR);
		}
		writer.write("Business Element Name");			// Column 2
		writer.write(SEPARATOR);
		if (m_description.isSelected()) {
			writer.write("Business Element Description");	// Column 3
			writer.write(SEPARATOR);
		}
		writer.write("Data Type Name");					// Column 4
		writer.write(SEPARATOR);
		if (m_codeList.isSelected()) {
			writer.write("Code List");						// Column 5
			writer.write(SEPARATOR);
		}

		// Semantic Element
		// Source and Target
		String [] directions = {"Source", "Target"};
		if (targetGroup2 != null) {
			directions = new String[]{"Source", "Target1", "Target2"};
		}
		for (String direction : directions) {
			writer.write(SEPARATOR);	// force a blank column between sections
			
			writer.write(direction + " Semantic Element Name");	
			writer.write(SEPARATOR);
			if (m_description.isSelected()) {
				writer.write("Semantic Element Description");
				writer.write(SEPARATOR);
			}
			writer.write("Data Type Name");
			writer.write(SEPARATOR);
			if (m_absoluteLocation.isSelected()) {
				writer.write("Absolute Location");
				writer.write(SEPARATOR);
			}
			if (m_absoluteName.isSelected()) {
				writer.write("Absolute Name");
				writer.write(SEPARATOR);
			}
			if (m_cardinality.isSelected()) {
				writer.write("Cardinality");
				writer.write(SEPARATOR);
			}
			if (m_codeList.isSelected()) {
				writer.write("Code List");
				writer.write(SEPARATOR);
			}
			writer.write("Relationship");
			writer.write(SEPARATOR);
			if (m_computedValue.isSelected()) {
				writer.write("Computed Value");	
				writer.write(SEPARATOR);
			}
		}

		
		writer.newLine();
		writer.flush();

		// count
		int linesWritten = 0;

		// sort BEs by name
		Comparator<MdmiBusinessElementReference> beCompare = getMdmiBusinessElementReferenceComparator();
		
		// do it
		MdmiDomainDictionaryReference domainDictionary = sourceGroup.getDomainDictionary();

		List<MdmiBusinessElementReference> businessElements = new ArrayList<MdmiBusinessElementReference>();
		businessElements.addAll(domainDictionary.getBusinessElements());

		// Sort BEs by name
		Collections.sort(businessElements, beCompare);

		for (MdmiBusinessElementReference be : businessElements) {

			// find semantic elements that reference this BER as a source
			Map<SemanticElement, List<ConversionRule>> sourceSEs = findSemanticElements(be, sourceGroup, true);
			if (sourceSEs.isEmpty()) {
				continue;
			}
			
			// find semantic elements that reference this BER as a target
			Map<SemanticElement, List<ConversionRule>> target1SEs = findSemanticElements(be, targetGroup1, false);
			if (target1SEs.isEmpty()) {
				continue;
			}
			
			// find semantic elements that reference this BER as a target
			Map<SemanticElement, List<ConversionRule>> target2SEs = null;
			if (targetGroup2 != null) {
				target2SEs = findSemanticElements(be, targetGroup2, false);
				if (target2SEs.isEmpty()) {
					continue;
				}
			}
			
			// clear list
			m_foundSourceSes.clear();
			m_foundTarget1Ses.clear();
			m_foundTarget2Ses.clear();
			
			// If dataType is complex, write an entry for each attribute of the data type
			MdmiDatatype dataType = be.getReferenceDatatype();
			if (dataType instanceof DTComplex) {
				// add fields
				ArrayList<Field> fields = ((DTComplex)dataType).getFields();
				if (fields == null || fields.isEmpty()) {
					// no fields
					linesWritten++;
					writeBusinessElementData(be, dataType, null, sourceSEs, target1SEs, target2SEs, writer);
				} else {
					for (Field field : fields) {
						// will be formatted as typeName.attribute
						linesWritten++;
						writeBusinessElementData(be, dataType, field, sourceSEs, target1SEs, target2SEs, writer);
					}
				}
			} else {
				linesWritten++;
				writeBusinessElementData(be, dataType, null, sourceSEs, target1SEs, target2SEs, writer);
			}

			//---------------------------------------------------
			// Write additional lines for unmapped SE/rules
			//---------------------------------------------------
			for (SemanticElement semanticElement : sourceSEs.keySet()) {
				if (m_foundSourceSes.contains(semanticElement)) {
					continue;
				}
				List<ConversionRule> ruleList = sourceSEs.get(semanticElement);
				for (ConversionRule rule : ruleList) {

					// write BE
					writeBusinessElementFields(be, dataType.getTypeName(), writer);
					writer.write(SEPARATOR);
					
					// write Source
					writeSemaniticElementRule(semanticElement.getDatatype(), null, rule, writer);
					writer.write(SEPARATOR);
					
					// write empty Target
					writeSemaniticElementRule(null, null, null, writer);
					
					writer.newLine();
					writer.flush();
				}
			}
			for (SemanticElement semanticElement : target1SEs.keySet()) {
				if (m_foundTarget1Ses.contains(semanticElement)) {
					continue;
				}
				List<ConversionRule> ruleList = target1SEs.get(semanticElement);
				for (ConversionRule rule : ruleList) {

					// write BE
					writeBusinessElementFields(be, dataType.getTypeName(), writer);
					writer.write(SEPARATOR);
					
					// write empty Source
					writeSemaniticElementRule(null, null, null, writer);
					writer.write(SEPARATOR);
					
					// write Target
					writeSemaniticElementRule(semanticElement.getDatatype(), null, rule, writer);
					
					writer.newLine();
					writer.flush();
				}
			}
			if (target2SEs != null) {
				for (SemanticElement semanticElement : target2SEs.keySet()) {
					if (m_foundTarget2Ses.contains(semanticElement)) {
						continue;
					}
					List<ConversionRule> ruleList = target2SEs.get(semanticElement);
					for (ConversionRule rule : ruleList) {

						// write BE
						writeBusinessElementFields(be, dataType.getTypeName(), writer);
						writer.write(SEPARATOR);

						// write empty Source
						writeSemaniticElementRule(null, null, null, writer);
						writer.write(SEPARATOR);

						// write empty Target 1
						writeSemaniticElementRule(null, null, null, writer);
						writer.write(SEPARATOR);

						// write Target 2
						writeSemaniticElementRule(semanticElement.getDatatype(), null, rule, writer);

						writer.newLine();
						writer.flush();
					}
				}
			}

			// put a new line between BEs
			writer.newLine();
		}
		
		return linesWritten;
	}
	
	/** write Business Element information for data type/field pair. The attribute may be a field of a data type or a dataType
	 * @throws IOException */
	private void writeBusinessElementData(MdmiBusinessElementReference be, MdmiDatatype beDataType, Field beField, 
			Map<SemanticElement, List<ConversionRule>> sourceSEs,
			Map<SemanticElement, List<ConversionRule>> target1SEs,
			Map<SemanticElement, List<ConversionRule>> target2SEs,	// optional
			BufferedWriter writer) throws IOException {
		
		
		if (sourceSEs.isEmpty() || target1SEs.isEmpty()) {
			return;
		}
		
		if (target2SEs != null &&  target2SEs.isEmpty()) {
			return;
		}
		
		// Attribute
		String attributeName = beDataType.getTypeName();
		if (beField != null) {
			// dataTypeName.fieldName
			attributeName  += "." + beField.getName();
		}
		
		// 1. Find Source 
		ConversionRule foundSourceRule = findConversionRule(be, beField, sourceSEs);
		
		// 2. Find Target(s)
		ConversionRule foundTarget1Rule = findConversionRule(be, beField, target1SEs);
		ConversionRule foundTarget2Rule = null;
		if (target2SEs != null) {
			foundTarget2Rule = findConversionRule(be, beField, target2SEs);
		}
		
		// 3.  write BE
		writeBusinessElementFields(be, attributeName, writer);
		writer.write(SEPARATOR);
		
		// 4. write Source
		writeSemaniticElementRule(beDataType, beField, foundSourceRule, writer);
		if (foundSourceRule != null) {
			m_foundSourceSes.add(foundSourceRule.getOwner());
		}
		writer.write(SEPARATOR);
		
		// 5. write Target 1
		writeSemaniticElementRule(beDataType, beField, foundTarget1Rule, writer);
		if (foundTarget1Rule != null) {
			m_foundTarget1Ses.add(foundTarget1Rule.getOwner());
		}

		// 6. write Target 2
		if (target2SEs != null) {
			writer.write(SEPARATOR);

			writeSemaniticElementRule(beDataType, beField, foundTarget2Rule, writer);
			if (foundTarget2Rule != null) {
				m_foundTarget2Ses.add(foundTarget2Rule.getOwner());
			}
		}

		writer.newLine();
		writer.flush();
		
	}

	/** Write the semantic element fields, according to the rule and/or type */
	public void writeSemaniticElementRule(MdmiDatatype beDataType, Field beField,
			ConversionRule rule, BufferedWriter writer) throws IOException {
		if (rule != null) {
			SemanticElement sourceSE = rule.getOwner();
			if (sourceSE.getDatatype().getTypeName().equals(beDataType.getTypeName())) {
				// isomorphic - show field as part of data type
				writeSemanticElementFields(sourceSE, beField, rule, writer);
			} else {
				writeSemanticElementFields(sourceSE, null, rule, writer);
			}
		} else {
			// write blanks
			writeSemanticElementFields(null, null, null, writer);
		}
	}

	
	/** Find the rule from the list of SEs/Rules that corresponds to this Business Element/field */
	public static ConversionRule findConversionRule(MdmiBusinessElementReference be, Field beField, 
			Map<SemanticElement, List<ConversionRule>> seMap) {
		
		ConversionRule foundRule = null;
		for (SemanticElement semanticElement : seMap.keySet()) {
			List<ConversionRule> ruleList = seMap.get(semanticElement);
			for (ConversionRule rule : ruleList) {
				if (doesRuleApply(be, beField, rule)) {
					foundRule = rule;
					break;
				}
			}
		}
		return foundRule;
	}

	
	/** Does this rule apply */
	private static boolean doesRuleApply(MdmiBusinessElementReference ber, Field beField, ConversionRule rule) {
		SemanticElement se = rule.getOwner();
		
		MdmiDatatype beDatatype = ber.getReferenceDatatype();
		MdmiDatatype seDatatype = se.getDatatype();
		
		// do the SE and the BE have the same data type?
		if (seDatatype.getTypeName().equals(beDatatype.getTypeName())) {
			return true;
		}

		String exp = rule.getRule();
		StringBuilder textToMatch = new StringBuilder();
		StringBuilder altTextToMatch = new StringBuilder();
		if (beField != null && exp != null && exp.length() > 0) {

			if (rule instanceof ToMessageElement) {
				// look for text in the form "set value to From_<beName>.<field>"
				textToMatch.append("set value to ").append("From_");
				textToMatch.append(ber.getName()).append('.').append(beField.getName());
				
				if (exp.contains(textToMatch.toString())) {
					return true;
				}

				// now look for text like this:
				//var source = From_DocumentID.getValue();
				//var target = value.getXValue();
				//target.setValue(source.getValue('root'));
				textToMatch.setLength(0);
				textToMatch.append("source = ").append("From_").append(ber.getName()).append(".getValue()");
				altTextToMatch.append("source.getValue('").append(beField.getName()).append("'");

				if (exp.contains(textToMatch.toString()) && exp.contains(altTextToMatch.toString())) {
					return true;
				}
								
			} else if (rule instanceof ToBusinessElement) {
				// look for text in the form "set To_<beName>.<field> to value"
				textToMatch.append("set ").append("To_");
				textToMatch.append(ber.getName()).append('.').append(beField.getName());
				textToMatch.append(" to value");
				
				if (exp.contains(textToMatch.toString())) {
					return true;
				}

				// now look for text like this:
				//var source = value.value();
				//var target = To_DocumentID.getValue();
				//target.setValue('root', source);
				textToMatch.setLength(0);
				textToMatch.append("target = ").append("To_").append(ber.getName()).append(".getValue()");
				altTextToMatch.append("target.setValue('").append(beField.getName()).append("'");

				if (exp.contains(textToMatch.toString()) && exp.contains(altTextToMatch.toString())) {
					return true;
				}
				
			} else {
				return false;
			}
		}
		
		return false;
	}
	
	

	/** Write the fields of a Business Element */
	private void writeBusinessElementFields(MdmiBusinessElementReference be, String attributeName,
			BufferedWriter writer) throws IOException {
		
		// Unique Id
		if (m_uniqueID.isSelected()) {
			writer.write(be.getUniqueIdentifier());	
			writer.write(SEPARATOR);
		}
		
		// Business Element Name
		writer.write(be.getName());
		writer.write(SEPARATOR);
		
		// Business Element Description
		if (m_description.isSelected()) {
			if (be.getDescription() != null) {
				writer.write(be.getDescription());
			} else {
				writer.write("");
			}
			writer.write(SEPARATOR);
		}
		
		// Data Type Name
		writer.write(attributeName);
		writer.write(SEPARATOR);
		
		// Code List
		if (m_codeList.isSelected()) {
			if (be.getEnumValueSet() != null) {
				writer.write(be.getEnumValueSet());
			} else {
				writer.write("");
			}
			writer.write(SEPARATOR);
		}
	}

	/** Write the fields of a Semantic Element. If a null SE is provided, blanks will be written */
	private void writeSemanticElementFields(SemanticElement se, Field field, ConversionRule rule,
			BufferedWriter writer) throws IOException {
		
		// Get syntax node
		Node syntaxNode = (se==null) ? null : se.getSyntaxNode();
		
		//1. Semantic Element Name
		writer.write(se == null ? "" : se.getName());	
		writer.write(SEPARATOR);
		
		// 2. Semantic Element Description
		if (m_description.isSelected()) {
			if (se != null && se.getDescription() != null) {
				writer.write(se.getDescription());
			} else {
				writer.write("");
			}
			writer.write(SEPARATOR);
		}
		
		// 3. Data Type Name
		if (se != null && se.getDatatype() != null) {
			writer.write(se.getDatatype().getTypeName());
			if (field != null) {
				// dataTypeName.fieldName
				writer.write(".");
				writer.write(field.getName());
			}
		} else {
			writer.write("");
		}
		writer.write(SEPARATOR);
		
		// 4. Absolute Location
		if (m_absoluteLocation.isSelected()) {
			// walk parent
			StringBuilder buf = new StringBuilder();
			Node parentNode = syntaxNode;
			while (parentNode != null) {
				// add location
				if (buf.length() > 0) buf.insert(0, PATH_SEPARATOR);
				buf.insert(0, parentNode.getLocation());
				
				parentNode = parentNode.getParentNode();
			}
			writer.write(buf.toString());
			writer.write(SEPARATOR);
		}

		// 5. Absolute Name
		if (m_absoluteName.isSelected()) {
			// walk parent
			StringBuilder buf = new StringBuilder();
			Node parentNode = syntaxNode;
			while (parentNode != null) {
				// add name
				if (buf.length() > 0) buf.insert(0, NAME_SEPARATOR);
				buf.insert(0, parentNode.getName());
				
				parentNode = parentNode.getParentNode();
			}
			writer.write(buf.toString());
			writer.write(SEPARATOR);
		}
		
		// 6. Cardinality
		if (m_cardinality.isSelected()) {
			StringBuilder buf = new StringBuilder();
			if (syntaxNode != null) {
				int min = syntaxNode.getMinOccurs();
				int max = syntaxNode.getMaxOccurs();
				if (min == 0 && max == Integer.MAX_VALUE) {
					buf.append("unbounded");
				} else {
					// min..max
					buf.append(min).append("..");
					if (max != Integer.MAX_VALUE) {
						buf.append(max);
					} 
				}
			}
			writer.write(buf.toString());
			writer.write(SEPARATOR);
		}
		
		// 7. Code List
		if (m_codeList.isSelected()) {
			if (se != null && se.getEnumValueSet() != null) {
				writer.write(se.getEnumValueSet());
			} else {
				writer.write("");
			}
			writer.write(SEPARATOR);
		}
		
		// 8. Java Script
		if (rule != null && rule.getRule() != null && !rule.getRule().isEmpty()) {
			// script can contain special characters, so enclose in quotes
			writer.write("\"");
			writer.write(rule.getRule());
			writer.write("\"");
		} else {
			writer.write("");
		}
		writer.write(SEPARATOR);
		
		// 9. Computed Value
		if (m_computedValue.isSelected()) {
			if (se != null && se.getComputedValue() != null && se.getComputedValue().getExpression() != null) {
				writer.write(se.getComputedValue().getExpression());
			} else {
				writer.write("");
			}
			writer.write(SEPARATOR);
		}
	}
    
	/** Find all semantic elements, and the conversions rules that reference this business element.
	 * 
	 * @param ber
	 * @param seIsSource
	 * @return
	 */
    public static Map<SemanticElement, List<ConversionRule>> findSemanticElements(MdmiBusinessElementReference ber, 
    		MessageGroup group, boolean seIsSource) {

    	HashMap<SemanticElement, List<ConversionRule>> semanticElementMap = new HashMap<SemanticElement, List<ConversionRule>>();

		String uniqueIdentifier = ber.getUniqueIdentifier();
		if (uniqueIdentifier == null) {
			return semanticElementMap;
		}
		
		// search SEs
		for (MessageModel messageModel : group.getModels()) {
			for (SemanticElement se : messageModel.getElementSet().getSemanticElements()) {
				ConversionRule rule = null;
				
				if (seIsSource) {
					// SE is the Source
					Collection<ToBusinessElement> toBElist = se.getFromMdmi();
					for( ToBusinessElement toBe : toBElist ) {
						MdmiBusinessElementReference referencedBE = toBe.getBusinessElement();
						if( uniqueIdentifier.equals(referencedBE.getUniqueIdentifier()) ) {
							rule = toBe;
						}
					}
				} else {
					// SE is the target
					Collection<ToMessageElement> toMdmiList = se.getToMdmi();
					for( ToMessageElement toMdmi : toMdmiList ) {
						MdmiBusinessElementReference referencedBE = toMdmi.getBusinessElement();
						if( uniqueIdentifier.equals(referencedBE.getUniqueIdentifier()) ) {
							rule = toMdmi;
						}
					}
					
				}
				
				if (rule != null) {
					// add to map
					List<ConversionRule> listOfSErules = semanticElementMap.get(se);
					if (listOfSErules == null) {
						// first rule for this SE
						listOfSErules = new ArrayList<ConversionRule>();
						semanticElementMap.put(se, listOfSErules);
					}
					
					// add to list for the SE
					listOfSErules.add(rule);
				}
			}
			
		}

		
		return semanticElementMap;
    }
	
	/** Get Business Element for Semantic Element (if there is only one).
	 * Returns null if there aren't any, or there are more than one */
	public static MdmiBusinessElementReference getBusinessElement(MessageGroup group, SemanticElement se) {
		MdmiBusinessElementReference be = null;
		Collection<ToBusinessElement> fromMdmiElements = se.getFromMdmi();
		Collection<ToMessageElement> toMdmiElements = se.getToMdmi();

		if (fromMdmiElements != null && toMdmiElements != null &&
			fromMdmiElements.size() == 1 && toMdmiElements.size() == 1) {
			for (ToBusinessElement toBe : fromMdmiElements) {
				for (ToMessageElement toMe : toMdmiElements) {
					if (toBe.getBusinessElement() == toMe.getBusinessElement()) {
						be = toBe.getBusinessElement();
					}
				}
			}
		}
		
		return be;
	}

    
    /** Get a comparator for Business Elements (by name) */
	public static Comparator<MdmiBusinessElementReference> getMdmiBusinessElementReferenceComparator() {
		return new Comparator<MdmiBusinessElementReference> () {
			@Override
			public int compare(MdmiBusinessElementReference ber1, MdmiBusinessElementReference ber2) {
				String name1 = ber1.getName() == null ? "" : ber1.getName();
				String name2 = ber2.getName() == null ? "" : ber2.getName();
				return name1.compareTo(name2);
			}
		};
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == m_browseSourceBtn) {
			File file = getFile(true);
			if (file != null) {
				m_sourceFileName.setText(file.getAbsolutePath());
			}
			
		} else if (source == m_browseTargetBtn[0]) {
			File file = getFile(true);
			if (file != null) {
				m_targetFileName[0].setText(file.getAbsolutePath());
			}
			
		} else if (source == m_browseTargetBtn[1]) {
			File file = getFile(true);
			if (file != null) {
				m_targetFileName[1].setText(file.getAbsolutePath());
			}
			
		} else if (source == m_browseOutputBtn) {
			File file = getFile(false);
			if (file != null) {
				m_outputFileName.setText(file.getAbsolutePath());
			}

		} else if (source == m_uniqueID) {
			m_pref.putBooleanValue(UNIQUEID_STATE, m_description.isSelected());
			
		} else if (source == m_description) {
			// save state
			m_pref.putBooleanValue(DESCRIPTION_STATE, m_description.isSelected());
			
		} else if (source == m_codeList) {
			// save state
			m_pref.putBooleanValue(CODELLIST_STATE, m_codeList.isSelected());
			
		} else if (source == m_absoluteLocation) {
			// save state
			m_pref.putBooleanValue(ABS_LOCATION_STATE, m_absoluteLocation.isSelected());
			
		} else if (source == m_absoluteName) {
			m_pref.putBooleanValue(ABS_NAME_STATE, m_absoluteName.isSelected());
			
		} else if (source == m_cardinality) {
			m_pref.putBooleanValue(CARDINALITY_STATE, m_cardinality.isSelected());
			
		} else if (source == m_computedValue) {
			// save state
			m_pref.putBooleanValue(COMPUTED_VALUE_STATE, m_computedValue.isSelected());
			
		} else if (source == m_sourceToTarget) {
			// make sure one of the two is checked
			if (!m_sourceToTarget.isSelected()) {
				m_targetToSource.setSelected(true);
			}
			// save states
			m_pref.putBooleanValue(SOURCE_TO_TARGET_STATE, m_sourceToTarget.isSelected());
			m_pref.putBooleanValue(TARGET_TO_SOURCE_STATE, m_targetToSource.isSelected());
			
		} else if (source == m_targetToSource) {
			// make sure one of the two is checked
			if (!m_targetToSource.isSelected()) {
				m_sourceToTarget.setSelected(true);
			}
			// save states
			m_pref.putBooleanValue(SOURCE_TO_TARGET_STATE, m_sourceToTarget.isSelected());
			m_pref.putBooleanValue(TARGET_TO_SOURCE_STATE, m_targetToSource.isSelected());
			
			
		} else if (source == m_generateBtn) {
			generateOutputData();
		}
		
		// enable generate buttons if we have files
		m_generateBtn.setEnabled(false);
		
		if (!m_sourceFileName.getText().trim().isEmpty() && !m_targetFileName[0].getText().trim().isEmpty() &&
				!m_outputFileName.getText().trim().isEmpty()) {
			m_generateBtn.setEnabled(true);
		}
		
		// disable Target-To-Source if we have a second target file
		m_sourceToTarget.setEnabled(true);
		m_targetToSource.setEnabled(true);
		if (!m_targetFileName[1].getText().trim().isEmpty()) {
			m_sourceToTarget.setEnabled(false);
			m_targetToSource.setEnabled(false);
			m_sourceToTarget.setSelected(true);
			m_targetToSource.setSelected(false);
		}
	}
	
	

	/**
     * Starting point for application.
     */
    public static void main(String[] args) {

        try {
            String lAndF = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(lAndF);
        } catch (Exception e) {
        }

        TraceabilityTool editor = new TraceabilityTool();
        editor.setVisible(true);
    }
}
