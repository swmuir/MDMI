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

public class DataRuleWriter extends DirectWriterAbstract<DataRule> {
   private static final StringWriter s_ruleWriter = new StringWriter();

   @Override
   protected String getRootElementName() {
      return DataRule.class.getSimpleName();
   }

   @Override
   protected void writeAttributesAndChildren( DataRule object, XMLStreamWriter writer, Map<Object, String> refMap )
         throws XMLStreamException {

      // Write attributes
      WriterUtil.writeAttribute(writer, DataRuleValidate.s_nameField, object.getName());
      WriterUtil.writeAttribute(writer, DataRuleValidate.s_ruleExprName, object.getRuleExpressionLanguage());
      WriterUtil.writeAttribute(writer, DataRuleValidate.s_descName, object.getDescription());

      // Write rule as child element
      s_ruleWriter.writeElement(DataRuleValidate.s_ruleField, object.getRule(), writer, refMap);

      // Write child elements- in this case all are references.
      for( MdmiDatatype type : object.getDatatypes() ) {
         WriterUtil.writeRefElement(writer, DataRuleValidate.s_typesField, type, refMap);
      }

      if( object.getSemanticElement() != null ) {
         WriterUtil.writeRefElement(writer, DataRuleValidate.s_semElemField, object.getSemanticElement(), refMap);
      }
   }
}
