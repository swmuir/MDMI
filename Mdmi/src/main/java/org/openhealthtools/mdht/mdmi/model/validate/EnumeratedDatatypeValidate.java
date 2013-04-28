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

public class EnumeratedDatatypeValidate extends MdmiDatatypeValidate<DTSEnumerated> {
   public static final String s_nameField    = "name";
   public static final String s_codeField    = "code";
   public static final String s_descName     = "description";
   public static final String s_literalsName = "literals";   // TODO- add validation for this

   @Override
   public void validate( DTSEnumerated object, ModelValidationResults results ) {
      super.validate(object, results);

      for( EnumerationLiteral literal : object.getLiterals() ) {
         if( ValidateHelper.isEmptyField(literal.getName()) ) {
            results.addErrorFromRes(literal, s_nameField, object.getTypeName());
         }
         if( ValidateHelper.isEmptyField(literal.getCode()) ) {
            results.addErrorFromRes(literal, s_codeField, object.getTypeName());
         }
      }
   }
}
