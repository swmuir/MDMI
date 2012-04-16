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

public class ValidateHelper {
   public static boolean isEmptyField( String value ) {
      return value == null || value.length() == 0;
   }

   public static String getResourceKey( Object obj, String fieldName ) {
      return getResourceKey(obj.getClass().getSimpleName(), fieldName);
   }

   public static String getResourceKey( String className, String fieldName ) {
      StringBuilder builder = new StringBuilder(className);
      builder.append(".").append(fieldName);

      return builder.toString();
   }
}
