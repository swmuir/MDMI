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

public abstract class CSVDatatypeRowReader implements ICSVRowReader {
   public void readRow( CsvReader reader, RawRoot root, HashMap<String, ClassDef> singletonMap,
         LinkedList<ItemInfo> stack ) throws IOException {
      // Read remaining info, adding it the current type.
      ItemInfo info = stack.peekFirst();
      if( info == null ) {
         throw new IllegalStateException("Unable to parse.  No nesting info is available.");
      }
      readRowWithInfo(reader, root, singletonMap, info);
   }

   protected abstract void readRowWithInfo( CsvReader reader, RawRoot root, HashMap<String, ClassDef> singletonMap,
         ItemInfo info ) throws IOException;
}
