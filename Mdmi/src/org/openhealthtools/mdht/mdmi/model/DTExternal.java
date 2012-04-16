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

import java.net.*;

import net.sourceforge.nrl.parser.model.*;

public class DTExternal extends MdmiDatatype implements IDataType {
   private URI m_typeSpec;

   public URI getTypeSpec() {
      return m_typeSpec;
   }

   public void setTypeSpec(URI typeSpec) {
      m_typeSpec = typeSpec;
   }

	@Override
	public boolean isSimple() {
		return false;
	}

	@Override
	public boolean isComplex() {
		return false;
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

	@Override
	public boolean isDerived() {
		return false;
	}

   @Override
   public boolean isChoice() {
      return false;
   }

   @Override
   public boolean isEnum() {
      return false;
   }

   @Override
   public boolean isStruct() {
      return false;
   }

   @Override
   public boolean isExternal() {
      return true;
   }

	@Override
	protected void toString( StringBuffer out, String indent ) {
		out.append( indent + "External datatype: " + m_name + " = '" + m_typeSpec + "'\r\n" );
	}

	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public boolean isBuiltIn() {
		return true;
	}

	@Override
	public ElementType getElementType() {
		return ElementType.DataType;
	}
} // DTExternal
