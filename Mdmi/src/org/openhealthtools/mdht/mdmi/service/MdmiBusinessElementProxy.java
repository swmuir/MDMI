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

import org.openhealthtools.mdht.mdmi.MdmiException;
import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.service.entities.*;

public class MdmiBusinessElementProxy {
   private static final String RPATH = "bers";
   
   private String token;
   private WebResource service;
   private MessageGroup messageGroup;
   private MdmiDatatypeProxy dtProxy;
   
   public MdmiBusinessElementProxy( URI uri, String token, MessageGroup messageGroup, MdmiDatatypeProxy dtProxy ) {
      if( uri == null || messageGroup == null || dtProxy == null )
         throw new IllegalArgumentException("Null service URI, or message group, or datatype proxy!");
      ClientConfig config = new DefaultClientConfig();
      Client client = Client.create(config);
      service = client.resource(uri);
      this.token = token;
      this.messageGroup = messageGroup;
      this.dtProxy = dtProxy;
   }
   
   public MdmiBusinessElementReference[] getAll( int offset ) {
      try {
         MdmiNetBusinessElement[] lst = new MdmiNetBusinessElement[0];
         lst = service.path(RPATH).queryParam("offset", String.valueOf(offset))
               .accept(MediaType.APPLICATION_XML).get(lst.getClass());
         ArrayList<MdmiBusinessElementReference> a = new ArrayList<MdmiBusinessElementReference>();
         for( int i = 0; i < lst.length; i++ ) {
            MdmiBusinessElementReference br = toModel(lst[i]);
            messageGroup.getDomainDictionary().addBusinessElement(br);
            a.add(br);
         }
         return a.toArray(new MdmiBusinessElementReference[] {});
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Proxy getAll() failed!");
      }
   }
   
   public MdmiBusinessElementReference get( String value ) {
      try {
         MdmiNetBusinessElement nbr = service.path(RPATH + "/" + value).accept(MediaType.APPLICATION_XML)
               .get(MdmiNetBusinessElement.class);
         MdmiBusinessElementReference br = toModel(nbr);
         messageGroup.getDomainDictionary().addBusinessElement(br);
         return br;
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Proxy get() failed!");
      }
   }
   
   public MdmiBusinessElementReference add( MdmiBusinessElementReference br ) {
      try {
         MdmiNetBusinessElement o = fromModel(br);
         MdmiNetBusinessElement nbr = service.path(RPATH).queryParam("token", token).accept(MediaType.APPLICATION_XML)
               .post(MdmiNetBusinessElement.class, o);
         br = toModel(nbr);
         MdmiBusinessElementReference existing = messageGroup.getDomainDictionary().getBusinessElement(br.getName());
         if( null != existing )
            messageGroup.getDomainDictionary().getBusinessElements().remove(existing);
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
   
   public MdmiBusinessElementReference update( MdmiBusinessElementReference br ) {
      try {
         MdmiNetBusinessElement o = fromModel(br);
         MdmiNetBusinessElement nbr = service.path(RPATH + "/" + o.getName()).queryParam("token", token).accept(MediaType.APPLICATION_XML)
               .put(MdmiNetBusinessElement.class, o);
         br = toModel(nbr);
         MdmiBusinessElementReference existing = messageGroup.getDomainDictionary().getBusinessElement(br.getName());
         if( null != existing )
            messageGroup.getDomainDictionary().getBusinessElements().remove(existing);
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
   
   public void delete( MdmiBusinessElementReference br ) {
      try {
         service.path(RPATH + "/" + br.getName()).queryParam("token", token).delete(MdmiNetBusinessElement.class);
         if( null != messageGroup.getDatatype(br.getName()) )
            messageGroup.getDomainDictionary().getBusinessElements().remove(br);
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Proxy delete() failed!");
      }
   }
   
   private MdmiBusinessElementReference toModel( MdmiNetBusinessElement nbr ) {
      MdmiBusinessElementReference br = new MdmiBusinessElementReference();
      br.setName(nbr.getName());
      br.setDescription(nbr.getDescription());
      try {
         br.setReference(new URI(nbr.getUri()));
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Invalid reference URI {0} for BER {1}.", nbr.getUri(), nbr.getName());
      }
      br.setUniqueIdentifier(nbr.getUniqueId());
      br.setReadonly(true);
      String tn = nbr.getDataType();
      MdmiDatatype t = null;
      try {
         t = messageGroup.getDatatype(tn);
         if( t == null )
            t = dtProxy.get(tn);
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Cannot find datatype {0} for BER {1}.", tn, nbr.getName());
      }
      br.setReferenceDatatype(t);
      return br;
   }
   
   private MdmiNetBusinessElement fromModel( MdmiBusinessElementReference br ) {
      MdmiNetBusinessElement nbr = new MdmiNetBusinessElement();
      nbr.setName(br.getName());
      nbr.setDescription(br.getDescription());
      nbr.setUri(br.getReference() == null ? null : br.getReference().toString());
      nbr.setUniqueId(br.getUniqueIdentifier());
      nbr.setDataType(br.getReferenceDatatype().getName());
      return nbr;
   }
} // MdmiBusinessElementProxy
