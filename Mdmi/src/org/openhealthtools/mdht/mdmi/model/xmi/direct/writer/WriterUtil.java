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

import java.util.*;

import javax.xml.stream.*;

import org.openhealthtools.mdht.mdmi.model.xmi.direct.*;

public class WriterUtil {
   public static void writeAttribute( XMLStreamWriter writer, String name, Object value ) throws XMLStreamException {
      if( value != null ) {
         writer.writeAttribute(name, value.toString());
      }
   }

   public static void writeAttribute( XMLStreamWriter writer, String name, Object value, String namespace )
         throws XMLStreamException {
      if( value != null ) {
         writer.writeAttribute(namespace, name, value.toString());
      }
   }

   public static void writeRefElement( XMLStreamWriter writer, String elementName, Object refObject,
         Map<Object, String> refMap ) throws XMLStreamException {
      writer.writeStartElement(elementName);
      // Determine ref id
      String refId = refMap.get(refObject);
      if( refId == null ) {
         refId = UUID.randomUUID().toString();
         refMap.put(refObject, refId);
      }

      writer.writeAttribute(XMIDirectConstants.XMI_NAMESPACE, XMIDirectConstants.XMI_ID_REF_ATTRIB, refId);

      writer.writeEndElement();
   }
}
