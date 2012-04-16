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

public class ChoiceDatatypeBuilder extends ModelBuilderAttributesOnly<DTCChoice> {
   private static final FieldBuilder s_fieldBuilder = new FieldBuilder();

   @Override
   public DTCChoice buildMessageModelObject( ClassDef classDef, RawRoot root, Map<String, Object> objectMap ) {

      DTCChoice datatype = buildMessageModelObject(classDef, root);

      // All datatype attributes are fields
      ArrayList<Field> fields = new ArrayList<Field>();
      for( Attribute attrib : classDef.getAttributes() ) {
         if( attrib.getAssociation() != null ) {
            continue;
         }

         ClassDef typeDef = root.getModel().getClass(attrib.getType());
         if( typeDef == null ) {
            throw new IllegalArgumentException("No class definition found " + "for type '" + attrib.getType()
                  + "'.  It appears that a field " + "on a choice data type has an undefined type.");
         }

         Field field = s_fieldBuilder.buildMessageModelObject(root.getModel().getClass(attrib.getType()), root,
               objectMap);

         // TODO - it would be better to do this in the
         // field builder class. May want to modify design
         // to accomplish that.
         field.setName(attrib.getName());
         field.setOwnerType(datatype);
         field.setDescription(attrib.getCommentString());
         field.setMinOccurs(attrib.getLowerLimit());
         field.setMaxOccurs(attrib.getUpperLimit());
         fields.add(field);
      }
      datatype.setFields(fields);

      objectMap.put(classDef.getId(), datatype);

      return datatype;
   }

   @Override
   protected DTCChoice buildMessageModelObject( ClassDef classDef, RawRoot root ) {
      DTCChoice o = new DTCChoice();
      o.setTypeName(classDef.getName());
      o.setDescription(classDef.getCommentString());
      return o;
   }
}
