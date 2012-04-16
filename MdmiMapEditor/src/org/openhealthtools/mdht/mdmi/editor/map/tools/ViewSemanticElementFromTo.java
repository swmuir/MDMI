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
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.ToBusinessElement;
import org.openhealthtools.mdht.mdmi.model.ToMessageElement;

/** Show a Semantic Element, and corresponding from and to elements
 * 
 *     element <-- SE --> FROM --> BER --> element
 *                     --> TO --> BER --> element
 **/
public class ViewSemanticElementFromTo extends ViewSemanticElement {
	private static final Color s_fromElementColor = new Color(0x96ff96);	   // green gray
	private static final Color s_fromBizElementColor  = new Color(0x80ff80);		// darker green gray
	
	private static final Color s_toElementColor  = new Color(0xffafaa);		// pale pink
	private static final Color s_toBizElementColor  = new Color(0xee9999);		// salmon
	
	public ViewSemanticElementFromTo(SemanticElement element) {
		super(element);
		setTitle(MessageFormat.format(s_res.getString("ViewSemanticElement.title"),
				ClassUtil.beautifyName(element.getClass()), element.getName()));
	}

	public ViewSemanticElementFromTo(Collection<SemanticElement> elements) {
		super(elements);
		setTitle(s_res.getString("ViewSemanticElement.multiTitle"));
	}



	/** Add From and To elements
	 * @param semanticElement
	 */
	@Override
	protected void addOtherElements(SemanticElement element, RelationshipView view) {
		// From elements (to the left of the Semantic Element)
		addFromMdmiElements(element, view);
		
		// To elements (to the right of the Semantic Element)
		addToMdmiElements(element, view);
	}

	
	/** Add shapes to show "to-Mdmi" (toMessageElement), it's business element reference, and
	 * the corresponding data type (right to left)
	 * @param toElement
	 * @param point	top-left corner of MessageElement
	 * @param shapes
	 */
	private void addToMdmiElements(SemanticElement element, RelationshipView view) {

		Collection<ToMessageElement> toMdmis = getToMdmi(element);
		if (toMdmis == null|| toMdmis.size() == 0) {
			return;
		}
		
		// top-left corner of first "to element"
		Point topLeft = new Point(m_seShape.getXmax() + s_horizontalGap,
				m_seShape.getY() + (m_seShape.getHeight()-s_elementSize.height)/2);
		int xLocation;
		int maxX = m_seShape.getXmax();
		
		for (ToMessageElement toElement : toMdmis) {

			// Message Element
			Shape toShape = new Shapes.RectangleShape(s_toElementColor, 
					s_elementSize.width, s_elementSize.height);
			toShape.setUserObject(toElement);
			labelShape(toShape, s_res.getString("ViewSemanticElement.toMdmi"),
					toElement.getName(), Font.ITALIC);
			toShape.setLocation(topLeft.x, topLeft.y);
			view.add(toShape);
			// link from SE to element
			view.addLink(new Connection(m_seShape, ConnectionPoint.Center),
					new Connection(toShape, ConnectionPoint.West, ArrowShape.Arrow));
			maxX = Math.max(maxX, toShape.getXmax());

			// Business Ref
			MdmiBusinessElementReference bizRef = toElement.getBusinessElement();
			if (bizRef != null) {
				xLocation = topLeft.x + toShape.getWidth() + s_horizontalGap;
				Shape bizRefShape = new Shapes.RectangleShape(s_toBizElementColor,
						s_elementSize.width);
				bizRefShape.setUserObject(bizRef);
				labelShape(bizRefShape, bizRef.getClass(), 
						bizRef.getName(), Font.ITALIC);
				bizRefShape.setLocation(xLocation, topLeft.y);
				view.add(bizRefShape);
				// link from element to business ref
				view.addLink(new Connection(toShape, ConnectionPoint.East),
						new Connection(bizRefShape, ConnectionPoint.West, ArrowShape.Arrow));
				maxX = Math.max(maxX, bizRefShape.getXmax());
				topLeft.y += bizRefShape.getHeight() + s_verticalGap;

				// Business Ref's datatypes
				MdmiDatatype refType = bizRef.getReferenceDatatype();
				if (refType != null) {
					Shape typeShape = createDatatypeShape(refType);
					// center on business ref 
					typeShape.centerHorizontally(bizRefShape, topLeft.y);
					view.add(typeShape);
					// link from bizRef to type
					view.addLink(new Connection(bizRefShape, ConnectionPoint.Center),
							new Connection(typeShape, ConnectionPoint.Center, ArrowShape.Arrow));
					maxX = Math.max(maxX, typeShape.getXmax());

					topLeft.y += typeShape.getHeight() + s_verticalGap;
				}
			}
		}

		// Label 
		Shape toLabel = new Label(s_res.getString("ViewSemanticElement.toTitle"));
		toLabel.centerHorizontally(m_seShape.getXmax(), maxX,
				m_seShape.getY());
		view.add(toLabel);
	}
	
	/** Add shapes to show "from-Mdmi" (toBusinessElement), it's business element reference, and
	 * the corresponding data type (right-to-left)
	 * @param semanticElement
	 * @param shapes
	 */
	private void addFromMdmiElements(SemanticElement element, RelationshipView view) {

		List<ToBusinessElement> fromMdmis = getFromMdmi(element);
		if (fromMdmis == null || fromMdmis.size() == 0) {
			return;
		}

		// top-right corner of first "from element"
		Point topRight = new Point(m_seShape.getX() - s_horizontalGap,
				m_seShape.getY() + (m_seShape.getHeight()-s_elementSize.height)/2);
		int xLocation;
		int minX = m_seShape.getX();
		
		for (ToBusinessElement fromElement : fromMdmis) {

			// From Element
			Shape fromShape = new Shapes.RectangleShape(s_fromElementColor, 
					s_elementSize.width, s_elementSize.height);
			fromShape.setUserObject(fromElement);
			labelShape(fromShape, s_res.getString("ViewSemanticElement.fromMdmi"), 
					fromElement.getName(), Font.ITALIC);
			xLocation = topRight.x - fromShape.getWidth();
			fromShape.setLocation(xLocation, topRight.y);
			view.add(fromShape);
			// link from SE to element
			view.addLink(new Connection(m_seShape, ConnectionPoint.Center),
					new Connection(fromShape, ConnectionPoint.East, ArrowShape.Arrow));
			minX = Math.min(minX, fromShape.getX());

			// Business Ref
			MdmiBusinessElementReference bizRef = fromElement.getBusinessElement();
			if (bizRef != null) {
				Shape bizRefShape = new Shapes.RectangleShape(s_fromBizElementColor,
						s_elementSize.width);
				bizRefShape.setUserObject(bizRef);
				labelShape(bizRefShape, bizRef.getClass(), 
						bizRef.getName(), Font.ITALIC);
				xLocation = xLocation - bizRefShape.getWidth() - s_horizontalGap;
				bizRefShape.setLocation(xLocation, topRight.y);
				view.add(bizRefShape);
				// link from element to business ref
				view.addLink(new Connection(fromShape, ConnectionPoint.West),
						new Connection(bizRefShape, ConnectionPoint.East, ArrowShape.Arrow));
				minX = Math.min(minX, bizRefShape.getX());
				topRight.y += bizRefShape.getHeight() + s_verticalGap;

				// Business Ref's datatypes
				MdmiDatatype refType = bizRef.getReferenceDatatype();
				if (refType != null) {
					Shape typeShape = createDatatypeShape(refType);
					typeShape.centerHorizontally(bizRefShape, topRight.y);
					view.add(typeShape);
					// link from bizRef to type
					view.addLink(new Connection(bizRefShape, ConnectionPoint.Center),
							new Connection(typeShape, ConnectionPoint.Center, ArrowShape.Arrow));
					minX = Math.min(minX, typeShape.getX());
					topRight.y += typeShape.getHeight() + s_verticalGap;
				}
			}
		}

		// Label
		Shape fromLabel = new Label(s_res.getString("ViewSemanticElement.fromTitle"));
		fromLabel.centerHorizontally(minX, m_seShape.getX(), m_seShape.getY());
		view.add(fromLabel);

	}



	/** get the toMdmi elements, sorted by name */
	private Collection<ToMessageElement> getToMdmi(SemanticElement semanticElement) {
		List<ToMessageElement> rules = new ArrayList<ToMessageElement>();
		rules.addAll(semanticElement.getToMdmi());
		Collections.sort(rules, new Comparators.ToMessageElementComparator());
		return rules;
	}


	/** get the fromMdmi elements, sorted by name */
	private List<ToBusinessElement> getFromMdmi(SemanticElement semanticElement) {
		List<ToBusinessElement> rules = new ArrayList<ToBusinessElement>();
		rules.addAll(semanticElement.getFromMdmi());
		Collections.sort(rules, new Comparators.ToBusinessElementComparator());
		return rules;
	}
}
