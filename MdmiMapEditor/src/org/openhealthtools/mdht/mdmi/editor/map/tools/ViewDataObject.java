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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.text.StyleConstants;

import org.openhealthtools.mdht.mdmi.editor.common.layout.RelationshipView;
import org.openhealthtools.mdht.mdmi.editor.common.layout.Shape;
import org.openhealthtools.mdht.mdmi.editor.common.layout.Shapes;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;

/** Show some data object in a graphical view */
public abstract class ViewDataObject extends PrintableView {

	protected static final Color s_semanticElementColor = new Color(0x4d4dff);	// pale blue
	protected static final Dimension s_semanticElementSize = new Dimension(101, 101);
	
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");

	/** Minimum/maximum dimensions */
	protected Dimension m_min = new Dimension( 400, 300);
	protected Dimension m_max = new Dimension(1000, 700);
	
	private RelationshipView m_dataView;
	
	@SuppressWarnings("unchecked")
	public ViewDataObject(Object dataObject) {
		if (dataObject instanceof Collection) {
			setTitle(s_res.getString("ViewDataObject.multiTitle"));
			m_dataView = createMultiObjectView((Collection<Object>)dataObject);
			
		} else {
			// View of Data Type 'foo'
			setTitle( MessageFormat.format(s_res.getString("ViewDataObject.title"), 
					ClassUtil.beautifyName(dataObject.getClass()), getName(dataObject)) );
			m_dataView = createSingleObjectView(dataObject);
		}
		
		// wrap in scroll pane
		setCenterComponent( new JScrollPane(m_dataView) );
		
		pack(m_min, m_max);
		
		setLocation(100, 100);
	}
	
	private RelationshipView createSingleObjectView(Object dataObject) {

		RelationshipView view = new EnhancedRelationshipView();
		
		// fill
		layoutDataObject(dataObject, view);
		return view;
	}
	
	private RelationshipView createMultiObjectView(Collection<Object> dataObjects) {
		
		// create a view containing all objects
		RelationshipView multiView = new EnhancedRelationshipView();
		
		int xOffset = 0;
		int yOffset = 0;
		for (Object dataObject : dataObjects) {
			RelationshipView view = new EnhancedRelationshipView();

			// fill
			layoutDataObject(dataObject, view);
			
			// transfer to multi-view
			multiView.transferContents(view, xOffset, yOffset);
			
			// adjust yOffset
			Rectangle bounds = multiView.getMinimumBounds();
			yOffset = bounds.y + bounds.height;
		}
		return multiView;
	}
	
	@Override
	protected Component getPrintComponent() {
		return m_dataView;
	}
//
//	/** Keep track of all shapes added to the view */
//	protected void addShape(ObjectView objectView, Shape shape) {
//		objectView.shapes.add(shape);
//		objectView.view.add(shape);
//	}
//	
//	/** get all the shapes */
//	protected List<Shape> getShapes(ObjectView objectView) {
//		return objectView.shapes;
//	}
	
	/** Get the name of this data object */
	public abstract String getName(Object object);
	
	/** Layout the display */
	protected abstract void layoutDataObject(Object dataObject, RelationshipView view);
	

	/** Create a shape for a Semantic Element. The shape will be lableled with 
	 * the SE's name
	 */
	public static Shape createSemanticElementShape(SemanticElement semanticElement, int fontStyle) {
		Shape semanticElementShape = new Shapes.Circle(s_semanticElementColor, s_semanticElementSize.width);
		semanticElementShape.setUserObject(semanticElement);
		labelShape(semanticElementShape, semanticElement.getClass(),
				// pad name since oval-shape cuts into text
				" " + semanticElement.getName() + " ", fontStyle);
		return semanticElementShape;
	}
	
	/** Create a shape for an MdmiDatatype. The shape will be labeled with the
	 * datatype class, name, and fields */
	public static Shape createDatatypeShape(MdmiDatatype datatype) {
		Shape typeShape = ViewDatatype.createDatatypeShape(datatype);
		// show type
		typeShape.insertTextLine(0, ClassUtil.beautifyName(datatype.getClass()),
				StyleConstants.ALIGN_CENTER, null);
		return typeShape;
	}
	
	/** Add text to a shape showing the class type, a separator line, and the name (using the font style) */
	public static void labelShape(Shape shape, Class<?> clazz, String name, int fontStyle) {
		labelShape(shape, ClassUtil.beautifyName(clazz), name, fontStyle);
	}
	
	/** Add text to a shape showing the type, a separator line, then the name (using the font style) */
	public static void labelShape(Shape shape, String type, String name, int fontStyle) {
		shape.addTextLine(type);
		shape.addSeparator();
		shape.addTextLine(name, shape.getFont().deriveFont(fontStyle));
	}

	////////////////////////////////////////
	// Our Own RelationshipView
	///////////////////////////////////////
	private class EnhancedRelationshipView extends RelationshipView {
		/** Create a popup menu based on the selected component - override
		 * to edit item */
		@Override
		protected JPopupMenu createPopupMenu(Component selected) {
			JPopupMenu popupMenu = super.createPopupMenu(selected);
			if (selected instanceof Shape && ((Shape)selected).getUserObject() != null) {
				popupMenu.insert(new OpenShapeAction((Shape)selected), 0);
				popupMenu.insert( new JPopupMenu.Separator(), 1);
			}
			return popupMenu;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				Component pressedComponent = e.getComponent();
				if (pressedComponent instanceof Shape) {
					openSelection(((Shape)pressedComponent).getUserObject());
				}
			}
		}

	}

	private class OpenShapeAction extends AbstractAction {
		private Shape m_shape;
		public OpenShapeAction(Shape shape) {
			super(MessageFormat.format(s_res.getString("ViewDataObject.openFormat"), 
					ClassUtil.beautifyName(shape.getUserObject().getClass())));
			m_shape = shape;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			openSelection(m_shape.getUserObject());
		}
		
	}
	
}
