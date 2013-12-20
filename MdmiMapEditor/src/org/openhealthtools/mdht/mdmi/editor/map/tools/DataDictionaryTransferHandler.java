/*******************************************************************************
 * Copyright (c) 2013 Firestar Software, Inc.
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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreeNode;
import javax.xml.stream.XMLStreamException;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.components.ExceptionDetailsDialog;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.tree.BusinessElementReferenceNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.DomainDictionaryReferenceNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MdmiModelTree;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.DTSDerived;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MdmiDomainDictionaryReference;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.MessageModel;
import org.openhealthtools.mdht.mdmi.model.validate.ModelValidationResults;
import org.openhealthtools.mdht.mdmi.model.xmi.direct.reader.MapBuilderXMIDirect;
import org.openhealthtools.mdht.mdmi.model.xmi.direct.writer.XMIWriterDirect;


/** Drop support for copying Business Element References from another source */
public class DataDictionaryTransferHandler extends TransferHandler {


	@Override
	public boolean canImport(TransferHandler.TransferSupport info) {
		// we only import Strings
		if (info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean importData(TransferHandler.TransferSupport info) {
		if (!info.isDrop()) {
			return false;
		}
    
	    
		// Check for String flavor
		if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			return false;
		}


		// read it
	    Transferable t = info.getTransferable();
	    
	    // TODO how do we prevent drop into our own
	    
	    try {

		    String data = (String) t.getTransferData(DataFlavor.stringFlavor);
			
			// try to import it
			ByteArrayInputStream is = new ByteArrayInputStream(data.getBytes());
			ModelValidationResults results = new ModelValidationResults();

            List<MessageGroup> newGroups = MapBuilderXMIDirect.build(is, results);
            if (newGroups != null) {
                // update tree - overwrite and warn if reference exists
                ModelIOUtilities.addImportedBusinessElementRefToTree(newGroups, true, true);
            }
			
			
		} catch (Exception e) {
			ExceptionDetailsDialog.showException(SystemContext.getApplicationFrame(), e);
		}


		return true;
	}

	@Override
	public int getSourceActions(JComponent c) {
		return COPY;
	}

	@Override
	protected Transferable createTransferable(JComponent c)
	{
		if (c instanceof JTree) {
			SelectionManager selectionManager = SelectionManager.getInstance();
			MdmiModelTree entitySelector = selectionManager.getEntitySelector();
			List<TreeNode> selectedNodes = entitySelector.getSelectedNodes(); 
			
			List<MdmiBusinessElementReference> selectedBERs = new ArrayList<MdmiBusinessElementReference>();
			// check
			boolean allBERnodes = true;
			for (TreeNode treeNode : selectedNodes) {
				if (treeNode instanceof BusinessElementReferenceNode) {
					BusinessElementReferenceNode berNode = (BusinessElementReferenceNode)treeNode;
					selectedBERs.add((MdmiBusinessElementReference)berNode.getUserObject());
				} else if (treeNode instanceof DomainDictionaryReferenceNode) {
					DomainDictionaryReferenceNode dictNode = (DomainDictionaryReferenceNode)treeNode;
					MdmiDomainDictionaryReference dictionary = (MdmiDomainDictionaryReference)dictNode.getUserObject();
					selectedBERs.addAll(dictionary.getBusinessElements());
				} else {
					allBERnodes = false;
					break;
				}
			}
			
			if (allBERnodes) {
				Transferable transfer = createTransferableBusinessElements(c, selectedBERs);
				return transfer;
			}
		}
		return null;
	}

	
	// create a Transferable from a list of Business Element Reference nodes
	private Transferable createTransferableBusinessElements(JComponent source,
			List<MdmiBusinessElementReference> neededBERs) {

		// we need these items too
		ArrayList<MessageGroup> neededGroups = new ArrayList<MessageGroup>();
		ArrayList<MdmiDatatype> neededDatatypes = new ArrayList<MdmiDatatype>();
		
		// First determine all the groups and datatypes that are needed
		for (MdmiBusinessElementReference ber : neededBERs) {
			// identify message group
			MessageGroup group = ber.getDomainDictionaryReference().getMessageGroup();
			if (!neededGroups.contains(group)) {
				neededGroups.add(group);
			}

			// get all referenced datatypes
			if (ber.getReferenceDatatype() != null) {
				MdmiDatatype datatype = ber.getReferenceDatatype();
				saveDataTypes(neededDatatypes, datatype);
			}
		}
		
		// We're going to clear out the message groups of unwanted data when we transfer.
		// So first, make a copy of the existing lists of BERs, Data Types, and MessageModels (for each group)
		HashMap<MessageGroup, ArrayList<MdmiBusinessElementReference>> savedBERsMap = 
				new HashMap<MessageGroup, ArrayList<MdmiBusinessElementReference>>();
		HashMap<MessageGroup, ArrayList<MdmiDatatype>> savedDatatypesMap = 
				new HashMap<MessageGroup, ArrayList<MdmiDatatype>>();
		HashMap<MessageGroup, ArrayList<MessageModel>> savedMessageModelsMap = 
				new HashMap<MessageGroup, ArrayList<MessageModel>>();

		for (MessageGroup group : neededGroups) {
			// save the existing datatypes
			Collection<MdmiDatatype> datatypeList = group.getDatatypes();
			ArrayList<MdmiDatatype> savedDatatypes = new ArrayList<MdmiDatatype>();
			savedDatatypes.addAll(group.getDatatypes());
			savedDatatypesMap.put(group, savedDatatypes);

			// save the existing BERs
			MdmiDomainDictionaryReference dictionary = group.getDomainDictionary();
			ArrayList<MdmiBusinessElementReference> savedBERs = new ArrayList<MdmiBusinessElementReference>();
			savedBERs.addAll(dictionary.getBusinessElements());
			savedBERsMap.put(group, savedBERs);
			
			// save the MessageModels
			ArrayList<MessageModel> savedMessageModels = new ArrayList<MessageModel>();
			savedMessageModels.addAll(group.getModels());
			savedMessageModelsMap.put(group, savedMessageModels);

			// clear out the datatypes, BERs and message model
			datatypeList.clear();
			dictionary.getBusinessElements().clear();
			group.getModels().clear();

			// now add the data we want to transfer
			
			// Add the BERs
			for (MdmiBusinessElementReference ber : neededBERs) {
				if (ber.getDomainDictionaryReference() == dictionary) {
					dictionary.addBusinessElement(ber);
				}
			}

			// Add the required data types
			for (MdmiDatatype datatype : neededDatatypes) {
				if (datatype.getOwner() == group || datatype.isPrimitive()) {
					datatypeList.add(datatype);
				}
			}

		}
		
		// convert MessageGroups to stream
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			XMIWriterDirect.write(os, neededGroups);
		} catch (XMLStreamException ex) {
			ExceptionDetailsDialog.showException(SelectionManager.getInstance().getEntitySelector(), ex);
		}
		
		// restore BERs, Datatypes and model
		for (MessageGroup group : neededGroups) {
			// restore the old datatypes
			Collection<MdmiDatatype> datatypes = group.getDatatypes();
			datatypes.clear();
			ArrayList<MdmiDatatype> savedDatatypes = savedDatatypesMap.get(group);
			datatypes.addAll(savedDatatypes);

			// restore the old BERs
			MdmiDomainDictionaryReference dictionary = group.getDomainDictionary();
			dictionary.getBusinessElements().clear();
			ArrayList<MdmiBusinessElementReference> savedBERs = savedBERsMap.get(group);
			dictionary.getBusinessElements().addAll(savedBERs);

			// restore the old datatypes
			Collection<MessageModel> messageModelList = group.getModels();
			messageModelList.clear();
			ArrayList<MessageModel> savedMessageModels = savedMessageModelsMap.get(group);
			messageModelList.addAll(savedMessageModels);
		}

		return new StringSelection(os.toString());
	}
	
	// save this datatype, and all associated datatypes, in the list (if it's not there already)
	private void saveDataTypes(ArrayList<MdmiDatatype> neededDatatypes, MdmiDatatype datatype) {
		if (datatype == null) {
			return;
		}

		// check first
		if (!neededDatatypes.contains(datatype)) {
			// add it
			neededDatatypes.add(datatype);
			
			// Check on fields, etc.
			if (datatype instanceof DTComplex) {
				DTComplex complexType = (DTComplex)datatype;
				for (Field field : complexType.getFields()) {
					if (field.getDatatype() != null) {
						saveDataTypes(neededDatatypes, field.getDatatype());
					}
				}
				
            } else if (datatype instanceof DTSDerived) {
            	DTSDerived derType = (DTSDerived)datatype;
            	saveDataTypes(neededDatatypes, derType.getBaseType());
            }
		}
	}
	
}