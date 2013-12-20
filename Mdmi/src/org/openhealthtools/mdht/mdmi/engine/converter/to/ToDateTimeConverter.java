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

import java.text.*;
import java.util.*;

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.engine.converter.DateWrapper;
import org.openhealthtools.mdht.mdmi.util.*;

public class ToDateTimeConverter implements IConvertToString {

    @Override
    public String convertToString(Object obj, String format) {
        if (!((obj instanceof Date) || obj instanceof DateWrapper))
            throw new IllegalArgumentException("Object is not a java.util.Date type.");
        try {
            if (format != null && 0 < format.length() && !format.equals("DATETIME")) {
                String[] formatStrings = format.split(";");
                if (obj instanceof DateWrapper) {
                    DateWrapper dateWrapper = (DateWrapper) obj;
                    obj = dateWrapper.getDate();
                    String orgFormat = dateWrapper.getOriginalFormat();
                    if (orgFormat != null && Arrays.asList(formatStrings).contains(orgFormat))
                        return convert(obj, orgFormat);
                }

                for (String formatString : formatStrings) {
                    String convert = convert(obj, formatString);
                    if (convert != null) return convert;
                }
            }
        } catch (Exception ex) {
            throw new MdmiException(ex, "ToDateTimeConverter.convertToString({0}, {1}) failed.", obj, format);
        }
        return XmlUtil.formatDateYMDHMSMZ((Date) obj);
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
