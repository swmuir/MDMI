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
package org.openhealthtools.mdht.mdmi.model.xmi.direct.writer;

import java.lang.reflect.*;
import java.util.*;

import javax.xml.stream.*;

public class XMLStreamIndentProxy implements InvocationHandler {
   private XMLStreamWriter           m_decorated;
   private static final String       s_defaultIndent       = "  ";
   private static final String       s_defaultLineFeed     = System.getProperty("line.separator");
   private static final String       s_startElemMethodName = "writeStartElement";
   private static final String       s_endElemMethodName   = "writeEndElement";
   private static final String       s_emptyElemMethodName = "writeEmptyElement";

   private int                       depth                 = 0;
   private String                    m_indent;
   private String                    m_lineFeed;
   private HashMap<Integer, Boolean> m_hasChildElement     = new HashMap<Integer, Boolean>();

   public XMLStreamIndentProxy( XMLStreamWriter toDecorate ) {
      this(toDecorate, s_defaultIndent);
   }

   public XMLStreamIndentProxy( XMLStreamWriter toDecorate, String indent ) {
      this(toDecorate, indent, s_defaultLineFeed);
   }

   public XMLStreamIndentProxy( XMLStreamWriter toDecorate, String indent, String lineFeed ) {
      m_decorated = toDecorate;
      m_indent = indent;
      m_lineFeed = lineFeed;
   }

   @Override
   public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
      String m = method.getName();
      if( s_startElemMethodName.equals(m) ) {
         // update state of parent node
         if( depth > 0 ) {
            m_hasChildElement.put(depth - 1, true);
         }

         // reset state of current node
         m_hasChildElement.put(depth, false);

         // indent for current depth
         m_decorated.writeCharacters(m_lineFeed);
         m_decorated.writeCharacters(repeat(depth, m_indent));
         depth++;
      }

      if( s_endElemMethodName.equals(m) ) {
         depth--;

         if( m_hasChildElement.get(depth) == true ) {
            m_decorated.writeCharacters(m_lineFeed);
            m_decorated.writeCharacters(repeat(depth, m_indent));
         }
      }

      if( s_emptyElemMethodName.equals(m) ) {
         // update state of parent node
         if( depth > 0 ) {
            m_hasChildElement.put(depth - 1, true);
         }

         // indent for current depth
         m_decorated.writeCharacters(m_lineFeed);
         m_decorated.writeCharacters(repeat(depth, m_indent));
      }

      return method.invoke(m_decorated, args);
   }

   private String repeat( int d, String s ) {
      String _s = "";
      while( d-- > 0 ) {
         _s += s;
      }
      return _s;
   }
}
