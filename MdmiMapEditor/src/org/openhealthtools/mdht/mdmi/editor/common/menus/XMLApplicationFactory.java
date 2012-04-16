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
 * XMLWorkSpaceFactory.java
 *
 * Created on June 18, 2002, 4:49 PM
 */

package org.openhealthtools.mdht.mdmi.editor.common.menus;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.actions.AbstractAction;
import org.openhealthtools.mdht.mdmi.editor.common.actions.ActionRegistry;
import org.openhealthtools.mdht.mdmi.util.XmlParser;
import org.openhealthtools.mdht.mdmi.util.XmlUtil;

/**
 *
 * Create menus from an XML file
 */
public class XMLApplicationFactory
{
    
    private Element m_root = null;

    private ArrayList<JToolBar> m_toolBarList;
    private JMenuBar  m_menuBar;
    
    /** Creates a new instance of XMLApplicationFactory */
    
    public XMLApplicationFactory(String appResource) throws IOException
    {
    	/* Step 1: Load the XML Configuration */
    	load(appResource);

    	/* Step 2: Setup the System variable for ApplicationResources config */
    	setSystemResourceProperty();
          
    }
    
    private void setSystemResourceProperty()
    {
        String resourcePath = getValue(m_root, "resource");
        ResourceBundle bundle = ResourceBundle.getBundle(resourcePath);
        SystemContext.setApplicationResource(bundle);
    }
    
    public void load(String resource) throws IOException
    {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(resource);
//        SAXBuilder builder = new SAXBuilder();
//        Document doc = builder.build(in);
//        in.close();
//        
//        m_root = doc.getRootElement();
        
    	XmlParser parser = new XmlParser();
    	Document doc = parser.parse(in);
    	m_root = doc.getDocumentElement();
    }
    
    public void build() throws Exception
    {
        /* Load and register the actions including executing startup actions */
        registerActions();
        
        /* Build the Menu bar */
        Element menubar = XmlUtil.getElement( m_root, "menubar" );	//m_root.getChild("menubar")
        XMLMenuFactory menuFac = new XMLMenuFactory(menubar);
        
        /* Build the toolbars */
        Element toolbars = XmlUtil.getElement(m_root, "toolbars");	// m_root.getChild("toolbars")
        XMLToolBarFactory toolFac = new XMLToolBarFactory(toolbars);
        
        /* Complete the build of the UI */
        Map<String, JToolBar> toolBars = toolFac.getToolBars();
        ArrayList<String> toolBarsList = toolFac.getToolBarsList();
        //            toollist = new ArrayList(toolBars.values());
        m_toolBarList = new ArrayList<JToolBar>();
        
        
        
        // In order to preserve the order of the toolbar icons being displayed the value
        // from the list is used as the key in the hashmap instead of iterating
        // thru the values of the hashmap
        Iterator<String> iter = toolBarsList.iterator();
        while ( iter.hasNext() )
        {
            String toolbarName = iter.next();
            JToolBar toolbar = (JToolBar) toolBars.get(toolbarName);
            m_toolBarList.add(toolbar);
        }
        m_menuBar = menuFac.getMenuBar();
        
    }
    
    public JToolBar[] getToolBars()
    {
        JToolBar[] retVal = new JToolBar[m_toolBarList.size()];
        return m_toolBarList.toArray(retVal);
    }
    
    public JMenuBar getMenuBar()
    {
        return m_menuBar;
    }
    
    
    /** Get the attribute value from this element. Returns null if attribute is not
     * defined */
    public static String getValue(Element node, String attrName) {
        String value = null;
        
//        Attribute attr = node.getAttribute(attrName);
//        if (attr != null) {
//            value = attr.getValue();
//        }
        Attr attr = node.getAttributeNode(attrName);
        if (attr != null) {
        	value = attr.getValue();
        }
        
        return value;
    }
    
    public void registerActions() throws InstantiationException {
        Action action = null;
//      Element child = null;
//        List actions = m_root.getChild("actions").getChildren();
//        for ( int i=0; i < actions.size();i++ )
//        {
//            child = (Element) actions.get(i);


		Element actions = XmlUtil.getElement(m_root, "actions");
		ArrayList<Element> children = XmlUtil.getElements(actions);
		for (Element child: children) {

            String className = getValue(child, "class");
            String name = getValue(child, "name");
            /* test to see if the class actually exists*/
            if (className != null) {
                try {
                    Class.forName(className);
                }  catch ( ClassNotFoundException e ) {
                    String msg = "Class '" + className
                            + "' for action '" + name
                            + "' can not be located ";
                    AbstractAction.logActionError(msg, e);
                    
                    throw new InstantiationException(e.getMessage());
                }
            } else {
                String msg = "Action, '" + name
                        + "', does not have an associated class ";
                
                InstantiationException err = new InstantiationException(msg);
                AbstractAction.logActionError(err);
                throw err;
            }
                
            ActionRegistry.bindAction(name, className);
            
            action = ActionRegistry.getActionInstance(name);
            setActionProperties(action, child);

            
            String enabled = getValue(child, "enabled");
            if ( "false".equals(enabled) ) {
                action.setEnabled(false);
            }
        }
        
    }
    
    
    private void setActionProperties(Action action, Element node)
    {
//        List properties = node.getChildren("property");
//        Element child = null;
//        for ( int i=0; i < properties.size();i++ )
//        {
//            child = (Element) properties.get(i);

		ArrayList<Element> children = XmlUtil.getElements(m_root, "property");
		for (Element child: children) {
            if ( child.getAttributeNode("key") != null && child.getAttributeNode("value") != null ) {
                action.putValue(child.getAttribute("key"), child.getAttribute("value"));
            }
        }
    }
    
    
}
