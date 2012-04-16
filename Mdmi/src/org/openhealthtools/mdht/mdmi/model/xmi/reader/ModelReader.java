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

public class ModelReader extends XMIReaderAbstract<ModelRoot> implements IXMINodeReader<ModelRoot> {
   private static final String                     s_nodeName    = "Model";
   private static final String                     s_namespace   = "http://schema.omg.org/spec/UML/2.1.2";
   private static final PackagedElementClassReader m_classReader = new PackagedElementClassReader();

   @Override
   public String getNodeName() {
      return s_nodeName;
   }

   @Override
   public String getNodeNamespace() {
      return s_namespace;
   }

   @Override
   protected ModelRoot createObject() {
      return new ModelRoot();
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, ModelRoot model ) {
      // We don't need any attributes on the model.
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ModelRoot model ) throws XMLStreamException {
      if( m_classReader.canRead(reader) ) {
         model.addClassDef(m_classReader.read(reader));
      }
   }
}
