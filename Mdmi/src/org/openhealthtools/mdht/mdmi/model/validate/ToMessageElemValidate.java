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

public class ToMessageElemValidate extends ConversionRuleValidate<ToMessageElement> {
   public static final String s_busElemField = "businessElement";

   public void validate( ToMessageElement object, ModelValidationResults results ) {
      super.validate(object, results);

      if( object.getBusinessElement() == null ) {
         results.addErrorFromRes(object, s_busElemField, object.getRule());
      }
   }
}
