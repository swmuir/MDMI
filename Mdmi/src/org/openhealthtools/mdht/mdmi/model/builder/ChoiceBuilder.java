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

public class ChoiceBuilder extends ModelBuilderAbstract<Choice> {
   private static final BagBuilder             s_bagBuilder           = new BagBuilder();
   private static final ChoiceBuilder          s_setChoiceBuilder     = new ChoiceBuilder();
   private static final LeafSyntaxTransBuilder s_leafTransBuilder     = new LeafSyntaxTransBuilder();
   private static final NodeBuilder            s_nodeBuilder          = new NodeBuilder();
   private static final String                 s_constraintAttrib     = "constraint";
   private static final String                 s_constraintExprAttrib = "constraintExpressionLanguage";

   @Override
   protected Choice buildMessageModelObject( ClassDef classDef, RawRoot root ) {

      Choice choice = new Choice();
      choice.setConstraint(BuilderUtil.getAttribVal(classDef, s_constraintAttrib));
      choice.setConstraintExpressionLanguage(BuilderUtil.getAttribVal(classDef, s_constraintExprAttrib));

      // Init Node fields
      s_nodeBuilder.initMessageModelObject(choice, classDef, root);
      return choice;
   }

   @Override
   protected boolean processAssociation( Choice modelObject, Object assocObject, String stereotypeName, Attribute attrib ) {
      boolean rv = true;
      if( stereotypeName.equals(StereotypeNames.SEMANTIC_ELEMENT_SET) ) {
         s_nodeBuilder.processMsgElemAssociation(modelObject, (SemanticElement)assocObject);
      }
      else {
         rv = false;
      }
      return rv;
   }

   @Override
   protected boolean createAssociation( Choice modelObject, RawRoot root, ClassDef classDef, String stereotypeName,
         Attribute srcAttrib, Map<String, Object> objectMap ) {
      boolean rv = true;
      if( stereotypeName.equals(StereotypeNames.BAG)
            && !BuilderUtil.s_aggregationNone.equalsIgnoreCase(srcAttrib.getAggregationType()) ) {

         Bag bag = s_bagBuilder.buildMessageModelObject(classDef, root, objectMap);

         bag.setParentNode(modelObject);
         modelObject.addNode(bag);
      }
      if( stereotypeName.equals(StereotypeNames.CHOICE)
            && !BuilderUtil.s_aggregationNone.equalsIgnoreCase(srcAttrib.getAggregationType()) ) {

         Choice choice = s_setChoiceBuilder.buildMessageModelObject(classDef, root, objectMap);

         choice.setParentNode(modelObject);
         modelObject.addNode(choice);
      }
      else if( stereotypeName.equals(StereotypeNames.LEAF_SYNTAX_TRANSLATOR) ) {
         // These can be shared with message elements. Make sure
         // this one doesn't already exist.
         LeafSyntaxTranslator leafTrans = s_leafTransBuilder.buildMessageModelObject(classDef, root, objectMap);

         leafTrans.setParentNode(modelObject);
         modelObject.addNode(leafTrans);
      }
      else {
         rv = false;
      }
      return rv;
   }
}
