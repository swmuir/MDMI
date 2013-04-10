package org.openhealthtools.mdht.mdmiplugins.parsers;

import java.util.*;

import org.openhealthtools.mdht.mdmi.MdmiException;
import org.openhealthtools.mdht.mdmi.util.*;

class HL7Message {
   private ArrayList<HL7Section> sections = new ArrayList<HL7Section>();
   private String dataBuffer;
   
   void load( byte[] data ) {
      sections.clear();
      dataBuffer = StringUtil.getString(data).trim();
      while( dataBuffer != null && 0 < dataBuffer.length() ) {
         HL7Section s = readNextSection();
         if( s != null )
            sections.add(s);
      }
   }

   ArrayList<HL7Section> getSections( String name ) {
      ArrayList<HL7Section> a = new ArrayList<HL7Section>();
      for( int i = 0; i < sections.size(); i++ ) {
         HL7Section s = sections.get(i);
         if( s.name.equals(name) )
            a.add(s);
      }
      return a;
   }
   
   private HL7Section readNextSection() {
      HL7Section s = new HL7Section();
      String line = readLine();
      if( line == null || line.length() <= 0 )
         return null;
      s.fields = parseLine(line);
      if( s.fields == null || s.fields.length <= 0 )
         throw new MdmiException("Cannot parse line " + line);
      s.name = s.fields[0].trim();
      return s;
   }
   
   // read next line from the buffer, and remove it
   private String readLine() {
      if( dataBuffer == null || dataBuffer.trim().length() <= 0 )
         return null;
      String ret = null;
      int n = dataBuffer.indexOf('\n');
      if( n <= 0 ) {
         ret = dataBuffer;
         dataBuffer = null;
      }
      else {
         ret = dataBuffer.substring(0, n);
         if( dataBuffer.length() <= n + 1 )
            dataBuffer = null;
         else
            dataBuffer = dataBuffer.substring(n + 1);
         if( 0 < ret.length() && ret.charAt(ret.length() - 1) == '\r' )
            ret = ret.substring(0, ret.length() - 1);
      }
      if( ret != null )
         ret = ret.trim();
      return ret;
   }

   private String[] parseLine( String line ) {
      if( line == null )
         throw new IllegalArgumentException("Null argument!");
      ArrayList<String> a = new ArrayList<String>();
      int i = line.indexOf(HL7Parser.FIELD_DELIMITER);
      while( 0 <= i  ) {
         if( i == 0 )
            a.add("");
         else
            a.add(line.substring(0, i));
         if( line.length() <= i + 1 )
            line = "";
         else
            line = line.substring(i + 1);
         i = line.indexOf(HL7Parser.FIELD_DELIMITER);
      }
      a.add(line);
      return a.toArray(new String[0]);
   }
} // HL7Message

class HL7Section {
   String   name;
   String[] fields;
} // HL7Section