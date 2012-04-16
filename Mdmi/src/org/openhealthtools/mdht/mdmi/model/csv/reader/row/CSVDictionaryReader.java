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

public class CSVDictionaryReader extends CSVRowReaderAbstract {
   public static final String    s_dictRefIdAppend = "MDMIDomainDictionaryRef";
   public static final String    s_nameActual      = "name";

   private static final String[] s_simpleFields    = {};

   @Override
   protected void createAssociations( ClassDef def, CsvReader reader, RawRoot root,
         HashMap<String, ClassDef> singletonMap ) throws IOException {

      // For now, use same name as message group
      Attribute attrib = CSVReaderUtil.addAttributeToClass(def);
      attrib.setName("name");
      attrib.getDefaultValueObject().setValue(reader.get(CSVMessageGroupReader.s_msgGrpName));

      // Add this to singleton map. This is used to
      // handle implicit associations.
      singletonMap.put(StereotypeNames.MDMI_DOMAIN_DICT_REF, def);

      // If the message group is already created, then create
      // the association to it.
      ClassDef groupDef = singletonMap.get(StereotypeNames.MESSAGE_GROUP);
      if( groupDef != null ) {
         attrib = CSVReaderUtil.addAssociationAttributeToClass(groupDef);
         attrib.setType(def.getId());
      }
   }

   @Override
   protected String getClassId( CsvReader reader ) throws IOException {
      return reader.get(CSVMessageGroupReader.s_refId) + s_dictRefIdAppend;
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
      return StereotypeNames.MDMI_DOMAIN_DICT_REF;
   }

}
