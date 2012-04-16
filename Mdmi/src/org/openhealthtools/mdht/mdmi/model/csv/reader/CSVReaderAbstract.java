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
package org.openhealthtools.mdht.mdmi.model.csv.reader;

import java.io.*;
import java.util.*;

import org.openhealthtools.mdht.mdmi.model.csv.reader.row.*;
import org.openhealthtools.mdht.mdmi.model.raw.*;

public abstract class CSVReaderAbstract implements ICSVFileReader {
   @Override
   public boolean canReadFile( String fileName ) {
      for( String curVal : getFileNameIndicators() ) {
         if( !fileName.toLowerCase().contains(curVal.toLowerCase()) ) {
            return false;
         }
      }
      return true;
   }

   @Override
   public void readFile( String filePath, RawRoot root, HashMap<String, ClassDef> singletonMap )
         throws FileNotFoundException, IOException {
      // The sheet we are reading should have a header.
      CsvReader reader = new CsvReader(filePath);
      if( !reader.readHeaders() ) {
         throw new IllegalArgumentException("'" + filePath + "' does not "
               + "contain a .csv file with valid map information.  " + "Header is missing.");
      }

      preProcess(root, singletonMap);
      LinkedList<ItemInfo> stack = new LinkedList<ItemInfo>();
      while( reader.readRecord() ) {
         ICSVRowReader[] readers = getRowReaders(reader, stack);
         if( readers == null ) {
            // The current row cannot be read.
            continue;
         }
         for( ICSVRowReader rowReader : readers ) {
            rowReader.readRow(reader, root, singletonMap, stack);
         }
      }
   }

   /**
    * File name must contain each string in the array in order for <code>canRead</code> to return true. Case
    * insensitive.
    */
   protected abstract String[] getFileNameIndicators();

   protected abstract ICSVRowReader[] getRowReaders( CsvReader reader, LinkedList<ItemInfo> stack ) throws IOException;

   protected void preProcess( RawRoot root, HashMap<String, ClassDef> singletonMap ) {
   }

   public static final class ItemInfo {
      ClassDef m_def;
      String   m_stereotypeName;
      int      m_index;

      public ItemInfo( ClassDef def, String stereotypeName ) {
         this(def, stereotypeName, 0);
      }

      public ItemInfo( ClassDef def, String stereotypeName, int index ) {
         m_def = def;
         m_stereotypeName = stereotypeName;
         m_index = index;
      }

      public String getStereotype() {
         return m_stereotypeName;
      }

      public ClassDef getClassDef() {
         return m_def;
      }

      public int getIndex() {
         return m_index;
      }
   }
}
