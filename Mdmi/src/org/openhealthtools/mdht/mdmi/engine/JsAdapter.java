/*******************************************************************************
 * Copyright (c) 2014 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Firestar Software, Inc. - initial API and implementation
 *     Semantix Software, Inc. - Updated for performance
 *
 * Author:
 *     Wency Chingcuangco
 *
 *******************************************************************************/
package org.openhealthtools.mdht.mdmi.engine;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.openhealthtools.mdht.mdmi.ElementValueSet;
import org.openhealthtools.mdht.mdmi.IElementValue;
import org.openhealthtools.mdht.mdmi.IExpressionInterpreter;
import org.openhealthtools.mdht.mdmi.MdmiException;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;

public class JsAdapter implements IExpressionInterpreter  {
   private ElementValueSet m_eset;
   private String          m_name;
   private XValue          m_value;
   final static ScriptEngineManager factory = new ScriptEngineManager();
   final static ScriptEngine engine = factory.getEngineByName("JavaScript");
   
   final static String PACKAGES = " importPackage(Packages.org.openhealthtools.mdht.mdmi);"
         + "importPackage(Packages.org.openhealthtools.mdht.mdmi.engine);"
         + "importPackage(Packages.org.openhealthtools.mdht.mdmi.model);";
   
   private static Double javaVersion = null;
   private static String loadPrefix = "";
   
   public static void setJavaVersion( Double javaVersion ) {
		JsAdapter.javaVersion = javaVersion;
	}

	public JsAdapter( ElementValueSet eset, String name, XValue value ) {
      initialize(eset, null, name, value);
   }
   
   /* 
    * If the version of java is greater the 1.7, need to add load compatibility 
    * see https://wiki.openjdk.java.net/display/Nashorn/Rhino+Migration+Guide
    * 
    * (non-Javadoc)
    * @see org.openhealthtools.mdht.mdmi.IExpressionInterpreter#initialize(org.openhealthtools.mdht.mdmi.ElementValueSet, org.openhealthtools.mdht.mdmi.engine.XElementValue, java.lang.String, org.openhealthtools.mdht.mdmi.engine.XValue)
    */
   @Override
   public void initialize( ElementValueSet eset, XElementValue context, String name, XValue value ) {
      m_eset = eset;
      m_name = name;
      m_value = value;
      if (javaVersion == null) {
      	javaVersion = Double.parseDouble(System.getProperty("java.specification.version"));
      	if (javaVersion > 1.7) {
      		loadPrefix = "load(\"nashorn:mozilla_compat.js\");";
      	}
      }
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
         throw new MdmiException(lex.get(0), "evalAction({0}, {1}) fails!", context.toString(), rule);
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
      	//if( context.getSemanticElement().getName().equalsIgnoreCase("MedicationEnteringOrganizationIdentifier") ) {
      		//test((XElementValue)context, m_value);
      	//}
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
   
   private void test( XElementValue value, XValue xv ) {
   	XElementValue key = value.getRelation("Key");
   	if( null != key && key.value().equals("Fill") ) {
   	   XDataStruct dt = (XDataStruct)xv.getValue();
   	   dt.setValue("assigningAuthorityName", value.value());
   	}
   	
   	//value.setValue(v);
   	//XElementValue key = value.getRelation("Key");
   	//if( null == key ) {
   	//   SemanticElement se = value.getSemanticElement();
   	//   SemanticElementRelationship re = se.getRelationshipByName("Key");
   	//   SemanticElement te = re.getRelatedSemanticElement();
   	//   XElementValue owner = (XElementValue)value.getParent();
   	//   key = new XElementValue(te, value.getOwner());
   	//   owner.addChild(key);
   	//}
   	//key.setValue("Claim");
   }

   private String addPackages( String rule ) {
      return loadPrefix + PACKAGES + rule;
   }
} // JsAdapter
