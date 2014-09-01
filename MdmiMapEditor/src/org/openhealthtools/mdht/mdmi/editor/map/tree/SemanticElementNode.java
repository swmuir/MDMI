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

import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.StatusPanel;
import org.openhealthtools.mdht.mdmi.editor.map.console.ReferenceLink;
import org.openhealthtools.mdht.mdmi.editor.map.editor.AbstractComponentEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.DataEntryFieldInfo;
import org.openhealthtools.mdht.mdmi.editor.map.editor.DataFormatException;
import org.openhealthtools.mdht.mdmi.editor.map.editor.GenericEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.IEditorField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.NestedEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.SemanticElementField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.SyntaxNodeField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.ValueSetData;
import org.openhealthtools.mdht.mdmi.editor.map.tools.GenerateComputedInValuesDialog;
import org.openhealthtools.mdht.mdmi.editor.map.tools.GenerateToFromElementsDialog;
import org.openhealthtools.mdht.mdmi.editor.map.tools.GenerateToFromRelationshipRuleDialog;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ViewDatatypeSyntax;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ViewSemanticElement;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ViewSemanticElementFromTo;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ViewSemanticElementHeirarchy;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ViewSemanticElementRelationships;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.MdmiExpression;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.Node;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.SemanticElementSet;

public class SemanticElementNode extends EditableObjectNode {
	private Collection<NewObjectInfo> m_newObjectInfo = null;
	
	/* -->  right arrow */
	public static final char RIGHT_ARROW  = '\u2192';
	/* <--  left arrow */
	public static final char LEFT_ARROW   = '\u2190';
	/* <--> double arrow */
	public static final char DOUBLE_ARROW = '\u2194';
	/* ... Ellipses */
	public static final char DASHES = '-';

	// need to save parent element in case user changes it
	private SemanticElement m_parentElement = null;
	private boolean m_isHierarchical = false;

	// default constructor
	public SemanticElementNode(SemanticElement elem) {
		this(elem, SemanticElementSetNode.getDefaultHierarchySetting(), true);
	}
	
	public SemanticElementNode(SemanticElement elem, boolean isHierarchical) {
		this(elem, isHierarchical, true);
	}
	
	public SemanticElementNode(SemanticElement elem, boolean isHierarchical, boolean showRules) {
		super(elem);
		setNodeIcon(TreeNodeIcon.SemanticElementIcon);
		
		m_isHierarchical = isHierarchical;
		loadChildren(elem, showRules);
		m_parentElement = elem.getParent();
	}
	
	private void loadChildren(SemanticElement elem, boolean showRules) {
		EditableObjectNode topNode = this;

		if (showRules) {
			// if hierarchical, use an intermediate node for the rules
			if (isHierarchical()) {
				topNode = new SemanticElementRuleSetNode(elem);
				add(topNode);
			}
		
			// Data Rules
			DefaultMutableTreeNode dataRulesNode = new DataRuleSetNode(elem);
			topNode.add(dataRulesNode);

			// Business Elements
			DefaultMutableTreeNode bizElemsTitleNode = new ToBusinessElementSetNode(elem);
			topNode.add(bizElemsTitleNode);

			// Message Elements
			DefaultMutableTreeNode msgElemsTitleNode = new ToMessageElementSetNode(elem);
			topNode.add(msgElemsTitleNode);

			// Relationships
			DefaultMutableTreeNode relationshipsNode = new SemanticElementRelationshipSetNode(elem);
			topNode.add(relationshipsNode);

			// Later...
			//		// Business Rules
			//		DefaultMutableTreeNode bizRulesNode = new SemanticElementBusinessRuleSetNode(elem);
			//		topNode.add(bizRulesNode);
		}
		
	}
	
	@Override
	public void setUserObject(Object userObject) {
		super.setUserObject(userObject);
		
		MdmiModelTree entitySelector = SelectionManager.getInstance().getEntitySelector();
		DefaultTreeModel treeModel = (DefaultTreeModel)entitySelector.getMessageElementsTree().getModel();

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

			// we'll need to change the tree too
			if (isHierarchical()) {
				// remove from old node
				EditableObjectNode oldParentNode = (EditableObjectNode)getParent();
				oldParentNode.remove(this);
				treeModel.nodeStructureChanged(oldParentNode);
				
				// add to new node
				DefaultMutableTreeNode newParentNode = entitySelector.findNode(parentElement);
				if (newParentNode instanceof EditableObjectNode) {
					((EditableObjectNode)newParentNode).addSorted(this);
					treeModel.nodeStructureChanged(newParentNode);
				}
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
				DefaultMutableTreeNode syntaxNodeNode = entitySelector.findNode(syntaxNode);
				if (syntaxNodeNode != null) {
					treeModel.nodeChanged(syntaxNodeNode);
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

	public SemanticElement getSemanticElement() {
		return (SemanticElement)getUserObject();
	}

	@Override
	public String getDisplayName(Object userObject) {
		return ((SemanticElement)userObject).getName();
	}

	@Override
	public String getToolTipText() {
		return getSemanticElement().getDescription();
	}
	
	//Get the parent SemanticElementSetNode
	SemanticElementSetNode getSemanticElementSetNode() {
		TreeNode parent = getParent();
		if (parent instanceof SemanticElementSetNode) {
			// got it
			return (SemanticElementSetNode)parent;
		} else if (parent instanceof SemanticElementNode) {
			// walk up the tree
			return ((SemanticElementNode)parent).getSemanticElementSetNode();
		}
		
		return null;
	}

	
	@Override
	public String toString() {
		// Show as:
		//  SemanticElementName <-> SyntaxNodeName
		SemanticElement element = getSemanticElement();
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
		// don't allow removal if in hierarchical view - there are issues with showing
		// broken parent/child relationships
		if (isHierarchical()) {
			return false;
		}
		return true;
	}

	@Override
	public void deleteChild(MutableTreeNode child) {
		// remove from parent model
		Object childObject = ((DefaultMutableTreeNode)child).getUserObject();
		if (childObject instanceof SemanticElement) {
			SemanticElement se = (SemanticElement)childObject;
			// remove from element Set
			se.getElementSet().getSemanticElements().remove(se);
			// remove from parent SE too
			if (se.getParent() != null) {
				se.getParent().getChildren().remove(se);
			}
		}
		
		
		super.remove(child);
	}
	
	/** What new items can be created */
	@Override
	public Collection<NewObjectInfo> getNewObjectInformation(boolean changeType) {
		if (isHierarchical()) {
			if (m_newObjectInfo == null) {
				m_newObjectInfo = super.getNewObjectInformation(changeType);
				m_newObjectInfo.add(new NewSemanticElement());
			}
		} else {
			m_newObjectInfo = null;
		}

		return m_newObjectInfo;
	}


	public boolean isHierarchical() {
		return m_isHierarchical;
	}

	//////////////////////////////////////////////////////////////////
	//    Custom Classes
	//////////////////////////////////////////////////////////////
	
	public class NewSemanticElement extends NewObjectInfo {

		@Override
		public EditableObjectNode addNewChild(Object childObject) {
			SemanticElement parentElement = getSemanticElement();
			SemanticElementSet elementSet = parentElement.getElementSet();
			
			SemanticElement childElement = (SemanticElement)childObject;
			
			// Add to SemanticElementSet
			elementSet.addSemanticElement(childElement);
			childElement.setElementSet(elementSet);
			
			// Add to SemanticElement
			parentElement.addChild(childElement);
			childElement.setParent(parentElement);
			
			return new SemanticElementNode(childElement, isHierarchical());
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
					nodeAt_i instanceof SemanticElementBusinessRuleSetNode ||
					nodeAt_i instanceof SemanticElementRuleSetNode) {
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


	
	/** override copying the user object to set the syntax node to null */
	@Override
	public Object copyUserObject() throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Object userObjectCopy = super.copyUserObject();

		// clear syntax node
		SemanticElement se = (SemanticElement)userObjectCopy;
		se.setSyntaxNode(null);
		
		return userObjectCopy;
	}

	/** Add a menu to show the semantic element in a new view */
	@Override
	public List<JComponent> getAdditionalPopuMenus() {
		List<JComponent> menus = super.getAdditionalPopuMenus();
		if (menus == null) {
			menus = new ArrayList<JComponent>();
		}
		
		final SemanticElement semanticElement = getSemanticElement();
		
		// Generate From/To Elements
		menus.add(new JMenuItem(new AbstractAction(s_res.getString("SemanticElementNode.generateToFromElements")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				Frame frame = SystemContext.getApplicationFrame();
				GenerateToFromElementsDialog dlg = new GenerateToFromElementsDialog(frame, semanticElement);
				dlg.display(frame);
			}
			
		}));
		
		// Generate From/To Relationships
		JMenuItem menuItem = new JMenuItem(new AbstractAction(s_res.getString("SemanticElementNode.generateToFromRelationships")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				Frame frame = SystemContext.getApplicationFrame();
				GenerateToFromRelationshipRuleDialog dlg = new GenerateToFromRelationshipRuleDialog(frame, semanticElement, true);
				dlg.display(frame);
			}
			
		});
		// disable if no relationships
		if (semanticElement.getRelationships().isEmpty()) {
			menuItem.setEnabled(false);
		}
		menus.add(menuItem);
		
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
		private ValueSetData m_valueSetditorField;
		
		public CustomEditor(MessageGroup group, Class<?> objectClass) {
			super(group, objectClass);
		}

		/** Use a custom combo box for parent values */
		@Override
		protected IEditorField createEditorField(DataEntryFieldInfo fieldInfo) {
			String fieldName = fieldInfo.getFieldName();
			if ("Parent".equalsIgnoreCase(fieldName)) {
				// Use a special selector that filters out this SemanticElement
				return new SemanticElementSelector(this);
				
			} else if ("SyntaxNode".equalsIgnoreCase(fieldName) &&
				Node.class.isAssignableFrom(fieldInfo.getReturnType())) {
				SyntaxNodeField syntaxField = new SyntaxNodeField(this);
				// set model context
				SemanticElement semanticElement = getSemanticElement();
				syntaxField.setSemanticElement(semanticElement);
				return syntaxField;
				
			} else if ("ComputedInValue".equalsIgnoreCase(fieldName)) {
				return new ComputedInEditor(this, MdmiExpression.class, 
						ClassUtil.beautifyName(fieldName));
				
			} else if (fieldName.startsWith("EnumValue")) {
				// we'll handle these with the ValueSetData component
				return null;
			}
			return super.createEditorField(fieldInfo);
		}


		/** We need to create a special field for setting the EnumVale fields */
		@Override
		protected void createDataEntryFields(List<Method[]> methodPairList) {
			super.createDataEntryFields(methodPairList);
			
			// create data entry field for ValueSets
			m_valueSetditorField = new ValueSetData(this, getMessageGroup(),
					getSemanticElement().getDatatype());

			// add to layout 
			addFieldFullWidth(m_valueSetditorField, GridBagConstraints.HORIZONTAL);
			addDataEntryFieldInfo(m_valueSetditorField.getDataEntryFieldInfo());
		}

		/** Find the field that has an error, and highlight it with a red line
		 * @param errorMsg
		 */
		@Override
		public void highlightFieldWithError(String fieldName) {
			//  handle EnumValues
			if (fieldName.startsWith("enumValue")) {
				m_valueSetditorField.highlightFieldWithError(fieldName);
			}
			super.highlightFieldWithError(fieldName);
		}

		@Override
		public void highlightField(Object value, Color highlightColor) {
			// if value is a Field, we need to highlight the field name
			if (value instanceof Field) {
				try {
					m_valueSetditorField.highlightText(((Field)value).getName(), highlightColor);
				} catch (Exception e) {
					// ignore
				}
			} else {
				super.highlightField(value, highlightColor);
			}
		}
	}
	
	protected class ComputedInEditor extends NestedEditor {
		private JButton m_wizButton;
		public ComputedInEditor(GenericEditor parentEditor, Class<?> objectClass, String fieldName) {
			super(parentEditor, objectClass, fieldName);
			
			// add a "generate" button
			m_wizButton = new JButton();	// just use icon with tooltip
			m_wizButton.setIcon(AbstractComponentEditor.getIcon(this.getClass(),
					SemanticElementNode.s_res.getString("SemanticElementNode.generatedComputedInIcon")));
			getButtonPanel().add(m_wizButton);
			
			// disable button if SE doesn't have a computedIn value or dataType
			if (getSemanticElement().getComputedInValue() == null ||
					getSemanticElement().getDatatype() == null) {
				m_wizButton.setEnabled(false);
			}
		}

		@Override
		public void addNotify() {
			super.addNotify();
			m_wizButton.addActionListener(this);
			m_wizButton.setToolTipText(SemanticElementNode.s_res.getString("SemanticElementNode.generatedComputedInToolTip"));
		}


		@Override
		public void removeNotify() {
			m_wizButton.removeActionListener(this);
			m_wizButton.setToolTipText(null);
			super.removeNotify();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == m_wizButton) {
				// display Generate Computed In Values wizard
				Frame frame = SystemContext.getApplicationFrame();
				// make sure SE has a dataType
				if (getSemanticElement().getDatatype() == null) {
					JOptionPane.showMessageDialog(frame,
							SemanticElementNode.s_res.getString("SemanticElementNode.missingDataTypeMsg"),
							SemanticElementNode.s_res.getString("SemanticElementNode.missingDataType"),
							JOptionPane.ERROR_MESSAGE);
				} else {
					GenerateComputedInValuesDialog dlg = new GenerateComputedInValuesDialog(frame, getSemanticElement(),
							(MdmiExpression)getObject());
					dlg.setComputedInEditor(this.getEditor());
					dlg.display(frame);
				}
			} else {
				super.actionPerformed(e);
			}
		}

		@Override
		public void setDisplayValue(Object value) throws DataFormatException {
			// enable/disable wiz button
			m_wizButton.setEnabled(value instanceof MdmiExpression);
			super.setDisplayValue(value);
		}
		
	}

	protected class SemanticElementSelector extends SemanticElementField {

		public SemanticElementSelector(GenericEditor parentEditor) {
			super(parentEditor, getSemanticElement());
		}

		@Override
		protected Collection<? extends Object> getComboBoxData() {
			// remove any entries that contain this SemanticElement 
			SemanticElement thisElement = getSemanticElement();
			
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


}
