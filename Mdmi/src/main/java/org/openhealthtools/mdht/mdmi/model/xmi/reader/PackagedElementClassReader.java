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
package org.openhealthtools.mdht.mdmi.model.xmi.reader;

import javax.xml.stream.*;

import org.openhealthtools.mdht.mdmi.model.raw.*;

public class PackagedElementClassReader extends XMIReaderAbstract<ClassDef> implements IXMINodeReader<ClassDef> {
   private static final String               s_nodeName        = "packagedElement";
   private static final String               s_attribNamespace = "http://schema.omg.org/spec/XMI/2.1";

   // Attribute names
   private static final String               s_idAtt           = "id";
   private static final String               s_typeAtt         = "type";
   private static final String               s_nameAtt         = "name";

   // Attribute values
   private static final String               s_typeValueClass  = "uml:Class";
   private static final String               s_typeValueEnum   = "uml:Enumeration";

   // Child readers
   private static final OwnedAttributeReader m_attribReader    = new OwnedAttributeReader();
   private static final OwnedLiteralReader   m_literalReader   = new OwnedLiteralReader();
   private static final OwnedCommentReader   m_commentReader   = new OwnedCommentReader();

   @Override
   protected ClassDef createObject() {
      return new ClassDef();
   }

   @Override
   public boolean canRead( XMLStreamReader reader ) {
      return super.canRead(reader) && isClassType(reader);
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, ClassDef classDef ) {
      // We want to hold onto the name and id attribs
      classDef.setId(reader.getAttributeValue(s_attribNamespace, s_idAtt));
      classDef.setName(reader.getAttributeValue(null, s_nameAtt));
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, ClassDef def ) throws XMLStreamException {

      if( m_attribReader.canRead(reader) ) {
         def.addAttribute(m_attribReader.read(reader));
      }
      else if( m_literalReader.canRead(reader) ) {
         def.addLiteral(m_literalReader.read(reader));
      }
      else if( m_commentReader.canRead(reader) ) {
         def.setComment(m_commentReader.read(reader));
      }
   }

   @Override
   public String getNodeName() {
      return s_nodeName;
   }

   private boolean isClassType( XMLStreamReader reader ) {
      String type = reader.getAttributeValue(s_attribNamespace, s_typeAtt);
      return s_typeValueClass.equals(type) || s_typeValueEnum.equals(type);
   }
}
