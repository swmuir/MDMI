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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.tree.MutableTreeNode;

import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.editor.AbstractComponentEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.GenericEditor;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ModelIOUtilities;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SyntaxNodeNode.AbstractNewBag;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SyntaxNodeNode.AbstractNewChoice;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SyntaxNodeNode.AbstractNewLeaf;
import org.openhealthtools.mdht.mdmi.model.Bag;
import org.openhealthtools.mdht.mdmi.model.Choice;
import org.openhealthtools.mdht.mdmi.model.LeafSyntaxTranslator;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.MessageSyntaxModel;

public class MessageSyntaxModelNode extends EditableObjectNode {
	private Collection<NewObjectInfo> m_newObjectInfo = null;

	public MessageSyntaxModelNode(MessageSyntaxModel syntaxModel) {
		super(syntaxModel);
		setNodeIcon(TreeNodeIcon.MessageSyntaxModelIcon);
		
		loadChildren(syntaxModel);
	}
	
	private void loadChildren(MessageSyntaxModel syntaxModel) {
		SyntaxNodeNode rootNode = SyntaxNodeNode.createSyntaxNode(syntaxModel.getRoot());
		if (rootNode != null) {
			addSorted(rootNode);
		}
	}

	@Override
	public String getDisplayName(Object userObject) {
		return ClassUtil.beautifyName(MessageSyntaxModel.class);
	}

	@Override
	public String getToolTipText() {
		return ((MessageSyntaxModel)getUserObject()).getDescription();
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
		((MessageSyntaxModel)getUserObject()).setRoot(null);
		
		super.remove(child);
	}

	
	/** What new items can be created */
	@Override
	public Collection<NewObjectInfo> getNewObjectInformation(boolean changeType) {
		if (m_newObjectInfo == null) {
			m_newObjectInfo = super.getNewObjectInformation(false);
			m_newObjectInfo.add(new NewBag());
			m_newObjectInfo.add(new NewChoice());
			m_newObjectInfo.add(new NewLeaf());
		}
		
		// ADD only allows one child
		if (changeType || getChildCount() == 0) {
			return m_newObjectInfo;
		} else {
			return new ArrayList<NewObjectInfo>();
		}
	}


	@Override
	public AbstractComponentEditor getEditorForNode() {
		return new CustomEditor(getMessageGroup());
	}

	/** Add a menu to show the semantic element in a new view */
	@Override
	public List<JComponent> getAdditionalPopuMenus() {
		List<JComponent> menus = super.getAdditionalPopuMenus();
		if (menus == null) {
			menus = new ArrayList<JComponent>();
		}
		
		// Add Import menu ONLY if there's no root yet
		if (this.getChildCount() == 0) {
			final MessageSyntaxModelNode syntaxModelNode = this;
			menus.add(new JMenuItem(new AbstractAction(s_res.getString("MessageSyntaxModelNode.importXMLSchema")) {
				@Override
				public void actionPerformed(ActionEvent e) {
					ModelIOUtilities.importSyntaxModelFromFile(syntaxModelNode);
				}
				
			}));
		}
		
		return menus;
	}

	//////////////////////////////////////////////////////////////////
	// Custom Classes
	//////////////////////////////////////////////////////////////////

	public class CustomEditor extends GenericEditor {
		public CustomEditor(MessageGroup group) {
			super(group, MessageSyntaxModel.class);
		}
		
		/** Determine if this field should be shown read-only */
		@Override
		public boolean isReadOnlyFields(String fieldName) {
			// Root is read-only
			return "Root".equalsIgnoreCase(fieldName);
		}
	}
	
	public class NewBag extends AbstractNewBag {
		@Override
		public void addNode(Bag node) {
			MessageSyntaxModel parent = (MessageSyntaxModel)getUserObject();
			parent.setRoot(node);
			node.setSyntaxModel(parent);
		}
	}
	
	public class NewChoice extends AbstractNewChoice {
		@Override
		public void addNode(Choice node) {
			MessageSyntaxModel parent = (MessageSyntaxModel)getUserObject();
			parent.setRoot(node);
			node.setSyntaxModel(parent);
		}
	}
	
	public class NewLeaf extends AbstractNewLeaf {
		@Override
		public void addNode(LeafSyntaxTranslator node) {
			MessageSyntaxModel parent = (MessageSyntaxModel)getUserObject();
			parent.setRoot(node);
			node.setSyntaxModel(parent);
		}
	}
}
