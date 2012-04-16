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

import java.util.*;

import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.model.raw.*;

public class MdmiDictionaryBuilder extends ModelBuilderCompositionOnly<MdmiDomainDictionaryReference> {
   private static final String         s_refAttrib         = "reference";
   private static final MdmiBerBuilder s_busElemRefBuilder = new MdmiBerBuilder();

   @Override
   protected MdmiDomainDictionaryReference buildMessageModelObject( ClassDef classDef, RawRoot root ) {
      MdmiDomainDictionaryReference dictRef = new MdmiDomainDictionaryReference();
      dictRef.setName(BuilderUtil.getNameAttribVal(classDef));
      dictRef.setDescription(BuilderUtil.getDescriptionAttribVal(classDef));
      dictRef.setReference(BuilderUtil.getURIAttribVal(classDef, s_refAttrib));
      return dictRef;
   }

   @Override
   protected boolean createAssociation( MdmiDomainDictionaryReference modelObject, RawRoot root, ClassDef classDef,
         String stereotypeName, Attribute srcAttrib, Map<String, Object> objectMap ) {
      boolean rv = true;
      if( stereotypeName.equals(StereotypeNames.MDMI_BUS_ELEM_REF) ) {
         MdmiBusinessElementReference ref = s_busElemRefBuilder.buildMessageModelObject(classDef, root, objectMap);

         ref.setDomainDictionaryReference(modelObject);
         modelObject.addBusinessElement(ref);
      }
      else {
         rv = false;
      }
      return rv;
   }
}
