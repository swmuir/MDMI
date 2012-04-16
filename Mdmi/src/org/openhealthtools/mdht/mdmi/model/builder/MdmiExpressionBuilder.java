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

public class MdmiExpressionBuilder extends ModelBuilderAttributesOnly<MdmiExpression> {
   private static final String s_expressionAttrib = "expression";
   private static final String s_languageAttrib   = "language";

   @Override
   protected MdmiExpression buildMessageModelObject( ClassDef classDef, RawRoot root ) {
      MdmiExpression expr = new MdmiExpression();
      expr.setExpression(BuilderUtil.getAttribVal(classDef, s_expressionAttrib));
      expr.setLanguage(BuilderUtil.getAttribVal(classDef, s_languageAttrib));
      return expr;
   }
}
