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

public class PrimitiveDatatypeReader extends MdmiDatatypeReader<DTSPrimitive> {
   public PrimitiveDatatypeReader( String nodeName ) {
      super(nodeName);
   }

   public PrimitiveDatatypeReader() {
      super();
   }

   @Override
   public boolean canRead( XMLStreamReader reader ) {
      return super.canRead(reader) && isPrimitiveType(reader);
   }

   private boolean isPrimitiveType( XMLStreamReader reader ) {
      return DTSPrimitive.class.getSimpleName().equals(XMIReaderUtil.getType(reader));
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root,
         Map<String, Object> objectMap, DTSPrimitive object ) throws XMLStreamException {
   }

   @Override
   protected DTSPrimitive createObject( XMLStreamReader reader ) {
      String typeName = reader.getAttributeValue(null, MdmiDatatypeValidate.s_nameField);
      // Based on the type name, retrieve the appropriate single instance.
      for( DTSPrimitive primitive : DTSPrimitive.ALL_PRIMITIVES ) {
         if( primitive.getTypeName().equalsIgnoreCase(typeName) ) {
            return primitive;
         }
      }
      return null;
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, DTSPrimitive object ) {
   }
}
