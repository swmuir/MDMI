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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.editor.AbstractComponentEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.BooleanField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.DataEntryFieldInfo;
import org.openhealthtools.mdht.mdmi.editor.map.editor.DataFormatException;
import org.openhealthtools.mdht.mdmi.editor.map.editor.GenericEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.IEditorField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.URIField;
import org.openhealthtools.mdht.mdmi.editor.map.tools.GenerateTypeSpecDialog;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ViewDatatype;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ViewDatatypeTree;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ViewDatatypeUsageTree;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.DTExternal;
import org.openhealthtools.mdht.mdmi.model.DTSDerived;
import org.openhealthtools.mdht.mdmi.model.DTSPrimitive;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;

/** Node for simple data types. Complex and Enumeration types are handled in their own class */
public class DataTypeNode extends EditableObjectNode {

	public DataTypeNode(MdmiDatatype datatype) {
		super(datatype);
		if (datatype instanceof DTSDerived) {
			setNodeIcon(TreeNodeIcon.DerivedDataTypeIcon);
		} else {
			setNodeIcon(TreeNodeIcon.DataTypeIcon);
		}
	}


	@Override
	public String getDisplayName(Object userObject) {
		if (userObject == null) {
			return "";
		}
		return ((MdmiDatatype)userObject).getTypeName();
	}

	@Override
	public String getToolTipText() {
		return ((MdmiDatatype)getUserObject()).getDescription();
	}


	@Override
	public boolean isEditable() {
		return true;
	}
	
	@Override
	public AbstractComponentEditor getEditorForNode() {
		return new CustomEditor(getMessageGroup(), getUserObject().getClass());
	}

	@Override
	public boolean isRemovable() {
		// primitives cannot be removed or copied
		return !(getUserObject() instanceof DTSPrimitive);
	}

	/** Can this object's type be changed */
	@Override
	public boolean canChangeType() {
		// cannot change primitives
		if (getUserObject() instanceof DTSPrimitive) {
			return false;
		}
		return super.canChangeType();
	}

	/** Add a menu to show the datatype in a new view */
	@Override
	public List<JComponent> getAdditionalPopuMenus() {
		List<JComponent> menus = super.getAdditionalPopuMenus();
		if (menus == null) {
			menus = new ArrayList<JComponent>();
		}
		
		final MdmiDatatype datatype = (MdmiDatatype)getUserObject();
		
		// Graphical view
		menus.add(new JMenuItem(new AbstractAction(MessageFormat.format(s_res.getString("DataTypeNode.viewDatatype"),
				getDisplayType())) {
			@Override
			public void actionPerformed(ActionEvent e) {
				ViewDatatype view = new ViewDatatype(datatype);
				view.setVisible(true);
			}
			
		}));

		// Usage
		menus.add(new JMenuItem(new AbstractAction(MessageFormat.format(s_res.getString("DataTypeNode.viewUsage"),
				getDisplayType())) {
			@Override
			public void actionPerformed(ActionEvent e) {
				ViewDatatypeUsageTree view = new ViewDatatypeUsageTree(datatype);
				view.setVisible(true);
			}

		}));

		// Tree Hierarchy (complex only)
		if (datatype instanceof DTComplex) {
			menus.add(new JMenuItem(new AbstractAction(s_res.getString("DataTypeNode.viewTypeHierarchy")) {
				@Override
				public void actionPerformed(ActionEvent e) {
					ViewDatatypeTree view = new ViewDatatypeTree(datatype);
					view.setVisible(true);
				}

			}));
		}
		return menus;
	}

	// indicate object was imported 
	@Override
	public boolean isImported() {
		if (super.isImported()) {
			return true;
		}
		if (getUserObject() instanceof DTSPrimitive) {
			return false;
		}
		// if read-only, treat as imported
		return ((MdmiDatatype)getUserObject()).isReadonly();
	}
	
	@Override
	public void setImported(boolean imported) {
		// make read-only
		if (imported) {
			((MdmiDatatype)getUserObject()).setReadonly(true);
		}

		super.setImported(imported);
	}
	

	
	@Override
	public Icon getNodeIcon() {
		Icon icon =  super.getNodeIcon();
		// add an "R" if referent
		if (icon != null && isReferenceType()) {
			icon = new ReferentIcon(icon);
		}
		return icon;
	}


	/** Is this datatype an attribute of an MdmiBusinessElementReference in the message group
	 **/
	public  boolean isReferenceType() {
		MdmiDatatype dataType = (MdmiDatatype)getUserObject();
		return DataTypeSetNode.isReferenceType(dataType, dataType.getOwner());
	}

	//////////////////////////////////////////////////////////////////
	//    Custom Classes
	//////////////////////////////////////////////////////////////
	public class CustomEditor extends GenericEditor {
		private BooleanField m_referentField;
		
		CustomEditor(MessageGroup group, Class<?> objectClass) {
			super(group, objectClass);
		}

		@Override
		public boolean isReadOnlyFields(String fieldName) {
			// look at read-only flag
			if (!SelectionManager.getInstance().isReferentIndexEditingAllowed() &&
					((MdmiDatatype)getUserObject()).isReadonly()) {
				return true;
			} else if (getUserObject() instanceof DTSPrimitive) {
				return true;
			}
			return super.isReadOnlyFields(fieldName);
		}

		@Override
		protected void createDataEntryFields(List<Method[]> methodPairList) {
			
			// Add a field to show if Isomorphic
			m_referentField = new BooleanField(this, "Referent");
			try {
				boolean isRef = isReferenceType();
				if (isRef) {
					// make it stand out
					m_referentField.setForeground(Color.red);
					Font font = m_referentField.getFont();
					font = font.deriveFont(Font.BOLD);
					m_referentField.setFont(font);
				}
				m_referentField.setDisplayValue(Boolean.valueOf(isRef));
			} catch (DataFormatException e) {
				// don't care
			}
			m_referentField.setReadOnly();
			addLabeledField(null, m_referentField, 0.0, GridBagConstraints.NONE);

			super.createDataEntryFields(methodPairList);
		}

		/** Add a wizard for TypeSpec */
		@Override
		protected IEditorField createEditorField(DataEntryFieldInfo fieldInfo) {
			if (getUserObject() instanceof DTExternal) {
				if ("TypeSpec".equalsIgnoreCase(fieldInfo.getFieldName())) {
					// Add a Wizard button
					return new TypeSpecField(this);
				}
				
			}
			
			return super.createEditorField(fieldInfo);
		}
		
		protected class TypeSpecField extends JPanel implements IEditorField, ActionListener {
			private URIField m_uriField = null;
			private JButton m_wizButton;
			
			TypeSpecField(GenericEditor parentEditor) {
				setLayout(new BorderLayout(Standards.RIGHT_INSET, 0));
				m_uriField = new URIField(parentEditor);
				m_wizButton = new JButton();	// todo  just use icon with tooltip
				m_wizButton.setIcon(AbstractComponentEditor.getIcon(this.getClass(),
						DataTypeNode.s_res.getString("TypeSpecField.icon")));
				add(BorderLayout.CENTER, m_uriField);
				add(BorderLayout.EAST, m_wizButton);
			}
			
			

			@Override
			public void addNotify() {
				super.addNotify();
				m_wizButton.addActionListener(this);
				m_wizButton.setToolTipText(DataTypeNode.s_res.getString("TypeSpecField.toolTip"));
			}



			@Override
			public void removeNotify() {
				m_wizButton.removeActionListener(this);
				m_wizButton.setToolTipText(null);
				super.removeNotify();
			}



			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == m_wizButton) {

					if (SelectionManager.getInstance().getEntityEditor().acceptEdits(getUserObject())) {
						Frame frame = SystemContext.getApplicationFrame();
						GenerateTypeSpecDialog dlg = new GenerateTypeSpecDialog(frame, (DTExternal)getUserObject());
						dlg.display(frame);
					}
				}
				
			}

			@Override
			public Object getValue() throws DataFormatException {
				return m_uriField.getValue();
			}

			@Override
			public void setDisplayValue(Object value)
					throws DataFormatException {
				m_uriField.setDisplayValue(value);
			}

			@Override
			public void setReadOnly() {
				m_uriField.setReadOnly();
			}

			@Override
			public JComponent getComponent() {
				return this;
			}

			@Override
			public void highlightText(String text, Color highlightColor) {
				m_uriField.highlightText(text, highlightColor);
			}
		}
		
	}
}
