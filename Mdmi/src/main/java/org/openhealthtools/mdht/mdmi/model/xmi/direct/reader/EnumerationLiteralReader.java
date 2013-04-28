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

import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.model.raw.*;
import org.openhealthtools.mdht.mdmi.model.validate.*;

public class EnumerationLiteralReader extends XMIReaderDirectAbstract<EnumerationLiteral> {
   public EnumerationLiteralReader( String nodeName ) {
      super(nodeName);
   }

   public EnumerationLiteralReader() {
      super(EnumerationLiteral.class.getSimpleName());
   }

   @Override
   protected String getObjectName( EnumerationLiteral object ) {
      return object.getName();
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root,
         Map<String, Object> objectMap, EnumerationLiteral object ) throws XMLStreamException {

      // No-op - no child elements
   }

   @Override
   protected EnumerationLiteral createObject( XMLStreamReader reader ) {
      return new EnumerationLiteral();
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, EnumerationLiteral object ) {

      object.setName(reader.getAttributeValue(null, EnumeratedDatatypeValidate.s_nameField));
      object.setCode(reader.getAttributeValue(null, EnumeratedDatatypeValidate.s_codeField));
      object.setDescription(reader.getAttributeValue(null, EnumeratedDatatypeValidate.s_descName));
   }
}
