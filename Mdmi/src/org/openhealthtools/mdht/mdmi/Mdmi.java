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

import org.openhealthtools.mdht.mdmi.engine.*;
import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.util.*;

/**
 * MDMI main entry point.
 * 
 * @author goancea
 */
public final class Mdmi {
   public static final String VERSION = "1.2.0.3";
   
   public static final String PARAM_MDMI_ROOT_DIR = "mdmi.root.dir";
   public static final Mdmi   INSTANCE            = new Mdmi();

   private File                  m_rootDir;
   private LogWriter             m_logger;
   private MdmiConfig            m_config;
   private MdmiResolver          m_resolver;
   private MdmiEngine            m_engine;
   private MdmiExternalResolvers m_resolvers;
   private MdmiPreProcessors     m_preProcessors;
   private MdmiPostProcessors    m_postProcessors;
   private MdmiTargetSemanticModelPostProcessors targetSemanticModelPostProcessors;

   public MdmiTargetSemanticModelPostProcessors getTargetSemanticModelPostProcessors() {
		return targetSemanticModelPostProcessors;
	}

	/**
    * Initialize this runtime, by specifying the root folder where normally the mdmi config file resides.
    * 
    * @param rootDir The root folder where this runtime is initialized from.
    */
   public void initialize( File rootDir ) {
      if( rootDir == null || !rootDir.exists() || !rootDir.isDirectory() )
         rootDir = getDefaultRootDir();
      m_rootDir = rootDir;
      System.out.println("MDMI Runtime initialized from " + m_rootDir.getAbsolutePath());
   }

   /**
    * Starts the runtime. 
    * This method will load the configuration data from the 'mdmi.config' file in the root folder, 
    * and then it will initialize and start the runtime. 
    */
   public void start() {
      MdmiConfig config = loadConfigFromFile();
      startImpl(config);
   }

   /**
    * Starts the runtime. 
    * This method will use the given configuration file, and then it will initialize and start the runtime.
    *  
    * @param config The configuration data to use.
    */
   public void start( MdmiConfig config ) {
      startImpl(config);
   }

   /**
    * Starts the runtime. 
    * This method will use the given configuration file, and then it will initialize and start the runtime.
    *  
    * @param config The configuration data to use.
    */
   private void startImpl( MdmiConfig config ) {
      if( m_config != null ) // started already
         stop();
      if( config == null )
         throw new IllegalArgumentException("Null config!");
      m_config = config;
      MdmiConfig.LogInfo li = m_config.getLogInfo();
      m_logger = new LogWriter(li.logLevel, new File(li.logFolder), li.echoToConsole, li.logThreadName);
      m_resolvers = new MdmiExternalResolvers(m_config);
      m_preProcessors = new MdmiPreProcessors(m_config);
      m_postProcessors = new MdmiPostProcessors(m_config);
      targetSemanticModelPostProcessors = new MdmiTargetSemanticModelPostProcessors(m_config);
      
      m_resolver = new MdmiResolver(m_config);
      m_engine = new MdmiEngine(this);
      m_engine.start();
   }

   /**
    * Stop the runtime, unload all configuration.
    */
   public void stop() {
      m_engine.stop();
      m_engine = null;
      m_resolver = null;
      m_resolvers = null;
      m_preProcessors = null;
      m_postProcessors = null;
      m_logger = null;
      m_config = null;
   }

   /**
    * Get the root folder from where the runtime is started.
    * 
    * @return The root folder from where the runtime is started.
    */
   public File rootDir() {
      return m_rootDir;
   }

   /**
    * Get the shared logger.
    * 
    * @return The shared logger.
    */
   public LogWriter logger() {
      return m_logger;
   }
   
   /** 
    * Get the configuration data for this instance. 
    * Note that if you modify this, it is recommended that you can stop() and start() to reset the runtime properly. 
    * 
    * @return The configuration data for this instance.
    */
   public MdmiConfig getConfig() {
      return m_config;
   }

   /**
    * Get the MDMI model resolver for this runtime instance.
    * 
    * @return The MDMI model resolver for this runtime instance.
    */
   public MdmiResolver getResolver() {
      return m_resolver;
   }

   /**
    * Get the wrapper for the external resolvers.
    * 
    * @return The wrapper for the external resolvers.
    */
   public MdmiExternalResolvers getExternalResolvers() {
      return m_resolvers;
   }

   /**
    * Get the wrapper for the pre-processors.
    * 
    * @return The wrapper for the pre-processors.
    */
   public MdmiPreProcessors getPreProcessors() {
      return m_preProcessors;
   }

   /**
    * Get the wrapper for the post-processors.
    * 
    * @return The wrapper for the post-processors.
    */
   public MdmiPostProcessors getPostProcessors() {
      return m_postProcessors;
   }
   
   /**
    * Utility method to get a relative path to the root folder.
    * 
    * @param relPath The relative path (may be a file or folder).
    * @return The File that wraps the relative path, can be used to get the absolute path.
    */
   public File fileFromRelPath( String relPath ) {
      return new File(m_rootDir, relPath);
   }

   /**
    * Execute the specified transfer synchronously.
    * 
    * @param transferInfo The transfer to execute.
    */
   public void executeTransfer( MdmiTransferInfo transferInfo ) {
      exec( transferInfo, false );
   }
   
   /**
    * Execute the specified transfer synchronously.
    * 
    * @param transferInfo The transfer to execute.
    */
   public void executeTransferAsync( MdmiTransferInfo transferInfo ) {
      exec( transferInfo, true );
   }

   public static IExpressionInterpreter getInterpreter( ConversionRule cr, XElementValue context, String name, XValue value ) {
      String lang = cr.getRuleExpressionLanguage();
      SemanticElement se = cr.getOwner();
      if( null == lang || lang.length() <= 0 ) {
         SemanticElementSet ses = se.getElementSet();
         MessageModel mm = ses.getModel();
         MessageGroup mg = mm.getGroup();
         lang = mg.getDefaultRuleExprLang();
         if( lang == null || lang.length() <= 0 )
            throw new MdmiException("Language not set for conversion in semantic element {0} and no default set in model group {1}", se.getName(), mg.getName());
      }
      return getInterpreter(lang, context, name, value);
   }

   public static IExpressionInterpreter getInterpreter( DataRule dr, XElementValue context, String name, XValue value ) {
      String lang = dr.getRuleExpressionLanguage();
      SemanticElement se = dr.getSemanticElement();
      if( null == lang || lang.length() <= 0 ) {
         SemanticElementSet ses = se.getElementSet();
         MessageModel mm = ses.getModel();
         MessageGroup mg = mm.getGroup();
         lang = mg.getDefaultRuleExprLang();
         if( lang == null || lang.length() <= 0 )
            throw new MdmiException("Language not set for data rule in semantic element {0} and no default set in model group {1}", se.getName(), mg.getName());
      }
      return getInterpreter(lang, context, name, value);
   }

   public static IExpressionInterpreter getInterpreter( SemanticElementBusinessRule sebr, XElementValue context, String name, XValue value ) {
      String lang = sebr.getRuleExpressionLanguage();
      SemanticElement se = sebr.getSemanticElement();
      if( null == lang || lang.length() <= 0 ) {
         SemanticElementSet ses = se.getElementSet();
         MessageModel mm = ses.getModel();
         MessageGroup mg = mm.getGroup();
         lang = mg.getDefaultRuleExprLang();
         if( lang == null || lang.length() <= 0 )
            throw new MdmiException("Language not set for business rule in semantic element {0} and no default set in model group {1}", se.getName(), mg.getName());
      }
      return getInterpreter(lang, context, name, value);
   }

   public static IExpressionInterpreter getInterpreter( SemanticElementRelationship ser, XElementValue context, String name, XValue value ) {
      String lang = ser.getRuleExpressionLanguage();
      SemanticElement se = ser.getContext();
      if( null == lang || lang.length() <= 0 ) {
         SemanticElementSet ses = se.getElementSet();
         MessageModel mm = ses.getModel();
         MessageGroup mg = mm.getGroup();
         lang = mg.getDefaultRuleExprLang();
         if( lang == null || lang.length() <= 0 )
            throw new MdmiException("Language not set for business rule in semantic element {0} and no default set in model group {1}", se.getName(), mg.getName());
      }
      return getInterpreter(lang, context, name, value);
   }

   public static IExpressionInterpreter getInterpreter( String lang, XElementValue context, String name, XValue value ) { 
      if( lang == null || lang.length() <= 0 )
         throw new MdmiException("Language not set!");
      if( lang.equalsIgnoreCase("nrl") )
         return new NrlAdapter(context == null ? null: context.getOwner(), context, name, value);
      if( lang.equalsIgnoreCase("js") || lang.equalsIgnoreCase("javascript") || lang.equalsIgnoreCase("ecmascript") )
         return new JsAdapter(context == null ? null: context.getOwner(), name, value);
      throw new MdmiException("Language {0} not supported!", lang);
   }
   
   // call the engine to execute
   private void exec( MdmiTransferInfo transferInfo, boolean async ) {
      try {
         transferInfo.sourceModel.resolve();
         transferInfo.targetModel.resolve();
         if( async )
            m_engine.executeTransferAsync( transferInfo );
         else
            m_engine.executeTransfer( transferInfo );
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException( ex, "Mdmi.exec() Unexpected exception for transformnation " + transferInfo.toString() );
      }
   }

   // clients should use the INSTANCE
   private Mdmi() {
      System.out.println("MDMI version " + VERSION);
   }

   // load the configuration data.
   private MdmiConfig loadConfigFromFile() {
      MdmiConfig config = new MdmiConfig();
      File f = fileFromRelPath(MdmiConfig.FILE_NAME);
      if( f.exists() && f.isFile() )
         config.load(f);
      return config;
   }

   // get the default root folder, if none was specified
   private static File getDefaultRootDir() {
      String d = System.getProperties().getProperty(PARAM_MDMI_ROOT_DIR);
      if( d != null ) {
         File f = new File(d);
         if( f.exists() && f.isDirectory() )
            return f;
      }
      d = System.getProperties().getProperty("user.dir");
      return new File(d);
   }
} // Mdmi
