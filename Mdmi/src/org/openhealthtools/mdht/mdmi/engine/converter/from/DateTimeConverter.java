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

import java.text.*;
import java.util.Date;

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.engine.converter.DateWrapper;
import org.openhealthtools.mdht.mdmi.util.*;

public class DateTimeConverter implements IConvertFromString {

    @Override
    public Object convertFromString(String value, String format) {
        if (format != null && 0 < format.length()) {
            String[] formatStrings = format.split(";");
            for (String formatString : formatStrings) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(formatString);
                    return new DateWrapper(sdf.parse(value), formatString, value);
                } catch (ParseException ignored) {
                } catch (Exception ex) {
                    throw new MdmiException(ex, "DateTimeConverter.convertFromString({0}, {1}) failed.", value, formatString);
                }
            }
        }

        try {
            return new DateWrapper(XmlUtil.parseDateYMDHMSMZ(value), "YMDHMSMZ", value);
        } catch (Exception ignored) {
        }
        try {
            return new DateWrapper(XmlUtil.parseDateYMDHMSZ(value), "YMDHMSZ", value);
        } catch (Exception ignored) {
        }
        try {
            return new DateWrapper(XmlUtil.parseDateYMDZ(value), "YMDZ", value);
        } catch (Exception ignored) {
        }
        try {
            return new DateWrapper(parseDateYMD(value), "YMD", value);
        } catch (Exception ignored) {
        }
        throw new MdmiException("DateTimeConverter.convertFromString({0}, {1}) failed.", value, format);
    }


    private Date parseDateYMD(String value) throws ParseException {
        String formatString = "yyyyMMdd";
        SimpleDateFormat sdf = new SimpleDateFormat(formatString);
        return sdf.parse(value);
    }

}
