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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Stack;

import javax.swing.tree.DefaultMutableTreeNode;

import org.openhealthtools.mdht.mdmi.editor.common.treetable.AbstractTreeTableModel;
import org.openhealthtools.mdht.mdmi.editor.common.treetable.TreeTableModel;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.tools.DatatypeTree.IDatatypeField;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.TreeNodeIcon;
import org.openhealthtools.mdht.mdmi.model.LeafSyntaxTranslator;
import org.openhealthtools.mdht.mdmi.model.Node;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;

/** A TreeTable Model that shows the datatypes and syntax nodes that correspond to
 * a Semantic Element
 * @author Conway
 *
 */
public class DatatypeToNodeModel extends AbstractTreeTableModel {
	
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");

	// column indices
	public static final int s_dataTypeCol   = 0;
	public static final int s_syntaxNodeCol = 1;
	public static final int s_formatCol     = 2;
	public static final int s_locationCol   = 3;
	
	// Column names
	public static final String [] s_columns = new String[] {
		s_res.getString("DatatypeToNodeTable.datatypeCol"),
		s_res.getString("DatatypeToNodeTable.syntaxCol"),
		s_res.getString("DatatypeToNodeTable.formatCol"),
		s_res.getString("DatatypeToNodeTable.locationCol"),
	};

	// Preferred column widths
	public static final int [] s_columnWidths = new int [] {
		300,	// Datatype
		250,	// Syntax Node
		150,	// Format
		50		// Location
	};
	
	private List<SemanticElement> m_semanticElementList = new ArrayList<SemanticElement>();
	private Map<SemanticElement, DatatypeToNodeMap> m_seToNodeMap = new HashMap<SemanticElement, DatatypeToNodeMap>();
	
	/** Turn a single SemanticElement into a collection containing the SE and Syntax Node hierarchy */
	private static Collection<Object> makeCollection(SemanticElement semanticElement) {
		ArrayList<Object> collection = new ArrayList<Object>();
		// check parentage
		Node syntaxNode = semanticElement.getSyntaxNode();
		if (syntaxNode != null) {
			Stack<Node> nodes = new Stack<Node>();
			while (syntaxNode != null) {
				nodes.push(syntaxNode);
				// get parent
				syntaxNode = syntaxNode.getParentNode();
			} 
			
			// add each semantic element
			while (!nodes.isEmpty()) {
				syntaxNode = nodes.pop();
				if (syntaxNode.getSemanticElement() != null) {
					// add the SE
					collection.add(syntaxNode.getSemanticElement());
				} else {
					// add the node
					collection.add(syntaxNode);
				}
			}

		}
		
		if (!collection.contains(semanticElement)) {
			collection.add(semanticElement);
		}
		
		return collection;
	}

	/** Multiple Semantic Elements and Syntax Nodes */
	public DatatypeToNodeModel(Collection<?> elements) {
		
		// create a =collection node, with each element as a child
		super(createTopNode(elements));
		
		for (Object element : elements) {
			if (element instanceof SemanticElement) {
				SemanticElement semanticElement = (SemanticElement)element;
				m_semanticElementList.add(semanticElement);

				DatatypeToNodeMap nodeMap = new DatatypeToNodeMap(getTopDatatype(semanticElement), semanticElement.getSyntaxNode());
				m_seToNodeMap.put(semanticElement, nodeMap);
			}
		}
		
		validateModel(false, null);
	}

	/** Single Semantic Element */
	public DatatypeToNodeModel(SemanticElement semanticElement) {
		// create a semantic element node, with a single child datatype node (with children for each field),
		// and use that as the root of the model
		this(makeCollection(semanticElement));		
	}
	

	
	/** Create a TreeTable node for this SemanticElement */
	private static SimpleSemanticElementNode createSemanticElementNode(SemanticElement semanticElement) {
		// start with Semantic Element
		SimpleSemanticElementNode rootNode = new SimpleSemanticElementNode(semanticElement);
		if (semanticElement.getDatatype() != null) {
			rootNode.add(DatatypeTree.createDataTypeNode(semanticElement.getDatatype(), null));
		}
		
		return rootNode;
	}


	/** Create a TreeTable node with all elements as children */
	private static DefaultMutableTreeNode createTopNode(Collection<?> elementList) {

		// start with a dummy node
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Data Elements");

		DefaultMutableTreeNode prevTreeNode = null;
		
		// create a node for each SE
		for (Object element : elementList) {
			DefaultMutableTreeNode treeNode = null;
			if (element instanceof SemanticElement) {
				// Semantic Element Node - children are Datatypes
				treeNode = createSemanticElementNode((SemanticElement)element);
			} else if (element instanceof Node) {
				// Syntax Node Node - children are more Syntax Nodes
				treeNode = new SimpleSyntaxNode((Node)element);
			}
			
			if (treeNode != null) {
				if (elementList.size() == 1) {
					// if single node, just show that one
					rootNode = treeNode;
					
				} else if (prevTreeNode != null && treeNode.getUserObject() instanceof Node &&
						prevTreeNode.getUserObject() == ((Node)treeNode.getUserObject()).getParentNode()) {
					// keep syntax node hierarchy
					prevTreeNode.add( treeNode );
					
				} else {
					rootNode.add( treeNode );
				}
				
				prevTreeNode = treeNode;
			}
		}

		return rootNode;
	}
	
	
	/** Validate that the nodes and datatypes agree.
	 * 
	 * @param modify if true, the syntax nodes will be updated to match the datatypes
	 * @parem parent	used if modification is allowed to display dialogs to user 
	 * @return	a list of errors
	 */
	public List<String> validateModel(boolean modify, Component parent) {
		List<String> errors = new ArrayList<String>();
		for (SemanticElement se : m_semanticElementList) {
			DatatypeToNodeMap map = m_seToNodeMap.get(se);
			errors.addAll( map.createMap(modify, parent) );
		}
		return errors;
	}
	
	/** return any validation errors */ 
	public List<String> getValidationErrors() {
		List<String> errors = new ArrayList<String>();
		for (SemanticElement se : m_semanticElementList) {
			DatatypeToNodeMap map = m_seToNodeMap.get(se);
			errors.addAll( map.getErrorMessages() );
		}
		return errors;
	}

	public Node getNodeForDatatype(IDatatypeField datatype) {
		// search all maps
		for (DatatypeToNodeMap map : m_seToNodeMap.values()) {
			Node node = map.getNodeForDatatype(datatype);
			if (node != null) {
				return node;
			}
		}
		return null;
	}
	
	@Override
	public int getColumnCount() {
		return s_columns.length;
	}

	@Override
	public String getColumnName(int column) {
		return s_columns[column];
	}

	@Override
	public Class<?> getColumnClass(int column) {
		if (column == 0) {
			return TreeTableModel.class;
		}
		return String.class;
	}


	@Override
	public Object getValueAt(Object node, int column) {
		Object value = "";
		if (column == s_dataTypeCol) {
			// first column is the tree node
			value = node;
			

		} else {
			// other columns - look like a table
			Node syntaxNode = null;
			if (node instanceof SimpleSyntaxNode) {
				syntaxNode = (Node)((SimpleSyntaxNode)node).getUserObject();
				
			} else if (node instanceof IDatatypeField) {
				// determine syntax node
				syntaxNode = getNodeForDatatype((IDatatypeField)node);
			}
			
			if (syntaxNode != null) {
				if (column == s_syntaxNodeCol) {
					value = syntaxNode;

				} else if (column == s_formatCol) {
					if (syntaxNode instanceof LeafSyntaxTranslator) {
						value = ((LeafSyntaxTranslator)syntaxNode).getFormat();
					}

				} else if (column == s_locationCol) {
					if (syntaxNode instanceof LeafSyntaxTranslator) {
						value = ((LeafSyntaxTranslator)syntaxNode).getLocation();
					}
				}
			}
		}

		if (value == null) {
			value = "";
		}
		return value;
	}

   @Override
   public void setValueAt(Object value, Object node, int column) {
	   if (node instanceof IDatatypeField) {
		   // determine syntax node
		   Node syntaxNode = getNodeForDatatype((IDatatypeField)node);
		   if (syntaxNode instanceof LeafSyntaxTranslator) {
			   LeafSyntaxTranslator leaf = (LeafSyntaxTranslator)syntaxNode;
			   if (value == null) {
				   value = "";
			   }
			   if (column == s_formatCol) {
				   if (!value.equals(leaf.getFormat())) {
					   leaf.setFormat(value.toString());
					   // notify listeners
					   notifyOnChange(syntaxNode);
				   }

			   } else if (column == s_locationCol) {
				   if (!value.equals(leaf.getLocation())) {
					   leaf.setLocation(value.toString());
					   // notify listeners
					   notifyOnChange(syntaxNode);
				   }
			   }
		   }
	   }
   }
   
   /** Notify listeners about a change to the data model */
   private void notifyOnChange(Node node) {
		SelectionManager.getInstance().setUpdatesPending();
		SelectionManager.getInstance().notifyModelChangeListeners(node);
   }

	@Override
	public Object getChild(Object parent, int index) {
		return ((DefaultMutableTreeNode)parent).getChildAt(index);
	}

	@Override
	public int getChildCount(Object parent) {
		return ((DefaultMutableTreeNode)parent).getChildCount();
	}

	private IDatatypeField getTopDatatype(SemanticElement se) {
		Object root = getRoot();
		// root is either a Semantic element, or contains Semantic Elements
		if (root instanceof SimpleSemanticElementNode) {
			// first child
			return getDatatypeChild((SimpleSemanticElementNode)root);
			
		} else if (root instanceof DefaultMutableTreeNode) {
			// look at each SE
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)root;
			for (int i=0; i<treeNode.getChildCount(); i++) {
				DefaultMutableTreeNode seNode = (DefaultMutableTreeNode)treeNode.getChildAt(i);
				if (seNode.getUserObject() == se) {
					return getDatatypeChild(seNode);
				}
			}
		}
		return null;
	}
	
	private IDatatypeField getDatatypeChild(DefaultMutableTreeNode node) {
		// should be first child
		if (node.getChildCount() > 0 && node.getChildAt(0) instanceof IDatatypeField) {
			return (IDatatypeField)node.getChildAt(0);
		}
		return null;
	}

	@Override
	public boolean isCellEditable(Object node, int column) {
		// Location and Format are editable if syntax node is a LST
		if (column == s_formatCol || column == s_locationCol) {
			if (node instanceof IDatatypeField && 
					getNodeForDatatype((IDatatypeField)node) instanceof LeafSyntaxTranslator) {
				return true;
			}
			return false;
		}
		return super.isCellEditable(node, column);
	}
	
	/** Tree Node for SemanticElement */
	public static class SimpleSemanticElementNode extends EditableObjectNode {

		public SimpleSemanticElementNode(SemanticElement elem) {
			super(elem);
			setNodeIcon(TreeNodeIcon.SemanticElementIcon);
		}

		@Override
		public String getDisplayName(Object userObject) {
			return ((SemanticElement)userObject).getName();
		}
		
	}

	/** Tree Node for SyntaxNode */
	public static class SimpleSyntaxNode extends EditableObjectNode {

		public SimpleSyntaxNode(Node elem) {
			super(elem);
			setNodeIcon(TreeNodeIcon.getTreeIcon(elem.getClass()));
		}

		@Override
		public String getDisplayName(Object userObject) {
			return ((Node)userObject).getName();
		}
		
	}

}
