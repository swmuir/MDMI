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

public class UpperValueReader extends XMIReaderAbstract<LimitValue> {
   private static final String s_elemName = "upperValue";
   private static final String s_valueAtt = "value";

   @Override
   protected LimitValue createObject() {
      return new LimitValue();
   }

   @Override
   protected String getNodeName() {
      return s_elemName;
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, LimitValue object ) {
      String value = reader.getAttributeValue(null, s_valueAtt);
      if( value == null || value.length() == 0 ) {
         object.setValue(0);
      }
      else if( value.equals("*") ) {
         object.setValue(Integer.MAX_VALUE);
      }
      else {
         try {
            object.setValue(Integer.parseInt(value));
         }
         catch( NumberFormatException exc ) {
            throw new IllegalArgumentException("Unable to parse upper limit of '" + value + "'.", exc);
         }
      }
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, LimitValue object ) throws XMLStreamException {
   }
}
