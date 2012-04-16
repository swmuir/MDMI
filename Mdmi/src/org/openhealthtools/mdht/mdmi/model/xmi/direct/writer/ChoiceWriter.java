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

public class ChoiceWriter extends NodeWriter<Choice> {
   @Override
   protected void writeAdditionalAttributes( Choice object, XMLStreamWriter writer ) throws XMLStreamException {

      WriterUtil.writeAttribute(writer, ChoiceValidate.s_constrName, object.getConstraint());
      WriterUtil.writeAttribute(writer, ChoiceValidate.s_constrLangName, object.getConstraintExpressionLanguage());
   }

   @SuppressWarnings( "unchecked" )
   @Override
   protected void writeAdditionalElements( Choice object, XMLStreamWriter writer, Map<Object, String> refMap )
         throws XMLStreamException {

      for( Node node : object.getNodes() ) {
         @SuppressWarnings( "rawtypes" )
         NodeWriter nodeWriter = NodeWriter.getWriterByNodeType(node);
         nodeWriter.writeElement(ChoiceValidate.s_nodesField, node, writer, refMap);
      }
   }
}
