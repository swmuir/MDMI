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

import net.sourceforge.nrl.parser.model.*;

/**
 * A structured data type is a named type consisting of one or more named fields. Each field has a type: either
 * primitive type, or a Datatype. It also has a multiplicity, expressed as min-max no of occurrences.
 */
public final class DTCStructured extends DTComplex {

   @Override
   public boolean isStruct() {
      return true;
   }

   @Override
   public boolean isChoice() {
      return false;
   }

   @Override
   public IAttribute getAttributeByName( String name, boolean includeInherited ) {
      for( Field f : m_fields ) {
         if( f.getName().equals(name) )
            return f;
      }
      return null;
   }

   @Override
   public List<IAttribute> getAttributes( boolean includeInherited ) {
      ArrayList<IAttribute> a = new ArrayList<IAttribute>();
      for( Field f : m_fields ) {
         a.add(f);
      }
      return a;
   }

   @Override
   public boolean hasAttribute( String name ) {
      for( Field f : m_fields ) {
         if( f.getName().equals(name) )
            return true;
      }
      return false;
   }

   @Override
   protected void toString( StringBuffer out, String indent ) {
      out.append(indent + "Structured datatype: " + m_name + "\r\n");
      indent += "  ";
      if( m_description != null && m_description.length() > 0 )
         out.append(indent + "description: " + m_description + "\r\n");
      for( int i = 0; i < m_fields.size(); i++ ) {
         Field f = m_fields.get(i);
         f.toString(out, indent + "  ");
      }
   }
} // DTCStructured
