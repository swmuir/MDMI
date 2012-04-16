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


/** Listener for additions/deletions/changes to a collection of objects of a particular
 * class.
 * @author Conway
 */
public interface CollectionChangeListener {
	/**
	 * Get the type of item the listener is expecting information on
	 * @return
	 */
	public Class<?> getListenForClass();
	
   /**
    * Invoked when the target of the listener has changed its contents.
    *
    * @param e  a CollectionChangeEvent object
    */
   public void contentsChanged(CollectionChangeEvent e);

}
