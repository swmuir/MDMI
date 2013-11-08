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
 * 
 */
public class SemanticElementRelationship implements IAttribute {
   private String                  m_name;
   private String                  m_description;
   private String                  m_rule;
   private String                  m_ruleExpressionLanguage;
   private boolean                 m_sourceIsInstance = true;
   private boolean                 m_targetIsInstance = true;
   private SemanticElement         m_context;
   private SemanticElement         m_relatedSemanticElement;
   private int                     m_minOccurs        = 1;
   private int                     m_maxOccurs        = 1;
   private HashMap<String, Object> m_userData         = new HashMap<String, Object>();

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

   public String getActualRuleExpressionLanguage() {
      if( null != m_ruleExpressionLanguage && 0 < m_ruleExpressionLanguage.length() )
         return m_ruleExpressionLanguage;
      try {
         return m_context.getElementSet().getModel().getGroup().getDefaultRuleExprLang();
      }
      catch( Exception ex ) {
         return null;
      }
   }

   public boolean isSourceIsInstance() {
      return m_sourceIsInstance;
   }

   public void setSourceIsInstance( boolean sourceIsInstance ) {
      m_sourceIsInstance = sourceIsInstance;
   }

   public boolean isTargetIsInstance() {
      return m_targetIsInstance;
   }

   public void setTargetIsInstance( boolean targetIsInstance ) {
      m_targetIsInstance = targetIsInstance;
   }

   public SemanticElement getContext() {
      return m_context;
   }

   public void setContext( SemanticElement context ) {
      m_context = context;
   }

   public SemanticElement getRelatedSemanticElement() {
      return m_relatedSemanticElement;
   }

   public void setRelatedSemanticElement( SemanticElement relatedSemanticElement ) {
      m_relatedSemanticElement = relatedSemanticElement;
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

   public String getDescription() {
      return m_description;
   }

   public void setDescription( String description ) {
      m_description = description;
   }

   @Override
   public List<String> getDocumentation() {
      return null;
   }

   @Override
   public String getName() {
      return m_name;
   }

   public void setName( String name ) {
      m_name = name;
   }

   @Override
   public String getOriginalName() {
      return getName();
   }

   @Override
   public IClassifier getOwner() {
      return m_context;
   }

   @Override
   public IModelElement getType() {
      return m_relatedSemanticElement;
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
      return !m_sourceIsInstance;
   }

   @Override
   public String toString() {
      StringBuffer out = new StringBuffer();
      toString(out, "");
      return out.toString();
   }

   protected void toString( StringBuffer out, String indent ) {
      out.append(indent + "SemanticElementRelationship: " + m_name + "\r\n");
      indent += "  ";
      if( m_description != null && m_description.length() > 0 )
         out.append(indent + "description: " + m_description + "\r\n");
      if( m_rule != null && m_rule.length() > 0 )
         MessageGroup.appendML(out, indent, "rule: ", m_rule);
      if( m_ruleExpressionLanguage != null && m_ruleExpressionLanguage.length() > 0 )
         out.append(indent + "rule expression language: " + m_ruleExpressionLanguage + "\r\n");
      if( !m_sourceIsInstance )
         out.append(indent + "source is instance: false\r\n");
      if( !m_targetIsInstance )
         out.append(indent + "target is instance: false\r\n");
      if( m_relatedSemanticElement != null )
         out.append(indent + "related semantic element: " + m_relatedSemanticElement.getName() + "\r\n");
      out.append(indent + "multiplicity: [" + m_minOccurs + ".." + (m_maxOccurs < 0 ? "*" : m_maxOccurs) + "]\r\n");
   }

   @Override
   public boolean isRepeating() {
      return m_maxOccurs < 0 || 1 < m_maxOccurs;
   }
} // SemanticElementRelationship
