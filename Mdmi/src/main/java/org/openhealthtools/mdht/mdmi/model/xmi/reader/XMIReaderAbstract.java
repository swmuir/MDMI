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
package org.openhealthtools.mdht.mdmi.model.xmi.reader;

import javax.xml.stream.*;

import org.openhealthtools.mdht.mdmi.model.xmi.*;

public abstract class XMIReaderAbstract<T> implements IXMINodeReader<T> {
   @Override
   public boolean canRead( XMLStreamReader reader ) {
      return namespacesMatch(reader) && getNodeName().equals(reader.getLocalName());
   }

   @Override
   public T read( XMLStreamReader reader ) throws XMLStreamException {
      // All readers assume that the reader is positioned at
      // a start element.
      if( reader.getEventType() != XMLStreamConstants.START_ELEMENT ) {
         throw new IllegalStateException("XML reader is not positioned at " + "the start of an element.");
      }

      // Make sure we can actual read this element.
      if( !canRead(reader) ) {
         throw new IllegalStateException("Unable to read XML stream.  " + "Is the XML Reader positioned correctly?");
      }

      // Create a new object that will be populated from the XML
      // and returned.
      T returnObject = createObject();

      // First, read attributes
      readAttributes(reader, returnObject);

      // Read all child elements.
      while( reader.hasNext() && !atEndOfThisElement(reader) ) {
         if( reader.next() == XMLStreamConstants.START_ELEMENT ) {
            readSingleChildElement(reader, returnObject);
         }
      }
      return returnObject;
   }

   // Overrides
   /**
    * Called when the reader is position at the element start
    */
   protected abstract void readAttributes( XMLStreamReader reader, T object );

   /**
    * Called when the reader is position at the element start. Called after readAttributes.
    */
   protected abstract void readSingleChildElement( XMLStreamReader reader, T object ) throws XMLStreamException;

   /**
    * Called after attributes and child elements have been processed.
    * 
    * @return The object created from reading XML.
    */
   protected abstract T createObject();

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
   protected boolean atEndOfThisElement( XMLStreamReader reader ) {
      return XMIReaderUtil.atEndOfThisElement(reader, getNodeName(), getNodeNamespace());
   }

   protected boolean namespacesMatch( XMLStreamReader reader ) {
      return XMIReaderUtil.namespacesMatch(reader, getNodeNamespace());
   }
}
