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
/**
 * 
 */
package org.openhealthtools.mdht.mdmi.model.builder;

import java.util.*;

import org.openhealthtools.mdht.mdmi.model.raw.*;

/**
 * Super class for all classes which build objects that have no associations at all.
 */
public abstract class ModelBuilderAttributesOnly<T> extends ModelBuilderAbstract<T> {
   @Override
   protected boolean processAssociation( T modelObject, Object assocObject, String stereotypeName, Attribute attrib ) {
      // This should never be called when building an object of type 'T'.
      return false;
   }

   @Override
   protected boolean createAssociation( T modelObject, RawRoot root, ClassDef classDef, String stereotypeName,
         Attribute srcAttrib, Map<String, Object> objectMap ) {
      // This should never be called when building an object of type 'T'.
      return false;
   }
}
