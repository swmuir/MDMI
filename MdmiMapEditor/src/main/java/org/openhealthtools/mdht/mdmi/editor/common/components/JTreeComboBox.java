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
package org.openhealthtools.mdht.mdmi.editor.common.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.metal.MetalComboBoxUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.map.MapEditor;

import com.sun.java.swing.plaf.motif.MotifComboBoxUI;
import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;


/** A combobox that uses a tree for a renderer */
public class JTreeComboBox extends JComboBox implements TreeSelectionListener {
	protected JTree m_tree = null;

	public JTreeComboBox(TreeModel treeModel) {
		initializeTree();
		setTreeModel(treeModel);
	}

	public void setCellRenderer(TreeCellRenderer theRenderer) {
		m_tree.setCellRenderer(theRenderer);
	}

	public JTree getTree() {
		return m_tree;
	}

	@Override
	public void addNotify() {
		super.addNotify();
		m_tree.addTreeSelectionListener(this);
	}

	@Override
	public void removeNotify() {
		m_tree.removeTreeSelectionListener(this);
		super.removeNotify();
	}
	
	private void initializeTree() {
		m_tree = new JTree();
		m_tree.setCellRenderer(new CustomTreeRenderer(m_tree));
		m_tree.setVisibleRowCount(8);
//		m_tree.setBackground(m_background);
//		m_tree.addTreeSelectionListener(this);
		m_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		m_tree.setShowsRootHandles(true);

		Dimension prefCb = getPreferredSize();
		Dimension prefTree = m_tree.getPreferredSize();
		setPreferredSize(new Dimension(2*prefTree.width, prefCb.height));
	}

	public void setTreeModel(TreeModel treeModel) {
		m_tree.setModel(treeModel);

		setSelection((TreeNode)treeModel.getRoot());
	}

	public void makeVisible(TreePath aPath) {
		m_tree.makeVisible(aPath);
	}

	@Override
	public void setSelectedItem(Object item) {
		if (!(item instanceof TreeNode)) {
			// search tree
			for (Enumeration<?> en = ((DefaultMutableTreeNode)m_tree.getModel().getRoot()).preorderEnumeration(); en != null && en.hasMoreElements();) {
				Object next = en.nextElement();
				if (next instanceof DefaultMutableTreeNode) {
					DefaultMutableTreeNode child = (DefaultMutableTreeNode)next;
					if (child.getUserObject().equals(item)) {
						item = child;
						TreePath path = new TreePath(child.getPath());
						m_tree.setSelectionPath(path);
						setSelection(child);
						return;
					}
				}
			}
		}
		
		if (item instanceof TreeNode) {
			TreeNode treeNode = (TreeNode)item;
			if (treeContains((TreeNode)m_tree.getModel().getRoot(), treeNode)) {
				setSelection(treeNode);
			}
		} else {
			removeAllItems();
		}
	}
	
	private void setSelection(TreeNode item) {
		removeAllItems();
		addItem(item);
	}

	private boolean treeContains(TreeNode root, TreeNode node) {
		if(root.getIndex(node) != -1)
			return true;

		for(int i = 0; i < root.getChildCount(); i++)
			if(treeContains(root.getChildAt(i), node))
				return true;

		return false;
	}

	@Override
	public void updateUI() {
		ComboBoxUI cui = (ComboBoxUI) UIManager.getUI(this);

		if (cui instanceof MetalComboBoxUI) {
			cui = new MetalTreeComboBoxUI();
		} else if (cui instanceof MotifComboBoxUI) {
			cui = new MotifTreeComboBoxUI();
		} else if (cui instanceof WindowsComboBoxUI) {
			cui = new WindowsTreeComboBoxUI();
		} else {
			// default is windows
			cui = new WindowsTreeComboBoxUI();
		}
		
		setUI(cui);
	}


	public void valueChanged(TreeSelectionEvent e) {
		TreeNode selectedNode = (TreeNode)
		m_tree.getLastSelectedPathComponent();

		if (selectedNode == null)
			return;

		setSelection(selectedNode);

		hidePopup();
	}

	//////////////////////////////////////////////////////////////
	// UI Inner classes -- one for each supported Look and Feel
	//////////////////////////////////////////////////////////////	
	class MetalTreeComboBoxUI extends MetalComboBoxUI {
		@Override
		protected ComboPopup createPopup() {
			return new TreePopup( comboBox );
		}
	}

	class WindowsTreeComboBoxUI extends WindowsComboBoxUI {
		@Override
		protected ComboPopup createPopup() {
			return new TreePopup( comboBox );
		}
	}

	class MotifTreeComboBoxUI extends MotifComboBoxUI {
		@Override
		protected ComboPopup createPopup() {
			return new TreePopup( comboBox );
		}
	}


	//////////////////////////////////////////////////////////////
	// TreePopup inner class
	//////////////////////////////////////////////////////////////

	class TreePopup implements ComboPopup, MouseMotionListener,
	MouseListener, KeyListener, PopupMenuListener {

		protected JComboBox  m_comboBox;
		protected JPopupMenu m_popup;


		public TreePopup(JComboBox comboBox) {
			this.m_comboBox = comboBox;

			initializePopup();
		}

		//========================================
		// begin ComboPopup method implementations
		//
		public void show() {
			updatePopup();
			m_popup.show(m_comboBox, 0, m_comboBox.getHeight());
			m_popup.setVisible(true);
		}

		public void hide() {
			m_popup.setVisible(false);
		}

		protected JList list = new JList();
		public JList getList() {
			return list;
		}

		public MouseListener getMouseListener() {
			return this;
		}

		public MouseMotionListener getMouseMotionListener() {
			return this;
		}

		public KeyListener getKeyListener() {
			return this;
		}

		public boolean isVisible() {
			return m_popup.isVisible();
		}

		public void uninstallingUI() {
			m_popup.removePopupMenuListener(this);
		}

		//
		// end ComboPopup method implementations
		//======================================



		//===================================================================
		// begin Event Listeners
		//

		// MouseListener

		public void mousePressed( MouseEvent e ) {}
		public void mouseReleased( MouseEvent e ) {}
		// something else registered for MousePressed
		public void mouseClicked(MouseEvent e) {
			//System.out.println("clicked");
			if ( !SwingUtilities.isLeftMouseButton(e) )
				return;
			if ( !m_comboBox.isEnabled() )
				return;
			if ( m_comboBox.isEditable() ) {
				m_comboBox.getEditor().getEditorComponent().requestFocus();
			} else {
				m_comboBox.requestFocus();
			}
			togglePopup();
		}

		protected boolean mouseInside = false;
		public void mouseEntered(MouseEvent e) {
			mouseInside = true;
		}
		public void mouseExited(MouseEvent e) {
			mouseInside = false;
		}

		// MouseMotionListener
		public void mouseDragged(MouseEvent e) {}
		public void mouseMoved(MouseEvent e) {}

		// KeyListener
		public void keyPressed(KeyEvent e) {}
		public void keyTyped(KeyEvent e) {}
		public void keyReleased( KeyEvent e ) {
			if ( e.getKeyCode() == KeyEvent.VK_SPACE ||
					e.getKeyCode() == KeyEvent.VK_ENTER ) {
				togglePopup();
			}
		}

		/**
		 * Variables hideNext and mouseInside are used to
		 * hide the popupMenu by clicking the mouse in the JComboBox
		 */
		public void popupMenuCanceled(PopupMenuEvent e) {}
		protected boolean hideNext = false;
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			hideNext = mouseInside;
		}
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}

		//
		// end Event Listeners
		//=================================================================

		//===================================================================
		// begin Utility methods
		//

		protected void togglePopup() {
			//System.out.println("toggle "  + popup.isVisible());

			if ( isVisible() || hideNext  ) {
				hide();
			} else {
				show();
			}
			hideNext = false;
		}

		//
		// end Utility methods
		//=================================================================
		JScrollPane scroller = new JScrollPane();
		protected void initializePopup() {
			m_popup = new JPopupMenu();
			m_popup.setLayout(new BorderLayout());
			m_popup.setBorder(new EmptyBorder(0,0,0,0));
			m_popup.addPopupMenuListener(this);
			m_popup.add(scroller);
			m_popup.pack();
		}

		protected void updatePopup() {
			//System.out.println("update");
			scroller.setViewportView(m_tree);

			int width = m_comboBox.getWidth();
			int height = (int) m_tree.getPreferredScrollableViewportSize().getHeight();

			m_popup.setPopupSize(width, height);
		}

	}



	public static class CustomTreeRenderer extends DefaultTreeCellRenderer {

		protected Color m_selectedBackground;
		protected Color m_selectedForeground;
		protected Color m_background;
		protected Color m_foreground;

		protected Object m_lastNode = null;

		public CustomTreeRenderer(final JTree tree) {
			// initialize colors
			m_foreground = UIManager.getColor("Tree.foreground");
			m_background = UIManager.getColor("Tree.background");
			m_selectedForeground = UIManager.getColor("Tree.selectionForeground");
			
			// for unknown reasons, we need to make a copy the background color or else it is
			// drawn in white when we render it
			Color selBk = UIManager.getColor("Tree.selectionBackground");
			m_selectedBackground = new Color(selBk.getRGB());

			setOpaque(true);
			
			tree.addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseMoved(MouseEvent me) {
					TreePath treePath = tree.getPathForLocation(me.getX(), me.getY());
					Object obj = null;
					if (treePath != null) {
						obj = treePath.getLastPathComponent();
					}
					if (obj != m_lastNode) {
						m_lastNode = obj;
						tree.repaint();
					}
				}
			});
		}

		@Override
		public Component getTreeCellRendererComponent(
				JTree tree, Object value,
				boolean isSelected, boolean isExpanded,
				boolean isLeaf, int row, boolean hasFocus) {

			JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value,
					isSelected, isExpanded, isLeaf, row, hasFocus);
			if (renderSelected(value, isSelected)) {
				label.setBackground(m_selectedBackground);
				label.setForeground(m_selectedForeground);
			} else {
				label.setBackground(m_background);
				label.setForeground(m_foreground);
			}

			return label;
		}
		
		/** check if this this value should be shown as selected when rendering*/
		protected boolean renderSelected(Object value, boolean isSelected) {
			return (value == m_lastNode || (m_lastNode == null && isSelected));
		}
	}

	//////////////////////////////////////////////////////////////
	// This is only included to provide a sample GUI
	//////////////////////////////////////////////////////////////
	public static void main(String args[]) {

		try {
			String lAndF = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(lAndF);
		} catch (Exception e) {
			SystemContext.getLogWriter().loge(e, "Error setting look and feel");
		}

		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
		DefaultTreeModel treeModel = new DefaultTreeModel(root);

		root.add(new DefaultMutableTreeNode("first node"));
		for(int i=0; i < 10; i++) {
			String nodeName = "Node " + (char)('A'+i);
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(nodeName);
			root.add(node);
			for (int j=0; j<4; j++) {
				nodeName = "" + (char)('a' + i) + "_" + (j+1);
				node.add(new DefaultMutableTreeNode(nodeName));
			}
		}



		JFrame frame = new JFrame();

		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		panel.add(new JLabel("Tree 1:"));
		final JTreeComboBox comboBox = new JTreeComboBox(treeModel);
		comboBox.getTree().setRootVisible(false);
		panel.add(comboBox);
		
		// pre-set
		comboBox.setSelectedItem("b_2");

		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Selected " + comboBox.getSelectedItem());

			}
		});

		frame.getContentPane().add(panel);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 200);
		frame.setVisible(true);
	}

}
