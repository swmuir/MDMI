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
package org.openhealthtools.mdht.mdmi.engine.converter.to;

import java.math.*;

public class DecimalToBooleanConverter implements IConvertToString {
   @Override
   public String convertToString( Object obj, String format ) {
      if( !(obj instanceof BigDecimal) )
         throw new IllegalArgumentException("Object is not a BigDecimal type.");
      BigDecimal bigDec = (BigDecimal)obj;
      return Boolean.toString(bigDec.compareTo(BigDecimal.ZERO) != 0);
   }
}
