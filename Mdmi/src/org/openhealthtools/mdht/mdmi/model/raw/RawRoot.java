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
package org.openhealthtools.mdht.mdmi.model.raw;

import java.util.*;

public class RawRoot {
   private ModelRoot                           m_model;
   private HashMap<String, StereotypeInstance> m_stereotypeInstances = new HashMap<String, StereotypeInstance>();

   public void setModel( ModelRoot model ) {
      m_model = model;
   }

   public void addStereotypeInstance( StereotypeInstance instance ) {
      m_stereotypeInstances.put(instance.getBaseRef(), instance);
   }

   public Collection<StereotypeInstance> getStereotypeInstances() {
      return m_stereotypeInstances.values();
   }

   public StereotypeInstance getStereotypeInstance( String classRef ) {
      return m_stereotypeInstances.get(classRef);
   }

   public ModelRoot getModel() {
      return m_model;
   }
}
