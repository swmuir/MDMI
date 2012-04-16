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

public class SemanticElemRelateWriter extends DirectWriterAbstract<SemanticElementRelationship> {
   private static final StringWriter s_ruleWriter = new StringWriter();

   @Override
   protected String getRootElementName() {
      return SemanticElementRelationship.class.getSimpleName();
   }

   @Override
   protected void writeAttributesAndChildren( SemanticElementRelationship object, XMLStreamWriter writer,
         Map<Object, String> refMap ) throws XMLStreamException {

      // Write attributes.
      WriterUtil.writeAttribute(writer, SemanticElemRelateValidate.s_nameField, object.getName());
      WriterUtil.writeAttribute(writer, SemanticElemRelateValidate.s_descName, object.getDescription());
      WriterUtil.writeAttribute(writer, SemanticElemRelateValidate.s_ruleLangName, object.getRuleExpressionLanguage());
      WriterUtil.writeAttribute(writer, SemanticElemRelateValidate.s_minOccursName, object.getMinOccurs());
      WriterUtil.writeAttribute(writer, SemanticElemRelateValidate.s_maxOccursName, object.getMaxOccurs());
      WriterUtil.writeAttribute(writer, SemanticElemRelateValidate.s_srcInstanceName, object.isSourceIsInstance());
      WriterUtil.writeAttribute(writer, SemanticElemRelateValidate.s_targetInstanceName, object.isTargetIsInstance());

      // Write rule as child element.
      s_ruleWriter.writeElement(SemanticElemRelateValidate.s_ruleField, object.getRule(), writer, refMap);

      // Write child element reference
      if( object.getRelatedSemanticElement() != null ) {
         WriterUtil.writeRefElement(writer, SemanticElemRelateValidate.s_relateSemElem,
               object.getRelatedSemanticElement(), refMap);
      }
   }
}
