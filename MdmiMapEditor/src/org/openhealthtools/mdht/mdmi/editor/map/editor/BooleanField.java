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
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

/** An IEdtorField that uses a CheckBox to display a boolean value */
public class BooleanField extends JCheckBox implements IEditorField, ActionListener {	
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.editor.Local");

	private GenericEditor m_parentEditor;

	public BooleanField(GenericEditor parentEditor, String displayName) {
		super(displayName);
		
		m_parentEditor = parentEditor;
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public Object getValue() throws DataFormatException {
		return isSelected();
	}
	
	@Override
	public void setReadOnly() {
		setEnabled(false);
	}


	@Override
	public void setDisplayValue(Object value) throws DataFormatException {
		if (value == null) {
		} else if (value instanceof Boolean) {
			setSelected(((Boolean)value).booleanValue());
		} else {
			// '{0}' is not a {1}.
			throw new DataFormatException(MessageFormat.format(s_res.getString("GenericEditor.dataFormatExceptionFormat"),
					value, "boolean"));
		}
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
	public void highlightText(String text, Color highlightColor) {
		// not applicable
	}



	@Override
	public void actionPerformed(ActionEvent e) {
		m_parentEditor.setModified(true);
	}
}
