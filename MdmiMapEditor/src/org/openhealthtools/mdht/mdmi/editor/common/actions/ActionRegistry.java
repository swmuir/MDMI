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
 *
 */
package org.openhealthtools.mdht.mdmi.editor.common.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.Action;

/** The <CODE> ActionRegistry</CODE> is a central repository for <CODE>Action</CODE> instances within
 * an application.  There is only one instance of an application created in
 * the VM by the registry to conserve resources. The ActionRegistry is static an can be queried from anywhere
 * and is used as a central place to enable and disable <CODE>Action</CODE> instances.
 * @author TPerkins
 */

public class ActionRegistry {

    protected static HashMap<Class<?>, Action> s_actions     = new HashMap<Class<?>, Action>();
    protected static HashMap<String, String>   s_bindings    = new HashMap<String, String>();
    protected static HashMap<String, String>   s_classtoPath = new HashMap<String, String>();

    /** Clear any registered actions */
    public static void clearAllActions() {
       s_actions = new HashMap<Class<?>, Action>();
       s_bindings = new HashMap<String, String>();
       s_classtoPath = new HashMap<String, String>();
    }
    
    /** Binds a path/Action pair
     * @param path The String path defined in the ADF
     * @param actionClass The action class to bind
     */
    public static void bindAction(String path, String actionClass) {
        s_bindings.put(path, actionClass);
        s_classtoPath.put(actionClass, path);

    }

    /** Unbinds a given path/Action pair
     * @param path String path defined in application descriptor file
     * @param actionClass Action to be unbound
     */
    public static void unbindAction(String path, String actionClass){
        s_bindings.remove(path);
        s_classtoPath.remove(actionClass);
    }

    /** Returns the binding of path
     * @param path The path in the application descriptor file that is bound to the class
     * @return The AbstractAction subclass name
     */
    public static String getBinding(String path){
        return s_bindings.get(path);
    }

    /** Given an Action class it will return the String path that is bound to it
     * @param cls The Action class for the binding
     * @return The String path that is bound to the Action
     */
    public static String getPathFromClass(Class<?> cls){
        return s_classtoPath.get(cls.getName());
    }

    /** Returns an Action based on the action name.
     * @param actionName The path that is bound to the Action
     * @throws InstantiationException Thrown if the Action class specified is
     * not loaded by the virtual machine.
     * @return Returns the bound Action subclass
     */
    public static Action getActionInstance(String actionName) throws InstantiationException {
        try {
            String classid = getBinding(actionName);
            if (classid == null) {
            	throw new InstantiationException("No class for " + actionName);
            }
            return getActionInstance(Class.forName(classid));
            
        } catch (ClassNotFoundException e){
            throw new InstantiationException(e.getMessage() + " for " + actionName);
            
        } catch (Exception e) {
           throw new InstantiationException(e.getMessage() + " for " + actionName);
        }
        
    }


    /** Returns an Action instance
     * @param actionClass The Action Class
     * @throws InstantiationException Thrown if the class is not loaded by the VM
     * @return Returns an instance of Action
     */
    public static Action getActionInstance(Class<?> actionClass ) throws InstantiationException {
        try {
            Action action = null;
            if (s_actions.containsKey(actionClass)){
                return s_actions.get(actionClass);
                
            } else {
                action = (Action)actionClass.newInstance();
                s_actions.put(actionClass, action);
            }
            return action;
        } catch (IllegalAccessException e){
            throw new InstantiationException(e.getMessage());
        } catch (Exception e) {
           throw new InstantiationException("Cannot create action for " + actionClass.getName());
        }
    }

    /** Disables all of the Action instances in the ActionRegistry
     */
    public static void disableAll(){
        List<Action> actionlist = new ArrayList<Action>(s_actions.values());
        Action action = null;
        for (int i=0; i < actionlist.size(); i++){
            action = actionlist.get(i);
            action.setEnabled(false);
        }
    }

    /** Enables an Action instance
     * @param path The path to the Action instance
     * @param enabled If true the underlying Action is enabled
     * @throws InstantiationException Thrown if the Actpion class
     * that is bound to the path is not present in the VM
     */
    public static void setEnabled(String path, boolean enabled) throws InstantiationException{
        getActionInstance(path).setEnabled(enabled);
    }

    /** Enables an Action instance
     * @param classid The Action class to be enable/disabled
     * @param enabled If true the underlying Action is enabled
     * @throws InstantiationException Thrown if the Actpion class that is bound
     * to the path is not present in the VM
     */
    public static void setEnabled(Class<?> classid, boolean enabled) throws InstantiationException{
        getActionInstance(classid).setEnabled(enabled);
    }


}
