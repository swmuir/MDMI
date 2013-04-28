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

public class CSVMessageModelReader extends CSVRowReaderAbstract {
   public static final String    s_modelIdAppend   = "Model";
   public static final String    s_msgModelName    = "MessageModelName";
   public static final String    s_msgModelSrc     = "MessageModelSource";
   public static final String    s_msgModelDesc    = "MessageModelDescription";
   public static final String    s_modelNameActual = "messageModelName";
   public static final String    s_modelDescActual = "description";
   public static final String    s_modelSrcActual  = "source";

   private static final String[] s_simpleFields    = {};

   @Override
   protected void createAssociations( ClassDef def, CsvReader reader, RawRoot root,
         HashMap<String, ClassDef> singletonMap ) throws IOException {
      // We have to do attributes here also because the names do not match the spec.
      Attribute attrib = CSVReaderUtil.addAttributeToClass(def);
      attrib.setName(s_modelNameActual);
      attrib.getDefaultValueObject().setValue(reader.get(s_msgModelName));

      attrib = CSVReaderUtil.addAttributeToClass(def);
      attrib.setName(s_modelDescActual);
      attrib.getDefaultValueObject().setValue(reader.get(s_msgModelDesc));

      attrib = CSVReaderUtil.addAttributeToClass(def);
      attrib.setName(s_modelSrcActual);
      attrib.getDefaultValueObject().setValue(reader.get(s_msgModelSrc));

      // Add this to singleton map. This is used to handle implicit associations.
      singletonMap.put(StereotypeNames.MESSAGE_MODEL, def);

      // If the message group is already created, then create
      // the association to it. Same for syntax model.
      ClassDef groupDef = singletonMap.get(StereotypeNames.MESSAGE_GROUP);
      if( groupDef != null ) {
         attrib = CSVReaderUtil.addAssociationAttributeToClass(groupDef);
         attrib.setType(def.getId());
      }

      ClassDef syntaxDef = singletonMap.get(StereotypeNames.MESSAGE_SYNTAX_MODEL);
      if( syntaxDef != null ) {
         attrib = CSVReaderUtil.addAssociationAttributeToClass(def);
         attrib.setType(syntaxDef.getId());
      }
   }

   @Override
   protected String getClassId( CsvReader reader ) throws IOException {
      return reader.get(CSVMessageGroupReader.s_refId) + s_modelIdAppend;
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
      return StereotypeNames.MESSAGE_MODEL;
   }

}
