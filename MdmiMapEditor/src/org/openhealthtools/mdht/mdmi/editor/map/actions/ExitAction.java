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
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.actions.AbstractMenuAction;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ModelIOUtilities;

public class ExitAction extends AbstractMenuAction {
	private static final long serialVersionUID = -1;
	
	/** Resource for localization */
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.actions.Local");


	@Override
	public void execute(ActionEvent actionEvent) {
		if (checkForChanges()) {
			System.exit(0);
		}
	}
	
	/** Check for unaccepted or pending changes.
	 * @return	true is returned if there are no changes, or all changes have been saved. */
	public static boolean checkForChanges() {
		boolean okToExit = false;
		
		SelectionManager selectionMgr = SelectionManager.getInstance();
		
		boolean unacceptedChanges = selectionMgr.getEntityEditor().isAnyEntityModified();

		String title = s_res.getString("ExitAction.unacceptedChangesTitle");
		String message;
		if (unacceptedChanges) {
			message = s_res.getString("ExitAction.unacceptedChangesMsg");
			
		} else if (selectionMgr.hasPendingUpdates()) {
			title = s_res.getString("ExitAction.unsavedChangesTitle");
			message = s_res.getString("ExitAction.pendingChangesMsg");
			
		} else {
			// nothing to worry about
			// persist any local files
			ModelIOUtilities.persistDirectoryData();
			return true;
		}

		Frame frame = SystemContext.getApplicationFrame();
		int opt = JOptionPane.showConfirmDialog(frame, message,
				title, JOptionPane.YES_NO_CANCEL_OPTION);
		
		if (opt == JOptionPane.NO_OPTION) {
			// exit without making changes
			okToExit = true;
			
		} else if (opt == JOptionPane.YES_OPTION) {
			// make changes
			if (unacceptedChanges) {
				if (!selectionMgr.acceptAllEdits()) {
					JOptionPane.showMessageDialog(frame,
							s_res.getString("ExitAction.errorAcceptingMsg"),
							title, JOptionPane.ERROR_MESSAGE);
					// Accept failed - quit now
					return false;
				}
			}
			
			if (selectionMgr.hasPendingUpdates()) {
				if (!selectionMgr.saveUpdates()) {
					JOptionPane.showMessageDialog(frame,
							s_res.getString("ExitAction.errorSavingMsg"),
							title, JOptionPane.ERROR_MESSAGE);
					// Save failed - quit
					return false;
				}
			}

			// changes made
			JOptionPane.showMessageDialog(frame,
					s_res.getString("ExitAction.changesAcceptedMsg"),
					s_res.getString("ExitAction.changesAcceptedTitle"),
					JOptionPane.INFORMATION_MESSAGE);
			okToExit = true;
			
		} else {
			// cancel
			okToExit = false;
		}
		
		if (okToExit) {
			// persist any local files
			ModelIOUtilities.persistDirectoryData();
		}
		return okToExit;
	}

}
