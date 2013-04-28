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

public abstract class NodeWriter<T extends Node> extends DirectWriterAbstract<T> {
   private static final BagWriter    s_bagWriter    = new BagWriter();
   private static final ChoiceWriter s_choiceWriter = new ChoiceWriter();
   private static final LeafWriter   s_leafWriter   = new LeafWriter();

   @SuppressWarnings( { "rawtypes" } )
   public static NodeWriter getWriterByNodeType( Node node ) {
      if( node instanceof Bag ) {
         return s_bagWriter;
      }
      else if( node instanceof Choice ) {
         return s_choiceWriter;
      }
      else if( node instanceof LeafSyntaxTranslator ) {
         return s_leafWriter;
      }

      // Should never get here.
      throw new IllegalArgumentException("Unknown node type '" + node.getClass().getSimpleName()
            + "'.  Has model changed?");
   }

   @Override
   protected String getRootElementName() {
      return Node.class.getName();
   }

   @Override
   protected void writeAttributesAndChildren( T object, XMLStreamWriter writer, Map<Object, String> refMap )
         throws XMLStreamException {

      // Write XMI type attribute.
      WriterUtil.writeAttribute(writer, XMIDirectConstants.XMI_TYPE_ATTRIB, XMIDirectConstants.MDMI_PREFIX + ":"
            + object.getClass().getSimpleName(), XMIDirectConstants.XMI_NAMESPACE);

      // Write node attributes
      WriterUtil.writeAttribute(writer, NodeValidate.s_nameField, object.getName());
      WriterUtil.writeAttribute(writer, NodeValidate.s_locationField, object.getLocation());
      WriterUtil.writeAttribute(writer, NodeValidate.s_descName, object.getDescription());
      WriterUtil.writeAttribute(writer, NodeValidate.s_minName, object.getMinOccurs());
      WriterUtil.writeAttribute(writer, NodeValidate.s_maxName, object.getMaxOccurs());
      WriterUtil.writeAttribute(writer, NodeValidate.s_locExprLangName, object.getLocationExpressionLanguage());
      WriterUtil.writeAttribute(writer, NodeValidate.s_fieldName, object.getFieldName());

      writeAdditionalAttributes(object, writer);

      // Write child elements
      if( object.getSemanticElement() != null ) {
         WriterUtil.writeRefElement(writer, NodeValidate.s_semElemName, object.getSemanticElement(), refMap);
      }

      writeAdditionalElements(object, writer, refMap);
   }

   protected abstract void writeAdditionalAttributes( T object, XMLStreamWriter writer ) throws XMLStreamException;

   protected abstract void writeAdditionalElements( T object, XMLStreamWriter writer, Map<Object, String> refMap )
         throws XMLStreamException;
}
