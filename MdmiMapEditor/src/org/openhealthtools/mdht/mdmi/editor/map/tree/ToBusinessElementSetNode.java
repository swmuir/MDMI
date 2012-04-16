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
import org.openhealthtools.mdht.mdmi.editor.map.tools.ViewSemanticElementFromTo;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.ToBusinessElement;

/** Node for a collection of SemanticElement ToBusinessElements */
public class ToBusinessElementSetNode extends EditableObjectNode {
	private static String s_display = s_res.getString("ToBusinessElementSetNode.fromMdmis");
	
	private Collection<NewObjectInfo> m_newObjectInfo = null;

	private SemanticElement m_element = null;
	
	public ToBusinessElementSetNode(SemanticElement elem) {
		super(elem);
		setDisplayType(s_display);
		m_element = elem;
		setNodeIcon(TreeNodeIcon.ToBusinessElementSetIcon);

		loadChildren(elem);
	}
	
	private void loadChildren(SemanticElement elem) {
		// ToBusinessElements
		Collection<ToBusinessElement> toBizElems = elem.getFromMdmi();
		for (ToBusinessElement bizElem : toBizElems) {
			EditableObjectNode bizElemNode = new ToBusinessElementNode(bizElem);
			addSorted(bizElemNode);
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


	@Override
	public void deleteChild(MutableTreeNode child) {
		// remove from parent model
		ToBusinessElement model = (ToBusinessElement)((DefaultMutableTreeNode)child).getUserObject();
		m_element.getFromMdmi().remove(model);
		
		super.remove(child);
	}

	
	/** What new items can be created */
	@Override
	public Collection<NewObjectInfo> getNewObjectInformation(boolean changeType) {
		if (m_newObjectInfo == null) {
			m_newObjectInfo = super.getNewObjectInformation(changeType);
			m_newObjectInfo.add(new NewToBusinessElement());
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
		// View From/To Elements
		menus.add(new JMenuItem(new AbstractAction(s_res.getString("SemanticElementNode.viewElements")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				ViewSemanticElement view = new ViewSemanticElementFromTo(semanticElement);
				view.setVisible(true);
			}
			
		}));
		return menus;
	}
	
	///////////////////////////////////////////////////////////////
	//    Custom Classes
	//////////////////////////////////////////////////////////////
	
	public class NewToBusinessElement extends NewObjectInfo {
		public NewToBusinessElement() {
//			super(s_res.getString("ToBusinessElementSetNode.fromMdmi"));
		}

		@Override
		public EditableObjectNode addNewChild(Object childObject) {
			ToBusinessElement fromMdmi = (ToBusinessElement)childObject;
			m_element.addFromMdmi(fromMdmi);
			
			fromMdmi.setOwner(m_element);
			
			return new ToBusinessElementNode(fromMdmi);
		}

		@Override
		public Class<?> getChildClass() {
			return ToBusinessElement.class;
		}

		@Override
		public String getChildName(Object childObject) {
			return ((ToBusinessElement)childObject).getName();
		}

		@Override
		public void setChildName(Object childObject, String newName) {
			((ToBusinessElement)childObject).setName(newName);
		}
	}
}
