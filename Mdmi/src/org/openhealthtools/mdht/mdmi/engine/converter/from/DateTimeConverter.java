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
 * Author:
 *     Gabriel Oancea
 *     Jeff Klann
 *
 *******************************************************************************/
package org.openhealthtools.mdht.mdmi.engine.converter.from;

import org.openhealthtools.mdht.mdmi.util.DateUtil;


public class DateTimeConverter implements IConvertFromString {

    @Override
    public Object convertFromString(String value, String format) {

    	String myFormat = "";
        if (format != null && 0 < format.length())
        	myFormat=DateUtil.getLongestWithoutSemiColons(format);
        return DateUtil.parseDateImplicitOptional(myFormat, value);
    }
}