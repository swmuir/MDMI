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

import java.util.*;

import org.openhealthtools.mdht.mdmi.model.builder.*;
import org.openhealthtools.mdht.mdmi.model.csv.reader.row.*;
import org.openhealthtools.mdht.mdmi.model.raw.*;

public class CSVSemanticElementReader extends CSVReaderAbstract {
   private static final String          s_elemSetIdAppend = "ElementSet";
   private static final String[]        s_elemsNameInd    = { "semantic", "element" };
   private static final String          s_syntaxNodeName  = "syntaxNode";
   private static final String          s_messageElemName = "semanticElement";

   private static final ICSVRowReader[] s_rowReader       = { new CSVPropertyQualifierBuilder(),
         new CSVElementRowReader() };

   @Override
   protected String[] getFileNameIndicators() {
      return s_elemsNameInd;
   }

   @Override
   protected void preProcess( RawRoot root, HashMap<String, ClassDef> singletonMap ) {
      // Create the message element set here, because it is not specified explicitly in any spreadsheet.
      ClassDef modelDef = singletonMap.get(StereotypeNames.MESSAGE_MODEL);
      if( modelDef == null ) {
         throw new IllegalArgumentException("Unable to find message model to "
               + "associate with new message element set.");
      }

      ClassDef def = new ClassDef();
      def.setId(modelDef.getId() + s_elemSetIdAppend);
      def.setName(modelDef.getName()); // Use same name as message model.

      // For now, use same name as message model
      Attribute attrib = CSVReaderUtil.addAttributeToClass(def);
      attrib.setName("name");
      attrib.getDefaultValueObject().setValue(modelDef.getName());

      root.getModel().addClassDef(def);

      // Create stereotype definition.
      StereotypeInstance instance = new StereotypeInstance();
      instance.setName(StereotypeNames.SEMANTIC_ELEMENT_SET);
      instance.setBaseRef(def.getId());

      root.addStereotypeInstance(instance);

      // Create association to message model.
      attrib = CSVReaderUtil.addAssociationAttributeToClass(modelDef);
      attrib.setType(def.getId());

      // Add to map
      singletonMap.put(StereotypeNames.SEMANTIC_ELEMENT_SET, def);

      // Create association to syntax model.
      ClassDef syntDef = singletonMap.get(StereotypeNames.MESSAGE_SYNTAX_MODEL);
      if( syntDef != null ) {
         CSVReaderUtil.createAssociation(def, s_syntaxNodeName, syntDef.getId());
         CSVReaderUtil.createAssociation(syntDef, s_messageElemName, def.getId());
      }
   }

   @Override
   protected ICSVRowReader[] getRowReaders( CsvReader reader, LinkedList<ItemInfo> stack ) {
      return s_rowReader;
   }
}
