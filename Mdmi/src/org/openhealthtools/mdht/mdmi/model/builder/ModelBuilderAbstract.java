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

import org.openhealthtools.mdht.mdmi.model.raw.*;

public abstract class ModelBuilderAbstract<T> implements IModelBuilder<T> {
   public T buildMessageModelObject( ClassDef classDef, RawRoot root, Map<String, Object> objectMap ) {
      // Pass the actual creation off to subclass. All we do is add to the object map.
      T rv = buildMessageModelObject(classDef, root);
      objectMap.put(classDef.getId(), rv);
      // Set all associations
      processAllAssociations(rv, classDef, root, objectMap);
      return rv;
   }

   protected void processAllAssociations( T modelObject, ClassDef classDef, RawRoot root, Map<String, Object> objectMap ) {
      if( getStereotypesToProcess() != null ) {
         for( String stereotype : getStereotypesToProcess() ) {
            processAssociationsByStereotype(modelObject, classDef, root, stereotype, objectMap);
         }
      }
      else {
         processAssociationsByStereotype(modelObject, classDef, root, null, objectMap);
      }
   }

   // If stereotype is null, all stereotypes will be processed.
   private void processAssociationsByStereotype( T modelObject, ClassDef classDef, RawRoot root, String stereotype,
         Map<String, Object> objectMap ) {
      for( Attribute attrib : classDef.getAttributes() ) {
         if( attrib.getAssociation() != null ) {
            StereotypeInstance instance = root.getStereotypeInstance(attrib.getType());
            if( instance != null && (stereotype == null || stereotype.equals(instance.getName())) ) {
               // We now have an association as well as the associated item's stereotype.
               Object mapObj = objectMap.get(instance.getBaseRef());
               if( mapObj != null ) {
                  // The object to associate with has already been created.
                  // Use that version of the object.
                  processAssociation(modelObject, mapObj, instance.getName(), attrib);
               }
               else {
                  // The object to associate with has NOT already been created. Create it now.
                  ClassDef assocClass = root.getModel().getClass(instance.getBaseRef());
                  createAssociation(modelObject, root, assocClass, instance.getName(), attrib, objectMap);
               }
            }
         }
      }
   }

   /**
    * Override to define the order in which associations are processed.
    * 
    * @return List of stereotype names which will be processed in order specified. Return <code>null</code> to process
    *         all associations in an undefined order.
    */
   protected List<String> getStereotypesToProcess() {
      return null;
   }

   protected abstract T buildMessageModelObject( ClassDef classDef, RawRoot root );

   protected abstract boolean processAssociation( T modelObject, Object assocObject, String stereotypeName,
         Attribute srcAttrib );

   protected abstract boolean createAssociation( T modelObject, RawRoot root, ClassDef classDef, String stereotypeName,
         Attribute srcAttrib, Map<String, Object> objectMap );
}
