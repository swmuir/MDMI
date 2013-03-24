package org.openhealthtools.mdht.mdmi.editor.map.tools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.ActionMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.actions.AbstractAction;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.common.components.CursorManager;
import org.openhealthtools.mdht.mdmi.editor.map.Actions;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.CollectionChangeEvent;
import org.openhealthtools.mdht.mdmi.editor.map.CollectionChangeListener;
import org.openhealthtools.mdht.mdmi.editor.map.ModelChangeEvent;
import org.openhealthtools.mdht.mdmi.editor.map.ModelChangeListener;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.UserPreferences;
import org.openhealthtools.mdht.mdmi.editor.map.editor.AbstractComponentEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.AdvancedSelectionField;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ModelIOUtilities.DataDictionaryTransferHandler;
import org.openhealthtools.mdht.mdmi.editor.map.tree.BusinessElementReferenceNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MdmiModelTree;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SemanticElementNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SyntaxNodeNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.TreeNodeIcon;
import org.openhealthtools.mdht.mdmi.model.Bag;
import org.openhealthtools.mdht.mdmi.model.Choice;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.LeafSyntaxTranslator;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.MessageModel;
import org.openhealthtools.mdht.mdmi.model.MessageSyntaxModel;
import org.openhealthtools.mdht.mdmi.model.Node;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.SemanticElementSet;
import org.openhealthtools.mdht.mdmi.model.ToBusinessElement;
import org.openhealthtools.mdht.mdmi.model.ToMessageElement;
import org.openhealthtools.mdht.mdmi.model.enums.SemanticElementType;

public class TableViewer extends PrintableView  {
	/** Window Width key */
	public static final String TABLE_VIEWER_WIDTH         = "tableViewerWidth";
	/** Window Height key */
	public static final String  TABLE_VIEWER_HEIGHT        = "tableViewerHeight";
	/** Window state (Minimized/Maximized) */
	public static final String  TABLE_VIEWER_MAXIMIZED     = "tableViewerMaximized";
	

	// Filtering Criteria
	private static final String ALL_SEMANTIC_ELEMENTS       = s_res.getString("TableViewer.AllSemanticElements");
	private static final String NORMAL_SEMANTIC_ELEMENTS    = s_res.getString("TableViewer.NormalSemanticElements");
	private static final String LOCAL_SEMANTIC_ELEMENTS     = s_res.getString("TableViewer.LocalSemanticElements");
	private static final String MAPPED_SEMANTIC_ELEMENTS    = s_res.getString("TableViewer.MappedSemanticElements");
	private static final String UN_MAPPED_SEMANTIC_ELEMENTS = s_res.getString("TableViewer.UnMappedSemanticElements");
	
	
	// Size information
	private UserPreferences 	 m_preferences;
	
	// Filters
	private JComboBox m_filters;
	private JCheckBox m_showLeafNodes;
	
	// extra buttons
	private JButton m_refreshButton;
	private JButton m_mainWindowButton;
	private ActionListener m_buttonAction;

	// listeners
	private WindowSizeListener    m_windowSizeListener    = new WindowSizeListener();
	private WindowStateListener   m_windowStateListener   = new WindowStateListener();
	private MouseClickListener	  m_mouseListener = new MouseClickListener();
	
	// keyboard actions
	private AbstractAction m_copyAction = null;
	private AbstractAction m_pasteAction = null;
	
	// Table Parts
	private TableViewModel m_tableModel;
	private JTable	   m_table;

	// table columns
	private int m_rowNumbCol	  = -1;
	private int m_leafNodeCol	  = -1;
	private int m_nodeSeLinkCol	  = -1;
	private int m_semanticElemCol = -1;
	private int m_seBeLinkCol	  = -1;
	private int m_BusinessElemCol = -1;
	
	// column names - must match column indices
	private ArrayList<String> m_columnNames = new ArrayList<String>();
	
	// Table Cell Editors (acts as CollectionChangeListener)
	CustomCellEditor m_nameCellEditor = new CustomCellEditor();
	BusinessElementCellEditor m_beCellEditor = new BusinessElementCellEditor();
	
	// Listener for changes to the model
	ChangeListener m_changeListener = new ChangeListener();
	
	public TableViewer() {
		// set up frame parameters
		setDefaultCloseOperation(HIDE_ON_CLOSE);

		String appName = SystemContext.getApplicationName();
		m_preferences = UserPreferences.getInstance(appName, null);

		// Set the size based on user preferences
		setWindowSize();
		configureTableColumns();
		
		m_tableModel = new TableViewModel();
		m_table = new JTable(m_tableModel);
		m_table.setColumnSelectionAllowed(true);	// allow single item selection

		// allow dropping from another application
		m_table.setDragEnabled(true);
		m_table.setTransferHandler(new DataDictionaryTransferHandler());
		
		int rowHeight = m_table.getRowHeight();
		m_table.setRowHeight(18);	// a bit bigger since we'll use 16x16 icons
		Dimension headerSize = m_table.getTableHeader().getPreferredSize();
		headerSize.height += m_table.getRowHeight() - rowHeight;
		m_table.getTableHeader().setPreferredSize(headerSize);

		// configure the table columns
		configureTableColumns();
		
		// add listeners for changes to the SEs and BEs
		SelectionManager.getInstance().addCollectionChangeListener(m_beCellEditor);
		SelectionManager.getInstance().addModelChangeListener(m_changeListener);
		SelectionManager.getInstance().addCollectionChangeListener(m_changeListener);
		
		setCenterComponent(new JScrollPane(m_table));
		m_table.addMouseListener(m_mouseListener);

		// add listeners for resizing and minimizing
		addComponentListener(m_windowSizeListener);
		addWindowStateListener(m_windowStateListener);
		
		// add mappings for copy/paste
		addKeyboardMap();
		
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

	private void configureTableColumns() {

		//  table columns
		m_columnNames.clear();
		int colIdx = 0;

		m_columnNames.add("");
		m_rowNumbCol	  = colIdx++;
		
		if (isShowLeafNodes()) {
			m_columnNames.add(s_res.getString("TableViewer.leafNode"));
			m_leafNodeCol	  = colIdx++;

			m_columnNames.add("");
			m_nodeSeLinkCol	  = colIdx++;
		} else {
			m_leafNodeCol = -1;
			m_nodeSeLinkCol = -1;
		}

		m_columnNames.add(s_res.getString("TableViewer.semanticElement"));
		m_semanticElemCol = colIdx++;

		m_columnNames.add("");
		m_seBeLinkCol	  = colIdx++;

		m_columnNames.add(s_res.getString("TableViewer.businessElement"));
		m_BusinessElemCol = colIdx++;
		
		if (m_tableModel == null)
			return;
		
		m_tableModel.fireTableStructureChanged();
		
		// add custom renderer for link columns
		Icon icon = AbstractComponentEditor.getIcon(this.getClass(),
				s_res.getString("TableViewer.linkIcon"));
		IconRenderer iconRenderer = new IconRenderer(icon);
		for (int c=0; c<m_table.getColumnCount(); c++) {
			TableColumn column = m_table.getTableHeader().getColumnModel().getColumn(c);
			if (c == m_nodeSeLinkCol || c == m_seBeLinkCol) {
				column.setHeaderRenderer(iconRenderer);
			}
		}
		
		// set width and renderer for data
		int width = getWidth()/3;
		RowDataRenderer customRenderer = new RowDataRenderer();
		for (int c=0; c<m_table.getColumnCount(); c++) {
			TableColumn column = m_table.getColumnModel().getColumn(c);
			column.setCellRenderer(customRenderer);
			// columns 1, 3 and 5 get most of the width
			if (c == m_leafNodeCol) {
				column.setPreferredWidth(width/2);
				column.setCellEditor(m_nameCellEditor);
			} else if (c == m_semanticElemCol) {
				column.setPreferredWidth(width);
				column.setCellEditor(m_nameCellEditor);
			} else if (c == m_BusinessElemCol) {
				column.setPreferredWidth(width);
				column.setCellEditor(m_beCellEditor);
			} else {
				// fixed-width column
				int w = 32;
				column.setMinWidth(w);
				column.setMaxWidth(w);
				column.setPreferredWidth(w);
				column.setResizable(false);
			}
		}
		
		m_tableModel.loadTable();
	}

	private void addKeyboardMap() {
		// Add bindings for Copy (Ctrl C), Paste,
		InputMap inputMap = m_table.getInputMap(JComponent.WHEN_FOCUSED);
		ActionMap actionMap = m_table.getActionMap();
		
		m_copyAction = new TableViewAction(Actions.COPY_ACTION) {
			@Override
			public void execute(ActionEvent actionEvent) {
				copyLeafNodes();
			}
		};
		m_pasteAction = new TableViewAction(Actions.PASTE_ACTION) {
			@Override
			public void execute(ActionEvent actionEvent) {
				pasteLeafNodesIntoSemanticElement();
			}
		};

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK),
				Actions.COPY_ACTION);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK),
				Actions.PASTE_ACTION);
		
		actionMap.put(Actions.COPY_ACTION, m_copyAction);
		actionMap.put(Actions.PASTE_ACTION, m_pasteAction);
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
		JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel rightPanel = super.createButtonPanel();

		m_refreshButton = new JButton(s_res.getString("TableViewer.refresh"));
		m_mainWindowButton = new JButton(s_res.getString("TableViewer.mainWindow"));
//		m_saveButton = new JButton(s_res.getString("TableViewer.save"));
		m_buttonAction = new ButtonAction();
		
		m_refreshButton.setIcon(AbstractComponentEditor.getIcon(this.getClass(),
				s_res.getString("TableViewer.refreshIcon")));
		m_refreshButton.setToolTipText(s_res.getString("TableViewer.refreshToolTip"));
		m_refreshButton.addActionListener(m_buttonAction);
		
		m_mainWindowButton.setToolTipText(s_res.getString("TableViewer.mainWindowToolTip"));
		m_mainWindowButton.addActionListener(m_buttonAction);
		m_mainWindowButton.setIcon(AbstractComponentEditor.getIcon(this.getClass(),
				s_res.getString("TableViewer.mainWindowIcon")));
		
		rightPanel.add(m_refreshButton);
		rightPanel.add(m_mainWindowButton);
		
		// Filters
		m_showLeafNodes = new JCheckBox(s_res.getString("TableViewer.showLeafNodes"));
		m_showLeafNodes.setSelected(true);
		leftPanel.add(m_showLeafNodes);
		
		m_showLeafNodes.addActionListener(m_buttonAction);
		
		centerPanel.add(new JLabel("Show:"));
		
		m_filters = new JComboBox();
		m_filters.addItem(ALL_SEMANTIC_ELEMENTS);
		m_filters.addItem(NORMAL_SEMANTIC_ELEMENTS);
		m_filters.addItem(LOCAL_SEMANTIC_ELEMENTS);
		m_filters.addItem(MAPPED_SEMANTIC_ELEMENTS);
		m_filters.addItem(UN_MAPPED_SEMANTIC_ELEMENTS);
		
		m_filters.addActionListener(m_buttonAction);
		
		centerPanel.add(m_filters);
		
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.NONE;

		gbc.anchor = GridBagConstraints.WEST;
		buttonPanel.add(leftPanel, gbc);

		gbc.anchor = GridBagConstraints.CENTER;
		buttonPanel.add(centerPanel, gbc);

		gbc.anchor = GridBagConstraints.EAST;
		buttonPanel.add(rightPanel, gbc);
		
		return buttonPanel;
	}

	/** refresh table */
	public void refreshTable() {
		configureTableColumns();
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
	
	public boolean isShowLeafNodes() {
		return m_showLeafNodes.isSelected();
	}
	
	/** check whether we should show this row */
	private boolean showRow(RowData rowData) {
		boolean showRow = true;
		
		if (m_filters == null || rowData == null) {
			return showRow;
		}

		SemanticElement semanticElement = rowData.semanticElement;
		SEtoBELink seToBELink = rowData.seToBELink;
		
		Object filterCriteria = m_filters.getSelectedItem();

		if (ALL_SEMANTIC_ELEMENTS.equals(filterCriteria)) {
			showRow = true;
			
		} else if (NORMAL_SEMANTIC_ELEMENTS.equals(filterCriteria)) {
			// Normal SEs 
			showRow = (semanticElement != null) && 
					SemanticElementType.NORMAL.equals(semanticElement.getSemanticElementType());

		} else if (LOCAL_SEMANTIC_ELEMENTS.equals(filterCriteria)) {
			// Local SEs
			showRow = (semanticElement != null) && 
					SemanticElementType.LOCAL.equals(semanticElement.getSemanticElementType());

		} else if (MAPPED_SEMANTIC_ELEMENTS.equals(filterCriteria)) {
			// Normal + Mapped
			showRow = (semanticElement != null) && 
					SemanticElementType.NORMAL.equals(semanticElement.getSemanticElementType()) && 
					(seToBELink != null) && 
					(seToBELink.direction != SEtoBELink.Direction.None);
			
		} else if (UN_MAPPED_SEMANTIC_ELEMENTS.equals(filterCriteria)) {
			// Normal + Un-Mapped
			showRow = (semanticElement != null) && 
					SemanticElementType.NORMAL.equals(semanticElement.getSemanticElementType()) && 
					((seToBELink == null) || seToBELink.direction == SEtoBELink.Direction.None);
		}

		return showRow;
	}
	
	/** Delete this row */
	public void deleteRow(int rowNum) {
		// delete all To/From elements between SE and BE
		RowData rowData = m_tableModel.getRowData(rowNum);
		if (rowData == null) {
			return;
		}
		
		// if there's only one To/From set, delete the node
		int [] rowsToRemove = getRowRange(rowData.leafNode);
		if (rowsToRemove.length == 1) {
			deleteLeafNode(rowNum);
		} else {
			// delete from & to elements
			if (rowData.seToBELink != null && rowData.seToBELink.direction != SEtoBELink.Direction.None) {
				if (!deleteMappings(rowData.semanticElement, rowData.businessElement)) {
					return;
				}
			}

			m_tableModel.deleteRow(rowNum);

			m_table.repaint();
			// select the next row (if there is one)
			int rowCount = m_tableModel.getRowCount();
			if (rowNum >= rowCount) {
				rowNum--;
			}
			if (rowNum >= 0) {
				m_table.setRowSelectionInterval(rowNum, rowNum);
			}
		}
		
	}
	
	/** Delete the BER to SE and SE-to-BER mappings */
	private boolean deleteMappings(SemanticElement semanticElement, MdmiBusinessElementReference businessElement) {
		// are you sure you want to delete...
		String message;
		ToMessageElement toMdmi = SemanticElementBERLinkDialog.findToMessageElement(semanticElement, businessElement);
		ToBusinessElement fromMdmi = SemanticElementBERLinkDialog.findToBusinessElement(semanticElement, businessElement);
		if (toMdmi != null && fromMdmi != null) {
			// both
			message = MessageFormat.format(s_res.getString("TableViewer.deleteFormat2"),
					ClassUtil.beautifyName(fromMdmi.getClass()), fromMdmi.getName(),
					ClassUtil.beautifyName(toMdmi.getClass()), toMdmi.getName());
		} else if (fromMdmi != null) {
			message = MessageFormat.format(s_res.getString("TableViewer.deleteFormat1"),
					ClassUtil.beautifyName(fromMdmi.getClass()), fromMdmi.getName());
		} else if (toMdmi != null) {
			message = MessageFormat.format(s_res.getString("TableViewer.deleteFormat1"),
					ClassUtil.beautifyName(toMdmi.getClass()), toMdmi.getName());
		} else {
			return true;
		}
		// Warn
		String title = s_res.getString("TableViewer.deleteRow");
		int confirm = JOptionPane.showConfirmDialog(this, message, title,
				JOptionPane.YES_NO_OPTION);
		if (confirm != JOptionPane.YES_OPTION) {
			return false;
		}
		
		// delete from model and tree
		MdmiModelTree entitySelector = SelectionManager.getInstance().getEntitySelector();
		if (toMdmi != null) {
			DefaultMutableTreeNode treeNode = entitySelector.findNode(toMdmi);
			entitySelector.deleteNode(treeNode, false);
		}
		if (fromMdmi != null) {
			DefaultMutableTreeNode treeNode = entitySelector.findNode(fromMdmi);
			entitySelector.deleteNode(treeNode, false);
		}
		return true;
	}

	/** Delete this leaf node */
	public void deleteLeafNode(int rowNum) {
		RowData rowData = m_tableModel.getRowData(rowNum);
		if (rowData == null) {
			return;
		}
		
		// Strategy:
		//  delete Leaf Node
		//  delete SE
		//  if Leaf's parent has no other children, delete parent (recursively)
		//  remove all rows with this leaf
		MdmiModelTree entitySelector = SelectionManager.getInstance().getEntitySelector();
		DefaultMutableTreeNode treeNode = entitySelector.findNode(rowData.leafNode);
		if (treeNode == null) {
			// shouldn't happen
			return;
		}
		DefaultMutableTreeNode parentTreeNode = (DefaultMutableTreeNode)treeNode.getParent();

		Node node = (Node)treeNode.getUserObject();
		SemanticElement se = node.getSemanticElement();
		node.setSemanticElement(null);	// zero out so there are no complaints trying to delete it
		if (se != null) {
			se.setSyntaxNode(null);
		}
		
		// delete Leaf Node
		if (!entitySelector.deleteNode(treeNode, true)) {	// prompt
			// user says "no"
			node.setSemanticElement(se);	// restore
			if (se != null) {
				se.setSyntaxNode(null);
			}
			return;
		}
		// delete SE
		if (se != null) {
			DefaultMutableTreeNode seTreeNode = entitySelector.findNode(se);
			entitySelector.deleteNode(seTreeNode, false);	// don't prompt any more
		}
		
		// work up the tree, removing child-less nodes
		while (parentTreeNode != null && parentTreeNode instanceof SyntaxNodeNode &&
				parentTreeNode.getChildCount() == 0) {
			// child-less syntax node - delete it
			treeNode = parentTreeNode;
			parentTreeNode  = (DefaultMutableTreeNode)treeNode.getParent();
			
			node = (Node)treeNode.getUserObject();
			se = node.getSemanticElement();
			node.setSemanticElement(null);
			se.setSyntaxNode(null);
			
			// delete node
			entitySelector.deleteNode(treeNode, false);
			// delete se
			if (se != null) {
				DefaultMutableTreeNode seTreeNode = entitySelector.findNode(se);
				entitySelector.deleteNode(seTreeNode, false);
			}
		}
		
		// find all rows with the same node
		int [] rowsToRemove = getRowRange(rowData.leafNode);
		// remove rows from table (iterate backwards to maintain number)
		for (int r=rowsToRemove.length-1; r>=0; r--)
		{
			rowNum = rowsToRemove[r];
			m_tableModel.deleteRow(rowNum);
		}
		
		m_table.repaint();
		// select the next row (if there is one)
		int rowCount = m_tableModel.getRowCount();
		if (rowNum >= rowCount) {
			rowNum--;
		}
		if (rowNum >= 0) {
			m_table.setRowSelectionInterval(rowNum, rowNum);
		}
	}
	
	/** Get row numbers for rows containing this Leaf Node */
	private int [] getRowRange(Node leafNode) {
		ArrayList<Integer> rows = new ArrayList<Integer>();
		for (int r = 0; r < m_tableModel.getRowCount(); r++) {
			// find other rows with the same node
			RowData rowData = m_tableModel.getRowData(r);
			if (leafNode == rowData.leafNode) {
				rows.add(new Integer(r));
			}	
		}
		
		int [] range = new int [rows.size()];
		int i = 0;
		for (Integer integer : rows) {
			range[i++] = integer.intValue();
		}
		return range;
	}

	/** show dialog to create Node and Semantic Element, and choice of Business ELement */
	public void newLeafNode(int rowNum) {
		RowData rowData = m_tableModel.getRowData(rowNum);
		if (rowData == null) {
			return;
		}
		
		AddRowToTableViewerDialog dlg = new AddRowToTableViewerDialog(this, rowData == null ? null : rowData.leafNode);

		int result = dlg.display(TableViewer.this);
		if (result == BaseDialog.OK_BUTTON_OPTION) {
			Node node = dlg.getSyntaxNode();
			
			// find first row that has a different leaf node
			if (rowData != null) {
				rowNum++;
				for (; rowNum < m_tableModel.getRowCount(); rowNum++) {
					if (m_tableModel.getRowData(rowNum).leafNode != rowData.leafNode) {
						break;
					}
				}
			}
			
			// create new row in table
			m_tableModel.addNode(node, rowNum);
			
			// add BE
			RowData newRowData = m_tableModel.getRowData(rowNum);
			newRowData.businessElement = dlg.getBusinessElement();
			if (newRowData.businessElement != null) {
				newRowData.seToBELink.direction = getDirection(newRowData.semanticElement, newRowData.businessElement);
			}
			
			m_table.repaint();
			
			m_table.setRowSelectionInterval(rowNum, rowNum);
		}
	}
	
	/** create a new SE with the same name as the node - for each row */
	public boolean newSEfromNode(int [] rows) {
		String message = null;
		boolean dataValid = true;
		
		// check target cells
		for (int row = 0; row < rows.length && dataValid; row++) {
			RowData rowData = m_tableModel.getRowData(rows[row]);
			if (rowData.semanticElement != null) {
				// All target Semantic Element cells must be empty
				message = s_res.getString("TableViewer.targetCellsMessage");
				dataValid = false;
			}
		}
		
		if (message != null) {
			JOptionPane.showMessageDialog(this, message, s_res.getString("TableViewer.createSE"), 
					JOptionPane.WARNING_MESSAGE);
			return false;
		}
		
		for (int row = 0; row < rows.length; row++) {
			newSEfromNode(rows[row]);
		}
		
		return true;
	}
	
	/** create a new SE with the same name as the node */
	public boolean newSEfromNode(int rowNum)
	{
		RowData rowData = m_tableModel.getRowData(rowNum);
		if (rowData == null || rowData.leafNode == null || rowData.semanticElement != null) {
			return false;
		}

		MdmiModelTree entitySelector = SelectionManager.getInstance().getEntitySelector();
		DefaultMutableTreeNode treeNode = entitySelector.findNode(rowData.leafNode);
		if (treeNode == null) {
			return false;
		}
		MessageGroup messageGroup = ((EditableObjectNode)treeNode).getMessageGroup();
		
		SemanticElement se = AddRowToTableViewerDialog.createSemanticElement(rowData.leafNode.getName(), rowData.leafNode, messageGroup);
		rowData.semanticElement = se;
		rowData.nodeSELink = new LeafNodeSELink();
		
		m_table.repaint();
		
		m_table.setRowSelectionInterval(rowNum, rowNum);
		
		return true;
	}
	
	/** add a new row with the same Node and SE, but a blank business element */
	public void addNewBE(int rowNum) {
		RowData rowData = m_tableModel.getRowData(rowNum);
		if (rowData == null) {
			return;
		}
		
		// copy
		RowData newRow = rowData.makeCopy();
		// zero-out BE
		if (newRow.seToBELink != null) {
			newRow.seToBELink.direction = SEtoBELink.Direction.None;
		}
		newRow.businessElement = null;
		
		// add it after this one
		if (showRow(rowData)) {
			m_tableModel.m_rowData.add(++rowNum, newRow);
		}
		
		m_table.repaint();
			
		// set focus to add new BE
		m_table.setRowSelectionInterval(rowNum, rowNum);
		m_table.setColumnSelectionInterval(m_BusinessElemCol, m_BusinessElemCol);
		m_tableModel.m_editable = true;
		m_table.editCellAt(rowNum, m_BusinessElemCol);
	}

	
	/** Fill a combo box with all Business Element References */
	public static void fillBERefComboBox(JComboBox comboBox) {
		
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
		comboBox.removeAllItems();
		comboBox.addItem(AdvancedSelectionField.BLANK_ENTRY);
		for (MdmiBusinessElementReference element : elements) {
			comboBox.addItem(element);
		}
		
	}

	/** notify listeners when model changes */
	public void modelChanged(Object model) {
		if (model == null) {
			return;
		}
		SelectionManager selectionMgr = SelectionManager.getInstance();
		selectionMgr.setUpdatesPending();
		selectionMgr.notifyCollectionChangeListeners(model.getClass());
		selectionMgr.notifyModelChangeListeners(model);
		selectionMgr.getEntitySelector().refreshUserObject(model);
	}


	/** clean up resources */
	@Override
	public void dispose() {
		for (int c=0; c<m_table.getColumnCount(); c++) {
			m_table.getColumnModel().getColumn(c).setCellRenderer(null);
			m_table.getTableHeader().getColumnModel().getColumn(c).setCellRenderer(null);
		}
		
		SelectionManager.getInstance().removeCollectionChangeListener(m_beCellEditor);
		SelectionManager.getInstance().removeModelChangeListener(m_changeListener);
		SelectionManager.getInstance().removeCollectionChangeListener(m_changeListener);

		m_table.removeMouseListener(m_mouseListener);

		// remove listeners
		m_refreshButton.setToolTipText(null);
		m_refreshButton.removeActionListener(m_buttonAction);
		m_mainWindowButton.setToolTipText(null);
		m_mainWindowButton.removeActionListener(m_buttonAction);
		m_showLeafNodes.removeActionListener(m_buttonAction);
		m_filters.removeActionListener(m_buttonAction);
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
	
	@Override
	protected void openSelection(Object model) {
		if (model instanceof SEtoBELink) {
			openSeToBerLink();
		} else {
			super.openSelection(model);
			// switch to main view
			SystemContext.getApplicationFrame().toFront();
		}
	}

	/** Open the dialog for the SE-to-BER link */
	private void openSeToBerLink() {
		RowData rowData = getSelectedRowData();
		SemanticElementBERLinkDialog dlg = new SemanticElementBERLinkDialog(TableViewer.this,
				rowData.semanticElement, rowData.businessElement);

		int result = dlg.display(TableViewer.this);
		if (result == BaseDialog.OK_BUTTON_OPTION) {
			// update direction
			rowData.seToBELink.direction = getDirection(rowData.semanticElement, rowData.businessElement);
		}
	}


	/** Allow table editing */
	public void allowEditing() {
		m_tableModel.m_editable = true;
		
		int row = m_table.getSelectedRow();
		int col = m_table.getSelectedColumn();
		m_table.editCellAt(row, col);
	}
	
	/** get selected RowData */
	private RowData getSelectedRowData() {
		int row = m_table.getSelectedRow();
		return m_tableModel.getRowData(row);
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

		int row = m_table.getSelectedRow();
		int col = m_table.getSelectedColumn();
		RowData rowData = getSelectedRowData();

		if (m_table.getSelectedRowCount() > 1) {
			// multiple rows - only allow single column
			if (m_table.getSelectedColumnCount() == 1) {
				// copy leaf node or paste semantic element
				int [] rows = m_table.getSelectedRows();
				popupMenu = new JPopupMenu();
				if (col == m_leafNodeCol) {
					popupMenu.add(m_copyAction);

					// Create Semantic Element
					boolean canCreateSE = true;
					for (int r=0; r<rows.length; r++) {
						rowData = m_tableModel.getRowData(rows[r]);
						// Leaf non-null, SE null
						if (rowData.leafNode == null || rowData.semanticElement != null) {
							canCreateSE = false;
							break;
						}
					}
					if (canCreateSE) {
						popupMenu.add(new CreateSemanticElementForNode(rows));
					}
					
				} else if (col == m_semanticElemCol && m_selectedLeafRows != null) {
					boolean allSEsBlank = true;
					for (int r=0; r<rows.length; r++) {
						rowData = m_tableModel.getRowData(rows[r]);
						if (rowData.semanticElement != null) {
							allSEsBlank = false;
							break;
						}
					}
					if (allSEsBlank) {
						popupMenu.add(m_pasteAction);
					}
				}
			}
			
		} else if (col == m_rowNumbCol) {
			// First column - Add / Remove
			popupMenu = new JPopupMenu();
			if (rowData != null) {
				if (isShowLeafNodes()) {
					popupMenu.add(new NewLeafAction(row));
				}
				popupMenu.add(new AddBEAction(row));
				popupMenu.addSeparator();
				popupMenu.add(new DeleteRowAction(row));
				popupMenu.add(new DeleteLeafNodeAction(row));
			}
		}
		else if (col == m_leafNodeCol || col == m_semanticElemCol || col == m_BusinessElemCol) {
			popupMenu = new JPopupMenu();
			// Open Object 'name'
			if (selection instanceof Node || selection instanceof SemanticElement ||
					selection instanceof MdmiBusinessElementReference) {
				popupMenu.add(new OpenSelectionAction(selection));
			}
			// Open Field
			if (selection instanceof SemanticElement) {
				if (rowData != null && rowData.field != null) {
					popupMenu.add(new OpenSelectionAction(rowData.field));
				}
			}
			
			// Create Semantic Element
			if (selection instanceof Node && rowData != null && rowData.semanticElement == null) {
				popupMenu.add(new CreateSemanticElementForNode(row));
			}
			
			// Edit Cell
			if (selection instanceof Node || selection instanceof SemanticElement) {
				popupMenu.add(new AllowNameChange());
			} else if (col == m_BusinessElemCol) {
				// can be blank
				popupMenu.add(new ChangeBusinessElement(rowData.businessElement));
			}

			// node-specific menus
			DefaultMutableTreeNode treeNode = SelectionManager.getInstance().getEntitySelector().findNode(selection);
			if (treeNode instanceof EditableObjectNode) {
				List<JComponent> nodeSpecificMenus = ((EditableObjectNode)treeNode).getAdditionalPopuMenus();
				if (nodeSpecificMenus != null && nodeSpecificMenus.size() > 0) {
					popupMenu.addSeparator();
					for (JComponent item : nodeSpecificMenus) {
						popupMenu.add(item);
					}
				}
			}
			
		} else if (selection instanceof SEtoBELink && rowData != null && 
				rowData.semanticElement != null && rowData.businessElement != null ) {
			// want to be able to see/enter To/From rules between SE and BE
			popupMenu = new JPopupMenu();
			popupMenu.add(new ViewSEtoBELinkAction());

			// Open To/From Mdmi
			if (rowData.seToBELink.direction == SEtoBELink.Direction.Both
					|| rowData.seToBELink.direction == SEtoBELink.Direction.ToMdmi) {
				ToMessageElement toMdmi = SemanticElementBERLinkDialog.findToMessageElement(rowData.semanticElement,
						rowData.businessElement);
				if (toMdmi != null) {
					popupMenu.add(new OpenSelectionAction(toMdmi));
				}
			}
			if (rowData.seToBELink.direction == SEtoBELink.Direction.Both
					|| rowData.seToBELink.direction == SEtoBELink.Direction.FromMdmi) {
				ToBusinessElement fromMdmi = SemanticElementBERLinkDialog.findToBusinessElement(rowData.semanticElement,
						rowData.businessElement);
				if (fromMdmi != null) {
					popupMenu.add(new OpenSelectionAction(fromMdmi));
				}
			}
		}
		
		return popupMenu;
	}
	
	/** Get the string name from a model. Blank is returned if not defined */
	public static String getItemName(Object value) {
		String strValue = ClassUtil.getItemName(value, "");
		return strValue;
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
		enum Direction { None, ToMdmi, FromMdmi, Both };
		Direction direction = Direction.None;
	};
	
	private SEtoBELink.Direction getDirection(SemanticElement se, MdmiBusinessElementReference ber) {
		SEtoBELink.Direction direction = SEtoBELink.Direction.None;
		if (ber == null) {
			return direction;
		}
		ToMessageElement toMdmi = SemanticElementBERLinkDialog.findToMessageElement(se, ber);
		ToBusinessElement fromMdmi = SemanticElementBERLinkDialog.findToBusinessElement(se, ber);
		if (toMdmi != null && fromMdmi != null) {
			direction = SEtoBELink.Direction.Both;
		} else if (toMdmi != null) { 
			direction = SEtoBELink.Direction.ToMdmi;
		} else if (fromMdmi != null) {
			direction = SEtoBELink.Direction.FromMdmi;
		}

		return direction;
	}
	
	private int [] m_selectedLeafRows = null;
	
	/** Copy leaf nodes for pasting into Semantic Element */
	private void copyLeafNodes() {
		// multiple rows - only leaf column
		int [] rows = m_table.getSelectedRows();
		int [] cols = m_table.getSelectedColumns();
		if (rows.length > 0 && cols.length == 1 && cols[0] == m_leafNodeCol)
		{
			m_selectedLeafRows = rows;
		}
	}
	
	private void pasteLeafNodesIntoSemanticElement() {
		// multiple rows - only SE column matching Leaf selection
		int [] rows = m_table.getSelectedRows();
		int [] cols = m_table.getSelectedColumns();
		if (m_selectedLeafRows != null && 
				cols.length == 1 && cols[0] == m_semanticElemCol)
		{
			String message = null;
			
			StringBuffer rowsCopied = new StringBuffer("row");
			if (m_selectedLeafRows.length > 1) {
				rowsCopied.append("s");
			}
			rowsCopied.append(" ");
			rowsCopied.append(listToString(m_selectedLeafRows));
			
			// check that from and to rows match
			boolean dataValid = true;
			if (rows.length != m_selectedLeafRows.length) {
				// The data may only be pasted into the same rows that were copied (21-25, 27, 28)
				message = MessageFormat.format(s_res.getString("TableViewer.pasteRowsMessageFormat"),
						rowsCopied);
				dataValid = false;
			} else {
				for (int row = 0; row < rows.length && dataValid; row++) {
					if (rows[row] != m_selectedLeafRows[row]) {
						message = MessageFormat.format(s_res.getString("TableViewer.pasteRowsMessageFormat"),
								rowsCopied);
						dataValid = false;
					}
				}
			}

			
			if (message != null) {
				JOptionPane.showMessageDialog(this, message, Actions.PASTE_ACTION, 
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			newSEfromNode(rows);
			
			m_selectedLeafRows = null;
		}
	}
	
	/** convert individual numbers into lists - e.g 1-3,5,7,10-12 */
	public static String listToString(int [] numbers) {
		StringBuffer buf = new StringBuffer();
		
		for (int i = 0; i < numbers.length; i++) {
			//combine contiguous numbers - e.g. 1,2,3 -> 1-3
			if (i == 0 || numbers[i-1] < numbers[i]-1) {
				// start new number
				if (i > 0) {
					buf.append(", ");
				}
				buf.append(numbers[i]);
			} else if (i == numbers.length-1 || numbers[i+1] > numbers[i]+1) {
				// end of number range
				buf.append('-').append(numbers[i]);
			}
		}
		return buf.toString();
	}

	/** Data in a row of the table */
	private class RowData {
		public Node leafNode = null;
		public LeafNodeSELink nodeSELink = null;
		public SemanticElement semanticElement = null;
		public Field field = null;
		public SEtoBELink seToBELink = new SEtoBELink();
		public MdmiBusinessElementReference businessElement = null;
		
		public RowData makeCopy() {
			RowData rowData = new RowData();
			rowData.leafNode = leafNode;
			rowData.nodeSELink = nodeSELink;
			rowData.semanticElement = semanticElement;
			rowData.field = field;
			rowData.seToBELink = new SEtoBELink();
			if (seToBELink != null) {
				rowData.seToBELink.direction = seToBELink.direction;
			}
			rowData.businessElement = businessElement;
			
			return rowData;
		}
	}

	
	/** Table Model 
	 *  |<blank>| Leaf Node |connection|Semantic Element|connection|Business Element|
	 */
	private class TableViewModel extends AbstractTableModel {
		
		public boolean m_editable = false;
		
		private boolean m_dirty = false;
		private ArrayList<RowData> m_rowData = new ArrayList<RowData>();
		
		public TableViewModel() {
		}
		
		public void deleteRow(int row) {
			if (row >= 0 && row < m_rowData.size()) {
				m_rowData.remove(row);
				fireTableRowsDeleted(row, row);
				m_dirty = true;
			}
		}

		public RowData getRowData(int row) {
			if (row >= 0 && row < m_rowData.size()) {
				return m_rowData.get(row);
			} else {
				return null;
			}
		}
		
		public boolean isDirty() {
			return m_dirty;
		}
		
		public void loadTable() {
			m_rowData.clear();
			
			if (isShowLeafNodes()) {
				// organize by Node
				for (MessageGroup group : SelectionManager.getInstance().getEntitySelector().getMessageGroups()) {
					for (MessageModel model : group.getModels()) {
						MessageSyntaxModel syntax = model.getSyntaxModel();
						Node root = syntax.getRoot();
						addNode(root, -1);	// walk tree
					}
				}
			} else {
				// sort by Semantic Element
				for (MessageGroup group : SelectionManager.getInstance().getEntitySelector().getMessageGroups()) {
					for (MessageModel model : group.getModels()) {
						SemanticElementSet elementSet = model.getElementSet();
						if (elementSet != null) {
							List <SemanticElement> elements = new ArrayList<SemanticElement>(elementSet.getSemanticElements());
							Collections.sort(elements, new Comparators.SemanticElementComparator());
							for (SemanticElement se : elements) {
								RowData rowData = new RowData();
								rowData.semanticElement = se;
								addSemanticElementData(-1, rowData);
							}
						}
					}
				}
			}
			m_dirty = false;
			setWindowTitle();
			fireTableDataChanged();
		}
		
		
		/** Add this node to the list at the position specified by index.
		 * A value of -1 indicates the end of the list.
		 * @param node
		 * @param index
		 * @return	index of next node
		 */
		public int addNode(Node node, int index) {
			if (index == -1) {
				index = m_rowData.size();
			}
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
				} else if (node.getFieldName() != null && node.getFieldName().length() > 0) {
					// find parent
					Node parent = node.getParentNode();
					if (parent != null) {
						if (parent.getSemanticElement() != null) {
							rowData.semanticElement = parent.getSemanticElement();
							// get field from parent se's datatype
							if (rowData.semanticElement.getDatatype() instanceof DTComplex ) {
								DTComplex complexDatatype = (DTComplex)rowData.semanticElement.getDatatype();
								rowData.field = complexDatatype.getField(node.getFieldName());
							}
							rowData.nodeSELink = new LeafNodeSELink();
						}
					}
				}
				
				// fill in Business Elements - (There can be multiples)
				if (rowData.semanticElement != null) {
					index = addSemanticElementData(index, rowData);
				} else {
					// No SE
					if (showRow(rowData)) {
						m_rowData.add(index++, rowData);
					}
				}
				
			} else if (node instanceof Bag) {
				// check children
				for (Node child : ((Bag)node).getNodes()) {
					index = addNode(child, index);
				}
				
			} else if (node instanceof Choice) {
				// check children
				for (Node child : ((Choice)node).getNodes()) {
					index = addNode(child, index);
				}
				
			}
			
			return index;
		}

		/** Add a row for every BER associated with this Semantic Element */
		private int addSemanticElementData(int index, RowData rowData) {
			
			if (index == -1) {
				index = m_rowData.size();
			}
			
			ArrayList<MdmiBusinessElementReference> beList = new ArrayList<MdmiBusinessElementReference>();
			for (ToMessageElement toMdmi: rowData.semanticElement.getToMdmi()) {
				// add to list
				if (toMdmi.getBusinessElement() != null) {
					if (!beList.contains(toMdmi.getBusinessElement())) {
						beList.add(toMdmi.getBusinessElement());
					}
				}
			}
			for (ToBusinessElement fromMdmi : rowData.semanticElement.getFromMdmi()) {
				// add to list
				if (!beList.contains(fromMdmi.getBusinessElement())) {
					beList.add(fromMdmi.getBusinessElement());
				}
			}
			// add row for each Business Element
			if (beList.size() > 0) {
				for (MdmiBusinessElementReference ber : beList)
				{
					rowData.seToBELink.direction = getDirection(rowData.semanticElement, ber);
					rowData.businessElement = ber;
					// add row data for each BER
					if (showRow(rowData)) {
						m_rowData.add(index++, rowData);
						rowData = rowData.makeCopy();
					}
				}
			} else {
				// No BER - just add row for SE
				if (showRow(rowData)) {
					m_rowData.add(index++, rowData);
				}
			}
			return index;
		}

		@Override
		public int getRowCount() {
			return m_rowData.size();
		}

		@Override
		public int getColumnCount() {
			return m_columnNames.size();
		}

		@Override
		public String getColumnName(int columnIndex) {
			return m_columnNames.get(columnIndex);
		}

		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			boolean editable = m_editable;
			if (rowIndex < m_rowData.size() && editable) {
				// only Leaf, SE and BE can be edited (for now)
				RowData rowData = m_rowData.get(rowIndex);
				if (columnIndex == m_leafNodeCol && rowData.leafNode != null) {
					editable = true;
				} else if (columnIndex == m_semanticElemCol && rowData.semanticElement != null) {
					editable = true;
				} else if (columnIndex == m_BusinessElemCol) {
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
				if (columnIndex == m_leafNodeCol) {
					// Leaf Node
					value = rowData.leafNode;
				} else if (columnIndex == m_nodeSeLinkCol) {
					// node-SE link
					value = rowData.nodeSELink;
				} else if (columnIndex == m_semanticElemCol) {
					// Semantic Element
					value = rowData.semanticElement;
				} else if (columnIndex == m_seBeLinkCol) {
					// SE - BE link
					value = rowData.seToBELink;
				} else if (columnIndex == m_BusinessElemCol) {
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
				if (columnIndex == m_leafNodeCol) {
					// can only change name
					if (value instanceof String && rowData.leafNode != null) {
						rowData.leafNode.setName((String)value);
						// notify
						modelChanged(rowData.leafNode);
						m_table.repaint();	// force redraw
					}
				} else if (columnIndex == m_nodeSeLinkCol) {
					// node-SE link
					if (rowData.nodeSELink != value) {
						dirty = true;
					}
					rowData.nodeSELink = (LeafNodeSELink)value;
				} else if (columnIndex == m_semanticElemCol) {
					// can only change name
					if (value instanceof String && rowData.semanticElement != null) {
						rowData.semanticElement.setName((String)value);
						// notify
						modelChanged(rowData.semanticElement);
						m_table.repaint();	// force redraw
					}
				} else if (columnIndex == m_seBeLinkCol) {
					// SE - BE link
					if (rowData.seToBELink != value) {
						dirty = true;
					}
					rowData.seToBELink = (SEtoBELink)value;
				} else if (columnIndex == m_BusinessElemCol) {
					// Business Element
					if (value instanceof MdmiBusinessElementReference) {
						if (rowData.businessElement != value) {
							// Delete mappings to old businessElement
							if (deleteMappings(rowData.semanticElement, rowData.businessElement)) {
								// change data
								rowData.businessElement = (MdmiBusinessElementReference)value;
								// change link
								rowData.seToBELink.direction = getDirection(rowData.semanticElement,
										rowData.businessElement);
							}
						}
					}
				}
			}
		
			if (dirty != m_dirty) {
				m_dirty = dirty;
				setWindowTitle();
			}
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

			// select entire row if first column
			JTable table = (JTable)e.getSource();
			int rowAtPoint = table.rowAtPoint(e.getPoint());
			int colAtPoint = table.columnAtPoint(e.getPoint());
			if (rowAtPoint != -1 && colAtPoint == m_rowNumbCol) {
				table.setRowSelectionInterval(rowAtPoint, rowAtPoint);
				table.setColumnSelectionInterval(0, m_columnNames.size()-1);
			}

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
				boolean selected = false;
				int [] selectedRows = table.getSelectedRows();
				for (int i=0; i<selectedRows.length; i++) {
					if (selectedRows[i] == rowAtPoint) {
						selected = true;
						break;
					}
				}
				if (!selected) {
					table.setRowSelectionInterval(rowAtPoint, rowAtPoint);
				}
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
	
	/** Text Editor to change name */
	public class CustomCellEditor extends AbstractCellEditor implements TableCellEditor {
		// This is the component that will handle the editing of the cell value
		JTextField m_textField = new JTextField();

		// This method is called when a cell value is edited by the user.
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean isSelected, int rowIndex, int colIndex) {
			// 'value' is value contained in the cell located at (rowIndex, colIndex)

			String name = getItemName(value);
			m_textField.setText(name);
			
			if (isSelected) {
				// cell selected
				m_textField.setCaretPosition(0);
				m_textField.moveCaretPosition(name.length());
				m_textField.requestFocusInWindow();
			}

			return m_textField;
		}

		// This method is called when editing is completed.
		// It must return the new value to be stored in the cell.
		@Override
		public Object getCellEditorValue() {
			return m_textField.getText();
		}

	}

	
	public class BusinessElementCellEditor extends AbstractCellEditor implements TableCellEditor,
				CollectionChangeListener {
	    // This is the component that will handle the editing of the cell value
		private JComboBox m_comboBox = new JComboBox();
		
	    public BusinessElementCellEditor() {
	    	// load with all business elements
	    	fillBERefComboBox(m_comboBox);
	    	m_comboBox.setRenderer(new ComboBoxRenderer());
	    }


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
			fillBERefComboBox(m_comboBox);
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
	public static class ComboBoxRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			
			value = TableViewer.getItemName(value);

			return super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
		}
		
	}
	
	///////////////////////////////////////
	// Table Cell Renderers
	///////////////////////////////////////
	private class IconRenderer extends DefaultTableCellRenderer {
		private Icon m_icon = null;
		public IconRenderer(Icon icon) {
			m_icon = icon;
			setIcon(m_icon);
			setHorizontalAlignment(CENTER);
	        setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		}
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
		    // Inherit the colors and font from the header component
	        if (table != null) {
	            JTableHeader header = table.getTableHeader();
	            if (header != null) {
	                setForeground(header.getForeground());
	                setBackground(header.getBackground());
	                setFont(header.getFont());
	            }
	        }

			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);
			return c;
		}
	}
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
			Color bgColor = isSelected ? table.getSelectionBackground() : table.getBackground();
			
			if (column == m_rowNumbCol) {
				value = String.valueOf(row);
				align = CENTER;
				style = Font.ITALIC;
				toolTip = "Row #" + row;
				if (!isSelected) {
					bgColor = table.getTableHeader().getBackground();
				}
				
			} else if (value instanceof Node) {
				Node node = (Node)value;
				value = node.getName();
				icon = TreeNodeIcon.getTreeIcon(node.getClass());
				// show path in tool tip
				StringBuilder buf = new StringBuilder(node.getName());
				Node parent = node.getParentNode();
				while (parent != null) {
					// prepend parent name  (parent->buf)
					buf.insert(0, "&rarr;");	// right arrow symbol
					buf.insert(0, parent.getName());
					parent = parent.getParentNode();
				}
				// surround with html tag so arrow appears
				buf.insert(0, "<html>").append("</html>");
				toolTip = buf.toString();
				
			} else if (value instanceof LeafNodeSELink) {
				value = SemanticElementNode.DOUBLE_ARROW;	
				align = CENTER;
				size = m_defaultFontSize + 4;
				
			} else if (value instanceof SemanticElement) {
				SemanticElement se = (SemanticElement)value;
				value = se.getName();
				// check for field name
				RowData rowData = m_tableModel.getRowData(row);
				if (rowData != null && rowData.field != null) {
					// SemanticElement.fieldName
					StringBuilder buf = new StringBuilder(se.getName());
					buf.append('.').append(rowData.field.getName());
					value = buf.toString();
				}
				
				icon = TreeNodeIcon.getTreeIcon(se.getClass());
				toolTip = ClassUtil.createToolTip(se);
				
			} else if (value instanceof SEtoBELink) {
				SEtoBELink se = (SEtoBELink)value;
				// <-- / --> / <-->
				switch (se.direction) {
				case None:
					value = "";
					RowData rowData = m_tableModel.getRowData(row);
					if (rowData != null && rowData.semanticElement != null
							&& SemanticElementType.LOCAL.equals(rowData.semanticElement.getSemanticElementType())) {
						value = "Local";
					}
					toolTip = "Un-linked";
					break;
				case Both:
					value = SemanticElementNode.DOUBLE_ARROW;
					size = m_defaultFontSize + 4;
					toolTip = "SE To/From BER";
					break;
				case ToMdmi:
					value = SemanticElementNode.LEFT_ARROW;
					size = m_defaultFontSize + 4;
					toolTip = "BER to SE";
					break;
				case FromMdmi:
					value = SemanticElementNode.RIGHT_ARROW;
					size = m_defaultFontSize + 4;
					toolTip = "SE to BER";
					break;
				}
				align = CENTER;
				
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
			setBackground(bgColor);
			
			return c;
		}
	}
	
	
	///////////////////////////////////////
	// Menu Action Handlers
	///////////////////////////////////////
	
	/** Base class for all actions in this class */
	public abstract class TableViewAction extends AbstractAction {
		
		protected TableViewAction(String name) {
			super(name);
		}
		
		@Override
		protected Frame getApplicationFrame() {
			return TableViewer.this;
		}
	}

	public class OpenSelectionAction extends TableViewAction {
		private Object m_selection = null;
		
		public OpenSelectionAction(Object object) {
			super(MessageFormat.format(s_res.getString("TableViewer.openFormat"), 
					ClassUtil.beautifyName(object.getClass()), ClassUtil.getItemName(object)));
			m_selection = object;
		}

		@Override
		public void execute(ActionEvent e) {
			openSelection(m_selection);
		}
		
	}

	// allow name change
	public class AllowNameChange extends TableViewAction {
		public AllowNameChange() {
			super(s_res.getString("TableViewer.changeName"));
		}

		@Override
		public void execute(ActionEvent e) {
			allowEditing();
		}
		
	}
	
	// create a semantic element for this node, using the node name
	public class CreateSemanticElementForNode extends TableViewAction {
		int [] m_rows;
		
		public CreateSemanticElementForNode(int rowNum) {
			super(s_res.getString("TableViewer.createSE"));
			m_rows = new int[1];
			m_rows[0] = rowNum;
		}
		
		public CreateSemanticElementForNode(int [] rows) {
			super(s_res.getString("TableViewer.createSE"));
			m_rows = rows;
		}

		@Override
		public void execute(ActionEvent e) {
			newSEfromNode(m_rows);
		}
		
	}

	// allow BE change
	public class ChangeBusinessElement extends TableViewAction {
		public ChangeBusinessElement(MdmiBusinessElementReference ber) {
			// select / change business element
			super(ber == null ? s_res.getString("TableViewer.selectBusinessElement"):
				s_res.getString("TableViewer.changeBusinessElement"));
		}
		
		@Override
		public void execute(ActionEvent e) {
			allowEditing();	// focus will be put on BER cell
		}
		
	}
	
	// View SE to BE Link
	public class ViewSEtoBELinkAction extends TableViewAction {
		public ViewSEtoBELinkAction() {
			super(s_res.getString("TableViewer.viewSEToBELink"));
		}
		
		@Override
		public void execute(ActionEvent e) {
			openSeToBerLink();
		}
	}

	
	// new leaf node
	public class NewLeafAction extends TableViewAction {
		int m_rowNum;
		public NewLeafAction(int rowNum) {
			super(s_res.getString("TableViewer.newLeaf"));
			m_rowNum = rowNum;
		}
		
		@Override
		public void execute(ActionEvent e) {
			newLeafNode(m_rowNum);
		}
		
	}


	// Add Business Element
	public class AddBEAction extends TableViewAction {
		int m_rowNum;
		public AddBEAction(int rowNum) {
			super(s_res.getString("TableViewer.addBE"));
			m_rowNum = rowNum;
		}
		
		@Override
		public void execute(ActionEvent e) {
			addNewBE(m_rowNum);
		}
		
	}
	
	// delete row
	public class DeleteRowAction extends TableViewAction {
		int m_rowNum;
		public DeleteRowAction(int rowNum) {
			super(s_res.getString("TableViewer.deleteRow"));
			m_rowNum = rowNum;
		}
		
		@Override
		public void execute(ActionEvent e) {
			deleteRow(m_rowNum);
		}
		
	}
	
	// delete row
	public class DeleteLeafNodeAction extends AbstractAction {
		int m_rowNum;
		public DeleteLeafNodeAction(int rowNum) {
			super(s_res.getString("TableViewer.deleteLeaf"));
			m_rowNum = rowNum;
		}

		@Override
		protected Frame getApplicationFrame() {
			return TableViewer.this;
		}
		
		@Override
		public void execute(ActionEvent e) {
			deleteLeafNode(m_rowNum);
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
				Object source = e.getSource();
				if (source == m_refreshButton) {
					refreshTable();
				} else if (source == m_mainWindowButton) {
					final Frame appFrame = SystemContext.getApplicationFrame();

					EventQueue.invokeLater(new Runnable() {
					    @Override
					    public void run() {
					    	appFrame.setVisible(true);
					    	appFrame.toFront();
					    	appFrame.repaint();
					    }
					});
				
				} else if (source == m_showLeafNodes) {
					refreshTable();
					
				} else if (source == m_filters) {
					refreshTable();
				}

			} catch (Exception ex) {
				SelectionManager.getInstance().getStatusPanel().writeException(ex);
			} finally {
				cm.restoreCursor();
			}	
		}
	}

	// ModelChangeListener
	private class ChangeListener implements ModelChangeListener, CollectionChangeListener {

		/////////////////////////////
		// ModelChangeListener
		/////////////////////////////
		@Override
		public void modelChanged(ModelChangeEvent e) {
			// repaint table
			m_table.repaint();
		}

		/////////////////////////////
		// CollectionChangeListener
		/////////////////////////////
		@Override
		public Class<?> getListenForClass() {
			// we want to know when message groups are added or removed
			return MessageGroup.class;
		}

		@Override
		public void contentsChanged(CollectionChangeEvent e) {
			// new message group
			refreshTable();
		}
	}

}
