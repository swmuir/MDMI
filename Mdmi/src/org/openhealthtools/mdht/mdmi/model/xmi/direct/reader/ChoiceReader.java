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

public class ChoiceReader extends NodeReader<Choice> {
   private static final BagReader             s_bagReader    = new BagReader(BagValidate.s_nodesField);
   private static final ChoiceReader          s_choiceReader = new ChoiceReader(BagValidate.s_nodesField);
   private static final LeafSyntaxTransReader s_leafReader   = new LeafSyntaxTransReader(BagValidate.s_nodesField);

   public ChoiceReader() {
      super();
   }

   public ChoiceReader( String nodeName ) {
      super(nodeName);
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, Choice object ) {
      super.readAttributes(reader, object);

      object.setConstraint(reader.getAttributeValue(null, ChoiceValidate.s_constrName));
      object.setConstraintExpressionLanguage(reader.getAttributeValue(null, ChoiceValidate.s_constrLangName));
   }

   @Override
   public boolean canRead( XMLStreamReader reader ) {
      return super.canRead(reader) && typeIsChoice(reader);
   }

   private boolean typeIsChoice( XMLStreamReader reader ) {
      return Choice.class.getSimpleName().equals(XMIReaderUtil.getType(reader));
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root,
         Map<String, Object> objectMap, Choice object ) throws XMLStreamException {

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
   protected Choice createObject( XMLStreamReader reader ) {
      return new Choice();
   }
}
