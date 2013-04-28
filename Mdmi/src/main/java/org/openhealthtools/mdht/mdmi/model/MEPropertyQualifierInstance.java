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
public class MEPropertyQualifierInstance {
   private MEPropertyQualifier m_qual;
   private String              m_value;

   public String getValue() {
      return m_value;
   }

   public List<String> getLiterals() {
      return m_qual.getLiterals();
   }

   public void setValue( String value ) {
      m_value = value;
   }

   public void setQualifier( MEPropertyQualifier qual ) {
      m_qual = qual;
   }

   @Override
   public String toString() {
      StringBuffer out = new StringBuffer();
      toString(out, "");
      return out.toString();
   }

   protected void toString( StringBuffer out, String indent ) {
      out.append(indent + "MEPropertyQualifierInstance: " + m_value + "\r\n");
   }
} // MEPropertyQualifierInstance
