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

public class CSVSyntaxModelReader extends CSVRowReaderAbstract {
   public static final String    s_syntaxModelIdAppend = "SyntaxModel";
   public static final String    s_nameActual          = "name";
   private static final String[] s_simpleFields        = {};

   @Override
   protected void createAssociations( ClassDef def, CsvReader reader, RawRoot root,
         HashMap<String, ClassDef> singletonMap ) throws IOException {

      // We have to do attributes here also because the names do not match the spec.
      // For now, use same name as message model
      Attribute attrib = CSVReaderUtil.addAttributeToClass(def);
      attrib.setName("name");
      attrib.getDefaultValueObject().setValue(reader.get(CSVMessageModelReader.s_msgModelName));

      // Add this to singleton map. This is used to handle implicit associations.
      singletonMap.put(StereotypeNames.MESSAGE_SYNTAX_MODEL, def);

      // If the message model is already created, then create the association to it.
      ClassDef modelDef = singletonMap.get(StereotypeNames.MESSAGE_MODEL);
      if( modelDef != null ) {
         attrib = CSVReaderUtil.addAssociationAttributeToClass(modelDef);
         attrib.setType(def.getId());
      }
   }

   @Override
   protected String getClassId( CsvReader reader ) throws IOException {
      return reader.get(CSVMessageGroupReader.s_refId) + s_syntaxModelIdAppend;
   }

   @Override
   protected String getClassName( CsvReader reader ) throws IOException {
      return reader.get(CSVMessageGroupReader.s_msgGrpName);
   }

   @Override
   protected String getRequiredColumn() {
      return CSVMessageGroupReader.s_msgGrpName;
   }

   @Override
   protected String[] getSimpleFieldNames() {
      return s_simpleFields;
   }

   @Override
   protected String getStereotypeName() {
      return StereotypeNames.MESSAGE_SYNTAX_MODEL;
   }
}
