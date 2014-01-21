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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.openhealthtools.mdht.mdmi.engine.converter.DateWrapper;

public class ToStringConverter implements IConvertToString {
   @Override
   public String convertToString( Object obj, String format ) {
	   if (format!=null && format.length()>0) {
	    if (obj instanceof Date) return convertStringSplit(obj, format);
	    if (obj instanceof DateWrapper) return convertStringSplit(((DateWrapper)obj).getDate(),format);
	   }
      return obj.toString();
   }

   public String convertStringSplit(Object obj, String format) {
	   String[] formatStrings = format.split(";");
       for (String formatString : formatStrings) {
           String convert = convert(obj, formatString);
           if (convert != null) return convert;
       }
	return null;
   }

   private String convert(Object obj, String format) {
       try {
           SimpleDateFormat sdf = new SimpleDateFormat(format);
           return sdf.format((Date) obj);
       } catch (IllegalArgumentException ignored) {
       }
       return null;
   }
}
