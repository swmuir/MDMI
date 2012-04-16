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

import java.awt.GridBagConstraints;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.openhealthtools.mdht.mdmi.editor.map.CollectionChangeEvent;
import org.openhealthtools.mdht.mdmi.editor.map.CollectionChangeListener;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.editor.AbstractComponentEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.AdvancedListField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.GenericEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.SemanticElementField;
import org.openhealthtools.mdht.mdmi.model.ConversionRule;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.SemanticElementRelationship;

public class BusinessElementReferenceNode extends EditableObjectNode {

	public BusinessElementReferenceNode(MdmiBusinessElementReference bizElemRef) {
		super(bizElemRef);
		setNodeIcon(TreeNodeIcon.BusinessElementReferenceIcon);
		
		loadChildren(bizElemRef);
	}

	private void loadChildren(MdmiBusinessElementReference bizElemRef) {
		// Business Element Rules
		// later
//		DefaultMutableTreeNode bizElemsTitleNode = new BusinessElementRuleSetNode(bizElemRef);
//		add(bizElemsTitleNode);	
	}

	@Override
	public String getDisplayName(Object userObject) {
		return ((MdmiBusinessElementReference)userObject).getName();
	}

	@Override
	public String getToolTipText() {
		return ((MdmiBusinessElementReference)getUserObject()).getDescription();
	}


	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public boolean isRemovable() {
		return true;
	}

	@Override
	public AbstractComponentEditor getEditorForNode() {
		return new CustomEditor(getMessageGroup(), getUserObject().getClass());
	}
	
	///////////////////////////////////////////////////////////////
	// Custom Editor adds a read-only display of Semantic Elements
	// using this reference
	//
	public class CustomEditor extends GenericEditor {
		private SemanticElementReference m_semanticElementField;

		public CustomEditor(MessageGroup group, Class<?> objectClass) {
			super(group, objectClass);
		}
		
		@Override
		protected void createDataEntryFields(List<Method[]> methodPairList) {
			super.createDataEntryFields(methodPairList);
			
			// Add a field to show all Semantic Elements that reference this one
			// add to layout 
			m_semanticElementField = new SemanticElementReference(null);
			addLabeledField("Associated Semantic Elements",
					m_semanticElementField, 0.0, GridBagConstraints.HORIZONTAL);

		}
	}
	
	// Semantic elements that reference this node
	public class SemanticElementReference extends AdvancedListField implements CollectionChangeListener {

		public SemanticElementReference(GenericEditor parentEditor) {
			super(parentEditor);
		}
		
		@Override
		public void addNotify() {
			super.addNotify();
			// Add a second collection change listener for SemanticElementRelationship changes
			SelectionManager.getInstance().addCollectionChangeListener(this);
		}

		@Override
		public void removeNotify() {
			super.removeNotify();
			SelectionManager.getInstance().removeCollectionChangeListener(getListenForClass(), this);
		}

		@Override
		public Class<?> getDataClass() {
			// We can only listen for changes on one class type, so the 
			//   SemanticElementRelationship will be handled in a different listener
			return ConversionRule.class;
		}

		@Override
		protected Collection<? extends Object> getListData() {
			// Find all Semantic Elements that have components referencing this BusinessElementReferenceNode
			// Find all the sememantic elements
			ArrayList<SemanticElement> elements = new ArrayList<SemanticElement>();

			List<EditableObjectNode> references = 
				SelectionManager.getInstance().getEntitySelector().findReferences(BusinessElementReferenceNode.this);
			
			// find Semantic Element parent
			for (EditableObjectNode treeNode : references) {
				// if node is a semantic element, or has a semantic element parent, add it
				DefaultMutableTreeNode node = treeNode;
				while (node != null)
				{
					Object userObject = node.getUserObject();
					if (userObject instanceof SemanticElement)
					{
						if (!elements.contains(userObject))
						{
							elements.add((SemanticElement)userObject);
						}
						break;
					}
					node = (DefaultMutableTreeNode)node.getParent();
				}
			}
			
			return elements;
		}

		@Override
		protected String getToolTipText(Object listObject) {
			if (listObject instanceof SemanticElement) {
				SemanticElement element = (SemanticElement)listObject;
				return element.getDescription();
			}
			return null;
		}

		@Override
		protected String toString(Object listObject) {
			if (listObject instanceof SemanticElement) {
				return SemanticElementField.makeString((SemanticElement)listObject);
			}
			return listObject.toString();
		}

		/////////////////////////////////////
		// CollectionChangeListener methods
		////////////////////////////////////
		@Override
		public void contentsChanged(CollectionChangeEvent e) {
			refreshSelections();
		}

		@Override
		public Class<?> getListenForClass() {
			return SemanticElementRelationship.class;
		}


	}
}
