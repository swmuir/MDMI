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
 * Created on Nov 3, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.openhealthtools.mdht.mdmi.editor.common;

import java.awt.Frame;
import java.util.HashMap;
import java.util.ResourceBundle;

import org.openhealthtools.mdht.mdmi.util.LogWriter;

/**
 * @author Conway
 *
 * Context information for the running application
 */
public class SystemContext {
    // KEYS

    /** Key for logWriter.
     * @see getLogWriter() */
    public static final String LOG_WRITER = "logWriter";
    
    /** Key for applicationFrame.
     * @see getApplicationFrame() */
    public static final String APPLICATION_FRAME = "applicationFrame";
    
    /** Key for applicationResource.
     * @see getApplicationResource() */
    public static final String APPLICATION_RESOURCE = "applicationResource";
    
    /** Key for applicationName.
     * @see getApplicationName() */
    public static final String APPLICATION_NAME = "applicationName";
    
    /** Key for map file name */
    public static final String MAP_FILE_NAME = "mapFileName";
    
    private static HashMap<String, Object> m_context = new HashMap<String, Object>();

    /** Set the LogWriter */
    public static void setLogWriter(LogWriter writer) {
    	m_context.put(LOG_WRITER, writer);
    }
    
    /** Get current the LogWriter */
    public static LogWriter getLogWriter() {
        return (LogWriter)m_context.get(LOG_WRITER);
    }

    /** Set the current application frame */
    public static void setApplicationFrame(Frame frame) {
        m_context.put(APPLICATION_FRAME, frame);
    }
    
    /** Get the current application frame */
    public static Frame getApplicationFrame() {
        return (Frame)m_context.get(APPLICATION_FRAME);
    }

    /** Set the current application resource bundle */
    public static void setApplicationResource(ResourceBundle res) {
        m_context.put(APPLICATION_RESOURCE, res);
    }
    
    /** Get the current application resource bundle */
    public static ResourceBundle getApplicationResource() {
        return (ResourceBundle)m_context.get(APPLICATION_RESOURCE);
    }

    /** Set the current application name */
    public static void setApplicationName(String name) {
        m_context.put(APPLICATION_NAME, name);
    }
    
    /** Get the current application name */
    public static String getApplicationName() {
        return (String)m_context.get(APPLICATION_NAME);
    }

    /** Set the current map file name */
    public static void setMapFileName(String name) {
        m_context.put(MAP_FILE_NAME, name);
    }
    
    /** Get the current map file name */
    public static String getMapFileName() {
        return (String)m_context.get(MAP_FILE_NAME);
    }
    
}
