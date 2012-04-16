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
 * AbstractXMLConfigurationFactory.java
 *
 * Created on July 16, 2002, 2:35 PM
 */

package org.openhealthtools.mdht.mdmi.editor.common.menus;

import java.io.IOException;

import org.w3c.dom.Element;

/**
 *
 * @author  tperkins
 */
public abstract class AbstractXMLConfigurationFactory {
    
    protected ApplicationResources resources = null;
    protected Element root = null;
    
    /** Creates a new instance of AbstractXMLConfigurationFactory */
    public AbstractXMLConfigurationFactory(Element root) throws IOException {
        try {
            resources = ApplicationResources.getResources();
            this.root = root;
            init();
            build();
        } catch (Exception e){
            throw new IOException(e.getMessage());
        }
    }
    
    public abstract void build() throws Exception;
    
    public abstract void init();
    
}
