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

public class EnumerationLiteral {
   private String m_name;
   private String m_description;
   private String m_code;

   public String getName() {
      return m_name;
   }

   public void setName( String name ) {
      m_name = name;
   }

   public String getDescription() {
      return m_description;
   }

   public void setDescription( String description ) {
      m_description = description;
   }

   public String getCode() {
      return m_code;
   }

   public void setCode( String code ) {
      m_code = code;
   }

   @Override
   public String toString() {
      return m_name == null || m_name.length() <= 0 ? m_code : m_name;
   }
} // EnumerationLiteral
