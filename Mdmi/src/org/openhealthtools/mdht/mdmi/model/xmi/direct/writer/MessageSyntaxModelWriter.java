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

public class MessageSyntaxModelWriter extends DirectWriterAbstract<MessageSyntaxModel> {
   @Override
   protected String getRootElementName() {
      return MessageSyntaxModel.class.getSimpleName();
   }

   @SuppressWarnings( "unchecked" )
   @Override
   protected void writeAttributesAndChildren( MessageSyntaxModel object, XMLStreamWriter writer,
         Map<Object, String> refMap ) throws XMLStreamException {

      // Write attributes
      WriterUtil.writeAttribute(writer, MessageSyntaxModelValidate.s_nameField, object.getName());
      WriterUtil.writeAttribute(writer, MessageSyntaxModelValidate.s_descName, object.getDescription());

      // Write child elements
      if( object.getElementSet() != null ) {
         WriterUtil.writeRefElement(writer, MessageSyntaxModelValidate.s_elemSetField, object.getElementSet(), refMap);
      }

      if( object.getRoot() != null ) {
         @SuppressWarnings( "rawtypes" )
         NodeWriter nodeWriter = NodeWriter.getWriterByNodeType(object.getRoot());
         nodeWriter.writeElement(MessageSyntaxModelValidate.s_rootField, object.getRoot(), writer, refMap);
      }
   }
}
