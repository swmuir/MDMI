package org.openhealthtools.mdht.mdmiplugins.parsers;

import java.util.*;

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.engine.*;
import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.util.*;

public class HL7Parser implements ISyntacticParser {
   private static final String FIELD_DELIMITER    = "|";
   private static final String SUBFIELD_DELIMITER = "^";
   private String              m_dataBuffer;

   public ISyntaxNode parse( MessageModel mdl, MdmiMessage msg ) {
      if( mdl == null || msg == null )
         throw new IllegalArgumentException("Null argument!");
      byte[] data = msg.getData();
      if( data == null )
         return null; // <---- NOTE message can be empty
      m_dataBuffer = StringUtil.getString(data);
      m_dataBuffer = m_dataBuffer.trim();
      YBag yroot = null;
      try {
         MessageSyntaxModel syn = mdl.getSyntaxModel();
         Node node = syn.getRoot();
         if( !(node instanceof Bag) )
            throw new MdmiException("Root node error expected a bag, found {0}", node.getClass().getName());
         yroot = new YBag((Bag)node, null);
         Bag root = (Bag)node;
         ArrayList<Node> nodes = root.getNodes();
         for( int i = 0; i < nodes.size(); i++ ) {
            Node childNode = nodes.get(i);
            if( !(childNode instanceof Bag) )
               throw new MdmiException("Node error expected a bag, found {0}", childNode.getClass().getName());
            Bag sectionBag = (Bag)childNode;
            int minOccurs = sectionBag.getMinOccurs();
            int maxOccurs = sectionBag.getMaxOccurs();
            if( maxOccurs < 0 )
               maxOccurs = Integer.MAX_VALUE;
            int count = 0;
            while( count < maxOccurs ) {
               YBag section = new YBag(sectionBag, yroot);
               if( !readSectionLine(sectionBag, section) )
                  break;
               yroot.addYNode(section);
               count++;
            }
            if( count < minOccurs ) {
               throw new MdmiException("Section {0}, expected at least {1} instances, found only {2}", sectionBag
                     .getName().trim(), minOccurs, count);
            }
         }
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Syntax.parse(): unexpected exception");
      }
      return yroot;
   }

   public void serialize( MessageModel mdl, MdmiMessage msg, ISyntaxNode root ) {
      if( mdl == null || msg == null || root == null )
         throw new IllegalArgumentException("Null argument!");
      try {
         MessageSyntaxModel syn = mdl.getSyntaxModel();
         Node node = syn.getRoot();
         if( !(node instanceof Bag) )
            throw new MdmiException("Root node error expected a bag, found {0}", node.getClass().getName());
         YBag rootBag = (YBag)root;
         m_dataBuffer = "";
         ArrayList<YNode> ynodes = rootBag.getYNodes();
         for( int i = 0; i < ynodes.size(); i++ ) {
            YNode child = ynodes.get(i);
            if( !(child instanceof YBag) )
               throw new MdmiException("Node error expected a bag, found {0}", child.getClass().getName());
            YBag section = (YBag)child;
            writeSection(section);
         }
         msg.setData(StringUtil.getBytes(m_dataBuffer));
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Syntax.serialize(): unexpected exception");
      }
   }

   private boolean readSectionLine( Bag sectionBag, YBag section ) {
      String sectionName = sectionBag.getName().trim();
      String line = readLine();
      if( line == null || line.length() <= 0 )
         return false;
      String[] fields = parseLine(line);
      if( fields == null || fields.length <= 0 )
         throw new MdmiException("Section {0}, cannot parse line {1}", sectionName, line);
      String name = fields[0].trim();
      if( !sectionName.equalsIgnoreCase(name) ) {
         undoReadLine(line);
         return false;
      }
      readOneSection(sectionBag, section, fields);
      return true;
   }

   private void readOneSection( Bag sectionBag, YBag section, String[] fields ) {
      ArrayList<Node> nodes = sectionBag.getNodes();
      for( int i = 0; i < nodes.size(); i++ ) {
         Node childNode = nodes.get(i);
         String location = childNode.getLocation();
         int index = -1;
         try {
            index = Integer.parseInt(location);
         }
         catch( Exception ex ) {
            throw new MdmiException("Section {0}, field {1} cannot parse location {2}", sectionBag.getName(), i,
                  location);
         }
         String value = fields[index];

         if( childNode instanceof LeafSyntaxTranslator ) {
            LeafSyntaxTranslator fieldLeaf = (LeafSyntaxTranslator)childNode;
            YLeaf field = new YLeaf(fieldLeaf, section);
            section.addYNode(field);
            field.setValue(value);
         }
         else if( childNode instanceof Bag ) {
            String[] subfields = parseField(value);
            Bag fieldBag = (Bag)childNode;
            YBag field = new YBag(fieldBag, section);
            section.addYNode(field);
            ArrayList<Node> fieldNodes = fieldBag.getNodes();
            for( int j = 0; j < fieldNodes.size(); j++ ) {
               Node subfieldNode = fieldNodes.get(j);
               location = subfieldNode.getLocation();
               index = -1;
               try {
                  index = Integer.parseInt(location);
               }
               catch( Exception ex ) {
                  throw new MdmiException("Section {0}, field {1} subfield {2} cannot parse location {3}",
                        sectionBag.getName(), i, j, location);
               }
               index--; // index is one-based
               if( !(subfieldNode instanceof LeafSyntaxTranslator) )
                  throw new MdmiException("Section {0} field {1} subfield {2}: expected a leaf, found {3}",
                        sectionBag.getName(), i, j, subfieldNode.getClass().getName());
               LeafSyntaxTranslator subfieldLeaf = (LeafSyntaxTranslator)subfieldNode;
               if( subfields.length <= index ) {
                  if( subfieldLeaf.isRequired() )
                     throw new MdmiException("Section {0} field {1} subfield {2} is required and was not supplied",
                           sectionBag.getName(), i, j);
               }
               else {
                  YLeaf subfield = new YLeaf(subfieldLeaf, field);
                  String subfieldValue = subfields[index];
                  field.addYNode(subfield);
                  subfield.setValue(subfieldValue);
               }
            }
         }
         else
            throw new MdmiException("Section {0} field {1}: expected a leaf or a bag, found {2}", sectionBag.getName(),
                  i, childNode.getClass().getName());
      }
   }

   private void writeSection( YBag section ) {
      StringBuffer sb = new StringBuffer(256);
      Bag sectionBag = section.getBag();
      sb.append(sectionBag.getName()).append(FIELD_DELIMITER);
      ArrayList<YNode> ynodes = section.getYNodes();
      for( int i = 0; i < ynodes.size(); i++ ) {
         YNode child = ynodes.get(i);
         if( child instanceof YLeaf ) {
            YLeaf leaf = (YLeaf)child;
            sb.append(leaf.getValue());
         }
         else if( child instanceof YBag ) {
            YBag bag = (YBag)child;
            ArrayList<YNode> subfieldNodes = bag.getYNodes();
            for( int j = 0; j < subfieldNodes.size(); j++ ) {
               if( 0 < j )
                  sb.append(SUBFIELD_DELIMITER);
               YNode subfield = subfieldNodes.get(j);
               if( !(subfield instanceof YLeaf) )
                  throw new MdmiException("Section {0} field {1} subfield {2}: expected a leaf, found {3}",
                        sectionBag.getName(), i, j, subfield.getClass().getName());
               YLeaf leaf = (YLeaf)subfield;
               sb.append(leaf.getValue());
            }
         }
         else
            throw new MdmiException("Section {0} field {1} write error - expecting a leaf or a bag, found {2}",
                  sectionBag.getName(), i, child.getClass().getName());
         sb.append(FIELD_DELIMITER);
      }
      sb.append('\n');
      m_dataBuffer += sb.toString();
   }

   private String[] parseLine( String line ) {
      if( line == null )
         throw new IllegalArgumentException("Null argument!");
      return line.split("\\" + FIELD_DELIMITER);
   }

   private String[] parseField( String value ) {
      if( value == null )
         throw new IllegalArgumentException("Null argument!");
      return value.split("\\" + SUBFIELD_DELIMITER);
   }

   // read next line from the buffer, and remove it
   private String readLine() {
      if( m_dataBuffer == null || m_dataBuffer.trim().length() <= 0 )
         return null;
      String ret = null;
      int n = m_dataBuffer.indexOf('\n');
      if( n <= 0 ) {
         ret = m_dataBuffer;
         m_dataBuffer = null;
      }
      else {
         ret = m_dataBuffer.substring(0, n);
         if( m_dataBuffer.length() <= n + 1 )
            m_dataBuffer = null;
         else
            m_dataBuffer = m_dataBuffer.substring(n + 1);
         if( 0 < ret.length() && ret.charAt(ret.length() - 1) == '\r' )
            ret = ret.substring(0, ret.length() - 1);
      }
      if( ret != null )
         ret = ret.trim();
      return ret;
   }

   // undo read next line from the buffer, add the line back to the start of the buffer
   private void undoReadLine( String line ) {
      if( line == null || line.length() <= 0 )
         throw new IllegalArgumentException("Null argument!");
      if( m_dataBuffer == null || m_dataBuffer.trim().length() <= 0 ) {
         m_dataBuffer = line;
         return;
      }
      m_dataBuffer = line + '\n' + m_dataBuffer;
   }

   static String location( Node node ) {
      String location = node.getLocation();
      if( location == null || location.trim().length() <= 0 )
         return null;
      return location.trim();
   }
} // HL7Parser
