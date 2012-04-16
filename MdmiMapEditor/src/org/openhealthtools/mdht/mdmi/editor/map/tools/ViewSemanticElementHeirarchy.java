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

import java.awt.Font;
import java.awt.Point;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openhealthtools.mdht.mdmi.editor.common.layout.RelationshipView;
import org.openhealthtools.mdht.mdmi.editor.common.layout.Shape;
import org.openhealthtools.mdht.mdmi.editor.common.layout.Shapes;
import org.openhealthtools.mdht.mdmi.editor.common.layout.Link.ArrowShape;
import org.openhealthtools.mdht.mdmi.editor.common.layout.Link.Connection;
import org.openhealthtools.mdht.mdmi.editor.common.layout.Link.ConnectionPoint;
import org.openhealthtools.mdht.mdmi.editor.common.layout.Shapes.Label;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;

/** Show a Semantic Element, and all children
 **/
public class ViewSemanticElementHeirarchy extends ViewSemanticElement {
	
	public ViewSemanticElementHeirarchy(SemanticElement element) {
		super(element);
		setTitle(MessageFormat.format(s_res.getString("ViewSemanticElementHeirarchy.title"),
				ClassUtil.beautifyName(element.getClass()), element.getName()));
	}
	
	public ViewSemanticElementHeirarchy(Collection<SemanticElement> elements) {
		super(elements);
		setTitle(s_res.getString("ViewSemanticElementHeirarchy.multiTitle"));
	}


	
	/** Add shapes to show relationships, their semantic element,
	 * the corresponding data type (right to left)
	 * @param toElement
	 * @param point	top-left corner of MessageElement
	 * @param shapes
	 */
	@Override
	protected void addOtherElements(SemanticElement element, RelationshipView view) {

		Collection<SemanticElement> children = getChildren(element);
		if (children == null || children.size() == 0) {
			return;
		}
		
		// add all child semantic elements
		Point max = addChildren(view, element, m_seShape, null);

		// label
		if (element.getChildren() != null && element.getChildren().size() > 0) {
			Shape childrenLabel = new Label(s_res.getString("ViewSemanticElementHeirarchy.children"));
			childrenLabel.centerHorizontally(m_seShape.getXmax(), max.x,
					m_seShape.getY());
			childrenLabel.centerVertically(m_seShape, Math.max(childrenLabel.getX(), m_seShape.getXmax()));
			view.add(childrenLabel);
		}
	}
	
	private Point addChildren(RelationshipView view, SemanticElement parent, Shape parentShape, Shape parentDatatypeShape) {
		Point max = new Point(parentShape.getXmax(), 
				parentShape.getYmax());
		
		Collection<SemanticElement> children = getChildren(parent);
		if (children == null || children.size() == 0) {
			return max;
		}

		// start slightly to the right, and slightly below parent
		Point topLeft = new Point(parentShape.getXmax() - s_horizontalGap,
				parentShape.getYmax() - s_verticalGap);
		int xLocation = topLeft.x;
		
		Shape prevDatatypeShape = parentDatatypeShape;
		Shape prevIntermediate = parentShape;
		for (SemanticElement child : children) {
			// shouldn't happen - but it does
			if (child.getParent() != parent) {
				continue;
			}
			// Child SE
			Shape childShape = createSemanticElementShape(child, Font.PLAIN);
			childShape.setColor(Shape.lighten(s_semanticElementColor, 0x55));
			childShape.setLocation(topLeft.x, topLeft.y);
			view.add(childShape);

			// check postion - if this shape touches, or overlaps the previous one,
			//  we need to move it
			if (prevDatatypeShape != null && childShape.getYmin() < prevDatatypeShape.getYmax()) {
				int deltaY = prevDatatypeShape.getYmax() - childShape.getYmin() + 5;
				childShape.setLocation(childShape.getX(), childShape.getY() + deltaY);
			}
			
			// intermediate shape - align with center of parent and child
			Shape intermediate = new Shapes.LinkPoint();
			intermediate.centerHorizontally(parentShape, topLeft.y);
			intermediate.centerVertically(childShape, intermediate.getX());
			view.add(intermediate);
			
			// link from parent to intermediate, and from intermediate to child
			view.addLink(new Connection(prevIntermediate, ConnectionPoint.Center),
					new Connection(intermediate, ConnectionPoint.Center));
			view.addLink(new Connection(intermediate, ConnectionPoint.Center),
					new Connection(childShape, ConnectionPoint.Center, ArrowShape.Arrow));
			max.x = Math.max(max.x, childShape.getXmax());
			topLeft.y += childShape.getHeight() + s_verticalGap;

			// child SE's datatype
			MdmiDatatype datatype = child.getDatatype();
			if (datatype != null) {
				Shape datatypeShape = ViewSemanticElement.createDatatypeShape(datatype);
				// align with SE
				xLocation = childShape.getXmax() + s_horizontalGap;
				datatypeShape.centerVertically(childShape, xLocation);
				view.add(datatypeShape);
				
				// check postion - if this datatype's shape touches, or overlaps the previous one,
				//  we need to move it
				if (prevDatatypeShape != null && datatypeShape.getYmin() < prevDatatypeShape.getYmax()) {
					int deltaY = prevDatatypeShape.getYmax() - datatypeShape.getYmin() + 5;
					childShape.setLocation(childShape.getX(), childShape.getY() + deltaY);
					datatypeShape.setLocation(childShape.getX(), datatypeShape.getY() + deltaY);
				}

				// link from SE to datatype
				view.addLink(new Connection(childShape, ConnectionPoint.Center),
						new Connection(datatypeShape, ConnectionPoint.Center, ArrowShape.Arrow));
				max.x = Math.max(max.x, datatypeShape.getXmax());
				topLeft.y = Math.max(topLeft.y, datatypeShape.getYmax()+s_verticalGap);
				
				prevDatatypeShape = datatypeShape;
			}

			prevIntermediate = intermediate;
			
			// next-level of children
			Point childMax = addChildren(view, child, childShape, prevDatatypeShape);
			topLeft.y = Math.max(topLeft.y, childMax.y + s_verticalGap);
		}
		max.y = topLeft.y - s_verticalGap;
		return max;
	}


	/** get the child elements, sorted by name */
	private Collection<SemanticElement> getChildren(SemanticElement semanticElement) {
		List<SemanticElement> rules = new ArrayList<SemanticElement>();
		rules.addAll(semanticElement.getChildren());
		Collections.sort(rules, new Comparators.SemanticElementComparator());
		return rules;
	}

}
