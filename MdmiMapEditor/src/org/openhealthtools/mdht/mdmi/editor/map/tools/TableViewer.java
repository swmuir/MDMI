package org.openhealthtools.mdht.mdmi.editor.map.tools;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.common.components.CursorManager;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.CollectionChangeEvent;
import org.openhealthtools.mdht.mdmi.editor.map.CollectionChangeListener;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.UserPreferences;
import org.openhealthtools.mdht.mdmi.editor.map.editor.AbstractComponentEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.AdvancedSelectionField;
import org.openhealthtools.mdht.mdmi.editor.map.tree.BusinessElementReferenceNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SemanticElementNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.TreeNodeIcon;
import org.openhealthtools.mdht.mdmi.model.Bag;
import org.openhealthtools.mdht.mdmi.model.Choice;
import org.openhealthtools.mdht.mdmi.model.LeafSyntaxTranslator;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.MessageModel;
import org.openhealthtools.mdht.mdmi.model.MessageSyntaxModel;
import org.openhealthtools.mdht.mdmi.model.Node;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.ToBusinessElement;
import org.openhealthtools.mdht.mdmi.model.ToMessageElement;

public class TableViewer extends PrintableView {

	/** Window Width key */
	public static final String TABLE_VIEWER_WIDTH         = "tableViewerWidth";
	/** Window Height key */
	public static final String  TABLE_VIEWER_HEIGHT        = "tableViewerHeight";
	/** Window state (Minimized/Maximized) */
	public static final String  TABLE_VIEWER_MAXIMIZED     = "tableViewerMaximized";
	
	// Size information
	private UserPreferences 	 m_preferences;
	
	// extra buttons
	private JButton m_refreshButton;
	private JButton m_saveButton;
	private ActionListener m_buttonAction;

	// listeners
	private WindowSizeListener    m_windowSizeListener    = new WindowSizeListener();
	private WindowStateListener   m_windowStateListener   = new WindowStateListener();
	private MouseClickListener	  m_mouseListener = new MouseClickListener();
	
	// Table Parts
	private TableViewModel m_tableModel;
	private JTable	   m_table;

	// table columns
	public static final int LEAF_NODE_COL		 = 0;
	public static final int NODE_SE_LINK_COL	 = 1;
	public static final int SEMANTIC_ELEMENT_COL = 2;
	public static final int SE_BER_LINK_COL		 = 3;
	public static final int BUSINESS_ELEMENT_COL = 4;
	
	private static String[] s_columnNames = {
		"Leaf Node", "   ", "Semantic Element", "   ", "Business Element"
	};
	
	// Table Editor (acts as CollectionChangeListener)
	SemanticElementCellEditor m_seCellRenderer = new SemanticElementCellEditor();
	BusinessElementCellRenderer m_beCellRenderer = new BusinessElementCellRenderer();
	
	public TableViewer() {
		// set up frame parameters
		setDefaultCloseOperation(HIDE_ON_CLOSE);

		String appName = SystemContext.getApplicationName();
		m_preferences = UserPreferences.getInstance(appName, null);

		// Set the size based on user preferences
		setWindowSize();

		m_tableModel = new TableViewModel();
		m_table = new JTable(m_tableModel);
		m_table.setColumnSelectionAllowed(true);	// allow single item selection

		
		int rowHeight = m_table.getRowHeight();
		m_table.setRowHeight(18);	// a bit bigger since we'll use 16x16 icons
		Dimension headerSize = m_table.getTableHeader().getPreferredSize();
		headerSize.height += m_table.getRowHeight() - rowHeight;
		m_table.getTableHeader().setPreferredSize(headerSize);

		
		// add custom renderer
		int width = getWidth()/3;
		RowDataRenderer customRenderer = new RowDataRenderer();
		for (int c=0; c<m_table.getColumnCount(); c++) {
			TableColumn column = m_table.getColumnModel().getColumn(c);
			column.setCellRenderer(customRenderer);
			// columns 0, 2 and 4 get most of the width
			if (c == LEAF_NODE_COL) {
				column.setPreferredWidth(width);
				//SemanticElementCellEditor editor = new SemanticElementCellEditor();
				//column.setCellEditor(editor);
			} else if (c == SEMANTIC_ELEMENT_COL) {
				column.setPreferredWidth(width);
				column.setCellEditor(m_seCellRenderer);
			} else if (c == BUSINESS_ELEMENT_COL) {
				column.setPreferredWidth(width);
				column.setCellEditor(m_beCellRenderer);
			} else {
				column.setPreferredWidth(25);
			}
		}
		
		// add listeners for changes to the SEs and BEs
		SelectionManager.getInstance().addCollectionChangeListener(m_seCellRenderer);
		SelectionManager.getInstance().addCollectionChangeListener(m_beCellRenderer);
		
		setCenterComponent(new JScrollPane(m_table));
		m_table.addMouseListener(m_mouseListener);

		// add listeners for resizing and minimizing
		addComponentListener(m_windowSizeListener);
		addWindowStateListener(m_windowStateListener);
		
		// display
		pack();
		if (m_preferences.getBooleanValue(TABLE_VIEWER_MAXIMIZED, false)) {
			setExtendedState(JFrame.MAXIMIZED_BOTH);
		} else {
			BaseDialog.centerOnScreen(this);
		}
		
		setWindowTitle();

		setVisible(true);
		toFront();
	}
	
	public void setWindowTitle() {
		String title = s_res.getString("TableViewer.title");
		if (m_tableModel != null && m_tableModel.isDirty()) {
			title = "*" + title;
		}
		setTitle(title);
	}
	
	/** Set the window size based on user's preferences */
	private void setWindowSize() {
		Dimension windowSize = new Dimension(m_preferences.getIntValue(TABLE_VIEWER_WIDTH, 900),
				m_preferences.getIntValue(TABLE_VIEWER_HEIGHT, 700));
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		// make sure application is within screen bounds
		windowSize.width = Math.min(windowSize.width, screenSize.width);
		windowSize.height = Math.min(windowSize.height, screenSize.height);
		setPreferredSize(windowSize);
		setSize(windowSize);
		
	}
	

	@Override
	protected JPanel createButtonPanel() {
		JPanel buttonPanel = super.createButtonPanel();

		m_refreshButton = new JButton(s_res.getString("TableViewer.refresh"));
		m_saveButton = new JButton(s_res.getString("TableViewer.save"));
		m_buttonAction = new ButtonAction();
		
		m_refreshButton.setIcon(AbstractComponentEditor.getIcon(this.getClass(),
				s_res.getString("TableViewer.refreshIcon")));
		m_refreshButton.setToolTipText(s_res.getString("TableViewer.refreshToolTip"));
		m_refreshButton.addActionListener(m_buttonAction);

		m_saveButton.setIcon(AbstractComponentEditor.getIcon(this.getClass(),
				s_res.getString("TableViewer.saveIcon")));
		m_saveButton.setToolTipText(s_res.getString("TableViewer.saveToolTip"));
		m_saveButton.addActionListener(m_buttonAction);

		buttonPanel.add(m_refreshButton);
		buttonPanel.add(m_saveButton);
		return buttonPanel;
	}

	/** refresh table */
	public void refreshTable() {
		if (m_tableModel.isDirty()) {
			// warn if dirty
			// The data in the table has been modified. Are you sure you want to refresh
			// the table and lose your changes?
			//    [Yes] [No]
			String message = s_res.getString("TableViewer.refreshWarningMesage");
			String title = s_res.getString("TableViewer.refreshWarningTitle");
			int confirm = JOptionPane.showConfirmDialog(this, message, title,
					JOptionPane.YES_NO_OPTION);
			if (confirm != JOptionPane.YES_OPTION) {
				return;
			}
		}
		m_tableModel.loadTable();
		setWindowTitle();
	}
	
	/** save data */
	public void saveData() {
		// TODO - update model
		// TODO - make sure to notify listeners too
		SelectionManager.getInstance().setUpdatesPending();
		//SelectionManager.getInstance().notifyCollectionChangeListeners(object.getClass())
		//SelectionManager.getInstance().notifyModelChangeListeners(object);
	}


	/** clean up resources */
	@Override
	public void dispose() {
		for (int c=0; c<m_table.getColumnCount(); c++) {
			m_table.getColumnModel().getColumn(c).setCellRenderer(null);
		}
		
		SelectionManager.getInstance().removeCollectionChangeListener(SemanticElement.class, m_seCellRenderer);
		SelectionManager.getInstance().removeCollectionChangeListener(MdmiBusinessElementReference.class, m_beCellRenderer);

		m_table.removeMouseListener(m_mouseListener);

		// remove listeners
		m_refreshButton.setToolTipText(null);
		m_refreshButton.removeActionListener(m_buttonAction);
		removeComponentListener(m_windowSizeListener);
		removeWindowStateListener(m_windowStateListener);
		
		super.dispose();
	}

	@Override
	protected Component getPrintComponent() {
		return m_table;
	}

	/** Process a double-click by opening the selected node */
	public void handleDoubleClick() {
		openSelection();
	}


	/** Open (edit) the selected item */
	public void openSelection() {
		Object obj = getSelection();
		if (obj != null) {
			openSelection(obj);
		}
	}

	/** Allow table editing */
	public void allowEditing() {
		m_tableModel.m_editable = true;
		
		int row = m_table.getSelectedRow();
		int col = m_table.getSelectedColumn();
		m_table.editCellAt(row, col);
	}

	/** get the selected object */
	private Object getSelection() {
		int row = m_table.getSelectedRow();
		int col = m_table.getSelectedColumn();
		Object obj = m_table.getModel().getValueAt(row, col);
		return obj;
	}

	public JPopupMenu createPopupMenu() {
		JPopupMenu popupMenu = null;
		Object selection = getSelection();
		if (selection instanceof Node || selection instanceof SemanticElement ||
				selection instanceof MdmiBusinessElementReference) {
			popupMenu = new JPopupMenu();
			// Open Object
			popupMenu.add(new OpenSelectionAction(selection));
			// Edit Cell
			popupMenu.add(new AllowEditAction(selection));
		}
		
		return popupMenu;
	}
	
	/** listener for minimize/maximize - save state */
	private class WindowStateListener extends WindowAdapter {
		@Override
		public void windowStateChanged(WindowEvent e) {
			// save state
			m_preferences.putBooleanValue(TABLE_VIEWER_MAXIMIZED, (e.getNewState()&JFrame.MAXIMIZED_BOTH) != 0 );
		}
	}

	/** listener for size changes - save new size */
	private class WindowSizeListener extends ComponentAdapter {
		@Override
		public void componentResized(ComponentEvent e) {
			// save size
			if (m_preferences != null) {
				m_preferences.putIntValue(TABLE_VIEWER_WIDTH, getWidth());
				m_preferences.putIntValue(TABLE_VIEWER_HEIGHT, getHeight());
			}
		}
	}

	/** Link between leaf node and SE */
	private class LeafNodeSELink {
	};
	
	/** Link between SE and BER */
	private static class SEtoBELink {
		enum Direction { ToMdmi, FromMdmi, Both };
		Direction direction = Direction.Both;
	};
	
	/** Data in a row of the table */
	private class RowData {
		public Node leafNode = null;
		public LeafNodeSELink nodeSELink = null;
		public SemanticElement semanticElement = null;
		public SEtoBELink seToBELink = null;
		public MdmiBusinessElementReference businessElement = null;
		
		public RowData makeCopy() {
			RowData rowData = new RowData();
			rowData.leafNode = leafNode;
			rowData.nodeSELink = nodeSELink;
			rowData.semanticElement = semanticElement;
			rowData.seToBELink = seToBELink;
			rowData.businessElement = businessElement;
			
			return rowData;
		}
	}

	
	/** Table Model 
	 *  | Leaf Node |connection|Semantic Element|connection|Business Element|
	 */
	private class TableViewModel implements TableModel {
		
		public boolean m_editable = false;
		
		private boolean m_dirty = false;
		private ArrayList<RowData> m_rowData = new ArrayList<RowData>();
		
		public TableViewModel() {
			// load table from data
			loadTable();
		}
		
		public boolean isDirty() {
			return m_dirty;
		}
		
		public void loadTable() {
			m_rowData.clear();
			for (MessageGroup group : SelectionManager.getInstance().getEntitySelector().getMessageGroups()) {
				for (MessageModel model : group.getModels()) {
					MessageSyntaxModel syntax = model.getSyntaxModel();
					Node root = syntax.getRoot();
					loadNode(root);	// walk tree
				}
			}
			m_dirty = false;
			setWindowTitle();
		}
		
		public void loadNode(Node node) {
			if (node instanceof LeafSyntaxTranslator) {
				// add to list
				RowData rowData = new RowData();
				
				// set data fields
				// leafNode is the node
				rowData.leafNode = node;
				// Semantic Element
				if (node.getSemanticElement() != null) {
					// set SE and link
					rowData.semanticElement = node.getSemanticElement();
					rowData.nodeSELink = new LeafNodeSELink();
				}
				// Business Element - (There can be many, so which one)
				if (rowData.semanticElement != null) {
					Map<MdmiBusinessElementReference, SEtoBELink.Direction> map = 
							new HashMap<MdmiBusinessElementReference, SEtoBELink.Direction>();
					for (ToMessageElement toMdmi: rowData.semanticElement.getToMdmi()) {
						// add to map
						if (toMdmi.getBusinessElement() != null) {
							SEtoBELink.Direction direction = SEtoBELink.Direction.ToMdmi;
							map.put(toMdmi.getBusinessElement(), direction);
						}
					}
					for (ToBusinessElement fromMdmi : rowData.semanticElement.getFromMdmi()) {
						// add to map
						if (fromMdmi.getBusinessElement() != null) {
							SEtoBELink.Direction direction = map.get(fromMdmi.getBusinessElement());
							if (direction == null) {
								// new BE
								direction = SEtoBELink.Direction.FromMdmi;
							} else if (direction == SEtoBELink.Direction.ToMdmi) {
								// existing as ToMdmi
								direction = SEtoBELink.Direction.Both;
							}
							map.put(fromMdmi.getBusinessElement(), direction);
						}
						
					}
					// add unique ones
					int count = 0;
					for (MdmiBusinessElementReference be : map.keySet()) {
						count++;
						if (count > 1) {
							// add previous, make a new one
							m_rowData.add(rowData);
							rowData = rowData.makeCopy();
						}
						
						rowData.seToBELink = new SEtoBELink();
						rowData.seToBELink.direction = map.get(be);
						rowData.businessElement = be;
					}
				}
				
				
				m_rowData.add(rowData);
				
			} else if (node instanceof Bag) {
				// check children
				for (Node child : ((Bag)node).getNodes()) {
					loadNode(child);
				}
				
			} else if (node instanceof Choice) {
				// check children
				for (Node child : ((Choice)node).getNodes()) {
					loadNode(child);
				}
				
			}
		}

		@Override
		public int getRowCount() {
			// always have blank row at the end
			return m_rowData.size() + 1;
		}

		@Override
		public int getColumnCount() {
			return s_columnNames.length;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return s_columnNames[columnIndex];
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			boolean editable = m_editable;
			if (rowIndex < m_rowData.size() && editable) {
				// only SE and BE can be edited (for now)
				if (columnIndex == SEMANTIC_ELEMENT_COL || columnIndex == BUSINESS_ELEMENT_COL) {
					editable = true;
				}
			}
			return editable;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object value = null;
			if (rowIndex < m_rowData.size()) {
				RowData rowData = m_rowData.get(rowIndex);
				if (columnIndex == LEAF_NODE_COL) {
					// Leaf Node
					value = rowData.leafNode;
				} else if (columnIndex == NODE_SE_LINK_COL) {
					// node-SE link
					value = rowData.nodeSELink;
				} else if (columnIndex == SEMANTIC_ELEMENT_COL) {
					// Semantic Element
					value = rowData.semanticElement;
				} else if (columnIndex == SE_BER_LINK_COL) {
					// SE - BE link
					value = rowData.seToBELink;
				} else if (columnIndex == BUSINESS_ELEMENT_COL) {
					// Business Element
					value = rowData.businessElement;
				}
				m_editable = false;	// end editing
			}
			
			if (value == null) {
				value = "";
			}
			return value;
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			boolean dirty = false;
			
			if (rowIndex < m_rowData.size()) {
				RowData rowData = m_rowData.get(rowIndex);
				if (columnIndex == LEAF_NODE_COL) {
					// Leaf Node
					if (rowData.leafNode != value) {
						dirty = true;
					}
					rowData.leafNode = (Node)value;
				} else if (columnIndex == NODE_SE_LINK_COL) {
					// node-SE link
					if (rowData.nodeSELink != value) {
						dirty = true;
					}
					rowData.nodeSELink = (LeafNodeSELink)value;
				} else if (columnIndex == SEMANTIC_ELEMENT_COL) {
					// Semantic Element
					if (rowData.semanticElement != value) {
						dirty = true;
					}
					rowData.semanticElement = (SemanticElement)value;
				} else if (columnIndex == SE_BER_LINK_COL) {
					// SE - BE link
					if (rowData.seToBELink != value) {
						dirty = true;
					}
					 rowData.seToBELink = (SEtoBELink)value;
				} else if (columnIndex == BUSINESS_ELEMENT_COL) {
					// Business Element
					if (rowData.businessElement != value) {
						dirty = true;
					}
					rowData.businessElement = (MdmiBusinessElementReference)value;
				}
			} else {
				// TODO - create data
			}
		
			if (dirty != m_dirty) {
				m_dirty = dirty;
				setWindowTitle();
			}
		}

		@Override
		public void addTableModelListener(TableModelListener l) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeTableModelListener(TableModelListener l) {
			// TODO Auto-generated method stub
			
		}

		
		
	};
	 

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

	///////////////////////////////////////
	// Table Cell Editors
	///////////////////////////////////////
	public abstract class AbstractCustomCellEditor extends AbstractCellEditor implements TableCellEditor,
				CollectionChangeListener {
	    // This is the component that will handle the editing of the cell value
		JComboBox m_comboBox = new JComboBox();
	    
	    public AbstractCustomCellEditor() {
	    	// load with all semantic elements
	    	fillComboBox(m_comboBox);
	    	m_comboBox.setRenderer(new ComboBoxRenderer());
	    }

	    protected abstract void fillComboBox(JComboBox comboBox);


		// This method is called when a cell value is edited by the user.
	    @Override
	    public Component getTableCellEditorComponent(JTable table, Object value,
	            boolean isSelected, int rowIndex, int colIndex) {
	        // 'value' is value contained in the cell located at (rowIndex, colIndex)

	        if (isSelected) {
	            // cell (and perhaps other cells) are selected
	        }

	        // Configure the component with the specified value
	        if (value == null) {
				value = AdvancedSelectionField.BLANK_ENTRY;
			}
			m_comboBox.setSelectedItem(value);

	        // Return the configured component
	        return m_comboBox;
	    }

	    // This method is called when editing is completed.
	    // It must return the new value to be stored in the cell.
	    @Override
	    public Object getCellEditorValue() {
	        return m_comboBox.getSelectedItem();
	    }

		@Override
		public void contentsChanged(CollectionChangeEvent e) {
			// Collection Change Listener method - refresh list
			fillComboBox(m_comboBox);
		}

	}

	public class SemanticElementCellEditor extends AbstractCustomCellEditor {
		@Override
		protected void fillComboBox(JComboBox comboBox) {
			ArrayList<SemanticElement> elements = new ArrayList<SemanticElement>();
			List<DefaultMutableTreeNode> semanticElementNodes = 
				SelectionManager.getInstance().getEntitySelector().getNodesOfType(SemanticElementNode.class);
			
			for (DefaultMutableTreeNode treeNode : semanticElementNodes) {
				SemanticElement element = (SemanticElement)treeNode.getUserObject();
				if (element.getName() != null && element.getName().length() > 0) {
					elements.add(element);
				}
			}

			Collections.sort(elements, new Comparators.SemanticElementComparator());

			// first item is blank
			comboBox.removeAll();
			comboBox.addItem(AdvancedSelectionField.BLANK_ENTRY);
			for (SemanticElement element : elements) {
				comboBox.addItem(element);
			}
		}

		@Override
		public Class<?> getListenForClass() {
			return SemanticElement.class;
		}
	}
	
	public class BusinessElementCellRenderer extends AbstractCustomCellEditor {

		@Override
		protected void fillComboBox(JComboBox comboBox) {
			
			ArrayList<MdmiBusinessElementReference> elements = new ArrayList<MdmiBusinessElementReference>();
			List<DefaultMutableTreeNode> businessElementNodes = 
				SelectionManager.getInstance().getEntitySelector().getNodesOfType(BusinessElementReferenceNode.class);
			
			for (DefaultMutableTreeNode treeNode : businessElementNodes) {
				MdmiBusinessElementReference element = (MdmiBusinessElementReference)treeNode.getUserObject();
				if (element.getName() != null && element.getName().length() > 0) {
					elements.add(element);
				}
			}

			Collections.sort(elements, new Comparators.BusinessElementReferenceComparator());

			// first item is blank
			comboBox.removeAll();
			comboBox.addItem(AdvancedSelectionField.BLANK_ENTRY);
			for (MdmiBusinessElementReference element : elements) {
				comboBox.addItem(element);
			}
			
		}


		@Override
		public Class<?> getListenForClass() {
			return MdmiBusinessElementReference.class;
		}
		
	}
	
	////////////////////////////////////////
	// Combo Box Renderer
	///////////////////////////////////////

	/** Renderer for the ComboBox - use getName() method. */
	protected class ComboBoxRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			
			if (value instanceof Node) {
				Node node = (Node)value;
				value = node.getName();
			} else if (value instanceof SemanticElement) {
				SemanticElement se = (SemanticElement)value;
				value = se.getName();
			} else if (value instanceof MdmiBusinessElementReference) {
				MdmiBusinessElementReference ber = (MdmiBusinessElementReference)value;
				value = ber.getName();
			}

			return super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
		}
		
	}
	
	///////////////////////////////////////
	// Table Cell Renderer
	///////////////////////////////////////
	private class RowDataRenderer extends DefaultTableCellRenderer {
		int m_defaultFontSize = -1;
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {

			Font font = getFont();
			if (m_defaultFontSize < 0) {
				m_defaultFontSize = font.getSize();
			}
			
			int align = LEFT;
			int style = Font.PLAIN;
			int size = m_defaultFontSize;
			Icon icon = null;
			String toolTip = null;
			
			if (value instanceof Node) {
				Node node = (Node)value;
				value = node.getName();
				icon = TreeNodeIcon.getTreeIcon(node.getClass());
				toolTip = ClassUtil.createToolTip(node);
				
			} else if (value instanceof LeafNodeSELink) {
				value = SemanticElementNode.DOUBLE_ARROW;	
				align = CENTER;
				size = m_defaultFontSize + 4;
				
			} else if (value instanceof SemanticElement) {
				SemanticElement se = (SemanticElement)value;
				value = se.getName();
				icon = TreeNodeIcon.getTreeIcon(se.getClass());
				toolTip = ClassUtil.createToolTip(se);
				
			} else if (value instanceof SEtoBELink) {
				SEtoBELink se = (SEtoBELink)value;
				// <-- / --> / <-->
				switch (se.direction) {
				case Both:
					value = SemanticElementNode.DOUBLE_ARROW;
					toolTip = "SE To/From BER";
					break;
				case ToMdmi:
					value = SemanticElementNode.LEFT_ARROW;
					toolTip = "BER to SE";
					break;
				case FromMdmi:
					value = SemanticElementNode.RIGHT_ARROW;
					toolTip = "SE to BER";
					break;
				}
				align = CENTER;
				size = m_defaultFontSize + 4;
				
			} else if (value instanceof MdmiBusinessElementReference) {
				MdmiBusinessElementReference ber = (MdmiBusinessElementReference)value;
				value = ber.getName();
				icon = TreeNodeIcon.getTreeIcon(ber.getClass());
				toolTip = ClassUtil.createToolTip(ber);
			}
			
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);
			
			setIcon(icon);
			setToolTipText(toolTip);
			setHorizontalAlignment(align);
			font = font.deriveFont(style, (float)size);
			setFont(font);
			
			return c;
		}
	}
	
	
	///////////////////////////////////////
	// Menu Action Handlers
	///////////////////////////////////////

	public class OpenSelectionAction extends AbstractAction {
		public OpenSelectionAction(Object object) {
			super(MessageFormat.format(s_res.getString("ViewDataObject.openFormat"), 
					ClassUtil.beautifyName(object.getClass())));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			openSelection();
		}
		
	}
	public class AllowEditAction extends AbstractAction {
		public AllowEditAction(Object object) {
			super("Edit Cell");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			allowEditing();
		}
		
	}
	
	/////////////////////////////////////////
	//  Button Action Listener 
	////////////////////////////////////////
	private class ButtonAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			CursorManager cm = CursorManager.getInstance(TableViewer.this);
			cm.setWaitCursor();

			try {
				if (e.getSource() == m_refreshButton) {
					refreshTable();
				} else if (e.getSource() == m_saveButton) {
					saveData();
				}

			} catch (Exception ex) {
				SelectionManager.getInstance().getStatusPanel().writeException(ex);
			} finally {
				cm.restoreCursor();
			}	
		}
	}
}
