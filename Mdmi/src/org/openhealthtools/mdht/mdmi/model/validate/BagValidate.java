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
package org.openhealthtools.mdht.mdmi.model.validate;

import org.openhealthtools.mdht.mdmi.model.*;

public class BagValidate extends NodeValidate<Bag> {
   public static final String s_nodesField  = "nodes";
   public static final String s_uniqueName  = "isUnique";
   public static final String s_orderedName = "isOrdered";

   @Override
   public void validate( Bag object, ModelValidationResults results ) {
      super.validate(object, results);

      if( object.getNodes().size() == 0 ) {
         results.addErrorFromRes(object, s_nodesField, object.getName());
      }
   }
}
