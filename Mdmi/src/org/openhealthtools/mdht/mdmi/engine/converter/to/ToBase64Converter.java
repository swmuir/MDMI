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

import org.openhealthtools.mdht.mdmi.util.*;

public class ToBase64Converter implements IConvertToString {
   private IConvertToString s_wrapped;

   public ToBase64Converter() {
      this(new ToStringConverter());
   }

   public ToBase64Converter( IConvertToString wrapped ) {
      s_wrapped = wrapped;
   }

   @Override
   public String convertToString( Object obj, String format ) {
      return StringUtil.encode(s_wrapped.convertToString(obj, format));
   }
}
