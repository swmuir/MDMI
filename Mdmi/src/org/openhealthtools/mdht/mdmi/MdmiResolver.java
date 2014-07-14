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

import org.openhealthtools.mdht.mdmi.engine.*;
import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.model.validate.*;
import org.openhealthtools.mdht.mdmi.model.xmi.direct.reader.*;
import org.openhealthtools.mdht.mdmi.util.*;

/**
 * Resolver for MDMI maps, syntax and semantic parsers. It loads and parses the maps, and the parsers. This
 * implementation caches all message groups.
 */
public class MdmiResolver {
   protected static final ISyntacticParser DEFAULT_SYN_PARSER = new org.openhealthtools.mdht.mdmi.engine.xml.DOMSAXSyntacticParser();
   protected static final ISemanticParser  DEFAULT_SEM_PARSER = new DefaultSemanticParser();

   protected HashMap<String, MI>           m_maps             = new HashMap<String, MI>();

   /**
    * Create a new instance and resolve the MDMI maps specified in the configuration file.
    * 
    * @param config Configuration data, used to locate the MDMI maps and providers.
    */
   public MdmiResolver( MdmiConfig config ) {
      resolveConfig(config);
   }

   /**
    * Resolve (load and parse) the MDMI maps specified in the configuration file.
    * 
    * @param config Configuration data, used to locate the MDMI maps and providers.
    */
   public void resolveConfig( MdmiConfig config ) {
      Collection<MdmiConfig.MapInfo> mes = config.getAllMapInfos();
      for( Iterator<MdmiConfig.MapInfo> iterator = mes.iterator(); iterator.hasNext(); ) {
         MdmiConfig.MapInfo me = iterator.next();
         resolveOne(me);
      }
   }

   /**
    * Resolve (load and parse) the specified MDMI map.
    * 
    * @param mapInfo The map info structure containing map location information.
    * @return The message groups in the given map, if it is valid.
    */
   public Collection<MessageGroup> resolveOne( MdmiConfig.MapInfo mapInfo ) {
      File mf = new File(mapInfo.mapFileName);
      if( !mf.exists() )
         mf = Mdmi.INSTANCE.fileFromRelPath(mapInfo.mapFileName);
      System.out.println("Loading map file " + mf.getAbsolutePath());

      ModelValidationResults r = new ModelValidationResults();
      List<MessageGroup> mgs = null;
      if( mapInfo.mapBuilderClassName == null || mapInfo.mapBuilderClassName.equals(MapBuilder.class.getName()) )
         mgs = MapBuilder.build(mf, r);
      else if( mapInfo.mapBuilderClassName.equals(MapBuilderCSV.class.getName()) )
         mgs = MapBuilderCSV.build(mf, r);
      else if( mapInfo.mapBuilderClassName.equals(MapBuilderXMIDirect.class.getName()) )
         mgs = MapBuilderXMIDirect.build(mf, r);
      else
         throw new MdmiException("Unknown map type: " + mapInfo.mapBuilderClassName);
      if( r.getErrors().size() > 0 )
         handleErrors(mf, r.getErrors());
      if( r.getWarnings().size() > 0 )
         handleWarnings(mf, r.getWarnings());
      
      // load value set handler from file (if one exists)
      MdmiValueSetsHandler vsh = null;
      String mfn = mf.getAbsolutePath().trim().toLowerCase();
      if( 0 < mfn.lastIndexOf(".xmi") ) {
         mfn = mf.getAbsolutePath().trim().substring(0, mfn.lastIndexOf(".xmi"));
         mfn += MdmiValueSetsHandler.FILE_EXTENSION;
         File ecf = new File(mfn);
         vsh = new MdmiValueSetsHandler(ecf);
      }
      
      for( MessageGroup mg : mgs ) {
         MI mi = new MI(mapInfo, mg);
         mi.valueSetsHandler = vsh;
         System.out.println("Loaded message group " + mg.getName());
         if( m_maps.containsKey(mi.messageGroup.getName()) )
            m_maps.remove(mi.messageGroup.getName());
         m_maps.put(mi.messageGroup.getName(), mi);
      }
      return mgs;
   }

   /**
    * Get the requested message model by group and message name.
    * 
    * @param messageGroup The message group name.
    * @param messageModel The message model name.
    * @return The model if found, null otherwise.
    */
   public MessageModel getModel( String messageGroup, String messageModel ) {
      if( messageGroup == null || messageModel == null )
         throw new IllegalArgumentException("Null argument");
      MI mi = m_maps.get(messageGroup);
      if( mi == null )
         return null;
      return mi.messageGroup.getModel(messageModel);
   }

   /**
    * Get the requested message group by group name.
    * 
    * @param messageGroup The message group name.
    * @return The message group if found, null otherwise.
    */
   public MessageGroup getMessageGroup( String messageGroup ) {
      if( messageGroup == null )
         throw new IllegalArgumentException("Null argument");
      MI mi = m_maps.get(messageGroup);
      if( mi == null )
         return null;
      return mi.messageGroup;
   }

   /**
    * Get the message group for the specified MdmiValueSetsHandler.
    * 
    * @param vsh The value set handler.
    * @return The message group if found, null otherwise.
    */
   public MessageGroup getMessageGroup( MdmiValueSetsHandler vsh ) {
      if( vsh == null )
         throw new IllegalArgumentException("Null argument");
      Collection<MI> c = m_maps.values();
      for( Iterator<MI> it = c.iterator(); it.hasNext(); ) {
         MI mi = it.next();
         if( mi.valueSetsHandler == vsh )
         	return mi.messageGroup;
      }
      return null;
   }

   /**
    * Get a list of all known message groups.
    * 
    * @return A list of all known message groups.
    */
   public Collection<MessageGroup> getMessageGroups() {
      ArrayList<MessageGroup> a = new ArrayList<MessageGroup>();
      Collection<MI> c = m_maps.values();
      for( Iterator<MI> it = c.iterator(); it.hasNext(); ) {
         MI mi = it.next();
         a.add(mi.messageGroup);
      }
      return a;
   }

   /**
    * Get the syntax parser for the specified message group.
    * 
    * @param messageGroup The message group name to look for.
    * @return The syntax parser for the specified group, or null if the group is not found.
    */
   public ISyntacticParser getSyntacticParser( String messageGroup ) {
      if( messageGroup == null )
         throw new IllegalArgumentException("Null argument");
      MI mi = m_maps.get(messageGroup);
      if( mi == null )
         return null;
      return mi.getSyntaxParser();
   }
   
   /**
    * Get the value sets handler for the specified message group.
    * 
    * @param messageGroup The message group name to look for.
    * @return The enumeration converter for the specified message group, or null if the group is not found.
    */
   public MdmiValueSetsHandler getValueSetsHandler( String messageGroup ) {
      if( messageGroup == null )
         throw new IllegalArgumentException("Null argument");
      MI mi = m_maps.get(messageGroup);
      if( mi == null )
         return null;
      return mi.valueSetsHandler;
   }

   /**
    * Get the semantic parser for the specified message group.
    * 
    * @param messageGroup The message group name to look for.
    * @return The semantic parser for the specified group, or null if the group is not found.
    */
   public ISemanticParser getSemanticParser( String messageGroup ) {
      if( messageGroup == null )
         throw new IllegalArgumentException("Null argument");
      MI mi = m_maps.get(messageGroup);
      if( mi == null )
         return null;
      return mi.getSemanticParser();
   }

   private void handleErrors( File f, List<ModelInfo> errors ) {
      StringBuffer sb = new StringBuffer();
      for( int i = 0; i < errors.size(); i++ ) {
         ModelInfo mi = errors.get(i);
         sb.append(mi.getMessage());
         sb.append("\n");
      }
      // TODO for now log the exceptions, we should throw on invalid maps!
      Mdmi.INSTANCE.logger().severe("MDMI map in file {0} has errors: {1}", f.getAbsolutePath(), sb.toString());
   }

   private void handleWarnings( File f, List<ModelInfo> errors ) {
      StringBuffer sb = new StringBuffer();
      for( int i = 0; i < errors.size(); i++ ) {
         ModelInfo mi = errors.get(i);
         sb.append(mi.getMessage());
         sb.append("\n");
      }
      Mdmi.INSTANCE.logger().severe("MDMI map in file {0} has warnings: {1}", f.getAbsolutePath(), sb.toString());
   }

   private static final class MI {
      MdmiConfig.MapInfo    mapInfo;
      MessageGroup          messageGroup;
      ISyntacticParser      syntacticSvcProvider;
      ISemanticParser       semanticSvcProvider;
      MdmiValueSetsHandler  valueSetsHandler;  

      public MI( MdmiConfig.MapInfo mapInfo, MessageGroup messageGroup ) {
         this.mapInfo = mapInfo;
         this.messageGroup = messageGroup;
      }

      ISyntacticParser getSyntaxParser() {
         if( syntacticSvcProvider == null ) {
            String cn = mapInfo.synSvcClassName;
            if( cn == null ) {
               syntacticSvcProvider = DEFAULT_SYN_PARSER;
            }
            else {
               File fn = mapInfo.synSvcJarName == null ? null : new File(mapInfo.synSvcJarName);
               try {
                  syntacticSvcProvider = Util.getInstance(cn, fn, null, null);
               }
               catch( Exception ex ) {
                  throw new MdmiException(ex, "Loading syntactic parser {0} from {1} failed!", cn, fn.getAbsolutePath());
               }
            }
         }
         return syntacticSvcProvider;
      }

      ISemanticParser getSemanticParser() {
         if( semanticSvcProvider == null ) {
            String cn = mapInfo.semSvcClassName;
            if( cn == null ) {
               semanticSvcProvider = DEFAULT_SEM_PARSER;
            }
            else {
               File fn = mapInfo.semSvcJarName == null ? null : new File(mapInfo.semSvcJarName);
               try {
                  semanticSvcProvider = Util.getInstance(cn, fn, null, null);
               }
               catch( Exception ex ) {
                  throw new MdmiException(ex, "Loading semantic parser {0} from {1} failed!", cn, fn.getAbsolutePath());
               }
            }
         }
         return semanticSvcProvider;
      }
   } // MdmiMapResolver$MT
} // MdmiMapResolver
