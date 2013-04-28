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

public class MessageModelWriter extends DirectWriterAbstract<MessageModel> {
   private static final MessageSyntaxModelWriter s_syntaxWriter  = new MessageSyntaxModelWriter();
   private static final SemanticElemSetWriter    s_elemSetWriter = new SemanticElemSetWriter();

   @Override
   protected String getRootElementName() {
      return MessageModel.class.getSimpleName();
   }

   @Override
   protected void writeAttributesAndChildren( MessageModel object, XMLStreamWriter writer, Map<Object, String> refMap )
         throws XMLStreamException {

      // Write attributes
      WriterUtil.writeAttribute(writer, MessageModelValidate.s_nameField, object.getMessageModelName());
      WriterUtil.writeAttribute(writer, MessageModelValidate.s_descName, object.getDescription());
      WriterUtil.writeAttribute(writer, MessageModelValidate.s_sourceName, object.getSource());

      // Write child elements
      if( object.getSyntaxModel() != null ) {
         s_syntaxWriter.writeElement(MessageModelValidate.s_syntaxField, object.getSyntaxModel(), writer, refMap);
      }
      if( object.getElementSet() != null ) {
         s_elemSetWriter.writeElement(MessageModelValidate.s_elemSetField, object.getElementSet(), writer, refMap);
      }
   }
}
