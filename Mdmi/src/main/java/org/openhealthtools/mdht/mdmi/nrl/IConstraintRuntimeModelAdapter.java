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

public interface IConstraintRuntimeModelAdapter {
   @SuppressWarnings( "rawtypes" )
   public abstract java.util.Collection getChildren( Object arg0 );

   @SuppressWarnings( "rawtypes" )
   public abstract java.util.Collection getChildrenByName( Object arg0, String arg1 );

   public abstract Object getFirstChildByName( Object arg0, String arg1 );

   public abstract boolean hasChild( Object arg0, String arg1 );

   public abstract boolean hasChildren( Object arg0 );

   public abstract boolean isAModelObject( Object arg0 );

   public abstract String getName( Object arg0 );

   public abstract IModelElement getType( Object arg0 );

   public abstract Object getValue( Object arg0 );
}
