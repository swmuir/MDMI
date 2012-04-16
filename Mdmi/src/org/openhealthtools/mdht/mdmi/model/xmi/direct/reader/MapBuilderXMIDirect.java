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

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import javax.xml.stream.*;

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.raw.*;
import org.openhealthtools.mdht.mdmi.model.validate.*;
import org.openhealthtools.mdht.mdmi.model.xmi.direct.reader.assoc.*;

public class MapBuilderXMIDirect {
   private static final XMIRootReaderDirect           s_reader            = new XMIRootReaderDirect();
   private static final SemanticElementSetAssocReader s_elemSetReader     = new SemanticElementSetAssocReader();
   private static final SemanticElementAssocReader    s_elemReader        = new SemanticElementAssocReader();
   private static final SemanticElemRelateAssocReader s_relateReader      = new SemanticElemRelateAssocReader();
   private static final ToBusinessElemAssocReader     s_busElemReader     = new ToBusinessElemAssocReader();
   private static final ToSemanticElemAssocReader     s_semElemReader     = new ToSemanticElemAssocReader();
   private static final FieldAssocReader              s_fieldReader       = new FieldAssocReader();
   private static final DerivedDatatypeAssocReader    s_derivedTypeReader = new DerivedDatatypeAssocReader();
   private static final DataRuleAssocReader           s_dataRuleReader    = new DataRuleAssocReader();
   private static final MdmiBerAssocReader            s_busElemRefReader  = new MdmiBerAssocReader();

   public static List<MessageGroup> build( String filePath, ModelValidationResults valResults ) {
      return build(new File(filePath), valResults);
   }

   public static List<MessageGroup> build( File file, ModelValidationResults valResults ) {
      BufferedInputStream bufStream = null;
      try {
         FileInputStream fileStream = new FileInputStream(file);
         bufStream = new BufferedInputStream(fileStream);
         return build(bufStream, valResults);
      }
      catch( FileNotFoundException exc ) {
         throw new MdmiException(exc, "MapBuilder: file {0} not found!", file.getAbsolutePath());
      }
      finally {
         try {
            if( bufStream != null ) {
               bufStream.close();
            }
         }
         catch( IOException e ) {
         }
      }
   }

   public static List<MessageGroup> build( InputStream inputStream, ModelValidationResults valResults ) {
      XMLStreamReader reader = null;
      List<MessageGroup> groups = null;
      try {
         RawRoot root = new RawRoot();
         root.setModel(new ModelRoot());
         HashMap<String, Object> map = new HashMap<String, Object>();

         // Create stream reader.
         XMLInputFactory factory = XMLInputFactory.newInstance();
         XMLStreamReader tempReader = factory.createXMLStreamReader(inputStream);

         // Wrap reader in a proxy that will keep track of depth.
         ParsingInfo info = new ParsingInfo();
         XMLStreamReaderProxy readerProxy = new XMLStreamReaderProxy(tempReader, info);
         reader = (XMLStreamReader)Proxy.newProxyInstance(XMLStreamReader.class.getClassLoader(),
               new Class[] { XMLStreamReader.class }, readerProxy);

         while( reader.hasNext() ) {
            if( reader.next() == XMLStreamConstants.START_ELEMENT ) {
               groups = s_reader.readAndBuild(reader, info, root, map);
            }
         }
         createAssociations(root, map);
      }
      catch( XMLStreamException exc ) {
         throw new MdmiException(exc, "MapBuilder: XML parser error");
      }
      finally {
         try {
            if( reader != null ) {
               reader.close();
            }
         }
         catch( XMLStreamException exc ) {
         }
      }
      return groups;
   }

   @SuppressWarnings( "unchecked" )
   private static void createAssociations( RawRoot root, HashMap<String, Object> map ) {
      // Iterate over all class defs in the metadata root.
      // We only have class definitions for those objects which have non-composition associations.
      for( ClassDef srcDef : root.getModel().getClassDefs() ) {
         Object srcObject = map.get(srcDef.getId());
         @SuppressWarnings( "rawtypes" )
         IXMIReaderAssoc reader = getAssocReader(srcObject);
         if( reader != null ) {
            // We have a reader for the current object. Process all attributes as associations.
            for( Attribute attrib : srcDef.getAttributes() ) {
               Object targetObj = map.get(attrib.getType());
               if( targetObj == null ) {
            	   String warning =  MapBuilderXMIDirect.class.getName() + ": Unable to reconcile reference id '" + attrib.getType()
        				   + "' on object '" + srcObject.getClass().getName() + "' with " + " id '" + srcDef.getId()
        				   + "'.";
            	   if (Mdmi.INSTANCE.logger() != null) {
            		   Mdmi.INSTANCE.logger().warning(warning);
            	   } else {
            		   System.err.println(warning);
            	   }
               }
               else {
                  reader.processAssociation(srcObject, targetObj, attrib.getName());
               }
            }
         }
      }
   }

   @SuppressWarnings( { "rawtypes" } )
   private static IXMIReaderAssoc getAssocReader( Object obj ) {
      if( obj instanceof SemanticElementSet ) {
         return s_elemSetReader;
      }
      else if( obj instanceof SemanticElement ) {
         return s_elemReader;
      }
      else if( obj instanceof SemanticElementRelationship ) {
         return s_relateReader;
      }
      else if( obj instanceof ToBusinessElement ) {
         return s_busElemReader;
      }
      else if( obj instanceof ToMessageElement ) {
         return s_semElemReader;
      }
      else if( obj instanceof Field ) {
         return s_fieldReader;
      }
      else if( obj instanceof DTSDerived ) {
         return s_derivedTypeReader;
      }
      else if( obj instanceof DataRule ) {
         return s_dataRuleReader;
      }
      else if( obj instanceof MdmiBusinessElementReference ) {
         return s_busElemRefReader;
      }
      return null;
   }
}
