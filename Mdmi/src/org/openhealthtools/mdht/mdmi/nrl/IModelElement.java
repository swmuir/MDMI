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
package org.openhealthtools.mdht.mdmi.nrl;

public interface IModelElement {
   public abstract IPackage getContainingPackage();

   @SuppressWarnings( "rawtypes" )
   public abstract java.util.List getDocumentation();

   public abstract IModelElement.ElementType getElementType();

   public abstract String getName();

   public abstract IModelElement getParent();

   @SuppressWarnings( "rawtypes" )
   public abstract java.util.List getDescendants( boolean arg0 );

   public abstract String getOriginalName();

   public abstract String getQualifiedName();

   public abstract Object getUserData( String arg0 );

   public abstract boolean isAssignableFrom( IModelElement arg0 );

   public abstract boolean isSupplementary();

   public abstract void setUserData( String arg0, Object arg1 );
   
   public enum ElementType {
      Classifier,
      DataType,
      DataTypeWithAttributes,
      Enumeration,
      Package
   }
}
