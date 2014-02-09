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

import org.openhealthtools.mdht.mdmi.model.SemanticElement;

/** Node for a collection of Semantic Element rules */
public class SemanticElementRuleSetNode extends EditableObjectNode {
	private static final String s_display = s_res.getString("SemanticElementRuleSetNode.rules");
	
	public SemanticElementRuleSetNode(SemanticElement semanticElement) {
		super(semanticElement);
		setDisplayType(s_display);
		setNodeIcon(TreeNodeIcon.DataRuleSetIcon);
	}
	
	@Override
	public String getDisplayName(Object userObject) {
		return s_display;
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public boolean isRemovable() {
		return false;
	}

}
