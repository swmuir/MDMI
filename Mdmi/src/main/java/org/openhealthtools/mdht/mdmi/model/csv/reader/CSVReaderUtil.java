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

import java.io.*;

import org.openhealthtools.mdht.mdmi.model.raw.*;

public class CSVReaderUtil {
   // TODO - move this
   private static final String s_assocInd = "association";

   // TODO - move this
   public static ClassDef createClass( String id, String name, RawRoot root ) {
      // Create class definition.
      ClassDef def = new ClassDef();
      def.setId(id);
      def.setName(name);

      // Add class definition to root.
      root.getModel().addClassDef(def);
      return def;
   }

   public static void createSimpleAttributes( String[] names, ClassDef def, CsvReader reader ) throws IOException {
      for( String curField : names ) {
         Attribute attrib = CSVReaderUtil.addAttributeToClass(def);
         attrib.setName(curField);
         attrib.getDefaultValueObject().setValue(reader.get(curField));
      }
   }

   public static void createStereotype( String stereotypeName, ClassDef def, RawRoot root ) {
      StereotypeInstance instance = new StereotypeInstance();
      instance.setName(stereotypeName);
      instance.setBaseRef(def.getId());
      root.addStereotypeInstance(instance);
   }

   // TODO- move these methods
   public static Attribute createAssociation( ClassDef def, String name, String type ) {
      Attribute attrib = CSVReaderUtil.addAssociationAttributeToClass(def);
      attrib.setName(name);
      attrib.setType(type);
      return attrib;
   }

   public static Attribute addAttributeToClass( ClassDef def ) {
      Attribute attrib = new Attribute();
      def.addAttribute(attrib);
      DefaultValue val = new DefaultValue();
      attrib.setDefaultValue(val);
      return attrib;
   }

   public static Attribute addAssociationAttributeToClass( ClassDef def ) {
      Attribute attrib = new Attribute();
      attrib.setAssociation(s_assocInd);
      def.addAttribute(attrib);
      return attrib;
   }
}
