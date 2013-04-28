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


import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;

/**
 * @author Conway
 *
 * Action Handler for system logoff
 */
public class LogoffAction extends AbstractMenuAction {

	private static final long serialVersionUID = -8045616024045749653L;
	
	/** Resource for localization */
   private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.common.actions.Local");
   
   /** Creates a new instance of LogoffAction */
   public LogoffAction() {}
   
   @Override
   public void execute(java.awt.event.ActionEvent actionEvent)
   {
      doLogout();
   }

   /** Prompt to log out, and perform the necessary actions if the user confirms.
    * An error message will be shown to the user if the service call fails, however
    * a true value will be returned.
    * @return false if the user does not confirm.  */
   public boolean doLogout()  {
      boolean loggedOut = false;
      

      // Do you really want to Log Off from the <Name> application?
      String message = MessageFormat.format( s_res.getString("LogoutDialog.text"),
            getActionName(), SystemContext.getApplicationName() );
      
      String title = MessageFormat.format(s_res.getString("LogoutDialog.title"),
            getActionName(), SystemContext.getApplicationName() );
      
      int opt = JOptionPane.showConfirmDialog(getApplicationFrame(), message, title,
            JOptionPane.YES_NO_OPTION);
      
      if (opt == JOptionPane.YES_OPTION) {
         loggedOut = true;
         //LoginMgr.getInstance().logout();
         
      } else {
         loggedOut = false;
      }
      return loggedOut;
   }

}
