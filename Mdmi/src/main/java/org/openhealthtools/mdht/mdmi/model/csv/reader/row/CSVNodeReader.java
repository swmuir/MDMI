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
import org.openhealthtools.mdht.mdmi.model.csv.reader.CSVReaderAbstract.*;
import org.openhealthtools.mdht.mdmi.model.raw.*;

public class CSVNodeReader implements ICSVRowReader {
   private static final String   s_nameField        = "name";
   private static final String   s_elemName         = "semanticElement";
   private static final String   s_nodeName         = "syntaxNode";

   public static final String    s_minName          = "minOccurs";
   public static final String    s_maxName          = "maxOccurs";
   public static final String    s_descName         = "description";
   public static final String    s_locName          = "location";
   public static final String    s_locExprLangName  = "locationExpressionLanguage";
   public static final String    s_fieldName        = "fieldName";

   private static final String[] s_nodeSimpleFields = { s_descName, s_locExprLangName, s_fieldName };

   private String[]              m_combinedSimpleFields;

   protected CSVNodeReader( String[] simpleFields ) {
      List<String> asList = new ArrayList<String>(Arrays.asList(s_nodeSimpleFields));
      for( String curField : simpleFields ) {
         asList.add(curField);
      }
      m_combinedSimpleFields = asList.toArray(new String[] {});
   }

   @Override
   public void readRow( CsvReader reader, RawRoot root, HashMap<String, ClassDef> singletonMap,
         LinkedList<ItemInfo> stack ) throws IOException {

      ItemInfo info = stack.peek();
      if( info == null ) {
         throw new IllegalStateException("Node stack is empty when trying to read node info.");
      }

      // Read in all non-association fields.
      CSVReaderUtil.createSimpleAttributes(m_combinedSimpleFields, info.getClassDef(), reader);
      addLocationAttribute(reader, info.getClassDef());

      // Add min and max.
      String min = reader.get(s_minName);
      String max = reader.get(s_maxName);

      Attribute attrib = CSVReaderUtil.addAttributeToClass(info.getClassDef());
      attrib.setName(s_minName);
      attrib.getDefaultValueObject().setValue("*".equals(min) ? "0" : min);

      attrib = CSVReaderUtil.addAttributeToClass(info.getClassDef());
      attrib.setName(s_maxName);
      attrib.getDefaultValueObject().setValue("*".equals(max) ? Integer.toString(Integer.MAX_VALUE) : max);

      // Name attribute does not have a column header.
      attrib = CSVReaderUtil.addAttributeToClass(info.getClassDef());
      attrib.setName(s_nameField);
      attrib.getDefaultValueObject().setValue(info.getClassDef().getName());

      // Create association to message element
      String elemId = reader.get(s_elemName);
      ClassDef elemDef = singletonMap.get(StereotypeNames.SEMANTIC_ELEMENT + elemId);

      if( elemDef != null ) {
         CSVReaderUtil.createAssociation(elemDef, s_elemName, info.getClassDef().getId());
         CSVReaderUtil.createAssociation(info.getClassDef(), s_nodeName, elemDef.getId());
      }
   }

   private void addLocationAttribute( CsvReader reader, ClassDef def ) throws IOException {
      String location = reader.get(s_locName);
      if( location == null || location.length() == 0 ) {
         location = def.getName();
      }

      Attribute attrib = CSVReaderUtil.addAttributeToClass(def);
      attrib.setName(s_locName);
      attrib.getDefaultValueObject().setValue(location);
   }
}
