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

import org.openhealthtools.mdht.mdmi.*;
import java.util.*;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.*;
import org.w3c.dom.*;

/**
 * XSLT Utilities. By default is caching the last 0x4000 expressions parsed.
 * 
 * @author goancea
 */
public class XslUtil {
   public static final XPathFactory XPATH_FACTORY = new org.apache.xpath.jaxp.XPathFactoryImpl();

   private static XCache  s_cache        ;
   private static boolean s_isInitialized;
   
   /**
    * Initialize with a cache size. If negative or 0, caching is disabled.
    * 
    * @param cacheSize Cache size. If negative or 0, caching is disabled.
    */
   public static void initialize( int cacheSize ) {
      if( s_isInitialized )
         return;
      if( 0 < cacheSize )
         s_cache = new XCache(cacheSize);
   }

   static XCache cache() {
      if( !s_isInitialized )
         initialize(0x1000);
      return s_cache;
   }
   
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
   public static ArrayList<Node> getNodes( Element root, String xpath, NamespaceContext context) {
      if( root == null )
         throw new IllegalArgumentException("The root element may not be null!");
      if( xpath == null || xpath.length() <= 0 )
         throw new IllegalArgumentException("The xpath cannot be null or empty!");
      NodeList nodes = getNodeList(root, xpath, context);
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

   /**
    * Create or get and existing node or hierarchy of nodes based on the given XPath.
    * Supported expressions are most XPath constructs:
    * <pre>
    * elem
    * {@literal @}attr
    * e1/e2/../eN
    * e1/e2/../eN/text()
    * e1/e2/../eN{@literal @}attr
    * a[2]/b[3]
    * a/b[3]/c/d[2]{@literal @}attr
    * a/b/c[d[{@literal @}a='value']]
    * a[b/c/text()='value']
    * </pre>
    * More than one node of the specified type may be created (for the nodes which support this)
    * by using the ordinalIndex.
    * 
    * @param parent Node relative to which the XPath expression will be evaluated.
    * @param path The XPath expression to evaluate
    * @param ordinalIndex The ordinal index if adding more than one node.
    * @return The existing or created end node, as identified in the path.
    */
   public static Node createNodeForPath( Element parent, String path, int ordinalIndex ) {
      if( parent == null || path == null || path.length() <= 0 )
         throw new IllegalArgumentException("Null or empty arguments!");
      
      int isqb = indexOfNotInQuotes(path, '[');
      int islh = indexOfNotInQuotes(path, '/');
      int icat = indexOfNotInQuotes(path, '@');
      
      // start with border cases
      if( isqb == 0 )
         throw new IllegalArgumentException("Invalid xpath expression '" + path + "', cannot start with '['");
      if( islh == 0 )
         throw new IllegalArgumentException("Invalid xpath expression '" + path + "', cannot start with '/'");
      if( path.length() <= islh + 1 )
         throw new IllegalArgumentException("Invalid xpath expression '" + path + "', cannot end with '/'");

      if( isqb < 0 ) {
         // no index or predicates
         if( icat == 0 ) {
            // is of this form @attr
            if( 0 < islh )
               throw new IllegalArgumentException("Invalid xpath expresion '" + path + "', cannot contain '/' after '@'");
            String name = path.substring(1);
            return getOrCreateAttribute(parent, name);
         }
         if( icat < 0 ) {
            // no attribute, must be something like elem1/elem2/.../elemN
            // or elem1/elem2/.../elemN/text()
            int i = islh;
            String spath = path;
            Element p = parent;
            while( 0 < i ) {
               // get the first element, make sure it exists, if not make one
               String name = spath.substring(0, i);
               spath = spath.substring(i + 1);
               i = indexOfNotInQuotes(spath, '/');
               if( spath.equals("text()") )
                  p = addElement(p, name, ordinalIndex);
               else
                  p = getOrCreateElement(p, name);
            }
            if( spath.equals("text()") )
               return p;
            else
               return addElement(p, spath, ordinalIndex);
         }
         else {
            // 0 < icat, that is elem@attr, or elem1/elem2/.../elemN@attr 
            if( icat < path.lastIndexOf('/') )
               throw new IllegalArgumentException("Invalid xpath expresion '" + path + "', cannot contain '/' after '@'");
            int i = islh;
            String spath = path;
            Element p = parent;
            String name = null;
            while( 0 < i ) {
               // get the first element, make sure it exists, if not make one
               name = spath.substring(0, i);
               spath = spath.substring(i + 1);
               i = indexOfNotInQuotes(spath, '/');
               p = getOrCreateElement(p, name);
            }
            i = indexOfNotInQuotes(spath, '@');
            name = spath.substring(0, i);
            spath = spath.substring(i + 1);
            if( 0 < name.length() ) // cover the strange format elem/@attr
               p = getOrCreateElement(p, name);
            return getOrCreateAttribute(p, spath);
         }
      }
      else {
         // we have and index like elem[2], or a predicate like [elem[@attr='test']]
         if( icat == 0 )
            throw new IllegalArgumentException("Invalid xpath expresion '" + path + "', cannot contain '@' after '['");
         int x = isqb;
         int y = islh;
         String spath = path;
         Element p = parent;
         String name = null;
         while( 0 < x || 0 <= y ) {
            if( 0 < x && x < y || y < 0 ) {
               // e[...]
               String[] a = splitForBrackets(spath);
               name = a[0];
               String expr = a[1];
               spath = a[2];
               int index = xpathIndex(expr);
               if( 0 < index ) {
                  // numeric index, one based
                  // get all child elements of the specified name and make sure we have at least index of them
                  ArrayList<Element> elems = XmlUtil.getElements(p, name);
                  while( elems.size() < index ) {
                     XmlUtil.addElement(p, name);
                     elems = XmlUtil.getElements(p, name);
                  }
                  p = elems.get(index - 1);
               }
               else { 
                  // we have a predicate, first check if it exists
                  String pred = name + '[' + expr + ']';
                  boolean isFinal = spath.length() <= 0;
                  NodeList nl = XslUtil.getNodeList(p, pred);
                  Node on = null;
                  if( isFinal ) {
                     int oi = ordinalIndex < 0 ? 0 : ordinalIndex;
                     if( nl == null || nl.getLength() < oi + 1 )
                        on = addNodeBasedOnPredicate(p, name, expr, ordinalIndex);
                     else
                        on = nl.item(oi);
                  }
                  else {
                     if( nl == null || nl.getLength() <= 0 )
                        on = addNodeBasedOnPredicate(p, name, expr, 0);
                     else
                        on = nl.item(0);
                  }
                  if( on instanceof Element )
                     p = (Element)on;
                  else if( !isFinal )
                     throw new IllegalArgumentException("Invalid attribute node followed by XPath expresion: " + path);
               }
            }
            else {
               // e/e, 
               name = spath.substring(0, y);
               spath = spath.substring(y + 1);
               p = getOrCreateElement(p, name);
            }
            if( 0 < spath.length() && spath.startsWith("/") )
               spath = spath.substring(1);
            x = indexOfNotInQuotes(spath, '[');
            y = indexOfNotInQuotes(spath, '/');
         }
         if( 0 < spath.length() )
            return addElement(p, spath, ordinalIndex);
         return p;
      }
   }
   
   private static Node addNodeBasedOnPredicate( Element p, String name, String expr, int ordinalIndex ) {
      if( p == null || name == null || expr == null || name.length() <= 0 || expr.length() <= 0 )
         throw new IllegalArgumentException("Invalid null or empty argument!");
      // no match, so we always create new element(s)
      Element child = null;
      int i = 0;
      int n = ordinalIndex <= 0 ? 1 : ordinalIndex + 1;
      while( i++ < n ) {
         child = XmlUtil.addElement(p, name);
         processPredicate( child, expr );
      }
      return child;
   }
   
   private static void processPredicate( Element child, String expr ) {
      if( expr.startsWith("@") ) {
         // @attr='value'
         String[] a = parseAttrValue(expr);
         Node n = getOrCreateAttribute(child, a[0]);
         n.setTextContent(a[1]);
      }
      else if( 0 < indexOfNotInQuotes(expr, '[') ) {
         // nested index or predicate, we recursively call createNodeForChild
         createNodeForPath(child, expr, -1);
      }
      else {
         int ieq = indexOfNotInQuotes(expr, '=');
         if( 0 < ieq ) {
            // e1/e2/../text()='value'
            String[] a = parseElemValue(expr);
            Node n = createNodeForPath(child, a[0], -1);
            n.setTextContent(a[1]);
         }
         else
            throw new MdmiException("Can't handle predicate expression: " + expr);
      }
   }
   
   // e1/e2/../text()='value'
   private static String[] parseElemValue( String s ) {
      if( s == null || s.length() <= 0 )
         throw new IllegalArgumentException("Invalid argument expresion " + s);
      int i = s.indexOf('=');
      String[] a = new String[2];
      a[0] = s.substring(0, i);
      a[1] = s.substring(i + 2, s.length() - 1);
      return a;
   } 

   // @name='value'
   private static String[] parseAttrValue( String s ) {
      if( s == null || s.length() <= 0 || !s.startsWith("@") )
         throw new IllegalArgumentException("Invalid argument expresion " + s);
      s = s.substring(1);
      int i = s.indexOf('=');
      String[] a = new String[2];
      a[0] = s.substring(0, i);
      a[1] = s.substring(i + 2, s.length() - 1);
      return a;
   } 
   
   private static int xpathIndex( String expr ) {
      try {
         return Integer.valueOf(expr);
      }
      catch( Exception ignored ) {}
      return -1;
   }
   
   private static String[] splitForBrackets( String s ) {
      String[] a = new String[3];
      int i = indexOfNotInQuotes(s, '[');
      if( i <= 0 )
         throw new IllegalArgumentException("Invalid xpath expresion '" + s + "', does not contain '[', or has it at index 0");
      int j = i;
      int closeBindex = -1;
      int openBcount = 0;
      boolean insideQuotes = false;
      while( j < s.length() && closeBindex < 0 ) {
         char c = s.charAt(++j);
         if( c == '\'') {
            if( insideQuotes ) {
               if( s.charAt(j - 1) != '\\')
                  insideQuotes = false;
            }
            else
               insideQuotes = true;
         }
         if( c == '[' && !insideQuotes )
            openBcount++;
         else if( c == ']' && !insideQuotes ) {
            if( openBcount == 0 )
               closeBindex = j; // found the closing bracket
            else
               openBcount--;
         }
      }
      if( closeBindex < 0 )
         throw new IllegalArgumentException("Invalid xpath expresion '" + s + "', does not contain matching ']'");
      a[0] = s.substring(0, i);
      a[1] = s.substring(i + 1, closeBindex);
      if( s.length() <= closeBindex + 1 )
         a[2] = "";
      else
         a[2] = s.substring(closeBindex + 1);
      return a;
   }
   
   private static int indexOfNotInQuotes( String s, char x ) {
      int i = s.indexOf(x);
      if( i < 0 )
         return i;
      i = -1;
      int j = 0;
      boolean insideQuotes = false;
      while( j < s.length() && i < 0 ) {
         char c = s.charAt(j);
         if( c == '\'') {
            if( insideQuotes ) {
               if( 0 <= j - 1 && s.charAt(j - 1) != '\\')
                  insideQuotes = false;
            }
            else
               insideQuotes = true;
         }
         if( c == x && !insideQuotes )
            i = j;
         j++;
      }
      return i;
   }
   
   private static Attr getOrCreateAttribute( Element parent, String name ) {
      Attr a = parent.getAttributeNode(name);
      if( a == null ) {
         Document doc = parent.getOwnerDocument();
         a = doc.createAttribute(name);
         parent.setAttributeNode(a);
      }
      return a;
   }
   
   private static Element getOrCreateElement( Element parent, String name ) {
      Element child = XmlUtil.getElement(parent, name);
      if( child == null )
         child = XmlUtil.addElement(parent, name);
      return child;
   }
   
   private static Element addElement( Element parent, String name, int index ) {
      if( index < 0 )
         return getOrCreateElement(parent, name);
      ArrayList<Element> elems = XmlUtil.getElements(parent, name);
      while( elems.size() < index + 1 ) {
         XmlUtil.addElement(parent, name);
         elems = XmlUtil.getElements(parent, name);
      }
      return elems.get(index);
   }
   
   private static XPathExpression getRule( String rule ) {
      return getRule(rule, null);
   }

   private static XPathExpression getRule( String rule, NamespaceContext context ) {
      XPathExpression r = cache().getExpression(rule);
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
         cache().addExpression(rule, r);
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

      XCache( int cacheSize ) {
         if( 0 < cacheSize )
            maxSize = cacheSize;
      }
      
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
