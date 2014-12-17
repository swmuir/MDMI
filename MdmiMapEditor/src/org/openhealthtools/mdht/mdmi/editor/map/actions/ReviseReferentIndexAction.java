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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.actions.AbstractMenuAction;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.common.components.WrappingDisplayText;
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
		ReferentIndexToCSV.TokenSelector sel = new TokenSelector(frame);
		int rc = sel.display(frame);
		if (rc == BaseDialog.OK_BUTTON_OPTION) {
			ReferentIndexToCSV importer = new ReferentIndexToCSV();
			importer.reviseReferentIndex(sel.getToken());
		}
	}

	private static class TokenSelector extends ReferentIndexToCSV.TokenSelector {

		public TokenSelector(Frame owner) {
			super(owner);
			
			// add text to the top
			String text = "Revise the Referent Index using a file containing the Business Elements that need to be changed.";
			text += "\n\nThe first column must be the UID or existing business element name.";
			text += "\n\nThe additional columns must be labeled with the fields to be replaced (e.g. \"Name\", \"Description\")";
			WrappingDisplayText description = new WrappingDisplayText(text);
			Font font = description.getFont();
			font = font.deriveFont((float)(font.getSize()-1));	// make smaller
			description.setFont(font);
			
			add(description, BorderLayout.NORTH);
			
			pack();
		}
		
	}
}