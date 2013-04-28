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
package org.openhealthtools.mdht.mdmi.model.validate;

import org.openhealthtools.mdht.mdmi.model.*;

public class SemanticElemRelateValidate implements IModelValidate<SemanticElementRelationship> {
   public static final String s_nameField          = "name";
   public static final String s_ruleField          = "rule";
   public static final String s_relateSemElem      = "relatedSemanticElement";
   public static final String s_descName           = "description";
   public static final String s_ruleLangName       = "ruleExpressionLanguage";
   public static final String s_minOccursName      = "minOccurs";
   public static final String s_maxOccursName      = "maxOccurs";
   public static final String s_srcInstanceName    = "sourceIsInstance";
   public static final String s_targetInstanceName = "targetIsInstance";

   @Override
   public void validate( SemanticElementRelationship object, ModelValidationResults results ) {

      if( ValidateHelper.isEmptyField(object.getName()) ) {
         results.addErrorFromRes(object, s_nameField);
      }
      if( ValidateHelper.isEmptyField(object.getRule()) ) {
         results.addErrorFromRes(object, s_ruleField, object.getName());
      }
      if( object.getRelatedSemanticElement() == null ) {
         results.addErrorFromRes(object, s_relateSemElem, object.getName());
      }
   }
}
