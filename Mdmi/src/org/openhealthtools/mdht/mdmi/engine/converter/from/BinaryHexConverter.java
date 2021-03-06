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

import java.math.*;
import java.nio.*;

public class BinaryHexConverter implements IConvertFromString {
   private IConvertFromString s_convertFrom = new IntegerHexConverter();

   @Override
   public Object convertFromString( String value, String format ) {
      BigInteger intVal = (BigInteger)s_convertFrom.convertFromString(value, format);
      return ByteBuffer.wrap(intVal.toByteArray());
   }
}
