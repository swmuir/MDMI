package org.openhealthtools.mdht.mdmi.editor.be_editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.openhealthtools.mdht.mdmi.editor.be_editor.ServerInterface.RetrievePosition;
import org.openhealthtools.mdht.mdmi.editor.be_editor.tables.MdmiTableModel;
import org.openhealthtools.mdht.mdmi.editor.be_editor.tables.TableEntry;
import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.common.components.CursorManager;
import org.openhealthtools.mdht.mdmi.editor.common.components.VerticalButtonPanel;
import org.openhealthtools.mdht.mdmi.editor.common.tables.TableSorter;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;

// 
//   Search: [______________] [Q]
//   
//    --------------------------
//   | * | Name         | type  |
//   |--------------------------| [New ]
//   |   |              |       | [Edit]
//   |   |              |       | [Revert]
//   |   |              |       | [Remove]
//   |   |              |       | [Delete] (future)
//   |   |              |       |
//    --------------------------
//
//       [Commit Changes]

public abstract class AbstractDisplayPanel extends JPanel {
	
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.be_editor.Local");
	
	private MdmiTableModel m_tableModel;
	private String m_objectType;	// "Business Element Reference" or "Datatype"
	private TableSorter m_tableSorter;	// allow sorting by clicking column header
	private JTable m_table;
	
	private JTextField m_searchText;
	// only one of these two will be visible
	private JButton m_searchBtn;
	private JButton m_searchMoreBtn;
	
	protected RetrievePosition m_retrievePosition = null;	// used for search more

	private JButton m_addBtn;
	private JButton m_editBtn;
	private JButton m_revertBtn;
	private JButton m_removeBtn;
	private JButton m_delBtn;
	
	private JButton m_commitBtn;
	
	// Listeners
	private ListSelectionListener m_tableListener = new TableListener();
	private MouseClickListener m_tableClickListener = new MouseClickListener();
	private ActionListener m_buttonListener = new ButtonListener();
	private DocumentListener m_docListener = new TextFieldListener();
	
	// Renderer
	private ModifiedColumnRenderer m_modifiedColRenderer = new ModifiedColumnRenderer();

	public AbstractDisplayPanel(String objectType) {
		m_objectType = objectType;
		
		m_searchText = new JTextField(20);
		m_searchBtn = createImageButton(s_res.getString("AbstractDisplayPanel.findIcon"), null);
		m_searchMoreBtn = createImageButton(s_res.getString("AbstractDisplayPanel.findMoreIcon"), null);
		
		m_addBtn = createImageButton(s_res.getString("AbstractDisplayPanel.newIcon"), 
				s_res.getString("AbstractDisplayPanel.newBtn"));
		m_editBtn = createImageButton(s_res.getString("AbstractDisplayPanel.editIcon"),
				s_res.getString("AbstractDisplayPanel.editBtn"));
		m_revertBtn = createImageButton(s_res.getString("AbstractDisplayPanel.revertIcon"),
				s_res.getString("AbstractDisplayPanel.revertBtn"));
		m_removeBtn = createImageButton(s_res.getString("AbstractDisplayPanel.removeIcon"),
				s_res.getString("AbstractDisplayPanel.removeBtn"));
		m_delBtn = createImageButton(s_res.getString("AbstractDisplayPanel.delIcon"),
				s_res.getString("AbstractDisplayPanel.delBtn"));

		
		m_commitBtn = createImageButton(s_res.getString("AbstractDisplayPanel.commitIcon"),
				MessageFormat.format(s_res.getString("AbstractDisplayPanel.commitBtnFmt"), objectType));
	}
	
	protected ImageIcon getImage(String imagePath) {
		// Use Icon
		URL imageURL = getClass().getResource(imagePath);
		if (imageURL != null) {
			ImageIcon icon = new ImageIcon(imageURL);
			return icon;
		}
		
		return null;
	}
	
	protected JButton createImageButton(String imagePath, String btnText) {
		// Use Icon
		ImageIcon icon = getImage(imagePath);
		if (icon != null) {
			JButton btn = new JButton(btnText, icon);
			return btn;
		}
		
		return new JButton(btnText == null ? "?" : btnText);
	}
	
	protected void setTableModel(MdmiTableModel tableModel)
	{

		m_tableSorter = new TableSorter( tableModel );
		
		m_tableModel = tableModel;
		m_table = new JTable(m_tableSorter);

		// this sets up the sorting on a column header
		m_tableSorter.addMouseListenerToHeaderInTable(m_table);
		
		
		m_table.setColumnSelectionAllowed(false);
		m_table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);	// multiple selection
		setTableColumnWidths();
		
		// set renderer for first column
		m_table. getColumnModel().getColumn(0).setCellRenderer(m_modifiedColRenderer);
		
		buildUI();
		
		enableFields();
	}

	public JTable getTable() {
		return m_table;
	}

	public MdmiTableModel getTableModel() {
		return m_tableModel;
	}
	
	public TableSorter getTableSorter() {
		return m_tableSorter;
	}
	
	//indicate that data has been added or removed from the table
	public void tableSizeChanged() {
		// save sort information
		int [] sortCols = m_tableSorter.getSortColumns();
		boolean [] sortOrder = new boolean [sortCols.length];
		for (int i=0; i<sortCols.length; i++) {
			sortOrder[i] = m_tableSorter.isAsscending(sortCols[i]);
		}
			
		m_tableSorter.reallocateIndexes();
		
		// re-sort
		if (sortCols.length > 0)
		{
			m_tableSorter.complexSort(sortCols, sortOrder);
		}
	}
	
	
	public TableEntry getEntry(int row)
	{
		// since we've sorted, we need to convert
		int idx = m_tableSorter.getModelIndexFromRow(row);
		return m_tableModel.getEntry(idx);
	}

	private void buildUI() {
		
		// 
		//   Search: [______________] [Q]
		//   
		//    --------------------------
		//   | * | Col 1        | Col 2 |
		//   |--------------------------| [New ]
		//   |   |              |       | [Edit]
		//   |   |              |       | [Revert]
		//   |   |              |       | [Delete] (future)
		//   |   |              |       |
		//   |   |              |       |
		//    --------------------------
		//
		//       [Commit Changes]
		
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = Standards.getInsets();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		
		// Search row
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		add(new JLabel("Search:"), gbc);
		gbc.gridx++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		add(m_searchText, gbc);
		gbc.gridx++;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		add(m_searchBtn, gbc);
		add(m_searchMoreBtn, gbc);
		m_searchMoreBtn.setVisible(false);

		gbc.gridx = 0;
		gbc.gridy++;
		
		// Action Buttons
		JPanel middle = new JPanel();
		middle.setLayout(new BorderLayout());
		VerticalButtonPanel buttons = createVerticalButtonPanel();
		middle.add(buttons, BorderLayout.EAST);
		
		// Table
		middle.add(new JScrollPane(m_table), BorderLayout.CENTER);
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.BOTH;
		add(middle, gbc);
		
		gbc.gridy++;
		
		// Commit Button
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		add(m_commitBtn, gbc);
		
	}

	/** Create and populate the button panel containing the add/remove/etc buttons */
	protected VerticalButtonPanel createVerticalButtonPanel() {
		VerticalButtonPanel buttons = new VerticalButtonPanel();
		buttons.addStrut(m_addBtn.getPreferredSize().height);
		buttons.add(m_addBtn);
		buttons.add(m_editBtn);
		buttons.add(m_revertBtn);
		buttons.add(m_removeBtn);
		buttons.add(m_delBtn);
		
		// For now, deleting is not permitted
		m_delBtn.setVisible(false);
		
		buttons.setBorder(Standards.createEmptyBorder());
		return buttons;
	}
	
	
	// set the column widths
	protected void setTableColumnWidths() {
		// just set first column
		setColumnWidth(0, 50);
		TableColumn column = m_table.getColumnModel().getColumn(0);
		column.setMaxWidth(50);
		column.setResizable(false);
	}
	
	// set a column width
	public void setColumnWidth(int col, int width) {
		TableColumn column = m_table.getColumnModel().getColumn(col);
		column.setWidth(width);
		column.setPreferredWidth(width);
	}
	
	
	@Override
	public void addNotify() {
		super.addNotify();
		
		// add all our listeners
		m_table.getSelectionModel().addListSelectionListener(m_tableListener);
		m_table.addMouseListener(m_tableClickListener);
		m_searchText.getDocument().addDocumentListener(m_docListener);
		m_searchBtn.addActionListener(m_buttonListener);
		m_searchMoreBtn.addActionListener(m_buttonListener);
		m_addBtn.addActionListener(m_buttonListener);
		m_editBtn.addActionListener(m_buttonListener);
		m_revertBtn.addActionListener(m_buttonListener);
		m_removeBtn.addActionListener(m_buttonListener);
		m_delBtn.addActionListener(m_buttonListener);
		m_commitBtn.addActionListener(m_buttonListener);
		
		// Add Tooltips
		m_searchBtn.setToolTipText(MessageFormat.format(s_res.getString("AbstractDisplayPanel.findTooltip"),
				m_objectType));
		m_searchMoreBtn.setToolTipText(MessageFormat.format(s_res.getString("AbstractDisplayPanel.findMoreTooltip"),
				m_objectType));
		m_addBtn.setToolTipText(MessageFormat.format(s_res.getString("AbstractDisplayPanel.newTooltip"),
				m_objectType));
		m_editBtn.setToolTipText(MessageFormat.format(s_res.getString("AbstractDisplayPanel.editTooltip"),
				m_objectType));
		m_revertBtn.setToolTipText(MessageFormat.format(s_res.getString("AbstractDisplayPanel.revertTooltip"),
				m_objectType));
		m_removeBtn.setToolTipText(MessageFormat.format(s_res.getString("AbstractDisplayPanel.removeTooltip"),
				m_objectType));
		m_delBtn.setToolTipText(MessageFormat.format(s_res.getString("AbstractDisplayPanel.delTooltip"),
				m_objectType));
		m_commitBtn.setToolTipText(MessageFormat.format(s_res.getString("AbstractDisplayPanel.commitTooltip"),
				m_objectType));
	}

	@Override
	public void removeNotify() {
		
		// remove all our listeners
		m_table.getSelectionModel().removeListSelectionListener(m_tableListener);
		m_table.removeMouseListener(m_tableClickListener);
		m_searchText.getDocument().removeDocumentListener(m_docListener);
		m_searchBtn.removeActionListener(m_buttonListener);
		m_searchMoreBtn.removeActionListener(m_buttonListener);
		m_addBtn.removeActionListener(m_buttonListener);
		m_editBtn.removeActionListener(m_buttonListener);
		m_revertBtn.removeActionListener(m_buttonListener);
		m_removeBtn.removeActionListener(m_buttonListener);
		m_delBtn.removeActionListener(m_buttonListener);
		m_commitBtn.removeActionListener(m_buttonListener);
		
		// Remove Tooltips
		m_searchBtn.setToolTipText(null);
		m_searchMoreBtn.setToolTipText(null);
		m_addBtn.setToolTipText(null);
		m_editBtn.setToolTipText(null);
		m_revertBtn.setToolTipText(null);
		m_removeBtn.setToolTipText(null);
		m_delBtn.setToolTipText(null);
		m_commitBtn.setToolTipText(null);
		
		super.removeNotify();
	}
	
	// enable/disable according to selection
	public void enableFields() {
		// Search button enabled if there's search text
		boolean hasSearchText = !m_searchText.getText().trim().isEmpty();
		m_searchBtn.setEnabled(hasSearchText);
		
		
		// Edit, Revert and Delete enabled if something is selected
		int[] rows = m_table.getSelectedRows();
		
		m_editBtn.setEnabled(false);
		m_revertBtn.setEnabled(false);
		m_removeBtn.setEnabled(false);
		m_delBtn.setEnabled(false);
		
		for (int row : rows) {
			TableEntry entry = getEntry(row);
			if (entry == null) continue;
			
			if (rows.length == 1) {
				// Edit - requires single selection
				m_editBtn.setEnabled(true);
			}

			// Revert and Delete - requires any valid selection
			if (entry.isDirty()) {
				m_revertBtn.setEnabled(true);
			}
			if (!entry.isDeleted()) {
				m_delBtn.setEnabled(true);
			}
			m_removeBtn.setEnabled(true);
		}
		
		// Commit enabled if anything in the table is dirty
		m_commitBtn.setEnabled(m_tableModel.hasChanges());
		
	}

	// find the item with this name on the server
	protected abstract Object getObjectFromService(ServerInterface service, String name);

	// search for objects matching the search text
	protected abstract boolean findItems(String searchText, RetrievePosition retrievePosition);
	
	// create a new Object
	protected abstract Object createNewItem();
	
	// modify the selected item
	protected abstract boolean modifyItem(TableEntry entry);
	
	// undo the changes on the selected items
	protected boolean undoChanges(int[] rows) {
		ServerInterface service = ServerInterface.getInstance();
		boolean changed = false;

		for (int row : rows) {
			TableEntry entry = getEntry(row);

			if (entry.isDirty()) {
				Object userObject = entry.getUserObject();
				String objectName = m_tableModel.getObjectName(userObject);
				Object serverObject = getObjectFromService(service, objectName);
				
				if (serverObject == null) {
					String message = m_tableModel.getObjectTypeName(userObject) + " '" + objectName +
							"' cannot be found on the server.";
					JOptionPane.showMessageDialog(this, message, "Invalid Data", JOptionPane.WARNING_MESSAGE);

				} else {
					// replace contents with the one from the server
					try {
						ClassUtil.copyData(userObject, serverObject);
					} catch (Exception ex) {
						BaseDialog.showError(this, "Error Encountered",
								ex.getLocalizedMessage());
					}
					entry.setDirty(false);
					changed = true;
				}
			}
		}
		
		return changed;
	}

	// delete the selected items from the display
	protected  boolean removeItems(int[] rows) {

		ArrayList<TableEntry> entriesToRemove = new ArrayList<TableEntry>();
		
		// check if any have changes
		int dirtyCount = 0;
		StringBuilder changedItems = new StringBuilder();
		for (int i = 0; i<rows.length; i++) {
			int row = rows[i];
			TableEntry entry = getEntry(row);
			if (entry.isDirty()) {
				dirtyCount++;
				if (changedItems.length() > 0) changedItems.append("\n");
				changedItems.append("    ").append(m_tableModel.getObjectName(entry.getUserObject()));
			}

			entriesToRemove.add(entry);
		}
		
		if (dirtyCount > 0) {
			String openingLine = "The following items have been modified:";
			String closingLine = "Do you really want to remove them and lose your changes?";
			
			// Wrap in scroll pane if too long
			Object message;

			if (dirtyCount == 1)
			{
				StringBuffer buf = new StringBuffer();
				TableEntry entry = entriesToRemove.get(0);
				buf.append(m_tableModel.getObjectName(entry.getUserObject())).append(" has been modified.");
				buf.append("\n").append("Do you really want to remove it and lose your changes?");
				message = buf.toString();
				
			} else if (dirtyCount <= 10){
				StringBuffer buf = new StringBuffer();
				buf.append(openingLine).append("\n");
				buf.append(changedItems).append("\n");
				buf.append(closingLine);
				message = buf.toString();
			
			} else  {
				JPanel panel = new JPanel(new BorderLayout());
				// opening line
				panel.add(new JLabel(openingLine), BorderLayout.NORTH);
				// scroll pane
				JTextArea textArea = new JTextArea(11, 60);
				textArea.setFont(panel.getFont());
				textArea.setLineWrap(false);
				textArea.setOpaque(false);
				textArea.setEditable(false);
				textArea.setText(changedItems.toString());
				panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
				message = panel;
				// closing line
				panel.add(new JLabel(closingLine), BorderLayout.SOUTH);
			}

			String title = "Remove Items";
			int rc = JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION);
			if (rc != JOptionPane.YES_OPTION) {
				return false;
			}
		}
		
		// now physically remove any 
		for (TableEntry entry : entriesToRemove) {
			m_tableModel.removeEntry(entry);
		}
		if (entriesToRemove.size() > 0) {
			// clear selection
			m_table.getSelectionModel().clearSelection();
			// reselect the last row in the selection list
			int lastRowSelected = rows[rows.length-1];
			if (lastRowSelected >= m_tableModel.getRowCount()) {
				lastRowSelected = m_tableModel.getRowCount()-1;
			}
			if (lastRowSelected >= 0) {
				m_table.getSelectionModel().setSelectionInterval(lastRowSelected, lastRowSelected);
			}
			tableSizeChanged();
		}
		return true;
	}
	
	// delete the selected items (by marking as needing delete)
	protected  boolean deleteItems(int[] rows) {

		ArrayList<Integer> rowsToRemoveList = new ArrayList<Integer>();

		// first save entries that can be removed
		for (int i = 0; i<rows.length; i++) {
			int row = rows[i];
			TableEntry entry = getEntry(row);
			
			if (m_tableModel.isNew(entry.getUserObject())) {
				// save row, so we can remove in the next step
				rowsToRemoveList.add(row);
				
			} else {
				// mark for deletion
				m_tableModel.markDeleted(entry);
			}
		}
		
		// now physically remove the new ones from the display
		int [] rowsToRemove = new int[rowsToRemoveList.size()];
		for (int i=0; i<rowsToRemove.length; i++) {
			rowsToRemove[i] = rowsToRemoveList.get(i);
		}
		if (rowsToRemove.length > 0) {
			removeItems(rowsToRemove);
		}
		
		return true;
	}

	protected void handleDoubleClick() {
		// enter button
		if (m_editBtn.isEnabled()) {
			m_editBtn.doClick();
		}
	}
	
	// commit all changes on server
	protected abstract boolean commitChanges();
	
	// commit error handling
	protected void appendErrorText(StringBuilder errors, String itemName, Exception ex) {
		errors.append("<br>&nbsp;&nbsp;&nbsp;&nbsp;");
		errors.append("<b>").append(itemName).append("</b>");
		errors.append(" - ").append(ex.getLocalizedMessage());
	}

	// add a referenced data type if we need one
	protected boolean addReferencedDatatype(MdmiDatatype refDatatype, StringBuilder errors)
	{
		ServerInterface service = ServerInterface.getInstance();
		
		if (!refDatatype.isPrimitive()) {
			MdmiDatatype datatypeFound = service.getDatatype(refDatatype.getTypeName());
			if (datatypeFound == null) {
				// need to add it

				// it's new, so set the group information
				MessageGroup group = service.getMessageGroup();
				refDatatype.setOwner(group);
				
				try {
					service.addDatatype(refDatatype);
				} catch (Exception ex) {
					appendErrorText(errors, refDatatype.getTypeName(), ex);
					return false;
				}
			}
		}
		
		return true;
	}


	/** Scan table, checking for any changes. Clear flag if all entries are clean */
	protected void checkTableForChanges() {
		// scan table to check if any other entries are dirty
		boolean foundDirtyEntry = false;
		for (int row = 0 ; row < m_tableModel.getRowCount() && !foundDirtyEntry; row++) {
			TableEntry otherEntry = getEntry(row);
			if (otherEntry.isDirty()) {
				foundDirtyEntry = true;
			}
		}
		if (!foundDirtyEntry) {
			m_tableModel.clearChanges();
		}
	}
	
	
	///////////////////////////////////////////////////////////
	//  Listeners
	///////////////////////////////////////////////////////////
	
	private class TableListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting()) {
				return;
			}
			enableFields();
		}
		
	}

	private class ButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			Frame frame = SystemContext.getApplicationFrame();
			CursorManager cm = CursorManager.getInstance(frame);
			
			try {
				cm.setWaitCursor();
				
				Object source = e.getSource();
				boolean changed = false;

				int[] selectedRows = m_table.getSelectedRows();
				
				
				if (source == m_searchBtn || source == m_searchMoreBtn) {
					String searchText = m_searchText.getText().trim();
					
					// use new position for search
					if (source == m_searchBtn || m_retrievePosition == null) {
						m_retrievePosition = new RetrievePosition();
					}
					changed = findItems(searchText, m_retrievePosition);
					
					// check position
					if (m_retrievePosition.numFound() == ServerInterface.MAX_SEARCH) {
						// there are more
						m_searchBtn.setVisible(false);
						m_searchMoreBtn.setVisible(true);
					} else {
						// no more left
						m_searchBtn.setVisible(true);
						m_searchMoreBtn.setVisible(false);
					}
					
				} else if (source == m_searchMoreBtn) {

					m_searchBtn.setVisible(true);
					m_searchMoreBtn.setVisible(false);
					
				} else if (source == m_addBtn) {
					Object obj = createNewItem();
					if (obj != null) {
						m_tableModel.addNewEntry(obj);
						changed = true;
					}

				} else if (source == m_commitBtn) {
					if (commitChanges()) {
						//  mark all elements in table 
						for (int row = m_tableModel.getRowCount()-1; row >= 0; row--) {
							TableEntry entry = m_tableModel.getEntry(row);
							if (entry.isDeleted()) {
								// remove it from table
								m_tableModel.removeEntry(row);
							} else {
								// mark as clean
								entry.setDirty(false);
							}
						}
						// mark table as updated
						m_tableModel.clearChanges();
					}
					changed = true;

				} else if (source == m_editBtn && selectedRows.length == 1) {
					// restricted to single entry
					TableEntry entry = getEntry(selectedRows[0]);
					changed = modifyItem(entry);
					// mark table as updated
					if (changed) {
						m_tableModel.setDirty(entry);
					}

				} else if (source == m_revertBtn) {
					changed = undoChanges(selectedRows);
					// mark entries as clean 
					if (changed) {
						// entire table may be clean
						checkTableForChanges();
					}

				} else if (source == m_delBtn) {
					changed = deleteItems(selectedRows);
					
				} else if (source == m_removeBtn) {
					changed = removeItems(selectedRows);
				}

				
				// refresh table
				if (changed) {
					tableSizeChanged();
					m_table.invalidate();
					m_table.repaint();
					revalidate();
				}
				enableFields();
				
			} catch (Exception ex) {
				BaseDialog.showError(AbstractDisplayPanel.this, "Error Encountered",
						ex.getLocalizedMessage());

			} finally {
				cm.restoreCursor();
			}
		}
		
	}

	private class MouseClickListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopup(e);
				
			} else if (e.getClickCount() == 2) {
				handleDoubleClick();
			}
		}
		
//		@Override
//		public void mousePressed(MouseEvent e) {
//			// select row
//			JTable table = (JTable)e.getSource();
//			int rowAtPoint = table.rowAtPoint(e.getPoint());
//			if (rowAtPoint != -1) {
//				table.setRowSelectionInterval(rowAtPoint, rowAtPoint);
//			}
//
//			if (e.isPopupTrigger()) {
//				showPopup(e);
//			}
//		}
//
//		@Override
//		public void mouseReleased(MouseEvent e) {
//			if (e.isPopupTrigger()) {
//				showPopup(e);
//			}
//		}
//
//		/** Show popup menu based on selection */
		private void showPopup(MouseEvent e) {
//			JTable table = (JTable)e.getSource();
//
//			// select row if its not already selected
//			int rowAtPoint = table.rowAtPoint(e.getPoint());
//			if (rowAtPoint != -1) {
//				boolean selected = false;
//				int [] selectedRows = table.getSelectedRows();
//				for (int i=0; i<selectedRows.length; i++) {
//					if (selectedRows[i] == rowAtPoint) {
//						selected = true;
//						break;
//					}
//				}
//				if (!selected) {
//					table.setRowSelectionInterval(rowAtPoint, rowAtPoint);
//				}
//			}
//			
//			JPopupMenu popupMenu = createPopupMenu();
//
//			if (popupMenu != null) {
//				popupMenu.show(table, e.getX(), e.getY());
//			}
		}
	}
	
	private class TextFieldListener implements DocumentListener {

		@Override
		public void insertUpdate(DocumentEvent e) {
			documentChanged(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			documentChanged(e);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			documentChanged(e);
		}
		
		private void documentChanged(DocumentEvent e) {
			enableFields();

			// always restore search button
			m_searchBtn.setVisible(true);
			m_searchMoreBtn.setVisible(false);
		}
		
	}

	// Renderer for Modified column
	protected  class ModifiedColumnRenderer extends DefaultTableCellRenderer {
		private ImageIcon m_newIcon = null;
		private ImageIcon m_dirtyIcon = null;
		private ImageIcon m_deletedIcon = null;
		
		public ModifiedColumnRenderer() {
			// create icons
			URL imageURL;
			
			imageURL = getClass().getResource(s_res.getString("ModifiedColumnRenderer.newIcon"));
			if (imageURL != null) {
				m_newIcon = new ImageIcon(imageURL);
			}
			
			imageURL = getClass().getResource(s_res.getString("ModifiedColumnRenderer.dirtyIcon"));
			if (imageURL != null) {
				m_dirtyIcon = new ImageIcon(imageURL);
			}

			imageURL = getClass().getResource(s_res.getString("ModifiedColumnRenderer.deletedIcon"));
			if (imageURL != null) {
				m_deletedIcon = new ImageIcon(imageURL);
			}
			
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
			// add icon
			JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);
			
			TableEntry entry = getEntry(row);
			if (m_tableModel.isNew(entry.getUserObject())) {
				label.setText("");
				label.setIcon(m_newIcon);
				label.setToolTipText("New Item");
			}
			else if (entry.isDeleted()) {
				label.setText("");
				label.setIcon(m_deletedIcon);
				label.setToolTipText("Item Deleted");
				
			} else if (entry.isDirty()) {
				label.setText("");
				label.setIcon(m_dirtyIcon);
				label.setToolTipText("Item Modified");
			} else {
				label.setIcon(null);
				label.setToolTipText(null);
			}
			
			return label;
		}
		
	}
	
}
