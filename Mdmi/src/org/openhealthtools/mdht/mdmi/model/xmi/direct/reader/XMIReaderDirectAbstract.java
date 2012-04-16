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
package org.openhealthtools.mdht.mdmi.model.xmi.direct.reader;

import java.util.*;

import javax.xml.stream.*;

import org.openhealthtools.mdht.mdmi.model.raw.*;
import org.openhealthtools.mdht.mdmi.model.xmi.*;
import org.openhealthtools.mdht.mdmi.model.xmi.direct.*;

public abstract class XMIReaderDirectAbstract<T> extends XMIReaderSimple<T> implements IDirectReader<T> {
   private String m_nodeName = null;

   protected XMIReaderDirectAbstract( String nodeName ) {
      m_nodeName = nodeName;
   }

   @Override
   protected String getNodeName() {
      return m_nodeName;
   }

   @Override
   public T readAndBuild( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root, Map<String, Object> objectMap )
         throws XMLStreamException {

      // Make sure the reader is in a valid position for us
      // to begin reading.
      validatePosition(reader);

      // Hold on to current depth so we know when we are
      // done processing our element.
      int startDepth = parsingInfo.getDepth();

      // Class definition will hold the association metadata.
      // Only create if we need it.
      ClassDef classDef = null;
      String classId = reader.getAttributeValue(XMIDirectConstants.XMI_NAMESPACE, XMIDirectConstants.XMI_ID_ATTRIB);

      // Create the object that we are building.
      T returnObject = createObject(reader);

      // Read all attributes from the current XML element.
      readAttributes(reader, returnObject);

      // Read each child element in succession.
      while( advanceToNextChildElement(reader, parsingInfo, startDepth) ) {
         // If the child is a reference, we have enough
         // information to handle it here.
         String refId = reader
               .getAttributeValue(XMIDirectConstants.XMI_NAMESPACE, XMIDirectConstants.XMI_ID_REF_ATTRIB);

         if( refId != null && refId.length() > 0 ) {
            // Create class definition if we don't already
            // have one.
            if( classDef == null ) {
               classDef = XMIReaderUtil.createClass(classId, getObjectName(returnObject), root);
            }

            XMIReaderUtil.createAssociation(classDef, reader.getLocalName(), refId);
         }
         else {
            // Child is not a reference, so it is composition.
            readSingleChildElement(reader, parsingInfo, root, objectMap, returnObject);
         }
      }
      objectMap.put(classId, returnObject);
      return returnObject;
   }

   /**
    * Called when the reader is position at the element start. Called after readAttributes.
    */
   protected abstract void readSingleChildElement( XMLStreamReader reader, ParsingInfo parsingInfo, RawRoot root,
         Map<String, Object> objectMap, T object ) throws XMLStreamException;

   protected abstract String getObjectName( T object );
}
