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
import java.lang.reflect.*;
import org.openhealthtools.mdht.mdmi.*;

/**
 * Static utilities with general purpose.
 * 
 * @author goancea
 */
public class Util {
   /**
    * Formats an array of objects into a String suitable for output (logging, etc.). The output format is
    * "{ v1, v2, ... }", where vN is a value at position N. Null values will be represented by the string "null" and
    * non-null values will have their "toString()" representation.
    * 
    * @param oarray The array of objects to be formatted into a single string.
    * @return The string representation of the objects in the array.
    */
   public static String valueOf( Object[] oarray ) {
      StringBuffer sbuf = new StringBuffer("{ ");

      for( int i = 0; i < oarray.length; ++i ) {
         String stringValue = oarray[i] == null ? "null" : oarray[i].toString();
         stringValue = trimToLength(stringValue);
         if( i > 0 )
            sbuf.append(", ");
         sbuf.append(stringValue);
      }
      sbuf.append(" }");
      return sbuf.toString();
   }

   private static String trimToLength( String v ) {
      int n = v.indexOf(System.getProperty("line.separator"));
      if( n > 0 )
         v = v.substring(0, n);
      if( v.length() > 32 )
         v = v.substring(0, 29) + "...";
      return v;
   }

   /**
    * Returns a string representation of the stack trace of a Throwable
    * 
    * @param t the throwable
    * @return a string containing the stack trace for the Throwable
    */
   public static String getStackTrace( Throwable t ) {
      StringWriter sw = new StringWriter(1024);
      PrintWriter pw = null;
      try {
         pw = new PrintWriter(sw);
         t.printStackTrace(pw);
         pw.close();
      }
      catch( Exception ignored ) {
      }
      return sw.toString();
   }

   /**
    * Execute the specified shell command and wait for it to finish. Return true if the return code is 0, false for
    * anything else. OUT ane ERR are redirected to normal System.out and System.err.
    * 
    * @param cmd Shell command string to execute.
    * @return true if the return code is 0, false for anything else.
    */
   public static boolean executeCommand( String cmd ) {
      return executeCommand(cmd, null, null);
   }

   /**
    * Execute the specified shell command with the given environment variables, and within the given work directory.
    * Wait for it to finish. Return true if the return code is 0, false for anything else. OUT and ERR are redirected to
    * normal System.out and System.err.
    * 
    * If env is null, the subprocess inherits the environment settings of the current process. If workDir is null the
    * subprocess will inherit the working directory of the current process.
    * 
    * @param cmd Shell command string to execute.
    * @param env Array of strings, each element of which has environment variable settings in format name=value.
    * @param workDir The working directory of the subprocess.
    * @return true if the return code is 0, false for anything else.
    */
   public static boolean executeCommand( String cmd, String[] env, File workDir ) {
      try {
         Runtime rt = Runtime.getRuntime();
         final Process pc = rt.exec(cmd, env, workDir);

         // intercept the out
         Thread to = new Thread() {
            public void run() {
               InputStream cin = pc.getInputStream();
               try {
                  int nBuf = 0;
                  byte[] buf = new byte[256];
                  while( (nBuf = cin.read(buf)) > 0 ) {
                     String z = new String(buf, 0, nBuf, "UTF8");
                     System.out.print(z);
                  }
               }
               catch( Exception e ) {
                  e.printStackTrace();
               }
            }
         };
         to.start();

         // intercept the err
         Thread te = new Thread() {
            public void run() {
               InputStream cin = pc.getErrorStream();
               try {
                  int nBuf = 0;
                  byte[] buf = new byte[256];
                  while( (nBuf = cin.read(buf)) > 0 ) {
                     String z = new String(buf, 0, nBuf, "UTF8");
                     System.err.print(z);
                  }
               }
               catch( Exception e ) {
                  e.printStackTrace();
               }
            }
         };
         te.start();

         int retCode = pc.waitFor();
         if( retCode != 0 ) {
            System.err.println("Execute '" + cmd + "' failed!");
            return false;
         }
      }
      catch( Exception e ) {
         e.printStackTrace();
         return false;
      }
      return true;
   }

   /**
    * Create an instance of the specified class (by name). If class is not loaded, it will attempt to load it from the
    * specified jar. Note that the class MUST have an empty constructor.
    * 
    * @param <T> Interface or base class of the instantiated class.
    * @param className The class name, required, must be non-null.
    * @param jarFile Optional, path to the jar file to load it from (if not already loaded).
    * @return An instance of the specified class, cast to T.
    */
   @SuppressWarnings( value = "all" )
   public static <T> T getInstance( String className, File jarFile, Class<?>[] paramTypes, Object[] params ) {
      if( className == null )
         throw new IllegalArgumentException("Class name must be specified!");
      Class<?> c = null;
      try {
         c = Class.forName(className);
      }
      catch( Exception ignored ) {
      }
      if( c == null ) {
         if( jarFile == null )
            throw new MdmiException("Jar file not specified for class " + className);
         // try to load the library
         if( !jarFile.exists() )
            throw new MdmiException("Jar file not found" + jarFile.getAbsolutePath());
         JarClassLoader jld = new JarClassLoader(jarFile);
         c = jld.findClass(className);
      }
      if( c == null )
         throw new MdmiException("Class not found " + className);
      Object o = null;
      try {
         if( paramTypes == null ) {
            o = c.newInstance();
         }
         else {
            Constructor ctr = c.getConstructor(paramTypes);
            if( ctr == null )
               throw new MdmiException("Constructor with the given parameters not found " + className);
            o = ctr.newInstance(params);
         }
      }
      catch( Exception ex ) {
         throw new MdmiException("Cannot instantiate " + className, ex);
      }
      T t = null;
      try {
         t = (T)o; // causes a warning, there is no way to use instanceof operator, unfortunately.
      }
      catch( Exception ex ) {
         throw new MdmiException("Unexpected exception " + className);
      }
      return t;
   }
} // Util