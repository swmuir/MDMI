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
 * An instance of a semantic element at runtime, holding its value. May hold a simple value or a complex one.
 * 
 * @author goancea
 */
public interface IElementValue {
   /**
    * The semantic element that describes this value.
    * 
    * @return The semantic element that describes this value.
    */
   public SemanticElement getSemanticElement();

   /**
    * The element name.
    * 
    * @return The element name.
    */
   public String getName();

   /**
    * The parent element value.
    * 
    * @return The parent element value.
    */
   public IElementValue getParent();

   /**
    * Set the parent element value.
    * 
    * @param parent The new parent element value.
    */
   public void setParent( IElementValue parent );

   /** 
    * Get the children of this element value.
    * 
    * @return The children of this element value.
    */
   public ArrayList<IElementValue> getChildren();

   /**
    * Get the total number of children.
    * 
    * @return The total number of children.
    */
   public int getChildCount();
   
   /**
    * Add the child to the children array and set the parent to this.
    * 
    * @param child The child to add.
    */
   public void addChild( IElementValue child );

   /**
    * Get the related element values of this.
    * 
    * @return The related element values of this.
    */
   public ArrayList<IElementValue> getRelations();

   /**
    * Get the actual value of this element value. May be a simple or complex type.
    * 
    * @return The actual value of this element value.
    */
   public IValue getValue();

   /**
    * Return true if this element value is of a simple type.
    * 
    * @return True if this element value is of a simple type.
    */
   public boolean isSimple();

   /**
    * Get the owner element value set.
    * 
    * @return The owner element value set.
    */
   public ElementValueSet getOwner();
} // IElementValue
