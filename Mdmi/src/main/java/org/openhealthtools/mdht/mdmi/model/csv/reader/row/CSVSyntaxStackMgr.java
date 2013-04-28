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

public class CSVSyntaxStackMgr implements ICSVRowReader {
   public static final String                   s_typeName       = "type";
   public static final String                   s_bagName        = "bag";
   public static final String                   s_choiceName     = "choice";
   public static final String                   s_leafName       = "leaf";
   private static final String                  s_nodesName      = "nodes";
   private static final String                  s_refName        = "Ref";
   private static final String                  s_rootName       = "root";
   private static final int                     s_firstNodeIndex = 1;
   private static final HashMap<String, String> s_typeMap        = new HashMap<String, String>();
   
   static {
      s_typeMap.put(s_bagName, StereotypeNames.BAG);
      s_typeMap.put(s_choiceName, StereotypeNames.CHOICE);
      s_typeMap.put(s_leafName, StereotypeNames.LEAF_SYNTAX_TRANSLATOR);
   }

   @Override
   public void readRow( CsvReader reader, RawRoot root, HashMap<String, ClassDef> singletonMap,
         LinkedList<ItemInfo> stack ) throws IOException {

      // Use this as sanity check.
      int firstFieldIndex = getFirstFieldColumnIndex(reader);

      // Get index of node name in the current row.
      // Make sure it's valid.
      int nameIndex = getCurrentNodeIndex(reader);
      if( nameIndex >= firstFieldIndex ) {
         throw new IllegalStateException("Node name not found before reaching a field column.");
      }

      // Create new item for the current row.
      ItemInfo curItemInfo = createNewType(reader, root, nameIndex);

      // Get current parent info.
      ItemInfo parentInfo = stack.peek();
      if( parentInfo == null ) {
         // This can only happen if we are at the root.
         if( nameIndex != s_firstNodeIndex ) {
            throw new IllegalStateException("Unable to parse.  No syntax nesting info is avaiable");
         }

         // Add association to syntax model.
         ClassDef syntaxModelDef = singletonMap.get(StereotypeNames.MESSAGE_SYNTAX_MODEL);
         if( syntaxModelDef == null ) {
            throw new IllegalStateException("Unable to find message syntax model while " + "parsing syntax nodes.");
         }

         CSVReaderUtil.createAssociation(syntaxModelDef, s_rootName, curItemInfo.getClassDef().getId());
      }
      else {
         // We are not at root. Pop back up to appropriate parent level.
         while( parentInfo != null && parentInfo.getIndex() >= nameIndex ) {
            stack.pop();
            parentInfo = stack.peek();
         }

         // Make sure parent is not a leaf.
         if( StereotypeNames.LEAF_SYNTAX_TRANSLATOR.equals(parentInfo.getStereotype()) ) {
            throw new IllegalStateException("Leaf node '" + parentInfo.getClassDef().getName() + "' has a child node.");
         }

         if( parentInfo != null ) {
            // Add association to parent.
            CSVReaderUtil.createAssociation(parentInfo.getClassDef(), s_nodesName, curItemInfo.getClassDef().getId());
         }
      }

      // Put new item on the stack.
      stack.push(curItemInfo);
   }

   private ItemInfo createNewType( CsvReader reader, RawRoot root, int nameIndex ) throws IOException {
      // Create class definition.
      ClassDef def = new ClassDef();
      def.setId(reader.get(s_refName));
      def.setName(reader.get(nameIndex));
      root.getModel().addClassDef(def);

      // Add stereotype.
      StereotypeInstance instance = new StereotypeInstance();
      instance.setName(getStereotypeName(reader));
      instance.setBaseRef(def.getId());
      root.addStereotypeInstance(instance);
      return new ItemInfo(def, instance.getName(), nameIndex);
   }

   private int getCurrentNodeIndex( CsvReader reader ) throws IOException {
      for( int index = s_firstNodeIndex; index < reader.getColumnCount(); index++ ) {
         String curItem = reader.get(index);
         if( curItem != null && curItem.length() > 0 ) {
            return index;
         }
      }
      return Integer.MAX_VALUE;
   }

   private int getFirstFieldColumnIndex( CsvReader reader ) throws IOException {
      for( int index = s_firstNodeIndex + 1; index < reader.getHeaderCount(); index++ ) {
         String header = reader.getHeader(index);
         if( header != null && header.length() > 0 ) {
            return index;
         }
      }
      throw new IllegalStateException("Unable to find a field header in syntax file.");
   }

   private String getStereotypeName( CsvReader reader ) throws IOException {
      return s_typeMap.get(reader.get(s_typeName));
   }
}
