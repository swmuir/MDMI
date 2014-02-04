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

public abstract class Node {
   protected String             m_name;
   protected int                m_minOccurs = 0;
   protected int                m_maxOccurs = 1;
   protected String             m_location;
   protected String             m_locationExpressionLanguage;
   protected String             m_description;
   protected SemanticElement    m_semanticElement;
   protected MessageSyntaxModel m_syntaxModel;
   protected Node               m_parentNode;
   protected String             m_fieldName;

   public boolean isSyntacticField() {
      String fieldName = getFieldName();
      return fieldName != null && fieldName.length() > 0;
   }

   public String getFieldName() {
      return m_fieldName;
   }

   public void setFieldName( String fieldName ) {
      m_fieldName = fieldName;
   }

   public MessageSyntaxModel getSyntaxModel() {
      return m_parentNode != null ? m_parentNode.getSyntaxModel() : m_syntaxModel;
   }

   public void setSyntaxModel( MessageSyntaxModel syntaxModel ) {
      m_syntaxModel = syntaxModel;
   }

   public String getName() {
      return m_name;
   }

   public void setName( String name ) {
      m_name = name;
   }

   public String getLocation() {
      return m_location;
   }

   public void setLocation( String location ) {
      m_location = location;
   }

   public String getLocationExpressionLanguage() {
      return m_locationExpressionLanguage;
   }

   public void setLocationExpressionLanguage( String locationExpressionLanguage ) {
      m_locationExpressionLanguage = locationExpressionLanguage;
   }

   public String getDescription() {
      return m_description;
   }

   public void setDescription( String description ) {
      m_description = description;
   }

   public void setParentNode( Node parent ) {
      m_parentNode = parent;
   }

   public Node getParentNode() {
      return m_parentNode;
   }

   public SemanticElement getSemanticElement() {
      return m_semanticElement;
   }

   public void setSemanticElement( SemanticElement semanticElement ) {
      m_semanticElement = semanticElement;
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

   public String getCardinality() {
      return m_minOccurs + ".." + (m_maxOccurs < 0 ? "*" : m_maxOccurs);
   }
   
   public boolean isSingle() {
      return m_maxOccurs == 1;
   }
   
   public boolean isRequired() {
      return 1 <= m_minOccurs;
   }

   @Override
   public String toString() {
      StringBuffer out = new StringBuffer();
      toString(out, "");
      return out.toString();
   }

   protected void toString( StringBuffer out, String indent ) {
      if( m_description != null && m_description.length() > 0 )
         out.append(indent + "description: " + m_description + "\r\n");
      if( m_location != null && m_location.length() > 0 )
         out.append(indent + "location: " + m_location + "\r\n");
      if( m_locationExpressionLanguage != null && m_locationExpressionLanguage.length() > 0 )
         out.append(indent + "location expression language: " + m_locationExpressionLanguage + "\r\n");
      if( m_semanticElement != null )
         out.append(indent + "semantic element: " + m_semanticElement.getName() + "\r\n");
      if( m_fieldName != null && m_fieldName.length() > 0 )
         out.append(indent + "field name: " + m_fieldName + "\r\n");
   }
   
 

	public void addField( String fieldName, String fieldLocation, String expressionLanguage ) {
	   // TODO Auto-generated method stub
	   
   }
} // Node
