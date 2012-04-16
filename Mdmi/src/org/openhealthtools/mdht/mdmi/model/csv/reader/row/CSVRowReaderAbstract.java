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
package org.openhealthtools.mdht.mdmi.model.csv.reader.row;

import java.io.*;
import java.util.*;

import org.openhealthtools.mdht.mdmi.model.csv.reader.*;
import org.openhealthtools.mdht.mdmi.model.csv.reader.CSVReaderAbstract.*;
import org.openhealthtools.mdht.mdmi.model.raw.*;

public abstract class CSVRowReaderAbstract implements ICSVRowReader {

   @Override
   public void readRow( CsvReader reader, RawRoot root, HashMap<String, ClassDef> singletonMap,
         LinkedList<ItemInfo> stack ) throws IOException {

      if( !rowIsValid(reader) ) {
         // Skip this row.
         return;
      }

      // Create class definition.
      ClassDef def = CSVReaderUtil.createClass(getClassId(reader), getClassName(reader), root);

      // Read in all non-association fields.
      CSVReaderUtil.createSimpleAttributes(getSimpleFieldNames(), def, reader);

      // Add stereotype to root.
      CSVReaderUtil.createStereotype(getStereotypeName(), def, root);

      // Create associations
      createAssociations(def, reader, root, singletonMap);
   }

   private boolean rowIsValid( CsvReader reader ) throws IOException {
      String req = reader.get(getRequiredColumn());
      return (req != null) && (req.length() > 0);
   }

   // Overrides
   protected abstract String getStereotypeName();

   protected abstract String getRequiredColumn();

   protected abstract String[] getSimpleFieldNames();

   protected abstract String getClassId( CsvReader reader ) throws IOException;

   protected abstract String getClassName( CsvReader reader ) throws IOException;

   protected abstract void createAssociations( ClassDef def, CsvReader reader, RawRoot root,
         HashMap<String, ClassDef> singletonMap ) throws IOException;
}
