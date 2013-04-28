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

public class MdmiExpressionReader extends XMIReaderDirectAbstract<MdmiExpression> {
   public MdmiExpressionReader( String nodeName ) {
      super(nodeName);
   }

   public MdmiExpressionReader() {
      super(MdmiExpression.class.getSimpleName());
   }

   @Override
   protected String getObjectName( MdmiExpression object ) {
      // This will never be used by this particular reader.
      return object.toString();
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root,
         Map<String, Object> objectMap, MdmiExpression object ) throws XMLStreamException {
   }

   @Override
   protected MdmiExpression createObject( XMLStreamReader reader ) {
      return new MdmiExpression();
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, MdmiExpression object ) {
      object.setExpression(reader.getAttributeValue(null, SemanticElementValidate.s_exprName));
      object.setLanguage(reader.getAttributeValue(null, SemanticElementValidate.s_langName));
   }
}
