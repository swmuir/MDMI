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
package org.openhealthtools.mdht.mdmi.model.xmi.direct;

public interface XMIDirectConstants {
   public static final String ENCODING              = "UTF-8";
   public static final String XML_VERSION           = "1.0";

   public static final String XMI_NAMESPACE         = "http://schema.omg.org/spec/XMI/2.1";

   // TODO- what should this be
   public static final String MDMI_NAMESPACE        = "http://schema.omg.org/spec/MDMI/1.0"; 

   public static final String XMI_PREFIX            = "xmi";
   public static final String MDMI_PREFIX           = "mdmi";

   public static final String XMI_ROOT_ELEM_NAME    = "XMI";
   public static final String DOC_ELEM_NAME         = "Documentation";
   public static final String EXPORTER_NAME         = "exporter";
   public static final String EXPORTER_VERSION_NAME = "exporterVersion";

   public static final String XMI_ID_ATTRIB         = "id";
   public static final String XMI_ID_REF_ATTRIB     = "idref";
   public static final String XMI_TYPE_ATTRIB       = "type";

   public static final String MDMI_TOOL_NAME        = "Firestar MDMI Tool";
   public static final String MDMI_TOOL_VERSION     = "1.0";
}
