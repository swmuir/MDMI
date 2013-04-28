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
package org.openhealthtools.mdht.mdmi.model.xmi.direct.writer;

import java.util.*;

import javax.xml.stream.*;

import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.model.validate.*;

public class SemanticElementWriter extends DirectWriterAbstract<SemanticElement> {
   private static final SemanticElemRelateWriter  s_relateWriter  = new SemanticElemRelateWriter();
   private static final SemanticElemBusRuleWriter s_busRuleWriter = new SemanticElemBusRuleWriter();
   private static final ToBusinessElemWriter      s_toBusWriter   = new ToBusinessElemWriter();
   private static final ToSemanticElemWriter      s_toSemWriter   = new ToSemanticElemWriter();
   private static final MdmiExpressionWriter      s_exprWriter    = new MdmiExpressionWriter();

   @Override
   protected String getRootElementName() {
      return SemanticElement.class.getSimpleName();
   }

   @Override
   protected void writeAttributesAndChildren( SemanticElement object, XMLStreamWriter writer, Map<Object, String> refMap )
         throws XMLStreamException {

      // TODO- property qualifiers

      // Write attributes
      WriterUtil.writeAttribute(writer, SemanticElementValidate.s_nameField, object.getName());
      WriterUtil.writeAttribute(writer, SemanticElementValidate.s_descName, object.getDescription());
      WriterUtil.writeAttribute(writer, SemanticElementValidate.s_elemTypeField, object.getSemanticElementType());
      WriterUtil.writeAttribute(writer, SemanticElementValidate.s_multInstName, object.isMultipleInstances());
      WriterUtil.writeAttribute(writer, SemanticElementValidate.s_orderName, object.getOrdering());
      WriterUtil.writeAttribute(writer, SemanticElementValidate.s_orderLangName, object.getOrderingLanguage());

      // Write child elements, including references
      if( object.getDatatype() != null ) {
         WriterUtil.writeRefElement(writer, SemanticElementValidate.s_typeField, object.getDatatype(), refMap);
      }

      if( object.getComputedValue() != null ) {
         s_exprWriter.writeElement(SemanticElementValidate.s_compName, object.getComputedValue(), writer, refMap);
      }

      if( object.getComputedInValue() != null ) {
         s_exprWriter.writeElement(SemanticElementValidate.s_compInName, object.getComputedInValue(), writer, refMap);
      }

      if( object.getComputedOutValue() != null ) {
         s_exprWriter.writeElement(SemanticElementValidate.s_compOutName, object.getComputedOutValue(), writer, refMap);
      }

      if( object.getComposite() != null ) {
         WriterUtil.writeRefElement(writer, SemanticElementValidate.s_compositeName, object.getComposite(), refMap);
      }

      if( object.getSyntaxNode() != null ) {
         WriterUtil.writeRefElement(writer, SemanticElementValidate.s_nodeName, object.getSyntaxNode(), refMap);
      }

      for( DataRule rule : object.getDataRules() ) {
         WriterUtil.writeRefElement(writer, SemanticElementValidate.s_dataRulesName, rule, refMap);
      }

      for( SemanticElementRelationship relate : object.getRelationships() ) {
         s_relateWriter.writeElement(SemanticElementValidate.s_relateName, relate, writer, refMap);
      }

      for( SemanticElement elem : object.getChildren() ) {
         WriterUtil.writeRefElement(writer, SemanticElementValidate.s_childName, elem, refMap);
      }

      for( ToBusinessElement busElem : object.getFromMdmi() ) {
         s_toBusWriter.writeElement(SemanticElementValidate.s_fromMdmiField, busElem, writer, refMap);
      }

      for( ToMessageElement msgElem : object.getToMdmi() ) {
         s_toSemWriter.writeElement(SemanticElementValidate.s_toMdmiField, msgElem, writer, refMap);
      }

      for( SemanticElementBusinessRule rule : object.getBusinessRules() ) {
         s_busRuleWriter.writeElement(SemanticElementValidate.s_busRulesName, rule, writer, refMap);
      }
   }
}
