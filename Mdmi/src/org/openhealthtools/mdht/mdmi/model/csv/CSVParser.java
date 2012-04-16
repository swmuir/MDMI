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
package org.openhealthtools.mdht.mdmi.model.csv;

import java.io.*;
import java.util.*;

import org.openhealthtools.mdht.mdmi.model.csv.reader.*;
import org.openhealthtools.mdht.mdmi.model.raw.*;

public class CSVParser {
   private static final CSVRootReader            m_rootReader       = new CSVRootReader();
   private static final CSVSemanticElementReader m_elemsReader      = new CSVSemanticElementReader();
   private static final CSVDatatypeReader        m_datatypeReader   = new CSVDatatypeReader();
   private static final CSVRelationshipsReader   m_relateReader     = new CSVRelationshipsReader();
   private static final CSVConversionReader      m_conversionReader = new CSVConversionReader();
   private static final CSVSyntaxReader          m_syntaxReader     = new CSVSyntaxReader();
   private static final CSVBusinessRefReader     m_busRefReader     = new CSVBusinessRefReader();

   public static RawRoot parse( String csvFilesDir ) throws FileNotFoundException, IOException {
      File file = new File(csvFilesDir);
      if( !file.isDirectory() ) {
         throw new IllegalArgumentException("CSV parser must be passed a directory path.  '" + file.getAbsolutePath()
               + "' does not point to a directory.");
      }
      // The objects that the CSV readers will populate.
      RawRoot root = new RawRoot();
      ModelRoot modelRoot = new ModelRoot();
      root.setModel(modelRoot);
      HashMap<String, ClassDef> singletonMap = new HashMap<String, ClassDef>();

      // Give all other readers a chance. This order
      // must be maintained.
      read(file, m_rootReader, root, singletonMap);
      read(file, m_datatypeReader, root, singletonMap);
      read(file, m_elemsReader, root, singletonMap);
      read(file, m_relateReader, root, singletonMap);
      read(file, m_conversionReader, root, singletonMap);
      read(file, m_syntaxReader, root, singletonMap);
      read(file, m_busRefReader, root, singletonMap);
      return root;
   }

   private static void read( File rootDir, ICSVFileReader toRead, RawRoot root, HashMap<String, ClassDef> singletonMap )
         throws FileNotFoundException, IOException {
      for( File curFile : rootDir.listFiles() ) {
         // Data types
         if( toRead.canReadFile(curFile.getName()) ) {
            toRead.readFile(curFile.getAbsolutePath(), root, singletonMap);
         }
      }
   }
}
