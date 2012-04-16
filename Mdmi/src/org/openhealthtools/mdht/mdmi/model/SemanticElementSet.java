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
public class SemanticElementSet implements IPackage {
   private String                            m_name;
   private String                            m_description;
   private MessageModel                      m_model;
   private MessageSyntaxModel                m_syntaxModel;
   private ArrayList<SimpleMessageComposite> m_compList = new ArrayList<SimpleMessageComposite>();
   private ArrayList<SemanticElement>        m_elemList = new ArrayList<SemanticElement>();
   private HashMap<String, Object>           m_userData = new HashMap<String, Object>();

   public MessageModel getModel() {
      return m_model;
   }

   public void setModel( MessageModel model ) {
      m_model = model;
   }

   public MessageSyntaxModel getSyntaxModel() {
      return m_syntaxModel;
   }

   public void setSyntaxModel( MessageSyntaxModel syntaxModel ) {
      m_syntaxModel = syntaxModel;
   }

   public Collection<SimpleMessageComposite> getComposites() {
      return m_compList;
   }

   public SimpleMessageComposite getComposite( String name ) {
      if( name == null )
         return null;
      for( SimpleMessageComposite curComp : getComposites() ) {
         if( name.equals(curComp.getName()) ) {
            return curComp;
         }
      }
      return null;
   }

   public void addComposite( SimpleMessageComposite composite ) {
      m_compList.add(composite);
   }

   public SemanticElement getSemanticElement( String elementName ) {
      if( elementName == null )
         return null;
      for( SemanticElement curElem : getSemanticElements() ) {
         if( elementName.equals(curElem.getName()) ) {
            return curElem;
         }
      }
      return null;
   }

   public Collection<SemanticElement> getSemanticElements() {
      return m_elemList;
   }

   public void addSemanticElement( SemanticElement semanticElement ) {
      m_elemList.add(semanticElement);
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

   public String getMessageModelName() {
      return m_model == null ? null : m_model.getMessageModelName();
   }

   @Override
   public List<IModelElement> getContents( boolean deep ) {
      ArrayList<IModelElement> mes = new ArrayList<IModelElement>();
      Collection<SemanticElement> values = getSemanticElements();
      for( Iterator<SemanticElement> iterator = values.iterator(); iterator.hasNext(); ) {
         SemanticElement me = iterator.next();
         mes.add(me);
      }
      return mes;
   }

   @Override
   public IModelElement getElementByName( String name, boolean deep ) {
      return getSemanticElement(name);
   }

   @Override
   public int getSize() {
      return getSemanticElements().size();
   }

   @Override
   public boolean isAmbiguous( String arg0 ) {
      return false;
   }

   @Override
   public IPackage getContainingPackage() {
      return null;
   }

   @Override
   public List<IModelElement> getDescendants( boolean transitive ) {
      return new ArrayList<IModelElement>();
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
   public IModelElement getParent() {
      return null;
   }

   @Override
   public String getQualifiedName() {
      return m_name;
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
      return src instanceof SemanticElementSet;
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
      out.append(indent + "SemanticElementSet: " + m_name + "\r\n");
      indent += "  ";
      if( m_description != null && m_description.length() > 0 )
         out.append(indent + "description: " + m_description + "\r\n");
      Collection<SemanticElement> c = getSemanticElements();
      for( Iterator<SemanticElement> i = c.iterator(); i.hasNext(); ) {
         SemanticElement x = i.next();
         x.toString(out, indent);
      }
      Collection<SimpleMessageComposite> d = getComposites();
      for( Iterator<SimpleMessageComposite> i = d.iterator(); i.hasNext(); ) {
         SimpleMessageComposite x = i.next();
         x.toString(out, indent);
      }
   }

   @Override
   public ElementType getElementType() {
      return ElementType.Package;
   }
} // SemanticElementSet
