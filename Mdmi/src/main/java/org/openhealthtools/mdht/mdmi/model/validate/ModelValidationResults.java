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

import java.util.*;

public class ModelValidationResults {
   private List<ModelInfo> m_warnings = new ArrayList<ModelInfo>();
   private List<ModelInfo> m_errors   = new ArrayList<ModelInfo>();

   public List<ModelInfo> getWarnings() {
      return m_warnings;
   }

   public void addWarning( ModelInfo warning ) {
      m_warnings.add(warning);
   }

   public void addWarning( Object obj, String field, String msg ) {
      ModelInfo info = new ModelInfo(obj, field, msg);
      addWarning(info);
   }

   public void addWarningFromRes( Object obj, String field, Object... args ) {
      String resKey = ValidateHelper.getResourceKey(obj, field);
      addWarning(obj, field, ResourceHelper.getString(resKey, args));
   }

   public void addWarningFromRes( String className, Object obj, String field, Object... args ) {
      String resKey = ValidateHelper.getResourceKey(className, field);
      addWarning(obj, field, ResourceHelper.getString(resKey, args));
   }

   public List<ModelInfo> getErrors() {
      return m_errors;
   }

   public void addError( ModelInfo error ) {
      m_errors.add(error);
   }

   public void addError( Object obj, String field, String msg ) {
      ModelInfo info = new ModelInfo(obj, field, msg);
      addError(info);
   }

   public void addErrorFromRes( Object obj, String field, Object... args ) {
      String resKey = ValidateHelper.getResourceKey(obj, field);
      addError(obj, field, ResourceHelper.getString(resKey, args));
   }

   public void addErrorFromRes( String className, Object obj, String field, Object... args ) {
      String resKey = ValidateHelper.getResourceKey(className, field);
      addError(obj, field, ResourceHelper.getString(resKey, args));
   }
}
