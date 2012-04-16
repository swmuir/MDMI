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
import java.awt.FlowLayout;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** A JPanel with a flow layout intended for showing status information */
public class StatusBar extends JPanel {

	private static final long serialVersionUID = 5029594518929636588L;

	private JLabel m_statusLabel = null;

	public StatusBar() {
		super(new FlowLayout(FlowLayout.LEFT, 5, 0));
		setBorder(BorderFactory.createLoweredBevelBorder());
		setPreferredSize(new Dimension(24,24));
	}

	/** Add and show a status message.
	 *  If there is already a message component, it will be updated with the
	 *  new text, if not, one will be added */
	public void setStatusMessage(String text) {
		getStatusLabel().setText(text);
		getStatusLabel().revalidate();
	}

	/** Add/show status icon */
	public void setStatusIcon(Icon icon) {
		getStatusLabel().setIcon(icon);
		getStatusLabel().revalidate();
	}

	/** Add/show status icon */
	public void setStatusIcon(String iconPath) {
		URL url = getClass().getResource(iconPath);
		if (url != null) {
			Icon icon = new ImageIcon(url);
			setStatusIcon(icon);
		}
	}

	/** Return the status label, creating and adding it if necessary */
	public JLabel getStatusLabel() {
		if (m_statusLabel == null) {
			m_statusLabel = new JLabel();
			add(m_statusLabel);
		}
		return m_statusLabel;
	}

}
