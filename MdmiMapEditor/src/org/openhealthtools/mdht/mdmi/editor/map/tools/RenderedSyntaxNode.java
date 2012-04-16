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
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Stack;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

import org.openhealthtools.mdht.mdmi.model.Node;

/** A component for rendering a syntax node in a table, such that the
 * node will appear as if in a tree
 * @author Conway
 *
 */
public abstract class RenderedSyntaxNode extends JPanel {

	private enum Symbol {
		Blank,
		Continue,	// |
		IntermediateNode,	// |-
		LastNode	// |_
	}
	
	private JTable m_table;
	
	public RenderedSyntaxNode(JTable table) {
		setLayout(new GridBagLayout());
		m_table = table;
	}
	
	public JTable getTable() {
		return m_table;
	}

	/** return the component to be shown in the renderer */
	public Component render(Node node, Component defaultComponent, int row) {
		removeAll();
		setBackground(defaultComponent.getBackground());

		if (node != null) {
			buildUI(node, defaultComponent, true);
		} else {
			// find next node in table 
			Node nextNode = getNextNodeInTable(row);
			if (nextNode != null) {
				buildUI(nextNode, defaultComponent, false);
			} else {
				add(defaultComponent);
			}
		}
		return this;
	}
	
	public Node getNextNodeInTable(int row) {
		Node nextNode = null;
		for (int i=row+1; i<getRowCount() && nextNode == null; i++) {
			nextNode = getNodeAtRow(i);
		}
		return nextNode;
	}

	public int getRowForNode(Node syntaxNode) {
		int rowCount = getRowCount();
		for (int i=0; i<rowCount; i++) {
			Node nodeAtRow = getNodeAtRow(i);
			if (syntaxNode == nodeAtRow) {
				return i;
			}
		}
		return -1;
	}
	
	abstract public Node getNodeAtRow(int row);
	
	public int  getRowCount() {
		return m_table.getRowCount();
	}
	
	private void buildUI(Node node, Component component, boolean realNode) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		
		gbc.gridx = 0;
		
		// count the depth from the top
		Stack<Node> nodes = new Stack<Node>();
		Node top = getTopDisplayedParent(node);
		while (node != null && node != top) {
			nodes.push(node);
			node = node.getParentNode();
		}

		JLabel icon;
		while (!nodes.isEmpty()) {
			node = nodes.pop();
			boolean lastChildOfParent = isLastChild(node);
			
			Symbol symbol = Symbol.Continue;
			if (nodes.isEmpty()) {
				// last node in stack
				if (!realNode) {
					symbol = Symbol.Continue;
				} else if (lastChildOfParent) {
					symbol = Symbol.LastNode;
				} else {
					symbol = Symbol.IntermediateNode;
				}
			} else {
				if (lastChildOfParent) {
					symbol = Symbol.Blank;
				} else {
					symbol = Symbol.Continue;
				}
			}

			icon = new JLabel(new TreeSpaceIcon(symbol));
			add(icon, gbc);

			gbc.gridx++;
		}			

		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		add(component, gbc);

	}

	/** Get the top displayed parent of this node - all other nodes will be indented relative to this one
	 * @return
	 */
	public Node getTopDisplayedParent(Node node) {
		Node top = null;
		while (node != null) {
			// is it in table
			int row = getRowForNode(node);
			if (row == -1) {
				break;
			}
			top = node;
			node = node.getParentNode();
		}
		return top;
	}

	/** Is this node the last child of its parent to be displayed */
	private boolean isLastChild(Node syntaxNode) {
		int row = getRowForNode(syntaxNode);
		if (row >= 0) {
			// look at node in next row - if it has the same parent, return false
			for (int i=row+1; i<getRowCount(); i++) {
				Node nextNode = getNodeAtRow(i);
				if (nextNode != null) {
					if (syntaxNode.getParentNode() == nextNode.getParentNode()) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private class TreeSpaceIcon implements Icon {
		private Symbol m_symbol;
		public TreeSpaceIcon(Symbol sym) {
			m_symbol = sym;
		}
		@Override
		public int getIconHeight() {
			return 16;
		}

		@Override
		public int getIconWidth() {
			return 17;
		}
		
		public Color getColor() {
			return Color.LIGHT_GRAY;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			//  ---------
			// |    *    |
			// |    *    |
			// |    *    |
			//  ---------
			// draw line 
			int xC = (x + getIconWidth())/2;
			int yC = (y + getIconHeight())/2;
			g.setColor(getColor());
			switch (m_symbol) {
			case Continue:
				// vertical line
				g.drawLine(xC, y, xC, y+getIconHeight());
				break;
			case IntermediateNode:
				// vertical, and horizontal
				g.drawLine(xC, y, xC, y+getIconHeight());
				g.drawLine(xC, yC, x+getIconWidth(), yC);
				break;
			case LastNode:
				// half-vertical, and horizontal
				g.drawLine(xC, y, xC, yC);
				g.drawLine(xC, yC, x+getIconWidth(), yC);
				break;
			}
		}
	}

}
