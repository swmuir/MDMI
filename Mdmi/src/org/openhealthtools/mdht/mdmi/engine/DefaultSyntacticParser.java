/*******************************************************************************
 * Copyright (c) 2012 Firestar Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Firestar Software, Inc. - initial API and implementation
 *
 * Author:
 *     Gabriel Oancea
 *
 *******************************************************************************/
package org.openhealthtools.mdht.mdmi.engine;

import java.io.*;
import java.util.*;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.namespace.NamespaceContext;

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.util.*;

public class DefaultSyntacticParser implements ISyntacticParser {
   protected NamespaceContext context;

   @Override
   public ISyntaxNode parse( MessageModel mdl, MdmiMessage msg ) {
      if( mdl == null || msg == null )
         throw new IllegalArgumentException("Null argument!");
      byte[] data = msg.getData();
      if( data == null )
         return null; // <---- NOTE message can be empty

      YNode yroot = null;
      try {
         XmlParser p = new XmlParser();
         Document doc = p.parse(new ByteArrayInputStream(data));

         Element root = doc.getDocumentElement();
         context = XmlNamespaceContext.getDocumentNamespaces(doc, XslUtil.DEFAULT_NS);
         MessageSyntaxModel syn = mdl.getSyntaxModel();
         Node node = syn.getRoot();
         String nodeName = location(node); // for the root node it is its name
         String rootName = root.getNodeName();
         if( !nodeName.equals(rootName) )
            throw new MdmiException("Root node mismatch, found {0}, expected {1}", rootName, nodeName);
         if( node instanceof Bag ) {
            yroot = new YBag((Bag)node, null);
            parseBag((YBag)yroot, root);
         }
         else if( node instanceof Choice ) {
            yroot = new YChoice((Choice)node, null);
            parseChoice((YChoice)yroot, root);
         }
         else {
            yroot = new YLeaf((LeafSyntaxTranslator)node, null);
            parseLeaf((YLeaf)yroot, root);
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

   @Override
   public void serialize( MessageModel mdl, MdmiMessage msg, ISyntaxNode yroot ) {
      if( mdl == null || msg == null || yroot == null )
         throw new IllegalArgumentException("Null argument!");
      try {
         MessageSyntaxModel syn = mdl.getSyntaxModel();
         Node node = syn.getRoot();
         if( node != yroot.getNode() )
            throw new MdmiException("Invalid serialization attempt, expected node {0} forund node {1}", node.getName(),
                  yroot.getNode().getName());
         String nodeName = location(node); // for the root node it is its name
         byte[] data = msg.getData();
         XmlParser p = new XmlParser();
         Document doc = null;
         Element root = null;
         if( data == null ) {
            doc = p.newDocument();
            root = doc.createElement(nodeName);
            doc.appendChild(root);
         }
         else {
            doc = p.parse(new ByteArrayInputStream(data));
            root = doc.getDocumentElement();
            String rootName = root.getNodeName();
            if( !nodeName.equals(rootName) )
               throw new MdmiException("Root node mismatch, found {0}, expected {1}", rootName, nodeName);
         }

         if( node instanceof Bag ) {
            serializeBag((YBag)yroot, root);
         }
         else if( node instanceof Choice ) {
            serializeChoice((YChoice)yroot, root);
         }
         else {
            serializeLeaf((YLeaf)yroot, root);
         }

         ByteArrayOutputStream baos = new ByteArrayOutputStream(0xFFFF);
         XmlWriter w = new XmlWriter(baos);
         w.write(doc);
         w = null;
         msg.setData(baos.toByteArray());
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Syntax.serialize(): unexpected exception");
      }
   }

   /**
    * Parse the contents of the given Element (root) into the given YNode (yroot).
    * 
    * @param yroot The root YNode.
    * @param root The element it is mapped to.
    */
   private void parseBag( YBag yroot, Element root ) {
      Bag rootBag = yroot.getBag(); // descriptor of the contents
      Collection<Node> nodes = rootBag.getNodes();
      for( Iterator<Node> iterator = nodes.iterator(); iterator.hasNext(); ) {
         Node node = iterator.next();
         String xpath = location(node);
         if( node instanceof LeafSyntaxTranslator ) {
            if( xpath == null )
               throw new MdmiException("Invalid empty xpath for leaf node {0}", getNodePath(node));
            LeafSyntaxTranslator leaf = (LeafSyntaxTranslator)node;
            ArrayList<org.w3c.dom.Node> xmlNodes = XslUtil.getNodes(root, xpath, context);
            int actualCount = xmlNodes.size();
            if( actualCount < leaf.getMinOccurs() )
               throw new MdmiException("Invalid cardinality for leaf node {0}, expected {1}, found {2}",
                     getNodePath(node), leaf.getCardinality(), actualCount);
            for( int i = 0; i < actualCount; i++ ) {
               org.w3c.dom.Node xmlNode = xmlNodes.get(i);
               YLeaf y = new YLeaf(leaf, yroot);
               yroot.addYNode(y);
               if( xmlNode.getNodeType() == org.w3c.dom.Node.ATTRIBUTE_NODE )
                  y.setValue(xmlNode.getNodeValue());
               else
                  y.setValue(xmlNode.getTextContent());
            }
         }
         else if( node instanceof Bag ) {
            Bag bag = (Bag)node;
            if( xpath == null ) {
               // it is mapped to the content of the root
               YBag y = new YBag(bag, yroot);
               yroot.addYNode(y);
               parseBag(y, root);
            }
            else {
               ArrayList<org.w3c.dom.Node> xmlNodes = XslUtil.getNodes(root, xpath, context);
               int actualCount = xmlNodes.size();
               if( actualCount < bag.getMinOccurs() )
                  throw new MdmiException("Invalid cardinality for bag node {0}, expected {1}, found {2}",
                        getNodePath(node), bag.getCardinality(), actualCount);
               for( int i = 0; i < actualCount; i++ ) {
                  org.w3c.dom.Node xmlNode = xmlNodes.get(i);
                  if( xmlNode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE )
                     throw new MdmiException("Invalid XML node for bag node {0}, must be an XML element, found {1}",
                           getNodePath(node), xmlNode.getNodeType());
                  YBag y = new YBag(bag, yroot);
                  yroot.addYNode(y);
                  parseBag(y, (Element)xmlNode);
               }
            }
         }
         else {
            Choice choice = (Choice)node;
            if( xpath == null ) {
               // it is mapped to the content of the root
               YChoice y = new YChoice(choice, yroot);
               yroot.addYNode(y);
               parseChoice(y, root);
            }
            else {
               ArrayList<org.w3c.dom.Node> xmlNodes = XslUtil.getNodes(root, xpath, context);
               int actualCount = xmlNodes.size();
               if( actualCount < choice.getMinOccurs() )
                  throw new MdmiException("Invalid cardinality for choice node {0}, expected {1}, found{2}",
                        getNodePath(node), choice.getCardinality(), actualCount);
               for( int i = 0; i < actualCount; i++ ) {
                  org.w3c.dom.Node xmlNode = xmlNodes.get(i);
                  if( xmlNode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE )
                     throw new MdmiException("Invalid XML node for choice node {0}, must be an XML element, found {1}",
                           getNodePath(node), xmlNode.getNodeType());
                  YChoice y = new YChoice(choice, yroot);
                  yroot.addYNode(y);
                  parseChoice(y, (Element)xmlNode);
               }
            }
         }
      }
   }

   /**
    * Parse the contents of the given Element (root) into the given YNode (yroot).
    * 
    * @param yroot The root YNode.
    * @param root The element it is mapped to.
    */
   private void parseChoice( YChoice yroot, Element root ) {
      Choice rootChoice = yroot.getChoice();
      Collection<Node> nodes = rootChoice.getNodes();
      for( Iterator<Node> iterator = nodes.iterator(); iterator.hasNext(); ) {
         Node node = iterator.next();
         String xpath = location(node);
         if( node instanceof LeafSyntaxTranslator ) {
            if( xpath == null )
               throw new MdmiException("Invalid empty xpath for leaf node {0}", getNodePath(node));
            LeafSyntaxTranslator leaf = (LeafSyntaxTranslator)node;
            ArrayList<org.w3c.dom.Node> xmlNodes = XslUtil.getNodes(root, xpath, context);
            int actualCount = xmlNodes.size();
            if( actualCount < leaf.getMinOccurs() ) // not found, continue
               continue; // not found
            for( int i = 0; i < actualCount; i++ ) {
               org.w3c.dom.Node xmlNode = xmlNodes.get(i);
               YLeaf y = new YLeaf(leaf, yroot);
               yroot.addYNode(y);
               y.setValue(xmlNode.getNodeValue());
            }
            break; // found the choice item
         }
         else if( node instanceof Bag ) {
            Bag bag = (Bag)node;
            if( xpath == null ) {
               // it is mapped to the content of the root
               YBag y = new YBag(bag, yroot);
               yroot.addYNode(y);
               parseBag(y, root);
            }
            else {
               ArrayList<org.w3c.dom.Node> xmlNodes = XslUtil.getNodes(root, xpath, context);
               int actualCount = xmlNodes.size();
               if( actualCount < bag.getMinOccurs() )
                  continue; // not found
               for( int i = 0; i < actualCount; i++ ) {
                  org.w3c.dom.Node xmlNode = xmlNodes.get(i);
                  if( xmlNode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE )
                     throw new MdmiException("Invalid XML node for bag node {0}, must be an XML element, found {1}",
                           getNodePath(node), xmlNode.getNodeType());
                  YBag y = new YBag(bag, yroot);
                  yroot.addYNode(y);
                  parseBag(y, (Element)xmlNode);
               }
            }
            break; // found the choice item
         }
         else {
            Choice choice = (Choice)node;
            if( xpath == null ) {
               // it is mapped to the content of the root
               YChoice y = new YChoice(choice, yroot);
               yroot.addYNode(y);
               parseChoice(y, root);
            }
            else {
               ArrayList<org.w3c.dom.Node> xmlNodes = XslUtil.getNodes(root, xpath, context);
               int actualCount = xmlNodes.size();
               if( actualCount < choice.getMinOccurs() )
                  continue; // not found
               for( int i = 0; i < actualCount; i++ ) {
                  org.w3c.dom.Node xmlNode = xmlNodes.get(i);
                  if( xmlNode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE )
                     throw new MdmiException("Invalid XML node for choice node {0}, must be an XML element, found {1}",
                           getNodePath(node), xmlNode.getNodeType());
                  YChoice y = new YChoice(choice, yroot);
                  yroot.addYNode(y);
                  parseChoice(y, (Element)xmlNode);
               }
            }
            break; // found the choice item
         }
      }
   }

   /**
    * Set the leaf value from the given element's text.
    * 
    * @param yroot The leaf to set the value for.
    * @param root The root element.
    */
   private void parseLeaf( YLeaf yroot, Element root ) {
      String v = XmlUtil.getText(root);
      if( v == null )
         v = "";
      yroot.setValue(v);
   }

   /**
    * Serialize a YBag to the given root element.
    * 
    * @param yroot The bag node to serialize.
    * @param root The node to store it into.
    */
   private void serializeBag( YBag yroot, Element root ) {
      Bag rootBag = yroot.getBag();
      ArrayList<Node> nodes = rootBag.getNodes();
      for( int i = 0; i < nodes.size(); i++ ) {
         Node node = nodes.get(i);
         String xpath = location(node);
         ArrayList<YNode> ynodes = yroot.getYNodesForNode(node);
         for( int j = 0; j < ynodes.size(); j++ ) {
            if( node instanceof LeafSyntaxTranslator ) {
               YLeaf y = (YLeaf)ynodes.get(j);
               setValue(root, xpath, y, j);
            }
            else if( node instanceof Bag ) {
               YBag y = (YBag)ynodes.get(j);
               setBag(root, xpath, y, j);
            }
            else if( node instanceof Choice ) {
               YChoice y = (YChoice)ynodes.get(j);
               setChoice(root, xpath, y, j);
            }
         }
      }
   }

   /**
    * Serialize a YChoice to the given root element.
    * 
    * @param yroot The choice node to serialize.
    * @param root The node to store it into.
    */
   private void serializeChoice( YChoice yroot, Element root ) {
      Node node = yroot.getChosenNode();
      String xpath = location(node);
      ArrayList<YNode> ynodes = yroot.getYNodes();
      for( int j = 0; j < ynodes.size(); j++ ) {
         if( node instanceof LeafSyntaxTranslator ) {
            YLeaf y = (YLeaf)ynodes.get(j);
            setValue(root, xpath, y, j);
         }
         else if( node instanceof Bag ) {
            YBag y = (YBag)ynodes.get(j);
            setBag(root, xpath, y, j);
         }
         else if( node instanceof Choice ) {
            YChoice y = (YChoice)ynodes.get(j);
            setChoice(root, xpath, y, j);
         }
      }
   }

   /**
    * Serialize a YLeaf to the given root element. Only called for the root leaf (boundary case).
    * 
    * @param yroot The leaf node to serialize.
    * @param root The node to store it into.
    */
   private void serializeLeaf( YLeaf yroot, Element root ) {
      LeafSyntaxTranslator rootLeaf = yroot.getLeaf();
      String xpath = location(rootLeaf);
      setValue(root, xpath, yroot, 0);
   }

   /**
    * Set the values from the given YBag into the specified xpath relative to the given node.
    * 
    * @param root The root XML element relative to which we serialize.
    * @param xpath The XPath to store it at, relative root the root element.
    * @param ybag The YBag containing the values to store.
    * @param yindex The index of the ynode to store, used when having multiple instances of ybag.
    */
   private void setBag( Element root, String xpath, YBag ybag, int yindex ) {
      if( xpath == null || xpath.length() <= 0 ) {
         serializeBag(ybag, root); // mapped to content
         return;
      }
      org.w3c.dom.Node xmlNode = XslUtil.createNodeForPath(root, xpath, yindex);
      if( xmlNode == null || xmlNode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE )
         throw new MdmiException("Invalid xpath expression {0} for element {1}, ybag {2}", xpath, root.getNodeName(),
               ybag.toString());
      Element e = (Element)xmlNode;
      serializeBag(ybag, e);
   }

   /**
    * Set the values from the given YChoice into the specified xpath relative to the given node.
    * 
    * @param root The root XML element relative to which we serialize.
    * @param xpath The XPath to store it at, relative root the root element.
    * @param ychoice The YChoice containing the values to store.
    * @param yindex The index of the ynode to store
    */
   private void setChoice( Element root, String xpath, YChoice ychoice, int yindex ) {
      if( xpath == null || xpath.length() <= 0 ) {
         serializeChoice(ychoice, root); // mapped to content
         return;
      }
      org.w3c.dom.Node xmlNode = XslUtil.createNodeForPath(root, xpath, yindex);
      if( xmlNode == null || xmlNode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE )
         throw new MdmiException("Invalid xpath expression {0} for element {1}, ychoice {2}", xpath,
               root.getNodeName(), ychoice.toString());
      Element e = (Element)xmlNode;
      serializeChoice(ychoice, e);
   }

   /**
    * Set one value (from the given YLeaf) into the specified xpath relative to the given node. The xpath can be: text()
    * 
    * @attrName elementName elementName/text() elementName[2] elementName[2]/text() elementName@attrName elementName[3]@attrName
    *           element1/element2...
    * 
    * @param root The root XML element relative to which we serialize.
    * @param xpath The XPath to store it at, relative root the root element.
    * @param yleaf The YLeaf containing the value to store.
    * @param yindex The index of the ynode to store
    */
   private void setValue( Element root, String xpath, YLeaf yleaf, int yindex ) {
      String value = yleaf.getValue();
      org.w3c.dom.Node xmlNode = XslUtil.createNodeForPath(root, xpath, yindex);

      if( xmlNode.getNodeType() == org.w3c.dom.Node.ATTRIBUTE_NODE ) {
         Attr o = (Attr)xmlNode;
         o.setTextContent(value);
      }
      else if( xmlNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE ) {
         Element o = (Element)xmlNode;
         XmlUtil.setText(o, value);
      }
      else if( xmlNode.getNodeType() == org.w3c.dom.Node.TEXT_NODE ) {
         Text o = (Text)xmlNode;
         o.setTextContent(value);
      }
      else
         throw new MdmiException("Invalid XML node type for xpath expression {0} for element {1}, yleaf {2}: {3}",
               xpath, root.getNodeName(), yleaf.toString(), xmlNode.getNodeType());
   }

   /**
    * Get the node location (XPath) name based on the Node.location in the model. This field can be empty, and it is
    * normally an XPath expression, such as "elem@attr", etc.
    * 
    * @param node The node.
    * @return The node location as an XPath expression.
    */
   static String location( Node node ) {
      String location = node.getLocation();
      if( location == null || location.trim().length() <= 0 )
         return null;
      return location.trim();
   }

   /**
    * Get the model path to the given node.
    * 
    * @param node The node.
    * @return The path (XPath) to this node.
    */
   static String getNodePath( Node node ) {
      Node parent = node.getParentNode();
      String name = node.getLocation();
      if( parent == null )
         return name;
      String p = getNodePath(parent);
      if( name.startsWith("@") )
         return p + name;
      return p + '/' + name;
   }
} // DefaultMdmiSyntaxParser
