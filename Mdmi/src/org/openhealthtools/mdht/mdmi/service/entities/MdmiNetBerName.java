package org.openhealthtools.mdht.mdmi.service.entities;

public class MdmiNetBerName {
   String name       ;
   String description;

   public MdmiNetBerName() {
   }

   public MdmiNetBerName( String name, String description ) {
	   this.name = name;
	   this.description = description;
   }

	public String getName() {
      return name;
   }

   public void setName( String name ) {
      this.name = name;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription( String description ) {
      this.description = description;
   }
} // MdmiNetBerName
