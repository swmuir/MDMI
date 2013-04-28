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

public class DataRuleBuilder extends ModelBuilderAssociationOnly<DataRule> {

   @Override
   protected DataRule buildMessageModelObject( ClassDef classDef, RawRoot root ) {
      DataRule dataRule = new DataRule();
      dataRule.setName(BuilderUtil.getNameAttribVal(classDef));
      dataRule.setDescription(BuilderUtil.getDescriptionAttribVal(classDef));
      dataRule.setRule(BuilderUtil.getRuleAttribVal(classDef));
      dataRule.setRuleExpressionLanguage(BuilderUtil.getRuleExprAttribVal(classDef));
      return dataRule;
   }

   @Override
   protected boolean processAssociation( DataRule modelObject, Object assocObject, String stereotypeName,
         Attribute attrib ) {
      boolean rv = true;
      if( stereotypeName.equals(StereotypeNames.SEMANTIC_ELEMENT) ) {
         SemanticElement msgElem = (SemanticElement)assocObject;

         msgElem.addDataRule(modelObject);
         modelObject.setSemanticElement(msgElem);
      }
      else {
         rv = false;
      }
      return rv;
   }
}
