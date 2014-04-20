/*******************************************************************************
* Copyright (c) 2012 Firestar Software, Inc.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Firestar Software, Inc. - initial API and implementation
*     Jeff Klann, PhD - revision for variable precision
*
* Authors:
*     Gabriel Oancea
*     Jeff Klann
*
*******************************************************************************/
package org.openhealthtools.mdht.mdmi.engine.converter.to;

import java.util.Date;

import org.openhealthtools.mdht.mdmi.engine.converter.DateWrapper;
import org.openhealthtools.mdht.mdmi.util.DateUtil;

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
	   String bestFormat = DateUtil.getLongestWithoutSemiColons(format);
	   String convert = convert(obj,bestFormat);
       if (convert != null) return convert;
	return null;
   }

   private String convert(Object obj, String format) {
	   if (obj==null) return "ERROR";
       try {
    	   return DateUtil.formatDate(format, (Date)obj);
       } catch (IllegalArgumentException ignored) {
       }
       return null;
   }
}
