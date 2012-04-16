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
package org.openhealthtools.mdht.mdmi.editor.map.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.actions.DefaultMenuAction;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ViewObjectDescriptions;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;

/**
 * @author Conway
 *
 * Action for View Descriptions
 */
public class ViewDescriptionsAction extends DefaultMenuAction {
	private static final long serialVersionUID = -1;

	@Override
	public void execute(ActionEvent actionEvent) {
		MessageGroup group = selectMessageGroup();
		if (group != null) {
			ViewObjectDescriptions view = new ViewObjectDescriptions(group);
			view.setVisible(true);
		}
	}
	
	public static MessageGroup selectMessageGroup() {
		// pick a message group if there are more than one
		List<MessageGroup> groups = SelectionManager.getInstance().getEntitySelector().getMessageGroups();
		if (groups.size() == 1) {
			return groups.get(0);
		}
		
		MessageGroup group = null;
		
		List<String> groupNames = new ArrayList<String>();
		for (MessageGroup msgGroup : groups) {
			if (msgGroup.getName() != null && msgGroup.getName().length() > 0) {
				groupNames.add(msgGroup.getName());
			}
		}
		
		String groupName;
		if (groupNames.size() == 0) {
			return null;
		} else if (groupNames.size() == 1) {
			groupName = groupNames.get(0);
		} else {
			groupName = (String)JOptionPane.showInputDialog(SystemContext.getApplicationFrame(), "Select a Message Group",
					"Message Group Selection", JOptionPane.PLAIN_MESSAGE, null,
					groupNames.toArray(new String[0]), groupNames.get(0)); 
			
		}
		
		if (groupName != null) {
			group = getMessageGroupByName(groups, groupName);
		}
		
		return group;
	}
	
	private static MessageGroup getMessageGroupByName(List<MessageGroup> groups, String name) {
		for (MessageGroup group : groups) {
			if (group.getName().equals(name)) {
				return group;
			}
		}
		return null;
	}

}
