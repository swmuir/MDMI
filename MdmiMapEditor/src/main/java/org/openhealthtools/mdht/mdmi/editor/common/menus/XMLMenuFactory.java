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
 * XMLMenuBarFactory.java
 *
 * Created on June 17, 2002, 1:59 PM
 */

package org.openhealthtools.mdht.mdmi.editor.common.menus;

import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import org.openhealthtools.mdht.mdmi.editor.common.actions.AbstractAction;
import org.openhealthtools.mdht.mdmi.editor.common.actions.ActionRegistry;
import org.openhealthtools.mdht.mdmi.util.XmlUtil;


/**
 *
 * @author  TPerkins
 */
public class XMLMenuFactory extends AbstractXMLConfigurationFactory{

    private JMenuBar m_menuBar;
    private Font m_defaultFont;
    
    private boolean m_debugOn     = false;    // to turn on, set to true in the init() method
    private int     m_debugLevel  = 0;
    

    /** Creates a new instance of XMLMenuBarFactory */
    public XMLMenuFactory(Element root) throws IOException {
        super(root);
    }


    public JMenuBar getMenuBar(){
        return m_menuBar;
    }

    @Override
    public void init(){
//        m_debugOn = true;
        m_menuBar = new JMenuBar();
        m_defaultFont = new Font("Dialog", 0, 12);
    }

    @Override
    public void build() throws Exception{
//        Element child = null;
//        List children = root.getChildren();
//        for (int i=0; i < children.size(); i++){
        ArrayList<Element> children = XmlUtil.getElements(root);
        for (Element child: children) {
//        	child = (Element)children.get(i);
        	JMenu menu = buildMenu(child);
        	if (menu != null && menu.getMenuComponentCount() > 0) {
        		m_menuBar.add(menu);
        	}
        }
    }
    
    private void debug(Element node) {
        if (!m_debugOn) {
            return;
        }
        try {
        	String actionClassName = XMLApplicationFactory.getValue(node, "action");
            Action action = ActionRegistry.getActionInstance(actionClassName);
            String actionName = (String)action.getValue(Action.NAME);
            KeyStroke accKey = (KeyStroke)action.getValue(Action.ACCELERATOR_KEY);
            Integer mn = (Integer)action.getValue(Action.MNEMONIC_KEY);
            
            for (int i=0; i<m_debugLevel; i++) {
                System.out.print("    ");
            }
            System.out.print(actionName);
            if (mn != null) {
                System.out.print("\tmnemonic = " + (char)mn.intValue());
            } else {
                System.out.print("\t");
            }
            if (accKey != null) {
                System.out.print("\taccel = " + accKey.toString().replaceAll("keyCode ", "").replaceAll("-P",""));
            }
            System.out.println();
        } catch (Exception ex) {
            
         }
    }

    public JMenu buildMenu(Element menuNode)
    {
        Action action = null;
        String type = null;
        JMenu menu = null;
        String name = null;
        String defaultValue = null;
        
        debug(menuNode);
        try
        {
            String actionClassName = XMLApplicationFactory.getValue(menuNode, "action");
            action = ActionRegistry.getActionInstance(actionClassName);

            if (action == null)
            {
            	String msg = "Action is null for value '" + actionClassName + "'.";
                AbstractAction.logActionError(new IllegalArgumentException(msg));
                return null;
            }

            menu = new JMenu(action);
            menu.setName(actionClassName);
            menu.setFont(m_defaultFont);
            
            ButtonGroup bg = new ButtonGroup();
            
//            List children = menuNode.getChildren();
//            for (int i=0; i < children.size(); i++)
//          child = (Element) children.get(i);
            
            ArrayList<Element> children = XmlUtil.getElements(menuNode);
            for (Element child: children) {
                name = child.getNodeName();	//child.getName();

                if ("separator".equalsIgnoreCase(name))
                {
                   // don't add a separator if the previous item is a separator
                    int count = menu.getMenuComponentCount();
                    if (count > 0) {
                       Object prev = menu.getMenuComponent(count-1);
                       if (prev instanceof JMenuItem) {
                          menu.addSeparator();
                       }
                    }
                    // start a new button group
                    bg = new ButtonGroup();
                }
                else if ("menu".equalsIgnoreCase(name))
                {
                    m_debugLevel++;
                    JMenu childMenu = buildMenu(child);
                    if (childMenu != null && childMenu.getMenuComponentCount() > 0) {
                        menu.add(childMenu);
                    }
                    m_debugLevel--;
                }
                else if ("item".equalsIgnoreCase(name))
                {
                    m_debugLevel++;
                    debug(child);
                    m_debugLevel--;
                    
                    type = XMLApplicationFactory.getValue(child, "type");
                    
                    boolean flag = false;
//                    Attribute dvAttr = child.getAttribute("defaultValue");
                    Attr dvAttr = child.getAttributeNode("defaultValue");
                    if (dvAttr != null)
                    {
                        defaultValue = dvAttr.getValue();
                        if ("true".equals(defaultValue))
                        {
                            flag = true;
                        }
                    }

                    if ("radio".equals(type))
                    {
                        JRadioButtonMenuItem radioItem = buildRadioMenuItem(child, flag);
                        if (radioItem != null) {
                           bg.add(radioItem);
                           menu.add(radioItem);
                        }
                    }
                    else if ("checkbox".equals(type))
                    {
                       JMenuItem checkItem = buildCheckBoxMenuItem(child, flag);
                       if (checkItem != null) {
                          menu.add(checkItem);
                       }
                    }
                    else
                    {
                        JMenuItem menuItem = buildMenuItem(child);
                        if (menuItem != null) {
                           menu.add(menuItem);
                        }
                    }
                }
            }
            if (menu == null){
            	String msg = "Menu is null";
                AbstractAction.logActionError(new IllegalArgumentException(msg));
            }
            
            // if last item is a separator, remove it
            int count = menu.getMenuComponentCount();
            if (count > 0 && !(menu.getMenuComponent(count-1) instanceof JMenuItem)) {
                  menu.remove(count-1);
            }
            return menu;
        } catch (Exception e) {
        	AbstractAction.logActionError("Exception while building menu", e);
            return new JMenu("Fatal Error loading menu");
        }
    }

    public JMenuItem buildMenuItem(Element menuNode)
    {
        String actionClassName = null;
        JMenuItem item = null;
        Action action = null;
        try
        {
           actionClassName = XMLApplicationFactory.getValue(menuNode, "action");
           action = ActionRegistry.getActionInstance(actionClassName);
           
           item = new JMenuItem(action);
           item.setFont(m_defaultFont);
           return item;
        }
        catch (Exception e)
        {
            AbstractAction.logActionError("Exception while building menu item for value '" + actionClassName + "'.", e);
            return new JMenuItem("Fatal error loading menu item for " + actionClassName);
        }
    }

    public JRadioButtonMenuItem buildRadioMenuItem(Element menuNode, boolean defaultValue)
    {
        JRadioButtonMenuItem item = null;
        Action action = null;

        try
        {
        	String actionClassName = XMLApplicationFactory.getValue(menuNode, "action");
            action = ActionRegistry.getActionInstance(actionClassName);
            
            item = new JRadioButtonMenuItem(action);
            item.setSelected(defaultValue);
            item.setFont(m_defaultFont);
            return item;
        }
        catch (Exception e)
        {
            return new JRadioButtonMenuItem("Fatal error loading menu item");
        }
    }
    
    public JCheckBoxMenuItem buildCheckBoxMenuItem(Element menuNode, boolean defaultValue)
    {
    	JCheckBoxMenuItem item = null;
    	Action action = null;

    	try
    	{
    		String actionClassName = XMLApplicationFactory.getValue(menuNode, "action");
    		action = ActionRegistry.getActionInstance(actionClassName);

    		item = new JCheckBoxMenuItem(action);
    		item.setSelected(defaultValue);
    		item.setFont(m_defaultFont);
    		return item;
    	}
    	catch (Exception e)
    	{
    		return new JCheckBoxMenuItem("Fatal error loading menu item");
    	}
    }

}
