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

import javax.xml.stream.*;

import org.openhealthtools.mdht.mdmi.model.*;

public class ToBusinessElementReader extends ConversionRuleReader<ToBusinessElement> {
   public ToBusinessElementReader(String nodeName) {
      super(nodeName);
   }
   
   public ToBusinessElementReader() {
      super(ToBusinessElement.class.getSimpleName());
   }

   @Override
   protected ToBusinessElement createObject(XMLStreamReader reader) {
      return new ToBusinessElement();
   }
}
