/*******************************************************************************
* Copyright (c) 2013 Firestar Software, Inc.
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
 * Structured view of the element value set, grouped by semantic elements and in tree form.
 * 
 * @author goancea
 */
final class XElementValues {
   public ArrayList<XES> elementValues = new ArrayList<XES>();
   
   /**
    * Construct one from an element values set.
    * 
    * @param eset The element value set to useas source.
    */
   public XElementValues( ElementValueSet eset ) {
      ArrayList<IElementValue> values = eset.getAllElementValues();
      for( IElementValue value : values ) {
         XElementValue v = (XElementValue)value;
         if( v.getParent() == null ) {
            XE xe = new XE(v);
            XES xes = get(v.getSemanticElement());
            if( xes == null ) {
               xes = new XES(v.getSemanticElement());
               elementValues.add(xes);
            }
            xes.elementValues.add(xe);
            elementValues.add(xes);
            addChildren( xe );
         }
      }
   }
   
   private XES get( SemanticElement se ) {
      for( XES xes : elementValues ) {
         if( xes.semanticElement == se )
            return xes;
      }
      return null;
   }
   
   private void addChildren( XE xe ) {
      ArrayList<IElementValue> values = xe.elementValue.getChildren();
      for( IElementValue value : values ) {
         XElementValue v = (XElementValue)value;
         SemanticElement se = v.getSemanticElement();
         XES xes = xe.get(se);
         if( xes == null ) {
            xes = new XES(se);
            xe.children.add(xes);
         }
         XE child = new XE(v);
         xes.elementValues.add(child);
         addChildren(child);
      }
   }
   
   @Override
   public String toString() {
      return toString("");
   }

   public String toString( String indent ) {
      StringBuffer sb = new StringBuffer(256);
      sb.append("ElementValues: [\n");
      for( XES xes: elementValues ) {
         sb.append(indent + " ").append(xes.toString(indent + " ")).append("\n");
      }
      sb.append(indent).append("]}");
      return sb.toString();
   }
   
   static class XE {
      public XElementValue elementValue;
      public ArrayList<XES> children = new ArrayList<XES>();
      
      public XE( XElementValue elementValue ) {
         this.elementValue = elementValue;
      }
      
      public XES get( SemanticElement se ) {
         for( XES c : children ) {
            if( c.semanticElement == se )
               return c;
         }
         return null;
      }
      
      @Override
      public String toString() {
         return toString("");
      }

      public String toString( String indent ) {
         StringBuffer sb = new StringBuffer(256);
         sb.append("{XE: '").append(elementValue.getName()).append("'");
         if( children.size() <= 0 ) {
            sb.append(", value: ").append(elementValue.getXValue().toString(indent + " "));
            sb.append("}");
            return sb.toString();
         }
         sb.append(", children: [\n");
         for( XES xes: children ) {
            sb.append(xes.toString(indent + " ")).append("\n");
         }
         sb.append("]}");
         return sb.toString();
      }
   }
   
   static class XES {
      public SemanticElement semanticElement;
      public ArrayList<XE> elementValues = new ArrayList<XE>();
      
      public XES( SemanticElement semanticElement ) {
         this.semanticElement = semanticElement;
      }
      
      @Override
      public String toString() {
         return toString("");
      }

      public String toString( String indent ) {
         StringBuffer sb = new StringBuffer(256);
         sb.append("{XES: '").append(semanticElement.getName()).append("'");
         if( elementValues.size() <= 0 ) {
            sb.append("}");
            return sb.toString();
         }
         sb.append(", elementValues: [\n");
         for( XE xe: elementValues ) {
            sb.append(indent + " ").append(xe.toString(indent + " ")).append("\n");
         }
         sb.append(indent).append("]}");
         return sb.toString();
      }
   }
} // XElementValues
