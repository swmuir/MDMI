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

public class MessageSyntaxModelReader extends XMIReaderDirectAbstract<MessageSyntaxModel> {
   private static final BagReader             s_bagReader    = new BagReader(MessageSyntaxModelValidate.s_rootField);
   private static final ChoiceReader          s_choiceReader = new ChoiceReader(MessageSyntaxModelValidate.s_rootField);
   private static final LeafSyntaxTransReader s_leafReader   = new LeafSyntaxTransReader(
                                                                   MessageSyntaxModelValidate.s_rootField);

   public MessageSyntaxModelReader( String nodeName ) {
      super(nodeName);
   }

   public MessageSyntaxModelReader() {
      super(MessageSyntaxModel.class.getSimpleName());
   }

   @Override
   protected String getObjectName( MessageSyntaxModel object ) {
      return object.getName();
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root,
         Map<String, Object> objectMap, MessageSyntaxModel object ) throws XMLStreamException {

      Node node = null;
      if( s_bagReader.canRead(reader) ) {
         node = s_bagReader.readAndBuild(reader, parsingInfo, root, objectMap);
      }
      else if( s_choiceReader.canRead(reader) ) {
         node = s_choiceReader.readAndBuild(reader, parsingInfo, root, objectMap);
      }
      else if( s_leafReader.canRead(reader) ) {
         node = s_leafReader.readAndBuild(reader, parsingInfo, root, objectMap);
      }

      if( node != null ) {
         node.setSyntaxModel(object);
         object.setRoot(node);
      }
   }

   @Override
   protected MessageSyntaxModel createObject( XMLStreamReader reader ) {
      return new MessageSyntaxModel();
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, MessageSyntaxModel object ) {
      object.setName(reader.getAttributeValue(null, MessageSyntaxModelValidate.s_nameField));
      object.setDescription(reader.getAttributeValue(null, MessageSyntaxModelValidate.s_descName));
   }
}
