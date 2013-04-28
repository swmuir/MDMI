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

import org.openhealthtools.mdht.mdmi.util.*;

public class ToHexStringConverter implements IConvertToString {
   private IConvertToString m_wrapped;

   public ToHexStringConverter() {
      m_wrapped = new ToStringConverter();
   }

   public ToHexStringConverter( IConvertToString toString ) {
      m_wrapped = toString;
   }

   @Override
   public String convertToString( Object obj, String format ) {
      String toConvert = m_wrapped.convertToString(obj, format);
      return StringUtil.encodeBytesHex(new BigInteger(toConvert).toByteArray());
   }
}
