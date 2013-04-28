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
 */
package org.openhealthtools.mdht.mdmi.editor.common;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/**
 * @author Conway
 *
 * An abstract document filter that allows only numeric values.
 * Sample usage:
 * <code>
 *   JTextField textField = new JTextField();
 *   Document textDoc = textField.getDocument();
 *   DocumentFilter filter = new IntegerDocumentFilter();
 *   (AbstractDocument)textDoc).setDocumentFilter(filter);
 * </code>
 * 
 */
public abstract class NumericDocumentFilter extends DocumentFilter {
    private Number m_numericValue;
    private boolean m_allowsLetters = false;
    
    public NumericDocumentFilter() {
    }
    
    /** Allow certain letters in the numeric value (i.e. "x" for hex).
     * The default is to not allow letters.
     */
    public void setAllowsLetters(boolean allowsLetters) {
       m_allowsLetters = allowsLetters;
    }
    
    /** Determine whether certain letters are permitted */
    public boolean isAllowsLetters() {
       return m_allowsLetters;
    }
    
    /** Return the numeric value of this document */
    public Number getNumericValue() {
        return m_numericValue;
    }

    @Override
    public void insertString(DocumentFilter.FilterBypass fb, 
            int offset, String string, AttributeSet attr) 
                        throws BadLocationException {   
        
        if (string == null) {
            return;
        } else {
            replace(fb, offset, 0, string, attr);
        }
    }   

    @Override
    public void remove(DocumentFilter.FilterBypass fb, 
            int offset, int length)
                        throws BadLocationException {
        
        replace(fb, offset, length, "", null);
    }

    @Override
    public void replace(DocumentFilter.FilterBypass fb, 
            int offset, int length, String text, AttributeSet attrs) 
                        throws BadLocationException {   
        
        Document doc = fb.getDocument();
        int currentLength = doc.getLength();
        String currentContent = doc.getText(0, currentLength);
        String before = currentContent.substring(0, offset);
        String after = currentContent.substring(length+offset, currentLength);
        String newValue = before + (text == null ? "" : text) + after;
        
        // treat negative sign by itself as -0
        if ("-".equals(newValue)) {
           newValue = "-0";
        }
        m_numericValue = checkInput(newValue, offset);
        
        // check for letters
        if (!isAllowsLetters()) {
           checkForLetters(newValue);
        }
        
        fb.replace(offset, length, text, attrs);
    }
    
    /** Check if the string contains any letters */
    protected void checkForLetters(String proposedValue) throws BadLocationException {
       for (int i=0; i<proposedValue.length(); i++) {
          char ch = proposedValue.charAt(i);
          if (Character.isLetter(ch)) {
             throw new BadLocationException(proposedValue, i);
          }
       }
    }
    
    /** validate the complete text string. The <i>offset</i> value is provided
     * for formatting the exception
     */
    protected abstract Number checkInput(String proposedValue, int offset)
                        throws BadLocationException;
}
