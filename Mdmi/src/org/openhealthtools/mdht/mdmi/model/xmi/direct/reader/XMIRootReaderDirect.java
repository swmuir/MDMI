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

import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.model.raw.*;
import org.openhealthtools.mdht.mdmi.model.xmi.direct.*;

public class XMIRootReaderDirect extends XMIReaderDirectAbstract<List<MessageGroup>> {
   private static final XMIDocumentationReader s_docReader = new XMIDocumentationReader();
   private static final MessageGroupReader     s_grpReader = new MessageGroupReader();

   public XMIRootReaderDirect() {
      super(XMIDirectConstants.XMI_ROOT_ELEM_NAME);
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root,
         Map<String, Object> objectMap, List<MessageGroup> object ) throws XMLStreamException {

      if( s_docReader.canRead(reader) ) {
         // Get tool info from documentation reader. If it's not
         // the tool and version we are expecting, there's a problem.
         ToolInfo info = s_docReader.readAndBuild(reader, parsingInfo, root, objectMap);
         if( !XMIDirectConstants.MDMI_TOOL_NAME.equals(info.getToolName()) ) {
            throw new IllegalStateException("XMI reader was expecting to read for tool '"
                  + XMIDirectConstants.MDMI_TOOL_NAME + "', but XMI was from tool '" + info.getToolName() + "'.");
         }
         if( !XMIDirectConstants.MDMI_TOOL_VERSION.equals(info.getToolVersion()) ) {
            throw new IllegalStateException("XMI reader was expecting to read version '"
                  + XMIDirectConstants.MDMI_TOOL_VERSION + "', but XMI was version '" + info.getToolVersion() + "'.");
         }
      }
      else if( s_grpReader.canRead(reader) ) {
         // Read message group.
         object.add(s_grpReader.readAndBuild(reader, parsingInfo, root, objectMap));
      }
   }

   @Override
   protected List<MessageGroup> createObject( XMLStreamReader reader ) {
      return new ArrayList<MessageGroup>();
   }

   @Override
   protected String getNodeNamespace() {
      return XMIDirectConstants.XMI_NAMESPACE;
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, List<MessageGroup> object ) {
   }

   @Override
   protected String getObjectName( List<MessageGroup> object ) {
      return "";
   }
}
