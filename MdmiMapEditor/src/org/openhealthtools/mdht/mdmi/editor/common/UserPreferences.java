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
/*
 * Created on Nov 4, 2005
 *
 */
package org.openhealthtools.mdht.mdmi.editor.common;

import java.util.HashMap;
import java.util.prefs.Preferences;

/**
 * @author Conway
 *
 * A collection of settings established by the user, such as
 * screen size, column names, etc.
 */
public class UserPreferences {

	// Preference Keys
	/** User Name */
	public static final String USER_NAME			= "userName";
	/** Application Width key */
	public static final String APPLICATION_WIDTH         = "applicationWidth";
	/** Application Height key */
	public static final String APPLICATION_HEIGHT        = "applicationHeight";
	/** Application state (Minimized/Maximized) */
	public static final String APPLICATION_MAXIMIZED     = "applicationMaximized";
//	
//	/** EdgeNode Root */
//	public static final String EDGE_NODE_ROOT	= "edgeNodeRoot";

	public static final int	   DEFAULT_APPLICATION_WIDTH	= (int)(.9*1024);
	public static final int 	DEFAULT_APPLICATION_HEIGHT	= (int)(.9*768);

	// Single instance (per user)
	private static HashMap <String, UserPreferences> s_instances = new HashMap <String, UserPreferences>();

	/** Implementation using Preferences */
	private Preferences m_prefs;

	/** Obtain the preference for this user */
	public static UserPreferences getInstance(String application, String userName) {
		UserPreferences instance = s_instances.get(userName);
		if (instance == null) {
			instance = new UserPreferences(application, userName);
			s_instances.put(userName, instance);
		}
		return instance;
	}   

	/** provide constructor */
	protected UserPreferences(String application, String userName) {
		if (userName == null || userName.length() == 0) {
			m_prefs = Preferences.userRoot().node(application);
		} else {
			m_prefs = Preferences.userRoot().node(application).node(userName);
		}
	}
	
	/** Access to implementation */
	protected Preferences getPreferences() {
		return m_prefs;
	}
	
	/** Get the preferred user name */
	public String getUserName() {
		return m_prefs.get(USER_NAME, null);
	}
	
	/** Save the preferred user name */
	public void setUserName(String userName) {
		m_prefs.put(USER_NAME, userName);
	}

	/** Get the application width */
	public int getApplicationWidth() {
		return m_prefs.getInt(APPLICATION_WIDTH, DEFAULT_APPLICATION_WIDTH );
	}

	/** Save the application width */
	public void putApplicationWidth(int width) {
		m_prefs.putInt(APPLICATION_WIDTH, width );
	}

	/** Get the application height */
	public int getApplicationHeight() {
		return m_prefs.getInt(APPLICATION_HEIGHT, DEFAULT_APPLICATION_HEIGHT );
	}

	/** Save the application height */
	public void putApplicationHeight(int height) {
		m_prefs.putInt(APPLICATION_HEIGHT, height );
	}

	/** Is the application maximized */
	public boolean isApplicationMaximized() {
		return m_prefs.getBoolean(APPLICATION_MAXIMIZED, false);
	}

	/** Save min/max state */
	public void putApplicationMaximized(boolean maximized) {
		m_prefs.putBoolean(APPLICATION_MAXIMIZED, maximized);
	}

	/////////////////////////////////////////////////////////////////////
	/** Generic get method */
	public String getValue(String key, String defaultValue) {
		return m_prefs.get(key, defaultValue);
	}

	/** Generic get method */
	public int getIntValue(String key, int defaultValue) {
		return m_prefs.getInt(key, defaultValue);
	}

	/** Generic get method */
	public boolean getBooleanValue(String key, boolean defaultValue) {
		return m_prefs.getBoolean(key, defaultValue);
	}

	/** Generic put method */
	public void putValue(String key, String value) {
		m_prefs.put(key, value);
	}


	/** Generic put method */
	public void putIntValue(String key, int value) {
		m_prefs.putInt(key, value);
	}

	/** Generic put method */
	public void putBooleanValue(String key, boolean value) {
		m_prefs.putBoolean(key, value);
	}
}
