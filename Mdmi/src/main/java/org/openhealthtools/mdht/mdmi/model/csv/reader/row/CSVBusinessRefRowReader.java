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

import org.openhealthtools.mdht.mdmi.model.builder.*;
import org.openhealthtools.mdht.mdmi.model.csv.reader.*;
import org.openhealthtools.mdht.mdmi.model.raw.*;

public class CSVBusinessRefRowReader extends CSVRowReaderAbstract {
   public static final String   s_name             = "name";
   public static final String   s_refName          = "reference";
   public static final String   s_uniqueName       = "uniqueIdentifier";
   public static final String   s_descName         = "description";
   public static final String   s_datatypeName     = "referenceDatatype";
   public static final String   s_idName           = "BE_REf";
   public static final String   s_typeName         = "datatypeName";
   public static final String[] s_simpleFieldNames = { s_name, s_refName, s_uniqueName, s_descName };

   @Override
   protected void createAssociations( ClassDef def, CsvReader reader, RawRoot root,
         HashMap<String, ClassDef> singletonMap ) throws IOException {

      // Create association to datatype
      CSVReaderUtil.createAssociation(def, s_datatypeName, reader.get(s_typeName));

      // Create association to domain dictionary reference.
      ClassDef dictDef = singletonMap.get(StereotypeNames.MDMI_DOMAIN_DICT_REF);
      if( dictDef != null ) {
         CSVReaderUtil.createAssociation(dictDef, def.getName(), def.getId());
      }
   }

   @Override
   protected String getClassId( CsvReader reader ) throws IOException {
      return reader.get(s_idName);
   }

   @Override
   protected String getClassName( CsvReader reader ) throws IOException {
      return reader.get(s_name);
   }

   @Override
   protected String getRequiredColumn() {
      return s_name;
   }

   @Override
   protected String[] getSimpleFieldNames() {
      return s_simpleFieldNames;
   }

   @Override
   protected String getStereotypeName() {
      return StereotypeNames.MDMI_BUS_ELEM_REF;
   }
}
