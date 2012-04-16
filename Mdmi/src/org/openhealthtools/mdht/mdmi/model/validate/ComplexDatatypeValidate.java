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

public class ComplexDatatypeValidate<T extends DTComplex> extends MdmiDatatypeValidate<T> {
   public static final String s_nameField  = "name";
   public static final String s_fieldsName = "fields";     // TODO - add validation for this
   public static final String s_descName   = "description";
   public static final String s_minName    = "minOccurs";
   public static final String s_maxName    = "maxOccurs";
   public static final String s_typeName   = "datatype";

   public void validate( T object, ModelValidationResults results ) {
      super.validate(object, results);
      for( Field field : object.getFields() ) {
         if( ValidateHelper.isEmptyField(field.getName()) ) {
            results.addErrorFromRes(field, s_nameField, object.getTypeName());
         }
      }
   }
}
