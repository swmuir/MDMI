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

import java.awt.Component;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.openhealthtools.mdht.mdmi.editor.common.components.WrappingDisplayText;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.tools.DatatypeTree.IDatatypeField;
import org.openhealthtools.mdht.mdmi.editor.map.tree.ChangeTypeDialog;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MdmiModelTree;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SyntaxNodeNode;
import org.openhealthtools.mdht.mdmi.model.Bag;
import org.openhealthtools.mdht.mdmi.model.Choice;
import org.openhealthtools.mdht.mdmi.model.DTCChoice;
import org.openhealthtools.mdht.mdmi.model.DTCStructured;
import org.openhealthtools.mdht.mdmi.model.LeafSyntaxTranslator;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.Node;

/** A mapping between datatypes and syntax nodes */
public class DatatypeToNodeMap {
	
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");

	private HashMap<IDatatypeField, Node> m_nodeMap = new HashMap<IDatatypeField, Node>();

	private IDatatypeField	m_rootTreeNode;
	private Node		   	m_rootSyntaxNode;
	
	private List<String> m_errors = new ArrayList<String>();
	
	public DatatypeToNodeMap(IDatatypeField treeNode, Node syntaxNode) {
		m_rootTreeNode   = treeNode;
		m_rootSyntaxNode = syntaxNode;
	}

	/** Create the map based on the parameters supplied to the constructor.
	 * 
	 * @param modify	If true, update the syntax nodes as necessary.
	 * @parem parent	If <i>modify</i> is true, this is the component for showing confirmation dialogs
	 * @return	An error list
	 */
	public List<String> createMap(boolean modify, Component parent) {
		synchronized (m_errors) {
			m_errors.clear();
			buildMap(m_rootTreeNode, null, m_rootSyntaxNode, modify, parent);
			return m_errors;	
		}
	}
	
	public Node getNodeForDatatype(IDatatypeField datatype) {
		return m_nodeMap.get(datatype);
	}

	public List<String> getErrorMessages() {
		return m_errors;
	}

	// get a class name for error handling
	private static String getClassName(Object object) {
		if (object == null) {
			return "null";
		}
		return ClassUtil.beautifyName(object.getClass());
	}

	/** add mapping between a Datatype node and the corresponding Syntax Node */
	private void buildMap(IDatatypeField datatypeTreeNode, Node parentSyntaxNode, Node syntaxNode,
			boolean createIfMissing, Component parent ) {
		boolean isValid = true;
		
		if (datatypeTreeNode == null) {
			return;
		}
		MdmiDatatype datatype = datatypeTreeNode.getDatatype();
		String fieldName = datatypeTreeNode.getFieldName();

		// 1. Does syntax node exist
		if (syntaxNode == null) {
			// Create if we should
			if (createIfMissing) {
				syntaxNode = createNewSyntaxNode(datatypeTreeNode, parentSyntaxNode);
				
			} else {
				// no node defined (and not creating) - There is no syntax element for xxx
				String errMsg = MessageFormat.format(s_res.getString("DatatypeToNodeMap.noNodeForDatatypeMsg"),
						DatatypeTree.formatFieldName(datatype, fieldName));
				m_errors.add(errMsg);
			}
		}

		if (syntaxNode == null) {
			isValid = false;

		} else {
			// 2. is it correct type
			Node newNode = verifySyntaxNodeType(datatype, syntaxNode, createIfMissing, parent);
			if (newNode == null) {
				isValid = false;
			} else {
				// node may have changed
				syntaxNode = newNode;
			}

			// map even if wrong type
			m_nodeMap.put(datatypeTreeNode, syntaxNode);
		}

		// mark tree to show error
		((EditableObjectNode)datatypeTreeNode).setHasError(!isValid);

		if (isValid) {
			// 3. check fields
			refreshChildrenMap(datatypeTreeNode, syntaxNode, createIfMissing, parent);
		}
	}

	/** update map for children of this datatype */
	private void refreshChildrenMap(IDatatypeField datatypeTreeNode,
			Node syntaxNode, boolean createIfMissing, Component parent) {
		for (int i=0; i<datatypeTreeNode.getChildCount(); i++) {
			if (datatypeTreeNode.getChildAt(i) instanceof IDatatypeField) {
				IDatatypeField childTreeNode = (IDatatypeField)datatypeTreeNode.getChildAt(i);
				// find child node with this field name
				Node childNode = findChildWithFieldname(syntaxNode, childTreeNode.getFieldName());
				// validate it
				buildMap(childTreeNode, syntaxNode, childNode, createIfMissing, parent);
			}
		}
	}
	
	/** Create a new syntaxNode to match this datatype and add it to the parent */
	private Node createNewSyntaxNode(IDatatypeField datatypeTreeNode, Node parentSyntaxNode) {
		MdmiDatatype datatype = datatypeTreeNode.getDatatype();
		String fieldName = datatypeTreeNode.getFieldName();
		
		Node syntaxNode = createNodeForDatatype(datatype);
		if (syntaxNode != null) {
			// name it with the field name
			syntaxNode.setName(fieldName);
			// set fieldName
			syntaxNode.setFieldName(fieldName);
			// add to parent syntax node and to tree
			if (parentSyntaxNode instanceof Choice) {
				((Choice)parentSyntaxNode).addNode(syntaxNode);
				syntaxNode.setParentNode(parentSyntaxNode);
			} else if (parentSyntaxNode instanceof Bag) {
				((Bag)parentSyntaxNode).addNode(syntaxNode);
				syntaxNode.setParentNode(parentSyntaxNode);
			} else {
				// wrong parent type
				//Unable to add a new <childType> to a <parentType> syntax element.
				String errMsg = MessageFormat.format(s_res.getString("DatatypeToNodeMap.wrongParentTypeMsg"),
						getClassName(syntaxNode),  getClassName(parentSyntaxNode));
				m_errors.add(errMsg);
				return null;
			}

			// notify
			SelectionManager.getInstance().setUpdatesPending();
			SelectionManager.getInstance().notifyCollectionChangeListeners(syntaxNode.getClass());
		   
		   
			// add node to MDMITree
			if (!updateMdmiTree( syntaxNode) ) {
				return null;
			}

			
		} else {
			// cannot create node
			// Cannot create a syntax element for <fieldName> (<dataType>)
			String errMsg = MessageFormat.format(s_res.getString("DatatypeToNodeMap.cannotCreateNodeMsg"),
					DatatypeTree.formatFieldName(datatype, fieldName), getClassName(datatype));
			m_errors.add(errMsg);
		}
		
		return syntaxNode;
	}
	
	/** Verify the syntax node type.
	 * Show an error message if the node type is not correct, and allow the user to fix it. */
	private Node verifySyntaxNodeType(MdmiDatatype datatype, Node syntaxNode,
			boolean createIfMissing, Component parent) {
		Class<?> expectedClass = getExpectedNodeClass(datatype);

		if (syntaxNode.getClass().equals(expectedClass)) {
			// Type is good
			return syntaxNode;
		}
		
		// Need to change type
		Node newSyntaxNode = null;
		// Existing syntax element, {name}, is a {nodeType}. The correct element type for a {type} datatype is {nodeType}.
		String msg = MessageFormat.format(s_res.getString("DatatypeToNodeMap.wrongNodeTypeMsg"), 
				syntaxNode.getName(), getClassName(syntaxNode), 
				getClassName(datatype), ClassUtil.beautifyName(expectedClass) );
		if (createIfMissing) {
			// try to fix
			int opt = JOptionPane.showConfirmDialog(parent == null ? getMdmiModelTree() : parent,
					// ... Do you want to change it to {expectedType}?
					new WrappingDisplayText(MessageFormat.format(s_res.getString("DatatypeToNodeMap.changeNodeTypeMsg"), 
							msg, ClassUtil.beautifyName(expectedClass))),
						s_res.getString("DatatypeToNodeMap.changeNodeTypeTitle"),
						JOptionPane.YES_NO_OPTION);
			
			if (opt == JOptionPane.YES_OPTION) {
				// call tree utility to change 
				EditableObjectNode treeNode = (EditableObjectNode) findTreeNodeForUserObject(syntaxNode);
				if (treeNode == null) {

					// Unable to find {type} {name} in the selection tree. The selection tree has not been updated.
					String errMsg = MessageFormat.format(s_res.getString("DatatypeToNodeMap.cannotFindTreeNodeMsg"),
							getClassName(syntaxNode), syntaxNode.getName());
					m_errors.add(errMsg);
					return null;
				}
				
				EditableObjectNode newTreeNode = ChangeTypeDialog.changeClassType(treeNode, expectedClass);
				if (newTreeNode != null) {
					newSyntaxNode = (Node)newTreeNode.getUserObject();
				} else {
					// Unable to change <nodeName> from a <nodeType> to a <nodeType>
					String errMsg = MessageFormat.format(s_res.getString("DatatypeToNodeMap.cannotChangeTypeMsg"),
							syntaxNode.getName(), getClassName(syntaxNode), ClassUtil.beautifyName(expectedClass));
					m_errors.add(errMsg);
				}
			} else {
				// user indicated no change
				m_errors.add(msg);
			}

		} else {
			//not supposed to change
			m_errors.add(msg);
		}
		
		return newSyntaxNode;
		
	}
	
	private MdmiModelTree getMdmiModelTree() {
		return SelectionManager.getInstance().getEntitySelector();
	}

	/** Find this user object in the selection tree */
	private DefaultMutableTreeNode findTreeNodeForUserObject(Object userObject) {
		MdmiModelTree entitySelector = getMdmiModelTree();
		DefaultMutableTreeNode treeNode = entitySelector.findNode(userObject);
		return treeNode;
	}
	
	/** Update the MDMITree when a node is added */
	private boolean updateMdmiTree(Node syntaxNode) {
		Node parentSyntaxNode = syntaxNode.getParentNode();

		MdmiModelTree entitySelector = getMdmiModelTree();
		DefaultTreeModel treeModel = (DefaultTreeModel)entitySelector.getMessageElementsTree().getModel();
		
		DefaultMutableTreeNode parentTreeNode = findTreeNodeForUserObject(parentSyntaxNode);
		if (parentTreeNode instanceof EditableObjectNode) {
			((EditableObjectNode)parentTreeNode).addSorted(SyntaxNodeNode.createSyntaxNode(syntaxNode));
			// Fire Model Change
			treeModel.nodeStructureChanged(parentTreeNode);
			return true;
		} else {
			// Unable to find {type} {name} in the selection tree. The selection tree has not been updated.
			String errMsg = MessageFormat.format(s_res.getString("DatatypeToNodeMap.cannotFindTreeNodeMsg"),
					getClassName(parentSyntaxNode), parentSyntaxNode.getName());
			m_errors.add(errMsg);
			
			return false;
		}
	}

	/** find the child node that matches this field name */
	private Node findChildWithFieldname(Node parent, String fieldName) {
		Collection<Node> children = new ArrayList<Node>();

		if (parent instanceof Choice) {
			children = ((Choice)parent).getNodes();
		} else if (parent instanceof Bag) {
			children = ((Bag)parent).getNodes();
		}

		// search children for fieldName
		for (Node child : children) {
			if (fieldName.equals(child.getFieldName())) {
				return child;
			}
		}
		return null;
	}

	private Node createNodeForDatatype(MdmiDatatype datatype) {
		Node node = null;
		Class<? extends Node> nodeClass = getExpectedNodeClass(datatype);
		try {
			node = nodeClass.getConstructor().newInstance();
		} catch (Exception e) {
			SelectionManager.getInstance().getStatusPanel().writeException(e);
		}
		return node;
	}

	private Class<? extends Node> getExpectedNodeClass(MdmiDatatype datatype) {
		Class<? extends Node> nodeClass = LeafSyntaxTranslator.class;
		if (datatype instanceof DTCChoice) {
			nodeClass = Choice.class;
		} else if (datatype instanceof DTCStructured) {
			nodeClass = Bag.class;
		}
		return nodeClass;
	}

}
