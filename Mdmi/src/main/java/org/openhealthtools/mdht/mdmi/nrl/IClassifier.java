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

public interface IClassifier extends IModelElement {
   @SuppressWarnings( "rawtypes" )
   public abstract java.util.List getAttributes( boolean arg0 );

   public abstract IAttribute getAttributeByName( String arg0, boolean arg1 );

   public abstract boolean hasAttribute( String arg0 );

   public abstract boolean hasStaticAttributes();

   public abstract boolean isEnumeration();
}
