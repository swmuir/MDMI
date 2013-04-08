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
package org.openhealthtools.mdht.mdmi.service;

import java.net.*;
import java.util.*;
import javax.ws.rs.core.*;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.config.*;
import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.service.entities.*;

public class MdmiDatatypeProxy {
   private static final String RPATH = "datatypes";
   
   private String token;
   private WebResource service;
   private MessageGroup messageGroup;
   
   public MdmiDatatypeProxy( URI uri, String token ) {
      this(uri, token, null);
   }
   
   public MdmiDatatypeProxy( URI uri, String token, MessageGroup messageGroup ) {
      ClientConfig config = new DefaultClientConfig();
      Client client = Client.create(config);
      service = client.resource(uri);
      this.token = token;
      this.messageGroup = messageGroup;
   }
   
   public MdmiDatatype[] getAll() {
      try {
         MdmiNetDatatype[] lst = new MdmiNetDatatype[0];
         lst = service.path(RPATH).accept(MediaType.APPLICATION_XML).get(lst.getClass());
         HashMap<String, MdmiDatatype> dataTypes = new HashMap<String, MdmiDatatype>(); 
         for( int i = 0; i < lst.length; i++ ) {
            MdmiDatatype dt = toModel(lst[i]);
            dataTypes.put(dt.getName(), dt);
         }
         for( int i = 0; i < lst.length; i++ ) {
            resolveToModel(lst[i], dataTypes);
         }
         return dataTypes.values().toArray(new MdmiDatatype[] {});
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Proxy getAll() failed!");
      }
   }
   
   public MdmiDatatype get( String value ) {
      try {
         MdmiNetDatatype ndt = service.path(RPATH + "/" + value).accept(MediaType.APPLICATION_XML)
               .get(MdmiNetDatatype.class);
         HashMap<String, MdmiDatatype> dataTypes = new HashMap<String, MdmiDatatype>(); 
         MdmiDatatype dt = toModel(ndt);
         dataTypes.put(dt.getName(), dt);
         resolveToModel(ndt, dataTypes);
         return dt;
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Proxy get() failed!");
      }
   }
   
   public MdmiDatatype add( MdmiDatatype dt ) {
      try {
         MdmiNetDatatype o = fromModel(dt);
         MdmiNetDatatype ndt = service.path(RPATH).queryParam("token", token).accept(MediaType.APPLICATION_XML)
               .post(MdmiNetDatatype.class, o);
         HashMap<String, MdmiDatatype> dataTypes = new HashMap<String, MdmiDatatype>(); 
         dt = toModel(ndt);
         dataTypes.put(dt.getName(), dt);
         resolveToModel(ndt, dataTypes);
         return dt;
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Proxy add() failed!");
      }
   }
   
   public MdmiDatatype update( MdmiDatatype dt ) {
      try {
         MdmiNetDatatype o = fromModel(dt);
         MdmiNetDatatype ndt = service.path(RPATH + "/" + o.getName()).queryParam("token", token).accept(MediaType.APPLICATION_XML)
               .put(MdmiNetDatatype.class, o);
         HashMap<String, MdmiDatatype> dataTypes = new HashMap<String, MdmiDatatype>(); 
         dt = toModel(ndt);
         dataTypes.put(dt.getName(), dt);
         resolveToModel(ndt, dataTypes);
         return dt;
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Proxy update() failed!");
      }
   }
   
   public void delete( String value ) {
      try {
         service.path(RPATH + "/" + value).queryParam("token", token).delete(MdmiNetDatatype.class);
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Proxy delete() failed!");
      }
   }
   
   private MdmiDatatype toModel( MdmiNetDatatype ndt ) {
      switch( ndt.getType() ) {
         case EXTERNAL: {
            DTExternal dt = new DTExternal();
            dt.setTypeName(ndt.getName());
            dt.setDescription(ndt.getDescription());
            String s = ndt.getReferenceUri();
            try {
               dt.setTypeSpec(new URI(s));
            }
            catch( Exception ex ) {
               throw new MdmiException(ex, "Invalid reference URI for external data type: " + s);
            }
            dt.setReadonly(true);
            return dt;
         }
         case DERIVED: {
            DTSDerived dt = new DTSDerived();
            dt.setTypeName(ndt.getName());
            dt.setDescription(ndt.getDescription());
            dt.setRestriction(ndt.getRestriction());
            dt.setReadonly(true);
         }
         case ENUMERATED: {
            DTSEnumerated dt = new DTSEnumerated();
            dt.setTypeName(ndt.getName());
            dt.setDescription(ndt.getDescription());
            ArrayList<MdmiNetEnumLiteral> literals = ndt.getEnumLiterals();
            for( MdmiNetEnumLiteral literal : literals ) {
               EnumerationLiteral el = new EnumerationLiteral();
               el.setName(literal.getName());
               el.setDescription(literal.getDescription());
               el.setCode(literal.getCode());
               dt.addLiteral(el);
            }
            dt.setReadonly(true);
         }
         case STRUCTURE: {
            DTCStructured dt = new DTCStructured();
            dt.setTypeName(ndt.getName());
            dt.setDescription(ndt.getDescription());
            ArrayList<MdmiNetField> fields = ndt.getFields();
            for( MdmiNetField field : fields ) {
               Field f = new Field();
               f.setName(field.getName());
               f.setDescription(field.getDescription());
               f.setMinOccurs(field.getMinOccurs());
               f.setMaxOccurs(field.getMaxOccurs());
               dt.getFields().add(f);
            }
            dt.setReadonly(true);
         }
         case CHOICE: {
            DTCChoice dt = new DTCChoice();
            dt.setTypeName(ndt.getName());
            dt.setDescription(ndt.getDescription());
            ArrayList<MdmiNetField> fields = ndt.getFields();
            for( MdmiNetField field : fields ) {
               Field f = new Field();
               f.setName(field.getName());
               f.setDescription(field.getDescription());
               f.setMinOccurs(field.getMinOccurs());
               f.setMaxOccurs(field.getMaxOccurs());
               dt.getFields().add(f);
            }
            dt.setReadonly(true);
         }
         default:
            throw new MdmiException("Invalid data type category returned from the service: NONE and PRIMITIVE not allowed!");
      }
   }
   
   private void resolveToModel( MdmiNetDatatype ndt, HashMap<String, MdmiDatatype> dataTypes ) {
      switch( ndt.getType() ) {
         case DERIVED: {
            DTSDerived dt = (DTSDerived)dataTypes.get(ndt.getName());
            String tn = ndt.getBaseType();
            MdmiDatatype t = dataTypes.get(tn);
            if( t == null && messageGroup != null )
               t = messageGroup.getDatatype(tn);
            if( t == null )
               throw new MdmiException("Cannot find base type {0} for data type {1}", tn, ndt.getName());
            try {
               dt.setBaseType((DTSimple)t);
            }
            catch( Exception ex ) {
               throw new MdmiException(ex, "Invalid base type {0} (must be DTSDerived) for data type {1}", tn, ndt.getName());
            }
         }
         case STRUCTURE: {
            DTCStructured dt = (DTCStructured)dataTypes.get(ndt.getName());
            ArrayList<MdmiNetField> fields = ndt.getFields();
            for( MdmiNetField field : fields ) {
               Field f = dt.getField(field.getName());
               String tn = field.getDataType();
               MdmiDatatype t = dataTypes.get(tn);
               if( t == null && messageGroup != null )
                  t = messageGroup.getDatatype(tn);
               if( t == null )
                  throw new MdmiException("Cannot find base type {0} for data type {1} field {2}", tn, ndt.getName()
                        , field.getName());
               f.setDatatype(t);
            }
         }
         case CHOICE: {
            DTCChoice dt = (DTCChoice)dataTypes.get(ndt.getName());
            ArrayList<MdmiNetField> fields = ndt.getFields();
            for( MdmiNetField field : fields ) {
               Field f = dt.getField(field.getName());
               String tn = field.getDataType();
               MdmiDatatype t = dataTypes.get(tn);
               if( t == null && messageGroup != null )
                  t = messageGroup.getDatatype(tn);
               if( t == null )
                  throw new MdmiException("Cannot find base type {0} for data type {1} field {2}", tn, ndt.getName()
                        , field.getName());
               f.setDatatype(t);
            }
         }
         case EXTERNAL:
         case ENUMERATED:
            break;
         default:
            throw new MdmiException("Invalid data type category returned from the service: NONE and PRIMITIVE not allowed!");
      }
   }
   
   private MdmiNetDatatype fromModel( MdmiDatatype dt ) {
      MdmiNetDatatype ndt = new MdmiNetDatatype();
      ndt.setName(dt.getName());
      ndt.setDescription(dt.getDescription());
      if( dt instanceof DTExternal ) {
         DTExternal t = (DTExternal)dt;
         ndt.setReferenceUri(t.getTypeSpec().toString());
         ndt.setType(MdmiNetDatatypeCategory.EXTERNAL);
      }
      else if( dt instanceof DTSDerived ) {
         DTSDerived t = (DTSDerived)dt;
         ndt.setBaseType(t.getBaseType().getName());
         ndt.setRestriction(t.getRestriction());
         ndt.setType(MdmiNetDatatypeCategory.DERIVED);
      }
      else if( dt instanceof DTSEnumerated ) {
         DTSEnumerated t = (DTSEnumerated)dt;
         for( EnumerationLiteral el : t.getLiterals() ) {
            MdmiNetEnumLiteral l = new MdmiNetEnumLiteral();
            l.setName(el.getName());
            l.setDescription(el.getDescription());
            l.setCode(el.getCode());
            ndt.getEnumLiterals().add(l);
         }
         ndt.setType(MdmiNetDatatypeCategory.ENUMERATED);
      }
      else if( dt instanceof DTCStructured ) {
         DTCStructured t = (DTCStructured)dt;
         for( Field f : t.getFields() ) {
            MdmiNetField fl = new MdmiNetField();
            fl.setName(f.getName());
            fl.setDescription(f.getDescription());
            fl.setMinOccurs(f.getMinOccurs());
            fl.setMaxOccurs(f.getMaxOccurs());
            fl.setDataType(f.getDatatype().getName());
            ndt.getFields().add(fl);
         }
         ndt.setType(MdmiNetDatatypeCategory.STRUCTURE);
      }
      else if( dt instanceof DTCChoice ) {
         DTCChoice t = (DTCChoice)dt;
         for( Field f : t.getFields() ) {
            MdmiNetField fl = new MdmiNetField();
            fl.setName(f.getName());
            fl.setDescription(f.getDescription());
            fl.setMinOccurs(f.getMinOccurs());
            fl.setMaxOccurs(f.getMaxOccurs());
            fl.setDataType(f.getDatatype().getName());
            ndt.getFields().add(fl);
         }
         ndt.setType(MdmiNetDatatypeCategory.CHOICE);
      }
      else
         throw new MdmiException("Invalid data type conversion requested: PRIMITIVE not allowed!");
      return ndt;
   }
} // MdmiDataTypeProxy
