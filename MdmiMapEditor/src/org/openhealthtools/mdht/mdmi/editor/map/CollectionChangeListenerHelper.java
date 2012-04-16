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
package org.openhealthtools.mdht.mdmi.editor.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNode;


/** Helper for additions/deletions/changes to a collection of objects of a particular
 * class.
 * @author Conway
 */
public class CollectionChangeListenerHelper {

	private List<Class<?>> m_changedClasses = new ArrayList<Class<?>>();
	private Collection<CollectionChangeListener> m_changeListeners;
	
	public CollectionChangeListenerHelper() {
		super();
		m_changeListeners = SelectionManager.getInstance().getChangeListeners();
	}

	/** Note that this class will need a notification */
	public void needNotification(Class<?> clazz) {
		// is there a listener?
		for (CollectionChangeListener listener : m_changeListeners) {
			Class<?> listenerClass = listener.getListenForClass();
			if (listenerClass.isAssignableFrom(clazz)) {
				if (!m_changedClasses.contains(listenerClass)) {
					m_changedClasses.add(listenerClass);
				}
			}
		}
	}
	
	/** Note that all classes at this node, and its children need a notificaiton */
	public void addAllNodes(EditableObjectNode node) {
		for (Enumeration<?> en = node.preorderEnumeration(); en != null && en.hasMoreElements();) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode)en.nextElement();
			Class<?> objectClass = child.getUserObject().getClass();
			needNotification(objectClass);
		}
	}
	
	/** Notify CollectionChange listeners for all classes */
	public void notifyListeners() {
		for (Class<?>changedClass : m_changedClasses) {
			SelectionManager.getInstance().notifyCollectionChangeListeners(changedClass);
		}
	}
}
