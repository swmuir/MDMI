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

public class NodeBuilder implements IModelInitializer<Node> {
   private static final String s_descAttrib      = "description";
   private static final String s_locAttrib       = "location";
   private static final String s_locExprAttrib   = "locationExpressionLanguage";
   private static final String s_minOccursAttrib = "minOccurs";
   private static final String s_maxOccursAttrib = "maxOccurs";
   private static final String s_fieldNameAttrib = "fieldName";

   @Override
   public void initMessageModelObject( Node modelObject, ClassDef classDef, RawRoot root ) {
      // Init all attributes
      modelObject.setName(BuilderUtil.getNameAttribVal(classDef));
      modelObject.setDescription(BuilderUtil.getAttribVal(classDef, s_descAttrib));
      modelObject.setLocation(BuilderUtil.getAttribVal(classDef, s_locAttrib));
      modelObject.setLocationExpressionLanguage(BuilderUtil.getAttribVal(classDef, s_locExprAttrib));
      modelObject.setFieldName(BuilderUtil.getAttribVal(classDef, s_fieldNameAttrib));

      try {
         modelObject.setMinOccurs(BuilderUtil.getIntegerAttribVal(classDef, s_minOccursAttrib));
      }
      catch( NumberFormatException exc ) {
         String value = BuilderUtil.getAttribVal(classDef, s_minOccursAttrib);
         Mdmi.INSTANCE.logger().warning(
               NodeBuilder.class.getName() + ": Unable to parse '" + s_minOccursAttrib + "' '" + value
                     + "' as integer.");
      }

      try {
         modelObject.setMaxOccurs(BuilderUtil.getIntegerAttribVal(classDef, s_maxOccursAttrib));
      }
      catch( NumberFormatException exc ) {
         String value = BuilderUtil.getAttribVal(classDef, s_maxOccursAttrib);
         Mdmi.INSTANCE.logger().warning(
               NodeBuilder.class.getName() + ": Unable to parse '" + s_maxOccursAttrib + "' '" + value
                     + "' as integer.");
      }
   }

   protected void processMsgElemAssociation( Node modelObject, SemanticElement msgElem ) {
      msgElem.setSyntaxNode(modelObject);
      modelObject.setSemanticElement(msgElem);
   }
}
