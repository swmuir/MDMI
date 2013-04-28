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

public class BagBuilder extends ModelBuilderAbstract<Bag> implements IModelBuilder<Bag> {
   private static final BagBuilder             s_bagBuilder       = new BagBuilder();
   private static final ChoiceBuilder          s_setChoiceBuilder = new ChoiceBuilder();
   private static final LeafSyntaxTransBuilder s_leafTransBuilder = new LeafSyntaxTransBuilder();
   private static final NodeBuilder            s_nodeBuilder      = new NodeBuilder();
   private static final String                 s_isOrdAttrib      = "isOrdered";
   private static final String                 s_isUniqueAttrib   = "isUnique";

   @Override
   public Bag buildMessageModelObject( ClassDef classDef, RawRoot root ) {

      Bag bag = new Bag();
      bag.setOrdered(BuilderUtil.getBooleanAttribVal(classDef, s_isOrdAttrib));
      bag.setUnique(BuilderUtil.getBooleanAttribVal(classDef, s_isUniqueAttrib));

      // Init Node fields
      s_nodeBuilder.initMessageModelObject(bag, classDef, root);
      return bag;
   }

   @Override
   protected boolean processAssociation( Bag modelObject, Object assocObject, String stereotypeName, Attribute attrib ) {
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
   protected boolean createAssociation( Bag modelObject, RawRoot root, ClassDef classDef, String stereotypeName,
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
