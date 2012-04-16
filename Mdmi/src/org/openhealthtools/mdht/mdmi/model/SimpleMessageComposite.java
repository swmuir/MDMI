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
public class SimpleMessageComposite {
   protected String                     m_name;
   protected String                     m_description;
   protected SemanticElementSet         m_elementSet;
   protected ArrayList<SemanticElement> m_elemList = new ArrayList<SemanticElement>();

   public SemanticElementSet getElementSet() {
      return m_elementSet;
   }

   public void setElementSet( SemanticElementSet elementSet ) {
      m_elementSet = elementSet;
   }

   public SemanticElement getSemanticElement( String elementName ) {
      if( elementName == null )
         return null;
      for( SemanticElement curElem : getSemanticElements() ) {
         if( elementName.equals(curElem.getName()) ) {
            return curElem;
         }
      }
      return null;
   }

   public Collection<SemanticElement> getSemanticElements() {
      return m_elemList;
   }

   public void addSemanticElement( SemanticElement semanticElement ) {
      m_elemList.add(semanticElement);
   }

   public String getName() {
      return m_name;
   }

   public void setName( String name ) {
      m_name = name;
   }

   public String getDescription() {
      return m_description;
   }

   public void setDescription( String desc ) {
      m_description = desc;
   }

   @Override
   public String toString() {
      StringBuffer out = new StringBuffer();
      toString(out, "");
      return out.toString();
   }

   protected void toString( StringBuffer out, String indent ) {
      out.append(indent + "SimpleMessageComposite: " + m_name + "\r\n");
      indent += "  ";
      if( m_description != null && m_description.length() > 0 )
         out.append(indent + "description: " + m_description + "\r\n");
      out.append(indent + "semantic elements: ");
      int i = 0;
      Collection<SemanticElement> c = getSemanticElements();
      for( Iterator<SemanticElement> it = c.iterator(); it.hasNext(); ) {
         SemanticElement x = it.next();
         if( i++ <= 0 )
            out.append(", ");
         out.append(x.getName());
      }
      out.append("\r\n");
   }
} // SimpleMessageComposite
