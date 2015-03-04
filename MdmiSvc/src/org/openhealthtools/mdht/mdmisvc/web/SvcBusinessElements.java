package org.openhealthtools.mdht.mdmisvc.web;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.*;

import org.openhealthtools.mdht.mdmi.service.entities.*;

@Path("/bers")
public class SvcBusinessElements {
   static MdmiNetBusinessElementCollection collection
         = new MdmiNetBusinessElementCollection(AppListener.getMongoDbUri());
   
   @Context
   UriInfo uriInfo;
   
   @Context
   Request request;

   @GET
   @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON } )
   public MdmiNetBusinessElement[] getAll( @QueryParam("offset") String offset ) {
      return collection.getAll(Integer.valueOf(offset)).toArray(new MdmiNetBusinessElement[0]);
   }
   
   @POST
   @Consumes( MediaType.APPLICATION_XML )
   @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON } )
   public Response addOne( JAXBElement<MdmiNetBusinessElement> businessElement, @QueryParam("token") String token ) {
      if( !AppListener.IsValid(token) )
         return Response.status(Response.Status.FORBIDDEN).type("text/html")
               .entity("<h3>Bad token!</h3>").build();
      MdmiNetBusinessElement o = businessElement.getValue();
      String err = isBusinessElementValid(o, true);
      if( err != null )
         return Response.status(Response.Status.BAD_REQUEST).type("text/html").entity(err).build();
      if( !collection.append(o) )
         return Response.status(Response.Status.BAD_REQUEST).type("text/html")
               .entity("<h3>Duplicate name or uniqueId!</h3>").build();
      return Response.ok(o).build();
   }
   
   @Path("{businessElement}")
   public SvcBusinessElement getSvcBusinessElement( @PathParam("businessElement") String name, @QueryParam("token") String token ) {
      return new SvcBusinessElement(uriInfo, request, name, token);
   }
   
   // return null if it is valid
   static String isBusinessElementValid( MdmiNetBusinessElement o, boolean forInsert ) {
      if( o.getNames().size() <= 0 )
         return "<h3>No name(s) provided!</h3>";
      if( null == o.getNames().get(0).getName() || o.getNames().get(0).getName().length() <= 0 )
         return "<h3>No name(s) provided, or an empty name provided!</h3>";
      if( null == o.getUniqueId() || o.getUniqueId().length() <= 0 )
         return "<h3>UniqueId not set to a valid value!</h3>";
      if( null == o.getUri() || o.getUri().length() <= 0 )
         return "<h3>URI not set to a valid value!</h3>";
      if( null == o.getDataType() || o.getDataType().length() <= 0 )
         return "<h3>Data type not set to a valid value!</h3>";
      if( !MdmiNetDatatype.isPrimitiveDatatype(o.getDataType()) ) {
         MdmiNetDatatype t = SvcDataTypes.collection.find(o.getDataType());
         if( t == null )
            return "<h3>Data type not found in the dictionary, and it is not a primitive!</h3>";
      }
      return null;
   }
} // MdmiDictionary
