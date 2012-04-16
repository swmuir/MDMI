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
package org.openhealthtools.mdht.mdmi.editor.common.actions;


public class ExitAction extends LogoffAction {
	private static final long serialVersionUID = 933746233731184092L;

	@Override
	public void execute(java.awt.event.ActionEvent actionEvent)
	{
		if (doLogout()) {   // prompt user
			System.exit(0);
		}
	}

}
