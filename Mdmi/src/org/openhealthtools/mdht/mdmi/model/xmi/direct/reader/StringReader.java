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

import java.util.*;

import javax.xml.stream.*;

import org.openhealthtools.mdht.mdmi.model.raw.*;

public class StringReader extends XMIReaderDirectAbstract<String> {
   public StringReader(String nodeName) {
      super(nodeName);
   }

   @Override
   protected String getObjectName(String object) {
      return String.class.getSimpleName();
   }

   @Override
   protected void readSingleChildElement(XMLStreamReader reader,
         ParsingInfo parsingInfo, RawRoot root, Map<String, Object> objectMap,
         String object) throws XMLStreamException {

      // No-op - no child elements
   }

   @Override
   protected String createObject(XMLStreamReader reader) throws XMLStreamException {
      return reader.getElementText();
   }

   @Override
   protected void readAttributes(XMLStreamReader reader, String object) {
   }
}
