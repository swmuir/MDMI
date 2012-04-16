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

import javax.xml.stream.*;

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.model.validate.*;
import org.openhealthtools.mdht.mdmi.model.xmi.*;

/**
 * Builds an object graph representing an MDMI map by reading map information from an XMI document.
 * 
 * Only supports v2.1 XMI docs exported by MagicDraw. If we need to support additional versions/formats, then we will
 * need to enhance this code.
 * 
 * 
 */
public class MapBuilder extends MapBuilderAbstract {
   /**
    * Builds an object graph representing an MDMI map.
    * 
    * @param filePath Path to XMI file containing the map definition.
    * @return
    */
   public static List<MessageGroup> build( String filePath, ModelValidationResults valResults ) {
      try {
         return buildFromRawModel(XMIParser.parse(filePath), valResults);
      }
      catch( FileNotFoundException exc ) {
         throw new MdmiException(exc, "MapBuilder: file {0} not found!", filePath);
      }
      catch( XMLStreamException exc ) {
         throw new MdmiException(exc, "MapBuilder: file {0} is not a valid XML file!", filePath);
      }
   }

   /**
    * 
    * Builds an object graph representing an MDMI map.
    * 
    * @param filePath XMI file containing the map definition.
    * @return
    */
   public static List<MessageGroup> build( File file, ModelValidationResults valResults ) {
      try {
         return buildFromRawModel(XMIParser.parse(file), valResults);
      }
      catch( FileNotFoundException exc ) {
         throw new MdmiException(exc, "MapBuilder: file {0} not found!", file.getAbsolutePath());
      }
      catch( XMLStreamException exc ) {
         throw new MdmiException(exc, "MapBuilder: file {0} is not a valid XML file!", file.getAbsolutePath());
      }
   }

   /**
    * Builds an object graph representing an MDMI map.
    * 
    * @param inputStream XMI document stream which contains map definition.
    * @return
    */
   public static List<MessageGroup> build( InputStream inputStream, ModelValidationResults valResults ) {
      try {
         return buildFromRawModel(XMIParser.parse(inputStream), valResults);
      }
      catch( XMLStreamException exc ) {
         throw new MdmiException(exc, "MapBuilder: file XML parser error!");
      }
   }
}
