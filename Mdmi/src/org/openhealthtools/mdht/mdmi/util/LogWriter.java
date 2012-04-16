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
package org.openhealthtools.mdht.mdmi.util;

import java.io.*;
import java.text.*;
import java.util.Date;
import java.util.UUID;
import java.util.logging.*;

/**
 * Handle logging and tracing.
 * <p>
 * loggingLevel - one of the java.util.logging.Level interface, i.e. "FINEST", "FINER", "FINE", "CONFIG", "INFO",
 * "WARNING", "SEVERE", or "OFF" default is ["SEVERE"], that is logging only exceptions
 * <p>
 * logFolderName - folder name relative or absolute
 * 
 * @author goancea
 */
public final class LogWriter {
   public static final Level   DEFAULT_LEVEL        = Level.SEVERE;
   public static final boolean DEFAULT_ECHO_CONSOLE = true;

   private Level               m_loggingLevel;
   private File                m_logFile;
   private Logger              m_logger;
   private Handler             m_fileHandler;
   private Handler             m_consoleHandler;
   private boolean             m_logThreadName;
   
   

   /**
    * Create a new log instance based on the specified parameters.
    */
   public LogWriter( Level level, File logFolder, boolean echoToConsole, boolean logThreadName ) {
      if( logFolder == null )
         throw new IllegalArgumentException("Null argument logFolder!");
      if( logFolder.exists() && !logFolder.isDirectory() )
         throw new IllegalArgumentException("Invalid argument logFolder: " + logFolder);
      if( !logFolder.exists() )
         logFolder.mkdirs();
      m_loggingLevel = level == null ? DEFAULT_LEVEL : level;
      m_logFile = new File(logFolder, getFileName());
      m_logThreadName = logThreadName;
      start(echoToConsole);
   }

   /**
    * Start using this logger.
    * 
    * @param echoToConsole If true the output will also go to the console.
    */
   private void start( boolean echoToConsole ) {
      try {
         m_logger = Logger.getLogger(UUID.randomUUID().toString());
         m_fileHandler = new FH();
         m_logger.addHandler(m_fileHandler);
         if( echoToConsole ) {
            m_consoleHandler = new ConsoleHandler();
            m_consoleHandler.setFormatter(new FSLogFormatter());
            m_consoleHandler.setLevel(m_loggingLevel);
            m_logger.addHandler(m_consoleHandler);
         }
         m_logger.setLevel(m_loggingLevel);
         m_logger.setUseParentHandlers(false);
      }
      catch( Exception ex ) {
         throw new RuntimeException("Unexpected IO exception", ex);
      }
   }

   /**
    * Stop logging for this logger.
    */
   public void stop() {
      try {
         if( m_fileHandler != null ) {
            m_fileHandler.close();
            m_logger.removeHandler(m_fileHandler);
            m_fileHandler = null;
         }
         if( m_consoleHandler != null ) {
            m_consoleHandler.close();
            m_logger.removeHandler(m_consoleHandler);
            m_consoleHandler = null;
         }
         m_logger.setLevel(Level.OFF);
      }
      catch( Exception ex ) {
         throw new RuntimeException("Unexpected IO exception", ex);
      }
   }

   public void addHandler( Handler handler ) {
      m_logger.addHandler(handler);
   }

   @Override
   public String toString() {
      return "Logging to file '" + m_logFile.getAbsolutePath() + "', level = '" + m_loggingLevel.toString() + ".";
   }

   /**
    * True if this logger is logging anything (i.e. it is not off).
    * 
    * @return True if this logger is logging anything, that is the loggingLevel is not Level.OFF.
    */
   public boolean isLogging() {
      return m_loggingLevel != Level.OFF;
   }

   /**
    * Get the logging level of this logger (an enumerated value from java.util.logging.Level).
    * 
    * @return Get the logging level of this logger.
    */
   public Level loggingLevel() {
      return m_loggingLevel;
   }

   /**
    * Log a message at the specified level. The message will be logged only if the loggingLevel is above the current
    * level.
    * 
    * For example if the loggingLevel is FINE the following call will be logged:
    * <code>logger.log( WARNING, "Some warning message!" );</code> but this one will not be logged:
    * <code>logger.log( FINEST, "A small trace event occured." );</code>
    * 
    * @param level logging level to use for this call.
    * @param msg The actual string message.
    * @param params Variable list of message parameters.
    */
   private void log( Level level, String msg, Object... params ) {
      if( level.intValue() < m_loggingLevel.intValue() || m_loggingLevel.intValue() == Level.OFF.intValue() )
         return;
      if( null != params && 0 < params.length )
         msg = MessageFormat.format(msg, params);
      FSLogRecord lr = new FSLogRecord(level, msg);
      updateLogRecord(lr);
      m_logger.log(lr);
   }

   /**
    * Log an exception.
    * 
    * @param thrown Exception thrown that needs to be logged.
    */
   public void loge( Throwable thrown ) {
      loge(Level.SEVERE, thrown, thrown.getMessage());
   }

   /**
    * Log an exception and a message string.
    * 
    * @param thrown Exception thrown that needs to be logged.
    * @param msg The actual string message.
    * @param params Variable list of message parameters.
    */
   public void loge( Throwable thrown, String msg, Object... params ) {
      loge(Level.SEVERE, thrown, msg, params);
   }

   /**
    * Log an exception and a message string, at the specified level.
    * 
    * @param level logging level to use for this call.
    * @param thrown Exception thrown that needs to be logged.
    * @param msg The actual string message.
    * @param params Variable list of message parameters.
    */
   private void loge( Level level, Throwable thrown, String msg, Object... params ) {
      if( level.intValue() < m_loggingLevel.intValue() || m_loggingLevel.intValue() == Level.OFF.intValue() )
         return;
      if( null != params && 0 < params.length )
         msg = MessageFormat.format(msg, params);
      FSLogRecord lr = new FSLogRecord(level, msg);
      updateLogRecord(lr);
      lr.setThrown(thrown);
      m_logger.log(lr);
   }

   private void updateLogRecord( FSLogRecord lr ) {
      StackTraceElement stack[] = (new Throwable()).getStackTrace();
      int ix = 0;
      while( ix < stack.length ) {
         StackTraceElement frame = stack[ix];
         String cname = frame.getClassName();
         if( !cname.equals(this.getClass().getName()) ) {
            lr.setSourceClassName(cname);
            lr.setSourceMethodName(frame.getMethodName());
            lr.setSourceFileName(frame.getFileName());
            lr.setSourceLineNumber(frame.getLineNumber());
            return;
         }
         ix++;
      }
   }

   /**
    * Log an exception about to be thrown.
    * 
    * @param className The name of the class that is throwing.
    * @param methodName The name of the method that is throwing.
    * @param thrown Exception thrown that needs to be logged.
    */
   public void throwing( String className, String methodName, Throwable thrown ) {
      if( Level.FINER.intValue() < m_loggingLevel.intValue() || m_loggingLevel.intValue() == Level.OFF.intValue() )
         return;
      FSLogRecord lr = new FSLogRecord(Level.FINER, "THROW");
      lr.setSourceClassName(className);
      lr.setSourceMethodName(methodName);
      lr.setThrown(thrown);
      m_logger.log(lr);
   }

   /**
    * Log a message at the SEVERE logging level.
    * 
    * @param msg The actual string message.
    * @param params Variable list of message parameters.
    */
   public void severe( String msg, Object... params ) {
      log(Level.SEVERE, msg, params);
   }

   /**
    * Log a message at the WARNING logging level. Only if the given logModule is being configured to be logged.
    * 
    * @param msg The actual string message.
    * @param params Variable list of message parameters.
    */
   public void warning( String msg, Object... params ) {
      log(Level.WARNING, msg, params);
   }

   /**
    * Log a message at the INFO logging level. Only if the given logModule is being configured to be logged.
    * 
    * @param msg The actual string message.
    * @param params Variable list of message parameters.
    */
   public void info( String msg, Object... params ) {
      log(Level.INFO, msg, params);
   }

   /**
    * Log a message at the CONFIG logging level. Only if the given logModule is being configured to be logged.
    * 
    * @param msg The actual string message.
    * @param params Variable list of message parameters.
    */
   public void config( String msg, Object... params ) {
      log(Level.CONFIG, msg, params);
   }

   /**
    * Log a message at the FINE logging level. Only if the given logModule is being configured to be logged.
    * 
    * @param msg The actual string message.
    * @param params Variable list of message parameters.
    */
   public void fine( String msg, Object... params ) {
      log(Level.FINE, msg, params);
   }

   /**
    * Log a message at the FINER logging level. Only if the given logModule is being configured to be logged.
    * 
    * @param msg The actual string message.
    * @param params Variable list of message parameters.
    */
   public void finer( String msg, Object... params ) {
      log(Level.FINER, msg, params);
   }

   /**
    * Log a message at the FINEST logging level. Only if the given logModule is being configured to be logged.
    * 
    * @param msg The actual string message.
    * @param params Variable list of message parameters.
    */
   public void finest( String msg, Object... params ) {
      log(Level.FINEST, msg, params);
   }
   
   /**
    * Get a Level enumerated value from a string. Usually used when reading a configuration for a logger
    * from a properties file or string.
    * 
    * @param level The string representation of the level.
    * @return The corresponding level if a match is found, DEFAULT_LEVEL otherwise.
    */
   public static Level levelFromString( String level ) {
      try {
         return Level.parse(level);
      }
      catch( Exception ex ) {
         return DEFAULT_LEVEL;
      }
   }

   private String getFileName() {
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss.SSSZ");
      return df.format(new Date()) + ".log";
   }

   /**
    * Extension of StreamHandler to create one file only and lock it.
    */
   private class FH extends StreamHandler {
      FH() throws IOException {
         final FileOutputStream fout = new FileOutputStream(m_logFile, false);
         setOutputStream(fout);
         super.setFormatter(new FSLogFormatter());
         super.setLevel(m_loggingLevel);
      }

      @Override
      public synchronized void publish( LogRecord record ) {
         super.publish(record);
         super.flush(); // Ensure the record is flushed from the stream to the file.
      }
   } // FH

   private final static SimpleDateFormat SDF           = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSZ");
   private final static String           LINESEPARATOR = System.getProperty("line.separator");

   private class FSLogFormatter extends Formatter {
      /**
       * Format the given LogRecord. The parameter LogRecord in this call back will be FSLogRecord.
       * 
       * @param record the log record to be formatted.
       * @return a formatted string to be printed
       */
      public synchronized String format( LogRecord record ) {
         if( !(record instanceof FSLogRecord) )
            throw new IllegalArgumentException("parameter to this call back must be instance of FSLogRecord");
         FSLogRecord fsLR = (FSLogRecord)record;

         StringBuffer sb = new StringBuffer(256);
         Date t = new Date(record.getMillis());
         sb.append(SDF.format(t));
         sb.append(" ");
         sb.append(record.getLevel().getLocalizedName());
         if( m_logThreadName )
            sb.append(" [").append(fsLR.getThreadName()).append("]");
         String message = formatMessage(record);
         sb.append(": ");
         sb.append(message);
         sb.append(LINESEPARATOR);
         if( record.getThrown() != null ) {
            if( record.getSourceClassName() != null )
               sb.append(record.getSourceClassName());
            if( record.getSourceMethodName() != null ) {
               sb.append(".");
               sb.append(record.getSourceMethodName());
            }
            if( fsLR.getSourceFileName() != null ) {
               sb.append("[");
               sb.append(fsLR.getSourceFileName());
               if( fsLR.getSourceLineNumber() != 0 ) {
                  sb.append(":");
                  sb.append(fsLR.getSourceLineNumber());
               }
               sb.append("] ");
            }
            sb.append(LINESEPARATOR);
            try {
               StringWriter sw = new StringWriter();
               PrintWriter pw = new PrintWriter(sw);
               record.getThrown().printStackTrace(pw);
               pw.close();
               sb.append(sw.toString());
               sb.append(LINESEPARATOR);
            }
            catch( Exception ex ) {
            }
         }
         return sb.toString();
      }
   } // FSLogFormatter

   private static class FSLogRecord extends LogRecord {
      private static final long serialVersionUID = 1L;

      private String            m_sourceThreadName;
      private String            m_sourceFileName;
      private int               m_sourceLineNumber;

      private FSLogRecord( Level level, String msg ) {
         super(level, msg);
         m_sourceThreadName = Thread.currentThread().getName();
      }

      private String getThreadName() {
         return m_sourceThreadName;
      }

      private void setSourceFileName( String fileName ) {
         m_sourceFileName = fileName;
      }

      private String getSourceFileName() {
         return m_sourceFileName;
      }

      private void setSourceLineNumber( int lineNumber ) {
         m_sourceLineNumber = lineNumber;
      }

      private int getSourceLineNumber() {
         return m_sourceLineNumber;
      }
   } // FSLogRecord
} // LogWriter