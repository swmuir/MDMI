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

public class BagReader extends NodeReader<Bag> {
   private static final BagReader             s_bagReader    = new BagReader(BagValidate.s_nodesField);
   private static final ChoiceReader          s_choiceReader = new ChoiceReader(BagValidate.s_nodesField);
   private static final LeafSyntaxTransReader s_leafReader   = new LeafSyntaxTransReader(BagValidate.s_nodesField);

   public BagReader() {
      super();
   }

   public BagReader( String nodeName ) {
      super(nodeName);
   }

   @Override
   public boolean canRead( XMLStreamReader reader ) {
      return super.canRead(reader) && typeIsBag(reader);
   }

   private boolean typeIsBag( XMLStreamReader reader ) {
      return Bag.class.getSimpleName().equals(XMIReaderUtil.getType(reader));
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, Bag object ) {
      super.readAttributes(reader, object);

      Boolean bool = XMIReaderUtil.convertToBoolean(reader.getAttributeValue(null, BagValidate.s_uniqueName));
      if( bool != null ) {
         object.setUnique(bool);
      }

      bool = XMIReaderUtil.convertToBoolean(reader.getAttributeValue(null, BagValidate.s_orderedName));
      if( bool != null ) {
         object.setOrdered(bool);
      }
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root,
         Map<String, Object> objectMap, Bag object ) throws XMLStreamException {

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
         object.addNode(node);
         node.setParentNode(object);
      }
   }

   @Override
   protected Bag createObject( XMLStreamReader reader ) {
      return new Bag();
   }
}
