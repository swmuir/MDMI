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

public class PrimitiveDataTypeBuilder extends ModelBuilderAttributesOnly<DTSPrimitive> {
   @Override
   protected DTSPrimitive buildMessageModelObject( ClassDef classDef, RawRoot root ) {
      // We don't actually build a new object. We just get the single instance.
      for( DTSPrimitive primitive : DTSPrimitive.ALL_PRIMITIVES ) {
         if( primitive.getTypeName().equalsIgnoreCase(classDef.getName()) ) {
            return primitive;
         }
      }
      return null;
   }
}
