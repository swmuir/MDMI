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

import java.text.*;
import java.util.*;

import org.openhealthtools.mdht.mdmi.*;

public class ResourceHelper {
   private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.model.validate.validateStrings");

   public static String getString( String name, Object... args ) {
      String resource = null;
      try {
         resource = s_res.getString(name);
      }
      catch( MissingResourceException ex ) {
         Mdmi.INSTANCE.logger().loge(ex, ResourceHelper.class.getName());
      }
      return resource == null ? null : MessageFormat.format(resource, args);
   }
}
