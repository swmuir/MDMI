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

import javax.xml.*;
import javax.xml.namespace.*;

public final class XmlNamespaceContext implements NamespaceContext {
   private String                                 defaultNsPrefix;
   private final HashMap<String, String>          prefixes;
   private final HashMap<String, HashSet<String>> namespaces;
   
   public XmlNamespaceContext( String defaultNsPrefix ) {
      this.defaultNsPrefix = defaultNsPrefix;
      prefixes = new HashMap<String, String>();
      namespaces = new HashMap<String, HashSet<String>>();
      addImpl(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
      addImpl(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
   }

   public XmlNamespaceContext( Map<String, String> prefixNamespaceMap, String defaultNsPrefix ) {
      this(defaultNsPrefix);
      if( prefixNamespaceMap == null )
         throw new IllegalArgumentException("Null prefixNamespaceMap");
      for( Iterator<String> i = prefixNamespaceMap.keySet().iterator(); i.hasNext(); ) {
         String prefix = i.next();
         add(prefix, prefixNamespaceMap.get(prefix));
      }
   }

   public Map<String, String> getPrefixNamespaceUriMap() {
      return Collections.unmodifiableMap(prefixes);
   }

   public void add( String prefix, String namespaceUri ) {
      if( prefix == null )
         throw new IllegalArgumentException("Null prefix");
      if( namespaceUri == null || namespaceUri.length() <= 0 )
         throw new IllegalArgumentException("Null or empty namespaceUri");
      if( prefix.equals(XMLConstants.XML_NS_PREFIX) ) {
         String ns = prefixes.get(prefix);
         if( ns != null && !ns.equals(namespaceUri) )
            throw new IllegalArgumentException("Prefix " + prefix + " must be " + XMLConstants.XML_NS_URI);
         return;
      }
      if( prefix.equals(XMLConstants.XMLNS_ATTRIBUTE) ) {
         String ns = prefixes.get(prefix);
         if( ns != null && !ns.equals(namespaceUri) )
            throw new IllegalArgumentException("Prefix " + prefix + " must be " + XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
         return;
      }
      addImpl(prefix, namespaceUri);
   }

   private void addImpl( String prefix, String namespaceUri ) {
      if( prefix.length() <= 0 )
         prefix = defaultNsPrefix;
      prefixes.put(prefix, namespaceUri);
      HashSet<String> ps = namespaces.get(namespaceUri);
      if( ps == null ) {
         ps = new HashSet<String>();
         namespaces.put(namespaceUri, ps);
      }
      if( !ps.contains(prefix) )
         ps.add(prefix);
   }
   
   @Override
   public String getNamespaceURI( String prefix ) {
      if( prefix == null )
         throw new IllegalArgumentException("Null prefix");
      String namespaceUri = prefixes.get(prefix);
      return namespaceUri == null ? XMLConstants.NULL_NS_URI : namespaceUri;
   }

   @Override
   public String getPrefix( String namespaceUri ) {
      if( namespaceUri == null || namespaceUri.length() <= 0 )
         throw new IllegalArgumentException("Null or empty namespaceUri");
      HashSet<String> ps = namespaces.get(namespaceUri);
      return ps == null ? null : ps.iterator().next();
   }

   @Override
   public Iterator<String> getPrefixes( String namespaceUri ) {
      if( namespaceUri == null || namespaceUri.length() <= 0 )
         throw new IllegalArgumentException("Null or empty namespaceUri");
      HashSet<String> ps = namespaces.get(namespaceUri);
      return ps == null ? null : ps.iterator();
   }
   
   @Override
   public String toString() {
      StringBuffer sb = new StringBuffer();
      for( Iterator<String> i = prefixes.keySet().iterator(); i.hasNext(); ) {
         String p = i.next();
         String n = prefixes.get(p);
         sb.append(p).append(":").append(n).append("\n");
      }
      return sb.toString();
   }
} // XmlNamespaceContext
