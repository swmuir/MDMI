/*******************************************************************************
* Copyright (c) 2012-2013 Firestar Software, Inc.
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
package org.openhealthtools.mdht.mdmi.service.entities;

/**
 * Enumeration literal for an enumerated data type.
 *  
 * @author goancea
 */
public class MdmiNetEnumLiteral {
   String name       ;
   String description;
   String code       ;
   
   public String getName() {
      return name;
   }

   public void setName( String name ) {
      this.name = name;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription( String description ) {
      this.description = description;
   }

   public String getCode() {
      return code;
   }

   public void setCode( String code ) {
      this.code = code;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      sb.append("name: ");
      if( name == null )
         sb.append("null");
      else
         sb.append("'").append(name).append("'");

      if( description != null )
         sb.append(", description: '").append(description).append("'");

      if( code != null )
         sb.append(", code: '").append(code).append("'");
      sb.append("}");
      return sb.toString();
   }
} // MdmiEnumLiteral
