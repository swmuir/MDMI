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

import org.openhealthtools.mdht.mdmi.model.raw.*;

public class StereotypeInstanceReader extends XMIReaderAbstract<StereotypeInstance> implements
      IXMINodeReader<StereotypeInstance> {
   private static final String s_namespace   = "http://www.magicdraw.com/schemas/MDMI.xmi";

   // Attribute names
   private static final String s_classRefAtt = "base_Class";
   private static final String s_enumRefAtt  = "base_Enumeration";

   @Override
   protected StereotypeInstance createObject() {
      return new StereotypeInstance();
   }

   @Override
   public boolean canRead( XMLStreamReader reader ) {
      // We only care that the namespaces match. The node name
      // is actually the name of the stereotype.
      return namespacesMatch(reader);
   }

   @Override
   protected boolean atEndOfThisElement( XMLStreamReader reader ) {
      return namespacesMatch(reader) && reader.getEventType() == XMLStreamConstants.END_ELEMENT;
   }

   @Override
   protected String getNodeName() {
      // We don't utilize the node name.
      return "";
   }

   @Override
   protected String getNodeNamespace() {
      return s_namespace;
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, StereotypeInstance object ) {

      object.setName(reader.getLocalName());

      // Reference is either to a class or an enumeration.
      String value = reader.getAttributeValue(null, s_classRefAtt);
      if( value == null ) {
         value = reader.getAttributeValue(null, s_enumRefAtt);
      }
      object.setBaseRef(value);
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, StereotypeInstance object ) throws XMLStreamException {
   }
}
