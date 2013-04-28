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

import org.openhealthtools.mdht.mdmi.model.*;

/**
 * Implementations of this interface will allow conversions between values of two different
 * simple enumerated data types.
 */
public interface IEnumerationConverter {
   /**
    * Return true if this instance can convert a source value of the specified type to a 
    * target value of the specified type.
    * 
    * @param source Source value type.
    * @param target Target value type.
    * @return True if this instance can convert the source type to the target type, false otherwise.
    * @throws IllegalArgumentException if any of the two types are null. 
    */
   public boolean canConvert( DTSEnumerated source, DTSEnumerated target );

   /**
    * Return the converted value of the target type obtained from the conversion of the given 
    * source value of the specified source type.
    * 
    * @param source Source value type.
    * @param target Target value type.
    * @param value The value of the source type to convert.
    * @return The converted value of the target type obtained from the conversion of the given 
    * source value of the specified source type.
    */
   public EnumerationLiteral convert( DTSEnumerated source, DTSEnumerated target, EnumerationLiteral value );
} // IEnumerationConverter
