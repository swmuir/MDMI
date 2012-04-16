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

import java.util.Collection;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.SemanticElementSet;

public class SemanticElementSetNode extends EditableObjectNode {
	private Collection<NewObjectInfo> m_newObjectInfo = null;

	public SemanticElementSetNode(SemanticElementSet elemSet) {
		super(elemSet);
		setNodeIcon(TreeNodeIcon.SemanticElementSetIcon);
		
		loadChildren(elemSet);
	}

	private void loadChildren(SemanticElementSet elemSet) {
		// Semantic Elements
		Collection<SemanticElement> semanticElements = elemSet.getSemanticElements();
		if (semanticElements != null) {
			for (SemanticElement elem : semanticElements) {
				addSorted(new SemanticElementNode(elem));
			}
		}
	}

	@Override
	public String getDisplayName(Object userObject) {
		return s_res.getString("SemanticElementSetNode.semanticElementSet");
	}

	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public boolean isRemovable() {
		return false;
	}


	@Override
	public void deleteChild(MutableTreeNode child) {
		// remove from parent model
		SemanticElement model = (SemanticElement)((DefaultMutableTreeNode)child).getUserObject();
		// remove from element Set
		((SemanticElementSet)getUserObject()).getSemanticElements().remove(model);
		// remove from parent too
		if (model.getParent() != null) {
			model.getParent().getChildren().remove(model);
		}
		
		
		super.remove(child);
	}

	
	/** What new items can be created */
	@Override
	public Collection<NewObjectInfo> getNewObjectInformation(boolean changeType) {
		if (m_newObjectInfo == null) {
			m_newObjectInfo = super.getNewObjectInformation(changeType);
			m_newObjectInfo.add(new NewSemanticElement());
		}
		
		return m_newObjectInfo;
	}

	//////////////////////////////////////////////////////////////////
	//    Custom Classes
	//////////////////////////////////////////////////////////////
	
	public class NewSemanticElement extends NewObjectInfo {

		@Override
		public EditableObjectNode addNewChild(Object childObject) {
			SemanticElementSet parentSet = (SemanticElementSet)getUserObject();
			SemanticElement element = (SemanticElement)childObject;
			
			parentSet.addSemanticElement(element);
			element.setElementSet(parentSet);
			
			return new SemanticElementNode(element);
		}

		@Override
		public Class<?> getChildClass() {
			return SemanticElement.class;
		}

		@Override
		public String getChildName(Object childObject) {
			return ((SemanticElement)childObject).getName();
		}

		@Override
		public void setChildName(Object childObject, String newName) {
			((SemanticElement)childObject).setName(newName);
		}
	}

}
