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
import java.awt.Graphics;
import java.awt.Polygon;

import javax.swing.Icon;

/** Overlay a symbol on an existing Icon to indicate "imported" */
public class ImportedIcon implements Icon {
	
	private Icon  m_innerIcon;
	private Color m_color = new Color(0x0D5C9A);	// bluish
	
	public ImportedIcon(Icon innerIcon) {
		m_innerIcon = innerIcon;
	}
	
	/** Change the color used for the triangle symbol */
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
        
        // small triangle, upper left
        //   --------------
        //  |  /|          |
        //  |/__|          |
        //  |              |
        //  |-w-|          |
        //   --------------
        int w = 6;
        g.setColor(Color.white);
        g.fillRect(x, y, w+1, w+1);
        
        // add small triangle
        
        g.setColor(m_color);
        Polygon p1 = new Polygon();
        p1.addPoint(x+w, y);
        p1.addPoint(x+w, y+w);
        p1.addPoint(x, y+w);
        g.fillPolygon(p1);
        
        // acccent
        g.setColor(m_color.darker());
        Polygon p2 = new Polygon();
        p2.addPoint(x+w, y+w/2);
        p2.addPoint(x+w, y+w);
        p2.addPoint(x+w/2, y+w);
        g.fillPolygon(p2);
                
    } 

}
