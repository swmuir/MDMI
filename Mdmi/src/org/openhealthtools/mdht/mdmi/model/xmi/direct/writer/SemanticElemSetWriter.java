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

public class SemanticElemSetWriter extends DirectWriterAbstract<SemanticElementSet> {
   private static final SemanticElementWriter s_elemWriter = new SemanticElementWriter();

   @Override
   protected String getRootElementName() {
      return SemanticElementSet.class.getSimpleName();
   }

   @SuppressWarnings( "unchecked" )
   @Override
   protected void writeAttributesAndChildren( SemanticElementSet object, XMLStreamWriter writer,
         Map<Object, String> refMap ) throws XMLStreamException {

      // Write attributes
      WriterUtil.writeAttribute(writer, SemanticElementSetValidate.s_nameField, object.getName());
      WriterUtil.writeAttribute(writer, SemanticElementSetValidate.s_descName, object.getDescription());

      // Write child elements
      if( object.getSyntaxModel() != null ) {
         WriterUtil.writeRefElement(writer, SemanticElementSetValidate.s_syntaxModelField, object.getSyntaxModel(),
               refMap);
      }

      for( SemanticElement semElem : object.getSemanticElements() ) {
         s_elemWriter.writeElement(SemanticElementSetValidate.s_semElemsField, semElem, writer, refMap);
      }

      for( SimpleMessageComposite msgComp : object.getComposites() ) {
         @SuppressWarnings( "rawtypes" )
         SimpleMsgCompositeWriter msgWriter = SimpleMsgCompositeWriter.getWriterByType(msgComp);

         msgWriter.writeElement(SemanticElementSetValidate.s_compName, msgComp, writer, refMap);
      }
   }
}
