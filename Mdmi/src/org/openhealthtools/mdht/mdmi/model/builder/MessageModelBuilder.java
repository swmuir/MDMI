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

public class MessageModelBuilder extends ModelBuilderCompositionOnly<MessageModel> implements IModelBuilder<MessageModel> {
   private static final String                    s_modelNameAttrib   = "messageModelName";
   private static final String                    s_srcAttribName     = "source";
   private static final MessageSyntaxModelBuilder m_msgSyntaxBuilder  = new MessageSyntaxModelBuilder();
   private static final SemanticElementSetBuilder m_msgElemSetBuilder = new SemanticElementSetBuilder();

   @Override
   public MessageModel buildMessageModelObject( ClassDef classDef, RawRoot root ) {
      // Set attributes
      MessageModel rv = new MessageModel();
      rv.setMessageModelName(BuilderUtil.getAttribVal(classDef, s_modelNameAttrib));
      rv.setDescription(BuilderUtil.getDescriptionAttribVal(classDef));
      rv.setSource(BuilderUtil.getURIAttribVal(classDef, s_srcAttribName));
      return rv;
   }

   @Override
   protected boolean createAssociation( MessageModel modelObject, RawRoot root, ClassDef classDef,
         String stereotypeName, Attribute srcAttrib, Map<String, Object> objectMap ) {
      boolean rv = true;
      if( stereotypeName.equals(StereotypeNames.MESSAGE_SYNTAX_MODEL) ) {
         // We need to create this. Get the class def and then create.
         MessageSyntaxModel syntaxModel = m_msgSyntaxBuilder.buildMessageModelObject(classDef, root, objectMap);
         // Set properties
         syntaxModel.setModel(modelObject);
         modelObject.setSyntaxModel(syntaxModel);
      }
      else if( stereotypeName.equals(StereotypeNames.SEMANTIC_ELEMENT_SET) ) {
         // We need to create this. Get the class def and then create.
         SemanticElementSet msgElemSet = m_msgElemSetBuilder.buildMessageModelObject(classDef, root, objectMap);
         // Set properties.
         msgElemSet.setModel(modelObject);
         modelObject.setElementSet(msgElemSet);
      }
      else {
         // Bad association.
         rv = false;
      }
      return rv;
   }
}
