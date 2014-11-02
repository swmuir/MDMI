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
package org.openhealthtools.mdht.mdmi.editor.common.layout;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import org.openhealthtools.mdht.mdmi.editor.common.layout.Link.ConnectionPoint;

/** A collection of Shape classes */
public class Shapes {


	////////////////////////////////////////////////////
	//     Rectangle
	///////////////////////////////////////////////////
	public static class RectangleShape extends Shape {

		// creat a square with this background color
		public RectangleShape(Color color, int width) {
			super(color, width, width);
		}

		// creat a rectangle with this background color
		public RectangleShape(Color color, int width, int height) {
			super(color, width, height);
		}

		@Override
		public void outlineShape(Graphics g, int x1, int y1, int x2, int y2) {
			g.drawRect(x1, y1, x2-x1, y2-y1);
		}

		@Override
		public void fillShape(Graphics g, int x1, int y1, int x2, int y2) {
			g.fillRect(x1, y1, x2-x1+1, y2-y1+1);
		}
	}

	////////////////////////////////////////////////////
	//     Circle
	///////////////////////////////////////////////////
	public static class Circle extends Shape {
		
		// creat a circle with this background color
		public Circle(Color color, int diameter) {
			super(color, diameter, diameter);
		}


		@Override
		public void outlineShape(Graphics g, int x1, int y1, int x2, int y2) {
			g.drawOval(x1, y1, x2-x1, y2-y1);
		}

		@Override
		public void fillShape(Graphics g, int x1, int y1, int x2, int y2) {
			g.fillOval(x1, y1, x2-x1+1, y2-y1+1);
		}


		/** Get the point where a line which passes from the other shape
		 * to this Center of this shape intersects the outside edge.
		 * @param lineToCenter
		 * @param bounds
		 * @return
		 */
		@Override
		public Point2D getIntersection(Line2D lineToCenter) {
			
			// line origin (other shape)
			double x1 = lineToCenter.getX1();
			double y1 = lineToCenter.getY1();
			
			// line target (shape's center)
			double xC = lineToCenter.getX2();
			double yC = lineToCenter.getY2();
			
			double deltaX = xC - x1;
			double deltaY = yC - y1;
						
			// point of intersection
			double xp, yp;
			
			double h = getHeight()/2.0;
			double w = getWidth()/2.0;
			
			// elipse equation is:
			//  (x - xC)^2/w^2  +  (y - yC)^2/h^2  = 1
			
			
			if (deltaX == 0) {
				// vertical line
				double sign = (deltaY > 0) ? -1 : 1;
				xp = xC;
				yp = yC + sign*h;
			} else {
				// if line is Y = mX + b
				// then intersection is:
				//    Xp = Xc +- (w*h)/sqrt(h^2+m^2*w^2)
				//    Yp = Yc + m*(Xp-Xc)
				double sign = (deltaX > 0) ? -1 : 1;
				double m = deltaY/deltaX;
				double sqrtTerm = Math.sqrt(h*h + m*m*w*w);
				
				double xOffset = sign*(w*h)/sqrtTerm;
				xp = xC + xOffset;
				yp = yC + m*xOffset;
				
			}

			return new Point2D.Double(xp, yp);
		}

		/** return the coordinates of this conneciton point */
		@Override
		public Point2D getConnectionCoordinates(ConnectionPoint point) {
			Rectangle bounds = getBounds();
			double sqrt2 = Math.sqrt(2.0);
			double deltaX = getWidth()/2.0/sqrt2;
			double deltaY = getHeight()/2.0/sqrt2;
			Point2D coordinates;
			switch (point) {
			case NorthWest:
				coordinates = new Point2D.Double(bounds.getCenterX()-deltaX,
						bounds.getCenterY()-deltaY);
				break;
			case NorthEast:
				coordinates = new Point2D.Double(bounds.getCenterX()+deltaX,
						bounds.getCenterY()-deltaY);
				break;

			case SouthWest:
				coordinates = new Point2D.Double(bounds.getCenterX()-deltaX,
						bounds.getCenterY()+deltaY);
				break;
			case SouthEast:
				coordinates = new Point2D.Double(bounds.getCenterX()+deltaX,
						bounds.getCenterY()+deltaY);
				break;
				
			default:
				coordinates = super.getConnectionCoordinates(point);
			}
			return coordinates;
		}

	}
	


	////////////////////////////////////////////////////
	//     Diamond
	///////////////////////////////////////////////////
	public static class Diamond extends Shape {
		
		// creat a diamond shape with this background color
		public Diamond(Color color, int width, int height) {
			super(color, width, height);
		}

		/** return a polygon marking the corners of a diamond with top left
		 * at x1, y1, and bottom right at x2, y2.
		 */
		private Polygon getCorners(int x1, int y1, int x2, int y2) {
			//    
			//        o p0
			//       / \
			//      /   \
			//  p3 o     o p1
			//      \   /
			//       \ /
			//        o p2
			Polygon poly = new Polygon();
			
			int xMid = (x1 + x2)/2;
			int yMid = (y1 + y2)/2;
			poly.addPoint(xMid, y1);
			poly.addPoint(x2, yMid);
			poly.addPoint(xMid, y2);
			poly.addPoint(x1, yMid);
			
			return poly;
		}

		@Override
		public void outlineShape(Graphics g, int x1, int y1, int x2, int y2) {
			g.drawPolygon(getCorners(x1, y1, x2, y2));
		}

		@Override
		public void fillShape(Graphics g, int x1, int y1, int x2, int y2) {
			g.fillPolygon(getCorners(x1, y1, x2, y2));
		}

		/** Get the point where a line which passes from the other shape
		 * to this Center of this shape intersects the outside edge.
		 * @param lineToCenter
		 * @param bounds
		 * @return
		 */
		@Override
		public Point2D getIntersection(Line2D lineToCenter) {
			Rectangle bounds = getBounds();
			
			// other shape's origin
			double x0 = lineToCenter.getX1();
			double y0 = lineToCenter.getY1();
			
			// our center-point
			double xC = lineToCenter.getX2();
			double yC = lineToCenter.getY2();
			
			//////////////////////////////
			// check left edges
			//////////////////////////////
			Line2D edge;
			if (x0 < xC) {
				// left edges
				if (y0 < yC) {
					// top left
					edge = new Line2D.Double(bounds.getX(), yC, xC, bounds.getY());
				} else {
					// bottom left
					edge = new Line2D.Double(bounds.getX(), yC, xC, bounds.getMaxY());
				}
			} else {
				// right edges
				if (y0 < yC) {
					// top right
					edge = new Line2D.Double(bounds.getMaxX(), yC, xC, bounds.getY());
				} else {
					// bottom right
					edge = new Line2D.Double(bounds.getMaxX(), yC, xC, bounds.getMaxY());
				}
			}
			
			// intersection point
			Point2D point = getIntersection(lineToCenter, edge);

			return point;
		}

		/** return the coordinates of this connection point */
		@Override
		public Point2D getConnectionCoordinates(ConnectionPoint point) {
			Rectangle bounds = getBounds();
			double xC = bounds.getCenterX();
			double yC = bounds.getCenterY();
			
			// xMin
			// |  xC   xMax
			// |   |   |
			// |   o   |--yMin
			// |  / \  |
			//  NW   NE 
			//  /     \
			// o       o--yC
			//  \     /
			//  SW   SE
			//    \ /
			//     o ----yMax
			
			Point2D coordinates;
			switch (point) {
			case NorthWest:
				coordinates = new Point2D.Double((bounds.getMinX()+xC)/2, (bounds.getMinY()+yC)/2);
				break;
			case NorthEast:
				coordinates = new Point2D.Double((bounds.getMaxX()+xC)/2, (bounds.getMinY()+yC)/2);
				break;

			case SouthWest:
				coordinates = new Point2D.Double((bounds.getMinX()+xC)/2, (bounds.getMaxY()+yC)/2);
				break;
			case SouthEast:
				coordinates = new Point2D.Double((bounds.getMaxX()+xC)/2, (bounds.getMaxY()+yC)/2);
				break;
				
			default:
				coordinates = super.getConnectionCoordinates(point);
			}
			return coordinates;

		}

	}
	
	//////////////////////////////////////////////////
	//   Label
	/////////////////////////////////////////////////
	/** A text-only label with a white background. The label will grow to 
	 * fit the text.
	 */
	public static class Label extends RectangleShape {

		/** Bold label */
		public Label(String text) {
			this(text, Font.BOLD);
		}

		/** Label with specified font style */
		public Label(String text, int fontStyle) {
			super(Color.white, 1, 1);
			addTextLine(text, getFont().deriveFont(fontStyle));
		}
		
		@Override
		public boolean hasShadow() {
			return false;
		}

		@Override
		public void outlineShape(Graphics g, int x1, int y1, int x2, int y2) {
			// only draw outline if highlighting selection
			if (getDrawingState() == DrawingState.ShowSelection) {
				super.outlineShape(g, x1, y1, x2, y2);
			}
		}
	}
	
	
	//////////////////////////////////////////////////
	//   LinkPoint
	/////////////////////////////////////////////////
	/** A small circle that can be used as an intermediate point when connecting
	 * two other shapes. 
	 */
	public static class LinkPoint extends Circle {
		private static final int s_padding = 3;

		/** Default to a 3-pixel circle */
		public LinkPoint() {
			this(Color.black, 3);
		}

		/** Default to a 3-pixel circle */
		public LinkPoint(Color color) {
			this(color, 3);
		}

		public LinkPoint(Color color, int diameter) {
			// add padding that shows when selected
			super(color, diameter + 2*s_padding);
			setSelectionThickness(1);
		}
		
		@Override
		public boolean hasShadow() {
			return false;
		}
		
		@Override
		public void fillShape(Graphics g, int x1, int y1, int x2, int y2) {
			// adjust for padding
			super.fillShape(g, x1+s_padding, y1+s_padding, x2-s_padding, y2-s_padding);
		}

		@Override
		public void outlineShape(Graphics g, int x1, int y1, int x2, int y2) {
			// adjust for padding
			if (getDrawingState() == DrawingState.OutlineShape) {
				super.outlineShape(g, x1+s_padding, y1+s_padding, x2-s_padding, y2-s_padding);
			} else if (getDrawingState() == DrawingState.ShowSelection) {
				super.outlineShape(g, x1, y1, x2, y2);
			}
		}
		
	}
}
