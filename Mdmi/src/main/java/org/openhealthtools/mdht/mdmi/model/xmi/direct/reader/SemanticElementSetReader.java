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

public class SemanticElementSetReader extends XMIReaderDirectAbstract<SemanticElementSet> {
   private static final SemanticElementReader        s_elemReader       = new SemanticElementReader(
                                                                              SemanticElementSetValidate.s_semElemsField);
   private static final SimpleMessageCompositeReader s_simpleCompReader = new SimpleMessageCompositeReader(
                                                                              SemanticElementSetValidate.s_compName);
   private static final MessageCompositeReader       s_msgCompReader    = new MessageCompositeReader(
                                                                              SemanticElementSetValidate.s_compName);

   public SemanticElementSetReader() {
      super(SemanticElementSet.class.getSimpleName());
   }

   public SemanticElementSetReader( String nodeName ) {
      super(nodeName);
   }

   @Override
   protected String getObjectName( SemanticElementSet object ) {
      return object.getName();
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root,
         Map<String, Object> objectMap, SemanticElementSet object ) throws XMLStreamException {
      if( s_elemReader.canRead(reader) ) {
         SemanticElement elem = s_elemReader.readAndBuild(reader, parsingInfo, root, objectMap);
         elem.setElementSet(object);
         object.addSemanticElement(elem);
      }
      else if( s_simpleCompReader.canRead(reader) ) {
         SimpleMessageComposite comp = s_simpleCompReader.readAndBuild(reader, parsingInfo, root, objectMap);
         comp.setElementSet(object);
         object.addComposite(comp);
      }
      else if( s_msgCompReader.canRead(reader) ) {
         MessageComposite comp = s_msgCompReader.readAndBuild(reader, parsingInfo, root, objectMap);
         comp.setElementSet(object);
         object.addComposite(comp);
      }
   }

   @Override
   protected SemanticElementSet createObject( XMLStreamReader reader ) {
      return new SemanticElementSet();
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, SemanticElementSet object ) {
      object.setName(reader.getAttributeValue(null, SemanticElementSetValidate.s_nameField));
      object.setDescription(reader.getAttributeValue(null, SemanticElementSetValidate.s_descName));
   }
}
