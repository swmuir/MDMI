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

public class MdmiBerBuilder extends ModelBuilderCompositionOnly<MdmiBusinessElementReference> {
   private static final String             s_idAttrib       = "uniqueIdentifier";
   private static final String             s_refAttrib      = "reference";
   private static final String             s_datatypeAttrib = "referenceDatatype";
   private static final MdmiBerRuleBuilder m_ruleBuilder    = new MdmiBerRuleBuilder();

   @Override
   public MdmiBusinessElementReference buildMessageModelObject( ClassDef classDef, RawRoot root,
         Map<String, Object> objectMap ) {

      MdmiBusinessElementReference ref = super.buildMessageModelObject(classDef, root, objectMap);

      // Get datatype attribute
      Attribute attrib = classDef.getAttribute(s_datatypeAttrib);
      if( attrib != null ) {
         MdmiDatatype datatype = (MdmiDatatype)objectMap.get(attrib.getType());
         if( datatype != null ) {
            ref.setReferenceDatatype(datatype);
         }
      }
      objectMap.put(classDef.getId(), ref);
      return ref;
   }

   @Override
   protected MdmiBusinessElementReference buildMessageModelObject( ClassDef classDef, RawRoot root ) {
      MdmiBusinessElementReference ref = new MdmiBusinessElementReference();
      ref.setName(BuilderUtil.getNameAttribVal(classDef));
      ref.setDescription(BuilderUtil.getDescriptionAttribVal(classDef));
      ref.setUniqueIdentifier(BuilderUtil.getAttribVal(classDef, s_idAttrib));
      ref.setReference(BuilderUtil.getURIAttribVal(classDef, s_refAttrib));
      return ref;
   }

   @Override
   protected boolean createAssociation( MdmiBusinessElementReference modelObject, RawRoot root, ClassDef classDef,
         String stereotypeName, Attribute srcAttrib, Map<String, Object> objectMap ) {
      boolean rv = true;
      if( stereotypeName.equals(StereotypeNames.MDMI_BUS_ELEM_RULE) ) {
         MdmiBusinessElementRule rule = m_ruleBuilder.buildMessageModelObject(classDef, root, objectMap);

         rule.setBusinessElement(modelObject);
         modelObject.addBusinessRule(rule);
      }
      else {
         rv = false;
      }
      return rv;
   }
}
