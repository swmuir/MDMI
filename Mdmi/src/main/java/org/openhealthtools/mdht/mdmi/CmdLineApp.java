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
package org.openhealthtools.mdht.mdmi;

import java.io.*;
import java.util.*;

public class CmdLineApp {
   /**
    * Command line app - can be invoke from other apps.
    * 
    * @param args The arguments, see usage below.
    */
   public static void main( String[] args ) {
      if( args.length < 7 )
         usage();
      
      String srcMap = args[0];
      String srcMdl = args[1];
      String srcMsg = args[2];
      String trgMap = args[3];
      String trgMdl = args[4];
      String trgMsg = args[5];
      String cnvElm = args[6];

      System.out.println( "USING PARAMETERS:" );
      System.out.println( "  sourceMap    = " + srcMap );
      System.out.println( "  sourceModel  = " + srcMdl );
      System.out.println( "  sourceMsg    = " + srcMsg );
      System.out.println( "  targetMap    = " + trgMap );
      System.out.println( "  targetModel  = " + trgMdl );
      System.out.println( "  targetMsg    = " + trgMsg );
      System.out.println( "  convElements = " + cnvElm );
      System.out.println( "" );

      transform( srcMap, srcMdl, srcMsg, trgMap, trgMdl, trgMsg, cnvElm );
   }
   
   public static String transform( String srcMap, String srcMdl, String srcMsg, 
         String trgMap, String trgMdl, String trgMsg, String cnvElm ) 
   {
      File rootDir = new File(System.getProperties().getProperty( "user.dir" ));
      System.out.println( "  rootDir = " + rootDir.getAbsolutePath() );
      System.out.println( "" );
      
      // initialize the runtime, using the current folder as the roo folder
      Mdmi.INSTANCE.initialize( rootDir );
      Mdmi.INSTANCE.start();

      String retVal = null;
      try {
         // 1. check to make sure the maps and messages exist
         File f = Mdmi.INSTANCE.fileFromRelPath(srcMap);
         if( !f.exists() || !f.isFile() ) {
            System.out.println( "Source map file '" + srcMap + "' does not exist!" );
            usage();
         }

         f = Mdmi.INSTANCE.fileFromRelPath(trgMap);
         if( !f.exists() || !f.isFile() ) {
            System.out.println( "Target map file '" + trgMap + "' does not exist!" );
            usage();
         }

         f = Mdmi.INSTANCE.fileFromRelPath(srcMsg);
         if( !f.exists() || !f.isFile() ) {
            System.out.println( "Source message file '" + srcMsg + "' does not exist!" );
            usage();
         }

         f = Mdmi.INSTANCE.fileFromRelPath(trgMsg);
         if( !f.exists() || !f.isFile() ) {
            System.out.println( "Target message file '" + trgMsg + "' does not exist!" );
            usage();
         }
         
         // 2. make sure the qualified message names are spelled right 
         String[] a = srcMdl.split( "\\." );
         if( a == null || a.length != 2 ) {
            System.out.println( "Invalid source model '" + srcMdl + "', must be formatted as MapName.MessageName" );
            usage();
         }
         String srcMapName = a[0];
         String srcMsgMdl = a[1];
         
         a = trgMdl.split( "\\." );
         if( a == null || a.length != 2 ) {
            System.out.println( "Invalid target model '" + trgMdl + "', must be formatted as MapName.MessageName" );
            usage();
         }
         String trgMapName = a[0];
         String trgMsgMdl = a[1];

         // 3. parse the elements array
         ArrayList<String> elements = new ArrayList<String>();
         String[] ss = cnvElm.split( ";" );
         for( String s : ss ) {
            elements.add(s);
         }
         
         // 4. load the maps into the runtime.
         Mdmi.INSTANCE.getConfig().putMapInfo( new MdmiConfig.MapInfo(srcMapName, srcMap) );
         Mdmi.INSTANCE.getConfig().putMapInfo( new MdmiConfig.MapInfo(trgMapName, trgMap) );
         Mdmi.INSTANCE.getResolver().resolveConfig( Mdmi.INSTANCE.getConfig() );

         // 5. Construct the parameters to the call based on the values passed in
         MdmiModelRef sMod = new MdmiModelRef( srcMapName, srcMsgMdl );
         MdmiMessage  sMsg = new MdmiMessage( Mdmi.INSTANCE.fileFromRelPath(srcMsg) );
         MdmiModelRef tMod = new MdmiModelRef( trgMapName, trgMsgMdl );
         MdmiMessage  tMsg = new MdmiMessage( Mdmi.INSTANCE.fileFromRelPath(trgMsg) );
         MdmiTransferInfo ti = new MdmiTransferInfo( sMod, sMsg, tMod, tMsg, elements );
         ti.useDictionary = true;
         
         // 6. call the runtime
         Mdmi.INSTANCE.executeTransfer( ti );
         
         // 7. set the return value
         retVal = tMsg.getDataAsString();
      }
      catch( Exception ex ) {
         ex.printStackTrace();
      }
      Mdmi.INSTANCE.stop();
      return retVal; // return the target message transformed
   }
   
   private static void usage() {
      System.out.println( "PARAMETERS:" );
      System.out.println( "  sourceMap   = the file name of the source map to load, relative path" );
      System.out.println( "  sourceModel = the source model name formatted as messageGroupName.messageModelName" );
      System.out.println( "  sourceMsg   = the file name of the source message to transform, relative path" );
      System.out.println( "  targetMap   = the file name of the target map to load, relative path" );
      System.out.println( "  targetModel = the target model name formatted as messageGroupName.messageModelName" );
      System.out.println( "  targetMsg   = the file name of the target message to transform, relative path" );
      System.out.println( "  elements    = the name(s) of the BERs to convert from source to target" );
      System.exit(-1);
   }
} // CmdLineApp

