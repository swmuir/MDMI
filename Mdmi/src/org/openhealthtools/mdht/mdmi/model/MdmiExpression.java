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
public class MdmiExpression {
   private String m_expression;
   private String m_language;

   public String getExpression() {
      return m_expression;
   }

   public void setExpression( String expression ) {
      m_expression = expression;
   }

   public String getLanguage() {
      return m_language;
   }

   public void setLanguage( String language ) {
      m_language = language;
   }

   @Override
   public String toString() {
      StringBuffer out = new StringBuffer();
      toString(out, "");
      return out.toString();
   }

   protected void toString( StringBuffer out, String indent ) {
      out.append(indent + "MdmiExpression:\r\n");
      indent += "  ";
      if( m_expression != null && m_expression.length() > 0 )
         MessageGroup.appendML(out, indent, "expression: ", m_expression);
      if( m_language != null && m_language.length() > 0 )
         out.append(indent + "expression language: " + m_language + "\r\n");
   }
} // MdmiExpression
