package org.openhealthtools.mdht.mdmi.tools;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
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
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.MessageModel;
import org.openhealthtools.mdht.mdmi.model.Node;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.ToBusinessElement;
import org.openhealthtools.mdht.mdmi.model.ToMessageElement;
import org.openhealthtools.mdht.mdmi.model.validate.ModelValidationResults;
import org.openhealthtools.mdht.mdmi.model.xmi.direct.reader.MapBuilderXMIDirect;

/*
 * Tool to generate XPath information on all isomorphic Business Element References
 */
public class XPathGenerator extends JFrame implements ActionListener {

	private static final String XMI_EXTENSION = "xmi";
	private static final String CSV_EXTENSION = "csv";
	private static final String LAST_FILE_OPENED = "LastFileOpened";
	private static final String LAST_FILE_WRITTEN = "LastFileWritten";
	
	/** Resource for localization */
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.tools.Local");
	
	private JTextField m_inputFileName = new JTextField(35);
	private JTextField m_outputFileName = new JTextField(35);
	private JButton    m_browseInputBtn = new JButton("...");
	private JButton    m_browseOutputBtn = new JButton("...");
	// Generate IDs button
	private JButton    m_generateBtn = new JButton(s_res.getString("XPathGenerator.generateIDs"));

	private File m_lastInputFile = null;
	private File m_lastOutputFile = null;
	
	private UserPreferences m_pref = UserPreferences.getInstance("XPathGenerator", null);
	
	/** field separator */
	public static char SEPARATOR = ',';	// TODO: Future allow user to pick
	
	public XPathGenerator() {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle(s_res.getString("XPathGenerator.title"));
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
		
		// Input File:  [_______________________________] [...]
		// Output File: [_______________________________] [...]
		//
		//            [Produce XPath]
		
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
		// Assign a unique identifier to all Business Element References in the specified file.
		// Any Business Element Reference that already has a unique identifier will not be changed.
		WrappingDisplayText instructions = new WrappingDisplayText();
		instructions.setText(s_res.getString("XPathGenerator.instructions"));
		main.add(instructions, gbc);
		gbc.gridwidth = 1;
		
		gbc.gridy++;

		// Input File: [__________________________][...]
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		main.add(new JLabel(s_res.getString("XPathGenerator.inputFile")), gbc);
		gbc.gridx++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		main.add(m_inputFileName, gbc);
		gbc.gridx++;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		gbc.insets.left = 0;
		main.add(m_browseInputBtn, gbc);
		gbc.insets.left = 10;
		
		gbc.insets.top = 0;
		gbc.gridx = 0;
		gbc.gridy++;

		// Output File: [__________________________][...]
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		main.add(new JLabel(s_res.getString("XPathGenerator.outputFile")), gbc);
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
		
		getContentPane().add(main, BorderLayout.CENTER);
		
		//        [Generate]
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttons.add(m_generateBtn);
		getContentPane().add(buttons, BorderLayout.SOUTH);
		

    	m_browseInputBtn.addActionListener(this);
    	m_browseOutputBtn.addActionListener(this);
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
    	m_browseInputBtn.removeActionListener(this);
    	m_browseOutputBtn.removeActionListener(this);
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
    
    /** Open the input file and produce the output file */
    private void createXPathData() {
    	try {
    		// check input file
    		String inFileName = m_inputFileName.getText().trim();
    		if (inFileName.isEmpty()) {
    			// No input file has been specified."
    			JOptionPane.showMessageDialog(this, s_res.getString("XPathGenerator.noInputMessage"),
    			s_res.getString("XPathGenerator.noInputTitle"), JOptionPane.ERROR_MESSAGE);
    			return;
    		}
    		File inFile = new File(inFileName);
    		if (!inFile.exists()) {
    			// The file, fileName, does not exist.
    			// Please specify an existing file.
    			JOptionPane.showMessageDialog(this, 
    					MessageFormat.format(s_res.getString("XPathGenerator.fileDoesntExistMessage"), inFile.getName()),
    					s_res.getString("XPathGenerator.fileDoesntExistTitle"),
    					JOptionPane.ERROR_MESSAGE);
    			return;
    		}

    		// check output file
    		String outFileName = m_outputFileName.getText().trim();
    		if (outFileName.isEmpty()) {
    			// No output file has been specified."
    			JOptionPane.showMessageDialog(this, s_res.getString("XPathGenerator.noOutputMessage"),
    			s_res.getString("XPathGenerator.noOutputTitle"), JOptionPane.ERROR_MESSAGE);
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
    					MessageFormat.format(s_res.getString("XPathGenerator.fileExistsMessage"), outFile.getName()),
    					s_res.getString("XPathGenerator.fileExistsTitle"),
    					JOptionPane.YES_NO_OPTION);
    			
    			if (opt != JOptionPane.YES_OPTION) {
    				return;
    			}
    		}
    		
    		// open output file
    		FileOutputStream fstream = new FileOutputStream(outFile);
    		DataOutputStream out = new DataOutputStream(fstream);
    		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

    		// read
    		ModelValidationResults results = new ModelValidationResults();
    		List<MessageGroup> groups = MapBuilderXMIDirect.build(inFile, results);

            // 1. write header
            writer.write("Semantic Element");
            writer.write(SEPARATOR);
            writer.write("XPath");
            writer.write(SEPARATOR);
            writer.write("Business Element");
            writer.write(SEPARATOR);
            writer.write("UID");
            writer.newLine();
    		writer.flush();

    		// count
    		int idsWithPath = 0;

    		// sort SEs by name
			Comparator<SemanticElement> seCompare = getSemanticElementComparator();
    		
    		// modify
    		for (MessageGroup group : groups) {
				// Write group name if there are multiples
    			if (groups.size() > 1) {
					writer.write("Message Group " + group.getName());
					writer.newLine();
    			}
    			Collection<MessageModel> models = group.getModels();
				for (MessageModel model : models) {
    				if (model.getElementSet() == null) {
    					continue;
    				}
    				// Write model name if there are multiples
        			if (models.size() > 1) {
    					writer.write("Message Model " + model.getMessageModelName());
    					writer.newLine();
        			}
        			
    				List<SemanticElement> semanticElements = (List<SemanticElement>) model.getElementSet().getSemanticElements();
    				// Sort by name
    				Collections.sort(semanticElements, seCompare);
    				
					for (SemanticElement se : semanticElements) {

    					String xPath = getXPathForElement(group, se);

    		            // 2. write xPath
    					if (xPath != null && !xPath.isEmpty()) {
    						idsWithPath++;
    						
    						// Semantic Element
    						writer.write(se.getName());
    						
    						// XPath
    						writer.write(SEPARATOR);
    						writer.write(xPath);
    						
    						// write BE, if there is one
    						MdmiBusinessElementReference be = getBusinessElement(group, se);
    						if (be != null) {
        						writer.write(SEPARATOR);
        						writer.write(be.getName());
        						writer.write(SEPARATOR);
        						writer.write(be.getUniqueIdentifier());
    						}
    						
    						writer.newLine();
    					}
    		    		writer.flush();
    				}
    			}
    		}

            // 3. Close
    		writer.close();
    		out.close();
    		fstream.close();
    		
    		// Finished updating:
    		//  X Business Element paths were identified. Would you like to open FILE to view the data
    		String message = MessageFormat.format(s_res.getString("XPathGenerator.analysisMessage"), idsWithPath,
    				outFile.getPath());

    		int option = JOptionPane.showConfirmDialog(this, message, s_res.getString("XPathGenerator.analysisTitle"),
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
    		JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    	}
    }

    
    /** Get a comparator for Semantic Elements (by name) */
	public static Comparator<SemanticElement> getSemanticElementComparator() {
		return new Comparator<SemanticElement> () {
			@Override
			public int compare(SemanticElement se1, SemanticElement se2) {
				String name1 = se1.getName() == null ? "" : se1.getName();
				String name2 = se2.getName() == null ? "" : se2.getName();
				return name1.compareTo(name2);
			}
		};
	}
    
    /** Create an XPath for this business element */
	public static String getXPathForElement(MessageGroup group, SemanticElement se) {
		StringBuilder buf = new StringBuilder();
		
		// look at Syntax Nodes to get XPath
		Node syntaxNode = se.getSyntaxNode();
		
		while (syntaxNode != null) {
			// Use syntax node's parentage and location
			// e.g.  Semantic Element's node is NodeC
			//  path of NodeC: NodeA->NodeB->NodeC
			//  Xpath = locationA/locationB/locationC
			String location = syntaxNode.getLocation();
			if (location == null || location.isEmpty()) {
				// no location
				return null;
			}
			
			if (buf.length() > 0) {
				buf.insert(0, "/");
			}
			buf.insert(0, location);
			
			syntaxNode = syntaxNode.getParentNode();
			
		}
		
		return buf.toString();
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

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == m_browseInputBtn) {
			File file = getFile(true);
			if (file != null) {
				m_inputFileName.setText(file.getAbsolutePath());
			}
			
		} else if (e.getSource() == m_browseOutputBtn) {
			File file = getFile(false);
			if (file != null) {
				m_outputFileName.setText(file.getAbsolutePath());
			}
			
		} else if (e.getSource() == m_generateBtn) {
			createXPathData();
		}
		
		// enable generate button if we have files
		if (!m_inputFileName.getText().trim().isEmpty() &&
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

        XPathGenerator editor = new XPathGenerator();
        editor.setVisible(true);
    }
}
