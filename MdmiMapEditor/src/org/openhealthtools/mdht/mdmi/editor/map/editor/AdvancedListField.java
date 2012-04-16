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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.CollectionChangeEvent;
import org.openhealthtools.mdht.mdmi.editor.map.CollectionChangeListener;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.console.LinkedObject;

/** An IEditorField that allows selection from a JList, plus contains a Find and New
 * button */
public abstract class AdvancedListField extends JPanel implements IEditorField, ActionListener, 
ListSelectionListener, MouseListener {

	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.editor.Local");

	private GenericEditor m_parentEditor;

   private AdvancedListModel m_model;
	private JList	     m_list;
	private JButton     m_viewButton = new JButton(s_res.getString("AdvancedListField.viewButton"));
	private JButton     m_createButton = new JButton(s_res.getString("AdvancedListField.createButton"));

	private ChangeListener m_changeListener = new ChangeListener();

	private boolean m_ignoreEvents = false;

	protected Object m_invalidValue = null;

	private CompoundBorder m_highlightBorder = null;


	/** Build the UI for the selector */
	protected AdvancedListField(GenericEditor parentEditor) {
		buildUI(parentEditor);
	}

	/** Do not build UI yet. Caller must call buildUI(GenericEditor) to build UI */
	protected AdvancedListField() {
	}
	
	/** Return the editor containing this field */
	protected GenericEditor getParentEditor() {
		return m_parentEditor;
	}
	
	/** Set the parent editor and build the UI for the component
	 * @param parentEditor
	 */
	protected void buildUI(GenericEditor parentEditor) {
		m_parentEditor = parentEditor;

		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;

		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.right = Standards.RIGHT_INSET;

      m_model = new AdvancedListModel();
      m_list = new AdvancedList(m_model);

      JScrollPane scroller = new JScrollPane(m_list);

		loadList();
		add(scroller, gbc);

		gbc.gridx++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets.left = Standards.LEFT_INSET;
		gbc.insets.right = 0;
		// View Button
		m_viewButton.setIcon(AbstractComponentEditor.getIcon(this.getClass(),
				s_res.getString("AdvancedListField.viewIcon")));
		m_viewButton.setEnabled(false);
		add(m_viewButton, gbc);
		
		// Create Button (hidden)
		gbc.gridx++;
		m_createButton.setVisible(false);
		add(m_createButton, gbc);

		m_list.setCellRenderer(getListRenderer());
		m_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	/** show/hide the "create" button. It is hidden by default */
	public void showCreateButton(boolean show) {
		 m_createButton.setVisible(show);
	}

	
	/** Get the data to load the list */
	protected abstract Collection<? extends Object> getListData();

	/** Re-load the list with data from getListData() */
	protected void loadList() {
		Collection<?> data = getListData();
		m_model.removeAllElements();
		for (Object item : data) {
	   	m_model.addElement(item);
		}
	}

	public JList getList() {
		return m_list;
	}

	public JButton getViewButton() {
		return m_viewButton;
	}

	public JButton getCreateButton() {
		return m_createButton;
	}

	@Override
	public JComponent getComponent() {
		return this;
	}


	/** Get the selected item in the combo box. If the selection is the
	 * invalid value, then return null
	 */
	@Override
	public Object getValue() {
		Object value = m_list.getSelectedValue();
		if (value == m_invalidValue) {
			return null;
		}
		return value;
	}

	@Override
	public void setDisplayValue(Object value) {
		m_list.setSelectedValue(value, true);

		// validate selection
		validateSetting(value);

		m_viewButton.setEnabled(getValue() != null);
	}

	/** Verify that the value to be set is in the list. If not,
	 * show an error message
	 * @param value
	 */
	private void validateSetting(Object value) {
		if (m_invalidValue != null) {
			// remove old value from list
			m_model.removeElement(m_invalidValue);
		}
		m_invalidValue = null;
		
		if (value == null) {
			return;
		}

		Object selection = m_list.getSelectedValue();
		if (!value.equals(selection)) {
			m_invalidValue = value;
			m_list.setForeground(Color.red);

			// add to list box, and tag as bad
			m_model.addElement(value);
			m_list.setSelectedValue(value, true);

			// show error
			Object modelObject = m_parentEditor.getEditObject();
			String fieldName = m_parentEditor.getFieldNameFor(this);
			m_parentEditor.highlightFieldWithError(fieldName);
			SelectionManager.getInstance().getStatusPanel().writeErrorLink(
					// Error in <objectType>
					MessageFormat.format(s_res.getString("AdvancedListField.errorInObjectFormat"),
							ClassUtil.beautifyName(m_parentEditor.getObjectClass())), 
							new LinkedObject(modelObject, m_parentEditor.getModelName(modelObject)),
							// - <value> is not a valid choice for <field name>
							MessageFormat.format(s_res.getString("AdvancedListField.itemNotFoundFormat"),
									toString(value), ClassUtil.beautifyName(fieldName)));
		}
	}

	/** Convert an object in the list to a string */
	protected abstract String toString(Object listObject);

	/** Get a tooltip for an item in the list */
	protected abstract String getToolTipText(Object listObject);

	@Override
	public void setReadOnly() {
		m_list.setEnabled(false);
	}

	@Override
	public void highlightText(String text, Color highlightColor) {
		// restore border
		if (m_highlightBorder != null) {
			m_list.setBorder(m_highlightBorder.getInsideBorder());
		}

		Object value = getValue();
		if (value != null && value.toString().equals(text)) {
			// SetBackground doesn't seem to work right, so we'll draw a border instead
			Border insideBorder = m_list.getBorder();
			Border outsideBorder = BorderFactory.createMatteBorder(3, 3, 3, 3, highlightColor);
			m_highlightBorder = BorderFactory.createCompoundBorder(outsideBorder, insideBorder);
			m_list.setBorder(m_highlightBorder);
		}
	}

	/** return the class of data in the list. If non-null, a listener will be addedd to
	 * the selection manager so that changes to a data type that is in the list will cause
	 * the list to be updated
	 * @return
	 */
	public abstract Class<?> getDataClass();

	@Override
	public void addNotify() {
		super.addNotify();
		m_list.addListSelectionListener(this);
		m_list.addMouseListener(this);
		m_viewButton.addActionListener(this);
		m_createButton.addActionListener(this);

		if (getDataClass() != null) {
			SelectionManager.getInstance().addCollectionChangeListener(m_changeListener);
		}

		m_list.setCellRenderer(getListRenderer());
	}
	@Override
	public void removeNotify() {
		m_list.addListSelectionListener(this);
		m_list.removeMouseListener(this);
		m_viewButton.removeActionListener(this);
		m_createButton.removeActionListener(this);

		if (getDataClass() != null) {
			SelectionManager.getInstance().removeCollectionChangeListener(getDataClass(), 
					m_changeListener);
		}
		m_list.setCellRenderer(null);

		super.removeNotify();
	}

	/** Get the renderer used for the list. The default is a ListRenderer */
	protected ListCellRenderer getListRenderer() {
		return new ListRenderer();
	}


	/** Open the selected item */
	protected void openSelection() {
		Object selection = m_list.getSelectedValue();
		if (selection != null) {

			DefaultMutableTreeNode treeNode = SelectionManager.getInstance().getEntitySelector().findNode(selection);
			if (treeNode != null) {
				// select it (this will expand if necessary)
				SelectionManager.getInstance().getEntitySelector().selectNode(treeNode);
				SelectionManager.getInstance().editItem(treeNode);
			}
		}
	}
	
	/** Create a new item */
	protected void createNewItem() {
		// default does nothing
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (m_ignoreEvents) {
			return;
		}

		if (e.getSource() == m_viewButton) {
			openSelection();
			
		} else if (e.getSource() == m_createButton) {
			createNewItem();
		}
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == m_list){
			// Item selected
			Object value = m_list.getSelectedValue();
			if (value == m_invalidValue) {
				m_list.setForeground(Color.red);
			} else {
				m_list.setForeground(UIManager.getColor("List.foreground"));
			}
			m_viewButton.setEnabled(getValue() != null);
			// update parent
			if (m_parentEditor != null) {
				m_parentEditor.setModified(true);
			}

			// restore border
			if (m_highlightBorder != null) {
				m_list.setBorder(m_highlightBorder.getInsideBorder());
			}
		}		
	}


	/**
	 * Refresh the selection list
	 */
	protected void refreshSelections() {
		m_ignoreEvents = true;
		Object selection = m_list.getSelectedValue();
		loadList();
		setDisplayValue(selection);
		m_ignoreEvents = false;
	}

	private class ChangeListener implements CollectionChangeListener {
		@Override
		public void contentsChanged(CollectionChangeEvent e) {
			refreshSelections();
		}

		@Override
		public Class<?> getListenForClass() {
			return getDataClass();
		}
	}


	/** Renderer for the List - use toString(value) method. Value may be a ListItem */
	protected class ListRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			Color foreground = isSelected ? list.getSelectionForeground() : list.getForeground();
			Color background = isSelected ? list.getSelectionBackground() : list.getBackground();
			
			String displayValue = "";
			String toolTip = null;

			if (value == null) {
				displayValue = "";
			} else if (value instanceof ListItem) {
				displayValue = ((ListItem)value).toString();
				// unwrap
				value = ((ListItem)value).m_object;
			} else {
				displayValue = AdvancedListField.this.toString(value);
			}
			
			
			if (value == m_invalidValue) {
				if (isSelected) {
					background = Color.red.darker();
				} else {
					foreground = Color.red;
				}
			}
				
			if (isSelected) {
				toolTip = AdvancedListField.this.getToolTipText(value);
				// split into multi-lines if too long
				toolTip = AbstractComponentEditor.formatToolTip(toolTip);
			}
			
			Component c = super.getListCellRendererComponent(list, displayValue, index, isSelected,
					cellHasFocus);
			c.setBackground(background);
			c.setForeground(foreground);
			setToolTipText(toolTip);
			return c;
		}

	}
	
	/** JList class that holds items wrapped in a ListItem */
	public class AdvancedList extends JList {
		public AdvancedList(AdvancedListModel dataModel) {
			super(dataModel);
		}

		@Override
		public Object getSelectedValue() {
			// un-wrap
			Object selected = super.getSelectedValue();
			if (selected instanceof ListItem) {
				selected = ((ListItem)selected).m_object;
			}
			return selected;
		}

		@Override
		public void setSelectedValue(Object anObject, boolean shouldScroll) {
			if (!(anObject instanceof ListItem)) {
				anObject = new ListItem(anObject);
			}
			super.setSelectedValue(anObject, shouldScroll);
		}
		
	}
	
	/** ListModel that holds items wrapped in a ListItem */
	public class AdvancedListModel extends DefaultListModel {
		@Override
		public void addElement(Object obj) {
			// wrap in ListItem
			if (!(obj instanceof ListItem)) {
				obj = new ListItem(obj);
			}
			super.addElement(obj);
		}

		@Override
		public Object elementAt(int index) {
			Object item =  super.elementAt(index);
			if (item instanceof ListItem) {
				item = ((ListItem)item).m_object;
			}
			return item;		
		}
	}

	/** Wrapper for items in the list */
	public class ListItem {
		public Object m_object;
		
		public ListItem(Object object) {
			m_object = object;
		}
		
		@Override
		public String toString() {
			return AdvancedListField.this.toString(m_object);
		}
		
		@Override
		public boolean equals(Object otherObject) {
			if (otherObject instanceof ListItem) {
				return equals(((ListItem)otherObject).m_object);
			}
			
			// compare objects
			if (m_object == null) {
				return (otherObject == null);
			}
			return m_object.equals(otherObject);
		}
		
	}

	////////////////////////////////////
	// Mouse Listener methods
	//////////////////////////////////
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			openSelection();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// ignored
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// ignored
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// ignored
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// ignored
	}
}
