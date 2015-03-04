package org.openhealthtools.mdht.mdmisvc.web;

import java.util.*;

import org.bson.types.*;

import com.google.code.morphia.annotations.*;
import com.mongodb.*;
import com.google.code.morphia.*;

import org.openhealthtools.mdht.mdmi.service.entities.*;

@Entity("BusinessElements")
class MdmiNetBusinessElementDA {
   @Id ObjectId                _id  ;
   ArrayList<MdmiNetBerNameDA> names = new ArrayList<MdmiNetBerNameDA>();
   String                      uri  ;
   String                      unId ;
   String                      dTyp ;
   String                      evdf ;
   String                      evf  ;
   String                      evs  ;
   String                      evsf ;
   
   public MdmiNetBusinessElementDA() {
   }
   
   public MdmiNetBusinessElementDA( MdmiNetBusinessElement src ) {
      _id   = new ObjectId();
      for( int i = 0; i < src.getNames().size(); i++ ) {
         MdmiNetBerName n = src.getNames().get(i);
         names.add(new MdmiNetBerNameDA(n.getName(), n.getDescription()));
      }
      uri   = src.getUri();
      unId  = src.getUniqueId();
      dTyp  = src.getDataType();
      evdf  = src.getEnumValueDescrField();
      evf   = src.getEnumValueField();
      evs   = src.getEnumValueSet();
      evsf  = src.getEnumValueSetField();
   }
   
   public MdmiNetBusinessElement toBO() {
      MdmiNetBusinessElement bo = new MdmiNetBusinessElement();
      for( int i = 0; i < names.size(); i++ ) {
         MdmiNetBerNameDA o = names.get(i);
         bo.getNames().add(new MdmiNetBerName(o.name, o.descr));
      }
      bo.setUri(uri);
      bo.setUniqueId(unId);
      bo.setDataType(dTyp);
      bo.setEnumValueDescrField(evdf);
      bo.setEnumValueField(evf);
      bo.setEnumValueSet(evs);
      bo.setEnumValueSetField(evsf);
      return bo;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      sb.append("names: [");
      for( int i = 0; i < names.size(); i++ ) {
         if( 0 < i )
            sb.append(", ");
         sb.append(names.get(i).toString());
      }
      sb.append("]");
      sb.append(", uri: '").append(uri).append("'");
      sb.append(", unId: '").append(unId).append("'");
      sb.append(", dTyp: '").append(dTyp).append("'");
      sb.append("}");
      return sb.toString();
   }
} // MdmiDataTypeDA

class MdmiNetBerNameDA {
   String       name ;
   String       descr;
   
   public MdmiNetBerNameDA() {
   }
   
   public MdmiNetBerNameDA( String name, String descr ) {
      this.name = name;
      this.descr = descr;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      sb.append("name: '").append(name).append("'");
      sb.append(", descr: '").append(descr).append("'}");
      return sb.toString();
   }
}

class MdmiNetBusinessElementCollection {
   private final static String DB_NAME = "ReferentIndex";
   private Mongo     server;
   private Morphia   morphia;
   private Datastore ds;
   
   public MdmiNetBusinessElementCollection( String mongoDbUri ) {
      try {
         server = new Mongo(mongoDbUri);
         morphia = new Morphia();
         morphia.map(MdmiNetDataTypeDA.class);
         ds = morphia.createDatastore(server, DB_NAME);
      }
      catch( Exception ex ) {
         throw new RuntimeException("MdmiNetBusinessElementCollection.ctor fails", ex);
      }
   }
   
   MdmiNetBusinessElement find( String name ) {
      MdmiNetBusinessElementDA o = findOne(name);
      if( null == o )
         AppListener.logger.warn("MdmiNetBusinessElement.find('" + name + "') not found.");
      else
         AppListener.logger.debug("MdmiNetBusinessElement.find('" + name + "') found: " + o.toString());
      return o == null ? null : o.toBO();
   }
   
   List<MdmiNetBusinessElement> getAll( int offset ) {
      List<MdmiNetBusinessElementDA> daList = ds.find(MdmiNetBusinessElementDA.class).limit(100).offset(offset).asList();
      List<MdmiNetBusinessElement> boList = new ArrayList<MdmiNetBusinessElement>();
      for( int i = 0; i < daList.size(); i++ ) {
         boList.add(daList.get(i).toBO());
      }
      AppListener.logger.debug("MdmiNetBusinessElement.getAll() returns: " + boList.toString());
      return boList;
   }
   
   boolean append( MdmiNetBusinessElement businessElement ) {
      if( null != findOneByUniqueId(businessElement.getUniqueId()) ) {
         AppListener.logger.error("MdmiNetBusinessElement.append fails, duplicate uniqueId: '" + businessElement.getUniqueId() + "'");
         return false;
      }
      MdmiNetBusinessElementDA o = new MdmiNetBusinessElementDA(businessElement);
      ds.save(o);
      AppListener.logger.debug("MdmiNetBusinessElement.append success: " + o.toString());
      return true;
   }
   
   boolean update( MdmiNetBusinessElement businessElement ) {
      MdmiNetBusinessElementDA fid = findOneByUniqueId(businessElement.getUniqueId());
      MdmiNetBusinessElementDA o = new MdmiNetBusinessElementDA(businessElement);
      if( null != fid )
         o._id = fid._id;
      ds.save(o);
      AppListener.logger.debug("MdmiNetBusinessElement.update success: " + o.toString());
      return true;
   }
   
   MdmiNetBusinessElement delete( String uniqueId ) {
      MdmiNetBusinessElementDA o = findOneByUniqueId(uniqueId);
      if( o == null ) {
         AppListener.logger.warn("MdmiNetBusinessElement.delete fails, uniqueId: '" + uniqueId + "' not found!");
         return null;
      }
      ds.delete(MdmiNetBusinessElementDA.class, o._id);
      AppListener.logger.debug("MdmiNetBusinessElement.delete success, deleted: " + o.toString());
      return o.toBO();
   }
   
   private MdmiNetBusinessElementDA findOne( String name ) {
      return ds.find(MdmiNetBusinessElementDA.class).field("names.name").equal(name).get();
   }
   
   private MdmiNetBusinessElementDA findOneByUniqueId( String uniqueId ) {
      return ds.find(MdmiNetBusinessElementDA.class).field("unId").equal(uniqueId).get();
   }
} // MdmiNetBusinessElementCollection