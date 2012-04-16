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

public class OwnedLiteralReader extends XMIReaderAbstract<Literal> {
   private static final String             s_nodeName        = "ownedLiteral";
   private static final String             s_attribNamespace = "http://schema.omg.org/spec/XMI/2.1";

   // Attribute names
   private static final String             s_idAtt           = "id";
   private static final String             s_nameAtt         = "name";

   // Child readers
   private static final OwnedCommentReader s_commentReader   = new OwnedCommentReader();

   @Override
   protected Literal createObject() {
      return new Literal();
   }

   @Override
   protected String getNodeName() {
      return s_nodeName;
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, Literal object ) {
      object.setId(reader.getAttributeValue(s_attribNamespace, s_idAtt));
      object.setName(reader.getAttributeValue(null, s_nameAtt));
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, Literal object ) throws XMLStreamException {

      if( s_commentReader.canRead(reader) ) {
         object.setComment(s_commentReader.read(reader));
      }
   }
}
