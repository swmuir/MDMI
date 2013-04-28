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
 * 
 */
public class MEPropertyQualifier {
   private ArrayList<String> m_literals = new ArrayList<String>();

   public List<String> getLiterals() {
      return m_literals;
   }

   public void addLiteral( String literal ) {
      m_literals.add(literal);
   }

   @Override
   public String toString() {
      StringBuffer out = new StringBuffer();
      toString(out, "");
      return out.toString();
   }

   protected void toString( StringBuffer out, String indent ) {
      out.append(indent + "MEPropertyQualifier: {");
      for( int i = 0; i < m_literals.size(); i++ ) {
         String x = m_literals.get(i);
         if( i > 0 )
            out.append(", ");
         out.append(x);
      }
      out.append("}\r\n");
   }
} // MEPropertyQualifier
