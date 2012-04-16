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

public class DomainDictionaryRefValidate implements IModelValidate<MdmiDomainDictionaryReference> {
   public static final String s_nameField    = "name";
   public static final String s_refField     = "reference";
   public static final String s_groupField   = "messageGroup";
   public static final String s_busElemField = "businessElements";
   public static final String s_descName     = "description";

   @Override
   public void validate( MdmiDomainDictionaryReference object, ModelValidationResults results ) {

      if( ValidateHelper.isEmptyField(object.getName()) ) {
         results.addErrorFromRes(object, s_nameField);
      }
      if( object.getReference() == null ) {
         results.addErrorFromRes(object, s_refField, object.getName());
      }
      if( object.getMessageGroup() == null ) {
         results.addErrorFromRes(object, s_groupField, object.getName());
      }
      if( object.getBusinessElements().size() == 0 ) {
         results.addErrorFromRes(object, s_busElemField, object.getName());
      }
   }
}
