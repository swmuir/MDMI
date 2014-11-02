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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.text.StyleConstants;

import org.openhealthtools.mdht.mdmi.editor.common.layout.Link.ArrowShape;
import org.openhealthtools.mdht.mdmi.editor.common.layout.Link.Connection;
import org.openhealthtools.mdht.mdmi.editor.common.layout.Link.ConnectionPoint;

public class RelationshipView extends JPanel implements MouseListener, MouseMotionListener {
	private static final BasicStroke DASHED_LINE = new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER,
						1, new float[] {2, 3}, 0);
	
	// links between components
	private List<Link> m_links = new ArrayList<Link>();
	private List<Shape> m_shapes = new ArrayList<Shape>();
	
	// selection information
	private List<Shape> m_componentsSelected = new ArrayList<Shape>();
	private Point m_mouseDownPoint = null;
	private Point m_mouseDragPoint = null;
	
	private boolean m_allowsMultipleSelection = true;

	public RelationshipView() {
		setLayout(null);
		setBackground(Color.white);
	}
	
	/** Add contents of provided view to this view */
	public void transferContents(RelationshipView other, int xOffset, int yOffset) {
		// Shapes
		for (Component child : other.getComponents()) {
			Point childPoint = child.getLocation();
			// remove, move, add
			other.remove(child);
			childPoint.x += xOffset;
			childPoint.y += yOffset;
			child.setLocation(childPoint);
			add(child);
		}
		// Links
		m_links.addAll(other.m_links);
		other.m_links.clear();
	}
	
	/** Allow multiple selection (default is true) */
	public void setAllowsMultipleSelection(boolean multiple) {
		m_allowsMultipleSelection = multiple;
	}
	
	/** Can multiple shapes be selected */
	public boolean allowsMultipleSelection() {
		return m_allowsMultipleSelection;
	}
	
	public List<Shape> getShapes() {
		return m_shapes;
	}
	
	/** Add a shape */
	public void add(Shape shape) {
		m_shapes.add(shape);
		super.add(shape);
	}

	/** Add a link between two components */
	public void addLink(Connection from, Connection to) {
		addLink(new Link(from, to));
	}

	/** Add a link between two components */
	public void addLink(Link link) {
		m_links.add(link);
	}

	@Override
	public void addNotify() {
		super.addNotify();
		addMouseListener(this);
		addMouseMotionListener(this);
		// add mouse listener to each child
		for (Component child : getComponents()) {
			child.addMouseListener(this);
			child.addMouseMotionListener(this);
		}
	}

	@Override
	public void removeNotify() {
		removeMouseListener(this);
		removeMouseMotionListener(this);
		// remove mouse listener to each child
		for (Component child : getComponents()) {
			child.removeMouseListener(this);
			child.removeMouseMotionListener(this);
		}
		super.removeNotify();
	}
	
	/** get the smallest rectangle that contains all components */
	public Rectangle getMinimumBounds() {
		Rectangle rect = new Rectangle();
		for (Component child : getComponents()) {
			Rectangle childRect = child.getBounds();
			rect.x = Math.min( rect.x, childRect.x );
			rect.y = Math.min( rect.y, childRect.y );
			
			int maxX = Math.max((rect.x + rect.width), (childRect.x + childRect.width));
			int maxY = Math.max((rect.y + rect.height), (childRect.y + childRect.height));
			
			rect.width = maxX - rect.x;
			rect.height = maxY - rect.y;
		}
		return rect;
	}

	@Override
	public Dimension getPreferredSize() {
		Rectangle components = getMinimumBounds();

		// make it just a bit bigger than it needs to be
		Dimension pref = new Dimension(components.x + components.width + 50, 
				components.y +  components.height + 50);

		return pref;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		// show drag outline on top of eveything
		if (m_mouseDownPoint != null && m_mouseDragPoint != null) {
			Stroke stroke = ((Graphics2D)g).getStroke();
			((Graphics2D)g).setStroke(DASHED_LINE);
			g.setColor(Color.gray);
			
			Rectangle rect = getRectangle(m_mouseDownPoint, m_mouseDragPoint);
			g.drawRect(rect.x, rect.y, rect.width, rect.height);
			
			((Graphics2D)g).setStroke(stroke);
		}
		
	}
	
	@Override
	public void paintComponent(Graphics g) {

		super.paintComponent(g);
		
		// draw a grid 
//		showGrid(g);
		
		// show links
		for (Link link : m_links) {
			paintLink(g, link);
		}
	}
	
	/** Get a rectangle bound by two points */
	private static Rectangle getRectangle(Point p1, Point p2) {
		int x = Math.min(p1.x, p2.x);
		int width = Math.abs(p1.x - p2.x);
		int y = Math.min(p1.y, p2.y);
		int height = Math.abs(p1.y - p2.y);
		return new Rectangle(x, y, width, height);
	}

	/** overlay a grid (10-pixels)
	 * @param g
	 */
	@SuppressWarnings("unused")
	private void showGrid(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		Color minor = Color.lightGray;
		Color major = Color.gray;
		for (int y=0; y<=height; y+= 10) {
			// horizontal lines
			g.setColor(minor);
			if (y % 50 == 0) g.setColor(major);
			g.drawLine(0, y, width, y);
		}
		for (int x=0; x<=width; x+= 10) {
			// vertical lines
			g.setColor(minor);
			if (x % 50 == 0) g.setColor(major);
			g.drawLine(x, 0, x, height);
		}
	}
	
	/** Draw a line linking the two endpoints on the link.
	 * Show an arrow on the "tail" */
	protected void paintLink(Graphics g, Link link) {
		//  _______
		// | head  |
		// |   +   | 
		// |_____._|
		//         .
		//           #________
		//          |  . tail |    
		//          |    +    |    
		//          |_________|
		//

		Shape tailShape = link.tail.shape;

		// Simple version - just connect with single line from head to tail
		g.setColor(Color.DARK_GRAY);
		Line2D line = tailShape.getLineFrom(link.head, link.tail.point);
		((Graphics2D)g).draw(line);

		// draw a mark at the intersection with tail shape
		drawTailMark(g, link.head, link.tail);

		// draw a mark at the intersection with head shape
		drawTailMark(g, link.tail, link.head);		
	}

	/** Draw a mark on the tail where a line from the head intersects the tail.
	 * <code>
	 *    ------             ------
	 *   | head |---------<>| tail |
	 *    ------             ------
	 * </code>
	 * @param line
	 * @param bounds
	 */
	protected void drawTailMark(Graphics g, Connection head, Connection tail) {
		
		if (tail.arrow == ArrowShape.None) {
			return;
		}
		
		Shape tailShape = tail.shape;
		Line2D  line = tailShape.getLineFrom(head, tail.point);
		Point2D point = line.getP2();
		if (tail.point == ConnectionPoint.Center) {
			point = tailShape.getIntersection(line);
		}

		// draw an arrow at the intersection point, p
		//
		//    Diamond   Triangle   Arrow     Circle
		//      |-w-|
		//
		// -    p o         o         o         o
		// |     / \       / \       /|\     /     \
		// h  1 o   o 3   /   \     / | \   o       o
		// |     \ /     o_____o   o  |  o   \     /
		// -    2 o         |         |         o
		//        |         |         |         |
		//        |         |         |         |
		//
		if (point != null) {
			double x1, x2, x3;
			double y1, y2, y3;
			
			
			// arrow dimensions
			int height = 10;
			int width  = 8;
			ArrowShape arrowType = tail.arrow;
			if (arrowType == ArrowShape.Diamond) {
				height = 10;
				width = 6;
			} else if (arrowType == ArrowShape.Triangle || arrowType == ArrowShape.Arrow) {
				height = 12;
				width = 10;
			} else if (arrowType == ArrowShape.Circle) {
				height = 8;
				width = 8;
			}

			double deltaX = line.getX2() - line.getX1();
			double deltaY = line.getY2() - line.getY1();
			
			/////////////////////////////////////////////////////////////////
			// Calculate X2, Y2 -
			//  find a point along the line that's height pixels from intersection point
			if (deltaX == 0) {
				// vertical line
				double sign = (deltaY > 0) ? -1 : 1;
				x2 = point.getX();
				y2 = point.getY() + sign*height;
			} else {
				double sign = (deltaX > 0) ? -1 : 1;
				double m = deltaY/deltaX;
				double mSquared = m*m;
				double sqrtOnePlusMsquared = Math.sqrt(1+mSquared);
				double xOffset = sign*(height/sqrtOnePlusMsquared);
				x2 = point.getX() + xOffset;
				y2 = point.getY() + m*xOffset;
			}

			/////////////////////////////////////////////////////////////////
			// Calculate X1,Y1 and X3,Y3
			//
			// first, find center point on line between point and x2,y2
			double xC = (x2 + point.getX())/2.0;
			double yC = (y2 + point.getY())/2.0;

			int depth = width/2;
			// perpendicular line (slope is -1/m)
			if (deltaY == 0) {
				// vertical perpendicular
				x1 = xC;
				y1 = yC + depth;
				
				x3 = xC;
				y3 = yC - depth;
			} else {
				double m = -deltaX/deltaY;
				double mSquared = m*m;
				double sqrtOnePlusMsquared = Math.sqrt(1+mSquared);
				double xOffset = depth/sqrtOnePlusMsquared;
				x1 = xC + xOffset;
				y1 = yC + m*xOffset;
				
				x3 = xC - xOffset;
				y3 = yC - m*xOffset;
			}

			///////////////////////////////////////////////////
			// Draw Arrow
			//  define a polygon for drawing the arrow
			Polygon polygon = new Polygon();
			polygon.addPoint((int)point.getX(), (int)point.getY());
			polygon.addPoint((int)x1, (int)y1);
			if (arrowType == ArrowShape.Triangle) {
				polygon.addPoint((int)xC, (int)yC);
			} else {
				polygon.addPoint((int)x2, (int)y2);
			}
			polygon.addPoint((int)x3, (int)y3);
			
			if (arrowType == ArrowShape.Circle) {
				// find enclosing rectangle
				Rectangle polygonBounds = polygon.getBounds();
				g.fillOval(polygonBounds.x, polygonBounds.y, polygonBounds.width, polygonBounds.height);
				g.drawOval(polygonBounds.x, polygonBounds.y, polygonBounds.width, polygonBounds.height);
				
			} else if (arrowType == ArrowShape.Arrow) {
				// draw with two lines
				g.drawLine((int)x1, (int)y1, (int)point.getX(), (int)point.getY());
				g.drawLine((int)x3, (int)y3, (int)point.getX(), (int)point.getY());
			} else {
				// draw with polygon
				g.fillPolygon(polygon);
				g.drawPolygon(polygon);
			}
		}
	}



	/** Show a popup menu on the component */
	private void showPopup(MouseEvent e) {
		if (m_componentsSelected.size() == 1) {
			// only applies if single selection
			JPopupMenu popupMenu = createPopupMenu(m_componentsSelected.get(0));
			if (popupMenu != null) {
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	/** Create a popup menu based on the selected component */
	protected JPopupMenu createPopupMenu(Component selected) {
		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.add(new BringToFrontAction(selected));
		popupMenu.add(new SendToBackAction(selected));
		return popupMenu;
	}

	/** turn the outline on, or off, on all selected component */
	private void outlineAllSelections(boolean outline) {
		for (Shape shape : m_componentsSelected) {
			outlineShape(shape, outline);
		}
	}

	/** turn the outline on, or off, on the selected component */
	private void outlineShape(Shape shape, boolean outline) {
		shape.showSelectionOutline(outline);
		repaintWhenOutlined(shape);
	}
	
	/** repaint this component when outlined, plus a bit extra, to accomodate arrows */
	private void repaintWhenOutlined(Component component) {
		int pad = 10;
		Rectangle componentRect = component.getBounds();
		repaint(componentRect.x-pad, componentRect.y-pad, 
				componentRect.width+2*pad,componentRect.height+2*pad);
	
	}

	///////////////////////////////////////////////////
	// Mouse Motion Listener Methods 
	//////////////////////////////////////////////////

	@Override
	public void mouseDragged(MouseEvent e) {
		if (m_mouseDownPoint == null) {
			return;
		}
		// adjust component position
		if (m_componentsSelected.size() > 0) {
			m_mouseDragPoint = null;
			Point newPoint = e.getPoint();
			int deltaX = newPoint.x - m_mouseDownPoint.x;
			int deltaY = newPoint.y - m_mouseDownPoint.y;

			for (Shape shape : m_componentsSelected) {
				Point oldLocation = shape.getLocation();
				// adjust location, but don't move too far up or left
				Point newLocation = new Point(
						Math.max(oldLocation.x + deltaX, 0),
						Math.max(oldLocation.y + deltaY, 0) );

				// move component
				shape.setLocation(newLocation);
			}

			repaint();
			
		} else {
			m_mouseDragPoint = e.getPoint();
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// not used
	}

	///////////////////////////////////////////////////
	// Mouse Listener Methods 
	//////////////////////////////////////////////////

	@Override
	public void mouseClicked(MouseEvent e) {
		// not used
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// not used
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// not used
	}

	@Override
	public void mousePressed(MouseEvent e) {
		Component pressedComponent = e.getComponent();
		m_mouseDownPoint = e.getPoint();
		
		boolean multiSelect = allowsMultipleSelection() && e.isControlDown();

		if (!(pressedComponent instanceof Shape) ) {
			// non-shape selected, clear selections
			outlineAllSelections(false);
			
			m_componentsSelected.clear();
			
		} else {
			Shape shape = (Shape)pressedComponent;
			if (shape.isSelectionShowing() && e.isControlDown()) {
				// if CTL-click on selection, clear that selection
				outlineShape(shape, false);
				m_componentsSelected.remove(shape);
				
			} else if (shape.isSelectionShowing()) {
				// already selected - ignore
					
			} else {
				// Shape pressed - make note of where and what was pressed
				if (!multiSelect) {
					// clear previous selection(s)
					outlineAllSelections(false);
					m_componentsSelected.clear();
				}
				
				outlineShape(shape, true);
				m_componentsSelected.add(shape);
				
				if (e.isPopupTrigger()) {
					showPopup(e);
				}
			}
				
		} 

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
		if (m_componentsSelected.size() > 0) {
			for (Shape shape : m_componentsSelected) {
				// make sure we can see the component
				Rectangle visible = getVisibleRect();
				Rectangle bounds = shape.getBounds();
				if (!visible.contains(bounds)) {
					scrollRectToVisible(bounds);
				}
			}
			
		} else if (m_mouseDownPoint != null && m_mouseDragPoint != null) {
			Rectangle bounds = getRectangle(m_mouseDownPoint, m_mouseDragPoint);
			for (Component child : getComponents()) {
				if (child instanceof Shape) {
					Shape shape = (Shape)child;
					Rectangle shapeBounds = shape.getBounds();
					if (bounds.contains(shapeBounds)) {
						outlineShape(shape, true);
						m_componentsSelected.add(shape);
					}
				}
			}
			
		}

		//clear information
		m_mouseDownPoint = null;
		m_mouseDragPoint = null;
		repaint();

		if (e.isPopupTrigger()) {
			showPopup(e);
		}

	}

	private class BringToFrontAction extends AbstractAction {
		private Component m_component;

		public BringToFrontAction(Component component) {
			super("Bring to Front");
			m_component = component;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			remove(m_component);
			// add to start
			add(m_component, 0);
			m_component.repaint();
		}
	}

	private class SendToBackAction extends AbstractAction {
		private Component m_component;

		public SendToBackAction(Component component) {
			super("Send to Back");
			m_component = component;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			remove(m_component);
			// add to end
			add(m_component);
			m_component.repaint();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// create a relationship view
		RelationshipView view = new RelationshipView();

		// add a few shapes
		Shape circle = new Shapes.Circle(new Color(0xaaffaa), 60);
		circle.addTextLine("Really Wide Circle");
		circle.setLocation(100, 250);
		Shape rect1 = new Shapes.RectangleShape(new Color(0xaaaaff), 75, 100);
		rect1.addTextLine("Rect 1");
		rect1.setLocation(450, 250);
		Shape rect2 = new Shapes.RectangleShape(new Color(0xffaaff), 85, 60);
		rect2.addTextLine("Rect 2", StyleConstants.ALIGN_CENTER, view.getFont().deriveFont(Font.BOLD));
		rect2.addTextLine("field 1", StyleConstants.ALIGN_RIGHT);
		rect2.addTextLine("field 2", StyleConstants.ALIGN_RIGHT);
		rect2.addTextLine("really, long field", StyleConstants.ALIGN_RIGHT);
		rect2.setLocation(300, 70);
		Shape diamond = new Shapes.Diamond(Color.pink, 60, 90);
		diamond.setLocation(40, 50);

		view.add(circle);
		view.add(rect1);
		view.add(rect2);
		view.add(diamond);
		
		// link some
		view.addLink(new Connection(circle), 
				new Connection(rect1, ConnectionPoint.SouthWest, ArrowShape.Diamond));
		view.addLink(new Connection(circle, ConnectionPoint.Center, ArrowShape.Diamond),
				new Connection(rect2, ArrowShape.Arrow));
		view.addLink(new Connection(rect2, ConnectionPoint.Center, ArrowShape.Circle),
				new Connection(rect1, ConnectionPoint.West, ArrowShape.Triangle));
		view.addLink(new Connection(diamond, ConnectionPoint.SouthEast, ArrowShape.Arrow),
				new Connection(circle, ConnectionPoint.Center));

		// add to frame
		frame.getContentPane().add(new JScrollPane(view)); 


		frame.pack();
		frame.setVisible(true);

	}

}
