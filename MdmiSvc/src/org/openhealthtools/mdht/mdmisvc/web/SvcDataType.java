package org.openhealthtools.mdht.mdmisvc.web;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.*;

import org.openhealthtools.mdht.mdmi.service.entities.*;

public class SvcDataType {
   
   @Context
   UriInfo uriInfo;
   
   @Context
   Request request;
   
   String name;
   String token;
   
   public SvcDataType( UriInfo uriInfo, Request request, String name, String token ) {
      this.uriInfo = uriInfo;
      this.request = request;
      this.name    = name;
      this.token   = token;
   }
   
   @GET
   @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON } )
   public Response getOne() {
      MdmiNetDatatype o = SvcDataTypes.collection.find(name);
      if( o == null )
         return Response.status(Response.Status.NOT_FOUND).build();
      return Response.ok(o).build();
   }
   
   @PUT
   @Consumes( MediaType.APPLICATION_XML )
   @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON } )
   public Response updateOne( JAXBElement<MdmiNetDatatype> dataType ) {
      if( !AppListener.IsValid(token) )
         return Response.status(Response.Status.FORBIDDEN).type("text/html")
               .entity("<h3>Bad token!</h3>").build();
      MdmiNetDatatype o = dataType.getValue();
      String err = SvcDataTypes.isDataTypeValid(o, true);
      if( err != null )
         return Response.status(Response.Status.BAD_REQUEST).type("text/html").entity(err).build();
      if( !SvcDataTypes.collection.update(o) )
         return Response.status(Response.Status.BAD_REQUEST).type("text/html")
               .entity("<h3>Invalid data type name!</h3>").build();
      return Response.ok(o).build();
   }
   
   @DELETE
   @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON } )
   public Response deleteOne() {
      if( !AppListener.IsValid(token) )
         return Response.status(Response.Status.FORBIDDEN).type("text/html")
               .entity("<h3>Bad token!</h3>").build();
      MdmiNetDatatype o = SvcDataTypes.collection.delete(name);
      if( o == null )
         return Response.status(Response.Status.NOT_FOUND).build();
      return Response.ok(o).build();
   }
} // SvcDataType
