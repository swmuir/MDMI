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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author Sally Conway
 *
 * An 16x16 Icon based on a text string. The text will appear etched.
 * This is convenient for providing buttons
 * with more elaborate text labels.
 * Note that this works with capital letters, as the base-line of the
 * text will be placed 3 pixels above the bottom edge of the icon.
 */
public class TextIcon implements Icon {

	private String m_text = "*";
	private Font   m_font = new Font("Serif", Font.BOLD, 18);
	private Color  m_color = Color.black;
	private boolean m_underline = false;
	
	/** Create a text icon for the provided string using the
	 * default font - Serif, Bold, 18 pt
	 */
	public TextIcon(String text) {
		m_text = text;
	}

	/** Create a text icon for the provided string using the
	 * default font - Serif, 18 pt, with the supplied font style
	 * @param	text	The text to be used
	 * @param	fontStyle  Font.PLAIN, Font.BOLD, Font.ITALIC or a combination
	 */
	public TextIcon(String text, int fontStyle) {
		m_text = text;
		m_font = m_font.deriveFont(fontStyle);
	}
	
	/** Return the font that will be used for the icon */
	public Font getFont() {
		return m_font;
	}
	
	/** Change the font used for the icon */
	public void setFont(Font font) {
		m_font = font;
	}

	/** Get the color used for the icon. */
	public Color getColor() {
		return m_color;
	}

	/** Change the color used for the icon. */
	public void setColor(Color color) {
		m_color = color;
	}
	
	/** Underline the text */
	public void setUnderlined(boolean underline) {
		m_underline = underline;
	}

	/** Set (or unset) the text to be bold */
	public void setBold(boolean bold) {
		int style = m_font.getStyle();
		if (bold) {
			style += Font.BOLD;
		} else if ((style & Font.BOLD) != 0) {
			style -= Font.BOLD;
		}
		m_font = m_font.deriveFont(style);
	}

	/** Set (or unset) the text to be italic */
	public void setItalic(boolean italic) {
		int style = m_font.getStyle();
		if (italic) {
			style += Font.ITALIC;
		} else if ((style & Font.ITALIC) != 0) {
			style -= Font.ITALIC;
		}
		m_font = m_font.deriveFont(style);
	}
	
	/** Get the icon height. Default is 16 */
	public int getIconHeight() {
		return 16;
	}

	/** Get the icon height. Default is 16 */
	public int getIconWidth() {
		return 16;
	}

	/* This is the Icon interface method that draws the icon. *
	 */
	public void paintIcon(Component c, Graphics g, int x0, int y0) {
		
		FontMetrics fm = c.getFontMetrics(m_font);

		// save color and font
		Color oldColor = g.getColor();
		Font oldFont = g.getFont();
		
//		// Debug - draw box around icon
//		g.setColor(Color.cyan);
//		g.drawRect(x0, y0, getIconWidth()-1, getIconHeight()-1);
		
		g.setFont(m_font);
		int textWidth = fm.stringWidth(m_text);
		
		// center text horizontally
		int x = x0 + (getIconWidth() - textWidth)/2;
		
		// align baseline of text with bottom of icon
		// (less a few pixels that will be used to underline)
		int y = y0 + getIconHeight() - 3;
		
		// draw a dark background above and to the left, and
		// a light background below and to the right to make it look etched
		g.setColor(c.getBackground().darker());
		drawText(g, x-1, y-1, textWidth);
		
		g.setColor(Color.white);
		drawText(g, x+1, y+1, textWidth);
		
		// now draw the text
		g.setColor(m_color);
		drawText(g, x, y, textWidth);
		
		// restore font and color
		g.setFont(oldFont);
		g.setColor(oldColor);
	}
	
	/** Draw the text in the Graphics context (and underline it if requested) */
	private void drawText(Graphics g, int x, int y, int textWidth) {
		g.drawString(m_text, x, y);
		// underline
		if (m_underline) {
			g.drawLine(x, y+1, x + textWidth - 1, y+1);
		}
	}
	

	public static void main(String[] args) {
		JFrame testFrame = new JFrame("Text Icon Demo");
		testFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		testFrame.getContentPane().setLayout(new BorderLayout());
		
		JPanel panel = new JPanel(new FlowLayout());

		// Bold button
		TextIcon icon = new TextIcon("B");
		icon.setColor(Color.blue.darker());
		panel.add(new JButton(icon));

		// Italic button
		icon = new TextIcon("I");
		icon.setItalic(true);
		panel.add(new JButton(icon));

		// Underline button
		icon = new TextIcon("U");
		icon.setUnderlined(true);
		icon.setColor(Color.green.darker());
		panel.add(new JButton(icon));
		
		testFrame.getContentPane().add(panel, BorderLayout.CENTER);
		
		testFrame.setSize(new Dimension(400, 500));
		testFrame.setVisible(true);
	}
}
