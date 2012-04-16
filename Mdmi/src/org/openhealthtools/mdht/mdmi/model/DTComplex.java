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

/**
 * A complex type - either a structure or a choice.
 */
public abstract class DTComplex extends MdmiDatatype {
   protected ArrayList<Field> m_fields = new ArrayList<Field>();

   public ArrayList<Field> getFields() {
      return m_fields;
   }

   public void setFields( ArrayList<Field> fields ) {
      m_fields = fields;
   }

   public Field getField( String name ) {
      if( name == null || name.length() <= 0 )
         return null;
      for( Field f : m_fields ) {
         if( f.getName().equals(name) )
            return f;
      }
      return null;
   }

   @Override
   public boolean isSimple() {
      return false;
   }

   @Override
   public boolean isComplex() {
      return true;
   }

   @Override
   public boolean isPrimitive() {
      return false;
   }

   @Override
   public boolean isEnum() {
      return false;
   }

   @Override
   public boolean isDerived() {
      return false;
   }

   @Override
   public boolean isExternal() {
      return false;
   }
} // DTComplex
