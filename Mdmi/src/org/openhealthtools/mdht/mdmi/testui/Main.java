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
 *     Gabriel Oancea
 *
 *******************************************************************************/
package org.openhealthtools.mdht.mdmi.testui;

import java.io.*;
import java.util.*;

import javax.swing.*;

import org.openhealthtools.mdht.mdmi.*;

public class Main {
   private static Properties s_properties;
   private static File       s_rootDir = new File(System.getProperties().getProperty("user.dir"));
   private static FrmMain    s_frmMain;

   private static File propertiesFile() {
      return new File(s_rootDir, "MdmiTestUI.settings");
   }

   public static void main( String[] args ) {
      if( args != null && 0 < args.length ) {
         File dir = new File(args[0]);
         if( dir.exists() && dir.isDirectory() )
            s_rootDir = dir;
      }

      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch( Exception e ) {
         e.printStackTrace();
         System.exit(-1);
      }
      try {
         if( !propertiesFile().exists() )
            createDefaultProperties();
         loadProperties();

         Mdmi.INSTANCE.initialize(s_rootDir);
         Mdmi.INSTANCE.start();

         s_frmMain = new FrmMain();
         s_frmMain.validate();
         s_frmMain.setVisible(true);
      }
      catch( Exception ex ) {
         JOptionPane.showMessageDialog(null, MdmiException.getFullDescription(ex), "Main fails",
               JOptionPane.ERROR_MESSAGE);
         System.exit(-1);
      }
   }

   public static Properties properties() {
      return s_properties;
   }

   public static boolean hasProperty( String key ) {
      return s_properties.containsKey(key);
   }

   public static String getPropertyString( String key ) {
      return s_properties.getProperty(key);
   }

   public static int getPropertyInt( String key ) {
      String s = s_properties.getProperty(key);
      if( s != null && s.length() > 0 )
         return Integer.valueOf(s).intValue();
      return 0;
   }

   public static boolean getPropertyBool( String key ) {
      String s = s_properties.getProperty(key);
      if( s != null && s.length() > 0 )
         return "true".equalsIgnoreCase(s);
      return false;
   }

   public static void setProperty( String key, String value ) {
      s_properties.setProperty(key, value);
   }

   public static void setProperty( String key, int value ) {
      s_properties.setProperty(key, new Integer(value).toString());
   }

   public static void setProperty( String key, boolean value ) {
      s_properties.setProperty(key, new Boolean(value).toString());
   }

   public static void loadProperties() {
      try {
         s_properties = new Properties();
         FileInputStream fis = new FileInputStream(propertiesFile());
         s_properties.loadFromXML(fis);
      }
      catch( Exception ex ) {
         throw new RuntimeException("Load properties fails", ex);
      }
   }

   public static void saveProperties() {
      try {
         File p = propertiesFile();
         File b = new File(p.getAbsolutePath() + ".bak");
         if( b.exists() )
            b.delete();
         if( p.exists() )
            p.renameTo(b);
         p.delete();
         FileOutputStream fos = new FileOutputStream(p);
         s_properties.storeToXML(fos, "MdmiTestUI properties");
      }
      catch( Exception ex ) {
         throw new RuntimeException("Saving properties fails", ex);
      }
   }

   private static void createDefaultProperties() {
      s_properties = new Properties();
      // add defaults
      saveProperties();
   }
} // MainApp
