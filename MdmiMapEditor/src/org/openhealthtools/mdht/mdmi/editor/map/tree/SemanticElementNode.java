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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.StatusPanel;
import org.openhealthtools.mdht.mdmi.editor.map.console.ReferenceLink;
import org.openhealthtools.mdht.mdmi.editor.map.editor.AbstractComponentEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.DataEntryFieldInfo;
import org.openhealthtools.mdht.mdmi.editor.map.editor.GenericEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.IEditorField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.SemanticElementField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.SyntaxNodeField;
import org.openhealthtools.mdht.mdmi.editor.map.tools.GenerateToFromElementsDialog;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ViewDatatypeSyntax;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ViewSemanticElement;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ViewSemanticElementFromTo;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ViewSemanticElementHeirarchy;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ViewSemanticElementRelationships;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.Node;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;

public class SemanticElementNode extends EditableObjectNode {
	/* -->  right arrow */
	public static final char RIGHT_ARROW  = '\u2192';
	/* <--  left arrow */
	public static final char LEFT_ARROW   = '\u2190';
	/* <--> double arrow */
	public static final char DOUBLE_ARROW = '\u2194';
	/* ... elipses */
	public static final char DASHES = '-';

	// need to save parent element in case user changes it
	private SemanticElement m_parentElement = null;


	public SemanticElementNode(SemanticElement elem) {
		super(elem);
		setNodeIcon(TreeNodeIcon.SemanticElementIcon);
		
		loadChildren(elem);
	}
	
	private void loadChildren(SemanticElement elem) {
		
		// Data Rules
		DefaultMutableTreeNode dataRulesNode = new DataRuleSetNode(elem);
		add(dataRulesNode);

		// Business Elements
		DefaultMutableTreeNode bizElemsTitleNode = new ToBusinessElementSetNode(elem);
		add(bizElemsTitleNode);
		
		// Message Elements
		DefaultMutableTreeNode msgElemsTitleNode = new ToMessageElementSetNode(elem);
		add(msgElemsTitleNode);
		
		// Relationships
		DefaultMutableTreeNode relationshipsNode = new SemanticElementRelationshipSetNode(elem);
		add(relationshipsNode);
		
		// Later...
//		// Business Rules
//		DefaultMutableTreeNode bizRulesNode = new SemanticElementBusinessRuleSetNode(elem);
//		add(bizRulesNode);
		
	}
	
	@Override
	public void setUserObject(Object userObject) {
		super.setUserObject(userObject);

		SemanticElement element = (SemanticElement)userObject;
		SemanticElement parentElement = element.getParent();
		
		// need to update parent
		if (m_parentElement != parentElement) {
			// remove from old parent
			if (m_parentElement != null) {
				m_parentElement.getChildren().remove(element);
			}
			
			// add to new parent
			if (parentElement != null && !parentElement.getChildren().contains(element)) {
				parentElement.addChild(element);
			}

			m_parentElement = parentElement;	
		}

		Node syntaxNode = element.getSyntaxNode();
		if (syntaxNode != null) {
			// make sure syntax node points here
			if (syntaxNode.getSemanticElement() != element) {
				SemanticElement previousElement = syntaxNode.getSemanticElement();
				// change syntax node to point here
				syntaxNode.setSemanticElement(element);
				
				// update node display(s) to reflect semantic element
				MdmiModelTree entitySelector = SelectionManager.getInstance().getEntitySelector();
				DefaultTreeModel model = (DefaultTreeModel)entitySelector.getMessageElementsTree().getModel();
				DefaultMutableTreeNode syntaxNodeNode = entitySelector.findNode(syntaxNode);
				if (syntaxNodeNode != null) {
					model.nodeChanged(syntaxNodeNode);
				}

				StatusPanel statusPanel = SelectionManager.getInstance().getStatusPanel();
				
				// This change has broken the link between one or more elements:
				String brokenLinkMsg = s_res.getString("EditableObjectNode.brokenLink");
				boolean messageShown = false;
				
				int count = 1;
				
				// produce warning for old semantic element
				if (previousElement != null && previousElement.getSyntaxNode() == syntaxNode) {
					EditableObjectNode prevElemeNode = (EditableObjectNode)entitySelector.findNode(previousElement);
					if (prevElemeNode != null) {
						prevElemeNode.setHasError(true);	// mark node with an error
					}
					// show error if previous element still references syntax node
					statusPanel.writeErrorText(brokenLinkMsg);
					messageShown = true;
					ReferenceLink link = new ReferenceLink(previousElement, getDisplayName(previousElement));
					link.addReferredToObject(syntaxNode);

					// Broken Link: Semantic Element <LINK> references Syntax Node ''nodeName'', 
					//  but Syntax Node ''nodeName'' does not refer back to Semantic Element 'elemName'
					String preMsg = MessageFormat.format(s_res.getString("EditableObjectNode.brokenLinkPreFormat"), 
							MdmiModelTree.formatItemNumber(count++), getDisplayType());
					String postMsg = MessageFormat.format(s_res.getString("EditableObjectNode.brokenLinkPostFormat"),
							ClassUtil.beautifyName(syntaxNode.getClass()), syntaxNode.getName(),
							getDisplayType(), getDisplayName(previousElement));
					
					statusPanel.writeErrorLink(preMsg, link, postMsg);
				}

				// warn if any syntax nodes reference this semantic element
				List <EditableObjectNode> references = entitySelector.findReferences(this);
				for (EditableObjectNode node : references) {
					if (node instanceof SyntaxNodeNode && 
							((Node)node.getUserObject()).getSemanticElement() == element) {
						// show error
						if (!messageShown) {
							statusPanel.writeErrorText(brokenLinkMsg);
							messageShown = true;
						}

						if (node.getUserObject() == syntaxNode) {
							// skip the one we just updated
							continue;
						}
						node.setHasError(true);	// mark node with an error
						ReferenceLink link = new ReferenceLink(node.getUserObject(), node.getDisplayName());
						link.addReferredToObject(element);

						// Broken Link: Syntax Node <LINK> references Semantic Element 'name', 
						//  but Semantic Element 'name' does not refer back to Syntax Node 'name'
						String preMsg = MessageFormat.format(s_res.getString("EditableObjectNode.brokenLinkPreFormat"), 
								MdmiModelTree.formatItemNumber(count++), node.getDisplayType());
						String postMsg = MessageFormat.format(s_res.getString("EditableObjectNode.brokenLinkPostFormat"),
								getDisplayType(), getDisplayName(element),
								node.getDisplayType(), node.getDisplayName());

						statusPanel.writeErrorLink(preMsg, link, postMsg);

					}
				}
				
				// notify listeners to the syntax node
				SelectionManager.getInstance().notifyModelChangeListeners(syntaxNode);
			}
		}
	}


	@Override
	public String getDisplayName(Object userObject) {
		return ((SemanticElement)userObject).getName();
	}

	@Override
	public String getToolTipText() {
		return ((SemanticElement)getUserObject()).getDescription();
	}

	
	@Override
	public String toString() {
		// Show as:
		//  SemanticElementName <-> SyntaxNodeName
		SemanticElement element = (SemanticElement)getUserObject();
		String displayName = getDisplayName();
		
		if (element.getSyntaxNode() != null) {
			String syntaxNodeName = element.getSyntaxNode().getName();
			char arrow = ' ';
			boolean fromMdmiExists = exists(element.getFromMdmi());
			boolean toMdmiExists = exists(element.getToMdmi());
			if (fromMdmiExists && toMdmiExists) {
				// <-->
				arrow = DOUBLE_ARROW ;
			} else if (toMdmiExists) {
				// <---
				arrow = LEFT_ARROW ;
			} else if (fromMdmiExists) {
				// --->
				arrow = RIGHT_ARROW ;
			} else {
				// ...  
				arrow = DASHES ;
			}
			displayName = displayName + " " + arrow + " " + syntaxNodeName; 
		}
		return displayName;
	}
	
	/** A collection exists if it is non-null, and is not empty */
	private static boolean exists(Collection<?> collection) {
		return collection != null && !collection.isEmpty();
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

	@Override
	public DefaultMutableTreeNode addSorted(DefaultMutableTreeNode childNode) {
		// want to add after the Business/Message/Relationships
		int idx = getChildCount();
		String childName = childNode.toString();
		for (int i=0; i<getChildCount(); i++) {
			TreeNode nodeAt_i = getChildAt(i);
			if (nodeAt_i instanceof ToBusinessElementSetNode ||
					nodeAt_i instanceof ToMessageElementSetNode ||
					nodeAt_i instanceof SemanticElementRelationshipSetNode ||
					nodeAt_i instanceof SemanticElementBusinessRuleSetNode) {
				// skip over these
				continue;
			}
			String nodeName = nodeAt_i.toString();
			if (childName.compareToIgnoreCase(nodeName) < 0) {
				idx = i;
				break;
			}
		}
		
		insert(childNode, idx);
		return childNode;
	}



	/** Add a menu to show the semantic element in a new view */
	@Override
	public List<JComponent> getAdditionalPopuMenus() {
		List<JComponent> menus = super.getAdditionalPopuMenus();
		if (menus == null) {
			menus = new ArrayList<JComponent>();
		}
		
		final SemanticElement semanticElement = (SemanticElement)getUserObject();
		
		// Generate From/To Elements
		menus.add(new JMenuItem(new AbstractAction(s_res.getString("SemanticElementNode.generateToFromElements")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				Frame frame = SystemContext.getApplicationFrame();
				GenerateToFromElementsDialog dlg = new GenerateToFromElementsDialog(frame, semanticElement);
				dlg.display(frame);
			}
			
		}));
		
		// View Datatype/Syntax Nodes
		if (semanticElement.getSyntaxNode() != null && semanticElement.getDatatype() != null
				&& semanticElement.getDatatype() instanceof DTComplex) {
			menus.add(new JMenuItem(new AbstractAction("View Syntax Tree") {
				@Override
				public void actionPerformed(ActionEvent e) {
					ViewDatatypeSyntax view = new ViewDatatypeSyntax(semanticElement);
					view.setVisible(true);
				}

			}));
		}
		
		// View From/To Elements
		menus.add(new JMenuItem(new AbstractAction(s_res.getString("SemanticElementNode.viewElements")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				ViewSemanticElement view = new ViewSemanticElementFromTo(semanticElement);
				view.setVisible(true);
			}
			
		}));
		
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
		
		// View Children
		if (semanticElement.getChildren() != null && semanticElement.getChildren().size() > 0) {
			menus.add(new JMenuItem(new AbstractAction(s_res.getString("SemanticElementNode.viewChildren")) {
				@Override
				public void actionPerformed(ActionEvent e) {
					ViewSemanticElement view = new ViewSemanticElementHeirarchy(semanticElement);
					view.setVisible(true);
				}
				
			}));
		}
		
		return menus;
	}

	//////////////////////////////////////////////////////////////////
	//    Custom Classes
	//////////////////////////////////////////////////////////////
	public class CustomEditor extends AbstractModelChangeEditor {
		public CustomEditor(MessageGroup group, Class<?> objectClass) {
			super(group, objectClass);
		}

		/** Use a custom combo box for parent values */
		@Override
		protected IEditorField createEditorField(DataEntryFieldInfo fieldInfo) {
			if ("Parent".equalsIgnoreCase(fieldInfo.getFieldName())) {
				// Use a special selector that filters out this SemanticElement
				return new SemanticElementSelector(this);
				
			} else if ("SyntaxNode".equalsIgnoreCase(fieldInfo.getFieldName()) &&
				Node.class.isAssignableFrom(fieldInfo.getReturnType())) {
				SyntaxNodeField syntaxField = new SyntaxNodeField(this);
				// set model context
				SemanticElement semanticElement = (SemanticElement)getUserObject();
				syntaxField.setSemanticElement(semanticElement);
				return syntaxField;
			}
			return super.createEditorField(fieldInfo);
		}
		
	}

	protected class SemanticElementSelector extends SemanticElementField {

		public SemanticElementSelector(GenericEditor parentEditor) {
			super(parentEditor);
		}

		@Override
		protected Collection<? extends Object> getComboBoxData() {
			// remove any entries that contain this SemanticElement 
			SemanticElement thisElement = (SemanticElement)getUserObject();
			
			Collection<? extends Object> data = super.getComboBoxData();
			for (Iterator<? extends Object> iter = data.iterator(); iter.hasNext();) {
				Object item = iter.next();
				if (item instanceof SemanticElement) {
					if (SemanticElementField.hasParentLoop((SemanticElement)item, true)) {
						iter.remove();
					} else if (isInPath(thisElement, (SemanticElement)item)) {
						iter.remove();
					}
				}
			}
			return data;
		}
		
		/** Check if the provided semantic element is in the path of the path element */
		public boolean isInPath(SemanticElement element, SemanticElement pathElement) {
			if (element == pathElement) {
				return true;
			}
			
			// recursion error
			ArrayList<SemanticElement> elementsCovered = new ArrayList<SemanticElement>();
			// check all elements on path
			while (pathElement.getParent() != null) {
				pathElement = pathElement.getParent();
				if (elementsCovered.contains(pathElement)) {
					// we're in trouble
					return true;
				}
				elementsCovered.add(pathElement);
				if (element == pathElement) {
					return true;
				}
			}
			return false;
		}
		
	}
//	
//	public class NewSemanticElement extends NewObjectInfo {
//
//		@Override
//		public EditableObjectNode addNewChild(Object childObject) {
//			SemanticElement parent = (SemanticElement)getUserObject();
//			SemanticElement element = (SemanticElement)childObject;
//			
//			parent.addChild(element);
//			element.setParent(parent);
//			element.setElementSet(parent.getElementSet());
//
//			return new SemanticElementNode(element);
//		}
//
//		@Override
//		public Class<?> getChildClass() {
//			return SemanticElement.class;
//		}
//
//		@Override
//		public String getChildName(Object childObject) {
//			return ((SemanticElement)childObject).getName();
//		}
//
//		@Override
//		public void setChildName(Object childObject, String newName) {
//			((SemanticElement)childObject).setName(newName);
//		}
//	}

}
