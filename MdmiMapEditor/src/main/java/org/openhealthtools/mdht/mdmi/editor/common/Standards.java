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
 * Created on Nov 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.openhealthtools.mdht.mdmi.editor.common;

import java.awt.Font;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.KeyStroke;
import javax.swing.border.Border;

/**
 * @author Conway
 *
 * Standard Colors, Insets, etc
 */
public class Standards {
    /////////////////////////////////////////
    //   Size Matters
    ////////////////////////////////////////
    
    /** Left inset */
    public static final int LEFT_INSET = 6;

    /** Right inset */
    public static final int RIGHT_INSET = LEFT_INSET;
    
    /** Top inset */
    public static final int TOP_INSET = 4;
    
    /** Bottom inset */
    public static final int BOTTOM_INSET = 4;

    /////////////////////////////////////////
    //   Fonts
    ////////////////////////////////////////
    
    /** Default Font */
    public static final Font DEFAULT_FONT = new Font("Dialog", Font.PLAIN, 12);

    /////////////////////////////////////////
    //   Colors
    ////////////////////////////////////////
//    /** Inbox Color */
//    public static final Color INBOX_COLOR = new Color(0xD4ECFB);   // light blue
//    
//    /** Outbox Color */
//    public static final Color OUTBOX_COLOR = new Color(0xFAEDC0);   // light amber
//    
//    /** Executed Trades Color */
//    public static final Color EXECUTED_COLOR = new Color(0xFCDAD5); // pinkish
//
//    /** Invalid Field Color */
//    public static final Color INVALID_FIELD_COLOR = new Color(0xFF0000); // red
//    
    
    ///////////////////////////////////////////
    //  Standard Key Strokes							
    ///////////////////////////////////////////
    /** Copy ^C */
    public static final KeyStroke CTRL_C = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C,
          java.awt.Event.CTRL_MASK);

    /** Paste ^V */
    public static final KeyStroke CTRL_V = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V,
          java.awt.Event.CTRL_MASK);
    
    /** Cut ^X */
    public static final KeyStroke CTRL_X = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X,
          java.awt.Event.CTRL_MASK);
    
    /** Delete */
    public static final KeyStroke DEL = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0);

    
    /** Default insets surrounding a component.
     * comonents.
     * Ex:
     * <code>
     *      ______________   ___
     *     |              |   |
     *     |              |  TOP
     *     |    ______    |  _|_
     *     |   |widget|   | 
     *     |   |______|   |  ___
     *     |              |   |
     *     |              | BOTTOM
     *     |______________|  _|_
     *     |---|      |---| 
     *      LEFT      RIGHT
     * </code>
     * */
    public static final Insets getInsets() {
        return new Insets(TOP_INSET, LEFT_INSET, BOTTOM_INSET, RIGHT_INSET);
    }
    
    /** get empty border with efault insets */
    public static final Border createEmptyBorder() {
        return BorderFactory.createEmptyBorder(TOP_INSET, LEFT_INSET, BOTTOM_INSET, RIGHT_INSET);
    }
    
}
