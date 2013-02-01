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

   private static final XCache      CACHE         = new XCache();
   
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
      NodeList nodes = getNodeList(root, xpath);
      ArrayList<Node> a = new ArrayList<Node>();
      for( int i = 0; i < nodes.getLength(); i++ ) {
         a.add(nodes.item(i));
      }
      return a;
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
   public static String getString( Node node, String rule ) {
      return getString(node, getRule(rule));
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
    * @return The Node result of the evaluation.
    * @throws RuntimeException If the rule is invalid, or its evaluation fails for any reason.
    */
   public static Node getNode( Node node, String rule ) {
      return getNode(node, getRule(rule));
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
   public static NodeList getNodeList( Node node, String rule ) {
      return getNodeList(node, getRule(rule));
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

   private static XPathExpression getRule( String rule ) {
      return getRule(rule, null);
   }

   private static XPathExpression getRule( String rule, NamespaceContext context ) {
      XPathExpression r = CACHE.getExpression(rule);
      if( r == null ) {
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

   private static class XC {
      String          rule;
      XPathExpression expression;
      Date            lastUsed;
   }

   private static class XCache {
      private final ArrayList<XC>            m_cache         = new ArrayList<XC>();
      private final HashMap<String, Integer> m_ruleNameIndex = new HashMap<String, Integer>();
      private final TreeMap<Date, Integer>   m_lastUsedIndex = new TreeMap<Date, Integer>();
      private final Object                   lock            = new Object();
      private int                            maxSize         = 0x4000;

      XPathExpression getExpression( String rule ) {
         synchronized( lock ) {
            Integer i = m_ruleNameIndex.get(rule);
            if( i == null )
               return null;
            XC xc = m_cache.get(i.intValue());
            xc.lastUsed = new Date();
            return xc.expression;
         }
      }

      void addExpression( String rule, XPathExpression expression ) {
         synchronized( lock ) {
            Integer i = m_ruleNameIndex.get(rule);
            if( i != null ) {
               XC xc = m_cache.get(i.intValue());
               xc.expression = expression;
               xc.lastUsed = new Date();
               return;
            }
         }

         if( m_cache.size() < maxSize ) {
            // still below cache limit
            XC xc = new XC();
            xc.rule = rule;
            xc.expression = expression;
            xc.lastUsed = new Date();
            synchronized( lock ) {
               Integer i = m_cache.size();
               m_cache.add(xc);
               m_ruleNameIndex.put(xc.rule, i);
               m_lastUsedIndex.put(xc.lastUsed, i);
            }
         }
         else {
            // need to remove the oldest one from the cache
            synchronized( lock ) {
               NavigableMap.Entry<Date, Integer> last = m_lastUsedIndex.lastEntry();
               Integer pos = last.getValue();
               XC xcOld = m_cache.get(pos);
               m_ruleNameIndex.remove(xcOld.rule);
               m_cache.remove(pos);

               XC xc = new XC();
               xc.rule = rule;
               xc.expression = expression;
               xc.lastUsed = new Date();
               m_cache.set(pos, xc);
               m_ruleNameIndex.put(xc.rule, pos);
               m_lastUsedIndex.put(xc.lastUsed, pos);
            }
         }
      }
   }
} // XslUtil
