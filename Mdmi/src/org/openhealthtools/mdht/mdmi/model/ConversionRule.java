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
public abstract class ConversionRule implements IAttribute {
   protected String                m_name;
   protected String                m_description;
   protected String                m_rule;
   protected String                m_ruleExpressionLanguage;
   protected String                m_enumExtResolverUri;
   protected SemanticElement       m_owner;
   private HashMap<String, Object> m_userData = new HashMap<String, Object>();

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

   public String getEnumExtResolverUri() {
		return m_enumExtResolverUri;
	}

	public void setEnumExtResolverUri( String enumExtResolverUri ) {
		this.m_enumExtResolverUri = enumExtResolverUri;
	}

	public String getActualRuleExpressionLanguage() {
      if( null != m_ruleExpressionLanguage && 0 < m_ruleExpressionLanguage.length() )
         return m_ruleExpressionLanguage;
      try {
         return m_owner.getElementSet().getModel().getGroup().getDefaultRuleExprLang();
      }
      catch( Exception ex ) {
         return null;
      }
   }

   public SemanticElement getOwner() {
      return m_owner;
   }

   public void setOwner( SemanticElement owner ) {
      m_owner = owner;
   }

   @Override
   public List<String> getDocumentation() {
      return null;
   }

   @Override
   public int getMaxOccurs() {
      return 1;
   }

   @Override
   public int getMinOccurs() {
      return 1;
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

   @Override
   public boolean isRepeating() {
      return false;
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
      if( m_rule != null && m_rule.length() > 0 )
         MessageGroup.appendML(out, indent, "rule: ", m_rule);
      if( m_ruleExpressionLanguage != null && m_ruleExpressionLanguage.length() > 0 )
         out.append(indent + "rule expression language: " + m_ruleExpressionLanguage + "\r\n");
   }
} // ConversionRule
