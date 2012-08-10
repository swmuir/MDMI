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
public class MdmiBusinessElementReference implements IClassifier {
   private String                        m_name;
   private String                        m_description;
   private URI                           m_reference;
   private String                        m_uniqueIdentifier;
   private MdmiDomainDictionaryReference m_domainDictionaryReference;
   private List<MdmiBusinessElementRule> m_businessRules;
   private MdmiDatatype                  m_referenceDatatype;
   protected boolean                     m_isReadonly;
   private HashMap<String, Object>       m_userData;
   private VALUE                         m_value;

   public MdmiBusinessElementReference() {
      m_businessRules = new ArrayList<MdmiBusinessElementRule>();
      m_userData = new HashMap<String, Object>();
   }

   public MdmiDatatype getReferenceDatatype() {
      return m_referenceDatatype;
   }

   public void setReferenceDatatype( MdmiDatatype datatype ) {
      m_referenceDatatype = datatype;
   }

   public MdmiDomainDictionaryReference getDomainDictionaryReference() {
      return m_domainDictionaryReference;
   }

   public void setDomainDictionaryReference( MdmiDomainDictionaryReference domainDictionaryReference ) {
      m_domainDictionaryReference = domainDictionaryReference;
   }

   public Collection<MdmiBusinessElementRule> getBusinessRules() {
      return m_businessRules;
   }

   public void addBusinessRule( MdmiBusinessElementRule businessRule ) {
      m_businessRules.add(businessRule);
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

   public URI getReference() {
      return m_reference;
   }

   public void setReference( URI reference ) {
      m_reference = reference;
   }

   public String getUniqueIdentifier() {
      return m_uniqueIdentifier;
   }

   public void setUniqueIdentifier( String identifier ) {
      m_uniqueIdentifier = identifier;
   }

   public boolean isReadonly() {
      return m_isReadonly;
   }

   public void setReadonly( boolean isReadonly ) {
      m_isReadonly = isReadonly;
   }

   @Override
   public IAttribute getAttributeByName( String name, boolean includeInherited ) {
      if( m_referenceDatatype != null ) {
         if( m_referenceDatatype.isSimple() ) {
            if( VALUE_NAME.equals(name) )
               return m_value;
         }
         else {
            IAttribute a = m_referenceDatatype.getAttributeByName(name, includeInherited);
            if( a != null )
               return a;
         }
      }
      return null;
   }

   @Override
   public List<IAttribute> getAttributes( boolean includeInherited ) {
      ArrayList<IAttribute> a = new ArrayList<IAttribute>();
      if( m_referenceDatatype != null ) {
         if( m_referenceDatatype.isSimple() ) {
            a.add(m_value);
         }
         else {
            a.addAll(m_referenceDatatype.getAttributes(includeInherited));
         }
      }
      return a;
   }

   @Override
   public boolean hasAttribute( String name ) {
      return VALUE_NAME.equals(name);
   }

   @Override
   public boolean hasStaticAttributes() {
      return false;
   }

   @Override
   public boolean isEnumeration() {
      if( m_referenceDatatype != null )
         return m_referenceDatatype.isEnum();
      return false;
   }

   @Override
   public IPackage getContainingPackage() {
      return m_domainDictionaryReference;
   }

   @Override
   public List<IModelElement> getDescendants( boolean deep ) {
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
   public String getQualifiedName() {
      return m_domainDictionaryReference.getName() + "." + m_name;
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
      return src instanceof SemanticElement;
   }

   @Override
   public boolean isSupplementary() {
      return false;
   }

   @Override
   public IModelElement getParent() {
      return null;
   }

   public static final String VALUE_NAME = "value";

   public static final class VALUE implements IAttribute {
      private MdmiBusinessElementReference m_owner;
      private HashMap<String, Object>      m_userData = new HashMap<String, Object>();

      private VALUE( MdmiBusinessElementReference owner ) {
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
      public String getName() {
         return VALUE_NAME;
      }

      @Override
      public String getOriginalName() {
         return VALUE_NAME;
      }

      @Override
      public IClassifier getOwner() {
         return m_owner;
      }

      @Override
      public IModelElement getType() {
         return m_owner.m_referenceDatatype;
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
   }

   @Override
   public String toString() {
      StringBuffer out = new StringBuffer();
      toString(out, "");
      return out.toString();
   }

   protected void toString( StringBuffer out, String indent ) {
      out.append(indent + "MdmiBusinessElementReference: " + m_name + "\r\n");
      indent += "  ";
      if( m_description != null && m_description.length() > 0 )
         out.append(indent + "description: " + m_description + "\r\n");
      if( m_reference != null )
         out.append(indent + "reference: " + m_reference + "\r\n");
      if( m_uniqueIdentifier != null && m_uniqueIdentifier.length() > 0 )
         out.append(indent + "unique identifier: " + m_uniqueIdentifier + "\r\n");
      if( m_referenceDatatype != null )
         out.append(indent + "reference datatype: " + m_referenceDatatype.getTypeName() + "\r\n");
      for( int i = 0; i < m_businessRules.size(); i++ ) {
         MdmiBusinessElementRule x = m_businessRules.get(i);
         x.toString(out, indent + "  ");
      }
      MessageGroup g = m_domainDictionaryReference.getMessageGroup();
      out.append(indent + "ToBusinessElement (FromMdmi) rules {\r\n");
      Collection<MessageModel> models = g.getModels();
      for( Iterator<MessageModel> it = models.iterator(); it.hasNext(); ) {
         MessageModel m = it.next();
         ArrayList<SemanticElement> a = getSourceSemanticElements(m);
         if( a.size() > 0 ) {
            out.append(indent + "  Model " + m.getMessageModelName() + ": ");
            for( int i = 0; i < a.size(); i++ ) {
               SemanticElement se = a.get(i);
               if( i > 0 )
                  out.append(", ");
               out.append(se.getName());
            }
            out.append("\r\n");
         }
      }
      out.append(indent + "}\r\n");
      out.append(indent + "ToMessageElement (ToMdmi) rules {\r\n");
      models = g.getModels();
      for( Iterator<MessageModel> it = models.iterator(); it.hasNext(); ) {
         MessageModel m = it.next();
         ArrayList<SemanticElement> a = getTargetSemanticElements(m);
         if( a.size() > 0 ) {
            out.append(indent + "  Model " + m.getMessageModelName() + ": ");
            for( int i = 0; i < a.size(); i++ ) {
               SemanticElement se = a.get(i);
               if( i > 0 )
                  out.append(", ");
               out.append(se.getName());
            }
            out.append("\r\n");
         }
      }
      out.append(indent + "}\r\n");
   }

   /**
    * Get a list of all Semantic Elements (SEs) that have a ToBusinessElement rule for this BER, within the specified
    * MessageModel.
    * 
    * @param model The message model.
    * @return The list of all Semantic Elements (SEs) that have a ToBusinessElement rule for this BER, within the
    *         specified MessageModel.
    */
   public ArrayList<SemanticElement> getSourceSemanticElements( MessageModel model ) {
      ArrayList<SemanticElement> a = new ArrayList<SemanticElement>();
      Collection<SemanticElement> srcSEs = model.getElementSet().getSemanticElements();
      for( Iterator<SemanticElement> itSE = srcSEs.iterator(); itSE.hasNext(); ) {
         SemanticElement se = itSE.next();
         Collection<ToBusinessElement> toBEs = se.getFromMdmi();
         for( Iterator<ToBusinessElement> itBE = toBEs.iterator(); itBE.hasNext(); ) {
            ToBusinessElement tbe = itBE.next();
            if( tbe != null && tbe.getBusinessElement() == this ) {
               a.add(se);
               break;
            }
         }
      }
      return a;
   }

   /**
    * Get a list of all Semantic Elements (SEs) that have a ToMessageElement rule for this BER, within the specified
    * MessageModel.
    * 
    * @param model The message model.
    * @return The list of all Semantic Elements (SEs) that have a ToMessageElement rule for this BER, within the
    *         specified MessageModel.
    */
   public ArrayList<SemanticElement> getTargetSemanticElements( MessageModel model ) {
      ArrayList<SemanticElement> a = new ArrayList<SemanticElement>();
      Collection<SemanticElement> srcSEs = model.getElementSet().getSemanticElements();
      for( Iterator<SemanticElement> itSE = srcSEs.iterator(); itSE.hasNext(); ) {
         SemanticElement se = itSE.next();
         Collection<ToMessageElement> toMEs = se.getToMdmi();
         for( Iterator<ToMessageElement> itME = toMEs.iterator(); itME.hasNext(); ) {
            ToMessageElement tme = itME.next();
            if( tme != null && tme.getBusinessElement() == this ) {
               a.add(se);
               break;
            }
         }
      }
      return a;
   }

   @Override
   public ElementType getElementType() {
      return ElementType.Classifier;
   }
} // MdmiBusinessElementReference
