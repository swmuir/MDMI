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

public class MdmiBerWriter extends DirectWriterAbstract<MdmiBusinessElementReference> {
   MdmiBerRuleWriter s_ruleWriter = new MdmiBerRuleWriter();

   @Override
   protected String getRootElementName() {
      return MdmiBusinessElementReference.class.getSimpleName();
   }

   @Override
   protected void writeAttributesAndChildren( MdmiBusinessElementReference object, XMLStreamWriter writer,
         Map<Object, String> refMap ) throws XMLStreamException {

      // Write attributes
      WriterUtil.writeAttribute(writer, BusinessElemRefValidate.s_nameField, object.getName());
      WriterUtil.writeAttribute(writer, BusinessElemRefValidate.s_descField, object.getDescription());
      WriterUtil.writeAttribute(writer, BusinessElemRefValidate.s_refField, object.getReference());
      WriterUtil.writeAttribute(writer, BusinessElemRefValidate.s_idField, object.getUniqueIdentifier());
      WriterUtil.writeAttribute(writer, BusinessElemRefValidate.s_isReadonly, object.isReadonly());
      WriterUtil.writeAttribute(writer, SemanticElementValidate.s_enumValueSetField, object.getEnumValueSetField());
      WriterUtil.writeAttribute(writer, SemanticElementValidate.s_enumValueField, object.getEnumValueField());
      WriterUtil.writeAttribute(writer, SemanticElementValidate.s_enumValueDescrField, object.getEnumValueDescrField());
      WriterUtil.writeAttribute(writer, SemanticElementValidate.s_enumValueSet, object.getEnumValueSet());

      // Write child element
      if( object.getReferenceDatatype() != null ) {
         WriterUtil.writeRefElement(writer, BusinessElemRefValidate.s_refTypeField, object.getReferenceDatatype(),
               refMap);
      }

      for( MdmiBusinessElementRule rule : object.getBusinessRules() ) {
         s_ruleWriter.writeElement(BusinessElemRefValidate.s_ruleName, rule, writer, refMap);
      }
   }

}
