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
package org.openhealthtools.mdht.mdmi.editor.map.tools;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.editor.AdvancedSelectionField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.MdmiDatatypeField;
import org.openhealthtools.mdht.mdmi.editor.map.tools.TableViewer.ComboBoxRenderer;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNodeRenderer;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MdmiModelTree;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MessageGroupNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MessageModelNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MessageSyntaxModelNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SemanticElementNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SemanticElementSetNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SyntaxBagNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SyntaxNodeNode;
import org.openhealthtools.mdht.mdmi.model.Bag;
import org.openhealthtools.mdht.mdmi.model.Choice;
import org.openhealthtools.mdht.mdmi.model.LeafSyntaxTranslator;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.MessageModel;
import org.openhealthtools.mdht.mdmi.model.MessageSyntaxModel;
import org.openhealthtools.mdht.mdmi.model.Node;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.SemanticElementSet;

/** A dialog used for populating the information in a row of the Table Viewer
 * @author Conway
 *
 */
public class AddRowToTableViewerDialog extends BaseDialog implements TreeSelectionListener, DocumentListener {
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");

	private JTextField m_leafNodeName = new JTextField();
	private JTree m_tree;
	private JTextField m_semanticElementName = new JTextField();
	private JComboBox m_dataTypes = new JComboBox();
	private JComboBox m_businessElements = new JComboBox();
	
	private Node m_insertionPoint = null;
	private Node m_node;

	public AddRowToTableViewerDialog(Frame owner, Node insertionPoint) {
		super(owner, BaseDialog.OK_CANCEL_OPTION);
		setTitle(s_res.getString("AddRowToTableViewerDialog.title"));
		m_insertionPoint = insertionPoint;
		buildUI();
		pack(new Dimension(500, 300));
	}
	
	private void buildUI() {
		//  Leaf Name: [_______________________________]
		//  Parent Node:
		//   -------------------------------------
		//  |   Tree                              |
		//  |                                     |
		//   -------------------------------------
		// Semantic Element:           [______________] 
		// Semantic Element Data Type: [___________|v]|
		// Business Element: [___________|v]|
		
		JPanel main = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = Standards.getInsets();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		
		// Name
		main.add(new JLabel(s_res.getString("AddRowToTableViewerDialog.leafName")), gbc);
		gbc.gridx++;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		main.add(m_leafNodeName, gbc);
		
		m_leafNodeName.getDocument().addDocumentListener(this);
		
		// Tree
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
		DefaultTreeModel treeModel = new DefaultTreeModel(root);
		m_tree = new JTree(treeModel);
		
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		main.add(new JLabel(s_res.getString("AddRowToTableViewerDialog.leafParent")), gbc);
		
		gbc.gridy++;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		main.add(new JScrollPane(m_tree), gbc);
		
		// Fill Tree
		m_tree.setRootVisible(false);
		m_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		fillTree(treeModel);

		m_tree.setCellRenderer(new EditableObjectNodeRenderer());
		
		// Semantic Element
		gbc.gridwidth = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		main.add(new JLabel(s_res.getString("AddRowToTableViewerDialog.semanticElement")), gbc);
		gbc.gridx++;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		main.add(m_semanticElementName, gbc);
		
		// SE Data Type
		fillDataTypeSelector();
		m_dataTypes.setRenderer(new ComboBoxRenderer());
		
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.weightx = 0;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		main.add(new JLabel(s_res.getString("AddRowToTableViewerDialog.dataType")), gbc);
		gbc.gridx++;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		main.add(m_dataTypes, gbc);
		
		// Business Element
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		main.add(new JLabel(s_res.getString("AddRowToTableViewerDialog.businessElement")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		main.add(m_businessElements, gbc);
		
		TableViewer.fillBERefComboBox(m_businessElements);
		m_businessElements.setRenderer(new ComboBoxRenderer());
		
		getContentPane().add(main);
	}

	/** Fill in data type selector with all data types */
	private void fillDataTypeSelector() {
		m_dataTypes.removeAll();
		m_dataTypes.addItem(AdvancedSelectionField.BLANK_ENTRY);
		List<MessageGroup> messageGroups = SelectionManager.getInstance().getEntitySelector().getMessageGroups();
		for (MessageGroup group : messageGroups) {
			for (MdmiDatatype datatype : MdmiDatatypeField.getAllDatatypes(group, MdmiDatatype.class)) {
				m_dataTypes.addItem(datatype);
			}
		}
	}
	
	private void fillTree(final DefaultTreeModel treeModel) {
		final DefaultMutableTreeNode root = (DefaultMutableTreeNode)treeModel.getRoot();
		// if there are multiple groups, show them in the tree
		// [x] Group 1
		//    [x] Model 1
		//    [x] Model 2
		// [x] Group 2
		
		List<MessageGroup> messageGroups = SelectionManager.getInstance().getEntitySelector().getMessageGroups();
		// if single message group, with single model, start with MessageSyntaxModel
		if (messageGroups.size() == 1 && messageGroups.get(0).getModels().size() == 1) {
			MessageGroup group = messageGroups.get(0);
			for (MessageModel messageModel : group.getModels()) {
				MessageSyntaxModel syntaxModel = messageModel.getSyntaxModel();
				if (syntaxModel != null) {
					MessageSyntaxModelNode treeNode = new MessageSyntaxModelNode(syntaxModel);
					root.add(treeNode);
				}
			}
		} else {
			// add nodes for group and model
			for (MessageGroup group : messageGroups) {
				CustomMessageGroupNode groupNode = new CustomMessageGroupNode(group);
				root.add(groupNode);
			}
		}

		treeModel.nodeStructureChanged(root);
		

		// expand first 4 levels of children
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// pre-select
				if (m_insertionPoint != null && treeModel.getRoot() instanceof TreeNode) {
					DefaultMutableTreeNode treeNode = findNode((TreeNode)treeModel.getRoot(), m_insertionPoint);

					TreePath path = new TreePath(treeNode.getPath());
					DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)treeNode.getParent();
					if (parentNode != null) {
						TreePath parentPath = new TreePath(parentNode.getPath());

						m_tree.expandPath(parentPath);
					}
					m_tree.scrollPathToVisible(path);

					// select node
					m_tree.setSelectionPath(path);
				} else {
					expandChildren(treeModel, root, 4, 1); 
				}
			}
		});
		
		// listen to tree selection 
		m_tree.getSelectionModel().addTreeSelectionListener(this);
				
	}

	private void expandChildren(DefaultTreeModel treeModel, TreeNode node, 
			int maxDepth, int depth) {
		for (int i=0; i<node.getChildCount(); i++) {
			TreeNode child = node.getChildAt(i);
			m_tree.expandPath(new TreePath(((DefaultMutableTreeNode)node).getPath()));
			if (depth < maxDepth) {
				expandChildren(treeModel, child, maxDepth, depth+1);
			}
		}
	}
	
	private DefaultMutableTreeNode findNode(TreeNode root, Object userObject) {
		DefaultMutableTreeNode node = null;
		for (int i=0; i<root.getChildCount(); i++) {
			TreeNode child = root.getChildAt(i);
			if (child instanceof DefaultMutableTreeNode && 
					((DefaultMutableTreeNode)child).getUserObject() == userObject) {
				node = (DefaultMutableTreeNode)child;
			} else {
				node = findNode(child, userObject);
			}
			
			if (node != null) {
				break;
			}
		}
		return node;
	}

	@Override
	protected void okButtonAction() {
		SelectionManager selectionManager = SelectionManager.getInstance();
		MdmiModelTree entitySelector = selectionManager.getEntitySelector();
		DefaultTreeModel treeModel =
			(DefaultTreeModel)entitySelector.getMessageElementsTree().getModel();
		
		m_node = new LeafSyntaxTranslator();
		m_node.setName(m_leafNodeName.getText().trim());
		
		SyntaxNodeNode syntaxNodeTreeNode = SyntaxNodeNode.createSyntaxNode(m_node);
		
		Node parentSyntaxNode = getInsertionNode();
		EditableObjectNode parentTreeNode = (EditableObjectNode)entitySelector.findNode(parentSyntaxNode);

		// Create and add the Semantic Element
		if (m_semanticElementName.getText().trim().length() > 0) {
			// create a semantic element, and add it to the tree
			MessageGroup messageGroup = parentSyntaxNode.getSyntaxModel().getModel().getGroup();
			
			SemanticElement semanticElement = createSemanticElement(m_semanticElementName.getText().trim(),
					m_node, messageGroup);	
			
			// set the datatype
			Object selection = m_dataTypes.getSelectedItem();
			if (selection instanceof MdmiDatatype) {
				MdmiDatatype datatype = (MdmiDatatype)selection;
				semanticElement.setDatatype(datatype);
			}
		}
	
		
		// update model
		
		// if we've selected a leaf, add this one after it, otherwise
		// add as the first child (for Bag), or sorted (for Choice)
		int index = 0;
		if (parentSyntaxNode instanceof LeafSyntaxTranslator) {
			// get its parent
			Node parent = parentSyntaxNode.getParentNode();
			if (parent instanceof Bag) {
				index = ((Bag)parent).getNodes().indexOf(parentSyntaxNode);
				index++;	// want to add after the selected one
			}
			parentSyntaxNode = parent;
			parentTreeNode = (EditableObjectNode) parentTreeNode.getParent();
		}
		if (parentSyntaxNode instanceof Bag) {
			// Bag is not sorted - so use index to add
			((Bag)parentSyntaxNode).getNodes().add(index, m_node);
			m_node.setParentNode((Bag)parentSyntaxNode);
		} else if (parentSyntaxNode instanceof Choice) {
			// Choice is sorted
			((Choice)parentSyntaxNode).addNode(m_node);
			m_node.setParentNode((Choice)parentSyntaxNode);
		}
		
		// add syntax node to selection tree
		if (parentTreeNode instanceof SyntaxBagNode) {
			// add at index
			parentTreeNode.insert(syntaxNodeTreeNode, index);
		} else {
			parentTreeNode.addSorted(syntaxNodeTreeNode);
		}
		treeModel.nodeStructureChanged(parentTreeNode);

		// notify listeners
		selectionManager.notifyModelChangeListeners(parentSyntaxNode);
		selectionManager.notifyCollectionChangeListeners(m_node.getClass());
		
		// highlight in selectionManager
		entitySelector.selectNode(syntaxNodeTreeNode);
		
		super.okButtonAction();
	}

	/** Create a semantic element connected to the syntax node, and insert it in the tree. The SE datatype is not set */
	public static SemanticElement createSemanticElement(String seName, Node syntaxNode, MessageGroup messageGroup ) {

		SelectionManager selectionManager = SelectionManager.getInstance();
		MdmiModelTree entitySelector = selectionManager.getEntitySelector();
		DefaultTreeModel treeModel =
			(DefaultTreeModel)entitySelector.getMessageElementsTree().getModel();
		
		// create a semantic element
		SemanticElement semanticElement = new SemanticElement();
		semanticElement.setName(seName);
		
		// add node to SE
		if (syntaxNode != null) {
			semanticElement.setSyntaxNode(syntaxNode);
			// add SE to node
			syntaxNode.setSemanticElement(semanticElement);
		}

		// add semantic element to selection tree
		EditableObjectNode messageGroupNode = (EditableObjectNode)entitySelector.findNode(messageGroup);
		if (messageGroupNode != null) {
			// find SemanticElementSetNode
			for (Enumeration<?> en = messageGroupNode.depthFirstEnumeration(); en != null
					&& en.hasMoreElements();) {
				TreeNode node = (TreeNode) en.nextElement();
				// found where to place it
				if (node instanceof SemanticElementSetNode) {
					SemanticElementSetNode seSetNode = (SemanticElementSetNode)node;
					SemanticElementSet set = (SemanticElementSet)seSetNode.getUserObject();
					set.addSemanticElement(semanticElement);
					semanticElement.setElementSet(set);

					SemanticElementNode seNode = new SemanticElementNode(semanticElement, seSetNode.isHierarchical());
					seSetNode.addSorted(seNode);
					treeModel.nodeStructureChanged(node);
					break;
				}
			}
		}
		
		return semanticElement;

	}
	
	
	@Override
	public void dispose() {
		m_tree.setCellRenderer(null);
		m_tree.getSelectionModel().removeTreeSelectionListener(this);
		m_dataTypes.setRenderer(null);
		m_businessElements.setRenderer(null);

		m_leafNodeName.getDocument().removeDocumentListener(this);
		super.dispose();
	}
	
	
	public Node getSyntaxNode() {
		return m_node;
	}

	public Node getInsertionNode() {
		return m_insertionPoint;
	}
	
	public MdmiBusinessElementReference getBusinessElement() {
		Object selected = m_businessElements.getSelectedItem();
		if (selected instanceof MdmiBusinessElementReference) {
			return (MdmiBusinessElementReference)selected;
		}
		return null;
	}
	
	@Override
	public boolean isDataValid() {
		// must have a node name
		if (m_leafNodeName.getText().trim().length() == 0) {
			return false;
		}
		// must be inserted at a Node
		return m_insertionPoint instanceof Node;
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		TreePath path = e.getPath();
		Object node = path.getLastPathComponent();
		
		if (e.isAddedPath() && node instanceof SyntaxNodeNode) {
			m_insertionPoint = ((SyntaxNodeNode)node).getSyntaxNode();
		} else {
			m_insertionPoint = null;
		}
		
		setDirty(true);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		setDirty(true);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		setDirty(true);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		setDirty(true);
	}
	
	/** Custom MessageGroupNode that only has MessageModel children */
	private class CustomMessageGroupNode extends MessageGroupNode {
		public CustomMessageGroupNode(MessageGroup group) {
			super(group);
		}

		@Override
		protected void loadChildren(MessageGroup group) {
			// only add Model
			for (MessageModel model : group.getModels()) {
				addSorted(new CustomMessageModelNode(model));
			}
		}
		
	}
	/** Custom MessageModelNode that only has SyntaxModel children */
	private class CustomMessageModelNode extends MessageModelNode {
		public CustomMessageModelNode(MessageModel model) {
			super(model);
		}

		@Override
		protected void loadChildren(MessageModel model) {
			// Message Syntax Model
			MessageSyntaxModel syntaxModel = model.getSyntaxModel();
			if (syntaxModel != null) {
				add(new MessageSyntaxModelNode(syntaxModel));
			}
		}
		
	}
	
}