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

public class MessageCompositeReader extends XMIReaderDirectAbstract<MessageComposite> {
   private static final SimpleMessageCompositeReader s_simpleCompReader = new SimpleMessageCompositeReader(
                                                                              SemanticElementSetValidate.s_compName);
   private static final MessageCompositeReader       s_msgCompReader    = new MessageCompositeReader(
                                                                              SemanticElementSetValidate.s_compName);

   public MessageCompositeReader( String nodeName ) {
      super(nodeName);
   }

   public MessageCompositeReader() {
      super(MessageComposite.class.getSimpleName());
   }

   @Override
   public boolean canRead( XMLStreamReader reader ) {
      return super.canRead(reader) && typeIsMessageComp(reader);
   }

   private boolean typeIsMessageComp( XMLStreamReader reader ) {
      return MessageComposite.class.getSimpleName().equals(XMIReaderUtil.getType(reader));
   }

   @Override
   protected String getObjectName( MessageComposite object ) {
      return object.getName();
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root,
         Map<String, Object> objectMap, MessageComposite object ) throws XMLStreamException {

      if( s_simpleCompReader.canRead(reader) ) {
         SimpleMessageComposite comp = s_simpleCompReader.readAndBuild(reader, parsingInfo, root, objectMap);
         object.addComposite(comp);
      }
      else if( s_msgCompReader.canRead(reader) ) {
         MessageComposite comp = s_msgCompReader.readAndBuild(reader, parsingInfo, root, objectMap);
         comp.setOwner(object);
         object.addComposite(comp);
      }
   }

   @Override
   protected MessageComposite createObject( XMLStreamReader reader ) {
      return new MessageComposite();
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, MessageComposite object ) {

      object.setName(reader.getAttributeValue(null, SimpleMsgCompositeValidate.s_nameField));
      object.setDescription(reader.getAttributeValue(null, SimpleMsgCompositeValidate.s_descName));
   }
}
