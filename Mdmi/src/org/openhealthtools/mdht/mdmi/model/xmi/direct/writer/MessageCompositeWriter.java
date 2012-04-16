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

public class MessageCompositeWriter extends SimpleMsgCompositeWriter<MessageComposite> {
   @SuppressWarnings( "unchecked" )
   @Override
   protected void writeAttributesAndChildren( MessageComposite object, XMLStreamWriter writer,
         Map<Object, String> refMap ) throws XMLStreamException {

      super.writeAttributesAndChildren(object, writer, refMap);

      // Write child message composites.
      for( SimpleMessageComposite msgComp : object.getComposites() ) {
         @SuppressWarnings( "rawtypes" )
         SimpleMsgCompositeWriter msgWriter = SimpleMsgCompositeWriter.getWriterByType(msgComp);

         msgWriter.writeElement(SemanticElementSetValidate.s_compName, msgComp, writer, refMap);
      }
   }
}
