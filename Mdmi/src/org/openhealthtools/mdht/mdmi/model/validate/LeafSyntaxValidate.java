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

public class LeafSyntaxValidate extends NodeValidate<LeafSyntaxTranslator> {
   public static final String s_formatField    = "format";
   public static final String s_formatLangName = "formatExpressionLanguage";

   @Override
   public void validate( LeafSyntaxTranslator object, ModelValidationResults results ) {

      super.validate(object, results);

      if( ValidateHelper.isEmptyField(object.getFormat()) ) {
         results.addErrorFromRes(object, s_formatField, object.getName());
      }
   }
}