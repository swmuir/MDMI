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

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.model.raw.*;

public class SemanticElementRelationshipBuilder extends ModelBuilderAssociationOnly<SemanticElementRelationship> {
   private static final String s_srcIsAttrib    = "sourceIsInstance";
   private static final String s_targetIsAttrib = "targetIsInstance";
   private static final String s_minAttrib      = "minOccurs";
   private static final String s_maxAttrib      = "maxOccurs";
   private static final String s_relElemName    = "relatedSemanticElement";

   @Override
   protected SemanticElementRelationship buildMessageModelObject( ClassDef classDef, RawRoot root ) {

      SemanticElementRelationship msgElemRel = new SemanticElementRelationship();
      msgElemRel.setName(BuilderUtil.getNameAttribVal(classDef));
      msgElemRel.setDescription(BuilderUtil.getDescriptionAttribVal(classDef));
      msgElemRel.setRule(BuilderUtil.getRuleAttribVal(classDef));
      msgElemRel.setRuleExpressionLanguage(BuilderUtil.getRuleExprAttribVal(classDef));
      msgElemRel.setSourceIsInstance(BuilderUtil.getBooleanAttribVal(classDef, s_srcIsAttrib));
      msgElemRel.setTargetIsInstance(BuilderUtil.getBooleanAttribVal(classDef, s_targetIsAttrib));

      try {
         msgElemRel.setMinOccurs(BuilderUtil.getIntegerAttribVal(classDef, s_minAttrib));
      }
      catch( NumberFormatException ex ) {
         String value = BuilderUtil.getAttribVal(classDef, s_minAttrib);
         Mdmi.INSTANCE.logger().loge(ex,
               SemanticElementRelationshipBuilder.class.getName() + ": Unable to parse '" + s_minAttrib + "' '" + value
                     + "' as integer.");
      }

      try {
         msgElemRel.setMaxOccurs(BuilderUtil.getIntegerAttribVal(classDef, s_maxAttrib));
      }
      catch( NumberFormatException ex ) {
         String value = BuilderUtil.getAttribVal(classDef, s_maxAttrib);
         Mdmi.INSTANCE.logger().loge(ex, 
               SemanticElementRelationshipBuilder.class.getName() + ": Unable to parse '" + s_maxAttrib + "' '" + value
                     + "' as integer.");
      }

      return msgElemRel;
   }

   @Override
   protected boolean processAssociation( SemanticElementRelationship modelObject, Object assocObject,
         String stereotypeName, Attribute attrib ) {

      boolean rv = true;
      if( stereotypeName.equals(StereotypeNames.SEMANTIC_ELEMENT) ) {
         SemanticElement msgElem = (SemanticElement)assocObject;

         if( s_relElemName.equals(attrib.getName()) ) {

            modelObject.setRelatedSemanticElement(msgElem);
         }
      }
      else {
         rv = false;
      }

      return rv;
   }

}
