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

public class MessageGroupWriter extends DirectWriterAbstract<MessageGroup> {
   // Child writers
   private static final MessageModelWriter   s_modelWriter    = new MessageModelWriter();
   private static final MdmiDictionaryWriter s_dictWriter     = new MdmiDictionaryWriter();
   private static final DataRuleWriter       s_dataRuleWriter = new DataRuleWriter();

   @Override
   protected String getRootElementName() {
      return MessageGroup.class.getSimpleName();
   }

   @SuppressWarnings( "unchecked" )
   @Override
   protected void writeAttributesAndChildren( MessageGroup object, XMLStreamWriter writer, Map<Object, String> refMap )
         throws XMLStreamException {

      // Write attributes
      WriterUtil.writeAttribute(writer, MessageGroupValidate.s_nameField, object.getName());
      WriterUtil.writeAttribute(writer, MessageGroupValidate.s_defConstrField, object.getDefaultConstraintExprLang());
      WriterUtil.writeAttribute(writer, MessageGroupValidate.s_defLocField, object.getDefaultLocationExprLang());
      WriterUtil.writeAttribute(writer, MessageGroupValidate.s_defRuleField, object.getDefaultRuleExprLang());
      WriterUtil.writeAttribute(writer, MessageGroupValidate.s_descField, object.getDescription());

      // Write child elements
      for( MdmiDatatype type : object.getDatatypes() ) {
         @SuppressWarnings( "rawtypes" )
         MdmiDatatypeWriter typeWriter = MdmiDatatypeWriter.getWriterByType(type);
         typeWriter.writeElement(MessageGroupValidate.s_typesName, type, writer, refMap);
      }

      for( DataRule rule : object.getDataRules() ) {
         s_dataRuleWriter.writeElement(MessageGroupValidate.s_dataRulesName, rule, writer, refMap);
      }

      if( object.getDomainDictionary() != null ) {
         s_dictWriter
               .writeElement(MessageGroupValidate.s_domainDictField, object.getDomainDictionary(), writer, refMap);
      }

      for( MessageModel model : object.getModels() ) {
         s_modelWriter.writeElement(MessageGroupValidate.s_modelsField, model, writer, refMap);
      }
   }
}
