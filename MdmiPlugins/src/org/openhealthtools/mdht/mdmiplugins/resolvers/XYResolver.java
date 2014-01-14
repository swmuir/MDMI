package org.openhealthtools.mdht.mdmiplugins.resolvers;

import java.net.URI;
import java.util.ArrayList;

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.model.*;

public class XYResolver implements IExternalResolver {
   private static final URI URI_X = URI.create("mdmi://external.types.mdmi.org/colors_x");
   private static final URI URI_Y = URI.create("mdmi://external.types.mdmi.org/colors_y");
   
   @Override
   public boolean canHandle( DTExternal dataType ) {
      return dataType.getTypeSpec().equals(URI_X) || dataType.getTypeSpec().equals(URI_Y);
   }

   @Override
   public ArrayList<String> getHandledDataTypes() {
      ArrayList<String> a = new ArrayList<String>();
      a.add(URI_X.toString());
      a.add(URI_Y.toString());
      return a;
   }

   @Override
   public Object getDictionaryValue( DTExternal dataType, String value ) {
      if( null == value )
         return null;
      if( dataType.getTypeSpec().equals(URI_X) ) {
         if( value.equals("R") )
            return "Red";
         if( value.equals("G") )
            return "Green";
         if( value.equals("B") )
            return "Blue";
         return "Unknown";
      }
      else if( dataType.getTypeSpec().equals(URI_Y) ) {
         if( value.equals("1") )
            return "Red";
         if( value.equals("2") )
            return "Green";
         if( value.equals("3") )
            return "Blue";
         return "Unknown";
      }
      return null;
   }

   @Override
   public String getModelValue( DTExternal dataType, Object valueObj ) {
      if( null == valueObj )
         return null;
      String value = (String)valueObj;
      if( dataType.getTypeSpec().equals(URI_X) ) {
         if( value.equals("Red") )
            return "R";
         if( value.equals("Green") )
            return "G";
         if( value.equals("Blue") )
            return "B";
         return "U";
      }
      else if( dataType.getTypeSpec().equals(URI_Y) ) {
         if( value.equals("Red") )
            return "1";
         if( value.equals("Green") )
            return "2";
         if( value.equals("Blue") )
            return "3";
         return "0";
      }
      return null;
   }

} // XResolver
