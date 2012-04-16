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

import org.openhealthtools.mdht.mdmi.model.EnumerationLiteral;

public class EnumerationNode extends EditableObjectNode {

	public EnumerationNode(EnumerationLiteral literal) {
		super(literal);
		setNodeIcon(TreeNodeIcon.EnumerationIcon);
	}
	
	@Override
	public String getDisplayName(Object userObject) {
		return ((EnumerationLiteral)userObject).getName();
	}

	@Override
	public String getToolTipText() {
		return ((EnumerationLiteral)getUserObject()).getDescription();
	}


	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public boolean isRemovable() {
		return true;
	}
}
