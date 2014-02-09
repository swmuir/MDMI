package org.openhealthtools.mdht.mdmi.editor.map.tools;

import java.awt.Frame;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.UserPreferences;
import org.openhealthtools.mdht.mdmi.editor.common.components.CursorManager;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.editor.EditorPanel;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MdmiModelTree;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.validate.ModelInfo;
import org.openhealthtools.mdht.mdmi.model.validate.ModelValidationResults;

// Utility for reading/writing Referent Index in CSV form
public class ReferentIndexToCSV extends SpreadSheetModelBuilder {

	/**
     * Resource for localization
     */
    private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");

    /**
     * last csv file used
     */
    public static final String LAST_CSV_FILE = "lastCSVfile";

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
        // set cursor
        CursorManager cm = CursorManager.getInstance(applicationFrame);
        cm.setWaitCursor();
        try {
            // TODO
       		// tell user to close edits
    		EditorPanel editor = SelectionManager.getInstance().getEntityEditor();
    		if (editor.isAnyEntityOpen()) {
    			JOptionPane.showMessageDialog(applicationFrame, s_res.getString("ReferentIndexToCSV.closeMessage"),
    					s_res.getString("ReferentIndexToCSV.closeTitle"), JOptionPane.INFORMATION_MESSAGE);
    			return;
    		}
    		
    		// open preferences to re-use directory name
    		UserPreferences preferences = getUserPreferences();
    		String lastFileName = preferences.getValue(LAST_CSV_FILE, null);

    		// create a file chooser
    		JFileChooser chooser = new JFileChooser(lastFileName == null ? "." : lastFileName);
    		// Select a CSV File
    		chooser.setDialogTitle(s_res.getString("ReferentIndexToCSV.csvFileTitle"));
    		chooser.setFileFilter(new CSVFilter());
    		chooser.setAcceptAllFileFilterUsed(false);
    		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            // get the file
            File file = ModelIOUtilities.getFileToWriteTo(lastFileName, chooser);
            if (file != null) {
                lastFileName = file.getAbsolutePath();
            	
            	exportReferentIndex(file);

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
    
    public void reviseReferentIndex() {
    	Frame applicationFrame = SystemContext.getApplicationFrame();
    	// set cursor
    	CursorManager cm = CursorManager.getInstance(applicationFrame);
    	cm.setWaitCursor();
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
    		JFileChooser chooser = new JFileChooser(lastFileName == null ? "." : lastFileName);
    		// Select a CSV File
    		chooser.setDialogTitle(s_res.getString("ReferentIndexToCSV.csvFileTitle"));
    		chooser.setFileFilter(new CSVFilter());
    		chooser.setAcceptAllFileFilterUsed(false);
    		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

    		// prompt for a CSV file
    		int opt = chooser.showOpenDialog(applicationFrame);
    		if (opt == JFileChooser.APPROVE_OPTION) {
    			File file = chooser.getSelectedFile();
    			lastFileName = file.getAbsolutePath();


    			ModelValidationResults valResults = reviseReferentIndex(file);

                // show errors
                for (ModelInfo errorMsg : valResults.getErrors()) {
                    SelectionManager.getInstance().getStatusPanel().writeValidationErrorMsg("", errorMsg);
                }
    			

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
    
    public ModelValidationResults reviseReferentIndex(File file) throws IOException {

        SelectionManager selectionManager = SelectionManager.getInstance();
        MdmiModelTree entitySelector = selectionManager.getEntitySelector();
		CSVFileReader reader = new CSVFileReader(file);

		// error information -  File and Line number
		String errorLine = new String();
		ModelValidationResults valResults = new ModelValidationResults();

		int lineNo = 0;
		
		// Fields
		String berName = null;
		String revisedName = null;
		String description = null;

		// Read File Line By Line
		List<String> stringList = null;
		
		// First line is the header
		if ((stringList = reader.getNextLine()) == null) {
			lineNo++;

			errorLine = FileAndLine(file, lineNo);
			valResults.addError(null, "", errorLine + s_res.getString("ReferentIndexToCSV.headerExpected"));
			return valResults;
		}
		

		while ((stringList = reader.getNextLine()) != null) {
			lineNo++;
			// skip empty lines
			if (isEmptyList(stringList)) {
				continue;
			}

			errorLine = FileAndLine(file, lineNo);

			// BER Name  | New Name  | Description
			int column = 0;
			berName = getString(stringList, column++);
			revisedName = getString(stringList, column++);
			description = getString(stringList, column++);
			
			// 1. Find BER
			MdmiBusinessElementReference ber = findBusinessElementReference(entitySelector, berName);
			if (ber == null) {
				// not found - log and move on
				Object obj = null;
				String field = "";
				if (entitySelector.getMessageGroups().size() > 0) {
					MessageGroup group = entitySelector.getMessageGroups().get(0);	
					obj = group;
					field = group.getName();
				}
				valResults.addError(obj, field, errorLine +
							"Business Element '" + berName + "' not found. Check spelling.");
				continue;
			}
			
			// 2. Check New Name
			MdmiBusinessElementReference newBer = findBusinessElementReference(entitySelector, revisedName);
			if (newBer != null && newBer != ber) {
				// already exists
				valResults.addError(ber, ber.getName(), errorLine +
							"A Business Element named '" + newBer.getName() + "' already exists. " +
							ber.getName() + " cannot be changed");
				continue;
			}
			ber.setName(revisedName);
			
			// 3. Change Description
			ber.setDescription(description);

			
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

    // Export to CSV as BER Name, Description
	public void exportReferentIndex(File file) throws IOException {
		// TODO
		FileOutputStream fstream = new FileOutputStream(file);
		
		// Write File Line By Line
		DataOutputStream out = new DataOutputStream(fstream);
		BufferedWriter br = new BufferedWriter(new OutputStreamWriter(out));

        SelectionManager selectionManager = SelectionManager.getInstance();
		MdmiModelTree entitySelector = selectionManager.getEntitySelector();
        // 1. write header
        br.write("BER Name");
        br.write(",");
        br.write("Description");
        br.newLine();
        
        // 2. Loop
		for (MessageGroup group : entitySelector.getMessageGroups()) {
			if (group.getDomainDictionary() != null) {
				for (MdmiBusinessElementReference ber : group.getDomainDictionary().getBusinessElements()) {
					if (ber.getName() != null) {
						br.write(ber.getName());
						br.write(",");
						if (ber.getDescription() != null) {
							br.write(ber.getDescription());
						}
						br.newLine();
					}
				}
			}
		}
        
        // 3. Close
		out.close();
	}

	// Find a BER with this name
	public MdmiBusinessElementReference findBusinessElementReference(MdmiModelTree entitySelector, String berName) {
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


    public static class CSVFilter extends FileFilter {
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
        }

        @Override
        public String getDescription() {
            return "CSV Files (.csv)";
        }
    }
}
