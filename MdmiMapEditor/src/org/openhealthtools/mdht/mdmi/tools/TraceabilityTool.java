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
	
	/** Resource for localization */
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.tools.Local");
	
	private JTextField m_sourceFileName = new JTextField(35);
	private JTextField m_targetFileName = new JTextField(35);
	private JTextField m_outputFileName = new JTextField(35);
	private JButton    m_browseSourceBtn = new JButton("...");
	private JButton    m_browseTargetBtn = new JButton("...");
	private JButton    m_browseOutputBtn = new JButton("...");
	
	// Optional Fields
	private JCheckBox m_uniqueID = new JCheckBox(s_res.getString("TraceabilityTool.uniqueID"));
	private JCheckBox m_description = new JCheckBox(s_res.getString("TraceabilityTool.description"));
	private JCheckBox m_codeList = new JCheckBox(s_res.getString("TraceabilityTool.codeList"));
	private JCheckBox m_absoluteLocation = new JCheckBox(s_res.getString("TraceabilityTool.absolutePathLocation"));
	private JCheckBox m_absoluteName = new JCheckBox(s_res.getString("TraceabilityTool.absolutePathName"));
	private JCheckBox m_cardinality = new JCheckBox(s_res.getString("TraceabilityTool.cardinality"));
	private JCheckBox m_computedValue = new JCheckBox(s_res.getString("TraceabilityTool.computedValue"));
	
	// Generate buttons
	private JButton    m_generateBtn = new JButton(s_res.getString("TraceabilityTool.generateBtn"));

	private File m_lastInputFile = null;
	private File m_lastOutputFile = null;
	
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
		
		// Map File 1:  [_______________________________] [...]
		// Map File 2:  [_______________________________] [...]
		// Output File: [_______________________________] [...]
		//
		//  - Optional Fields-------------------------
		// | [x] Description     [x] Location         |
		//  ------------------------------------------
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

		// Map File 1: [__________________________][...]
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		main.add(new JLabel(s_res.getString("TraceabilityTool.inputFile1")), gbc);
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
		gbc.gridx = 0;
		gbc.gridy++;

		// Map File 2: [__________________________][...]
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		main.add(new JLabel(s_res.getString("TraceabilityTool.inputFile2")), gbc);
		gbc.gridx++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		main.add(m_targetFileName, gbc);
		gbc.gridx++;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		gbc.insets.left = 0;
		main.add(m_browseTargetBtn, gbc);
		gbc.insets.left = 10;
		
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
		main.add(optionalFields, gbc);
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
    	m_browseTargetBtn.addActionListener(this);
    	m_browseOutputBtn.addActionListener(this);
    	
    	m_uniqueID.addActionListener(this);
		m_description.addActionListener(this);
		m_codeList.addActionListener(this);
		m_absoluteLocation.addActionListener(this);
		m_absoluteName.addActionListener(this);
		m_cardinality.addActionListener(this);
		m_computedValue.addActionListener(this);
		
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
    	m_browseTargetBtn.removeActionListener(this);
    	m_browseOutputBtn.removeActionListener(this);
    	
    	m_uniqueID.removeActionListener(this);
		m_description.removeActionListener(this);
		m_codeList.removeActionListener(this);
		m_absoluteLocation.removeActionListener(this);
		m_absoluteName.removeActionListener(this);
		m_cardinality.removeActionListener(this);
		m_computedValue.removeActionListener(this);
		
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
    private void generateOutputData(String srcFileName, String targetFileName) {
    	try {
    		// check input files
    		File srcFile = new File(srcFileName);
    		File targetFile = new File(targetFileName);

    		for (int i=0; i<2; i++) {
    			// validate file
    			String inFileName = (i==0) ? srcFileName :  targetFileName;
    			File inputFile = (i==0) ? srcFile :  targetFile;
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
    		if (srcFile.equals(targetFile)) {
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
    		List<MessageGroup> targetGroups = MapBuilderXMIDirect.build(targetFile, results);
    		
    		// there should only be one message group
    		StringBuilder warning = new StringBuilder();
    		if (srcGroups.size() != 1) {
    			// The file, {0}, contains multiple message groups. Only the first will be used.
				warning.append(MessageFormat.format(s_res.getString("TraceabilityTool.multipleGroupsMessage"),
						srcFileName));
    		}
    		if (targetGroups.size() != 1) {
    			// The file, {0}, contains multiple message groups. Only the first will be used.
    			if (warning.length() > 0) {
    				warning.append("\n");
    			}
				warning.append(MessageFormat.format(s_res.getString("TraceabilityTool.multipleGroupsMessage"),
						targetFileName));
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
    		writer.write("Source: ");
    		writer.write(srcFile.getName());
    		writer.newLine();
    		writer.write("Target: ");
    		writer.write(targetFile.getName());
    		writer.newLine();
    		writer.flush();
    		int beCount = generateOutput(srcGroups.get(0), targetGroups.get(0), writer);


    		writer.newLine();
    		
    		//---------------------------------------
    		// Generate Target->Source
    		//---------------------------------------
    		writer.write("Source: ");
    		writer.write(targetFile.getName());
    		writer.newLine();
    		writer.write("Target: ");
    		writer.write(srcFile.getName());
    		writer.newLine();
    		writer.flush();
    		int beCount2 = generateOutput(targetGroups.get(0), srcGroups.get(0), writer);

    		//---------------------------------------
    		// Close
    		//---------------------------------------
    		writer.close();
    		out.close();
    		fstream.close();
    		
    		// Finished updating:
    		//  X Business Elements were identified. Would you like to open FILE to view the data
    		String message = MessageFormat.format(s_res.getString("TraceabilityTool.analysisMessage"), Math.max(beCount, beCount2),
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
    
	private int generateOutput(MessageGroup sourceGroup, MessageGroup targetGroup, BufferedWriter writer)
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

		// Source and Target
		String [] directions = {"Source", "Target"};
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

		// Sort by name
		Collections.sort(businessElements, beCompare);

		for (MdmiBusinessElementReference be : businessElements) {

			// find semantic elements that reference this BER as a source
			Map<SemanticElement, List<ConversionRule>> sourceSEs = findSemanticElements(be, sourceGroup, true);
			if (sourceSEs.isEmpty()) {
				continue;
			}
			
			// find semantic elements that reference this BER as a target
			Map<SemanticElement, List<ConversionRule>> targetSEs = findSemanticElements(be, targetGroup, false);
			if (targetSEs.isEmpty()) {
				continue;
			}
			
			// If dataType is complex, write an entry for each attribute of the data type
			MdmiDatatype dataType = be.getReferenceDatatype();
			if (dataType instanceof DTComplex) {
				ArrayList<Field> fields = ((DTComplex)dataType).getFields();
				if (fields == null || fields.isEmpty()) {
					// no fields
					linesWritten += writeBusinessElementData(be, sourceSEs, targetSEs, dataType, null, writer);
				} else {
					for (Field field : fields) {
						// will be formatted as typeName.attribute
						linesWritten += writeBusinessElementData(be, sourceSEs, targetSEs, dataType, field, writer);
					}
				}
			} else {
				linesWritten += writeBusinessElementData(be, sourceSEs, targetSEs, dataType, null, writer);
			}

			// put a new line between BEs
			writer.newLine();
		}
		
		return linesWritten;
	}


	/** write Business Element information for data type. The attribute may be a the field of a data type or a dataType
	 * @throws IOException */
	private int writeBusinessElementData(MdmiBusinessElementReference be, Map<SemanticElement, List<ConversionRule>> sourceSEs,
			Map<SemanticElement, List<ConversionRule>> targetSEs,
			MdmiDatatype beDataType, Field beField, BufferedWriter writer) throws IOException {

		
		int linesWritten = 0;
		
		if (sourceSEs.isEmpty() || targetSEs.isEmpty()) {
			return linesWritten;
		}
		
		// Attribute
		String attributeName = beDataType.getTypeName();
		if (beField != null) {
			// dataTypeName.fieldName
			attributeName  += "." + beField.getName();
		}
		
		//----------------------------------------------
		// First, look at each source element/rule.
		//----------------------------------------------
		for (SemanticElement sourceSE : sourceSEs.keySet()) {
			MdmiDatatype seDatatype = sourceSE.getDatatype();
			
			// need to look at rules
			List<ConversionRule> sourceRules = sourceSEs.get(sourceSE);
			for (ConversionRule sourceRule : sourceRules) {
				// Business Element Data
				writeBusinessElementFields(be, attributeName, writer);
				
				// extra
				writer.write(SEPARATOR);
				
				// Source Semantic Element
				Field seField = null;
				if (seDatatype == beDataType) {
					// isomorphic
					seField = beField;
				}
				
				writeSemanticElementFields(sourceSE, seField, sourceRule, writer);


				// Target Semantic Element
				//--------------------------------------
				// Find Target
				//--------------------------------------
				ConversionRule targetRule = findTargetConversionRule(be, sourceSE, beField, sourceRule, targetSEs);

				if (targetRule != null) {
					SemanticElement targetSE = targetRule.getOwner();

					// extra
					writer.write(SEPARATOR);
					seField = null;
					if (targetSE.getDatatype() == beDataType) {
						// isomorphic
						seField = beField;
					}
					writeSemanticElementFields(targetSE, seField, targetRule, writer);
					

					// remove it
					List<ConversionRule> list =  targetSEs.get(targetRule.getOwner());
					list.remove(targetRule);
				}

				linesWritten++;
				writer.newLine();
				writer.flush();
			}
		}
		
		// add Target Semantic Elements if BE wan't traced
		if (linesWritten == 0) {
			for (SemanticElement targetSE : targetSEs.keySet()) {
				List<ConversionRule> targetRules = targetSEs.get(targetSE);
				for (ConversionRule targetRule: targetRules) {

					// Business Element Data
					writeBusinessElementFields(be, attributeName, writer);

					// Source Semantic Element (blanks)
					writer.write(SEPARATOR);
					writeSemanticElementFields(null, null, null, writer);

					// Target Semantic Element
					Field seField = null;
					if (targetSE.getDatatype() == beDataType) {
						// isomorphic
						seField = beField;
					}
					writer.write(SEPARATOR);
					writeSemanticElementFields(targetSE, seField, targetRule, writer);


					writer.newLine();
					writer.flush();
				}

			}
		}
		
		return linesWritten;
	}
	
	/** Find the corresponding target SE for this source SE */
	// From Jeffrey Klann
	//	Generating a BE to SE rule:
	//
	//		- Same datatypes: No code needed.
	//		- Both complex datatypes: Existing wizard works.
	//		- BE is a complex data type and SE is a simple type (e.g., String):
	//		var source = From_PatientID.getValue();
	//		var target = value.getXValue();
	//		target.setValue(source.getValue('name-of-field'));
	//
	//
	//	Generating SE to BE rule:
	//		- Same datatypes: No code needed.
	//		- Both complex datatypes: Existing wizard works.
	//		- BE is a complex type and SE is a simple type (e.g., String):
	//		var source = value.value();
	//		var target = To_ProblemCode.getValue();
	//		target.setValue('code', source);
	//
	private static ConversionRule findTargetConversionRule(MdmiBusinessElementReference ber,
			SemanticElement sourceSE, Field field,  ConversionRule sourceRule,
			Map<SemanticElement, List<ConversionRule>> targetSEs) {
		MdmiDatatype srcDatatype = sourceSE.getDatatype();
		
		for (SemanticElement targetSE : targetSEs.keySet()) {
			MdmiDatatype targetDatatype = targetSE.getDatatype();
			// if targetSE has same data type as sourceSe - that's all we need
			List<ConversionRule> targetRules = targetSEs.get(targetSE);
			for (ConversionRule targetRule: targetRules) {
				if (srcDatatype.getTypeName().equals(targetDatatype.getTypeName())) {
					return targetRule;
				}
				
				String exp = targetRule.getRule();
				// look for text in the form "set value to From_<beName>.<field>"
				if (field != null && exp != null && exp.length() > 0) {
					String textToMatch = "set value to From_" + ber.getName() + "." + field.getName();
					if (exp.contains(textToMatch)) {
						return targetRule;
					}
				}
			}
			
			// TODO: we need to parse the source and target rules to find the data we need
		}
		
		return null;
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
				if (buf.length() > 0) buf.insert(0, File.separatorChar);
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
				if (buf.length() > 0) buf.insert(0, '.');
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
		if (e.getSource() == m_browseSourceBtn) {
			File file = getFile(true);
			if (file != null) {
				m_sourceFileName.setText(file.getAbsolutePath());
			}
			
		} else if (e.getSource() == m_browseTargetBtn) {
			File file = getFile(true);
			if (file != null) {
				m_targetFileName.setText(file.getAbsolutePath());
			}
			
		} else if (e.getSource() == m_browseOutputBtn) {
			File file = getFile(false);
			if (file != null) {
				m_outputFileName.setText(file.getAbsolutePath());
			}

		} else if (e.getSource() == m_uniqueID) {
			m_pref.putBooleanValue(UNIQUEID_STATE, m_description.isSelected());
			
		} else if (e.getSource() == m_description) {
			// save state
			m_pref.putBooleanValue(DESCRIPTION_STATE, m_description.isSelected());
			
		} else if (e.getSource() == m_codeList) {
			// save state
			m_pref.putBooleanValue(CODELLIST_STATE, m_codeList.isSelected());
			
		} else if (e.getSource() == m_absoluteLocation) {
			// save state
			m_pref.putBooleanValue(ABS_LOCATION_STATE, m_absoluteLocation.isSelected());
			
		} else if (e.getSource() == m_absoluteName) {
			m_pref.putBooleanValue(ABS_NAME_STATE, m_absoluteName.isSelected());
			
		} else if (e.getSource() == m_cardinality) {
			m_pref.putBooleanValue(CARDINALITY_STATE, m_cardinality.isSelected());
			
		} else if (e.getSource() == m_computedValue) {
			// save state
			m_pref.putBooleanValue(COMPUTED_VALUE_STATE, m_computedValue.isSelected());
			
		} else if (e.getSource() == m_generateBtn) {
			generateOutputData( m_sourceFileName.getText().trim(), m_targetFileName.getText().trim());
		}
		
		// enable generate buttons if we have files
		m_generateBtn.setEnabled(false);
		
		if (!m_sourceFileName.getText().trim().isEmpty() && !m_targetFileName.getText().trim().isEmpty() &&
				!m_outputFileName.getText().trim().isEmpty()) {
			m_generateBtn.setEnabled(true);
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
