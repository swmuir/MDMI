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
package org.openhealthtools.mdht.mdmi.model;

import java.io.*;
import java.util.*;

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.model.csv.*;
import org.openhealthtools.mdht.mdmi.model.validate.*;

/**
 * 
 */
public class MapBuilderCSV extends MapBuilderAbstract {
   /**
    * Builds an object graph representing an MDMI map.
    * 
    * @param filePath Path to directory containing CSV files which define an MDMI map.
    * @return
    */
   public static List<MessageGroup> build( String filePath, ModelValidationResults valResults ) {
      try {
         return buildFromRawModel(CSVParser.parse(filePath), valResults);
      }
      catch( FileNotFoundException exc ) {
         throw new MdmiException(exc, "MapBuilder: folder {0} not found!", filePath);
      }
      catch( IOException exc ) {
         throw new MdmiException(exc, "MapBuilder: file {0} is not a valid CSV file folder!", filePath);
      }
   }

   /**
    * Builds an object graph representing an MDMI map.
    * 
    * @param dir Directory containing CSV files which define an MDMI map.
    * @param valResults
    * @return
    */
   public static List<MessageGroup> build( File dir, ModelValidationResults valResults ) {
      try {
         return buildFromRawModel(CSVParser.parse(dir.getAbsolutePath()), valResults);
      }
      catch( FileNotFoundException exc ) {
         throw new MdmiException(exc, "MapBuilder: folder {0} not found!", dir.getAbsolutePath());
      }
      catch( IOException exc ) {
         throw new MdmiException(exc, "MapBuilder: file {0} is not a valid CSV file folder!", dir.getAbsolutePath());
      }
   }
} // MapBuilderCSV
