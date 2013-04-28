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
import java.text.MessageFormat;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.tree.DefaultMutableTreeNode;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.CollectionChangeEvent;
import org.openhealthtools.mdht.mdmi.editor.map.CollectionChangeListener;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.console.LinkedObject;

/** An IEditorField that allows selection from a ComboBox, plus contains a Find and New
 * button */
public abstract class AdvancedSelectionField extends JPanel implements IEditorField, ActionListener {
	public static final String BLANK_ENTRY = "     ";

	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.editor.Local");

	private GenericEditor m_parentEditor;
	
	private GridBagConstraints m_gbc = null;

	private JComboBox   m_comboBox;
	private JButton     m_viewButton = new JButton(s_res.getString("AdvancedSelectionField.viewButton"));
	private JButton     m_refreshButton = new JButton(s_res.getString("AdvancedSelectionField.refreshButton"));
	private JButton     m_createButton = new JButton(s_res.getString("AdvancedSelectionField.createButton"));

	private ChangeListener m_changeListener = new ChangeListener();

	private boolean m_ignoreEvents = false;

	protected Object m_invalidValue = null;

	private CompoundBorder m_highlightBorder = null;


	/** Build the UI for the selector */
	protected AdvancedSelectionField(GenericEditor parentEditor) {
		buildUI(parentEditor);
	}

	/** Do not build UI yet. Caller must call buildUI(GenericEditor) to build UI */
	protected AdvancedSelectionField() {
	}
	
	/** Return the editor containing this field */
	protected GenericEditor getParentEditor() {
		return m_parentEditor;
	}
	
	protected  GridBagConstraints GetGridBagConstraints() {
		if (m_gbc == null) {
			m_gbc = new GridBagConstraints();
			m_gbc.gridx = 0;
			m_gbc.gridy = 0;

			m_gbc.weightx = 1;
			m_gbc.fill = GridBagConstraints.HORIZONTAL;
			m_gbc.insets.right = Standards.RIGHT_INSET;
		}
		return m_gbc;
	}
	
	/** Set the parent editor and build the UI for the component
	 * @param parentEditor
	 */
	protected void buildUI(GenericEditor parentEditor) {
		m_parentEditor = parentEditor;

		setLayout(new GridBagLayout());
		GridBagConstraints gbc = GetGridBagConstraints();
		
		m_comboBox = createComboBox();

		loadComboBox();
		add(m_comboBox, gbc);

		gbc.gridx++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets.left = Standards.LEFT_INSET;
		gbc.insets.right = 0;
		// View Button
		m_viewButton.setIcon(AbstractComponentEditor.getIcon(this.getClass(),
				s_res.getString("AdvancedSelectionField.viewIcon")));
		m_viewButton.setEnabled(false);
		add(m_viewButton, gbc);
		
		// Create Button (hidden)
		gbc.gridx++;
		m_createButton.setVisible(false);
		add(m_createButton, gbc);

		//		gbc.gridx++;
		//		m_refreshButton.setIcon(AbstractComponentEditor.getIcon(this.getClass(),
		//				s_res.getString("AdvancedSelectionField.refreshIcon")));
		//		add(m_refreshButton, gbc);

		m_comboBox.setRenderer(getComboBoxRenderer());
	}
	
	/** show/hide the "create" button. It is hidden by default */
	public void showCreateButton(boolean show) {
		 m_createButton.setVisible(show);
	}

	/** Create a JComboBox
	 * @return
	 */
	protected JComboBox createComboBox() {
		return new ComboBox();
	}
	
	/** Get the data to load the combo box. The first entry should be the BLANK_ENTRY if
	 * null values are allowed */
	protected abstract Collection<? extends Object> getComboBoxData();

	/** Re-load the combo box with data from getComboBoxData() */
	protected void loadComboBox() {
		Collection<?> data = getComboBoxData();
		m_comboBox.removeAllItems();
		for (Object item : data) {
			m_comboBox.addItem(item);
		}
	}

	public JComboBox getComboBox() {
		return m_comboBox;
	}

	public JButton getViewButton() {
		return m_viewButton;
	}

	public JButton getCreateButton() {
		return m_createButton;
	}
	
	public JButton getRefreshButton() {
		return m_refreshButton;
	}

	@Override
	public JComponent getComponent() {
		return this;
	}


	/** Get the selected item in the combo box. If the selection is the
	 * BLANK_ENTRY, then return null
	 */
	@Override
	public Object getValue() {
		Object value = m_comboBox.getSelectedItem();
		if (BLANK_ENTRY.equals(value) || value == m_invalidValue) {
			return null;
		}
		return value;
	}

	@Override
	public void setDisplayValue(Object value) {
		if (value == null) {
			value = BLANK_ENTRY;
		}
		m_comboBox.setSelectedItem(value);

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
			m_comboBox.removeItem(m_invalidValue);
		}
		m_invalidValue = null;

		Object selection = m_comboBox.getSelectedItem();
		if (!value.equals(selection)) {
			m_invalidValue = value;
			m_comboBox.setForeground(Color.red);

			// add to combo box, and tag as bad
			m_comboBox.addItem(value);
			m_comboBox.setSelectedItem(value);

			// show error
			Object modelObject = m_parentEditor.getEditObject();
			String fieldName = m_parentEditor.getFieldNameFor(this);
			m_parentEditor.highlightFieldWithError(fieldName);
			SelectionManager.getInstance().getStatusPanel().writeErrorLink(
					// Error in <objectType>
					MessageFormat.format(s_res.getString("AdvancedSelectionField.errorInObjectFormat"),
							ClassUtil.beautifyName(m_parentEditor.getObjectClass())), 
							new LinkedObject(modelObject, m_parentEditor.getModelName(modelObject)),
							// - <value> is not a valid choice for <field name>
							MessageFormat.format(s_res.getString("AdvancedSelectionField.itemNotFoundFormat"),
									toString(value), ClassUtil.beautifyName(fieldName)));
		}
	}

	/** Convert an object in the list to a string */
	protected abstract String toString(Object listObject);

	/** Get a tooltip for an item in the list */
	protected abstract String getToolTipText(Object listObject);

	@Override
	public void setReadOnly() {
		m_comboBox.setEnabled(false);
		m_refreshButton.setEnabled(false);
	}

	@Override
	public void highlightText(String text, Color highlightColor) {
		// restore border
		if (m_highlightBorder != null) {
			m_comboBox.setBorder(m_highlightBorder.getInsideBorder());
		}

		Object value = getValue();
		if (value != null && value.toString().equals(text)) {
			// SetBackground doesn't seem to work right, so we'll draw a border instead
			Border insideBorder = m_comboBox.getBorder();
			Border outsideBorder = BorderFactory.createMatteBorder(3, 3, 3, 3, highlightColor);
			m_highlightBorder = BorderFactory.createCompoundBorder(outsideBorder, insideBorder);
			m_comboBox.setBorder(m_highlightBorder);
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
		m_comboBox.addActionListener(this);
		m_viewButton.addActionListener(this);
		m_createButton.addActionListener(this);
		m_refreshButton.addActionListener(this);
		m_refreshButton.setToolTipText(s_res.getString("AdvancedSelectionField.refreshToolTip"));

		if (getDataClass() != null) {
			SelectionManager.getInstance().addCollectionChangeListener(m_changeListener);
		}

		m_comboBox.setRenderer(getComboBoxRenderer());
	}
	@Override
	public void removeNotify() {
		m_comboBox.removeActionListener(this);
		m_viewButton.removeActionListener(this);
		m_createButton.removeActionListener(this);
		m_refreshButton.removeActionListener(this);
		m_refreshButton.setToolTipText(null);

		if (getDataClass() != null) {
			SelectionManager.getInstance().removeCollectionChangeListener(m_changeListener);
		}
		m_comboBox.setRenderer(null);

		super.removeNotify();
	}

	/** Get the renderer used for the combobox. The default is a ComboBoxRenderer */
	protected ListCellRenderer getComboBoxRenderer() {
		return new ComboBoxRenderer();
	}


	/** Open the selected item */
	protected void openSelection() {
		Object selection = m_comboBox.getSelectedItem();
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

		} else if (e.getSource() == m_refreshButton) {
			refreshSelections();

		} else if (e.getSource() == m_comboBox){
			// Item selected
			Object value = m_comboBox.getSelectedItem();
			if (value == m_invalidValue) {
				m_comboBox.setForeground(Color.red);
			} else {
				m_comboBox.setForeground(UIManager.getColor("ComboBox.foreground"));
			}
			m_viewButton.setEnabled(getValue() != null);
			// update parent
			if (m_parentEditor != null) {
				m_parentEditor.setModified(true);
			}

			// restore border
			if (m_highlightBorder != null) {
				m_comboBox.setBorder(m_highlightBorder.getInsideBorder());
			}
		}
	}

	/**
	 * Refresh the selection list
	 */
	protected void refreshSelections() {
		m_ignoreEvents = true;
		Object selection = m_comboBox.getSelectedItem();
		loadComboBox();
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


	/** Renderer for the ComboBox - use toString(value) method. Value may be a ComboBoxItem */
	protected class ComboBoxRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			Color foreground = isSelected ? list.getSelectionForeground() : list.getForeground();
			Color background = isSelected ? list.getSelectionBackground() : list.getBackground();
			
			String displayValue = "";
			String toolTip = null;

			if (value == null) {
				displayValue = "";
			} else if (value instanceof ComboBoxItem) {
				displayValue = ((ComboBoxItem)value).toString();
				// unwrap
				value = ((ComboBoxItem)value).m_object;
			} else {
				displayValue = AdvancedSelectionField.this.toString(value);
			}
			
			
			if (value == m_invalidValue) {
				if (isSelected) {
					background = Color.red.darker();
				} else {
					foreground = Color.red;
				}
			}
				
			if (isSelected) {
				toolTip = AdvancedSelectionField.this.getToolTipText(value);
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
	
	/** JComboBox that holds items wrapped in a ComboBoxItem */
	public class ComboBox extends JComboBox {
		@Override
		public void addItem(Object anObject) {
			// wrap in ComboBoxItem
			if (!(anObject instanceof ComboBoxItem)) {
				anObject = new ComboBoxItem(anObject);
			}
			super.addItem(anObject);
		}

		@Override
		public Object getItemAt(int index) {
			// un-wrap
			Object item = super.getItemAt(index);
			if (item instanceof ComboBoxItem) {
				item = ((ComboBoxItem)item).m_object;
			}
			return item;		
		}

		@Override
		public Object getSelectedItem() {
			// un-wrap
			Object selected = super.getSelectedItem();
			if (selected instanceof ComboBoxItem) {
				selected = ((ComboBoxItem)selected).m_object;
			}
			return selected;
		}

		@Override
		public void setSelectedItem(Object anObject) {
			// wrap in ComboBoxItem
			if (!(anObject instanceof ComboBoxItem)) {
				anObject = new ComboBoxItem(anObject);
			}
			super.setSelectedItem(anObject);
		}	
	}

	/** Wrapper for items in the list */
	public class ComboBoxItem {
		public Object m_object;
		
		public ComboBoxItem(Object object) {
			m_object = object;
		}
		
		@Override
		public String toString() {
			return AdvancedSelectionField.this.toString(m_object);
		}
		
		@Override
		public boolean equals(Object otherObject) {
			if (otherObject instanceof ComboBoxItem) {
				return equals(((ComboBoxItem)otherObject).m_object);
			}
			
			// compare objects
			return m_object.equals(otherObject);
		}
		
	}

}
