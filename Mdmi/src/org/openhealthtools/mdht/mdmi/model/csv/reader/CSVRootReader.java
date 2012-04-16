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

public class CSVRootReader extends CSVReaderAbstract {
   private static final String[]        s_groupNameInd = { "group" };
   private static final ICSVRowReader[] s_rowReaders   = { new CSVMessageGroupReader(), new CSVMessageModelReader(),
         new CSVSyntaxModelReader(), new CSVDictionaryReader() };

   @Override
   protected String[] getFileNameIndicators() {
      return s_groupNameInd;
   }

   @Override
   protected ICSVRowReader[] getRowReaders( CsvReader reader, LinkedList<ItemInfo> stack ) throws IOException {
      return s_rowReaders;
   }
}
