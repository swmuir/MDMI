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
import org.openhealthtools.mdht.mdmi.model.enums.*;
import org.openhealthtools.mdht.mdmi.model.raw.*;
import org.openhealthtools.mdht.mdmi.model.validate.*;
import org.openhealthtools.mdht.mdmi.model.xmi.*;

public class SemanticElementReader extends XMIReaderDirectAbstract<SemanticElement> {
   private static final MdmiExpressionReader         s_exprReader        = new MdmiExpressionReader(
                                                                               SemanticElementValidate.s_compName);
   private static final MdmiExpressionReader         s_exprInReader      = new MdmiExpressionReader(
                                                                               SemanticElementValidate.s_compInName);
   private static final MdmiExpressionReader         s_exprOutReader     = new MdmiExpressionReader(
                                                                               SemanticElementValidate.s_compOutName);
   private static final SemanticElementRelateReader  s_relateReader      = new SemanticElementRelateReader(
                                                                               SemanticElementValidate.s_relateName);
   private static final ToBusinessElementReader      s_toBusReader       = new ToBusinessElementReader(
                                                                               SemanticElementValidate.s_fromMdmiField);
   private static final ToSemanticElementReader      s_toMsgReader       = new ToSemanticElementReader(
                                                                               SemanticElementValidate.s_toMdmiField);
   private static final SemanticElementBusRuleReader s_elemBusRuleReader = new SemanticElementBusRuleReader(
                                                                               SemanticElementValidate.s_busRulesName);

   public SemanticElementReader() {
      super(SemanticElement.class.getSimpleName());
   }

   public SemanticElementReader( String nodeName ) {
      super(nodeName);
   }

   @Override
   protected String getObjectName( SemanticElement object ) {
      return object.getName();
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root,
         Map<String, Object> objectMap, SemanticElement object ) throws XMLStreamException {

      if( s_exprReader.canRead(reader) ) {
         MdmiExpression expr = s_exprReader.readAndBuild(reader, parsingInfo, root, objectMap);
         object.setComputedValue(expr);
      }
      else if( s_exprInReader.canRead(reader) ) {
         MdmiExpression expr = s_exprInReader.readAndBuild(reader, parsingInfo, root, objectMap);
         object.setComputedInValue(expr);
      }
      else if( s_exprOutReader.canRead(reader) ) {
         MdmiExpression expr = s_exprOutReader.readAndBuild(reader, parsingInfo, root, objectMap);
         object.setComputedOutValue(expr);
      }
      else if( s_relateReader.canRead(reader) ) {
         SemanticElementRelationship relate = s_relateReader.readAndBuild(reader, parsingInfo, root, objectMap);
         relate.setContext(object);
         object.addRelationship(relate);
      }
      else if( s_toBusReader.canRead(reader) ) {
         ToBusinessElement busElem = s_toBusReader.readAndBuild(reader, parsingInfo, root, objectMap);
         busElem.setOwner(object);
         object.addFromMdmi(busElem);
      }
      else if( s_toMsgReader.canRead(reader) ) {
         ToMessageElement msgElem = s_toMsgReader.readAndBuild(reader, parsingInfo, root, objectMap);
         msgElem.setOwner(object);
         object.addToMdmi(msgElem);
      }
      else if( s_elemBusRuleReader.canRead(reader) ) {
         SemanticElementBusinessRule rule = s_elemBusRuleReader.readAndBuild(reader, parsingInfo, root, objectMap);
         rule.setSemanticElement(object);
         object.addBusinessRule(rule);
      }
   }

   @Override
   protected SemanticElement createObject( XMLStreamReader reader ) {
      return new SemanticElement();
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, SemanticElement object ) {
      // TODO- property qualifier

      object.setName(reader.getAttributeValue(null, SemanticElementValidate.s_nameField));
      object.setDescription(reader.getAttributeValue(null, SemanticElementValidate.s_descName));
      object.setOrdering(reader.getAttributeValue(null, SemanticElementValidate.s_orderName));
      object.setOrderingLanguage(reader.getAttributeValue(null, SemanticElementValidate.s_orderLangName));

      Boolean bool = XMIReaderUtil.convertToBoolean(reader.getAttributeValue(null,
            SemanticElementValidate.s_multInstName));
      if( bool != null ) {
         object.setMultipleInstances(bool);
      }

      SemanticElementType elemType = XMIReaderUtil.convertToElementType(reader.getAttributeValue(null,
            SemanticElementValidate.s_elemTypeField));
      if( elemType != null ) {
         object.setSemanticElementType(elemType);
      }
   }
}
