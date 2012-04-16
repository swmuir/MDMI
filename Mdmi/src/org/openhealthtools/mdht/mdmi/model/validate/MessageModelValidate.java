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

public class MessageModelValidate implements IModelValidate<MessageModel> {
   public static final String s_nameField    = "messageModelName";
   public static final String s_groupField   = "group";
   public static final String s_syntaxField  = "syntaxModel";
   public static final String s_elemSetField = "elementSet";
   public static final String s_descName     = "description";
   public static final String s_sourceName   = "source";

   @Override
   public void validate( MessageModel object, ModelValidationResults results ) {
      if( ValidateHelper.isEmptyField(object.getMessageModelName()) ) {
         results.addErrorFromRes(object, s_nameField);
      }
      if( object.getGroup() == null ) {
         results.addErrorFromRes(object, s_groupField, object.getMessageModelName());
      }
      if( object.getSyntaxModel() == null ) {
         results.addErrorFromRes(object, s_syntaxField, object.getMessageModelName());
      }
      if( object.getElementSet() == null ) {
         results.addErrorFromRes(object, s_elemSetField, object.getMessageModelName());
      }
   }
}
