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

public class MessageGroupValidate implements IModelValidate<MessageGroup> {
   public static final String s_nameField       = "name";
   public static final String s_defConstrField  = "defaultConstraintExprLang";
   public static final String s_defLocField     = "defaultLocationExprLang";
   public static final String s_defRuleField    = "defaultRuleExprLang";
   public static final String s_modelsField     = "models";
   public static final String s_domainDictField = "domainDictionary";
   public static final String s_dataRulesName   = "dataRules";
   public static final String s_typesName       = "datatypes";
   public static final String s_descField       = "description";

   @Override
   public void validate( MessageGroup object, ModelValidationResults results ) {
      if( ValidateHelper.isEmptyField(object.getName()) ) {
         results.addErrorFromRes(object, s_nameField);
      }

      if( ValidateHelper.isEmptyField(object.getDefaultConstraintExprLang()) ) {
         results.addErrorFromRes(object, s_defConstrField, object.getName());
      }

      if( ValidateHelper.isEmptyField(object.getDefaultLocationExprLang()) ) {
         results.addErrorFromRes(object, s_defLocField, object.getName());
      }

      if( ValidateHelper.isEmptyField(object.getDefaultRuleExprLang()) ) {
         results.addErrorFromRes(object, s_defRuleField, object.getName());
      }

      if( object.getModels().size() == 0 ) {
         results.addErrorFromRes(object, s_modelsField, object.getName());
      }

      if( object.getDomainDictionary() == null ) {
         results.addErrorFromRes(object, s_domainDictField, object.getName());
      }
   }
}
