package org.openhealthtools.mdht.mdmisvc.web;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.*;

import org.openhealthtools.mdht.mdmi.service.entities.*;

@Path("/datatypes")
public class SvcDataTypes {
   static MdmiNetDataTypeCollection collection 
         = new MdmiNetDataTypeCollection(AppListener.getMongoDbUri());
   
   @Context
   UriInfo uriInfo;
   
   @Context
   Request request;

   @GET
   @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON } )
   public MdmiNetDatatype[] getAll() {
      return collection.getAll().toArray(new MdmiNetDatatype[0]);
   }
   
   @POST
   @Consumes( MediaType.APPLICATION_XML )
   @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON } )
   public Response addOne( JAXBElement<MdmiNetDatatype> dataType, @QueryParam("token") String token ) {
      if( !AppListener.IsValid(token) )
         return Response.status(Response.Status.FORBIDDEN).type("text/html")
               .entity("<h3>Bad token!</h3>").build();
      MdmiNetDatatype o = dataType.getValue();
      String err = isDataTypeValid(o, true);
      if( err != null )
         return Response.status(Response.Status.BAD_REQUEST).type("text/html").entity(err).build();
      if( !collection.append(o) )
         return Response.status(Response.Status.BAD_REQUEST).type("text/html")
               .entity("<h3>Duplicate name!</h3>").build();
      return Response.ok(o).build();
   }
   
   @Path("{dataType}")
   public SvcDataType getMdmiNetDataTypeSvc( @PathParam("dataType") String name, @QueryParam("token") String token ) {
      return new SvcDataType(uriInfo, request, name, token);
   }
   
   // return null if it is valid
   static String isDataTypeValid( MdmiNetDatatype o, boolean forInsert ) {
      if( null == o.getName() || o.getName().length() <= 0 )
         return "<h3>Name is null or empty!</h3>";
      if( MdmiNetDatatype.isPrimitiveDatatype(o.getName()) )
         return "<h3>Invalid data type - cannot redefine primitive data types!</h3>";
      if( null == o.getType() || o.getType() == MdmiNetDatatypeCategory.NONE )
         return "<h3>Type not set, or set to an invalid value!</h3>";
      if( MdmiNetDatatypeCategory.PRIMITIVE == o.getType() )
         return "<h3>Invalid data type - cannot add primitive data types!</h3>";
      if( MdmiNetDatatypeCategory.EXTERNAL == o.getType() ) {
         if( null == o.getReferenceUri() || o.getReferenceUri().length() <= 0 )
            return "<h3>Reference URI not set to a valid value!</h3>";
         o.setBaseType(null);
         o.setRestriction(null);
         o.getEnumLiterals().clear();
         o.getFields().clear();
      }
      else if( MdmiNetDatatypeCategory.DERIVED == o.getType() ) {
         if( null == o.getBaseType() || o.getBaseType().length() <= 0 )
            return "<h3>Base type name is null or empty!</h3>";
         if( null == o.getRestriction() || o.getRestriction().length() <= 0 )
            return "<h3>Restriction of derived type is null or empty!</h3>";
         if( !MdmiNetDatatype.isPrimitiveDatatype(o.getBaseType()) ) {
            MdmiNetDatatype t = SvcDataTypes.collection.find(o.getBaseType());
            if( t == null )
               return "<h3>Base type not found in the dictionary, and it is not a primitive!</h3>";
         }
         o.setReferenceUri(null);
         o.getEnumLiterals().clear();
         o.getFields().clear();
      }
      else if( MdmiNetDatatypeCategory.ENUMERATED == o.getType() ) {
         o.setReferenceUri(null);
         o.setBaseType(null);
         o.setRestriction(null);
         o.getFields().clear();
      }
      else if( MdmiNetDatatypeCategory.STRUCTURE == o.getType() ) {
         o.setReferenceUri(null);
         o.setBaseType(null);
         o.setRestriction(null);
         o.getEnumLiterals().clear();
      }
      else if( MdmiNetDatatypeCategory.CHOICE == o.getType() ) {
         o.setReferenceUri(null);
         o.setBaseType(null);
         o.setRestriction(null);
         o.getEnumLiterals().clear();
      }
      return null;
   }
} // SvcDataTypes
