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

import java.util.*;

import javax.xml.stream.*;

import org.openhealthtools.mdht.mdmi.model.raw.*;
import org.openhealthtools.mdht.mdmi.model.xmi.direct.*;

public class XMIDocumentationReader extends XMIReaderDirectAbstract<ToolInfo> {
   public XMIDocumentationReader() {
      super(XMIDirectConstants.DOC_ELEM_NAME);
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root,
         Map<String, Object> objectMap, ToolInfo object ) throws XMLStreamException {

      // No-op - no child elements to read.
   }

   @Override
   protected ToolInfo createObject( XMLStreamReader reader ) {
      return new ToolInfo();
   }

   @Override
   protected String getNodeNamespace() {
      return XMIDirectConstants.XMI_NAMESPACE;
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, ToolInfo object ) {
      object.setToolName(reader.getAttributeValue(XMIDirectConstants.XMI_NAMESPACE, XMIDirectConstants.EXPORTER_NAME));
      object.setToolVersion(reader.getAttributeValue(XMIDirectConstants.XMI_NAMESPACE,
            XMIDirectConstants.EXPORTER_VERSION_NAME));
   }

   @Override
   protected String getObjectName( ToolInfo object ) {
      return object.getToolName();
   }
}
