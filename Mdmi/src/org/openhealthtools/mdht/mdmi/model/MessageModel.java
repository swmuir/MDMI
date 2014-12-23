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
import java.net.*;

/**
 * 
 */
public class MessageModel {
   private String             m_messageModelName;
   private String             m_description;
   private URI                m_source;
   private MessageGroup       m_group;
   private SemanticElementSet m_elementSet;
   private MessageSyntaxModel m_syntaxModel;

   public MessageGroup getGroup() {
      return m_group;
   }

   public void setGroup( MessageGroup group ) {
      m_group = group;
   }

   public SemanticElementSet getElementSet() {
      return m_elementSet;
   }

   public void setElementSet( SemanticElementSet elementSet ) {
      m_elementSet = elementSet;
   }

   public MessageSyntaxModel getSyntaxModel() {
      return m_syntaxModel;
   }

   public void setSyntaxModel( MessageSyntaxModel syntaxModel ) {
      m_syntaxModel = syntaxModel;
   }

   public String getMessageModelName() {
      return m_messageModelName;
   }

   public void setMessageModelName( String messageModelName ) {
      m_messageModelName = messageModelName;
   }

   public String getDescription() {
      return m_description;
   }

   public void setDescription( String description ) {
      m_description = description;
   }

   public URI getSource() {
      return m_source;
   }

   public void setSource( URI source ) {
      m_source = source;
   }
   
   public  HashMap<String, MdmiBusinessElementReference> getBusinessElementHashMap() {
   	 Collection<SemanticElement> ses = m_elementSet.getSemanticElements();
      HashMap<String, MdmiBusinessElementReference> bers = new HashMap<String, MdmiBusinessElementReference>();
      for( Iterator<SemanticElement> it = ses.iterator(); it.hasNext(); ) {
         SemanticElement se = it.next();
         Collection<ToMessageElement> toMdmi = se.getToMdmi();
         for( Iterator<ToMessageElement> tm = toMdmi.iterator(); tm.hasNext(); ) {
            ToMessageElement toME = tm.next();
            MdmiBusinessElementReference ber = toME.getBusinessElement();
            if( ber != null && null == bers.get(ber.getUniqueIdentifier()) )
               bers.put(ber.getUniqueIdentifier(), ber);
         }
         Collection<ToBusinessElement> toBers = se.getFromMdmi();
         for( Iterator<ToBusinessElement> tb = toBers.iterator(); tb.hasNext(); ) {
            ToBusinessElement toBE = tb.next();
            MdmiBusinessElementReference ber = toBE.getBusinessElement();
            if( ber != null && null == bers.get(ber.getUniqueIdentifier()) )
               bers.put(ber.getUniqueIdentifier(), ber);
         }
      }
      return bers;
   }

   public Collection<MdmiBusinessElementReference> getBusinessElementReferences() {
      return getBusinessElementHashMap().values();
   }

   @Override
   public String toString() {
      StringBuffer out = new StringBuffer();
      toString(out, "");
      return out.toString();
   }

   protected void toString( StringBuffer out, String indent ) {
      out.append(indent + "MessageModel: " + m_messageModelName + "\r\n");
      indent += "  ";
      if( m_description != null && m_description.length() > 0 )
         out.append(indent + "description: " + m_description + "\r\n");
      if( m_source != null )
         out.append(indent + "source: " + m_source.toString() + "\r\n");
      if( m_syntaxModel != null )
         m_syntaxModel.toString(out, indent);
      if( m_elementSet != null )
         m_elementSet.toString(out, indent);
   }
} // MessageModel
