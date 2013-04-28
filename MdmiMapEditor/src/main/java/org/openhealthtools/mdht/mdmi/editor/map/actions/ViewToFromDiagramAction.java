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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.Collection;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.actions.AbstractMenuAction;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.map.tools.SemanticElementSelectionDialog;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ViewSemanticElementFromTo;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;

public class ViewToFromDiagramAction extends AbstractMenuAction {
	private static final long serialVersionUID = -1;

	@Override
	public void execute(ActionEvent actionEvent) {
		viewToFromDiagram();
	}

	public static void viewToFromDiagram() {
		Frame frame = SystemContext.getApplicationFrame();
		SemanticElementSelectionDialog dlg = new SemanticElementSelectionDialog(frame);
		if (dlg.display(frame) == BaseDialog.OK_BUTTON_OPTION) {
			Collection<SemanticElement> types = dlg.getSemanticElements();
			ViewSemanticElementFromTo view = new ViewSemanticElementFromTo(types);
			view.setVisible(true);
		}
	}
}
