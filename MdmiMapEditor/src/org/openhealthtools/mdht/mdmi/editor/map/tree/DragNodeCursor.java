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

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

/** Image for a drag-cursor - contains a supplied node image, with an arrow.
 * Special handling is required to make it look like the background of the cursor
 * is transparent. */
public class DragNodeCursor extends BufferedImage {
	private Robot     m_robot;
	
	private ImageIcon m_nodeImage;
	private String    m_text;
	
	private Point m_hotSpot = new Point(0, 8);
	
	// Screen Image - we'll use a section of it for the background
   private BufferedImage m_screenImage;
   
	public DragNodeCursor(ImageIcon nodeImage, String text) throws AWTException {
		// only 32x32 works correctly
		super(32, 32, BufferedImage.TYPE_INT_RGB);

		// capture the screen image
		createScreenImage();

		// save information
		m_nodeImage = nodeImage;
		if (text != null && text.length() > 0) {
			m_text = text.substring(0, 1);
		}
	}
	
	/** Get the hot spot used when creating a custom cursor */
	public Point getHotSpot() {
		return m_hotSpot;
	}
	
	/** Create the graphics for this image */
	private void createContent(Component component, Point mousePoint) {
		// translate mouse coordinates to screen coordinates
   	Point screenLocation = component.getLocationOnScreen();
   	screenLocation.translate(mousePoint.x, mousePoint.y);
   	
   	// take into account hot spot position
   	screenLocation.translate(- m_hotSpot.x, -m_hotSpot.y);
		
		// get the section of the screen image within the bounds
   	BufferedImage background = m_screenImage.getSubimage(screenLocation.x,
   			screenLocation.y, getWidth(), getHeight());
		
		Graphics2D g2D = createGraphics();
		
		// draw "transparent" background
		g2D.drawImage(background, 0, 0, null);

		// draw "node" image
		g2D.drawImage(m_nodeImage.getImage(), 0, 0, null);
		
		// label with first character
		if (m_text != null) {
			g2D.setColor(Color.black);
			int h = g2D.getFont().getSize();
			int x = m_nodeImage.getIconWidth()+4;
			int y = (h+m_nodeImage.getIconHeight())/2;
			g2D.drawString(m_text, x, y);
		}
		
		// draw an arrow with the tip just to the right of the icon               
		int x = m_nodeImage.getIconWidth() + 1;                     
		int y = m_nodeImage.getIconHeight()/2;                       
		Polygon arrow = createArrow(x, y);
		
		// shadow
		g2D.setColor(Color.gray);
		g2D.fillPolygon(arrow);
		arrow.translate(-2, -2);
		
		// inside
		g2D.setColor(Color.white);
		g2D.fillPolygon(arrow);
		
		// outline
		g2D.setColor(Color.black);
		g2D.drawPolygon(arrow);
		

	}
	
	/** Create an arrow shape, where the tip is at x, y */
	private Polygon createArrow(int x, int y) {
		Polygon poly = new Polygon();                         
                                  //  *                       
		poly.addPoint(x,   y);      //  * *                                            
		poly.addPoint(x+11,y+11);   //  *   *                    
		poly.addPoint(x+7, y+11);   //  *     *                   
		poly.addPoint(x+11,y+19);   //  * * *                   
		poly.addPoint(x+8, y+21);   //     * *                    
		poly.addPoint(x+4, y+12);   //      * *                    
		poly.addPoint(x,   y+16);   //       * *  
		
		return poly;
	}


	/** Capture the background image 
	 * @throws AWTException */
   private void createScreenImage() throws AWTException {
   	if (m_robot == null) {
   		m_robot = new Robot();
   	}

   	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
   	Rectangle screenRect = new Rectangle(0,0,screenSize.width,screenSize.height);

      m_screenImage = m_robot.createScreenCapture(screenRect);

   }
   
   /** Set the cursor each time the mouse position changes */
   public Cursor setCursor(Component component, Point mousePosition) {
		flush();
		createContent(component, mousePosition);

		Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(DragNodeCursor.this,
				m_hotSpot, "node");
		component.setCursor(cursor);
		
		return cursor;
   }

}