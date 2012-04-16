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
package org.openhealthtools.mdht.mdmi.editor.map.tree;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;


public class ErrorIcon implements Icon {
	
	private Icon  m_innerIcon;
	private Color m_color = Color.red;
	
	public ErrorIcon(Icon innerIcon) {
		m_innerIcon = innerIcon;
	}
	
	/** Change the color used for errors */
	public void setErrorColor(Color color) {
		m_color = color;
	}
	
	/** Get the color used for errors */
	public Color getErrorColor() {
		return m_color;
	}

	@Override
	public int getIconHeight() {
		return m_innerIcon.getIconHeight();
	}

	@Override
	public int getIconWidth() {
		return m_innerIcon.getIconWidth() + 2;
	}

	@Override
	 public void paintIcon(Component c, Graphics g, int x, int y) {

        m_innerIcon.paintIcon(c, g, x, y);
        
        Graphics2D g2d = (Graphics2D)g;
        g2d.setStroke(new BasicStroke(2.0f));

        // red circle - bottom right
        g.setColor(m_color);
        int d = 10;
        int x1 = x+getIconWidth() - d;
        int y1 = y+getIconHeight() - d;
        g.drawOval(x1, y1, d, d);
        
        // diagonal line
        int d2 = (int)(0.293*d);
        g.drawLine(x1+d2, y1+d2, x1+d-d2, y1+d-d2);
        
    } 

}
