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
package org.openhealthtools.mdht.mdmi.editor.map;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.actions.ActionRegistry;

/** Common menu bar, toolbar and popup menu actions.
 * @see	MenuUtil, ApplicationResources */
public class Actions {

	// Action key names - these are used in application.xml and appres.properties
	// -- File Menu -- //
	public static final String FILE_MENU_ACTION = "file";
	public static final String NEW_CHILD_ACTION = "newChild";
	public static final String EXIT_ACTION      = "exit";
	public static final String SAVE_ACTION   	  = "save";
	public static final String ACCEPT_ACTION      = "accept";
	public static final String ACCEPT_ALL_ACTION  = "acceptAll";
	
	// -- New Sub Menu -- //
	public static final String NEW_MENU_ACTION = "new";
	
	// -- Edit Menu -- //
	public static final String EDIT_MENU_ACTION = "edit";
	public static final String EDIT_ACTION      = "openEditor";
	public static final String CUT_ACTION       = "cut";
	public static final String COPY_ACTION      = "copy";
	public static final String PASTE_ACTION     = "paste";
	public static final String DELETE_ACTION    = "delete";
	public static final String MOVE_UP_ACTION   = "moveUp";
	public static final String MOVE_DOWN_ACTION = "moveDown";
	
	// -- Admin Menu -- //
	public static final String ADMIN_MENU_ACTION = "admin";
	
	// -- View Menu -- //
	public static final String VIEW_MENU_ACTION     = "view";
	public static final String EXPAND_NODE_ACTION   = "expandAll";
	public static final String COLLAPSE_NODE_ACTION = "collapseAll";
	
	// -- Tools Menu -- //
	public static final String TOOLS_MENU_ACTION      = "tools";	
	public static final String FIND_REFERENCES_ACTION = "findReferences";
	public static final String CHANGE_TYPE_ACTION     = "changeType";
	
	// -- Help Menu -- //
	public static final String HELP_MENU_ACTION = "help";
	public static final String HELP_ABOUT_ACTION = "helpAbout";

	
	/** Return the Action associated with the action key name */
	public static Action getActionInstance(String actionKeyName) {
		try {
			Action action = ActionRegistry.getActionInstance(actionKeyName);
			return action;
		} catch (InstantiationException e) {
			SystemContext.getLogWriter().loge(e, "Unable to identify action for " + actionKeyName);
		}
		return null;
	}

	/** Enable the Action associated with the action key name */
	public static void enableAction(String actionKeyName, boolean enable) {
		Action action = getActionInstance(actionKeyName);
		if (action != null) {
			action.setEnabled(enable);
		}
	}
	
	/** Perform this action */
	public static void performAction(String actionKeyName) {
		Action action = getActionInstance(actionKeyName);
		if (action != null) {
			action.actionPerformed(new ActionEvent(Actions.class, 0, "perform action"));
		}
	}
	
}
