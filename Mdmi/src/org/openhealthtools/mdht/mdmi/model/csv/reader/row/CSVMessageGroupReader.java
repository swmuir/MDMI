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

public class CSVMessageGroupReader extends CSVRowReaderAbstract {
   public static final String    s_refId        = "Ref";
   public static final String    s_msgGrpName   = "MessageGroupName";
   public static final String    s_msgGrpDesc   = "MessageGroupDescription";
   public static final String    s_defLocEL     = "DefaultLocationExpressionLanguage";
   public static final String    s_defConEL     = "DefaultConstraintExpressionLanguage";
   public static final String    s_defRuleEL    = "DefaultRuleExpressionLanguage";
   public static final String    s_nameActual   = "name";
   public static final String    s_descActual   = "description";
   public static final String    s_locActual    = "defaultLocationExpressionLanguage";
   public static final String    s_conActual    = "defaultConstraintExpressionLanguage";
   public static final String    s_ruleActual   = "defaultRuleExpressionLanguage";
   private static final String[] s_simpleFields = {};

   @Override
   protected void createAssociations( ClassDef def, CsvReader reader, RawRoot root,
         HashMap<String, ClassDef> singletonMap ) throws IOException {

      // We have to do attributes here also because
      // the names do not match the spec.
      Attribute attrib = CSVReaderUtil.addAttributeToClass(def);
      attrib.setName(s_nameActual);
      attrib.getDefaultValueObject().setValue(reader.get(s_msgGrpName));

      attrib = CSVReaderUtil.addAttributeToClass(def);
      attrib.setName(s_descActual);
      attrib.getDefaultValueObject().setValue(reader.get(s_msgGrpDesc));

      attrib = CSVReaderUtil.addAttributeToClass(def);
      attrib.setName(s_locActual);
      attrib.getDefaultValueObject().setValue(reader.get(s_defLocEL));

      attrib = CSVReaderUtil.addAttributeToClass(def);
      attrib.setName(s_conActual);
      attrib.getDefaultValueObject().setValue(reader.get(s_defConEL));

      attrib = CSVReaderUtil.addAttributeToClass(def);
      attrib.setName(s_ruleActual);
      attrib.getDefaultValueObject().setValue(reader.get(s_defRuleEL));

      // Add this to singleton map. This is used to
      // handle implicit associations.
      singletonMap.put(StereotypeNames.MESSAGE_GROUP, def);

      // If the message model is already created, then create
      // the association to it.
      ClassDef modelDef = singletonMap.get(StereotypeNames.MESSAGE_MODEL);
      if( modelDef != null ) {
         attrib = CSVReaderUtil.addAssociationAttributeToClass(def);
         attrib.setType(modelDef.getId());
      }

      // If the domain dictionary reference is already created, then create
      // the association to it.
      ClassDef dictDef = singletonMap.get(StereotypeNames.MDMI_DOMAIN_DICT_REF);
      if( dictDef != null ) {
         attrib = CSVReaderUtil.addAssociationAttributeToClass(def);
         attrib.setType(dictDef.getId());
      }
   }

   @Override
   protected String getClassId( CsvReader reader ) throws IOException {
      return reader.get(s_refId);
   }

   @Override
   protected String getClassName( CsvReader reader ) throws IOException {
      return reader.get(s_msgGrpName);
   }

   @Override
   protected String getRequiredColumn() {
      return s_msgGrpName;
   }

   @Override
   protected String[] getSimpleFieldNames() {
      return s_simpleFields;
   }

   @Override
   protected String getStereotypeName() {
      return StereotypeNames.MESSAGE_GROUP;
   }
}
