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

public class MessageSyntaxModelValidate implements IModelValidate<MessageSyntaxModel> {
   public static final String s_nameField    = "name";
   public static final String s_modelField   = "model";
   public static final String s_elemSetField = "elementSet";
   public static final String s_rootField    = "root";
   public static final String s_descName     = "description";

   @Override
   public void validate( MessageSyntaxModel object, ModelValidationResults results ) {
      if( ValidateHelper.isEmptyField(object.getName()) ) {
         results.addErrorFromRes(object, s_nameField);
      }
      if( object.getModel() == null ) {
         results.addErrorFromRes(object, s_modelField, object.getName());
      }
      if( object.getElementSet() == null ) {
         results.addErrorFromRes(object, s_elemSetField, object.getName());
      }
      if( object.getRoot() == null ) {
         results.addErrorFromRes(object, s_rootField, object.getName());
      }
   }
}
