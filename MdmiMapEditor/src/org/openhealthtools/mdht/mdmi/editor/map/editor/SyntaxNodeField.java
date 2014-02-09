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
package org.openhealthtools.mdht.mdmi.editor.map.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.common.components.CursorManager;
import org.openhealthtools.mdht.mdmi.editor.common.components.JTreeComboBox;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MdmiModelTree;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MessageSyntaxModelNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SyntaxNodeNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.TreeNodeIcon;
import org.openhealthtools.mdht.mdmi.model.Bag;
import org.openhealthtools.mdht.mdmi.model.Choice;
import org.openhealthtools.mdht.mdmi.model.LeafSyntaxTranslator;
import org.openhealthtools.mdht.mdmi.model.MessageModel;
import org.openhealthtools.mdht.mdmi.model.MessageSyntaxModel;
import org.openhealthtools.mdht.mdmi.model.Node;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;

/** An IEditorField that shows Syntax Node values in a ComboBox */
public class SyntaxNodeField extends AdvancedSelectionField {

	private static final String s_blankText = s_res.getString("SyntaxNodeField.blankText");
	private static final Icon s_blankIcon = TreeNodeIcon.getIcon(s_res.getString("SyntaxNodeField.blankIcon"));
	private static final DefaultMutableTreeNode s_blankNode = new DefaultMutableTreeNode(BLANK_ENTRY);
	
	private SemanticElement m_semanticElement = null;
	private MessageModel m_messageModel = null;

	public SyntaxNodeField(GenericEditor parentEditor) {
		super(parentEditor);	
		
		// show the "create" button
		showCreateButton(true);
	}
	
	/** Define a semantic element context */
	public void setSemanticElement(SemanticElement semanticElement) {
		m_semanticElement = semanticElement;
		setMessageModel( semanticElement.getElementSet().getModel() );
	}
	
	/** Define a single message model */
	public void setMessageModel(MessageModel messageModel) {
		m_messageModel = messageModel;
	}
	
	@Override
	protected Collection<? extends Object> getComboBoxData() {
		// normally called by AdvancedSelectionField.loadComboBox().
		// not used
		return null;
	}
	
	@Override
	protected void loadComboBox() {
		((SyntaxNodeComboBox)getComboBox()).fillComboBox(getMessageModels());
	}

	public Collection<MessageModel> getMessageModels() {
		Collection<MessageModel> models;
		if (m_messageModel != null) {
			models = new ArrayList<MessageModel>();
			models.add(m_messageModel);
		} else {
			models = getParentEditor().getMessageGroup().getModels();
		}
		return models;
	}

	@Override
	public void addNotify() {
		super.addNotify();
		getViewButton().setToolTipText(s_res.getString("SyntaxNodeField.viewToolTip"));
		getCreateButton().setToolTipText(s_res.getString("SyntaxNodeField.createNewToolTip"));
	}

	@Override
	public void removeNotify() {
		getViewButton().setToolTipText(null);
		getCreateButton().setToolTipText(null);
		super.removeNotify();
	}
	
	@Override
	protected JComboBox createComboBox() {
		JTreeComboBox comboBox = new SyntaxNodeComboBox();
		JTree tree = comboBox.getTree();
		tree.setRootVisible(false);
		tree.setCellRenderer(new TreeRenderer(tree));
		
		Dimension pref = comboBox.getPreferredSize();
		comboBox.setPreferredSize(new Dimension(2*pref.width, pref.height));
		return comboBox;
	}


	@Override
	public Class<?> getDataClass() {
		return Node.class;
	}

	
	/** Create a new SyntaxNode */
	@Override
	protected void createNewItem() {
		CursorManager cm = CursorManager.getInstance(this);
		cm.setWaitCursor();
		try {
			
			NewSyntaxNodeDialog dialog = new NewSyntaxNodeDialog(SystemContext.getApplicationFrame());
			if (dialog.display(this) == BaseDialog.OK_BUTTON_OPTION) {
				Node syntaxNode = dialog.getSyntaxNode();				
				// set it
				setDisplayValue(syntaxNode);
			}
			
		} finally {
			cm.restoreCursor();
		}
	}


	public static String makeString(Node element) {
		StringBuilder buf = new StringBuilder();
		buf.append(element.getName());
		// check parent
		element = element.getParentNode();
		while (element != null) {
			buf.insert(0, ".");
			buf.insert(0, element.getName());
			
			element = element.getParentNode();
		}
		
		return buf.toString();
	}
	
	/** Convert an object in the list to a string */
	@Override
	protected String toString(Object listObject) {
		if (listObject instanceof Node) {
//			return makeString((Node)listObject);
			return ((Node)listObject).getName();
		}
		return listObject.toString();
	}
	
	/** Get a tooltip for an item in the list */
	@Override
	protected String getToolTipText(Object listObject) {
		if (listObject instanceof Node) {
			Node element = (Node)listObject;
			return element.getDescription();
		}
		return null;
	}

	
	/** Combo box that uses a tree model */
	public static class SyntaxNodeComboBox extends JTreeComboBox {

		public SyntaxNodeComboBox() {
			super( new DefaultTreeModel(new DefaultMutableTreeNode("root")));
		}
		
		public void fillComboBox(Collection<MessageModel> messageModels) {

			JTree tree = getTree();
			DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
			DefaultMutableTreeNode treeRoot = (DefaultMutableTreeNode)treeModel.getRoot();
			treeRoot.removeAllChildren();
						
			treeRoot.add(s_blankNode);
			for (MessageModel messageModel : messageModels) {
				if (messageModel.getSyntaxModel() != null 
						&& messageModel.getSyntaxModel().getRoot() != null) {
					Node syntaxRoot = messageModel.getSyntaxModel().getRoot();
					SyntaxNodeNode treeNode = SyntaxNodeNode.createSyntaxNode(syntaxRoot);
					treeRoot.add(treeNode);
				}
			}
			
			// expand first level children
			for (int i=0; i<treeRoot.getChildCount(); i++) {
				TreeNode child = treeRoot.getChildAt(i);
				tree.expandPath(new TreePath(((DefaultMutableTreeNode)child).getPath()));
			}
			
			treeModel.nodeStructureChanged(treeRoot);

		}

		@Override
		public Object getSelectedItem() {
			// JTreeComboBox selection is a tree node, need to get user object
			Object selected = super.getSelectedItem();
			if (selected instanceof DefaultMutableTreeNode) {
				selected = ((DefaultMutableTreeNode)selected).getUserObject();
			}
			return selected;
		}

		@Override
		public void addItem(Object anObject) {
			// wrap object in SyntaxNodeNode
			if (anObject instanceof Node) {
				DefaultTreeModel treeModel = (DefaultTreeModel)getTree().getModel();
				DefaultMutableTreeNode treeRoot = (DefaultMutableTreeNode)treeModel.getRoot();
				SyntaxNodeNode node = SyntaxNodeNode.createSyntaxNode((Node) anObject);
				node.removeAllChildren();
				
				int [] childIndices = new int[] {treeRoot.getChildCount()};
				treeRoot.add(node);

				treeModel.nodesWereInserted(treeRoot, childIndices);
				
			} else {
				super.addItem(anObject);
			}
		}
		
	}


	/** Tree Renderer for SyntaxNodeComboBox */
	public static class SyntaxNodeTreeRenderer extends JTreeComboBox.CustomTreeRenderer {

		public SyntaxNodeTreeRenderer(JTree tree) {
			super(tree);
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean isSelected, boolean isExpanded, boolean isLeaf, int row,
				boolean hasFocus) {

			Icon icon = null;
			
			JLabel label = (JLabel)super.getTreeCellRendererComponent(tree, value, isSelected, isExpanded,
					isLeaf, row, hasFocus);
			if (value == s_blankNode) {
				// display as "(not set)"
				label.setText(s_blankText);
				icon = s_blankIcon;
			}
			if (value instanceof SyntaxNodeNode) {
				icon = ((SyntaxNodeNode)value).getNodeIcon();
			}

			
			if (icon != null) {
				label.setIcon(icon);
			}
			return label;
		}
		
	}

	public class TreeRenderer extends SyntaxNodeTreeRenderer {

		public TreeRenderer(JTree tree) {
			super(tree);
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean isSelected, boolean isExpanded, boolean isLeaf, int row,
				boolean hasFocus) {
			
			JLabel label = (JLabel)super.getTreeCellRendererComponent(tree, value, isSelected, isExpanded,
					isLeaf, row, hasFocus);

			// change color of invalid value
			if (value instanceof SyntaxNodeNode &&
					((SyntaxNodeNode)value).getUserObject() == m_invalidValue) {
				Color background = label.getBackground();
				if (background == m_selectedBackground) {
					// change selection background
					label.setBackground(Color.red.darker());
				} else {
					// change foreground 
					label.setForeground(Color.red);
				}
			}

			return label;
		}
		
	}	
	
	
	private class NewSyntaxNodeDialog extends BaseDialog implements TreeSelectionListener {
		private  final String BagType = ClassUtil.beautifyName(Bag.class);
		private  final String ChoiceType = ClassUtil.beautifyName(Choice.class);
		private  final String LeafType = ClassUtil.beautifyName(LeafSyntaxTranslator.class);

		private JTextField m_nameField = new JTextField();
		private JComboBox  m_choices;
		private SyntaxNodeTree m_tree;
		
		private Node m_node;
		private Object m_insertionPoint = null;

		public NewSyntaxNodeDialog(Frame owner) {
			super(owner, BaseDialog.OK_CANCEL_OPTION);
			setTitle(s_res.getString("SyntaxNodeField.createNewToolTip"));
			buildUI();
			pack(new Dimension(500, 300));
		}
		
		private void buildUI() {
			//  Name: [_______________________________]
			//  Type: [______________|v]
			//  Select Insertion Point
			//   -------------------------------------
			//  |   Tree                              |
			
			JPanel main = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = Standards.getInsets();
			gbc.anchor = GridBagConstraints.WEST;
			gbc.weightx = 0;
			gbc.weighty = 0;
			gbc.gridx = 0;
			gbc.gridy = 0;
			
			// Name
			main.add(new JLabel("Name:"), gbc);
			gbc.gridx++;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			// Initialize with semanticElement name
			if (m_semanticElement != null && m_semanticElement.getName() != null) {
				m_nameField.setText(m_semanticElement.getName());
			}
			main.add(m_nameField, gbc);
			
			// Type
			gbc.gridy++;
			gbc.gridx = 0;
			gbc.weightx = 0;
			gbc.fill = GridBagConstraints.NONE;
			main.add(new JLabel("Type:"), gbc);
			gbc.gridx++;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			m_choices = new JComboBox(new Object[] {
					BagType, ChoiceType, LeafType
			});
			main.add(m_choices, gbc);
			
			// Tree
			m_tree = new SyntaxNodeTree();
			
			gbc.gridy++;
			gbc.gridx = 0;
			gbc.gridwidth = 2;
			gbc.insets.bottom = 0;
			main.add(new JLabel("Select Insertion Point"), gbc);
			
			gbc.gridy++;
			gbc.weighty = 1.0;
			gbc.fill = GridBagConstraints.BOTH;
			main.add(new JScrollPane(m_tree), gbc);
			
			// Fill Tree and expand to depth 4
			m_tree.fillTree(getMessageModels(), 4);
			
			DefaultMutableTreeNode root = m_tree.getRoot();
			if (root.getChildCount() == 1 && root.getChildAt(0).isLeaf()) {
				// if there's only one MessageModel, and it is empty, select it
				MessageSyntaxModelNode treeNode = (MessageSyntaxModelNode)root.getChildAt(0);
				m_tree.setSelectionPath(new TreePath(treeNode.getPath()));
				m_insertionPoint = treeNode.getUserObject();
				setDirty(true);
			}
			
			getContentPane().add(main);
		}
		
		@Override
		protected void okButtonAction() {
			SelectionManager selectionManager = SelectionManager.getInstance();
			MdmiModelTree entitySelector = selectionManager.getEntitySelector();
			DefaultTreeModel treeModel =
				(DefaultTreeModel)entitySelector.getMessageElementsTree().getModel();
			
			Node syntaxNode = createNode();
			syntaxNode.setSemanticElement(m_semanticElement);
			
			SyntaxNodeNode treeNode = SyntaxNodeNode.createSyntaxNode(syntaxNode);
			
			Object insertion = getInsertionNode();
			DefaultMutableTreeNode parentTreeNode = entitySelector.findNode(insertion);
			
			// update model
			if (insertion instanceof MessageSyntaxModel) {
				// Dialog will check that model doesn't have a root
				((MessageSyntaxModel)insertion).setRoot(syntaxNode);
				syntaxNode.setSyntaxModel((MessageSyntaxModel)insertion);
			} else if (insertion instanceof Bag) {
				((Bag)insertion).addNode(syntaxNode);
				syntaxNode.setParentNode((Bag)insertion);
			} else if (insertion instanceof Choice) {
				((Choice)insertion).addNode(syntaxNode);
				syntaxNode.setParentNode((Choice)insertion);
			}
			
			// update selection tree
			if (parentTreeNode instanceof EditableObjectNode) {
				((EditableObjectNode)parentTreeNode).addSorted(treeNode);
				treeModel.nodeStructureChanged(parentTreeNode);
			}

			// notify listeners
			selectionManager.notifyModelChangeListeners(insertion);
			selectionManager.notifyCollectionChangeListeners(syntaxNode.getClass());
			
			// highlight in selectionManager
			entitySelector.selectNode(treeNode);
			
			super.okButtonAction();
		}

		@Override
		public void dispose() {
			m_tree.setCellRenderer(null);
			m_tree.getSelectionModel().removeTreeSelectionListener(this);
			super.dispose();
		}
		
		private Node createNode() {
			m_node = null;
			String type = (String)m_choices.getSelectedItem();
			if (BagType.equals(type)) {
				m_node = new Bag();
			} else if (ChoiceType.equals(type)) {
				m_node = new Choice();
			} else if (LeafType.equals(type)) {
				m_node = new LeafSyntaxTranslator();
			}
			if (m_node != null) {
				m_node.setName(m_nameField.getText().trim());
			}
			return m_node;
		}
		
		public Node getSyntaxNode() {
			return m_node;
		}

		public Object getInsertionNode() {
			return m_insertionPoint;
		}
		
		@Override
		public boolean isDataValid() {
			if (m_insertionPoint instanceof MessageSyntaxModel) {
				// only valid if model doesn't have a root
				return ((MessageSyntaxModel)m_insertionPoint).getRoot() == null;
			}
			// must be Bag or Choice
			return m_insertionPoint instanceof Bag ||
					m_insertionPoint instanceof Choice;
		}

		@Override
		public void valueChanged(TreeSelectionEvent e) {
			TreePath path = e.getPath();
			Object node = path.getLastPathComponent();
			
			if (e.isAddedPath() &&
					node instanceof DefaultMutableTreeNode) {
				m_insertionPoint = ((DefaultMutableTreeNode)node).getUserObject();
			} else {
				m_insertionPoint = null;
			}
			
			setDirty(true);
		}
		
	}
	
}
