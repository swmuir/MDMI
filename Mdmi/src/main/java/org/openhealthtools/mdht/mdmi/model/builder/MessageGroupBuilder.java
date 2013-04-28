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
package org.openhealthtools.mdht.mdmi.model.builder;

import java.util.*;

import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.model.raw.*;

public class MessageGroupBuilder extends ModelBuilderAbstract<MessageGroup> implements IModelBuilder<MessageGroup> {
   private static final MessageModelBuilder m_msgModelBuilder  = new MessageModelBuilder();
   private static final DataRuleBuilder     m_dataRuleBuilder  = new DataRuleBuilder();
   private static final MdmiDictionaryBuilder  m_dictRefBuilder   = new MdmiDictionaryBuilder();
   private static final List<String>        s_stereotypeList   = new ArrayList<String>();
   private static final String              m_locExprAttrib    = "defaultLocationExpressionLanguage";
   private static final String              m_constrExprAttrib = "defaultConstraintExpressionLanguage";
   private static final String              m_ruleExprAttrib   = "defaultRuleExpressionLanguage";

   static {
      s_stereotypeList.add(StereotypeNames.CHOICE_DATA_TYPE);
      s_stereotypeList.add(StereotypeNames.STRUCT_DATA_TYPE);
      s_stereotypeList.add(StereotypeNames.EXTERNAL_DATA_TYPE);
      s_stereotypeList.add(StereotypeNames.ENUM_DATA_TYPE);
      s_stereotypeList.add(StereotypeNames.PRIM_DATA_TYPE);
      s_stereotypeList.add(StereotypeNames.DERIVED_DATA_TYPE);
      s_stereotypeList.add(StereotypeNames.MDMI_DOMAIN_DICT_REF);
      s_stereotypeList.add(StereotypeNames.DATA_RULE);
      s_stereotypeList.add(StereotypeNames.MESSAGE_MODEL);
   }

   @Override
   protected List<String> getStereotypesToProcess() {
      return s_stereotypeList;
   }

   @Override
   public MessageGroup buildMessageModelObject( ClassDef classDef, RawRoot root ) {
      // Create and init message group
      MessageGroup rv = new MessageGroup();
      rv.setName(BuilderUtil.getNameAttribVal(classDef));
      rv.setDescription(BuilderUtil.getDescriptionAttribVal(classDef));
      rv.setDefaultLocationExprLang(BuilderUtil.getAttribVal(classDef, m_locExprAttrib));
      rv.setDefaultConstraintExprLang(BuilderUtil.getAttribVal(classDef, m_constrExprAttrib));
      rv.setDefaultRuleExprLang(BuilderUtil.getAttribVal(classDef, m_ruleExprAttrib));
      return rv;
   }

   @Override
   protected void processAllAssociations( MessageGroup modelObject, ClassDef classDef, RawRoot root,
         Map<String, Object> objectMap ) {
      super.processAllAssociations(modelObject, classDef, root, objectMap);
      // Add association to all datatypes. These associations are implicit, so we can't rely
      // on them being handled the normal way.
      for( Object obj : objectMap.values() ) {
         if( obj instanceof MdmiDatatype ) {
            MdmiDatatype datatype = (MdmiDatatype)obj;

            modelObject.addDatatype(datatype);
            datatype.setOwner(modelObject);
         }
      }
   }

   @Override
   protected boolean createAssociation( MessageGroup modelObject, RawRoot root, ClassDef classDef,
         String stereotypeName, Attribute srcAttrib, Map<String, Object> objectMap ) {
      boolean rv = true;
      if( stereotypeName.equals(StereotypeNames.MESSAGE_MODEL) ) {
         MessageModel model = m_msgModelBuilder.buildMessageModelObject(classDef, root, objectMap);
         model.setGroup(modelObject);
         modelObject.addModel(model);
      }
      else if( stereotypeName.equals(StereotypeNames.MDMI_DOMAIN_DICT_REF) ) {
         MdmiDomainDictionaryReference ref = m_dictRefBuilder.buildMessageModelObject(classDef, root, objectMap);
         ref.setMessageGroup(modelObject);
         modelObject.setDomainDictionary(ref);
      }
      else if( stereotypeName.equals(StereotypeNames.DATA_RULE) ) {
         DataRule dataRule = m_dataRuleBuilder.buildMessageModelObject(classDef, root, objectMap);
         dataRule.setScope(modelObject);
         modelObject.addDataRule(dataRule);
      }
      else {
         // An association that we don't know anything about. We
         // can't handle this.
         rv = false;
      }
      return rv;
   }

   @Override
   protected boolean processAssociation( MessageGroup modelObject, Object assocObject, String stereotypeName,
         Attribute attrib ) {
      return true;
   }
}
