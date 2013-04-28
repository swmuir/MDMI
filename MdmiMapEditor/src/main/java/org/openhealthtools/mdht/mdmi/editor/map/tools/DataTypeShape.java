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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import org.openhealthtools.mdht.mdmi.editor.common.layout.Shapes;
import org.openhealthtools.mdht.mdmi.editor.common.layout.Link.ConnectionPoint;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;

/** Shape for a DataType - was barrel-shaped, now beveled rectangle */
public class DataTypeShape extends Shapes.RectangleShape {
	public static final Color s_defaultDatatypeColor = new Color(0x89a5c9);	// blue-gray

	//private static int s_depth = 12;
	private static int s_depth = 0;

	public DataTypeShape(MdmiDatatype datatype, int width, int height) {
		super(s_defaultDatatypeColor, width, height);
		setUserObject(datatype);
	}

	@Override
	public void outlineShape(Graphics g, int x1, int y1, int x2, int y2) {
		//      . ****** .    -- y1
		//     *.        .*   -- yA
		//    *   ******   *  
		//    *            *
		//    *            *
		//    *            *
		//    * . ****** . *  -- yB
		//     *.        .*   -- yC
		//    	 ******      -- y2
		int yA = y1+s_depth;
		int yB = y2-2*s_depth;
		int yC = yB+s_depth;
		
//		// top oval
//		g.drawOval(x1, y1, x2-x1, 2*s_depth);
		// top
		g.drawLine(x1, yA, x2, yA);
		
		// left side
		g.drawLine(x1, yA, x1, yC);
		// right side
		g.drawLine(x2, yA, x2, yC);
		
//		// bottom curve
//		g.drawArc(x1, yB, x2-x1, 2*s_depth, 0, -180);
		// bottom
		g.drawLine(x1, yC, x2, yC);
	}

	@Override
	public void fillShape(Graphics g, int x1, int y1, int x2, int y2) {
		//      . ****** .    -- y1
		//     *.        .*   -- yA
		//    *   ******   *  
		//    *            *
		//    *            *
		//    *            *
		//    * . ****** . *  -- yB
		//     *.        .*   -- yC
		//    	 ******      -- y2
		int yA = y1+s_depth;
		int yB = y2-2*s_depth;
		int yC = yB+s_depth;
		
//		// top oval
//		g.fillOval(x1, y1, x2-x1, 2*s_depth);
		// body
		g.fillRect(x1, yA, x2-x1, yC-yA);
//		g.fillArc(x1, yB, x2-x1, 2*s_depth, 0, -180);

		// add highlight/shading if drawing main body
		Color color = g.getColor();
		if (getDrawingState() == DrawingState.FillShape) {
			Color highlight = lighten(color, 0x1e);
			Color shadow = getShadowColor();
			// draw additional lines on left and right
			for (int i=0; i<6; i++) {
				g.setColor(highlight);
				g.drawLine(x1, yA, x1, yC);
				g.setColor(shadow);
				g.drawLine(x2, yA, x2, yC);
				
				x1++;
				x2--;
				yA++;
//				yC++;
				yC--;
			}

			g.setColor(color);
		}
	}

	@Override
	public Point2D getConnectionCoordinates(ConnectionPoint point) {
		Rectangle bounds = getBounds();;
		Point2D coordinates;
		switch (point) {
		case NorthWest:
			coordinates = new Point2D.Double(bounds.getMinX(), bounds.getMinY()+s_depth);
			break;
		case NorthEast:
			coordinates = new Point2D.Double(bounds.getMaxX(), bounds.getMinY()+s_depth);
			break;

		case SouthWest:
			coordinates = new Point2D.Double(bounds.getMinX(), bounds.getMaxY()-s_depth);
			break;
		case SouthEast:
			coordinates = new Point2D.Double(bounds.getMaxX(), bounds.getMaxY()-s_depth);
			break;
			
		default:
			// top left
			coordinates = super.getConnectionCoordinates(point);
		}
		return coordinates;
	}
	
	
}
