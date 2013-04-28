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

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.openhealthtools.mdht.mdmi.editor.map.tree.ComplexDataTypeNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.DataTypeNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EnumeratedDataTypeNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MdmiModelTree;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.DTSEnumerated;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;

/** A Tree view of a datatype, and all sub-types */
public class DatatypeTree extends ModelTree {
	
	/** Create the tree for this datatype */
	public DatatypeTree(MdmiDatatype datatype) {
		super(datatype);
	}
	
	/** Create a tree showing all datatypes in the message group */
	public DatatypeTree(Collection<MdmiDatatype> datatypes) {
		super(datatypes);
	}


	@SuppressWarnings("rawtypes")
	@Override
	protected DefaultMutableTreeNode createRootNode(Object model) {
		DefaultMutableTreeNode root = null;
		
		if (model instanceof MdmiDatatype) {
			// create the tree with the Datatype as the root
			root = createDataTypeNode((MdmiDatatype)model, null);
			
		} else if (model instanceof Collection) {
			// create the tree with a special root node
			root = new DefaultMutableTreeNode(s_res.getString("ViewDatatypeTree.datatypes"));
			// add each datatype
			for (Object object : (Collection)model) {
				if (object instanceof MdmiDatatype) {
					MdmiDatatype datatype = (MdmiDatatype)object;
					DefaultMutableTreeNode node = createDataTypeNode(datatype, null);
					MdmiModelTree.addSorted(root, node);
				}
			}

		}
		return root;
	}
	
	/** Create the appropriate DataTypeNode based on the data type */
	public static DataTypeNode createDataTypeNode(MdmiDatatype dataType, String fieldName) {
		if (dataType instanceof DTComplex) {
			return new ComplexDatatypeTreeNode((DTComplex)dataType, fieldName);
		} else if (dataType instanceof DTSEnumerated) {
			return new EnumeratedDatatypeTreeNode((DTSEnumerated)dataType, fieldName);
		}
		return new DatatypeTreeNode(dataType, fieldName);
	}

		
	/** Format a field and datatype as "fieldName : DataType" */
	public static String formatFieldName(MdmiDatatype datatype, String fieldName) {
		if (fieldName != null) {
			// fieldName : DataType
			return MessageFormat.format(s_res.getString("ViewDatatypeTree.fieldFormat"),
					fieldName, datatype.getTypeName());
		}
		return datatype.getTypeName();
	}

	
	/** Expand all complex datatypes (not enumerated types) within this node (and its children) */
	public void expandAllComplexTypes(DefaultMutableTreeNode node) {
		for (Enumeration<?> en = node.depthFirstEnumeration(); en != null && en.hasMoreElements();) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode)en.nextElement();
			// don't expand enumerated types
			if (!(child.getUserObject() instanceof DTSEnumerated)) {
				expandPath(new TreePath(child.getPath()));
			}
		}
	}


	///////////////////////////////////////////////////////////
	//  Tree Nodes
	///////////////////////////////////////////////////////////
	
	/** Tree Node interface for different kinds of Data Types */
	public static interface IDatatypeField extends TreeNode {
		public String getFieldName();
		public MdmiDatatype getDatatype();
	}

	public static class DatatypeTreeNode extends DataTypeNode implements IDatatypeField {
		private String m_fieldName;
		
		public DatatypeTreeNode(MdmiDatatype datatype, String fieldName) {
			super(datatype);
			m_fieldName = fieldName;
		}
		
		@Override
		public String toString() {
			return formatFieldName(getDatatype(), m_fieldName);
		}
		
		@Override
		public MdmiDatatype getDatatype() {
			return (MdmiDatatype)getUserObject();
		}
		
		@Override
		public String getFieldName() {
			return m_fieldName;
		}
	}
	
	public static class ComplexDatatypeTreeNode extends ComplexDataTypeNode
	implements IDatatypeField {
		private String m_fieldName;
		
		public ComplexDatatypeTreeNode(DTComplex datatype, String fieldName) {
			super(datatype);
			m_fieldName = fieldName;
			
			// remove default children, and load our own
			removeAllChildren();
			loadChildren(datatype);
		}
		
		private void loadChildren(DTComplex complexType) {
			for (Field field : complexType.getFields()) {
				if (field.getDatatype() == null) {
					continue;
				}
				DefaultMutableTreeNode childNode = createDataTypeNode(field.getDatatype(),
						field.getName());
				if (childNode == null) {
					continue;
				}
				// add in given order
				add(childNode);
			}
		}

		
		@Override
		public String toString() {
			return formatFieldName(getDatatype(), m_fieldName);
		}
		
		@Override
		public MdmiDatatype getDatatype() {
			return (MdmiDatatype)getUserObject();
		}
		
		@Override
		public String getFieldName() {
			return m_fieldName;
		}
	}
	
	public static class EnumeratedDatatypeTreeNode extends EnumeratedDataTypeNode 
	implements IDatatypeField {
		private String m_fieldName;
		
		public EnumeratedDatatypeTreeNode(DTSEnumerated datatype, String fieldName) {
			super(datatype);
			m_fieldName = fieldName;
		}

		
		@Override
		public String toString() {
			return formatFieldName(getDatatype(), m_fieldName);
		}

		@Override
		public MdmiDatatype getDatatype() {
			return (MdmiDatatype)getUserObject();
		}
		
		@Override
		public String getFieldName() {
			return m_fieldName;
		}
	}

}
