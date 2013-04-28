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
import org.openhealthtools.mdht.mdmi.editor.map.editor.DatatypeSelectionPanel;
import org.openhealthtools.mdht.mdmi.editor.map.editor.MdmiDatatypeField;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;

/** A dialog used for selecting a MessageGroup, and one or more
 * datatypes within that message group
 * @author Conway
 *
 */
public class DatatypeSelectionDialog extends BaseDialog {
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");

	
	private JComboBox m_messageGroupSelector  = new JComboBox();
	private JPanel    m_selectionArea = new JPanel(new BorderLayout());
	private DatatypeSelectionPanel m_selectionPanel;
	
	private ModelRenderers.MessageGroupRenderer m_messageGroupRenderer = new ModelRenderers.MessageGroupRenderer();
	private ActionListener m_messageGroupListener = new MessageGroupListener();

	public DatatypeSelectionDialog(Frame owner) {
		super(owner, BaseDialog.OK_CANCEL_OPTION);
		buildUI();
		setTitle(s_res.getString("DatatypeSelectionDialog.title"));
		pack(new Dimension(400,400));
	}


	public DatatypeSelectionPanel createDatatypeSelectionPanel(MessageGroup group) {
		List<MdmiDatatype> types = new ArrayList<MdmiDatatype>();
		types.addAll(group.getDatatypes());
		// Sort by type name
		Collections.sort(types, MdmiDatatypeField.getDatatypeComparator());
		DatatypeSelectionPanel selectionPanel = new DatatypeSelectionPanel(types);
		
		// show 20 rows
		if (types.size() > selectionPanel.getList().getVisibleRowCount()) {
			int rowCount = Math.min(20, types.size());
			selectionPanel.getList().setVisibleRowCount(rowCount);
		}
		
		return selectionPanel;
	}

	public Collection<MdmiDatatype> getDatatypes() {
		return m_selectionPanel.getSelectedTypes();
	}
	
	private void buildUI() {
		// 
		// Message Group: [__________________|v]
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
		
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = Standards.getInsets();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 0;
		gbc.weighty = 0;
		
		// Message Group
		mainPanel.add(new JLabel(s_res.getString("DatatypeSelectionDialog.messageGroup")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.left = 0;
		mainPanel.add(m_messageGroupSelector, gbc);
		gbc.insets.left = Standards.LEFT_INSET;
		
		// selection
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		mainPanel.add(m_selectionArea, gbc);
		// add a border: "Select one or more Data Types"
		m_selectionArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
				s_res.getString("DatatypeSelectionDialog.selectTitle")));
		
		// pick first message group
		fillSelectionArea();
		
		setDirty(true);	// allow OK button
		
		getContentPane().add(mainPanel, BorderLayout.CENTER);
	}
	
	/** Replace the item selector based on the message group */
	private void fillSelectionArea() {
		MessageGroup group = (MessageGroup)m_messageGroupSelector.getSelectedItem();
		if (m_selectionPanel != null) {
			m_selectionArea.remove(m_selectionPanel);
		}
		m_selectionPanel = createDatatypeSelectionPanel(group);
		m_selectionArea.add(m_selectionPanel, BorderLayout.CENTER);
		m_selectionArea.revalidate();
	}
	
	public DatatypeSelectionPanel getDatatypeSelectionPanel() {
		return m_selectionPanel;
	}

	@Override
	public void dispose() {
		m_messageGroupSelector.removeActionListener(m_messageGroupListener);
		m_messageGroupSelector.setRenderer(null);
		super.dispose();
	}

	@Override
	public boolean isDataValid() {
		return true;
	}
	
	@Override
	protected void okButtonAction() {
		// verify selection
		if (getDatatypes().size() == 0) {
			JOptionPane.showMessageDialog(this, 
					s_res.getString("DatatypeSelectionDialog.noSelectionMessage"),
					s_res.getString("DatatypeSelectionDialog.noSelectionTitle"),
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		super.okButtonAction();
	}

	////////////////////////////////
	private class MessageGroupListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			fillSelectionArea();
		}
	}

}
