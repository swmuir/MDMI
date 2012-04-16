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
import org.openhealthtools.mdht.mdmi.model.xmi.*;

public class FieldReader extends XMIReaderDirectAbstract<Field> {
   public FieldReader( String nodeName ) {
      super(nodeName);
   }

   public FieldReader() {
      super(Field.class.getSimpleName());
   }

   @Override
   protected String getObjectName( Field object ) {
      return object.getName();
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root,
         Map<String, Object> objectMap, Field object ) throws XMLStreamException {
   }

   @Override
   protected Field createObject( XMLStreamReader reader ) {
      return new Field();
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, Field object ) {
      object.setName(reader.getAttributeValue(null, ComplexDatatypeValidate.s_nameField));
      object.setDescription(reader.getAttributeValue(null, ComplexDatatypeValidate.s_descName));

      Integer value = XMIReaderUtil.convertToInteger(reader.getAttributeValue(null, ComplexDatatypeValidate.s_minName));
      if( value != null ) {
         object.setMinOccurs(value);
      }

      value = XMIReaderUtil.convertToInteger(reader.getAttributeValue(null, ComplexDatatypeValidate.s_maxName));
      if( value != null ) {
         object.setMaxOccurs(value);
      }
   }
}
