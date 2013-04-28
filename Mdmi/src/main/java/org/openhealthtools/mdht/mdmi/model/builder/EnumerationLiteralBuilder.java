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

public class EnumerationLiteralBuilder extends ModelBuilderAttributesOnly<EnumerationLiteral> {
   @Override
   protected EnumerationLiteral buildMessageModelObject( ClassDef classDef, RawRoot root ) {
      // there is no code attribute in the standard UML enums, MDMI has one, so we make it to be the name
      EnumerationLiteral literal = new EnumerationLiteral();
      literal.setCode(BuilderUtil.getNameAttribVal(classDef)); // .getAttribVal(classDef, s_codeAttrib));
      literal.setName(BuilderUtil.getNameAttribVal(classDef));
      literal.setDescription(BuilderUtil.getDescriptionAttribVal(classDef));
      return literal;
   }
}
