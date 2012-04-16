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
 * Created on Nov 3, 2005
 */
package org.openhealthtools.mdht.mdmi.editor.common.actions;


import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;


/**
 *
 * This is a base class for any application menus/toolbar actions. These actions
 * are created via the application initialization.
 */
public abstract class AbstractMenuAction extends AbstractAction {

	/**
	 * generated id
	 */
	private static final long serialVersionUID = -6190621780903664431L;

	private ResourceBundle m_bundle = null;

	/** Creates a new instance of AbstractSystemShellAction */
	public AbstractMenuAction() {
		init();
	}

	private void init(){
		// must be initialized every time because SystemContext can change
		//   for sub-menus
		m_bundle = SystemContext.getApplicationResource();

		String actionKey = ActionRegistry.getPathFromClass(this.getClass());
		if (actionKey != null){
			this.putValue(Action.NAME, getLocalText(actionKey));

			char mnemonic = getLocalMnemonic(actionKey);
			if (mnemonic != 0) {
				putValue(Action.MNEMONIC_KEY, new Integer(mnemonic));
			}

			ImageIcon icon =  getLocalIcon(actionKey);
			if (icon != null){
				this.putValue(Action.SMALL_ICON, icon);
			}

			KeyStroke accel = getLocalAccelerator(actionKey);
			if (accel != null) {
				this.putValue(Action.ACCELERATOR_KEY, accel);
			}
		}
	}

	/** Get the locale-specific value of the <i>text</i> property */
	public String getLocalText(String actionKey){
		try {
			String text = m_bundle.getString(actionKey+".text");
			return text;
		} catch (MissingResourceException e){
			return null;
		}
	}

	/** Get the service operations associated with this action.
	 * This will be the value of the <i>operation</i> property */
	public String[] getServiceOperations(String actionKey){
		try {
			List <String> operations = new ArrayList<String>();
			String text = m_bundle.getString(actionKey+".operation");
			StringTokenizer tok = new StringTokenizer(text, ",");
			while(tok.hasMoreTokens()) {
				operations.add(tok.nextToken().trim());
			}
			return operations.toArray(new String[operations.size()]);
		} catch (MissingResourceException e){
			return null;
		}
	}

	/** Get the value of the <i>icon</i> property */
	public ImageIcon getLocalIcon(String actionKey){
		try {
			// this will throw an exception if there is no icon defined
			String urlName = m_bundle.getString(actionKey+".icon");

			URL url = this.getClass().getResource(urlName);
			if (url == null) {
				MissingResourceException ex = new MissingResourceException("Unable to locate image file",
						this.getClass().getName(), urlName);
				logActionError(ex);
				throw ex;
			}
			return new ImageIcon(url);
		} catch (MissingResourceException e){
			return null;
		}
	}

	/** Get the value of the <i>mnemonic</i> property */
	public char getLocalMnemonic(String actionKey){
		try {
			String text = m_bundle.getString(actionKey+".mnemonic");
			if (text == null || text.length() == 0) {
				return 0;
			}
			return text.charAt(0);
		} catch (MissingResourceException e){
			return 0;
		}
	}

	/** Get the value of the <i>accelerator</i> property */
	public KeyStroke getLocalAccelerator(String actionKey){
		try {
			String text = m_bundle.getString(actionKey+".accelerator");
			if (text == null || text.length() == 0) {
				return null;
			}
			return KeyStroke.getKeyStroke(text);
		} catch (MissingResourceException e){
			return null;
		}
	}
}
