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
package org.openhealthtools.mdht.mdmi.model.xmi.direct.reader.assoc;

import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.model.validate.*;

public class SemanticElementAssocReader implements IXMIReaderAssoc<SemanticElement> {
   @Override
   public void processAssociation( SemanticElement modelObject, Object assocObject, String srcFieldName ) {

      if( SemanticElementValidate.s_nodeName.equals(srcFieldName) ) {
         Node node = (Node)assocObject;

         node.setSemanticElement(modelObject);
         modelObject.setSyntaxNode(node);
      }
      else if( SemanticElementValidate.s_compositeName.equals(srcFieldName) ) {
         SimpleMessageComposite comp = (SimpleMessageComposite)assocObject;

         comp.addSemanticElement(modelObject);
         modelObject.setComposite(comp);
      }
      else if( SemanticElementValidate.s_dataRulesName.equals(srcFieldName) ) {
         DataRule rule = (DataRule)assocObject;

         rule.setSemanticElement(modelObject);
         modelObject.addDataRule(rule);
      }
      else if( SemanticElementValidate.s_childName.equals(srcFieldName) ) {
         SemanticElement childElem = (SemanticElement)assocObject;

         childElem.setParent(modelObject);
         modelObject.addChild(childElem);
      }
      else if( SemanticElementValidate.s_typeField.equals(srcFieldName) ) {
         modelObject.setDatatype((MdmiDatatype)assocObject);
      }
   }
}
