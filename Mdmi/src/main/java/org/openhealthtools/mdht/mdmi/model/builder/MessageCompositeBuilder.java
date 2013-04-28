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

public class MessageCompositeBuilder extends ModelBuilderAbstract<MessageComposite> {
   private static final MessageCompositeBuilder s_msgCompBuilder = new MessageCompositeBuilder();

   @Override
   protected MessageComposite buildMessageModelObject( ClassDef classDef, RawRoot root ) {
      MessageComposite msgComp = new MessageComposite();
      msgComp.setName(BuilderUtil.getNameAttribVal(classDef));
      msgComp.setDescription(BuilderUtil.getDescriptionAttribVal(classDef));
      return msgComp;
   }

   @Override
   protected boolean createAssociation( MessageComposite modelObject, RawRoot root, ClassDef classDef,
         String stereotypeName, Attribute srcAttrib, Map<String, Object> objectMap ) {
      boolean rv = true;
      if( stereotypeName.equals(StereotypeNames.MESSAGE_COMPOSITE) ) {
         MessageComposite msgComp = s_msgCompBuilder.buildMessageModelObject(classDef, root, objectMap);

         msgComp.setOwner(modelObject);
         modelObject.addComposite(msgComp);
      }
      else {
         rv = false;
      }
      return rv;
   }

   @Override
   protected boolean processAssociation( MessageComposite modelObject, Object assocObject, String stereotypeName,
         Attribute attrib ) {
      boolean rv = true;
      if( stereotypeName.equals(StereotypeNames.SEMANTIC_ELEMENT) ) {
         SemanticElement msgElem = (SemanticElement)assocObject;

         msgElem.setComposite(modelObject);
         modelObject.addSemanticElement(msgElem);
      }
      else {
         rv = false;
      }
      return rv;
   }
}
