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
package org.openhealthtools.mdht.mdmi.engine;

import org.openhealthtools.mdht.mdmi.MdmiException;
import org.openhealthtools.mdht.mdmi.engine.converter.from.ConvertFromFactory;
import org.openhealthtools.mdht.mdmi.engine.converter.from.IConvertFromString;
import org.openhealthtools.mdht.mdmi.engine.converter.to.ConvertToFactory;
import org.openhealthtools.mdht.mdmi.engine.converter.to.IConvertToString;
import org.openhealthtools.mdht.mdmi.model.DTSPrimitive;

/**
 * Xml Data Types enumeration.
 * See http://www.w3.org/TR/xmlschema-2/#built-in-datatypes
 * 
 * @author goancea
 */
public enum XDT {
   NONE                ,
   ANYTYPE             ,
   ANYSIMPLETYPE       ,
   DURATION            ,
   DATETIME            ,
   TIME                ,
   DATE                ,
   GYEARMONTH          ,
   GYEAR               ,
   GMONTHDAY           ,
   GDAY                ,
   GMONTH              ,
   BOOLEAN             ,
   BASE64BINARY        ,
   HEXBINARY           ,
   FLOAT               ,
   DOUBLE              ,
   ANYURI              ,
   QNAME               ,
   NOTATION            ,
   STRING              ,
   NORMALIZEDSTRING    ,
   TOKEN               ,
   LANGUAGE            ,
   NAME                ,
   NCNAME              ,
   ID                  ,
   IDREF               ,
   IDREFS              ,
   ENTITY              ,
   ENTITIES            ,
   NMTOKEN             ,
   NMTOKENS            ,
   DECIMAL             ,
   INTEGER             ,
   NONPOSITIVEINTEGER  ,
   NEGATIVEINTEGER     ,
   LONG                ,
   INT                 ,
   SHORT               ,
   BYTE                ,
   NONNEGATIVEINTEGER  ,
   POSITIVEINTEGER     ,
   UNSIGNEDLONG        ,
   UNSIGNEDINT         ,
   UNSIGNEDSHORT       ,
   UNSIGNEDBYTE        ;

   /**
    * Construct one from a string with the same name as the enum literal.
    * 
    * @param v The string value, case insensitive.
    * @return The enum literal corresponding to it, null if not found.
    */
   public static XDT fromString( String v ) {
      if( v == null ) return null;
      v = v.toUpperCase();
      if( v.equals("NONE"               ) ) return NONE               ;
      if( v.equals("ANYTYPE"            ) ) return ANYTYPE            ;
      if( v.equals("ANYSIMPLETYPE"      ) ) return ANYSIMPLETYPE      ;
      if( v.equals("DURATION"           ) ) return DURATION           ;
      if( v.equals("DATETIME"           ) ) return DATETIME           ;
      if( v.equals("TIME"               ) ) return TIME               ;
      if( v.equals("DATE"               ) ) return DATE               ;
      if( v.equals("GYEARMONTH"         ) ) return GYEARMONTH         ;
      if( v.equals("GYEAR"              ) ) return GYEAR              ;
      if( v.equals("GMONTHDAY"          ) ) return GMONTHDAY          ;
      if( v.equals("GDAY"               ) ) return GDAY               ;
      if( v.equals("GMONTH"             ) ) return GMONTH             ;
      if( v.equals("BOOLEAN"            ) ) return BOOLEAN            ;
      if( v.equals("BASE64BINARY"       ) ) return BASE64BINARY       ;
      if( v.equals("HEXBINARY"          ) ) return HEXBINARY          ;
      if( v.equals("FLOAT"              ) ) return FLOAT              ;
      if( v.equals("DOUBLE"             ) ) return DOUBLE             ;
      if( v.equals("ANYURI"             ) ) return ANYURI             ;
      if( v.equals("QNAME"              ) ) return QNAME              ;
      if( v.equals("NOTATION"           ) ) return NOTATION           ;
      if( v.equals("STRING"             ) ) return STRING             ;
      if( v.equals("NORMALIZEDSTRING"   ) ) return NORMALIZEDSTRING   ;
      if( v.equals("TOKEN"              ) ) return TOKEN              ;
      if( v.equals("LANGUAGE"           ) ) return LANGUAGE           ;
      if( v.equals("NAME"               ) ) return NAME               ;
      if( v.equals("NCNAME"             ) ) return NCNAME             ;
      if( v.equals("ID"                 ) ) return ID                 ;
      if( v.equals("IDREF"              ) ) return IDREF              ;
      if( v.equals("IDREFS"             ) ) return IDREFS             ;
      if( v.equals("ENTITY"             ) ) return ENTITY             ;
      if( v.equals("ENTITIES"           ) ) return ENTITIES           ;
      if( v.equals("NMTOKEN"            ) ) return NMTOKEN            ;
      if( v.equals("NMTOKENS"           ) ) return NMTOKENS           ;
      if( v.equals("DECIMAL"            ) ) return DECIMAL            ;
      if( v.equals("INTEGER"            ) ) return INTEGER            ;
      if( v.equals("NONPOSITIVEINTEGER" ) ) return NONPOSITIVEINTEGER ;
      if( v.equals("NEGATIVEINTEGER"    ) ) return NEGATIVEINTEGER    ;
      if( v.equals("LONG"               ) ) return LONG               ;
      if( v.equals("INT"                ) ) return INT                ;
      if( v.equals("SHORT"              ) ) return SHORT              ;
      if( v.equals("BYTE"               ) ) return BYTE               ;
      if( v.equals("NONNEGATIVEINTEGER" ) ) return NONNEGATIVEINTEGER ;
      if( v.equals("POSITIVEINTEGER"    ) ) return POSITIVEINTEGER    ;
      if( v.equals("UNSIGNEDLONG"       ) ) return UNSIGNEDLONG       ;
      if( v.equals("UNSIGNEDINT"        ) ) return UNSIGNEDINT        ;
      if( v.equals("UNSIGNEDSHORT"      ) ) return UNSIGNEDSHORT      ;
      if( v.equals("UNSIGNEDBYTE"       ) ) return UNSIGNEDBYTE       ;
      return null;
   }

   /**
    * Get a XDT enum corresponding to a primitive data type.
    * 
    * @param pdt The primitive data type.
    * @return The XDT corresponding to it.
    */
   public static XDT fromPDT( DTSPrimitive pdt ) {
      if( pdt == null ) return null;
      else if( pdt == DTSPrimitive.BINARY   ) return HEXBINARY;
      else if( pdt == DTSPrimitive.BOOLEAN  ) return BOOLEAN  ;
      else if( pdt == DTSPrimitive.DATETIME ) return DATETIME ;
      else if( pdt == DTSPrimitive.DECIMAL  ) return DECIMAL  ;
      else if( pdt == DTSPrimitive.INTEGER  ) return INTEGER  ;
      else if( pdt == DTSPrimitive.STRING   ) return STRING   ;
      return null;
   }
   
   /**
    * Convert a string (with an optional format) of the given xdt type, into a primitive value of the pdt type.
    * An exception will be thrown if the value does not match the XDT, or if it is incompatible to the PDT given.
    *  
    * @param xdt The type of the value represented by the string.
    * @param value The actual string representation of the value.
    * @param format The format (one of the XDT literals or a custom format, in some cases).
    * @param pdt The target primitive data type to convert to.
    * @return The converted value.
    */
   public static Object convertFromString( XDT xdt, String value, String format, DTSPrimitive pdt ) {
      if( pdt == null || xdt == null || xdt.equals(NONE) || xdt.equals(NOTATION) )
         throw new IllegalArgumentException("Null or invalid data type");
      if( value == null || value.length() <= 0 )
         return null;

      IConvertFromString converter = ConvertFromFactory.INSTANCE.getConverter(xdt, pdt);
      if( converter == null ) {
         throw new MdmiException("Conversion from XML data type {0} to primitive data type {1} is not supported.",
               xdt.toString(), pdt.getTypeName());
      }
      return converter.convertFromString(value, format);
   }

   /**
    * Convert a value of the specified primitive data type (pdt) into a string that matches the given XDT
    * and format (if necessary).
    *  
    * @param pdt The value primitive data type.
    * @param value The actual value object.
    * @param format The format requested (one of the XDT literals or a custom format, in some cases).
    * @param xdt The target datatype of the string representation returned.
    * @return A string representation of the primitive data type supplied.
    */
   public static String convertToString( DTSPrimitive pdt, Object value, String format, XDT xdt ) {
      if( pdt == null || xdt == null || xdt.equals(NONE) || xdt.equals(NOTATION) )
         throw new IllegalArgumentException("Null or invalid data type");
      if( value == null )
         return null;

      IConvertToString converter = ConvertToFactory.INSTANCE.getConverter(xdt, pdt);
      if( converter == null ) {
         throw new MdmiException("Conversion from primitive data type {0} to XML data type {1} is not supported.",
               pdt.getTypeName(), xdt.toString());
      }
      return converter.convertToString(value, format);
   }
} // XDT
