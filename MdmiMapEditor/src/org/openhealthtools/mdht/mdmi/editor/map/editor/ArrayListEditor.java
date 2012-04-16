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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;

/** An IEditorField for an ArrayList of objects, each of which requires its own editor */
public class ArrayListEditor extends JPanel implements IEditorField,
		ActionListener, PropertyChangeListener {
	
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.editor.Local");

	private GenericEditor m_parentEditor;
	private ArrayList<Object> m_arrayList = null;
	private Class<?> m_objectClass;

	private List<GenericEditor> m_editorList = new ArrayList<GenericEditor>();

	private JButton m_addButton = new JButton("+");
	private JPanel  m_editorDisplay = new JPanel(new GridBagLayout());
	private JScrollPane m_scroller = new JScrollPane();
	private GridBagConstraints m_gbc = new GridBagConstraints();
	
	public ArrayListEditor(GenericEditor parentEditor, Class<?> objectClass, String fieldName) {
		setLayout(new BorderLayout());
		
		m_parentEditor = parentEditor;
		m_objectClass = objectClass;

		// Add a button for creating a new entry. It will be added to the end
		m_addButton = new JButton("Add " + objectClass.getSimpleName());
		Icon icon = AbstractComponentEditor.getIcon(getClass(), s_res.getString("GenericEditor.addIcon"));
		m_addButton.setIcon(icon);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.add(m_addButton);
		add(buttonPanel, BorderLayout.NORTH);
		
		// initialize layout parameters
		m_gbc.insets = Standards.getInsets();
		m_gbc.weightx = 1;
		m_gbc.weighty = 1;
		m_gbc.gridx = 0;
		m_gbc.gridy = 0;
		
		
		m_scroller.setViewportView(m_editorDisplay);
		add(m_scroller, BorderLayout.CENTER);
		m_scroller.setPreferredSize(new Dimension(200,200));
		
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), fieldName));
	}
	
	@Override
	public void addNotify() {
		super.addNotify();

		m_addButton.addActionListener(this);
	}
	
	@Override
	public void removeNotify() {
		m_addButton.removeActionListener(this);
		super.removeNotify();
	}
	


	/** Action Listener */
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == m_addButton) {
			// add a new one to the end
			addNewElement();
			
		} else if (source instanceof JButton) {
			// delete button - remove corresponding editor
			Point p = ((JButton)source).getLocation();
			int idx = -1;
			for (int i=0; i<m_editorList.size(); i++) {
				GenericEditor editor = m_editorList.get(i);
				Rectangle editorBounds = editor.getBounds();
				// check y-coordinate
				if (p.y >= editorBounds.y && p.y <= (editorBounds.y+editorBounds.height)) {
					// found the one
					idx = i;
					break;
				}
			}
			
			if (idx != -1) {
				removeElement(idx);
			}
			
		}
		m_parentEditor.setModified(true);
	}
	
	/** Create and add a new object to the display */
	public void addNewElement() {
		try {
			Object entry = m_objectClass.newInstance();
			final GenericEditor ed = addElement(entry);
			m_editorDisplay.revalidate();
			
			
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					Rectangle edBounds = ed.getBounds();
					m_scroller.getViewport().setViewPosition(new Point(edBounds.x, edBounds.y));
				}
			});
			
		} catch (Exception ex) {
			// Unable to create a new {0}
			String msg = MessageFormat.format(s_res.getString("GenericEditor.creationErrorFormat"),
					m_objectClass);
			SelectionManager.getInstance().getStatusPanel().writeException(msg, ex);
		}
	}
	
	/** remove the editor */
	public void removeElement(int idx) {
		m_editorList.remove(idx);
		
		// re-layout all editors
		m_editorDisplay.removeAll();
		for (GenericEditor ed : m_editorList) {
			addEditorToDisplay(ed);
		}
		m_editorDisplay.revalidate();
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
		m_arrayList.clear();
		for (GenericEditor ed : m_editorList) {
			Object value = ed.getUpdatedModel();
			m_arrayList.add(value);
		}
		return m_arrayList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setDisplayValue(Object value) throws DataFormatException {
		if (value instanceof ArrayList) {
			m_arrayList = (ArrayList<Object>)value;
			
			// create elements
			m_editorList.clear();
			m_editorDisplay.removeAll();
			for (Object elementInList : m_arrayList) {
				addElement(elementInList);
			}
			
		} else {
			// '{0}' is not a {1}.
			throw new DataFormatException(MessageFormat.format(s_res.getString("GenericEditor.dataFormatExceptionFormat"),
					value, "ArrayList"));
		}
		revalidate();	
	}
	
	@Override
	public void setReadOnly() {
		for (GenericEditor ed : m_editorList) {
			ed.setReadOnly();
		}
	}

	/** Create a new editor for this element, and add it to the end of the list
	 * @param elementInList
	 */
	private GenericEditor addElement(Object elementInList) {
		GenericEditor ed = new CustomGenericEditor(m_parentEditor.getMessageGroup(), 
				m_objectClass);
		ed.populateUI(elementInList);
		addEditorToDisplay(ed);
		m_editorList.add(ed);
		
		return ed;
	}

	/** add editor to the display
	 * @param elementInList
	 */
	private void addEditorToDisplay(GenericEditor ed) {
		ed.setBorder(BorderFactory.createEtchedBorder());

		m_gbc.fill = GridBagConstraints.HORIZONTAL;
		m_gbc.weightx = 1;
		m_editorDisplay.add(ed, m_gbc);
		
		// add a delete button
		m_gbc.gridx++;
		
		JButton deleteButton = new DeleteButton();

		m_gbc.fill = GridBagConstraints.NONE;
		m_gbc.weightx = 0;
		m_gbc.insets.left = 0;
		m_editorDisplay.add(deleteButton, m_gbc);

		m_gbc.insets.left = Standards.LEFT_INSET;
		m_gbc.gridx = 0;
		m_gbc.gridy++;
	}

	@Override
	public void highlightText(String text, Color highlightColor) {
		// TODO Auto-generated method stub
		
	}


	/////////////////////////////////
	
	private class CustomGenericEditor extends GenericEditor {

		public CustomGenericEditor(MessageGroup group, Class<?> objectClass) {
			super(group, objectClass, false);
		}

		@Override
		public void addNotify() {
			super.addNotify();
			addPropertyChangeListener(ArrayListEditor.this);
		}
		
		@Override
		public void removeNotify() {
			removePropertyChangeListener(ArrayListEditor.this);
			super.removeNotify();
		}
	}
	
	private class DeleteButton extends JButton {

		public DeleteButton() {
			Icon icon = AbstractComponentEditor.getIcon(getClass(),
					s_res.getString("GenericEditor.deleteIcon"));
			setIcon(icon);
		}

		@Override
		public void addNotify() {
			super.addNotify();
			addActionListener(ArrayListEditor.this);
		}
		
		@Override
		public void removeNotify() {
			removeActionListener(ArrayListEditor.this);
			super.removeNotify();
		}
	}

}
