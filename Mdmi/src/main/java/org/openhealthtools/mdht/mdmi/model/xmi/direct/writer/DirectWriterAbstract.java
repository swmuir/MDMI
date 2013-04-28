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

public abstract class DirectWriterAbstract<T> implements IDirectWriter<T> {
   public void writeElement( T object, XMLStreamWriter writer, Map<Object, String> refMap ) throws XMLStreamException {
      writer.writeStartElement(XMIDirectConstants.MDMI_NAMESPACE, getRootElementName());
      writeXMIId(writer, object, refMap);
      writeAttributesAndChildren(object, writer, refMap);
      writer.writeEndElement();
   }

   public void writeElement( String elementName, T object, XMLStreamWriter writer, Map<Object, String> refMap )
         throws XMLStreamException {
      writer.writeStartElement(elementName);
      writeXMIId(writer, object, refMap);
      writeAttributesAndChildren(object, writer, refMap);
      writer.writeEndElement();
   }

   private void writeXMIId( XMLStreamWriter writer, T object, Map<Object, String> refMap ) throws XMLStreamException {
      // See if there is already an id for this object.
      String refId = refMap.get(object);
      if( refId == null ) {
         refId = UUID.randomUUID().toString();
         refMap.put(object, refId);
      }

      // Write xmi id attribute.
      WriterUtil.writeAttribute(writer, XMIDirectConstants.XMI_ID_ATTRIB, refId, XMIDirectConstants.XMI_NAMESPACE);
   }

   protected abstract String getRootElementName();

   protected abstract void writeAttributesAndChildren( T object, XMLStreamWriter writer, Map<Object, String> refMap )
         throws XMLStreamException;
}
