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
 * Created on Nov 3, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.openhealthtools.mdht.mdmi.editor.common.actions;

/**
 * @author Conway
 *
 * Action Handler for system logoff
 */
public class LoginAction extends AbstractMenuAction {

	private static final long serialVersionUID = 3974639674651731393L;

	/** Creates a new instance of LogoffAction */
	public LoginAction() {}

	@Override
	public void execute(java.awt.event.ActionEvent actionEvent)
	{
		// Use the login manager to display the dialog.
		// Requires that applicationFrame and applicationName be set
		//  in the SystemContext class
		//LoginMgr.getInstance().showLoginDialog();
	}


}
