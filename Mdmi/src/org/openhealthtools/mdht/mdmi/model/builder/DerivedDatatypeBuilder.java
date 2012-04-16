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

import java.util.*;

import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.model.raw.*;

public class DerivedDatatypeBuilder extends ModelBuilderAttributesOnly<DTSDerived> {
   private static final EnumeratedDatatypeBuilder s_enumTypeBuilder    = new EnumeratedDatatypeBuilder();
   private static final PrimitiveDataTypeBuilder  s_primTypeBuilder    = new PrimitiveDataTypeBuilder();
   private static final DerivedDatatypeBuilder    s_derivedTypeBuilder = new DerivedDatatypeBuilder();
   private static final String                    s_restricAttrib      = "restriction";
   private static final String                    s_baseTypeAttrib     = "baseType";

   @Override
   public DTSDerived buildMessageModelObject( ClassDef classDef, RawRoot root, Map<String, Object> objectMap ) {

      DTSDerived datatype = new DTSDerived();
      datatype.setTypeName(classDef.getName());
      datatype.setDescription(classDef.getCommentString());
      datatype.setRestriction(BuilderUtil.getAttribVal(classDef, s_restricAttrib));
      objectMap.put(classDef.getId(), datatype);

      String baseTypeName = BuilderUtil.getAttribVal(classDef, s_baseTypeAttrib);
      Object obj = objectMap.get(baseTypeName);
      if( obj != null ) {
         datatype.setBaseType((DTSimple)obj);
         return datatype;
      }

      // That type has not been created yet. Do it now.
      ClassDef newDef = root.getModel().getClass(baseTypeName);
      StereotypeInstance instance = root.getStereotypeInstance(newDef.getId());
      if( StereotypeNames.PRIM_DATA_TYPE.equals(instance.getName()) ) {
         DTSimple simpleType = s_primTypeBuilder.buildMessageModelObject(newDef, root, objectMap);
         datatype.setBaseType(simpleType);
      }
      else if( StereotypeNames.DERIVED_DATA_TYPE.equals(instance.getName()) ) {
         DTSimple simpleType = s_derivedTypeBuilder.buildMessageModelObject(newDef, root, objectMap);
         datatype.setBaseType(simpleType);
      }
      else if( StereotypeNames.ENUM_DATA_TYPE.equals(instance.getName()) ) {
         DTSimple simpleType = s_enumTypeBuilder.buildMessageModelObject(newDef, root, objectMap);
         datatype.setBaseType(simpleType);
      }

      return datatype;
   }

   @Override
   protected DTSDerived buildMessageModelObject( ClassDef classDef, RawRoot root ) {
      return null;
   }
}