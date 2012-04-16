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

public class Literal {
   private String  m_id;
   private String  m_name;
   private Comment m_comment = null;

   public String getCommentString() {
      return m_comment != null ? m_comment.getValue() : "";
   }

   public void setComment( Comment comment ) {
      m_comment = comment;
   }

   public String getId() {
      return m_id;
   }

   public void setId( String id ) {
      m_id = id;
   }

   public String getName() {
      return m_name;
   }

   public void setName( String name ) {
      m_name = name;
   }
}
