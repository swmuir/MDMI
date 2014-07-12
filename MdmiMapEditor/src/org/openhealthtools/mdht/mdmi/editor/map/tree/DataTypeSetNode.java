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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.openhealthtools.mdht.mdmi.editor.map.Actions;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ViewDatatypeTree;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ViewDatatypeUsageSummary;
import org.openhealthtools.mdht.mdmi.model.DTCChoice;
import org.openhealthtools.mdht.mdmi.model.DTCStructured;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.DTExternal;
import org.openhealthtools.mdht.mdmi.model.DTSDerived;
import org.openhealthtools.mdht.mdmi.model.DTSEnumerated;
import org.openhealthtools.mdht.mdmi.model.DTSPrimitive;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MdmiDomainDictionaryReference;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;

/** Node for a collection of MdmiDatatypes */
public class DataTypeSetNode extends EditableObjectNode {
	private static final String s_display = s_res.getString("DataTypeSetNode.displayName");

	private Collection<NewObjectInfo> m_newObjectInfo = null;
	
	private MessageGroup m_messageGroup = null;

	public DataTypeSetNode(MessageGroup group) {
		super(group);
		setNodeIcon(TreeNodeIcon.DataTypeSetIcon);
		setDisplayType(s_display);
		m_messageGroup = group;
		
		loadChildren(group);
	}
	
	private void loadChildren(MessageGroup group) {
		// pre-defined types
		List<? extends MdmiDatatype> preDefinedTypes = Arrays.asList(DTSPrimitive.ALL_PRIMITIVES);
		for (MdmiDatatype dataType : preDefinedTypes) {
			if (!group.getDatatypes().contains(dataType)) {
				// add to the message group if not already present
				group.addDatatype(dataType);
			}
			DataTypeNode dataTypeNode = createDataTypeNode(dataType);
			addSorted(dataTypeNode);
		}
		
		// others
		for (MdmiDatatype dataType : group.getDatatypes()) {
			if (dataType == null) {
				continue;
			}
			if (preDefinedTypes.contains(dataType)) {
				// ignore pre-defined types
				continue;
			}
			DataTypeNode dataTypeNode = createDataTypeNode(dataType);
			addSorted(dataTypeNode);
		}
		
	}
	
	/** Is this datatype an attribute of an MdmiBusinessElementReference */
	public static boolean isReferenceType(MdmiDatatype datatype, MessageGroup messageGroup) {
		if (messageGroup == null || datatype == null) {
			return false;
		}
		MdmiDomainDictionaryReference dictionary = messageGroup.getDomainDictionary();
		if (dictionary != null) {
			for (MdmiBusinessElementReference ref : dictionary.getBusinessElements()) {
				if (ref.getReferenceDatatype() == datatype) {
					return true;
				}
			}
		}
		return false;
	}
	
	/** create the appropriate DatatTypeNode based on the datatype */
	private DataTypeNode createDataTypeNode(MdmiDatatype dataType) {
		if (dataType instanceof DTComplex) {
			return new ComplexDataTypeNode((DTComplex)dataType);
		} else if (dataType instanceof DTSEnumerated) {
			return new EnumeratedDataTypeNode((DTSEnumerated)dataType);
		}
		return new DataTypeNode(dataType);
	}

	@Override
	public String getDisplayName(Object userObject) {
		return s_display;
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public boolean isRemovable() {
		return false;
	}

	@Override
	public void deleteChild(MutableTreeNode child) {
		// remove from parent model
		MdmiDatatype dataType = (MdmiDatatype)((DefaultMutableTreeNode)child).getUserObject();
		m_messageGroup.getDatatypes().remove(dataType);
		
		super.remove(child);
	}

	/** What new items can be created */
	@Override
	public Collection<NewObjectInfo> getNewObjectInformation(boolean changeType) {
		if (m_newObjectInfo == null) {
			m_newObjectInfo = super.getNewObjectInformation(changeType);
			
			List<Class<? extends MdmiDatatype>> subclasses = new ArrayList<Class<? extends MdmiDatatype>>();
			subclasses.add(DTCChoice.class);
			subclasses.add(DTCStructured.class);
			subclasses.add(DTExternal.class);
			subclasses.add(DTSDerived.class);
			subclasses.add(DTSEnumerated.class);
			for (Class<? extends MdmiDatatype> subclass : subclasses) {
				m_newObjectInfo.add(new NewDataType(subclass));
			}
		}
		
		return m_newObjectInfo;
	}

	/** Add a menu to show the datatype in a new view */
	@Override
	public List<JComponent> getAdditionalPopuMenus() {
		List<JComponent> menus = super.getAdditionalPopuMenus();
		if (menus == null) {
			menus = new ArrayList<JComponent>();
		}

		// Create enumerated type
		JMenuItem menuItem = new JMenuItem(Actions.getActionInstance(Actions.CREATE_ENUM_ACTION));
		menus.add(menuItem);
		
		// Tree
		menus.add(new JMenuItem(new AbstractAction(s_res.getString("DataTypeNode.viewTypeHierarchy")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				ViewDatatypeTree view = new ViewDatatypeTree(m_messageGroup.getDatatypes());
				view.setVisible(true);
			}
			
		}));
		// Usage Summary
		menus.add(new JMenuItem(new AbstractAction(s_res.getString("DataTypeSetNode.usageSummary")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				ViewDatatypeUsageSummary usage = new ViewDatatypeUsageSummary(m_messageGroup.getDatatypes());
				usage.setVisible(true);
			}
		}));
		return menus;
	}

	///////////////////////////////////////////////////////////////
	//    Custom Classes
	//////////////////////////////////////////////////////////////
	
	public class NewDataType extends NewObjectInfo {
		private Class<? extends MdmiDatatype> m_datatypeClass;
		
		public NewDataType(Class<? extends MdmiDatatype> datatypeClass) {
			super(ClassUtil.beautifyName(datatypeClass));
			m_datatypeClass = datatypeClass;
		}

		@Override
		public EditableObjectNode addNewChild(Object childObject) {
			MdmiDatatype datatype = (MdmiDatatype)childObject;
			m_messageGroup.addDatatype(datatype);
			datatype.setOwner( m_messageGroup );
			
			EditableObjectNode treeNode = createDataTypeNode(datatype);
			return treeNode;
		}

		@Override
		public Class<?> getChildClass() {
			return m_datatypeClass;
		}

		@Override
		public String getChildName(Object childObject) {
			return ((MdmiDatatype)childObject).getTypeName();
		}

		@Override
		public void setChildName(Object childObject, String newName) {
			((MdmiDatatype)childObject).setTypeName(newName);
		}
	}
}
