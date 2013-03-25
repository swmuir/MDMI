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

import org.openhealthtools.mdht.mdmi.MdmiException;
import org.openhealthtools.mdht.mdmi.model.*;

/**
 * A bag syntax node, it has one or mode nodes, may be of mixed types.
 * 
 * @author goancea
 */
public final class YBag extends YNode {
   private Bag              m_bag;
   private ArrayList<YNode> m_nodes; // more than one, mixed types

   /**
    * Construct a bag syntax node from its model node and its parent.
    * 
    * @param bag The model node - a Bag.
    * @param parent The owner of this instance. Null only for the root node.
    */
   public YBag( Bag bag, YNode parent ) {
      super(bag, parent);
      m_bag = bag;
      m_nodes = new ArrayList<YNode>();
   }

   /**
    * Construct one from the model node, its parent and a value node, which will be added as the first value node.
    * 
    * @param bag The model node - a Bag.
    * @param parent The owner of this instance. Null only for the root node.
    * @param ynode value The value node to be added.
    */
   public YBag( Bag bag, YNode parent, YNode ynode ) {
      this(bag, parent);
      m_nodes.add(ynode);
   }

   /**
    * Get the model node.
    * 
    * @return The model node, a bag.
    */
   public Bag getBag() {
      return m_bag;
   }

   /**
    * Get all the child nodes.
    * 
    * @return The list of child syntax nodes. 
    */
   public ArrayList<YNode> getYNodes() {
      return m_nodes;
   }

   /**
    * Get all the nodes of a given type, that is for a given model node.
    * 
    * @param node The model node to look for.
    * @return All the nodes of a given type.
    */
   public ArrayList<YNode> getYNodesForNode( Node node ) {
      ArrayList<YNode> ynodes = new ArrayList<YNode>();
      for( int i = 0; i < m_nodes.size(); i++ ) {
         YNode ynode = m_nodes.get(i);
         if( ynode.getNode() == node )
            ynodes.add(ynode);
      }
      return ynodes;
   }

   /**
    * Add a child node.
    * @param ynode
    */
   public void addYNode( YNode ynode ) {
      ArrayList<Node> childNodes = m_bag.getNodes();
      boolean found = false;
      for( int i = 0; i < childNodes.size() && !found; i++ ) {
         Node n = childNodes.get(i);
         if( n.equals(ynode.getNode()) )
            found = true;
      }
      if( !found )
         throw new MdmiException("Invalid add to bag {0}: unknown child type {1}",
               m_bag.getName(), ynode.getNode().getName());
      m_nodes.add(ynode);
   }

   @Override
   public boolean isBag() {
      return true;
   }

   @Override
   protected String toString( String indent ) {
      StringBuffer sb = new StringBuffer();
      sb.append( indent + m_bag.getName() );
      for( int i = 0; i < m_nodes.size(); i++ ) {
         sb.append( "\r\n" );
         YNode ynode = m_nodes.get( i );
         sb.append( ynode.toString(indent + "  ") );
      }
      return sb.toString();
   }
} // YBag
