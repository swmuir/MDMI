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

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.model.*;

/** 
 * Syntax tree node - base class of all classes in the syntax tree.
 */
public abstract class YNode implements ISyntaxNode {
   protected Node  m_node  ;
   protected YNode m_parent;
   
   /**
    * Construct one from the model Node instance and its parent.
    * 
    * @param node The model Node instance.
    * @param parent The owner YNode.
    */
   public YNode( Node node, YNode parent ) {
      if( node == null )
         throw new IllegalArgumentException("Invalid call, the model node cannot be null!");
      m_node   = node  ;
      m_parent = parent;
   }

   @Override
   public Node getNode() {
      return m_node;
   }

   @Override
   public ISyntaxNode getParent() {
      return m_parent;
   }

   @Override
   public boolean isLeaf() {
      return false;
   }

   @Override
   public boolean isBag() {
      return false;
   }

   @Override
   public boolean isChoice() {
      return false;
   }
   
   @Override
   public String toString() {
      return toString( "" );
   }

   /**
    * Output a representation of this instance to a string.
    *  
    * @param indent The indentation.
    * @return The representation of this instance to a string.
    */
   protected abstract String toString( String indent );
} // YNode
