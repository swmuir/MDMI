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

import java.net.*;
import java.util.*;

import net.sourceforge.nrl.parser.model.*;

/**
 * 
 */
public class MdmiDomainDictionaryReference implements IPackage {
   private String                                  m_name;
   private String                                  m_description;
   private URI                                     m_reference;
   private MessageGroup                            m_messageGroup;
   private ArrayList<MdmiBusinessElementReference> m_businessElemList = new ArrayList<MdmiBusinessElementReference>();
   private HashMap<String, Object>                 m_userData         = new HashMap<String, Object>();

   public String getName() {
      return m_name;
   }

   public void setName( String name ) {
      m_name = name;
   }

   public String getDescription() {
      return m_description;
   }

   public void setDescription( String description ) {
      m_description = description;
   }

   public URI getReference() {
      return m_reference;
   }

   public void setReference( URI reference ) {
      m_reference = reference;
   }

   public Collection<MdmiBusinessElementReference> getBusinessElements() {
      return m_businessElemList;
   }

   public void addBusinessElement( MdmiBusinessElementReference businessElement ) {
      m_businessElemList.add(businessElement);
   }

   public MessageGroup getMessageGroup() {
      return m_messageGroup;
   }

   public void setMessageGroup( MessageGroup messageGroup ) {
      m_messageGroup = messageGroup;
   }

   public MdmiBusinessElementReference getBusinessElement( String name ) {
      if( name == null ) {
         return null;
      }

      for( MdmiBusinessElementReference curRef : getBusinessElements() ) {
         if( name.equals(curRef.getName()) ) {
            return curRef;
         }
      }

      return null;
   }

   public MdmiBusinessElementReference getBusinessElementByUniqueID( String uniqueID ) {
      Collection<MdmiBusinessElementReference> values = getBusinessElements();
      for( Iterator<MdmiBusinessElementReference> it = values.iterator(); it.hasNext(); ) {
         MdmiBusinessElementReference ber = it.next();
         String uid = ber.getUniqueIdentifier();
         if( uid != null && uid.equalsIgnoreCase(uniqueID) )
            return ber;
      }
      return null;
   }

   @Override
   public List<IModelElement> getContents( boolean deep ) {
      ArrayList<IModelElement> es = new ArrayList<IModelElement>();
      Collection<MdmiBusinessElementReference> values = getBusinessElements();
      for( Iterator<MdmiBusinessElementReference> iterator = values.iterator(); iterator.hasNext(); ) {
         MdmiBusinessElementReference e = iterator.next();
         es.add(e);
      }
      return es;
   }

   @Override
   public IModelElement getElementByName( String name, boolean deep ) {
      return getBusinessElement(name);
   }

   @Override
   public int getSize() {
      return getBusinessElements().size();
   }

   @Override
   public boolean isAmbiguous( String arg0 ) {
      return false;
   }

   @Override
   public IPackage getContainingPackage() {
      return null;
   }

   @Override
   public List<IModelElement> getDescendants( boolean transitive ) {
      return new ArrayList<IModelElement>();
   }

   @Override
   public List<String> getDocumentation() {
      return null;
   }

   @Override
   public String getOriginalName() {
      return m_name;
   }

   @Override
   public IModelElement getParent() {
      return null;
   }

   @Override
   public String getQualifiedName() {
      return m_name;
   }

   @Override
   public Object getUserData( String key ) {
      return m_userData.get(key);
   }

   @Override
   public void setUserData( String key, Object data ) {
      m_userData.put(key, data);
   }

   @Override
   public boolean isAssignableFrom( IModelElement src ) {
      return src instanceof MdmiDomainDictionaryReference;
   }

   @Override
   public boolean isSupplementary() {
      return false;
   }

   @Override
   public String toString() {
      StringBuffer out = new StringBuffer();
      toString(out, "");
      return out.toString();
   }

   protected void toString( StringBuffer out, String indent ) {
      out.append(indent + "MdmiDomainDictionaryReference: " + m_name + "\r\n");
      indent += "  ";
      if( m_description != null && m_description.length() > 0 )
         out.append(indent + "description: " + m_description + "\r\n");
      if( m_reference != null )
         out.append(indent + "reference: " + m_reference + "\r\n");
      Collection<MdmiBusinessElementReference> c = getBusinessElements();
      for( Iterator<MdmiBusinessElementReference> i = c.iterator(); i.hasNext(); ) {
         MdmiBusinessElementReference x = i.next();
         x.toString(out, indent);
      }
   }

   @Override
   public ElementType getElementType() {
      return ElementType.Package;
   }

	public MdmiBusinessElementReference getBusinessElement( MdmiBusinessElementReference ber ) {
		
	     if( ber == null ) {
	         return null;
	      }

	      for( MdmiBusinessElementReference curRef : getBusinessElements() ) {
	         if(ber.getUniqueIdentifier() != null && ber.getUniqueIdentifier().equals(curRef.getUniqueIdentifier())) {
	            return curRef;
	         }
	      }

	      return null;
   }
} // MdmiDomainDictionaryReference
