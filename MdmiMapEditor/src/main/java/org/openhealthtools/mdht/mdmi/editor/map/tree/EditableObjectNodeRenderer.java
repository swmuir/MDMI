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
package org.openhealthtools.mdht.mdmi.editor.map.tree;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.openhealthtools.mdht.mdmi.editor.map.editor.AbstractComponentEditor;

/** A TreeCellRenderer that specifically handles EditableObjectNodes.
 * To Take advantage of ToolTips, the user must regester the tree as 
 * with the tool tip manager:
 * <code>
 *			ToolTipManager.sharedInstance().registerComponent(myTree);
 * <code>
 * @author Conway
 *
 */
public class EditableObjectNodeRenderer extends DefaultTreeCellRenderer {
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		
		Icon icon = null;
		String toolTip = null;
		
		if (value instanceof EditableObjectNode) {
			EditableObjectNode node = (EditableObjectNode)value;
			icon = node.getNodeIcon();
			toolTip = createToolTip(node);
		}

		Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded,
				leaf, row, hasFocus);

		if (icon != null) {
			setIcon(icon);
		}
		setToolTipText(toolTip);

		return c;
	}

	/** Create a tool tip for this node. If the node has a tool tip 
	 * associated with it, that tip wil be shown, preceded by the node display type.
	 * If no tool tip exists, just the node display type will be shown.
	 * 
	 * @param node
	 * @return
	 */
	private String createToolTip(EditableObjectNode node) {
		StringBuilder toolTip = new StringBuilder();
		// start with type
		toolTip.append(node.getDisplayType());
		
		// get tip from node
		String nodeTip = node.getToolTipText();
		if (nodeTip != null && nodeTip.length() > 0) {
			// add nodeTip, but add line-breaks it so that it doesn't excede 100 chars per line
			toolTip .append(" -<br>");
			toolTip.append(AbstractComponentEditor.wordwrap(nodeTip, 100, "<br>"));
			// wrap in html
			toolTip.insert(0, "<html>");
			toolTip.append("</html>");
		}
		
		return toolTip.toString();
	}

}
