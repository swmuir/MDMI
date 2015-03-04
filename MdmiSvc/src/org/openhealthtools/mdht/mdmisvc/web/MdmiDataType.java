package org.openhealthtools.mdht.mdmisvc.web;

import java.util.*;

import org.bson.types.*;

import com.google.code.morphia.annotations.*;
import com.mongodb.*;
import com.google.code.morphia.*;

import org.openhealthtools.mdht.mdmi.service.entities.*;

@Entity("DataTypes")
class MdmiNetDataTypeDA {
   @Id
   ObjectId                      _id;
   MdmiNetDatatypeCategory       type;
   String                        name;
   String                        desc;
   String                        rUri;
   String                        bTyp;
   String                        rest;
   ArrayList<MdmiNetEnumLiteral> lits = new ArrayList<MdmiNetEnumLiteral>();
   ArrayList<MdmiNetField>       flds = new ArrayList<MdmiNetField>();
   
   public MdmiNetDataTypeDA() {
   }
   
   public MdmiNetDataTypeDA( MdmiNetDatatype src ) {
      _id  = new ObjectId();
      type = src.getType();
      name = src.getName();
      desc = src.getDescription();
      rUri = src.getReferenceUri();
      bTyp = src.getBaseType();
      rest = src.getRestriction();
      
      lits = src.getEnumLiterals();
      flds = src.getFields();
   }
   
   public MdmiNetDatatype toBO() {
      MdmiNetDatatype bo = new MdmiNetDatatype();
      bo.setType(type);
      bo.setName(name);
      bo.setDescription(desc);
      bo.setReferenceUri(rUri);
      bo.setBaseType(bTyp);
      bo.setRestriction(rest);
      bo.getEnumLiterals().addAll(lits);
      bo.getFields().addAll(flds);
      return bo;
   }
   
  @Override
  public String toString() {
     StringBuilder sb = new StringBuilder();
     sb.append("{name: '").append(name).append("'");

     if( desc != null )
        sb.append(", desc: '").append(desc).append("'");

     if( rUri != null )
        sb.append(", rUri: '").append(rUri).append("'");

     if( bTyp != null )
        sb.append(", bTyp: '").append(bTyp).append("'");

     if( rest != null )
        sb.append(", rest: '").append(rest).append("'");
     
     if( 0 < lits.size() ) {
        sb.append(", lits: [");
        for( int i = 0; i < lits.size(); i++ ) {
           if( 0 < i )
              sb.append(", ");
           sb.append(lits.toString());
        }
        sb.append("]");
     }
     
     if( 0 < flds.size() ) {
        sb.append(", flds: [");
        for( int i = 0; i < flds.size(); i++ ) {
           if( 0 < i )
              sb.append(", ");
           sb.append(flds.toString());
        }
        sb.append("]");
     }
     sb.append("}");
     return sb.toString();
  }
} // MdmiNetDataTypeDA

class MdmiNetDataTypeCollection {
   private final static String DB_NAME = "ReferentIndex";
   private Mongo     server;
   private Morphia   morphia;
   private Datastore ds;
   
   public MdmiNetDataTypeCollection( String mongoDbUri ) {
      try {
         server = new Mongo(mongoDbUri);
         morphia = new Morphia();
         morphia.map(MdmiNetDataTypeDA.class);
         ds = morphia.createDatastore(server, DB_NAME);
      }
      catch( Exception ex ) {
         throw new RuntimeException("MdmiNetDataTypeCollection.ctor fails", ex);
      }
   }
   
   MdmiNetDatatype find( String name ) {
      MdmiNetDataTypeDA o = findOne(name);
      if( null == o )
         AppListener.logger.warn("MdmiNetDataType.find('" + name + "') not found!");
      else
         AppListener.logger.debug("MdmiNetDataType.find('" + name + "') found: " + o.toString());
      return o == null ? null : o.toBO();
   }
   
   List<MdmiNetDatatype> getAll( int offset ) {
      List<MdmiNetDataTypeDA> daList = ds.find(MdmiNetDataTypeDA.class).limit(100).offset(offset).asList();
      List<MdmiNetDatatype> boList = new ArrayList<MdmiNetDatatype>();
      for( int i = 0; i < daList.size(); i++ ) {
         boList.add(daList.get(i).toBO());
      }
      AppListener.logger.debug("MdmiNetDataType.getAll() returns: " + boList.toString());
      return boList;
   }
   
   boolean append( MdmiNetDatatype dataType ) {
      if( null != findOne(dataType.getName()) ) {
         AppListener.logger.error("MdmiNetDataType.append fails, duplicate name: '" + dataType.getName() + "'");
         return false;
      }
      MdmiNetDataTypeDA o = new MdmiNetDataTypeDA(dataType);
      ds.save(o);
      AppListener.logger.debug("MdmiNetDataType.append success: " + o.toString());
      return true;
   }
   
   boolean update( MdmiNetDatatype dataType ) {
      MdmiNetDataTypeDA found = findOne(dataType.getName());
      MdmiNetDataTypeDA o = new MdmiNetDataTypeDA(dataType);
      if( null != found )
         o._id = found._id;
      ds.save(o);
      AppListener.logger.debug("MdmiNetDataType.update success: " + o.toString());
      return true;
   }
   
   MdmiNetDatatype delete( String name ) {
      MdmiNetDataTypeDA o = findOne(name);
      if( null == o ) {
         AppListener.logger.warn("MdmiNetDataType.delete fails, name: '" + name + "' not found!");
         return null;
      }
      ds.delete(MdmiNetDataTypeDA.class, o._id);
      AppListener.logger.debug("MdmiNetDataType.delete success, deleted: " + o.toString());
      return o.toBO();
   }

   private MdmiNetDataTypeDA findOne( String name ) {
      return ds.find(MdmiNetDataTypeDA.class).field("name").equal(name).get();
   }
} // MdmiNetDataTypeCollection