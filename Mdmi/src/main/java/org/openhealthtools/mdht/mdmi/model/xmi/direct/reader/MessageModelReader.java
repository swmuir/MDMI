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

public class MessageModelReader extends XMIReaderDirectAbstract<MessageModel> {
   private static final SemanticElementSetReader s_elemSetReader     = new SemanticElementSetReader(
                                                                           MessageModelValidate.s_elemSetField);
   private static final MessageSyntaxModelReader s_syntaxModelReader = new MessageSyntaxModelReader(
                                                                           MessageModelValidate.s_syntaxField);

   public MessageModelReader( String nodeName ) {
      super(nodeName);
   }

   public MessageModelReader() {
      super(MessageModel.class.getSimpleName());
   }

   @Override
   protected String getObjectName( MessageModel object ) {
      return object.getMessageModelName();
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root,
         Map<String, Object> objectMap, MessageModel object ) throws XMLStreamException {

      if( s_elemSetReader.canRead(reader) ) {
         SemanticElementSet set = s_elemSetReader.readAndBuild(reader, parsingInfo, root, objectMap);
         set.setModel(object);
         object.setElementSet(set);
      }
      else if( s_syntaxModelReader.canRead(reader) ) {
         MessageSyntaxModel syntaxModel = s_syntaxModelReader.readAndBuild(reader, parsingInfo, root, objectMap);
         syntaxModel.setModel(object);
         object.setSyntaxModel(syntaxModel);
      }
   }

   @Override
   protected MessageModel createObject( XMLStreamReader reader ) {
      return new MessageModel();
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, MessageModel object ) {
      object.setMessageModelName(reader.getAttributeValue(null, MessageModelValidate.s_nameField));
      object.setDescription(reader.getAttributeValue(null, MessageModelValidate.s_descName));
      object.setSource(XMIReaderUtil.convertToURI(reader.getAttributeValue(null, MessageModelValidate.s_sourceName)));
   }

}
