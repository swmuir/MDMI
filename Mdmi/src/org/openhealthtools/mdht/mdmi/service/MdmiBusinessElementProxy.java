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
   
   public MdmiBusinessElementProxy( URI uri, String token ) {
      this(uri, token, null);
   }
   
   public MdmiBusinessElementProxy( URI uri, String token, MessageGroup messageGroup  ) {
      ClientConfig config = new DefaultClientConfig();
      Client client = Client.create(config);
      service = client.resource(uri);
      this.token = token;
      this.messageGroup = messageGroup;
   }
   
   public MdmiBusinessElementReference[] getAll() {
      try {
         MdmiNetBusinessElement[] lst = new MdmiNetBusinessElement[0];
         lst = service.path(RPATH).accept(MediaType.APPLICATION_XML).get(lst.getClass());
         ArrayList<MdmiBusinessElementReference> a = new ArrayList<MdmiBusinessElementReference>();
         for( int i = 0; i < lst.length; i++ ) {
            MdmiBusinessElementReference br = toModel(lst[i]);
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
         return toModel(nbr);
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
         return toModel(nbr);
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
         return toModel(nbr);
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Proxy add() failed!");
      }
   }
   
   public void delete( String value ) {
      try {
         service.path(RPATH + "/" + value).queryParam("token", token).delete(MdmiNetBusinessElement.class);
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
      if( messageGroup != null ) {
         t = messageGroup.getDatatype(tn);
         if( t == null )
            throw new MdmiException("Cannot find data type {0} for BER {1}", tn, nbr.getName());
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
