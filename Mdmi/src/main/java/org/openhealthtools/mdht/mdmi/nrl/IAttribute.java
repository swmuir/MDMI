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

public interface IAttribute {
   public static final int UNBOUNDED = -1;

   @SuppressWarnings( "rawtypes" )
   public abstract java.util.List getDocumentation();

   public abstract String getName();

   public abstract int getMinOccurs();

   public abstract int getMaxOccurs();

   public abstract String getOriginalName();

   public abstract IClassifier getOwner();

   public abstract Object getUserData( String arg0 );

   public abstract IModelElement getType();

   public abstract boolean isRepeating();

   public abstract boolean isStatic();

   public abstract void setUserData( String arg0, Object arg1 );
}
