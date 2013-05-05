package org.openhealthtools.mdht.mdmi.editor.be_editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.openhealthtools.mdht.mdmi.editor.be_editor.tables.MdmiTableModel;
import org.openhealthtools.mdht.mdmi.editor.be_editor.tables.TableEntry;
import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.common.components.VerticalButtonPanel;

// 
//   Search: [______________] [Q]
//   
//    --------------------------
//   | * | Name         | type  |
//   |--------------------------| [New ]
//   |   |              |       | [Edit]
//   |   |              |       | [Revert]
//   |   |              |       | [Delete] (future)
//   |   |              |       |
//   |   |              |       |
//    --------------------------
//
//       [Commit Changes]

public abstract class AbstractDisplayPanel extends JPanel {
	
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.be_editor.Local");
	
	private MdmiTableModel m_tableModel;
	private JTable m_table;
	
	private JTextField m_searchText;
	private JButton m_searchBtn;

	private JButton m_addBtn;
	private JButton m_editBtn;
	private JButton m_revertBtn;
	private JButton m_delBtn;
	
	private JButton m_commitBtn;
	
	// Listeners
	private ListSelectionListener m_tableListener = new TableListener();
	private ActionListener m_buttonListener = new ButtonListener();
	private DocumentListener m_docListener = new TextFieldListener();
	
	// Renderer
	private ModifiedColumnRenderer m_modifiedColRenderer = new ModifiedColumnRenderer();

	public AbstractDisplayPanel(String objectType) {
		
		m_searchText = new JTextField(20);
		m_searchBtn = createImageButton(s_res.getString("AbstractDisplayPanel.findIcon"), null);
		
		m_addBtn = createImageButton(s_res.getString("AbstractDisplayPanel.newIcon"), 
				s_res.getString("AbstractDisplayPanel.newBtn"));
		m_editBtn = createImageButton(s_res.getString("AbstractDisplayPanel.editIcon"),
				s_res.getString("AbstractDisplayPanel.editBtn"));
		m_revertBtn = createImageButton(s_res.getString("AbstractDisplayPanel.revertIcon"),
				s_res.getString("AbstractDisplayPanel.revertBtn"));
		m_delBtn = createImageButton(s_res.getString("AbstractDisplayPanel.delIcon"),
				s_res.getString("AbstractDisplayPanel.delBtn"));
		m_commitBtn = createImageButton(s_res.getString("AbstractDisplayPanel.commitIcon"),
				MessageFormat.format(s_res.getString("AbstractDisplayPanel.commitBtnFmt"), objectType));
		
	}
	
	protected JButton createImageButton(String imagePath, String btnText) {
		// Use Icon
		URL imageURL = getClass().getResource(imagePath);
		if (imageURL != null) {
			ImageIcon icon = new ImageIcon(imageURL);
			JButton btn = new JButton(btnText, icon);
			return btn;
		}
		
		return new JButton(btnText == null ? "?" : btnText);
	}
	
	protected void setTableModel(MdmiTableModel tableModel)
	{
		m_tableModel = tableModel;
		m_table = new JTable(m_tableModel);
		m_table.setColumnSelectionAllowed(false);
		m_table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);	// single selection
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
		

		gbc.gridx = 0;
		gbc.gridy++;
		
		// Table and buttons
		JPanel middle = new JPanel();
		middle.setLayout(new BorderLayout());
		VerticalButtonPanel buttons = new VerticalButtonPanel();
		buttons.addStrut(m_addBtn.getPreferredSize().height);
		buttons.add(m_addBtn);
		buttons.add(m_editBtn);
		buttons.add(m_revertBtn);
		buttons.add(m_delBtn);
		buttons.setBorder(Standards.createEmptyBorder());
		middle.add(buttons, BorderLayout.EAST);
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
	
	
	// set the column widths
	protected void setTableColumnWidths() {
		// just set first column
		setColumnWidth(0, 50);
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
		m_searchText.getDocument().addDocumentListener(m_docListener);
		m_searchBtn.addActionListener(m_buttonListener);
		m_addBtn.addActionListener(m_buttonListener);
		m_editBtn.addActionListener(m_buttonListener);
		m_revertBtn.addActionListener(m_buttonListener);
		m_delBtn.addActionListener(m_buttonListener);
		m_commitBtn.addActionListener(m_buttonListener);
	}

	@Override
	public void removeNotify() {
		
		// remove all our listeners
		m_table.getSelectionModel().removeListSelectionListener(m_tableListener);
		m_searchText.getDocument().removeDocumentListener(m_docListener);
		m_searchBtn.removeActionListener(m_buttonListener);
		m_addBtn.removeActionListener(m_buttonListener);
		m_editBtn.removeActionListener(m_buttonListener);
		m_revertBtn.removeActionListener(m_buttonListener);
		m_delBtn.removeActionListener(m_buttonListener);
		m_commitBtn.removeActionListener(m_buttonListener);
		
		super.removeNotify();
	}
	
	// enable/disable according to selection
	private void enableFields() {
		// Search button enabled if there's search text
		m_searchBtn.setEnabled(!m_searchText.getText().trim().isEmpty());
		
		// Edit and Delete enabled if something is selected
		int row = m_table.getSelectedRow();
		TableEntry entry = m_tableModel.getEntry(row);
		
		m_editBtn.setEnabled(entry != null);
		m_revertBtn.setEnabled(entry != null && entry.isDirty());
		m_delBtn.setEnabled(entry != null && !entry.isDeleted());
		
		// Commit enabled if anything in the table is dirty
		m_commitBtn.setEnabled(m_tableModel.hasChanges());
	}

	// search for an object
	protected abstract boolean findItems(String searchText);
	
	// create a new Object
	protected abstract Object createNewItem();
	
	// modify the selected item
	protected abstract boolean modifyItem(TableEntry entry);
	
	// undo the changes on the selected item
	protected abstract boolean undoChanges(TableEntry entry);
	
	// delete the selected item (by marking as needing delete)
	protected  boolean deleteItem(TableEntry entry) {
		m_tableModel.deleteEntry(entry);
		return true;
	}
	
	// commit all changes on server
	protected abstract boolean commitChanges();


	/** Scan table, checking for any changes. Clear flag if all entries are clean */
	protected void checkTableForChanges() {
		// scan table to check if any other entries are dirty
		boolean foundDirtyEntry = false;
		for (int row = 0 ; row < m_tableModel.getRowCount() && !foundDirtyEntry; row++) {
			TableEntry otherEntry = m_tableModel.getEntry(row);
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
			try {
				if (e.getSource() == m_searchBtn) {
					findItems(m_searchText.getText().trim());
					
				} else if (e.getSource() == m_addBtn) {
					Object obj = createNewItem();
					if (obj != null) {
						m_tableModel.addEntry(obj);
						m_table.invalidate();
					}

				} else if (e.getSource() == m_commitBtn) {
					if (commitChanges()) {

						//  mark all elements in table as clean
						for (int row = 0 ; row < m_tableModel.getRowCount(); row++) {
							TableEntry entry = m_tableModel.getEntry(row);
							entry.setDirty(false);
						}
						m_tableModel.clearChanges();
						m_table.invalidate();
					}

				} else {
					int selectedRow = m_table.getSelectedRow();
					TableEntry entry = getTableModel().getEntry(selectedRow);
					boolean changed = false;
					
					if (e.getSource() == m_editBtn) {
						changed = modifyItem(entry);
						// mark table as updated
						if (changed) {
							m_tableModel.setDirty(entry);
						}
						
					} else if (e.getSource() == m_revertBtn) {
						changed = undoChanges(entry);
						// mark entry as clean 
						if (changed) {
							entry.setDirty(false);
							// entire table may be clean
							checkTableForChanges();
						}

					} else if (e.getSource() == m_delBtn) {
						changed = deleteItem(entry);
					}
					
					// update table
					if (changed) {
						m_table.invalidate();
						m_table.repaint();
					}
					
				}

				enableFields();
				
			} catch (Exception ex) {
				BaseDialog.showError(AbstractDisplayPanel.this, "Error Encountered",
						ex.getLocalizedMessage());
			}
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
		}
		
	}

	// Renderer for Modified column
	protected  class ModifiedColumnRenderer extends DefaultTableCellRenderer {

		private ImageIcon m_dirtyIcon = null;
		private ImageIcon m_deletedIcon = null;
		
		public ModifiedColumnRenderer() {
			// create icons
			URL imageURL = getClass().getResource(s_res.getString("ModifiedColumnRenderer.dirtyIcon"));
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
			

			TableEntry entry = m_tableModel.getEntry(row);
			if (entry.isDeleted()) {
				label.setText("");
				label.setIcon(m_deletedIcon);
				label.setToolTipText("Item Removed");
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
