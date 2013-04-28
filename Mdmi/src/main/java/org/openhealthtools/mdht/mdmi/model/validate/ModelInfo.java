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
package org.openhealthtools.mdht.mdmi.model.validate;

public class ModelInfo {
   private String m_message;
   private String m_field;
   private Object m_object;

   public ModelInfo( Object obj, String field, String msg ) {
      setObject(obj);
      setField(field);
      setMessage(msg);
   }

   public ModelInfo( String msg ) {
      setMessage(msg);
   }

   public String getMessage() {
      return m_message;
   }

   private void setMessage( String message ) {
      m_message = message;
   }

   public String getField() {
      return m_field;
   }

   private void setField( String field ) {
      m_field = field;
   }

   public Object getObject() {
      return m_object;
   }

   private void setObject( Object obj ) {
      m_object = obj;
   }
}
