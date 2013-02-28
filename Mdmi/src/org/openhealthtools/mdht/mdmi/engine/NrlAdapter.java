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

import net.sourceforge.nrl.parser.NRLError;
import org.openhealthtools.mdht.mdmi.ElementValueSet;
import org.openhealthtools.mdht.mdmi.IElementValue;
import org.openhealthtools.mdht.mdmi.IExpressionInterpreter;
import org.openhealthtools.mdht.mdmi.MdmiException;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.util.JarClassLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NrlAdapter implements IExpressionInterpreter {
   IExpressionInterpreter m_adapter = null;

   public NrlAdapter() {
      tryInitAdapter();
   }

   public NrlAdapter( ElementValueSet eset, XElementValue context, String name, XValue value ) {
      tryInitAdapter();
      try {
         initialize(eset, context, name, value);
      }
      catch( Exception ex ) {
      }
   }

   private void tryInitAdapter() {
      try {
         File i = new File("NRL/nrl-interpreter.jar");
         File f = new File("nrl-adapter.jar");
          if( !i.exists() || !f.exists() ) {
              i = new File("./bin/NRL/nrl-interpreter.jar");
              f = new File("./bin/nrl-adapter.jar");
          }
         if( !i.exists() || !f.exists() ) {
            i = new File("../bin/NRL/nrl-interpreter.jar");
            f = new File("../bin/nrl-adapter.jar");
         }
         if( !i.exists() || !f.exists() )
            return;
         JarClassLoader jl = new JarClassLoader(i);
         jl.addJarFile(f);
         Class<?> c = jl.findClass("com.whitestar.mdmi.NrlAdapter");
         Object o = c.newInstance();
         m_adapter = (IExpressionInterpreter)o;
      }
      catch( Exception ex ) {
         System.out.println(ex.getMessage());
      }
   }
   
   public void initialize( ElementValueSet eset, XElementValue context, String name, XValue value ) {
      if( m_adapter != null )
         m_adapter.initialize(eset, context, name, value);
   }

   public boolean evalConstraint( IElementValue context, String rule ) {
      if( m_adapter != null && !rule.isEmpty() )
         return m_adapter.evalConstraint(context, rule);
      else if( !rule.isEmpty() )
         throw new MdmiException("The open source version does not support the use of NRL rules.");

      return true;
   }

   public void evalAction( IElementValue context, String rule ) {
      if( m_adapter != null && !rule.isEmpty() )
         m_adapter.evalAction(context, rule);
      else if( !rule.isEmpty() )
         throw new MdmiException("The open source version does not support the use of NRL rules.");
   }

   public List<NRLError> compileConstraint( SemanticElement se, String rule ) {
      if( m_adapter != null && !rule.isEmpty() )
         return m_adapter.compileConstraint(se, rule);
      else if( !rule.isEmpty() )
         throw new MdmiException("The open source version does not support the use of NRL rules.");

      return new ArrayList<NRLError>();
   }

   public List<NRLError> compileAction( SemanticElement se, String rule ) {
      if( m_adapter != null && !rule.isEmpty() )
         return m_adapter.compileAction(se, rule);
      else if( !rule.isEmpty() )
         throw new MdmiException("The open source version does not support the use of NRL rules.");

      return new ArrayList<NRLError>();
   }
} // NrlAdapter
