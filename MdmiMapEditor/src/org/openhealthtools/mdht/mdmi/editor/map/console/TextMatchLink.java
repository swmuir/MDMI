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

import org.openhealthtools.mdht.mdmi.editor.map.editor.TabContents;


/** text match information link */
public class TextMatchLink extends LinkedObject {
	public static final Color m_textMatchBackgroundColor = new Color(0xffffaa);	// pale yellow
	
	private String m_fieldName;
	private String m_searchText;
	
	/** Create a link to this target object */
	public TextMatchLink(Object target, String name, String fieldName, String searchText) {
		super(target, name);
		m_fieldName = fieldName;
		m_searchText = searchText;
	}

	/** Get the Field name */
	public String getFieldName() {
		return m_fieldName;
	}
	
	/** Get the Search Text */
	public String getSearchText() {
		return m_searchText;
	}

	@Override
	public TabContents openTarget() {
		// highlight text matches
		TabContents editor = super.openTarget();
		if (editor != null) {
			editor.getEditor().highlightField(m_fieldName, m_searchText, m_textMatchBackgroundColor);
		}
		return editor;
	}
	
}
