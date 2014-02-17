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

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.actions.AbstractMenuAction;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ReferentIndexToCSV;

public class ReviseReferentIndexAction extends AbstractMenuAction implements Runnable {
	private static final long serialVersionUID = -1;

	@Override
	public void execute(ActionEvent actionEvent) {
		// run in a different thread (the cursor manager will prevent any user input)
		new Thread(this).start();
	}

	@Override
	public void run() {
		Frame frame = SystemContext.getApplicationFrame();
		ReferentIndexToCSV.TokenSelector sel = new ReferentIndexToCSV.TokenSelector(frame);
		int rc = sel.display(frame);
		if (rc == BaseDialog.OK_BUTTON_OPTION) {
			ReferentIndexToCSV importer = new ReferentIndexToCSV();
			importer.reviseReferentIndex(sel.getToken());
		}
	}

	
}