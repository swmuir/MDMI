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

public class DataRuleReader extends XMIReaderDirectAbstract<DataRule> {
   // Child readers
   private static final StringReader s_ruleReader = new StringReader(DataRuleValidate.s_ruleField);

   public DataRuleReader( String nodeName ) {
      super(nodeName);
   }

   public DataRuleReader() {
      super(DataRule.class.getSimpleName());
   }

   @Override
   protected String getObjectName( DataRule object ) {
      return object.getName();
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root,
         Map<String, Object> objectMap, DataRule object ) throws XMLStreamException {

      // Read rule element
      if( s_ruleReader.canRead(reader) ) {
         object.setRule(s_ruleReader.readAndBuild(reader, parsingInfo, root, objectMap));
      }
   }

   @Override
   protected DataRule createObject( XMLStreamReader reader ) {
      return new DataRule();
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, DataRule object ) {
      object.setName(reader.getAttributeValue(null, DataRuleValidate.s_nameField));
      object.setDescription(reader.getAttributeValue(null, DataRuleValidate.s_descName));
      object.setRuleExpressionLanguage(reader.getAttributeValue(null, DataRuleValidate.s_ruleExprName));
   }
}
