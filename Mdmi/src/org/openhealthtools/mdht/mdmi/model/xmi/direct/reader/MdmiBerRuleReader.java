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

public class MdmiBerRuleReader extends XMIReaderDirectAbstract<MdmiBusinessElementRule> {
   private static final StringReader s_ruleReader = new StringReader(BusinessElemRuleValidate.s_ruleField);

   public MdmiBerRuleReader() {
      super(MdmiBusinessElementRule.class.getSimpleName());
   }

   public MdmiBerRuleReader( String nodeName ) {
      super(nodeName);
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root,
         Map<String, Object> objectMap, MdmiBusinessElementRule object ) throws XMLStreamException {
      // Read rule element
      if( s_ruleReader.canRead(reader) ) {
         object.setRule(s_ruleReader.readAndBuild(reader, parsingInfo, root, objectMap));
      }
   }

   @Override
   protected MdmiBusinessElementRule createObject( XMLStreamReader reader ) {
      return new MdmiBusinessElementRule();
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, MdmiBusinessElementRule object ) {

      object.setName(reader.getAttributeValue(null, BusinessElemRuleValidate.s_nameField));
      object.setDescription(reader.getAttributeValue(null, BusinessElemRuleValidate.s_descName));
      object.setRuleExpressionLanguage(reader.getAttributeValue(null, BusinessElemRuleValidate.s_ruleLangName));
   }

   @Override
   protected String getObjectName( MdmiBusinessElementRule object ) {
      return object.getName();
   }
}
