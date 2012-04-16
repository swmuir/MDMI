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

import org.openhealthtools.mdht.mdmi.IExpressionInterpreter;
import org.openhealthtools.mdht.mdmi.engine.Conversion.ConversionInfo;
import org.openhealthtools.mdht.mdmi.model.*;

/**
 * Implementation class for rule execution.
 */
class ConversionImpl {
   private ConversionImpl() {
   }
   
   static void convert( XElementValue src, ConversionInfo ci, XElementValue trg ) {
      ToBusinessElement toBE = Conversion.getToBE( src.getSemanticElement(), ci.srcBER );
      ToMessageElement  toSE = Conversion.getToSE( trg.getSemanticElement(), ci.trgBER );
      ConversionImpl c = new ConversionImpl();
      System.out.println( "    converting: " + src.toStringShort() + " to " + trg.getName() );
      XValue v = new XValue( "v", toBE.getBusinessElement().getReferenceDatatype() );
      if( !c.hasTrgRule(toSE) ) {
         c.cloneValue( trg.getValue(), v );
         System.out.println( "    target cloned: " + v.toString() );
      }
      c.execSrcRule( src, ci, trg, toBE, v );
      System.out.println( "    source transformed: " + v.toString() );
      c.execTrgRule( v, ci, trg, toSE );
      System.out.println( "    target now is: " + trg.toString() );
   }
   
   boolean hasSrcRule( ToBusinessElement toBE ) {
      return toBE != null && toBE.getRule() != null && 0 < toBE.getRule().length(); 
   }

   boolean hasTrgRule( ToMessageElement toSE ) {
      return toSE != null && toSE.getRule() != null && 0 < toSE.getRule().length(); 
   }
   
   void execSrcRule( XElementValue src, ConversionInfo ci, XElementValue trg, ToBusinessElement toBE, XValue v ) {
      if( !hasSrcRule(toBE) ) {
         cloneValue( src.getValue(), v );
      }
      else {
         IExpressionInterpreter m_adapter = new NrlAdapter( src.getOwner(), src, toBE.getName(), v );
         m_adapter.evalAction( src, toBE.getRule() );
      }
   }

   void execTrgRule( XValue v, ConversionInfo ci, XElementValue trg, ToMessageElement toSE ) {
      if( !hasTrgRule(toSE) ) {
         cloneValue( v, trg.getValue() );
      }
      else {
         IExpressionInterpreter m_adapter = new NrlAdapter( trg.getOwner(), trg, toSE.getName(), v );
         m_adapter.evalAction( trg, toSE.getRule() );
      }
   }

   private void cloneStruct( XDataStruct src, XDataStruct trg ) {
      if( src == null || trg == null )
         throw new IllegalArgumentException( "Null argument!" );
      ArrayList<XValue> values = trg.getValues();
      for( int i = 0; i < values.size(); i++ ) {
         XValue t = values.get( i );
         XValue s = src.getValue( t.getName() );
         if( s != null )
            cloneValue( s, t );
      }
   }

   private void cloneChoice( XDataChoice src, XDataChoice trg ) {
      XValue s = src.getValue();
      String fieldName = s.getName();
      XValue t = trg.setValue( fieldName );
      cloneValue( s, t );
   }

   private void cloneValue( XValue src, XValue trg ) {
      ArrayList<Object> values = src.getValues();
      if( values.size() <= 0 )
         return;
      if( src.getDatatype().isStruct() ) {
         for( int i = 0; i < values.size(); i++ ) {
            XDataStruct srcXD = (XDataStruct)values.get( i );
            XDataStruct trgXD = new XDataStruct( trg, (DTCStructured)trg.getDatatype() );
            trg.setValue( trgXD );
            cloneStruct( srcXD, trgXD );
         }
      }
      else if( src.getDatatype().isChoice() ) {
         for( int i = 0; i < values.size(); i++ ) {
            XDataChoice srcXD = (XDataChoice)values.get( i );
            XDataChoice trgXD = new XDataChoice( trg, (DTCChoice)trg.getDatatype() );
            trg.setValue( trgXD );
            cloneChoice( srcXD, trgXD );
         }
      }
      else { // simple
         if( src.getDatatype().isPrimitive() || src.getDatatype().isDerived() ) {
            trg.cloneValues(src);
         }
         else {
            DTSEnumerated edt = (DTSEnumerated)trg.getDatatype();
            trg.clear();
            for( int i = 0; i < values.size(); i++ ) {
               EnumerationLiteral srcEL = (EnumerationLiteral)values.get( i );
               if( srcEL != null ) {
                  EnumerationLiteral trgEL = edt.getLiteralByCode( srcEL.getCode() );
                  trg.setValue( trgEL, -1 );
               }
            }
         }
      }
   }
} // ConversionImpl
