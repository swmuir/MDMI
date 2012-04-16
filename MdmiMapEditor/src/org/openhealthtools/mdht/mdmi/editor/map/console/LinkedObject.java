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
package org.openhealthtools.mdht.mdmi.editor.map.console;

import javax.swing.tree.DefaultMutableTreeNode;

import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.editor.TabContents;

/** Information in a text link on the console window */
public class LinkedObject {
	private Object m_target;
	private String m_name;
	
	/** Create a link to this target object */
	public LinkedObject(Object target, String name) {
		this.m_target = target;
		this.m_name = name;
	}
	
	/** Get the target object */
	public Object getTarget() {
		return m_target;
	}
	
	/** Get the name of the target object */
	public String getName() {
		return m_name;
	}
	
	/** Find the target node in the tree
	 * 
	 * @return a node in the tree where the target is found
	 */
	public DefaultMutableTreeNode findTarget() {
		SelectionManager selectionManager = SelectionManager.getInstance();
		
		DefaultMutableTreeNode targetNode = 
			selectionManager.getEntitySelector().findNode(m_target);
		
		if (targetNode != null) {
			// select it (this will expand if necessary)
			selectionManager.getEntitySelector().selectNode(targetNode);
		}
		
		return targetNode;
	}
	
	/** Open the target node for editing (as well as show in tree)
	 * 
	 * @return	the TabContents of the editor
	 */
	public TabContents openTarget() {
		SelectionManager selectionManager = SelectionManager.getInstance();
		
		DefaultMutableTreeNode targetNode = findTarget();
		if (targetNode != null) {
			// open it
			return selectionManager.editItem(targetNode);
		}
		
		return null;
	}
}
