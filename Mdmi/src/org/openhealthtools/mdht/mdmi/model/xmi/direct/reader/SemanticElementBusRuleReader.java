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

public class SemanticElementBusRuleReader extends XMIReaderDirectAbstract<SemanticElementBusinessRule> {
   // Child readers
   private static final StringReader s_ruleReader = new StringReader(SemanticElemBusRuleValidate.s_ruleField);

   public SemanticElementBusRuleReader( String nodeName ) {
      super(nodeName);
   }

   public SemanticElementBusRuleReader() {
      super(SemanticElementBusinessRule.class.getSimpleName());
   }

   @Override
   protected String getObjectName( SemanticElementBusinessRule object ) {
      return object.getName();
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root,
         Map<String, Object> objectMap, SemanticElementBusinessRule object ) throws XMLStreamException {

      // Read rule element
      if( s_ruleReader.canRead(reader) ) {
         object.setRule(s_ruleReader.readAndBuild(reader, parsingInfo, root, objectMap));
      }
   }

   @Override
   protected SemanticElementBusinessRule createObject( XMLStreamReader reader ) {
      return new SemanticElementBusinessRule();
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, SemanticElementBusinessRule object ) {

      object.setName(reader.getAttributeValue(null, SemanticElemBusRuleValidate.s_nameField));
      object.setDescription(reader.getAttributeValue(null, SemanticElemBusRuleValidate.s_descName));
      object.setRuleExpressionLanguage(reader.getAttributeValue(null, SemanticElemBusRuleValidate.s_ruleLangName));
   }
}
