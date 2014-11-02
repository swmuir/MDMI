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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JPanel;
import javax.swing.text.StyleConstants;

import org.openhealthtools.mdht.mdmi.editor.common.layout.Link.Connection;
import org.openhealthtools.mdht.mdmi.editor.common.layout.Link.ConnectionPoint;

/** A generic labeled shape */
public abstract class Shape extends JPanel {

	protected enum DrawingState {
		None,
		FillShadow,
		FillShape,
		OutlineShape,
		ShowSelection,
		LabelText
	}
	private DrawingState m_drawingState = DrawingState.None;

	private boolean m_showSelection = false;
	private Color m_selectionColor = Color.blue;
	private int   m_selectionThickness = 3;
	private int   m_shadowThickness = 3;

	private List<TextInformation> m_textInformation = new ArrayList<TextInformation>();
	private Color m_color = Color.white; 
	private Color m_textColor = Color.black;
	
	private Object m_userObject = null;

	// Create a rectangle with this background color
	public Shape(Color color, int width, int height) {
		m_color = color;
		Dimension size = new Dimension(width, height);
		setPreferredSize(size);
		setSize(size);
	}
	
	/** Associate a user-object with this shape */
	public void setUserObject(Object userObject) {
		m_userObject = userObject;
	}
	
	public Object getUserObject() {
		return m_userObject;
	}

	/** get the minimum x-coordinate */
	public int getXmin() {
		return getX();
	}
	
	/** get the maximum x-coordinate (x+width) */
	public int getXmax() {
		return getX() + getWidth() - 1;
	}
	
	/** get the minimum y-coordinate */
	public int getYmin() {
		return getY();
	}
	
	/** get the maximum y-coordinate (y+height) */
	public int getYmax() {
		return getY() + getHeight() - 1;
	}


	/** Change the thickness of the shadow (default is 3) */
	public void setShadowThickness(int thickness) {
		m_shadowThickness = thickness;
	}

	/** get the thickness of the shadow (default is 3) */
	public int getShadowThickness() {
		return m_shadowThickness;
	}


	/** Change the thickness of the outline used to show selection (default is 3) */
	public void setSelectionThickness(int thickness) {
		m_selectionThickness = thickness;
	}

	/** get the thickness of the outline used to show selection (default is 3) */
	public int getSelectionThickness() {
		return m_selectionThickness;
	}
	
	/** Change the shape color */
	public void setColor(Color color) {
		m_color = color;
		repaint();
	}
	
	/** Change the text color */
	public void setTextColor(Color color) {
		m_textColor = color;
	}
	
	/** Get the color used to outline a selected shape */
	public Color getSelectionColor() {
		return m_selectionColor;
	}

	/** add a new line of text to be displayed in the center of the shape */
	public void addTextLine(String text) {
		addTextLine(text, StyleConstants.ALIGN_CENTER);
	}

	/** add a new line of text to be displayed in the center of the shape */
	public void addTextLine(String text, int alignment) {
		addTextLine(text, alignment, null);
	}

	/** add a new line of text to be displayed in the center of the shape */
	public void addTextLine(String text, Font font) {
		addTextLine(text, StyleConstants.ALIGN_CENTER, font);
	}

	/** add a new line of text to be displayed in the center of the shape */
	public void addTextLine(String text, int alignment, Font font) {
		TextInformation textInformation = new TextInformation();
		textInformation.text = (text == null) ? "" : text;
		textInformation.alignment = alignment;
		textInformation.font = font;

		m_textInformation.add(textInformation);
		pack();
	}

	/** add a new line of text at a specific location */
	public void insertTextLine(int pos, String text, int alignment, Font font) {
		TextInformation textInformation = new TextInformation();
		textInformation.text = (text == null) ? "" : text;
		textInformation.alignment = alignment;
		textInformation.font = font;

		m_textInformation.add(pos, textInformation);
		pack();
	}
	
	public int getLinesOfText() {
		return m_textInformation.size();
	}
	
	/** Add a separator after the previous line of text */
	public void addSeparator() {
		m_textInformation.add(new Separator());
		pack();
	}
	
	/** Is shape selected */
	public boolean isSelectionShowing() {
		return m_showSelection;
	}
	
	/** Turn selection outline on/off */
	public void showSelectionOutline(boolean show) {
		if (m_showSelection != show) {
			m_showSelection = show;
		}
	}

	/** Turn selection outline on/off */
	public void toggleSelectionOutline() {
		m_showSelection = !m_showSelection;
	}
	
	public Color getShadowColor() {
		return m_color;
	}
	
	public Color getFillColor() {
		int level = 0x44;
		return lighten(m_color, level);
	}
	
	/** lighten red, green and blue values of a color */
	public static Color lighten(Color color, int level) {
		int r = Math.min(color.getRed()+level, 255);
		int g = Math.min(color.getGreen()+level, 255);
		int b = Math.min(color.getBlue()+level, 255);
		return new Color(r, g, b);
	}
	
	public Color getOutlineColor() {
		return m_color.darker();
	}
	
	public Color getTextColor() {
		return m_textColor;
	}

	
	/** Horizontally align the center of this shape with the second shape */
	public void centerHorizontally(Shape otherShape, int y) {
		centerHorizontally(otherShape.getXmin(), otherShape.getXmax(), y);
	}
		
	/** Set this shape's position mid-way between the left and right coordinates */
	public void centerHorizontally(int leftX, int rightX, int y) {
		//left       |<-- w -->|       right
		//  |<- dx ->[XXXXXXXXX]<- dx ->|
		// 2*dx + w = (r - l)  -->   dx = (r - l - w)/2
		int w = getWidth()-1;
		int x = leftX + (rightX - leftX - w)/2;
		this.setLocation(x, y);
	}
		
	/** Veritacally align the center of this shape with the second shape */
	public void centerVertically(Shape otherShape, int x) {
		centerVertically(x, otherShape.getYmin(), otherShape.getYmax());
	}

	
	/** Set this shape's position mid-way between the top and bottom coordinates */
	public void centerVertically(int x, int topY, int bottomY) {
		int ht = getHeight()-1;
		int y = topY + (bottomY - topY - ht)/2;
		this.setLocation(x, y);
	}
	
	/** does shape have a shadow */
	public boolean hasShadow() {
		return true;
	}
	
	/** Draw the component. Derived classes need to provide <i>fillShape()</i>
	 * and <i>drawShape()</i> for this method.
	 * There are 5 steps in drawing the component. Each step has a corresponding
	 * drawing state that derived classes can test for:
	 * <ul>
	 * <li>FillShadow - fill in the shadow</li>
	 * <li>FillShape - fill in the shape</li>
	 * <li>OutlineShape - outline the shape</li>
	 * <li>ShowSelection - show the shape as selected</li>
	 * <li>LabelText - label the text</li>
	 * </ul>
	 */
	@Override
	public void paintComponent(Graphics g) {
		// A Simple Tutorial on Sizes
		//  In this example, the width is 15, and height is 13
		// 
		//   |<--------- width (15) ------>|
		//    x _ _ _ _ _ _ _ _ _ _ _ _ _ _     _
		//  y|1|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 1  ^
		//   |_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 2  |
		//   |_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 3  |
		//   |_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 4  |
		//   |_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 5  |
		//   |_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 6 height
		//   |_|_|_|_|_|_|_|C|_|_|_|_|_|_|_| 7 (13)
		//   |_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 8  |
		//   |_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 9  |
		//   |_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|10  |
		//   |_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|11  |
		//   |_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|12  |
		//   |_|_|_|_|_|_|_|_|_|_|_|_|_|_|2|13  _
		//    1 2 3 4 5 6 7 8 9 0 1 2 3 4 5
		//
		//  point 1, is at (x,y)
		//  point 2, is at (x+width-1, y+height-1)
		//  point C (mid-point), is at ((x+width)/2, (y+height)/2)
		//
		int x1 = 0;
		int y1 = 0;
		int x2 = x1 + getWidth() - 1;
		int y2 = y1 + getHeight() - 1;


		// fill in shadow
		//    _ _ _ _ _ _ _ _ _ _ _ _ _ _ _  
		//   |_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 1
		//   |_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 2
		//   |_|_|_|_|_|_|_|_|_|_|_|_|_|_|_| 3
		//   |_|_|_|=|=|=|=|=|=|=|=|=|=|=|=| 4
		//   |_|_|_|=|=|=|=|=|=|=|=|=|=|=|=| 5
		//   |_|_|_|=|=|=|=|=|=|=|=|=|=|=|=| 6
		//   |_|_|_|=|=|=|=|=|=|=|=|=|=|=|=| 7
		//   |_|_|_|=|=|=|=|=|=|=|=|=|=|=|=| 8
		//   |_|_|_|=|=|=|=|=|=|=|=|=|=|=|=| 9
		//   |_|_|_|=|=|=|=|=|=|=|=|=|=|=|=| 10
		//   |_|_|_|=|=|=|=|=|=|=|=|=|=|=|=| 11
		//   |_|_|_|=|=|=|=|=|=|=|=|=|=|=|=| 12
		//   |_|_|_|=|=|=|=|=|=|=|=|=|=|=|=| 13
		//    1 2 3 4 5 6 7 8 9 0 1 2 3 4 5
		if (hasShadow()) {
			m_drawingState = DrawingState.FillShadow;
			g.setColor(getShadowColor());
			fillShape(g, m_shadowThickness, m_shadowThickness, x2, y2);

			x2 -= m_shadowThickness;
			y2 -= m_shadowThickness;
		}

		// fill in shape
		//    _ _ _ _ _ _ _ _ _ _ _ _ _ _ _  
		//   |.|.|.|.|.|.|.|.|.|.|.|.|_|_|_| 1
		//   |.|.|.|.|.|.|.|.|.|.|.|.|_|_|_| 2
		//   |.|.|.|.|.|.|.|.|.|.|.|.|_|_|_| 3
		//   |.|.|.|.|.|.|.|.|.|.|.|.|=|=|=| 4
		//   |.|.|.|.|.|.|.|.|.|.|.|.|=|=|=| 5
		//   |.|.|.|.|.|.|.|.|.|.|.|.|=|=|=| 6
		//   |.|.|.|.|.|.|.|.|.|.|.|.|=|=|=| 7
		//   |.|.|.|.|.|.|.|.|.|.|.|.|=|=|=| 8
		//   |.|.|.|.|.|.|.|.|.|.|.|.|=|=|=| 9
		//   |.|.|.|.|.|.|.|.|.|.|.|.|=|=|=| 10
		//   |_|_|_|=|=|=|=|=|=|=|=|=|=|=|=| 11
		//   |_|_|_|=|=|=|=|=|=|=|=|=|=|=|=| 12
		//   |_|_|_|=|=|=|=|=|=|=|=|=|=|=|=| 13
		//    1 2 3 4 5 6 7 8 9 0 1 2 3 4 5
		m_drawingState = DrawingState.FillShape;
		g.setColor(getFillColor());
		fillShape(g, x1, y1, x2, y2);

		// draw single-pixel outline
		//    _ _ _ _ _ _ _ _ _ _ _ _ _ _ _  
		//   |#|#|#|#|#|#|#|#|#|#|#|#|_|_|_| 1
		//   |#|.|.|.|.|.|.|.|.|.|.|#|_|_|_| 2
		//   |#|.|.|.|.|.|.|.|.|.|.|#|_|_|_| 3
		//   |#|.|.|.|.|.|.|.|.|.|.|#|=|=|=| 4
		//   |#|.|.|.|.|.|.|.|.|.|.|#|=|=|=| 5
		//   |#|.|.|.|.|.|.|.|.|.|.|#|=|=|=| 6
		//   |#|.|.|.|.|.|.|.|.|.|.|#|=|=|=| 7
		//   |#|.|.|.|.|.|.|.|.|.|.|#|=|=|=| 8
		//   |#|.|.|.|.|.|.|.|.|.|.|#|=|=|=| 9
		//   |#|#|#|#|#|#|#|#|#|#|#|#|=|=|=| 10
		//   |_|_|_|=|=|=|=|=|=|=|=|=|=|=|=| 11
		//   |_|_|_|=|=|=|=|=|=|=|=|=|=|=|=| 12
		//   |_|_|_|=|=|=|=|=|=|=|=|=|=|=|=| 13
		//    1 2 3 4 5 6 7 8 9 0 1 2 3 4 5
		m_drawingState = DrawingState.OutlineShape;
		g.setColor(getOutlineColor());
		outlineShape(g, x1, y1, x2, y2);
		
		if (isSelectionShowing()) {
			// draw thick outline
			//    _ _ _ _ _ _ _ _ _ _ _ _ _ _ _  
			//   |%|%|%|%|%|%|%|%|%|%|%|%|_|_|_| 1
			//   |%|%|%|%|%|%|%|%|%|%|%|%|_|_|_| 2
			//   |%|%|%|%|%|%|%|%|%|%|%|%|_|_|_| 3
			//   |%|%|%|.|.|.|.|.|.|%|%|%|=|=|=| 4
			//   |%|%|%|.|.|.|.|.|.|%|%|%|=|=|=| 5
			//   |%|%|%|.|.|.|.|.|.|%|%|%|=|=|=| 6
			//   |%|%|%|.|.|.|.|.|.|%|%|%|=|=|=| 7
			//   |%|%|%|%|%|%|%|%|%|%|%|%|=|=|=| 8
			//   |%|%|%|%|%|%|%|%|%|%|%|%|=|=|=| 9
			//   |%|%|%|%|%|%|%|%|%|%|%|%|=|=|=| 10
			//   |_|_|_|=|=|=|=|=|=|=|=|=|=|=|=| 11
			//   |_|_|_|=|=|=|=|=|=|=|=|=|=|=|=| 12
			//   |_|_|_|=|=|=|=|=|=|=|=|=|=|=|=| 13
			//    1 2 3 4 5 6 7 8 9 0 1 2 3 4 5
			m_drawingState = DrawingState.ShowSelection;
			g.setColor(m_selectionColor);
			for (int i=0; i<m_selectionThickness; i++) {
				outlineShape(g, x1, y1, x2, y2);

				x1++;
				y1++;
				x2--;
				y2--;
			}
		}

		// label in the center
		m_drawingState = DrawingState.LabelText;
		g.setColor(m_textColor);
		Rectangle labelRect = new Rectangle(x1, y1, x2-x1+1, y2-y1+1);
		drawLabel(g, m_textInformation, labelRect);

		m_drawingState = DrawingState.None;
	}
	
	/** Show the state of the <i>paintComponent</i> method when a call is made to fillShape() or
	 * outlineShape().
	 */
	protected DrawingState getDrawingState() {
		return m_drawingState;
	}

	/** Draw the shape outline, where top left is (x1,y1), and bottom right is (x2,y2) */
	public abstract void outlineShape(Graphics g, int x1, int y1, int x2, int y2);

	/** Fill the shape, where top left is (x,y), and bottom right is (x2,y2)*/
	public abstract void fillShape(Graphics g, int x1, int y1, int x2, int y2);

	/** Draw a label centered in the rectangle */
	protected void drawLabel(Graphics g, List<TextInformation> textInfoList, Rectangle rect) {
		Font defaultFont = getFont();
		// calculate text height
		int textHeight = 0;
		for (TextInformation textInfo : textInfoList) {
			FontMetrics fm = getFontMetrics(textInfo.font == null ? defaultFont : textInfo.font);
			textHeight += fm.getHeight();
		}
		
		int baseline = rect.y + (rect.height - textHeight)/2;
		for (TextInformation textInfo : textInfoList) {
			g.setFont(textInfo.font == null ? defaultFont : textInfo.font);
			FontMetrics fm = getFontMetrics(g.getFont());
			int yPos = baseline + fm.getAscent();

			if (textInfo instanceof Separator) {
				// draw a horizontal line
				yPos -= fm.getHeight()/2;

				drawSeparator(g, rect.x+3, yPos, rect.width-6);
			} else {
				int xPos;
				int textLength = fm.stringWidth(textInfo.text);
				if (textInfo.alignment == StyleConstants.ALIGN_LEFT) {
					xPos = rect.x + m_selectionThickness;
				} else if (textInfo.alignment == StyleConstants.ALIGN_RIGHT) {
					xPos = rect.x + rect.width - textLength - m_selectionThickness;	
				} else {
					// center
					xPos = rect.x + (rect.width - textLength)/2;	
				}
				
				Color lineColor = g.getColor();
				// over-draw text in background color
				g.setColor(getFillColor());
				for (int i=-1; i<2; i++) {
					for (int j=-1; j<2; j++) {
						g.drawString(textInfo.text, xPos+i, yPos+j);
					}
				}
				
				g.setColor(lineColor);
				g.drawString(textInfo.text, xPos, yPos);
			}
			
			baseline += fm.getHeight();
		}
		
		// restore font
		g.setFont(defaultFont);
	}

	/**
	 * @param g
	 * @param x	x-position
	 * @param y	y-position
	 * @param length line length
	 */
	private void drawSeparator(Graphics g, int x, int y, int length) {
		g.drawLine(x, y, x+length, y);
	}
	
	/** Split a single line of text into multiple lines when a line-break is found */
	public static List<String> splitIntoLines(String text) {
		List<String> lines = new ArrayList<String>();
		StringTokenizer tok = new StringTokenizer(text, "\n\r");
		while(tok.hasMoreTokens()) {
			lines.add(tok.nextToken());
		}
		
		return lines;
	}

	/** Get a line from the connection point of the <i>fromShape</i> to
	 *  the connection point of this shape */
	public Line2D getLineFrom(Connection fromConnection, ConnectionPoint toConnectionPoint) {
		Point2D fromPoint = fromConnection.shape.getConnectionCoordinates(fromConnection.point);
		Point2D toPoint = getConnectionCoordinates(toConnectionPoint);
		Line2D line;
		line = new Line2D.Double(fromPoint.getX(), fromPoint.getY(),
				toPoint.getX(), toPoint.getY());
		
		return line;
	}
	
	/** return the coordinates of this conneciton point */
	public Point2D getConnectionCoordinates(ConnectionPoint point) {
		Rectangle bounds = getBounds();
		Point2D coordinates;
		switch (point) {
		case NorthWest:
			coordinates = new Point2D.Double(bounds.getMinX(), bounds.getMinY());
			break;
		case North:
			coordinates = new Point2D.Double(bounds.getCenterX(), bounds.getMinY());
			break;
		case NorthEast:
			coordinates = new Point2D.Double(bounds.getMaxX(), bounds.getMinY());
			break;

		case West:
			coordinates = new Point2D.Double(bounds.getMinX(), bounds.getCenterY());
			break;
		case Center:
			coordinates = new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
			break;
		case East:
			coordinates = new Point2D.Double(bounds.getMaxX(), bounds.getCenterY());
			break;

		case SouthWest:
			coordinates = new Point2D.Double(bounds.getMinX(), bounds.getMaxY());
			break;
		case South:
			coordinates = new Point2D.Double(bounds.getCenterX(), bounds.getMaxY());
			break;
		case SouthEast:
			coordinates = new Point2D.Double(bounds.getMaxX(), bounds.getMaxY());
			break;
			
		default:
			// top left
			coordinates = new Point2D.Double(bounds.getMinX(), bounds.getMinY());
		}
		return coordinates;
	}
	
	
	/** Get the point where a line which passes from the other shape
	 * to this Center of this shape intersects the outside edge.
	 * @param lineToCenter
	 * @param bounds
	 * @return
	 */
	public Point2D getIntersection(Line2D lineToCenter) {
		Rectangle bounds = getBounds();
		
		// other shape's origin
		double x1 = lineToCenter.getX1();
		double y1 = lineToCenter.getY1();
		
		// our center-point
		double xC = lineToCenter.getX2();
		double yC = lineToCenter.getY2();
		
		double deltaX = xC - x1;
		double deltaY = yC - y1;
		
		//////////////////////////////
		// check top and bottom edges
		//////////////////////////////
		Line2D edge;
		if (deltaY > 0) {
			// top edge
			edge = new Line2D.Double(bounds.getX(), bounds.getY(), bounds.getMaxX(), bounds.getY());
		} else {
			// bottom edge
			edge = new Line2D.Double(bounds.getX(), bounds.getMaxY(), bounds.getMaxX(), bounds.getMaxY());
		}
		
		// intersection point
		Point2D point = getIntersection(lineToCenter, edge);
		
		// is it out-of bounds
		if (point == null ||  point.getX() < edge.getX1() || edge.getX2() < point.getX()) {
			//////////////////////////////
			// check left and right edges
			//////////////////////////////
			if (deltaX > 0) {
				// left edge
				edge = new Line2D.Double(bounds.getX(), bounds.getY(), bounds.getX(), bounds.getMaxY());
			} else {
				// right edge
				edge = new Line2D.Double(bounds.getMaxX(), bounds.getY(), bounds.getMaxX(), bounds.getMaxY());
			}
			point = getIntersection(lineToCenter, edge);
			if (point == null ||  point.getY() < edge.getY1() || edge.getY2() < point.getY()) {
				point = null;
			}
		}
		return point;
	}
	
	/** Get the intersection between two lines */
	public static Point2D getIntersection(Line2D lineA, Line2D lineB) {
		
		Point2D cp = null;

		// line 1: a1x + b1y + c1 = 0
		double a1 = lineA.getY2()-lineA.getY1();
		double b1 = lineA.getX1()-lineA.getX2();
		double c1 = lineA.getX2()*lineA.getY1()-lineA.getX1()*lineA.getY2();
		
		// line 2: a2x + b2y + c2 = 0
		double a2 = lineB.getY2()-lineB.getY1();
		double b2 = lineB.getX1()-lineB.getX2();
		double c2 = lineB.getX2()*lineB.getY1()-lineB.getX1()*lineB.getY2();

		double delta = a1*b2 - a2*b1;
		if (delta != 0) {
			cp = new Point2D.Double((b1*c2 - b2*c1)/delta,
					(a2*c1 - a1*c2)/delta);
		} else {
			// lines are parallel
		}
		return cp;
	}

	public static class TextInformation {
		public String text;
		public int    alignment = StyleConstants.ALIGN_CENTER;
		public Font   font = null;
	}
	
	private static class Separator extends TextInformation {
		public Separator() {
			text = "------";
		}
	}

	/** Enlarge this shape to fit the text */
	private void pack() {
		Rectangle drawingSize = getBounds();
		drawingSize.width -= m_shadowThickness;
		drawingSize.height -= m_shadowThickness;

		drawingSize.width -= 2*m_selectionThickness;
		drawingSize.height -= 2*m_selectionThickness; 

		Font defaultFont = getFont();
		
		int heightMax = drawingSize.height;
		int widthMax = drawingSize.width;

		int yPos = drawingSize.y;
		
		for (TextInformation textInfo : m_textInformation) {
			Font font = textInfo.font == null ? defaultFont : textInfo.font;
			FontMetrics fm = getFontMetrics(font);
			
			// adjust y-position
			yPos += fm.getHeight(); 

			int textWidth = fm.stringWidth(textInfo.text);
			// check width
			widthMax = Math.max(widthMax, textWidth);
		}
		heightMax = Math.max(heightMax, yPos);


		// if text is bigger then space provided, we need to resize

		Rectangle newBounds = getBounds();
		boolean resizeNeeded = false;
		if (widthMax > drawingSize.width) {
			newBounds.width += (widthMax - drawingSize.width);
			// drawing works better if size is odd
			if (newBounds.width % 2 == 0) {
				newBounds.width++;
			}
			resizeNeeded = true;
		}
		if (heightMax > drawingSize.height) {
			newBounds.height += (heightMax - drawingSize.height);
			// drawing works better if size is odd
			if (newBounds.height % 2 == 0) {
				newBounds.height++;
			}
			resizeNeeded = true;
		}
		if (resizeNeeded) {
			setBounds(newBounds);
			repaint(newBounds);
		}

	}
}