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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.openhealthtools.mdht.mdmi.editor.common.treetable.JTreeTable;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.tools.DatatypeTree.IDatatypeField;
import org.openhealthtools.mdht.mdmi.editor.map.tree.DataRuleSetNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNodeRenderer;
import org.openhealthtools.mdht.mdmi.editor.map.tree.NewObjectInfo;
import org.openhealthtools.mdht.mdmi.editor.map.tree.TreeNodeIcon;
import org.openhealthtools.mdht.mdmi.model.DTSEnumerated;
import org.openhealthtools.mdht.mdmi.model.DataRule;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.Node;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;

/** A TreeTable with DataTypes in the first column, and Syntax Nodes in the second */
public class DatatypeToNodeTable extends JTreeTable {
	
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");

	private static final Color s_oddRowColor = Color.white;
	private static final Color s_evenRowColor = new Color(0xee, 0xff, 0xee);	// pale green

	private DatatypeToNodeModel m_treeTableModel;
	
	private TableMouseListener m_mouseListener = new TableMouseListener();

	private DatatypeToNodeTable(DatatypeToNodeModel model) {
		super(model);
		m_treeTableModel = model;
		
		// Initialize Tree parameters

		// override defaults
		tree.setShowsRootHandles(true);
		tree.setCellRenderer(new TreeRenderer());

		// set selection model to all  multi-selection 
		getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		
		// set column widths and renderers
		TableColumnModel tcm = getColumnModel();
		TableCellRenderer renderer = new EvenOddTableRenderer();
		tcm.getColumn(0).setPreferredWidth(DatatypeToNodeModel.s_columnWidths[0]);
		for (int i=1; i<getColumnCount(); i++) {
			tcm.getColumn(i).setCellRenderer(renderer);
			tcm.getColumn(i).setPreferredWidth(DatatypeToNodeModel.s_columnWidths[i]);
		}
		
		// expand
		expandAllExceptEnumerated((DefaultMutableTreeNode)model.getRoot());
	}

	public DatatypeToNodeTable(SemanticElement semanticElement) {
		// initialize tree
		this(new DatatypeToNodeModel(semanticElement));
	}

	
	public DatatypeToNodeTable(Collection<SemanticElement> elements) {
		// initialize tree
		this(new DatatypeToNodeModel(elements));
	}


	@Override
	public void addNotify() {
		super.addNotify();

		addMouseListener(m_mouseListener);
//		setCellRenderer(new MdmiModelTree.EditableObjectNodeRenderer());
	}

	
	@Override
	public void removeNotify() {
		removeMouseListener(m_mouseListener);
//		setCellRenderer(null);
		
		super.removeNotify();
	}

	/** expand all datatypes except for Enumerated ones */
	private void expandAllExceptEnumerated(DefaultMutableTreeNode rootNode) {
		for (Enumeration<?> en = rootNode.depthFirstEnumeration(); en != null && en.hasMoreElements();) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode)en.nextElement();
			// don't expand enumerated types
			if (!(child.getUserObject() instanceof DTSEnumerated)) {
				tree.expandPath(new TreePath(child.getPath()));
			}
		}
	}

	public List<String> getValidationErrors() {
		return m_treeTableModel.getValidationErrors();
	}

	/** Validate the syntax nodes that correspond to the semantic element,
	 * creating new nodes, or changing existing node types as necessary.
	 * Call getValidationErrors() to read errors.
	 * @return true if no errors
	 */
	public boolean validateSyntaxNodes() {
		List<String> errors = m_treeTableModel.validateModel(true, this);
		return errors.size() == 0;
	}


	/** get the color to use for a table row */
	public static Color getColorForRow(int row) {
		return (row %2 == 0) ? s_evenRowColor : s_oddRowColor;
	}

   
   /** Get the (single) selected tree node */
   public DefaultMutableTreeNode getSelectedNode() {
       int row = getSelectedRow();
       if (row == -1) {
           return null;
       }
       
       Object val = getModel().getValueAt(row, 0);
       if (val instanceof DefaultMutableTreeNode) {
           return (DefaultMutableTreeNode)val;
       }
       
       return null;
   }
   
   /** Get all selected nodes */
   public List<DefaultMutableTreeNode> getSelectedNodes() {
   	List<DefaultMutableTreeNode> selections = new ArrayList<DefaultMutableTreeNode>();
   	int [] rows = getSelectedRows();
   	for (int row : rows) {
         Object val = getModel().getValueAt(row, 0);
         if (val instanceof DefaultMutableTreeNode) {
             selections.add((DefaultMutableTreeNode)val);
         }
   	}
   	return selections;
   }

	/** Open (edit) the supplied model */
	protected void openSelection(Object model) {
		ModelTree.openUserObject(this, model);
	}

	private SemanticElement getSemanticElementForRow(int row) {
		Object value = getModel().getValueAt(row, DatatypeToNodeModel.s_dataTypeCol);
		
		while (value instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)value;
			if (treeNode.getUserObject() instanceof SemanticElement) {
				return (SemanticElement)((DefaultMutableTreeNode)value).getUserObject();
			}
			
			value = treeNode.getParent();
		}
		return null;
	}
	
	/** Create a new dataRule for this semantic element, with the data types pre-assigned to
	 * all referenced types
	 */
	private void createDataRule() {
		int row = getSelectedRow();
		SemanticElement semanticElement = getSemanticElementForRow(row);
		if (semanticElement == null) {
			return;
		}

		DataRuleSetNode ruleSetNode = null;
		SelectionManager selectionManager = SelectionManager.getInstance();
		DefaultMutableTreeNode seNode = selectionManager.getEntitySelector().findNode(semanticElement);
		if (seNode != null) {
			// find DataRuleSetNode
			for (int i=0; i<seNode.getChildCount(); i++) {
				if (seNode.getChildAt(i) instanceof DataRuleSetNode) {
					ruleSetNode = (DataRuleSetNode)seNode.getChildAt(i);
					break;
				}
			}
		}

		// Create a new DataRule
		if (ruleSetNode != null) {
			DataRule dataRule = new DataRule();

			// add to parent SemanticElement 
			NewObjectInfo newObjectInfo = ruleSetNode.getNewObjectInformationForClass(DataRule.class);
			EditableObjectNode ruleNode = newObjectInfo.addNewChild(dataRule);
			
			// pre-set datatypes referenced by SE
			for (row++; row<getRowCount(); row++) {
				Object value = getModel().getValueAt(row, DatatypeToNodeModel.s_dataTypeCol);
				if (value instanceof DefaultMutableTreeNode) {
					DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)value;
					if (treeNode.getUserObject() instanceof MdmiDatatype) {
						MdmiDatatype datatype = (MdmiDatatype)treeNode.getUserObject();
						dataRule.addDatatype(datatype);
					} else if (treeNode.getUserObject() instanceof SemanticElement) {
						// stop if we get to a semantic element
						break;
					}
				}
			}
			
			selectionManager.getEntitySelector().insertNewNode(ruleSetNode, ruleNode);
			
			selectionManager.getEntitySelector().selectNode(ruleNode);
			selectionManager.editItem(ruleNode);
		}
	}



	private void handleDoubleClick() {
		// open syntax node
		DefaultMutableTreeNode selectedNode = getSelectedNode();
		if (selectedNode instanceof IDatatypeField) {
			Node syntaxNode = m_treeTableModel.getNodeForDatatype((IDatatypeField) selectedNode);
			if (syntaxNode != null) {
				openSelection(syntaxNode);
			}
		}
	}

	public void setVisibleRowCount(int rows){ 
	    setPreferredScrollableViewportSize(new Dimension( 
	   		 getPreferredScrollableViewportSize().width, 
	            rows*getRowHeight() 
	    )); 
	} 
	
	public int getVisibleRowCount() {
		int viewportHeight = getPreferredScrollableViewportSize().height;
		int rows = viewportHeight/getRowHeight();
		return rows;
	}


	@Override
	protected TreeTableCellRenderer createTreeTableCellRenderer(
			TreeModel model) {
		// override to use a special renderer
		return new EvenOddTreeTableCellRenderer(model);
	}		


	/////////////////////////////////////////////////////
	// Renderers - one for TreeTable, one for Tree (column 0),
	//             one for Table (columns 1+)
	/////////////////////////////////////////////////////	
	
	/** Overall renderer, uses a JTree - override to show odd/even row colors */
	private class EvenOddTreeTableCellRenderer extends TreeTableCellRenderer {
		public EvenOddTreeTableCellRenderer(TreeModel model) {
			super(model);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);

			if (!isSelected) {
				// color even/odd
				setBackground(getColorForRow(row));
			}
			// add a border on right
			((JComponent)c).setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2,
					table.getGridColor()));
			return c;
		}
	}

	/** renderer for table columns (except first) */
	private class EvenOddTableRenderer extends DefaultTableCellRenderer {
		private SyntaxNodeComponent m_nodeComponent = new SyntaxNodeComponent(DatatypeToNodeTable.this);
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {

			if (!isSelected) {
				setBackground(getColorForRow(row));
			}

			// Handle nodes
			Object displayValue = value;
			Icon icon = null;

			if (value instanceof Node) {
				Node node = (Node)value;
				displayValue = node.getName();
				icon = TreeNodeIcon.getTreeIcon(node.getClass());
			}

			Component c = super.getTableCellRendererComponent(table, displayValue, 
					isSelected, hasFocus,
					row, column);

			setIcon(icon);

			
			// For Node, show indented to mimic tree structure
			if (column == DatatypeToNodeModel.s_syntaxNodeCol) {
				c = m_nodeComponent.render(value instanceof Node ? (Node)value : null, c, row);
			}

			if (column > 0 && c instanceof JComponent) {
				// we need to add right borders
				((JComponent)c).setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1,
						table.getGridColor()));
			}
			return c;

		}

	}
	
	private class SyntaxNodeComponent extends RenderedSyntaxNode {

		public SyntaxNodeComponent(JTable table) {
			super(table);
		}

		@Override
		public Node getNodeAtRow(int row) {
			Object value = getModel().getValueAt(row, DatatypeToNodeModel.s_syntaxNodeCol);
			if (value instanceof Node) {
				return (Node)value;
			}
			return null;
		}

	}
	
	
	/** renderer for tree - override to set background for odd/even rows */
	private static class TreeRenderer extends EditableObjectNodeRenderer {
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {

			if (!selected) {
				setOpaque(true);
				tree.setBackground(getColorForRow(row));
			}

			Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded,
					leaf, row, hasFocus);

			return c;
		}

	}


	///////////////////////////////////////////////////////////////////
	// Tree and Table selection listeners
	//////////////////////////////////////////////////////////////////

	private class TableMouseListener extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopup(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopup(e);
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopup(e);

			} else if( e.getClickCount() == 2) {
				handleDoubleClick();
			}
		}


		/** Show the popup menu for this node */
		private void showPopup(MouseEvent e) {
         int row = rowAtPoint(e.getPoint());
         if (row < 0) {
         	return;
         }

         // select row (if not already selected)
         if (!isRowSelected(row)) {
         	// don't select if already selected, since this will un-select others
         	setRowSelectionInterval(row, row);
         }
			
			List<DefaultMutableTreeNode> treeNodes = getSelectedNodes();
			if (treeNodes == null || treeNodes.size() == 0) {
				return;
			}
			
			// Display a popup menu 
			JPopupMenu popup = new JPopupMenu();

			if (treeNodes.size() == 1) {
				DefaultMutableTreeNode treeNode = treeNodes.get(0);
				
				// Open Syntax Element (single selection)
				if (treeNode instanceof IDatatypeField) {
					Node syntaxNode = m_treeTableModel.getNodeForDatatype((IDatatypeField)treeNode);
					if (syntaxNode != null) {
						popup.add(new OpenSelectionAction(syntaxNode));
					}
				} else if (treeNode.getUserObject() instanceof SemanticElement) {
					popup.add(new OpenSelectionAction((SemanticElement)treeNode.getUserObject()));
				}

				// Expand/Collapse
				if (!treeNode.isLeaf()) {
					if (popup.getComponentCount() > 0) {
						popup.addSeparator();
					}
					popup.add(new ExpandAllAction());
					popup.add(new CollapseAllAction());
				}
			}
			
			// Data Rules
			if (popup.getComponentCount() > 0) {
				popup.addSeparator();
			}
			popup.add(new AssignDataRulesAction());

			popup.show(e.getComponent(), e.getX(), e.getY());

		}
		
		private boolean isRowSelected(int row) {
			int [] selectedRows = getSelectedRows();
			for (int selectedRow : selectedRows) {
				if (row == selectedRow) {
					return true;
				}
			}
			return false;
		}
	}
	
	///////////////////////////////////////
	// Actions for Table
	///////////////////////////////////////
	private class ExpandAllAction extends AbstractAction {
		public ExpandAllAction() {
			super(s_res.getString("ModelTree.expandAll"));
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			DefaultMutableTreeNode node = getSelectedNode();
			/** Expand this node, and all children */
			for (Enumeration<?> en = node.depthFirstEnumeration(); en != null && en.hasMoreElements();) {
				DefaultMutableTreeNode child = (DefaultMutableTreeNode)en.nextElement();
				getTree().expandPath(new TreePath(child.getPath()));
			}
		}
	}

	private class CollapseAllAction extends AbstractAction {
		public CollapseAllAction() {
			super(s_res.getString("ModelTree.collapseAll"));
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			DefaultMutableTreeNode node = getSelectedNode();
			/** Collapse this node, and all children */
			for (Enumeration<?> en = node.depthFirstEnumeration(); en != null && en.hasMoreElements();) {
				DefaultMutableTreeNode child = (DefaultMutableTreeNode)en.nextElement();
				getTree().collapsePath(new TreePath(child.getPath()));
			}
		}
	}


	private class OpenSelectionAction extends AbstractAction {
		private Object m_selectedItem;
		public OpenSelectionAction(Object item) {
			super(MessageFormat.format(s_res.getString("ViewDataObject.openFormat"), 
					ClassUtil.beautifyName(item.getClass()) + " " + ClassUtil.getItemName(item)));
			m_selectedItem = item;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			openSelection(m_selectedItem);
		}
	}
	
	private class AssignDataRulesAction extends AbstractAction {
		public AssignDataRulesAction() {
			super("Assign Data Rules");
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			createDataRule();
		}		
	}
}
