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
package org.openhealthtools.mdht.mdmi.model.validate;

import org.openhealthtools.mdht.mdmi.model.*;

public class SemanticElementValidate implements IModelValidate<SemanticElement> {
   public static final String s_nameField           = "name";
   public static final String s_elemTypeField       = "elementType";
   public static final String s_typeField           = "datatype";
   public static final String s_elemSetField        = "elementSet";
   public static final String s_toMdmiField         = "toMdmi";
   public static final String s_fromMdmiField       = "fromMdmi";
   public static final String s_descName            = "description";
   public static final String s_multInstName        = "multipleInstances";
   public static final String s_orderName           = "ordering";
   public static final String s_orderLangName       = "orderingLanguage";
   public static final String s_compName            = "computedValue";
   public static final String s_compInName          = "computedInValue";
   public static final String s_compOutName         = "computedOutValue";
   public static final String s_compositeName       = "composite";
   public static final String s_nodeName            = "syntaxNode";
   public static final String s_dataRulesName       = "dataRules";
   public static final String s_relateName          = "relationships";
   public static final String s_childName           = "children";
   public static final String s_busRulesName        = "businessRules";
   public static final String s_exprName            = "expression";
   public static final String s_langName            = "language";
   public static final String s_enumValueSetField   = "enumValueSetField";
   public static final String s_enumValueField      = "enumValueField";
   public static final String s_enumValueDescrField = "enumValueDescrField";
   public static final String s_enumValueSet        = "enumValueSet";

   @Override
   public void validate( SemanticElement object, ModelValidationResults results ) {
      if( ValidateHelper.isEmptyField(object.getName()) ) {
         results.addErrorFromRes(object, s_nameField);
      }
      if( object.getElementType() == null ) {
         results.addErrorFromRes(object, s_elemTypeField, object.getName());
      }
      if( object.getDatatype() == null ) {
         results.addErrorFromRes(object, s_typeField, object.getName());
      }
      if( object.getElementSet() == null ) {
         results.addErrorFromRes(object, s_elemSetField, object.getName());
      }
      if( object.getToMdmi().size() == 0 ) {
         results.addErrorFromRes(object, s_toMdmiField, object.getName());
      }
      if( object.getFromMdmi().size() == 0 ) {
         results.addErrorFromRes(object, s_fromMdmiField, object.getName());
      }
      String vsf = object.getEnumValueSetField();
      String vf  = object.getEnumValueField();
      String vdf = object.getEnumValueDescrField();
      String vs  = object.getEnumValueSet();
      if( null != vf && 0 < vf.trim().length() ) {
      	if( null == vs || vs.trim().length() <= 0 )
            results.addErrorFromRes(object, s_enumValueSet, object.getName());
      	if( null == vsf || vsf.trim().length() <= 0 )
            results.addErrorFromRes(object, s_enumValueSetField, object.getName());
      }
      if( null != vs && 0 < vs.trim().length() ) {
      	if( null == vf || vf.trim().length() <= 0 )
            results.addErrorFromRes(object, s_enumValueField, object.getName());
      	if( null == vsf || vsf.trim().length() <= 0 )
            results.addErrorFromRes(object, s_enumValueSetField, object.getName());
      }
      if( null != vsf && 0 < vsf.trim().length() ) {
      	if( null == vf || vf.trim().length() <= 0 )
            results.addErrorFromRes(object, s_enumValueField, object.getName());
      	if( null == vs || vs.trim().length() <= 0 )
            results.addErrorFromRes(object, s_enumValueSet, object.getName());
      }
      if( null != vdf && 0 < vdf.trim().length() ) {
      	if( null == vf || vf.trim().length() <= 0 )
            results.addErrorFromRes(object, s_enumValueField, object.getName());
      	if( null == vs || vs.trim().length() <= 0 )
            results.addErrorFromRes(object, s_enumValueSet, object.getName());
      	if( null == vsf || vsf.trim().length() <= 0 )
            results.addErrorFromRes(object, s_enumValueSetField, object.getName());
      }
   }
}
