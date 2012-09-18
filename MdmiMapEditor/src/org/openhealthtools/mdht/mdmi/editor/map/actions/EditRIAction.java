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
package org.openhealthtools.mdht.mdmi.editor.map.actions;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBoxMenuItem;

import org.openhealthtools.mdht.mdmi.editor.common.actions.AbstractMenuAction;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;

/** Allow Editing Referent Index */
public class EditRIAction extends AbstractMenuAction {
	private static final long serialVersionUID = -1;

	@Override
	public void execute(ActionEvent actionEvent) {
		boolean allow = false;
		if (actionEvent.getSource() instanceof JCheckBoxMenuItem) {
			allow = ((JCheckBoxMenuItem)actionEvent.getSource()).isSelected();
		}
		SelectionManager.getInstance().allowReferentIndexEditing(allow);
	}

}
