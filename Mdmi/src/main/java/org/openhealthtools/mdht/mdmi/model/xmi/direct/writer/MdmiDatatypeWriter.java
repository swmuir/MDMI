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
package org.openhealthtools.mdht.mdmi.model.xmi.direct.writer;

import java.util.*;

import javax.xml.stream.*;

import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.model.validate.*;
import org.openhealthtools.mdht.mdmi.model.xmi.direct.*;

public abstract class MdmiDatatypeWriter<T extends MdmiDatatype> extends DirectWriterAbstract<T> {
   private static final PrimitiveTypeWriter  s_primTypeWriter    = new PrimitiveTypeWriter();
   private static final DerivedTypeWriter    s_derivedTypeWriter = new DerivedTypeWriter();
   private static final EnumeratedTypeWriter s_enumTypeWriter    = new EnumeratedTypeWriter();
   private static final ExternalTypeWriter   s_extTypeWriter     = new ExternalTypeWriter();
   private static final ComplexTypeWriter    s_complexTypeWriter = new ComplexTypeWriter();

   @SuppressWarnings( "rawtypes" )
   public static MdmiDatatypeWriter getWriterByType( MdmiDatatype type ) {
      if( type instanceof DTSPrimitive ) {
         return s_primTypeWriter;
      }
      else if( type instanceof DTSDerived ) {
         return s_derivedTypeWriter;
      }
      else if( type instanceof DTSEnumerated ) {
         return s_enumTypeWriter;
      }
      else if( type instanceof DTExternal ) {
         return s_extTypeWriter;
      }
      else if( type instanceof DTComplex ) {
         return s_complexTypeWriter;
      }

      throw new IllegalArgumentException("Unknown data type '" + type.getClass().getSimpleName()
            + "'.  Has model changed?");
   }

   @Override
   protected String getRootElementName() {
      return MdmiDatatype.class.getSimpleName();
   }

   @Override
   protected void writeAttributesAndChildren( T object, XMLStreamWriter writer, Map<Object, String> refMap )
         throws XMLStreamException {

      // Write XMI type attribute.
      WriterUtil.writeAttribute(writer, XMIDirectConstants.XMI_TYPE_ATTRIB, XMIDirectConstants.MDMI_PREFIX + ":"
            + object.getClass().getSimpleName(), XMIDirectConstants.XMI_NAMESPACE);

      // Write type attributes
      WriterUtil.writeAttribute(writer, MdmiDatatypeValidate.s_nameField, object.getTypeName());
      WriterUtil.writeAttribute(writer, MdmiDatatypeValidate.s_descName, object.getDescription());
      WriterUtil.writeAttribute(writer, MdmiDatatypeValidate.s_isReadonly, object.isReadonly());
   }
}
