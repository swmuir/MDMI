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

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreeNode;
import javax.xml.stream.XMLStreamException;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.components.ExceptionDetailsDialog;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.tree.BusinessElementReferenceNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.DataTypeNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.DomainDictionaryReferenceNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MdmiModelTree;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SemanticElementNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SemanticElementSetNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SyntaxNodeNode;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.DTSDerived;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MdmiDomainDictionaryReference;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.MessageModel;
import org.openhealthtools.mdht.mdmi.model.Node;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.SemanticElementSet;
import org.openhealthtools.mdht.mdmi.model.ToBusinessElement;
import org.openhealthtools.mdht.mdmi.model.ToMessageElement;
import org.openhealthtools.mdht.mdmi.model.validate.ModelValidationResults;
import org.openhealthtools.mdht.mdmi.model.xmi.direct.reader.MapBuilderXMIDirect;
import org.openhealthtools.mdht.mdmi.model.xmi.direct.writer.XMIWriterDirect;


/* We'll only allow transfer of selections of the same type. And even with that, there are more rules.
 *  BusinessElementReference - also transfer the DataTypes
 *  DataTypes - also transfer referenced DataTypes (e.g. Field types, and derived-from types)
 *  SemanticElements - also transfer the rules (TBD - what about SyntaxNodes?)
 *  SyntaxNode - single item only. Target must be a valid syntaxNode Bag/Container. Also transfers SEs and DataTypes
 * 
 */
/** Drop support for copying Business Element References from another source */
public class DataDictionaryTransferHandler extends TransferHandler {

	// Source Identifier
	// <source>
	//   <id 12345>
	//   <type BusinessElementReferences>
	// </source>
	private static final String BEGIN_SOURCE = "<source>\n";
	private static final String END_SOURCE = " </source>\n";
	private static final String BEGIN_ID_TAG = "<id ";
	private static final String BEGIN_TYPE_TAG = "<type ";
	private static final String END_TAG = ">\n";
	
	
	static String SourceIdentifier = null;
	
	// extract the text between the start and end tags
	private static String extractValue(String sourceData, String startTag, String endTag) {
		String value = null;
		int idx = sourceData.indexOf(startTag);
    	if (idx != -1) {
    		int endIdx = sourceData.indexOf(endTag, idx);
    		if (endIdx != -1) {
    			value = sourceData.substring(idx+startTag.length(), endIdx);
    		}
    	}
    	return value;
	}
	
	static {
		if (SourceIdentifier == null) {
			SourceIdentifier = ""+System.identityHashCode(SystemContext.getApplicationFrame());
		}
	};
	
	// what are we transferring
	private static final String BUSINESS_ELEMENT_REFERENCES = "BusinessElementReferences";
	private static final String DATA_TYPES = "DataTypes";
	private static final String SEMANTIC_ELEMENTS = "SemanticElements";
	private static final String SYNTAX_NODE = "SyntaxNode";

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

    	Component target = info.getComponent();	// we'll use this for SyntaxNodes

		// read it
	    Transferable t = info.getTransferable();
	    
	    try {
		    String data = (String) t.getTransferData(DataFlavor.stringFlavor);
		    
		    // extract header information
		    String sourceID  = null;
		    String objectType = null;
		    
		    if (data.startsWith(BEGIN_SOURCE)) {
		    	int idx = data.indexOf(END_SOURCE);
		    	if (idx != -1) {
		    		int endIdx = idx+END_SOURCE.length();
		    		// <source>
		    		//   <id 12345>
		    		//   <type BusinessElementReferences>
		    		// </source>
		    		// extract, and strip off sourceData
			    	String sourceData = data.substring(0, endIdx);
		    		data = data.substring(endIdx);
			    	
		    		sourceID = extractValue(sourceData, BEGIN_ID_TAG, END_TAG);

			    	objectType = extractValue(sourceData, BEGIN_TYPE_TAG, END_TAG);
		    	}
		    }
		    
		    // fill in defaults
	    	if (sourceID == null) {
	    		sourceID = "";
	    	}
	    	if (objectType == null) {
	    		objectType = BUSINESS_ELEMENT_REFERENCES;
	    	}
	    	
		    
		    // prevent drop into our own
		    if (SourceIdentifier.equals(sourceID)) {
		    	return false;
		    }
			
			// try to import it
			ByteArrayInputStream is = new ByteArrayInputStream(data.getBytes());
			ModelValidationResults results = new ModelValidationResults();

            List<MessageGroup> newGroups = MapBuilderXMIDirect.build(is, results);
            if (newGroups != null) {
            	// TODO: use IElementTransfer
                // update tree - overwrite and warn if reference exists
    	    	if (BUSINESS_ELEMENT_REFERENCES.equals(objectType)) {
                    ModelIOUtilities.addImportedBusinessElementRefToTree(newGroups, true, true);
                    
    	    	} else if (DATA_TYPES.equals(objectType)) {
                    ModelIOUtilities.addImportedDatatypesToTree(newGroups, true, false);
    	    	}

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
		// All selected items must be in the same message group
		MessageGroup group = null;
		// All items must be the same type
		IElementTransfer transferHelper = null;
		
		Transferable transfer = null;
		if (c instanceof JTree) {
			SelectionManager selectionManager = SelectionManager.getInstance();
			MdmiModelTree entitySelector = selectionManager.getEntitySelector();
			List<TreeNode> selectedTreeNodes = entitySelector.getSelectedNodes(); 

			
			// check
			for (TreeNode treeNode : selectedTreeNodes) {
				// sanity test
				if (treeNode instanceof EditableObjectNode) {
					MessageGroup nodesGroup = ((EditableObjectNode)treeNode).getMessageGroup();
					if (group == null) {
						group = nodesGroup;
					} else if (group != nodesGroup) {
						// can't have multiple groups
						return null;
					}
				}

				//////////////////////////////////////////////
				// node-specific
				/////////////////////////////////////////////
				if (treeNode instanceof DataTypeNode) {
					if (transferHelper == null) {
						transferHelper = new DataTypeTransfer();
					} else if (!(transferHelper instanceof DataTypeTransfer)) {
						// can't have multiple types
						return null;
					}
					
				} else if (treeNode instanceof BusinessElementReferenceNode ||
						treeNode instanceof DomainDictionaryReferenceNode) {
					if (transferHelper == null) {
						transferHelper = new BusinessElementReferenceTransfer();
					} else if (!(transferHelper instanceof BusinessElementReferenceTransfer)) {
						// can't have multiple types
						return null;
					}

				} else if (treeNode instanceof SemanticElementNode ||
						treeNode instanceof SemanticElementSetNode) {
					if (transferHelper == null) {
						transferHelper = new SemanticElementTransfer();
					} else if (!(transferHelper instanceof SemanticElementTransfer)) {
						// can't have multiple types
						return null;
					}
					
					
				} else if (treeNode instanceof SyntaxNodeNode) {
					if (transferHelper == null) {
						transferHelper = new SyntaxNodeTransfer();
					} else {
						// can't have multiples
						return null;
					}
					
				} else {
					return null;
				}
				
				if (transferHelper == null || !transferHelper.canTransfer(treeNode)) {
					// not supported
					return null;
				}
			}
			
			if (transferHelper != null && group != null) {
				// save the message group
				MessageGroup originalGroup = group;

				// 1. Copy (and Clear) Message Group
				MessageGroup copiedGroup = copyAndClearMessageGroup(originalGroup);

				// 2. save selected items and all associated data
				transferHelper.saveForTransfer(originalGroup);

				// 3. Serialize
				OutputStream os = serializeMessageGroup(originalGroup, transferHelper.getElementType());
				
				// 4. Restore original message group contents
				copyGroup(copiedGroup, originalGroup);
				
				// 5. Wrap it
				transfer = new StringSelection(os.toString());
			}
		}
		return transfer;
	}

	
	
//	// create a Transferable from a SyntaxNode
//	private Transferable createTransferableSyntaxNode(Node node) {
//		
//		// Determine all the other objects that are needed
//		MessageModel messageModel = node.getSyntaxModel().getModel();
//		MessageGroup messageGroup = messageModel.getGroup();
//
//		MessageGroup copiedGroup = copyAndClearMessageGroup(messageGroup);
//
//		// Find the SE - we may have to walk up the tree
//		SemanticElement se = null;
//		Node seNode = node;
//		while (seNode != null) {
//			neededNodes.add(seNode);
//			se = seNode.getSemanticElement();
//			if (se != null) {
//				break;	// found it
//				
//			} else if (node.getFieldName() != null && node.getFieldName().length() > 0) {
//				// If we have a field name, work our way up until we find a SE
//				seNode = seNode.getParentNode();
//				
//			} else {
//				break;
//			}
//		}
//		
//		// Add SE and all referenced data types
//		if (se != null) {
//			neededSemanticElements.add(se);
//			// get all referenced data types
//			saveDataTypes(neededDatatypes, se.getDatatype());
//		}
//		
//
//		// 1. Copy (and Clear) Message Group(s)
//		ArrayList<MessageGroup> copiedGroups = copyAndClearMessageGroups(neededGroups);
//		
//		// 3. Add Items that we want to transfer
//		for (MessageGroup group : neededGroups) {
//			// TODO
//			//  data types
//			Collection<MdmiDatatype> datatypeList = group.getDatatypes();
//			if (neededDatatypes != null) { 
//				for (MdmiDatatype datatype : neededDatatypes) {
//					if (datatype.getOwner() == group || datatype.isPrimitive()) {
//						datatypeList.add(datatype);
//					}
//				}
//			}
//			
//			// semantic elements
////			MdmiDomainDictionaryReference dictionary = group.getDomainDictionary();
////			for (MdmiBusinessElementReference ber : neededBERs) {
////				if (ber.getDomainDictionaryReference() == dictionary) {
////					dictionary.addBusinessElement(ber);
////				}
////			}
//			
//			// syntax nodes
//		}
//		
//		// 4. Serialize
//		OutputStream os = serializeMessageGroups(neededGroups, SYNTAX_NODE);
//		
//		// 5. Restore Message Groups
//		restoreMessageGroups(neededGroups, copiedGroups);
//		
//		return new StringSelection(os.toString());
//	}

	// Make a copy of the message group, and clear the original
	protected MessageGroup copyAndClearMessageGroup(MessageGroup original) {
		MessageGroup copy = new MessageGroup();
		copyGroup(original, copy);
		return copy;
	}


	//  copy the data from the source MessageGroup to the target, then clear the source
	protected void copyGroup(MessageGroup source, MessageGroup target) {
		try {
			ClassUtil.copyData(source, target);	// shallow copy

			// Data Types
			Collection<MdmiDatatype> originaltypes = source.getDatatypes();
			Collection<MdmiDatatype> copyTypes = new ArrayList<MdmiDatatype>();
			copyTypes.addAll(originaltypes);
			originaltypes.clear();

			// Business Element References
			MdmiDomainDictionaryReference originalDictionary = source.getDomainDictionary();
			MdmiDomainDictionaryReference copyDictionary = new MdmiDomainDictionaryReference();
			target.setDomainDictionary(copyDictionary);
			Collection<MdmiBusinessElementReference> originalBEs = originalDictionary.getBusinessElements();
			Collection<MdmiBusinessElementReference> copyBEs = copyDictionary.getBusinessElements();
			copyBEs.addAll(originalBEs);
			originalBEs.clear();


			// Message Models
			Collection<MessageModel> originalModels = source.getModels();
			Collection<MessageModel> copyModels = target.getModels();
			copyModels.addAll(originalModels);
			originalModels.clear();
			
			
		} catch (Exception e) {
			ExceptionDetailsDialog.showException(SystemContext.getApplicationFrame(), e);
		}
	}


	// convert the message group to a Stream
	protected OutputStream serializeMessageGroup(MessageGroup group, String objectType) {
		
		// convert MessageGroups to stream
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		// add Source information
		try {
			StringBuffer sourceID = new StringBuffer();
    		// <source>
    		//   <id 12345>
    		//   <type BusinessElementReferences>
    		// </source>
			sourceID.append(BEGIN_SOURCE);
			sourceID.append(BEGIN_ID_TAG).append(SourceIdentifier).append(END_TAG);
			sourceID.append(BEGIN_TYPE_TAG).append(objectType).append(END_TAG);
			sourceID.append(END_SOURCE);
			
			os.write(sourceID.toString().getBytes());

		} catch (IOException e) {
			// ignore
		}

		try {
			List<MessageGroup> listOfGroups = new ArrayList<MessageGroup>();
			listOfGroups.add(group);
			XMIWriterDirect.write(os, listOfGroups);
		} catch (XMLStreamException ex) {
			ExceptionDetailsDialog.showException(SelectionManager.getInstance().getEntitySelector(), ex);
		}
		
		
		return os;
	}
	

	// save this BusinessElement, and all associated data (if not already there) in the MessageGroup
	private void addBusinessElementReferenceTogroup(MessageGroup group, MdmiBusinessElementReference ber) {
		if (ber == null) {
			return;
		}
		
		// check if we have it
		Collection<MdmiBusinessElementReference> bersInGroup = group.getDomainDictionary().getBusinessElements();
		
		// save the BER
		if (!bersInGroup.contains(ber)) {
			bersInGroup.add(ber);

			// save the referenced data types
			if (ber.getReferenceDatatype() != null) {
				MdmiDatatype datatype = ber.getReferenceDatatype();
				addDatatypesToGroup(group, datatype);
			}
		}
	}
	
	// save this datatype, and all associated data (if not already there) in the MessageGroup
	private void addDatatypesToGroup(MessageGroup group, MdmiDatatype datatype) {
		if (datatype == null) {
			return;
		}

		// check first
		Collection<MdmiDatatype> dataTypesInGroup = group.getDatatypes();
		
		// save it
		if (!dataTypesInGroup.contains(datatype)) {
			// add it
			dataTypesInGroup.add(datatype);
			
			// Check on fields, etc.
			if (datatype instanceof DTComplex) {
				DTComplex complexType = (DTComplex)datatype;
				for (Field field : complexType.getFields()) {
					if (field.getDatatype() != null) {
						addDatatypesToGroup(group, field.getDatatype());
					}
				}
				
            } else if (datatype instanceof DTSDerived) {
            	DTSDerived derType = (DTSDerived)datatype;
            	addDatatypesToGroup(group, derType.getBaseType());
            }
		}
	}
	

	// save this SemanticElement, and all associated data (if not already there) in the MessageGroup
	private void addSemanticElementToGroup(MessageGroup group, SemanticElement se) {
		if (se == null) {
			return;
		}
		
		// check if we have it
		Collection<SemanticElement> seInGroup = se.getElementSet().getSemanticElements();
		
		// save it
		if (!seInGroup.contains(se)) {
			seInGroup.add(se);
			
			// save the datatype
			addDatatypesToGroup(group, se.getDatatype());
			
			// save the node
			addSyntaxNodeToGroup(group, se.getSyntaxNode());

			// save the parent
			addSemanticElementToGroup(group, se.getParent());

			// save the children
			for (SemanticElement child: se.getChildren()) {
				addSemanticElementToGroup(group, child);
			}
			
			// save BERs from rules
			for (ToMessageElement toME : se.getToMdmi()) {
				addBusinessElementReferenceTogroup(group, toME.getBusinessElement());
			}
			for (ToBusinessElement toBE : se.getFromMdmi()) {
				addBusinessElementReferenceTogroup(group, toBE.getBusinessElement());
			}

		}
	}

	// save this SyntaxNode, and all associated data (if not already there) in the MessageGroup
	private void addSyntaxNodeToGroup(MessageGroup group, Node syntaxNode) {
		// TODO
	}
	
	////////////////////////////////////////////////////////////////////////////////
	// Base Class for writing and reading MDMI elements 
	///////////////////////////////////////////////////////////////////////////////
	private static interface IElementTransfer {
		public String getElementType();
		public boolean canTransfer(TreeNode treeNode);
		public void saveForTransfer(MessageGroup group);
		public void importElements(List<MessageGroup> newGroups);
	}
	
	private class BusinessElementReferenceTransfer implements IElementTransfer {
		List<MdmiBusinessElementReference> selectedBERs = new ArrayList<MdmiBusinessElementReference>();

		@Override
		public String getElementType() {
			return BUSINESS_ELEMENT_REFERENCES;
		}
		
		@Override
		public boolean canTransfer(TreeNode treeNode) {
			if (treeNode instanceof BusinessElementReferenceNode) {
				BusinessElementReferenceNode berNode = (BusinessElementReferenceNode)treeNode;
				selectedBERs.add((MdmiBusinessElementReference)berNode.getUserObject());
				return true;
			} else if (treeNode instanceof DomainDictionaryReferenceNode) {
				DomainDictionaryReferenceNode dictNode = (DomainDictionaryReferenceNode)treeNode;
				MdmiDomainDictionaryReference dictionary = (MdmiDomainDictionaryReference)dictNode.getUserObject();
				selectedBERs.addAll(dictionary.getBusinessElements());
			}
			return false;
		}

		@Override
		public void saveForTransfer(MessageGroup group) {
			for (MdmiBusinessElementReference ber : selectedBERs) {
				addBusinessElementReferenceTogroup(group, ber);
			}
		}

		@Override
		public void importElements(List<MessageGroup> newGroups) {
            ModelIOUtilities.addImportedBusinessElementRefToTree(newGroups, true, true);
		}
	}
	
	private class DataTypeTransfer implements IElementTransfer {
		private List<MdmiDatatype> selectedDataTypes = new ArrayList<MdmiDatatype>();

		@Override
		public String getElementType() {
			return DATA_TYPES;
		}

		@Override
		public boolean canTransfer(TreeNode treeNode) {
			if (treeNode instanceof DataTypeNode) {
				MdmiDatatype dataType = (MdmiDatatype)((DataTypeNode)treeNode).getUserObject();
				
				selectedDataTypes.add(dataType);
				return true;
			}
			return false;
		}

		@Override
		public void saveForTransfer(MessageGroup group) {
			for (MdmiDatatype dataType : selectedDataTypes) {
				addDatatypesToGroup(group, dataType);
			}
		}

		@Override
		public void importElements(List<MessageGroup> newGroups) {
            ModelIOUtilities.addImportedDatatypesToTree(newGroups, true, true);
		}
		
	}
	
	private class SemanticElementTransfer implements IElementTransfer {
		List<SemanticElement> selectedSEs = new ArrayList<SemanticElement>();
		// All selected SemanticElements must be in the same MessageModel
		MessageModel messageModel = null;

		@Override
		public String getElementType() {
			return SEMANTIC_ELEMENTS;
		}

		@Override
		public boolean canTransfer(TreeNode treeNode) {
			if (treeNode instanceof SemanticElementSetNode) {
				SemanticElementSet seSet = (SemanticElementSet)((SemanticElementSetNode)treeNode).getUserObject();
				if (messageModel == null) {
					messageModel = seSet.getModel();
				} else if (seSet.getModel() != messageModel) {
					return false;
				}
				
				selectedSEs.addAll(seSet.getSemanticElements());
				return true;
				
			} else if (treeNode instanceof SemanticElementNode) {
				SemanticElement se = (SemanticElement)((SemanticElementNode)treeNode).getUserObject();
				if (messageModel == null) {
					messageModel = se.getElementSet().getModel();
				} else if (se.getElementSet().getModel() != messageModel) {
					return false;
				}
				
				selectedSEs.add(se);
				return true;
			}
			return false;
		}

		@Override
		public void saveForTransfer(MessageGroup group) {
			for (SemanticElement se : selectedSEs) {
				addSemanticElementToGroup(group, se);
			}
		}

		@Override
		public void importElements(List<MessageGroup> newGroups) {
            // TODO
		}
		
	}

	
	private class SyntaxNodeTransfer implements IElementTransfer {
		Node selectedNode = null;

		@Override
		public String getElementType() {
			return SYNTAX_NODE;
		}

		@Override
		public boolean canTransfer(TreeNode treeNode) {
			if (treeNode instanceof SyntaxNodeNode) {
				selectedNode = ((SyntaxNodeNode)treeNode).getSyntaxNode();
			}
			
			return false;
		}

		@Override
		public void saveForTransfer(MessageGroup group) {
			addSyntaxNodeToGroup(group, selectedNode);
		}

		@Override
		public void importElements(List<MessageGroup> newGroups) {
            // TODO
		}
		
	}
}