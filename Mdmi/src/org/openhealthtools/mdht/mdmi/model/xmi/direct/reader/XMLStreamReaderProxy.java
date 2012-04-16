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
package org.openhealthtools.mdht.mdmi.model.xmi.direct.reader;

import java.lang.reflect.*;

import javax.xml.stream.*;

public class XMLStreamReaderProxy implements InvocationHandler {
   private XMLStreamReader     m_wrapped           = null;
   private ParsingInfo         m_info              = null;

   private static final String s_nextMethod        = "next";
   private static final String s_getElemTextMethod = "getElementText";

   public XMLStreamReaderProxy( XMLStreamReader toWrap, ParsingInfo info ) {
      m_info = info;
      m_wrapped = toWrap;
   }

   @Override
   public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
      // Invoke normally.
      Object rv = method.invoke(m_wrapped, args);

      if( s_nextMethod.equals(method.getName()) ) {
         if( !method.getReturnType().isAssignableFrom(int.class) ) {
            throw new IllegalStateException("Return type of 'next' method is "
                  + "not type Integer.  Cannot read XML stream.");
         }

         int parseEvent = (Integer)rv;
         if( parseEvent == XMLStreamConstants.START_ELEMENT ) {
            m_info.stepDown();
         }
         else if( parseEvent == XMLStreamConstants.END_ELEMENT ) {
            m_info.stepUp();
         }
      }
      else if( s_getElemTextMethod.equals(method.getName()) ) {
         // Calls to getNextElement leave the stream and END_ELEMENT
         m_info.stepUp();
      }
      return rv;
   }
}
