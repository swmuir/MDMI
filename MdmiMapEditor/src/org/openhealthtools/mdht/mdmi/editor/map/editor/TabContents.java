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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.console.LinkedObject;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNode;
import org.openhealthtools.mdht.mdmi.MdmiException;

/** The component that is shown in each tab of the EditorPanel */
public class TabContents extends JPanel {

	/** Resource for localization */
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.editor.Local");

	
	private JPanel  m_titleBar      = new JPanel(new GridBagLayout());
	private JLabel   m_className     = new JLabel(" ");
	private JButton  m_displayName   = new JButton(" ");
	
	private JButton m_acceptButton           = new JButton();
	private JButton m_closeButton          = new JButton();
	private JButton m_acceptAndCloseButton   = new JButton();

	private AbstractAction		m_findListener = new FindAction();
	private AbstractAction     m_acceptListener = new AcceptAction();
	private AbstractAction     m_closeListener = new CloseAction();
	private AbstractAction		m_acceptAndCloseListener = new AcceptAndCloseAction();
	
	private AbstractComponentEditor m_theEditor;
	private Object  m_theObject;
	
//	public TabContents(AbstractComponentEditor editor, Object entity) {
//		this(editor, entity, false);
//	}
	
	public TabContents(AbstractComponentEditor editor, Object entity, boolean readOnly) {
		super(new BorderLayout());
		
		// pass the entity to the editor
		editor.populateUI(entity);
		
		m_theEditor = editor;
		m_theObject = entity;
		
		
		add(m_titleBar, BorderLayout.NORTH);
		add((Component)editor, BorderLayout.CENTER);
		
		// Top: [class   name            [#][X]]
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.SOUTHWEST;	// bottom left
		gbc.gridx = 0;
		gbc.weightx = 1.0;
		gbc.insets = Standards.getInsets();
		
		// Class
		m_className.setText(getObjectType());
		Font biggerFont = m_className.getFont().deriveFont(14f);
		m_className.setFont(biggerFont);
		m_titleBar.add(m_className, gbc);
		
		// Name
		gbc.insets = new Insets(0,0,0,0);
		gbc.gridx++;
		// use magnifying glass icon
		m_displayName.setIcon(getIcon(s_res.getString("AdvancedSelectionField.viewIcon")));
		m_displayName.setFont(biggerFont.deriveFont(Font.ITALIC));
		m_titleBar.add(m_displayName, gbc);
		
		// Accept/Close Buttons
		createButtons();
		gbc.anchor = GridBagConstraints.SOUTHEAST;	// bottom right
		gbc.weightx = 0.0;
		gbc.insets = new Insets(0,0,0,0);
		gbc.gridx++;
		m_titleBar.add(m_acceptButton, gbc);
		gbc.gridx++;
		m_titleBar.add(m_acceptAndCloseButton, gbc);
		gbc.gridx++;
		m_titleBar.add(m_closeButton, gbc);
		
		if (readOnly) {
			// hide buttons
			m_acceptButton.setVisible(false);
			m_acceptAndCloseButton.setVisible(false);
		}
		
		m_titleBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.DARK_GRAY));
		
		setDisplayName(entity);
	}
	
	public Object getObjectBeingEdited() {
		return m_theObject;
	}
	
	/** Return a string representing the object type (class) */
	public String getObjectType() {
		return ClassUtil.beautifyName(m_theObject.getClass());
	}
	
	public AbstractComponentEditor getEditor() {
		return m_theEditor;
	}
	
	@Override
	public String toString() {
		return m_theEditor.getModelName(m_theObject);
	}
	
	/** Create buttons */
	private void createButtons() {
		Insets insets = m_acceptButton.getInsets();
		Icon icon = getIcon(s_res.getString("TabContents.acceptIcon"));
		m_acceptButton.setIcon(icon);
		m_acceptButton.setToolTipText(s_res.getString("TabContents.acceptToolTip"));
		m_acceptButton.setPreferredSize(new Dimension(icon.getIconWidth()+insets.top+insets.bottom,
				icon.getIconHeight()+insets.top+insets.bottom));
		
		icon = new AcceptAndCloseIcon(icon);
		m_acceptAndCloseButton.setIcon(icon);
		m_acceptAndCloseButton.setPreferredSize(new Dimension(icon.getIconWidth()+insets.top+insets.bottom,
				icon.getIconHeight()+insets.top+insets.bottom));
		m_acceptAndCloseButton.setToolTipText(s_res.getString("TabContents.acceptAndCloseToolTip"));
		
		icon = getIcon(s_res.getString("TabContents.closeIcon"));
		m_closeButton.setIcon(icon);
		m_closeButton.setPreferredSize(new Dimension(icon.getIconWidth()+insets.top+insets.bottom,
				icon.getIconHeight()+insets.top+insets.bottom));
		m_closeButton.setToolTipText(s_res.getString("TabContents.closeToolTip"));
	}
	
	@Override
	public void addNotify() {
		super.addNotify();
		m_displayName.addActionListener(m_findListener);
		// Show this <object> in the tree
		m_displayName.setToolTipText(MessageFormat.format(s_res.getString("TabContents.showObjectToolTip"),
				getObjectType()));
		m_acceptButton.addActionListener(m_acceptListener);
		m_acceptAndCloseButton.addActionListener(m_acceptAndCloseListener);
		m_closeButton.addActionListener(m_closeListener);		
	}

	@Override
	public void removeNotify() {
		m_displayName.removeActionListener(m_findListener);
		m_displayName.setToolTipText(null);
		m_acceptButton.removeActionListener(m_acceptListener);
		m_acceptAndCloseButton.removeActionListener(m_acceptAndCloseListener);
		m_closeButton.removeActionListener(m_closeListener);

		super.removeNotify();
	}


	/** Set/Change the title shown on the display
	 * @param entity
	 */
	public void setDisplayName(Object entity) {
		setDisplayName(m_theEditor.getModelName(entity));
	}
	
	/** Set the name shown in the title bar */
	public void setDisplayName(String objectName) {
		m_displayName.setText(objectName);
		m_displayName.revalidate();
	}
	
	public String getDisplayName() {
		return m_displayName.getText().trim();
	}

	/** create an icon */
	public Icon getIcon(String iconPath) {
		Icon icon = null;
		URL url = getClass().getResource(iconPath);
		if (url != null) {
			icon = new ImageIcon(url);
		}
		return icon;
	}
	
	/** Check if there are changes that need to be accepted */
	public boolean isModified() {
		return m_theEditor.isModified();
	}
	
	/** Close this session. Checks and accepts first */
	public void closeEditor() {
		boolean okayToClose = true;
		
		// check for modifications
		if (isModified()) {
			okayToClose = false;
			// Item has been modified. accept changes?
			//    [Yes] [No] [Cancel]
			String message = s_res.getString("TabContents.closeMessage");
			String title = s_res.getString("TabContents.closeTitle");
			int confirm = JOptionPane.showConfirmDialog(TabContents.this, message, title,
					JOptionPane.YES_NO_CANCEL_OPTION);
			
			if (confirm == JOptionPane.YES_OPTION) {
				boolean accepted = acceptEdits();
				// if accept failed, or was cancelled, revert
				if (!accepted) {
					m_theEditor.revertModel();
				}
				okayToClose = true;
				
			} else if (confirm == JOptionPane.NO_OPTION) {
				// revert
				m_theEditor.revertModel();
				okayToClose = true;
			}
		}
		
		if (okayToClose) {
			Container parent = getParent();
			parent.remove(this);
		}
	}
	
	/** accept edits (in memory) */
	public boolean acceptEdits() {
		
		boolean editsaccepted = false;
		
		SelectionManager selectionManager = SelectionManager.getInstance();
		try {
			Object updatedEntity = m_theEditor.getUpdatedModel();
			
			// check validity
			if (updatedEntity != null) {
				m_theObject = updatedEntity;
				
				editsaccepted = true;
				
				// indicate that there are un-saved changes
				selectionManager.setUpdatesPending();

				// update tree to show new name (may need to re-order)
				DefaultMutableTreeNode node = 
					selectionManager.getEntitySelector().refreshUserObject(m_theObject);
				
				// update display to show new name
				selectionManager.getEntityEditor().refreshEditDisplay(((EditableObjectNode)node));

				String message = MessageFormat.format(s_res.getString("TabContents.acceptComplete"), 
						getObjectType(), toString());
				selectionManager.writeToConsole(message);

				// notify listeners
				selectionManager.notifyModelChangeListeners(m_theObject);
				selectionManager.notifyCollectionChangeListeners(m_theObject.getClass());
			}
			
		} catch (Exception ex) {
			selectionManager.writeError("Error saving entity, ", 
					new LinkedObject(m_theObject, toString()), 
					MdmiException.getFullDescription(ex));
			
		}
		
		return editsaccepted;
	}
	
	private class FindAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			// find element in tree
			DefaultMutableTreeNode treeNode = SelectionManager.getInstance().getEntitySelector().findNode(m_theObject);
			if (treeNode != null) {
				// select it (this will expand if necessary)
				SelectionManager.getInstance().getEntitySelector().selectNode(treeNode);
			}
		}
	}
	
	/** Button listeners */
	private class AcceptAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent event) {
			// clear error display
			SelectionManager.getInstance().getStatusPanel().clearErrors();
			acceptEdits();
		}
	}

	/** Button listeners */
	private class CloseAction extends AbstractAction {// ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			closeEditor();
		}
	}
	
	/** Button listeners */
	private class AcceptAndCloseAction extends AbstractAction {// ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			if (acceptEdits()) {
				closeEditor();
			}
		}
	}
	
	/** Hybrid icon with a provided icon, over-layed with a large red X */
	private static class AcceptAndCloseIcon implements Icon {
		private static String xMarker = "X";
		
		private Icon m_acceptIcon;
		
		public AcceptAndCloseIcon(Icon acceptIcon) {
			m_acceptIcon = acceptIcon;
		}

		@Override
		public int getIconHeight() {
			return m_acceptIcon.getIconHeight();
		}

		@Override
		public int getIconWidth() {
			return m_acceptIcon.getIconWidth();
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			m_acceptIcon.paintIcon(c, g, x, y);

			// red X 
			g.setColor(Color.red);
			Font font = c.getFont().deriveFont(Font.BOLD + Font.ITALIC, 14);
			g.setFont(font);
			FontMetrics fm = c.getFontMetrics(font);
			// bottom right
			int x1 = x + (getIconWidth() - fm.stringWidth(xMarker));
			int y1 = y + getIconHeight();
			g.drawString(xMarker, x1, y1);
		}

	}

}
