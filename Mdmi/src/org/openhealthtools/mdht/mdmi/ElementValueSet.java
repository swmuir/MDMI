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
package org.openhealthtools.mdht.mdmi;

import java.util.*;

import org.openhealthtools.mdht.mdmi.model.*;

/**
 * Collection of all message element instances in a message, grouped by semantic element.
 */
public final class ElementValueSet {
   private HashMap<String, SEVS> m_xelements = new HashMap<String, SEVS>();

   public HashMap<String, SEVS> getM_xelements() {
   	return m_xelements;
   }

   /**
    * Get all the element values in this element values set.
    * 
    * @return All the element values in this element values set.
    */
   public ArrayList<IElementValue> getAllElementValues() {
      ArrayList<IElementValue> xes = new ArrayList<IElementValue>();
      Collection<SEVS> values = m_xelements.values();
      for( Iterator<SEVS> iterator = values.iterator(); iterator.hasNext(); ) {
         SEVS xse = iterator.next();
         xes.addAll(xse.xelements);
      }
      return xes;
   }

   /**
    * Get all element values of the specified type (for the given semantic element) from this set.
    * 
    * @param semanticElement The semantic element to look for.
    * @return The sub-set of all element values of the specified type.
    */
   public ArrayList<IElementValue> getElementValuesByType( SemanticElement semanticElement ) {
      if( semanticElement == null )
         throw new IllegalArgumentException("Null argument!");
      return getElementValuesByName(semanticElement.getName());
   }

   /**
    * Get all element values of the specified type (for the given semantic element) from this set, 
    * which are children or descendants of the given element value.
    * 
    * @param semanticElement The semantic element to look for.
    * @param thiz The element value at whose descendants we should look at.
    * @return The sub-set of all element values of the specified type, which are descendants of thiz.
    */
   public ArrayList<IElementValue> getChildElementValuesByType( SemanticElement semanticElement, IElementValue thiz ) {
      if( semanticElement == null )
         throw new IllegalArgumentException("Null argument!");
      ArrayList<IElementValue> values = new ArrayList<IElementValue>();
      processChildren(semanticElement, thiz, values);
      return values;
   }

   /**
    * Get all element values of the specified type (for the given semantic element) from this set, 
    * which are direct children of the given element value.
    * 
    * @param semanticElement The semantic element to look for.
    * @param thiz The element value at whose descendants we should look at.
    * @return The sub-set of all element values of the specified type, which are direct descendants of thiz.
    */
   public ArrayList<IElementValue> getDirectChildValuesByType( SemanticElement semanticElement, IElementValue thiz ) {
      if( semanticElement == null )
         throw new IllegalArgumentException("Null argument!");
      ArrayList<IElementValue> values = new ArrayList<IElementValue>();
      ArrayList<IElementValue> children = thiz.getChildren();
      for( IElementValue child : children ) {
         if( child.getSemanticElement() == semanticElement )
            values.add(child);
      }
      return values;
   }

   /**
    * Get all element values of the specified type (for the given semantic element) from this set, 
    * which are either the owner, or owner's descendants of the given element value.
    * It will go up the tree and look at all children of all ancestors of thiz.
    * 
    * @param semanticElement The semantic element to look for.
    * @param thiz The element value at whose owner and ancestors we should look at.
    * @return The sub-set of all element values of the specified type, which are ancestors or ancestors children of thiz.
    */
   public ArrayList<IElementValue> getOwnerElementValuesByType( SemanticElement semanticElement, IElementValue thiz ) {
      if( semanticElement == null )
         throw new IllegalArgumentException("Null argument!");
      ArrayList<IElementValue> values = new ArrayList<IElementValue>();
      IElementValue owner = thiz.getParent();
      if( owner != null )
         processOwner( semanticElement, owner, values, thiz );
      return values;
   }

   /**
    * Get all element values of the specified type (for the given semantic element name) from this set.
    * 
    * @param semanticElementName The semantic element name to look for.
    * @return The sub-set of all element values of the specified type.
    */
   public ArrayList<IElementValue> getElementValuesByName( String semanticElementName ) {
      if( semanticElementName == null )
         throw new IllegalArgumentException("Null argument!");
      ArrayList<IElementValue> a = new ArrayList<IElementValue>();
      SEVS xse = m_xelements.get(semanticElementName);
      if( xse != null )
         a.addAll(xse.xelements);
      return a;
   }

   /**
    * Add the given element value to the set.
    * 
    * @param xelement The element value to add.
    */
   public void addElementValue( IElementValue xelement ) {
      SEVS xse = m_xelements.get(xelement.getSemanticElement().getName());
      if( xse == null ) {
         xse = new SEVS(xelement.getSemanticElement());
         m_xelements.put(xse.getName(), xse);
      }
      if( !xse.xelements.contains(xelement) )
         xse.xelements.add(xelement);
   }

   /**
    * Add the given element values to this set.
    * 
    * @param xelements The element values to add.
    */
   public void addElementValues( ArrayList<IElementValue> xelements ) {
      for( IElementValue xelement : xelements ) {
         addElementValue(xelement);
      }
   }

   @Override
   public String toString() {
      StringBuffer sb = new StringBuffer();
      Collection<SEVS> values = m_xelements.values();
      for( Iterator<SEVS> iterator = values.iterator(); iterator.hasNext(); ) {
         SEVS xse = iterator.next();
         for( int i = 0; i < xse.xelements.size(); i++ ) {
            sb.append(xse.xelements.get(i));
         }
      }
      return sb.toString();
   }
   
   private void processChildren( SemanticElement semanticElement, IElementValue thiz, ArrayList<IElementValue> values ) {
      ArrayList<IElementValue> children = thiz.getChildren();
      for( IElementValue child : children ) {
         if( child.getSemanticElement() == semanticElement )
            values.add(child);
      }
      for( IElementValue child : children ) {
         if( child.getSemanticElement() != semanticElement )
            processChildren( semanticElement, child, values );
      }
   }
   
   private void processOwner( SemanticElement semanticElement, IElementValue owner, ArrayList<IElementValue> values, IElementValue thiz ) {
      ArrayList<IElementValue> children = owner.getChildren();
      for( IElementValue child : children ) {
         if( !child.equals(thiz) && child.getSemanticElement() == semanticElement )
            values.add(child);
      }
      for( IElementValue child : children ) {
         if( !child.equals(thiz) && child.getSemanticElement() != semanticElement )
            processChildren( semanticElement, child, values );
      }
      IElementValue parent = owner.getParent();
      if( parent != null )
         processOwner( semanticElement, parent, values, owner );
   }

   /**
    * All values for one semantic element in a message.
    */
   public static final class SEVS {
      public SemanticElement          semanticElement;
      public ArrayList<IElementValue> xelements;

      SEVS( SemanticElement me ) {
         semanticElement = me;
         xelements = new ArrayList<IElementValue>();
      }

      String getName() {
         return semanticElement.getName();
      }
   } // ElementSet$SEVS
} // ElementSet
