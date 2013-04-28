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
public class DataRule {
   private String             m_name;
   private String             m_description;
   private String             m_rule;
   private String             m_ruleExpressionLanguage;
   private List<MdmiDatatype> m_datatypes = new ArrayList<MdmiDatatype>();
   private MessageGroup       m_scope;
   private SemanticElement    m_semanticElement;

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

   public String getRule() {
      return m_rule;
   }

   public void setRule( String rule ) {
      m_rule = rule;
   }

   public String getRuleExpressionLanguage() {
      return m_ruleExpressionLanguage;
   }

   public void setRuleExpressionLanguage( String ruleExpressionLanguage ) {
      m_ruleExpressionLanguage = ruleExpressionLanguage;
   }

   public Collection<MdmiDatatype> getDatatypes() {
      return m_datatypes;
   }

   public void addDatatype( MdmiDatatype datatype ) {
      m_datatypes.add(datatype);
   }

   public MessageGroup getScope() {
      return m_scope;
   }

   public void setScope( MessageGroup scope ) {
      m_scope = scope;
   }

   public SemanticElement getSemanticElement() {
      return m_semanticElement;
   }

   public void setSemanticElement( SemanticElement semanticElement ) {
      m_semanticElement = semanticElement;
   }

   @Override
   public String toString() {
      StringBuffer out = new StringBuffer();
      toString(out, "");
      return out.toString();
   }

   protected void toString( StringBuffer out, String indent ) {
      out.append(indent + "DataRule: " + m_name + "\r\n");
      indent += "  ";
      if( m_description != null && m_description.length() > 0 )
         out.append(indent + "description: " + m_description + "\r\n");
      if( m_rule != null && m_rule.length() > 0 )
         MessageGroup.appendML(out, indent, "rule: ", m_rule);
      if( m_ruleExpressionLanguage != null && m_ruleExpressionLanguage.length() > 0 )
         out.append(indent + "rule expression language: " + m_ruleExpressionLanguage + "\r\n");
      if( m_semanticElement != null )
         out.append(indent + "semantic element: " + m_semanticElement.getName() + "\r\n");
      out.append(indent + "datatypes: ");
      for( int i = 0; i < m_datatypes.size(); i++ ) {
         MdmiDatatype x = m_datatypes.get(i);
         if( i > 0 )
            out.append(", ");
         out.append(x.getTypeName());
      }
      out.append("\r\n");
   }
} // DataRule
