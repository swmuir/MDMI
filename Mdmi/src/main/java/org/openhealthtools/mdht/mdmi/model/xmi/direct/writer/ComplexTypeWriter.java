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

public class ComplexTypeWriter extends MdmiDatatypeWriter<DTComplex> {
   private static final String      s_fieldsName  = "fields";
   private static final FieldWriter s_fieldWriter = new FieldWriter();

   @Override
   protected void writeAttributesAndChildren( DTComplex object, XMLStreamWriter writer, Map<Object, String> refMap )
         throws XMLStreamException {

      // Parent only writes attributes.
      super.writeAttributesAndChildren(object, writer, refMap);

      for( Field field : object.getFields() ) {
         s_fieldWriter.writeElement(s_fieldsName, field, writer, refMap);
      }
   }
}
