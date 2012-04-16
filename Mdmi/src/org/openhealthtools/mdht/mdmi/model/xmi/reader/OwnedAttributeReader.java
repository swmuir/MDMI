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

public class OwnedAttributeReader extends XMIReaderAbstract<Attribute> implements IXMINodeReader<Attribute> {
   private static final String             s_nodeName        = "ownedAttribute";
   private static final String             s_attribNamespace = "http://schema.omg.org/spec/XMI/2.1";

   // Attribute names
   private static final String             s_idAtt           = "id";
   private static final String             s_nameAtt         = "name";
   private static final String             s_assocAtt        = "association";
   private static final String             s_aggrAtt         = "aggregation";
   private static final String             s_typeAtt         = "type";

   // Child readers
   private static final DefaultValueReader s_defValueReader  = new DefaultValueReader();
   private static final OwnedCommentReader s_commentReader   = new OwnedCommentReader();
   private static final UpperValueReader   s_upperValReader  = new UpperValueReader();
   private static final LowerValueReader   s_lowerValReader  = new LowerValueReader();

   @Override
   protected Attribute createObject() {
      return new Attribute();
   }

   @Override
   protected String getNodeName() {
      return s_nodeName;
   }

   @Override
   protected void readAttributes( XMLStreamReader reader, Attribute object ) {
      object.setId(reader.getAttributeValue(s_attribNamespace, s_idAtt));
      object.setName(reader.getAttributeValue(null, s_nameAtt));
      object.setAssociation(reader.getAttributeValue(null, s_assocAtt));
      object.setAggregationType(reader.getAttributeValue(null, s_aggrAtt));
      object.setType(reader.getAttributeValue(null, s_typeAtt));
   }

   @Override
   protected void readSingleChildElement( XMLStreamReader reader, Attribute object ) throws XMLStreamException {
      if( s_defValueReader.canRead(reader) ) {
         object.setDefaultValue(s_defValueReader.read(reader));
      }
      else if( s_commentReader.canRead(reader) ) {
         object.setComment(s_commentReader.read(reader));
      }
      else if( s_upperValReader.canRead(reader) ) {
         object.setUpperLimit(s_upperValReader.read(reader));
      }
      else if( s_lowerValReader.canRead(reader) ) {
         object.setLowerLimit(s_lowerValReader.read(reader));
      }
   }
}
