package org.openhealthtools.mdht.mdmi.editor.map.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.UserPreferences;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.common.components.CursorManager;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.StatusPanel;
import org.openhealthtools.mdht.mdmi.editor.map.console.LinkedObject;
import org.openhealthtools.mdht.mdmi.editor.map.console.ValidationErrorLink;
import org.openhealthtools.mdht.mdmi.editor.map.editor.EditorPanel;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MdmiModelTree;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.validate.ModelInfo;

// Utility for reading/writing Referent Index in CSV form
public class ReferentIndexToCSV extends SpreadSheetModelBuilder {


	private static final String NAME = "Name";
	private static final String DESCRIPTION = "Description";
	private static final String READ_ONLY = "Readonly";
	private static final String UID = "Unique Identifier";
	private static final String REFERENCE = "Reference";
	private static final String DATA_TYPE = "Reference Datatype";


	/**
     * Resource for localization
     */
    private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");

    /**
     * last csv file used
     */
    public static final String LAST_CSV_FILE = "lastCSVfile";
    public static final String CSV_SEPARATOR = "CSVSeparator";

    public ReferentIndexToCSV() {
		super(null);
	}

    private static UserPreferences getUserPreferences() {
        String appName = SystemContext.getApplicationName();
        UserPreferences preferences = UserPreferences.getInstance(appName, null);
        return preferences;
    }
    
    /**
     * Export Referent Indices to a spread sheet
     */
    public void exportReferentIndex() {
        Frame applicationFrame = SystemContext.getApplicationFrame();

        // prompt for separator 
		ReferentIndexToCSV.TokenSelector sel = new ReferentIndexToCSV.TokenSelector(applicationFrame);
		int rc = sel.display(applicationFrame);
		if (rc != BaseDialog.OK_BUTTON_OPTION) {
			return;
		}
		char separator = sel.getToken();
		
        // set cursor
        CursorManager cm = CursorManager.getInstance(applicationFrame);
        cm.setWaitCursor();
        try {
       		// tell user to close edits
    		EditorPanel editor = SelectionManager.getInstance().getEntityEditor();
    		if (editor.isAnyEntityOpen()) {
    			JOptionPane.showMessageDialog(applicationFrame, s_res.getString("ReferentIndexToCSV.saveMessage"),
    					s_res.getString("ReferentIndexToCSV.saveTitle"), JOptionPane.INFORMATION_MESSAGE);
    			return;
    		}
    		
    		// open preferences to re-use directory name
    		UserPreferences preferences = getUserPreferences();
    		String lastFileName = preferences.getValue(LAST_CSV_FILE, null);

    		// create a file chooser
    		JFileChooser chooser = CSVFileReader.getCSVFileChooser(lastFileName);
    		// Select a CSV File
    		chooser.setDialogTitle(s_res.getString("ReferentIndexToCSV.csvFileTitle"));

            // get the file
            File file = ModelIOUtilities.getFileToWriteTo(lastFileName, chooser);
            if (file != null) {
                lastFileName = file.getAbsolutePath();
            	
            	exportReferentIndex(file, separator);

    			// save file name
    			preferences.putValue(LAST_CSV_FILE, lastFileName);
            }
            
        } catch (FileNotFoundException e) {
            SelectionManager.getInstance().getStatusPanel().writeException(e);
		} catch (IOException e) {
            SelectionManager.getInstance().getStatusPanel().writeException(e);
		} finally {
            cm.restoreCursor();
        }
    }
    
    /** Modify the Referent Index based on the contents of a CSV file. The user will be prompted to provide
     * the file.
     * 
     * The first line of the file provides the names of the fields of the Business Element Reference
     * to be changed.
     *
     * The first column identifies the Business Element Reference to be modified; it must be either
     * 'Unique Identifier', for the UID, or 'Name', for the current name.
     * 
     * The additional columns provide the data to be changed. The column name (in the first line) will designate
     * which field (e.g. "Description").
     * 
     * @param sep	Field separator in the file
     */
    public void reviseReferentIndex(char sep) {
    	Frame applicationFrame = SystemContext.getApplicationFrame();
    	// set cursor
    	CursorManager cm = CursorManager.getInstance(applicationFrame);
    	cm.setWaitCursor();
    	StatusPanel statusPanel = SelectionManager.getInstance().getStatusPanel();
		try {
    		// tell user to save edits
    		EditorPanel editor = SelectionManager.getInstance().getEntityEditor();
    		if (editor.isAnyEntityModified()) {
    			JOptionPane.showMessageDialog(applicationFrame, s_res.getString("ReferentIndexToCSV.saveMessage"),
    					s_res.getString("ReferentIndexToCSV.saveTitle"), JOptionPane.INFORMATION_MESSAGE);
    			return;
    		}
    		
    		// open preferences to re-use directory name
    		UserPreferences preferences = getUserPreferences();
    		String lastFileName = preferences.getValue(LAST_CSV_FILE, null);

    		// create a file chooser
    		JFileChooser chooser = CSVFileReader.getCSVFileChooser(lastFileName);
    		chooser.setDialogTitle(s_res.getString("ReferentIndexToCSV.csvFileTitle"));

    		// prompt for a CSV file
    		int opt = chooser.showOpenDialog(applicationFrame);
    		if (opt == JFileChooser.APPROVE_OPTION) {
    			File file = chooser.getSelectedFile();
    			lastFileName = file.getAbsolutePath();


    			Collection<ModelInfo> valResults = reviseReferentIndex(file, sep);

                // show errors
                for (ModelInfo results : valResults) {
                	String preText = "";
                	if (results.getObject() instanceof MdmiBusinessElementReference) {
                		preText = ((MdmiBusinessElementReference)results.getObject()).getName() + ": field ";
                	}

            		LinkedObject linkedObject = new ValidationErrorLink(results);
            		statusPanel.writeConsoleLink(preText, linkedObject, results.getMessage());
                }
    			

    			// save file name
    			preferences.putValue(LAST_CSV_FILE, lastFileName);
    		}
    		
    	} catch (FileNotFoundException e) {
            statusPanel.writeException(e);
		} catch (IOException e) {
            statusPanel.writeException(e);
		} finally {
    		cm.restoreCursor();
    	}
    }
    
    
    /** Modify the Referent Index based on the contents of the provided CSV file.
     * 
     * The first line of the file provides the names of the fields of the Business Element Reference
     * to be changed.
     *
     * The first column identifies the Business Element Reference to be modified; it can be either
     * 'Unique Identifier', for the UID, or 'Name', for the current name.
     * 
     * The additional columns provide the data to be changed. The column name (in the first line) will designate
     * which field (e.g. "Description").
     */
    public Collection<ModelInfo> reviseReferentIndex(File file, char sep) throws IOException {

        SelectionManager selectionManager = SelectionManager.getInstance();
        MdmiModelTree entitySelector = selectionManager.getEntitySelector();
		CSVFileReader reader = new CSVFileReader(file);
		reader.setSeparatorToken(sep);

		// error information -  File and Line number
		String errorLine = new String();
		List<ModelInfo> valResults = new ArrayList<ModelInfo>();

		int lineNo = 0;
		
		// Fields
		String identifier = null;

		// Read first line of the file
		List<String> columnNames = reader.getNextLine();
		
		// First line is the header
		if (columnNames == null) {
			lineNo++;

			errorLine = FileAndLine(file, lineNo);
			valResults.add(new ModelInfo(null, "", 
					errorLine + s_res.getString("ReferentIndexToCSV.headerExpected")));
			return valResults;
		}
		
		// assume we'll be replacing by UID (otherwise by name)
		boolean findByUID = true;
		
		// Scan header to find data
		List<Method[]> getSetMethods = ClassUtil.getMethodPairs(MdmiBusinessElementReference.class);
		
		
		ArrayList<Method[]> getSetMethodList = new ArrayList<Method[]>();
		
		int column = 0;
		for (String attributeName : columnNames)
		{
			// find matching getXXX method
			String condensedName = attributeName.replaceAll(" ", "");
			String getMethodNameToMatch = "get" + condensedName;
			String isMethodNameToMatch = "is" + condensedName;
			
			Method[] foundPair = null;
			for (Method[] getSetPair : getSetMethods) {
				// look for a method with a name 'getXxxx' or 'isXxx'
				String existingGetMethodName = getSetPair[0].getName();
				if (existingGetMethodName.equalsIgnoreCase(getMethodNameToMatch) ||
						existingGetMethodName.equalsIgnoreCase(isMethodNameToMatch)) {
					foundPair = getSetPair;
					break;
				}
			}
			
			// no method found
			if (foundPair == null) {
				errorLine = FileAndLine(file, lineNo);
				valResults.add(new ModelInfo(null, "", errorLine + "No Business Element Reference attribute named '" +
						attributeName + "'"));
				return valResults;
				
			} else if (column == 0 && !UID.equalsIgnoreCase(attributeName) &&
					!NAME.equalsIgnoreCase(attributeName)) {
				// first column must be UID or Name
				errorLine = FileAndLine(file, lineNo);
				valResults.add(new ModelInfo(null, "", errorLine + "Invalid column name '" + attributeName + "'. "
						+ "The first column must be either '" + UID + "' or '" + NAME + "'."));
				return valResults;
			
			}

			getSetMethodList.add(foundPair);
			
			column++;
		}

		List<String> stringList = null;

		while ((stringList = reader.getNextLine()) != null) {
			lineNo++;
			// skip empty lines
			if (CSVFileReader.isEmptyList(stringList)) {
				continue;
			}

			errorLine = FileAndLine(file, lineNo);

			// first field must be Unique Identifier or Name
			column = 0;
			identifier = CSVFileReader.getString(stringList, column++);
			
			// 1. Find BER
			MdmiBusinessElementReference ber = null;
			if (findByUID) {
				ber = findBusinessElementReferenceByUID(entitySelector, identifier);
			} else {
				ber = findBusinessElementReferenceByName(entitySelector, identifier);
			}
			
			if (ber == null) {
				// not found - log and move on
				Object obj = null;
				String field = "";
				if (entitySelector.getMessageGroups().size() > 0) {
					MessageGroup group = entitySelector.getMessageGroups().get(0);	
					obj = group;
					field = group.getName();
				}
				valResults.add(new ModelInfo(obj, field, errorLine +
							"Business Element '" + identifier + "' not found. Check spelling."));
				continue;
			}
			
			// do the other fields
			for (;column < stringList.size(); column++) {
				String newStringValue = CSVFileReader.getString(stringList, column);
				

				if (newStringValue.isEmpty()) {
					// skip empty cells
					continue;
				}
				
				Method getMethod = getSetMethodList.get(column)[0];
				Method setMethod = getSetMethodList.get(column)[1];

				// check for original item
				Object oldValue = null;
				try {
					oldValue = getMethod.invoke(ber);
				} catch (Exception e) {
				}

				// we may need to convert the value
				Object newValue = newStringValue;

				// we know there's only one parameter
				Class<?> param = setMethod.getParameterTypes()[0];
				if (param.equals(URI.class)) {
					newValue = URI.create(newStringValue);
				} else if (param.equals(boolean.class)) {
					newValue = Boolean.valueOf(newStringValue).booleanValue();
				} else if (param.equals(MdmiDatatype.class)) {
					// need to find the datatype
					newValue = ModelIOUtilities.findDatatype(ber.getDomainDictionaryReference().getMessageGroup().getDatatypes(),
							newStringValue);
					if (newValue == null) {
						valResults.add(new ModelInfo(ber, columnNames.get(column), errorLine + "Datatype '" +
								newStringValue + "' doesn't exist"));
						return valResults;
					}
				}

				try {
					setMethod.invoke(ber, newValue);

				} catch (Exception e) {
					valResults.add(new ModelInfo(ber, columnNames.get(column), errorLine + "Cannot invoke " +
							setMethod.getName() + ". " + e.getLocalizedMessage()));
					return valResults;
				}

				// compare the two
				if (oldValue == null || (oldValue instanceof String && ((String)oldValue).isEmpty()) ) {
					// added
					valResults.add(new ModelInfo(ber, columnNames.get(column), 
							"- Value set to '" + newStringValue + "'"));
				} else if (!oldValue.equals(newValue)) {
					String oldStringValue;
					if (oldValue instanceof MdmiDatatype) {
						oldStringValue = ((MdmiDatatype)oldValue).getTypeName();
					} else {
						oldStringValue = oldValue.toString();
					}
					// changed
					valResults.add(new ModelInfo(ber, columnNames.get(column), "- Value changed from '" + 
							oldStringValue + "' to '" + newStringValue + "'"));
				}
			}

			
			// indicate that there are un-saved changes
			selectionManager.setUpdatesPending();

			// update tree to show new name (may need to re-order)
			DefaultMutableTreeNode node = 
				selectionManager.getEntitySelector().refreshUserObject(ber);
			
			// update display to show new name
			selectionManager.getEntityEditor().refreshEditDisplay(((EditableObjectNode)node));

			// notify listeners
			selectionManager.notifyModelChangeListeners(ber);
		}
		selectionManager.notifyCollectionChangeListeners(MdmiBusinessElementReference.class);
		
		return valResults;
    }

    /** Export a ReferentIndex to CSV as: UID, Name, Description, Read-Only, Reference (URI), Reference Datatype */
	public void exportReferentIndex(File file, char separator) throws IOException {
		FileOutputStream fstream = new FileOutputStream(file);
		
		// Write File Line By Line
		DataOutputStream out = new DataOutputStream(fstream);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

        SelectionManager selectionManager = SelectionManager.getInstance();
		MdmiModelTree entitySelector = selectionManager.getEntitySelector();
        // 1. write header
        writer.write(UID);
        writer.write(separator);
        writer.write(NAME);
        writer.write(separator);
        writer.write(DESCRIPTION);
        writer.write(separator);
        writer.write(READ_ONLY);
        writer.write(separator);
        writer.write(REFERENCE);
        writer.write(separator);
        writer.write(DATA_TYPE);
        writer.newLine();
		writer.flush();
        
        // 2. Loop
		for (MessageGroup group : entitySelector.getMessageGroups()) {
			if (group.getDomainDictionary() != null) {
				for (MdmiBusinessElementReference ber : group.getDomainDictionary().getBusinessElements()) {
					if (ber.getName() != null) {
						// UID
						if (ber.getUniqueIdentifier() != null) {
							writer.write(ber.getUniqueIdentifier());
						}
						// name
						writer.write(separator);
						writer.write(ber.getName());
						writer.write(separator);
						// description
						if (ber.getDescription() != null) {
							writer.write(ber.getDescription());
						}
						// read-only
						writer.write(separator);
						writer.write(Boolean.toString(ber.isReadonly()));
						// URI
						writer.write(separator);
						if (ber.getReference() != null) {
							writer.write(ber.getReference().toString());
						}
						// Data Type
						writer.write(separator);
						if (ber.getReferenceDatatype() != null && ber.getReferenceDatatype().getTypeName() != null) {
							writer.write(ber.getReferenceDatatype().getTypeName());
						}
						
						writer.newLine();
						writer.flush();
					}
				}
			}
		}
        
        // 3. Close
		out.close();
	}

	// Find a BER with this UID
	public MdmiBusinessElementReference findBusinessElementReferenceByUID(MdmiModelTree entitySelector, String uid) {
		MdmiBusinessElementReference ber = null;
		for (MessageGroup group : entitySelector.getMessageGroups()) {
			if (group.getDomainDictionary() != null) {
				for (MdmiBusinessElementReference berInGroup : group.getDomainDictionary().getBusinessElements()) {
					if (berInGroup.getUniqueIdentifier() != null && uid.equalsIgnoreCase(berInGroup.getUniqueIdentifier())) {
						ber = berInGroup;
						break;
					}
				}
			}
		}
		return ber;
	}

	// Find a BER with this name
	public MdmiBusinessElementReference findBusinessElementReferenceByName(MdmiModelTree entitySelector, String berName) {
		MdmiBusinessElementReference ber = null;
		for (MessageGroup group : entitySelector.getMessageGroups()) {
			if (group.getDomainDictionary() != null) {
				for (MdmiBusinessElementReference berInGroup : group.getDomainDictionary().getBusinessElements()) {
					if (berInGroup.getName() != null && berName.equalsIgnoreCase(berInGroup.getName())) {
						ber = berInGroup;
						break;
					}
				}
			}
		}
		return ber;
	}
    
    public static class TokenSelector extends BaseDialog implements ActionListener, DocumentListener {
    	private JRadioButton m_commaButton = new JRadioButton("Comma (,)", false);
    	private JRadioButton m_pipeButton = new JRadioButton("Pipe (|)", false);
    	private JRadioButton m_otherButton = new JRadioButton("Other", false);
    	private JTextField m_otherText = new JTextField(2);
    	
		public TokenSelector(Frame owner) {
			super(owner, BaseDialog.OK_CANCEL_OPTION);
			setTitle("Select Separator");
			buildUI();
			
			pack(new Dimension(200,200));
		}
		
		@Override
		public void dispose() {
			m_commaButton.removeActionListener(this);
			m_pipeButton.removeActionListener(this);
			m_otherButton.removeActionListener(this);
			m_otherText.getDocument().removeDocumentListener(this);
			super.dispose();
		}
		
		public char getToken() {
			if (m_commaButton.isSelected()) {
				return ',';
			} else if (m_pipeButton.isSelected()) {
				return '|';
			} else {
				String text = m_otherText.getText().trim();
				if (text.length() > 0) {
					return text.charAt(0);
				}
			}
			return ',';
		}

		@Override
		public boolean isDataValid() {
			if (m_otherButton.isSelected()) {
				String text = m_otherText.getText().trim();
				return text.length() == 1;
			}
			return true;
		}
    	
		private void buildUI() {
			JPanel main = new JPanel(new GridBagLayout());
			add(main, BorderLayout.CENTER);
			GridBagConstraints gbc = new GridBagConstraints();
			// Please select the separator character
			//  (o) Comma (,)
			//  ( ) Pipe (|)
			//  ( ) Other [______]
			gbc.insets = Standards.getInsets();
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 2;
			gbc.weightx = 0;
			gbc.weighty = 0;
			
			String sep = getUserPreferences().getValue(CSV_SEPARATOR, ",");
			if (",".equals(sep)) {
				m_commaButton.setSelected(true);
			} else if ("|".equals(sep)) {
				m_pipeButton.setSelected(true);
			} else {
				m_otherButton.setSelected(true);
				m_otherText.setText(sep);
			}

			ButtonGroup buttons = new ButtonGroup();
			buttons.add(m_commaButton);
			buttons.add(m_pipeButton);
			buttons.add(m_otherButton);
			
			m_commaButton.addActionListener(this);
			m_pipeButton.addActionListener(this);
			m_otherButton.addActionListener(this);
			m_otherText.getDocument().addDocumentListener(this);
			
			main.add(new JLabel("Please select the separator character"), gbc);
			
			gbc.gridy++;
			gbc.insets.bottom = 0;
			main.add(m_commaButton, gbc);
			
			gbc.gridy++;
			main.add(m_pipeButton, gbc);
			
			gbc.gridy++;
			gbc.weighty = 1;
			gbc.gridwidth = 1;
			main.add(m_otherButton, gbc);
			gbc.gridx++;
			gbc.weightx = 1;
			gbc.insets.left = 0;
			main.add(m_otherText, gbc);

			setDirty(true);
		}

		@Override
		protected void okButtonAction() {
			// save selection
			String sep = "" + getToken();
			getUserPreferences().putValue(CSV_SEPARATOR, sep);
			super.okButtonAction();
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			setDirty(true);
			
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			setDirty(true);
			
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			setDirty(true);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			setDirty(true);
		}
    }
}
