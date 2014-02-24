package org.openhealthtools.mdht.mdmiplugins.resolvers;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;
import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.util.*;

public class CcdaPostProcessor implements IPostProcessor {
   private static final String MESSAGE_GROUP = "CCDMessageGroup"; 
   private static final String MESSAGE_MODEL = "CCD"; 
   
   @Override
   public String getName() {
      return "CcdaPostProcessor";
   }

   @Override
   public ArrayList<String> getHandledQualifiedMessageNames() {
      ArrayList<String> a = new ArrayList<String>();
      a.add(MESSAGE_MODEL);
      return a;
   }

   @Override
   public boolean canProcess( String messageGroupName, String messageModelName ) {
      if( messageGroupName.equalsIgnoreCase(MESSAGE_GROUP) && messageModelName.equalsIgnoreCase(MESSAGE_MODEL) )
         return true;
      return false;
   }

   @Override
   public void processMessage( MdmiMessage message, MdmiModelRef model ) {
      // if not the relevant mesage type return with no changes - this should never happen
      if( !model.getGroupName().equalsIgnoreCase(MESSAGE_GROUP) && !model.getModelName().equalsIgnoreCase(MESSAGE_MODEL) )
         return;
      
      // Parse the target message
      XmlParser p = new XmlParser();
      Document doc = p.parse(new ByteArrayInputStream(message.getData()));
      
      // 1. Add a <component> for each <section>
      Element root = doc.getDocumentElement();
      Element component = XmlUtil.getElement(root, "component");
      Element structuredBody = XmlUtil.getElement(component, "structuredBody");
      ArrayList<Element> components = XmlUtil.getElements(structuredBody, "component");
      for( int i = 0; i < components.size(); i++ ) {
         Element c = components.get(i);
         processComponent(structuredBody, c, doc);
      }
      
      // Serialize it to a string and put it back
      StringWriter sw = new StringWriter();
      XmlWriter w  = new XmlWriter(sw);
      w.write(doc);
      message.setData(sw.toString());
   }

   private void processComponent( Element parent, Element component, Document doc ) {
      ArrayList<Element> sections = XmlUtil.getElements(component, "section");
      for( int i = 0; i < sections.size(); i++ ) {
         Element section = sections.get(i);
         processElement(section, doc);
         if( 0 < i ) {
            Element nc = doc.createElement("component");
            parent.appendChild(nc);
            nc.appendChild(section);
         }
      }
   }

   private void processElement( Element parent, Document doc ) {
      ArrayList<Element> children = XmlUtil.getElements(parent);
      for( int i = 0; i < children.size(); i++ ) {
         Element child = children.get(i);
         processElement(child, doc);
         String cn = XmlUtil.localName(child);
         if( cn.equals("entryRelationship") ) { 
            // case 1a from Ken's doc
            ArrayList<Element> observations = XmlUtil.getElements(child, "observation");
            for( int j = 1; j < observations.size(); j++ ) {
               Element observation = observations.get(j);
               Element nc = doc.createElement("entryRelationship");
               parent.appendChild(nc);
               nc.appendChild(observation);
            }
         }
         else if(cn.equals("act") || cn.equals("encounter") || cn.equals("procedure") || cn.equals("organizer") 
               || cn.equals("substanceAdministration") ) {
            // case || from Ken's doc
            Element entry = null;
            Element entryParent = null;
            if( XmlUtil.localName(parent).equals("entry") ) {
               entry = parent;
               if( !entry.hasAttribute("typeCode") )
                  entry.setAttribute("typeCode", "DRIV");
               entryParent = (Element)entry.getParentNode();
            }
            else {
               entryParent = parent;
               entry = doc.createElement("entry");
               entry.setAttribute("typeCode", "DRIV");
               entryParent.appendChild(entry);
               entry.appendChild(child);
            }

            ArrayList<Element> elems = XmlUtil.getElements(entry, cn);
            for( int j = 1; j < elems.size(); j++ ) {
               Element elem = elems.get(j);
               Element nc = doc.createElement("entry");
               nc.setAttribute("typeCode", "DRIV");
               entryParent.appendChild(nc);
               nc.appendChild(elem);
            }
         }
      }
   }
   
} // CcdaPostProcessor
