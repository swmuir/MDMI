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
import java.util.*;

import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.model.builder.*;
import org.openhealthtools.mdht.mdmi.model.csv.reader.row.*;
import org.openhealthtools.mdht.mdmi.model.raw.*;

/**
 * 
 *
 */
public class CSVDatatypeReader extends CSVReaderAbstract {
   private static final String[]                         s_fileNameInd = { "data", "types" };
   private static final String                           s_typeName    = "Type";
   private static final HashMap<String, ICSVRowReader[]> s_readers     = new HashMap<String, ICSVRowReader[]>();
   
   static {
      s_readers.put(StereotypeNames.CHOICE_DATA_TYPE, new ICSVRowReader[] { new CSVDatatypeStackMgr(),
         new CSVComplexTypeReader() });
      s_readers.put(StereotypeNames.STRUCT_DATA_TYPE, new ICSVRowReader[] { new CSVDatatypeStackMgr(),
         new CSVComplexTypeReader() });
      s_readers.put(StereotypeNames.ENUM_DATA_TYPE, new ICSVRowReader[] { new CSVDatatypeStackMgr(),
         new CSVEnumDatatypeReader() });
      s_readers.put(StereotypeNames.DERIVED_DATA_TYPE, new ICSVRowReader[] { new CSVDatatypeStackMgr(),
         new CSVDerivedTypeReader() });
      s_readers.put(StereotypeNames.EXTERNAL_DATA_TYPE, new ICSVRowReader[] { new CSVDatatypeStackMgr(),
         new CSVExternalTypeReader() });
   }

   @Override
   protected void preProcess( RawRoot root, HashMap<String, ClassDef> singletonMap ) {
      addAllPrimitiveTypes(root);
   }

   @Override
   protected String[] getFileNameIndicators() {
      return s_fileNameInd;
   }

   @Override
   protected ICSVRowReader[] getRowReaders( CsvReader reader, LinkedList<ItemInfo> stack ) throws IOException {
      String stereotype = reader.get(s_typeName);
      if( stereotype == null || stereotype.length() == 0 ) {
         // We can't find the reader in the current row- which
         // means we use the one for the item at the top of
         // the stack.
         ItemInfo info = (ItemInfo)stack.peek();
         if( info == null ) {
            throw new IllegalStateException("Can't get row reader for datatype.  " + "Expected a stack element.");
         }
         stereotype = info.getStereotype();
      }
      ICSVRowReader[] readers = s_readers.get(stereotype);
      if( readers == null ) {
         throw new IllegalArgumentException("Unable to find reader for " + "data type stereotype '" + stereotype + "'.");
      }
      return readers;
   }

   private void addAllPrimitiveTypes( RawRoot root ) {
      for( DTSPrimitive primitive : DTSPrimitive.ALL_PRIMITIVES ) {
         addPrimitiveType(primitive.getTypeName(), root);
      }
   }

   private void addPrimitiveType( String typeName, RawRoot root ) {
      ClassDef def = new ClassDef();
      def.setId(typeName);
      def.setName(typeName);
      root.getModel().addClassDef(def);

      StereotypeInstance instance = new StereotypeInstance();
      instance.setName(StereotypeNames.PRIM_DATA_TYPE);
      instance.setBaseRef(def.getId());
      root.addStereotypeInstance(instance);
   }
}
