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

import java.util.ArrayList;
import java.util.List;

import org.openhealthtools.mdht.mdmi.editor.map.editor.TabContents;
import org.openhealthtools.mdht.mdmi.model.validate.ModelInfo;

/** Validation information link */
public class ValidationErrorLink extends LinkedObject {
	private ModelInfo m_errorInfo;
	
	/** Create a link to this target object */
	public ValidationErrorLink(ModelInfo error) {
		super(error.getObject(), error.getField());
		m_errorInfo = error;
	}
	
	/** Get the Error information */
	public ModelInfo getModelInfo() {
		return m_errorInfo;
	}

	@Override
	public TabContents openTarget() {
		TabContents editor = super.openTarget();
		
		if (editor != null) {
			// validate and highlight errors
			List<ModelInfo> errors = new ArrayList<ModelInfo>();
			errors.add(getModelInfo());
			editor.getEditor().highlightErrors( errors );
		}
		return editor;
	}
	
}
