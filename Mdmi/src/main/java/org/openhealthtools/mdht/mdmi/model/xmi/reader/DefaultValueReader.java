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
package org.openhealthtools.mdht.mdmi.model.xmi.reader;

import javax.xml.stream.*;

import org.openhealthtools.mdht.mdmi.model.raw.*;

public class DefaultValueReader extends XMIReaderAbstract<DefaultValue> implements IXMINodeReader<DefaultValue> {
   private static final String s_nodeName    = "defaultValue";

   // Attribute names
   private static final String s_valueAtt    = "value";
   private static final String s_instanceAtt = "instance";

   @Override
   protected DefaultValue createObject() {
      return new DefaultValue();
   }

   @Override
   protected String getNodeName() {
      return s_nodeName;
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, DefaultValue object ) {
      object.setValue(reader.getAttributeValue(null, s_valueAtt));
      object.setInstance(reader.getAttributeValue(null, s_instanceAtt));
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, DefaultValue object ) throws XMLStreamException {
      // No child elements to read.
   }
}
