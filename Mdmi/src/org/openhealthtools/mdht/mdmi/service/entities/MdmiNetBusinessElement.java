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
 * Represents a Business Element reference to the data in the dictionary.
 * 
 * @author goancea
 */
@XmlRootElement
public class MdmiNetBusinessElement {
   ArrayList<MdmiNetBerName> names = new ArrayList<MdmiNetBerName>();
   String                    uri                ;
   String                    uniqueId           ;
   String                    dataType           ;
   String                    enumValueSetField  ;
   String                    enumValueField     ;
   String                    enumValueDescrField;
	String                    enumValueSet       ;

   public ArrayList<MdmiNetBerName> getNames() {
      return names;
   }

   public void setNames( ArrayList<MdmiNetBerName> names ) {
      this.names = names;
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
   
   public String getEnumValueSetField() {
		return enumValueSetField;
	}

	public void setEnumValueSetField( String enumValueSetField ) {
		this.enumValueSetField = enumValueSetField;
	}

	public String getEnumValueField() {
		return enumValueField;
	}

	public void setEnumValueField( String enumValueField ) {
		this.enumValueField = enumValueField;
	}

	public String getEnumValueDescrField() {
		return enumValueDescrField;
	}

	public void setEnumValueDescrField( String enumValueDescrField ) {
		this.enumValueDescrField = enumValueDescrField;
	}

	public String getEnumValueSet() {
		return enumValueSet;
	}

	public void setEnumValueSet( String enumValueSet ) {
		this.enumValueSet = enumValueSet;
	}

	@Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      sb.append("names: [");
      for( int i = 0; i < names.size(); i++ ) {
	      MdmiNetBerName mnbn = names.get(i);
	      if( 0 < i )
	      	sb.append(", ");
	      sb.append("{name: '").append(mnbn.name).append("', description: '").append(mnbn.description).append("'}");
      }
      sb.append("]");
      if( null != uri && 0 < uri.length() )
      	sb.append(", uri: '").append(uri).append("'");
      sb.append(", uniqueId: '").append(uniqueId).append("'");
      sb.append(", dataType: '").append(dataType).append("'");
      sb.append("}");
      return sb.toString();
   }
} // MdmiBusinessElement
