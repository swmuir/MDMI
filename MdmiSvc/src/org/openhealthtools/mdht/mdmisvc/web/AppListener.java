package org.openhealthtools.mdht.mdmisvc.web;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.apache.log4j.*;

public class AppListener implements ServletContextListener {
   final static String FS = System.getProperty("file.separator");
   final static String PROJECT_PATH = FS + "mdmisvc" + FS + "WEB-INF";

   protected static HashMap<String, String> tokens = new HashMap<String, String>();
   protected static String mongoDbUri = "localhost";

   static Logger logger = Logger.getLogger(AppListener.class.getName());
   
   @Override
   public void contextDestroyed( ServletContextEvent arg0 ) {
   }

   @Override
   public void contextInitialized( ServletContextEvent ignored ) {
      try {
         File base = baseFolder();
         File cl = new File(base, "log4j.properties");
         PropertyConfigurator.configure(cl.getAbsolutePath());
         logger.debug("MdmiSvc logging started from '" + cl.getAbsolutePath() + "'");
         
         File configFile = new File(base, "mdmisvc.config");
         logger.debug("MdmiSvc initializing from '" + configFile.getAbsolutePath() + "'");
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder parser = factory.newDocumentBuilder();
         Document doc = parser.parse(configFile);
         NodeList nodes = doc.getElementsByTagName("token");
         for( int i = 0; i < nodes.getLength(); i++ ) {
            Node n = nodes.item(i);
            String tok = n.getTextContent();
            tokens.put(tok, tok);
         }
         nodes = doc.getElementsByTagName("mongoDbUri");
         if( 0 < nodes.getLength() ) {
            Node n = nodes.item(0);
            mongoDbUri = n.getTextContent();
         }
         logger.debug("MdmiSvc database initialized at '" + mongoDbUri + "'");
      }
      catch( Exception ex ) {
         throw new RuntimeException("Invalid configuration!", ex);
      }
   }

   public static String getMongoDbUri() {
      return mongoDbUri;
   }
   
   public static boolean IsValid( String token ) {
      return null != tokens.get(token);
   }

   private static File baseFolder() {
      File basePath = null;
      String catalinaBase = System.getProperty("catalina.home");
      String wtpDeploy = System.getProperty("wtp.deploy");
      if( wtpDeploy != null && wtpDeploy.length() > 0 ) {
         // NOTE: DEBUG version under Eclipse
         String path = wtpDeploy + PROJECT_PATH;
         basePath = new File(path);
         if( !basePath.exists() )
            throw new IllegalArgumentException("Invalid path " + path);
      }
      else {
         // NOTE: RELEASE version under Tomcat
         String path = catalinaBase + FS + "webapps" + PROJECT_PATH;
         basePath = new File(path);
         if( !basePath.exists() )
            throw new IllegalArgumentException("Invalid path " + path);
      }
      return basePath;
   }
} // AppListener
