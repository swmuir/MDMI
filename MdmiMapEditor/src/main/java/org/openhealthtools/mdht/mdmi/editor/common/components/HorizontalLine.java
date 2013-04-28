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
 * Created on Oct 19, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.openhealthtools.mdht.mdmi.editor.common.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.SystemColor;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * @author SallyConway
 *
 * A component for drawing a horizontal line
 */
public class HorizontalLine extends JPanel
{
    
	private static final long serialVersionUID = 1334651357953602404L;

	/** Lowered Etched Line */
    public static final int ETCHED = 0;
    
    /** Raised Line */
    public static final int RAISED = 1;
    
    /** Solid Line */
    public static final int SOLID = 2;
    
    private int _thickness = 1;
    private int _type = SOLID;
    private Color _lineColor = Color.black;
    
    /** Create a solid single-pixel etched line */
    public HorizontalLine() {
        this(ETCHED, 1);
    }
    
    /** Create a line of <var>type</var> and <var>thickness</var>.
     * @param type  one of <var>ETCHED</var>,  <var>RAISED</var> or <var>SOLID</var>
     * @param thickness The line thickness, in pixels
     **/
    public HorizontalLine(int type, int thickness) {
        _type = type;
        _thickness = thickness;
        
        if (type == ETCHED || type == RAISED) {
            _lineColor = SystemColor.control;
        } else {
            _lineColor = Color.black;
        }
        
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        int x = 0;
        int y = (getHeight() - _thickness)/2;
        int width = getWidth() - 1;
            
        g.setColor(_lineColor);
        // fill in background color
        g.fillRect(0, y, width, _thickness);
        
        // draw edges
        switch (_type) {
        case SOLID:
            g.drawRect(x, y, width, _thickness);
            break;
        
        case RAISED:
            g.draw3DRect(x, y, width, _thickness, true);
            break;
            
        case ETCHED:
            g.draw3DRect(x, y, width, _thickness, false);
            break;
            
        default:
            g.drawRect(x, y, width, _thickness);
        }
    }
    
    /**
     * Returns the line color.
     */
    public Color getLineColor() {
        return _lineColor;
    }
    
    /**
     * Set the line color.
     */
    public void setLineColor(Color color) {
        _lineColor = color;
    }
    
    
    /**
     * Returns the line thickness.
     */
    public int getThickness() {
        return _thickness;
    }
    
    /**
     * Set the line thickness, in pixels.
     */
    public void setThickness(int thickness) {
        this._thickness = thickness;
    }
    
    /**
     * Returns the line type.
     */
    public int getType() {
        return _type;
    }
    
    /**
     * Sets the line type
     * @param type  one of <var>ETCHED</var>,  <var>RAISED</var> or <var>SOLID</var>
     */
    public void setType(int type) {
        this._type = type;
    }
    
    
    /** Create a sample frame demonstrating various line styles */
    public static void main(String[] args) { 
        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { }

        JFrame frame = new JFrame("Horizontal Line Demonstration"); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 

         
        Container main = frame.getContentPane();
        main.setLayout(new BorderLayout()); 
        
        JPanel contents = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets.top = gbc.insets.left = gbc.insets.bottom = gbc.insets.right = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        main.add(contents, BorderLayout.CENTER);

        HorizontalLine line = new HorizontalLine();
        addLabeledRow("Default:", contents, line, gbc);
        
        // Add some other examples
        for (int loop = 0; loop < 3; loop++) {
            Color color = null;
            int thickness = 1;
            
            gbc.insets.top = 10;
            String title = "Default Color, Thickness 1:";
            if (loop == 1) {
                thickness = 6;
                title = "Default Color, Thickness of 6:";
            } else if (loop > 1) {
                thickness = 6;
                color = new Color(0x7c9bcf);
                title = "Blue-gray Color, thickness of 6:";
            }
            contents.add(new JLabel(title), gbc);
            gbc.insets.top = gbc.insets.bottom;
            
            line = new HorizontalLine(HorizontalLine.ETCHED, thickness);
            if (color != null) {
                line.setLineColor(color);
            }
            addLabeledRow("Etched:", contents, line, gbc);
            
            line = new HorizontalLine(HorizontalLine.RAISED, thickness);
            if (color != null) {
                line.setLineColor(color);
            }
            addLabeledRow("Raised:", contents, line, gbc);
            
            line = new HorizontalLine(HorizontalLine.SOLID, thickness);
            if (color != null) {
                line.setLineColor(color);
            }
            addLabeledRow("Solid:", contents, line, gbc);
        }
        
        frame.pack();
        frame.setSize(300, frame.getHeight());
        frame.setVisible(true); 
 
    }
    
    private static void addLabeledRow(String text, Container container,
                                        HorizontalLine line, GridBagConstraints gbc) {
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.weightx = 0;
        container.add(new JLabel(text), gbc);
        
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        gbc.insets.bottom = gbc.insets.top;
        container.add(line, gbc);
    }
}
