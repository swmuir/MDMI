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
package org.openhealthtools.mdht.mdmi.model.builder;

import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.model.raw.*;

public class ToBusinessElementBuilder extends ModelBuilderAssociationOnly<ToBusinessElement> {
   private static final ConversionRuleBuilder s_conRuleBuilder = new ConversionRuleBuilder();
   @Override
   protected ToBusinessElement buildMessageModelObject( ClassDef classDef, RawRoot root ) {
      ToBusinessElement elem = new ToBusinessElement();
      s_conRuleBuilder.initMessageModelObject(elem, classDef, root);
      return elem;
   }

   @Override
   protected boolean processAssociation( ToBusinessElement modelObject, Object assocObject, String stereotypeName,
         Attribute attrib ) {
      boolean rv = true;
      if( stereotypeName.equals(StereotypeNames.MDMI_BUS_ELEM_REF) ) {
         MdmiBusinessElementReference ref = (MdmiBusinessElementReference)assocObject;
         modelObject.setBusinessElement(ref);
      }
      else {
         rv = false;
      }
      return rv;
   }
}
