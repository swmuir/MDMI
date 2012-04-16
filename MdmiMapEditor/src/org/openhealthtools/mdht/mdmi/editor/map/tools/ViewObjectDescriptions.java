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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.editor.MdmiDatatypeField;
import org.openhealthtools.mdht.mdmi.editor.map.tree.TreeNodeIcon;
import org.openhealthtools.mdht.mdmi.model.Bag;
import org.openhealthtools.mdht.mdmi.model.Choice;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MdmiDomainDictionaryReference;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.MessageModel;
import org.openhealthtools.mdht.mdmi.model.MessageSyntaxModel;
import org.openhealthtools.mdht.mdmi.model.Node;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.SemanticElementSet;

/** Show all objects and their descriptions */
public class ViewObjectDescriptions extends PrintableView {
	
	private static final String[] s_columnNames = new String[] {s_res.getString("ViewObjectDescriptions.nameColumn"),
						s_res.getString("ViewObjectDescriptions.descriptionColumn") };

	private JTabbedPane m_tabs = new JTabbedPane();
	private BoldTextRenderer     m_col_0_renderer = new BoldTextRenderer();
	private WrappingTextRenderer m_col_1_renderer = new WrappingTextRenderer();
	
	public ViewObjectDescriptions(MessageGroup messageGroup) {
		setTitle(MessageFormat.format(s_res.getString("ViewObjectDescriptions.title"), 
				messageGroup.getName()));
		
		// create tabs for:
		//   Data Types
		//   Message Model A
		//      Semantic Elements
		//      Syntax Model
		//   Message Model B
		//      Semantic Elements
		//      Syntax Model
		//   ...
		//   Domain Dictionary
		
		// Datatypes
		DescriptionPanel p = getDatatypes(messageGroup);
		m_tabs.addTab(p.getCategoryName(), TreeNodeIcon.DataTypeSetIcon, p);
		
		// Message Model(s)
		for (MessageModel messageModel : messageGroup.getModels()) {
			JTabbedPane modelTab = new JTabbedPane();
			
			// Semantic Elements
			p = getSemanticElements(messageModel);
			modelTab.addTab(p.getCategoryName(), TreeNodeIcon.SemanticElementSetIcon, p);
			
			// Syntax Model
			p = getSyntaxModel(messageModel);
			modelTab.addTab(p.getCategoryName(), TreeNodeIcon.MessageSyntaxModelIcon, p);
			
			m_tabs.addTab(MessageFormat.format(s_res.getString("ViewObjectDescriptions.messageModelHeading"), 
					messageModel.getMessageModelName()),
					TreeNodeIcon.MessageModelIcon, modelTab);
		}
		
		// Domain dictionary
		p = getDomainDictionary(messageGroup);
		m_tabs.addTab(p.getCategoryName(), TreeNodeIcon.DomainDictionaryReferenceIcon, p);
		
		setCenterComponent(m_tabs);
		
		pack(new Dimension(800, 600), new Dimension(1000, 700));
		BaseDialog.centerOnScreen(this);
	}
	
	private DescriptionPanel getDatatypes(MessageGroup group) {

		Collection <ItemAndDescription> itemList = new ArrayList <ItemAndDescription>();
		// get sorted types
		for (MdmiDatatype datatype : MdmiDatatypeField.getAllDatatypes(group, MdmiDatatype.class)) {
			ItemAndDescription item = new ItemAndDescription(datatype, datatype.getDescription());
			itemList.add(item);
		}

		return new DescriptionPanel(s_res.getString("ViewObjectDescriptions.datatypesHeading"), null, null, itemList);
	}	


	private DescriptionPanel getDomainDictionary(MessageGroup group) {
		MdmiDomainDictionaryReference dictionary = group.getDomainDictionary();
		Collection <ItemAndDescription> itemList = new ArrayList <ItemAndDescription>();
		
		// get sorted refs
		List<MdmiBusinessElementReference> refs = new ArrayList<MdmiBusinessElementReference>( dictionary.getBusinessElements() );
		Collections.sort(refs, new Comparators.BusinessElementReferenceComparator());
		
		for (MdmiBusinessElementReference ref : refs) {
			ItemAndDescription item = new ItemAndDescription(ref, ref.getDescription());
			itemList.add(item);
		}
		return new DescriptionPanel(s_res.getString("ViewObjectDescriptions.domainDictionaryHeading"),
				dictionary.getName(), dictionary.getDescription(), itemList);
	}


	private DescriptionPanel getSemanticElements(MessageModel model) {
		SemanticElementSet elementSet = model.getElementSet();
		Collection <ItemAndDescription> itemList = new ArrayList <ItemAndDescription>();
		
		// get sorted elements
		List<SemanticElement> elements = new ArrayList<SemanticElement>( elementSet.getSemanticElements() );
		Collections.sort(elements, new Comparators.SemanticElementComparator());
		
		for (SemanticElement elem : elements) {
			ItemAndDescription item = new ItemAndDescription(elem, elem.getDescription());
			itemList.add(item);
		}
		return new DescriptionPanel(s_res.getString("ViewObjectDescriptions.semanticElementsHeading"),
				elementSet.getName(), elementSet.getDescription(), itemList);
	}

	
	private DescriptionPanel getSyntaxModel(MessageModel messageModel) {
		MessageSyntaxModel syntaxModel = messageModel.getSyntaxModel();
		
		Collection <ItemAndDescription> itemList = new ArrayList <ItemAndDescription>();
		// walk tree
		Node root = syntaxModel.getRoot();
		if (root != null) {
			addNodeAndChildren(root, itemList);
		}
		return new SyntaxNodeDescriptionPanel(s_res.getString("ViewObjectDescriptions.syntaxModelHeading"),
				syntaxModel.getName(), syntaxModel.getDescription(), itemList);
	}
	
	private void addNodeAndChildren(Node node, Collection<ItemAndDescription> itemList) {
		// add this node
		itemList.add(new ItemAndDescription(node, node.getDescription()));
		
		// add children
		if (node instanceof Bag) {
			// add each child, in given order
			for (Node child : ((Bag)node).getNodes()) {
				addNodeAndChildren(child, itemList);
			}
			
		} else if (node instanceof Choice) {
			// add each child, sorted by name
			List<Node> sortedChildren = new ArrayList<Node>( ((Choice)node).getNodes() );
			Collections.sort(sortedChildren, new Comparators.SyntaxNodeComparator());
			for (Node child : sortedChildren) {
				addNodeAndChildren(child, itemList);
			}
		}
	}

	@Override
	protected Component getPrintComponent() {
		// show active tab
		Component selected = m_tabs.getSelectedComponent();
		if (selected instanceof JTabbedPane) {
			selected = ((JTabbedPane)selected).getSelectedComponent();
		}
		
		if (selected instanceof DescriptionPanel) {
			return ((DescriptionPanel)selected).getTable();
		}
		return selected;
	}
	
	/** Structure for description table */
	private static class ItemAndDescription {
		public Object item;
		public String name;
		public String description;
		
		public ItemAndDescription(Object item, String description) {
			this.item = item;
			this.name = ClassUtil.getItemName(item);
			this.description = description;
		}
		
		@Override
		public String toString() {
			return this.name;
		}
	}
	
	/** A JPanel showing the name and description of the group object,
	 * followed by a table containing the name and description of each item
	 * in that group.
	 */
	private class DescriptionPanel extends JPanel {
		private String m_categoryName;
		private JTable m_descriptionTable;
		
		private MouseListener m_tableListener = new TableMouseListener();
		
		public DescriptionPanel(String categoryName, String itemName, String itemDescription,
				Collection<ItemAndDescription> items) {
			setLayout(new BorderLayout());
			
			m_categoryName = categoryName;
			// Name and description of item
			StringBuilder titleText = new StringBuilder("<html><b>");
			titleText.append(categoryName);
			if (itemName != null && itemName.length() > 0) {
				titleText.append(" ").append(itemName);
			}
			titleText.append("</b>");
			if (itemDescription != null && itemDescription.length() > 0) {
				titleText.append("<br>").append(itemDescription);
			}
			titleText.append("</html>");
			JLabel itemLabel = new JLabel(titleText.toString());
			itemLabel.setBorder(Standards.createEmptyBorder());
			add(itemLabel, BorderLayout.NORTH);
			
			// fill table
			m_descriptionTable = createTableFromData(items);
			
			setColumnWidth(0, 300);
			setColumnWidth(1, 700);
			
			
			add(new JScrollPane(m_descriptionTable), BorderLayout.CENTER);
		}
		
		/** create a table using the data */
		@SuppressWarnings("unchecked")
		private JTable createTableFromData(Collection<ItemAndDescription> items) {
			DefaultTableModel tableModel = new NonEditableTableModel();
			for (ItemAndDescription item : items) {
				tableModel.getDataVector().add(item);
			}
			return new JTable(tableModel );
		}

		@Override
		public void addNotify() {
			super.addNotify();
			
			// add listener
			m_descriptionTable.addMouseListener(m_tableListener);
			
			// add special renderers
			m_descriptionTable.getColumnModel().getColumn(0).setCellRenderer(m_col_0_renderer);
			m_descriptionTable.getColumnModel().getColumn(1).setCellRenderer(m_col_1_renderer);
			
			// add listener
		}

		@Override
		public void removeNotify() {
			m_descriptionTable.removeMouseListener(m_tableListener);
			
			// remove special renderers
			m_descriptionTable.getColumnModel().getColumn(0).setCellRenderer(null);
			m_descriptionTable.getColumnModel().getColumn(1).setCellRenderer(null);
			
			super.removeNotify();
		}
		
		private void setColumnWidth(int col, int width) {
			m_descriptionTable.getColumnModel().getColumn(col).setPreferredWidth(width);
			m_descriptionTable.getColumnModel().getColumn(col).setWidth(width);	
		}
		
		public String getCategoryName() {
			return m_categoryName;
		}
		
		public JTable getTable() {
			return m_descriptionTable;
		}
		
		private void handleDoubleClick() {
			int row = m_descriptionTable.getSelectedRow();
			Object value = m_descriptionTable.getValueAt(row, 0);
			if (value instanceof ItemAndDescription) {
				ModelTree.openUserObject(this, ((ItemAndDescription)value).item);
			}
		}
		
		private class TableMouseListener extends MouseAdapter {
			@Override
			public void mouseClicked(MouseEvent e) {
				if( e.getClickCount() == 2) {
					handleDoubleClick();
				}
			}
		}
	}
	
	/** Special panel for syntax nodes */
	private class SyntaxNodeDescriptionPanel extends DescriptionPanel {
		private SyntaxNodeRenderer m_specialRenderer = new SyntaxNodeRenderer();
		
		public SyntaxNodeDescriptionPanel(String categoryName, String itemName,
				String itemDescription, Collection<ItemAndDescription> items) {
			
			super(categoryName, itemName, itemDescription, items);
		}

		@Override
		public void addNotify() {
			super.addNotify();
			// use special renderer for column 0
			getTable().getColumnModel().getColumn(0).setCellRenderer(m_specialRenderer);
		}
		
	}

	private static class NonEditableTableModel extends DefaultTableModel {
		
		public NonEditableTableModel() {
			super();
		}
		
		@Override
		public int getColumnCount() {
			return s_columnNames.length;
		}

		@Override
		public String getColumnName(int column) {
			return s_columnNames[column];
		}


		@Override
		public Object getValueAt(int row, int column) {
			ItemAndDescription itemAndDescription = (ItemAndDescription)getDataVector().get(row);
			if (column == 0) {
				return itemAndDescription;
			} else if (column == 1) {
				// description
				return itemAndDescription.description;
			}
			return "";
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}
	
	private class BoldTextRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);
			// make bold
			setFont( getFont().deriveFont(Font.BOLD) );
			
			// add icon
			if (value instanceof ItemAndDescription) {
				setIcon( TreeNodeIcon.getTreeIcon(((ItemAndDescription)value).item.getClass()) );
			} else {
				setIcon(null);
			}
			return c;
		}
		
	}
	
	private class SyntaxNodeRenderer extends DefaultTableCellRenderer {
		private SyntaxNodeComponent m_nodeComponent = null;
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
			setIcon(null);
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);
			

			// For Node, show indented to mimic tree structure
			if (column == 0) {
				if (m_nodeComponent == null || m_nodeComponent.getTable() != table) {
					m_nodeComponent = new SyntaxNodeComponent(table);
				}
				
				Node node = null;
				if (value instanceof ItemAndDescription && ((ItemAndDescription)value).item instanceof Node) {
					node = (Node)((ItemAndDescription)value).item;
					setIcon(TreeNodeIcon.getTreeIcon(node.getClass()));
				}
				c = m_nodeComponent.render(node, c, row);
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
			Object value = getTable().getModel().getValueAt(row, 0);
			if (value instanceof ItemAndDescription && ((ItemAndDescription)value).item instanceof Node) {
				return (Node)((ItemAndDescription)value).item;
			}
			return null;
		}
		
	}
	
	private class WrappingTextRenderer extends JTextArea implements TableCellRenderer {
		
		public WrappingTextRenderer() {
			setFont(UIManager.getFont("Table.font"));
			setLineWrap(true);
			setWrapStyleWord(true);
			setOpaque(true);
			setEditable(false);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
		
			int colWidth = table.getColumnModel().getColumn(column).getWidth();
			setSize(colWidth, table.getRowHeight());
			
			setText(value == null ? "" : value.toString());
			Dimension pref = getPreferredSize();
			
			table.setRowHeight(row, pref.height);
			
			if (isSelected) {
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			} else {
				setBackground(table.getBackground());
				setForeground(table.getForeground());
			}
			
			return this;
		}
		
	}
}
