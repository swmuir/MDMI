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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.model.validate.ModelInfo;

/** The specific UI component for editing some MDMI object.
 * Listeners of this class will be notified
 * whenever changes are made such that the <i>isModified()</i> method will return a different
 * value.
 * Users of this class should register as a PropertyChange listener for the DATA_MODIFED property.
 * @author Conway
 *
 */
public abstract class AbstractComponentEditor extends JPanel  {

	/** Property change for data */
	public static final String DATA_MODIFIED = "DataModified";
	
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.editor.Local");
	
	private boolean m_dataModified = false;
	
	/** Set the state of the modification. A property change event will
	 * be sent to all registered listeners.
	 * @param modified
	 */
	public void setModified(boolean modified) {
		boolean oldState = m_dataModified;
		m_dataModified = modified;
		if (oldState != modified) {
			firePropertyChange(DATA_MODIFIED, oldState, modified);
		}
	}
	
	/**
	 * Return true if the current state of the UI controls is different from the data passed in
	 * originally using the setPanelData().
	 * 
	 * @return True if the entity data was changed in the UI.
	 */
	public boolean isModified() {
		return m_dataModified;
	}

	/**
	 * Get a name for the object being edited
	 */
	public String getModelName(Object model) {
		String name = ClassUtil.getItemName(model);
		return name;
	}
	
	/**
	 * Populate the UI controls from some object.
	 * 
	 * @param model The entity to be edited.
	 */
	public abstract void populateUI( Object model );

	/** 
	 * Get the updated model after applying the UI edits.
	 * The implementation will set the fields in the entity from the UI controls and return it.
	 * 
	 * @return The modified entity.
	 */
	public abstract Object getUpdatedModel();

	/** 
	 * Revert the model to the last valid state. If no changes have been applied, this
	 * will be the inital state.
	 */
	public abstract void revertModel();

	/**
	 * Validate the contents of the model.
	 */
	public abstract List<ModelInfo> validateModel();

	/**
	 * Highlight the errors in the view.
	 */
	public abstract void highlightErrors(List<ModelInfo> errors);
	

	/** Highlight the text in the provided field.
	 * 
	 * @param fieldName	The name of the field to highlight
	 * @param textToHighlight	The text that gets highlighted
	 * @param highlightColor	The color. If null, restore all text to default color
	 */
	public abstract void highlightField(String fieldName, String textToHighlight, Color highlightColor);
	
	/** Highlight the field with this value
	 * 
	 * @param value	The field value that gets highlighted
	 * @param highlightColor	The color. If null, restore all text to default color
	 */
	public abstract void highlightField(Object value, Color highlightColor);
	
	/** Get an Icon for the provided path using the resources for the class */
	public static Icon getIcon(Class<?> clazz, String iconPath) {
		URL url = clazz.getResource(iconPath);
		if (url != null) {
			return new ImageIcon(url);
		}
		return null;
	}
	
	/** Create a button with just an Icon, and make the button just big enough
	 * to fit the Icon */
	public static JButton createIconButton(Class<?> clazz, String iconPath) {
		return createIconButton(getIcon(clazz, iconPath));
	}
	
	/** Create a button with just an Icon, and make the button just big enough
	 * to fit the Icon */
	public static JButton createIconButton(Icon icon) {
		JButton button = new JButton();
      // make button just big enough to show icon
      Insets insets = button.getInsets();
      button.setIcon(icon);
		button.setPreferredSize(new Dimension(icon.getIconWidth()+2*insets.top, icon.getIconHeight()+2*insets.top));
		button.setSize(button.getPreferredSize());
		return button;
	}
	

	/** Using reflection on the supplied class, 
	 * return pairs of get/set or is/set methods, where
	 * the argument of the set method and the return type of the get/is method
	 * are the same. */
	public static List<Method[]> getMethodPairs(Class<?> clazz) {
		return ClassUtil.getMethodPairs(clazz);
	}
	
	/** return string showing pairs of get/set or is/set methods */
	public static String getMethodPairsString(Class<?> entityClass) {
		return getMethodPairsString(getMethodPairs(entityClass));
	}

	/** return string showing pairs of get/set or is/set methods */
	public static String getMethodPairsString(List<Method[]> methodPairs) {
		StringBuffer buf = new StringBuffer();
		// look for get/set or get/is pairs
		for (Method[]pair : methodPairs) {
			for (int p=0; p<pair.length; p++) {
				Method method = pair[p];
				Class<?> returnType = method.getReturnType();
				Class<?>[] params = method.getParameterTypes();
				
				if (p > 0) buf.append(" / ");
				buf.append(returnType.getSimpleName()).append(" ").append(method.getName()).append("(");
				for (int i=0; i<params.length; i++) {
					if (i > 0) buf.append(",");
					buf.append(" ").append(params[i].getSimpleName());
				}
				if (params.length > 0) buf.append(" ");
				buf.append(")");
			}
			buf.append("\n");
		}
		return buf.toString();
	}
	
	/** Split a long tooltip into multiple lines, and add html formatting */
	public static String formatToolTip(String toolTip) {
		if ("".equals(toolTip)) {
			// handle blank
			toolTip = null;
			
		} else if (toolTip != null && toolTip.length() > 120) {
			// handle long text
			toolTip = "<html>"
				+ wordwrap(toolTip, 100, "<br>")
				+ "</html>";
		}
		return toolTip;
	}
	
	
	/** Split a long line of text into multiple lines */
	public static String wordwrap(String text, int len, String lineBreakStr){

		if (lineBreakStr == null) {
			lineBreakStr = "\n";
		}

		//Prepare variables
		StringBuilder result = new StringBuilder(text);
		boolean hasSpace = false;
		boolean hasBreak = false;

		//Jump to characters to add line feeds
		int pos = len;
		while (pos < result.length()){
			//Progressivly go backwards until next space
			int bf = pos-len; // stopping point
			hasSpace = false;
			hasBreak = false;

			//Find space just before to avoid cutting words
			for (int ap=pos; ap>bf; ap--){
				//Is it a space?
				char ch = result.charAt(ap);
				if (!hasBreak && String.valueOf(ch).equals(lineBreakStr)){
					// if there's already a line-break, use that one
					hasBreak = true;
					pos = ap;
				}
				// any other white-space, mark as a position for a break
				else if (!hasSpace && Character.isWhitespace(ch)) {
					//Insert line-break
					hasSpace = true;
					pos = ap;
				} 
			}
			//Insert a line feed to the appropriate place
			if (!hasBreak){
				if (hasSpace){
					result.insert(pos+1, lineBreakStr);
				} else{
					result.insert(pos, lineBreakStr);
				}
			}
			//Increment position by length and restart loop
			pos += (len+1);
		}
		//Return the result
		return (result.toString());
	}
}
