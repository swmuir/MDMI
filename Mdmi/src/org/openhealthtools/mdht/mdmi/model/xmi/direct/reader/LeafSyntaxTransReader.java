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

public class LeafSyntaxTransReader extends NodeReader<LeafSyntaxTranslator> {
   public LeafSyntaxTransReader() {
      super();
   }

   public LeafSyntaxTransReader( String nodeName ) {
      super(nodeName);
   }

   @Override
   public boolean canRead( XMLStreamReader reader ) {
      return super.canRead(reader) && typeIsLeaf(reader);
   }

   private boolean typeIsLeaf( XMLStreamReader reader ) {
      return LeafSyntaxTranslator.class.getSimpleName().equals(XMIReaderUtil.getType(reader));
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, LeafSyntaxTranslator object ) {
      super.readAttributes(reader, object);
      object.setFormat(reader.getAttributeValue(null, LeafSyntaxValidate.s_formatField));
      object.setFormatExpressionLanguage(reader.getAttributeValue(null, LeafSyntaxValidate.s_formatLangName));
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root,
         Map<String, Object> objectMap, LeafSyntaxTranslator object ) throws XMLStreamException {
   }

   @Override
   protected LeafSyntaxTranslator createObject( XMLStreamReader reader ) {
      return new LeafSyntaxTranslator();
   }

}
