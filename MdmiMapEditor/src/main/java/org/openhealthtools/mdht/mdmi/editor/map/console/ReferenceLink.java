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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.openhealthtools.mdht.mdmi.editor.map.editor.TabContents;

/** Validation information link */
public class ReferenceLink extends LinkedObject {
	public static final Color s_referenceBackgroundColor = new Color(0xffff77);	// pale yellow
	
	private List<Object> m_referredToList;
	
	/** Create a link to this target object */
	public ReferenceLink(Object target, String name) {
		super(target, name);
		m_referredToList = new ArrayList<Object>();
	}
	
	/** Add a referred-to object */
	public void addReferredToObject(Object object) {
		m_referredToList.add(object);
	}
	
	/** Get the List of objects the target refers to */
	public List<Object> getReferringToList() {
		return m_referredToList;
	}

	@Override
	public TabContents openTarget() {
		// Show field(s) where there is a reference
		TabContents editor = super.openTarget();
		if (editor != null) {
			for (Object refersToObject : m_referredToList) {
				editor.getEditor().highlightField(refersToObject, s_referenceBackgroundColor);
			}
		}
		return editor;
	}
	
}
