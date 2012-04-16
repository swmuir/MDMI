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

import java.util.*;

import org.openhealthtools.mdht.mdmi.engine.*;
import org.openhealthtools.mdht.mdmi.model.*;

public class ConvertFromFactory {
   // Map of converters
   private static final HashMap<XDT, HashMap<DTSPrimitive, IConvertFromString>> CONVERT_MAP
         = new HashMap<XDT, HashMap<DTSPrimitive, IConvertFromString>>();

   // Converters- they are state-less, so we only need one of each.
   public static final BooleanConverter            CONV_BOOL       = new BooleanConverter();
   public static final BinaryConverter             CONV_BINARY     = new BinaryConverter();
   public static final BigDecimalConverter         CONV_DEC        = new BigDecimalConverter();
   public static final BigIntegerConverter         CONV_INT        = new BigIntegerConverter();
   public static final StringConverter             CONV_STRING     = new StringConverter();
   public static final DateTimeConverter           CONV_DATETIME   = new DateTimeConverter();
   public static final DateConverter               CONV_DATE       = new DateConverter();
   public static final TimeConverter               CONV_TIME       = new TimeConverter();

   public static final GDayConverter               CONV_DAY        = new GDayConverter();
   public static final GMonthDayConverter          CONV_MONTHDAY   = new GMonthDayConverter();
   public static final GMonthConverter             CONV_MONTH      = new GMonthConverter();
   public static final GYearMonthConverter         CONV_YEARMONTH  = new GYearMonthConverter();
   public static final GYearConverter              CONV_YEAR       = new GYearConverter();

   public static final DecimalHexConverter         CONV_DECHEX     = new DecimalHexConverter();
   public static final IntegerHexConverter         CONV_INTHEX     = new IntegerHexConverter();
   public static final DateHexMillisConverter      CONV_DATEHEX    = new DateHexMillisConverter();
   public static final BinaryHexConverter          CONV_BINHEX     = new BinaryHexConverter();
   public static final Base64ByteConverter         CONV_BASE64     = new Base64ByteConverter();

   public static final ArrayConverter              CONV_STRINGARR  = new ArrayConverter(CONV_STRING);

   private static final IntegerBooleanConverter    CONV_INTBOOL    = new IntegerBooleanConverter(CONV_BOOL);
   private static final IntegerHexBooleanConverter CONV_INTHEXBOOL = new IntegerHexBooleanConverter();
   private static final DecimalBooleanConverter    CONV_DOUBLEBOOL = new DecimalBooleanConverter(CONV_BOOL);

   public static final ConvertFromFactory          INSTANCE        = new ConvertFromFactory();

   public IConvertFromString getConverter( XDT fromType, DTSPrimitive toType ) {
      HashMap<DTSPrimitive, IConvertFromString> fromMap = CONVERT_MAP.get(fromType);
      return fromMap == null ? null : fromMap.get(toType);
   }

   private ConvertFromFactory() {
      /*********** anyURI *****************/
      HashMap<DTSPrimitive, IConvertFromString> local = createNewMap(XDT.ANYURI);
      local.put(DTSPrimitive.STRING, CONV_STRING);

      /*********** base64Binary *****************/
      local = new HashMap<DTSPrimitive, IConvertFromString>();
      CONVERT_MAP.put(XDT.BASE64BINARY, local);
      local.put(DTSPrimitive.INTEGER, new Base64Converter(CONV_INT));
      local.put(DTSPrimitive.DECIMAL, new Base64Converter(CONV_DEC));
      local.put(DTSPrimitive.STRING, new Base64Converter(CONV_STRING));
      local.put(DTSPrimitive.BOOLEAN, new Base64Converter(CONV_BOOL));
      local.put(DTSPrimitive.DATETIME, new Base64Converter(CONV_DATETIME));
      local.put(DTSPrimitive.BINARY, CONV_BASE64);

      /*********** boolean *****************/
      local = createNewMap(XDT.BOOLEAN);
      local.put(DTSPrimitive.BOOLEAN, CONV_BOOL);
      local.put(DTSPrimitive.INTEGER, new BooleanNumberConverter(CONV_INT));
      local.put(DTSPrimitive.DECIMAL, new BooleanNumberConverter(CONV_DEC));
      local.put(DTSPrimitive.STRING, CONV_STRING);

      /*********** byte *****************/
      local = createNewMap(XDT.BYTE);
      local.put(DTSPrimitive.BOOLEAN, CONV_INTBOOL);
      local.put(DTSPrimitive.INTEGER, CONV_INT);
      local.put(DTSPrimitive.DECIMAL, CONV_DEC);
      local.put(DTSPrimitive.STRING, CONV_STRING);

      /*********** date *****************/
      local = createStringConversions(XDT.DATE);
      local.put(DTSPrimitive.DATETIME, CONV_DATE);

      /*********** datetime *****************/
      local = createStringConversions(XDT.DATETIME);
      local.put(DTSPrimitive.DATETIME, CONV_DATETIME);

      /*********** decimal *****************/
      createDecimalConversions(XDT.DECIMAL);

      /*********** double *****************/
      createDecimalConversions(XDT.DOUBLE);

      /*********** entities *****************/
      local = createNewMap(XDT.ENTITIES);
      local.put(DTSPrimitive.STRING, CONV_STRINGARR);

      /*********** entity *****************/
      createStringConversions(XDT.ENTITY);

      /*********** float *****************/
      createDecimalConversions(XDT.FLOAT);

      /*********** gday *****************/
      local = createStringConversions(XDT.GDAY);
      local.put(DTSPrimitive.DATETIME, CONV_DAY);

      /*********** gmonth *****************/
      local = createStringConversions(XDT.GMONTH);
      local.put(DTSPrimitive.DATETIME, CONV_MONTH);

      /*********** gmonthday *****************/
      local = createStringConversions(XDT.GMONTHDAY);
      local.put(DTSPrimitive.DATETIME, CONV_MONTHDAY);

      /*********** gyear *****************/
      local = createStringConversions(XDT.GYEAR);
      local.put(DTSPrimitive.DATETIME, CONV_YEAR);

      /*********** gyearmonth *****************/
      local = createStringConversions(XDT.GYEARMONTH);
      local.put(DTSPrimitive.DATETIME, CONV_YEARMONTH);

      /*********** hexbinary *****************/
      local = new HashMap<DTSPrimitive, IConvertFromString>();
      CONVERT_MAP.put(XDT.HEXBINARY, local);
      local.put(DTSPrimitive.INTEGER, CONV_INTHEX);
      local.put(DTSPrimitive.DECIMAL, CONV_DECHEX);
      local.put(DTSPrimitive.STRING, CONV_STRING);
      local.put(DTSPrimitive.BOOLEAN, CONV_INTHEXBOOL);
      local.put(DTSPrimitive.DATETIME, CONV_DATEHEX);
      local.put(DTSPrimitive.BINARY, CONV_BINHEX);

      /*********** id *****************/
      createIntegerConversions(XDT.ID);

      /*********** idref *****************/
      createIntegerConversions(XDT.IDREF);

      /*********** idrefs *****************/
      local = createNewMap(XDT.IDREFS);
      local.put(DTSPrimitive.INTEGER, new ArrayConverter(CONV_INT));
      local.put(DTSPrimitive.DECIMAL, new ArrayConverter(CONV_DEC));
      local.put(DTSPrimitive.BOOLEAN, new ArrayConverter(CONV_INTBOOL));
      local.put(DTSPrimitive.STRING, CONV_STRINGARR);

      /*********** int *****************/
      createIntegerConversions(XDT.INT);

      /*********** integer *****************/
      createIntegerConversions(XDT.INTEGER);

      /*********** language *****************/
      createStringConversions(XDT.LANGUAGE);

      /*********** long *****************/
      createIntegerConversions(XDT.LONG);

      /*********** name *****************/
      createStringConversions(XDT.NAME);

      /*********** ncname *****************/
      createStringConversions(XDT.NCNAME);

      /*********** negative integer *****************/
      createIntegerConversions(XDT.NEGATIVEINTEGER);

      /*********** nmtoken *****************/
      createStringConversions(XDT.NMTOKEN);

      /*********** nmtokens *****************/
      local = createNewMap(XDT.NMTOKENS);
      local.put(DTSPrimitive.STRING, CONV_STRINGARR);

      /*********** nonnegative integer *****************/
      createIntegerConversions(XDT.NONNEGATIVEINTEGER);

      /*********** nonpositive integer *****************/
      createIntegerConversions(XDT.NONPOSITIVEINTEGER);

      /*********** normalized string *****************/
      createStringConversions(XDT.NORMALIZEDSTRING);

      /*********** positive integer *****************/
      createIntegerConversions(XDT.POSITIVEINTEGER);

      /*********** qname *****************/
      createStringConversions(XDT.QNAME);

      /*********** short *****************/
      createIntegerConversions(XDT.SHORT);

      /*********** string *****************/
      local = createStringConversions(XDT.STRING);
      local.put(DTSPrimitive.DATETIME, CONV_DATETIME);

      /*********** time *****************/
      local = createStringConversions(XDT.TIME);
      local.put(DTSPrimitive.DATETIME, CONV_TIME);

      /*********** token *****************/
      createStringConversions(XDT.TOKEN);

      /*********** unsigned byte *****************/
      createIntegerConversions(XDT.UNSIGNEDBYTE);

      /*********** unsigned int *****************/
      createIntegerConversions(XDT.UNSIGNEDINT);

      /*********** unsigned long *****************/
      createIntegerConversions(XDT.UNSIGNEDLONG);

      /*********** unsigned short *****************/
      createIntegerConversions(XDT.UNSIGNEDSHORT);
   }

   private HashMap<DTSPrimitive, IConvertFromString> createNewMap( XDT xmlType ) {
      HashMap<DTSPrimitive, IConvertFromString> local = new HashMap<DTSPrimitive, IConvertFromString>();
      CONVERT_MAP.put(xmlType, local);
      local.put(DTSPrimitive.BINARY, CONV_BINARY);
      return local;
   }

   private HashMap<DTSPrimitive, IConvertFromString> createNumericConversions( XDT xdt ) {
      HashMap<DTSPrimitive, IConvertFromString> local = createNewMap(xdt);
      local.put(DTSPrimitive.INTEGER, CONV_INT);
      local.put(DTSPrimitive.DECIMAL, CONV_DEC);
      local.put(DTSPrimitive.STRING, CONV_STRING);
      return local;
   }

   private HashMap<DTSPrimitive, IConvertFromString> createIntegerConversions( XDT xdt ) {
      HashMap<DTSPrimitive, IConvertFromString> local = createNumericConversions(xdt);
      local.put(DTSPrimitive.BOOLEAN, CONV_INTBOOL);
      return local;
   }

   private HashMap<DTSPrimitive, IConvertFromString> createDecimalConversions( XDT xdt ) {
      HashMap<DTSPrimitive, IConvertFromString> local = createNumericConversions(xdt);
      local.put(DTSPrimitive.BOOLEAN, CONV_DOUBLEBOOL);
      return local;
   }

   private HashMap<DTSPrimitive, IConvertFromString> createStringConversions( XDT xdt ) {
      HashMap<DTSPrimitive, IConvertFromString> local = createNewMap(xdt);
      local.put(DTSPrimitive.STRING, CONV_STRING);
      return local;
   }
}
