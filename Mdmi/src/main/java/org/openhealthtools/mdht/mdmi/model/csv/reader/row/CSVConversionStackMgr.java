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

import org.openhealthtools.mdht.mdmi.model.csv.reader.*;

import java.io.*;
import java.util.*;

import org.openhealthtools.mdht.mdmi.model.builder.*;
import org.openhealthtools.mdht.mdmi.model.csv.reader.CSVReaderAbstract.*;
import org.openhealthtools.mdht.mdmi.model.raw.*;

public class CSVConversionStackMgr implements ICSVRowReader {
   public static final String s_elementName = "semanticElement";

   @Override
   public void readRow( CsvReader reader, RawRoot root, HashMap<String, ClassDef> singletonMap,
         LinkedList<ItemInfo> stack ) throws IOException {
      // Get the element in question
      String elemName = reader.get(s_elementName);
      if( elemName != null && elemName.length() > 0 ) {
         stack.poll();
         stack.offer(getNewParent(reader, root, singletonMap));
      }
   }

   private ItemInfo getNewParent( CsvReader reader, RawRoot root, HashMap<String, ClassDef> singletonMap )
         throws IOException {
      String elemId = reader.get(s_elementName);
      ClassDef elemDef = singletonMap.get(StereotypeNames.SEMANTIC_ELEMENT + elemId);
      if( elemDef == null ) {
         throw new IllegalStateException("Invalid element reference '" + elemId + "' in conversion.");
      }
      return new ItemInfo(elemDef, StereotypeNames.SEMANTIC_ELEMENT);
   }
}
