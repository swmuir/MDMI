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
 * ApplicationResources.java
 *
 * Created on June 19, 2002, 5:32 PM
 */

package org.openhealthtools.mdht.mdmi.editor.common.menus;

import java.net.URL;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.actions.AbstractAction;


/**
 *
 * @author  TPerkins
 */
public class ApplicationResources {
    
    /*Missing Resource icon is part of the framework; not configurable */
    public static final String MISSING_RESOURCE_ICON = "./images/Missing16.gif";
    private static ResourceBundle bundle = null;

    private static HashMap<String, ImageIcon> icons = new HashMap<String, ImageIcon>();
    
    
    /** Creates a new instance of ApplicationResources */
    protected ApplicationResources() {
        
    }
    
    public static ApplicationResources getResources(){
        if (bundle == null) {
            bundle = SystemContext.getApplicationResource();
        }
        
        return new ApplicationResources();
    }
    
    public String getActionText(Class<?> classid){
        String className = classid.getName();
        return getText(className+".text");
    }
    
    public ImageIcon getActionIcon(Class<?> classid){
        String className = classid.getName();
        return getIcon(className+".icon");
    }
    
    public String getText(String resource){
        try {
            String text = bundle.getString(resource);
            return text;
        } catch (MissingResourceException e){
            String msg = "Missing Resource for '" + resource + "'.";
            AbstractAction.logActionError( msg, e);

            return "-";
        }
    }
    
    public ImageIcon getIcon(String resourceName){
        ImageIcon image;
        String urlName = "";
        try {
            if (icons.containsKey(resourceName)){
                return icons.get(resourceName);
            } else {
                urlName = bundle.getString(resourceName);
                URL url = this.getClass().getResource(urlName);
                if (url == null) {
                   throw new MissingResourceException("Unable to locate image file",
                         this.getClass().getName(), urlName);
                }
                image = new ImageIcon(url);
            }
        } catch (Exception e){
            String msg = "Missing Resource for '" + resourceName
                    + "', value is '" + urlName + "'";
            
            AbstractAction.logActionError(msg, e);

            image = new ImageIcon(this.getClass().getResource(MISSING_RESOURCE_ICON));
        }
        
        icons.put(resourceName, image);
        return image;
    }

    
}
