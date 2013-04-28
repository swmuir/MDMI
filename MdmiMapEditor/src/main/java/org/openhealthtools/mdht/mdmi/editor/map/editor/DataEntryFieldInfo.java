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

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

/** Information associated with reading, displaying, editing and saving a single
 * parameter within a data model object.
 * @author Conway
 *
 */
public class DataEntryFieldInfo {
	private String   m_fieldName;
	private Method   m_getMethod;
	private Method   m_setMethod;
	private Class<?> m_returnType;
	
	private IEditorField  m_editComponent;
	private Object	 m_savedValue;
	private boolean m_dataInputError;
	
	private static String s_iconPath = "/org/openhealthtools/mdht/mdmi/editor/map/editor/images/errorLine.gif";
	
	public DataEntryFieldInfo(String displayName, Method getMethod, 
			Method setMethod, Class<?> returnType) {
		this.m_fieldName = displayName;
		this.m_getMethod = getMethod;
		this.m_setMethod = setMethod;
		this.m_returnType = returnType;
	}
	
	////////////////////////////////////////////////
	//  Get and Set Methods                       //
	////////////////////////////////////////////////
	public String getFieldName() {
		return m_fieldName;
	}
	public void setFieldName(String fieldName) {
		this.m_fieldName = fieldName;
	}
	
	public Method getSetMethod() {
		return m_setMethod;
	}
	public void setSetMethod(Method setMethod) {
		this.m_setMethod = setMethod;
	}
	
	public Method getGetMethod() {
		return m_getMethod;
	}
	public void setGetMethod(Method getMethod) {
		this.m_getMethod = getMethod;
	}

	public void setReturnType(Class<?> returnType) {
		this.m_returnType = returnType;
	}
	public Class<?> getReturnType() {
		return m_returnType;
	}
	
	public IEditorField getEditComponent() {
		return m_editComponent;
	}
	public void setEditComponent(IEditorField editComponent) {
		this.m_editComponent = editComponent;
	}
	
	/** Indicate that this field has an error */
	public void showDataInputError() {
		if (!m_dataInputError) {
			Border errorBorder = createErrorBorder();
			
			// add the error border outside the existing border
			Border border = m_editComponent.getComponent().getBorder();

			border = BorderFactory.createCompoundBorder(errorBorder, border);
			m_editComponent.getComponent().setBorder(border);
		}
		
		m_dataInputError = true;
	}

	/** Create a border that has a red squiggly line on the bottom, but no
	 * top or side impact
	 * @return
	 */
	private static Border createErrorBorder() {
		// create a border with a red line on the bottom
		//         ________________________________
		//        |                                |
		//        |                                |
		//        |XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX|
		//
		URL url = DataEntryFieldInfo.class.getResource(s_iconPath);
		Border outerPattern; 
		if (url != null) {
			ImageIcon icon = new ImageIcon(url);
			outerPattern = BorderFactory.createMatteBorder(0, 0, icon.getIconHeight(), 0, icon);
		} else {
			outerPattern = BorderFactory.createMatteBorder(0, 0, 2, 0, Color.red);
		}
		
		// add a white line on the inside
		//         ________________________________
		//        |                                |
		//        |                                |
		//        |--------------------------------|
		//        |XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX|
		//
		Border errorBorder = BorderFactory.createCompoundBorder(outerPattern,
				BorderFactory.createMatteBorder(0, 0, 1, 0, Color.white) );
		return errorBorder;
	}
	
	/** Clear indication of an error */
	public void clearDataInputError() {
		if (m_dataInputError) {
			// clear the border
			Border border = m_editComponent.getComponent().getBorder();
			if (border instanceof CompoundBorder) {
				border = ((CompoundBorder)border).getInsideBorder();
			}
			m_editComponent.getComponent().setBorder(border);
		}
		
		m_dataInputError = false;
	}
	

	
	////////////////////////////////////////////////
	//        Other Methods                       //
	////////////////////////////////////////////////
	
	/** Call the model's setXXX() method using the supplied value
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException */
	public void setValueInModel(Object model, Object value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Method setMethod = m_setMethod;
		setMethod.invoke(model, value);
	}

	/** Call the model's getXXX() method to return a value 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException */
	public Object getValueFromModel(Object model) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Method getMethod = m_getMethod;
		Object value = getMethod.invoke(model);
		return value;
	}

	/** Save the value that's currently in the model in case we need to revert.
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException */
	public Object saveModelValue(Object model) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		m_savedValue = getValueFromModel(model);
		return m_savedValue;
	}

	/** Revert the model to the saved value.
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException */
	public void restoreModelValue(Object model) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		setValueInModel(model, m_savedValue);
	}
	
}
