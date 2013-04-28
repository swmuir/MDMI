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
package org.openhealthtools.mdht.mdmi.editor.common.components;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;

/** Create a read-only JTextArea that supports word wrapping, and uses the
 * standard dialog font. The text area will be transparent - so that it will be colored
 * the same as the parent container.
 */
public class WrappingDisplayText extends JTextArea {
	private static final long serialVersionUID = -4263867271396692006L;

	public WrappingDisplayText(String text) {
		this();
		setText(text);
	}

	public WrappingDisplayText() {
		setFont(Standards.DEFAULT_FONT);
		setLineWrap(true);
		setWrapStyleWord(true);
		setOpaque(false);
		setEditable(false);
	}

	@Override
	public void setText(String text) {
		super.setText(text);

		Dimension pref = getPreferredSize();

		Font font = getFont();
		FontMetrics fm = getFontMetrics(font);
		int textWidth = fm.stringWidth(getText()) + 10;

		// force it to wrap if the line is too wide
		pref.width = Math.min(450, textWidth);

		setSize(pref);
	}


	public static void main(String [] args) {
		String one = "This is a really long line." +
		" If this were used in a standard JOptionPane, it would stretch accross the screen." +
		" Since we're using a Wrapping Text, it will do the right thing";

		String two = "This is a short line";

		JOptionPane.showMessageDialog(null, new WrappingDisplayText(one), "Sample", JOptionPane.INFORMATION_MESSAGE);
		JOptionPane.showMessageDialog(null, new WrappingDisplayText(two), "Sample", JOptionPane.INFORMATION_MESSAGE);
	}
}
