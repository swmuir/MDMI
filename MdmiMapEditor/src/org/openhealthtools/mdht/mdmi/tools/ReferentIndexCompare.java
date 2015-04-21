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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
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
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.validate.ModelValidationResults;
import org.openhealthtools.mdht.mdmi.model.xmi.direct.reader.MapBuilderXMIDirect;

import com.google.common.base.Equivalence;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;

/*
 * Tool to compare the Referent Index of two models
 */
public class ReferentIndexCompare extends JFrame implements ActionListener {

	
	private static final String XMI_EXTENSION = "xmi";
	private static final String HTML_EXTENSION = "html";
	
	// persisted values
	private static final String LAST_FILE_OPENED = "LastFileOpened";
	private static final String LAST_FILE_WRITTEN = "LastFileWritten";
	
	/** Resource for localization */
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.tools.Local");
	
	private JTextField m_sourceFileName[] =  new JTextField[] {new JTextField(35), new JTextField(35)};
	private JButton    m_browseSourceBtn[] = new JButton[] { new JButton("..."), new JButton("...")};
	
	private JTextField m_outputFileName = new JTextField(35);
	private JButton    m_browseOutputBtn = new JButton("...");

	// Generate button
	private JButton    m_generateBtn = new JButton(s_res.getString("ReferentIndexCompare.generateBtn"));
	
//	private File [] m_srcFile = new File[2];

	private File m_lastInputFile = null;
	private File m_lastOutputFile = null;
	
	// Helper classes
	private static BusinessElementDifference s_businessElementDifference = new BusinessElementDifference();
	private static DatatypeDifference s_datatypeDifference = new DatatypeDifference();
	private static FieldDifference s_fieldDifference = new FieldDifference();
	
	
	private UserPreferences m_pref = UserPreferences.getInstance("ReferentIndexCompare", null);
	
	/** field separator */
	public static char SEPARATOR = ',';
	
	public ReferentIndexCompare() {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle(s_res.getString("ReferentIndexCompare.title"));
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
		
		// File 1:         [_______________________________] [...]
		// File 2:         [_______________________________] [...]
		// Output File:    [_______________________________] [...]
		//
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
		
		// Compare the referent index from two files
		WrappingDisplayText instructions = new WrappingDisplayText();
		instructions.setText(s_res.getString("ReferentIndexCompare.instructions"));
		main.add(instructions, gbc);
		gbc.gridwidth = 1;
		
		gbc.gridy++;

		// Source File 1: [__________________________][...]
		// Source File 2: [__________________________][...]
		for (int i=0; i<2; i++) {
			gbc.insets.bottom = 0;
			gbc.weightx = 0;
			gbc.fill = GridBagConstraints.NONE;
			main.add(new JLabel(s_res.getString("ReferentIndexCompare.sourceFile1")), gbc);
			gbc.gridx++;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			main.add(m_sourceFileName[i], gbc);
			gbc.gridx++;
			gbc.fill = GridBagConstraints.NONE;
			gbc.weightx = 0;
			gbc.insets.left = 0;
			main.add(m_browseSourceBtn[i], gbc);
			gbc.insets.left = 10;

			gbc.gridx = 0;
			gbc.gridy++;

			gbc.insets.bottom = 10;
		}

		

		// Output File: [__________________________][...]
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		main.add(new JLabel(s_res.getString("ReferentIndexCompare.outputFile")), gbc);
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
		
		gbc.gridx = 0;
		gbc.gridy++;
		
		
		getContentPane().add(main, BorderLayout.CENTER);
		
		//        [Generate]
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		buttons.add(m_generateBtn);
		// disable until input and output files defined
		m_generateBtn.setEnabled(false);
		getContentPane().add(buttons, BorderLayout.SOUTH);
		

    	m_browseSourceBtn[0].addActionListener(this);
    	m_browseSourceBtn[1].addActionListener(this);
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
    	m_browseSourceBtn[0].removeActionListener(this);
    	m_browseSourceBtn[0].removeActionListener(this);
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
        	// if not found, use input file with HTML extension
        	if (lastFile == null && m_lastInputFile != null) {
        		String inputFileName = m_lastInputFile.getAbsolutePath();
        		// replace .xmi with .html
        		inputFileName = inputFileName.replace(".xmi", ".html");
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
            chooser.setFileFilter(new FileNameExtensionFilter("HTML file", HTML_EXTENSION));
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
    	try {
    		// check input files
    		File [] srcFile = new File[2];

    		for (int i=0; i<2; i++) {
    			String srcFileName = m_sourceFileName[i].getText().trim();
    			// validate file
    			if (srcFileName.isEmpty()) {
    				// No input file has been specified."
    				JOptionPane.showMessageDialog(this, s_res.getString("ReferentIndexCompare.noInputMessage"),
    						s_res.getString("ReferentIndexCompare.noInputTitle"), JOptionPane.ERROR_MESSAGE);
    				return;
    			}
    			srcFile[i] = new File(srcFileName);
    			if (!srcFile[i].exists()) {
    				// The file, fileName, does not exist.
    				// Please specify an existing file.
    				JOptionPane.showMessageDialog(this, 
    						MessageFormat.format(s_res.getString("ReferentIndexCompare.fileDoesntExistMessage"), srcFileName),
    						s_res.getString("ReferentIndexCompare.fileDoesntExistTitle"),
    						JOptionPane.ERROR_MESSAGE);
    				return;
    			}
        		
    			// check for duplicate names
    			for (int j=0; j<i; j++) {
    				if (srcFile[i].equals(srcFile[j])) {
    					//The input files must be different.
    					JOptionPane.showMessageDialog(this, s_res.getString("ReferentIndexCompare.duplicateInputMessage"),
    							s_res.getString("ReferentIndexCompare.duplicateInputTitle"), JOptionPane.ERROR_MESSAGE);
    					return;
    				}
    			}
    		}

    		// check output file
    		String outFileName = m_outputFileName.getText().trim();
    		if (outFileName.isEmpty()) {
    			// No output file has been specified."
    			JOptionPane.showMessageDialog(this, s_res.getString("ReferentIndexCompare.noOutputMessage"),
    			s_res.getString("ReferentIndexCompare.noOutputTitle"), JOptionPane.ERROR_MESSAGE);
    			return;
    		}
    		if (!outFileName.endsWith(HTML_EXTENSION)) {
    			// tack on ".html" extension
    			outFileName += "." + HTML_EXTENSION;
    		}
    		File outFile = new File(outFileName);
    		if (outFile.exists()) {
    			// The file, fileName, already exists.
    			// Are you sure you want to overwrite it?
    			int opt = JOptionPane.showConfirmDialog(this, 
    					MessageFormat.format(s_res.getString("ReferentIndexCompare.fileExistsMessage"), outFile.getName()),
    					s_res.getString("ReferentIndexCompare.fileExistsTitle"),
    					JOptionPane.YES_NO_OPTION);
    			
    			if (opt != JOptionPane.YES_OPTION) {
    				return;
    			}
    		}
    		

    		// read
    		ModelValidationResults results = new ModelValidationResults();
    		List<MessageGroup> src1Groups = MapBuilderXMIDirect.build(srcFile[0], results);
    		List<MessageGroup> src2Groups = MapBuilderXMIDirect.build(srcFile[1], results);
    		
    		// there should only be one message group
    		StringBuilder warning = new StringBuilder();
    		if (src1Groups.size() != 1) {
    			// The file, {0}, contains multiple message groups. Only the first will be used.
				warning.append(MessageFormat.format(s_res.getString("ReferentIndexCompare.multipleGroupsMessage"),
						srcFile[0].getName()));
    		}
    		if (src2Groups.size() != 1) {
    			// The file, {0}, contains multiple message groups. Only the first will be used.
    			if (warning.length() > 0) {
    				warning.append("\n");
    			}
				warning.append(MessageFormat.format(s_res.getString("ReferentIndexCompare.multipleGroupsMessage"),
						srcFile[1].getName()));
    		}
    		if (warning.length() > 0) {
    			JOptionPane.showMessageDialog(this, warning,
    					s_res.getString("ReferentIndexCompare.multipleGroupsTitle"),
    					JOptionPane.INFORMATION_MESSAGE);
    		}

    		//---------------------------------------
    		// open output file
    		//---------------------------------------
    		FileOutputStream fstream = new FileOutputStream(outFile);
    		DataOutputStream out = new DataOutputStream(fstream);
    		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
    		
    		//---------------------------------------
    		// Compare Left, Right
    		//---------------------------------------

    		String index1Name = srcFile[0].getName();
    		String index2Name =  srcFile[1].getName();
    		Collection<MdmiBusinessElementReference> beList1 = src1Groups.get(0).getDomainDictionary().getBusinessElements();
    		Collection<MdmiBusinessElementReference> beList2 = src2Groups.get(0).getDomainDictionary().getBusinessElements();
    		compare(index1Name, beList1, index2Name, beList2, writer);

    		//---------------------------------------
    		// Close
    		//---------------------------------------
    		writer.close();
    		out.close();
    		fstream.close();
    		
    		// Finished updating. Would you like to open FILE to view the data
    		String message = MessageFormat.format(s_res.getString("ReferentIndexCompare.analysisMessage"),
    				outFile.getPath());

    		int option = JOptionPane.showConfirmDialog(this, message, s_res.getString("ReferentIndexCompare.analysisTitle"),
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


    /** Compare two referent index collections. Produce HTML 
     * @throws IOException */
	public static void compare(String nameLeft, Collection<MdmiBusinessElementReference> beListLeft, 
			String nameRight, Collection<MdmiBusinessElementReference> beListRight,
			BufferedWriter writer) throws IOException {
		
		// create two maps, keyed by UID
		Map<String, MdmiBusinessElementReference> leftMap = extractMap(beListLeft);
		Map<String, MdmiBusinessElementReference> rightMap = extractMap(beListRight);
		
		// perform the difference
		MapDifference<String, MdmiBusinessElementReference> difference = Maps.difference(leftMap, rightMap,
				new MdmiBusinessElementReferenceEquivalence());
		
		Map<String, MdmiBusinessElementReference> entriesOnlyOnLeft = difference.entriesOnlyOnLeft();
		Map<String, MdmiBusinessElementReference> entriesOnlyOnRight = difference.entriesOnlyOnRight();
		Map<String, ValueDifference<MdmiBusinessElementReference>> entriesDiffering = difference.entriesDiffering();

		//---------------------------------------
		// Write Header information
		//--------------------------------------
		writer.write("<html><head></head>");
		writer.newLine();
		writer.write("<body>");
		writer.newLine();

		writer.write("<h1>");
		writer.write("Compare ");
		writer.write(nameLeft);
		writer.write(" to ");
		writer.write(nameRight);
		writer.write("</h1>");
		writer.newLine();

		//---------------------------------------
		// Start Table
		//--------------------------------------
		writer.write("<table border=\"1\">");
		writer.newLine();
		
		// write Source and Target files in heading
		writer.write("<tr valign=\"top\">");
		writer.write("<th>");
		writer.write(nameLeft);
		writer.write("</th>");
		writer.write("<th>");
		writer.write(nameRight);
		writer.write("</th>");
		writer.write("</tr>");
		
		// left only

		if (!entriesOnlyOnLeft.isEmpty()) {
			writer.write("<tr><td colspan=\"2\"><b>Entries only in " + nameLeft + "</b></td></tr>");
			writer.newLine();
			for (MdmiBusinessElementReference ber : entriesOnlyOnLeft.values()) {

				writeBERsToTable(ber, null, writer);
				writer.newLine();
				writer.flush();
			}
		}

		// right only
		if (!entriesOnlyOnRight.isEmpty()) {
			writer.write("<tr><td colspan=\"2\"><b>Entries only in " + nameRight + "</b></td></tr>");
			writer.newLine();
			for (MdmiBusinessElementReference ber : entriesOnlyOnRight.values()) {

				writeBERsToTable(null, ber, writer);
				writer.newLine();
				writer.flush();
			}
		}

		// write each difference
		if (!entriesDiffering.isEmpty()) {
			writer.write("<tr><td colspan=\"2\"><b>Entries that differ</b></td></tr>");
			writer.newLine();
			for (ValueDifference<MdmiBusinessElementReference> dif : entriesDiffering.values()) {

				writeBERsToTable(dif.leftValue(), dif.rightValue(), writer);
				writer.newLine();
				writer.flush();
			}
		}
		writer.write("</table>");
		
		//-----------------------------------------------
		// Close up
		//-----------------------------------------------
		
		writer.write("</body>");
		writer.newLine();
		writer.write("</html>");
		writer.newLine();
		writer.flush();
	}

	/** Write two MDMI BusinessElements as an HTMl table
	 * @throws IOException */
	private static void writeBERsToTable(MdmiBusinessElementReference leftBER, MdmiBusinessElementReference rightBER,
			BufferedWriter writer) throws IOException {

		List<String> berLeftString = new ArrayList<String>();
		List<String> berRightString = new ArrayList<String>();
		
		// Convert to string array
		if (leftBER != null) {
			berLeftString = s_businessElementDifference.getString(leftBER);
		}
		if (rightBER != null) {
			berRightString = s_businessElementDifference.getString(rightBER);
		}
		
		StringBuilder leftBuf = new StringBuilder();
		StringBuilder rightBuf = new StringBuilder();
		
		// compare each entry in the arrays
		int i;
		for (i=0; i<berLeftString.size() && i < berRightString.size(); i++) {
			String left = berLeftString.get(i);
			String right = berRightString.get(i);
			
			if (left.equals(right)) {
				leftBuf.append(left);
				rightBuf.append(right);
			} else {
				// make bold, blue
				leftBuf.append("<b><font color=\"blue\">").append(left).append("</font></b>");
				rightBuf.append("<b><font color=\"blue\">").append(right).append("</font></b>");
			}
			leftBuf.append("<br>");
			rightBuf.append("<br>");
		}
		
		// add the remainder
		for (int j=i; j<berLeftString.size(); j++) {
			String left = berLeftString.get(j);
			// make bold, green
			leftBuf.append("<b><font color=\"green\">").append(left).append("</font></b>");
			leftBuf.append("<br>");
		}
		
		for (int j=i; j<berRightString.size(); j++) {
			String right = berRightString.get(j);
			// make bold, red
			rightBuf.append("<b><font color=\"red\">").append(right).append("</font></b>");
			rightBuf.append("<br>");
		}
		

		writer.write("<tr valign=\"top\">");
		
		// left side
		writer.write("<td>");
		writer.write(leftBuf.toString());
		writer.write("</td>");
		writer.newLine();

		// right side
		writer.write("<td>");
		writer.write(rightBuf.toString());
		writer.write("</td>");
		
		writer.write("</tr>");
		writer.newLine();
	}

	
	/** Convert a collection of MDMIBusinessElements into a map, keyed by UID */
	private static Map<String, MdmiBusinessElementReference> extractMap(Collection<MdmiBusinessElementReference> businessElements) {
		Map<String, MdmiBusinessElementReference> map = new HashMap<String, MdmiBusinessElementReference>();
		
		for (MdmiBusinessElementReference ber : businessElements) {
			map.put(ber.getUniqueIdentifier(), ber);
		}
		
		return map;
	}
	
	// Action Listener
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == m_browseSourceBtn[0]) {
			File file = getFile(true);
			if (file != null) {
				m_sourceFileName[0].setText(file.getAbsolutePath());
			}
			
		} else if (source == m_browseSourceBtn[1]) {
			File file = getFile(true);
			if (file != null) {
				m_sourceFileName[1].setText(file.getAbsolutePath());
			}
			
		} else if (source == m_browseOutputBtn) {
			File file = getFile(false);
			if (file != null) {
				m_outputFileName.setText(file.getAbsolutePath());
			}
			
			
		} else if (source == m_generateBtn) {
			generateOutputData();
		}
		
		// enable generate buttons if we have files
		m_generateBtn.setEnabled(false);
		
		if (!m_sourceFileName[0].getText().trim().isEmpty() && !m_sourceFileName[1].getText().trim().isEmpty() &&
				!m_outputFileName.getText().trim().isEmpty()) {
			m_generateBtn.setEnabled(true);
		}
		
	}

	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Helper class for comparing two business elements */
	public static class MdmiBusinessElementReferenceEquivalence extends Equivalence<MdmiBusinessElementReference> {

		@Override
		protected boolean doEquivalent(MdmiBusinessElementReference ber1, MdmiBusinessElementReference ber2) {
			
			// Compare Name, Description, URI, Enum(s) and Datatype
			boolean equals = s_businessElementDifference.compare(ber1, ber2);
			return equals;
		}
		

		@Override
		protected int doHash(MdmiBusinessElementReference ber) {
			// use GUID
			return ber.getUniqueIdentifier().hashCode();
		}
		
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Abstract class for comparing and printing */
	public static abstract class Difference<V> {
		
		/** Compare two objects. Returns the name of the attribute where they differ */
		public abstract boolean compare(V v1, V v2);
		
		/** Get a string representation of the object */
		public abstract List<String> getString(V v1);
		

		
		/** Compare two objects that could be null */
		public static boolean equals(Object o1, Object o2) {
			if (o1 == null && o2 == null) {
				// both null
				return true;
			}
			else if (o1 == null || o2 == null) {
				// one or the other, but not both are null
				return false;
			}
			return o1.equals(o2);
		}

		
		/** get attribute as "Name = Value".  */
		public static String getAttribute(String name, String value) {
			StringBuilder buf = new StringBuilder();
			buf.append(name);
			buf.append(" = ");
			buf.append(value);
			
			return buf.toString();
		}
		
		/** get an indentation (4 spaces) */
		public static String getIndent(int indent) {
			StringBuilder buf = new StringBuilder();
			// indentation
			for (int i=0; i<indent; i++) {
				// 4 spaces
				for (int j=0; j<4; j++) {
					buf.append("&nbsp;");
				}
			}
			return buf.toString();
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Difference Class for comparing and printing Fields */
	public static class FieldDifference extends Difference<Field> {

		@Override
		public boolean compare(Field o1, Field o2) {

			if (o1 == null && o2 == null) {
				// both null
				return true;
			} else if (o1 == null || o2 == null) {
				// one or the other, but not both are null
				return false;
			}

			// Name
			if (!equals(o1.getName(), o2.getName())) {
				return false;
			}
			// Data type
			boolean diff = s_datatypeDifference.compare(o1.getDatatype(), o2.getDatatype());
			if (!diff) {
				return false;
			}
			// Description
			if (!equals(o1.getDescription(), o2.getDescription())) {
				return false;
			}
			return true;
		}



		/** Get a string representation of the object */
		@Override
		public List<String> getString(Field o1) {
			ArrayList<String> stringList = new ArrayList<String>();
			// Name
			stringList.add(getAttribute("Name", o1.getName()));
			
			// Data Type
			stringList.add(getAttribute("Datatype", o1.getDatatype().getTypeName()));
			
			// Description
			if (o1.getDescription() != null && o1.getDescription().length() > 0) {
				stringList.add(getAttribute("Description", o1.getDescription()));
			}
			
			return stringList;
		}
		
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////

	/** Difference Class for comparing and printing datatypes */
	public static class DatatypeDifference extends Difference<MdmiDatatype> {

		@Override
		public boolean compare(MdmiDatatype o1, MdmiDatatype o2) {

			if (o1 == null && o2 == null) {
				// both null
				return true;
			} else if (o1 == null || o2 == null) {
				// one or the other, but not both are null
				return false;
			}
			
			// Class
			if (!o1.getClass().getName().equals(o2.getClass().getName())) {
				return false;
			}
			// Name
			if (!equals(o1.getTypeName(), o2.getTypeName())) {
				return false;
			}
			// Description
			if (!equals(o1.getDescription(), o2.getDescription())) {
				return false;
			}
			// Fields
			if (o1 instanceof DTComplex) {
				ArrayList<Field> fields1 = ((DTComplex)o1).getFields();
				ArrayList<Field> fields2 = ((DTComplex)o2).getFields();
				// check length
				if (fields1.size() != fields2.size()) {
					return false;
				}
				// check each element
				for (int i=0; i<fields1.size(); i++) {
					boolean diff = s_fieldDifference.compare(fields1.get(i), fields2.get(i));
					if (!diff) {
						return false;
					}
				}
			}
			return true;
		}


		/** Get a string representation of the object */
		@Override
		public List<String> getString(MdmiDatatype o1) {
			ArrayList<String> stringList = new ArrayList<String>();
			// Name
			stringList.add(getAttribute("Name", o1.getTypeName()));

			// Class
			stringList.add(getAttribute("Class", o1.getClass().getName()));
			
			// Description
			if (o1.getDescription() != null && o1.getDescription().length() > 0) {
				stringList.add(getAttribute("Description", o1.getDescription()));
			}
			
			// Fields
			if (o1 instanceof DTComplex) {
				ArrayList<Field> fields = ((DTComplex)o1).getFields();
				if (fields.size() > 0) {
					// write each field
					for (int i=0; i<fields.size(); i++) {
						// all attributes on one line, separated by commas
						StringBuilder fieldBuf = new StringBuilder();
						Field subField = fields.get(i);

						fieldBuf.append("Field ").append(i+1).append(":").append(getIndent(1));
						
						List<String> fieldList = s_fieldDifference.getString(subField);
						for (int j=0; j<fieldList.size(); j++) {
							if (j > 0) {
								// extra indent
								fieldBuf.append(",").append(getIndent(1));
							}
							fieldBuf.append(fieldList.get(j));
						}
						
						stringList.add(fieldBuf.toString());
					}
				}
			}
			return stringList;
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	

	/** Difference Class for comparing and printing BusinessElementReference */
	public static class BusinessElementDifference extends Difference<MdmiBusinessElementReference> {

		@Override
		public boolean compare(MdmiBusinessElementReference o1, MdmiBusinessElementReference o2) {

			// Compare Name, Description, URI, Enum(s) and Datatype

			// Name
			if (!equals(o1.getName(), o2.getName())) {
				return false;
			}

			// Description
			if (!equals(o1.getDescription(), o2.getDescription())) {
				return false;
			}

			// URI
			if (!equals(o1.getReference(), o2.getReference())) {
				return false;
			}
			
			// Datatype - name and class
			boolean diff = s_datatypeDifference.compare(o1.getReferenceDatatype(), o2.getReferenceDatatype());
			if (!diff) {
				return false;
			}

			// Enum Value Set
			if (!equals(o1.getEnumValueSet(), o2.getEnumValueSet())) {
				return false;
			}

			// Enum Value Set Field
			if (!equals(o1.getEnumValueSetField(), o2.getEnumValueSetField())) {
				return false;
			}

			// Enum Value Field
			if (!equals(o1.getEnumValueField(), o2.getEnumValueField())) {
				return false;
			}

			// Enum Value Description Field
			if (!equals(o1.getEnumValueDescrField(), o2.getEnumValueDescrField())) {
				return false;
			}
			
			return true;
		}


		/** Get a string representation of the object */
		@Override
		public List<String> getString(MdmiBusinessElementReference o1) {
			ArrayList<String> stringList = new ArrayList<String>();
			
			// Name
			stringList.add(getAttribute("Name", o1.getName()));
						
			// UID
			if (o1.getUniqueIdentifier() != null && o1.getUniqueIdentifier().length() > 0) {
				stringList.add(getAttribute("UID", o1.getUniqueIdentifier()));
			}

			// Description
			if (o1.getDescription() != null && o1.getDescription().length() > 0) {
				stringList.add(getAttribute("Description", o1.getDescription()));
			}

			// URI
			if (o1.getReference() != null) {
				stringList.add(getAttribute("Reference", o1.getReference().toString()));
			}

			// Datatype 
			if (o1.getReferenceDatatype() != null) {
				String heading = "ReferenceDatatype:";
				stringList.add(heading);
				
				List<String> datatypeList = s_datatypeDifference.getString(o1.getReferenceDatatype());
				for (String val : datatypeList) {
					// extra indent
					stringList.add(getIndent(1) + val);
				}
			}
			
			return stringList;
			
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

        ReferentIndexCompare editor = new ReferentIndexCompare();
        editor.setVisible(true);
    }
}
