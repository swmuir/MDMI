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

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.model.enums.*;
import org.openhealthtools.mdht.mdmi.model.raw.*;

public class SemanticElementBuilder extends ModelBuilderAbstract<SemanticElement> {

   private static final String                             s_elemNameAttrib        = "elementName";
   private static final String                             s_elemTypeAttrib        = "elementType";
   private static final String                             s_multInstAttrib        = "multipleInstances";
   private static final String                             s_orderingAttrib        = "ordering";
   private static final String                             s_orderingLangAttrib    = "orderingLanguage";
   private static final String                             s_enumValueSetField     = "enumValueSetField";
   private static final String                             s_enumValueField        = "enumValueField";
   private static final String                             s_enumValueDescrField   = "enumValueDescrField";
   private static final String                             s_enumValueSet          = "enumValueSet";
   private static final String                             s_computedAttrib        = "computedValue";
   private static final String                             s_computedInAttrib      = "computedInValue";
   private static final String                             s_computedOutAttrib     = "computedOutValue";
   private static final String                             s_childrenAttrib        = "children";
   private static final String                             s_datatypeAttrib        = "datatype";
   private static final String                             s_propQualAttrib        = "propertyQualifier";
   private static final SemanticElementRelationshipBuilder s_msgElemRelBuilder     = new SemanticElementRelationshipBuilder();
   private static final SemanticElementBusinessRuleBuilder s_msgElemBusRuleBuilder = new SemanticElementBusinessRuleBuilder();
   private static final ToMessageElementBuilder            s_toMsgElemBuilder      = new ToMessageElementBuilder();
   private static final ToBusinessElementBuilder           s_toBusElemBuilder      = new ToBusinessElementBuilder();
   private static final MdmiExpressionBuilder              s_exprBuilder           = new MdmiExpressionBuilder();
   private static final SemanticElementBuilder             s_elemBuilder           = new SemanticElementBuilder();

   @Override
   public SemanticElement buildMessageModelObject( ClassDef classDef, RawRoot root, Map<String, Object> objectMap ) {
      SemanticElement elem = super.buildMessageModelObject(classDef, root, objectMap);
      // Get datatype attribute
      Attribute attrib = classDef.getAttribute(s_datatypeAttrib);
      if( attrib != null ) {
         MdmiDatatype datatype = (MdmiDatatype)objectMap.get(attrib.getType());
         if( datatype != null ) {
            elem.setDatatype(datatype);
         }
      }

      // Handle MEPropertyQualifier.
      // TODO - This implementation will only work if cardinality is 0..1.
      // However, the spec indicates 1..*. Will need to see how that will
      // be modeled.
      attrib = classDef.getAttribute(s_propQualAttrib);
      if( attrib != null ) {
         MEPropertyQualifier qual = (MEPropertyQualifier)objectMap.get(attrib.getType());
         if( qual != null ) {
            // Dereference the default value instance
            ClassDef meClassDef = root.getModel().getClass(attrib.getType());
            if( meClassDef != null && meClassDef.getLiteral(attrib.getDefaultValueInstance()) != null ) {
               // We found an instance. Add it to the msg element
               MEPropertyQualifierInstance qualInstance = new MEPropertyQualifierInstance();
               qualInstance.setQualifier(qual);
               qualInstance.setValue(meClassDef.getLiteral(attrib.getDefaultValueInstance()).getName());
               elem.addPropertyQualifier(qualInstance);
            }
         }
      }
      objectMap.put(classDef.getId(), elem);
      return elem;
   }

   @Override
   protected SemanticElement buildMessageModelObject( ClassDef classDef, RawRoot root ) {
      SemanticElement msgElem = new SemanticElement();
      msgElem.setName(BuilderUtil.getAttribVal(classDef, s_elemNameAttrib));
      msgElem.setDescription(BuilderUtil.getDescriptionAttribVal(classDef));
      msgElem.setMultipleInstances(BuilderUtil.getBooleanAttribVal(classDef, s_multInstAttrib));
      msgElem.setOrdering(BuilderUtil.getAttribVal(classDef, s_orderingAttrib));
      msgElem.setOrderingLanguage(BuilderUtil.getAttribVal(classDef, s_orderingLangAttrib));
      msgElem.setEnumValueSetField(BuilderUtil.getAttribVal(classDef, s_enumValueSetField));
      msgElem.setEnumValueField(BuilderUtil.getAttribVal(classDef, s_enumValueField));
      msgElem.setEnumValueDescrField(BuilderUtil.getAttribVal(classDef, s_enumValueDescrField));
      msgElem.setEnumValueSet(BuilderUtil.getAttribVal(classDef, s_enumValueSet));

      String value = BuilderUtil.getAttribVal(classDef, s_elemTypeAttrib);
      if( value != null ) {
         try {
            msgElem.setSemanticElementType(SemanticElementType.valueOf(value));
         }
         catch( IllegalArgumentException ex ) {
            System.out.println("Unable to parse SemanticElementType '" + value + "'.");
            Mdmi.INSTANCE.logger().loge(ex, SemanticElementBuilder.class.getName() + ": Unable to parse SemanticElementType '"
                  + value + "'.");
         }
      }
      return msgElem;
   }

   @Override
   protected boolean createAssociation( SemanticElement modelObject, RawRoot root, ClassDef classDef,
         String stereotypeName, Attribute srcAttrib, Map<String, Object> objectMap ) {
      boolean rv = true;
      if( stereotypeName.equals(StereotypeNames.SEMANTIC_ELEMENT) && s_childrenAttrib.equals(srcAttrib.getName()) ) {
         SemanticElement child = s_elemBuilder.buildMessageModelObject(classDef, root, objectMap);
         modelObject.addChild(child);
      }
      else if( stereotypeName.equals(StereotypeNames.SEMANTIC_ELEMENT_RELATIONSHIP) ) {
         SemanticElementRelationship msgElemRel = s_msgElemRelBuilder
               .buildMessageModelObject(classDef, root, objectMap);
         modelObject.addRelationship(msgElemRel);
         msgElemRel.setContext(modelObject);
      }
      else if( stereotypeName.equals(StereotypeNames.SEMANTIC_ELEM_BUSINESS_RULE) ) {
         SemanticElementBusinessRule msgElemBusRule = s_msgElemBusRuleBuilder.buildMessageModelObject(classDef, root,
               objectMap);
         msgElemBusRule.setSemanticElement(modelObject);
         modelObject.addBusinessRule(msgElemBusRule);
      }
      else if( stereotypeName.equals(StereotypeNames.TO_BUSINESS_ELEMENT) ) {
         ToBusinessElement busElem = s_toBusElemBuilder.buildMessageModelObject(classDef, root, objectMap);
         busElem.setOwner(modelObject);
         modelObject.addFromMdmi(busElem);
      }
      else if( stereotypeName.equals(StereotypeNames.TO_MESSAGE_ELEMENT) ) {
         ToMessageElement msgElem = s_toMsgElemBuilder.buildMessageModelObject(classDef, root, objectMap);
         msgElem.setOwner(modelObject);
         modelObject.addToMdmi(msgElem);
      }
      else if( stereotypeName.equals(StereotypeNames.MDMI_EXPRESSION) ) {
         MdmiExpression expr = s_exprBuilder.buildMessageModelObject(classDef, root, objectMap);
         if( s_computedAttrib.equals(srcAttrib.getName()) ) {
            modelObject.setComputedValue(expr);
         }
         else if( s_computedInAttrib.equals(srcAttrib.getName()) ) {
            modelObject.setComputedInValue(expr);
         }
         else if( s_computedOutAttrib.equals(srcAttrib.getName()) ) {
            modelObject.setComputedOutValue(expr);
         }
      }
      else {
         rv = false;
      }
      return rv;
   }

   @Override
   protected boolean processAssociation( SemanticElement modelObject, Object assocObject, String stereotypeName,
         Attribute attrib ) {
      boolean rv = true;
      if( stereotypeName.equals(StereotypeNames.LEAF_SYNTAX_TRANSLATOR) || stereotypeName.equals(StereotypeNames.BAG)
            || stereotypeName.equals(StereotypeNames.CHOICE) ) {
         Node node = (Node)assocObject;
         node.setSemanticElement(modelObject);
         modelObject.setSyntaxNode(node);
      }
      else if( stereotypeName.equals(StereotypeNames.SIMPLE_MESSAGE_COMPOSITE)
            || stereotypeName.equals(StereotypeNames.MESSAGE_COMPOSITE) ) {
         SimpleMessageComposite msgComp = (SimpleMessageComposite)assocObject;
         msgComp.addSemanticElement(modelObject);
         modelObject.setComposite(msgComp);
      }
      else if( stereotypeName.equals(StereotypeNames.DATA_RULE) ) {
         DataRule dataRule = (DataRule)assocObject;

         dataRule.setSemanticElement(modelObject);
         modelObject.addDataRule(dataRule);
      }
      else {
         rv = false;
      }
      return rv;
   }
}
