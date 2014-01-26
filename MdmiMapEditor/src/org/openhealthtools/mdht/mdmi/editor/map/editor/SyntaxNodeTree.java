package org.openhealthtools.mdht.mdmi.editor.map.editor;

import java.util.Collection;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNodeRenderer;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MessageSyntaxModelNode;
import org.openhealthtools.mdht.mdmi.model.MessageModel;

/**
 * A Special Tree that shows the syntax model
 * @author Sally Conway
 *
 */
public class SyntaxNodeTree extends JTree {

	public SyntaxNodeTree() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
		DefaultTreeModel treeModel = new DefaultTreeModel(root);
		setModel(treeModel);
		
		setRootVisible(false);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		setCellRenderer(new EditableObjectNodeRenderer());
	}
	
	public DefaultMutableTreeNode getRoot() {
		DefaultTreeModel treeModel = (DefaultTreeModel)getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)treeModel.getRoot();
		return root;
	}

	public void fillTree(Collection<MessageModel> models, final int expandToDepth) {
		final DefaultTreeModel treeModel = (DefaultTreeModel)getModel();
		final DefaultMutableTreeNode root = (DefaultMutableTreeNode)treeModel.getRoot();
		
		for (MessageModel messageModel : models) {
			if (messageModel.getSyntaxModel() != null) {
				MessageSyntaxModelNode treeNode = new MessageSyntaxModelNode(messageModel.getSyntaxModel());
				root.add(treeNode);
			}
		}
		treeModel.nodeStructureChanged(root);
		
		// if there's only one MessageModel, and it is empty, select it
		if (root.getChildCount() == 1 && root.getChildAt(0).isLeaf()) {
			MessageSyntaxModelNode treeNode = (MessageSyntaxModelNode)root.getChildAt(0);
			setSelectionPath(new TreePath(treeNode.getPath()));
		}
		
		// expand first 4 levels of children
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				expandChildren(treeModel, root, expandToDepth, 1);
			}
		});	
	}
	
	private void expandChildren(DefaultTreeModel treeModel, TreeNode node, 
			int maxDepth, int depth) {
		for (int i=0; i<node.getChildCount(); i++) {
			TreeNode child = node.getChildAt(i);
			expandPath(new TreePath(((DefaultMutableTreeNode)node).getPath()));
			if (depth < maxDepth) {
				expandChildren(treeModel, child, maxDepth, depth+1);
			}
		}
	}

}
