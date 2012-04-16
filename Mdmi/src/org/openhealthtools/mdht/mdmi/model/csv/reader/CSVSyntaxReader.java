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

import org.openhealthtools.mdht.mdmi.model.csv.reader.row.*;

public class CSVSyntaxReader extends CSVReaderAbstract {
   private static final String[]                         s_fileInd = { "syntax" };
   private static final HashMap<String, ICSVRowReader[]> s_readers = new HashMap<String, ICSVRowReader[]>();

   static {
      s_readers
            .put(CSVSyntaxStackMgr.s_bagName, new ICSVRowReader[] { new CSVSyntaxStackMgr(), new CSVBagRowReader() });
      s_readers.put(CSVSyntaxStackMgr.s_choiceName, new ICSVRowReader[] { new CSVSyntaxStackMgr(),
         new CSVChoiceRowReader() });
      s_readers.put(CSVSyntaxStackMgr.s_leafName,
            new ICSVRowReader[] { new CSVSyntaxStackMgr(), new CSVLeafRowReader() });
   }

   @Override
   protected String[] getFileNameIndicators() {
      return s_fileInd;
   }

   @Override
   protected ICSVRowReader[] getRowReaders( CsvReader reader, LinkedList<ItemInfo> stack ) throws IOException {
      return s_readers.get(reader.get(CSVSyntaxStackMgr.s_typeName));
   }
}
