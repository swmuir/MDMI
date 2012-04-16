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
package org.openhealthtools.mdht.mdmi.editor.common.menus;

import java.awt.Component;
import java.awt.Container;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

/**
 * @author fiedler
 *
 */
public abstract class MenuUtil
{

   /** change the selection state of the menu item (typically a JRadioButton or
    * JCheckboxButton) that uses the provided action.
    * @param	parent	The top component to start searching; typically a toolbar or menuBar
    * @param	action	The action to locate
    * @param	selected	The new selection state
    * @return	the menu component associated with the action
    */  
	public static AbstractButton selectMenuItem( JMenuBar menuBar, Action action, boolean selected ) {
		AbstractButton item = getMenuItem(menuBar, action);
		if (item != null) {
			item.setSelected(selected);
		}
		return item;
   }


   /** Find the JButton in the toolbar that is associated with the provided action */
   public static AbstractButton getToolbarButton (JToolBar toolbar, Action action) {
	   return getMenuItem(toolbar, action);
   }
   
   
   /** Recursively search the contents of the container to find a MenuItem or Button
    * that uses this action.
    * @param container
    * @param action
    * @return
    */
   public static AbstractButton getMenuItem( Container container, Action action ) {
      for (int i=0; i < container.getComponentCount(); i++ ) {
         Component item = container.getComponent(i);
         if (item instanceof AbstractButton && ((AbstractButton)item).getAction().equals( action ) ) {
            return (AbstractButton)item;
         } else if (item instanceof Container){
            return getMenuItem((Container)item, action);
         }
      }
      return null;
   }
}

