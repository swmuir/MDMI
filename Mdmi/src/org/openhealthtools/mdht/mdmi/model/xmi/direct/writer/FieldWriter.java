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
import org.openhealthtools.mdht.mdmi.model.validate.*;

public class FieldWriter extends DirectWriterAbstract<Field> {
   @Override
   protected String getRootElementName() {
      return Field.class.getSimpleName();
   }

   @Override
   protected void writeAttributesAndChildren( Field object, XMLStreamWriter writer, Map<Object, String> refMap )
         throws XMLStreamException {

      // Write attributes.
      WriterUtil.writeAttribute(writer, ComplexDatatypeValidate.s_nameField, object.getName());
      WriterUtil.writeAttribute(writer, ComplexDatatypeValidate.s_descName, object.getDescription());
      WriterUtil.writeAttribute(writer, ComplexDatatypeValidate.s_minName, object.getMinOccurs());
      WriterUtil.writeAttribute(writer, ComplexDatatypeValidate.s_maxName, object.getMaxOccurs());

      // Write type element reference
      if( object.getDatatype() != null ) {
         WriterUtil.writeRefElement(writer, ComplexDatatypeValidate.s_typeName, object.getDatatype(), refMap);
      }
   }
}
