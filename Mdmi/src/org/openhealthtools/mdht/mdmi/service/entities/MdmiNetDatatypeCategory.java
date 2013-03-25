/*******************************************************************************
* Copyright (c) 2012-2013 Firestar Software, Inc.
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
package org.openhealthtools.mdht.mdmi.service.entities;

/**
 * Supported MDMI data types.
 * <pre>
 * PRIMITIVE  = built in data types, namely BINARY, BOOLEAN, DECIMAL, DATTIME, INTEGER, and STRING.
 * EXTERNAL   = an externally defined data type, must have a reference URI.
 * DERIVED    = a restriction of a simple type (primitive or another derived type).
 * ENUMERATED = a set of enumerated values (literals).
 * STRUCTURE  = a complex data structure, contains fields which have their own data types.
 * CHOICE     = a choice between a number of fields, each field has its own data types.
 * </pre>
 * @author goancea
 */
public enum MdmiNetDatatypeCategory {
   NONE,
   PRIMITIVE,
   EXTERNAL,
   DERIVED,
   ENUMERATED,
   STRUCTURE,
   CHOICE
} // MdmiDataTypeClass
