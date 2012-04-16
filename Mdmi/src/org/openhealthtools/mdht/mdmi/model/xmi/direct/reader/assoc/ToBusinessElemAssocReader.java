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
package org.openhealthtools.mdht.mdmi.model.xmi.direct.reader.assoc;

import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.model.validate.*;

public class ToBusinessElemAssocReader implements IXMIReaderAssoc<ToBusinessElement> {
   @Override
   public void processAssociation( ToBusinessElement modelObject, Object assocObject, String srcFieldName ) {
      if( ToBusinessElemValidate.s_busElemField.equals(srcFieldName) ) {
         modelObject.setBusinessElement((MdmiBusinessElementReference)assocObject);
      }
   }
}
