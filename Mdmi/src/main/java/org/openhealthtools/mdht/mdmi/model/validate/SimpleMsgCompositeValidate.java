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

public class SimpleMsgCompositeValidate implements IModelValidate<SimpleMessageComposite> {
   public static final String s_nameField    = "name";
   public static final String s_elemSetField = "elementSet";
   public static final String s_semElemField = "semanticElements";
   public static final String s_descName     = "description";

   @Override
   public void validate( SimpleMessageComposite object, ModelValidationResults results ) {
      if( ValidateHelper.isEmptyField(object.getName()) ) {
         results.addErrorFromRes(SimpleMessageComposite.class.getSimpleName(), object, s_nameField);
      }
      if( object.getElementSet() == null ) {
         results
               .addErrorFromRes(SimpleMessageComposite.class.getSimpleName(), object, s_elemSetField, object.getName());
      }
      if( object.getSemanticElements().size() == 0 ) {
         results
               .addErrorFromRes(SimpleMessageComposite.class.getSimpleName(), object, s_semElemField, object.getName());
      }
   }
}
