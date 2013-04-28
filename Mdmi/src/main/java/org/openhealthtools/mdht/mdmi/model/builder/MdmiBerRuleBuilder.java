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

public class MdmiBerRuleBuilder extends ModelBuilderAttributesOnly<MdmiBusinessElementRule> {
   @Override
   protected MdmiBusinessElementRule buildMessageModelObject( ClassDef classDef, RawRoot root ) {
      MdmiBusinessElementRule rv = new MdmiBusinessElementRule();
      rv.setName(BuilderUtil.getNameAttribVal(classDef));
      rv.setDescription(BuilderUtil.getDescriptionAttribVal(classDef));
      rv.setRule(BuilderUtil.getRuleAttribVal(classDef));
      rv.setRuleExpressionLanguage(BuilderUtil.getRuleExprAttribVal(classDef));
      return rv;
   }
}
