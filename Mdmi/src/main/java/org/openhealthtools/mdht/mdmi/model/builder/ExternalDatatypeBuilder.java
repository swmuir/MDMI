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

public class ExternalDatatypeBuilder extends ModelBuilderAttributesOnly<DTExternal> {
   private static final String s_refAttrib  = "typeSpecification";
   private static final String s_isReadonly = "isReadonly";

   @Override
   protected DTExternal buildMessageModelObject( ClassDef classDef, RawRoot root ) {
      DTExternal datatype = new DTExternal();
      datatype.setTypeName(classDef.getName());
      datatype.setDescription(classDef.getCommentString());
      datatype.setTypeSpec(BuilderUtil.getURIAttribVal(classDef, s_refAttrib));
      datatype.setReadonly(BuilderUtil.getBooleanAttribVal(classDef, s_isReadonly));
      return datatype;
   }
}
