/*******************************************************************************
 * Copyright (c) 2012 Firestar Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Firestar Software, Inc. - initial API and implementation
 *
 * Author: Gabriel Oancea
 *
 *******************************************************************************/
package org.openhealthtools.mdht.mdmi.engine;

import java.util.*;

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.model.*;

/**
 * An instance of a DTCStructured data type - a structure with some fields.
 * 
 * @author goancea
 */
public class XDataStruct extends XData {
	// private ArrayList<XValue> m_values;
	private LinkedHashMap<String, XValue> m_values = new LinkedHashMap<String, XValue>();

	/**
	 * Construct one from its owner value and its data type.
	 * 
	 * @param owner The owner value.
	 */
	public XDataStruct( XValue owner ) {
		super(owner, (DTCStructured) owner.getDatatype());
		ArrayList<Field> fields = ((DTCStructured) owner.getDatatype()).getFields();
		for( int i = 0; i < fields.size(); i++ ) {
			XValue v = new XValue(this, fields.get(i));
			m_values.put(v.getName(), v);
		}
	}

	/**
	 * Construct one from its owner value and its data type, and initialize its
	 * fields, optionally recursive. Used only from the XValue when constructing
	 * a stand alone value.
	 * 
	 * @param owner The owner value.
	 * @param recursive If true recursively create nested structs.
	 */
	XDataStruct( XValue owner, boolean recursive ) {
		super(owner, (DTCStructured) owner.getDatatype());
		ArrayList<Field> fields = ((DTCStructured) owner.getDatatype()).getFields();
		for( int i = 0; i < fields.size(); i++ ) {
			XValue v = new XValue(this, fields.get(i));
			v.intializeStructs();
			m_values.put(v.getName(), v);
		}
	}

	/**
	 * A copy ctor, but for a different owner. Used when cloning a value.
	 * 
	 * @param owner The owner value.
	 * @param src The source for the cloning.
	 */
	XDataStruct( XValue owner, XDataStruct src ) {
		super(owner, src.m_datatype);
		for( int i = 0; i < src.m_values.size(); i++ ) {
			XValue xv = src.m_values.get(i);
			m_values.put(xv.getName(), xv.clone(true));
		}
	}

	/**
	 * Get the list of field values, one for each field.
	 * 
	 * @return The list of field values, one for each field.
	 */
	public Collection<XValue> getXValues() {
		return m_values.values();
	}

	/**
	 * Get the value of the specified field.
	 * 
	 * @param fieldName The field.
	 * @return The value, if found, null otherwise.
	 */
	public XValue getXValue( Field field ) {
		return getXValue(field.getName());
	}

	/**
	 * Get the value of the specified field.
	 * 
	 * @param fieldName The field name.
	 * @return The value, if found, null otherwise.
	 */
	public XValue getXValue( String fieldName ) {
		if( fieldName == null )
			throw new IllegalArgumentException("Null 'fieldName'");
		return m_values.get(fieldName);
	}

	/**
	 * Set the value for a field, replaces the existing one.
	 * 
	 * @param value The new value.
	 * @return The index of the field being updated.
	 */
	public void setXValue( XValue value ) {
		if( value == null )
			throw new IllegalArgumentException("Null 'value'");
		m_values.put(value.getName(), value);
	}

	/**
	 * Set the value of the given field to the given value.
	 * 
	 * @param fieldName The name of the field to set.
	 * @param value The value to set it to.
	 */
	public void setValue( String fieldName, Object value ) {
		XValue xv = getXValue(fieldName);
		if( xv == null )
			throw new MdmiException("Invalid fieldName: " + fieldName);
		xv.setValue(value);
	}

	public void setXValues( Collection<XValue> xValues ) {
		for( XValue xValue : xValues ) {
			m_values.put(xValue.getName(), xValue);
		}
	}

	/**
	 * Get the value of the given field.
	 * 
	 * @param fieldName The name of the field to get.
	 * @return The value of the field.
	 */
	public Object getValue( String fieldName ) {
		XValue xv = getXValue(fieldName);
		if( xv == null )
			throw new MdmiException("Invalid fieldName: " + fieldName);
		return xv.getValue();
	}
	
	/**
	 * Get the value of the given field.
	 * 
	 * @param fieldName The name of the field to get.
	 * @return The value of the field.
	 */
	public Object getValueByIndex( String fieldName, int index ) {
		XValue xv = getXValue(fieldName);
		if( xv == null )
			throw new MdmiException("Invalid fieldName: " + fieldName);
		return xv.getValue(index);
	}
	
	/**
	 * Clear the specified value, setting it to null in effect.
	 * 
	 * @param name The field name of the value to clear.
	 * @return The index of the value to be removed (cleared).
	 */
	public void clearValue( String name ) {
		if( name == null )
			throw new IllegalArgumentException("Null 'name'");
		XValue v = m_values.get(name);
		if( null == v )
			throw new IllegalArgumentException("Onvalid field name " + name);
		v.clear();
	}

	@Override
	protected String toString( String indent ) {
		StringBuffer sb = new StringBuffer();
		if( null == indent )
			indent = "";
		for( Iterator<XValue> iterator = m_values.values().iterator(); iterator.hasNext(); ) {
			XValue v = iterator.next();
			if( v != null ) {
				sb.append(v.toString(indent));
			}
			else {
				sb.append(indent + "null");
			}
			sb.append("\r\n");
		}
		return sb.toString();
	}

	@Override
	public boolean isEmpty() {
		Collection<XValue> values = m_values.values();
		for( XValue xvalue : values ) {
	      if( !xvalue.isEmpty() )
	      	return false;
      }
		return true;
	}

	public boolean isNullOrEmpty() {
		if( m_datatype.getName().equalsIgnoreCase("Container") )
			return false;
		Collection<XValue> values = m_values.values();
		for( XValue xvalue : values ) {
	      if( !xvalue.isNullOrEmpty() )
	      	return false;
      }
		return true;
	}
} // XDataStruct
