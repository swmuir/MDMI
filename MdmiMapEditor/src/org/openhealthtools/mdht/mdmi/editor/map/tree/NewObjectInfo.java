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
package org.openhealthtools.mdht.mdmi.editor.map.tree;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.tree.DefaultMutableTreeNode;

import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;

/** Wrapper for creating a new object - define the text to display,
 * and how to create it.
 * @author Conway
 *
 */
public abstract class NewObjectInfo {
	public String m_displayName;

	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tree.Local");

	public NewObjectInfo() {
		this.m_displayName = ClassUtil.beautifyName(getChildClass());
	}
	
	public NewObjectInfo(String objectName) {
		this.m_displayName = objectName;
	}
	
	public String getDisplayName() {
		return m_displayName;
	}

	/** Create a new child, add it to it's parent, and wrap it in a
	 * tree node. */
	public EditableObjectNode createNewChild() {
		EditableObjectNode childNode = null;
		Object child = createChildObject();
		if (child != null) {
			childNode = addNewChild(child);
		}
		return childNode;
	}
	
	/** Create the new child object */
	public Object createChildObject() {
		Object child = null;
		try {
			child = getChildClass().newInstance();
		
		} catch (Exception e) {
			String msg = MessageFormat.format(s_res.getString("EditableObjectNode.creatErrorFormat"),
					getChildClass());
			SelectionManager.getInstance().getStatusPanel().writeException(msg, e);
		}
		return child;
	}

	/** return the type of child that will be created */
	public abstract Class<?> getChildClass();
	
	/** Add this child object to its parent, and wrap it in a tree node */
	public abstract EditableObjectNode addNewChild(Object childObject);
	
	/** get the name of the object being added (or another child object) */
	public abstract String getChildName(Object childObject);
	
	/** set the name of the object being added */
	public abstract void setChildName(Object childObject, String newName);
	
	/** rename this child object with a unique name */
	protected void giveChildUniqueName(EditableObjectNode parentNode, Object childObject) {
		String originalName = getChildName(childObject);
		if (originalName == null || originalName.length() == 0) {
			return;
		}
		
		int copyNumber = 0;
		String newName = originalName;
		boolean nameUnique = false;
		
		while (!nameUnique) {
			nameUnique = true;	// until proven otherwise
			for (int i=0; i<parentNode.getChildCount(); i++) {
				if (parentNode.getChildAt(i) instanceof DefaultMutableTreeNode) {
					Object object = ((DefaultMutableTreeNode)parentNode.getChildAt(i)).getUserObject();
					if (object.getClass().equals(getChildClass())) {
						String name = getChildName(object);
						if (newName.equals(name)) {
							nameUnique = false;
							break;
						}
					}
				}
			}
			if (!nameUnique) {
				// change name - "CopyNOfName"
				newName = "Copy" + (copyNumber == 0 ? "" : String.valueOf(copyNumber))
				+ "Of" + originalName;
				copyNumber++;
			}
		}
		
		// rename
		if (!newName.equals(originalName)) {
			setChildName(childObject, newName);
		}
	}
}
