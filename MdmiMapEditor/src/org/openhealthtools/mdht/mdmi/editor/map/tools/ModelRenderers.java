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

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.MessageModel;

/** Renderers for all kinds of model objects */
public class ModelRenderers {
	
	public static class MessageGroupRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			if (value instanceof MessageGroup) {
				value = ((MessageGroup)value).getName();
				if (value == null || "".equals(value)) {
					value = ClassUtil.s_unNamedItem;
				}
			}
			return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		}
		
	}


	public static class MessageModelRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			if (value instanceof MessageModel) {
				value = ((MessageModel)value).getMessageModelName();
				if (value == null || "".equals(value)) {
					value = ClassUtil.s_unNamedItem;
				}
			}
			return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		}
		
	}

	public static class BusinessElementRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			if (value instanceof MdmiBusinessElementReference) {
				value = ((MdmiBusinessElementReference)value).getName();
				if (value == null || "".equals(value)) {
					value = ClassUtil.s_unNamedItem;
				}
			}
			return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		}
		
	}
}
