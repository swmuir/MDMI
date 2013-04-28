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
package org.openhealthtools.mdht.mdmi.model.xmi;

import java.io.*;

import javax.xml.stream.*;

import org.openhealthtools.mdht.mdmi.model.raw.*;
import org.openhealthtools.mdht.mdmi.model.xmi.reader.*;

/**
 * Parses an XMI document into an object model that directly reflects the classes, attributes, associations and
 * stereotypes defined in the XMI document. This object model can be used to create an application specific model (e.g.
 * an object graph representing an MDMI map).
 * 
 * Only supports v2.1 XMI docs exported by MagicDraw. If we need to support additional versions/formats, then we will
 * need to enhance this code.
 */
public class XMIParser {
   private static final XMIRootReader m_rootReader = new XMIRootReader();

   /**
    * Parses XMI document at specified location.
    * 
    * @param filePath Path to XMI file.
    * @return
    * @throws FileNotFoundException
    * @throws XMLStreamException
    */
   public static RawRoot parse( String filePath ) throws FileNotFoundException, XMLStreamException {
      return parse(new File(filePath));
   }

   /**
    * Parses XMI document specified by File object.
    * 
    * @param file File object representing the file to be parsed.
    * @return
    * @throws FileNotFoundException
    * @throws XMLStreamException
    */
   public static RawRoot parse( File file ) throws FileNotFoundException, XMLStreamException {
      FileInputStream fileStream = new FileInputStream(file);
      BufferedInputStream bufStream = new BufferedInputStream(fileStream);

      try {
         return parse(bufStream);
      }
      finally {
         try {
            bufStream.close();
         }
         catch( IOException e ) {
         }
      }
   }

   /**
    * Parses XMI document accessible via the specified stream.
    * 
    * @param stream Input stream which streams the XMI document.
    * @return
    * @throws XMLStreamException
    */
   public static RawRoot parse( InputStream stream ) throws XMLStreamException {
      XMLInputFactory factory = XMLInputFactory.newInstance();
      XMLStreamReader reader = factory.createXMLStreamReader(stream);
      try {
         return parse(reader);
      }
      finally {
         try {
            reader.close();
         }
         catch( XMLStreamException exc ) {
         }
      }
   }

   private static RawRoot parse( XMLStreamReader xmlReader ) throws XMLStreamException {
      // Get to the first start element event.
      while( xmlReader.hasNext() ) {
         if( xmlReader.next() == XMLStreamConstants.START_ELEMENT ) {
            return m_rootReader.read(xmlReader);
         }
      }
      return null;
   }
}
