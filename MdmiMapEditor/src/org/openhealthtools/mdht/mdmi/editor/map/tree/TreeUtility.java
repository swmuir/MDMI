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
package org.openhealthtools.mdht.mdmi.editor.map.tree;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.model.DataRule;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.MessageSyntaxModel;
import org.openhealthtools.mdht.mdmi.model.Node;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;

/** A collection of utility methods for the MDMI Tree */
public class TreeUtility {
	private DefaultMutableTreeNode m_root;
	
	public TreeUtility(DefaultMutableTreeNode root) {
		m_root = root;
	}

	/** Find all nodes that have a user-object with references to the supplied user-object
	 * @param referencedObject
	 * @return
	 */
	public List<EditableObjectNode> findReferences(Object referencedObject, MessageGroup group) {

		List<EditableObjectNode> referenceNodes = new ArrayList<EditableObjectNode>();
		
		// walk tree and look for references
		for (Enumeration<?> en = m_root.preorderEnumeration(); en != null && en.hasMoreElements();) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode)en.nextElement();
			if (child instanceof EditableObjectNode) {
				EditableObjectNode edChild = (EditableObjectNode) child;
				// skip the nodes that are not in this message group
				if (edChild.getMessageGroup() != group) {
					continue;
				}
				
				// skip the non-editable nodes, since these are just for show
				if (!edChild.isEditable()) {
					continue;
				}

				Object childUserObject = child.getUserObject();
				
				// special handling of Field, which is referenced by name in a SyntaxNode, SemanticElement and BER
				if (referencedObject instanceof Field) {
					if (nodeReferencesField(child, (Field)referencedObject)) {
						referenceNodes.add(edChild);
						continue;
					}
				}
				
				// Special handling of an MdmiDatatype, which is referenced in a list by a DataRule
				if (referencedObject instanceof MdmiDatatype && childUserObject instanceof DataRule) {
					if (((DataRule)childUserObject).getDatatypes().contains(referencedObject)) {
						referenceNodes.add(edChild);
					}
					continue;
				}
				
				// inspect user object to determine if it has any references
				if (hasReferenceTo(childUserObject, referencedObject)) {
					referenceNodes.add(edChild);
				}
			}
		}
		return referenceNodes;
	}

	/** Find all nodes that have a user-object with references to the user-object
	 * in the provided node
	 * @param node
	 * @return
	 */
	public  List<EditableObjectNode> findReferences(EditableObjectNode node) {
		Object referencedObject = node.getUserObject();

		return findReferences(referencedObject, node.getMessageGroup());
	}
	


	/** Determine what nodes are affected (i.e. contain references to the items being deleted),
	 * if these nodes are deleted.
	 * @param affectedNodes
	 * @param edNode
	 */
	public Map <EditableObjectNode, List<EditableObjectNode>> findAffectedNodes(List<EditableObjectNode> nodesToRemove) {
		Map <EditableObjectNode, List<EditableObjectNode>> referenceLinks = new HashMap<EditableObjectNode, List<EditableObjectNode>>();
		
		
		for (TreeNode node : nodesToRemove) {
			// find references to this node and its children
			for (Enumeration<?> en = ((DefaultMutableTreeNode) node).preorderEnumeration(); en != null && en.hasMoreElements();) {
				DefaultMutableTreeNode child = (DefaultMutableTreeNode)en.nextElement();
				if (child instanceof EditableObjectNode) {
					List<EditableObjectNode> references = findReferences((EditableObjectNode)child);
					for (EditableObjectNode referencedNode : references) {
						// add reference node to map
						List<EditableObjectNode> refersToList = referenceLinks.get(referencedNode);
						if (refersToList == null) {
							refersToList = new ArrayList<EditableObjectNode>();
							referenceLinks.put(referencedNode, refersToList);
						}
						// add child to the "refersTo" list
						if (!refersToList.contains(child)) {
							refersToList.add((EditableObjectNode)child);
						}
					}
				}
			}
		}
		
		// ignore references within the nodes we're removing
		for (EditableObjectNode node : nodesToRemove) {
			for (Enumeration<?> en = ((DefaultMutableTreeNode) node).preorderEnumeration(); en != null && en.hasMoreElements();) {
				DefaultMutableTreeNode child = (DefaultMutableTreeNode)en.nextElement();
				referenceLinks.remove(child);
			}
			
			// special case - don't include MessageSyntaxModelNode if we're removing the root
			if (node.getParent() instanceof MessageSyntaxModelNode) {
				MessageSyntaxModelNode syntaxModelNode = (MessageSyntaxModelNode)node.getParent();
				MessageSyntaxModel syntaxModel = (MessageSyntaxModel)syntaxModelNode.getUserObject();
				if (syntaxModel.getRoot() == node.getUserObject()) {
					referenceLinks.remove(syntaxModelNode);
				}
			}
		}

		return referenceLinks;
	}

	
	/** Check if the user object in a tree node references this field. 
	 * We'll use the node's fieldName attribute to match the name, plus look at
	 * the datatype of the parent node
	 **/
	private static boolean nodeReferencesField(DefaultMutableTreeNode treeNode, Field field) {

		String fieldName = field.getName();
		if (fieldName == null) {
			return false;
		}
		Object userObject = treeNode.getUserObject();

		if (userObject instanceof SemanticElement) { 
			SemanticElement se = (SemanticElement)userObject;
			if (se.getDatatype() == field.getOwnerType() &&
					(fieldName.equals(se.getEnumValueDescrField()) || fieldName.equals(se.getEnumValueField()) ||
							fieldName.equals(se.getEnumValueSet()) || fieldName.equals(se.getEnumValueSetField()))
					) {

				return true;
			}
			
		} else if (userObject instanceof MdmiBusinessElementReference) { 
			MdmiBusinessElementReference ber = (MdmiBusinessElementReference)userObject;
			if (ber.getReferenceDatatype() == field.getOwnerType() &&
					(fieldName.equals(ber.getEnumValueDescrField()) || fieldName.equals(ber.getEnumValueField()) ||
							fieldName.equals(ber.getEnumValueSet()) || fieldName.equals(ber.getEnumValueSetField()))
					) {

				return true;
			}
			
		} else if (userObject instanceof Node) { 
			Node node = (Node)userObject;
			// first check if names match
			if (fieldName.equals(node.getFieldName())) {
				// names match, so check datatype
				MdmiDatatype fieldDatatype = field.getDatatype();
				MdmiDatatype nodeDatatype = ((SyntaxNodeNode)treeNode).getDataType();
				if (fieldDatatype == nodeDatatype) {
					// datatypes match - check owner type against the node's parent's datatype
					MdmiDatatype ownerType = field.getOwnerType();
					if (treeNode.getParent() instanceof SyntaxNodeNode) {
						MdmiDatatype parentType = ((SyntaxNodeNode)treeNode.getParent()).getDataType();
						if (ownerType == parentType) {
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Determine if the provided model has any references to the <i>referenceObject</i>.
	 * The model will be identified as having a reference it there are any "getXXX()" methods
	 * on the model that return the referenced object.
	 * @param modelObject
	 * @param referencedObject
	 * @return
	 */
	private static boolean hasReferenceTo(Object modelObject, Object referencedObject) {

		// inspect user object to determine if it has any methods that return an object
		// of the type we want
		List<Method[]> getSetPairs = ClassUtil.getMethodPairs(modelObject.getClass(),
				referencedObject.getClass());
		if (getSetPairs.size() != 0) {
			for (Method [] methodPair : getSetPairs) {
				Method getMethod = methodPair[0];
				try {
					Object objectInModel = getMethod.invoke(modelObject);
					
					// did we find it?
					if (referencedObject.equals(objectInModel)) {
						return true;
					}
				} catch (Exception e) {
					// don't care
				}
			}
		}
		
		return false;
	}

	/**
	 * Replace any references to the <i>referenceObject</i> with the <i>replacementObject</i>.
	 * The model will be identified as having a reference it there are any "getXXX()" methods
	 * on the model that return the referenced object.
	 * @param modelObject
	 * @param referencedObject
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public static void replaceReferenceTo(Object modelObject, Object referencedObject, Object replacementObject)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		// inspect user object to determine if it has any methods that return an object
		// of the type we want
		List<Method[]> getSetPairs = ClassUtil.getMethodPairs(modelObject.getClass(),
				referencedObject.getClass());
		if (getSetPairs.size() != 0) {
			for (Method [] methodPair : getSetPairs) {
				Method getMethod = methodPair[0];
				Method setMethod = methodPair[1];
				Object objectInModel = null;
				try {
					objectInModel = getMethod.invoke(modelObject);
				} catch (Exception e) {
					// don't care on the "get"
				}
				
				if (objectInModel != null) {
					// did we find it?
					if (referencedObject.equals(objectInModel)) {
						// change it
						setMethod.invoke(modelObject, replacementObject);
					}
				}
			}
		}
	}


//	/** Check if this collection includes a node with the supplied user object */
//	private static boolean containsUserObject(Collection<EditableObjectNode> nodes, Object userObject) {
//		for (EditableObjectNode node: nodes) {
//			if (node.getUserObject().equals(userObject)) {
//				return true;
//			}
//		}
//		return false;
//	}

}
