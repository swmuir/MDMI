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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.common.components.CursorManager;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.StatusPanel;
import org.openhealthtools.mdht.mdmi.editor.map.console.ReferenceLink;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MdmiModelTree;
import org.openhealthtools.mdht.mdmi.editor.map.tree.NewObjectInfo;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SemanticElementNode;
import org.openhealthtools.mdht.mdmi.model.MessageModel;
import org.openhealthtools.mdht.mdmi.model.MessageSyntaxModel;
import org.openhealthtools.mdht.mdmi.model.Node;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.SemanticElementSet;

/** An IEditorField that shows SemanticElement values in a ComboBox */
public class SemanticElementField extends AdvancedSelectionField {
	
	public static final String FLAT = s_res.getString("SemanticElementField.flatPresentation");
	public static final String HIERARCHICAL = s_res.getString("SemanticElementField.hierarchicalPresentation");

	private JComboBox   m_presentationBox;
	private boolean m_showHierarchy = false;
	
	private Node m_node = null;
	private MessageModel m_messageModel = null;
	
	public SemanticElementField(GenericEditor parentEditor) {
		super(parentEditor);
		m_showHierarchy = false;	// for now
		// show the "create" button, but disable it until a node is set
		showCreateButton(true);
		getCreateButton().setEnabled(false);
		
		//  add presentation box
		m_presentationBox = new JComboBox();
		m_presentationBox.addItem(FLAT);
		m_presentationBox.addItem(HIERARCHICAL);

		GridBagConstraints gbc = GetGridBagConstraints();
		gbc.gridx++;
		add(m_presentationBox, gbc);
	}
	
	/** Define node context */
	public void setNode(Node node) {
		m_node = node;
		MessageSyntaxModel msgModel = m_node.getSyntaxModel();
		if (msgModel != null) {
			setMessageModel( msgModel.getModel() );
		}
	}
	
	/** Define a single message model */
	public void setMessageModel(MessageModel messageModel) {
		m_messageModel = messageModel;
		// now we're allowed to enable the "new" button
		if (messageModel != null && getCreateButton().isVisible())
		{
			getCreateButton().setEnabled(true);
		}
	}
	
	@Override
	public void setReadOnly() {
		// hide Flat/Hierarchical button
		m_presentationBox.setVisible(false);
		super.setReadOnly();
	}

	// toggle the showHierarchy flag for output
	public void setShowHierarchy(boolean show) {
		m_showHierarchy = show;
		m_presentationBox.setSelectedItem(show ? HIERARCHICAL : FLAT);
	}


	@Override
	protected Collection<? extends Object> getComboBoxData() {
		// Find all the semantic elements
		ArrayList<SemanticElement> elements = new ArrayList<SemanticElement>();
		List<DefaultMutableTreeNode> semanticElementNodes = 
			SelectionManager.getInstance().getEntitySelector().getNodesOfType(SemanticElementNode.class);
		
		for (DefaultMutableTreeNode treeNode : semanticElementNodes) {
			SemanticElement element = (SemanticElement)treeNode.getUserObject();
			if (element.getName() != null && element.getName().length() > 0) {
				elements.add(element);
			}
		}

		// these should be sorted by something useful
		Collections.sort(elements, new Comparator<SemanticElement>() {
			@Override
			public int compare(SemanticElement o1, SemanticElement o2) {
				// sort by name (including parent)
				String v1 = makeString(o1, m_showHierarchy);
				String v2 = makeString(o2, m_showHierarchy);
				int c = v1.compareToIgnoreCase(v2);
				return c;
			}
		});
			
		List<Object> data = new ArrayList<Object>();
		data.addAll(elements);
		// make first item blank
		data.add(0, BLANK_ENTRY);
		return data;
	}


	@Override
	public void addNotify() {
		super.addNotify();
		getViewButton().setToolTipText(s_res.getString("SemanticElementField.viewToolTip"));
		getCreateButton().setToolTipText(s_res.getString("SemanticElementField.createToolTip"));
		m_presentationBox.addActionListener(this);
		m_presentationBox.setRenderer(new PresentationBoxRenderer());
	}

	@Override
	public void removeNotify() {
		getViewButton().setToolTipText(null);
		getCreateButton().setToolTipText(null);
		super.removeNotify();
		m_presentationBox.removeActionListener(this);
		m_presentationBox.setRenderer(null);
	}

	
	/** Create a new item */
	@Override
	protected void createNewItem() {

		CursorManager cm = CursorManager.getInstance(this);
		cm.setWaitCursor();
		try {
			// look for a "Name" field
			String defaultName = null;
			Container parent = getParent();
			while (parent != null) {
				if (parent instanceof GenericEditor) {
					IEditorField editorField = ((GenericEditor)parent).getEditorField("Name");
					if (editorField != null) {
						try {
							defaultName = editorField.getValue().toString();
							break;
							
						} catch (DataFormatException e) {
						}
					}
				}
				parent = parent.getParent();
			}
			NewSemanticElementDialog dialog = new NewSemanticElementDialog(SystemContext.getApplicationFrame());
			dialog.setNameField(defaultName);
			if (dialog.display(this) == BaseDialog.OK_BUTTON_OPTION) {
				SemanticElement se = dialog.getSemanticElement();				
				// set it
				setDisplayValue(se);
			}
			
		} finally {
			cm.restoreCursor();
		}
	}

	@Override
	public Class<?> getDataClass() {
		return SemanticElement.class;
	}
	
	/** Check for parent loop A->B->C->D->E->C->D->E.... */
	public static boolean hasParentLoop(SemanticElement element, boolean showError)
	{
		ArrayList <SemanticElement> heirarchy = new ArrayList<SemanticElement>();
		heirarchy.add(element);
		
		// check parent
		SemanticElement parent = element.getParent();
		while (parent != null) {
			if (heirarchy.contains(parent)) {
				// found a loop
				if (showError) {
					// Semantic Element Loop: Semantic Element <LINK> has a parent that references back to <this>'
					ReferenceLink link = new ReferenceLink(element, element.getName());
					link.addReferredToObject(parent);
					StatusPanel statusPanel = SelectionManager.getInstance().getStatusPanel();
					String preMsg = "Semantic Element Loop: Semantic Element";
					StringBuilder postMsg = new StringBuilder("has a loop in its parent hierarchy -");
					for (SemanticElement elem : heirarchy) {
						postMsg.append(elem.getName());
						postMsg.append("->");
					}
					postMsg.append(parent.getName());

					statusPanel.writeErrorLink(preMsg, link, postMsg.toString());
				}
				
				return true;
			}
			heirarchy.add(0, parent);
			
			parent = parent.getParent();
		}
		return false;
	}
	
	/** Create a string from the SE's name */
	public static String makeString(SemanticElement element) {
		return makeString(element, false);
	}

	/** Create a string from the SE's name, and the name of its parent(s) */
	public static String makeString(SemanticElement element, boolean showParent) {
		StringBuilder buf = new StringBuilder();
		if (showParent) {
			ArrayList <SemanticElement> heirarchy = new ArrayList<SemanticElement>();
			heirarchy.add(element);
			
			// check parent
			element = element.getParent();
			while (element != null && !heirarchy.contains(element)) {	// avoid cycles
				heirarchy.add(0, element);
				
				element = element.getParent();
			}
			
			// show path to element
			for (SemanticElement se : heirarchy) {
				if (buf.length() > 0) {
					buf.append(".");
				}
				buf.append(se.getName());
			}
		} else {
			buf.append(element.getName());
		}
		
		return buf.toString();
	}
	
	/** Convert an object in the list to a string */
	@Override
	protected String toString(Object listObject) {
		if (listObject instanceof SemanticElement) {
			return makeString((SemanticElement)listObject, m_showHierarchy);
		}
		return listObject.toString();
	}
	
	/** Get a tooltip for an item in the list */
	@Override
	protected String getToolTipText(Object listObject) {
		if (listObject instanceof SemanticElement) {
			SemanticElement element = (SemanticElement)listObject;
			return element.getDescription();
		}
		return null;
	}
	


	private void changePresentation() {
		boolean showHierarchy = false;
		if (m_presentationBox.getSelectedItem() == HIERARCHICAL) {
			showHierarchy = true;
		}
		
		if (m_showHierarchy != showHierarchy) {
			m_showHierarchy = showHierarchy;
			refreshSelections();
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == m_presentationBox) {
			changePresentation();
		} else {
			super.actionPerformed(e);
		}
	}
	
	private class PresentationBoxRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			Icon icon = AbstractComponentEditor.getIcon(PresentationBoxRenderer.class,
					value == FLAT ?
							s_res.getString("SemanticElementField.flatIcon") :
							s_res.getString("SemanticElementField.hierarchicalIcon"));
			
			Component c = super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			
			setIcon(icon);
			return c;
		}
	};
	
	// dialog to get information for creating a new semantic element
	private class NewSemanticElementDialog extends BaseDialog implements DocumentListener {

		private JTextField m_nameField = new JTextField();
		private JCheckBox  m_setSENode = new JCheckBox(s_res.getString("NewSemanticElementDialog.setSEsNode"));
		private JCheckBox  m_setSEParent = new JCheckBox(s_res.getString("NewSemanticElementDialog.setSEsParent"));
		private JCheckBox  m_multipleInstances = new JCheckBox(s_res.getString("NewSemanticElementDialog.multipleInstances"), true);
		
		private SemanticElement m_semanticElement;

		public NewSemanticElementDialog(Frame owner) {
			super(owner, BaseDialog.OK_CANCEL_OPTION);
			setTitle(s_res.getString("SemanticElementField.createToolTip"));
			buildUI();
			setDirty(true);
			pack(new Dimension(400, 200));
		}
		
		public void setNameField(String name) {
			// Initialize with node name
			if (name != null) {
				m_nameField.setText(name);
			}	
		}
		
		private void buildUI() {
			//  Name: [_______________________________]
			//  [X] Set the Semantic Element's Node to this Node
			
			JPanel main = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = Standards.getInsets();
			gbc.anchor = GridBagConstraints.WEST;
			gbc.weightx = 0;
			gbc.weighty = 0;
			gbc.gridx = 0;
			gbc.gridy = 0;
			
			// Name
			main.add(new JLabel("Name:"), gbc);
			gbc.gridx++;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			main.add(m_nameField, gbc);

			gbc.insets.top = 0;
			gbc.insets.bottom = 0;
			
			// [X] Set the Semantic Element's Node to this Node
			gbc.gridy++;
			gbc.gridx = 1;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			main.add(m_setSENode, gbc);
			// disable if we don't have a node
			if (m_node == null) {
				m_setSENode.setEnabled(false);
			} else {
				m_setSENode.setSelected(true);
			}

			
			// [X] Set the Semantic Element's Parent
			gbc.gridy++;
			gbc.gridx = 1;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			main.add(m_setSEParent, gbc);
			// disable if we don't have a node
			if (m_node == null || m_node.getParentNode() == null || m_node.getParentNode().getSemanticElement() == null) {
				m_setSEParent.setEnabled(false);
			} else {
				m_setSEParent.setSelected(true);
			}
			
			// [X] Multiple Instances
			gbc.gridy++;
			gbc.gridx = 1;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			main.add(m_multipleInstances, gbc);
			
			
			getContentPane().add(main);
			
			// listen to text changes
			m_nameField.getDocument().addDocumentListener(this);
		}

		@Override
		public void dispose() {
			m_nameField.getDocument().removeDocumentListener(this);
			super.dispose();
		}
		

		@Override
		protected void okButtonAction() {
			String seName = m_nameField.getText().trim();

			SelectionManager selectionManager = SelectionManager.getInstance();
			MdmiModelTree entitySelector = selectionManager.getEntitySelector();
			
			MessageModel model = m_messageModel;
			SemanticElementSet seSet = model.getElementSet();
			if (seSet == null) {
				return;
			}

			DefaultMutableTreeNode seSetNode = entitySelector.findNode(seSet);
			if (seSetNode instanceof EditableObjectNode) {
				EditableObjectNode edNode = (EditableObjectNode)seSetNode;
				Collection<NewObjectInfo> newOperations = edNode.getNewObjectInformation(false);
				// should only be one kind of child to create
				if (newOperations.size() == 1) {
					for (NewObjectInfo newObjectInfo : newOperations) {
						EditableObjectNode newNode = newObjectInfo.createNewChild();
						m_semanticElement = (SemanticElement)newNode.getUserObject();
						m_semanticElement.setName(seName);
						newNode.setUserObject(m_semanticElement);	// force name to appear in tree
						
						// set these attributes after setUserObject call
						if (m_setSENode.isSelected()) {
							m_semanticElement.setSyntaxNode(m_node);
						}
						if (m_setSEParent.isSelected()) {
							m_semanticElement.setParent(m_node.getParentNode().getSemanticElement());
						}
						m_semanticElement.setMultipleInstances(m_multipleInstances.isSelected());

						entitySelector.insertNewNode(edNode, newNode);
						entitySelector.refreshNode(edNode);	// refresh parent node
					}
				}
			}


			super.okButtonAction();
		}

		
		public SemanticElement getSemanticElement() {
			return m_semanticElement;
		}

		
		@Override
		public boolean isDataValid() {
			return !m_nameField.getText().trim().isEmpty();
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			setDirty(true);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			setDirty(true);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			setDirty(true);
		}

	}
}
