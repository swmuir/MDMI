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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.common.tables.NonEditableTableModel;
import org.openhealthtools.mdht.mdmi.editor.common.tables.TableSorter;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.tools.DatatypeUsageTree.Usage;
import org.openhealthtools.mdht.mdmi.editor.map.tree.TreeNodeIcon;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;

/** Show all datatypes, and how they are used (Message Model, Business Element Reference, Not Used) */
public class ViewDatatypeUsageSummary extends PrintableView {
	
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");

	/** Minimum/maximum dimensions */
	protected Dimension m_min = new Dimension(600, 300);
	protected Dimension m_max = new Dimension(800, 730);

	private TableSorter m_sorter;
	private JTable      m_table;
	
	private MouseClickListener m_mouseListener = new MouseClickListener();
		
	public ViewDatatypeUsageSummary(Collection<MdmiDatatype> datatypes) {
		// View Datatypes
		setTitle( s_res.getString("ViewDatatypeUsageSummary.showUsage") );
		
		// create a table that can be sorted
		TableModel model = createTableModel(datatypes);
		m_sorter = new TableSorter( model );
		m_table = new JTable(m_sorter);
		
		// add custom renderer
		DatatypeRenderer customRenderer = new DatatypeRenderer();
		for (int c=0; c<m_table.getColumnCount(); c++) {
			m_table.getColumnModel().getColumn(c).setCellRenderer(customRenderer);
		}
		
		int rowHeight = m_table.getRowHeight();
		m_table.setRowHeight(18);	// a bit bigger since we'll use 16x16 icons
		Dimension headerSize = m_table.getTableHeader().getPreferredSize();
		headerSize.height += m_table.getRowHeight() - rowHeight;
		m_table.getTableHeader().setPreferredSize(headerSize);
		
		setCenterComponent(new JScrollPane(m_table));
		m_sorter.addMouseListenerToHeaderInTable(m_table);
		m_table.addMouseListener(m_mouseListener);
		
		initFrame();	
	}
	
	/** Create table model from datatypes */
	private TableModel createTableModel(Collection<MdmiDatatype> datatypes) {
		DefaultTableModel tableModel = new NonEditableTableModel();
		// two columns - Data Type, Usage
		tableModel.setColumnIdentifiers(new String[] {s_res.getString("ViewDatatypeUsageSummary.datatype"),
				s_res.getString("ViewDatatypeUsageSummary.usage")});
		
		// this will return one entry per datatype
		Collection<Usage> users = DatatypeUsageTree.getUsage(datatypes);
		for (Usage usage : users) {
			// add a row for each datatype
			DatatypeWrapper wrapper = new DatatypeWrapper((MdmiDatatype)usage.getModel());
			UsageSummary summary = new UsageSummary(usage);
			tableModel.addRow(new Object[] {wrapper, summary});
		}
		
		return tableModel;
	}
	
	
	
	@Override
	protected Component getPrintComponent() {
		return m_table;
	}

	private void initFrame() {
		Dimension prefScrollSize = m_table.getPreferredScrollableViewportSize();
		int rowCount = m_table.getRowCount();
		int visibleRowCount =  prefScrollSize.height/m_table.getRowHeight();

		if (rowCount > visibleRowCount) {
			visibleRowCount = Math.min(rowCount, 50);
			m_table.setPreferredScrollableViewportSize(new Dimension(prefScrollSize.width,
					visibleRowCount*m_table.getRowHeight()));
		}
		
		pack(m_min, m_max);
		BaseDialog.centerOnScreen(this);
	}

	@Override
	public void dispose() {	
		for (int c=0; c<m_table.getColumnCount(); c++) {
			m_table.getColumnModel().getColumn(c).setCellRenderer(null);
		}

//		m_sorter.removeMouseListener(m_table);
		m_table.removeMouseListener(m_mouseListener);
		super.dispose();
	}

	
	public JPopupMenu createPopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();
		DatatypeWrapper selection = getSelectedDatatypeWrapper();
		if (selection != null) {
			// Open datatype
			popupMenu.add(new OpenSelectionAction(selection));
			// View usage details
			popupMenu.add(new ViewDetailsAction(selection));
		}
		
		return popupMenu;
	}

	/** Process a double-click by opening the selected node */
	public void handleDoubleClick() {
		openSelection();
	}

	/** Open (edit) the selected item */
	public void openSelection() {
		DatatypeWrapper wrapper = getSelectedDatatypeWrapper();
		if (wrapper != null) {
			openSelection(wrapper.getDatatype());
		}
	}
	
	public DatatypeWrapper getSelectedDatatypeWrapper() {
		int row = m_table.getSelectedRow();
		if (row != -1) {
			return (DatatypeWrapper)m_table.getValueAt(row, 0);
		}
		return null;
		
	}

	//-----------------------------------------------------------------//
	public static class DatatypeRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Icon icon = null;
			String toolTip = null;
			
			if (value instanceof DatatypeWrapper) {
				MdmiDatatype datatype = ((DatatypeWrapper)value).getDatatype();
				icon = TreeNodeIcon.getTreeIcon(datatype.getClass());
				toolTip = ClassUtil.createToolTip(datatype);
				
			} else if (value instanceof UsageSummary) {
				icon = ((UsageSummary)value).getIcon();
			}
			
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);
			
			setIcon(icon);
			setToolTipText(toolTip);
			
			return c;
		}
	}
	
	public static class UsageSummary {
		private String text;
		private Icon   icon;
		
		public UsageSummary(String text, Icon icon) {
			this.text = text;
			this.icon = icon;
		}
		
		public UsageSummary(Usage usage) {
			boolean isInBusinessReference = usage.isInBusinessReference();
			boolean isInMessageModel = usage.isInMessageModel();

			// determine text and icon
			StringBuilder buf = new StringBuilder();
			if (isInMessageModel && isInBusinessReference) {
				buf.append(s_res.getString("ViewDatatypeUsageSummary.messageModel"))
				   .append(", ")
				   .append(s_res.getString("ViewDatatypeUsageSummary.bizElementRef"));
				
				icon = new DoubleIcon(TreeNodeIcon.MessageModelIcon,
						TreeNodeIcon.BusinessElementReferenceIcon);

			} else if (isInMessageModel) {
				buf.append(s_res.getString("ViewDatatypeUsageSummary.messageModel"));
				icon = TreeNodeIcon.MessageModelIcon;

			} else if (isInBusinessReference) {
				buf.append(s_res.getString("ViewDatatypeUsageSummary.bizElementRef"));
				icon = TreeNodeIcon.BusinessElementReferenceIcon;

			} else {
				buf.append(s_res.getString("ViewDatatypeUsageSummary.notUsed"));
			}
			text = buf.toString();

		}

		@Override
		public String toString() {
			return text;
		}
		
		public Icon getIcon() {
			return icon;
		}
	}
	
	
	public static class DoubleIcon implements Icon {
		private Icon m_left;
		private Icon m_right;
		
		private static int s_gap = 2;
		
		public DoubleIcon(Icon left, Icon right) {
			m_left = left;
			m_right = right;
		}
		
		@Override
		public int getIconHeight() {
			return Math.max(m_left.getIconHeight(), m_right.getIconHeight());
		}

		@Override
		public int getIconWidth() {
			return m_left.getIconWidth() + s_gap + m_right.getIconWidth();
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			m_left.paintIcon(c, g, x, y);
			m_right.paintIcon(c, g, x+m_left.getIconWidth()+s_gap, y);
		}
	}
	
	/** wrapper for MdmiDatatype to allow sorting */
	public static class DatatypeWrapper {
		private MdmiDatatype m_datatype;
		public DatatypeWrapper(MdmiDatatype datatype) {
			m_datatype = datatype;
		}
		
		public MdmiDatatype getDatatype() {
			return m_datatype;
		}
		
		@Override
		public String toString() {
			return m_datatype.getTypeName();
		}
	}
	////////////////////////////////////////////////////
	// Popup menu handling
	//////////////////////////////////////////////////
	private class MouseClickListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopup(e);
				
			} else if (e.getClickCount() == 2) {
				handleDoubleClick();
			}
		}
		
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

		/** Show popup menu based on selection */
		private void showPopup(MouseEvent e) {
			JTable table = (JTable)e.getSource();

			// select row if its not already selected
			int rowAtPoint = table.rowAtPoint(e.getPoint());
			if (rowAtPoint != -1) {
				table.setRowSelectionInterval(rowAtPoint, rowAtPoint);
			}
			
			JPopupMenu popupMenu = createPopupMenu();

			if (popupMenu != null) {
				popupMenu.show(table, e.getX(), e.getY());
			}
		}
	}
	
	public class ViewDetailsAction extends AbstractAction {
		public ViewDetailsAction(DatatypeWrapper wrapper) {
			super(MessageFormat.format(s_res.getString("ViewDatatypeUsageSummary.usageDetailsFormat"), 
					wrapper.toString()));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			DatatypeWrapper wrapper = getSelectedDatatypeWrapper();
			if (wrapper != null) {
				ViewDatatypeUsageTree view = new ViewDatatypeUsageTree(wrapper.getDatatype());
				ViewDatatypeUsageSummary.this.setVisible(true);
				view.setVisible(true);
			}
		}
		
	}

	public class OpenSelectionAction extends AbstractAction {
		public OpenSelectionAction(DatatypeWrapper wrapper) {
			super(MessageFormat.format(s_res.getString("ViewDataObject.openFormat"), 
					ClassUtil.beautifyName(wrapper.getDatatype().getClass())));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			openSelection();
		}
		
	}

}
