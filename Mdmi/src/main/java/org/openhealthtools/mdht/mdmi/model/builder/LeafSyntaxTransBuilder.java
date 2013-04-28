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

public class LeafSyntaxTransBuilder extends ModelBuilderAssociationOnly<LeafSyntaxTranslator> {
   private static final NodeBuilder s_nodeBuilder      = new NodeBuilder();
   private static final String      s_formatAttrib     = "format";
   private static final String      s_formatExprAttrib = "formatExpressionLanguage";

   @Override
   protected LeafSyntaxTranslator buildMessageModelObject( ClassDef classDef, RawRoot root ) {

      LeafSyntaxTranslator leafSynTrans = new LeafSyntaxTranslator();
      leafSynTrans.setFormat(BuilderUtil.getAttribVal(classDef, s_formatAttrib));
      leafSynTrans.setFormatExpressionLanguage(BuilderUtil.getAttribVal(classDef, s_formatExprAttrib));

      // Init Node fields
      s_nodeBuilder.initMessageModelObject(leafSynTrans, classDef, root);

      return leafSynTrans;
   }

   @Override
   protected boolean processAssociation( LeafSyntaxTranslator modelObject, Object assocObject, String stereotypeName,
         Attribute attrib ) {

      boolean rv = true;
      if( stereotypeName.equals(StereotypeNames.SEMANTIC_ELEMENT_SET) ) {
         s_nodeBuilder.processMsgElemAssociation(modelObject, (SemanticElement)assocObject);
      }
      else {
         rv = false;
      }

      return rv;
   }
}
