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
public class Choice extends Node {
   private String          m_constraint;
   private String          m_constraintExpressionLanguage;
   private ArrayList<Node> m_nodes = new ArrayList<Node>();

   public String getConstraint() {
      return m_constraint;
   }

   public void setConstraint( String constraint ) {
      m_constraint = constraint;
   }

   public String getConstraintExpressionLanguage() {
      return m_constraintExpressionLanguage;
   }

   public void setConstraintExpressionLanguage( String constraintExpressionLanguage ) {
      m_constraintExpressionLanguage = constraintExpressionLanguage;
   }

   public ArrayList<Node> getNodes() {
      return m_nodes;
   }

   public void addNode( Node node ) {
      m_nodes.add(node);
   }

   public Node getNode( String name ) {
      if( name == null )
         return null;
      for( Node node : m_nodes ) {
         if( name.equals(node.getName()) )
            return node;
      }
      return null;
   }

   @Override
   protected void toString( StringBuffer out, String indent ) {
      out.append(indent + "Choice: " + m_name + "[" + m_minOccurs + ".." + (m_maxOccurs < 0 ? "*" : m_maxOccurs)
            + "]\r\n");
      indent += "  ";
      super.toString(out, indent);
      if( m_constraint != null && m_constraint.length() > 0 )
         out.append(indent + "constraint: " + m_constraint + "\r\n");
      if( m_constraintExpressionLanguage != null && m_constraintExpressionLanguage.length() > 0 )
         out.append(indent + "constraint expression language: " + m_constraintExpressionLanguage + "\r\n");
      Collection<Node> c = getNodes();
      for( Iterator<Node> i = c.iterator(); i.hasNext(); ) {
         Node node = i.next();
         node.toString(out, indent);
      }
   }
   
 
} // Choice
