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
public class MessageComposite extends SimpleMessageComposite {
   private MessageComposite                  m_owner;
   private ArrayList<SimpleMessageComposite> m_compList = new ArrayList<SimpleMessageComposite>();

   public MessageComposite getOwner() {
      return m_owner;
   }

   public void setOwner( MessageComposite owner ) {
      m_owner = owner;
   }

   public SimpleMessageComposite getComposite( String name ) {
      if( name == null ) {
         return null;
      }

      for( SimpleMessageComposite curComp : getComposites() ) {
         if( name.equals(curComp.getName()) ) {
            return curComp;
         }
      }

      return null;
   }

   public Collection<SimpleMessageComposite> getComposites() {
      return m_compList;
   }

   public void addComposite( SimpleMessageComposite composite ) {
      m_compList.add(composite);
   }

   @Override
   public SemanticElementSet getElementSet() {
      return m_owner != null ? m_owner.getElementSet() : super.getElementSet();
   }

   @Override
   protected void toString( StringBuffer out, String indent ) {
      out.append(indent + "MessageComposite: " + m_name + "\r\n");
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
      Collection<SimpleMessageComposite> d = getComposites();
      for( Iterator<SimpleMessageComposite> it = d.iterator(); it.hasNext(); ) {
         SimpleMessageComposite x = it.next();
         x.toString(out, indent);
      }
   }
} // MessageComposite
