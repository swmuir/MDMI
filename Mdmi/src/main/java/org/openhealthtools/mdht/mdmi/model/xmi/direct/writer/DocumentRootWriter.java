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

import javax.xml.stream.*;

import org.openhealthtools.mdht.mdmi.model.xmi.direct.*;

public class DocumentRootWriter {
   private static final String s_versionName = "version";
   public static final String  s_version     = "2.1";

   public void writeDocumentOpen( XMLStreamWriter writer ) throws XMLStreamException {
      writer.writeStartDocument(XMIDirectConstants.ENCODING, XMIDirectConstants.XML_VERSION);

      // Write root XMI element. We will leave this open.
      writer.writeStartElement(XMIDirectConstants.XMI_PREFIX, XMIDirectConstants.XMI_ROOT_ELEM_NAME,
            XMIDirectConstants.XMI_NAMESPACE);
      writer.writeAttribute(XMIDirectConstants.XMI_NAMESPACE, s_versionName, s_version);
      writer.writeNamespace(XMIDirectConstants.XMI_PREFIX, XMIDirectConstants.XMI_NAMESPACE);
      writer.writeNamespace(XMIDirectConstants.MDMI_PREFIX, XMIDirectConstants.MDMI_NAMESPACE);

      // Write documentation element.
      writer.writeStartElement(XMIDirectConstants.XMI_NAMESPACE, XMIDirectConstants.DOC_ELEM_NAME);
      writer.writeAttribute(XMIDirectConstants.XMI_NAMESPACE, XMIDirectConstants.EXPORTER_NAME,
            XMIDirectConstants.MDMI_TOOL_NAME);
      writer.writeAttribute(XMIDirectConstants.XMI_NAMESPACE, XMIDirectConstants.EXPORTER_VERSION_NAME,
            XMIDirectConstants.MDMI_TOOL_VERSION);
      writer.writeEndElement();
   }

   public void writeDocumentClose( XMLStreamWriter writer ) throws XMLStreamException {
      writer.writeEndElement();
      writer.writeEndDocument();
   }
}
