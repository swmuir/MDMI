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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.UIManager;

import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;

/** An IEdtorField that shows enumeration values as a ComboBox */
public class EnumField extends JComboBox implements IEditorField, ActionListener {
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.editor.Local");

	private GenericEditor m_parentEditor;

	public EnumField(GenericEditor parentEditor, Class<?> enumType) {
		m_parentEditor = parentEditor;
		
		fillValuesForEnum(enumType);
	}

	/** Fill in all values based on the enum type
	 * @param enumType
	 */
	private void fillValuesForEnum(Class<?> enumType) {
		// use enumeration values() to fill combo box
		try {
			Method valuesMethod = enumType.getMethod("values");
			Object[] values = (Object[])valuesMethod.invoke(null);
			for (Object v: values) {
				addItem(v);
			}
			
		} catch (Exception e) {
			// Unable to identifiy valid values for {0}
			String msg = MessageFormat.format(s_res.getString("EnumField.valuesMsgFormat"),
					enumType);
			SelectionManager.getInstance().getStatusPanel().writeException(msg, e);
		}
	}
	
	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public Object getValue() throws DataFormatException {
		return getSelectedItem();
	}

	@Override
	public void setDisplayValue(Object value) throws DataFormatException {
		setSelectedItem(value);	
	}
	
	@Override
	public void setReadOnly() {
		setEnabled(false);
	}
	
	
	@Override
	public void highlightText(String text, Color highlightColor) {
		if (highlightColor == null) {
			highlightColor = UIManager.getColor("ComboBox.background");	// restore
		}
		setBackground(highlightColor);
	}

	@Override
	public void addNotify() {
		super.addNotify();
		addActionListener(this);
	}

	@Override
	public void removeNotify() {
		removeActionListener(this);
		super.removeNotify();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// update parent
		m_parentEditor.setModified(true);
	}
}
