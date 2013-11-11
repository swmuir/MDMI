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
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.tree.DefaultMutableTreeNode;

import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.StatusPanel;
import org.openhealthtools.mdht.mdmi.editor.map.console.ReferenceLink;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SemanticElementNode;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;

/** An IEditorField that shows SemanticElement values in a ComboBox */
public class SemanticElementField extends AdvancedSelectionField {
	
	public static final String FLAT = s_res.getString("SemanticElementField.flatPresentation");
	public static final String HIERARCHICAL = s_res.getString("SemanticElementField.hierarchicalPresentation");

	private JComboBox   m_presentationBox;
	private boolean m_showHierarchy = false;
	
	public SemanticElementField(GenericEditor parentEditor) {
		super(parentEditor);
		m_showHierarchy = false;	// for now
		
		//  add presentation box
		m_presentationBox = new JComboBox();
		m_presentationBox.addItem(FLAT);
		m_presentationBox.addItem(HIERARCHICAL);

		GridBagConstraints gbc = GetGridBagConstraints();
		gbc.gridx++;
		add(m_presentationBox, gbc);
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
		m_presentationBox.addActionListener(this);
		m_presentationBox.setRenderer(new PresentationBoxRenderer());
	}

	@Override
	public void removeNotify() {
		getViewButton().setToolTipText(null);
		super.removeNotify();
		m_presentationBox.removeActionListener(this);
		m_presentationBox.setRenderer(null);
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

}
