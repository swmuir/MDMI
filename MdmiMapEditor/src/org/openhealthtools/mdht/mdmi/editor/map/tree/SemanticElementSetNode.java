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
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.UserPreferences;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.SemanticElementSet;

public class SemanticElementSetNode extends EditableObjectNode {
	// flat/hierarchy key
    public static final String SHOW_HIERARCHY = "showHierachy";
    
	private Collection<NewObjectInfo> m_newObjectInfo = null;
	
	private boolean m_showHierarchy = true;
	private boolean m_showSERules = true;
	
	private FlatHierarchyAction m_flatAction = null;
	private FlatHierarchyAction m_hierarchicalAction = null;

	public SemanticElementSetNode(SemanticElementSet elemSet) {
		this(elemSet,
				UserPreferences.getInstance(SystemContext.getApplicationName(), null).getBooleanValue(SHOW_HIERARCHY, false),
				false);
	}

    
	public SemanticElementSetNode(SemanticElementSet elemSet, boolean showHierarchy, boolean showSERules) {
		super(elemSet);
		setNodeIcon(TreeNodeIcon.SemanticElementSetIcon);
		
		m_showHierarchy = showHierarchy;
		m_showSERules = showSERules;
		loadChildren(elemSet);
	}

	private void loadChildren(SemanticElementSet elemSet) {
		// Semantic Elements
		Collection<SemanticElement> semanticElements = elemSet.getSemanticElements();
		if (semanticElements != null) {
			if (!m_showHierarchy) {
				// simply add them
				for (SemanticElement elem : semanticElements) {
					addSorted(new SemanticElementNode(elem, false, m_showSERules));
				}
			} else {
				HierarchicalSemanticElementHelper helper = new HierarchicalSemanticElementHelper();
				helper.createTree(this, semanticElements, m_showSERules);
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
	

	public boolean isHierarchical() {
		return m_showHierarchy;
	}
	
	private void setHierarchical(boolean hierarchical) {
		// change
		if (hierarchical != m_showHierarchy) {
			m_showHierarchy = hierarchical;
			
			// save it
			UserPreferences userPreference = UserPreferences.getInstance(SystemContext.getApplicationName(), null);
			userPreference.putBooleanValue(SHOW_HIERARCHY, hierarchical);
			
			// we need to reload children
			SemanticElementSet elemSet = (SemanticElementSet)getUserObject();
			removeAllChildren();
			loadChildren(elemSet);
			
			MdmiModelTree entitySelector = SelectionManager.getInstance().getEntitySelector();
			entitySelector.refreshNode(this);
		}
	}

	@Override
	public void deleteChild(MutableTreeNode child) {
		// remove from parent model
		SemanticElement se = (SemanticElement)((DefaultMutableTreeNode)child).getUserObject();
		// remove from element Set
		se.getElementSet().getSemanticElements().remove(se);
		// remove from parent too
		if (se.getParent() != null) {
			se.getParent().getChildren().remove(se);
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
	


	/** Add a menu to show the semantic elements either flat or hierarchical */
	@Override
	public List<JComponent> getAdditionalPopuMenus() {
		List<JComponent> menus = super.getAdditionalPopuMenus();
		if (menus == null) {
			menus = new ArrayList<JComponent>();
		}

		// Flat
		if (m_flatAction == null) {
			Icon flatIcon = TreeNodeIcon.getIcon(s_res.getString("SemanticElementSetNode.flatIcon"));
			m_flatAction = new FlatHierarchyAction(s_res.getString("SemanticElementSetNode.orderedFlat"), flatIcon, !m_showHierarchy);
		}
		
		// Hierarchical
		if (m_hierarchicalAction == null) {
			Icon hierarchicalIcon = TreeNodeIcon.getIcon(s_res.getString("SemanticElementSetNode.hierarchicalIcon"));
			m_hierarchicalAction = new FlatHierarchyAction(s_res.getString("SemanticElementSetNode.orderedHierarchical"), hierarchicalIcon, m_showHierarchy);
		}
		
		menus.add(new JRadioButtonMenuItem(m_flatAction));
		menus.add(new JRadioButtonMenuItem(m_hierarchicalAction));
		
		return menus;
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
			
			return new SemanticElementNode(element, m_showHierarchy);
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
	
	private class FlatHierarchyAction extends AbstractAction {

		public FlatHierarchyAction(String name, Icon icon, boolean state) {
			super(name, icon);
			putValue(SELECTED_KEY, state);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Boolean sel = (Boolean)getValue(SELECTED_KEY);

			// toggle selection
			// tell the other action
			boolean isHierarchical = false;
			if (FlatHierarchyAction.this == m_flatAction) {
				isHierarchical = !sel; 
				m_hierarchicalAction.putValue(SELECTED_KEY, isHierarchical);
				
			} else if (FlatHierarchyAction.this == m_hierarchicalAction) {
				isHierarchical = sel;
				m_flatAction.putValue(SELECTED_KEY, !isHierarchical);
			}

			// redo display
			setHierarchical(isHierarchical);
			
		}
		
	}

}
