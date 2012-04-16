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

public class ExternalDatatypeReader extends MdmiDatatypeReader<DTExternal> {

   public ExternalDatatypeReader( String nodeName ) {
      super(nodeName);
   }

   public ExternalDatatypeReader() {
      super();
   }

   @Override
   public boolean canRead( XMLStreamReader reader ) {
      return super.canRead(reader) && isExternalType(reader);
   }

   private boolean isExternalType( XMLStreamReader reader ) {
      return DTExternal.class.getSimpleName().equals(XMIReaderUtil.getType(reader));
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root,
         Map<String, Object> objectMap, DTExternal object ) throws XMLStreamException {

      // No-op - no child elements
   }

   @Override
   protected DTExternal createObject( XMLStreamReader reader ) {
      return new DTExternal();
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, DTExternal object ) {
      super.readAttributes(reader, object);
      object.setTypeSpec(XMIReaderUtil.convertToURI(reader.getAttributeValue(null,
            ExternalDatatypeValidate.s_typeSpecField)));
   }
}
