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
package org.openhealthtools.mdht.mdmi.editor.map;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.openhealthtools.mdht.mdmi.editor.map.editor.AbstractComponentEditor;

/** Utilities for inspecting, copying, etc. */
public class ClassUtil {
	
	/** Resource for localization */
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.Local");

	/** Get a string indicating a model item that has a null/empty name */
	public static final String s_unNamedItem = s_res.getString("ClassUtil.unNamedItem");
	
	/** Convert a classname something more readable,
	 * by inserting blanks between characters. For example, the class "ThisIsTheClass"
	 * will become "This Is The Class" 
	 * @param name
	 * @return
	 */
	public static String beautifyName(Class<?> clazz) {
		// first look for a ClassUtil.<classpath>.display entry in the resource
		try {
			String displayName = s_res.getString("ClassUtil." + clazz.getName() + ".display");
			return displayName;
		} catch (MissingResourceException ex) {
			// ignore
		}

		// add spacing
		return beautifyName(clazz.getSimpleName());
	}
	
	/** Convert a parameter/classname string into something more readable,
	 * by inserting blanks between characters. For example, the string "thisIsTheString"
	 * will become "This Is The String" 
	 * @param name
	 * @return
	 */
	public static String beautifyName(String name) {
		StringBuilder buf = new StringBuilder();
		
		char prevChar = ' ';
		for (int i=0; i<name.length(); i++) {
			char ch = name.charAt(i);
			
			// capitalize first letter
			if (i == 0) {
				ch = Character.toUpperCase(ch);
			} else {
				// insert a blank before an uppercase letter if it is preceded by a lowercase
				// letter - e.g. "myName" becomes "my Name"
				if (Character.isUpperCase(ch) && 
						(Character.isLowerCase(prevChar) || !Character.isLetter(prevChar))) {
					buf.append(' ');
				}
			}
			
			buf.append(ch);
			
			prevChar = ch;
		}
		
		return buf.toString();
	}
	
	/** Get the name attribute (or equivalent) from a data model object */
	public static String getItemName(Object item) {

		String itemName = null;
		if (item == null) {
			return "";
		} else if (item instanceof String) {
			return (String)item;
		}
		
		String methodName = getNameMethod(item.getClass());		
		// invoke the getNameMethod() method
		if (item != null) {
			try {
				Method method = item.getClass().getMethod(methodName);
				Object getNameResult = method.invoke(item);
				if (getNameResult instanceof String && ((String)getNameResult).length() > 0) {
					itemName = getNameResult.toString();
				}
			} catch (Exception e) {
			}
		}
		if (itemName == null || itemName.length() == 0) {
			itemName = s_unNamedItem;
		}
		return itemName;
	}
	
	/** Create a tooltip for this model object. The tooltip will contain the
	 * object class, followed by the description. The tooltip will be wrapped in
	 * html tags to support multi-line text.
	 * @param object
	 * @return
	 */
	public static String createToolTip(Object object) {
		StringBuilder toolTip = new StringBuilder();
		// start with class name
		toolTip.append(ClassUtil.beautifyName(object.getClass()));
		
		// get description
		try {
			Method getDescriptionMethod = object.getClass().getMethod("getDescription");
			Object description = getDescriptionMethod.invoke(object);
			if (description != null && description.toString().length() > 0) {
				// add description, but add line-breaks it so that it doesn't excede 100 chars per line
				toolTip .append(" -<br>");
				toolTip.append(wordwrap(description.toString(), 100));
				// wrap in html
				toolTip.insert(0, "<html>");
				toolTip.append("</html>");
			}
		} catch (Exception ex) {

		}
		
		return toolTip.toString();
	}

	/** insert html line-breaks ("<br>") into a long line of text.
	 * 
	 * @param text	The original text
	 * @param len	The desired length of each line
	 * @return	New string with line-breaks interspersed in the text
	 */
	public static String wordwrap(String text, int len){
		return AbstractComponentEditor.wordwrap(text, len, "<br>");
	}

	
	/** Return the name of the method used to obtain a object's name.
	 * For example, an MdmiDatatype uses "getTypeName"
	 * @param itemClass
	 * @return
	 */
	private static String getNameMethod(Class<?> itemClass) {
		String methodName = null;
		// look for a ClassUtil.<classpath>.getNameMethod entry in the resource
		while (methodName == null && !Object.class.equals(itemClass)) {;
			try {
				String key = "ClassUtil." + itemClass.getName() + ".getNameMethod";
				methodName = s_res.getString(key);
				
			} catch (MissingResourceException ex) {
				// no methodName found for the item's class - try super class
				itemClass = itemClass.getSuperclass();
			}
		}
		
		if (methodName == null) {
			// no resource defined - use getName()
			methodName = "getName";
		}
		return methodName;
	}
	
	
	/** Make a shallow copy of this object. Only attributes with get/set pairs
	 * will be copied.
	 * @param oldObject
	 * @return
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 */
	public static Object clone(Object oldObject) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object newObject = null;
		Class <?> clazz = oldObject.getClass();
		
		// first - try to copy directly
		try {
			Constructor<?> ctor = clazz.getConstructor(clazz);
			newObject = ctor.newInstance(oldObject);
			return newObject;
		} catch (Exception ex) {
			// didn't work - keep going
		}
		
		newObject = clazz.newInstance();
		
		// copy data
		List<Method[]> methodPairs = getMethodPairs(clazz);
		for (Method[] pair : methodPairs) {
			Method getMethod = pair[0];
			Method setMethod = pair[1];
			
			Object value = getMethod.invoke(oldObject);
			setMethod.invoke(newObject, value);
		}
		return newObject;
	}
	
	/** Make a shallow copy of all relevant data in the source object into the
	 * target object. Only get/set methods that appear in both objects will
	 * be invoked.
	 * @param source
	 * @param target
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public static void copyData(Object source, Object target) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Class <?> sourceClass = source.getClass();
		Class <?> targetClass = target.getClass();

		List<Method[]> sourceMethods = getMethodPairs(sourceClass);
		List<Method[]> targetMethods = getMethodPairs(targetClass);

		for (Method[] sourcePair : sourceMethods) {
			Method sourceGetMethod = sourcePair[0];
			Class<?> srcReturnType = sourceGetMethod.getReturnType();
			Method targetSetMethod = null;
			// find a setMethod in the target
			for (Method[] targetPair : targetMethods) {
				Method targetGetMethod = targetPair[0];
				// same getMethod name and return value
				if (targetGetMethod.getName().equals(sourceGetMethod.getName())
						&& targetGetMethod.getReturnType().equals(srcReturnType)) {
					targetSetMethod = targetPair[1];
					break;
				}
			}
			
			if (targetSetMethod != null) {
				Object value = sourceGetMethod.invoke(source);
				targetSetMethod.invoke(target, value);
			}
			
		}
	}

	/** Using reflection on the supplied class, 
	 * return pairs of get/set or is/set methods ([0] is the get method,
	 * [1] is the set method), where the argument of the set method and
	 * the return type of the get/is method are the same. */
	public static List<Method[]> getMethodPairs(Class<?> clazz) {
		return getMethodPairs(clazz, null);
	}
	
	/** Using reflection on the supplied class, 
	 * return pairs of get/set or is/set methods ([0] is the get method,
	 * [1] is the set method), where the argument of the set method and
	 * the return type of the get/is method are the same. 
	 * @param	clazz	The class
	 * @param	expectedReturnType if non-null, only return method pairs with this return type
	 * */
	public static List<Method[]> getMethodPairs(Class<?> clazz, Class<?> expectedReturnType) {
		List<Method[]> methodList = new ArrayList<Method[]>();
		
		// look for get/set or get/is pairs
		for (Method method : clazz.getMethods()) {
			String methodName = method.getName();
			if (methodName.startsWith("get") || methodName.startsWith("is")) {
				// look for matching "set" method
				String setMethodName = methodName.startsWith("get") ?
						methodName.replaceFirst("get", "set") : methodName.replaceFirst("is", "set");
				Class<?> returnType = method.getReturnType();
				if (expectedReturnType == null || returnType.isAssignableFrom(expectedReturnType)) {
					try {
						Method setMethod = clazz.getMethod(setMethodName, returnType);
						Method[] pair = new Method[] {method, setMethod};

						methodList.add(pair);

					} catch (Exception e) {
					}
				}
			}
		}
		
		return methodList;
	}
	
	
}
