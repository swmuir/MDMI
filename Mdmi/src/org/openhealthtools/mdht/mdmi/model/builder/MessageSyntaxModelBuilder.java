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

public class MessageSyntaxModelBuilder extends ModelBuilderAbstract<MessageSyntaxModel> implements IModelBuilder<MessageSyntaxModel> {
   private static final BagBuilder             s_setBuilder       = new BagBuilder();
   private static final ChoiceBuilder          s_setChoiceBuilder = new ChoiceBuilder();
   private static final LeafSyntaxTransBuilder s_leafTransBuilder = new LeafSyntaxTransBuilder();

   @Override
   public MessageSyntaxModel buildMessageModelObject( ClassDef classDef, RawRoot root ) {
      // Set attributes
      MessageSyntaxModel syntModel = new MessageSyntaxModel();
      syntModel.setName(BuilderUtil.getNameAttribVal(classDef));
      syntModel.setDescription(BuilderUtil.getDescriptionAttribVal(classDef));
      return syntModel;
   }

   @Override
   protected boolean createAssociation( MessageSyntaxModel modelObject, RawRoot root, ClassDef classDef,
         String stereotypeName, Attribute srcAttrib, Map<String, Object> objectMap ) {
      boolean rv = true;
      if( stereotypeName.equals(StereotypeNames.BAG) ) {
         Bag bag = s_setBuilder.buildMessageModelObject(classDef, root, objectMap);
         bag.setSyntaxModel(modelObject);
         modelObject.setRoot(bag);
      }
      else if( stereotypeName.equals(StereotypeNames.CHOICE) ) {
         Choice choice = s_setChoiceBuilder.buildMessageModelObject(classDef, root, objectMap);
         choice.setSyntaxModel(modelObject);
         modelObject.setRoot(choice);
      }
      else if( stereotypeName.equals(StereotypeNames.LEAF_SYNTAX_TRANSLATOR) ) {
         LeafSyntaxTranslator leafTrans = s_leafTransBuilder.buildMessageModelObject(classDef, root, objectMap);
         leafTrans.setSyntaxModel(modelObject);
         modelObject.setRoot(leafTrans);
      }
      else {
         rv = false;
      }
      return rv;
   }

   @Override
   protected boolean processAssociation( MessageSyntaxModel modelObject, Object assocObject, String stereotypeName,
         Attribute attrib ) {
      boolean rv = true;
      if( stereotypeName.equals(StereotypeNames.SEMANTIC_ELEMENT_SET) ) {
         SemanticElementSet elemSet = (SemanticElementSet)assocObject;
         elemSet.setSyntaxModel(modelObject);
         modelObject.setElementSet(elemSet);
      }
      else {
         rv = false;
      }
      return rv;
   }

}
