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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;

public abstract class AbstractHelpAboutDialog extends BaseDialog {
	private static final long serialVersionUID = -854231632645719272L;

	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.common.components.Local");

	/** List Model */
	protected DefaultListModel m_model = new DefaultListModel();

	protected AbstractHelpAboutDialog(Frame owner) {
		super(owner, BaseDialog.OK_BUTTON_OPTION);
	}

	protected void createComponents() {
		setTitle(MessageFormat.format(s_res.getString("HelpAboutDialog.title"),
				SystemContext.getApplicationName()));

		buildUI();
		pack();
	}

	private void buildUI() {
		//  |                |  XXXXX  Application
		//  |                | 
		//  |    picture     |  Version 01.13.01.01
		//  |                |  
		//  |                |  
		//  |                |  Copyright(c) yada yada yada

		JPanel main = new JPanel(new BorderLayout());

		// Application Icon 
//		URL url = getClass().getResource(s_res.getString("HelpAboutDialog.icon"));
//		if (url != null) {
//			JPanel west = new JPanel(new FlowLayout());
//			west.add(new JLabel(new ImageIcon(url)));
//			main.add(west, BorderLayout.WEST);
//		}

		// Contents
		JPanel center = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = Standards.getInsets();
		gbc.insets.top += 10;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = GridBagConstraints.REMAINDER; // one component per row

		// Name (in large font)
		JLabel nameLabel = new JLabel(MessageFormat.format(s_res.getString("HelpAboutDialog.application"),
				SystemContext.getApplicationName()));
		Font font = nameLabel.getFont();
		Font biggerFont = font.deriveFont(Font.ITALIC, font.getSize()*1.5f);
		nameLabel.setFont(biggerFont);
		center.add(nameLabel, gbc);

		// TODO: Version
		String version = getVersionString();
		JLabel versionLabel = new JLabel(MessageFormat.format(s_res.getString("HelpAboutDialog.version"),
				version));
		center.add(versionLabel, gbc);

		// Java Version
		gbc.insets.top = 0;
		version = System.getProperty("java.version");
		JLabel javaVersionLabel = new JLabel(MessageFormat.format(s_res.getString("HelpAboutDialog.javaVersion"),
				version));
		center.add(javaVersionLabel, gbc);

		
		gbc.insets = Standards.getInsets();
		gbc.insets.top += 10;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Copyright, etc
		WrappingDisplayText copyright = new WrappingDisplayText(s_res.getString("HelpAboutDialog.copyright"));
		copyright.setFont(font);
		center.add(copyright, gbc);

		gbc.weighty = 1;
		gbc.insets.top = 0;
		JLabel visitLabel = new JLabel(s_res.getString("HelpAboutDialog.visitNote"));

		center.add(visitLabel, gbc);

		main.add(center, BorderLayout.CENTER);

		getContentPane().add(main, BorderLayout.CENTER);

		setDirty(true);
	}

	public abstract String getVersionString();

	@Override
	public boolean isDataValid() {
		return true;
	}

}
