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
package org.openhealthtools.mdht.mdmi.editor.map.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.console.LinkedObject;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MdmiExpression;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.Node;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.validate.IModelValidate;
import org.openhealthtools.mdht.mdmi.model.validate.ModelInfo;
import org.openhealthtools.mdht.mdmi.model.validate.ModelValidationResults;
import org.openhealthtools.mdht.mdmi.model.validate.ValidatorFactory;

/** A generic editor with simple widgets for a bunch of known data types */
public class GenericEditor extends AbstractComponentEditor {
	private static String s_modelPackage = "org.openhealthtools.mdht.mdmi.model";
	
	private MessageGroup  m_messageGroup;
	
	private Class<?> m_objectClass = null;
	private Object   m_object = null;
	
	private JPanel  m_mainPanel;
	private GridBagConstraints m_gbc;
	
	// Editor Fields
	private List<DataEntryFieldInfo> m_dataEntryFieldList = new ArrayList<DataEntryFieldInfo>();

	// Methods display - debugging
	private boolean m_debugging = false;
	private JTextArea m_methodsDisplayArea = new JTextArea(10, 20);
	private JScrollPane m_methodsScroller = new JScrollPane(m_methodsDisplayArea);
	private JCheckBox m_showMethodsCheckBox = new JCheckBox("Show Other Methods", false); 
	private ActionListener m_showHideMethodListener = null;

	public GenericEditor(MessageGroup group, Class<?> objectClass) {
		this(group, objectClass, false);	// debugging off
	}
	
	GenericEditor(MessageGroup group, Class<?> objectClass, boolean debugging) {
		setLayout(new BorderLayout());
		
		m_messageGroup = group;
		m_objectClass = objectClass;
		
		m_mainPanel = new JPanel(new GridBagLayout());
		add(new JScrollPane(m_mainPanel), BorderLayout.CENTER);
		
		m_gbc = new GridBagConstraints();
		m_gbc.gridx = 0;
		m_gbc.gridy = 0;
		m_gbc.insets = Standards.getInsets();
		
		m_debugging = debugging;
		layoutUI();
	}
	
	@Override
	public void addNotify() {
		super.addNotify();

		// set tool tips
		for (DataEntryFieldInfo fieldInfo : m_dataEntryFieldList) {
			// look for a tool tip
			String toolTip = getToolTipText(fieldInfo.getFieldName());
			if (toolTip != null) {
				fieldInfo.getEditComponent().getComponent().setToolTipText(toolTip);
			}
		}
		
		if (m_showHideMethodListener == null) {
			m_showHideMethodListener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					m_methodsScroller.setVisible(m_showMethodsCheckBox.isSelected());
					m_mainPanel.revalidate();
				}
			};
		}
		m_showMethodsCheckBox.addActionListener(m_showHideMethodListener);
	}

	@Override
	public void removeNotify() {

		// clear tool tips
		for (DataEntryFieldInfo fieldInfo : m_dataEntryFieldList) {
			fieldInfo.getEditComponent().getComponent().setToolTipText(null);
		}

		// Remove listeners
		m_showMethodsCheckBox.removeActionListener(m_showHideMethodListener);
		
		super.removeNotify();
	}

	protected void layoutUI() {
		List<Method[]> methodPairList = getMethodPairs(m_objectClass);
		
		createDataEntryFields(methodPairList);

		for (DataEntryFieldInfo fieldInfo : m_dataEntryFieldList) {
			String fieldName = fieldInfo.getFieldName();
//			// look for a tool tip
//			String toolTip = getToolTipText(fieldName);
//			if (toolTip != null) {
//				fieldInfo.getEditComponent().getComponent().setToolTipText(toolTip);
//			}
			// make fields read-only
			if (isReadOnlyFields(fieldName)) {
				fieldInfo.getEditComponent().setReadOnly();
			}

		}
		
		/////////////////////////////////////////////////////
		
		// Other Methods not covered in the above list - normally hidden
		if (m_debugging) {
			m_gbc.fill = GridBagConstraints.BOTH;
			m_gbc.weightx = 1.0;
			m_gbc.weighty = 1.0;
			m_gbc.gridwidth = 2;
			m_mainPanel.add(m_methodsScroller, m_gbc);

			m_methodsDisplayArea.setText( getMethodPairsString(methodPairList) );
			m_methodsScroller.setVisible(false);
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			buttonPanel.add(m_showMethodsCheckBox);
			add(buttonPanel, BorderLayout.SOUTH);
		}
	}

	/** create all fields for data entry
	 * @param methodPairList
	 */
	protected void createDataEntryFields(List<Method[]> methodPairList) {
		addStringFields(methodPairList);
		addBooleanFields(methodPairList);
		addEnumFields(methodPairList);
		addIntegerFields(methodPairList);
		addURIFields(methodPairList);
		addSemanticElementFields(methodPairList);
		addSyntaxNodeFields(methodPairList);
		addMdmiDatatypeFields(methodPairList);
		addBusinessElementReferenceFields(methodPairList);
		addMdmiExpressionFields(methodPairList);
	}

	/** Add all fields that have string values, using a JTextField.
	 * @param methodPairList
	 */
	protected void addStringFields(List<Method[]> methodPairList) {
		// String fields will have StringField components
		for (DataEntryFieldInfo fieldInfo : getFieldsForType(methodPairList, String.class)) {
			IEditorField editField = createEditorField(fieldInfo);
			if (editField != null) {
				double weightY = m_gbc.weighty;
				int fill = GridBagConstraints.HORIZONTAL;
				if (editField instanceof RuleField) {
					weightY = 5;	// give extra weight to Rules
					fill = GridBagConstraints.BOTH;
				}
				addLabeledField(ClassUtil.beautifyName(fieldInfo.getFieldName()),
						editField.getComponent(), weightY, fill);

				fieldInfo.setEditComponent(editField);
				addDataEntryFieldInfo(fieldInfo);
			}
		}
	}

	/** Add A DataEntryFieldInfo to the list of fields in the UI */
	protected void addDataEntryFieldInfo(DataEntryFieldInfo fieldInfo) {
		m_dataEntryFieldList.add(fieldInfo);
	}

	/** Add all fields that have boolean values, using a JRadioButton.
	 * @param methodPairList
	 */
	protected void addBooleanFields(List<Method[]> methodPairList) {
		// Boolean fields will have a check box
		for (DataEntryFieldInfo fieldInfo : getFieldsForType(methodPairList, boolean.class)) {
			IEditorField editField = createEditorField(fieldInfo);
			if (editField != null) {
				addLabeledField(null, editField.getComponent(), m_gbc.weighty, GridBagConstraints.NONE);

				fieldInfo.setEditComponent(editField);
				addDataEntryFieldInfo(fieldInfo);
			}
		}
	}
	/** Add all fields that have Enum values, using a JComboBox.
	 * @param methodPairList
	 */
	protected void addEnumFields(List<Method[]> methodPairList) {
		// Enum fields will have combo box
		for (DataEntryFieldInfo fieldInfo : getFieldsForType(methodPairList, Enum.class)) {
			IEditorField editField = createEditorField(fieldInfo);
			if (editField != null) {
				addLabeledField(ClassUtil.beautifyName(fieldInfo.getFieldName()),
						editField.getComponent(), m_gbc.weighty, GridBagConstraints.NONE);

				fieldInfo.setEditComponent(editField);
				addDataEntryFieldInfo(fieldInfo);
			}
		}
	}

	/** Add all fields that have integer values, using a filtered JTextField.
	 * @param methodPairList
	 */
	protected void addIntegerFields(List<Method[]> methodPairList) {
		// Integer fields will have JTextField components with a filter
		for (DataEntryFieldInfo fieldInfo : getFieldsForType(methodPairList, int.class)) {
			IEditorField editField = createEditorField(fieldInfo);
			if (editField != null) {
				addLabeledField(ClassUtil.beautifyName(fieldInfo.getFieldName()),
						editField.getComponent(), m_gbc.weighty, GridBagConstraints.NONE);

				fieldInfo.setEditComponent(editField);
				addDataEntryFieldInfo(fieldInfo);
			}
		}
	}
	
	/** Add all fields that have URI values, using a JTextField.
	 * @param methodPairList
	 */
	protected void addURIFields(List<Method[]> methodPairList) {
		// URI fields will have JTextField components
		for (DataEntryFieldInfo fieldInfo : getFieldsForType(methodPairList, URI.class)) {
			IEditorField editField = createEditorField(fieldInfo);
			if (editField != null) {
				addLabeledField(ClassUtil.beautifyName(fieldInfo.getFieldName()),
						editField.getComponent(), m_gbc.weighty, GridBagConstraints.HORIZONTAL);

				fieldInfo.setEditComponent(editField);
				addDataEntryFieldInfo(fieldInfo);
			}
		}
	}

	/** Add all fields that have SemanticElement values, using a JComboBox.
	 * @param methodPairList
	 */
	protected void addSemanticElementFields(List<Method[]> methodPairList) {
		// SemanticElement fields will have a JComboBox component
		for (DataEntryFieldInfo fieldInfo : getFieldsForType(methodPairList, SemanticElement.class)) {
			IEditorField editField = createEditorField(fieldInfo);
			if (editField != null) {
				addLabeledField(ClassUtil.beautifyName(fieldInfo.getFieldName()),
						editField.getComponent(), m_gbc.weighty, GridBagConstraints.NONE);

				fieldInfo.setEditComponent(editField);
				addDataEntryFieldInfo(fieldInfo);
			}
		}
	}

	/** Add all fields that have SyntaxNode values, using a JComboBox.
	 * @param methodPairList
	 */
	protected void addSyntaxNodeFields(List<Method[]> methodPairList) {
		// SyntaxNode fields will have a JComboBox component
		for (DataEntryFieldInfo fieldInfo : getFieldsForType(methodPairList, Node.class)) {
			IEditorField editField = createEditorField(fieldInfo);
			if (editField != null) {
				addLabeledField(ClassUtil.beautifyName(fieldInfo.getFieldName()),
						editField.getComponent(), m_gbc.weighty, GridBagConstraints.NONE);

				fieldInfo.setEditComponent(editField);
				addDataEntryFieldInfo(fieldInfo);
			}
		}
	}

	/** Add all fields that have MdmiBusinessElementReference values, using a JComboBox.
	 * @param methodPairList
	 */
	protected void addBusinessElementReferenceFields(List<Method[]> methodPairList) {
		// MdmiBusinessElementReference fields will have a JComboBox component
		for (DataEntryFieldInfo fieldInfo : getFieldsForType(methodPairList, MdmiBusinessElementReference.class)) {
			IEditorField editField = createEditorField(fieldInfo);
			if (editField != null) {
				addLabeledField(ClassUtil.beautifyName(fieldInfo.getFieldName()),
						editField.getComponent(), m_gbc.weighty, GridBagConstraints.NONE);

				fieldInfo.setEditComponent(editField);
				addDataEntryFieldInfo(fieldInfo);
			}
		}
	}
	
	/** Add all fields that have MdmiDatatype values, using a JComboBox.
	 * @param methodPairList
	 */
	protected void addMdmiDatatypeFields(List<Method[]> methodPairList) {
		// MdmiData fields will have a JComboBox component
		for (DataEntryFieldInfo fieldInfo : getFieldsForType(methodPairList, MdmiDatatype.class)) {
			IEditorField editField = createEditorField(fieldInfo);
			if (editField != null) {
				addLabeledField(ClassUtil.beautifyName(fieldInfo.getFieldName()),
						editField.getComponent(), m_gbc.weighty, GridBagConstraints.NONE);

				fieldInfo.setEditComponent(editField);
				addDataEntryFieldInfo(fieldInfo);
			}
		}
	}

	/** Add all fields that have MdmiExpression values, using an MdmiExpressionEditor
	 * @param methodPairList
	 */
	protected void addMdmiExpressionFields(List<Method[]> methodPairList) {
		for (DataEntryFieldInfo fieldInfo : getFieldsForType(methodPairList, MdmiExpression.class)) {
			IEditorField editField = createEditorField(fieldInfo);
			if (editField != null) {
				addFieldFullWidth(editField.getComponent(), GridBagConstraints.HORIZONTAL);

				fieldInfo.setEditComponent(editField);
				addDataEntryFieldInfo(fieldInfo);
			}
		}
	}
	
	protected void addListOfObjects(List<Method[]> methodPairList) {
		List<DataEntryFieldInfo> fields = getFieldsForType(methodPairList, ArrayList.class);
		for (DataEntryFieldInfo fieldInfo : fields) {
			IEditorField editField = createEditorField(fieldInfo);
			if (editField != null) {
				addFieldFullWidth(editField.getComponent(), GridBagConstraints.BOTH);

				fieldInfo.setEditComponent(editField);
				addDataEntryFieldInfo(fieldInfo);
			}
		}
			
	}

	/** Create the editor component for this field */
	@SuppressWarnings("unchecked")
	protected IEditorField createEditorField(DataEntryFieldInfo fieldInfo) {
		IEditorField editorField = null;
		Class<?> returnType = fieldInfo.getReturnType();
		
		if (String.class.isAssignableFrom(returnType)) {
			// use RuleField or StringField
			if ("Rule".equalsIgnoreCase(fieldInfo.getFieldName())) {
				editorField = new RuleField(this);
			} else {
				editorField = new StringField(this, 1, 10);
			}
		
		} else if (boolean.class.isAssignableFrom(returnType)) {
			// Boolean
			editorField = new BooleanField(this,
					ClassUtil.beautifyName(fieldInfo.getFieldName()));

		} else if (Enum.class.isAssignableFrom(returnType)) {
			// Enum
			editorField = new EnumField(this, returnType);

		} else if (int.class.isAssignableFrom(returnType)) {
			// Integer
			editorField = new IntegerField(this, 7);

		} else if (URI.class.isAssignableFrom(returnType)) {
			// URI
			editorField = new URIField(this);

		} else if (SemanticElement.class.isAssignableFrom(returnType)) {
			// SemanticElement
			editorField = new SemanticElementField(this);

		} else if (Node.class.isAssignableFrom(returnType)) {
			// Node
			editorField = new SyntaxNodeField(this);

		} else if (MdmiBusinessElementReference.class.isAssignableFrom(returnType)) {
			// MdmiBusinessElementReference
			editorField = new BusinessElementReferenceField(this);

		} else if (MdmiDatatype.class.isAssignableFrom(returnType)) {
			// MdmiDatatype
			editorField = new MdmiDatatypeField(this, (Class<? extends MdmiDatatype>) returnType);

		} else if (MdmiExpression.class.isAssignableFrom(returnType)) {
			// MdmiExpression
			editorField = new NestedEditor(this, MdmiExpression.class, 
					ClassUtil.beautifyName(fieldInfo.getFieldName()));
			
		} else if (ArrayList.class.isAssignableFrom(returnType)) {
			// List of Thingies - assume that the name (getNames()) is the plural of
			// the type of thingies
			String fieldName = fieldInfo.getFieldName();
			if (fieldName.endsWith("s")) {
				String className = fieldName.substring(0, fieldName.length()-1);
				try {
					// HACK - assume package is "org.openhealthtools.mdht.mdmi.model"
					className = s_modelPackage + "." + className;
					Class<?> fieldClass = Class.forName(className);
					
					editorField = new ArrayListEditor(this, fieldClass, 
							ClassUtil.beautifyName(fieldName));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}

		}
		
		return editorField;		
	}

	/** Add a component with an optional label to the layout
	 * @param stringField
	 * @return
	 */
	protected void addLabeledField(String display, JComponent field, double weightY, int fieldFill) {
		double wY = m_gbc.weighty;
		m_gbc.weighty = weightY;
		
		if (display != null) {
			JLabel title = new JLabel(display + ":");
			m_gbc.weightx = 0;
			m_gbc.anchor = GridBagConstraints.EAST;
			m_gbc.fill = GridBagConstraints.NONE;
			m_gbc.insets.right = 0;
			m_mainPanel.add(title, m_gbc);
			m_gbc.insets.right = Standards.RIGHT_INSET;
		}
		
		m_gbc.gridx++;

		m_gbc.weightx = 1;
		m_gbc.anchor = GridBagConstraints.WEST;
		m_gbc.fill = fieldFill;
		m_mainPanel.add(field, m_gbc);
		
		m_gbc.gridx = 0;
		m_gbc.gridy++;
		
		// restore
		m_gbc.weighty = wY;
	}
	
	/** Add this field to the container so that it occupies the full width */
	protected void addFieldFullWidth(JComponent field, int fill) {

		m_gbc.weightx = 1;
		m_gbc.gridwidth = 2;
		m_gbc.anchor = GridBagConstraints.WEST;
		m_gbc.fill = fill;
		m_gbc.weighty = 1;
		m_mainPanel.add(field, m_gbc);

		m_gbc.gridwidth = 1;
		m_gbc.gridy++;
	}

	/** Top of the tree */
	public MessageGroup getMessageGroup() {
		return m_messageGroup;
	}
	
	/** get the object being edited */
	public Object getEditObject() {
		return m_object;
	}
	

	/** Get the class of the type of model that this editor supports */
	public Class<?> getObjectClass() {
		return m_objectClass;
	}
	
	/** populate model with UI data */
	@Override
	public Object getUpdatedModel() {
		
		// clear displayed errors
		for (DataEntryFieldInfo fieldInfo : m_dataEntryFieldList) {
			fieldInfo.clearDataInputError();
		}
		
		// Update Model from UI
		for (DataEntryFieldInfo fieldInfo : m_dataEntryFieldList) {
			try {
				// get value from UI component
				Object value = fieldInfo.getEditComponent().getValue();

				// update model
				fieldInfo.setValueInModel(m_object, value);
				
			} catch (DataFormatException e) {
				// User Input Error - Show an error message and highlight text
				fieldInfo.showDataInputError();
				
				JOptionPane.showMessageDialog(this, e.getLocalizedMessage(), 
						s_res.getString("GenericEditor.inputErrorTitle"),
						JOptionPane.ERROR_MESSAGE);
				return null;

				
			} catch (Exception e) {
				// Unable to invoke '{0}' on {1}
				String msg = MessageFormat.format(s_res.getString("GenericEditor.invocationErrorFormat"),
						fieldInfo.getSetMethod().getName(), m_objectClass.getName());
				SelectionManager.getInstance().getStatusPanel().writeException(msg, e);
			}
		}

		
		// Validate
		List<ModelInfo> errors = validateModel();
		// highlight fields that contain errors
		highlightErrors(errors);


		// Show errors on console
		displayErrors(errors);


		boolean acceptChanges = true;
		// If there are errors, ask the user if they want to accept anyway
		if (errors.size() > 0) {
			acceptChanges = false;
			SelectionManager.getInstance().getEntityEditor().showEditPanel(m_object);
			String objectName = getModelName(m_object);
			// ask user if they want to accept anyway
			//  There are validation errors in <name>. Do you want to accept these changes anyway?
			int opt = JOptionPane.showConfirmDialog(this, 
					MessageFormat.format(s_res.getString("GenericEditor.validationErrorMessage"),
							objectName),
					s_res.getString("GenericEditor.validationErrorTitle"),
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (opt == JOptionPane.YES_OPTION) {
				acceptChanges = true;
			}
		}
		
		//  accept updated model values
		if (acceptChanges) {
			for (DataEntryFieldInfo fieldInfo : m_dataEntryFieldList) {
				try {
					Object value = fieldInfo.saveModelValue(m_object);
					
					// re-set the value in the UI
					fieldInfo.getEditComponent().setDisplayValue(value);
					
				} catch (Exception e) {
					// Unable to extract data for '{0}' from a {1} object.
					String msg = MessageFormat.format(s_res.getString("GenericEditor.dataExtractionErrorFormat"),
							ClassUtil.beautifyName(fieldInfo.getFieldName()), m_objectClass.getName());
					SelectionManager.getInstance().getStatusPanel().writeException(msg, e);
				}
			}

		} else {
			return null;
		}
		
		setModified(false);
		return m_object;
	}

	/** Validate the data in the model.
	 *
	 * @return a list of errors
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<ModelInfo> validateModel() {
		ModelValidationResults results = new ModelValidationResults();
		@SuppressWarnings("rawtypes")
		IModelValidate validator = ValidatorFactory.getInstance().getValidator(m_object);
		if (validator != null) {
			validator.validate(m_object, results);
		}
		List<ModelInfo> errors = results.getErrors();
		return errors;
	}

	/** Highlight the fields with errors
	 * @param errors
	 */
	@Override
	public void highlightErrors(List<ModelInfo> errors) {
		for (ModelInfo errorMsg : errors) {
			highlightFieldWithError(errorMsg.getField());
		}
	}

	/** Display validations errors on the error screen
	 * @param errors
	 */
	public void displayErrors(List<ModelInfo> errors) {
		if (errors.size() > 0) {
			String objectName = getModelName(m_object);
			// Error Found In {0}
			String msg = MessageFormat.format(s_res.getString("GenericEditor.errorsFoundFormat"),
					errors.size(), m_objectClass.getSimpleName());
			SelectionManager.getInstance().getStatusPanel().writeErrorLink(msg, 
					new LinkedObject(m_object,objectName), ":");
			
			// show individual errors
			for (ModelInfo errorMsg : errors) {		
				SelectionManager.getInstance().getStatusPanel().writeValidationErrorMsg("   - ",
						errorMsg);
			}
		}
	}

	/** Highlight the text in the provided field.
	 * 
	 * @param fieldName	The name of the field to highlight
	 * @param textToHighlight	The text that gets highlighted
	 * @param highlightColor	The color. If null, restore all text to default color
	 */
	@Override
	public void highlightField(String fieldName, String textToHighlight, Color highlightColor) {
		// find this field and highlight it
		IEditorField field = getEditorField(fieldName);
		if (field != null) {
			field.highlightText(textToHighlight, highlightColor);
		}
	}

	/** Highlight the field that has the provided value
	 * 
	 * @param value	The model value to highlight
	 * @param highlightColor	The color. If null, restore all text to default color
	 */
	@Override
	public void highlightField(Object value, Color highlightColor) {
		// find this field and highlight it
		for (DataEntryFieldInfo fieldInfo : m_dataEntryFieldList) {
			try {
				Object modelValue = fieldInfo.getValueFromModel(m_object);
				if (value.equals(modelValue) || 
						(modelValue instanceof Collection && ((Collection<?>)modelValue).contains(value))) {
					fieldInfo.getEditComponent().highlightText(value.toString(), highlightColor);
					break;
				}
			} catch (Exception e) {
				// ignore
			}
		}
	}

	/** Find the field that has an error, and highlight it with a red line
	 * @param errorMsg
	 */
	public void highlightFieldWithError(String fieldName) {
		// find this field and highlight it
		for (DataEntryFieldInfo fieldInfo : m_dataEntryFieldList) {
			if (fieldInfo.getFieldName().equalsIgnoreCase(fieldName)) {
				fieldInfo.showDataInputError();
				break;
			}
		}
	}

	/** 
	 * Revert the model to the last valid state. If no changes have been applied, this
	 * will be the inital state.
	 */
	@Override
	public void revertModel() {
		for (DataEntryFieldInfo fieldInfo : m_dataEntryFieldList) {
			try {
				fieldInfo.restoreModelValue(m_object);
			} catch (Exception e) {

				// Unable to extract data for '{0}' from a {1} object.
				String msg = MessageFormat.format(s_res.getString("GenericEditor.dataExtractionErrorFormat"),
						ClassUtil.beautifyName(fieldInfo.getFieldName()), m_objectClass.getName());
				SelectionManager.getInstance().getStatusPanel().writeException(msg, e);
			}
		}
	}

	/** Make all sub-components read-only */
	public void setReadOnly() {
		for (DataEntryFieldInfo fieldInfo : m_dataEntryFieldList) {
			fieldInfo.getEditComponent().setReadOnly();
		}
	}

	/** populate the UI from the model */
	@Override
	public void populateUI(Object entity) {
		m_object = entity;
		
		// accept the original value from the model, and use it to populate the UI
		for (DataEntryFieldInfo fieldInfo : m_dataEntryFieldList) {
			try {
				// accept the original value from the model in case we need to revert
				Object value = fieldInfo.saveModelValue(entity);
				
				// set the value in the UI
				fieldInfo.getEditComponent().setDisplayValue(value);
				
			} catch (DataFormatException e) {
				// error updating UI - shouldn't happen
				String msg = e.getLocalizedMessage();
				SelectionManager.getInstance().getStatusPanel().writeException(msg, e);

				
			} catch (Exception e) {
				// error reading from model
				// Unable to invoke '{0}' on {1}
				String msg = MessageFormat.format(s_res.getString("GenericEditor.invocationErrorFormat"),
						fieldInfo.getGetMethod().getName(), m_objectClass.getName());
				SelectionManager.getInstance().getStatusPanel().writeException(msg, e);
			}
		}
		
	}

	/** Get fields of a particular type by checking for getThing()/setThing(class) pairs */
	protected List<DataEntryFieldInfo> getFieldsForType(List<Method[]> methodPairList,
			Class<?> getMethodReturnType) {
		
		List<DataEntryFieldInfo> fieldInfoList = new ArrayList<DataEntryFieldInfo>();
		
		// look for get/set pairs with String arguments
		for (int i=methodPairList.size()-1; i>=0; i--) {
			Method[] pair = methodPairList.get(i);
			
			Method getMethod = pair[0];
			Method setMethod = pair[1];
			
			Class<?> returnType = getMethod.getReturnType();
			
			if (getMethodReturnType.isAssignableFrom(returnType)) {
				String fieldName = setMethod.getName().replaceFirst("set", "");
				
				DataEntryFieldInfo fieldInfo = new DataEntryFieldInfo(fieldName, 
						getMethod, setMethod, returnType);
				fieldInfoList.add(fieldInfo);
				
				// remove method pair since we've already checked it
				methodPairList.remove(i);
			}
		}

		
		// Sort by field name
		Collections.sort(fieldInfoList, getDataEntryFieldComparator());
		
		return fieldInfoList;
	}
	
	protected DataEntryFieldComparator getDataEntryFieldComparator() {
		return new DataEntryFieldComparator();
	}
	

	/** Determine the tool tip to show for this field. The default is none */
	public String getToolTipText(String fieldName) {
		return null;
	}

	/** Determine if this field should be shown read-only */
	public boolean isReadOnlyFields(String fieldName) {
		return false;
	}
	
//	/** Get the default value for this field */
//	public Object getDefaultValue(String fieldName) {
//		return null;
//	}
	

	/** Get the Field Name of this component */
	public String getFieldNameFor(IEditorField editor) {
		for (DataEntryFieldInfo fieldInfo : m_dataEntryFieldList) {
			if (fieldInfo.getEditComponent() == editor) {
				return fieldInfo.getFieldName();
			}
		}
		return null;
	}

	/** Get the IEditorField for a given field name */
	public IEditorField getEditorField(String fieldName) {
		for (DataEntryFieldInfo fieldInfo : m_dataEntryFieldList) {
			if (fieldInfo.getFieldName().equalsIgnoreCase(fieldName)) {
				return fieldInfo.getEditComponent();
			}
		}
		
		return null;
	}
	
	/** Get the component for a given field name */
	public JComponent getComponent(String fieldName) {
		IEditorField field = getEditorField(fieldName);
		return (field == null) ? null : field.getComponent();
	}
	
	/** Get all fields */
	protected List<DataEntryFieldInfo> getDataEntryFieldList() {
		return m_dataEntryFieldList;
	}
	
//	/** Convenient structure for fieldName/value pair */
//	public static class DefaultValue {
//		public String fieldName;
//		public Object value;
//		
//		public DefaultValue(String fieldName, Object value) {
//			this.fieldName = fieldName;
//			this.value = value;
//		}
//	}
	
	/** comparator for DataEntryFieldInfo - "Name" comes first, followed by any
	 * field that ends with "Name", followed by "Description"
	 */
	public static class DataEntryFieldComparator implements Comparator<DataEntryFieldInfo> {
		@Override
		public int compare(DataEntryFieldInfo o1, DataEntryFieldInfo o2) {
			// If field ends with "Name", we want it first,
			// followed by "Description"
			String s1 = o1.getFieldName();
			String s2 = o2.getFieldName();
			
			// we need "max" to come after "min"
			if (s1.startsWith("Max")) {
				s1 = s1.replace("Max", "Mio");
			}
			if (s2.startsWith("Max")) {
				s2 = s2.replace("Max", "Mio");
			}
			
			// name is always first
			if ("Name".equalsIgnoreCase(s1)) {
				return -1;
			} else if ("Name".equalsIgnoreCase(s2)) {
				return 1;
			}
			if (s1.endsWith("Name")) {
				if (s2.endsWith("Name")) {
					return s1.compareTo(s2);
				}
				return -1;
			} else if (s2.endsWith("Name")) {
				return 1;
			}
			if ("Description".equalsIgnoreCase(s1)) {
				return -1;
			} else if ("Description".equalsIgnoreCase(s2)) {
				return 1;
			}
			// sort by length first
			int c = s1.length() - s2.length();
			if (c == 0) {
				c = s1.compareTo(s2);
			}
			return c;
		}
	}
}
