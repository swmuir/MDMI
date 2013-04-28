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
package org.openhealthtools.mdht.mdmi.editor.map.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;

import org.openhealthtools.mdht.mdmi.editor.common.components.CheckBoxListPanel;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;

/** A collection of SemanticElements displayed as check boxes in a list.
 * Any one, or more of the elements can be selected.
 * 
 * Changes to the selections are noted via a property change event using the
 * NUM_SELECTIONS_PROPERTY property name.
 * @author Conway
 *
 */
public class SemanticElementSelectionPanel extends CheckBoxListPanel {

	public SemanticElementSelectionPanel(Collection <SemanticElement> elements) {
		super();
		fillListModel(elements);
	}
	
	/** Fill the list model from this collection of elements */
	public void fillListModel(Collection <SemanticElement> elements) {
		DefaultListModel listModel = getModel();
		listModel.removeAllElements();
		
      for (SemanticElement element : elements) {
         addCheckBox(new SemanticElementCheckBox(element));
      }
	}

	/** Find the checkbox that contains this element */
	public SemanticElementCheckBox findCheckBox(SemanticElement element) {
		for (JCheckBox checkbox : getCheckBoxes()) {
			if (checkbox instanceof SemanticElementCheckBox &&
					((SemanticElementCheckBox)checkbox).element == element) {
				return (SemanticElementCheckBox)checkbox;
			}
		}
		return null;
	}

	/** Get all selected items in the list */
	public List<SemanticElement> getSelectedElements() {
		ArrayList<SemanticElement> elements = new ArrayList<SemanticElement>();
		
		for (JCheckBox checkbox : getCheckBoxes()) {
      	if (checkbox.isSelected()) {
            elements.add(((SemanticElementCheckBox)checkbox).element);
         }
      }
      
		return elements;
	}
	
	/** Select all items specified */
	public void setSelectedElements(Collection<SemanticElement> elements) {
		selectAll(false);

		// select items in list
		for (SemanticElement element : elements) {
			JCheckBox checkbox = findCheckBox(element);
			checkbox.setSelected(true);
		}
	}

	////////////////////////////////////////////////////////////////

	/** Wrapper for JCheckBox created from a semantic element */
	public static class SemanticElementCheckBox extends JCheckBox {
		public SemanticElement element;
		public SemanticElementCheckBox(SemanticElement element) {
			super.setText(element.getName());
			this.element = element;
		}
	}

}
