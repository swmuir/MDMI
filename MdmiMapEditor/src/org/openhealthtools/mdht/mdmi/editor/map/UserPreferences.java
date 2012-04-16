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
package org.openhealthtools.mdht.mdmi.editor.map;

import java.util.HashMap;
import java.util.prefs.Preferences;

/**
 * @author Conway
 *
 * A collection of settings established by the user, such as
 * screen size, column names, etc.
 */
public class UserPreferences extends org.openhealthtools.mdht.mdmi.editor.common.UserPreferences {

	// Preference Keys

	/** Main (Horizontal) Divider */
	public static final String MAIN_DIVIDER_LOCATION     = "mainDividerLocation";
	/** Selected Item (Veritical) Divider */
	public static final String SELECTED_ITEM_DIVIDER_LOCATION = "selectedItemDividerLocation";

	/** EdgeNode Root */
	public static final String EDGE_NODE_ROOT	= "edgeNodeRoot";
	
	/** Default Jar File Folder */
	public static final String DEFAULT_JAR_FILE_FOLDER  = "defaultJarFileFolder";

	public static final int	DEFAULT_APPLICATION_WIDTH	= (int)(.9*1024);
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
		super(application, userName);
		m_prefs = getPreferences();
	}


	/** Get the horizontal divider location */
	public int getMainDividerLocation() {
		return m_prefs.getInt(MAIN_DIVIDER_LOCATION, (int)(DEFAULT_APPLICATION_WIDTH*.35) );
	}

	/** Save the divider location */
	public void putMainDividerLocation(int location) {
		m_prefs.putInt(MAIN_DIVIDER_LOCATION, location );
	}

	/** Get the vertical divider location */
	public int getSelectedItemDividerLocation() {
		return m_prefs.getInt(SELECTED_ITEM_DIVIDER_LOCATION, (int)(DEFAULT_APPLICATION_HEIGHT*.75) );
	}

	/** Save the divider location */
	public void putSelectedItemDividerLocation(int location) {
		m_prefs.putInt(SELECTED_ITEM_DIVIDER_LOCATION, location );
	}

}
