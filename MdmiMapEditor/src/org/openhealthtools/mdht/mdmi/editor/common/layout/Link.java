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

/**
 * A link between two shapes. Each end of the link is a Connection, which defines
 * the shape, where the connection is made, and what arrow is used
 */
public class Link {
	/** Arrow shapes */
	public enum ArrowShape {
		None,
		Diamond,	 // --<>
		Triangle, // --|>
		Arrow,    // --->
		Circle    // ---O
	};
	
	/** Point of connection */
	public enum ConnectionPoint {
		NorthWest,  North,  NorthEast,
		     West,  Center,      East,
		SouthWest,  South,  SouthEast,
	};

	public Connection head;
	public Connection tail;
	
	/** Create a link betwen the <i>head</i> shape to the <i>tail</i> shape.
	 * 
	 * @param head
	 * @param tail
	 */
	public Link(Connection head, Connection tail) {
		this.head = head;
		this.tail = tail;
	}

	
	public static class Connection {
		public Shape shape;
		public ConnectionPoint point = ConnectionPoint.NorthWest;
		public ArrowShape arrow = ArrowShape.None;

		/** Connection to north-west corner of shape, no arrow */
		public Connection(Shape shape) {
			this.shape = shape;
		}
		
		/** Connection to specified portion of shape, no arrow */
		public Connection(Shape shape, ConnectionPoint point) {
			this.shape = shape;
			this.point = point;
		}

		/** Connection to north-west corner of shape, specified arrow */
		public Connection(Shape shape, ArrowShape arrow) {
			this.shape = shape;
			this.arrow = arrow;
		}

		/** Connection to specified portion of shape, specified arrow */
		public Connection(Shape shape, ConnectionPoint point, ArrowShape arrow) {
			this.shape = shape;
			this.point = point;
			this.arrow = arrow;
		}
		
	}

}
