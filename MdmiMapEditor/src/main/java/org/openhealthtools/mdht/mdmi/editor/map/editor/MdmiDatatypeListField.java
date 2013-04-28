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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.openhealthtools.mdht.mdmi.editor.common.components.VerticalButtonPanel;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.console.LinkedObject;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;

/** A collection of MdmiDatatypes displayed as check boxes in a list.
 * Any one, or more of the datatypes can be selected.
 * 
 * Changes to the selections are noted via a property change event using the
 * NUM_SELECTIONS_PROPERTY property name.
 * @author Conway
 *
 */
public class MdmiDatatypeListField extends DatatypeSelectionPanel  implements IEditorField, ActionListener {

	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.editor.Local");

	private GenericEditor m_parentEditor;
	
	private JButton     m_viewButton;
	private SelectionListener m_selectionListener = new SelectionListener();

	private JCheckBox m_highlightBox = null;
	private List<JCheckBox> m_invalidValues = new ArrayList<JCheckBox>();


	public MdmiDatatypeListField(GenericEditor parentEditor, Collection <MdmiDatatype> datatypes) {
		super(datatypes);
		m_parentEditor = parentEditor;
	}

	@Override
	protected VerticalButtonPanel createVerticalButtons() {
		VerticalButtonPanel panel = super.createVerticalButtons();
		

		// View Button
		m_viewButton  = new JButton(s_res.getString("AdvancedSelectionField.viewButton"));
		m_viewButton.setIcon(AbstractComponentEditor.getIcon(this.getClass(),
				s_res.getString("AdvancedSelectionField.viewIcon")));
		m_viewButton.setEnabled(false);
		panel.addStrut(m_viewButton.getPreferredSize().height);
		panel.add(m_viewButton);
			
		return panel;
	}
	
	
	@Override
	public void addNotify() {
		super.addNotify();
		
		m_viewButton.addActionListener(this);
		getList().getSelectionModel().addListSelectionListener(m_selectionListener);
		// use our own renderer
		getList().setCellRenderer(new ListRenderer());
	}

	@Override
	public void removeNotify() {
		m_viewButton.removeActionListener(this);
		getList().getSelectionModel().removeListSelectionListener(m_selectionListener);

		getList().setCellRenderer(null);
		super.removeNotify();
	}

	/** Enable/disable buttons based on selection */
	protected void enableButtonsForSelection() {
		Object selected = getList().getSelectedValue();
		m_viewButton.setEnabled(selected != null);
	}
	
	/** Select all items specified */
	@Override
	public void setSelectedTypes(Collection<MdmiDatatype> types) {
		// restore background
		if (m_highlightBox != null) {
			m_highlightBox.setBackground(getList().getBackground());
			m_highlightBox = null;
		}

		super.setSelectedTypes(types);

	}

	/** Open the selected item */
	protected void openSelection() {
		Object selection = getList().getSelectedValue();
		if (selection instanceof DataTypeCheckBox) {
			Object dataType = ((DataTypeCheckBox)selection).datatype;
			DefaultMutableTreeNode treeNode = SelectionManager.getInstance().getEntitySelector().findNode(dataType);
			if (treeNode != null) {
				// select it (this will expand if necessary)
				SelectionManager.getInstance().getEntitySelector().selectNode(treeNode);
				SelectionManager.getInstance().editItem(treeNode);
			}
		}
	}

	////////////////////////////////////////////////
	//  IEditorField methods

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public Object getValue() throws DataFormatException {
		return getSelectedTypes();
	}
	
	@Override
	public void highlightText(String text, Color highlightColor) {

		// restore background
		if (m_highlightBox != null) {
			m_highlightBox.setBackground(getList().getBackground());
			m_highlightBox = null;
		}
		
		//find checkbox with a data object that matches this text
		List<JCheckBox> checkBoxes = getCheckBoxes();
		for (int i=0; i<checkBoxes.size(); i++) {
			JCheckBox checkbox = checkBoxes.get(i);
			if (checkbox instanceof DataTypeCheckBox &&
					((DataTypeCheckBox)checkbox).datatype.toString().equals(text)) {
				
				checkbox.setBackground(highlightColor);
				m_highlightBox = checkbox;
				
				// scroll so that highlight is displayed
	         int idx1 = Math.max(0, i-1);
	         int idx2 = Math.min(getModel().getSize()-1, i+1);
	         Rectangle last = getList().getCellBounds(idx1, idx2);
	         getList().scrollRectToVisible(last);
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setDisplayValue(Object value) throws DataFormatException {
		m_invalidValues.clear();
		if (value instanceof Collection<?> ) {
			// verify, and add if not in list
			for (Object obj : (Collection<?>)value) {
				JCheckBox checkbox = findCheckBox((MdmiDatatype)obj);
				if (checkbox == null) {
					// not found - add to end
					checkbox = new DataTypeCheckBox((MdmiDatatype)obj);
					addCheckBox(checkbox);

					// scroll to bottom
					int idx = getModel().getSize() - 1;
					Rectangle last = getList().getCellBounds(idx, idx);
					getList().scrollRectToVisible(last);
					
					// mark as bad
					m_invalidValues.add(checkbox);
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
											checkbox.getText(), ClassUtil.beautifyName(fieldName)));
				}
			}
			
			setSelectedTypes((Collection<MdmiDatatype>) value);
		}
	}

	@Override
	public void setReadOnly() {
		getList().setEnabled(false);
	}			

	////////////////////////////////////////////////////////////////
	// List Selection listener
	private class SelectionListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting()) {
				return;
			}
			enableButtonsForSelection();
		}
		
	}
	
	////////////////////////////////////////////////
	// Custom Renderer
	private class ListRenderer extends CellRenderer {
		@Override
		public Component getListCellRendererComponent(
            JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
			Color background = list.getBackground();
			if (value == m_highlightBox) {
				// save color
				background = m_highlightBox.getBackground();
			}
			Component c = super.getListCellRendererComponent(list, value, index, 
					isSelected, cellHasFocus);

			// restore color
			if (value == m_highlightBox) {
				m_highlightBox.setBackground(background);
				
			} else if (m_invalidValues.contains(value)) {
				if (isSelected) {
					((JCheckBox)value).setBackground(Color.red.darker());
				} else {
					((JCheckBox)value).setForeground(Color.red);
				}
			}
			return c;
		}

	}

	/////////////////////////////////////
	// Action Listener Interface
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == m_viewButton) {
			openSelection();
		}
	}
}
