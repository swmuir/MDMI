package org.openhealthtools.mdht.mdmiplugins.parsers;

import java.util.*;

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.engine.*;
import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.util.*;

public class HL7Parser implements ISyntacticParser {
   static final String FIELD_DELIMITER    = "|";
   static final String SUBFIELD_DELIMITER = "^";

   private HL7Message  m_message;
   private String      m_dataBuffer;

   public ISyntaxNode parse( MessageModel mdl, MdmiMessage msg ) {
      if( mdl == null || msg == null )
         throw new IllegalArgumentException("Null argument!");
      byte[] data = msg.getData();
      if( data == null )
         return null; // <---- NOTE message can be empty
      m_message = new HL7Message();
      m_message.load(data);
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
            ArrayList<HL7Section> sections = m_message.getSections(sectionBag.getName().trim());
            if( sections.size() < minOccurs )
               throw new MdmiException("Section {0}, expected at least {1} instances, found only {2}", sectionBag
                     .getName().trim(), minOccurs, sections.size());
            if( maxOccurs < sections.size() )
               throw new MdmiException("Section {0}, expected at most {1} instances, found {2}", sectionBag.getName()
                     .trim(), maxOccurs, sections.size());
            for( int j = 0; j < sections.size(); j++ ) {
               HL7Section section = sections.get(j);
               YBag ybag = new YBag(sectionBag, yroot);
               yroot.addYNode(ybag);
               readOneSection(sectionBag, ybag, section);
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
         Map<String, List<YNode>> groupBySegmentMap = new LinkedHashMap<String, List<YNode>>();
         for( YNode ynode : rootBag.getYNodes() ) {
            String segmentName = ynode.getNode().getName();
            List<YNode> yNodes = groupBySegmentMap.get(segmentName);
            if( yNodes == null ) {
               yNodes = new ArrayList<YNode>();
               groupBySegmentMap.put(segmentName, yNodes);
            }
            yNodes.add(ynode);
         }
         for( List<YNode> yNodes : groupBySegmentMap.values() ) {
            for( int i = 0; i < yNodes.size(); i++ ) {
               YNode child = yNodes.get(i);
               if( !(child instanceof YBag) )
                  throw new MdmiException("Node error expected a bag, found {0}", child.getClass().getName());
               YBag section = (YBag)child;
               writeSection(section);
            }
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

   private void readOneSection( Bag sectionBag, YBag ybag, HL7Section section ) {
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
         String value = null;
         try {
            value = section.fields[index];
         }
         catch( Exception ex ) {
            throw new MdmiException("Section {0}, field {1} cannot read value at index {2}", sectionBag.getName(), i,
                  index);
         }

         if( childNode instanceof LeafSyntaxTranslator ) {
            LeafSyntaxTranslator fieldLeaf = (LeafSyntaxTranslator)childNode;
            if( fieldLeaf.isRequired() && (value == null || value.length() <= 0) )
               throw new MdmiException("Section {0}, field {1} is required and no value was given",
                     sectionBag.getName(), i);
            YLeaf field = new YLeaf(fieldLeaf, ybag);
            ybag.addYNode(field);
            field.setValue(value);
         }
         else if( childNode instanceof Bag ) {
            String[] subfields = parseField(value);
            Bag fieldBag = (Bag)childNode;
            YBag field = new YBag(fieldBag, ybag);
            ybag.addYNode(field);
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
                  if( subfieldLeaf.isRequired() && (subfieldValue == null || subfieldValue.length() <= 0) )
                     throw new MdmiException("Section {0}, field {1}, subfield {2} is required and no value was given",
                           sectionBag.getName(), i, j);
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
      sortFiledByLocation(ynodes);
      int location = 0;
      for( int i = 0; i < ynodes.size(); i++ ) {
         YNode child = ynodes.get(i);
         Integer nodeLocation = Integer.valueOf(child.getNode().getLocation());
         for( int j = 1; j < nodeLocation - location; j++ ) {
            sb.append(FIELD_DELIMITER);
         }
         location = nodeLocation;
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

   private void sortFiledByLocation( ArrayList<YNode> subfieldNodes ) {
      Collections.sort(subfieldNodes, new Comparator<YNode>() {
         public int compare( YNode node1, YNode node2 ) {
            Integer location1 = null;
            Integer location2 = null;
            if( node1.getNode() != null && node1.getNode().getLocation() != null ) {
               location1 = Integer.valueOf(node1.getNode().getLocation());
            }
            if( node2.getNode() != null && node2.getNode().getLocation() != null ) {
               location2 = Integer.valueOf(node2.getNode().getLocation());
            }
            if( location1 == null && location2 == null )
               return 0;
            if( location1 == null )
               return 1;
            if( location2 == null )
               return -1;
            if( location1 == location2 )
               return 0;
            return location1 < location2 ? -1 : 1;
         }
      });
   }

   private String[] parseField( String value ) {
      if( value == null )
         throw new IllegalArgumentException("Null argument!");
      ArrayList<String> a = new ArrayList<String>();
      int i = value.indexOf(SUBFIELD_DELIMITER);
      while( 0 <= i ) {
         if( i == 0 )
            a.add("");
         else
            a.add(value.substring(0, i));
         if( value.length() <= i + 1 )
            value = "";
         else
            value = value.substring(i + 1);
         i = value.indexOf(SUBFIELD_DELIMITER);
      }
      a.add(value);
      return a.toArray(new String[0]);
   }

   static String location( Node node ) {
      String location = node.getLocation();
      if( location == null || location.trim().length() <= 0 )
         return null;
      return location.trim();
   }
} // HL7Parser
