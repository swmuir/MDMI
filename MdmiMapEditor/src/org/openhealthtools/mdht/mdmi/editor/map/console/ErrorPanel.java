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
package org.openhealthtools.mdht.mdmi.editor.map.console;

import java.awt.Color;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.openhealthtools.mdht.mdmi.MdmiException;

/**
 * A console to display error text. 
 */
public class ErrorPanel extends ConsolePanel {
	private static final long serialVersionUID = -1;

	SimpleAttributeSet m_errorSet;

	@Override
	public SimpleAttributeSet getAttributeSet() {
		if (m_errorSet == null) {
			m_errorSet = new SimpleAttributeSet();
			m_errorSet.addAttribute(StyleConstants.Foreground, Color.RED);
		}
		return m_errorSet;
	}


	public void writeException( String msg, Exception ex) {
		if (msg != null) writeln(msg);
		writeln(MdmiException.getFullDescription(ex));
	}
	
	public void writeException(Exception ex) {
		writeln(MdmiException.getFullDescription(ex));
	}

}
