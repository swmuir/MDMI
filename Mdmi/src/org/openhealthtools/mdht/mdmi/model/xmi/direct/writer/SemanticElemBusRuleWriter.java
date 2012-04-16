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

public class SemanticElemBusRuleWriter extends DirectWriterAbstract<SemanticElementBusinessRule> {
   private static final StringWriter s_ruleWriter = new StringWriter();

   @Override
   protected String getRootElementName() {
      return SemanticElementBusinessRule.class.getSimpleName();
   }

   @Override
   protected void writeAttributesAndChildren( SemanticElementBusinessRule object, XMLStreamWriter writer,
         Map<Object, String> refMap ) throws XMLStreamException {

      // Write attributes
      WriterUtil.writeAttribute(writer, SemanticElemBusRuleValidate.s_nameField, object.getName());
      WriterUtil.writeAttribute(writer, SemanticElemBusRuleValidate.s_descName, object.getDescription());
      WriterUtil.writeAttribute(writer, SemanticElemBusRuleValidate.s_ruleLangName, object.getRuleExpressionLanguage());

      // Write rule as child element.
      s_ruleWriter.writeElement(SemanticElemBusRuleValidate.s_ruleField, object.getRule(), writer, refMap);
   }
}
