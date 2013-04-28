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
public class Bag extends Node {
   private boolean         m_unique  = true;
   private boolean         m_ordered = false;
   private ArrayList<Node> m_nodes   = new ArrayList<Node>();

   public boolean isUnique() {
      return m_unique;
   }

   public void setUnique( boolean unique ) {
      m_unique = unique;
   }

   public boolean isOrdered() {
      return m_ordered;
   }

   public void setOrdered( boolean ordered ) {
      m_ordered = ordered;
   }

   public Node getNode( String name ) {
      if( name == null )
         return null;
      for( Node node : getNodes() ) {
         if( name.equals(node.getName()) ) {
            return node;
         }
      }
      return null;
   }

   public ArrayList<Node> getNodes() {
      return m_nodes;
   }

   public void addNode( Node node ) {
      m_nodes.add(node);
   }

   @Override
   protected void toString( StringBuffer out, String indent ) {
      out.append(indent + "Bag: " + m_name + "[" + m_minOccurs + ".." + (m_maxOccurs < 0 ? "*" : m_maxOccurs) + "]\r\n");
      indent += "  ";
      super.toString(out, indent);
      if( m_unique )
         out.append(indent + "unique: " + m_unique + "\r\n");
      if( m_ordered )
         out.append(indent + "ordered: " + m_ordered + "\r\n");
      for( int i = 0; i < m_nodes.size(); i++ ) {
         Node node = m_nodes.get(i);
         node.toString(out, indent);
      }
   }
} // Bag
