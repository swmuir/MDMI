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
import org.openhealthtools.mdht.mdmi.model.enums.*;

/**
 * 
 */
public class SemanticElement implements IClassifier {
   private String                            m_name;
   private SemanticElementType               m_elementType       = SemanticElementType.NORMAL;
   private String                            m_description;
   private MdmiDatatype                      m_datatype;
   private boolean                           m_multipleInstances = false;
   private SemanticElement                   m_parent;
   private String                            m_ordering;
   private String                            m_orderingLanguage;
   private String                            m_enumValueSetField;
   private String                            m_enumValueField;
   private String                            m_enumValueDescrField;
	private String                            m_enumValueSet;
   private MdmiExpression                    m_computedValue;
   private MdmiExpression                    m_computedInValue;
   private MdmiExpression                    m_computedOutValue;
   private Node                              m_syntaxNode;
   private SemanticElementSet                m_elementSet;
   private SimpleMessageComposite            m_composite;
   private List<DataRule>                    m_dataRules         = new ArrayList<DataRule>();
   private List<SemanticElementBusinessRule> m_businessRules     = new ArrayList<SemanticElementBusinessRule>();
   private List<ToMessageElement>            m_toMdmi            = new ArrayList<ToMessageElement>();
   private List<ToBusinessElement>           m_fromMdmi          = new ArrayList<ToBusinessElement>();
   private List<SemanticElementRelationship> m_relationships     = new ArrayList<SemanticElementRelationship>();
   private List<SemanticElement>             m_childList         = new ArrayList<SemanticElement>();
   private List<MEPropertyQualifierInstance> m_qualifiers        = new ArrayList<MEPropertyQualifierInstance>();
   private HashMap<String, Object>           m_userData          = new HashMap<String, Object>();
   private VALUE                             m_value;

   public List<MEPropertyQualifierInstance> getProperyQualifiers() {
      return m_qualifiers;
   }

   public void addPropertyQualifier( MEPropertyQualifierInstance qual ) {
      m_qualifiers.add(qual);
   }

   public void addChild( SemanticElement msgElem ) {
      m_childList.add(msgElem);
   }

   public Collection<SemanticElement> getChildren() {
      return m_childList;
   }

   public boolean hasChild( SemanticElement potentialChild ) {
      for( int i = 0; i < m_childList.size(); i++ ) {
         if( potentialChild == m_childList.get(i) )
            return true;
      }
      return false;
   }
   
   public SemanticElement getChild( String elementName ) {
      if( elementName == null ) {
         return null;
      }

      for( SemanticElement curElem : getChildren() ) {
         if( elementName.equals(curElem.getName()) ) {
            return curElem;
         }
      }

      return null;
   }

   public String getName() {
      return m_name;
   }

   public void setName( String elementName ) {
      m_name = elementName;
   }

   public SemanticElementType getSemanticElementType() {
      return m_elementType;
   }

   public void setSemanticElementType( SemanticElementType elementType ) {
      m_elementType = elementType;
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
      m_value = m_datatype == null ? null : new VALUE(this);
   }

   public boolean isMultipleInstances() {
      return m_multipleInstances;
   }

   public void setMultipleInstances( boolean multipleInstances ) {
      m_multipleInstances = multipleInstances;
   }

   public SemanticElement getParent() {
      return m_parent;
   }

   public void setParent( SemanticElement parent ) {
      m_parent = parent;
   }

   public String getOrdering() {
      return m_ordering;
   }

   public void setOrdering( String ordering ) {
      m_ordering = ordering;
   }

   public String getOrderingLanguage() {
      return m_orderingLanguage;
   }

   public void setOrderingLanguage( String orderingLanguage ) {
      m_orderingLanguage = orderingLanguage;
   }

   public String getEnumValueSetField() {
		return m_enumValueSetField;
	}

	public void setEnumValueSetField( String enumValueSetField ) {
		this.m_enumValueSetField = enumValueSetField;
	}

	public String getEnumValueField() {
		return m_enumValueField;
	}

	public void setEnumValueField( String enumValueField ) {
		this.m_enumValueField = enumValueField;
	}

	public String getEnumValueDescrField() {
		return m_enumValueDescrField;
	}

	public void setEnumValueDescrField( String enumValueDescrField ) {
		this.m_enumValueDescrField = enumValueDescrField;
	}

	public String getEnumValueSet() {
		return m_enumValueSet;
	}

	public void setEnumValueSet( String m_enumValueSet ) {
		this.m_enumValueSet = m_enumValueSet;
	}

	public MdmiExpression getComputedValue() {
      return m_computedValue;
   }

   public void setComputedValue( MdmiExpression computedValue ) {
      m_computedValue = computedValue;
   }

   public MdmiExpression getComputedInValue() {
      return m_computedInValue;
   }

   public void setComputedInValue( MdmiExpression computedInValue ) {
      m_computedInValue = computedInValue;
   }

   public MdmiExpression getComputedOutValue() {
      return m_computedOutValue;
   }

   public void setComputedOutValue( MdmiExpression computedOutValue ) {
      m_computedOutValue = computedOutValue;
   }

   public Node getSyntaxNode() {
      return m_syntaxNode;
   }

   public void setSyntaxNode( Node node ) {
      m_syntaxNode = node;
   }

   public SemanticElementSet getElementSet() {
      return m_elementSet;
   }

   public void setElementSet( SemanticElementSet elementSet ) {
      m_elementSet = elementSet;
   }

   public SimpleMessageComposite getComposite() {
      return m_composite;
   }

   public void setComposite( SimpleMessageComposite composite ) {
      m_composite = composite;
   }

   public Collection<DataRule> getDataRules() {
      return m_dataRules;
   }

   public void addDataRule( DataRule dataRule ) {
      m_dataRules.add(dataRule);
   }

   public Collection<SemanticElementBusinessRule> getBusinessRules() {
      return m_businessRules;
   }

   public void addBusinessRule( SemanticElementBusinessRule businessRule ) {
      m_businessRules.add(businessRule);
   }

   public Collection<ToMessageElement> getToMdmi() {
      return m_toMdmi;
   }

   public void addToMdmi( ToMessageElement toMdmi ) {
      m_toMdmi.add(toMdmi);
   }

   public Collection<ToBusinessElement> getFromMdmi() {
      return m_fromMdmi;
   }

   public void addFromMdmi( ToBusinessElement fromMdmi ) {
      m_fromMdmi.add(fromMdmi);
   }

   public Collection<SemanticElementRelationship> getRelationships() {
      return m_relationships;
   }

   public void addRelationship( SemanticElementRelationship relationship ) {
      m_relationships.add(relationship);
   }

   public boolean isComputed() {
      if( m_computedValue == null )
         return false;
      String x = m_computedValue.getExpression();
      return x != null && x.length() > 0;
   }

   public boolean isComputedIn() {
      if( m_computedInValue == null )
         return false;
      String x = m_computedInValue.getExpression();
      return x != null && x.length() > 0;
   }

   public boolean isComputedOut() {
      if( m_computedOutValue == null )
         return false;
      String x = m_computedOutValue.getExpression();
      return x != null && x.length() > 0;
   }
   
   public boolean usesValueSet() {
   	return null != m_enumValueField && 0 < m_enumValueField.length();
   }

   @Override
   public IAttribute getAttributeByName( String name, boolean includeInherited ) {
      if( m_datatype != null ) {
         if( m_datatype.isSimple() ) {
            if( VALUE_NAME.equals(name) )
               return m_value;
         }
         else {
            IAttribute a = m_datatype.getAttributeByName(name, includeInherited);
            if( a != null )
               return a;
         }
      }
      for( SemanticElementRelationship r : m_relationships ) {
         if( name.equals(r.getName()) )
            return r;
      }
      for( ToBusinessElement e : m_fromMdmi ) {
         if( name.equals(e.getName()) )
            return e;
      }
      for( ToMessageElement e : m_toMdmi ) {
         if( name.equals(e.getName()) )
            return e;
      }
      return null;
   }

   @Override
   public List<IAttribute> getAttributes( boolean includeInherited ) {
      ArrayList<IAttribute> a = new ArrayList<IAttribute>();
      if( m_datatype != null ) {
         if( m_datatype.isSimple() ) {
            a.add(m_value);
         }
         else {
            a.addAll(m_datatype.getAttributes(includeInherited));
         }
      }
      for( SemanticElementRelationship r : m_relationships ) {
         a.add(r);
      }
      for( ToBusinessElement e : m_fromMdmi ) {
         a.add(e);
      }
      for( ToMessageElement e : m_toMdmi ) {
         a.add(e);
      }
      return a;
   }

   @Override
   public boolean hasAttribute( String name ) {
      List<IAttribute> a = getAttributes(false);
      for( IAttribute t : a ) {
         if( t.getName().equals(name) )
            return true;
      }
      return false;
   }

   @Override
   public boolean hasStaticAttributes() {
      for( SemanticElementRelationship r : m_relationships ) {
         if( r.isStatic() )
            return true;
      }
      return false;
   }

   @Override
   public boolean isEnumeration() {
      if( m_datatype != null )
         return m_datatype.isEnum();
      return false;
   }

   @Override
   public IPackage getContainingPackage() {
      return m_elementSet;
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
      return m_elementSet.getName() + "." + m_name;
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

   public static final String VALUE_NAME = "value";

   public static final class VALUE implements IAttribute {
      private SemanticElement         m_owner;
      private HashMap<String, Object> m_userData = new HashMap<String, Object>();

      private VALUE( SemanticElement owner ) {
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
         return m_owner.m_datatype;
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
      out.append(indent + "SemanticElement: " + m_name + "\r\n");
      indent += "  ";
      if( m_description != null && m_description.length() > 0 )
         out.append(indent + "description: " + m_description + "\r\n");
      if( m_elementType != null )
         out.append(indent + "element type: " + m_elementType + "\r\n");
      if( m_datatype != null )
         out.append(indent + "datatype: " + m_datatype.getTypeName() + "\r\n");
      if( m_multipleInstances )
         out.append(indent + "multiple instances: true\r\n");
      if( m_parent != null )
         out.append(indent + "parent: " + m_parent.getName() + "\r\n");
      if( m_ordering != null && m_ordering.length() > 0 )
         out.append(indent + "ordering: " + m_ordering + "\r\n");
      if( m_orderingLanguage != null && m_orderingLanguage.length() > 0 )
         out.append(indent + "ordering expression language: " + m_orderingLanguage + "\r\n");
      if( m_computedValue != null )
         m_computedValue.toString(out, indent);
      if( m_computedInValue != null )
         m_computedInValue.toString(out, indent);
      if( m_computedOutValue != null )
         m_computedOutValue.toString(out, indent);
      if( m_syntaxNode != null )
         out.append(indent + "syntax node: " + m_syntaxNode.getName() + "\r\n");

      if( m_dataRules.size() > 0 ) {
         out.append(indent + "DATA RULES: ");
         for( int i = 0; i < m_dataRules.size(); i++ ) {
            DataRule x = m_dataRules.get(i);
            if( i > 0 )
               out.append(", ");
            out.append(x.getName());
         }
         out.append("\r\n");
      }
      for( int i = 0; i < m_businessRules.size(); i++ ) {
         SemanticElementBusinessRule x = m_businessRules.get(i);
         x.toString(out, indent);
      }
      for( int i = 0; i < m_toMdmi.size(); i++ ) {
         ToMessageElement x = m_toMdmi.get(i);
         x.toString(out, indent);
      }
      for( int i = 0; i < m_fromMdmi.size(); i++ ) {
         ToBusinessElement x = m_fromMdmi.get(i);
         x.toString(out, indent);
      }
      for( int i = 0; i < m_relationships.size(); i++ ) {
         SemanticElementRelationship x = m_relationships.get(i);
         x.toString(out, indent);
      }
      for( int i = 0; i < m_qualifiers.size(); i++ ) {
         MEPropertyQualifierInstance x = m_qualifiers.get(i);
         x.toString(out, indent);
      }
      Collection<SemanticElement> c = getChildren();
      if( c.size() > 0 ) {
         int i = 0;
         out.append(indent + "children: ");
         for( Iterator<SemanticElement> it = c.iterator(); it.hasNext(); ) {
            SemanticElement x = it.next();
            if( i++ <= 0 )
               out.append(", ");
            out.append(x.getName());
         }
         out.append("\r\n");
      }
   }

   @Override
   public ElementType getElementType() {
      return ElementType.Classifier;
   }
} // SemanticElement
