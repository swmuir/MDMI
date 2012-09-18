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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNode;

/** 
 * Editor Panel that contains a tabbed pane to allow editing selections from the tree
 * @author Conway
 *
 */
public class EditorPanel extends JPanel {
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.editor.Local");

	// action names for key strok bindings
	private static final String ACCEPT_ACTION  = "accept";
	private static final String CLOSE_ACTION = "close";

	// Accept key stroke (ctrl S)
	private static final KeyStroke CTRL_S = 
		KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK);
	// Close key stroke (ctrl W)
	private static final KeyStroke CTRL_W = 
		KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK);


	// Components
	private JPanel      m_messageArea  = new JPanel(new FlowLayout());
	private JTabbedPane m_activeEdits  = new JTabbedPane();
	
	// Tabs
	private HashMap<Object, TabContents> m_entityMap = new HashMap<Object, TabContents>();
	
	// Listeners
	private ComponentRemovedListener m_componentRemovedListener = new ComponentRemovedListener();
	private TabChangeListener  m_tabChangeListener = new TabChangeListener();
	private DataChangeListener m_dataChangeListener = new DataChangeListener();

	private AbstractAction     m_acceptListener = new AcceptAction();
	private AbstractAction     m_closeListener = new CloseAction();
	
	// allow editing of "imported" items
	private boolean m_allowEditingImports = false;
	
	@Override
	public void addNotify() {
		super.addNotify();
		addEditPanelListener(m_componentRemovedListener);
		m_activeEdits.addChangeListener(m_tabChangeListener);
	
		//Add bindings for Accept (Ctrl S)
		getInputMapForActions().put(CTRL_S, ACCEPT_ACTION);
		getActionMap().put(ACCEPT_ACTION, m_acceptListener);
		getInputMapForActions().put(CTRL_W, CLOSE_ACTION);
		getActionMap().put(CLOSE_ACTION, m_closeListener);

	}

	/** Get the input map for handling actions on this component
	 * @return
	 */
	private InputMap getInputMapForActions() {
		return getInputMap(WHEN_IN_FOCUSED_WINDOW);
	}


	@Override
	public void removeNotify() {
		removeEditPanelListener(m_componentRemovedListener);
		m_activeEdits.removeChangeListener(m_tabChangeListener);

		getInputMapForActions().remove(CTRL_S);
		getActionMap().remove(ACCEPT_ACTION);
		getInputMapForActions().remove(CTRL_W);
		getActionMap().remove(CLOSE_ACTION);

		super.removeNotify();
	}
	
	/** Add a listener for additions/removals of edit panels */
	public void addEditPanelListener(ContainerListener listener) {
		m_activeEdits.addContainerListener(listener);
	}


	/** remove a listener for additions/removals of edit panels */
	public void removeEditPanelListener(ContainerListener listener) {
		m_activeEdits.removeContainerListener(listener);
	}

	// allow editing of "imported" items
	public void setAllowEditingImports(boolean flag) {
		m_allowEditingImports = flag;
	}
	
	// can imported items be edited?
	public boolean isAllowEditingImports() {
		return m_allowEditingImports;
	}
	
	/**
	 * <code>
	 *    -------------------------------
	 *   |  message area                 |
	 *   |-------------------------------|
	 *   |_[Tab 1] |Tab 2| |Tab 3|_______|
	 *   |                               |
	 *   |       active edits            |
	 *   |                               |
	 *    -------------------------------
	 * </code>
	 */
	public EditorPanel() {
		setLayout(new BorderLayout());
		add(m_messageArea, BorderLayout.NORTH);
		add(m_activeEdits, BorderLayout.CENTER);
	}
	
	/** Refresh the edit panel for this entity with new data */
	public void refreshEditDisplay(EditableObjectNode entityNode) {
		Object entity = entityNode.getUserObject();
		TabContents editPanel = m_entityMap.get(entity);
		if (editPanel != null) {
			
			// change display title
			editPanel.setDisplayName(entity);

			// change tab title and icon
			int idx = m_activeEdits.indexOfComponent(editPanel);
			setTitleFor(idx, editPanel);

			Icon icon = entityNode.getNodeIcon();
			m_activeEdits.setIconAt(idx, icon);
		}
	}
	
	
	private void setTitleFor(AbstractComponentEditor editor) {
		// Find it
		for (int idx = 0; idx < m_activeEdits.getComponentCount(); idx++) {
			TabContents editPanel = (TabContents)m_activeEdits.getComponentAt(idx);
			if (editPanel.getEditor() == editor) {
				setTitleFor(idx, editPanel);
				break;
			}
		}
	}

	/** Change the title for the EditPanel at this location
	 * @param idx
	 * @param contentPanel
	 */
	private void setTitleFor(int idx, TabContents contentPanel) {
		if (idx != -1) {
			String title = contentPanel.getDisplayName();
			if (contentPanel.isModified()) {
				title = "*" + title;
			}

			m_activeEdits.setTitleAt(idx, title);
			SelectionManager.getInstance().enableActionsForSelection();
		}
	}
	
	/** Add a new tab containing this entity. If the entity is already being shown,
	 * make it active.
	 * @param entity
	 */
	public TabContents editEntity(EditableObjectNode entityNode) {
		Object entity = entityNode.getUserObject();
		TabContents editPanel = m_entityMap.get(entity);
		
		if (editPanel == null || editPanel.getParent() == null) {
			// Get the editor from the node
			AbstractComponentEditor ed = entityNode.getEditorForNode();
			
			// if imported, make read-only
			boolean isReadOnly = false;
			if (!m_allowEditingImports) {
				if (entityNode.isImported() && ed instanceof GenericEditor) {
					((GenericEditor)ed).setReadOnly();
					isReadOnly = true;
				}
			}
			
			// Register for property change so we can update the title
			ed.addPropertyChangeListener(AbstractComponentEditor.DATA_MODIFIED,
						m_dataChangeListener);
			
			editPanel = new TabContents(ed, entity, isReadOnly);
			m_entityMap.put(entity, editPanel);
			Icon icon = entityNode.getNodeIcon();
			m_activeEdits.addTab(entityNode.getDisplayName(), icon, editPanel);

		}
		
		// bring to front
		if (editPanel != null) {
			m_activeEdits.setSelectedComponent(editPanel);
			editPanel.requestFocus();
		}
		
		return editPanel;
	}
	
	/** Bring the tab that contains this object to the front */
	public void showEditPanel(Object entity) {
		TabContents editPanel = m_entityMap.get(entity);
		if (editPanel != null) {
			m_activeEdits.setSelectedComponent(editPanel);
		}
	}
	
	/** Does this entity have unaccepted changes */
	public boolean isModified(Object entity) {
		TabContents editor = m_entityMap.get(entity);
		if (editor != null) {
			return editor.isModified();
		}
		return false;
	}
	
	/** Is this entity currently being edited */
	public boolean isOpen(Object entity) {
		TabContents editor = m_entityMap.get(entity);
		return (editor != null);
	}
	
	/** Does any entity have unaccepted changes */
	public boolean isAnyEntityModified() {
		for (TabContents editor : m_entityMap.values()) {
			if (editor.isModified()) {
				return true;
			}
		}
		return false;
	}
	
	/** Get the selected tab
	 * @return
	 */
	public TabContents getSelection() {
		return (TabContents)m_activeEdits.getSelectedComponent();
	}

	
	/** Accept edits on current window */
	public boolean acceptSelectedEdits() {
		TabContents editor = getSelection();
		if (editor != null) {
			return editor.acceptEdits();
		}
		return true;
	}

	
	/** Accept any edits on this entity
	 * @param entity
	 */
	public boolean acceptEdits(Object entity) {
		TabContents editor = m_entityMap.get(entity);
		if (editor != null) {
			return editor.acceptEdits();
		}
		return true;	// nothing to accept
	}
	
	/** Accept all open edits
	 * @param entity
	 * @return	true if all changes are accepted. If any entity fails, the operation will fail
	 */
	public boolean acceptAllEdits() {
		// copy map since saving edits changes the map
		ArrayList <TabContents> edits = new ArrayList<TabContents> (m_entityMap.values()); 
		for (TabContents editor : edits) {
			if (editor.isModified()) {
				boolean accepted = editor.acceptEdits();
				if (!accepted) {
					SelectionManager.getInstance().getStatusPanel().writeErrorText("Error saving changes");
					return false;	// error should already be shown
				}
			}
		}
		return true;
	}

	
	/** close current editor window */
	public void closeSelectedEditor() {
		TabContents editor = getSelection();
		if (editor != null) {
			editor.closeEditor();
		}

	}
	
	/** Remove the tab containing this entity. If the entity does not exist, nothing will happen
	 * @param entity
	 */
	public void stopEditing(Object entity) {
		Component editor = m_entityMap.get(entity);
		if (editor != null) {
			m_entityMap.remove(entity);
			m_activeEdits.remove(editor);
		}
	}
	
	/** Listen for components removing themselves */
	private class ComponentRemovedListener extends ContainerAdapter {
		@Override
		public void componentRemoved(ContainerEvent event) {
			if (event.getChild() instanceof TabContents) {
				TabContents editPanel = (TabContents)event.getChild();
				// remove it from table
				for (Object entity : m_entityMap.keySet()) {
					TabContents editor = m_entityMap.get(entity);
					if (editor == editPanel) {
						// remove entity from map
						m_entityMap.remove(entity);
						// remove listener from editor
						if (editor.getEditor() instanceof AbstractComponentEditor) {
							((AbstractComponentEditor)editor.getEditor()).removePropertyChangeListener(AbstractComponentEditor.DATA_MODIFIED,
									m_dataChangeListener);
						}
						break;
					}
				}
			}
		}
	}
	
	/** Listener when a tab is selected */
	private class TabChangeListener implements ChangeListener {
		private TabContents  m_prevSelection = null;
		
		private TabChangeListener() {
			m_prevSelection = getSelection();
		}

		@Override
		public void stateChanged(ChangeEvent event) {
			TabContents newSelection = getSelection();
			if (m_prevSelection != null) {
				// check previous panel for changes, and update title
				int idx = m_activeEdits.indexOfComponent(m_prevSelection);
				setTitleFor(idx, m_prevSelection);
			}
			m_prevSelection = newSelection;
			
			// show in tree
			if (newSelection != null) {
				DefaultMutableTreeNode treeNode = SelectionManager.getInstance().getEntitySelector().findNode(newSelection.getObjectBeingEdited());
				if (treeNode != null) {
					// select it (this will expand if necessary)
					SelectionManager.getInstance().getEntitySelector().selectNode(treeNode);
				}
			}
		}
	}

	
	/** Data Change Listener */
	private class DataChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getSource() instanceof AbstractComponentEditor) {
				// update title
				setTitleFor((AbstractComponentEditor)event.getSource());
			}
		}
		
	}

	/** Button listeners */
	private class AcceptAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent event) {
			// clear error display
			SelectionManager.getInstance().getStatusPanel().clearErrors();
			acceptSelectedEdits();
		}
	}

	/** Button listeners */
	private class CloseAction extends AbstractAction {// ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			closeSelectedEditor();
		}
	}

}
