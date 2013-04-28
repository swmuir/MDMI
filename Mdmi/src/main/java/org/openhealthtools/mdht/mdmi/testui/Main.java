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
   private static final File PROPERTIES = new File( "MdmiTestUI.settings" );

   private static Properties s_properties; 

	public static final File rootDir = new File(System.getProperties().getProperty("user.dir"));

	public static FrmMain frmMain;
	
	public static void main( String[] args ) {
      try {
         UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
      }
      catch( Exception e ) {
         e.printStackTrace();
         System.exit( -1 );
      }
      try {
   		if( !PROPERTIES.exists() )
            createDefaultProperties();
         loadProperties();
         
         Mdmi.INSTANCE.initialize(rootDir);
         Mdmi.INSTANCE.start();

         frmMain = new FrmMain();
         frmMain.validate();
         frmMain.setVisible( true );
      }
      catch( Exception ex ) {
         JOptionPane.showMessageDialog( null, MdmiException.getFullDescription(ex), 
         		"Main fails", JOptionPane.ERROR_MESSAGE );
         System.exit( -1 );
      }
	}

	public static Properties properties() {
		return s_properties;
	}
   
   public static boolean hasProperty( String key ) {
   	return s_properties.containsKey( key );
   }
   
   public static String getPropertyString( String key ) {
   	return s_properties.getProperty( key );
   }
   
   public static int getPropertyInt( String key ) {
   	String s = s_properties.getProperty( key );
   	if( s != null && s.length() > 0 )
   		return Integer.valueOf( s ).intValue();
   	return 0;
   }
   
   public static boolean getPropertyBool( String key ) {
   	String s = s_properties.getProperty( key );
   	if( s != null && s.length() > 0 )
   		return "true".equalsIgnoreCase( s );
   	return false;
   }

   public static void setProperty( String key, String value ) {
   	s_properties.setProperty( key, value );
   }

   public static void setProperty( String key, int value ) {
   	s_properties.setProperty( key, new Integer(value).toString() );
   }

   public static void setProperty( String key, boolean value ) {
   	s_properties.setProperty( key, new Boolean(value).toString() );
   }
   
	public static void loadProperties() {
   	try {
      	s_properties = new Properties();
   		FileInputStream fis = new FileInputStream( PROPERTIES );
   		s_properties.loadFromXML( fis );
		}
		catch( Exception ex ) {
			throw new RuntimeException( "Load properties fails", ex );
		}
   }

   public static void saveProperties() {
   	try {
			File b = new File( PROPERTIES.getAbsolutePath() + ".bak" );
   		if( b.exists() )
   			b.delete();
   		if( PROPERTIES.exists() )
   			PROPERTIES.renameTo( b );
   		PROPERTIES.delete();
   		FileOutputStream fos = new FileOutputStream( PROPERTIES );
      	s_properties.storeToXML( fos, "MdmiTestUI properties" );
		}
		catch( Exception ex ) {
			throw new RuntimeException( "Saving properties fails", ex );
		}
   }

   private static void createDefaultProperties() {
		s_properties = new Properties();
		// add defaults
		saveProperties();
   }
} // MainApp
