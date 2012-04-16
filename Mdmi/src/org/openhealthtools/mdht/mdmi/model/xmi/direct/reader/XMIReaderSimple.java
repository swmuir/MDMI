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
package org.openhealthtools.mdht.mdmi.model.xmi.direct.reader;

import javax.xml.stream.*;

import org.openhealthtools.mdht.mdmi.model.xmi.*;

public abstract class XMIReaderSimple<T> implements IXMIReaderSimple {
   @Override
   public boolean canRead( XMLStreamReader reader ) {
      return namespacesMatch(reader) && getNodeName().equals(reader.getLocalName());
   }

   /**
    * Called when the reader is position at the element start
    */
   protected abstract void readAttributes( XMLStreamReader reader, T object );

   /**
    * Called after attributes and child elements have been processed.
    * 
    * @return The object created from reading XML.
    */
   protected abstract T createObject( XMLStreamReader reader ) throws XMLStreamException;

   /**
    * 
    * @return The XML element name of the node to be read.
    */
   protected abstract String getNodeName();

   /**
    * 
    * @return The namespace URI for the node to be read.
    */
   protected String getNodeNamespace() {
      // No namespace by default.
      return null;
   }

   // Other
   protected boolean atEndOfThisElement( XMLStreamReader reader, ParsingInfo info, int startDepth ) {
      return XMIReaderUtil.atEndOfThisElement(reader, getNodeName(), getNodeNamespace())
            && startDepth > info.getDepth();
   }

   protected boolean namespacesMatch( XMLStreamReader reader ) {
      return XMIReaderUtil.namespacesMatch(reader, getNodeNamespace());
   }

   protected void validatePosition( XMLStreamReader reader ) {
      if( reader.getEventType() != XMLStreamConstants.START_ELEMENT ) {
         throw new IllegalStateException("XMI reader is not positioned at " + "the start of an element.");
      }
      if( !canRead(reader) ) {
         throw new IllegalStateException("Unable to read XMI direct stream.  "
               + "Is the XML Reader positioned correctly?");
      }
   }

   protected boolean advanceToNextChildElement( XMLStreamReader reader, ParsingInfo info, int startDepth )
         throws XMLStreamException {

      while( reader.hasNext() && !atEndOfThisElement(reader, info, startDepth) ) {
         if( reader.next() == XMLStreamConstants.START_ELEMENT ) {
            return true;
         }
      }
      return false;
   }
}
