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

import org.openhealthtools.mdht.mdmi.model.csv.reader.*;
import org.openhealthtools.mdht.mdmi.model.csv.reader.CSVReaderAbstract.*;
import org.openhealthtools.mdht.mdmi.model.raw.*;

public class CSVComplexTypeReader extends CSVDatatypeRowReader {
   private static final String s_fieldQualifier = "Field:";
   private static final String s_fieldName      = "name";
   private static final String s_fieldType      = "datatype";
   private static final String s_description    = "description";
   private static final String s_minOccurs      = "minOccurs";
   private static final String s_maxOccurs      = "maxOccurs";

   @Override
   protected void readRowWithInfo( CsvReader reader, RawRoot root, HashMap<String, ClassDef> singletonMap, ItemInfo info )
         throws IOException {

      String name = reader.get(s_fieldQualifier + s_fieldName);
      String type = reader.get(s_fieldQualifier + s_fieldType);
      if( name != null && name.length() > 0 && type != null && type.length() > 0 ) {
         Attribute attrib = CSVReaderUtil.addAttributeToClass(info.getClassDef());
         attrib.setName(name);
         attrib.setType(type);

         String value = reader.get(s_fieldQualifier + s_description);
         if( value != null && value.length() > 0 ) {
            Comment comment = new Comment();
            comment.setValue(value);
            attrib.setComment(comment);
         }

         value = reader.get(s_fieldQualifier + s_minOccurs);
         if( value != null && value.length() > 0 ) {
            LimitValue limit = new LimitValue();
            if( value.equals("*") ) {
               limit.setValue(0);
            }
            else {
               try {
                  limit.setValue(Integer.parseInt(value));
               }
               catch( NumberFormatException exc ) {
                  throw new IllegalArgumentException("Unable to parse minOccurs of '" + value + "'.", exc);
               }
            }

            attrib.setLowerLimit(limit);
         }

         value = reader.get(s_fieldQualifier + s_maxOccurs);
         if( value != null && value.length() > 0 ) {
            LimitValue limit = new LimitValue();
            if( value.equals("*") ) {
               limit.setValue(Integer.MAX_VALUE);
            }
            else {
               try {
                  limit.setValue(Integer.parseInt(value));
               }
               catch( NumberFormatException exc ) {
                  throw new IllegalArgumentException("Unable to parse maxOccurs of '" + value + "'.", exc);
               }
            }

            attrib.setUpperLimit(limit);
         }
      }
   }

}
