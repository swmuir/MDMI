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

import java.util.*;
import javax.xml.bind.annotation.*;

/**
 * Data structure used to exchange data between the MDMI service and its clients.
 * Represents a Data Type reference to the data types in the dictionary.
 * 
 * @author goancea
 */
@XmlRootElement
public class MdmiNetDatatype {
   MdmiNetDatatypeCategory       type         ;
   String                     name         ;
   String                     description  ;
   String                     referenceUri ;
   String                     baseType     ;
   String                     restriction  ;
   ArrayList<MdmiNetEnumLiteral> enumLiterals = new ArrayList<MdmiNetEnumLiteral>();
   ArrayList<MdmiNetField>       fields       = new ArrayList<MdmiNetField>();

   public MdmiNetDatatypeCategory getType() {
      return type;
   }

   public void setType( MdmiNetDatatypeCategory type ) {
      this.type = type;
   }

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

   public String getReferenceUri() {
      return referenceUri;
   }

   public void setReferenceUri( String referenceUri ) {
      this.referenceUri = referenceUri;
   }

   public String getBaseType() {
      return baseType;
   }

   public void setBaseType( String baseType ) {
      this.baseType = baseType;
   }

   public String getRestriction() {
      return restriction;
   }

   public void setRestriction( String restriction ) {
      this.restriction = restriction;
   }

   public ArrayList<MdmiNetEnumLiteral> getEnumLiterals() {
      return enumLiterals;
   }

   public ArrayList<MdmiNetField> getFields() {
      return fields;
   }

   public static boolean isPrimitiveDatatype( String name ) {
      if( null == name || name.length() <= 0 )
         return false;
      if( name.equals("Binary"  ) ) return true;
      if( name.equals("Boolean" ) ) return true;
      if( name.equals("DateTime") ) return true;
      if( name.equals("Decimal" ) ) return true;
      if( name.equals("Integer" ) ) return true;
      if( name.equals("String"  ) ) return true;
      return false;
   }
    
   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("{name: '").append(name).append("'");

      if( description != null )
         sb.append(", description: '").append(description).append("'");

      if( referenceUri != null )
         sb.append(", referenceUri: '").append(referenceUri).append("'");

      if( baseType != null )
         sb.append(", baseType: '").append(baseType).append("'");

      if( restriction != null )
         sb.append(", restriction: '").append(restriction).append("'");
      
      if( 0 < enumLiterals.size() ) {
         sb.append(", enumLiterals: [");
         for( int i = 0; i < enumLiterals.size(); i++ ) {
            if( 0 < i )
               sb.append(", ");
            sb.append(enumLiterals.toString());
         }
         sb.append("]");
      }
      
      if( 0 < fields.size() ) {
         sb.append(", fields: [");
         for( int i = 0; i < fields.size(); i++ ) {
            if( 0 < i )
               sb.append(", ");
            sb.append(fields.toString());
         }
         sb.append("]");
      }
      sb.append("}");
      return sb.toString();
   }
} // MdmiDataType
