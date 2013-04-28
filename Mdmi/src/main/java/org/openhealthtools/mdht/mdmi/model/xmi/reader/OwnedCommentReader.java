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

public class OwnedCommentReader extends XMIReaderAbstract<Comment> {
   private static final String s_elemName = "ownedComment";
   private static final String s_bodyAtt  = "body";

   @Override
   protected Comment createObject() {
      return new Comment();
   }

   @Override
   protected String getNodeName() {
      return s_elemName;
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, Comment object ) {
      object.setValue(reader.getAttributeValue(null, s_bodyAtt));
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, Comment object ) throws XMLStreamException {
      // Don't care about child elements.
   }
}
