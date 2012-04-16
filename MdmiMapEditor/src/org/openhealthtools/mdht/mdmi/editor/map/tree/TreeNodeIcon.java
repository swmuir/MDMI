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

import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openhealthtools.mdht.mdmi.model.Bag;
import org.openhealthtools.mdht.mdmi.model.Choice;
import org.openhealthtools.mdht.mdmi.model.ConversionRule;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.DTSDerived;
import org.openhealthtools.mdht.mdmi.model.DataRule;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.LeafSyntaxTranslator;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementRule;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MdmiDomainDictionaryReference;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.MessageModel;
import org.openhealthtools.mdht.mdmi.model.MessageSyntaxModel;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.SemanticElementBusinessRule;
import org.openhealthtools.mdht.mdmi.model.SemanticElementRelationship;
import org.openhealthtools.mdht.mdmi.model.SemanticElementSet;

/** Icons used in the tree */
public class TreeNodeIcon {

	/** Resource for localization */
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tree.Local");

	public static final Icon BusinessElementReferenceIcon = getIcon(s_res.getString("BusinessElementReferenceNode.icon"));
	public static final Icon BusinessElementRuleIcon = getIcon(s_res.getString("BusinessElementRuleNode.icon"));
	public static final Icon BusinessElementRuleSetIcon = getIcon(s_res.getString("BusinessElementRuleSetNode.icon"));
	public static final Icon DataRuleSetIcon = getIcon(s_res.getString("DataRuleSetNode.icon"));
	public static final Icon DataRuleIcon = getIcon(s_res.getString("DataRuleNode.icon"));
	public static final Icon DataTypeSetIcon  = getIcon(s_res.getString("DataTypeSetNode.icon"));
	public static final Icon DataTypeIcon = getIcon(s_res.getString("DataTypeNode.icon"));
	public static final Icon ComplexDataTypeIcon = getIcon(s_res.getString("DataTypeNode.complexIcon"));
	public static final Icon DerivedDataTypeIcon = getIcon(s_res.getString("DataTypeNode.derivedIcon"));
	public static final Icon DomainDictionaryReferenceIcon = getIcon(s_res.getString("DomainDictionaryReferenceNode.icon"));
	public static final Icon MessageGroupIcon = getIcon(s_res.getString("MessageGroupNode.icon"));
	public static final Icon MessageModelIcon = getIcon(s_res.getString("MessageModelNode.icon"));
	public static final Icon MessageSyntaxModelIcon = getIcon(s_res.getString("MessageSyntaxModelNode.icon"));
	public static final Icon SemanticElementIcon = getIcon(s_res.getString("SemanticElementNode.icon"));
	public static final Icon SemanticElementRelationshipIcon = getIcon(s_res.getString("SemanticElementRelationshipNode.icon"));
	public static final Icon SemanticElementRelationshipSetIcon = getIcon(s_res.getString("SemanticElementRelationshipSetNode.icon"));
	public static final Icon SemanticElementBusinessRuleIcon = getIcon(s_res.getString("SemanticElementBusinessRuleNode.icon"));
	public static final Icon SemanticElementBusinessRuleSetIcon = getIcon(s_res.getString("SemanticElementBusinessRuleSetNode.icon"));
	public static final Icon SemanticElementSetIcon = getIcon(s_res.getString("SemanticElementSetNode.icon"));
	public static final Icon SyntaxBagIcon = getIcon(s_res.getString("SyntaxBagNode.icon"));
	public static final Icon SyntaxChoiceIcon = getIcon(s_res.getString("SyntaxChoiceNode.icon"));
	public static final Icon SyntaxLeafIcon = getIcon(s_res.getString("SyntaxLeafNode.icon"));
	public static final Icon ConversionRuleIcon = getIcon(s_res.getString("ConversionRuleNode.icon"));
	public static final Icon ToBusinessElementSetIcon = getIcon(s_res.getString("ToBusinessElementSetNode.icon"));
	public static final Icon ToMessageElementSetIcon = getIcon(s_res.getString("ToMessageElementSetNode.icon"));
	public static final Icon FieldIcon = getIcon(s_res.getString("FieldNode.icon"));
	public static final Icon EnumerationIcon = getIcon(s_res.getString("EnumerationNode.icon"));

	
	/** Return the tree node icon for this type of user-object */
	public static Icon getTreeIcon(Class<?> objectClass) {

		if (MdmiBusinessElementReference.class.equals(objectClass)) {
			return BusinessElementReferenceIcon;
		}
		else if (MdmiBusinessElementRule.class.equals(objectClass)) {
			return BusinessElementRuleIcon;
		}
		else if (DataRule.class.equals(objectClass)) {
			return DataRuleIcon;
		}
		else if (DTComplex.class.isAssignableFrom(objectClass)) {
			return ComplexDataTypeIcon;
		}
		else if (DTSDerived.class.isAssignableFrom(objectClass)) {
			return DerivedDataTypeIcon;
		}
		else if (MdmiDatatype.class.isAssignableFrom(objectClass)) {
			return DataTypeIcon;
		}
		else if (MdmiDomainDictionaryReference.class.isAssignableFrom(objectClass)) {
			return DomainDictionaryReferenceIcon;
		}
		else if (MessageGroup.class.isAssignableFrom(objectClass)) {
			return MessageGroupIcon;
		}
		else if (MessageModel.class.isAssignableFrom(objectClass)) {
			return MessageModelIcon;
		}
		else if (MessageSyntaxModel.class.isAssignableFrom(objectClass)) {
			return MessageSyntaxModelIcon;
		}
		else if (SemanticElement.class.isAssignableFrom(objectClass)) {
			return SemanticElementIcon;
		}
		else if (SemanticElementRelationship.class.isAssignableFrom(objectClass)) {
			return SemanticElementRelationshipIcon;
		}
		else if (SemanticElementBusinessRule.class.isAssignableFrom(objectClass)) {
			return SemanticElementBusinessRuleIcon;
		}
		else if (SemanticElementSet.class.isAssignableFrom(objectClass)) {
			return SemanticElementSetIcon;
		}
		else if (Bag.class.isAssignableFrom(objectClass)) {
			return SyntaxBagIcon;
		}
		else if (Choice.class.isAssignableFrom(objectClass)) {
			return SyntaxChoiceIcon;
		}
		else if (LeafSyntaxTranslator.class.isAssignableFrom(objectClass)) {
			return SyntaxLeafIcon;
		}
		else if (ConversionRule.class.isAssignableFrom(objectClass)) {
			return ConversionRuleIcon;
		}
		else if (Field.class.isAssignableFrom(objectClass)) {
			return FieldIcon;
		}

		return null;
	}
	

	/** create an icon from a path */
	public static ImageIcon getIcon(String iconPath) {
		ImageIcon icon = null;
		URL url = TreeNodeIcon.class.getResource(iconPath);
		if (url != null) {
			icon = new ImageIcon(url);
		}
		return icon;
	}
	
}
