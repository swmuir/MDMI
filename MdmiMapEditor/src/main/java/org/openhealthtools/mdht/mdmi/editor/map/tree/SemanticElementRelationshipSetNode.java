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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.openhealthtools.mdht.mdmi.editor.map.tools.ViewSemanticElement;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ViewSemanticElementRelationships;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.SemanticElementRelationship;

/** Node for a collection of SemanticElementRelationships */
public class SemanticElementRelationshipSetNode extends EditableObjectNode {
	private static final String s_display = s_res.getString("SemanticElementRelationshipSetNode.relationships");

	private Collection<NewObjectInfo> m_newObjectInfo = null;

	private SemanticElement m_element = null;
	
	public SemanticElementRelationshipSetNode(SemanticElement elem) {
		super(elem);
		setDisplayType(s_display);
		m_element = elem;
		setNodeIcon(TreeNodeIcon.SemanticElementRelationshipSetIcon);
		
		loadChildren(elem);
	}

	private void loadChildren(SemanticElement elem) {
		// add Relationships
		for (SemanticElementRelationship relationship : elem.getRelationships()) {
			EditableObjectNode relNode = new SemanticElementRelationshipNode(relationship);
			addSorted(relNode);
		}
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

	
	/** Delete this child from the tree, and from it's model parent */
	@Override
	public void deleteChild(MutableTreeNode child) {
		// remove relationship from semantic element
		SemanticElementRelationship rel = (SemanticElementRelationship)((DefaultMutableTreeNode)child).getUserObject();
		m_element.getRelationships().remove(rel);
		
		super.remove(child);
	}
	
	
	/** What new items can be created */
	@Override
	public Collection<NewObjectInfo> getNewObjectInformation(boolean changeType) {
		if (m_newObjectInfo == null) {
			m_newObjectInfo = super.getNewObjectInformation(changeType);
			m_newObjectInfo.add(new NewSemanticElementRelationship());
		}
		
		return m_newObjectInfo;
	}
	/** Add a menu to show the semantic element in a new view */
	@Override
	public List<JComponent> getAdditionalPopuMenus() {
		List<JComponent> menus = super.getAdditionalPopuMenus();
		if (menus == null) {
			menus = new ArrayList<JComponent>();
		}
		final SemanticElement semanticElement = (SemanticElement)getUserObject();
		// View Relationships
		if (semanticElement.getRelationships() != null && semanticElement.getRelationships().size() > 0) {
			menus.add(new JMenuItem(new AbstractAction(s_res.getString("SemanticElementNode.viewRelationships")) {
				@Override
				public void actionPerformed(ActionEvent e) {
					ViewSemanticElement view = new ViewSemanticElementRelationships(semanticElement);
					view.setVisible(true);
				}
				
			}));
		}
		return menus;
	}
	
	
	///////////////////////////////////////////////////////////////
	//    Custom Classes
	//////////////////////////////////////////////////////////////
	
	public class NewSemanticElementRelationship extends NewObjectInfo {

		@Override
		public EditableObjectNode addNewChild(Object childObject) {
			SemanticElementRelationship rel = (SemanticElementRelationship)childObject;
			m_element.addRelationship(rel);
			rel.setContext(m_element);
			
			return new SemanticElementRelationshipNode(rel);
		}

		@Override
		public Class<?> getChildClass() {
			return SemanticElementRelationship.class;
		}

		@Override
		public String getChildName(Object childObject) {
			return ((SemanticElementRelationship)childObject).getName();
		}

		@Override
		public void setChildName(Object childObject, String newName) {
			((SemanticElementRelationship)childObject).setName(newName);
		}
	}

}
