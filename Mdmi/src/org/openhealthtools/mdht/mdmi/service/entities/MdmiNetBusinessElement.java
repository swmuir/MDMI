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

import javax.xml.bind.annotation.*;

/**
 * Data structure used to exchange data between the MDMI service and its clients.
 * Represents a Business Element reference to the data in the dictionary.
 * 
 * @author goancea
 */
@XmlRootElement
public class MdmiNetBusinessElement {
   String name       ;
   String description;
   String uri        ;
   String uniqueId   ;
   String dataType   ;

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

   public String getUri() {
      return uri;
   }

   public void setUri( String uri ) {
      this.uri = uri;
   }

   public String getUniqueId() {
      return uniqueId;
   }

   public void setUniqueId( String uniqueId ) {
      this.uniqueId = uniqueId;
   }

   public String getDataType() {
      return dataType;
   }

   public void setDataType( String dataType ) {
      this.dataType = dataType;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      sb.append("name: '").append(name).append("'");
      sb.append(", description: '").append(description).append("'");
      sb.append(", uri: '").append(uri).append("'");
      sb.append(", uniqueId: '").append(uniqueId).append("'");
      sb.append(", dataType: '").append(dataType).append("'");
      sb.append("}");
      return sb.toString();
   }
} // MdmiBusinessElement
