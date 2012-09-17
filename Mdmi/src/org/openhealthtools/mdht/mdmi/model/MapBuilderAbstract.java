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
package org.openhealthtools.mdht.mdmi.model;

import java.util.*;

import org.openhealthtools.mdht.mdmi.model.builder.*;
import org.openhealthtools.mdht.mdmi.model.raw.*;
import org.openhealthtools.mdht.mdmi.model.validate.*;

/**
 *
 */
public class MapBuilderAbstract {
   private static final MessageGroupBuilder        s_groupBuilder       = new MessageGroupBuilder();
   private static final ChoiceDatatypeBuilder      s_choiceTypeBuilder  = new ChoiceDatatypeBuilder();
   private static final EnumeratedDatatypeBuilder  s_enumTypeBuilder    = new EnumeratedDatatypeBuilder();
   private static final StructuredDatatypeBuilder  s_structTypeBuilder  = new StructuredDatatypeBuilder();
   private static final ExternalDatatypeBuilder    s_externalBuilder    = new ExternalDatatypeBuilder();
   private static final PrimitiveDataTypeBuilder   s_primTypeBuilder    = new PrimitiveDataTypeBuilder();
   private static final DerivedDatatypeBuilder     s_derivedTypeBuilder = new DerivedDatatypeBuilder();
   private static final MEPropertyQualifierBuilder s_propQualBuilder    = new MEPropertyQualifierBuilder();

   @SuppressWarnings( "unchecked" )
   protected static List<MessageGroup> buildFromRawModel( RawRoot root, ModelValidationResults valResults ) {

      // Map which will contain all objects created. This is
      // used during the creation of the object graph, but will
      // be discarded when that process is complete.
      HashMap<String, Object> objMap = new HashMap<String, Object>();

      // Create all MEPropertyQualifier objects. These are
      // referenced by other types.
      addMEPropQualTypesToMap(root, objMap);

      // Create all MDMIDatatype objects. These are
      // referenced by many other types.
      addMdmiDataTypesToMap(root, objMap);

      // Assemble return value from root objects.
      List<MessageGroup> rv = getMessageGroupReferences(root, objMap);

      // Perform validations.
      for( Object obj : objMap.values() ) {
         @SuppressWarnings( "rawtypes" )
         IModelValidate validate = ValidatorFactory.getInstance().getValidator(obj);
         if( validate != null ) {
            validate.validate(obj, valResults);
         }
      }

      return rv;
   }

   protected static List<MessageGroup> getMessageGroupReferences( RawRoot root, HashMap<String, Object> objMap ) {
      List<MessageGroup> rv = new ArrayList<MessageGroup>();
      for( StereotypeInstance instance : root.getStereotypeInstances() ) {
         if( StereotypeNames.MESSAGE_GROUP.equals(instance.getName()) ) {
            // We found a message group object.
            ClassDef classDef = root.getModel().getClass(instance.getBaseRef());
            // Create and initialize the data type.
            MessageGroup msgGroup = s_groupBuilder.buildMessageModelObject(classDef, root, objMap);
            rv.add(msgGroup);
         }
      }
      return rv;
   }

   protected static void addMEPropQualTypesToMap( RawRoot root, HashMap<String, Object> objMap ) {
      for( StereotypeInstance instance : root.getStereotypeInstances() ) {
         if( StereotypeNames.ME_PROP_QUALIFIER.equals(instance.getName()) ) {
            ClassDef classDef = root.getModel().getClass(instance.getBaseRef());
            s_propQualBuilder.buildMessageModelObject(classDef, root, objMap);
         }
      }
   }

   private static void addMdmiDataTypesToMap( RawRoot root, HashMap<String, Object> objMap ) {
      for( StereotypeInstance instance : root.getStereotypeInstances() ) {
         // If the class already exists, there is nothing to do
         // regardless of its type.
         ClassDef classDef = root.getModel().getClass(instance.getBaseRef());
         if( objMap.get(classDef.getId()) != null ) {
            continue;
         }

         if( StereotypeNames.ENUM_DATA_TYPE.equals(instance.getName()) ) {
            // Create and initialize the data type.
            s_enumTypeBuilder.buildMessageModelObject(classDef, root, objMap);
         }
         else if( StereotypeNames.CHOICE_DATA_TYPE.equals(instance.getName()) ) {
            s_choiceTypeBuilder.buildMessageModelObject(classDef, root, objMap);
         }
         else if( StereotypeNames.STRUCT_DATA_TYPE.equals(instance.getName()) ) {
            s_structTypeBuilder.buildMessageModelObject(classDef, root, objMap);
         }
         else if( StereotypeNames.EXTERNAL_DATA_TYPE.equals(instance.getName()) ) {
            s_externalBuilder.buildMessageModelObject(classDef, root, objMap);
         }
         else if( StereotypeNames.DERIVED_DATA_TYPE.equals(instance.getName()) ) {
            s_derivedTypeBuilder.buildMessageModelObject(classDef, root, objMap);
         }
         else if( StereotypeNames.PRIM_DATA_TYPE.equals(instance.getName()) ) {
            s_primTypeBuilder.buildMessageModelObject(classDef, root, objMap);
         }
      }
   }
} // MapBuilderAbstract
