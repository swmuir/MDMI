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

public class EnumeratedDatatypeBuilder extends ModelBuilderAttributesOnly<DTSEnumerated> {
   @Override
   protected DTSEnumerated buildMessageModelObject( ClassDef classDef, RawRoot root ) {
      DTSEnumerated dataType = new DTSEnumerated();
      dataType.setTypeName(classDef.getName());
      dataType.setDescription(classDef.getCommentString());
      for( Literal literal : classDef.getLiterals() ) {
         EnumerationLiteral enumLiteral = new EnumerationLiteral();
         enumLiteral.setCode(literal.getName());
         enumLiteral.setName(literal.getName());
         enumLiteral.setDescription(literal.getCommentString());

         dataType.addLiteral(enumLiteral);
      }
      return dataType;
   }
}
