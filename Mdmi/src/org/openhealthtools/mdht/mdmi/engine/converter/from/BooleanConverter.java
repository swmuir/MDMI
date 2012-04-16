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
package org.openhealthtools.mdht.mdmi.engine.converter.from;

public class BooleanConverter implements IConvertFromString {
   @Override
   public Object convertFromString( String value, String format ) {
      if( "0".equals(value) )
         value = "false";
      else if( "1".equals(value) )
         value = "true";
      return Boolean.valueOf(value);
   }
}
