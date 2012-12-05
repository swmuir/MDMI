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

import java.net.*;
import java.util.*;

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.model.*;

public class DefaultSemanticParser implements ISemanticParser {
   private ElementValueSet valueSet;

   @Override
   public void buildSemanticModel( MessageModel mdl, ISyntaxNode yroot, ElementValueSet eset ) {
      if( mdl == null || yroot == null || eset == null )
         throw new IllegalArgumentException("Null argument!");
      valueSet = eset;
      Node root = mdl.getSyntaxModel().getRoot();
      if( root != yroot.getNode() )
         throw new MdmiException("Invalid root node, expected {0}, found {1}.", root.getName(),
               (yroot.getNode() == null ? "null" : yroot.getNode().getName()));

      // 1. create all XElementValues
      getElements((YNode)yroot);

      // 2. set relationships
      ArrayList<IElementValue> xes = valueSet.getAllElementValues();
      for( IElementValue xe : xes ) {
         setRelations((XElementValue)xe);
      }

      // 3. set computed SEs
      SemanticElementSet set = mdl.getElementSet();
      Collection<SemanticElement> ses = set.getSemanticElements();
      for( SemanticElement se : ses ) {
         if( se.isComputed() )
            setComputedValue(se);
         else if( se.isComputedOut() )
            setComputedOutValue(se);
      }
   }

   @Override
   public YNode createSyntacticModel( MessageModel mdl, ElementValueSet eset ) {
      if( mdl == null || eset == null )
         throw new IllegalArgumentException("Null argument!");
      valueSet = eset;

      // 1. set computed SEs
      SemanticElementSet set = mdl.getElementSet();
      Collection<SemanticElement> ses = set.getSemanticElements();
      for( SemanticElement se : ses ) {
         if( se.isComputedIn() )
            setComputedInValue(se);
      }

      // 2. Create the syntax model
      Node root = mdl.getSyntaxModel().getRoot();
      if( root.getMinOccurs() != 1 || root.getMaxOccurs() != 1 )
         throw new MdmiException("Invalid mapping for node " + DefaultSyntacticParser.getNodePath(root));
      YNode yroot = null;
      Context context = new Context(valueSet);
      SemanticElement me = root.getSemanticElement();
      if( me == null ) {
         if( isSet(root, context) ) {
            yroot = createYNode(root, null);
            createChildYNodes(yroot, context);
         }
      }
      else {
         ArrayList<IElementValue> xes = context.getElementValues(me);
         if( xes.size() > 1 )
            throw new MdmiException("Invalid mapping for node " + DefaultSyntacticParser.getNodePath(root));
         if( xes.size() == 1 ) {
            yroot = createYNode(root, null);
            XElementValue xe = (XElementValue)xes.get(0);
            createChildYNodes(yroot, xe, context);
         }
      }
      return yroot;
   }

   private void getElements( YNode yroot ) {
      Node node = yroot.getNode();
      SemanticElement me = node.getSemanticElement();
      if( me == null ) {
         // node is not mapped to an element or field (one of the top nodes)
         if( yroot instanceof YBag ) {
            YBag y = (YBag)yroot;
            ArrayList<YNode> yns = y.getYNodes();
            for( YNode yn : yns ) {
               getElements(yn);
            }
         }
         else if( yroot instanceof YChoice ) {
            YChoice y = (YChoice)yroot;
            ArrayList<YNode> yns = y.getYNodes();
            for( YNode yn : yns ) {
               getElements(yn);
            }
         }
         // else we have a not-mapped Leaf, we ignore it
      }
      else {
         // top node is mapped to an element, no owner
         getMappedElement(yroot, null);
      }
   }

   private void getMappedElement( YNode yroot, XElementValue owner ) {
      if( yroot.isLeaf() ) {
         getSimpleElement((YLeaf)yroot, owner);
      }
      else if( yroot.isChoice() ) {
         getChoiceElement((YChoice)yroot, owner);
      }
      else if( yroot.isBag() ) { // Bag
         getStructElement((YBag)yroot, owner);
      }
      else
         throw new IllegalArgumentException("Invalid YNode!");
   }

   // a leaf mapped to a simple type (must be simple)
   private void getSimpleElement( YLeaf yleaf, XElementValue owner ) {
      SemanticElement me = yleaf.getLeaf().getSemanticElement();
      if( me == null )
         throw new MdmiException("Null SE for node {0}", DefaultSyntacticParser.getNodePath(yleaf.getNode()));
      XElementValue xe = new XElementValue(me, valueSet);
      if( owner != null ) {
         // set parent-child relationship
         owner.addChild(xe);
      }

      // set the value
      MdmiDatatype dt = me.getDatatype();
      if( !(dt.isSimple() || dt.isExternal()) )
         throw new MdmiException("Invalid mapping for node " + DefaultSyntacticParser.getNodePath(yleaf.getNode()));
      String value = yleaf.getValue();
      String format = yleaf.getLeaf().getFormat();
      XDT xdt = XDT.fromString(format);
      if( dt.isPrimitive() ) {
         DTSPrimitive pdt = (DTSPrimitive)dt;
         if( xdt == null )
            xdt = XDT.fromPDT(pdt);
         Object o = XDT.convertFromString(xdt, value, format, pdt);
         xe.getValue().addValue(o);
      }
      else if( dt.isDerived() ) {
         DTSDerived ddt = (DTSDerived)dt;
         DTSPrimitive pdt = ddt.getPrimitiveBaseType();
         if( xdt == null )
            xdt = XDT.fromPDT(pdt);
         Object o = XDT.convertFromString(xdt, value, format, pdt);
         xe.getValue().addValue(o);
      }
      else if( dt.isExternal() ) {
         DTExternal dte = (DTExternal)dt;
         URI uri = dte.getTypeSpec();
         if( uri != null ) {
            Object o = Mdmi.INSTANCE.getExternalResolvers().getDictionaryValue(dte, value);
            xe.getValue().addValue(o);
         }
         else {
            DTSPrimitive pdt = DTSPrimitive.STRING;
            if( xdt == null )
               xdt = XDT.fromPDT(pdt);
            Object o = XDT.convertFromString(xdt, value, format, pdt);
            xe.getValue().addValue(o);
         }
      }
      else {
         DTSEnumerated edt = (DTSEnumerated)dt;
         EnumerationLiteral el = edt.getLiteralByCode(value);
         xe.getValue().addValue(el);
      }
   }

   private void getChoiceElement( YChoice ychoice, XElementValue owner ) {
      SemanticElement me = ychoice.getChoice().getSemanticElement();
      if( me == null )
         throw new IllegalArgumentException("Null ME!");
      XElementValue xe = new XElementValue(me, valueSet);
      if( owner != null ) {
         // set parent-child relationship
         owner.addChild(xe);
      }

      // set the value
      MdmiDatatype dt = me.getDatatype();
      if( !dt.isChoice() )
         throw new MdmiException("Invalid mapping for node {0}, expected datatype choice, found {1}.",
               DefaultSyntacticParser.getNodePath(ychoice.getNode()), dt.toString());
      DTCChoice cdt = (DTCChoice)dt;
      XDataChoice xc = new XDataChoice(xe.getValue(), cdt);
      xe.getValue().addValue(xc);

      ArrayList<YNode> yns = ychoice.getYNodes();
      if( yns.size() > 0 ) { // otherwise its an empty choice
         YNode yn0 = yns.get(0);
         Node n = yn0.getNode();
         if( n.getSemanticElement() != null ) {
            // the choice has only child elements
            for( YNode yn : yns ) {
               getMappedElement(yn, xe);
            }
         }
         else {
            // the choice has only fields
            String fieldName = n.getFieldName();
            XValue xv = xc.setValue(fieldName);
            for( YNode yn : yns ) {
               getValue(yn, xv, xe);
            }
         }
      }
   }

   private void getStructElement( YBag ybag, XElementValue owner ) {
      SemanticElement me = ybag.getBag().getSemanticElement();
      if( me == null )
         throw new IllegalArgumentException("Null ME!");
      XElementValue xe = new XElementValue(me, valueSet);
      if( owner != null ) {
         // set parent-child relationship
         owner.addChild(xe);
      }

      // set the value
      MdmiDatatype dt = me.getDatatype();
      if( dt == null || !dt.isStruct() )
         throw new MdmiException("Invalid mapping for node " + DefaultSyntacticParser.getNodePath(ybag.getNode()));
      DTCStructured sdt = (DTCStructured)dt;
      XDataStruct xs = new XDataStruct(xe.getValue(), sdt);
      xe.getValue().addValue(xs);

      ArrayList<YNode> yns = ybag.getYNodes();
      for( YNode yn : yns ) {
         Node n = yn.getNode();
         if( n.getSemanticElement() != null ) {
            // child element
            getMappedElement(yn, xe);
         }
         else {
            // field
            String fieldName = n.getFieldName();
            XValue xv = xs.getValue(fieldName);
            if( xv == null )
               throw new MdmiException("Invalid mapping for node "
                     + DefaultSyntacticParser.getNodePath(ybag.getNode()));
            getValue(yn, xv, xe);
         }
      }
   }

   private void getValue( YNode yroot, XValue xv, XElementValue owner ) {
      if( yroot.isLeaf() ) {
         getValueLeaf((YLeaf)yroot, xv);
      }
      else if( yroot.isChoice() ) {
         getValueChoice((YChoice)yroot, xv, owner);
      }
      else if( yroot.isBag() ) { // Bag
         getValueStruct((YBag)yroot, xv, owner);
      }
      else
         throw new IllegalArgumentException("Invalid YNode!");
   }

   private void getValueLeaf( YLeaf yleaf, XValue xv ) {
      MdmiDatatype dt = xv.getDatatype();
      if( !(dt.isSimple() || dt.isExternal()) )
         throw new MdmiException("Invalid mapping for node " + DefaultSyntacticParser.getNodePath(yleaf.getNode()));

      String value = yleaf.getValue();
      String format = yleaf.getLeaf().getFormat();
      XDT xdt = XDT.fromString(format);
      if( xdt != null )
         format = null; // null the format so it does not get into the conversions
      if( dt.isPrimitive() ) {
         DTSPrimitive pdt = (DTSPrimitive)dt;
         if( xdt == null )
            xdt = XDT.fromPDT(pdt);
         Object o = XDT.convertFromString(xdt, value, format, pdt);
         xv.addValue(o);
      }
      else if( dt.isDerived() ) {
         DTSDerived ddt = (DTSDerived)dt;
         DTSPrimitive pdt = ddt.getPrimitiveBaseType();
         if( xdt == null )
            xdt = XDT.fromPDT(pdt);
         Object o = XDT.convertFromString(xdt, value, format, pdt);
         xv.addValue(o);
      }
      else if( dt.isExternal() ) {
         DTExternal dte = (DTExternal)dt;
         URI uri = dte.getTypeSpec();
         if( uri != null ) {
            Object o = Mdmi.INSTANCE.getExternalResolvers().getDictionaryValue(dte, value);
            xv.addValue(o);
         }
         else {
            DTSPrimitive pdt = DTSPrimitive.STRING;
            if( xdt == null )
               xdt = XDT.fromPDT(pdt);
            Object o = XDT.convertFromString(xdt, value, format, pdt);
            xv.addValue(o);
         }
      }
      else {
         DTSEnumerated edt = (DTSEnumerated)dt;
         EnumerationLiteral el = edt.getLiteralByCode(value);
         xv.addValue(el);
      }
   }

   private void getValueChoice( YChoice ychoice, XValue xv, XElementValue owner ) {
      MdmiDatatype dt = xv.getDatatype();
      if( !dt.isChoice() )
         throw new MdmiException("Invalid mapping for node " + DefaultSyntacticParser.getNodePath(ychoice.getNode()));
      DTCChoice cdt = (DTCChoice)dt;
      XDataChoice xc = new XDataChoice(xv, cdt);
      xv.addValue(xc);

      ArrayList<YNode> yns = ychoice.getYNodes();
      if( yns.size() > 0 ) { // otherwise its an empty choice
         YNode yn0 = yns.get(0);
         Node n = yn0.getNode();
         if( n.getSemanticElement() != null ) {
            // the choice has only child elements
            for( YNode yn : yns ) {
               getMappedElement(yn, owner);
            }
         }
         else {
            // the choice has only fields
            String fieldName = n.getFieldName();
            XValue xvalue = xc.setValue(fieldName);
            for( YNode yn : yns ) {
               getValue(yn, xvalue, owner);
            }
         }
      }
   }

   private void getValueStruct( YBag ybag, XValue xv, XElementValue owner ) {
      MdmiDatatype dt = xv.getDatatype();
      if( !dt.isStruct() )
         throw new MdmiException("Invalid mapping for node " + DefaultSyntacticParser.getNodePath(ybag.getNode()));
      DTCStructured sdt = (DTCStructured)dt;
      XDataStruct xs = new XDataStruct(xv, sdt);
      xv.addValue(xs);

      ArrayList<YNode> yns = ybag.getYNodes();
      for( YNode yn : yns ) {
         Node n = yn.getNode();
         if( n.getSemanticElement() != null ) {
            // child element
            getMappedElement(yn, owner);
         }
         else {
            // field
            String fieldName = n.getFieldName();
            XValue xvalue = xs.getValue(fieldName);
            if( xvalue == null )
               throw new MdmiException("Invalid mapping for node "
                     + DefaultSyntacticParser.getNodePath(ybag.getNode()));
            getValue(yn, xvalue, owner);
         }
      }
   }

   private void setRelations( XElementValue xe ) {
      SemanticElement me = xe.getSemanticElement();
      Collection<SemanticElementRelationship> rels = me.getRelationships();
      for( SemanticElementRelationship rel : rels ) {
         SemanticElement trg = rel.getRelatedSemanticElement();
         // 1. Check child elements
         ArrayList<IElementValue> xevs = valueSet.getChildElementValuesByType(trg, xe);
         if( xevs != null && 0 < xevs.size() ) {
            xe.getRelations().addAll(xevs);
            return;
         }
         // 2. Check child elements of the owner
         xevs = valueSet.getOwnerElementValuesByType(trg, xe);
         if( xevs != null && 0 < xevs.size() ) {
            xe.getRelations().addAll(xevs);
            return;
         }
         // 3. Last resort: get all
         xevs = valueSet.getElementValuesByType(trg);
         xe.getRelations().addAll(xevs);
      }
   }

   // ==================================================================================================================
   // owner does not have a ME
   private void createChildYNodes( YNode owner, Context context ) {
      Node node = owner.getNode();
      if( node instanceof Bag ) {
         // top-level bag unmapped
         Bag bag = (Bag)node;
         Collection<Node> nodes = bag.getNodes();
         for( Iterator<Node> it = nodes.iterator(); it.hasNext(); ) {
            Node n = it.next();
            SemanticElement me = n.getSemanticElement();
            if( me == null ) {
               if( isSet(n, context) ) {
                  YNode yn = createYNode(n, owner);
                  createChildYNodes(yn, context);
               }
            }
            else {
               ArrayList<IElementValue> xes = context.getElementValues(me);
               int max = n.getMaxOccurs();
               if( max < 0 )
                  max = Integer.MAX_VALUE;
               for( int i = 0; i < xes.size() && i <= max; i++ ) {
                  XElementValue xe = (XElementValue)xes.get(i);
                  YNode yn = createYNode(n, owner);
                  createChildYNodes(yn, xe, context);
               }
            }
         }
      }
      else if( node instanceof Choice ) {
         // top-level choice unmapped
         Choice choice = (Choice)node;
         Collection<Node> nodes = choice.getNodes();
         for( Iterator<Node> it = nodes.iterator(); it.hasNext(); ) {
            Node n = it.next();
            SemanticElement me = n.getSemanticElement();
            if( me == null ) {
               if( isSet(n, context) ) {
                  YNode yn = createYNode(n, owner);
                  createChildYNodes(yn, context);
                  break; // <-- NOTE, this is a choice
               }
            }
            else {
               ArrayList<IElementValue> xes = context.getElementValues(me);
               int max = n.getMaxOccurs();
               if( max < 0 )
                  max = Integer.MAX_VALUE;
               int count = 0;
               for( int i = 0; i < xes.size() && i <= max; i++ ) {
                  XElementValue xe = (XElementValue)xes.get(i);
                  YNode yn = createYNode(n, owner);
                  createChildYNodes(yn, xe, context);
                  count++;
               }
               if( count > 0 )
                  break; // <-- NOTE, this is a choice
            }
         }
      }
      else { // Leaf
         // top-level leaf unmapped, ignore
      }
   }

   // the ynode is mapped to the XE given
   private void createChildYNodes( YNode ynode, XElementValue xe, Context ownerContext ) {
      Node node = ynode.getNode();
      if( node instanceof Bag ) {
         // top-level bag mapped, process
         createChildYNodesForBag((YBag)ynode, xe, ownerContext);
      }
      else if( node instanceof Choice ) {
         // top-level choice mapped, process
         createChildYNodesForChoice((YChoice)ynode, xe, ownerContext);
      }
      else { // Leaf
         setLeafValue((YLeaf)ynode, xe);
      }
   }

   // the ynode is mapped to the XE given
   private void setChildYNodes( YNode ynode, Object value ) {
      Node node = ynode.getNode();
      if( node instanceof Bag ) {
         // top-level bag mapped, process
         if( !(value instanceof XDataStruct) )
            throw new IllegalArgumentException("Invalid map: expected XDataStruct for node "
                  + ynode.getNode().getName());
         setChildYNodesForBag((YBag)ynode, (XDataStruct)value);
      }
      else if( node instanceof Choice ) {
         // top-level choice mapped, process
         if( !(value instanceof XDataChoice) )
            throw new IllegalArgumentException("Invalid map: expected XDataChoice for node "
                  + ynode.getNode().getName());
         setChildYNodesForChoice((YChoice)ynode, (XDataChoice)value);
      }
      else { // Leaf
         setLeafValue((YLeaf)ynode, value);
      }
   }

   private void createChildYNodesForBag( YBag ybag, XElementValue xe, Context ownerContext ) {
      Node node = ybag.getNode();
      Bag bag = (Bag)node;
      Collection<Node> nodes = bag.getNodes();

      Context context = null; // NOTE this is a temporary fix for old maps
      if( xe.getChildCount() <= 0 )
         context = ownerContext;
      else
         context = new Context(xe);

      Object value = xe.getValue().getValue();
      if( value == null )
         throw new IllegalArgumentException("Null value!");
      if( !(value instanceof XDataStruct) )
         throw new IllegalArgumentException("Invalid structure!");
      XDataStruct xds = (XDataStruct)value;
      for( Iterator<Node> it = nodes.iterator(); it.hasNext(); ) {
         Node n = it.next();
         if( n.getSemanticElement() != null ) {
            ArrayList<IElementValue> xes = context.getElementValues(n.getSemanticElement());
            int max = n.getMaxOccurs();
            if( max < 0 )
               max = Integer.MAX_VALUE;
            for( int i = 0; i < xes.size() && i < max; i++ ) {
               XElementValue nxe = (XElementValue)xes.get(i);
               YNode yn = createYNode(n, ybag);
               createChildYNodes(yn, nxe, context);
            }
         }
         else {
            String fieldName = n.getFieldName();
            XValue xvalue = xds.getValue(fieldName);
            if( xvalue == null || xvalue.size() <= 0 )
               continue;
            for( int i = 0; i < xvalue.size(); i++ ) {
               Object xv = xvalue.getValue(i);
               YNode yn = createYNode(n, ybag);
               setChildYNodes(yn, xv);
            }
         }
      }
   }

   private void setChildYNodesForBag( YBag ybag, XDataStruct xds ) {
      Node node = ybag.getNode();
      Bag bag = (Bag)node;
      Collection<Node> nodes = bag.getNodes();

      for( Iterator<Node> it = nodes.iterator(); it.hasNext(); ) {
         Node n = it.next();
         String fieldName = n.getFieldName();
         XValue xvalue = xds.getValue(fieldName);
         if( xvalue == null || xvalue.size() <= 0 )
            continue;
         for( int i = 0; i < xvalue.size(); i++ ) {
            Object xv = xvalue.getValue(i);
            YNode yn = createYNode(n, ybag);
            setChildYNodes(yn, xv);
         }
      }
   }

   private void createChildYNodesForChoice( YChoice ychoice, XElementValue xe, Context ownerContext ) {
      Node node = ychoice.getNode();
      Choice choice = (Choice)node;
      Collection<Node> nodes = choice.getNodes();

      Context context = null; // NOTE this is a temporary fix for old maps
      if( xe.getChildCount() <= 0 )
         context = ownerContext;
      else
         context = new Context(xe);

      Object value = xe.getValue().getValue();
      if( value == null )
         throw new IllegalArgumentException("Null value!");
      if( !(value instanceof XDataChoice) )
         throw new IllegalArgumentException("Invalid structure!");
      XDataChoice xdc = (XDataChoice)value;
      XValue xvalue = xdc.getValue();
      if( xvalue == null || xvalue.size() <= 0 ) {
         for( Iterator<Node> it = nodes.iterator(); it.hasNext(); ) {
            Node n = it.next();
            if( n.getSemanticElement() != null ) {
               ArrayList<IElementValue> xes = context.getElementValues(n.getSemanticElement());
               int max = n.getMaxOccurs();
               if( max < 0 )
                  max = Integer.MAX_VALUE;
               for( int i = 0; i < xes.size() && i < max; i++ ) {
                  XElementValue nxe = (XElementValue)xes.get(i);
                  YNode yn = createYNode(n, ychoice);
                  createChildYNodes(yn, nxe, context);
               }
               if( xes.size() > 0 )
                  return; // <-- NOTE this is a choice
            }
         }
      }
      else {
         Node n = getNodeForFieldName(choice, xvalue.getName());
         for( int i = 0; i < xvalue.size(); i++ ) {
            Object xv = xvalue.getValue(i);
            YNode yn = createYNode(n, ychoice);
            setChildYNodes(yn, xv);
         }
      }
   }

   private void setChildYNodesForChoice( YChoice ychoice, XDataChoice xdc ) {
      Node node = ychoice.getNode();
      Choice choice = (Choice)node;

      XValue xvalue = xdc.getValue();
      if( xvalue != null && xvalue.size() > 0 ) {
         Node n = getNodeForFieldName(choice, xvalue.getName());
         for( int i = 0; i < xvalue.size(); i++ ) {
            Object xv = xvalue.getValue(i);
            YNode yn = createYNode(n, ychoice);
            setChildYNodes(yn, xv);
         }
      }
   }

   private void setLeafValue( YLeaf yleaf, XElementValue xe ) {
      setLeafValue(yleaf, xe.getValue().getValue());
   }

   private void setLeafValue( YLeaf yleaf, Object value ) {
      if( value == null )
         return;
      Node node = yleaf.getNode();
      String format = yleaf.getLeaf().getFormat();
      XDT xdt = XDT.fromString(format);
      MdmiDatatype dt = getDatatype(node);
      if( !dt.isSimple() )
         throw new MdmiException("Invalid mapping for node " + DefaultSyntacticParser.getNodePath(yleaf.getNode()));

      String v = null;
      if( dt.isPrimitive() ) {
         DTSPrimitive pdt = (DTSPrimitive)dt;
         if( xdt == null )
            xdt = XDT.fromPDT(pdt);
         v = XDT.convertToString(pdt, value, format, xdt);
      }
      else if( dt.isDerived() ) {
         DTSDerived ddt = (DTSDerived)dt;
         DTSPrimitive pdt = ddt.getPrimitiveBaseType();
         if( xdt == null )
            xdt = XDT.fromPDT(pdt);
         v = XDT.convertToString(pdt, value, format, xdt);
      }
      else if( dt.isExternal() ) {
         DTExternal dte = (DTExternal)dt;
         URI uri = dte.getTypeSpec();
         if( uri != null ) {
            v = Mdmi.INSTANCE.getExternalResolvers().getModelValue(dte, value);
         }
         else {
            DTSPrimitive pdt = DTSPrimitive.STRING;
            if( xdt == null )
               xdt = XDT.fromPDT(pdt);
            v = XDT.convertToString(pdt, value, format, xdt);
         }
      }
      else {
         DTSEnumerated edt = (DTSEnumerated)dt;
         if( value instanceof EnumerationLiteral )
            v = ((EnumerationLiteral)value).getCode();
         else if( value instanceof String )
            v = (String)value;
         else
            throw new MdmiException("Invalid enum conversion for type {0} value {1}", edt.getTypeName(), value);
      }
      yleaf.setValue(v);
   }

   private MdmiDatatype getDatatype( Node node ) {
      ArrayList<String> fns = new ArrayList<String>();
      Node n = node;
      while( n.getSemanticElement() == null ) {
         fns.add(0, n.getFieldName());
         n = n.getParentNode();
      }

      MdmiDatatype dt = n.getSemanticElement().getDatatype();
      for( int i = 0; i < fns.size(); i++ ) {
         String fieldName = fns.get(i);
         Field f = null;
         if( dt.isStruct() ) {
            DTCStructured dts = (DTCStructured)dt;
            f = dts.getField(fieldName);
         }
         else if( dt.isChoice() ) {
            DTCChoice dtc = (DTCChoice)dt;
            f = dtc.getField(fieldName);
         }
         if( f == null )
            throw new MdmiException("Invalid construct for node " + DefaultSyntacticParser.getNodePath(node));
         dt = f.getDatatype();
      }
      return dt;
   }

   private YNode createYNode( Node node, YNode owner ) {
      YNode ynode = null;
      if( node instanceof Bag ) {
         //System.out.println("Creating bag node " + node.getName() + (owner == null ? "" : " as child of " + owner.getNode().getName()));
         ynode = new YBag((Bag)node, owner);
      }
      else if( node instanceof Choice ) {
         //System.out.println("Creating choice node " + node.getName() + (owner == null ? "" : " as child of " + owner.getNode().getName()));
         ynode = new YChoice((Choice)node, owner);
      }
      else {
         //System.out.println("Creating leaf node " + node.getName() + (owner == null ? "" : " as child of " + owner.getNode().getName()));
         ynode = new YLeaf((LeafSyntaxTranslator)node, owner);
      }
      if( owner != null ) {
         if( owner instanceof YBag )
            ((YBag)owner).addYNode(ynode);
         else if( owner instanceof YChoice )
            ((YChoice)owner).addYNode(ynode);
         else
            throw new IllegalArgumentException("Invalid state: owner is a leaf!");
      }
      return ynode;
   }

   /**
    * Returns true if the node appears in the context between min and max times.
    *
    * @param node The node.
    * @param context The context.
    * @return True if the node appears in the context between min and max times.
    */
   private boolean isSet( Node node, Context context ) {
      SemanticElement me = node.getSemanticElement();
      if( me != null ) {
         int count = context.getCount(me);
         return isNodeSet(node, count);
      }

      if( node instanceof Bag ) {
         Bag bag = (Bag)node;
         Collection<Node> nodes = bag.getNodes();
         for( Iterator<Node> i = nodes.iterator(); i.hasNext(); ) {
            Node n = i.next();
            if( !isSet(n, context) ) {
            	System.out.println("Error: isSet returning from " + n.getName() + " (" + n.getLocation() + ")");
               return false;
            }
         }
         return true;
      }
      else if( node instanceof Choice ) {
         Choice choice = (Choice)node;
         Collection<Node> nodes = choice.getNodes();
         for( Iterator<Node> i = nodes.iterator(); i.hasNext(); ) {
            Node n = i.next();
            if( isSet(n, context) )
               return true;
         }
         return false;
      }
      return false;
   }

   // returns true if the count is between min and max for the given node
   private static boolean isNodeSet( Node node, int count ) {
      //if( count <= 0 )
      //   return false;
      if( count < node.getMinOccurs() )
         return false;
      if( node.getMaxOccurs() < 0 )
         return true;
      return count <= node.getMaxOccurs();
   }

   private static Node getNodeForFieldName( Choice choice, String fieldName ) {
      Collection<Node> nodes = choice.getNodes();
      for( Iterator<Node> it = nodes.iterator(); it.hasNext(); ) {
         Node node = it.next();
         if( fieldName.equals(node.getFieldName()) )
            return node;
      }
      return null;
   }

   private static class Context {
      ElementValueSet eset;
      XElementValue   owner;

      Context( ElementValueSet eset ) {
         this.eset = eset;
      }

      Context( XElementValue owner ) {
         this.owner = owner;
      }

      boolean isSet() {
         return eset != null;
      }

      int getCount( SemanticElement me ) {
         int count = 0;
         if( isSet() ) {
            ArrayList<IElementValue> xes = eset.getElementValuesByType(me);
            count = xes.size();
         }
         else {
            ArrayList<IElementValue> xes = owner.getChildren();
            for( IElementValue xe : xes ) {
               if( me == xe.getSemanticElement() )
                  count++;
            }
         }
         return count;
      }

      ArrayList<IElementValue> getElementValues( SemanticElement me ) {
         if( isSet() )
            return eset.getElementValuesByType(me);
         ArrayList<IElementValue> a = new ArrayList<IElementValue>();
         ArrayList<IElementValue> xes = owner.getChildren();
         for( IElementValue xe : xes ) {
            if( me == xe.getSemanticElement() )
               a.add(xe);
         }
         return a;
      }
   }

   private void setComputedValue( SemanticElement se ) {
      String rule = se.getComputedValue().getExpression();
      XElementValue xe = new XElementValue(se, valueSet);
      IExpressionInterpreter adapter = new NrlAdapter(valueSet, xe, "", null);
      adapter.evalAction(xe, rule);
   }

   private void setComputedOutValue( SemanticElement se ) {
      String rule = se.getComputedOutValue().getExpression();
      XElementValue xe = new XElementValue(se, valueSet);
      IExpressionInterpreter adapter = new NrlAdapter(valueSet, xe, "", null);
      adapter.evalAction(xe, rule);
   }

    private void setComputedInValue(SemanticElement se) {
        String rule = se.getComputedInValue().getExpression();
        XElementValue xe;
        if (valueSet.getElementValuesByType(se).size() > 0) {
            xe = (XElementValue) valueSet.getElementValuesByType(se).get(0);
        } else {
            xe = new XElementValue(se, valueSet);
        }

        IExpressionInterpreter adapter = new NrlAdapter(valueSet, xe, "", null);
        adapter.evalAction(xe, rule);
    }
} // DefaultMdmiSemanticParser
