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

public class ConversionRuleBuilder implements IModelInitializer<ConversionRule> {

   @Override
   public void initMessageModelObject( ConversionRule modelObject, ClassDef classDef, RawRoot root ) {

      modelObject.setName(BuilderUtil.getNameAttribVal(classDef));
      modelObject.setDescription(BuilderUtil.getDescriptionAttribVal(classDef));
      modelObject.setRule(BuilderUtil.getRuleAttribVal(classDef));
      modelObject.setRuleExpressionLanguage(BuilderUtil.getRuleExprAttribVal(classDef));
   }
}
