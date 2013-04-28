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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.swing.text.StyleConstants;
import javax.swing.tree.DefaultMutableTreeNode;

import org.openhealthtools.mdht.mdmi.editor.common.layout.RelationshipView;
import org.openhealthtools.mdht.mdmi.editor.common.layout.Shape;
import org.openhealthtools.mdht.mdmi.editor.common.layout.Shapes;
import org.openhealthtools.mdht.mdmi.editor.common.layout.Link.ArrowShape;
import org.openhealthtools.mdht.mdmi.editor.common.layout.Link.Connection;
import org.openhealthtools.mdht.mdmi.editor.common.layout.Link.ConnectionPoint;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SyntaxNodeNode;
import org.openhealthtools.mdht.mdmi.model.Choice;
import org.openhealthtools.mdht.mdmi.model.LeafSyntaxTranslator;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.Node;

/** Show a SyntaxNode, it's parentage, and associated data types */
public class ViewSyntaxNode extends ViewDataObject {
	
	private static final Color s_nodeColor  = new Color(0xaaaaff);		// pale blue
	
	private static final Dimension m_nodeSize = new Dimension(51, 51);
	
	private static final int		 s_horizontalGap = 25;
	private static final int		 s_verticalGap = 25;
	
	public ViewSyntaxNode(Node node) {
		super(node);
	}

	/** Get the name of this data object */
	@Override
	public String getName(Object object) {
		if (object instanceof Node) {
			return ((Node)object).getName();
		}
		return null;
	}
	
	/** Layout the display */
	@Override
	protected void layoutDataObject(Object dataObject, RelationshipView view) {
		// Each syntax node has either a Semantic Element, or a Field Name.
		// If there is a field name, it refers to the field in the parent node's
		// datatype                                            ____________
		//    ___________________      _________________      |Datatype DT1|
		//   |  Bag bag1         |    |Semantic Element |     |------------|
		//   |-------------------|--->|      SE1        |---->|a1 : String |
		//   |semanticElement SE1|     -----------------      |a2 : DT2    |
		//    -------------------                             |a3 : Binary |---a2  
		//       |                                             ------------    |
		//       |     _____________                                   ________v____ 
		//       |----| Choice cho1 |                                 |Datatype DT2 |
		//            |-------------|-------------------------------->|-------------|
		//            |fieldName a2 |                                 |b1 : Date    |
		//             -------------                                  |b2 : String  |
		//                |                                           |b3 : DT3     |--b3   
		//                |                                            -------------   |      
		//                |     _____________                                   _______v____
		//                |----| Bag bag2    |                                 |Datatype DT3|
		//                     |-------------|-------------------------------->|------------|
		//                     |fieldName b3 |                                 |c1 : String |
		//                      -------------                                  |c2 : Date   |
		//                         |                                            ------------
		//                         |     _____________
		//                         |----| Leaf lf1    |
		//                              |-------------| [Date]
		//                              |fieldName c2 |
		//                               -------------
		//     
		
		// find top parent (has semantic element instead of field)
		Node node = (Node)dataObject;
		
		Stack<Node> nodes = new Stack<Node>();
		nodes.push(node);
		while (node != null) { //node.getSemanticElement() == null) {
			// get parent
			node = node.getParentNode();
			if (node == null) {
				break;
			}
			nodes.push(node);
		} 

		// start with first node on top left
		int xPos = s_horizontalGap;
		int yPos = s_verticalGap;
		NodeDisplay parentDisplay = null;
		while (!nodes.isEmpty()) {
			node = nodes.pop();
			NodeDisplay nodeDisplay = displayNodeAndType(view, node, xPos, yPos);
			

			Shape nodeShape = nodeDisplay.nodeShape;
			Shape datatypeShape = nodeDisplay.datatypeShape;
			// check postion - if datatype shape touches, or overlaps the previous one,
			//  we need to move it
			if (parentDisplay != null && parentDisplay.datatypeShape != null ) {
				int yMin = nodeShape.getYmin();
				if (datatypeShape != null) {
					yMin = Math.min(yMin, datatypeShape.getYmin());	
				}
				if (yMin < parentDisplay.datatypeShape.getYmax()) {
					int deltaY = parentDisplay.datatypeShape.getYmax() - yMin + 5;
					nodeShape.setLocation(nodeShape.getX(), nodeShape.getY() + deltaY);
					datatypeShape.setLocation(datatypeShape.getX(), datatypeShape.getY() + deltaY);
				}
			}

			if (parentDisplay != null) {
				// draw link from parent node to child
				Shape parentShape = parentDisplay.nodeShape;
				//      [parent]
				//          |
				//          o--->[node]
				//
				Shape intermediate = new Shapes.LinkPoint();
				intermediate.centerHorizontally(parentShape, yPos);
				intermediate.centerVertically(nodeShape, intermediate.getX());
				view.add(intermediate);
				// link from parent to intermediate, and from intermediate to child
				view.addLink(new Connection(parentShape, ConnectionPoint.Center),
						new Connection(intermediate, ConnectionPoint.Center));
				view.addLink(new Connection(intermediate, ConnectionPoint.Center),
						new Connection(nodeShape, ConnectionPoint.Center, ArrowShape.Arrow));
				

				// draw link from parent data type to child datatype
				if (parentDisplay.datatypeShape != null && datatypeShape != null) {
					//      [ parent ]             xI
					//      [datatype]------------ field name  y1
					//                                 |
					//               [ child  ]o<------o       y2
					//               [datatype]  
					int xI = Math.max(parentDisplay.datatypeShape.getXmax(), datatypeShape.getXmax()) + s_horizontalGap;
					int y1 = (int)parentDisplay.datatypeShape.getConnectionCoordinates(ConnectionPoint.SouthEast).getY();
					Shape intermediate1 = new Shapes.Label(node.getFieldName(), Font.ITALIC);
					intermediate1.centerVertically(xI, y1, y1);
					view.add(intermediate1);
					
					Shape intermediate2 = new Shapes.LinkPoint();
					int y2 = (int)datatypeShape.getConnectionCoordinates(ConnectionPoint.NorthEast).getY();
					intermediate2.centerHorizontally(intermediate1, y2);
					intermediate2.centerVertically(intermediate2.getX(), y2, y2);
					view.add(intermediate2);
					
					// Link 'em
					view.addLink(new Connection(parentDisplay.datatypeShape, ConnectionPoint.SouthEast),
							new Connection(intermediate1, ConnectionPoint.Center));
					view.addLink(new Connection(intermediate1, ConnectionPoint.Center),
							new Connection(intermediate2, ConnectionPoint.Center));
					view.addLink(new Connection(intermediate2, ConnectionPoint.Center),
							new Connection(datatypeShape, ConnectionPoint.NorthEast, ArrowShape.Arrow));
				}
			}

			// adjust x & y for next round
			for (Shape shape : nodeDisplay.allShapes()) {
				yPos = Math.max(shape.getYmax(), yPos);
			}
			// adjust x & y
			xPos = nodeShape.getXmax();
			yPos += s_verticalGap;
			
			parentDisplay = nodeDisplay;
			
		}
		
		
	}

	/** display the node (and datatype) at this x,y position */
	private NodeDisplay displayNodeAndType(RelationshipView view, Node node, int xPos, int yPos) {
		NodeDisplay nodeDisplay = new NodeDisplay();
		
		MdmiDatatype datatype = getDatatype(node);
		nodeDisplay.nodeShape = createNodeShape(node, datatype);
		nodeDisplay.nodeShape.setLocation(xPos, yPos);
		view.add(nodeDisplay.nodeShape);

		Shape prevShape = nodeDisplay.nodeShape;
		xPos = nodeDisplay.nodeShape.getXmax() + s_horizontalGap;
		
		// show semantic element
		if (node.getSemanticElement() != null) {
			nodeDisplay.seShape = createSemanticElementShape(node.getSemanticElement(), Font.BOLD);
			nodeDisplay.seShape.centerVertically(nodeDisplay.nodeShape, xPos);
			view.add(nodeDisplay.seShape);
			// link node to datatype
			view.addLink(new Connection(nodeDisplay.nodeShape, ConnectionPoint.Center), 
					new Connection(nodeDisplay.seShape, ConnectionPoint.Center, ArrowShape.Arrow));
			
			prevShape = nodeDisplay.seShape;
			xPos = nodeDisplay.seShape.getXmax() + s_horizontalGap;
		}
		
		// show datatype
		if (datatype != null) {
			nodeDisplay.datatypeShape = createDatatypeShape(datatype);
			nodeDisplay.datatypeShape.centerVertically(nodeDisplay.nodeShape, xPos);
			view.add(nodeDisplay.datatypeShape);
			// link node to datatype
			view.addLink(new Connection(prevShape, ConnectionPoint.Center), 
					new Connection(nodeDisplay.datatypeShape, ConnectionPoint.Center, ArrowShape.Arrow));
		}
		
		return nodeDisplay;
	}
	
	/** Get the datatype from the tree */
	public static MdmiDatatype getDatatype(Node node) {
		DefaultMutableTreeNode treeNode = SelectionManager.getInstance().getEntitySelector().findNode(node);
		if (treeNode instanceof SyntaxNodeNode) {
			SyntaxNodeNode syntaxNodeNode = (SyntaxNodeNode)treeNode;
			MdmiDatatype datatype = syntaxNodeNode.getDataType();
			return datatype;
		}
		return null;
	}
	
	/** Create a shape showing a node with the fieldname or semantic element */
	public static Shape createNodeShape(Node node, MdmiDatatype datatype) {
		// Bag is rectangle, Choice is diamond, Leaf is oval
		Shape shape;
		if (node instanceof LeafSyntaxTranslator) {
			shape = new Shapes.Circle(s_nodeColor, m_nodeSize.width);
		} else if (node instanceof Choice) {
			shape = new Shapes.Diamond(s_nodeColor, m_nodeSize.width, m_nodeSize.height*2);
		} else {
			shape = new Shapes.RectangleShape(s_nodeColor, m_nodeSize.width, m_nodeSize.height);
		}
		shape.setUserObject(node);
		
		// type
		String type = ClassUtil.beautifyName(node.getClass());
		shape.addTextLine(type);
		// name
		String name = node.getName();
		shape.addTextLine(name, StyleConstants.ALIGN_CENTER, shape.getFont().deriveFont(Font.BOLD));

		// if node has a semantic element - show semantic element and datatype, otherwise
		//   if it has a fieldName, show the fieldName and datatype
		String otherElementName = null;
		if (node.getSemanticElement() != null) {
			otherElementName = MessageFormat.format(s_res.getString("ViewSyntaxNode.semanticElementName"),
					node.getSemanticElement().getName());

		} else if (node.getFieldName() != null && node.getFieldName().length() > 0) {
			otherElementName = MessageFormat.format(s_res.getString("ViewSyntaxNode.fieldName"),
					node.getFieldName());
		}

		if (otherElementName != null) {
			shape.addSeparator();
			shape.addTextLine(otherElementName);
		}
		return shape;
	}
	
	/** Structure containing a Node shape, the SE shape (if applicable),
	 * and the data type shape (if applicable)
	 */
	private static class NodeDisplay {
		Shape nodeShape;
		Shape seShape;
		Shape datatypeShape;
		
		public List<Shape> allShapes() {
			List<Shape> all = new ArrayList<Shape>();
			if (nodeShape != null) all.add(nodeShape);
			if (seShape != null) all.add(seShape);
			if (datatypeShape != null) all.add(datatypeShape);
			
			return all;
		}
	}
	
	
}
