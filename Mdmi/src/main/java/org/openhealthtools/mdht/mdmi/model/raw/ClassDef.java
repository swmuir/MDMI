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

import java.util.*;

public class ClassDef {
   private String                   m_id;
   private String                   m_name;
   private Comment                  m_comment;
   private List<Attribute>          m_attribs  = new ArrayList<Attribute>();
   private HashMap<String, Literal> m_literals = new LinkedHashMap<String, Literal>();

   public void setComment( Comment comment ) {
      m_comment = comment;
   }

   public String getCommentString() {
      return m_comment != null ? m_comment.getValue() : "";
   }

   public void setId( String id ) {
      m_id = id;
   }

   public String getId() {
      return m_id;
   }

   public void setName( String name ) {
      m_name = name;
   }

   public String getName() {
      return m_name;
   }

   public void addAttribute( Attribute attrib ) {
      m_attribs.add(attrib);
   }

   public void addLiteral( Literal literal ) {
      m_literals.put(literal.getId(), literal);
   }

   public Attribute getAttribute( String name ) {
      for( Attribute attrib : m_attribs ) {
         if( name.equals(attrib.getName()) ) {
            return attrib;
         }
      }

      return null;
   }

   public Collection<Attribute> getAttributes() {
      return m_attribs;
   }

   public Collection<Literal> getLiterals() {
      return m_literals.values();
   }

   public Literal getLiteral( String id ) {
      return m_literals.get(id);
   }
}
