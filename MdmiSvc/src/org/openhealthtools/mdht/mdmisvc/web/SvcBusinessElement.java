package org.openhealthtools.mdht.mdmisvc.web;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.*;

import org.openhealthtools.mdht.mdmi.service.entities.*;

public class SvcBusinessElement {
   
   @Context
   UriInfo uriInfo;
   
   @Context
   Request request;
   
   String name;
   String token;
   
   public SvcBusinessElement( UriInfo uriInfo, Request request, String name, String token ) {
      this.uriInfo = uriInfo;
      this.request = request;
      this.name    = name;
      this.token   = token;
   }
   
   @GET
   @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON } )
   public Response getOne() {
      MdmiNetBusinessElement o = SvcBusinessElements.collection.find(name);
      if( o == null )
         return Response.status(Response.Status.NOT_FOUND).build();
      return Response.ok(o).build();
   }
   
   @PUT
   @Consumes( MediaType.APPLICATION_XML )
   @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON } )
   public Response updateOne( JAXBElement<MdmiNetBusinessElement> businessElement ) {
      if( !AppListener.IsValid(token) )
         return Response.status(Response.Status.FORBIDDEN).type("text/html")
               .entity("<h3>Bad token!</h3>").build();
      MdmiNetBusinessElement o = businessElement.getValue();
      String err = SvcBusinessElements.isBusinessElementValid(o, false);
      if( err != null )
         return Response.status(Response.Status.BAD_REQUEST).type("text/html").entity(err).build();
      if( !SvcBusinessElements.collection.update(o) )
         return Response.status(Response.Status.BAD_REQUEST).type("text/html")
               .entity("<h3>Unique ID constraint violation!</h3>").build();
      return Response.ok(o).build();
   }
   
   @DELETE
   @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON } )
   public Response deleteOne() {
      if( !AppListener.IsValid(token) )
         return Response.status(Response.Status.FORBIDDEN).type("text/html")
               .entity("<h3>Bad token!</h3>").build();
      MdmiNetBusinessElement o = SvcBusinessElements.collection.delete(name);
      if( o == null )
         return Response.status(Response.Status.NOT_FOUND).build();
      return Response.ok(o).build();
   }
} // SvcBusinessElement
