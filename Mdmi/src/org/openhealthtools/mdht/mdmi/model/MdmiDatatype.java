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

import net.sourceforge.nrl.parser.model.*;

public abstract class MdmiDatatype implements IClassifier {
   protected MessageGroup            m_owner;
   protected String                  m_name;
   protected String                  m_description;
   protected boolean                 m_isReadonly;
   protected HashMap<String, Object> m_userData = new HashMap<String, Object>();

   public MessageGroup getOwner() {
      return m_owner;
   }

   public void setOwner( MessageGroup owner ) {
      m_owner = owner;
   }

   public String getTypeName() {
      return m_name;
   }

   public void setTypeName( String name ) {
      m_name = name;
   }

   public String getDescription() {
      return m_description;
   }

   public void setDescription( String description ) {
      m_description = description;
   }

   public boolean isReadonly() {
      return m_isReadonly;
   }

   public void setReadonly( boolean isReadonly ) {
      m_isReadonly = isReadonly;
   }

   public abstract boolean isSimple();

   public abstract boolean isComplex();

   public abstract boolean isExternal();

   public abstract boolean isPrimitive();

   public abstract boolean isDerived();

   public abstract boolean isEnum();

   public abstract boolean isStruct();

   public abstract boolean isChoice();

   @Override
   public String getName() {
      return m_name;
   }

   @Override
   public IAttribute getAttributeByName( String name, boolean includeInherited ) {
      return null;
   }

   @Override
   public List<IAttribute> getAttributes( boolean includeInherited ) {
      return new ArrayList<IAttribute>();
   }

   @Override
   public boolean hasAttribute( String name ) {
      return false;
   }

   @Override
   public boolean hasStaticAttributes() {
      return false;
   }

   @Override
   public boolean isEnumeration() {
      return false;
   }

   @Override
   public IPackage getContainingPackage() {
      return m_owner;
   }

   @Override
   public List<IModelElement> getDescendants( boolean transitive ) {
      return new ArrayList<IModelElement>();
   }

   @Override
   public List<String> getDocumentation() {
      ArrayList<String> a = new ArrayList<String>();
      a.add(m_description);
      return a;
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
      return m_owner == null ? m_name : m_owner.getName() + "::" + m_name;
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
      if( src == null || !(src instanceof MdmiDatatype) )
         return false;
      return this == (MdmiDatatype)src;
   }

   @Override
   public boolean isSupplementary() {
      return false;
   }

   @Override
   public ElementType getElementType() {
      return ElementType.Classifier;
   }

   @Override
   public String toString() {
      StringBuffer out = new StringBuffer();
      toString(out, "");
      return out.toString();
   }

   protected abstract void toString( StringBuffer out, String indent );
} // MdmiDatatype
