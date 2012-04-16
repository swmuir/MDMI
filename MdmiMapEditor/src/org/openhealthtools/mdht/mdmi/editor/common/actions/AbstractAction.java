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
/*
 * AbstractTreeNodeAction.java
 *
 * Created on August 20, 2002, 11:15 AM
 */

package org.openhealthtools.mdht.mdmi.editor.common.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.components.CursorManager;
import org.openhealthtools.mdht.mdmi.editor.common.components.ExceptionDetailsDialog;



/**
 *
 * AbstractAction that handles cursor and exceptions
 */
public abstract class AbstractAction extends javax.swing.AbstractAction {
	
	/**
	 * generated id
	 */
	private static final long serialVersionUID = 5385542095934495513L;

	/** Creates a new instance of AbstractTreeNodeAction */
	public AbstractAction() {
	}

	/** get the value of the NAME attribute if set. Otherwise return
	 * the class name */
	protected String getActionName() {
		Object actionName = getValue(NAME);
		if (actionName == null) {
			actionName = getClass().getName();
		}
		return actionName.toString();
	}

	/** get the application frame */
	protected Frame getApplicationFrame() {
		Frame frame = SystemContext.getApplicationFrame();
		if (frame == null) {
			frame = new Frame();
		}

		return frame;
	}

	/** perform the action by calling execute() */
	public void actionPerformed(ActionEvent actionEvent) {

		Frame frame = getApplicationFrame();

		CursorManager cm = CursorManager.getInstance(frame);

		try {
			cm.setWaitCursor();
			
			if (actionAllowed(actionEvent)) {
				execute(actionEvent);
			}

		} catch (Exception ex) {

			String message = "Error invoking \"" + getActionName() + "\"";
			logActionError(message, ex);
			ExceptionDetailsDialog.showException(frame, ex);

		} finally {
			cm.restoreCursor();
		}
	}
	
	/** Verify that action is allowed for whatever reason - default is always allowed */
	public boolean actionAllowed(ActionEvent actionEvent) {
		return true;
	}

	public abstract void execute(ActionEvent actionEvent);


	/** Log error associated with creating or using Actions */
	public static void logActionError(Throwable thrown) {
		SystemContext.getLogWriter().loge(thrown, AbstractAction.class.getName());
	}
	/** Log error associated with creating or using Actions */
	public static void logActionError(String msg, Throwable thrown) {
		SystemContext.getLogWriter().loge(thrown, msg);
	}

}
