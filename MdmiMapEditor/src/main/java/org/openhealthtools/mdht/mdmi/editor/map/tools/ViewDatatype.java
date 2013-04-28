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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.text.StyleConstants;

import org.openhealthtools.mdht.mdmi.editor.common.layout.RelationshipView;
import org.openhealthtools.mdht.mdmi.editor.common.layout.Shape;
import org.openhealthtools.mdht.mdmi.editor.common.layout.Link.ArrowShape;
import org.openhealthtools.mdht.mdmi.editor.common.layout.Link.Connection;
import org.openhealthtools.mdht.mdmi.editor.common.layout.Link.ConnectionPoint;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.DTSDerived;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;

/** Show a diagram MdmiDatatype, and all sub-fields */
public class ViewDatatype extends ViewDataObject {
	
	private static final Color s_fieldTypesColor  = new Color(0xaaffaa);	// pale green
	private static final Color s_additionalTypesColor  = new Color(0xaaaaff);		// pale blue
	
	private static final Dimension s_datatypeSize = new Dimension(51, 51);
	
	private static final int		 s_horizontalGap = 25;
	private static final int		 s_verticalGap = 25;

	private Point m_dataPoint = new Point();
	private int   m_Xmax = 0;
	
	public ViewDatatype(MdmiDatatype datatype) {
		super(datatype);
	}
	
	public ViewDatatype(Collection<MdmiDatatype> datatypes) {
		super(datatypes);
		setTitle(s_res.getString("ViewDatatypeTree.multiTitle"));
	}

	/** Get the name of this data object */
	@Override
	public String getName(Object object) {
		if (object instanceof MdmiDatatype) {
			return ((MdmiDatatype)object).getTypeName();
		}
		return null;
	}
	
	/** Layout the display */
	@Override
	protected void layoutDataObject(Object dataObject, RelationshipView view) {
		MdmiDatatype datatype = (MdmiDatatype)dataObject;
		
		m_Xmax = 0;
		int xPos = s_horizontalGap;
		int yPos = s_verticalGap;
		
		// keep track of which datatypes we've shown
		HashMap<MdmiDatatype, Shape> shapeMap = new HashMap<MdmiDatatype, Shape>();
		
		// start with primary datatype on top left
		Shape datatypeShape = createDatatypeShape(datatype);		
		datatypeShape.setLocation(xPos, yPos);
		view.add(datatypeShape);	// add to view
		shapeMap.put(datatype, datatypeShape);	// add to map so we can track it

		// show secondary datatypes (datatypes of fields)
		if (datatype instanceof DTComplex) {
			m_dataPoint = new Point(xPos + datatypeShape.getWidth() + s_verticalGap, yPos);
			showAllDatatypes((DTComplex)datatype, s_fieldTypesColor, view, shapeMap, true);
		}
		
	}
	
	/** Show all datatypes that are used by the field(s) of this type */
	private void showAllDatatypes(DTComplex complexType, Color color, RelationshipView view,
			HashMap<MdmiDatatype, Shape> shapeMap, boolean newColumn) {

		Shape datatypeShape = shapeMap.get(complexType);
		List<MdmiDatatype> newTypes = new ArrayList<MdmiDatatype>();

		for (Field field : complexType.getFields()) {
			MdmiDatatype fieldType = field.getDatatype();
			if (fieldType == null) {
				continue;
			}
			Shape shape = shapeMap.get(fieldType);
			if (shape == null) {
				// new datatype
				newTypes.add(fieldType);
				
				shape = createDatatypeShape(fieldType);
				shapeMap.put(fieldType, shape);

				shape.setColor(color);
				shape.setLocation(m_dataPoint.x, m_dataPoint.y);
				view.add(shape);	// add to view
				
				// keep track of maximum x-location
				m_Xmax = Math.max(m_Xmax, shape.getX() + shape.getWidth());

				// adjust y-location of next datatypes
				m_dataPoint.y += shape.getHeight() + s_horizontalGap;

				// if y gets too big, move to next column
				if (m_dataPoint.y > datatypeShape.getY() + 700) {
					m_dataPoint.y = s_verticalGap;	//datatypeShape.getY();
					m_dataPoint.x = m_Xmax + s_horizontalGap;
				}
			}
			// link datatype to field's type
			view.addLink(new Connection(datatypeShape, ConnectionPoint.Center), 
					new Connection(shape, ArrowShape.Arrow));
		}

		// tertiary types
		if (newColumn) {
			m_dataPoint.y = datatypeShape.getY();	//s_verticalGap;
			m_dataPoint.x = m_Xmax + s_horizontalGap;
		}
		for (MdmiDatatype newType : newTypes) {
			if (newType instanceof DTComplex) {
				showAllDatatypes((DTComplex)newType, s_additionalTypesColor, view,
						shapeMap, false);
			}
		}
		
	}

	/** Create a shape showing a datatype and all its fields (if complex) */
	public static Shape createDatatypeShape(MdmiDatatype datatype) {
		Shape datatypeShape = new DataTypeShape(datatype, s_datatypeSize.width, s_datatypeSize.height);

		final String pad = "  ";
		
		String datatypeLabel = datatype.getTypeName();
		if (datatype instanceof DTSDerived && ((DTSDerived)datatype).getBaseType() != null) {
			DTSDerived derivedType = (DTSDerived)datatype;
			// show derivation
			datatypeLabel = MessageFormat.format(s_res.getString("ViewDatatype.derivedLabel"),
					datatypeLabel, derivedType.getBaseType().getTypeName());
		}
		datatypeShape.addTextLine(pad + datatypeLabel + pad,
				StyleConstants.ALIGN_CENTER, 
				datatypeShape.getFont().deriveFont(Font.BOLD));
		datatypeShape.addSeparator();

		// show fields
		if (datatype instanceof DTComplex) {
			DTComplex complexType = (DTComplex)datatype;
			for (Field field : complexType.getFields()) {
				// add field names:  "+ fieldName : dataType"
				String fieldLabel = field.getDatatype() != null ?
						MessageFormat.format(s_res.getString("ViewDatatype.fieldLabel"),
						 field.getName(), field.getDatatype().getTypeName()) :
							 field.getName();
				datatypeShape.addTextLine(pad + fieldLabel + pad, 
						StyleConstants.ALIGN_LEFT);
			}
			
		}
		
		return datatypeShape;
	}
}
