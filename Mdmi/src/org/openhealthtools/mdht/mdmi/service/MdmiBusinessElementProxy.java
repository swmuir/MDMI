package org.openhealthtools.mdht.mdmi.service;

import java.net.*;

import javax.ws.rs.core.*;

import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.config.*;

import org.openhealthtools.mdht.mdmi.MdmiException;
import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.service.entities.*;

public class MdmiBusinessElementProxy {
   private static final String RPATH = "bers";
   
   private String token;
   private WebResource service;
   private MdmiDatatypeProxy dtProxy;
   
   public MdmiBusinessElementProxy( URI uri, String token, MdmiDatatypeProxy dtProxy ) {
      if( uri == null || dtProxy == null )
         throw new IllegalArgumentException("Null service URI, or datatype proxy!");
      ClientConfig config = new DefaultClientConfig();
      Client client = Client.create(config);
      service = client.resource(uri);
      this.token = token;
      this.dtProxy = dtProxy;
   }
   
   public MdmiNetBusinessElement[] getAll( int offset ) {
      try {
         MdmiNetBusinessElement[] lst = new MdmiNetBusinessElement[0];
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
   
   public MdmiBusinessElementReference[] getAll( int offset, MessageGroup messageGroup, int nameIndex ) {
      try {
         MdmiNetBusinessElement[] lst = getAll(offset);
         MdmiBusinessElementReference[] bers = new MdmiBusinessElementReference[lst.length];
         for( int i = 0; i < lst.length; i++ ) {
	         bers[i] = toModel(messageGroup, lst[i], nameIndex);
	         messageGroup.getDomainDictionary().addBusinessElement(bers[i]);
         }
         return bers;
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Proxy getAll() failed!");
      }
   }
   
   public MdmiNetBusinessElement get( String value ) {
      try {
         MdmiNetBusinessElement nbr = service.path(RPATH + "/" + value).accept(MediaType.APPLICATION_XML)
               .get(MdmiNetBusinessElement.class);
         return nbr;
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
   
   public MdmiBusinessElementReference get( String value, MessageGroup messageGroup, int nameIndex ) {
      try {
         MdmiNetBusinessElement nbr = get(value);
         MdmiBusinessElementReference ber = toModel(messageGroup, nbr, nameIndex);
         messageGroup.getDomainDictionary().addBusinessElement(ber);
         return ber;
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
   
   public MdmiNetBusinessElement add( MdmiNetBusinessElement nbr ) {
      try {
         nbr = service.path(RPATH).queryParam("token", token).accept(MediaType.APPLICATION_XML)
               .post(MdmiNetBusinessElement.class, nbr);
         return nbr;
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Proxy add() failed!");
      }
   }
   
   public MdmiBusinessElementReference add( MdmiBusinessElementReference br, MessageGroup messageGroup, int nameIndex ) {
      try {
      	MdmiNetBusinessElement nbr = add(fromModel(br));
      	br = toModel(messageGroup, nbr, nameIndex);
         messageGroup.getDomainDictionary().addBusinessElement(br);
         return br;
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Proxy add() failed!");
      }
   }
   
   public MdmiNetBusinessElement update( MdmiNetBusinessElement nbr ) {
      try {
         nbr = service.path(RPATH + "/" + nbr.getUniqueId()).queryParam("token", token).accept(MediaType.APPLICATION_XML)
               .put(MdmiNetBusinessElement.class, nbr);
         return nbr;
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Proxy add() failed!");
      }
   }
   
   public MdmiBusinessElementReference update( MdmiBusinessElementReference br, MessageGroup messageGroup, int nameIndex ) {
      try {
      	MdmiNetBusinessElement nbr = update(fromModel(br));
      	br = toModel(messageGroup, nbr, nameIndex);
         messageGroup.getDomainDictionary().addBusinessElement(br);
         return br;
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Proxy add() failed!");
      }
   }
   
   public void delete( MdmiNetBusinessElement nbr ) {
   	delete(nbr.getUniqueId());
   }
   
   public void delete( MdmiBusinessElementReference br ) {
   	delete(br.getUniqueIdentifier());
   }
   
   public void delete( String uniqueIdentifier ) {
      try {
         service.path(RPATH + "/" + uniqueIdentifier).queryParam("token", token).delete(MdmiNetBusinessElement.class);
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Proxy delete() failed!");
      }
   }
   
   public MdmiBusinessElementReference toModel( MessageGroup messageGroup, MdmiNetBusinessElement nbr, int nameIndex ) {
   	if( nameIndex < 0 || nbr.getNames().size() <= nameIndex )
   		nameIndex = 0; // default for all that do not have multiple names
      MdmiBusinessElementReference br = new MdmiBusinessElementReference();
      String name = nbr.getNames().get(nameIndex).getName();
      String description = nbr.getNames().get(nameIndex).getDescription();
      br.setName(name);
      br.setDescription(description);
      try {
         br.setReference(new URI(nbr.getUri()));
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Invalid reference URI {0} for BER {1}.", nbr.getUri(), name);
      }
      br.setUniqueIdentifier(nbr.getUniqueId());
      br.setReadonly(true);
      br.setEnumValueDescrField(nbr.getEnumValueDescrField());
      br.setEnumValueField(nbr.getEnumValueField());
      br.setEnumValueSet(nbr.getEnumValueSet());
      br.setEnumValueSetField(nbr.getEnumValueSetField());
      String tn = nbr.getDataType();
      MdmiDatatype t = null;
      try {
         t = messageGroup.getDatatype(tn);
         if( t == null )
            t = dtProxy.get(messageGroup, tn);
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Cannot find datatype {0} for BER {1}.", tn, name);
      }
      br.setReferenceDatatype(t);
      return br;
   }
   
   public MdmiNetBusinessElement fromModel( MdmiBusinessElementReference br ) {
      MdmiNetBusinessElement nbr = new MdmiNetBusinessElement();
      nbr.getNames().add(new MdmiNetBerName(br.getName(), br.getDescription()));
      nbr.setUri(br.getReference() == null ? null : br.getReference().toString());
      nbr.setUniqueId(br.getUniqueIdentifier());
      nbr.setDataType(br.getReferenceDatatype().getName());
      return nbr;
   }
} // MdmiBusinessElementProxy
