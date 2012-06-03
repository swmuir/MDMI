/*******************************************************************************
* Copyright (c) 2012 Firestar Software, Inc.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Firestar Software, Inc. - initial API and implementation
*
* Author:
*     Sally Conway
*
*******************************************************************************/
package org.openhealthtools.mdht.mdmi.editor.map.tools;

import java.awt.Frame;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.stream.XMLStreamException;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.UserPreferences;
import org.openhealthtools.mdht.mdmi.editor.common.components.CursorManager;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.MapEditor;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.console.LinkedObject;
import org.openhealthtools.mdht.mdmi.editor.map.tree.DomainDictionaryReferenceNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MdmiModelTree;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MessageGroupNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MessageSyntaxModelNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.NewObjectInfo;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SyntaxNodeNode;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.DTSPrimitive;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.MapBuilderCSV;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MdmiDomainDictionaryReference;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.MessageSyntaxModel;
import org.openhealthtools.mdht.mdmi.model.Node;
import org.openhealthtools.mdht.mdmi.model.syntax.XSDReader;
import org.openhealthtools.mdht.mdmi.model.validate.ModelInfo;
import org.openhealthtools.mdht.mdmi.model.validate.ModelValidationResults;
import org.openhealthtools.mdht.mdmi.model.xmi.direct.reader.MapBuilderXMIDirect;
import org.openhealthtools.mdht.mdmi.model.xmi.direct.writer.XMIWriterDirect;


/** A collection of methods for reading and writing model data */
public class ModelIOUtilities {
	/** Resource for localization */
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");

	/** last directory used */
	public static final String LAST_FILE_OPENED	= "openFileDir";
	
	public static final String XMI_Extension = ".xmi";
	public static final String CSV_Extension = ".csv";
	public static final String XSD_Extension = ".xsd";

	public static final String[] SupportedExtensions = {
		XMI_Extension,
		CSV_Extension
	};
	
	public static boolean fileNameEndsWith(File file, String extension) {
		return file.getName().toLowerCase().endsWith(extension);
	}
	
	public static boolean supportedFileName(File file) {
		for (String extension : SupportedExtensions) {
			if (fileNameEndsWith(file, extension)) {
				return true;
			}
		}
		return false;
	}
	
	private static UserPreferences getUserPreferences() {
		String appName = SystemContext.getApplicationName();
		UserPreferences preferences = UserPreferences.getInstance(appName, null);
		return preferences;
	}

	/** Save the model data to a file */
	public static boolean writeModelToFile() {
		// open preferences to re-use directory name
		UserPreferences preferences = getUserPreferences();
		String lastFileName = preferences.getValue(LAST_FILE_OPENED,  ".");
		
		// create a file chooser
		JFileChooser chooser = new JFileChooser(lastFileName);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		// Save the Model Data
		chooser.setDialogTitle(s_res.getString("ModelIOUtilities.writeTitle"));
		chooser.setFileFilter(new SupportedFilesFilter());
		chooser.setAcceptAllFileFilterUsed(false);
		
		// get the file
		Frame applicationFrame = SystemContext.getApplicationFrame();
		File file = null;
		while (file == null) {
			// pre-set
			if (lastFileName != null) {
				File lastFile = new File(lastFileName);
				if (lastFile.exists()) {
					chooser.setSelectedFile(lastFile);
				}
			}
			
			// prompt
			int opt = chooser.showSaveDialog(applicationFrame);
			if (opt == JFileChooser.APPROVE_OPTION) {
				file = chooser.getSelectedFile();
			} else {
				// quit out of chooser dialog
				break;
			}

			// check
			if (file.exists() && !file.getAbsolutePath().equals(lastFileName)) {
				// File already exists, do you want to replace it
				opt = JOptionPane.showConfirmDialog(applicationFrame,
						MessageFormat.format(s_res.getString("ModelIOUtilities.fileExists"), file.getName()),
						s_res.getString("ModelIOUtilities.writeTitle"), JOptionPane.YES_NO_CANCEL_OPTION
						);
				
				if (opt == JOptionPane.CANCEL_OPTION) {
					// quit
					return false;
				} else if (opt == JOptionPane.YES_OPTION) {
					// keep going
				} else {
					// prompt again
					file = null;
				}
			}
			
		}
		
		if (file != null) {
			List<MessageGroup> groups = SelectionManager.getInstance().getEntitySelector().getMessageGroups();
			
			try {
				// CSV - not supported
				if (file.isDirectory() || fileNameEndsWith(file, CSV_Extension)) {
					JOptionPane.showMessageDialog(applicationFrame,
							MessageFormat.format(s_res.getString("ModelIOUtilities.writeNotSupportedMsgFormat"), 
									CSV_Extension),
							s_res.getString("ModelIOUtilities.notSupportedTitle"),
							JOptionPane.INFORMATION_MESSAGE);
					return false;
					
				} else {
					// XMI
					if (!fileNameEndsWith(file, XMI_Extension)) {
						// tack on ".xmi" extension
						file = new File(file.getAbsolutePath() + XMI_Extension);
					}
					
					XMIWriterDirect.write(file, groups);
				}

				// save file name
				lastFileName = file.getAbsolutePath();
				preferences.putValue(LAST_FILE_OPENED, lastFileName);

				// change name on title
				((MapEditor)SystemContext.getApplicationFrame()).updateTitle(lastFileName);
				
				SelectionManager.getInstance().writeToConsole(MessageFormat.format(s_res.getString("ModelIOUtilities.writeSucceededFormat"),
						lastFileName));
				return true;
				
			} catch (FileNotFoundException e) {
				SelectionManager.getInstance().getStatusPanel().writeException(e);
			} catch (XMLStreamException e) {
				SelectionManager.getInstance().getStatusPanel().writeException(e);
			}
		}
		return false;
	}
	
	/** Load the model data from a file or directory of files */
	public static void loadModelFromFile() {
		Frame applicationFrame = SystemContext.getApplicationFrame();
		// set cursor
		CursorManager cm = CursorManager.getInstance(applicationFrame);
		cm.setWaitCursor();
		try {

			ModelValidationResults results = new ModelValidationResults();
			List<MessageGroup> groups = new ArrayList<MessageGroup>();
			
			// fill in the groups
			String fileName = readModel(groups, results);
			
			if (fileName != null) {
				// update title
				((MapEditor)SystemContext.getApplicationFrame()).updateTitle(fileName);

				MdmiModelTree entitySelector = SelectionManager.getInstance().getEntitySelector();
				
				// check for imported nodes - save them
				List<MessageGroup> savedGroups = null;
				
				List<EditableObjectNode> importedNodes = findImportedData(entitySelector);
				if (importedNodes.size() > 0) {
					int opt = JOptionPane.showConfirmDialog(applicationFrame,
							s_res.getString("ModelIOUtilities.importedDataMsg"),
							s_res.getString("ModelIOUtilities.importedDataTitle"), 
							JOptionPane.YES_NO_CANCEL_OPTION);

					if (opt == JOptionPane.CANCEL_OPTION) {
						// quit
						return;
					} else if (opt == JOptionPane.YES_OPTION) {
						// keep track
						savedGroups = entitySelector.getMessageGroups();
					} else {
						// discard data
					}
				}

				// load tree
				entitySelector.loadTree(groups);
				
				// restore imported data
				if (savedGroups != null && savedGroups.size() > 0) {
					addImportedDatatypesToTree(savedGroups, true, false);
					addImportedBusinessElementRefToTree(savedGroups, true, false);
				}
				
				// show errors
				SelectionManager.getInstance().getStatusPanel().clearErrors();
				for (ModelInfo errorMsg : results.getErrors()) {
					SelectionManager.getInstance().getStatusPanel().writeValidationErrorMsg("",
							errorMsg);
				}
			}
		} finally {
			cm.restoreCursor();
		}
	}
	
	/** find all nodes at this node that are marked as imported */
	private static List<EditableObjectNode> findImportedData(MdmiModelTree entitySelector) {
		List<MessageGroup> groups = entitySelector.getMessageGroups();

		List<EditableObjectNode> importedNodes = new ArrayList<EditableObjectNode>();

		for (MessageGroup group : groups) {
			MessageGroupNode groupNode = (MessageGroupNode)entitySelector.findNode(group);

			for (Enumeration<?> en = groupNode.depthFirstEnumeration(); en != null
					&& en.hasMoreElements(); ) {
				DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) en.nextElement();
	
				if (treeNode instanceof EditableObjectNode && ((EditableObjectNode)treeNode).isImported()) {
					importedNodes.add((EditableObjectNode)treeNode);
				}
			}
		}
		return importedNodes;
	}

	/** Prompt the user for a file and read the data */
	private static String readModel(List<MessageGroup> groups, ModelValidationResults results) {

		// open preferences to re-use directory name
		UserPreferences preferences = getUserPreferences();
		String lastFileName = preferences.getValue(LAST_FILE_OPENED, null);

		// create a file chooser
		JFileChooser chooser = new JFileChooser(lastFileName == null ?  "." : lastFileName);
		// Select the data file, or the directory containing the data files
		chooser.setDialogTitle(s_res.getString("ModelIOUtilities.readTitle"));
		chooser.setFileFilter(new SupportedFilesFilter());
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		// pre-set
		if (lastFileName != null) {
			File lastFile = new File(lastFileName);
			if (lastFile.exists()) {
				chooser.setSelectedFile(lastFile);
			}
		}


		// prompt for a file
		int opt = chooser.showOpenDialog(SystemContext.getApplicationFrame());
		if (opt == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			lastFileName = file.getAbsolutePath();

			// create model from file
			if (fileNameEndsWith(file, CSV_Extension)) {
				// use parent directory
				file = file.getParentFile();
			}
			
			if (file.isDirectory()) {
				// CSV files in a directory - check type of files
				if (SpreadSheetModelBuilder.checkDirectory(file)) {
					// spread sheet files
					SpreadSheetModelBuilder builder = new SpreadSheetModelBuilder(file);
					groups.addAll( builder.build(results) );
				} else {
					groups.addAll( MapBuilderCSV.build(file, results) );
				}

			} else if (fileNameEndsWith(file, XMI_Extension)) {
				// XMI
				groups.addAll( MapBuilderXMIDirect.build(file, results) );
			}

			// save file name
			preferences.putValue(LAST_FILE_OPENED, lastFileName);
			
			return lastFileName;
		}
		
		return null;
	}

	
	/** Import just the dataTypes from a file or directory of files */
	public static void importDataTypesFromFile() {
		Frame applicationFrame = SystemContext.getApplicationFrame();
		// set cursor
		CursorManager cm = CursorManager.getInstance(applicationFrame);
		cm.setWaitCursor();
		try {
			ModelValidationResults results = new ModelValidationResults();
			List<MessageGroup> newGroups = new ArrayList<MessageGroup>();
			
			String fileName = readModel(newGroups, results);
			
			if (fileName != null) {
				// update tree (warn if datatype is already in tree)
				addImportedDatatypesToTree(newGroups, true, true);
				
			}
		} finally {
			cm.restoreCursor();
		}
	}

	/** Add dataypes from the groups into the tree */
	private static void addImportedDatatypesToTree(List<MessageGroup> newGroups, boolean copyIfExists, boolean warnIfExists) {
		MdmiModelTree entitySelector = SelectionManager.getInstance().getEntitySelector();
		for (MessageGroup newGroup : newGroups) {
			for (MessageGroup treeGroup : entitySelector.getMessageGroups()) {
				MessageGroupNode groupNode = (MessageGroupNode)entitySelector.findNode(treeGroup);
				
				// add all datatypes that aren't in the tree yet
				for (MdmiDatatype datatype : newGroup.getDatatypes()) {
					addImportedDatatype(datatype, groupNode, copyIfExists, warnIfExists);
				}
			}
		}
	}
	
	/** Mark the tree node associated with this object as "imported" */
	private static void setNodeAsImported(MdmiModelTree entitySelector, Object userObject) {
		EditableObjectNode treeNode = (EditableObjectNode)entitySelector.findNode(userObject);
		if (treeNode != null) {
			treeNode.setImported(true);
		}
	}

	/** Add this datatype to the message group if it doesn't already exist.
	 * 
	 * @param datatype
	 * @param groupNode
	 * @param copyIfExists	If an object already exists, copy the data from the datatypes
	 * @param warnIfExists	If an object already exists, show a warning message on the console
	 * 
	 * @return	The datatype that is in the tree - either newly added, or already existing
	 */
	private static MdmiDatatype addImportedDatatype(MdmiDatatype datatype, MessageGroupNode groupNode, boolean copyIfExists,
			boolean warnIfExists) {
		MdmiModelTree entitySelector = SelectionManager.getInstance().getEntitySelector();
		MessageGroup messageGroup = groupNode.getMessageGroup();
		//search for datatype with same name
		MdmiDatatype found = findDatatype(messageGroup.getDatatypes(), datatype.getTypeName());
		
		if (found == null) {
			groupNode.addDatatype(datatype);
			
			if (datatype instanceof DTComplex) {
				// re-set the datatype of each field
				for (Field field : ((DTComplex)datatype).getFields()) {
					found = addImportedDatatype(field.getDatatype(), groupNode, copyIfExists, false);	// don't warn if these already exist
					if (found != field.getDatatype()) {
						// make sure field uses the type that's already in the tree
						field.setDatatype(found);
					}
				}
			}
			found = datatype;
			setNodeAsImported(entitySelector, found);
			
		} else if (datatype instanceof DTSPrimitive) {
			// these should always exist, so we don't care
			
		} else {
			// copy if we can (if we can't an error message will be shown
			if (copyIfExists && found.getClass() == datatype.getClass()) {
				// copy data into found object
				EditableObjectNode newNode = entitySelector.replaceUserObject(found, datatype);

				// mark as imported
				newNode.setImported(true);
				
				found = datatype;
			}
			if (warnIfExists) {
				// already exists - show message
				LinkedObject link = new LinkedObject(found, found.getTypeName());
				String preMessage = ClassUtil.beautifyName(found.getClass());
				String postMessage = s_res.getString("ModelIOUtilities.alreadyExists");//already exists
				if (found.getClass() != datatype.getClass()) {
					// different types
					// "Cannot replace with a {otherType}"
					postMessage = MessageFormat.format(s_res.getString("ModelIOUtilities.typeConflictFormat"),
							postMessage, ClassUtil.beautifyName(datatype.getClass()) );
				} else if (copyIfExists) {
					postMessage = s_res.getString("ModelIOUtilities.replaceExists");//has been replaced
				}
				SelectionManager.getInstance().getStatusPanel().writeConsoleLink(preMessage,
						link, postMessage);
			}
		}
		
		return found;
	}
	
	public static MdmiDatatype findDatatype(Collection<MdmiDatatype> datatypes, String typeName) {
		for (MdmiDatatype datatype : datatypes) {
			if (datatype.getTypeName().equals(typeName)) {
				return datatype;
			}
		}
		return null;
	}
	
	/** Import just the data dictionary from a file or directory of files */
	public static void importDataDictionaryFromFile() {
		Frame applicationFrame = SystemContext.getApplicationFrame();
		// set cursor
		CursorManager cm = CursorManager.getInstance(applicationFrame);
		cm.setWaitCursor();
		try {
			ModelValidationResults results = new ModelValidationResults();
			List<MessageGroup> newGroups = new ArrayList<MessageGroup>();
			
			String fileName = readModel(newGroups, results);
			
			if (fileName != null) {
				// update tree - overwrite and warn if reference exists 
				addImportedBusinessElementRefToTree(newGroups, true, true);
			}
		} finally {
			cm.restoreCursor();
		}
	}

	/** Import Syntax Model from a file; determine input format based on file extension */
	public static void importSyntaxModelFromFile(MessageSyntaxModelNode syntaxModelNode) {
		Frame applicationFrame = SystemContext.getApplicationFrame();
		// set cursor
		CursorManager cm = CursorManager.getInstance(applicationFrame);
		cm.setWaitCursor();
		try {
			// open preferences to re-use directory name
			UserPreferences preferences = getUserPreferences();
			String lastFileName = preferences.getValue(LAST_FILE_OPENED, null);

			// create a file chooser
			JFileChooser chooser = new JFileChooser(lastFileName == null ?  "." : lastFileName);
			// Select the data file, or the directory containing the data files
			chooser.setDialogTitle(s_res.getString("ModelIOUtilities.readTitle"));
			chooser.setFileFilter(new SupportedSyntaxFileFilter());
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			// prompt for a file
			int opt = chooser.showOpenDialog(SystemContext.getApplicationFrame());
			if (opt == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				lastFileName = file.getAbsolutePath();

				// create model from file
				if (fileNameEndsWith(file, XSD_Extension)) {
					List<Node> roots = XSDReader.parse("file://localhost/"+lastFileName);

					if (!roots.isEmpty()) {
						MdmiModelTree tree = SelectionManager.getInstance().getEntitySelector();
						Node root = roots.get(0);
						SyntaxNodeNode rootNode = SyntaxNodeNode.createSyntaxNode(root);

						// add to the model
						MessageSyntaxModel syntaxModel = (MessageSyntaxModel) syntaxModelNode.getUserObject();
						syntaxModel.setRoot(root);
						root.setSyntaxModel(syntaxModel);

						// add to tree
						syntaxModelNode.removeAllChildren();
						syntaxModelNode.addSorted(rootNode);
						tree.refreshNode(syntaxModelNode);
					}
				}
				
				// save file name
				preferences.putValue(LAST_FILE_OPENED, lastFileName);
			}			
		} finally {
			cm.restoreCursor();
		}
	}

	/** Add business element references from the groups into the tree. 
	 * New businessElementReferences will be marked as "imported".
	 * Datatypes will be added if necessary */
	private static void addImportedBusinessElementRefToTree(List<MessageGroup> newGroups, boolean copyIfExists, boolean warnIfExists) {
		MdmiModelTree entitySelector = SelectionManager.getInstance().getEntitySelector();
		
		for (MessageGroup newGroup : newGroups) {
			for (MessageGroup treeGroup : entitySelector.getMessageGroups()) {
				MessageGroupNode groupNode = (MessageGroupNode)entitySelector.findNode(treeGroup);
				MdmiDomainDictionaryReference treeDictionary = treeGroup.getDomainDictionary();
				DomainDictionaryReferenceNode dictionaryNode = (DomainDictionaryReferenceNode)entitySelector.findNode(treeDictionary);
				
				// add all dictionary elements that aren't in the tree yet
				Collection<MdmiBusinessElementReference> references = newGroup.getDomainDictionary().getBusinessElements();
				for (MdmiBusinessElementReference newBizElemRef : references) {
					//search for element with same name
					MdmiBusinessElementReference found = findBusinessElementReference(treeGroup.getDomainDictionary().getBusinessElements(),
							newBizElemRef.getName());
					
					if (found == null) {
						// make sure datatype exists
						MdmiDatatype refDatatype = newBizElemRef.getReferenceDatatype();
						if (refDatatype != null) {
							MdmiDatatype foundType = addImportedDatatype(refDatatype, groupNode, false, false);	// don't warn if these already exist
							if (foundType != refDatatype) {
								// exists
								newBizElemRef.setReferenceDatatype(foundType);
							}
						}
						
						// Add to tree
						NewObjectInfo info = dictionaryNode.getNewObjectInformationForClass(newBizElemRef.getClass());

						EditableObjectNode childNode = info.addNewChild(newBizElemRef);
						childNode.setImported(true);
						SelectionManager.getInstance().getEntitySelector().insertNewNode(dictionaryNode,
								childNode);
						
					} else {

						if (copyIfExists) {
							// copy data into found object
							EditableObjectNode newNode = entitySelector.replaceUserObject(found, newBizElemRef);

							// mark as imported
							newNode.setImported(true);
							
							found = newBizElemRef;
						}
						if (warnIfExists) {
							// already exists - show message
							LinkedObject link = new LinkedObject(found, found.getName());
							String preMessage = ClassUtil.beautifyName(found.getClass());
							String postMessage = s_res.getString("ModelIOUtilities.alreadyExists"); //already exists
							if (copyIfExists) {
								postMessage = s_res.getString("ModelIOUtilities.replaceExists");//has been replaced
							}
						
							SelectionManager.getInstance().getStatusPanel().writeConsoleLink(preMessage,
									link, postMessage);
						}
					}
				}
			}
		}
	}
	
	private static MdmiBusinessElementReference findBusinessElementReference(
			Collection<MdmiBusinessElementReference> businessElements, String name) {
		for (MdmiBusinessElementReference reference : businessElements) {
			if (reference.getName().equals(name)) {
				return reference;
			}
		}
		return null;
	}

	/** Return a file filter that allows directories and supported files */
	public static class SupportedFilesFilter extends FileFilter {
			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				} else {
					return supportedFileName(f);
				}
			}

			@Override
			public String getDescription() {
				// XMI or CSV files
				return s_res.getString("ModelIOUtilities.fileTypesDescription");
			}
	}
	
	public static class SupportedSyntaxFileFilter extends FileFilter {
		@Override
		public boolean accept(File f) {
			return f.isDirectory() || f.getName().toLowerCase().endsWith(".xsd");			
		}
		@Override
		public String getDescription() {
			return "XML Schema (.xsd)";
		}
	}
}
