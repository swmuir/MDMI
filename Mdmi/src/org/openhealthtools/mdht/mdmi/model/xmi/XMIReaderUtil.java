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
package org.openhealthtools.mdht.mdmi.model.xmi;

import java.net.*;

import javax.xml.stream.*;

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.model.csv.reader.*;
import org.openhealthtools.mdht.mdmi.model.enums.*;
import org.openhealthtools.mdht.mdmi.model.raw.*;
import org.openhealthtools.mdht.mdmi.model.xmi.direct.*;

public class XMIReaderUtil {
   private static final String s_assocInd = "association";

   public static String getType( XMLStreamReader reader ) {
      String rawValue = reader.getAttributeValue(XMIDirectConstants.XMI_NAMESPACE, XMIDirectConstants.XMI_TYPE_ATTRIB);
      String[] values = rawValue.split(":");
      if( values == null || values.length != 2 || !XMIDirectConstants.MDMI_PREFIX.equals(values[0]) ) {
         return null;
      }
      return values[1];
   }

   public static boolean atEndOfThisElement( XMLStreamReader reader, String nodeNameToCompare, String namespaceToCompare ) {
      return reader.getEventType() == XMLStreamConstants.END_ELEMENT && namespacesMatch(reader, namespaceToCompare)
            && nodeNameToCompare.equals(reader.getLocalName());
   } // JGK: Changed order of short-circuit

   public static boolean namespacesMatch( XMLStreamReader reader, String namespaceToCompare ) {
      String readerNamespaceURI = reader.getNamespaceURI();
      if( namespaceToCompare == null ) {
         return readerNamespaceURI == null;
      }
      return namespaceToCompare.equals(readerNamespaceURI);
   }

   public static URI convertToURI( String value ) {
      try {
         return value == null ? null : new URI(value);
      }
      catch( URISyntaxException exc ) {
         return null;
      }
   }

   public static Boolean convertToBoolean( String value ) {
      if( value != null ) {
         return Boolean.parseBoolean(value);
      }
      return null;
   }

   public static Integer convertToInteger( String value ) {
      if( value != null ) {
         try {
            return Integer.parseInt(value);
         }
         catch( NumberFormatException exc ) {
            if( Mdmi.INSTANCE != null && Mdmi.INSTANCE.logger() != null )
               Mdmi.INSTANCE.logger().warning(XMIReaderUtil.class.getName() + ": Unable to parse '" + value + "' as integer.");
         }
      }
      return null;
   }

   public static SemanticElementType convertToElementType( String value ) {
      if( value != null ) {
         try {
            return SemanticElementType.valueOf(value);
         }
         catch( IllegalArgumentException exc ) {
            System.out.println("Unable to parse SemanticElementType '" + value + "'.");
            if( Mdmi.INSTANCE != null && Mdmi.INSTANCE.logger() != null )
               Mdmi.INSTANCE.logger().warning(XMIReaderUtil.class.getName() + ": Unable to parse SemanticElementType '" + value + "'.");
         }
      }
      return null;
   }

   public static Attribute createAssociation( ClassDef def, String name, String type ) {
      Attribute attrib = CSVReaderUtil.addAssociationAttributeToClass(def);
      attrib.setName(name);
      attrib.setType(type);
      return attrib;
   }

   public static Attribute addAttributeToClass( ClassDef def ) {
      Attribute attrib = new Attribute();
      def.addAttribute(attrib);
      DefaultValue val = new DefaultValue();
      attrib.setDefaultValue(val);
      return attrib;
   }

   public static Attribute addAssociationAttributeToClass( ClassDef def ) {
      Attribute attrib = new Attribute();
      attrib.setAssociation(s_assocInd);
      def.addAttribute(attrib);
      return attrib;
   }

   public static ClassDef createClass( String id, String name, RawRoot root ) {
      // Create class definition.
      ClassDef def = new ClassDef();
      def.setId(id);
      def.setName(name);
      // Add class definition to root.
      root.getModel().addClassDef(def);
      return def;
   }
}
