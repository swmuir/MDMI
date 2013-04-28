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
public class MessageGroup implements IPackage {
   private String                        m_name;
   private String                        m_description;
   private String                        m_defaultLocationExprLang;
   private String                        m_defaultConstraintExprLang;
   private String                        m_defaultRuleExprLang;
   private ArrayList<MessageModel>       m_modelList = new ArrayList<MessageModel>();
   private List<DataRule>                m_dataRules = new ArrayList<DataRule>();
   private List<MdmiDatatype>            m_datatypes = new ArrayList<MdmiDatatype>();
   private MdmiDomainDictionaryReference m_domainDictionary;
   protected HashMap<String, Object>     m_userData  = new HashMap<String, Object>();

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

   public String getDefaultLocationExprLang() {
      return m_defaultLocationExprLang;
   }

   public void setDefaultLocationExprLang( String defaultLocationExprLang ) {
      m_defaultLocationExprLang = defaultLocationExprLang;
   }

   public String getDefaultConstraintExprLang() {
      return m_defaultConstraintExprLang;
   }

   public void setDefaultConstraintExprLang( String defaultConstraintExprLang ) {
      m_defaultConstraintExprLang = defaultConstraintExprLang;
   }

   public String getDefaultRuleExprLang() {
      return m_defaultRuleExprLang;
   }

   public void setDefaultRuleExprLang( String defaultRuleExprLang ) {
      m_defaultRuleExprLang = defaultRuleExprLang;
   }

   public MessageModel getModel( String name ) {
      if( name == null ) {
         return null;
      }

      for( MessageModel curModel : m_modelList ) {
         if( name.equals(curModel.getMessageModelName()) ) {
            return curModel;
         }
      }

      return null;
   }

   public Collection<MessageModel> getModels() {
      return m_modelList;
   }

   public void addModel( MessageModel model ) {
      m_modelList.add(model);
   }

   public Collection<DataRule> getDataRules() {
      return m_dataRules;
   }

   public void addDataRule( DataRule dataRule ) {
      m_dataRules.add(dataRule);
   }

   public Collection<MdmiDatatype> getDatatypes() {
      return m_datatypes;
   }

   public void addDatatype( MdmiDatatype datatype ) {
      m_datatypes.add(datatype);
   }

   public MdmiDatatype getDatatype( String name ) {
      for( int i = 0; i < m_datatypes.size(); i++ ) {
         MdmiDatatype dt = m_datatypes.get(i);
         if( dt.getName().equals(name) )
            return dt;
      }
      return null;
   }

   public MdmiDomainDictionaryReference getDomainDictionary() {
      return m_domainDictionary;
   }

   public void setDomainDictionary( MdmiDomainDictionaryReference domainDictionary ) {
      m_domainDictionary = domainDictionary;
   }

   @Override
   public List<IModelElement> getContents( boolean deep ) {
      ArrayList<IModelElement> a = new ArrayList<IModelElement>();
      for( MdmiDatatype dt : m_datatypes ) {
         a.add(dt);
      }
      return a;
   }

   @Override
   public IModelElement getElementByName( String name, boolean deep ) {
      for( MdmiDatatype dt : m_datatypes ) {
         if( dt.getName().equals(name) )
            return dt;
      }
      return null;
   }

   @Override
   public int getSize() {
      // TODO Auto-generated method stub
      return 0;
   }

   @Override
   public boolean isAmbiguous( String name ) {
      return false;
   }

   @Override
   public IPackage getContainingPackage() {
      return null;
   }

   @Override
   public List<IModelElement> getDescendants( boolean arg0 ) {
      return null;
   }

   @Override
   public List<String> getDocumentation() {
      return null;
   }

   @Override
   public String getOriginalName() {
      return getName();
   }

   @Override
   public IModelElement getParent() {
      return null;
   }

   @Override
   public String getQualifiedName() {
      return getName();
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
      return false;
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
      out.append(indent + "MessageGroup: " + m_name + "\r\n");
      indent += "  ";
      if( m_description != null && m_description.length() > 0 )
         out.append(indent + "description: " + m_description + "\r\n");
      if( m_defaultLocationExprLang != null && m_defaultLocationExprLang.length() > 0 )
         out.append(indent + "default location expression language: " + m_defaultLocationExprLang + "\r\n");
      if( m_defaultConstraintExprLang != null && m_defaultConstraintExprLang.length() > 0 )
         out.append(indent + "default constraint expression language: " + m_defaultConstraintExprLang + "\r\n");
      if( m_defaultRuleExprLang != null && m_defaultRuleExprLang.length() > 0 )
         out.append(indent + "default rule expression language: " + m_defaultRuleExprLang + "\r\n");
      if( m_domainDictionary != null )
         m_domainDictionary.toString(out, indent);
      if( m_datatypes.size() > 0 ) {
         out.append(indent + "DATATYPES:\r\n");
         for( int i = 0; i < m_datatypes.size(); i++ ) {
            MdmiDatatype x = m_datatypes.get(i);
            x.toString(out, indent + "  ");
         }
      }
      if( m_dataRules.size() > 0 ) {
         out.append(indent + "DATA RULES:\r\n");
         for( int i = 0; i < m_dataRules.size(); i++ ) {
            DataRule x = m_dataRules.get(i);
            x.toString(out, indent + "  ");
         }
      }

      Collection<MessageModel> c = getModels();
      if( c.size() > 0 ) {
         out.append(indent + "MODELS:\r\n");
         for( Iterator<MessageModel> i = c.iterator(); i.hasNext(); ) {
            MessageModel x = i.next();
            x.toString(out, indent + "  ");
         }
      }
   }

   static void appendML( StringBuffer out, String indent, String name, String text ) {
      ArrayList<String> a = tokenizeText(text);
      if( a.size() == 0 )
         out.append(indent + name + "\r\n");
      else if( a.size() == 1 )
         out.append(indent + name + a.get(0) + "\r\n");
      else {
         int n = indent.length() + name.length();
         StringBuffer sb = new StringBuffer(n);
         for( int i = 0; i < n; i++ ) {
            sb.append(' ');
         }
         String h = sb.toString();
         out.append(indent + name + a.get(0) + "\r\n");
         for( int i = 1; i < a.size(); i++ ) {
            out.append(h + a.get(i) + "\r\n");
         }
      }

   }

   private static ArrayList<String> tokenizeText( String text ) {
      text = text.replace("\r\n", "\n"); // CRLF -> LF
      text = text.replace("\r", "\n"); // CR -> LF
      ArrayList<String> a = new ArrayList<String>();
      while( text != null && text.length() > 0 )
         text = readOneLine(text, a);
      return a;
   }

   // delimited by LF
   private static String readOneLine( String text, ArrayList<String> lines ) {
      if( text == null || text.length() <= 0 )
         return null;

      int pos = text.indexOf('\n');
      if( pos < 0 ) { // end was reached
         lines.add(text);
         return "";
      }

      lines.add(text.substring(0, pos));
      if( text.length() == pos + 1 )
         return "";
      return text.substring(pos + 1);
   }

   @Override
   public ElementType getElementType() {
      return ElementType.Package;
   }
} // MessageGroup
