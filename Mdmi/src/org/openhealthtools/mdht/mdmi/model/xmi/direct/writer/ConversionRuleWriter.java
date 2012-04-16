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

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import javax.xml.stream.*;

import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.model.validate.*;
import org.openhealthtools.mdht.mdmi.model.xmi.direct.*;

public abstract class ConversionRuleWriter<T extends ConversionRule> extends DirectWriterAbstract<T> {
   private static final StringWriter s_ruleWriter = new StringWriter();

   @Override
   protected void writeAttributesAndChildren( T object, XMLStreamWriter writer, Map<Object, String> refMap )
         throws XMLStreamException {

      // Write attributes
      WriterUtil.writeAttribute(writer, ConversionRuleValidate.s_nameField, object.getName());
      WriterUtil.writeAttribute(writer, ConversionRuleValidate.s_descName, object.getDescription());
      WriterUtil.writeAttribute(writer, ConversionRuleValidate.s_ruleLangName, object.getRuleExpressionLanguage());

      // Write rule as child element. At a minimum, the rule field may
      // contain line feeds, which are not valid on an attribute.
      s_ruleWriter.writeElement(ConversionRuleValidate.s_ruleName, object.getRule(), writer, refMap);
   }

   public static void main( String[] args ) {
      try {
         ByteArrayOutputStream outStream = new ByteArrayOutputStream();

         XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(outStream,
               XMIDirectConstants.ENCODING);
         XMLStreamIndentProxy proxy = new XMLStreamIndentProxy(writer);
         XMLStreamWriter indentWriter = (XMLStreamWriter)Proxy.newProxyInstance(XMLStreamWriter.class.getClassLoader(),
               new Class[] { XMLStreamWriter.class }, proxy);

         indentWriter.writeStartDocument();
         indentWriter.writeStartElement("theRootElem");
         indentWriter.writeStartElement("someChildElem");

         indentWriter.writeCharacters("some text to constitute \nthe element.");

         indentWriter.writeEndElement();
         indentWriter.writeEndElement();
         indentWriter.writeEndDocument();

         System.out.println(new String(outStream.toByteArray()));
      }
      catch( Exception exc ) {
         exc.printStackTrace();
      }
   }
}
