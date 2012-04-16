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

import java.util.Collection;

import org.openhealthtools.mdht.mdmi.model.MessageGroup;

/** The top of the tree */
public class MdmiModelNode extends EditableObjectNode {
	private static final String s_display = s_res.getString("MdmiModelTree.messageMap");
	
	private Collection<NewObjectInfo> m_newObjectInfo = null;

	public MdmiModelNode() {
		super(s_display);
		setDisplayType(s_display);
	}
	
	
	
	@Override
	public String getDisplayName(Object userObject) {
		return s_display;
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public boolean isRemovable() {
		return false;
	}


	/** What new items can be created */
	@Override
	public Collection<NewObjectInfo> getNewObjectInformation(boolean changeType) {
		if (m_newObjectInfo == null) {
			m_newObjectInfo = super.getNewObjectInformation(changeType);
			m_newObjectInfo.add(new NewMessageGroup());
		}
		
		return m_newObjectInfo;
	}
	
	///////////////////////////////////////////////////////////////
	//    Custom Classes
	//////////////////////////////////////////////////////////////
	
	public class NewMessageGroup extends NewObjectInfo {
		
		@Override
		public Class<?> getChildClass() {
			return MessageGroup.class;
		}
		
		@Override
		public EditableObjectNode addNewChild(Object childObject) {
			MessageGroup group = (MessageGroup)childObject;
			
			return new MessageGroupNode(group);
		}

		@Override
		public String getChildName(Object childObject) {
			return ((MessageGroup)childObject).getName();
		}

		@Override
		public void setChildName(Object childObject, String newName) {
			((MessageGroup)childObject).setName(newName);
		}
	}
}
