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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.openhealthtools.mdht.mdmi.editor.common.components.CursorManager;
import org.openhealthtools.mdht.mdmi.editor.common.components.WindowUtil;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.editor.AbstractComponentEditor;
import org.openhealthtools.mdht.mdmi.*;

/** a Frame with a print button. Derived classes must set the center panel
 * by calling setCenterComponent() */
public abstract class PrintableView extends JFrame  {
	
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");
	
	private JButton m_printButton = new JButton(s_res.getString("ViewDataObject.print"));
	private PrintAction m_printAction = new PrintAction();
	
	private Component m_centerComponent = null;
	
	/** Create a frame with a BorderLayout */
	protected PrintableView() {		
		// set up frame parameters
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(createButtonPanel(), BorderLayout.NORTH);
	}
	
	/** set the component that displays in the center of the frame */
	protected void setCenterComponent(Component centerComponent) {
		// remove previous if there is one
		if (m_centerComponent != null) {
			getContentPane().remove(m_centerComponent);
		}
		m_centerComponent = centerComponent;
		getContentPane().add(centerComponent, BorderLayout.CENTER);
	}

	/** Pack, but ensure that size is within the specified bounds */
	public void pack(Dimension min, Dimension max) {
		pack();
		Dimension size = getSize();

		/** Make sure the width is between the minimum and maximum */
		size.width = Math.max(size.width, min.width);
		size.width = Math.min(size.width, max.width);
			
		/** Make sure the height is between the minimum and maximum */
		size.height = Math.max(size.height, min.height);
		size.height = Math.min(size.height, max.height);
		
		setSize(size);
	}
	
	/** create a top panel using a right-justified FlowLayout, with a print button */
	protected JPanel createButtonPanel() {
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		buttons.add(m_printButton);
		m_printButton.setIcon(AbstractComponentEditor.getIcon(this.getClass(),
				s_res.getString("ViewDataObject.printIcon")));
		m_printButton.addActionListener(m_printAction);
		
		return buttons;
	}
	
	@Override
	public void dispose() {
		m_printButton.removeActionListener(m_printAction);

		// remove all children, and listeners, so garbage collection works better
		try {
			WindowUtil.removeAllComponents(this);
		} catch (Exception ex) {
			Mdmi.INSTANCE.logger().severe( "exception removing listeners. " + ex.getLocalizedMessage());
		}

		super.dispose();
	}
	
	/** Print the component */
	public void print() throws PrinterException {
		PrintUtilities.printComponent(getPrintComponent(), getTitle());
	}
	
	/** Get the componenet to be printed */
	protected abstract Component getPrintComponent();

	/** Open (edit) the supplied model */
	protected void openSelection(Object model) {
		if (model != null) {
			CursorManager cm = CursorManager.getInstance(PrintableView.this);
			try {
				cm.setWaitCursor();
				//find model in editor tree
				SelectionManager selectionManager = SelectionManager.getInstance();
				DefaultMutableTreeNode treeNode =
					selectionManager.getEntitySelector().findNode(model);
				if (treeNode != null) {
					selectionManager.getEntitySelector().selectNode(treeNode);
					selectionManager.editItem(treeNode);
					
					// reset focus
					this.toFront();
				}
			} finally {
				cm.restoreCursor();
			}
		}
	}
			
	/////////////////////////////////////////
	//  Action Listener 
	////////////////////////////////////////
	private class PrintAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			CursorManager cm = CursorManager.getInstance(PrintableView.this);
			cm.setWaitCursor();
			
			try {
				if (e.getSource() == m_printButton) {
					print();
				}
				
			} catch (Exception ex) {
				SelectionManager.getInstance().getStatusPanel().writeException(ex);
			} finally {
				cm.restoreCursor();
			}	
		}
	}
	
}
