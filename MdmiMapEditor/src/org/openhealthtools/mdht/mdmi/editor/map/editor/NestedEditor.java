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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;

/** An IEditorField that uses a GenericEditor for use inside another editor component */
public class NestedEditor extends JPanel implements IEditorField,
		ActionListener, PropertyChangeListener {
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.editor.Local");

	private static Icon s_addIcon = AbstractComponentEditor.getIcon(NestedEditor.class,
			s_res.getString("GenericEditor.addIcon"));
	private static Icon s_deleteIcon = AbstractComponentEditor.getIcon(NestedEditor.class,
			s_res.getString("GenericEditor.deleteIcon"));
	
	private JButton m_addDeleteButon = AbstractComponentEditor.createIconButton(s_addIcon);

	private GenericEditor m_parentEditor;
	private GenericEditor m_editor;
	private Object m_entity = null;
	
	
	public NestedEditor(GenericEditor parentEditor, Class<?> objectClass, String fieldName) {
		setLayout(new BorderLayout());
		
		m_parentEditor = parentEditor;
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.add(m_addDeleteButon);
		buttonPanel.add(new JLabel(fieldName));
		add(buttonPanel, BorderLayout.NORTH);
		
		m_editor = new GenericEditor(parentEditor.getMessageGroup(), objectClass, false);
		m_editor.setVisible(false);
		add(m_editor, BorderLayout.CENTER);
	}
	
	@Override
	public void addNotify() {
		super.addNotify();
		
		String className = m_editor.getObjectClass().getSimpleName();
		String toolTip = MessageFormat.format((m_entity == null) ?
				s_res.getString("NestedEditor.addToolTipFormat") :
					s_res.getString("NestedEditor.delToolTipFormat"), className);
		m_addDeleteButon.setToolTipText(toolTip);
		m_addDeleteButon.addActionListener(this);
		// we'll listen for DATA_MODIFIED events to indicate that the object has been changed
		m_editor.addPropertyChangeListener(AbstractComponentEditor.DATA_MODIFIED, this);
	}
	
	@Override
	public void removeNotify() {
		m_addDeleteButon.setToolTipText(null);
		m_addDeleteButon.removeActionListener(this);
		m_editor.removePropertyChangeListener(AbstractComponentEditor.DATA_MODIFIED, this);
		super.removeNotify();
	}
	
	public GenericEditor getEditor() {
		return m_editor;
	}



	/** Action Listener */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (m_entity != null) {
			// remove the object
			m_entity = null;

		} else {
			// create a new object
			try {
				m_entity = m_editor.getObjectClass().newInstance();
			} catch (Exception ex) {

				// Unable to create a new {0}
				String msg = MessageFormat.format(s_res.getString("GenericEditor.creationErrorFormat"),
						m_editor.getObjectClass());
				SelectionManager.getInstance().getStatusPanel().writeException(msg, ex);
			}
		}
		
		// update the display to show that the item is new or removed
		try {
			setDisplayValue(m_entity);
		} catch (DataFormatException ex) {
			SelectionManager.getInstance().getStatusPanel().writeException(ex);
		}
		m_parentEditor.setModified(true);
	}

	/** Property Change Listener (propegate changes to editor) */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		m_parentEditor.setModified(true);
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public Object getValue() throws DataFormatException {
		if (m_entity != null) {
			return m_editor.getUpdatedModel();
		} else {
			return null;
		}
	}

	@Override
	public void setDisplayValue(Object value) throws DataFormatException {

		String className = m_editor.getObjectClass().getSimpleName();
		String toolTip = MessageFormat.format((value == null) ?
				s_res.getString("NestedEditor.addToolTipFormat") :
					s_res.getString("NestedEditor.delToolTipFormat"), className);
		m_addDeleteButon.setToolTipText(toolTip);
		
		if (value != null) {
			m_editor.populateUI(value);
			
			m_editor.setVisible(true);
			m_addDeleteButon.setIcon(s_deleteIcon);
		} else {
			m_editor.setVisible(false);
			m_addDeleteButon.setIcon(s_addIcon);
		}
		m_entity = value;
		revalidate();	
	}
	
	@Override
	public void setReadOnly() {
		m_editor.setReadOnly();
	}
	
	
	@Override
	public void highlightText(String text, Color highlightColor) {
		// does nothing
	}
}
