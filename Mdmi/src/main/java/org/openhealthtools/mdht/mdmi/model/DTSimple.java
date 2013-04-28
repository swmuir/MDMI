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
package org.openhealthtools.mdht.mdmi.model;

import net.sourceforge.nrl.parser.model.*;

/**
 * A simple type (single value).
 */
public abstract class DTSimple extends MdmiDatatype implements IDataType {
   public DTSPrimitive getPrimitiveBaseType() {
      if( this instanceof DTSPrimitive )
         return (DTSPrimitive)this;
      if( this instanceof DTSEnumerated )
         return DTSPrimitive.STRING;
      // it is a derived type
      DTSDerived dt = (DTSDerived)this;
      if( dt.m_baseType == null )
         return null;
      if( dt.m_baseType.isEnum() )
         return DTSPrimitive.STRING;
      else if( dt.m_baseType.isDerived() )
         return ((DTSDerived)dt.m_baseType).getPrimitiveBaseType();
      return (DTSPrimitive)dt.m_baseType;
   }

   @Override
   public boolean isSimple() {
      return true;
   }

   @Override
   public boolean isComplex() {
      return false;
   }

   @Override
   public boolean isStruct() {
      return false;
   }

   @Override
   public boolean isChoice() {
      return false;
   }

   @Override
   public boolean isExternal() {
      return false;
   }

   @Override
   public boolean isBuiltIn() {
      return false;
   }

   @Override
   public ElementType getElementType() {
      return ElementType.DataType;
   }
} // DTSimple
