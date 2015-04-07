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
 * A field of structures or choice data type. Must have a data type, either simple or complex.
 * Default occurrences are 1..1.
 * 
 * @author goancea
 */
public class MdmiNetField {
   String name       ;
   String description;
   String dataType   ;
   int    minOccurs  = 1;
   int    maxOccurs  = 1;
   
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

   public String getDataType() {
      return dataType;
   }

   public void setDataType( String dataType ) {
      this.dataType = dataType;
   }

   public int getMinOccurs() {
      return minOccurs;
   }

   public void setMinOccurs( int minOccurs ) {
      this.minOccurs = minOccurs;
   }

   public int getMaxOccurs() {
      return maxOccurs;
   }

   public void setMaxOccurs( int maxOccurs ) {
      this.maxOccurs = maxOccurs;
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

      if( dataType != null )
         sb.append(", dataType: '").append(dataType).append("'");

      sb.append(", minOccurs: ").append(minOccurs);
      sb.append(", maxOccurs: ").append(maxOccurs);
      sb.append("}");
      return sb.toString();
   }
} // MdmiField
