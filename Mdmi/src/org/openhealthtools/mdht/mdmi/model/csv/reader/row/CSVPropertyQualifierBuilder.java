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

public class CSVPropertyQualifierBuilder implements ICSVRowReader {
   private static final String s_propName = "propertyQualifier";

   @Override
   public void readRow( CsvReader reader, RawRoot root, HashMap<String, ClassDef> singletonMap,
         LinkedList<ItemInfo> stack ) throws IOException {

      // See if there is a property qualifier for this row.
      String prop = reader.get(s_propName);
      if( prop == null || prop.length() == 0 ) {
         return;
      }

      // We have a property qualifier. First, make sure it's
      // on the qualifier definition.
      ClassDef def = getPropertyQualifierDef(root, singletonMap);
      Literal literal = def.getLiteral(prop);
      if( literal == null ) {
         // Literal does not yet exist on the parent enum. We
         // need to add it now.
         literal = new Literal();
         literal.setId(prop);
         literal.setName(prop);
         def.addLiteral(literal);
      }
   }

   private ClassDef getPropertyQualifierDef( RawRoot root, HashMap<String, ClassDef> singletonMap ) {
      ClassDef def = singletonMap.get(StereotypeNames.ME_PROP_QUALIFIER);
      if( def == null ) {
         // Create one now. There can only be one property
         // qualifier when parsing from CSV.
         def = CSVReaderUtil.createClass(StereotypeNames.ME_PROP_QUALIFIER, StereotypeNames.ME_PROP_QUALIFIER, root);
         // Add stereotype to root.
         CSVReaderUtil.createStereotype(StereotypeNames.ME_PROP_QUALIFIER, def, root);
         singletonMap.put(StereotypeNames.ME_PROP_QUALIFIER, def);
      }
      return def;
   }
}
