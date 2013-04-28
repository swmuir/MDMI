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

public class SemanticElementSetValidate implements IModelValidate<SemanticElementSet> {
   public static final String s_nameField        = "name";
   public static final String s_modelField       = "model";
   public static final String s_syntaxModelField = "syntaxModel";
   public static final String s_semElemsField    = "semanticElements";
   public static final String s_descName         = "description";
   public static final String s_compName         = "composites";

   @Override
   public void validate( SemanticElementSet object, ModelValidationResults results ) {
      if( ValidateHelper.isEmptyField(object.getName()) ) {
         results.addErrorFromRes(object, s_nameField);
      }
      if( object.getModel() == null ) {
         results.addErrorFromRes(object, s_modelField, object.getName());
      }
      if( object.getSyntaxModel() == null ) {
         results.addErrorFromRes(object, s_syntaxModelField, object.getName());
      }
      if( object.getSemanticElements().size() == 0 ) {
         results.addErrorFromRes(object, s_semElemsField, object.getName());
      }
   }
}
