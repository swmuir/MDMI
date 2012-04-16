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
package org.openhealthtools.mdht.mdmi.editor.common;

import java.awt.event.*;
import javax.swing.*;

/**
 * Deault listener for popup menus.
 */
public class DefPopupListener extends MouseAdapter {
   private JPopupMenu mnuPopup;

   public DefPopupListener( JPopupMenu mnu ) {
      mnuPopup = mnu;
   }

   public void mousePressed( MouseEvent e ) {
      maybeShowPopup( e );
   }
   
   public void mouseReleased( MouseEvent e ) {
      maybeShowPopup( e );
   }

   private void maybeShowPopup( MouseEvent e ) {
      if( e.isPopupTrigger() )
    	  showPopup( e );
   }
   
   /** Show the popup menu. Derived classes may use this to enable/disable actions
    * before the menu is displayed.
    * @param e
    */
   protected void showPopup( MouseEvent e ) {
       mnuPopup.show( e.getComponent(), e.getX(), e.getY() );
   }
} // DefPopupListener