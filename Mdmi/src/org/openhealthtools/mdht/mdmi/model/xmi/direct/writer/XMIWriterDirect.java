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
package org.openhealthtools.mdht.mdmi.model.xmi.direct.writer;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import javax.xml.stream.*;

import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.model.xmi.direct.*;

public class XMIWriterDirect {
   private static final DocumentRootWriter m_rootWriter = new DocumentRootWriter();
   private static final MessageGroupWriter m_grpWriter  = new MessageGroupWriter();

   public static void write( String filePath, List<MessageGroup> groups ) throws FileNotFoundException,
         XMLStreamException {
      write(new File(filePath), groups);
   }

   public static void write( File file, List<MessageGroup> groups ) throws FileNotFoundException, XMLStreamException {
      FileOutputStream fileStream = new FileOutputStream(file);
      BufferedOutputStream bufStream = new BufferedOutputStream(fileStream);
      try {
         write(bufStream, groups);
      }
      finally {
         try {
            bufStream.close();
         }
         catch( IOException e ) {
         }
      }
   }

   public static void write( OutputStream stream, List<MessageGroup> groups ) throws XMLStreamException {
      XMLStreamWriter writer = XMLOutputFactory.newInstance()
            .createXMLStreamWriter(stream, XMIDirectConstants.ENCODING);
      // Create a proxy that will decorate the output with line feeds and indentation.
      XMLStreamIndentProxy proxy = new XMLStreamIndentProxy(writer);
      XMLStreamWriter indentWriter = (XMLStreamWriter)Proxy.newProxyInstance(XMLStreamWriter.class.getClassLoader(),
            new Class[] { XMLStreamWriter.class }, proxy);
      write(indentWriter, groups);
   }

   private static void write( XMLStreamWriter writer, List<MessageGroup> groups ) throws XMLStreamException {
      m_rootWriter.writeDocumentOpen(writer);
      for( MessageGroup grp : groups ) {
         HashMap<Object, String> refMap = new HashMap<Object, String>();
         m_grpWriter.writeElement(grp, writer, refMap);
      }
      m_rootWriter.writeDocumentClose(writer);
   }
}
