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
package org.openhealthtools.mdht.mdmi.editor.map.help;

import java.awt.Frame;
import java.text.MessageFormat;

import javax.swing.Action;

import org.openhealthtools.mdht.mdmi.editor.common.actions.AbstractHelpAboutAction;
import org.openhealthtools.mdht.mdmi.editor.common.components.AbstractHelpAboutDialog;
import org.openhealthtools.mdht.mdmi.editor.map.Actions;
import org.openhealthtools.mdht.mdmi.editor.map.MapEditor;

public class HelpAboutAction extends AbstractHelpAboutAction {

	private static final long serialVersionUID = -1957877247380068222L;

	public HelpAboutAction() {
		super();

		// initialize menu to use product information in menu name ("Help About Configuration Editor"
		String menuName = MessageFormat.format(getLocalText(Actions.HELP_ABOUT_ACTION),
				MapEditor.getApplicationName());
		
		this.putValue(Action.NAME, menuName);
	}

	@Override
	public AbstractHelpAboutDialog getHelpAboutDialog(Frame parent) {
		return new HelpAboutDialog(parent);
	}

}
