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

public class CSVDatatypeStackMgr implements ICSVRowReader {
   private static final String s_name        = "name";
   private static final String s_description = "description";
   private static final String s_typeName    = "Type";

   @Override
   public void readRow( CsvReader reader, RawRoot root, HashMap<String, ClassDef> singletonMap,
         LinkedList<ItemInfo> stack ) throws IOException {
      // If there is a new type name, then we have a new definition.
      String value = getName(reader);
      if( value != null && value.length() > 0 ) {
         ItemInfo newInfo = createNewType(reader, root);
         // Replace the first element in the list.
         stack.pollFirst();
         stack.offer(newInfo);
      }

      // Make sure there is an item on the stack
      ItemInfo info = stack.peekFirst();
      if( info == null ) {
         throw new IllegalStateException("Unable to parse.  No datatype nesting info is avaiable");
      }
   }

   private ItemInfo createNewType( CsvReader reader, RawRoot root ) throws IOException {
      ClassDef def = new ClassDef();
      def.setId(getName(reader));
      def.setName(getName(reader));
      root.getModel().addClassDef(def);

      String desc = reader.get(s_description);
      if( desc != null && desc.length() > 0 ) {
         Comment comment = new Comment();
         comment.setValue(desc);
         def.setComment(comment);
      }

      StereotypeInstance instance = new StereotypeInstance();
      instance.setName(getStereotypeName(reader));
      instance.setBaseRef(def.getId());
      root.addStereotypeInstance(instance);

      return new ItemInfo(def, instance.getName());
   }

   protected String getName( CsvReader reader ) throws IOException {
      return reader.get(s_name);
   }

   protected String getStereotypeName( CsvReader reader ) throws IOException {
      return reader.get(s_typeName);
   }
}
