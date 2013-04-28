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
package org.openhealthtools.mdht.mdmi.engine;

import java.util.*;

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.model.*;

/**
 * A choice syntax node, it has one node usually (or more but all of the same type).
 * 
 * @author goancea
 */
class YChoice extends YNode {
   private Choice           m_choice;
   private ArrayList<YNode> m_nodes; // can be more than one, but all the same type

   /**
    * Construct a choice syntax node from its model node and its parent.
    * 
    * @param choice The model node - a Choice.
    * @param parent The owner of this instance. Null only for the root node.
    */
   public YChoice( Choice choice, YNode parent ) {
      super(choice, parent);
      m_choice = choice;
      m_nodes = new ArrayList<YNode>();
   }

   /**
    * Construct one from the model node, its parent and a value node, which is the selected node value.
    * 
    * @param choice The model node - a Choice.
    * @param parent The owner of this instance. Null only for the root node.
    * @param ynode value The value node - the selected choice item.
    */
   public YChoice( Choice choice, YNode parent, YNode value ) {
      this(choice, parent);
      m_nodes.add(value);
   }

   /**
    * Get the model node.
    * 
    * @return The model node, a choice.
    */
   public Choice getChoice() {
      return m_choice;
   }

   /**
    * Get all the child nodes. All these nodes are of the same type.
    * That is, all of them have the same model node.
    * 
    * @return The list of child syntax nodes. 
    */
   public ArrayList<YNode> getYNodes() {
      return m_nodes;
   }

   /**
    * Add a new child syntax node. The child syntax node's model node must match the existing nodes,
    * if there are any.
    * 
    * @param ynode The syntax node to be added as a child. 
    */
   public void addYNode( YNode ynode ) {
      if( m_nodes.size() > 0 ) {
         Node n = m_nodes.get(0).getNode();
         if( n != ynode.getNode() )
            throw new MdmiException("Invalid add to choice {0}: node type mismatch, expected {1}, found {2}",
                  m_choice.getName(), n.getName(), ynode.getNode().getName());
      }
      else {
         ArrayList<Node> childNodes = m_choice.getNodes();
         boolean found = false;
         for( int i = 0; i < childNodes.size() && !found; i++ ) {
            Node n = childNodes.get(i);
            if( n.equals(ynode.getNode()) )
               found = true;
         }
         if( !found )
            throw new MdmiException("Invalid add to choice {0}: unknown child type {1}",
                  m_choice.getName(), ynode.getNode().getName());
      }
      m_nodes.add(ynode);
   }

   /**
    * Get the chosen (selected) model node (type). Identifies which of the choice's child nodes is really present.
    * May be null if none selected yet.
    * 
    * @return The chosen (selected) model node (type).
    */
   public Node getChosenNode() {
      if( m_nodes.size() > 0 )
         return m_nodes.get(0).getNode();
      return null;
   }

   @Override
   public boolean isChoice() {
      return true;
   }

   @Override
   protected String toString( String indent ) {
      StringBuffer sb = new StringBuffer();
      sb.append( indent + m_choice.getName() );
      for( int i = 0; i < m_nodes.size(); i++ ) {
         sb.append( "\r\n" );
         YNode ynode = m_nodes.get( i );
         sb.append( ynode.toString(indent + "  ") );
      }
      return sb.toString();
   }
} // YChoice
