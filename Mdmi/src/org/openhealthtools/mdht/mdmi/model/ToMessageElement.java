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
 * 
 */
public class ToMessageElement extends ConversionRule {
   private MdmiBusinessElementReference m_businessElement;

   public MdmiBusinessElementReference getBusinessElement() {
      return m_businessElement;
   }

   public void setBusinessElement( MdmiBusinessElementReference businessElement ) {
      m_businessElement = businessElement;
   }

   @Override
   public String getOriginalName() {
      if( m_businessElement == null ) {
         return m_name;
      }

      return m_name == null || m_name.length() <= 0 ? m_businessElement.getName() : m_name;
   }

   @Override
   public IModelElement getType() {
      return m_businessElement == null ? null : m_businessElement.getReferenceDatatype();
   }

   @Override
   protected void toString( StringBuffer out, String indent ) {
      out.append(indent + "ToMessageElement: " + m_name + "\r\n");
      indent += "  ";
      super.toString(out, indent);
      if( m_businessElement != null )
         out.append(indent + "business element reference: " + m_businessElement.getName() + "\r\n");
   }
} // ToMessageElement
