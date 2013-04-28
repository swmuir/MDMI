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

import java.text.MessageFormat;
import java.util.List;

import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.editor.AbstractComponentEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.DataEntryFieldInfo;
import org.openhealthtools.mdht.mdmi.editor.map.editor.GenericEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.IEditorField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.IntegerField;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.validate.ModelInfo;

public class FieldNode extends EditableObjectNode {

	public FieldNode(Field field) {
		super(field);
		setNodeIcon(TreeNodeIcon.FieldIcon);
	}
	
	@Override
	public String getDisplayName(Object userObject) {
		return ((Field)userObject).getName();
	}

	@Override
	public String getToolTipText() {
		return ((Field)getUserObject()).getDescription();
	}


	
	@Override
	public String toString() {
		Field field = (Field)getUserObject();
		String display = getDisplayName();
		if (field.getDatatype() != null) {
			// Field : Datatype
			display = MessageFormat.format(s_res.getString("EditableObjectNode.relationshipFormat"),
					display, field.getDatatype().getTypeName()); //field.getDatatype().getName());
		}
		return display;
	}
	
	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public boolean isRemovable() {
		return true;
	}

	@Override
	public AbstractComponentEditor getEditorForNode() {
		return new CustomEditor(getMessageGroup(), getUserObject().getClass());
	}
	


	// treat as imported if parent read-only
	@Override
	public boolean isImported() {
		if (super.isImported()) {
			return true;
		}
		// if parent type is read-only, treat as imported
		MdmiDatatype datatype = ((Field)getUserObject()).getOwnerType();
		return (datatype != null && datatype.isReadonly());
	}
	
	////////////////////////////////////////////
	
	public class CustomEditor extends GenericEditor {

		public CustomEditor(MessageGroup group, Class<?> objectClass) {
			super(group, objectClass);
		}

		/** Determine if this field should be shown read-only */
		@Override
		public boolean isReadOnlyFields(String fieldName) {
			// look at read-only flag on parent
			if (!SelectionManager.getInstance().isReferentIndexEditingAllowed()) {
				MdmiDatatype datatype = ((Field)getUserObject()).getOwnerType();
				if (datatype != null && datatype.isReadonly()) {
					return true;
				}
			}
			// OwnerType is read-only
			return "OwnerType".equalsIgnoreCase(fieldName);
		}

		/** Add "Unbounded" box to Max */
		@Override
		protected IEditorField createEditorField(DataEntryFieldInfo fieldInfo) {
			if ("MaxOccurs".equalsIgnoreCase(fieldInfo.getFieldName())) {
				IEditorField field = super.createEditorField(fieldInfo);
				if (field instanceof IntegerField) {
					((IntegerField)field).addUnboundedBox();
				}
				return field;
			}
			return super.createEditorField(fieldInfo);
		}


		@Override
		public List<ModelInfo> validateModel() {
			List<ModelInfo> errors = super.validateModel();
			
			// check min and max
			Field field = (Field)getEditObject();
			if (field.getMaxOccurs() < field.getMinOccurs()) {
				errors.add( new ModelInfo(field, "maxOccurs", 
						// maxOccurs must be >= minOccurs"
						MessageFormat.format(EditableObjectNode.s_res.getString("EditableObjectNode.minMaxError"),
								"maxOccurs", "minOccurs")
						) );
			}
			
			return errors;
		}
		
	}
}
