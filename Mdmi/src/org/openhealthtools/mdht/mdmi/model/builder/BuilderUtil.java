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
package org.openhealthtools.mdht.mdmi.model.builder;

import java.net.*;

import org.openhealthtools.mdht.mdmi.model.raw.*;

public class BuilderUtil {
   public static final String  s_aggregationNone = "none";
   private static final String s_nameAttrib      = "name";
   private static final String s_descAttrib      = "description";
   private static final String s_ruleAttrib      = "rule";
   private static final String s_ruleExprAttrib  = "ruleExpressionLanguage";

   /**
    * Convenience method for those (many) classes which have a 'rule' attribute.
    * 
    * @param classDef Class definition from which to retrieve the attribute value.
    * @return The value of the 'name' attribute.
    */
   public static String getRuleAttribVal( ClassDef classDef ) {
      return getAttribVal(classDef, s_ruleAttrib);
   }

   /**
    * Convenience method for those (many) classes which have a 'ruleExpressionLanguage' attribute.
    * 
    * @param classDef Class definition from which to retrieve the attribute value.
    * @return The value of the 'name' attribute.
    */
   public static String getRuleExprAttribVal( ClassDef classDef ) {
      return getAttribVal(classDef, s_ruleExprAttrib);
   }

   /**
    * Convenience method for those (many) classes which have a 'name' attribute.
    * 
    * @param classDef Class definition from which to retrieve the attribute value.
    * @return The value of the 'name' attribute.
    */
   public static String getNameAttribVal( ClassDef classDef ) {
      return getAttribVal(classDef, s_nameAttrib);
   }

   /**
    * Convenience method for those (many) classes which have a 'description' attribute.
    * 
    * @param classDef Class definition from which to retrieve the attribute value.
    * @return The value of the 'description' attribute.
    */
   public static String getDescriptionAttribVal( ClassDef classDef ) {
      return getAttribVal(classDef, s_descAttrib);
   }

   /**
    * @param classDef Class definition from which to retrieve the attribute value.
    * @param attribName The name of the attribute whose value is to be retrieved.
    * @return The value of the attribute, null if the attribute does not exist.
    */
   public static String getAttribVal( ClassDef classDef, String attribName ) {
      Attribute attrib = classDef.getAttribute(attribName);
      return attrib == null ? null : attrib.getDefaultValue();
   }

   /**
    * @param classDef Class definition from which to retrieve the URI attribute value.
    * @param attribName The name of the attribute whose value is to be retrieved.
    * @return URI object for the attribute's value, null if the attribute does not exist OR if the value is not a valid
    *         URI.
    */
   public static URI getURIAttribVal( ClassDef classDef, String attribName ) {
      String uriString = getAttribVal(classDef, attribName);
      try {
         return uriString == null ? null : new URI(uriString);
      }
      catch( URISyntaxException exc ) {
         return null;
      }
   }

   /**
    * @param classDef Class definition from which to retrieve the boolean attribute value.
    * @param attribName The name of the attribute whose value is to be retrieved.
    * @return boolean value parsed from attribute value.
    */
   public static boolean getBooleanAttribVal( ClassDef classDef, String attribName ) {
      try {
         return Boolean.parseBoolean(getAttribVal(classDef, attribName));
      }
      catch( Exception ex ) {
         return false;
      }
   }

   public static int getIntegerAttribVal( ClassDef classDef, String attribName ) {
      try {
         return Integer.parseInt(getAttribVal(classDef, attribName));
      }
      catch( Exception ex ) {
         return 0;
      }
   }
}
