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

public class MdmiBerReader extends XMIReaderDirectAbstract<MdmiBusinessElementReference> {
   private static final MdmiBerRuleReader s_ruleReader = new MdmiBerRuleReader(
                                                                 BusinessElemRefValidate.s_ruleName);

   public MdmiBerReader( String nodeName ) {
      super(nodeName);
   }

   public MdmiBerReader() {
      super(MdmiBusinessElementReference.class.getSimpleName());
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root,
         Map<String, Object> objectMap, MdmiBusinessElementReference object ) throws XMLStreamException {
      if( s_ruleReader.canRead(reader) ) {
         MdmiBusinessElementRule rule = s_ruleReader.readAndBuild(reader, parsingInfo, root, objectMap);
         rule.setBusinessElement(object);
         object.addBusinessRule(rule);
      }
   }

   @Override
   protected MdmiBusinessElementReference createObject( XMLStreamReader reader ) {
      return new MdmiBusinessElementReference();
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, MdmiBusinessElementReference object ) {
      object.setName(reader.getAttributeValue(null, BusinessElemRefValidate.s_nameField));
      object.setUniqueIdentifier(reader.getAttributeValue(null, BusinessElemRefValidate.s_idField));
      object.setDescription(reader.getAttributeValue(null, BusinessElemRefValidate.s_descField));

      Boolean bool = XMIReaderUtil.convertToBoolean(reader.getAttributeValue(null,
            BusinessElemRefValidate.s_isReadonly));
      if( bool != null ) {
         object.setReadonly(bool);
      }
      object.setReference(XMIReaderUtil.convertToURI(reader.getAttributeValue(null, BusinessElemRefValidate.s_refField)));
   }

   @Override
   protected String getObjectName( MdmiBusinessElementReference object ) {
      return object.getName();
   }
}
