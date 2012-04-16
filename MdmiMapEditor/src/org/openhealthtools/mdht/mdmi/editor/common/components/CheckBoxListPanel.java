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
package org.openhealthtools.mdht.mdmi.editor.common.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;

/**
 * CheckBoxListPanel incorporates a CheckBoxList and buttons for selecting
 * and clearing all the checkboxes.
 * 
 * @author fiedler
 *
 */
public class CheckBoxListPanel extends JPanel
{
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.common.components.Local");

   /** Property for change to number of selections */
   public static final String NUM_SELECTIONS_PROPERTY  = "NumSelections";
   
   private DefaultListModel m_model;
   private CheckBoxList m_list; 
   private JButton m_btnSelectAll;
   private JButton m_btnClearAll;
   
   // Button Listener
   private ActionListener m_buttonListener;
   
   /** Create a list panel */
   public CheckBoxListPanel() {
      this.setLayout(new BorderLayout());
      
      m_model = new DefaultListModel();
      m_list = new CheckBoxList(m_model);
      JScrollPane scroller = new JScrollPane(m_list);

      m_btnSelectAll = new JButton(s_res.getString("CheckBoxListPanel.selectAll"));
      m_btnClearAll = new JButton(s_res.getString("CheckBoxListPanel.clearAll"));
      
      this.add(scroller, BorderLayout.CENTER);
      
      // buttons on right
      VerticalButtonPanel buttonPanel = createVerticalButtons();

      m_buttonListener = new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            boolean selectedValue = evt.getSource().equals(m_btnSelectAll);
            
            selectAll(selectedValue);
         }
      };

      this.add(buttonPanel, BorderLayout.EAST);
   }

	/** Create a panel for buttons. Two buttons will be added - Select All and Clear All
	 * @return
	 */
	protected VerticalButtonPanel createVerticalButtons() {
		VerticalButtonPanel buttonPanel = new VerticalButtonPanel();
      buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, Standards.LEFT_INSET, 0, 0));
      
      buttonPanel.addStrut(m_btnClearAll.getHeight());
      buttonPanel.add(m_btnSelectAll);
      buttonPanel.add(m_btnClearAll);
      
		return buttonPanel;
	}
   
   /** Get all check boxes in the model */
   public List<JCheckBox> getCheckBoxes() {
   	List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();

      for (int i=0; i < m_model.getSize(); i++) {
      	Object element = m_model.getElementAt(i);
         if (element instanceof JCheckBox) {
            checkBoxes.add((JCheckBox)element);
         }
      }
      
		return checkBoxes;
   }
   
   /** Add a checkBox to the model */
   public void addCheckBox(JCheckBox checkBox) {
   	m_model.addElement(checkBox);
   }

	/** Find the checkbox that matches this text */
	public JCheckBox findCheckBox(String text) {
		for (JCheckBox checkbox : getCheckBoxes()) {
			if (checkbox.getText().equalsIgnoreCase(text)) {
				return checkbox;
			}
		}
		return null;
	}

   /** 
    * add listeners
    */
   @Override
   public void addNotify() {
      super.addNotify();
      m_btnSelectAll.addActionListener( m_buttonListener );
      m_btnClearAll.addActionListener( m_buttonListener );
   }
   
   /** 
    * cleanup listeners
    */
   @Override
   public void removeNotify() {
      m_btnSelectAll.removeActionListener( m_buttonListener );
      m_btnClearAll.removeActionListener( m_buttonListener );
      super.removeNotify();
   }
   
   /**
    * Return number of checkboxes checked.
    * @return checked boxes
    */
   public int getNumSelected() {
      return m_list.getNumSelected();
   }
   
   /** Get the list used in this panel*/
   public JList getList() {
      return m_list;
   }
   
   /** Get the list model */
   public DefaultListModel getModel() {
   	return m_model;
   }
   
   /**
    * Fire property change for number of selections.
    * Always indicate a change, so oldValue always == -1
    */
   private void fireNumSelectionsPropertyChange() {
      firePropertyChange( NUM_SELECTIONS_PROPERTY, -1, m_list.getNumSelected() );
   }
   
   /** Select (or deselect) all check boxes.
    * @param select
    */
   public void selectAll(boolean select) {
      ListModel model = m_list.getModel();
      for (int i=0; i < model.getSize(); i++) {
         Object item = model.getElementAt(i);
         if (item instanceof JCheckBox) {
            ((JCheckBox)item).setSelected(select);
         }
      }
      
      repaint();
      
      fireNumSelectionsPropertyChange();
   }

   ////////////////////////////////////////////////////////////
   // CheckBoxList
   ////////////////////////////////////////////////////////////
   protected static final int s_leftInset = 10;
   protected static Border s_noFocusBorder = new EmptyBorder(2, s_leftInset, 2, 0 );
//   protected static Border s_focusBorder = BorderFactory.createCompoundBorder(
//                                                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
//                                                BorderFactory.createEmptyBorder(1, s_leftInset-1, 1, 0 ));
   protected static int s_checkBoxIconWidth = 10;
   
   static {
   	s_checkBoxIconWidth = UIManager.getIcon("CheckBox.icon").getIconWidth();
   }
   
   /** 
    * CheckBoxList component is a list where each item in the model is a checkbox.
    * @author fiedler
    */
   public class CheckBoxList extends JList {
      
      private CellRenderer m_cellRenderer = new CellRenderer();
      
      private KeyListener m_keyListener = new KeyAdapter() {
         @Override
         public void keyReleased(KeyEvent arg0) {
            super.keyReleased( arg0 );
            if (arg0.getKeyChar() == KeyEvent.VK_SPACE) {
               updateCheckBox();
            }
         }
         
      };
      
      private MouseListener m_mouseListener = new MouseAdapter()
      {
         @Override
         public void mousePressed(MouseEvent e)
         {
         	// check coordinates
            Object selected = getSelectedValue();
            if (selected instanceof JCheckBox) {
            	// only update if mouse point is within the [ ] image
            	if (s_leftInset <= e.getX() && e.getX() <= s_leftInset+s_checkBoxIconWidth) {
                  updateCheckBox();		
            	}
            }
         }
      };

      public CheckBoxList(ListModel model) {
      	super(model);
      	setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      }

      /**
       * Update the checkbox and fire a property change.
       */
      private void updateCheckBox() {
         Object selected = getSelectedValue();
         
         if (selected instanceof JCheckBox) {
            JCheckBox checkbox = (JCheckBox)selected;
            checkbox.setSelected(!checkbox.isSelected());
            fireNumSelectionsPropertyChange();
            repaint();
         }
      }
      
      /**
       * Add mouse and key listeners to list.
       */
      @Override
      public void addNotify() {
         super.addNotify();
         addMouseListener( m_mouseListener );
         addKeyListener( m_keyListener );
         setCellRenderer( m_cellRenderer );
      }
      
      /**
       * Remove mouse and key listeners to list.
       */
      @Override
      public void removeNotify() {
         removeMouseListener( m_mouseListener );
         removeKeyListener( m_keyListener );
         setCellRenderer( null );
         super.removeNotify();
      }
      
      /**
       * Return number of checkboxes checked.
       * @return checked boxes
       */
      public int getNumSelected() {
         int num = 0;
         
         for (int i=0; i < getModel().getSize(); i++) {
            JCheckBox checkbox = (JCheckBox)getModel().getElementAt(i);
            if (checkbox.isSelected())
               num++;
         }
         
         return num;
      }
   }

   ////////////////////////////////////////////////////////
   //  List Renderer
   ////////////////////////////////////////////////////////
   protected class CellRenderer implements ListCellRenderer
   {
      public Component getListCellRendererComponent(
                    JList list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus)
      {
         JCheckBox checkbox = (JCheckBox) value;
         checkbox.setEnabled(isEnabled());
         checkbox.setFont(getFont());
         checkbox.setBorder(s_noFocusBorder);
         checkbox.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
         checkbox.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

         return checkbox;
      }
   }
}
