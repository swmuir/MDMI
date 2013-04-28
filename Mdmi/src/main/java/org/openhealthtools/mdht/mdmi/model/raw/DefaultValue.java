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
package org.openhealthtools.mdht.mdmi.model.raw;

public class DefaultValue {
   private String m_value;
   private String m_instance;

   public void setInstance( String instance ) {
      m_instance = instance;
   }

   public String getInstance() {
      return m_instance;
   }

   public void setValue( String value ) {
      m_value = value;
   }

   public String getValue() {
      return m_value;
   }
}
