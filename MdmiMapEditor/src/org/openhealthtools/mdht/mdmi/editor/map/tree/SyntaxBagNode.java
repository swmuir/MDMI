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

import org.openhealthtools.mdht.mdmi.model.Bag;
import org.openhealthtools.mdht.mdmi.model.Choice;
import org.openhealthtools.mdht.mdmi.model.LeafSyntaxTranslator;
import org.openhealthtools.mdht.mdmi.model.Node;

public class SyntaxBagNode extends SyntaxNodeNode {
	private Collection<NewObjectInfo> m_newObjectInfo = null;

	public SyntaxBagNode(Bag bag) {
		super(bag);
		setNodeIcon(TreeNodeIcon.SyntaxBagIcon);
		loadChildren(bag);
	}

	private void loadChildren(Bag bag) {
		for (Node child : bag.getNodes()) {
			addSorted(createSyntaxNode(child));
		}	
	}


	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public boolean isRemovable() {
		return true;
	}



	/** children of this node are not sorted */
	@Override
	public boolean showChildrenSorted() {
		return false;
	}

	@Override
	public int moveChildNode(EditableObjectNode childNode, int amt) {
		int oldIdx = getIndex(childNode);
		int newIdx = super.moveChildNode(childNode, amt);
		
		if (newIdx != oldIdx) {
			Node child = (Node)childNode.getUserObject();
			// change model
			Bag bag = (Bag)getUserObject();
			bag.getNodes().remove(oldIdx);
			bag.getNodes().add(newIdx, child);
		}
		
		return newIdx;
	}

	@Override
	public void deleteChild(MutableTreeNode child) {
		// remove from parent model
		Node model = (Node)((DefaultMutableTreeNode)child).getUserObject();
		((Bag)getUserObject()).getNodes().remove(model);
		
		super.remove(child);
	}

	
	/** Can a node be dragged from its current position and dropped into this node */
	@Override
	public boolean canDrop(EditableObjectNode newChild) {
		return (newChild.getUserObject() instanceof Node);
	}

	/** What new items can be created */
	@Override
	public Collection<NewObjectInfo> getNewObjectInformation(boolean changeType) {
		if (m_newObjectInfo == null) {
			m_newObjectInfo = super.getNewObjectInformation(changeType);
			m_newObjectInfo.add(new NewBag());
			m_newObjectInfo.add(new NewChoice());
			m_newObjectInfo.add(new NewLeaf());
		}

		return m_newObjectInfo;
	}



	//////////////////////////////////////////////////////////////////
	//    Custom Classes
	//////////////////////////////////////////////////////////////

	public class NewBag extends AbstractNewBag  {
		@Override
		public void addNode(Bag node) {
			Bag parent = (Bag)getUserObject();
			parent.addNode(node);
			node.setParentNode(parent);
		}
	}
	
	public class NewChoice extends AbstractNewChoice {
		@Override
		public void addNode(Choice node) {
			Bag parent = (Bag)getUserObject();
			parent.addNode(node);
			node.setParentNode(parent);
		}
	}
	
	public class NewLeaf extends AbstractNewLeaf {
		@Override
		public void addNode(LeafSyntaxTranslator node) {
			Bag parent = (Bag)getUserObject();
			parent.addNode(node);
			node.setParentNode(parent);
		}
	}

}
