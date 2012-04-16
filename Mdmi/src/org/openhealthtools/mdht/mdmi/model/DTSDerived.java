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
 * A derived type is simple type with a restricted set of values of another (base) simple type.
 */
public final class DTSDerived extends DTSimple {
   protected DTSimple m_baseType;
   protected String   m_restriction;

   public DTSimple getBaseType() {
      return m_baseType;
   }

   public void setBaseType( DTSimple baseType ) {
      m_baseType = baseType;
   }

   public String getRestriction() {
      return m_restriction;
   }

   public void setRestriction( String restriction ) {
      m_restriction = restriction;
   }

   @Override
   public boolean isPrimitive() {
      return false;
   }

   @Override
   public boolean isDerived() {
      return true;
   }

   @Override
   public boolean isEnum() {
      return false;
   }

   @Override
   public IModelElement getParent() {
      return m_baseType;
   }

   @Override
   protected void toString( StringBuffer out, String indent ) {
      MessageGroup.appendML(out, indent, "Derived datatype: " + m_name + " = ", m_restriction);
   }
} // DTSDerived
