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
package org.openhealthtools.mdht.mdmi.engine.impl.nrl;

public class StringOperators {
   private static final boolean DEBUG = false;
   
   public String substringStart( String s, int index ) {
      if( s == null || s.length() <= index )
         return s;
      if( DEBUG )
         System.out.println( "StringOperators.substringStart('" + s + "', " + index + ")" );
      return s.substring( 0, index );
   }
   
   public String substringEnd( String s, int index ) {
      if( s == null )
         return null;
      if( s.length() <= index )
         return "";
      if( DEBUG )
         System.out.println( "StringOperators.substringEnd('" + s + "', " + index + ")" );
      return s.substring( index );
   }
   
   public boolean startsWith( String s, String z ) {
      if( s == null || s.length() <= 0 || z == null || z.length() <= 0 )
         return false;
      if( DEBUG )
         System.out.println( "StringOperators.startsWith('" + s + "', '" + z + "')" );
      return s.startsWith( z );
   }
   
   public boolean endsWith( String s, String z ) {
      if( s == null || s.length() <= 0 || z == null || z.length() <= 0 )
         return false;
      if( DEBUG )
         System.out.println( "StringOperators.endsWith('" + s + "', '" + z + "')" );
      return s.endsWith( z );
   }
   
   public int indexOf( String s, String z ) {
      if( s == null || s.length() <= 0 || z == null || z.length() <= 0 )
         return -1;
      if( DEBUG )
         System.out.println( "StringOperators.indexOf('" + s + "', '" + z + "')" );
      return s.indexOf( z );
   }
   
   public int lastIndexOf( String s, String z ) {
      if( s == null || s.length() <= 0 || z == null || z.length() <= 0 )
         return -1;
      if( DEBUG )
         System.out.println( "StringOperators.lastIndexOf('" + s + "', '" + z + "')" );
      return s.lastIndexOf( z );
   }
   
   public boolean matches( String s, String z ) {
      if( s == null || s.length() <= 0 || z == null || z.length() <= 0 )
         return false;
      if( DEBUG )
         System.out.println( "StringOperators.matches('" + s + "', '" + z + "')" );
      return s.matches( z );
   }
} // StringOperators
