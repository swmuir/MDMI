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
package org.openhealthtools.mdht.mdmi.model.validate;

public interface IModelValidate<T> {

   /**
    * Validate object.
    *  
    * @param object The object to validate.
    * @param results Object to which warnings and errors can be added. Only warnings and errors which apply to
    *           <code>object</code> should be added.
    */
   public void validate( T object, ModelValidationResults results );
}
