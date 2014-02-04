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
 *     Gabriel Oancea
 *
 *******************************************************************************/
package org.openhealthtools.mdht.mdmi.engine;

import java.util.*;

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.model.*;

/**
 * A semantic value (a field). It has a name, datatype and the actual value or
 * values. It may be owned by an element value, or by one of the structured
 * semantic type (XDataStruct or XDataChoice).
 * 
 * @author goancea
 */
public final class XValue implements IValue {

	private String m_name;
	private MdmiDatatype m_datatype;
	private ArrayList<Object> m_values;

	/**
	 * Construct one as the main value of an element value,
	 * 
	 * @param owner
	 *           The element owner, may not be null.
	 */
	public XValue( IElementValue owner ) {
		if( owner == null )
			throw new IllegalArgumentException("Null argument!");
		initialize(SemanticElement.VALUE_NAME, owner.getSemanticElement().getDatatype());
	}

	/**
	 * Construct one as a field in a structure or choice.
	 * 
	 * @param owner
	 *           The owner structure or choice.
	 * @param field
	 *           The field this is an instance of.
	 */
	public XValue( XData owner, Field field ) {
		if( owner == null || field == null )
			throw new IllegalArgumentException("Null argument!");
		initialize(field.getName(), field.getDatatype());
	}

	/**
	 * Construct a standalone one, no owner.
	 * 
	 * @param name
	 *           The value name.
	 * @param datatype
	 *           The value data type.
	 */
	public XValue( String name, MdmiDatatype datatype ) {
		if( name == null || datatype == null )
			throw new IllegalArgumentException("Null argument!");
		initialize(name, datatype);
		intializeStructs();
	}

	public XValue( Field field, Object value ) {
		if( field == null )
			throw new IllegalArgumentException("Null argument!");
		initialize(field.getName(), field.getDatatype());
		setValue(value);
	}

	/**
	 * Initialize the value with a name and a type.
	 * 
	 * @param name
	 *           The value name.
	 * @param datatype
	 *           The value data type.
	 */
	private void initialize( String name, MdmiDatatype datatype ) {
		m_name = name;
		m_datatype = datatype;
		m_values = new ArrayList<Object>();
	}

	/**
	 * For stand alone values we add the fields, if the type is a structure or
	 * choice.
	 */
	void intializeStructs() {
		if( m_datatype.isStruct() ) {
			XDataStruct t = new XDataStruct(this, true);
			m_values.add(t);
		}
		else if( m_datatype.isChoice() ) {
			XDataChoice t = new XDataChoice(this);
			m_values.add(t);
		}
	}

	/**
	 * Private copy ctor.
	 * 
	 * @param src
	 *           The source value.
	 * @param deep
	 *           If deep is true, it will clone all child values, otherwise just
	 *           the references.
	 */
	private XValue( XValue src, boolean deep ) {
		m_name = src.m_name;
		m_datatype = src.m_datatype;
		m_values = new ArrayList<Object>();

		if( src.m_values.size() <= 0 )
			return;

		if( !deep || getDatatype().isSimple() ) {
			for( int i = 0; i < src.m_values.size(); i++ ) {
				m_values.add(src.m_values.get(i));
			}
		}
		else {
			for( int i = 0; i < src.m_values.size(); i++ ) {
				if( getDatatype().isChoice() ) {
					XDataChoice x = (XDataChoice) src.m_values.get(i);
					XDataChoice c = new XDataChoice(this, x);
					m_values.add(c);
				}
				else {
					XDataStruct x = (XDataStruct) src.m_values.get(i);
					XDataStruct c = new XDataStruct(this, x);
					m_values.add(c);
				}
			}
		}
	}

	/**
	 * Clone this value, optionally its child values as well (if deep is true).
	 * 
	 * @param deep
	 *           If deep is true, it will clone all child values, otherwise just
	 *           the references.
	 * @return The cloned value.
	 */
	public XValue clone( boolean deep ) {
		return new XValue(this, deep);
	}

	@Override
	public void cloneValues( IValue src ) {
		m_values.clear();
		ArrayList<Object> srcValues = src.getValues();
		for( int i = 0; i < srcValues.size(); i++ ) {
			m_values.add(srcValues.get(i));
		}
	}

	@Override
	public MdmiDatatype getDatatype() {
		return m_datatype;
	}

	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public void setName( String name ) {
		m_name = name;
	}

	@Override
	public int size() {
		return m_values.size();
	}

	@Override
	public Object getValue() {
		return m_values.size() <= 0 ? null : getValue(0);
	}

	@Override
	public Object getValue( int index ) {
		return m_values.get(index);
	}

	@Override
	public ArrayList<Object> getValues() {
		return m_values;
	}

	@Override
	public int getIndexOf( Object value ) {
		for( int i = 0; i < m_values.size(); i++ ) {
			Object o = m_values.get(i);
			if( o == value )
				return i;
		}
		return -1;
	}

	@Override
	public void setValue( Object value ) {
		setValue(value, 0);
	}

	@Override
	public void setValue( Object value, int index ) {
		if( index < 0 || index >= m_values.size() )
			m_values.add(value);
		else
			m_values.set(index, value);
	}

	@Override
	public void addValue( String field, Object value ) {

		for( Object object : m_values ) {
			if( object instanceof XDataStruct ) {
				XDataStruct xds = (XDataStruct) object;
				xds.setValue(field, value);
			}
		}

	}

	@Override
	public void addValue( Object value ) {
		addValue(value, -1);
	}

	@Override
	public void addValue( Object value, int index ) {
		if( index < 0 || index >= m_values.size() )
			m_values.add(value);
		else
			m_values.add(index, value);
	}

	@Override
	public void removeValue( Object value ) {
		if( value == null )
			return;
		int index = m_values.indexOf(value);
		if( index >= 0 )
			m_values.remove(index);
	}

	@Override
	public void clear() {
		m_values.clear();
	}

	@Override
	public void removeValue( int index ) {
		m_values.remove(index);
	}

	@Override
	public String toString() {
		return toString("");
	}

	/**
	 * Get the string representation of this value.
	 * 
	 * @param indent
	 *           The indentation to use.
	 * @return The string representation of this value.
	 */
	String toString( String indent ) {
		StringBuffer sb = new StringBuffer();
		sb.append(indent + getName() + ": " + getDatatype().getTypeName() + " = ");
		if( m_values.size() == 0 ) {
			sb.append("null");
		}
		else {
			if( getDatatype().isSimple() || getDatatype().isExternal() ) {
				if( m_values.size() == 1 ) {
					sb.append(m_values.get(0));
				}
				else {
					sb.append("{ ");
					for( int i = 0; i < m_values.size(); i++ ) {
						if( i > 0 )
							sb.append(", ");
						sb.append(m_values.get(i));
					}
					sb.append("}");
				}
			}
			else {
				int noOfFields = 0;
				for( int i = 0; i < m_values.size(); i++ ) {
					if( getDatatype().isChoice() ) {
						XDataChoice x = (XDataChoice) m_values.get(i);
						noOfFields += x.getDatatype().getFields().size();
					}
					else {
						XDataStruct x = (XDataStruct) m_values.get(i);
						noOfFields += x.getDatatype().getFields().size();
					}
				}
				if( noOfFields <= 0 ) {
					sb.append("{}");
				}
				else {
					sb.append("{\r\n");
					for( int i = 0; i < m_values.size(); i++ ) {
						if( getDatatype().isChoice() ) {
							XDataChoice x = (XDataChoice) m_values.get(i);
							sb.append(x.toString(indent + "  "));
						}
						else {
							XDataStruct x = (XDataStruct) m_values.get(i);
							sb.append(x.toString(indent + "  "));
						}
					}
					sb.append(indent + "}");
				}
			}
		}
		return sb.toString();
	}
} // XValue
