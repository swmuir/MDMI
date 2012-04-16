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
public class MessageSyntaxModel {
   private String             m_name;
   private String             m_description;
   private MessageModel       m_model;
   private SemanticElementSet m_elementSet;
   private Node               m_root;

   public void setDescription( String description ) {
      m_description = description;
   }

   public String getDescription() {
      return m_description;
   }

   public MessageModel getModel() {
      return m_model;
   }

   public void setModel( MessageModel model ) {
      m_model = model;
   }

   public SemanticElementSet getElementSet() {
      return m_elementSet;
   }

   public void setElementSet( SemanticElementSet elementSet ) {
      m_elementSet = elementSet;
   }

   public Node getRoot() {
      return m_root;
   }

   public void setRoot( Node node ) {
      m_root = node;
   }

   public String getName() {
      return m_name;
   }

   public void setName( String name ) {
      m_name = name;
   }

   @Override
   public String toString() {
      StringBuffer out = new StringBuffer();
      toString(out, "");
      return out.toString();
   }

   protected void toString( StringBuffer out, String indent ) {
      out.append(indent + "MessageSyntaxModel: " + m_name + "\r\n");
      indent += "  ";
      if( m_description != null && m_description.length() > 0 )
         out.append(indent + "description: " + m_description + "\r\n");
      if( m_root != null )
         m_root.toString(out, indent);
   }
} // MessageSyntaxModel
