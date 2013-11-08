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

import java.awt.GridBagConstraints;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.editor.AbstractComponentEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.DataEntryFieldInfo;
import org.openhealthtools.mdht.mdmi.editor.map.editor.MdmiDatatypeField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.MdmiDatatypeListField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.RuleField;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.DataRule;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.MessageModel;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.SemanticElementSet;
import org.openhealthtools.mdht.mdmi.model.validate.ModelInfo;

/** Node for simple data types. Complex and Enumeration types are handled in their own class */
public class DataRuleNode extends EditableObjectNode {

	public DataRuleNode(DataRule dataRule) {
		super(dataRule);
		setNodeIcon(TreeNodeIcon.DataRuleIcon);
	}

	@Override
	public String getDisplayName(Object userObject) {
		return ((DataRule)userObject).getName();
	}

	@Override
	public String getToolTipText() {
		return ((DataRule)getUserObject()).getDescription();
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
		return true;
	}

	@Override
	public String getRulesExpressionLanguage() {
		String language = ((DataRule)userObject).getRuleExpressionLanguage();
		if (language == null || language.length() == 0) {
			language = getDefaultRulesExpressionLanguage();
		}
		return language;
	}

	public String getDefaultRulesExpressionLanguage() {
		String language = super.getRulesExpressionLanguage();
		return language;
	}



	@Override
	public Object copyUserObject() throws IllegalArgumentException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException {
		Object newObject = super.copyUserObject();
		if (newObject instanceof DataRule) {
			// manually copy data types
			((DataRule) newObject).getDatatypes().addAll(((DataRule)getUserObject()).getDatatypes());
		}
		return newObject;
	}



	//////////////////////////////////////////////////////////////////
	//    Custom Classes
	//////////////////////////////////////////////////////////////
	public class CustomEditor extends AbstractRuleEditor implements PropertyChangeListener {
		private MdmiDatatypeListField m_datatypeList;
		
		public CustomEditor(MessageGroup group, Class<?> objectClass) {
			super(group, objectClass);
		}

		/** Determine if this field should be shown read-only */
		@Override
		public boolean isReadOnlyFields(String fieldName) {
			// SemanticElement is read-only
			return "SemanticElement".equalsIgnoreCase(fieldName);
		}


		@Override
		public void addNotify() {
			super.addNotify();
			if (m_datatypeList != null) {
				m_datatypeList.addPropertyChangeListener(MdmiDatatypeListField.NUM_SELECTIONS_PROPERTY, this);
			}
		}

		@Override
		public void removeNotify() {
			if (m_datatypeList != null) {
				m_datatypeList.removePropertyChangeListener(MdmiDatatypeListField.NUM_SELECTIONS_PROPERTY, this);
			}
			super.removeNotify();
		}

		/** We need to create a special field for selecting one or more DataTypes */
		@Override
		protected void createDataEntryFields(List<Method[]> methodPairList) {
			super.createDataEntryFields(methodPairList);
			
			// create data entry field for MdmiDatatype list
			String fieldName = "Datatypes";
			String getMethodName = "get" + fieldName;
			Method getMethod = null;
			try {
				getMethod = DataRule.class.getMethod(getMethodName);
			} catch (Exception e) {
				SelectionManager.getInstance().getStatusPanel().writeException(e);
			}
			
			if (getMethod != null) {
				DataEntryFieldInfo fieldInfo = new DataTypeFieldInfo(fieldName, getMethod,
						null,	// there's no corresponding set method
						getMethod.getReturnType());
				
				Collection<MdmiDatatype> dataTypes = getAllDatatypes();
				m_datatypeList = new MdmiDatatypeListField(CustomEditor.this, dataTypes);
				

				// add to layout 
				addLabeledField(ClassUtil.beautifyName(fieldInfo.getFieldName()),
						m_datatypeList, 10.0, GridBagConstraints.BOTH);

				fieldInfo.setEditComponent(m_datatypeList);
				addDataEntryFieldInfo(fieldInfo);
			}
		}

		/** Get all the appropriate datatypes. If the semantic element has a datatype,
		 * we will just allow those datatypes referenced by the semantic element's type.
		 * Otherwise we'll allow all types within the Message Group
		 * @return
		 */
		private Collection<MdmiDatatype> getAllDatatypes() {
			Collection<MdmiDatatype> dataTypes;

			DataRule dataRule = (DataRule)getUserObject();
			SemanticElement semanticElement = dataRule.getSemanticElement();
			if (semanticElement != null && semanticElement.getDatatype() != null) {
				// Get datatypes for this semantic element
				dataTypes = new TreeSet<MdmiDatatype>(MdmiDatatypeField.getDatatypeComparator());
				addAllDatatypes(dataTypes, semanticElement.getDatatype());
				
			} else {
				// Get all DataTypes from the message group
				dataTypes = MdmiDatatypeField.getAllDatatypes(getMessageGroup(), 
						MdmiDatatype.class);	// don't filter by type
			   	
			}
			return dataTypes;
		}
		
		/** Recursively add a datatype, and all of its fields (if complex) to the collection */
		private void addAllDatatypes(Collection<MdmiDatatype> dataTypes, MdmiDatatype datatype) {
			if (dataTypes.contains(datatype)) {
				// been there, done that
				return;
			}
			
			// add this type
			dataTypes.add(datatype);
			
			// search fields
			if (datatype instanceof DTComplex) {
				for (Field field : ((DTComplex)datatype).getFields()) {
					// search field's type
					addAllDatatypes(dataTypes, field.getDatatype());
				}
			}
		}

		@Override
		public String getDefaultRuleLanguage() {
			return getDefaultRulesExpressionLanguage();
		}

		@Override
		public String getRuleLanguage() {
			return getConstraintExpressionLanguage();
		}

		@Override
		protected List<ModelInfo> validateRuleField(RuleField ruleField) {
			List<ModelInfo> errors = new ArrayList<ModelInfo>();
			DataRule dataRule = (DataRule)getEditObject();
			SemanticElement semanticElement = dataRule.getSemanticElement();
			if (semanticElement != null) {
				errors = ruleField.validateConstraintRule(dataRule.getActualRuleExpressionLanguage(), semanticElement);
			}
			return errors;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			setModified(true);
		}
		
		private class DataTypeFieldInfo extends DataEntryFieldInfo {
			public DataTypeFieldInfo(String displayName, Method getMethod,
					Method setMethod, Class<?> returnType) {
				super(displayName, getMethod, setMethod, returnType);
			}

			@Override
			public Object getValueFromModel(Object model)
					throws IllegalArgumentException, IllegalAccessException,
					InvocationTargetException {

				// clone model value so if we modify the model, we don't change the value
				Object value = super.getValueFromModel(model);
				if (value instanceof Collection<?>) {
					ArrayList<Object> copy = new ArrayList<Object>();
					copy.addAll((Collection<?>)value);
					value = copy;
				}
				return value;
			}

			/** Override setValueInModel method because there is no corresponding
			 * "setDatatypes" method
			 */
			@Override
			public void setValueInModel(Object model, Object value)
					throws IllegalArgumentException, IllegalAccessException,
					InvocationTargetException {
				if (model instanceof DataRule && value instanceof Collection) {
					DataRule dataRule = (DataRule)model;
					dataRule.getDatatypes().clear();
					for (Object obj : (Collection<?>)value) {
						if (obj instanceof MdmiDatatype) {
							dataRule.addDatatype((MdmiDatatype)obj);
						}
					}
				} else {
					super.setValueInModel(model, value);
				}
			}
			
			
		}
		
	}
}
