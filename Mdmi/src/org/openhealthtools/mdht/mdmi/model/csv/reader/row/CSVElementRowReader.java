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

public class CSVElementRowReader extends CSVRowReaderAbstract {
   public static final String    s_refId          = "Ref";
   public static final String    s_msgElemName    = "elementName";
   public static final String    s_datatypeName   = "datatype";
   public static final String    s_datatypeRef    = "datatype";
   public static final String    s_descName       = "description";
   public static final String    s_multInstName   = "multipleInstances";
   public static final String    s_orderName      = "ordering";
   public static final String    s_orderLangName  = "orderingLanguage";
   public static final String    s_elemType       = "elementType";
   private static final String   s_propQualAttrib = "propertyQualifier";

   private static final String[] s_simpleFields   = { s_msgElemName, s_descName, s_multInstName, s_orderName,
         s_orderLangName, s_elemType             };

   @Override
   protected String getClassId( CsvReader reader ) throws IOException {
      return reader.get(s_refId);
   }

   @Override
   protected String getClassName( CsvReader reader ) throws IOException {
      return reader.get(s_msgElemName);
   }

   @Override
   protected String getRequiredColumn() {
      return s_msgElemName;
   }

   @Override
   protected String[] getSimpleFieldNames() {
      return s_simpleFields;
   }

   @Override
   protected String getStereotypeName() {
      return StereotypeNames.SEMANTIC_ELEMENT;
   }

   @Override
   protected void createAssociations( ClassDef def, CsvReader reader, RawRoot root,
         HashMap<String, ClassDef> singletonMap ) throws IOException {

      ClassDef elemSetDef = singletonMap.get(StereotypeNames.SEMANTIC_ELEMENT_SET);

      // Create association to element set
      if( elemSetDef != null ) {
         CSVReaderUtil.createAssociation(elemSetDef, def.getId(), def.getId());
      }

      // Create association to datatype
      CSVReaderUtil.createAssociation(def, s_datatypeName, reader.get(s_datatypeRef));

      // Create association to property qualifier
      Attribute propAttrib = CSVReaderUtil.createAssociation(def, s_propQualAttrib, StereotypeNames.ME_PROP_QUALIFIER);
      DefaultValue val = new DefaultValue();
      val.setInstance(reader.get(s_propQualAttrib));
      propAttrib.setDefaultValue(val);

      // Make element def available to associated objects.
      singletonMap.put(StereotypeNames.SEMANTIC_ELEMENT + def.getId(), def);
   }

}
