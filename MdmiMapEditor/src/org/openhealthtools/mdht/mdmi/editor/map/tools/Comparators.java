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

import java.util.Comparator;

import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.MessageModel;
import org.openhealthtools.mdht.mdmi.model.Node;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.SemanticElementRelationship;
import org.openhealthtools.mdht.mdmi.model.ToBusinessElement;
import org.openhealthtools.mdht.mdmi.model.ToMessageElement;

/** Comparators for all kinds of model objects */
public class Comparators {

	public static class DataTypeComparator implements Comparator<MdmiDatatype> {
		@Override
		public int compare(MdmiDatatype o1, MdmiDatatype o2) {
			String n1 = o1.getTypeName() == null ? "" : o1.getTypeName();
			String n2 = o2.getTypeName() == null ? "" : o2.getTypeName();
			return n1.compareToIgnoreCase(n2);
		}
	}

	public static class FieldComparator implements Comparator<Field> {
		@Override
		public int compare(Field o1, Field o2) {
			String n1 = o1.getName() == null ? "" : o1.getName();
			String n2 = o2.getName() == null ? "" : o2.getName();
			return n1.compareToIgnoreCase(n2);
		}
	}

	public static class SemanticElementComparator implements Comparator<SemanticElement> {
		@Override
		public int compare(SemanticElement o1, SemanticElement o2) {
			String n1 = o1.getName() == null ? "" : o1.getName();
			String n2 = o2.getName() == null ? "" : o2.getName();
			return n1.compareToIgnoreCase(n2);
		}
	}

	public static class MessageGroupComparator implements Comparator<MessageGroup> {
		@Override
		public int compare(MessageGroup o1, MessageGroup o2) {
			String n1 = o1.getName() == null ? "" : o1.getName();
			String n2 = o2.getName() == null ? "" : o2.getName();
			return n1.compareToIgnoreCase(n2);
		}
	}


	public static class MessageModelComparator implements Comparator<MessageModel> {
		@Override
		public int compare(MessageModel o1, MessageModel o2) {
			String n1 = o1.getMessageModelName() == null ? "" : o1.getMessageModelName();
			String n2 = o2.getMessageModelName() == null ? "" : o2.getMessageModelName();
			return n1.compareToIgnoreCase(n2);
		}
	}
	
	public static class BusinessElementReferenceComparator implements Comparator<MdmiBusinessElementReference> {
		@Override
		public int compare(MdmiBusinessElementReference o1, MdmiBusinessElementReference o2) {
			String n1 = o1.getName() == null ? "" : o1.getName();
			String n2 = o2.getName() == null ? "" : o2.getName();
			return n1.compareToIgnoreCase(n2);
		}
	}

	
	public static class SyntaxNodeComparator implements Comparator<Node> {
		@Override
		public int compare(Node o1, Node o2) {
			String n1 = o1.getName() == null ? "" : o1.getName();
			String n2 = o2.getName() == null ? "" : o2.getName();
			return n1.compareToIgnoreCase(n2);
		}
	}

	public static class ToMessageElementComparator implements Comparator<ToMessageElement> {
		@Override
		public int compare(ToMessageElement o1, ToMessageElement o2) {
			String n1 = o1.getName() == null ? "" : o1.getName();
			String n2 = o2.getName() == null ? "" : o2.getName();
			return n1.compareToIgnoreCase(n2);
		}
	}

	public static class ToBusinessElementComparator implements Comparator<ToBusinessElement> {
		@Override
		public int compare(ToBusinessElement o1, ToBusinessElement o2) {
			String n1 = o1.getName() == null ? "" : o1.getName();
			String n2 = o2.getName() == null ? "" : o2.getName();
			return n1.compareToIgnoreCase(n2);
		}
	}


	public static class SemanticElementRelationshipComparator implements Comparator<SemanticElementRelationship> {
		@Override
		public int compare(SemanticElementRelationship o1, SemanticElementRelationship o2) {
			String n1 = o1.getName() == null ? "" : o1.getName();
			String n2 = o2.getName() == null ? "" : o2.getName();
			return n1.compareToIgnoreCase(n2);
		}
	}

}
