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

public class MdmiDictRefReader extends XMIReaderDirectAbstract<MdmiDomainDictionaryReference> {
   private static final MdmiBerReader s_refReader = new MdmiBerReader(DomainDictionaryRefValidate.s_busElemField);

   public MdmiDictRefReader( String nodeName ) {
      super(nodeName);
   }

   public MdmiDictRefReader() {
      super(MdmiDomainDictionaryReference.class.getSimpleName());
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root,
         Map<String, Object> objectMap, MdmiDomainDictionaryReference object ) throws XMLStreamException {
      if( s_refReader.canRead(reader) ) {
         MdmiBusinessElementReference ref = s_refReader.readAndBuild(reader, parsingInfo, root, objectMap);
         ref.setDomainDictionaryReference(object);
         object.addBusinessElement(ref);
      }
   }

   @Override
   protected MdmiDomainDictionaryReference createObject( XMLStreamReader reader ) {
      return new MdmiDomainDictionaryReference();
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, MdmiDomainDictionaryReference object ) {
      object.setName(reader.getAttributeValue(null, DomainDictionaryRefValidate.s_nameField));
      object.setDescription(reader.getAttributeValue(null, DomainDictionaryRefValidate.s_descName));
      object.setReference(XMIReaderUtil.convertToURI(reader.getAttributeValue(null,
            DomainDictionaryRefValidate.s_refField)));
   }

   @Override
   protected String getObjectName( MdmiDomainDictionaryReference object ) {
      return object.getName();
   }
}
