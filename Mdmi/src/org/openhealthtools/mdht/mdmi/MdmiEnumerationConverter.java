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
package org.openhealthtools.mdht.mdmi;

import java.io.*;
import java.util.*;

import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Default implementation of the IEnumerationConverter interface, based on an XML file map.
 */
public class MdmiEnumerationConverter implements IEnumerationConverter {
   public final static String FILE_EXTENSION = ".conversion.xml";
   
   private ArrayList<EnumMap> maps = new ArrayList<EnumMap>();
   
   public MdmiEnumerationConverter() {
   }
   
   public MdmiEnumerationConverter( File file ) {
      loadFromFile(file);
   }

   @Override
   public boolean canConvert( DTSEnumerated source, DTSEnumerated target ) {
      if( source == null )
         throw new IllegalArgumentException("source");
      if( target == null )
         throw new IllegalArgumentException("target");
      for( int i = 0; i < maps.size(); i++ ) {
         EnumMap m = maps.get(i);
         if( (m.sourceEnum.equalsIgnoreCase(source.getName()) && m.targetEnum.equalsIgnoreCase(target.getName()))
             || (m.sourceEnum.equalsIgnoreCase(target.getName()) && m.targetEnum.equalsIgnoreCase(source.getName())) ) {
            return true;
         }
      }
      return false;
   }

   @Override
   public EnumerationLiteral convert( DTSEnumerated source, DTSEnumerated target, EnumerationLiteral value ) {
      if( source == null )
         throw new IllegalArgumentException("source");
      if( target == null )
         throw new IllegalArgumentException("target");
      if( value == null )
         throw new IllegalArgumentException("value");
      for( int i = 0; i < maps.size(); i++ ) {
         EnumMap m = maps.get(i);
         if( m.sourceEnum.equalsIgnoreCase(source.getName()) && m.targetEnum.equalsIgnoreCase(target.getName()) ) {
            return getLiteral(target, m.getSourceMap(value.getName()));
         }
         else if( m.sourceEnum.equalsIgnoreCase(target.getName()) && m.targetEnum.equalsIgnoreCase(source.getName()) ) {
            return getLiteral(source, m.getTargetMap(value.getName()));
         }
      }
      return null;
   }
   
   public void saveToFile( File file ) {
      if( file == null )
         throw new IllegalArgumentException("Null file argument!");
      if( file.exists() && !file.isFile() )
         throw new IllegalArgumentException("Invalid file argument: " + file.getAbsolutePath());
      try {
         XmlParser p = new XmlParser();
         Document doc = p.newDocument();
         Element root = doc.createElement("conversions");
         doc.appendChild(root);
         for( int i = 0; i < maps.size(); i++ ) {
            EnumMap m = maps.get(i);
            Element e = XmlUtil.addElement(root, "emap");
            m.toXml(e);
         }
         XmlWriter w = new XmlWriter(file.getAbsolutePath());
         w.write(doc);
         w = null;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Cannot save enumeration conversion file " + file.getAbsolutePath());
      }
   }
   
   public void loadFromFile( File file ) {
      if( file == null || !file.exists() || !file.isFile() )
         throw new IllegalArgumentException("Invalid or null file argument!");
      maps.clear();
      try {
         XmlParser p = new XmlParser();
         Document doc = p.parse(file);
         Element root = doc.getDocumentElement();
         ArrayList<Element> elems = XmlUtil.getElements(root, "emap");
         if( elems == null || elems.size() <= 0 )
            return;
         for( int i = 0; i < elems.size(); i++ ) {
            Element e = elems.get(i);
            maps.add(EnumMap.fromXml(e));
         }
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Cannot load enumeration conversion file " + file.getAbsolutePath());
      }
   }

   public EnumMap getMap( DTSEnumerated source, DTSEnumerated target ) {
      if( source != null && target != null ) {
         for( int i = 0; i < maps.size(); i++ ) {
            EnumMap m = maps.get(i);
            if( (m.sourceEnum.equalsIgnoreCase(source.getName()) && m.targetEnum.equalsIgnoreCase(target.getName()))
                || (m.sourceEnum.equalsIgnoreCase(target.getName()) && m.targetEnum.equalsIgnoreCase(source.getName())) ) {
               return m;
            }
         }
	  }
      return null;
   }

   public ArrayList<EnumMap> getMaps( DTSEnumerated type ) {
      ArrayList<EnumMap> a = new ArrayList<EnumMap>();
      if( type != null ) {
         for( int i = 0; i < maps.size(); i++ ) {
            EnumMap m = maps.get(i);
            if( m.sourceEnum.equalsIgnoreCase(type.getName()) && m.targetEnum.equalsIgnoreCase(type.getName()) )
               a.add(m);
         }
      }
      return a;
   }
   
   public EnumMap add( DTSEnumerated source, DTSEnumerated target ) {
      EnumMap m = getMap(source, target);
      if( m != null )
         return m;
      m = new EnumMap(source.getName(), target.getName());
      maps.add(m);
      return m;
   }

   public void add( DTSEnumerated source, DTSEnumerated target, 
         EnumerationLiteral sourceLiteral, EnumerationLiteral targetLiteral ) 
   {
      EnumMap emap = null;
      for( int i = 0; i < maps.size() && emap == null; i++ ) {
         EnumMap m = maps.get(i);
         if( (m.sourceEnum.equalsIgnoreCase(source.getName()) && m.targetEnum.equalsIgnoreCase(target.getName()))
             || (m.sourceEnum.equalsIgnoreCase(target.getName()) && m.targetEnum.equalsIgnoreCase(source.getName())) ) {
            emap = m;
         }
      }
      if( emap == null ) {
         emap = new EnumMap(source.getName(), target.getName());
         maps.add(emap);
      }
      emap.add(sourceLiteral.getName(), targetLiteral.getName());
   }
   
   public void remove( DTSEnumerated source, DTSEnumerated target ) {
      for( int i = 0; i < maps.size(); i++ ) {
         EnumMap m = maps.get(i);
         if( (m.sourceEnum.equalsIgnoreCase(source.getName()) && m.targetEnum.equalsIgnoreCase(target.getName()))
             || (m.sourceEnum.equalsIgnoreCase(target.getName()) && m.targetEnum.equalsIgnoreCase(source.getName())) ) {
            maps.remove(i);
            return;
         }
      }
   }
   
   public void remove( DTSEnumerated source, DTSEnumerated target, 
         EnumerationLiteral sourceLiteral, EnumerationLiteral targetLiteral )
   {
      EnumMap emap = null;
      for( int i = 0; i < maps.size() && emap == null; i++ ) {
         EnumMap m = maps.get(i);
         if( (m.sourceEnum.equalsIgnoreCase(source.getName()) && m.targetEnum.equalsIgnoreCase(target.getName()))
             || (m.sourceEnum.equalsIgnoreCase(target.getName()) && m.targetEnum.equalsIgnoreCase(source.getName())) ) {
            emap = m;
         }
      }
      if( emap == null )
         return;
      emap.remove(sourceLiteral.getName(), targetLiteral.getName());
   }

   public void removeAll() {
      maps = new ArrayList<EnumMap>(); 
   }
   
   private EnumerationLiteral getLiteral( DTSEnumerated e, String lit ) {
      return e.getLiteralByName(lit);
   }
   
   public static class EnumMap {
      private String sourceEnum;
      private String targetEnum;
      private ArrayList<LiteralMap> map = new ArrayList<LiteralMap>();
      
      public EnumMap() {
      }
      
      public EnumMap( String source, String target ) {
         if( source == null || source.length() <= 0 )
            throw new IllegalArgumentException("source");
         if( target == null || target.length() <= 0 )
            throw new IllegalArgumentException("target");
         sourceEnum = source;
         targetEnum = target;
      }
      
      public String getSourceEnum() {
         return sourceEnum;
      }

      public void setSourceEnum( String sourceEnum ) {
         this.sourceEnum = sourceEnum;
      }

      public String getTargetEnum() {
         return targetEnum;
      }

      public void setTargetEnum( String targetEnum ) {
         this.targetEnum = targetEnum;
      }

      public ArrayList<LiteralMap> getMap() {
         return map;
      }

      public void setMap( ArrayList<LiteralMap> map ) {
         this.map = map;
      }

      void add( String src, String trg ) {
         for( int i = 0; i < map.size(); i++ ) {
            LiteralMap m = map.get(i);
            if( m.sourceLiteral.equalsIgnoreCase(src) && m.targetLiteral.equalsIgnoreCase(trg) )
               return;
         }
         map.add(new LiteralMap(src, trg));
      }
      
      void remove( String src, String trg ) {
         for( int i = 0; i < map.size(); i++ ) {
            LiteralMap m = map.get(i);
            if( m.sourceLiteral.equalsIgnoreCase(src) && m.targetLiteral.equalsIgnoreCase(trg) ) {
               map.remove(i);
               return;
            }
         }
      }
      
      String getSourceMap( String src ) {
         if( src != null && 0 < src.length() ) {
            for( int i = 0; i < map.size(); i++ ) {
               LiteralMap m = map.get(i);
               if( m.sourceLiteral.equalsIgnoreCase(src) )
                  return m.targetLiteral;
            }
         }
         return null;
      }
      
      String getTargetMap( String trg ) {
         if( trg != null && 0 < trg.length() ) {
            for( int i = 0; i < map.size(); i++ ) {
               LiteralMap m = map.get(i);
               if( m.targetLiteral.equalsIgnoreCase(trg) )
                  return m.sourceLiteral;
            }
         }
         return null;
      }
      
      void toXml( Element root ) {
         root.setAttribute("sourceEnum", sourceEnum);
         root.setAttribute("targetEnum", targetEnum);
         for( int i = 0; i < map.size(); i++ ) {
            Element e = XmlUtil.addElement(root, "lmap");
            map.get(i).toXml(e);
         }
      }
      
      static EnumMap fromXml( Element root ) {
         String s = root.getAttribute("sourceEnum");
         String t = root.getAttribute("targetEnum");
         EnumMap emap = new EnumMap(s, t);
         ArrayList<Element> elems = XmlUtil.getElements(root, "lmap");
         for( int i = 0; i < elems.size(); i++ ) {
            Element e = elems.get(i);
            emap.map.add(LiteralMap.fromXml(e));
         }
         return emap;
      }
   }
   
   // a literal conversion map from source to target
   // either can be null
   public static class LiteralMap {
      private String sourceLiteral;
      private String targetLiteral;

      public LiteralMap() {
      }
      
      public LiteralMap( String source, String target ) {
         sourceLiteral = source == null || source.length() <= 0 ? null : source.trim();
         targetLiteral = target == null || target.length() <= 0 ? null : target.trim();
      }
      
      public String getSourceLiteral() {
         return sourceLiteral;
      }

      public void setSourceLiteral( String sourceLiteral ) {
         this.sourceLiteral = sourceLiteral;
      }

      public String getTargetLiteral() {
         return targetLiteral;
      }

      public void setTargetLiteral( String targetLiteral ) {
         this.targetLiteral = targetLiteral;
      }

      void toXml( Element root ) {
         if( sourceLiteral != null && 0 < sourceLiteral.length() )
            root.setAttribute("sourceLiteral", sourceLiteral);
         if( targetLiteral != null && 0 < targetLiteral.length() )
            root.setAttribute("targetLiteral", targetLiteral);
      }
      
      static LiteralMap fromXml( Element root ) {
         String s = root.getAttribute("sourceLiteral");
         String t = root.getAttribute("targetLiteral");
         return new LiteralMap(s, t);
      }
   }
} // MdmiEnumerationConverter
