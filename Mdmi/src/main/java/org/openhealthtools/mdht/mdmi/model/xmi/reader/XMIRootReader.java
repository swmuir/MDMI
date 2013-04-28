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

public class XMIRootReader extends XMIReaderAbstract<RawRoot> implements IXMINodeReader<RawRoot> {
   private static final String                   s_nodeName         = "XMI";
   private static final String                   s_namespace        = "http://schema.omg.org/spec/XMI/2.1";
   private static final ModelReader              m_modelReader      = new ModelReader();
   private static final StereotypeInstanceReader m_stereotypeReader = new StereotypeInstanceReader();

   @Override
   public String getNodeName() {
      return s_nodeName;
   }

   @Override
   public String getNodeNamespace() {
      return s_namespace;
   }

   @Override
   protected RawRoot createObject() {
      return new RawRoot();
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, RawRoot root ) {
      // We don't need any attributes from the root.
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, RawRoot root ) throws XMLStreamException {
      if( m_modelReader.canRead(reader) ) {
         root.setModel(m_modelReader.read(reader));
      }
      else if( m_stereotypeReader.canRead(reader) ) {
         root.addStereotypeInstance(m_stereotypeReader.read(reader));
      }
   }
}
