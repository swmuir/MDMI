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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;

import org.openhealthtools.mdht.mdmi.editor.common.layout.Link.ArrowShape;
import org.openhealthtools.mdht.mdmi.editor.common.layout.Link.Connection;
import org.openhealthtools.mdht.mdmi.editor.common.layout.Link.ConnectionPoint;
import org.openhealthtools.mdht.mdmi.editor.common.layout.RelationshipView;
import org.openhealthtools.mdht.mdmi.editor.common.layout.Shape;
import org.openhealthtools.mdht.mdmi.editor.common.layout.Shapes.Label;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;

/** Show a Semantic Element, its datatype, and its parent(s)
 **/
public class ViewSemanticElement extends ViewDataObject {
	protected static final Color s_parentColor = new Color(0xa6a6b4);	// grey-blue
	
	protected static final Dimension s_elementSize = new Dimension(51, 51);
	
	protected static final int		 s_horizontalGap = 15;
	protected static final int		 s_verticalGap = 15;
	
	protected Shape m_seShape;
	protected Shape m_datatypeShape;
	
	public ViewSemanticElement(SemanticElement element) {
		super(element);
	}

	public ViewSemanticElement(Collection<SemanticElement> elements) {
		super(elements);
	}


	/** Get the name of this data object */
	@Override
	public String getName(Object object) {
		if (object instanceof SemanticElement) {
			return ((SemanticElement)object).getName();
		}
		return null;
	}
	
	/** Layout the display */
	@Override
	protected void layoutDataObject(Object dataObject, RelationshipView view) {
		layoutElementView((SemanticElement)dataObject, view);
	}

	
	/** Layout the display */
	private void layoutElementView(SemanticElement semanticElement, RelationshipView view) {
		//    parent          dataType
		//            \           ^
		//            parent      |
		//                   \   SE 
		
		// start with SE at 0,0 - we'll move everything later
		Point point = new Point(0,0);
		
		m_seShape = createSemanticElementShape(semanticElement, Font.BOLD);
		m_seShape.setLocation(point.x, point.y);
		view.add(m_seShape);

		
		// SE's datatype - show above SE
		MdmiDatatype datatype = semanticElement.getDatatype();
		if (datatype != null) {
			m_datatypeShape = createDatatypeShape(datatype);
			point.y = m_seShape.getY() - (m_datatypeShape.getHeight() + s_horizontalGap);

			m_datatypeShape.centerHorizontally(m_seShape, point.y);
			view.add(m_datatypeShape);
			
			// link from SE to datatype
			view.addLink(new Connection(m_seShape, ConnectionPoint.Center),
					new Connection(m_datatypeShape, ConnectionPoint.South, ArrowShape.Arrow));
		}
		
		// any other elements
		addOtherElements(semanticElement, view);
		
		// show SE's parents - above, and to the left of SE
		showSEParents(semanticElement.getParent(), view);
		
		// adjust all shape positions
		Point min = new Point(0,0);
		Point max = new Point(0,0);
		for (Shape shape : view.getShapes()) {
			// compute min and max
			min.x = Math.min(shape.getX(), min.x);
			min.y = Math.min(shape.getY(), min.y);
			max.x = Math.max(shape.getXmax(), max.x);
			max.y = Math.max(shape.getYmax(), max.y);
		}
		// adjust all x and y values so that min.x, min.y is horizontal/vertical gap
		int deltaX = s_horizontalGap - min.x;
		int deltaY = s_verticalGap - min.y;
		for (Shape shape : view.getShapes()) {
			shape.setLocation(shape.getX()+deltaX, shape.getY()+deltaY);
		}
		min.translate(deltaX, deltaY);
		max.translate(deltaX, deltaY);		
	}
	


	/** Show the Semantic Element parent(s) (above and to the left of the SE)
	 * @param semanticElement
	 * @param point
	 * @param shapes
	 */
	protected void showSEParents(SemanticElement parent, RelationshipView view) {
		if (parent == null) {
			return;
		}
		
		// keep track to avoid recursion
		HashMap<SemanticElement, Shape> parentShapes = new HashMap<SemanticElement, Shape>();

		// adjust position for first parent
		Point point = new Point(m_seShape.getX(), m_seShape.getY());
		if (m_datatypeShape != null && m_datatypeShape.getX() < m_seShape.getX()) {
			point.x = m_datatypeShape.getX();
		}
		point.x -= 2*s_horizontalGap;
		point.y -= 3*s_verticalGap;
		
		Shape firstParentShape = null;
		Shape childShape = m_seShape;
		while (parent != null) {
			// check first
			Shape parentShape = parentShapes.get(parent);
			if (parentShape == null) {
				parentShape = createSemanticElementShape(parent, Font.PLAIN);
				parentShape.setColor(s_parentColor);
				point.x = point.x - parentShape.getWidth();
				point.y = point.y - parentShape.getHeight()/2;
				parentShape.setLocation(point.x, point.y);
				view.add(parentShape);

				parentShapes.put(parent, parentShape);

				// link from parent to child
				view.addLink(new Connection(parentShape, ConnectionPoint.Center),
						new Connection(childShape, ConnectionPoint.Center, ArrowShape.Arrow));
				if (firstParentShape == null) {
					firstParentShape = parentShape;
				}
				childShape = parentShape;
				parent = parent.getParent();
			} else {
				// we've gone back to the start - show it and quit
				view.addLink(new Connection(parentShape, ConnectionPoint.Center),
						new Connection(childShape, ConnectionPoint.Center, ArrowShape.Arrow));
				break;
			}
		}
		// label
		// Semantic Element Parent(s)
		Shape label = new Label(MessageFormat.format(s_res.getString("ViewSemanticElement.parentsTitle"),
				parentShapes.size()));
		label.setTextColor(Color.GRAY);
		label.centerHorizontally(point.x, firstParentShape.getXmax(),
				point.y - label.getHeight());
		view.add(label);
	}
	


	/** Add any other shapes related to this SE
	 * @param semanticElement
	 * @param shapes
	 */
	protected void addOtherElements(SemanticElement element, RelationshipView view) {
		// does nothing
	}
}
