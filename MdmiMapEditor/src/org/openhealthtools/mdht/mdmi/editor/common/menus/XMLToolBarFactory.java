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
 * XMLToolBarFactory.java
 *
 * Created on June 18, 2002, 10:59 AM
 */

package org.openhealthtools.mdht.mdmi.editor.common.menus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;

import org.w3c.dom.Element;

import org.openhealthtools.mdht.mdmi.editor.common.actions.AbstractAction;
import org.openhealthtools.mdht.mdmi.editor.common.actions.ActionRegistry;
import org.openhealthtools.mdht.mdmi.util.XmlUtil;

/**
 *
 * @author  TPerkins
 */
public class XMLToolBarFactory extends AbstractXMLConfigurationFactory{

	private HashMap<String, JToolBar> toolbars;
	private ArrayList<String> toolbarsList_;

	/** Creates a new instance of XMLMenuBarFactory */
	public XMLToolBarFactory(Element root)  throws IOException {
		super(root);
	}

	@Override
	public void init() {
		toolbars = new HashMap<String, JToolBar>();
		toolbarsList_ = new ArrayList<String>();
	}


	public Map<String, JToolBar>  getToolBars() {
		return toolbars;
	}

	public ArrayList<String> getToolBarsList()
	{
		return toolbarsList_;
	}

	@Override
	public void build() throws Exception {
		//        Element child = null;
		//        List children = root.getChildren();
		//        for (int i=0; i < children.size(); i++){
		//            child = (Element) children.get(i);

		ArrayList<Element> children = XmlUtil.getElements(root);
		for (Element child: children) {
			String toolbarName = child.getAttribute("id");
			JToolBar toolbar = buildToolBar(child);
			if (toolbar != null && toolbar.getComponentCount() > 0) {
				toolbars.put(toolbarName, toolbar);
				toolbarsList_.add(toolbarName);
			}
		}
	}

	public JToolBar buildToolBar(Element toolNode){
		Action action = null;
		JToolBar toolBar = null;
		String name = null;
		try {
			String actionClassName = XMLApplicationFactory.getValue(toolNode, "action");
			action = ActionRegistry.getActionInstance(actionClassName);
			toolBar = new JToolBar((String)action.getValue(Action.NAME),JToolBar.HORIZONTAL);
			//            List children = toolNode.getChildren();
			//            for (int i=0; i < children.size(); i++){
			//                child = (Element) children.get(i);

			ArrayList<Element> children = XmlUtil.getElements(toolNode);
			for (Element child: children) {

				if (child == null) {
					continue;
				}

				name = child.getNodeName();	//getName();
				if ("separator".equalsIgnoreCase(name)){
					//  don't add a separator if the previous item is a separator
					int count = toolBar.getComponentCount();
					if (count > 0) {
						Object prev = toolBar.getComponent(count-1);
						if (prev instanceof JButton) {
							toolBar.addSeparator();
						}
					}

				} else if ("button".equalsIgnoreCase(name)){
					JButton button = buildButton(child);
					if (button != null) {
						toolBar.add(button);
					}
				}
			}
			return toolBar;

		} catch (Exception e){
			AbstractAction.logActionError("Cannot load toolbar", e);
			return new JToolBar("Fatal Error loading toolbar");
		}
	}

	public JButton buildButton(Element buttonNode){
		JButton item = null;
		Action action = null;
		try {
			String actionKeyName = XMLApplicationFactory.getValue(buttonNode, "action");
			action = ActionRegistry.getActionInstance(actionKeyName);

			item = new JButton(action);
			item.setToolTipText(item.getText());
			item.setText("");
			boolean borderPainted = false;  // default
			if (buttonNode.getAttribute("border") != null) {
				if ("true".equals(buttonNode.getAttribute("border"))) {
					borderPainted = true;
				}
			}
			item.setBorderPainted(borderPainted);
			return item;
		} catch (Exception e){
			AbstractAction.logActionError("Cannot load JButton", e);
			return new JButton("Fatal error loading JButton");
		}
	}



}
