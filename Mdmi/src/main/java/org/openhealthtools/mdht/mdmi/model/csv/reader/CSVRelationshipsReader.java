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

import java.util.*;

import org.openhealthtools.mdht.mdmi.model.csv.reader.row.*;

public class CSVRelationshipsReader extends CSVReaderAbstract {
   private static final String[]        s_fileNameInd = { "relationships" };
   private static final ICSVRowReader[] s_rowReader   = { new CSVRelationshipRowReader() };

   @Override
   protected String[] getFileNameIndicators() {
      return s_fileNameInd;
   }

   @Override
   protected ICSVRowReader[] getRowReaders( CsvReader reader, LinkedList<ItemInfo> stack ) {
      return s_rowReader;
   }
}
