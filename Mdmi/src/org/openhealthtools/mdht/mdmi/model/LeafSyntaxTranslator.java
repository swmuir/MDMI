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

/**
 * 
 */
public class LeafSyntaxTranslator extends Node {
   private String m_format;
   private String m_formatExpressionLanguage;

   public String getFormat() {
      return m_format;
   }

   public void setFormat( String format ) {
      m_format = format;
   }

   public String getFormatExpressionLanguage() {
      return m_formatExpressionLanguage;
   }

   public void setFormatExpressionLanguage( String formatExpressionLanguage ) {
      m_formatExpressionLanguage = formatExpressionLanguage;
   }

   @Override
   protected void toString( StringBuffer out, String indent ) {
      out.append(indent + "LeafSyntaxTranslator: " + m_name + "[" + m_minOccurs + ".."
            + (m_maxOccurs < 0 ? "*" : m_maxOccurs) + "]\r\n");
      indent += "  ";
      super.toString(out, indent);
      if( m_format != null && m_format.length() > 0 )
         out.append(indent + "format: " + m_format + "\r\n");
      if( m_formatExpressionLanguage != null && m_formatExpressionLanguage.length() > 0 )
         out.append(indent + "format expression language: " + m_formatExpressionLanguage + "\r\n");
   }

   public static void main( String[] args ) {
      for( java.lang.reflect.Field field : LeafSyntaxTranslator.class.getDeclaredFields() ) {
         System.out.println(field.getName());
      }

      System.out.println("Superclass: " + LeafSyntaxTranslator.class.getSuperclass().getName());
   }
} // LeafSyntaxTranslator
