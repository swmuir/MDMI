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
 * An enumerated simple type that consists of a list of string literals.
 */
public final class DTSEnumerated extends DTSimple {
   private ArrayList<EnumerationLiteral> m_literals = new ArrayList<EnumerationLiteral>();

   public ArrayList<EnumerationLiteral> getLiterals() {
      return m_literals;
   }

   public void addLiteral( EnumerationLiteral literal ) {
      m_literals.add(literal);
   }

   public EnumerationLiteral getLiteralByName( String name ) {
      for( int i = 0; i < m_literals.size(); i++ ) {
         EnumerationLiteral el = m_literals.get(i);
         if( el.getName().equalsIgnoreCase(name) )
            return el;
      }
      return null;
   }

   public EnumerationLiteral getLiteralByCode( String code ) {
      for( int i = 0; i < m_literals.size(); i++ ) {
         EnumerationLiteral el = m_literals.get(i);
         if( el.getCode().equalsIgnoreCase(code) )
            return el;
      }
      return null;
   }

   @Override
   public boolean isPrimitive() {
      return false;
   }

   @Override
   public boolean isDerived() {
      return false;
   }

   @Override
   public boolean isEnum() {
      return true;
   }

   @Override
   public IModelElement getParent() {
      return DTSPrimitive.STRING;
   }

   @Override
   public IAttribute getAttributeByName( String name, boolean includeInherited ) {
      return super.getAttributeByName(name, includeInherited);
   }

   @Override
   public List<IAttribute> getAttributes( boolean includeInherited ) {
      return super.getAttributes(includeInherited);
   }

   @Override
   public boolean hasAttribute( String name ) {
      return super.hasAttribute(name);
   }

   @Override
   public boolean isEnumeration() {
      return true;
   }

   @Override
   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("{ ");
      for( int i = 0; i < m_literals.size(); i++ ) {
         EnumerationLiteral el = m_literals.get(i);
         if( i > 0 )
            sb.append(", ");
         sb.append(el.toString());
      }
      sb.append(" }");
      return super.toString();
   }

   @Override
   protected void toString( StringBuffer out, String indent ) {
      out.append(indent + "Enumerated datatype: " + m_name + " = { ");
      for( int i = 0; i < m_literals.size(); i++ ) {
         EnumerationLiteral el = m_literals.get(i);
         if( i > 0 )
            out.append(", ");
         out.append(el.toString());
      }
      out.append(" }\r\n");
   }
} // DTSEnumerated
