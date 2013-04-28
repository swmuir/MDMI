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

import java.awt.Frame;
import java.awt.event.ActionEvent;

import org.openhealthtools.mdht.mdmi.editor.common.components.AbstractHelpAboutDialog;

public abstract class AbstractHelpAboutAction extends AbstractMenuAction {

	private static final long serialVersionUID = -3468128886151826545L;

	/** Display the HelpAboutDialog as a non-modal dialog. */
   @Override
   public void execute(ActionEvent actionEvent) {
      Frame frame = getApplicationFrame();
      
      AbstractHelpAboutDialog dlg = getHelpAboutDialog(frame);
      dlg.display(frame);
   }

   /** Get the appropriate Help About Dialog */
   public abstract AbstractHelpAboutDialog getHelpAboutDialog(Frame parent);

}
