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
import org.openhealthtools.mdht.mdmi.model.xmi.direct.*;

public class MessageGroupReader extends XMIReaderDirectAbstract<MessageGroup> {
   private static final MdmiDictRefReader        s_dictRefReader     = new MdmiDictRefReader(
                                                                           MessageGroupValidate.s_domainDictField);
   private static final MessageModelReader       s_modelReader       = new MessageModelReader(
                                                                           MessageGroupValidate.s_modelsField);
   private static final DataRuleReader           s_dataRuleReader    = new DataRuleReader(
                                                                           MessageGroupValidate.s_dataRulesName);
   private static final ExternalDatatypeReader   s_extTypeReader     = new ExternalDatatypeReader(
                                                                           MessageGroupValidate.s_typesName);
   private static final ChoiceDatatypeReader     s_choiceTypeReader  = new ChoiceDatatypeReader(
                                                                           MessageGroupValidate.s_typesName);
   private static final StructuredDatatypeReader s_structTypeReader  = new StructuredDatatypeReader(
                                                                           MessageGroupValidate.s_typesName);
   private static final EnumeratedDatatypeReader s_enumTypeReader    = new EnumeratedDatatypeReader(
                                                                           MessageGroupValidate.s_typesName);
   private static final DerivedDatatypeReader    s_derivedTypeReader = new DerivedDatatypeReader(
                                                                           MessageGroupValidate.s_typesName);
   private static final PrimitiveDatatypeReader  s_primTypeReader    = new PrimitiveDatatypeReader(
                                                                           MessageGroupValidate.s_typesName);

   public MessageGroupReader() {
      super(MessageGroup.class.getSimpleName());
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root,
         Map<String, Object> objectMap, MessageGroup object ) throws XMLStreamException {

      if( s_dictRefReader.canRead(reader) ) {
         MdmiDomainDictionaryReference ref = s_dictRefReader.readAndBuild(reader, parsingInfo, root, objectMap);
         ref.setMessageGroup(object);
         object.setDomainDictionary(ref);
      }
      else if( s_modelReader.canRead(reader) ) {
         MessageModel model = s_modelReader.readAndBuild(reader, parsingInfo, root, objectMap);
         model.setGroup(object);
         object.addModel(model);
      }
      else if( s_dataRuleReader.canRead(reader) ) {
         DataRule rule = s_dataRuleReader.readAndBuild(reader, parsingInfo, root, objectMap);
         rule.setScope(object);
         object.addDataRule(rule);
      }
      else if( s_extTypeReader.canRead(reader) ) {
         DTExternal type = s_extTypeReader.readAndBuild(reader, parsingInfo, root, objectMap);
         object.addDatatype(type);
         type.setOwner(object);
      }
      else if( s_choiceTypeReader.canRead(reader) ) {
         DTCChoice type = s_choiceTypeReader.readAndBuild(reader, parsingInfo, root, objectMap);
         object.addDatatype(type);
         type.setOwner(object);
      }
      else if( s_structTypeReader.canRead(reader) ) {
         DTCStructured type = s_structTypeReader.readAndBuild(reader, parsingInfo, root, objectMap);
         object.addDatatype(type);
         type.setOwner(object);
      }
      else if( s_enumTypeReader.canRead(reader) ) {
         DTSEnumerated type = s_enumTypeReader.readAndBuild(reader, parsingInfo, root, objectMap);
         object.addDatatype(type);
         type.setOwner(object);
      }
      else if( s_derivedTypeReader.canRead(reader) ) {
         DTSDerived type = s_derivedTypeReader.readAndBuild(reader, parsingInfo, root, objectMap);
         object.addDatatype(type);
         type.setOwner(object);
      }
      else if( s_primTypeReader.canRead(reader) ) {
         DTSPrimitive type = s_primTypeReader.readAndBuild(reader, parsingInfo, root, objectMap);
         object.addDatatype(type);
      }
   }

   @Override
   protected MessageGroup createObject( XMLStreamReader reader ) {
      return new MessageGroup();
   }

   @Override
   protected String getNodeNamespace() {
      return XMIDirectConstants.MDMI_NAMESPACE;
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, MessageGroup object ) {
      object.setName(reader.getAttributeValue(null, MessageGroupValidate.s_nameField));
      object.setDescription(reader.getAttributeValue(null, MessageGroupValidate.s_descField));
      object.setDefaultConstraintExprLang(reader.getAttributeValue(null, MessageGroupValidate.s_defConstrField));
      object.setDefaultLocationExprLang(reader.getAttributeValue(null, MessageGroupValidate.s_defLocField));
      object.setDefaultRuleExprLang(reader.getAttributeValue(null, MessageGroupValidate.s_defRuleField));
   }

   @Override
   protected String getObjectName( MessageGroup object ) {
      return object.getName();
   }
}
