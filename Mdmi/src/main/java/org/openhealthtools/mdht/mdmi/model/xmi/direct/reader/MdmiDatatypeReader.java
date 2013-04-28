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

import javax.xml.stream.*;

import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.model.validate.*;
import org.openhealthtools.mdht.mdmi.model.xmi.XMIReaderUtil;

public abstract class MdmiDatatypeReader<T extends MdmiDatatype> extends XMIReaderDirectAbstract<T> {
   protected MdmiDatatypeReader( String nodeName ) {
      super(nodeName);
   }

   protected MdmiDatatypeReader() {
      super(MdmiDatatype.class.getSimpleName());
   }

   @Override
   protected String getObjectName( T object ) {
      return object.getTypeName();
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, T object ) {
      object.setTypeName(reader.getAttributeValue(null, MdmiDatatypeValidate.s_nameField));
      object.setDescription(reader.getAttributeValue(null, MdmiDatatypeValidate.s_descName));

      Boolean bool = XMIReaderUtil.convertToBoolean(reader.getAttributeValue(null,
            MdmiDatatypeValidate.s_isReadonly));
      if( bool != null ) {
         object.setReadonly(bool);
      }
   }
}
