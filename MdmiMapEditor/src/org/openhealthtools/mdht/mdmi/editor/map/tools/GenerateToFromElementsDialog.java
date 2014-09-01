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

import java.awt.Frame;

import org.openhealthtools.mdht.mdmi.model.ConversionRule;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;

/** A dialog used for generating To/From Rules for a selected Semantic Element
 * @author Conway
 *
 */
public class GenerateToFromElementsDialog extends GenerateToFromRelationshipRuleDialog {

	public GenerateToFromElementsDialog(Frame owner, SemanticElement semanticElement) {
		super(owner, semanticElement, false);	// don't show relationship selection
	}


	public static String generateRuleText(String language, ConversionRule theRule, String seFieldName, String beFieldName) {
		return GenerateToFromRelationshipRuleDialog.generateRuleText(language, theRule, seFieldName, beFieldName, 
				null, null);	// no relationship data
	}
	
}
