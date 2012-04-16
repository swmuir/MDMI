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
 * An instance of this class is the value of a semantic element.
 * 
 * @author goancea
 */
public class XElementValue implements IElementValue {
   private SemanticElement          m_semanticElement;
   private XElementValue            m_parent;
   private ArrayList<XElementValue> m_children;
   private ArrayList<XElementValue> m_relations;
   private XValue                   m_xvalue;
   private ElementValueSet          m_owner;

   /**
    * Construct one from the SemanticElement model instance and its owner set.
    * 
    * @param semanticElement The SemanticElement model for this value.
    * @param eset The owner element values set.
    */
   public XElementValue( SemanticElement semanticElement, ElementValueSet eset ) {
      if( semanticElement == null )
         throw new IllegalArgumentException("Null argument!");
      m_semanticElement = semanticElement;
      m_children = new ArrayList<XElementValue>();
      m_relations = new ArrayList<XElementValue>();
      m_xvalue = new XValue(this);
      m_owner = eset;
      m_owner.addElementValue(this);
   }

   // a private clone
   private XElementValue( XElementValue src, boolean deep ) {
      m_semanticElement = src.m_semanticElement;
      if( deep ) {
         m_children = new ArrayList<XElementValue>();
         for( int i = 0; i < src.m_children.size(); i++ ) {
            XElementValue e = src.m_children.get(i);
            m_children.add(e.clone(true));
         }
         m_relations = new ArrayList<XElementValue>();
         for( int i = 0; i < src.m_relations.size(); i++ ) {
            XElementValue e = src.m_relations.get(i);
            m_relations.add(e.clone(true));
         }
         m_xvalue = src.m_xvalue.clone(true);
      }
      else {
         m_children = src.m_children;
         m_relations = src.m_relations;
         m_xvalue = src.m_xvalue;
      }
      m_owner = src.m_owner;
      m_owner.addElementValue(this);
   }

   /**
    * Clone this, optionally deep (meaning clone the referenced data constructs as well).
    * A deep clone can be modified in any way without affecting the source.
    * 
    * @param deep If true a deep clone is requested.
    * @return The newly created clone.
    */
   public XElementValue clone( boolean deep ) {
      return new XElementValue(this, deep);
   }

   @Override
   public SemanticElement getSemanticElement() {
      return m_semanticElement;
   }

   @Override
   public String getName() {
      return m_semanticElement.getName();
   }

   @Override
   public IElementValue getParent() {
      return m_parent;
   }

   @Override
   public void setParent( IElementValue parent ) {
      m_parent = (XElementValue)parent;
   }

   @Override
   public ArrayList<IElementValue> getChildren() {
      ArrayList<IElementValue> a = new ArrayList<IElementValue>();
      for( XElementValue e : m_children ) {
         a.add(e);
      }
      return a;
   }

   @Override
   public int getChildCount() {
      return m_children.size();
   }
   
   @Override
   public void addChild( IElementValue child ) {
      m_children.add((XElementValue)child);
      child.setParent(this);
   }

   @Override
   public ArrayList<IElementValue> getRelations() {
      ArrayList<IElementValue> a = new ArrayList<IElementValue>();
      for( XElementValue e : m_relations ) {
         a.add(e);
      }
      return a;
   }

   @Override
   public XValue getValue() {
      return m_xvalue;
   }

   @Override
   public boolean isSimple() {
      return m_semanticElement.getDatatype().isSimple();
   }

   @Override
   public ElementValueSet getOwner() {
      return m_owner;
   }

   @Override
   public String toString() {
      return toString("");
   }

   String toStringShort() {
      if( m_xvalue.size() <= 0 )
         return "null";
      else if( m_xvalue.getDatatype().isStruct() )
         return "struct...";
      else if( m_xvalue.getDatatype().isChoice() )
         return "choice...";
      return m_xvalue.toString();
   }

   String toString( String indent ) {
      StringBuffer sb = new StringBuffer();
      sb.append(indent);
      if( indent.length() <= 0 )
         sb.append("SemanticElement: ");
      sb.append(getName());
      sb.append("\r\n");
      indent += "  ";
      if( m_parent != null )
         sb.append(indent + "parent: " + m_parent.getName() + " [" + m_parent.toStringShort() + "]\r\n");
      if( m_children.size() > 0 ) {
         sb.append(indent + "children:\r\n");
         for( int i = 0; i < m_children.size(); i++ ) {
            XElementValue xe = m_children.get(i);
            sb.append(indent + "  " + xe.getName() + " [" + xe.toStringShort() + "]\r\n");
         }
      }
      if( m_relations.size() > 0 ) {
         sb.append(indent + "relations:\r\n");
         for( int i = 0; i < m_relations.size(); i++ ) {
            XElementValue xe = m_relations.get(i);
            sb.append(indent + "  " + xe.getName() + " [" + xe.toStringShort() + "]\r\n");
         }
      }
      sb.append(m_xvalue.toString(indent));
      sb.append("\r\n");
      return sb.toString();
   }
} // XElementValue
