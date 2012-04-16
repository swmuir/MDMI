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

import org.openhealthtools.mdht.mdmi.model.*;

public class EnumeratedTypeWriter extends MdmiDatatypeWriter<DTSEnumerated> {
   private static final String                   s_literalsName  = "literals";
   private static final EnumerationLiteralWriter s_literalWriter = new EnumerationLiteralWriter();

   @Override
   protected void writeAttributesAndChildren( DTSEnumerated object, XMLStreamWriter writer, Map<Object, String> refMap )
         throws XMLStreamException {

      // Parent only writes attribute.
      super.writeAttributesAndChildren(object, writer, refMap);

      // Write child elements.
      for( EnumerationLiteral literal : object.getLiterals() ) {
         s_literalWriter.writeElement(s_literalsName, literal, writer, refMap);
      }
   }
}
