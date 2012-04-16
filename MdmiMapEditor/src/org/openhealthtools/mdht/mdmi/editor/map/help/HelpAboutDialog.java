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
package org.openhealthtools.mdht.mdmi.editor.map.help;


import java.awt.Frame;
import java.util.ResourceBundle;

import org.openhealthtools.mdht.mdmi.editor.common.components.AbstractHelpAboutDialog;

public class HelpAboutDialog extends AbstractHelpAboutDialog {

	/** Resource for localization */
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.help.Local");
	
   private static final long serialVersionUID = -1;

	public HelpAboutDialog(Frame owner) {
      super(owner);
      
      createComponents();
   }
	
   @Override
   public String getVersionString() {
	   // TODO Auto-generated method stub
	   return s_res.getString("HelpAboutDialog.version");
   }

}
