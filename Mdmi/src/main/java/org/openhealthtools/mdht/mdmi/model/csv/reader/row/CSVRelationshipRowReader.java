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

public class CSVRelationshipRowReader extends CSVRowReaderAbstract {
   private static final String   s_idName       = "ID";
   private static final String   s_relateName   = "relatedSemanticElement";
   private static final String   s_shipsName    = "relationships";
   private static final String   s_minName      = "minOccurs";
   private static final String   s_maxName      = "maxOccurs";

   private static final String[] s_simpleFields = { "rule", "ruleExpressionLanguage", "sourceInstance",
         "targetInstance", "name", "description" };

   @Override
   protected void createAssociations( ClassDef def, CsvReader reader, RawRoot root,
         HashMap<String, ClassDef> singletonMap ) throws IOException {
      // Add min and max.
      String min = reader.get(s_minName);
      String max = reader.get(s_maxName);

      Attribute attrib = CSVReaderUtil.addAttributeToClass(def);
      attrib.setName(s_minName);
      attrib.getDefaultValueObject().setValue("*".equals(min) ? "0" : min);

      attrib = CSVReaderUtil.addAttributeToClass(def);
      attrib.setName(s_maxName);
      attrib.getDefaultValueObject().setValue("*".equals(max) ? Integer.toString(Integer.MAX_VALUE) : max);

      // Create links between this relationship and its elements.
      String relationshipLink = reader.get(s_shipsName);
      ClassDef elemDef = singletonMap.get(StereotypeNames.SEMANTIC_ELEMENT + relationshipLink);
      if( elemDef == null ) {
         throw new IllegalStateException("Relationship '" + def.getId() + "' has an " + "invalid link to element id '"
               + relationshipLink + "'.");
      }

      CSVReaderUtil.createAssociation(def, s_relateName, reader.get(s_relateName));
      CSVReaderUtil.createAssociation(def, s_shipsName, relationshipLink);
      CSVReaderUtil.createAssociation(elemDef, "", def.getId());
   }

   @Override
   protected String getClassId( CsvReader reader ) throws IOException {
      return reader.get(s_idName);
   }

   @Override
   protected String getClassName( CsvReader reader ) throws IOException {
      return reader.get(s_idName);
   }

   @Override
   protected String getRequiredColumn() {
      return s_idName;
   }

   @Override
   protected String[] getSimpleFieldNames() {
      return s_simpleFields;
   }

   @Override
   protected String getStereotypeName() {
      return StereotypeNames.SEMANTIC_ELEMENT_RELATIONSHIP;
   }
}
