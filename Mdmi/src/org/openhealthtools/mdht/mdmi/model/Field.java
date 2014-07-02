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

/**
 * A field of a struct or a choice.
 */
public final class Field implements IAttribute {
   private DTComplex                 m_ownerType;
   private String                    m_name;
   private String                    m_description;
   private MdmiDatatype              m_datatype;
   private int                       m_minOccurs = 1;
   private int                       m_maxOccurs = 1;
   protected HashMap<String, Object> m_userData  = new HashMap<String, Object>();

   public DTComplex getOwnerType() {
      return m_ownerType;
   }

   public void setOwnerType( DTComplex ownerType ) {
      m_ownerType = ownerType;
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

   public void setDescription( String description ) {
      m_description = description;
   }

   public MdmiDatatype getDatatype() {
      return m_datatype;
   }

   public void setDatatype( MdmiDatatype datatype ) {
      m_datatype = datatype;
   }

   public int getMinOccurs() {
      return m_minOccurs;
   }

   public void setMinOccurs( int minOccurs ) {
      m_minOccurs = minOccurs;
   }

   public int getMaxOccurs() {
      return m_maxOccurs;
   }

   public void setMaxOccurs( int maxOccurs ) {
      m_maxOccurs = maxOccurs;
   }

   public boolean isSimple() {
      return m_datatype != null && m_datatype.isSimple();
   }

   public boolean isComplex() {
      return m_datatype != null && m_datatype.isComplex();
   }

   public boolean isPrimitive() {
      return m_datatype != null && m_datatype.isPrimitive();
   }

   public boolean isDerived() {
      return m_datatype != null && m_datatype.isDerived();
   }

   public boolean isEnum() {
      return m_datatype != null && m_datatype.isEnum();
   }

   public boolean isStruct() {
      return m_datatype != null && m_datatype.isStruct();
   }

   public boolean isChoice() {
      return m_datatype != null && m_datatype.isChoice();
   }

   public boolean isMany() {
      return getMaxOccurs() < 0 || getMaxOccurs() > 1;
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
   public IClassifier getOwner() {
      return m_ownerType;
   }

   @Override
   public IModelElement getType() {
      return m_datatype;
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
   public boolean isStatic() {
      return false;
   }

   public String toString() {
      StringBuffer out = new StringBuffer();
      toString(out, "");
      return out.toString();
   }

   protected void toString( StringBuffer out, String indent ) {
   	if (m_datatype == null) {
   		out.append(indent + m_name);
   	} else {
   		out.append(indent + m_name + ": " + m_datatype.getTypeName());
   	}
   	if( m_minOccurs != 1 || m_maxOccurs != 1 )
   		out.append("[" + m_minOccurs + ".." + (m_maxOccurs < 0 ? "*" : m_maxOccurs) + "]");
   	out.append("\r\n");
   }

   @Override
   public boolean isRepeating() {
      return isMany();
   }
} // Field

