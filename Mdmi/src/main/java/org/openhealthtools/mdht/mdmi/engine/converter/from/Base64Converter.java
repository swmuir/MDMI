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

import org.openhealthtools.mdht.mdmi.util.*;

public class Base64Converter implements IConvertFromString {
   private IConvertFromString m_wrapped;

   public Base64Converter( IConvertFromString convert ) {
      m_wrapped = convert;
   }

   @Override
   public Object convertFromString( String value, String format ) {
      return m_wrapped.convertFromString(StringUtil.decode(value), format);
   }
}
