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
import org.openhealthtools.mdht.mdmi.model.validate.*;
import org.openhealthtools.mdht.mdmi.model.xmi.*;

public class SimpleMessageCompositeReader extends XMIReaderDirectAbstract<SimpleMessageComposite> {
   public SimpleMessageCompositeReader( String nodeName ) {
      super(nodeName);
   }

   public SimpleMessageCompositeReader() {
      super(SimpleMessageComposite.class.getSimpleName());
   }

   @Override
   public boolean canRead( XMLStreamReader reader ) {
      return super.canRead(reader) && typeIsSimpleComp(reader);
   }

   private boolean typeIsSimpleComp( XMLStreamReader reader ) {
      return SimpleMessageComposite.class.getSimpleName().equals(XMIReaderUtil.getType(reader));
   }

   @Override
   protected String getObjectName( SimpleMessageComposite object ) {
      return object.getName();
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root,
         Map<String, Object> objectMap, SimpleMessageComposite object ) throws XMLStreamException {

      // No-op - no child elements by composition.
   }

   @Override
   protected SimpleMessageComposite createObject( XMLStreamReader reader ) {
      return new SimpleMessageComposite();
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, SimpleMessageComposite object ) {

      object.setName(reader.getAttributeValue(null, SimpleMsgCompositeValidate.s_nameField));
      object.setDescription(reader.getAttributeValue(null, SimpleMsgCompositeValidate.s_descName));
   }
}
