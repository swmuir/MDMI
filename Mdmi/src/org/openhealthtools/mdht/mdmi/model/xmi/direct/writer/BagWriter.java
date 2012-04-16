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

import java.util.*;

import javax.xml.stream.*;

import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.model.validate.*;

public class BagWriter extends NodeWriter<Bag> {
   @Override
   protected void writeAdditionalAttributes( Bag object, XMLStreamWriter writer ) throws XMLStreamException {

      WriterUtil.writeAttribute(writer, BagValidate.s_uniqueName, object.isUnique());
      WriterUtil.writeAttribute(writer, BagValidate.s_orderedName, object.isOrdered());
   }

   @SuppressWarnings( "unchecked" )
   @Override
   protected void writeAdditionalElements( Bag object, XMLStreamWriter writer, Map<Object, String> refMap )
         throws XMLStreamException {

      for( Node node : object.getNodes() ) {
         @SuppressWarnings( "rawtypes" )
         NodeWriter nodeWriter = NodeWriter.getWriterByNodeType(node);
         nodeWriter.writeElement(BagValidate.s_nodesField, node, writer, refMap);
      }
   }
}
