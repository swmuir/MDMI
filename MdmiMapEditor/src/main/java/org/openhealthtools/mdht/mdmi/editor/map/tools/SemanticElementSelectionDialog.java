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
package org.openhealthtools.mdht.mdmi.editor.map.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.editor.SemanticElementSelectionPanel;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.MessageModel;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;

/** A dialog used for selecting a MessageGroup, MessageModel, and one or more
 * Semantic Elements within that message model
 * @author Conway
 *
 */
public class SemanticElementSelectionDialog extends BaseDialog {
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");

	
	private JComboBox m_messageGroupSelector  = new JComboBox();
	private JComboBox m_messageModelSelector  = new JComboBox();
	private JPanel    m_selectionArea = new JPanel(new BorderLayout());
	private SemanticElementSelectionPanel m_selectionPanel;

	private ModelRenderers.MessageGroupRenderer m_messageGroupRenderer = new ModelRenderers.MessageGroupRenderer();
	private ModelRenderers.MessageModelRenderer m_messageModelRenderer = new ModelRenderers.MessageModelRenderer();
	private ActionListener m_messageGroupListener = new MessageGroupListener();
	private ActionListener m_messageModelListener = new MessageModelListener();

	public SemanticElementSelectionDialog(Frame owner) {
		super(owner, BaseDialog.OK_CANCEL_OPTION);
		buildUI();
		setTitle(s_res.getString("SemanticElementSelectionDialog.title"));
		pack(new Dimension(400,400));
	}


	public SemanticElementSelectionPanel createSemanticElementSelectionPanel(MessageGroup group,
			MessageModel model) {
		List<SemanticElement> elements = new ArrayList<SemanticElement>( group.getModel(model.getMessageModelName()).getElementSet().getSemanticElements() );
		// Sort by elemement name
		Collections.sort(elements, new Comparators.SemanticElementComparator());
		SemanticElementSelectionPanel selectionPanel = new SemanticElementSelectionPanel(elements);
		
		// show up to 18 rows
		if (elements.size() > selectionPanel.getList().getVisibleRowCount()) {
			int rowCount = Math.min(20, elements.size());
			selectionPanel.getList().setVisibleRowCount(rowCount);
		}
		
		return selectionPanel;
	}

	public Collection<SemanticElement> getSemanticElements() {
		return m_selectionPanel.getSelectedElements();
	}
	
	private void buildUI() {
		// 
		// Message Group: [__________________|v]
		// Message Model: [__________________|v]
		//  - Select One Or More items --------
		// | [ ] Object 1                      |
		// | [ ] Object 2                      |
		// | [ ] Object 3                      |
		// | [ ] Object 4                      |
		// | [ ] Object 5                      |
		//  -----------------------------------
		
		for (MessageGroup group : SelectionManager.getInstance().getEntitySelector().getMessageGroups()) {
			m_messageGroupSelector.addItem(group);
		}
		m_messageGroupSelector.addActionListener(m_messageGroupListener);
		m_messageGroupSelector.setRenderer(m_messageGroupRenderer);
		
		m_messageModelSelector.addActionListener(m_messageModelListener);
		m_messageModelSelector.setRenderer(m_messageModelRenderer);
		
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = Standards.getInsets();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 0;
		gbc.weighty = 0;

		// Message Group
		mainPanel.add(new JLabel(s_res.getString("SemanticElementSelectionDialog.messageGroup")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.left = 0;
		mainPanel.add(m_messageGroupSelector, gbc);
		gbc.insets.left = Standards.LEFT_INSET;
		

		// Message Model
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("SemanticElementSelectionDialog.messageModel")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.left = 0;
		mainPanel.add(m_messageModelSelector, gbc);
		gbc.insets.left = Standards.LEFT_INSET;
		
		// selection
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		mainPanel.add(m_selectionArea, gbc);
		// add a border: "Select one or more Semantic Elements"
		m_selectionArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
				s_res.getString("SemanticElementSelectionDialog.selectTitle")));
		
		// pick first message group
		fillMessageModelChoices();
		fillSelectionArea();
		
		setDirty(true);	// allow OK button
		
		getContentPane().add(mainPanel, BorderLayout.CENTER);
	}
	
	/** Replace the Message Model choices */
	private void fillMessageModelChoices() {
		MessageGroup group = (MessageGroup)m_messageGroupSelector.getSelectedItem();
		// disable
		m_messageModelSelector.removeActionListener(m_messageModelListener);
		// clear
		m_messageModelSelector.removeAllItems();
		List<MessageModel> models = new ArrayList<MessageModel>(group.getModels());
		// sort
		Collections.sort(models, new Comparators.MessageModelComparator());
		for (MessageModel model : models) {
			m_messageModelSelector.addItem(model);
		}
		
		// re-enable
		m_messageModelSelector.addActionListener(m_messageModelListener);
	}
	
	/** Replace the item selector based on the message group */
	private void fillSelectionArea() {
		MessageGroup group = (MessageGroup)m_messageGroupSelector.getSelectedItem();
		MessageModel model = (MessageModel)m_messageModelSelector.getSelectedItem();
		if (m_selectionPanel != null) {
			m_selectionArea.remove(m_selectionPanel);
		}
		m_selectionPanel = createSemanticElementSelectionPanel(group, model);
		m_selectionArea.add(m_selectionPanel, BorderLayout.CENTER);
		m_selectionArea.revalidate();
	}
	
	public SemanticElementSelectionPanel getSemanticElementSelectionPanel() {
		return m_selectionPanel;
	}

	@Override
	public void dispose() {
		m_messageGroupSelector.removeActionListener(m_messageGroupListener);
		m_messageGroupSelector.setRenderer(null);
		m_messageModelSelector.removeActionListener(m_messageModelListener);
		m_messageModelSelector.setRenderer(null);
		super.dispose();
	}

	@Override
	public boolean isDataValid() {
		return true;
	}
	
	@Override
	protected void okButtonAction() {
		// verify selection
		if (getSemanticElements().size() == 0) {
			JOptionPane.showMessageDialog(this, 
					s_res.getString("SemanticElementSelectionDialog.noSelectionMessage"),
					s_res.getString("SemanticElementSelectionDialog.noSelectionTitle"),
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		super.okButtonAction();
	}

	////////////////////////////////
	private class MessageGroupListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			fillMessageModelChoices();
		}
	}
	
	private class MessageModelListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			fillSelectionArea();
		}
	}

}
