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

public class CSVConversionRuleFromReader implements ICSVRowReader {
   public static final String s_refName        = "FRRef";
   public static final String s_ruleName       = "fromMdmi";
   public static final String s_busElemName    = "fromBusinessElement";
   public static final String s_ruleAttribName = "rule";
   public static final String s_busElemRefName = "businessElement";

   @Override
   public void readRow( CsvReader reader, RawRoot root, HashMap<String, ClassDef> singletonMap,
         LinkedList<ItemInfo> stack ) throws IOException {

      // Make sure there is actually a rule on the current row.
      String rule = reader.get(s_ruleName);
      if( rule == null || rule.trim().length() == 0 ) {
         // No rule.
         return;
      }

      // Get the parent element id.
      String elemId = stack.peek().getClassDef().getId();

      // Create class definition.
      String ref = reader.get(s_refName);
      ClassDef def = CSVReaderUtil.createClass(ref, ref, root);

      // Add stereotype to root.
      CSVReaderUtil.createStereotype(StereotypeNames.TO_BUSINESS_ELEMENT, def, root);

      // Add rule attribute
      Attribute attrib = CSVReaderUtil.addAttributeToClass(def);
      attrib.setName(s_ruleAttribName);
      attrib.getDefaultValueObject().setValue(rule);

      // Create association to element.
      ClassDef elemDef = singletonMap.get(StereotypeNames.SEMANTIC_ELEMENT + elemId);
      if( elemDef != null ) {
         CSVReaderUtil.createAssociation(elemDef, s_ruleName, def.getId());
      }

      // Create association to business element reference.
      String elemName = reader.get(s_busElemName);
      if( elemName != null && elemName.length() > 0 ) {
         // TODO- this is only temporary- there should not be
         // more than one value.
         String[] values = elemName.split(",");
         CSVReaderUtil.createAssociation(def, s_busElemRefName, values[0]);
      }
   }
}
