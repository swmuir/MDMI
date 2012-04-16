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
import org.openhealthtools.mdht.mdmi.model.SemanticElementRelationship;

/** Show a Semantic Element, and corresponding relationships
 **/
public class ViewSemanticElementRelationships extends ViewSemanticElement {
	
	private static final Color s_relationshipColor  = new Color(0xffc2aa);		// pale pink
	
	public ViewSemanticElementRelationships(SemanticElement element) {
		super(element);
		setTitle(MessageFormat.format(s_res.getString("ViewSemanticElementRelationships.title"),
				ClassUtil.beautifyName(element.getClass()), element.getName()));
	}

	public ViewSemanticElementRelationships(Collection<SemanticElement> elements) {
		super(elements);
		setTitle(s_res.getString("ViewSemanticElementRelationships.multiTitle"));
	}

	
	/** Add shapes to show relationships, their semantic element,
	 * the corresponding data type (right to left)
	 * @param toElement
	 * @param point	top-left corner of MessageElement
	 * @param shapes
	 */
	@Override
	protected void addOtherElements(SemanticElement element, RelationshipView view) {

		List<SemanticElementRelationship> relationships = getRelationships(element);
		if (relationships == null || relationships.size() == 0) {
			return;
		}
		
		// top-left corner of first "relationship"
		Point topLeft = new Point(m_seShape.getXmax() + s_horizontalGap,
				m_seShape.getY() + (m_seShape.getHeight()-s_elementSize.height)/2);
		int xLocation;
		int maxX = topLeft.x; 
		
		for (SemanticElementRelationship relationship : relationships) {

			// Relationship
			Shape relationshipShape = new Shapes.RectangleShape(s_relationshipColor, 
					s_elementSize.width, s_elementSize.height);
			relationshipShape.setUserObject(relationship);
			ViewSemanticElement.labelShape(relationshipShape, relationship.getClass(),
					relationship.getName(), Font.ITALIC);
			relationshipShape.setLocation(topLeft.x, topLeft.y);
			view.add(relationshipShape);
			// link from SE to relationship
			view.addLink(new Connection(m_seShape, ConnectionPoint.Center),
					new Connection(relationshipShape, ConnectionPoint.West, ArrowShape.Arrow));
			topLeft.y += relationshipShape.getHeight() + s_verticalGap;
			maxX = Math.max(maxX, relationshipShape.getXmax());

			// Related SE
			SemanticElement relatedSE = relationship.getRelatedSemanticElement();
			if (relatedSE != null) {
				xLocation = topLeft.x + relationshipShape.getWidth() + s_horizontalGap;
				Shape relatedSEShape = ViewSemanticElement.createSemanticElementShape(relatedSE, Font.PLAIN);
				relatedSEShape.setColor(Shape.lighten(s_semanticElementColor, 0x55));
				relatedSEShape.centerVertically(relationshipShape, xLocation);
				view.add(relatedSEShape);
				// link from element to business ref
				view.addLink(new Connection(relationshipShape, ConnectionPoint.Center),
						new Connection(relatedSEShape, ConnectionPoint.Center, ArrowShape.Arrow));
				topLeft.y = Math.max(topLeft.y, relatedSEShape.getYmax() + s_verticalGap);
				
				// Related SE's datatype
				MdmiDatatype datatype = relatedSE.getDatatype();
				if (datatype != null) {
					Shape datatypeShape = ViewSemanticElement.createDatatypeShape(datatype);
					// align with SE
					xLocation += relatedSEShape.getWidth() + s_horizontalGap;
					datatypeShape.centerVertically(relatedSEShape, xLocation);
					view.add(datatypeShape);
					
					// link from SE to datatype
					view.addLink(new Connection(relatedSEShape, ConnectionPoint.Center),
							new Connection(datatypeShape, ConnectionPoint.Center, ArrowShape.Arrow));
					topLeft.y = Math.max(topLeft.y, datatypeShape.getYmax()+s_verticalGap);
				}

			}

		}


		// label
		Shape toLabel = new Label(s_res.getString("ViewSemanticElementRelationships.relationships"));
		toLabel.centerHorizontally(m_seShape.getXmax(), maxX,
				m_seShape.getY());
		view.add(toLabel);
	}

	/** get the semantic element relationships, sorted by name */
	private List<SemanticElementRelationship> getRelationships( SemanticElement semanticElement) {
		List<SemanticElementRelationship> relationships = new ArrayList<SemanticElementRelationship>();
		relationships.addAll(semanticElement.getRelationships());
		Collections.sort(relationships, new Comparators.SemanticElementRelationshipComparator());
		return relationships;
	}

}
