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

import java.net.*;

/**
 * The simple built-in (predefined) types.
 */
public final class DTSPrimitive extends DTSimple {
   public static final DTSPrimitive   BINARY         = new DTSPrimitive("Binary", "Binary",
                                                           "http://www.w3.org/2001/XMLSchema#hexBinary");
   public static final DTSPrimitive   BOOLEAN        = new DTSPrimitive("Boolean", "Boolean",
                                                           "http://www.w3.org/2001/XMLSchema#boolean");
   public static final DTSPrimitive   DATETIME       = new DTSPrimitive("DateTime", "DateTime",
                                                           "http://www.w3.org/2001/XMLSchema#dateTime");
   public static final DTSPrimitive   DECIMAL        = new DTSPrimitive("Decimal", "Decimal",
                                                           "http://www.w3.org/2001/XMLSchema#decimal");
   public static final DTSPrimitive   INTEGER        = new DTSPrimitive("Integer", "Integer",
                                                           "http://www.w3.org/2001/XMLSchema#integer");
   public static final DTSPrimitive   STRING         = new DTSPrimitive("String", "String",
                                                           "http://www.w3.org/2001/XMLSchema#string");

   // BINARY : java.nio.ByteBuffer
   // BOOLEAN : java.lang.Boolean
   // DATETIME : java.util.Date
   // DECIMAL : java.math.BigDecimal
   // INTEGER : java.math.BigInteger
   // STRING : java.lang.String

   public static final DTSPrimitive[] ALL_PRIMITIVES = { BINARY, BOOLEAN, DATETIME, DECIMAL, INTEGER, STRING };

   public static DTSPrimitive getByName( String name ) {
      if( name == null )
         return null;
      for( int i = 0; i < ALL_PRIMITIVES.length; i++ ) {
         if( ALL_PRIMITIVES[i].m_name.equalsIgnoreCase(name) ) // DO NOT USE getName()
            return ALL_PRIMITIVES[i];
      }
      return null;
   }
   
   private URI                        m_reference;

   private DTSPrimitive( String name, String description, String reference ) {
      m_name = name;
      m_description = description;
      try {
         m_reference = new URI(reference);
      }
      catch( Exception ignored ) {
      }
   }

   public URI getReference() {
      return m_reference;
   }

   @Override
   public String getName() {
      // this override should be strictly used by NRL IDataType.getName() calls
      if( m_name.equalsIgnoreCase(BINARY.m_name) )
         return "String";
      else if( m_name.equalsIgnoreCase(DATETIME.m_name) )
         return "Date";
      else if( m_name.equalsIgnoreCase(DECIMAL.m_name) )
         return "decimal";
      else if( m_name.equalsIgnoreCase(INTEGER.m_name) )
         return "integer";
      return m_name;
   }

   @Override
   public boolean isBuiltIn() {
      return true;
   }

   @Override
   public boolean isPrimitive() {
      return true;
   }

   @Override
   public boolean isDerived() {
      return false;
   }

   @Override
   public boolean isEnum() {
      return false;
   }

   @Override
   public boolean equals( Object other ) {
      if( other == null )
         return false;
      if( !(other instanceof DTSPrimitive) )
         return false;
      return m_reference.equals(((DTSPrimitive)other).m_reference);
   }

   @Override
   public boolean isReadonly() {
      return true;
   }

   @Override
   public void setReadonly( boolean value ) {
   }

   @Override
   protected void toString( StringBuffer out, String indent ) {
      out.append(indent + "Primitive datatype: " + m_name + " = '" + m_reference + "'\r\n");
   }
} // DTSPrimitive
