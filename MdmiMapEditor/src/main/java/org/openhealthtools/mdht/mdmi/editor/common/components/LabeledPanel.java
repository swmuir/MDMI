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
 * Created on Nov 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.openhealthtools.mdht.mdmi.editor.common.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;


/**
 * @author Conway
 *
 * A panel component using a BorderLayout, with a label in the NORTH
 */
public class LabeledPanel extends JPanel {
	
	private static final long serialVersionUID = -8112970332128027984L;
	
	private Component m_center = null;
    private JLabel m_titleLabel;
    
    public LabeledPanel(String title) {
        setLayout(new BorderLayout());
        
        m_titleLabel = new JLabel(title);
        m_titleLabel.setBorder(Standards.createEmptyBorder());
        // adjust font
        m_titleLabel.setFont(m_titleLabel.getFont().deriveFont(Font.BOLD, 16.0f));
        
        add(m_titleLabel, BorderLayout.NORTH);
    }
    
    /** Change the title */
    public void setText(String title) {
        m_titleLabel.setText(title);
    }
    
    /** Get the title label */
    public JLabel getTitleLable() {
        return m_titleLabel;
    }
    
    /** Add a component to the center of this panel */
    @Override
    public Component add(Component c) {
        if (m_center != null) {
            remove(m_center);
        }
        m_center = c;
        add(c, BorderLayout.CENTER);
        return c;
    }
}
