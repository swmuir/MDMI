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
/*
 * Created on Oct 20, 2005
 *

 */
package org.openhealthtools.mdht.mdmi.editor.common.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.UIManager;

/**
 * @author SallyConway
 *
 * A component that contains buttons arranged vertially.
 */
public class VerticalButtonPanel extends Box {
    
    private static final long serialVersionUID = -382408710793782477L;

	public VerticalButtonPanel() {
        super(BoxLayout.Y_AXIS);
    }
    
    
    /** Add a button to the panel */
    public Component add(JComponent button) {
        
        // make all buttons the same width
        button.setAlignmentX(LEFT_ALIGNMENT);
        Dimension d = button.getPreferredSize();
        button.setMaximumSize(new Dimension(Short.MAX_VALUE, d.height));
        
        return super.add(button);
    }
    
    /** Add a button at the specified position */
    public Component add(JComponent button, int pos) {
       // make all buttons the same width
       button.setAlignmentX(LEFT_ALIGNMENT);
       Dimension d = button.getPreferredSize();
       button.setMaximumSize(new Dimension(Short.MAX_VALUE, d.height));
       
       return super.add(button, pos);
    }
    
    /** Add a fixed size, invisible space between buttons */
    public void addStrut(int size) {
        add(Box.createVerticalStrut(size));
    }
    
    /** Add glue to the component.
     * Glue can expand or shrink to fill in extra space.
     */
    public void addGlue() {
        add(createVerticalGlue());
    }

    /** Sample usage of VerticalButtonPanel */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { }


        JFrame frame = new JFrame("Demo using VerticalButtonPanel"); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        
        Container main = frame.getContentPane();
        main.setLayout(new BorderLayout());
        
        // put a big button in the center
        JButton bigPinkButton = new JButton("Big Pink Button");
        bigPinkButton.setBackground(Color.pink);
        
        main.add(bigPinkButton, BorderLayout.CENTER);
        
        // put 3 groups of buttons in the EAST
        VerticalButtonPanel bp = new VerticalButtonPanel();
        main.add(bp, BorderLayout.EAST);

        bp.addGlue();
        
        bp.add(new JButton("Group 1"));
        bp.add(new JButton("Add"));
        bp.add(new JButton("Delete"));
        
        bp.addStrut(5);
        
        bp.add(new JButton("Group 2"));
        bp.add(new JButton("Add"));
        bp.add(new JButton("Delete"));
        
        bp.addGlue();
        
        bp.add(new JButton("Group 3"));
        bp.add(new JButton("Add"));
        bp.add(new JButton("Delete"));

        bp.addGlue();

        frame.setSize(400, 400); 
        frame.setVisible(true); 
    }
}
