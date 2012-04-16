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
package org.openhealthtools.mdht.mdmi.model.builder;

import org.openhealthtools.mdht.mdmi.model.raw.*;

public interface IModelInitializer<T> {

   /**
    * Initializes the specified model object rather than creating one.
    * 
    * @param modelObject The model object to initialize.
    * @param classDef The class definition from which to build the object.
    * @param root An object graph representation of an intermediate document (e.g. an XMI doc). This can be used to
    *           dereference items defined in <code>classDef</code>.
    */
   public void initMessageModelObject( T modelObject, ClassDef classDef, RawRoot root );
}
