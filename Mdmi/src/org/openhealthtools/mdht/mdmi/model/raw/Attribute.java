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
package org.openhealthtools.mdht.mdmi.model.raw;

public class Attribute {
   private String       m_id;
   private String       m_name;
   private String       m_association;
   private String       m_aggregationType;
   private DefaultValue m_defaultValue;
   private Comment      m_comment;
   private LimitValue   m_upperLimit = new LimitValue();
   private LimitValue   m_lowerLimit = new LimitValue();
   private String       m_type;

   public String getCommentString() {
      return m_comment != null ? m_comment.getValue() : "";
   }

   public void setComment( Comment comment ) {
      m_comment = comment;
   }

   public int getUpperLimit() {
      return m_upperLimit.getValue();
   }

   public void setUpperLimit( LimitValue upperLimit ) {
      m_upperLimit = upperLimit;
   }

   public int getLowerLimit() {
      return m_lowerLimit.getValue();
   }

   public void setLowerLimit( LimitValue lowerLimit ) {
      m_lowerLimit = lowerLimit;
   }

   public void setId( String id ) {
      m_id = id;
   }

   public String getId() {
      return m_id;
   }

   public void setName( String name ) {
      m_name = name;
   }

   public void setAssociation( String association ) {
      m_association = association;
   }

   public void setAggregationType( String aggregationType ) {
      m_aggregationType = aggregationType;
   }

   public void setDefaultValue( DefaultValue defaultValue ) {
      m_defaultValue = defaultValue;
   }

   public void setType( String type ) {
      m_type = type;
   }

   public String getName() {
      return m_name;
   }

   public String getDefaultValue() {
      return m_defaultValue == null ? null : m_defaultValue.getValue();
   }

   public String getDefaultValueInstance() {
      return m_defaultValue == null ? null : m_defaultValue.getInstance();
   }

   public DefaultValue getDefaultValueObject() {
      return m_defaultValue;
   }

   public String getType() {
      return m_type;
   }

   public String getAssociation() {
      return m_association;
   }

   public String getAggregationType() {
      return m_aggregationType;
   }
}
