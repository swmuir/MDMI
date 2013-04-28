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

public class ToolInfo {
   private String m_toolName;
   private String m_toolVersion;

   public void setToolName( String toolName ) {
      m_toolName = toolName;
   }

   public void setToolVersion( String toolVersion ) {
      m_toolVersion = toolVersion;
   }

   public String getToolName() {
      return m_toolName;
   }

   public String getToolVersion() {
      return m_toolVersion;
   }
}
