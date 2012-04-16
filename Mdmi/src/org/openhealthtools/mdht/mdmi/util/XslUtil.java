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
package org.openhealthtools.mdht.mdmi.util;

import java.util.*;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.*;
import org.w3c.dom.*;
import org.openhealthtools.mdht.mdmi.*;

/**
 * XSLT Utilities.
 * 
 * @author goancea
 */
public class XslUtil {
   public static final XPathFactory XPATH_FACTORY = new org.apache.xpath.jaxp.XPathFactoryImpl();
   
   /**
    * Get the namespaces from a DOM document.
    * 
    * @param doc The document.
    * @param defaultNsPrefix The string to use as default prefix.
    * @return The map of namspaces used.
    */
   public static XmlNamespaceContext getDocumentNamespaces( Document doc, String defaultNsPrefix ) {
      if( doc == null )
         throw new IllegalArgumentException("Null document!");
      Element root = doc.getDocumentElement();
      XmlNamespaceContext ctx = new XmlNamespaceContext(defaultNsPrefix);
      scan(root, ctx);
      return ctx;
   }

   private static void scan( Element root, XmlNamespaceContext ctx ) {
      NamedNodeMap attrs = root.getAttributes();
      if( attrs != null ) {
         for( int i = 0; i < attrs.getLength(); i++ ) {
            Attr attr = (Attr)attrs.item(i);
            String an = attr.getNodeName();
            if( an.startsWith("xmlns") ) {
               if( an.equals("xmlns") ) {
                  ctx.add("", attr.getNodeValue());
               }
               else if( an.startsWith("xmlns:") ) {
                  ctx.add(an.substring(6), attr.getNodeValue());
               }
            }
         }
      }
      ArrayList<Element> children = XmlUtil.getElements(root);
      for( int i = 0; i < children.size(); i++ ) {
         scan(children.get(i), ctx);
      }
   }
   
   /**
    * Get the DOM nodes that match the XPath passed in, relative to the element given.
    * XPath must be relative.
    * 
    * @param root The element to which the XPath is relative to.
    * @param xpath The actual XPath expression.
    * @return The list of nodes that match (if any). Will never be null.
    */
   public static ArrayList<Node> getNodes( Element root, String xpath ) {
      if( root == null )
         throw new IllegalArgumentException("The root element may not be null!");
      if( xpath == null || xpath.length() <= 0 )
         throw new IllegalArgumentException("The xpath cannot be null or empty!");
      if( xpath.startsWith("/") )
         throw new IllegalArgumentException("The xpath cannot start with '/' - absolute xpaths not supported!");
      return getNodes(root, parseXPath(xpath));
   }
   
   private static ArrayList<Node> getNodes( Element root, ArrayList<XNode> xpath ) {
      if( xpath.size() == 1 ) // last one
         return getNodes(root, xpath.get(0));
      XNode x = xpath.get(0);
      xpath.remove(0);
      if( x.isAttr )
         throw new IllegalArgumentException("Invalid xpath, attributes must be the last in the path");
      if( x.index <= 0 ) {
         Element e = XmlUtil.getElement(root, x.nodeName);
         if( e != null )
            return getNodes(e, xpath);
      }
      else { // indexed
         int index = x.index - 1;
         ArrayList<Element> es = XmlUtil.getElements(root, x.nodeName);
         if( es != null && index < es.size() )
            return getNodes(es.get(index), xpath);
      }
      return new ArrayList<Node>();
   }
   
   private static ArrayList<Node> getNodes( Element root, XNode x ) {
      ArrayList<Node> a = new ArrayList<Node>();
      if( x.isAttr ) {
         Attr attr = root.getAttributeNode(x.nodeName);
         if( attr != null )
            a.add(attr);
      }
      else if( x.isText() ) {
         NodeList lst = root.getChildNodes();
         int size = lst.getLength();
         for( int i = 0; i < size; i++ ) {
            Node n = lst.item(i);
            if( n.getNodeType() == Node.TEXT_NODE ) {
               a.add((Text)n);
            }
         }
      }
      else if( x.index <= 0 ) {
         ArrayList<Element> es = XmlUtil.getElements(root, x.nodeName);
         if( es != null )
            a.addAll(es);
      }
      else { // indexed
         int index = x.index - 1;
         ArrayList<Element> es = XmlUtil.getElements(root, x.nodeName);
         if( es != null && index < es.size() )
            a.add(es.get(index));
      }
      return a;
   }
   
   private static ArrayList<XNode> parseXPath( String xpath ) {
      if( xpath == null || xpath.length() <= 0 )
         throw new IllegalArgumentException("Null or empty xpath!");
      ArrayList<XNode> a = new ArrayList<XNode>();
      String[] nodes = xpath.split("/");
      for( int i = 0; i < nodes.length; i++ ) {
         String n = nodes[i];
         if( i + 1 == nodes.length && n.contains("@") ) {
            if( n.startsWith("@") ) {
               XNode xn = new XNode(n);
               a.add(xn);
            }
            else {
               String[] ns = n.split("@");
               if( ns.length != 2 )
                  throw new IllegalArgumentException("Invalid xpath '" + xpath + "', cannot contain more than one @");
               XNode xn = new XNode(ns[0]);
               a.add(xn);
               XNode an = new XNode("@" + ns[1]);
               a.add(an);
            }
         }
         else {
            XNode xn = new XNode(n);
            a.add(xn);
         }
      }
      return a;
   }

   /**
    * Evaluate the XPath rule and return the evaluation result as a string. Use to get the values of simple type
    * elements or attributes, like <code>node1/node2@attr</code>, or <code>node1/node2/text()</code>
    * 
    * @param node The node relative to which the evaluation of the rule takes place.
    * @param rule The rule to evaluate.
    * @param context The namespace context, may be null.
    * @return The string result of the evaluation.
    * @throws RuntimeException If the rule is invalid, or its evaluation fails for any reason.
    */
   public static String getString( Node node, String rule, NamespaceContext context ) {
      return getString(node, getRule(rule, context));
   }

   /**
    * Evaluate the XPath rule and return the evaluation result as a string. Use to get the values of simple type
    * elements or attributes, like <code>node1/node2@attr</code>, or <code>node1/node2/text()</code>
    * 
    * @param node The node relative to which the evaluation of the rule takes place.
    * @param rule The rule to evaluate.
    * @return The string result of the evaluation.
    * @throws RuntimeException If the rule is invalid, or its evaluation fails for any reason.
    */
   public static String getString( Node node, XPathExpression rule ) {
      // try a node first
      try {
         Node n = getNode(node, rule);
         if( n != null )
            return getText(n);
      }
      catch( Exception ignored ) {
      }

      String value = null;
      try {
         value = rule.evaluate(node);
      }
      catch( Exception ex ) {
         String err = "XslUtil.getString(): Error evaluating xpath expression '" + rule + "'";
         throw new MdmiException(ex, err);
      }
      return value;
   }

   /**
    * Evaluate the XPath rule and return the evaluation result as an XML Node. Use to get the values of any single node,
    * like <code>node1/node2</code>, or <code>node1/node2@attr</code>
    * 
    * @param node The node relative to which the evaluation of the rule takes place.
    * @param rule The rule to evaluate.
    * @param context The namespace context, may be null.
    * @return The Node result of the evaluation.
    * @throws RuntimeException If the rule is invalid, or its evaluation fails for any reason.
    */
   public static Node getNode( Node node, String rule, NamespaceContext context ) {
      return getNode(node, getRule(rule, context));
   }

   /**
    * Evaluate the XPath rule and return the evaluation result as an XML Node. Use to get the values of any single node,
    * like <code>node1/node2</code>, or <code>node1/node2@attr</code>
    * 
    * @param node The node relative to which the evaluation of the rule takes place.
    * @param rule The rule to evaluate.
    * @return The Node result of the evaluation.
    * @throws RuntimeException If the rule is invalid, or its evaluation fails for any reason.
    */
   public static Node getNode( Node node, XPathExpression rule ) {
      Node value = null;
      try {
         value = (Node)rule.evaluate(node, XPathConstants.NODE);
      }
      catch( Exception ex ) {
         String err = "XslUtil.getNode(): Error evaluating xpath expression '" + rule + "'";
         throw new MdmiException(ex, err);
      }
      return value;
   }

   /**
    * Evaluate the XPath rule and return the evaluation result as an XML NodeList. Use to get the values for multiple
    * nodes, like <code>node1/node2</code>.
    * 
    * @param node The node relative to which the evaluation of the rule takes place.
    * @param rule The rule to evaluate.
    * @param context The namespace context, may be null.
    * @return The NodeList result of the evaluation.
    * @throws RuntimeException If the rule is invalid, or its evaluation fails for any reason.
    */
   public static NodeList getNodeList( Node node, String rule, NamespaceContext context ) {
      return getNodeList(node, getRule(rule, context));
   }

   /**
    * Evaluate the XPath rule and return the evaluation result as an XML NodeList. Use to get the values for multiple
    * nodes, like <code>node1/node2</code>.
    * 
    * @param node The node relative to which the evaluation of the rule takes place.
    * @param rule The rule to evaluate.
    * @return The NodeList result of the evaluation.
    * @throws RuntimeException If the rule is invalid, or its evaluation fails for any reason.
    */
   public static NodeList getNodeList( Node node, XPathExpression rule ) {
      NodeList value = null;
      try {
         value = (NodeList)rule.evaluate(node, XPathConstants.NODESET);
      }
      catch( Exception ex ) {
         String err = "XslUtil.getNode(): Error evaluating xpath expression '" + rule + "'";
         throw new MdmiException(ex, err);
      }
      return value;
   }

   private static XPathExpression getRule( String rule, NamespaceContext context ) {
      XPathExpression r = null;
      XPath xpath = XPATH_FACTORY.newXPath();
      if( context != null )
         xpath.setNamespaceContext(context);
      try {
         r = xpath.compile(rule);
      }
      catch( Exception ex ) {
         String err = "XslUtil.getRule(): Error compiling xpath expression '" + rule + "'";
         throw new MdmiException(ex, err);
      }
      return r;
   }

   private static String getText( Node node ) {
      if( node == null )
         return null;
      NodeList lst = node.getChildNodes();
      int size = lst.getLength();
      StringBuffer sb = new StringBuffer();
      for( int i = 0; i < size; i++ ) {
         Node n = lst.item(i);
         if( n.getNodeType() == Node.TEXT_NODE ) {
            Text t = (Text)n;
            sb.append(t.getData());
         }
      }
      return sb.toString();
   }
   
   private static class XNode {
      String nodeName;
      int index = 0; // indexes are 1 based
      boolean isAttr;
      
      XNode( String s ) {
         if( s == null || s.length() <= 0 )
            throw new IllegalArgumentException("Null or empty node name!");
         if( s.startsWith("@") ) {
            nodeName = s.substring(1);
            isAttr = true;
         }
         else if( s.contains("[") ) {
            int i = s.indexOf('[');
            try {
               int j = s.indexOf(']');
               index = Integer.valueOf(s.substring(i + 1, j));
            }
            catch( Exception ex ) {
               throw new RuntimeException("Invalid indexed node " + s, ex);
            }
            if( index <= 0 )
               throw new RuntimeException("Invalid index for node " + s + ", index must be greater than 0!");
            nodeName = s.substring(0, i);
         }
         else
            nodeName = s;
      }
      
      boolean isText() {
         return "text()".equals(nodeName);
      }
      
      @Override
      public String toString() {
         return index <= 0 ? (isAttr ? "@" + nodeName : nodeName) : nodeName + "[" + index + "]";
      }
   }
} // XslUtil
