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

public class SemanticElementRelateReader extends XMIReaderDirectAbstract<SemanticElementRelationship> {
   // Child readers
   private static final StringReader s_ruleReader = new StringReader(SemanticElemRelateValidate.s_ruleField);

   public SemanticElementRelateReader( String nodeName ) {
      super(nodeName);
   }

   public SemanticElementRelateReader() {
      super(SemanticElementRelationship.class.getSimpleName());
   }

   @Override
   protected String getObjectName( SemanticElementRelationship object ) {
      return object.getName();
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root,
         Map<String, Object> objectMap, SemanticElementRelationship object ) throws XMLStreamException {

      // Read rule element
      if( s_ruleReader.canRead(reader) ) {
         object.setRule(s_ruleReader.readAndBuild(reader, parsingInfo, root, objectMap));
      }
   }

   @Override
   protected SemanticElementRelationship createObject( XMLStreamReader reader ) {
      return new SemanticElementRelationship();
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, SemanticElementRelationship object ) {

      object.setName(reader.getAttributeValue(null, SemanticElemRelateValidate.s_nameField));
      object.setDescription(reader.getAttributeValue(null, SemanticElemRelateValidate.s_descName));
      object.setRuleExpressionLanguage(reader.getAttributeValue(null, SemanticElemRelateValidate.s_ruleLangName));

      Integer value = XMIReaderUtil.convertToInteger(reader.getAttributeValue(null,
            SemanticElemRelateValidate.s_minOccursName));
      if( value != null ) {
         object.setMinOccurs(value);
      }

      value = XMIReaderUtil
            .convertToInteger(reader.getAttributeValue(null, SemanticElemRelateValidate.s_maxOccursName));
      if( value != null ) {
         object.setMaxOccurs(value);
      }

      Boolean bool = XMIReaderUtil.convertToBoolean(reader.getAttributeValue(null,
            SemanticElemRelateValidate.s_srcInstanceName));
      if( bool != null ) {
         object.setSourceIsInstance(bool);
      }
      bool = XMIReaderUtil.convertToBoolean(reader.getAttributeValue(null,
            SemanticElemRelateValidate.s_targetInstanceName));
      if( bool != null ) {
         object.setTargetIsInstance(bool);
      }
   }
}
