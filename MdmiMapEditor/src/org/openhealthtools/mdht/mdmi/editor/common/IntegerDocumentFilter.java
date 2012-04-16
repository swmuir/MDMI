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
/*
 * Created on Nov 8, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.openhealthtools.mdht.mdmi.editor.common;

import javax.swing.text.BadLocationException;

/**
 * @author Conway
 *
 * A document filter that allows only integer values.
 * Sample usage:
 * <code>
 *   JTextField textField = new JTextField();
 *   Document textDoc = textField.getDocument();
 *   DocumentFilter filter = new IntegerDocumentFilter();
 *   (AbstractDocument)textDoc).setDocumentFilter(filter);
 *   //       textField.setDocument(textDoc);
 * </code>
 * 
 */
public class IntegerDocumentFilter extends NumericDocumentFilter {
   
    public IntegerDocumentFilter() {
    }
    

    @Override
    protected Number checkInput(String proposedValue, int offset)
                        throws BadLocationException {
        Integer newValue = null;
        if (proposedValue.length() > 0) {
            try {
                newValue = Integer.valueOf(proposedValue);
            } catch (NumberFormatException e) {
                throw new BadLocationException(proposedValue, offset);
            }
        }
        return newValue;
    }
}
