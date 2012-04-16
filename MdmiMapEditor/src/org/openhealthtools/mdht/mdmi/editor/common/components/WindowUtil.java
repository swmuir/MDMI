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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EventListener;

import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

public class WindowUtil {

	/** Listener classes to be cleaned up */
	private static final Class<?> [] s_listenerClasses = new Class<?>[] {
		ActionListener.class,
		CellEditorListener.class,
		ChangeListener.class,
		ComponentListener.class,
		ContainerListener.class,
		FocusListener.class,
		KeyListener.class,           
		ListSelectionListener.class,  
		MouseListener.class,
		MouseMotionListener.class,
		PropertyChangeListener.class,
		TreeSelectionListener.class,
		// Don't remove window listeners, since there may be other objects waiting for window to close
		//    WindowListener.class
	};


	/** Renderer classes to be cleaned up */
	private static final String [] s_Renders = new String[] {
		"setRenderer",
		"setCellRenderer",
		"setDefaultRenderer",
		"setHeaderRenderer",
	};


	/** recursively remove all child components from this component, and
	 * all listeners on those components */
	public static void removeAllComponents(Component component) throws Exception {

		// remove listeners before removing children in case the 
		// removal of the child produces an event
		removeListeners(component);   

		if (component instanceof Container) {
			Container container = (Container)component;
			Component [] children = container.getComponents();
			for (int i=0; i<children.length; i++) {
				removeAllComponents(children[i]);
				container.remove(children[i]);
			}
		}     
	}


	/** remove all listeners (and renderers) on this component */
	public static void removeListeners(Component component) throws Exception {

		for (Class<?> listenerClass : s_listenerClasses) {            
			removeListeners(component, listenerClass);
		}
		// DocumentListeners need to be handled differently
		if (component instanceof JTextComponent) {
			removeDocumentListeners((JTextComponent)component);
		}

		// Remove renderer if there is one
		for (String renderer : s_Renders) {
			// JTables are handled specially
			if (component instanceof JTable) {
				JTable table = (JTable)component;
				for (int c=0; c< table.getColumnCount(); c++) {
					removeRenderer(table.getColumnModel().getColumn(c), renderer);
				}
			}
			removeRenderer(component, renderer);
		}
	}

	/** Remove all document listeners from this text component
	 * @param component
	 */
	public static void removeDocumentListeners(JTextComponent component) {
		Document document = component.getDocument();
		if (document instanceof AbstractDocument) {
			DocumentListener[] dls = ((AbstractDocument)document).getDocumentListeners();
			for (int i=0; i<dls.length; i++) {
				((AbstractDocument)document).removeDocumentListener(dls[i]);
			}
			UndoableEditListener[] uels = ((AbstractDocument)document).getUndoableEditListeners();
			for (int i=0; i<uels.length; i++) {
				((AbstractDocument)document).removeUndoableEditListener(uels[i]);
			}
		}
	}

	/** Remove listeners of this type from the component */
	@SuppressWarnings("unchecked")
	public static void removeListeners(Component component, Class listenerClass) throws Exception {
		// ex: java.awt.event.ActionListener
		String listenerName = listenerClass.getName();

		// ex: removeActionListener
		int dotIdx = listenerName.lastIndexOf(".");
		String methodName = "remove" + listenerName.substring(dotIdx+1);

		String errMessage = "Error invoking " + methodName;
		// get all listeners of this type, and call the appropriate remove method
		try
		{
			// get all listenes of this type (for example all ActionListeners)
			EventListener[] listeners = component.getListeners(listenerClass);
			if (listeners == null || listeners.length == 0) {
				return;
			}

			Method removeMethod = component.getClass().getMethod(
					methodName, new Class[] {listenerClass});

			for (EventListener listener: listeners) {
				// ex: call component.removeActionListener(actionListener)
				// Debugging
				//  System.out.println((component.getName() != null ? component.getName() : component.toString())
				//             + "." + methodName + "( " + listener + " ");
				removeMethod.invoke(component, new Object[] {listener});
			}
		}
		catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(errMessage, e);        
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(errMessage, e);      
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException(errMessage, e);        
		}
	}


	/** Remove renderers of this type from the component */
	@SuppressWarnings("unchecked")
	public static void removeRenderer(Object object, String setRendererMethodName)
	throws Exception {

		String errMessage = "Error invoking " + setRendererMethodName;
		// find a method with this name, and one argument
		try
		{
			Method[] allMethods = object.getClass().getMethods();

			for (Method method: allMethods) {
				if (method.getName().equals(setRendererMethodName)) {
					Class[] args = method.getParameterTypes();
					// ex: call component.setXXXRenderer(null)
					if (args.length == 1) {
						method.invoke(object, new Object[] {null});
					}
					break;
				}
			}
		}  catch (IllegalAccessException e) {
			throw new IllegalArgumentException(errMessage, e);      
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException(errMessage, e);        
		}
	}
	


	/** 
	 * Utility to find if any of the owned windows of the given
	 * window currently has a modal dialog showing
	 * @param window check children of this frame for modal dialogs
	 * @return true if there is a modal dialog showing
	 */
	public static boolean isModalDialogShowing(Window window) {
		boolean showing = false;

		Window[] windows = window.getOwnedWindows();
		for (Window w : windows) {
			if (w.isVisible() && w instanceof Dialog && ((Dialog)w).isModal()) {
				showing = true;
				break;
			} else {
				showing = isModalDialogShowing(w);
			}

			if (showing) {
				break;
			}
		}

		return showing;
	}

}
