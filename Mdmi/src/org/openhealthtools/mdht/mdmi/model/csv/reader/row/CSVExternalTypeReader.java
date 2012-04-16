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

public class CSVExternalTypeReader extends CSVDatatypeRowReader {
   private static final String s_externalQualifier = "External:";
   private static final String s_specName          = "typeSpecification";

   @Override
   protected void readRowWithInfo( CsvReader reader, RawRoot root, HashMap<String, ClassDef> singletonMap, ItemInfo info )
         throws IOException {
      Attribute attrib = CSVReaderUtil.addAttributeToClass(info.getClassDef());
      attrib.setName(s_specName);
      attrib.getDefaultValueObject().setValue(reader.get(s_externalQualifier + s_specName));
   }
}
