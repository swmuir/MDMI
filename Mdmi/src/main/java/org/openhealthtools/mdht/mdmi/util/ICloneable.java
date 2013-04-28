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
package org.openhealthtools.mdht.mdmi.util;

/**
 * Interface defining a public clone() method. It is implemented by all classes that wish to be cloned. 
 * Note that the semantic is that of a DEEP clone.
 * 
 * @author goancea
 */
public interface ICloneable<T> extends Cloneable {
   /**
    * Clone method, implementations must take care of all aggregated and composite objects.
    */
   public T clone();
} // ICloneable