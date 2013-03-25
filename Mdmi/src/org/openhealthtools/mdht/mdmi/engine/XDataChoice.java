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

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.model.*;

/**
 * An instance of a DTCChoice data type - a choice with at most one chosen field value.
 * 
 * @author goancea
 */
public class XDataChoice extends XData {
   private XValue m_value;
   
   /**
    * Construct one from its owner and data type.
    * 
    * @param owner The owner value.
    * @param datatype The data type.
    */
   public XDataChoice( XValue owner, DTCChoice datatype ) {
      super( owner, datatype );
   }

   /**
    * Internal constructor use when cloning a value.
    * 
    * @param owner The new owner value.
    * @param src The source for the field value.
    */
   XDataChoice( XValue owner, XDataChoice src ) {
      super( owner, (DTCChoice)src.m_datatype );
      if( src.m_value != null )
         m_value = src.m_value.clone( true );
   }

   /**
    * Get the chosen field value.
    * 
    * @return The chosen field value.
    */
   public XValue getValue() {
      return m_value;
   }
   
   /**
    * Get the chosen field name.
    * 
    * @return The chosen field name.
    */
   public String getChoiceName() {
      return m_value.getName();
   }
   
   /**
    * Return true if the data type has the specified field (it can be a choice).
    * 
    * @param fieldName the field name to look for.
    * @return True if the choice data type has this field.
    */
   public boolean hasChoice( String fieldName ) {
      if( fieldName == null )
         throw new IllegalArgumentException( "Null field!" );
      DTCChoice cdt = (DTCChoice)m_datatype;
      return null != cdt.getField( fieldName );
   }
   
   /**
    * Set the chosen value.
    * 
    * @param fieldName The field name, must be one of the data type fields.
    * @return The set value reference.
    */
   public XValue setValue( String fieldName ) {
      if( fieldName == null )
         throw new IllegalArgumentException( "Null field!" );
      DTCChoice cdt = (DTCChoice)m_datatype;
      Field field = cdt.getField( fieldName );
      if( field == null )
         throw new MdmiException( "Invalid field {0} in choice {1}", fieldName, m_datatype.getName() );
      m_value = new XValue( this, field );
      return m_value;
   }

   /**
    * Set the value to the given value. The field name must be one of the data type fields.
    *  
    * @param value The new value.
    */
   public void setValue( XValue value ) {
      DTCChoice cdt = (DTCChoice)m_datatype;
      Field field = cdt.getField( value.getName() );
      if( field == null )
         throw new MdmiException( "Invalid field {0} in choice {1}", value.getName(), m_datatype.getName() );
      m_value = value;
   }

   /**
    * Clear the chosen value.
    */
   public void clearValue() {
      m_value = null;
   }

   @Override
   protected String toString( String indent ) {
      XValue v = (XValue)m_value;
      StringBuffer sb = new StringBuffer();
      sb.append( v.toString(indent + "  ") );
      sb.append("\r\n");
      return sb.toString();
   }
} // XDataChoice
