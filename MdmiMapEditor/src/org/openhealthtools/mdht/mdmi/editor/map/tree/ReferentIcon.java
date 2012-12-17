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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.Icon;

/** Overlay a Symbol ("R") on an existing Icon to indicate "Referent" */
public class ReferentIcon implements Icon {
	
	private Icon  m_innerIcon;
	private Color m_color = new Color(0x41894D/*0x10A210*/);	// greenish
	
	public ReferentIcon(Icon innerIcon) {
		m_innerIcon = innerIcon;
	}
	
	/** Change the color used for the R symbol */
	public void setSymbolColor(Color color) {
		m_color = color;
	}
	
	/** Get the color used for the symbol */
	public Color getSymbolColor() {
		return m_color;
	}

	@Override
	public int getIconHeight() {
		return m_innerIcon.getIconHeight();
	}

	@Override
	public int getIconWidth() {
		return m_innerIcon.getIconWidth();
	}

	@Override
	 public void paintIcon(Component c, Graphics g, int x, int y) {

        m_innerIcon.paintIcon(c, g, x, y);
        
        // small "R", bottom right
        //   ------------
        //  |            |
        //  |            |
        //  |       |D   |
        //  |       | \  |
        //   --------------
        Font font = c.getFont();
        font = font.deriveFont(Font.BOLD, 8.0f);
        
        String text = "R";
        FontMetrics fm = c.getFontMetrics(font);
        int w = fm.stringWidth(text)-1;
        int h = fm.getAscent()-1;
        
        g.setColor(Color.white);
        g.fillRect(getIconWidth()-w, getIconHeight()-h, w, h);
        
        g.setColor(m_color);
		g.drawString(text, getIconWidth()-w, getIconHeight());
        g.setColor(m_color.darker());
		g.drawString(text, getIconWidth()-w+1, getIconHeight());
                
    } 

}
