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

public class NodeValidate<T extends Node> implements IModelValidate<T> {
   public static final String s_nameField                 = "name";
   public static final String s_locationField             = "location";
   public static final String s_syntaxModelField          = "syntaxModel";
   public static final String s_descName                  = "description";
   public static final String s_minName                   = "minOccurs";
   public static final String s_maxName                   = "maxOccurs";
   public static final String s_locExprLangName           = "locationExpressionLanguage";
   public static final String s_fieldName                 = "fieldName";
   public static final String s_semElemName               = "semanticElement";
   // These don't correspond to fields.
   public static final String s_noAncestor                = "noAncestorElement";
   public static final String s_noAncestorType            = "noAncestorElemType";
   public static final String s_ancestorFieldMismatch     = "ancestorElemFieldMismatch";
   public static final String s_ancestorFieldTypeMismatch = "ancestorElemFieldTypeMismatch";
   public static final String s_fieldReconcile            = "fieldReconcile";
   public static final String s_fieldTypeReconcile        = "fieldTypeReconcile";
   public static final String s_nodeTypeLeaf              = "nodeTypeMismatchLeaf";
   public static final String s_nodeTypeChoice            = "nodeTypeMismatchChoice";
   public static final String s_nodeTypeBag               = "nodeTypeMismatchBag";
   public static final String s_typeMappingLeaf           = "typeMappingMismatchLeaf";
   public static final String s_typeMappingChoice         = "typeMappingMismatchChoice";
   public static final String s_typeMappingBag            = "typeMappingMismatchBag";
   public static final String s_unmapped                  = "unmapped";

   @Override
   public void validate( T object, ModelValidationResults results ) {
      // Empty Field errors
      if( ValidateHelper.isEmptyField(object.getName()) ) {
         results.addErrorFromRes(Node.class.getSimpleName(), object, s_nameField);
      }
      if( ValidateHelper.isEmptyField(object.getLocation()) ) {
         results.addErrorFromRes(Node.class.getSimpleName(), object, s_locationField, object.getName());
      }
      if( object.getSyntaxModel() == null ) {
         results.addErrorFromRes(Node.class.getSimpleName(), object, s_syntaxModelField, object.getName());
      }

      if( object.getSemanticElement() != null ) {
         validateSemanticElementMatch(object, object.getSemanticElement(), results);
      }

      if( !ValidateHelper.isEmptyField(object.getFieldName()) ) {
         validateFieldName(object, object.getFieldName(), results);
      }

      // Warnings
      if( !nodeIsMapped(object) ) {
         Node parent = object.getParentNode();
         if( parent != null && nodeOrAncestorIsMapped(parent) ) {
            // Add warning.
            results.addWarningFromRes(Node.class.getSimpleName(), object, s_unmapped, object.getName());
         }
      }
   }

   private void validateFieldName( Node object, String fieldName, ModelValidationResults results ) {
      List<Node> ancestors = getAncestorsUpToElementMapping(object);
      if( ancestors.size() == 0 ) {
         results.addErrorFromRes(Node.class.getSimpleName(), object, s_noAncestor, object.getName(), fieldName);
         return;
      }

      // Walk back down from the ancestor to the node we are validating,
      // making sure that all field names can be resolved along the way.
      Iterator<Node> iterAncestors = ancestors.iterator();
      MdmiDatatype curType = iterAncestors.next().getSemanticElement().getDatatype();
      if( curType == null ) {
         results.addErrorFromRes(Node.class.getSimpleName(), object, s_noAncestorType, object.getName(), fieldName);
         return;
      }

      while( iterAncestors.hasNext() ) {
         String curName = iterAncestors.next().getFieldName();
         Field field = getField(curType, curName);

         // Make sure a field by this name exists.
         if( field == null ) {
            results.addErrorFromRes(Node.class.getSimpleName(), object, s_ancestorFieldMismatch, object.getName(),
                  fieldName, curName, curType.getTypeName());
            return;
         }

         // Make sure the field has a type.
         curType = field.getDatatype();
         if( curType == null ) {
            results.addErrorFromRes(Node.class.getSimpleName(), object, s_ancestorFieldTypeMismatch, object.getName(),
                  fieldName, curName);
            return;
         }
      }

      // Finally, check the type of the field name passed in.
      Field field = getField(curType, fieldName);
      if( field == null ) {
         results.addErrorFromRes(Node.class.getSimpleName(), object, s_fieldReconcile, object.getName(), fieldName,
               curType.getTypeName());
         return;
      }

      curType = field.getDatatype();
      if( curType == null ) {
         results.addErrorFromRes(Node.class.getSimpleName(), object, s_fieldTypeReconcile, object.getName(), fieldName);
         return;
      }

      if( curType instanceof DTSimple ) {
         if( !(object instanceof LeafSyntaxTranslator) ) {
            results.addErrorFromRes(Node.class.getSimpleName(), object, s_nodeTypeLeaf, fieldName, object.getName(),
                  object.getClass().getSimpleName());
         }
      }
      else if( curType instanceof DTCChoice ) {
         if( !(object instanceof Choice) ) {
            results.addErrorFromRes(Node.class.getSimpleName(), object, s_nodeTypeChoice, fieldName, object.getName(),
                  object.getClass().getSimpleName());
         }
      }
      else if( curType instanceof DTCStructured ) {
         if( !(object instanceof Bag) ) {
            results.addErrorFromRes(Node.class.getSimpleName(), object, s_nodeTypeBag, fieldName, object.getName(),
                  object.getClass().getSimpleName());
         }
      }
   }

   private Field getField( MdmiDatatype curType, String fieldName ) {
      if( !(curType instanceof DTComplex) ) {
         // Data type is not complex, so it has
         // no fields.
         return null;
      }

      Field field = ((DTComplex)curType).getField(fieldName);
      return field;
   }

   private void validateSemanticElementMatch( Node object, SemanticElement semanticElement,
         ModelValidationResults results ) {
      // If there is no data type, we can't perform this validation.
      // This will be caught by the semantic element validation.
      MdmiDatatype datatype = semanticElement.getDatatype();
      if( datatype == null ) {
         return;
      }

      if( datatype instanceof DTSimple ) {
         if( !(object instanceof LeafSyntaxTranslator) ) {
            results.addErrorFromRes(Node.class.getSimpleName(), object, s_typeMappingLeaf, semanticElement.getName(),
                  object.getName(), object.getClass().getSimpleName());
         }
      }
      else if( datatype instanceof DTCChoice ) {
         if( !(object instanceof Choice) ) {
            results.addErrorFromRes(Node.class.getSimpleName(), object, s_typeMappingChoice, semanticElement.getName(),
                  object.getName(), object.getClass().getSimpleName());
         }
      }
      else if( datatype instanceof DTCStructured ) {
         if( !(object instanceof Bag) ) {
            results.addErrorFromRes(Node.class.getSimpleName(), object, s_typeMappingBag, semanticElement.getName(),
                  object.getName(), object.getClass().getSimpleName());
         }
      }
   }

   /**
    * Navigates up the node hierarchy until it finds the first ancestor with a semantic element mapping. Returns a list,
    * top->down of all nodes along this path. The node passed in is NOT included in this list.
    * 
    * @param node
    * @return
    */
   private List<Node> getAncestorsUpToElementMapping( Node node ) {
      LinkedList<Node> rv = new LinkedList<Node>();

      Node curAncestor = node.getParentNode();
      while( curAncestor != null ) {
         rv.addFirst(curAncestor);

         if( curAncestor.getSemanticElement() != null ) {
            // We found a node with a semantic element mapping.
            // No need to go any further.
            break;
         }

         curAncestor = curAncestor.getParentNode();
      }

      // If we never found one, return an empty list.
      if( curAncestor == null ) {
         rv.clear();
      }
      return rv;
   }

   private boolean nodeIsMapped( Node node ) {
      return node.getSemanticElement() != null || !ValidateHelper.isEmptyField(node.getFieldName());
   }

   private boolean nodeOrAncestorIsMapped( Node node ) {
      if( nodeIsMapped(node) ) {
         return true;
      }
      return node.getParentNode() == null ? false : nodeOrAncestorIsMapped(node.getParentNode());
   }
}
