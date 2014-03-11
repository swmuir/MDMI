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
package com.whitestar.mdmi;

import java.io.*;
import java.nio.*;
import java.util.*;
import java.math.*;

import javax.xml.bind.JAXBContext;

import net.sourceforge.nrl.parser.*;
import net.sourceforge.nrl.parser.model.*;
import net.sourceforge.nrl.parser.operators.*;

import com.modeltwozero.nrl.engine.*;
import com.modeltwozero.nrl.engine.operators.*;
import com.modeltwozero.nrl.engine.runtimemodel.*;

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.engine.*;
import org.openhealthtools.mdht.mdmi.model.*;

/**
 * Implementation of the IExpressionInterpreter for NRL. It is used by the runtime to evaluate NRL expressions at
 * runtime.
 * 
 * @author goancea
 */
public class NrlAdapter implements IRuntimeModelAdapter, IExpressionInterpreter {
   private static final boolean       DEBUG = false;

   private ElementValueSet            m_eset;
   private XElementValue              m_context;
   private String                     m_name;
   private XValue                     m_value;

   private static JavaOperatorManager s_jom = new JavaOperatorManager(NrlAdapter.class.getClassLoader());
   private static IOperators[]        s_operators;
   private static boolean             s_operatorsLoaded;

   /**
    * Construct one based on the given parameters.
    * 
    * @param eset The element values set for the whole message.
    * @param context The rule evaluation context is an element value.
    * @param name The name of the context, used by the NRL engine.
    * @param value The additional value transferred from or to.
    */
   public NrlAdapter() {
   }

   public void initialize( ElementValueSet eset, XElementValue context, String name, XValue value ) {
      m_eset = eset;
      m_context = context;
      m_name = name;
      m_value = value;
   }

   @Override
   public Collection<?> getRootObjects() {
      if( DEBUG )
         System.out.println("getRootObjects()");
      if( m_eset == null )
         throw new IllegalArgumentException("Invalid state: adapter is not initialized!");
      return m_eset.getAllElementValues();
   }

   @Override
   public Collection<?> getChildren( Object parent ) {
      if( DEBUG )
         System.out.println("getChildren(" + parent + ")");
      if( !((parent instanceof XElementValue) || (parent instanceof XValue)) )
         throw new IllegalArgumentException("getChildren() parent " + parent + " is not a valid model object!");
      return getChildren(parent, null, false);
   }

   @Override
   public Collection<?> getChildrenByName( Object parent, String name ) {
      if( DEBUG )
         System.out.println("getChildrenByName(" + parent + ", " + name + ")");
      if( !((parent instanceof XElementValue) || (parent instanceof XValue)) )
         throw new IllegalArgumentException("getChildrenByName() parent " + parent + " is not a valid model object!");
      if( name == null || name.length() <= 0 )
         throw new IllegalArgumentException("getChildrenByName() name is null or empty!");
      return getChildren(parent, name, false);
   }

   @Override
   public Object getFirstChildByName( Object parent, String name ) {
      if( DEBUG )
         System.out.println("getFirstChildByName(" + parent + ", " + name + ")");
      if( !((parent instanceof XElementValue) || (parent instanceof XValue)) )
         throw new IllegalArgumentException("getFirstChildByName() parent " + parent + " is not a valid model object!");
      if( name == null || name.length() <= 0 )
         throw new IllegalArgumentException("getFirstChildByName() name is null or empty!");
      ArrayList<Object> a = getChildren(parent, name, true);
      return a.size() <= 0 ? null : a.get(0);
   }

   @Override
   public String getName( Object obj ) {
      if( DEBUG )
         System.out.println("getName(" + obj + ")");
      if( obj == null )
         return null;
      else if( obj instanceof XElementValue )
         return ((XElementValue)obj).getSemanticElement().getName();
      else if( obj instanceof XValue )
         return ((XValue)obj).getName();
      throw new IllegalArgumentException("Invalid object type, not a model element: " + obj.getClass().getName());
   }

   @Override
   public IModelElement getType( Object obj ) {
      if( DEBUG )
         System.out.println("getType(" + obj + ")");
      if( obj == null )
         return null;
      else if( obj instanceof XElementValue )
         return ((XElementValue)obj).getSemanticElement();
      else if( obj instanceof XValue )
         return ((XValue)obj).getDatatype();
      throw new IllegalArgumentException("Invalid object type, not a model element: " + obj.getClass().getName());
   }

   @Override
   public Object getValue( Object obj ) {
      if( DEBUG )
         System.out.println("getValue(" + obj + ")");
      if( obj == null )
         return null;
      else if( obj instanceof XValue ) {
         XValue xv = (XValue)obj;
         MdmiDatatype dt = xv.getDatatype();
         if( !dt.isSimple() )
            throw new IllegalArgumentException("getValue() the data type " + dt.getTypeName() + " is not simple!");
         if( xv.size() == 0 )
            return null;

         DTSPrimitive pdt = null;
         if( dt.isPrimitive() )
            pdt = (DTSPrimitive)dt;
         else if( dt.isDerived() )
            pdt = ((DTSimple)dt).getPrimitiveBaseType();
         else
            pdt = DTSPrimitive.STRING;
         if( xv.size() == 1 ) {
            return convertToNRL(pdt, xv.getValue());
         }

         if( DEBUG )
            System.out.println("ARRAY WARNING! " + xv.toString());
         ArrayList<Object> values = new ArrayList<Object>();
         for( int i = 0; i < xv.getValues().size(); i++ ) {
            values.add(convertToNRL(pdt, xv.getValues().get(i)));
         }
         return values;
      }
      return null;
   }

   @Override
   public boolean hasChild( Object parent, String name ) {
      if( DEBUG )
         System.out.println("hasChild(" + parent + ", " + name + ")");
      if( !((parent instanceof XElementValue) || (parent instanceof XValue)) )
         throw new IllegalArgumentException("hasChild() parent " + parent + " is not a valid model object!");
      if( name == null || name.length() <= 0 )
         throw new IllegalArgumentException("hasChild() name is null or empty!");
      ArrayList<Object> a = getChildren(parent, name, true);
      return a.size() > 0;
   }

   @Override
   public boolean hasChildren( Object parent ) {
      if( DEBUG )
         System.out.println("hasChildren(" + parent + ")");
      if( !((parent instanceof XElementValue) || (parent instanceof XValue)) )
         throw new IllegalArgumentException("hasChild() parent " + parent + " is not a valid model object!");
      ArrayList<Object> a = getChildren(parent, null, true);
      return a.size() > 0;
   }

   @Override
   public boolean isAModelObject( Object obj ) {
      if( DEBUG )
         System.out.println("isAModelObject(" + obj + ")");
      if( obj == null )
         return false;
      return (obj instanceof XElementValue) || (obj instanceof XValue);
   }

   // ========== ACTION LANGUAGE ========================

   @Override
   public void addChild( Object parent, Object child ) {
      if( DEBUG )
         System.out.println("addChild(" + parent + ", " + child + ")");
      if( !((parent instanceof XElementValue) || (parent instanceof XValue)) )
         throw new IllegalArgumentException("addChild() parent " + parent + " is not a valid model object!");
      if( !(child instanceof XValue) )
         throw new IllegalArgumentException("addChild() child " + child + " is not a valid model object!");
      XValue src = (XValue)child;
      XValue trg = null;
      if( parent instanceof XElementValue ) {
         XElementValue xe = (XElementValue)parent;
         trg = xe.getXValue();
      }
      else
         trg = (XValue)parent;
      addChildValue(trg, src, -1);
   }

   private void addChildValue( XValue parent, XValue child, int index ) {
      if( parent.getDatatype().isSimple() ) {
         parent.addValue(child.getValue(), index);
      }
      else if( parent.getDatatype().isStruct() ) {
         // index is ignored in this case, must be the first
         XDataStruct x = (XDataStruct)parent.getValue();
         if( x == null ) {
            x = new XDataStruct(parent);
            parent.setValue(x);
         }
         x.setXValue(child);
      }
      else if( parent.getDatatype().isChoice() ) {
         // index is ignored in this case, must be the first
         XDataChoice x = (XDataChoice)parent.getValue();
         if( x == null ) {
            x = new XDataChoice(parent);
            parent.setValue(x);
         }
         x.setXValue(child);
      }
   }

   @Override
   public void addChild( Object parent, Object afterSibling, Object child ) throws ExecutionException {
      if( DEBUG )
         System.out.println("addChild(" + parent + ", " + afterSibling + ", " + child + ")");
      if( !((parent instanceof XElementValue) || (parent instanceof XValue)) )
         throw new IllegalArgumentException("addChild() parent " + parent + " is not a valid model object!");
      if( afterSibling != null && !(afterSibling instanceof XValue) )
         throw new IllegalArgumentException("addChild() afterSibling " + afterSibling + " is not a valid model object!");
      if( !(child instanceof XValue) )
         throw new IllegalArgumentException("addChild() child " + child + " is not a valid model object!");
      XValue src = (XValue)child;
      XValue trg = null;
      if( parent instanceof XElementValue ) {
         XElementValue xe = (XElementValue)parent;
         trg = xe.getXValue();
      }
      else
         trg = (XValue)parent;
      if( afterSibling == null )
         addChildValue(trg, src, -1);
      else {
         XValue a = (XValue)afterSibling;
         int p = trg.getIndexOf(a.getValue());
         if( p >= 0 )
            trg.addValue(src.getValue(), p + 1);
         else
            trg.addValue(src.getValue(), -1);
      }
   }

   @Override
   public Object createModelObject( IModelElement type ) throws ExecutionException {
      if( DEBUG )
         System.out.println("createModelObject(" + type + ")");
      if( type == null )
         throw new IllegalArgumentException("Null model element type!");
      else if( !((type instanceof SemanticElement) || (type instanceof MdmiDatatype)) )
         throw new IllegalArgumentException("Invalid model element type " + type.getClass().getName());
      Object obj = null;
      if( type instanceof SemanticElement ) {
         SemanticElement se = (SemanticElement)type;
         obj = new XElementValue(se, m_eset);
      }
      else if( type instanceof MdmiDatatype ) {
         MdmiDatatype dt = (MdmiDatatype)type;
         obj = new XValue(dt.getTypeName(), dt);
      }
      return obj;
   }

   @Override
   public Object cloneModelObject( Object obj, boolean deep ) throws ExecutionException {
      if( DEBUG )
         System.out.println("cloneModelObject(" + obj + ", " + deep + ")");
      if( !((obj instanceof XElementValue) || (obj instanceof XValue)) )
         throw new IllegalArgumentException("cloneModelObject() object " + obj + " is not a valid model object!");
      if( obj instanceof XElementValue )
         return ((XElementValue)obj).clone(deep);
      return ((XValue)obj).clone(deep);
   }

   @Override
   public void removeChild( Object parent, Object child ) throws ExecutionException {
      if( DEBUG )
         System.out.println("removeChild(" + parent + ", " + child + ")");
      if( !((parent instanceof XElementValue) || (parent instanceof XValue)) )
         throw new IllegalArgumentException("removeChild() parent " + parent + " is not a valid model object!");
      if( !(child instanceof XValue) )
         throw new IllegalArgumentException("removeChild() child " + child + " is not a valid model object!");
      XValue src = (XValue)child;
      XValue trg = null;
      if( parent instanceof XElementValue ) {
         XElementValue xe = (XElementValue)parent;
         trg = xe.getXValue();
      }
      else
         trg = (XValue)parent;

      if( trg.getDatatype().isSimple() ) {
         trg.removeValue(src);
      }
      else if( trg.getDatatype().isStruct() ) {
         XDataStruct x = (XDataStruct)trg.getValue();
         if( x == null ) {
            x = new XDataStruct(trg);
            trg.setValue(x);
         }
         x.clearValue(src.getName());
      }
      else if( trg.getDatatype().isChoice() ) {
         XDataChoice x = (XDataChoice)trg.getValue();
         if( x == null ) {
            x = new XDataChoice(trg);
            trg.setValue(x);
         }
         x.clearValue();
      }
   }

   @Override
   public void replaceChild( Object parent, Object child1, Object child2 ) throws ExecutionException {
      if( DEBUG )
         System.out.println("replaceChild(" + parent + ", " + child1 + ", " + child2 + ")");
      if( !((parent instanceof XElementValue) || (parent instanceof XValue)) )
         throw new IllegalArgumentException("replaceChild() parent " + parent + " is not a valid model object!");
      if( child1 != null && !(child1 instanceof XValue) )
         throw new IllegalArgumentException("replaceChild() child1 " + child1 + " is not a valid model object!");
      if( child2 != null && !(child2 instanceof XValue) )
         throw new IllegalArgumentException("replaceChild() child2 " + child2 + " is not a valid model object!");
      if( child1 == null ) {
         addChild(parent, child2);
         return;
      }
      else if( child2 == null ) {
         removeChild(parent, child1);
         return;
      }

      // XValue c1 = (XValue)child1;
      XValue c2 = (XValue)child2;
      XValue trg = null;
      if( parent instanceof XElementValue ) {
         XElementValue xe = (XElementValue)parent;
         trg = xe.getXValue();
      }
      else
         trg = (XValue)parent;

      if( trg.getDatatype().isSimple() ) {
         trg.setValue(c2);
      }
      else if( trg.getDatatype().isStruct() ) {
         XDataStruct x = (XDataStruct)trg.getValue();
         if( x == null ) {
            x = new XDataStruct(trg);
            trg.setValue(x);
         }
         x.setXValue(c2);
      }
      else if( trg.getDatatype().isChoice() ) {
         XDataChoice x = (XDataChoice)trg.getValue();
         if( x == null ) {
            x = new XDataChoice(trg);
            trg.setValue(x);
         }
         x.setXValue(c2);
      }
   }

   @Override
   public void setName( Object obj, String name ) {
      if( DEBUG )
         System.out.println("setName(" + obj + ", " + name + ")");
      if( !(obj instanceof XValue) )
         throw new IllegalArgumentException("setName() obj " + obj + " is not a valid model object!");
      ((XValue)obj).setName(name);
   }

   @Override
   public void setParent( Object child, Object parent ) {
      if( DEBUG )
         System.out.println("setParent( " + child + ", " + parent + " )");
      if( parent == null || child == null )
         throw new IllegalArgumentException("Null parent and/or child element!");

      if( parent instanceof XElementValue ) {
         XElementValue xparent = (XElementValue)parent;
         if( child instanceof XElementValue ) {
            XElementValue xchild = (XElementValue)child;
            xparent.addChild(xchild);
         }
         else if( child instanceof XValue ) {
            XValue xchild = (XValue)child;
            XValue v = xparent.getXValue();
            if( v.getDatatype().isSimple() )
               throw new IllegalArgumentException("Invalid set parent on a semantic element with a simple type!");
            else if( v.getDatatype().isStruct() ) {
               XDataStruct xdt = (XDataStruct)v.getValue();
               if( xdt == null ) {
                  xdt = new XDataStruct(v);
                  v.setValue(xdt);
               }
                xdt.setXValue(xchild);
         
            }
            else if( v.getDatatype().isChoice() ) {
               XDataChoice xdc = (XDataChoice)v.getValue();
               if( xdc == null ) {
                  xdc = new XDataChoice(v);
                  v.setValue(xdc);
               }
               xdc.setXValue(xchild);
            }
         }
         else
            throw new IllegalArgumentException("Invalid child - not a valid object!");
      }
      else if( parent instanceof XValue ) {
         XValue xparent = (XValue)parent;
         if( child instanceof XValue ) {
            XValue xchild = (XValue)child;
            if( xparent.getDatatype().isSimple() )
               throw new IllegalArgumentException("Invalid set parent on a semantic element with a simple type!");
            else if( xparent.getDatatype().isStruct() ) {
               XDataStruct xdt = (XDataStruct)xparent.getValue();
               if( xdt == null ) {
                  xdt = new XDataStruct(xparent);
                  xparent.setValue(xdt);
               }
                xdt.setXValue(xchild);
              
            }
            else if( xparent.getDatatype().isChoice() ) {
               XDataChoice xdc = (XDataChoice)xparent.getValue();
               if( xdc == null ) {
                  xdc = new XDataChoice(xparent);
                  xparent.setValue(xdc);
               }
               xdc.setXValue(xchild);
            }
         }
         else
            throw new IllegalArgumentException("Invalid child - not a valid object!");
      }
      else
         throw new IllegalArgumentException("Invalid parent - not a valid object!");
   }

   @Override
   public void setValue( Object obj, Object value ) {
      if( DEBUG )
         System.out.println("setValue(" + obj + ", " + value + ")");
      if( !(obj instanceof XValue) )
         throw new IllegalArgumentException("setValue() obj " + obj + " is not a valid model object!");
      XValue xv = (XValue)obj;
      MdmiDatatype dt = xv.getDatatype();
      if( !dt.isSimple() )
         throw new IllegalArgumentException("setValue() the data type " + dt.getTypeName() + " is not simple!");
      DTSPrimitive pdt = null;
      if( dt.isPrimitive() )
         pdt = (DTSPrimitive)dt;
      else if( dt.isDerived() )
         pdt = ((DTSimple)dt).getPrimitiveBaseType();
      else
         pdt = DTSPrimitive.STRING;
      Object v = convertFromNRL(pdt, value);
      if( xv.getDatatype().isEnum() )
         v = ((DTSEnumerated)xv.getDatatype()).getLiteralByCode((String)v);
      xv.setValue(v);
   }

   private ArrayList<Object> getChildren( Object parent, String name, boolean breakAtFirst ) {
      ArrayList<Object> a = new ArrayList<Object>();
      if( parent == m_context && name != null && m_name.equals(name) ) {
         a.add(m_value);
         return a;
      }

      if( parent instanceof XElementValue ) {
         XElementValue xe = (XElementValue)parent;
         ArrayList<IElementValue> c = xe.getChildren();
         c.addAll(xe.getRelations());
         for( int i = 0; i < c.size(); i++ ) {
            XElementValue cxe = (XElementValue)c.get(i);
            if( name == null || name.equalsIgnoreCase(cxe.getName()) ) {
               a.add(cxe);
               if( breakAtFirst )
                  return a;
            }
         }
         if( name == null || SemanticElement.VALUE_NAME.equalsIgnoreCase(name) )
            a.add(xe.getXValue());
         else {
            a.addAll(getXValueChildren(xe.getXValue(), name, breakAtFirst));
         }
      }
      else if( parent instanceof XValue ) {
         XValue xv = (XValue)parent;
         a.addAll(getXValueChildren(xv, name, breakAtFirst));
      }
      return a;
   }

   private ArrayList<Object> getXValueChildren( XValue parent, String name, boolean breakAtFirst ) {
      ArrayList<Object> a = new ArrayList<Object>();
      XValue xv = (XValue)parent;
      if( xv.getDatatype().isStruct() ) {
         ArrayList<Object> xvs = xv.getValues();
         for( int i = 0; i < xvs.size(); i++ ) {
            XDataStruct xs = (XDataStruct)xvs.get(i);
            Collection<XValue> vs = xs.getXValues();
            for( XValue v : vs ) {
               if( name == null || name.equalsIgnoreCase(v.getName()) ) {
                  a.add(v);
                  if( breakAtFirst )
                     return a;
               }
            }
         }
      }
      else if( xv.getDatatype().isChoice() ) {
         ArrayList<Object> xvs = xv.getValues();
         for( int i = 0; i < xvs.size(); i++ ) {
            XDataChoice xc = (XDataChoice)xvs.get(i);
            XValue v = xc.getXValue();
            if( name == null || name.equalsIgnoreCase(v.getName()) ) {
               a.add(v);
               if( breakAtFirst )
                  return a;
            }
         }
      }
      return a;
   }

   private Object convertToNRL( DTSPrimitive pdt, Object value ) {
      if( value == null )
         return null;
      if( DTSPrimitive.BINARY.equals(pdt) ) {
         ByteBuffer bb = (ByteBuffer)value;
         return bb.toString(); // TODO not sure how to convert binaries
      }
      else if( DTSPrimitive.BOOLEAN.equals(pdt) ) {
         return (Boolean)value;
      }
      else if( DTSPrimitive.DATETIME.equals(pdt) ) {
         return (Date)value;
      }
      else if( DTSPrimitive.DECIMAL.equals(pdt) ) {
         BigDecimal n = (BigDecimal)value;
         return n.doubleValue();
      }
      else if( DTSPrimitive.INTEGER.equals(pdt) ) {
         BigInteger n = (BigInteger)value;
         return n.doubleValue();
      }
      else { // STRING or enum literal
         if( value instanceof EnumerationLiteral )
            return ((EnumerationLiteral)value).getCode();
         return (String)value;
      }
   }

   private Object convertFromNRL( DTSPrimitive pdt, Object value ) {
      if( value == null )
         return null;
      if( DTSPrimitive.BINARY.equals(pdt) ) {
         // TODO
         ByteBuffer bb = ByteBuffer.wrap(null);
         return bb;
      }
      else if( DTSPrimitive.BOOLEAN.equals(pdt) ) {
         return (Boolean)value;
      }
      else if( DTSPrimitive.DATETIME.equals(pdt) ) {
         return (Date)value;
      }
      else if( DTSPrimitive.DECIMAL.equals(pdt) ) {
         return new BigDecimal((Double)value);
      }
      else if( DTSPrimitive.INTEGER.equals(pdt) ) {
         return new BigInteger(((Double)value).toString()); // TODO check
      }
      else { // STRING
         return (String)value;
      }
   }

   @Override
   public boolean evalConstraint( IElementValue context, String rule ) {
      try {
         SemanticElement se = context.getSemanticElement();
         IPackage pa = se.getContainingPackage();

         ModelCollection models = new ModelCollection();
         models.addModelPackage(pa);

         ICompiledConstraintRule r = IRuleFactory.INSTANCE.compileConstraint(rule, se, models, operators());

         IConstraintRuleResult result = r.evaluate(context, this, s_jom);
         return result.hasPassed();
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "NrlAdapter constraint rule {0} evaluation fails!", rule);
      }
   }

   @Override
   public void evalAction( IElementValue context, String rule ) {
      try {
         SemanticElement se = context.getSemanticElement();
         IPackage pa = se.getContainingPackage();
         IPackage da = se.getElementSet().getModel().getGroup();

         ModelCollection models = new ModelCollection();
         models.addModelPackage(pa);
         models.addModelPackage(da);

         ICompiledActionRule r = IRuleFactory.INSTANCE.compileActions(rule, se, models, operators());

         r.execute(context, this, s_jom);
      }
      catch( RuleFactoryException ex ) {
         System.err.println("--- RuleFactoryException ---");
         for( NRLError error : ex.getErrors() ) {
            System.err.println(error.getMessage());
         }
         ex.printStackTrace();
         throw new MdmiException(ex, "NrlAdapter action rule {0} evaluation fails!", rule);
      }
      catch( Exception ex ) {
         ex.printStackTrace();
         throw new MdmiException(ex, "NrlAdapter action rule {0} evaluation fails!", rule);
      }
   }

   @Override
   public List<String> compileAction( SemanticElement se, String rule ) {
      List<String> errors = new ArrayList<String>();

      IPackage modelPackage = se.getContainingPackage();
      String elementName = se.getName();

      ModelCollection models = new ModelCollection();
      models.addModelPackage(modelPackage);

      IClassifier classifier = (IClassifier)models.getElementByName(elementName);

      try {
         IRuleFactory.INSTANCE.compileActions(rule, classifier, models, new IOperators[0]);
      }
      catch( RuleFactoryException e ) {
         List<NRLError> lst = e.getErrors();
         for( int i = 0; i < lst.size(); i++ ) {
            errors.add(lst.get(i).getMessage());
         }
      }
      return errors;
   }

   @Override
   public List<String> compileConstraint( SemanticElement se, String rule ) {
      List<String> errors = new ArrayList<String>();

      IPackage modelPackage = se.getContainingPackage();
      String elementName = se.getName();

      ModelCollection models = new ModelCollection();
      models.addModelPackage(modelPackage);

      IClassifier classifier = (IClassifier)models.getElementByName(elementName);

      try {
         IRuleFactory.INSTANCE.compileConstraint(rule, classifier, models, new IOperators[0]);
      }
      catch( RuleFactoryException e ) {
         List<NRLError> lst = e.getErrors();
         for( int i = 0; i < lst.size(); i++ ) {
            errors.add(lst.get(i).getMessage());
         }
      }
      return errors;
   }

   private IOperators[] operators() {
      if( s_operatorsLoaded )
         return s_operators;

      IOperators operators = null;
      try {
         File ops = new File("operators.xml");
         if( ops.exists() ) {
            XmlOperatorPersistence loader = new XmlOperatorPersistence(
                  JAXBContext.newInstance("net.sourceforge.nrl.parser.jaxb14"),
                  JAXBContext.newInstance("net.sourceforge.nrl.parser.jaxb15"));
            loader.load(new File("operators.xml"));
            operators = loader.getOperators();
         }
      }
      catch( Exception ex ) {
         throw new RuntimeException("Operator loding fails!", ex);
      }

      if( operators == null ) {
         s_operators = new IOperators[0];
      }
      else {
         IOperators[] oplist = new IOperators[1];
         oplist[0] = operators;
         s_jom.addOperators(operators);
      }
      s_operatorsLoaded = true;
      return s_operators;
   }
} // NrlAdapter
