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
   
   public MdmiDatatypeProxy( URI uri, String token ) {
      if( uri == null )
         throw new IllegalArgumentException("Null service URI!");
      ClientConfig config = new DefaultClientConfig();
      Client client = Client.create(config);
      service = client.resource(uri);
      this.token = token;
   }
   
   public MdmiNetDatatype[] getAll( int offset ) {
      try {
         MdmiNetDatatype[] lst = new MdmiNetDatatype[0];
         lst = service.path(RPATH).queryParam("offset", String.valueOf(offset))
               .accept(MediaType.APPLICATION_XML).get(lst.getClass());
         return lst;
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Proxy getAll() failed!");
      }
   }
   
   public MdmiDatatype[] getAll( MessageGroup messageGroup, int offset ) {
      try {
         MdmiNetDatatype[] lst = getAll(offset);
         MdmiDatatype[] mds = new MdmiDatatype[lst.length];
         for( int i = 0; i < lst.length; i++ ) {
            MdmiDatatype t = toModel(lst[i]);
            messageGroup.addDatatype(t);
            resolveToModel(messageGroup, lst[i], t);
	         mds[i] = t;
         }
         return mds;
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Proxy getAll() failed!");
      }
   }
   
   public MdmiNetDatatype get( String name ) {
      try {
         MdmiNetDatatype ndt = service.path(RPATH + "/" + name).accept(MediaType.APPLICATION_XML)
               .get(MdmiNetDatatype.class);
         return ndt;
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         if( 0 < ex.getMessage().indexOf("404") )
            return null;
         throw new MdmiException(ex, "Proxy get() failed!");
      }
   }
   
   public MdmiDatatype get( MessageGroup messageGroup, String name ) {
      MdmiDatatype t = messageGroup.getDatatype(name);
      if( t != null )
         return t;
      try {
         MdmiNetDatatype ndt = get(name);
         t = toModel(ndt);
         messageGroup.addDatatype(t);
         resolveToModel(messageGroup, ndt, t);
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Cannot find datatype {0}", name);
      }
      return t;
   }
   
   public MdmiNetDatatype add( MdmiNetDatatype dt ) {
      try {
         MdmiNetDatatype ndt = service.path(RPATH).queryParam("token", token).accept(MediaType.APPLICATION_XML)
               .post(MdmiNetDatatype.class, dt);
         return ndt;
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Proxy add() failed!");
      }
   }
   
   public MdmiDatatype add( MessageGroup messageGroup, MdmiDatatype dt ) {
      try {
      	MdmiNetDatatype ndt = add(fromModel(dt));
         MdmiDatatype t = toModel(ndt);
         messageGroup.addDatatype(t);
         resolveToModel(messageGroup, ndt, t);
         return t;
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Proxy add() failed!");
      }
   }
   
   public MdmiNetDatatype update( MdmiNetDatatype dt ) {
      try {
         MdmiNetDatatype ndt = service.path(RPATH + "/" + dt.getName()).queryParam("token", token).accept(MediaType.APPLICATION_XML)
               .put(MdmiNetDatatype.class, dt);
         return ndt;
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Proxy update() failed!");
      }
   }
   
   public MdmiDatatype update( MessageGroup messageGroup, MdmiDatatype dt ) {
      try {
      	MdmiNetDatatype ndt = update(fromModel(dt));
         MdmiDatatype t = toModel(ndt);
         messageGroup.addDatatype(t);
         resolveToModel(messageGroup, ndt, t);
         return t;
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Proxy update() failed!");
      }
   }
   
   public void delete( MdmiNetDatatype datatype ) {
      try {
         service.path(RPATH + "/" + datatype.getName()).queryParam("token", token).delete(MdmiNetDatatype.class);
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Proxy delete() failed!");
      }
   }
   
   public void delete( MessageGroup messageGroup, MdmiDatatype datatype ) {
      try {
         delete(fromModel(datatype));
         messageGroup.getDatatypes().remove(datatype);
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Proxy delete() failed!");
      }
   }
   
   public MdmiDatatype toModel( MdmiNetDatatype ndt ) {
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
            return dt;
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
            return dt;
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
            return dt;
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
            return dt;
         }
         default:
            throw new MdmiException("Invalid data type category returned from the service: NONE and PRIMITIVE not allowed!");
      }
   }
   
   public MdmiNetDatatype fromModel( MdmiDatatype dt ) {
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
   
   private void resolveToModel( MessageGroup messageGroup, MdmiNetDatatype ndt, MdmiDatatype dataType ) {
      switch( ndt.getType() ) {
         case DERIVED: {
            String tn = null;
            try {
               DTSDerived dt = (DTSDerived)dataType;
               tn = ndt.getBaseType();
               dt.setBaseType((DTSimple)get(messageGroup, tn));
               return;
            }
            catch( Exception ex ) {
               throw new MdmiException(ex, "Invalid base type {0} (must be DTSDerived) for data type {1}"
                     , tn, ndt.getName());
            }
         }
         case STRUCTURE: {
            String tn = null;
            Field f = null;
            try {
               DTCStructured dt = (DTCStructured)dataType;
               ArrayList<MdmiNetField> fields = ndt.getFields();
               for( MdmiNetField field : fields ) {
                  f = dt.getField(field.getName());
                  tn = field.getDataType();
                  f.setDatatype(get(messageGroup, tn));
               }
               return;
            }
            catch( Exception ex ) {
               throw new MdmiException("Cannot find data type {0} for field {1} or data type {2}", tn
                     , f.getName(), ndt.getName());
            }
         }
         case CHOICE: {
            String tn = null;
            Field f = null;
            try {
               DTCChoice dt = (DTCChoice)dataType;
               ArrayList<MdmiNetField> fields = ndt.getFields();
               for( MdmiNetField field : fields ) {
                  f = dt.getField(field.getName());
                  tn = field.getDataType();
                  f.setDatatype(get(messageGroup, tn));
               }
               return;
            }
            catch( Exception ex ) {
               throw new MdmiException("Cannot find data type {0} for field {1} or data type {2}", tn
                     , f.getName(), ndt.getName());
            }
         }
         case EXTERNAL:
         case ENUMERATED:
            return;
         default:
            throw new MdmiException("Invalid data type category returned from the service: NONE and PRIMITIVE not allowed!");
      }
   }
} // MdmiDataTypeProxy
