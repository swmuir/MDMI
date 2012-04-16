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

public class FieldBuilder extends ModelBuilderAttributesOnly<Field> {
   private static final ChoiceDatatypeBuilder     s_choiceTypeBuilder  = new ChoiceDatatypeBuilder();
   private static final EnumeratedDatatypeBuilder s_enumTypeBuilder    = new EnumeratedDatatypeBuilder();
   private static final StructuredDatatypeBuilder s_structTypeBuilder  = new StructuredDatatypeBuilder();
   private static final PrimitiveDataTypeBuilder  s_primTypeBuilder    = new PrimitiveDataTypeBuilder();
   private static final DerivedDatatypeBuilder    s_derivedTypeBuilder = new DerivedDatatypeBuilder();
   private static final ExternalDatatypeBuilder   s_externalBuilder    = new ExternalDatatypeBuilder();

   @Override
   public Field buildMessageModelObject( ClassDef classDef, RawRoot root, Map<String, Object> objectMap ) {
      Object obj = objectMap.get(classDef.getId());
      if( obj != null ) {
         Field field = new Field();
         field.setDatatype((MdmiDatatype)obj);
         return field;
      }

      Field field = null;
      StereotypeInstance instance = root.getStereotypeInstance(classDef.getId());
      if( StereotypeNames.PRIM_DATA_TYPE.equals(instance.getName()) ) {
         DTSPrimitive datatype = s_primTypeBuilder.buildMessageModelObject(classDef, root, objectMap);
         field = new Field();
         field.setDatatype(datatype);
      }
      else if( StereotypeNames.DERIVED_DATA_TYPE.equals(instance.getName()) ) {
         DTSDerived datatype = s_derivedTypeBuilder.buildMessageModelObject(classDef, root, objectMap);
         field = new Field();
         field.setDatatype(datatype);
      }
      else if( StereotypeNames.CHOICE_DATA_TYPE.equals(instance.getName()) ) {
         DTCChoice datatype = s_choiceTypeBuilder.buildMessageModelObject(classDef, root, objectMap);
         field = new Field();
         field.setDatatype(datatype);
      }
      else if( StereotypeNames.ENUM_DATA_TYPE.equals(instance.getName()) ) {
         DTSEnumerated datatype = s_enumTypeBuilder.buildMessageModelObject(classDef, root, objectMap);
         field = new Field();
         field.setDatatype(datatype);
      }
      else if( StereotypeNames.STRUCT_DATA_TYPE.equals(instance.getName()) ) {
         DTCStructured datatype = s_structTypeBuilder.buildMessageModelObject(classDef, root, objectMap);
         field = new Field();
         field.setDatatype(datatype);
      }
      else if( StereotypeNames.EXTERNAL_DATA_TYPE.equals(instance.getName()) ) {
         DTExternal datatype = s_externalBuilder.buildMessageModelObject(classDef, root, objectMap);
         field = new Field();
         field.setDatatype(datatype);
      }
      return field;
   }

   @Override
   protected Field buildMessageModelObject( ClassDef classDef, RawRoot root ) {
      return null;
   }
}
