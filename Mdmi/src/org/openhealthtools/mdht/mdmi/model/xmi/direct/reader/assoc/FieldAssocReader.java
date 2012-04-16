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

public class FieldAssocReader implements IXMIReaderAssoc<Field> {
   @Override
   public void processAssociation( Field modelObject, Object assocObject, String srcFieldName ) {
      if( ComplexDatatypeValidate.s_typeName.equals(srcFieldName) ) {
         modelObject.setDatatype((MdmiDatatype)assocObject);
      }
   }
}
