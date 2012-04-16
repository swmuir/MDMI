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
import org.openhealthtools.mdht.mdmi.model.xmi.*;

public abstract class NodeReader<T extends Node> extends XMIReaderDirectAbstract<T> {
   public NodeReader( String nodeName ) {
      super(nodeName);
   }

   public NodeReader() {
      super(Node.class.getSimpleName());
   }

   @Override
   protected String getObjectName( T object ) {
      return object.getName();
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, T object ) {
      object.setName(reader.getAttributeValue(null, NodeValidate.s_nameField));
      object.setDescription(reader.getAttributeValue(null, NodeValidate.s_descName));
      object.setLocation(reader.getAttributeValue(null, NodeValidate.s_locationField));
      object.setLocationExpressionLanguage(reader.getAttributeValue(null, NodeValidate.s_locExprLangName));
      object.setFieldName(reader.getAttributeValue(null, NodeValidate.s_fieldName));

      Integer value = XMIReaderUtil.convertToInteger(reader.getAttributeValue(null, NodeValidate.s_minName));
      if( value != null ) {
         object.setMinOccurs(value);
      }

      value = XMIReaderUtil.convertToInteger(reader.getAttributeValue(null, NodeValidate.s_maxName));
      if( value != null ) {
         object.setMaxOccurs(value);
      }
   }

}
