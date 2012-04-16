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

import javax.swing.JComponent;

/** The UI component for entering data for one field of a class */
public interface IEditorField {
	/** Get the user-entered data in the correct format for insertion into the data model */
	public Object getValue() throws DataFormatException;
	
	/** Set the data to be displayed. */
	public void setDisplayValue(Object value) throws DataFormatException;
	
	/** Make read-only */
	public void setReadOnly();

	/** Get the UI widget that implements this interface */
	public JComponent getComponent();
	
	/**
	 * Highlight the specified text (or the entire field if not a text field(
	 */
	public abstract void highlightText(String text, Color highlightColor);
}
