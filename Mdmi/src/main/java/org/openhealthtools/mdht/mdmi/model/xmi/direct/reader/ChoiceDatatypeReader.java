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

public class ChoiceDatatypeReader extends MdmiDatatypeReader<DTCChoice> {
   private static final FieldReader s_fieldReader = new FieldReader(ComplexDatatypeValidate.s_fieldsName);

   public ChoiceDatatypeReader( String nodeName ) {
      super(nodeName);
   }

   public ChoiceDatatypeReader() {
      super();
   }

   @Override
   public boolean canRead( XMLStreamReader reader ) {
      return super.canRead(reader) && isChoiceType(reader);
   }

   private boolean isChoiceType( XMLStreamReader reader ) {
      return DTCChoice.class.getSimpleName().equals(XMIReaderUtil.getType(reader));
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root,
         Map<String, Object> objectMap, DTCChoice object ) throws XMLStreamException {

      if( s_fieldReader.canRead(reader) ) {
         Field field = s_fieldReader.readAndBuild(reader, parsingInfo, root, objectMap);

         object.getFields().add(field);
         field.setOwnerType(object);
      }
   }

   @Override
   protected DTCChoice createObject( XMLStreamReader reader ) {
      return new DTCChoice();
   }

}
