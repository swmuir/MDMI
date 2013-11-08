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
 *     Wency Chingcuangco
 *
 *******************************************************************************/
package org.openhealthtools.mdht.mdmi.engine;

import java.util.*;
import javax.script.*;

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.model.*;

public class JsAdapter implements IExpressionInterpreter  {
   private ElementValueSet m_eset;
   private String          m_name;
   private XValue          m_value;
   
   public JsAdapter( ElementValueSet eset, String name, XValue value ) {
      initialize(eset, null, name, value);
   }
   
   @Override
   public void initialize( ElementValueSet eset, XElementValue context, String name, XValue value ) {
      m_eset = eset;
      m_name = name;
      m_value = value;
   }

   @Override
   public boolean evalConstraint( IElementValue context, String rule ) {
      ArrayList<Exception> lex = new ArrayList<Exception>();
      boolean ret = eval(context, rule, lex);
      if( 0 < lex.size() )
         throw new MdmiException(lex.get(0), "evalConstraint({0}, {1}) fails!", context.toString(), rule);
      return ret;
   }

   @Override
   public void evalAction( IElementValue context, String rule ) {
      ArrayList<Exception> lex = new ArrayList<Exception>();
      eval(context, rule, lex);
      if( 0 < lex.size() )
         throw new MdmiException(lex.get(0), "evalConstraint({0}, {1}) fails!", context.toString(), rule);
   }

   @Override
   public List<String> compileAction( SemanticElement se, String rule ) {
      return compile(se, rule);
   }

   @Override
   public List<String> compileConstraint( SemanticElement se, String rule ) {
      return compile(se, rule);
   }

   private List<String> compile( SemanticElement se, String rule ) {
      ArrayList<String> ler = new ArrayList<String>();
      ArrayList<Exception> lex = new ArrayList<Exception>();
      IElementValue context = new XElementValue(se, m_eset == null ? new ElementValueSet() : m_eset);
      eval(context, rule, lex);
      for( Exception ex : lex ) {
         ler.add(ex.getMessage());
      }
      return ler;
   }
   
   private boolean eval( IElementValue context, String rule, ArrayList<Exception> lex ) {
      rule = addPackages(rule);
      boolean returnValue = false;
      try {
         ScriptEngineManager factory = new ScriptEngineManager();
         ScriptEngine engine = factory.getEngineByName("JavaScript");
         engine.put("value", context);
         if( null != m_name && 0 < m_name.length() )
            engine.put(m_name, m_value);
         engine.put("returnValue", returnValue);
         engine.eval(rule);
      }
      catch( Exception ex ) {
         lex.add(ex);
      }
      return returnValue;
   }

   private String addPackages( String rule ) {
      String packages = "importPackage(Packages.org.openhealthtools.mdht.mdmi);"
            + "importPackage(Packages.org.openhealthtools.mdht.mdmi.engine);"
            + "importPackage(Packages.org.openhealthtools.mdht.mdmi.model);";
      return packages + rule;
   }
} // JsAdapter
