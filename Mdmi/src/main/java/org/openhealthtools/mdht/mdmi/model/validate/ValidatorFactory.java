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

import java.util.*;

import org.openhealthtools.mdht.mdmi.model.*;

public class ValidatorFactory {
   @SuppressWarnings( "rawtypes" )
   private static final HashMap<String, IModelValidate> s_validators = new HashMap<String, IModelValidate>();
   private static final ValidatorFactory                s_instance   = new ValidatorFactory();

   private ValidatorFactory() {
      s_validators.put(MessageGroup.class.getName(), new MessageGroupValidate());
      s_validators.put(MdmiDomainDictionaryReference.class.getName(), new DomainDictionaryRefValidate());
      s_validators.put(MdmiBusinessElementReference.class.getName(), new BusinessElemRefValidate());
      s_validators.put(MdmiBusinessElementRule.class.getName(), new BusinessElemRuleValidate());
      s_validators.put(MessageModel.class.getName(), new MessageModelValidate());
      s_validators.put(MessageSyntaxModel.class.getName(), new MessageSyntaxModelValidate());
      s_validators.put(Bag.class.getName(), new BagValidate());
      s_validators.put(Choice.class.getName(), new ChoiceValidate());
      s_validators.put(LeafSyntaxTranslator.class.getName(), new LeafSyntaxValidate());
      s_validators.put(SimpleMessageComposite.class.getName(), new SimpleMsgCompositeValidate());
      s_validators.put(MessageComposite.class.getName(), new SimpleMsgCompositeValidate());
      s_validators.put(SemanticElementSet.class.getName(), new SemanticElementSetValidate());
      s_validators.put(DataRule.class.getName(), new DataRuleValidate());
      s_validators.put(SemanticElementBusinessRule.class.getName(), new SemanticElemBusRuleValidate());
      s_validators.put(ToBusinessElement.class.getName(), new ToBusinessElemValidate());
      s_validators.put(ToMessageElement.class.getName(), new ToMessageElemValidate());
      s_validators.put(SemanticElementRelationship.class.getName(), new SemanticElemRelateValidate());
      s_validators.put(SemanticElement.class.getName(), new SemanticElementValidate());
      s_validators.put(DTSPrimitive.class.getName(), new PrimitiveDatatypeValidate());
      s_validators.put(DTSDerived.class.getName(), new DerivedDatatypeValidate());
      s_validators.put(DTSEnumerated.class.getName(), new EnumeratedDatatypeValidate());
      s_validators.put(DTExternal.class.getName(), new ExternalDatatypeValidate());
      s_validators.put(DTCChoice.class.getName(), new ComplexDatatypeValidate<DTCChoice>());
      s_validators.put(DTCStructured.class.getName(), new ComplexDatatypeValidate<DTCStructured>());
   }

   public static ValidatorFactory getInstance() {
      return s_instance;
   }

   @SuppressWarnings( "rawtypes" )
   public IModelValidate getValidator( Object obj ) {
      return s_validators.get(obj.getClass().getName());
   }
}
