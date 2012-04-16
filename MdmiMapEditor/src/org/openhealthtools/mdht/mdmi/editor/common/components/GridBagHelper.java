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
package org.openhealthtools.mdht.mdmi.editor.common.components;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;

/** This class contains some useful methods for dealing
 * with a GridBagLayout
 * @author Conway
 *
 */
public class GridBagHelper {
   
   /** Create a panel containing a label, followed by an etched horizontal line */
   public static JPanel createLabeledLine(String text) {
      return createLabeledLine(new JLabel(text), new HorizontalLine(HorizontalLine.ETCHED, 1));
   }

   /** Create a panel containing a label, followed by an etched horizontal line */
   public static JPanel createLabeledLine(JLabel label, HorizontalLine horizLine) {
      JPanel panel = new JPanel(new GridBagLayout());
      
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.anchor = GridBagConstraints.WEST;
      gbc.weighty = 1;
      gbc.gridx = 0;
      
      // label
      gbc.weightx = 0;
      gbc.fill = GridBagConstraints.NONE;
      panel.add(label, gbc);

      // line
      gbc.gridx++;
      gbc.weightx = 1;
      gbc.insets.left = Standards.LEFT_INSET;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      panel.add(horizLine, gbc);
      
      return panel;
   }
   /** Create a panel containing a line of components. The components will be
    * layed out horizontally, with spacing between the components equal to the
    * LEFT and RIGHT inset values. There is no spacing top or bottom.
    * <code>
    *          R+L         R+L
    * |       |   |       |   |       |      |       |  
    * [--C1---]   [--C2---]   [--C3---] ...  [--Cn---]
    * </code>
    * @return a single-line panel
    */
   public static JPanel createLine(Component[] comp) {
      String [] text = new String[comp.length];
      return createLine(text, comp);
   }
   
   /** Create a panel containing a line of components. Each text item will become a
    * JLable. The components will be layed out in the form:
    * <code>
    *    Label1: [--C1---]   Label2: [--C2---] ...
    * </code>
    * Where the label/component tuples are spaced using the Standards.LEFT and
    * Standards.RIGHT values as shown below. There is no spacing top or bottom.
    * <code>
    *        R         R+L       R                      R        
    * |     | |       |   |     | |       |      |     | |       |  
    * Label1: [--C1---]   Label2: [--C2---] ...  LabelN: [--Cn---]
    * </code>
    * 
    * @return a single-line panel
    */
   public static JPanel createLine(String[] text, Component[] comp) {
      Icon [] icons = new Icon[text.length];
      return createLine(icons, text, comp);
   }
   
   /** Create a panel containing a line of components. Each text item will become a
    * JLable, with an icon (if the value is non-null).
    *  The components will be layed out in the form:
    * <code>
    *    (I)Label1: [--C1---]   (I)Label2: [--C2---] ...
    * </code>
    * Where the label/component tuples are spaced using the Standards.LEFT and
    * Standards.RIGHT values as shown below. There is no spacing top or bottom.
    * <code>
    *        R         R+L       R                      R        
    * |     | |       |   |     | |       |      |     | |       |  
    * Label1: [--C1---]   Label2: [--C2---] ...  LabelN: [--Cn---]
    * </code>
    * 
    * @return a single-line panel
    */
   public static JPanel createLine(Icon[] icons, String[] text, Component[] comp) {
      JPanel panel = new JPanel();

      createLine(panel, icons, text, comp);
      
      return panel;
   }
   
   /** Fill panel with a single row of components. Each text item will become a
    * JLable, with an icon (if the value is non-null).
    *  The components will be layed out in the form:
    * <code>
    *    (I)Label1: [--C1---]   (I)Label2: [--C2---] ...
    * </code>
    * Where the label/component tuples are spaced using the Standards.LEFT and
    * Standards.RIGHT values as shown below. There is no spacing top or bottom.
    * <code>
    *        R         R+L       R                      R        
    * |     | |       |   |     | |       |      |     | |       |  
    * Label1: [--C1---]   Label2: [--C2---] ...  LabelN: [--Cn---]
    * </code>
    * 
    * @return a single-line panel
    */
   public static void createLine(JPanel panel, Icon[] icons, String[] text, Component[] comp) {
      panel.setLayout(new GridBagLayout());
      
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.anchor = GridBagConstraints.WEST;
      gbc.weighty = 1;
      gbc.gridx = 0;
      gbc.insets.left = 0;
      
      for (int i=0; i<text.length && i<comp.length; i++) {
         gbc.fill = GridBagConstraints.NONE;
         gbc.weightx = 0;
         gbc.insets.right = Standards.RIGHT_INSET;
         if (i < text.length && text[i] != null) {
            JLabel label = new JLabel(text[i]);
            if (icons != null && i < icons.length && icons[i] != null) {
               label.setIcon(icons[i]);
            }
            panel.add(label, gbc);
            
            gbc.gridx++;
         }
         
         // last component doesn't have a right inset
         if (i == comp.length-1) {
            gbc.insets.right = 0;
         }
         gbc.fill = GridBagConstraints.BOTH;
         gbc.weightx = 1;
         gbc.insets.left = 0;
         if (comp != null && i < comp.length && comp[i] != null) {
            panel.add(comp[i], gbc);
         }
         
         // first label doesn't have a left inset, subsequent ones do
         gbc.insets.left = Standards.LEFT_INSET; 
         gbc.gridx++;
      }
   }
}
