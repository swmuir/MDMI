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
*     Sally Conway
*
*******************************************************************************/
package org.openhealthtools.mdht.mdmi.editor.common;

import java.io.File;
import java.util.HashMap;

import org.openhealthtools.mdht.mdmi.util.JarClassLoader;

/** Utility for keeping track of which jar files have been loaded */
public class JarClassLoaderMgr {
	
	private static HashMap<File, JarClassLoader> s_loadedJars = new HashMap<File, JarClassLoader> ();
	
	public static JarClassLoader getJarClassLoader(File jarFile) {
		synchronized (s_loadedJars) {
			// check first
			JarClassLoader loader = s_loadedJars.get(jarFile);
			if (loader == null) {
				// create
				loader = new JarClassLoader(jarFile);
				loader.loadAllClasses();
				s_loadedJars.put(jarFile, loader);
			}

			return loader;
		}
	}

}
