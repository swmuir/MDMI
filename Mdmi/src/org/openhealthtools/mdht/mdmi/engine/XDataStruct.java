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

import org.openhealthtools.mdht.mdmi.model.*;

/**
 * An instance of a DTCStructured data type - a structure with some fields.
 * 
 * @author goancea
 */
public class XDataStruct extends XData {
   private ArrayList<XValue> m_values;

   /**
    * Construct one from its owner value and its data type.
    * 
    * @param owner The owner value.
    * @param datatype The data type, must be not null.
    */
   public XDataStruct( XValue owner, DTCStructured datatype ) {
      super( owner, datatype );
      ArrayList<Field> fields = datatype.getFields();
      m_values = new ArrayList<XValue>( fields.size() );
      for( int i = 0; i < fields.size(); i++ ) {
         XValue v = new XValue( this, fields.get(i) );
         m_values.add( v );
      }
   }
   
   /**
    * Construct one from its owner value and its data type, and initialize its fields, optionally recursive.
    * Used only from the XValue when constructing a stand alone value. 
    * 
    * @param owner The owner value.
    * @param datatype The data type, must be not null.
    */
   XDataStruct( XValue owner, DTCStructured datatype, boolean recursive ) {
      super( owner, datatype );
      ArrayList<Field> fields = datatype.getFields();
      m_values = new ArrayList<XValue>( fields.size() );
      for( int i = 0; i < fields.size(); i++ ) {
         XValue v = new XValue( this, fields.get(i) );
         v.intializeStructs();
         m_values.add( v );
      }
   }

   /**
    * A copy ctor, but for a different owner. Used when cloning a value.
    * 
    * @param owner The owner value.
    * @param src The source for the cloning.
    */
   XDataStruct( XValue owner, XDataStruct src ) {
      super( owner, (DTCStructured)src.m_datatype );
      m_values = new ArrayList<XValue>( src.m_values.size() );
      for( int i = 0; i < src.m_values.size(); i++ ) {
         XValue xv = src.m_values.get( i );
         m_values.add( xv.clone(true) );
      }
   }

   /**
    * Get the list of field values, one for each field.
    * 
    * @return The list of field values, one for each field.
    */
   public ArrayList<XValue> getValues() {
      return m_values;
   }

   /**
    * Get the value of the specified field.
    * 
    * @param fieldName The field.
    * @return The value, if found, null otherwise.
    */
   public XValue getValue( Field field ) {
      return getValue(field.getName());
   }
   
   /**
    * Get the value of the specified field.
    * 
    * @param fieldName The field name.
    * @return The value, if found, null otherwise.
    */
   public XValue getValue( String fieldName ) {
      if( fieldName == null )
         throw new IllegalArgumentException( "Null 'fieldName'" );
      for( int i = 0; i < m_values.size(); i++ ) {
         XValue v = m_values.get( i );
         if( fieldName.equals(v.getName()) )
            return v;
      }
      return null;
   }

   /**
    * Set the value for a field, replaces the existing one.
    * 
    * @param value The new value.
    * @return The index of the field being updated.
    */
   public int setValue( XValue value ) {
      if( value == null )
         throw new IllegalArgumentException( "Null 'value'" );
      for( int i = 0; i < m_values.size(); i++ ) {
         XValue v = m_values.get( i );
         if( value.getName().equals(v.getName()) ) {
            m_values.remove( i );
            m_values.add( i, value );
            return i;
         }
      }
      return -1;
   }

   /**
    * Clear the specified value, setting it to null in effect.
    * 
    * @param index The index of the value to be cleared.
    */
   public void clearValue( int index ) {
      XValue v = m_values.get( index );
      v.clear();
   }

   /**
    * Clear the specified value, setting it to null in effect.
    * 
    * @param name The field name of the value to clear.
    * @return The index of the value to be removed (cleared).
    */
   public int clearValue( String name ) {
      if( name == null )
         throw new IllegalArgumentException( "Null 'name'" );
      int i = indexOf(name);
      if( 0 <= i ) {
         XValue v = m_values.get( i );
         v.clear();
      }
      return i;
   }

   /**
    * Get the index of the specified value, by field.
    * 
    * @param field The field to look for.
    * @return The index of the value for this field (-1 if not found).
    */
   public int indexOf( Field field ) {
      if( field == null )
         throw new IllegalArgumentException( "Null 'field'" );
      return indexOf(field.getName());
   }

   /**
    * Get the index of the specified value, by name.
    * 
    * @param name The field name to look for.
    * @return The index of the value for this field (-1 if not found).
    */
   public int indexOf( String name ) {
      if( name == null )
         throw new IllegalArgumentException( "Null 'name'" );
      for( int i = 0; i < m_values.size(); i++ ) {
         XValue v = m_values.get( i );
         if( v.getName().equals(name) )
            return i;
      }
      return -1;
   }
   
   @Override
   protected String toString( String indent ) {
      StringBuffer sb = new StringBuffer();
      for( int i = 0; i < m_values.size(); i++ ) {
         XValue v = (XValue)m_values.get( i );
         sb.append( v.toString(indent + "  ") );
         sb.append("\r\n");
      }
      return sb.toString();
   }
} // XDataStruct
