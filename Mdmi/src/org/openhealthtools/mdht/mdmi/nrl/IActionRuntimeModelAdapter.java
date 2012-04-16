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

public interface IActionRuntimeModelAdapter {
   public abstract void addChild( Object arg0, Object arg1 );

   public abstract void addChild( Object arg0, Object arg1, Object arg2 ) throws ExecutionException;

   public abstract Object cloneModelObject( Object arg0, boolean arg1 ) throws ExecutionException;

   public abstract Object createModelObject( IModelElement arg0 ) throws ExecutionException;

   public abstract void removeChild( Object arg0, Object arg1 ) throws ExecutionException;

   public abstract void replaceChild( Object arg0, Object arg1, Object arg2 ) throws ExecutionException;

   public abstract void setName( Object arg0, String arg1 );

   public abstract void setParent( Object arg0, Object arg1 ) throws ExecutionException;

   public abstract void setValue( Object arg0, Object arg1 ) throws ExecutionException;
}
