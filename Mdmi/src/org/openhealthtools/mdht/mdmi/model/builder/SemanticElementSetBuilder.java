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

public class SemanticElementSetBuilder extends ModelBuilderAbstract<SemanticElementSet> {
   private static final SimpleMessageCompositeBuilder      s_simpleBuilder       = new SimpleMessageCompositeBuilder();
   private static final MessageCompositeBuilder            s_msgCompBuilder      = new MessageCompositeBuilder();
   private static final SemanticElementBuilder             s_msgElemBuilder      = new SemanticElementBuilder();
   private static final SemanticElementRelationshipBuilder s_msgElemRelatBuilder = new SemanticElementRelationshipBuilder();

   @Override
   public SemanticElementSet buildMessageModelObject( ClassDef classDef, RawRoot root, Map<String, Object> objectMap ) {
      SemanticElementSet set = super.buildMessageModelObject(classDef, root, objectMap);
      // Fix all links between SemanticElement and SemanticElementRelationship.
      // This is a bit of a hack- it's the only relationship we handle
      // this way. We may want to reconsider the design because of this.
      for( StereotypeInstance stereotype : root.getStereotypeInstances() ) {
         if( StereotypeNames.SEMANTIC_ELEMENT_RELATIONSHIP.equals(stereotype.getName()) ) {
            Object mapObj = objectMap.get(stereotype.getBaseRef());
            if( mapObj != null ) {
               ClassDef relateClass = root.getModel().getClass(stereotype.getBaseRef());
               SemanticElementRelationship relate = (SemanticElementRelationship)mapObj;

               s_msgElemRelatBuilder.processAllAssociations(relate, relateClass, root, objectMap);
            }
         }
      }
      return set;
   }

   @Override
   protected SemanticElementSet buildMessageModelObject( ClassDef classDef, RawRoot root ) {
      SemanticElementSet msgElemSet = new SemanticElementSet();
      msgElemSet.setName(BuilderUtil.getNameAttribVal(classDef));
      msgElemSet.setDescription(BuilderUtil.getDescriptionAttribVal(classDef));
      return msgElemSet;
   }

   @Override
   protected boolean createAssociation( SemanticElementSet modelObject, RawRoot root, ClassDef classDef,
         String stereotypeName, Attribute srcAttrib, Map<String, Object> objectMap ) {
      boolean rv = true;
      if( stereotypeName.equals(StereotypeNames.SIMPLE_MESSAGE_COMPOSITE) ) {
         SimpleMessageComposite simpleComp = s_simpleBuilder.buildMessageModelObject(classDef, root, objectMap);
         simpleComp.setElementSet(modelObject);
         modelObject.addComposite(simpleComp);
      }
      else if( stereotypeName.equals(StereotypeNames.MESSAGE_COMPOSITE) ) {
         MessageComposite msgComp = s_msgCompBuilder.buildMessageModelObject(classDef, root, objectMap);
         msgComp.setElementSet(modelObject);
         modelObject.addComposite(msgComp);
      }
      else if( stereotypeName.equals(StereotypeNames.SEMANTIC_ELEMENT) ) {
         SemanticElement msgElem = s_msgElemBuilder.buildMessageModelObject(classDef, root, objectMap);
         msgElem.setElementSet(modelObject);
         modelObject.addSemanticElement(msgElem);
      }
      else {
         rv = false;
      }
      return rv;
   }

   @Override
   protected boolean processAssociation( SemanticElementSet modelObject, Object assocObject, String stereotypeName,
         Attribute attrib ) {
      boolean rv = true;
      if( stereotypeName.equals(StereotypeNames.MESSAGE_SYNTAX_MODEL) ) {
         MessageSyntaxModel syntaxModel = (MessageSyntaxModel)assocObject;
         syntaxModel.setElementSet(modelObject);
         modelObject.setSyntaxModel(syntaxModel);
      }
      else {
         rv = false;
      }
      return rv;
   }
}
