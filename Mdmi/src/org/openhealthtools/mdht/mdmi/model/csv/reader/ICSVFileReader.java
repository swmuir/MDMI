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

import org.openhealthtools.mdht.mdmi.model.raw.*;

public interface ICSVFileReader {
   public boolean canReadFile( String fileName );

   public void readFile( String filePath, RawRoot root, HashMap<String, ClassDef> singletonMap )
         throws FileNotFoundException, IOException;
}
