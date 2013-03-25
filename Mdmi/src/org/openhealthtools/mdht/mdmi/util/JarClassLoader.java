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

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.jar.*;
import org.openhealthtools.mdht.mdmi.*;

/**
 * <code>JarClassLoader</code> extends the standard <code>URLClassLoader</code> with additional utility methods.
 * 
 * @author goancea
 */
public class JarClassLoader extends URLClassLoader {
   /**
    * Empty constructor. Use <code>addJarFile</code> to load jar files.
    */
   public JarClassLoader() {
      super(new URL[] {});
   }

   /**
    * Class loader for classes in the specified jar file.
    * 
    * @param jarFile The jar file to be used as source.
    */
   public JarClassLoader( File jarFile ) {
      super(new URL[] {});
      addJarFile(jarFile);
   }

   /**
    * Class loader for classes in all the specified jar files (given as URLs).
    * 
    * @param urls The jar files to be used as sources.
    */
   public JarClassLoader( URL[] urls ) {
      super(urls);
   }

   /**
    * (non-Javadoc)
    * 
    * @see ClassLoader#findClass
    */
   public Class<?> findClass( String name ) {
      Class<?> c = findLoadedClass(name);
      if( c == null ) {
         try {
            c = getParent().loadClass(name);
         }
         catch( ClassNotFoundException e ) {
         }
         if( c == null ) {
            try {
               c = super.findClass(name);
            }
            catch( ClassNotFoundException ex ) {
               throw new MdmiException(ex, "Cannot find class " + name);
            }
         }
      }
      return c;
   }

   /**
    * Adds a jar file to the list of jars for this loader.
    * 
    * @param file Jar file to add
    */
   public void addJarFile( File file ) {
      if( file == null )
         throw new IllegalArgumentException("Null argument passed to JarClassLoader.addJarFile()");
      if( !file.exists() || !file.isFile() )
         throw new MdmiException("Invalid jar file " + file.getAbsolutePath());
      if( !file.getName().toLowerCase().endsWith(".jar") )
         throw new MdmiException("Invalid jar file " + file.getAbsolutePath());
      try {
         addURL(file.toURI().toURL());
      }
      catch( MalformedURLException ignored ) {
      }
   }

   /**
    * Loads all classes from all jar files in list
    */
   @SuppressWarnings( "resource" )
   public void loadAllClasses() {
      URL[] urls = getURLs();
      for( int i = 0; i < urls.length; i++ ) {
         File file = null;
         try {
            file = new File(urls[i].toURI());
            if( !file.getName().toLowerCase().endsWith(".jar") )
               continue;
         }
         catch( URISyntaxException e ) {
            continue;
         }

         JarFile jarFile = null;
         try {
            jarFile = new JarFile(file);
         }
         catch( IOException e ) {
            continue;
         }

         Enumeration<JarEntry> jes = jarFile.entries();
         while( jes.hasMoreElements() ) {
            JarEntry je = jes.nextElement();
            String entryName = je.getName();

            if( entryName.endsWith(".class") ) {
               String className = entryName.substring(0, entryName.indexOf(".class"));
               try {
                  className = className.replace('/', '.');
                  loadClass(className);
               }
               catch( ClassNotFoundException ex ) {
                  throw new MdmiException(ex, "Cannot find class " + className);
               }
               catch( NoClassDefFoundError ex ) {
                  throw new MdmiException(ex, "Cannot find DEF class" + className);
               }
            }
         }
      }
   }
} // JarClassLoader