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

public class DerivedDatatypeValidate extends MdmiDatatypeValidate<DTSDerived> {
   public static final String s_restrictField = "restriction";
   public static final String s_baseTypeField = "baseType";

   @Override
   public void validate( DTSDerived object, ModelValidationResults results ) {
      super.validate(object, results);

      if( ValidateHelper.isEmptyField(object.getRestriction()) ) {
         results.addErrorFromRes(object, s_restrictField, object.getTypeName());
      }
      if( object.getBaseType() == null ) {
         results.addErrorFromRes(object, s_baseTypeField, object.getTypeName());
      }
   }
}
