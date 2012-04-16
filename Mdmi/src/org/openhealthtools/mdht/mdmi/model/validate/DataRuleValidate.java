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

public class DataRuleValidate implements IModelValidate<DataRule> {
   public static final String s_nameField    = "name";
   public static final String s_ruleField    = "rule";
   public static final String s_scopeField   = "scope";
   public static final String s_semElemField = "semanticElement";
   public static final String s_typesField   = "datatypes";
   public static final String s_descName     = "description";
   public static final String s_ruleExprName = "ruleExpressionLanguage";

   @Override
   public void validate( DataRule object, ModelValidationResults results ) {
      if( ValidateHelper.isEmptyField(object.getName()) ) {
         results.addErrorFromRes(object, s_nameField);
      }
      if( ValidateHelper.isEmptyField(object.getRule()) ) {
         results.addErrorFromRes(object, s_ruleField, object.getName());
      }
      if( object.getScope() == null ) {
         results.addErrorFromRes(object, s_scopeField, object.getName());
      }
      if( object.getSemanticElement() == null ) {
         results.addErrorFromRes(object, s_semElemField, object.getName());
      }
      if( object.getDatatypes().size() == 0 ) {
         results.addErrorFromRes(object, s_typesField, object.getName());
      }
   }
}
