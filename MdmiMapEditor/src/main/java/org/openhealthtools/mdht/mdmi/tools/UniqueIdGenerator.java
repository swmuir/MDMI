package org.openhealthtools.mdht.mdmi.tools;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
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
import javax.swing.filechooser.FileFilter;

import org.openhealthtools.mdht.mdmi.editor.common.UniqueID;
import org.openhealthtools.mdht.mdmi.editor.common.UserPreferences;
import org.openhealthtools.mdht.mdmi.editor.common.components.WrappingDisplayText;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MdmiDomainDictionaryReference;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.validate.ModelValidationResults;
import org.openhealthtools.mdht.mdmi.model.xmi.direct.reader.MapBuilderXMIDirect;
import org.openhealthtools.mdht.mdmi.model.xmi.direct.writer.XMIWriterDirect;

/*
 * Tool to generate unique ids on all Business Element References
 */
public class UniqueIdGenerator extends JFrame implements ActionListener {

	private static final String XMI_EXTENSION = ".xmi";
	private static final String LAST_FILE_OPENED = "LastFileOpened";
	
	/** Resource for localization */
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.tools.Local");
	
	private JTextField m_inputFileName = new JTextField(35);
	private JTextField m_outputFileName = new JTextField(35);
	private JButton    m_browseInputBtn = new JButton("...");
	private JButton    m_browseOutputBtn = new JButton("...");
	// Generate IDs button
	private JButton    m_generateBtn = new JButton(s_res.getString("UniqueIdGenerator.generateIDs"));
	
	private File m_lastFile = null;
	
	private UserPreferences m_pref = UserPreferences.getInstance("UniqueIdGenerator", null);
	
	public UniqueIdGenerator() {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle(s_res.getString("UniqueIdGenerator.title"));
		buildUI();
		
		// read last file
        String lastFileName = m_pref.getValue(LAST_FILE_OPENED, null);
        if (lastFileName != null) {
        	m_lastFile = new File(lastFileName);
        }
		pack();
	}
	
	private void buildUI() {
		getContentPane().setLayout(new BorderLayout());
		
		JPanel main = new JPanel(new GridBagLayout());
		
		// Input File:  [_______________________________] [...]
		// Output File: [_______________________________] [...]
		//
		//            [Generate IDs]
		
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
		instructions.setText(s_res.getString("UniqueIdGenerator.instructions"));
		main.add(instructions, gbc);
		gbc.gridwidth = 1;
		
		gbc.gridy++;

		// Input File: [__________________________][...]
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		main.add(new JLabel(s_res.getString("UniqueIdGenerator.inputFile")), gbc);
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
		main.add(new JLabel(s_res.getString("UniqueIdGenerator.outputFile")), gbc);
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

    /**
     * Return a file filter that allows directories and supported files
     */
    public static class XMIFilter extends FileFilter {
        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            } else {
                return f.getName().endsWith(XMI_EXTENSION);
            }
        }

        @Override
        public String getDescription() {
            // XMI  files
            return "XMI Files";
        }
    }
	

    @Override
	public void dispose() {
    	// save
        if (m_lastFile != null) {
             m_pref.putValue(LAST_FILE_OPENED, m_lastFile.getAbsolutePath());
        }
        
		// cleanup
    	m_browseInputBtn.removeActionListener(this);
    	m_browseOutputBtn.removeActionListener(this);
    	m_generateBtn.removeActionListener(this);
    	
		super.dispose();
	}
    
    /** Browse for an input or output file */
    private File getFile(boolean read) {
        // create a file chooser
        JFileChooser chooser = new JFileChooser(m_lastFile);
        chooser.setFileFilter(new XMIFilter());
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        // pre-set
        if (m_lastFile != null) {
            if (m_lastFile.exists()) {
                chooser.setSelectedFile(m_lastFile);
            }
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
        	file = chooser.getSelectedFile();
        	m_lastFile = file;
        }
        
        return file;
    }
    
    /** Open the input file and produce the output file */
    private void generateUniqueIDs() {
    	try {
    		// check input file
    		String inFileName = m_inputFileName.getText().trim();
    		if (inFileName.isEmpty()) {
    			// No input file has been specified."
    			JOptionPane.showMessageDialog(this, s_res.getString("UniqueIdGenerator.noInputMessage"),
    			s_res.getString("UniqueIdGenerator.noInputTitle"), JOptionPane.ERROR_MESSAGE);
    			return;
    		}
    		File inFile = new File(inFileName);
    		if (!inFile.exists()) {
    			// The file, fileName, does not exist.
    			// Please specify an existing file.
    			JOptionPane.showMessageDialog(this, 
    					MessageFormat.format(s_res.getString("UniqueIdGenerator.fileDoesntExistMessage"), inFile.getName()),
    					s_res.getString("UniqueIdGenerator.fileDoesntExistTitle"),
    					JOptionPane.ERROR_MESSAGE);
    			return;
    		}

    		// check output file
    		String outFileName = m_outputFileName.getText().trim();
    		if (outFileName.isEmpty()) {
    			// No output file has been specified."
    			JOptionPane.showMessageDialog(this, s_res.getString("UniqueIdGenerator.noOutputMessage"),
    			s_res.getString("UniqueIdGenerator.noOutputTitle"), JOptionPane.ERROR_MESSAGE);
    			return;
    		}
    		if (!outFileName.endsWith(XMI_EXTENSION)) {
    			// tack on ".xmi" extension
    			outFileName += XMI_EXTENSION;
    		}
    		File outFile = new File(outFileName);
    		if (outFile.exists()) {
    			// The file, fileName, already exists.
    			// Are you sure you want to overwrite it?
    			int opt = JOptionPane.showConfirmDialog(this, 
    					MessageFormat.format(s_res.getString("UniqueIdGenerator.fileExistsMessage"), outFile.getName()),
    					s_res.getString("UniqueIdGenerator.fileExistsTitle"),
    					JOptionPane.YES_NO_OPTION);
    			
    			if (opt != JOptionPane.YES_OPTION) {
    				return;
    			}
    		}

    		// read
    		ModelValidationResults results = new ModelValidationResults();
    		List<MessageGroup> groups = MapBuilderXMIDirect.build(inFile, results);

    		// count
    		int idsChanged = 0;
    		int bizElemCount = 0;
    		
    		// modify
    		for (MessageGroup group : groups) {
    			MdmiDomainDictionaryReference dictionary = group.getDomainDictionary();
    			if (dictionary != null) {
    				for (MdmiBusinessElementReference bizElem : dictionary.getBusinessElements()) {
    					bizElemCount++;
    					if (!UniqueID.isUUID(bizElem.getUniqueIdentifier())) {
    						String uuid = UniqueID.getUUID();
    						bizElem.setUniqueIdentifier(uuid);
    						idsChanged++;
    					}
    					// make sure all BEs and Datatypes are read-only
    					bizElem.setReadonly(true);
    					MdmiDatatype datatype = bizElem.getReferenceDatatype();
    					if (datatype != null) {
    						datatype.setReadonly(true);
    					}
    				}
    			}
    		}


    		// write
    		XMIWriterDirect.write(outFile, groups);
    		
    		// Finished updating:
    		//  X Business Element References were updated
    		//  Y Business Element References were unchanged
    		int idsUnchanged = bizElemCount - idsChanged;
    		String message = MessageFormat.format( idsUnchanged == 0 ?
    				s_res.getString("UniqueIdGenerator.updateCompleteMessage") :
    				s_res.getString("UniqueIdGenerator.updateCompleteMessageSomeUnChanged"),
    				idsChanged, idsUnchanged);

    		JOptionPane.showMessageDialog(this, message,
    				s_res.getString("UniqueIdGenerator.updateCompleteTitle"),
    				JOptionPane.INFORMATION_MESSAGE);
    		
    	} catch (Exception ex) {
    		JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    	}
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
			generateUniqueIDs();
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

        UniqueIdGenerator editor = new UniqueIdGenerator();
        editor.setVisible(true);
    }
}
