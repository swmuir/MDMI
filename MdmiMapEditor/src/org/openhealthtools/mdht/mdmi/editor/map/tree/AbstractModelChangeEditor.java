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

import java.text.MessageFormat;

import javax.swing.JOptionPane;

import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.ModelChangeEvent;
import org.openhealthtools.mdht.mdmi.editor.map.ModelChangeListener;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.editor.DataEntryFieldInfo;
import org.openhealthtools.mdht.mdmi.editor.map.editor.DataFormatException;
import org.openhealthtools.mdht.mdmi.editor.map.editor.GenericEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.IEditorField;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;

/** A generic editor that responds to model change events */
public class AbstractModelChangeEditor extends GenericEditor implements ModelChangeListener  {

	public AbstractModelChangeEditor(MessageGroup group, Class<?> objectClass) {
		super(group, objectClass);
	}

	@Override
	public void addNotify() {
		super.addNotify();
		SelectionManager.getInstance().addModelChangeListener(this);
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		SelectionManager.getInstance().removeModelChangeListener(this);
	}
	
	/** Only show first N characters of a string */
	public static String truncate(String string, int max) {
		if (string.length() >  max) {
			string = string.substring(0, max-3) + "...";
		}
		return string;
	}

	@Override
	public void modelChanged(ModelChangeEvent e) {
		if (e.getSource() == getEditObject()) {
			Object model = getEditObject();
			// we will get this when another UI changes a value - we will prompt the user
			// whether to take the change
			for (DataEntryFieldInfo fieldInfo : getDataEntryFieldList()) {
				try {
					String fieldName = fieldInfo.getFieldName();
					IEditorField editorField = getEditorField(fieldName);
					if (editorField != null) {
						// Call getFieldName on edit object
						Object newValue = fieldInfo.getValueFromModel(model);
						// treat "" the same as null
						if (newValue == null) newValue = "";
						Object oldValue = editorField.getValue();
						if (oldValue == null) oldValue = "";
						if (!oldValue.equals(newValue)) {
							SelectionManager.getInstance().getEntityEditor().showEditPanel(model);
							//The FieldName field of the ITEM you are editing has been changed
							// from 'oldValue' to 'newValue'.
							// Do you want to accept those changes?
							// Pres "Yes" to take the changes.
							// Press "No" to ignore.
							String message = MessageFormat.format(EditableObjectNode.s_res.getString("EditableObjectNode.modelChangedMessage"),
									ClassUtil.beautifyName(fieldName), 
									ClassUtil.beautifyName(model.getClass()),
									truncate(getModelName(oldValue), 30),
									truncate(getModelName(newValue), 30) );
							int opt = JOptionPane.showConfirmDialog(this, message,
									EditableObjectNode.s_res.getString("EditableObjectNode.modelChangedTitle"),
									JOptionPane.YES_NO_OPTION);
							if (opt == JOptionPane.YES_OPTION) {
								// changing the display to reflect the model should not affect the dirty flag
								boolean modified = isModified();
								editorField.setDisplayValue(newValue);
								// restore
								setModified(modified);
							} else {
								// mark as dirty, since model differs from display
								setModified(true);
							}
						}
					}
				} catch (DataFormatException ex) {
					SelectionManager.getInstance().getStatusPanel().writeException(ex);

				} catch (Exception ex) {
					// Unable to invoke '{0}' on {1}
					String msg = MessageFormat.format(s_res.getString("GenericEditor.invocationErrorFormat"),
							fieldInfo.getSetMethod().getName(), getObjectClass().getName());
					SelectionManager.getInstance().getStatusPanel().writeException(msg, ex);
				}
			}
		}
	}

}