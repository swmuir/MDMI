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
import org.openhealthtools.mdht.mdmi.model.xmi.direct.*;

public class SimpleMsgCompositeWriter<T extends SimpleMessageComposite> extends DirectWriterAbstract<T> {
   private static final SimpleMsgCompositeWriter<SimpleMessageComposite> s_simpleMsgWriter = new SimpleMsgCompositeWriter<SimpleMessageComposite>();
   private static final MessageCompositeWriter                           s_msgWriter       = new MessageCompositeWriter();

   @SuppressWarnings( "rawtypes" )
   public static SimpleMsgCompositeWriter getWriterByType( SimpleMessageComposite comp ) {
      if( comp instanceof MessageComposite ) {
         return s_msgWriter;
      }

      return s_simpleMsgWriter;
   }

   @Override
   protected String getRootElementName() {
      return SimpleMessageComposite.class.getSimpleName();
   }

   @Override
   protected void writeAttributesAndChildren( T object, XMLStreamWriter writer, Map<Object, String> refMap )
         throws XMLStreamException {

      // Write XMI type attribute.
      WriterUtil.writeAttribute(writer, XMIDirectConstants.XMI_TYPE_ATTRIB, XMIDirectConstants.MDMI_PREFIX + ":"
            + object.getClass().getSimpleName(), XMIDirectConstants.XMI_NAMESPACE);

      // Write attributes
      WriterUtil.writeAttribute(writer, SimpleMsgCompositeValidate.s_nameField, object.getName());
      WriterUtil.writeAttribute(writer, SimpleMsgCompositeValidate.s_descName, object.getDescription());

      // Write child elements.
      for( SemanticElement elem : object.getSemanticElements() ) {
         WriterUtil.writeRefElement(writer, SimpleMsgCompositeValidate.s_semElemField, elem, refMap);
      }
   }

}
