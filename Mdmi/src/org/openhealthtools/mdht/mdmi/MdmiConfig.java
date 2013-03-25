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
import java.util.logging.*;
import org.w3c.dom.*;
import org.openhealthtools.mdht.mdmi.util.*;

/**
 * Configuration file for the MDMI. Holds information about the maps, resolvers and processors used by the runtime.
 * 
 * @author goancea
 */
public final class MdmiConfig {
   static final String                           FILE_NAME            = "mdmi.config";
   static final String                           TAG_NAME             = "mdmiConfig";
   static final LogInfo                          DEFAULT_LOG_INFO     = new LogInfo(Level.SEVERE, "./logs", true, false);

   private int                                   m_threadPoolSize     = -1;
   private LogInfo                               m_logInfo            = DEFAULT_LOG_INFO;
   private HashMap<String, MapInfo>              m_mapInfos           = new HashMap<String, MapInfo>();
   private HashMap<String, ExternalResolverInfo> m_resolverInfos      = new HashMap<String, ExternalResolverInfo>();
   private HashMap<String, PreProcessorInfo>     m_preProcessorInfos  = new HashMap<String, PreProcessorInfo>();
   private HashMap<String, PostProcessorInfo>    m_postProcessorInfos = new HashMap<String, PostProcessorInfo>();

   /**
    * Default ctor.
    */
   public MdmiConfig() {
   }

   /**
    * The number of threads in the pool.
    * 
    * @return The number of threads in the pool.
    */
   public int getThreadPoolSize() {
      return m_threadPoolSize;
   }

   /**
    * Set the number of threads in the pool.
    * 
    * @param threadPoolSize The new number of threads in the pool.
    */
   void setThreadPoolSize( int threadPoolSize ) {
      m_threadPoolSize = threadPoolSize;
   }

   /**
    * Get the logging information for this runtime.
    * 
    * @return The logging information for this runtime.
    */
   public LogInfo getLogInfo() {
      return m_logInfo;
   }

   /**
    * Set the logging information about this runtime.
    * 
    * @param logInfo The logging information about this runtime.
    */
   public void setLogInfo( LogInfo logInfo ) {
      m_logInfo = logInfo == null ? DEFAULT_LOG_INFO : logInfo;
   }

   /**
    * Get all loaded MapInfos.
    * 
    * @return All loaded MapInfos.
    */
   public Collection<MapInfo> getAllMapInfos() {
      return m_mapInfos.values();
   }

   /**
    * Get the requested MapInfo, by name.
    * 
    * @param mapName The map name.
    * @return The MapInfo instance if found, null otherwise.
    */
   public MapInfo getMapInfo( String mapName ) {
      return m_mapInfos.get(mapName);
   }

   /**
    * Get the requested MapInfo by file name.
    * 
    * @param mapFileName The file name of the map.
    * @return The MapInfo instance if found, null otherwise.
    */
   public MapInfo getMapInfoByFileName( String mapFileName ) {
      for( MapInfo me : m_mapInfos.values() ) {
         if( me.mapFileName.equals(mapFileName) )
            return me;
      }
      return null;
   }

   /**
    * Add or replace the given MapInfo.
    * 
    * @param me The MapInfo to add or replace.
    */
   public void putMapInfo( MapInfo me ) {
      m_mapInfos.put(me.mapName, me);
   }

   /**
    * Remove the specified MapInfo by name.
    * 
    * @param mapName The name of the map to remove.
    */
   public void removeMapInfo( String mapName ) {
      m_mapInfos.remove(mapName);
   }

   /**
    * Get all resolver infos registered with the runtime.
    * 
    * @return All resolver infos registered with the runtime.
    */
   public Collection<ExternalResolverInfo> getAllResolverInfos() {
      return m_resolverInfos.values();
   }

   /**
    * Get the requested resolver info by name.
    * 
    * @param providerName The resolver name.
    * @return The ExternalResolverInfo instance, if found, null otherwise.
    */
   public ExternalResolverInfo getResolverInfo( String providerName ) {
      return m_resolverInfos.get(providerName);
   }

   /**
    * Add or update the given resolver info.
    * 
    * @param me The instance to add or update.
    */
   public void putResolverInfo( ExternalResolverInfo me ) {
      m_resolverInfos.put(me.providerName, me);
   }

   /**
    * Remove the resolver by name.
    * 
    * @param providerName The name of the resolver to remove.
    */
   public void removeResolverInfo( String providerName ) {
      m_resolverInfos.remove(providerName);
   }

   /**
    * Get all the pre-processor infos registered with this runtime.
    * 
    * @return All the pre-processor infos registered with this runtime.
    */
   public Collection<PreProcessorInfo> getAllPreProcessorInfos() {
      return m_preProcessorInfos.values();
   }

   /**
    * Get a pre-processor info instance by name.
    * 
    * @param providerName The name to look for.
    * @return The pre-processor info instance if found, null otherwise.
    */
   public PreProcessorInfo getPreProcessorInfo( String providerName ) {
      return m_preProcessorInfos.get(providerName);
   }

   /**
    * Add or update the given pre-processor info instance.
    * 
    * @param me The pre-processor info instance to add or update.
    */
   public void putPreProcessorInfo( PreProcessorInfo me ) {
      m_preProcessorInfos.put(me.providerName, me);
   }

   /**
    * Remove the specified pre-processor info instance by name.
    * 
    * @param providerName The name of the pre-processor info instance to remove.
    */
   public void removePreProcessorInfo( String providerName ) {
      m_preProcessorInfos.remove(providerName);
   }

   /**
    * Get a list of all the post-processor info instances registered with the runtime.
    * 
    * @return The list of all post-processor info instances.
    */
   public Collection<PostProcessorInfo> getAllPostProcessorInfos() {
      return m_postProcessorInfos.values();
   }

   /**
    * Get the requested post-processor info instance by name.
    * 
    * @param providerName The name to look for.
    * @return The post-processor info instance, or null if not found.
    */
   public PostProcessorInfo getPostProcessorInfo( String providerName ) {
      return m_postProcessorInfos.get(providerName);
   }

   /**
    * Add or update the given post-processor info instance.
    * 
    * @param me The post-processor info instance to add or update.
    */
   public void putPostProcessorInfo( PostProcessorInfo me ) {
      m_postProcessorInfos.put(me.providerName, me);
   }

   /**
    * Remove the specified post-processor info instance by name.
    * 
    * @param providerName The name of the post-processor info instance to remove.
    */
   public void removePostProcessorInfo( String providerName ) {
      m_postProcessorInfos.remove(providerName);
   }

   /**
    * Load this instance from the given configuration file. File must exist.
    * 
    * @param configFile The configFile from which this will be loaded.
    */
   public void load( File configFile ) {
      if( configFile == null || !configFile.exists() || !configFile.isFile() )
         throw new IllegalArgumentException("Invalid or null file argument!");
      try {
         XmlParser p = new XmlParser();
         Document doc = p.parse(configFile);
         Element root = doc.getDocumentElement();
         Element e = XmlUtil.getElement(root, "threadPoolSize");
         if( e != null ) {
            try {
               m_threadPoolSize = Integer.parseInt(XmlUtil.getText(e));
            }
            catch( Exception ignored ) {
            }
         }

         e = XmlUtil.getElement(root, LogInfo.TAG);
         if( e != null ) {
            try {
               m_logInfo = LogInfo.fromXml(e);
            }
            catch( Exception ignored ) {
            }
         }

         ArrayList<Element> es = XmlUtil.getElements(root, MapInfo.TAG);
         for( Iterator<Element> iterator = es.iterator(); iterator.hasNext(); ) {
            e = iterator.next();
            MapInfo me = MapInfo.fromXml(e);
            m_mapInfos.put(me.mapName, me);
         }

         es = XmlUtil.getElements(root, ExternalResolverInfo.TAG);
         for( Iterator<Element> iterator = es.iterator(); iterator.hasNext(); ) {
            e = iterator.next();
            ExternalResolverInfo xp = ExternalResolverInfo.fromXml(e);
            m_resolverInfos.put(xp.providerName, xp);
         }

         es = XmlUtil.getElements(root, PreProcessorInfo.TAG);
         for( Iterator<Element> iterator = es.iterator(); iterator.hasNext(); ) {
            e = iterator.next();
            PreProcessorInfo xp = PreProcessorInfo.fromXml(e);
            m_preProcessorInfos.put(xp.providerName, xp);
         }

         es = XmlUtil.getElements(root, PostProcessorInfo.TAG);
         for( Iterator<Element> iterator = es.iterator(); iterator.hasNext(); ) {
            e = iterator.next();
            PostProcessorInfo xp = PostProcessorInfo.fromXml(e);
            m_postProcessorInfos.put(xp.providerName, xp);
         }
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Cannot load configuration file " + configFile.getAbsolutePath());
      }
   }

   /**
    * Save this to the given file. If the given file exists, it will be overwritten.
    * 
    * @param configFile The file to save to.
    */
   void save( File configFile ) {
      if( configFile == null )
         throw new IllegalArgumentException("Null file argument!");
      if( configFile.exists() && !configFile.isFile() )
         throw new IllegalArgumentException("Invalid file argument: " + configFile.getAbsolutePath());
      try {
         XmlParser p = new XmlParser();
         Document doc = p.newDocument();
         Element root = doc.createElement(TAG_NAME);
         doc.appendChild(root);
         if( m_threadPoolSize >= 0 )
            XmlUtil.addElement(root, "threadPoolSize", String.valueOf(m_threadPoolSize));

         if( m_logInfo != null )
            m_logInfo.toXml(root);

         Collection<MapInfo> mapInfos = m_mapInfos.values();
         for( Iterator<MapInfo> iterator = mapInfos.iterator(); iterator.hasNext(); ) {
            MapInfo me = iterator.next();
            me.toXml(root);
         }

         Collection<ExternalResolverInfo> resolvers = m_resolverInfos.values();
         for( Iterator<ExternalResolverInfo> iterator = resolvers.iterator(); iterator.hasNext(); ) {
            ExternalResolverInfo me = iterator.next();
            me.toXml(root);
         }

         Collection<PreProcessorInfo> preProcessors = m_preProcessorInfos.values();
         for( Iterator<PreProcessorInfo> iterator = preProcessors.iterator(); iterator.hasNext(); ) {
            PreProcessorInfo me = iterator.next();
            me.toXml(root);
         }

         Collection<PostProcessorInfo> postProcessors = m_postProcessorInfos.values();
         for( Iterator<PostProcessorInfo> iterator = postProcessors.iterator(); iterator.hasNext(); ) {
            PostProcessorInfo me = iterator.next();
            me.toXml(root);
         }
         XmlWriter w = new XmlWriter(configFile.getAbsolutePath());
         w.write(doc);
         w = null;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Cannot save configuration file " + configFile.getAbsolutePath());
      }
   }

   /**
    * Logger information.
    * 
    * @author goancea
    */
   public static class LogInfo {
      private static final String TAG             = "logInfo";
      private static final String LOG_LEVEL       = "logLevel";
      private static final String LOG_FOLDER      = "logFolder";
      private static final String ECHO_TO_CONSOLE = "echoToConsole";
      private static final String LOG_THREAD_NAME = "logThreadName";

      public Level                logLevel;
      public String               logFolder;
      public boolean              echoToConsole = true;
      public boolean              logThreadName;

      /**
       * Default ctor.
       */
      public LogInfo() {
      }

      public LogInfo( Level level, String folder, boolean console, boolean thread ) {
         logLevel = level;
         logFolder = folder;
         echoToConsole = console;
         logThreadName = thread;
      }

      private void toXml( Element owner ) {
         Element root = XmlUtil.addElement(owner, TAG);
         if( logLevel == null )
            root.setAttribute(LOG_LEVEL, DEFAULT_LOG_INFO.logLevel.toString());
         else
            root.setAttribute(LOG_LEVEL, logLevel.toString());
         if( logFolder == null )
            XmlUtil.addElement(root, LOG_FOLDER, DEFAULT_LOG_INFO.logFolder);
         else
            XmlUtil.addElement(root, LOG_FOLDER, logFolder);
         if( echoToConsole )
            root.setAttribute(ECHO_TO_CONSOLE, "true");
         if( logThreadName )
            root.setAttribute(LOG_THREAD_NAME, "true");
      }

      private static LogInfo fromXml( Element root ) {
         LogInfo me = new LogInfo();
         me.logLevel = LogWriter.levelFromString(root.getAttribute(LOG_LEVEL));
         if( me.logLevel == null )
            me.logLevel = DEFAULT_LOG_INFO.logLevel;
         me.logFolder = XmlUtil.getText(XmlUtil.getElement(root, LOG_FOLDER));
         if( me.logFolder == null )
            me.logFolder = DEFAULT_LOG_INFO.logFolder;
         if( root.hasAttribute(ECHO_TO_CONSOLE) )
            me.echoToConsole = "true".equals(root.getAttribute(ECHO_TO_CONSOLE));
         if( root.hasAttribute(LOG_THREAD_NAME) )
            me.logThreadName = "true".equals(root.getAttribute(LOG_THREAD_NAME));
         return me;
      }
   } // MdmiConfig$LogInfo

   /**
    * Metadata about one MDMI map.
    * 
    * @author goancea
    */
   public static class MapInfo {
      private static final String TAG                    = "mapEntry";
      private static final String MAP_NAME               = "mapName";
      private static final String MAP_FILE_NAME          = "mapFileName";
      private static final String MAP_BUILDER_CLASS_NAME = "mapBuilderClassName";
      private static final String SYN_SVC_CLASS_NAME     = "synSvcClassName";
      private static final String SYN_SVC_JAR_NAME       = "synSvcJarName";
      private static final String SEM_SVC_CLASS_NAME     = "semSvcClassName";
      private static final String SEM_SVC_JAR_NAME       = "semSvcJarName";

      public String               mapName;
      public String               mapFileName;
      public String               mapBuilderClassName;
      public String               synSvcClassName;
      public String               synSvcJarName;
      public String               semSvcClassName;
      public String               semSvcJarName;

      /**
       * Default ctor.
       */
      public MapInfo() {
      }

      /**
       * Construct a new MapInfo from a map name and file name.
       * 
       * @param mapName The map name to use. It is used internally to identify a given map.
       * @param mapFile The file name to use.
       */
      public MapInfo( String mapName, String mapFile ) {
         this(mapName, mapFile, "org.openhealthtools.mdht.mdmi.model.xmi.direct.reader.MapBuilderXMIDirect");
      }

      /**
       * Construct a new instance using the given parameters.
       * 
       * @param mapName The map name to use. It is used internally to identify a given map.
       * @param mapFile The file name to use.
       * @param mapBuilder The builder class name to use for reading/resolving the map.
       */
      public MapInfo( String mapName, String mapFile, String mapBuilder ) {
         this.mapName = mapName;
         mapFileName = mapFile;
         mapBuilderClassName = mapBuilder;
      }

      private void toXml( Element owner ) {
         Element root = XmlUtil.addElement(owner, TAG);
         XmlUtil.addElement(root, MAP_NAME, mapName);
         XmlUtil.addElement(root, MAP_FILE_NAME, mapFileName);
         if( mapBuilderClassName != null && mapBuilderClassName.length() > 0 ) {
            XmlUtil.addElement(root, MAP_BUILDER_CLASS_NAME, mapBuilderClassName);
         }
         if( synSvcClassName != null && synSvcClassName.length() > 0 ) {
            XmlUtil.addElement(root, SYN_SVC_CLASS_NAME, synSvcClassName);
            if( synSvcJarName != null && synSvcJarName.length() > 0 )
               XmlUtil.addElement(root, SYN_SVC_JAR_NAME, synSvcJarName);
         }
         if( semSvcClassName != null && semSvcClassName.length() > 0 ) {
            XmlUtil.addElement(root, SEM_SVC_CLASS_NAME, semSvcClassName);
            if( semSvcJarName != null && semSvcJarName.length() > 0 )
               XmlUtil.addElement(root, SEM_SVC_JAR_NAME, semSvcJarName);
         }
      }

      private static MapInfo fromXml( Element root ) {
         MapInfo me = new MapInfo();
         me.mapName = XmlUtil.getText(XmlUtil.getElement(root, MAP_NAME));
         me.mapFileName = XmlUtil.getText(XmlUtil.getElement(root, MAP_FILE_NAME));
         File mf = new File(me.mapFileName);
         if( !mf.exists() )
            mf = Mdmi.INSTANCE.fileFromRelPath(me.mapFileName);
         me.mapFileName = canonicalPathName(mf);

         Element e = XmlUtil.getElement(root, MAP_BUILDER_CLASS_NAME);
         if( e != null )
            me.mapBuilderClassName = XmlUtil.getText(e);

         e = XmlUtil.getElement(root, SYN_SVC_CLASS_NAME);
         if( e != null ) {
            me.synSvcClassName = XmlUtil.getText(e);
            Element f = XmlUtil.getElement(root, SYN_SVC_JAR_NAME);
            if( f != null ) {
               me.synSvcJarName = XmlUtil.getText(f);
               File file = new File(me.synSvcJarName);
               if( !file.exists() )
                  file = Mdmi.INSTANCE.fileFromRelPath(me.synSvcJarName);
               me.synSvcJarName = canonicalPathName(file);
            }
         }

         e = XmlUtil.getElement(root, SEM_SVC_CLASS_NAME);
         if( e != null ) {
            me.semSvcClassName = XmlUtil.getText(e);
            Element f = XmlUtil.getElement(root, SEM_SVC_JAR_NAME);
            if( f != null ) {
               me.semSvcJarName = XmlUtil.getText(f);
               File file = new File(me.semSvcJarName);
               if( !file.exists() )
                  file = Mdmi.INSTANCE.fileFromRelPath(me.semSvcJarName);
               me.semSvcJarName = canonicalPathName(file);
            }
         }

         return me;
      }
   } // MdmiConfig$MapInfo

   /**
    * Metadata about an external resolver. The jar and class names must be valid. The class must implement
    * IExternalResolver.
    * 
    * @author goancea
    */
   public static class ExternalResolverInfo {
      private static final String TAG           = "externalResolver";
      private static final String PROVIDER_NAME = "providerName";
      private static final String JAR_FILE_NAME = "jarFileName";
      private static final String CLASS_NAME    = "className";

      public String               providerName;
      public String               jarFileName;
      public String               className;

      /**
       * Default ctor.
       */
      public ExternalResolverInfo() {
      }

      /**
       * Construct a new instance based on the given parameters.
       * 
       * @param providerName The name of the provider.
       * @param jarFileName The full path and file name for the jar file containing the implementation.
       * @param className The class name of the class implementing the IExternalResolver.
       */
      public ExternalResolverInfo( String providerName, String jarFileName, String className ) {
         this.providerName = providerName;
         this.jarFileName = jarFileName;
         this.className = className;
      }

      private void toXml( Element owner ) {
         Element root = XmlUtil.addElement(owner, TAG);
         root.setAttribute(PROVIDER_NAME, providerName);
         XmlUtil.addElement(root, JAR_FILE_NAME, jarFileName);
         XmlUtil.addElement(root, CLASS_NAME, className);
      }

      private static ExternalResolverInfo fromXml( Element root ) {
         ExternalResolverInfo p = new ExternalResolverInfo();
         p.providerName = root.getAttribute(PROVIDER_NAME);
         p.jarFileName = XmlUtil.getText(XmlUtil.getElement(root, JAR_FILE_NAME));
         p.className = XmlUtil.getText(XmlUtil.getElement(root, CLASS_NAME));
         if( p.jarFileName != null ) {
            File file = new File(p.jarFileName);
            if( !file.exists() )
               file = Mdmi.INSTANCE.fileFromRelPath(p.jarFileName);
            p.jarFileName = canonicalPathName(file);
         }
         return p;
      }
   } // MdmiConfig$ExternalProviderInfo

   /**
    * Metadata about an external pre-processor. The jar and class names must be valid. The class must implement
    * IPreProcessor.
    * 
    * @author goancea
    */
   public static class PreProcessorInfo {
      private static final String TAG           = "preProcessor";
      private static final String PROVIDER_NAME = "providerName";
      private static final String JAR_FILE_NAME = "jarFileName";
      private static final String CLASS_NAME    = "className";

      public String               providerName;
      public String               jarFileName;
      public String               className;

      /**
       * Default ctor.
       */
      public PreProcessorInfo() {
      }

      /**
       * Construct a new instance based on the given parameters.
       * 
       * @param providerName The name of the provider.
       * @param jarFileName The full path and file name for the jar file containing the implementation.
       * @param className The class name of the class implementing the IPreProcessor.
       */
      public PreProcessorInfo( String providerName, String jarFileName, String className ) {
         this.providerName = providerName;
         this.jarFileName = jarFileName;
         this.className = className;
      }

      private void toXml( Element owner ) {
         Element root = XmlUtil.addElement(owner, TAG);
         root.setAttribute(PROVIDER_NAME, providerName);
         XmlUtil.addElement(root, JAR_FILE_NAME, jarFileName);
         XmlUtil.addElement(root, CLASS_NAME, className);
      }

      private static PreProcessorInfo fromXml( Element root ) {
         PreProcessorInfo p = new PreProcessorInfo();
         p.providerName = root.getAttribute(PROVIDER_NAME);
         p.jarFileName = XmlUtil.getText(XmlUtil.getElement(root, JAR_FILE_NAME));
         p.className = XmlUtil.getText(XmlUtil.getElement(root, CLASS_NAME));
         if( p.jarFileName != null ) {
            File file = new File(p.jarFileName);
            if( !file.exists() )
               file = Mdmi.INSTANCE.fileFromRelPath(p.jarFileName);
            p.jarFileName = canonicalPathName(file);
         }
         return p;
      }
   } // MdmiConfig$PreProcessorInfo

   /**
    * Metadata about an external post-processor. The jar and class names must be valid. The class must implement
    * IPostProcessor.
    * 
    * @author goancea
    */
   public static class PostProcessorInfo {
      private static final String TAG           = "postProcessor";
      private static final String PROVIDER_NAME = "providerName";
      private static final String JAR_FILE_NAME = "jarFileName";
      private static final String CLASS_NAME    = "className";

      public String               providerName;
      public String               jarFileName;
      public String               className;

      /**
       * Default ctor.
       */
      public PostProcessorInfo() {
      }

      /**
       * Construct a new instance based on the given parameters.
       * 
       * @param providerName The name of the provider.
       * @param jarFileName The full path and file name for the jar file containing the implementation.
       * @param className The class name of the class implementing the IPostProcessor.
       */
      public PostProcessorInfo( String providerName, String jarFileName, String className ) {
         this.providerName = providerName;
         this.jarFileName = jarFileName;
         this.className = className;
      }

      private void toXml( Element owner ) {
         Element root = XmlUtil.addElement(owner, TAG);
         root.setAttribute(PROVIDER_NAME, providerName);
         XmlUtil.addElement(root, JAR_FILE_NAME, jarFileName);
         XmlUtil.addElement(root, CLASS_NAME, className);
      }

      private static PostProcessorInfo fromXml( Element root ) {
         PostProcessorInfo p = new PostProcessorInfo();
         p.providerName = root.getAttribute(PROVIDER_NAME);
         p.jarFileName = XmlUtil.getText(XmlUtil.getElement(root, JAR_FILE_NAME));
         p.className = XmlUtil.getText(XmlUtil.getElement(root, CLASS_NAME));
         if( p.jarFileName != null ) {
            File file = new File(p.jarFileName);
            if( !file.exists() )
               file = Mdmi.INSTANCE.fileFromRelPath(p.jarFileName);
            p.jarFileName = canonicalPathName(file);
         }
         return p;
      }
   } // MdmiConfig$PostProcessorInfo
   
   private static String canonicalPathName( File f ) {
      try {
         return f.getCanonicalPath();
      }
      catch( Exception ex ) {
         return f.getAbsolutePath();
      }
   }} // MdmiConfig
