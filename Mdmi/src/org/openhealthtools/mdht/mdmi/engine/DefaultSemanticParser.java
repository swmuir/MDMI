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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.openhealthtools.mdht.mdmi.ElementValueSet;
import org.openhealthtools.mdht.mdmi.IElementValue;
import org.openhealthtools.mdht.mdmi.IExpressionInterpreter;
import org.openhealthtools.mdht.mdmi.ISemanticParser;
import org.openhealthtools.mdht.mdmi.ISyntaxNode;
import org.openhealthtools.mdht.mdmi.Mdmi;
import org.openhealthtools.mdht.mdmi.MdmiException;
import org.openhealthtools.mdht.mdmi.model.Bag;
import org.openhealthtools.mdht.mdmi.model.Choice;
import org.openhealthtools.mdht.mdmi.model.DTCChoice;
import org.openhealthtools.mdht.mdmi.model.DTCStructured;
import org.openhealthtools.mdht.mdmi.model.DTExternal;
import org.openhealthtools.mdht.mdmi.model.DTSDerived;
import org.openhealthtools.mdht.mdmi.model.DTSEnumerated;
import org.openhealthtools.mdht.mdmi.model.DTSPrimitive;
import org.openhealthtools.mdht.mdmi.model.EnumerationLiteral;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.LeafSyntaxTranslator;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MessageModel;
import org.openhealthtools.mdht.mdmi.model.Node;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.SemanticElementRelationship;
import org.openhealthtools.mdht.mdmi.model.SemanticElementSet;
import org.openhealthtools.mdht.mdmi.model.ToMessageElement;

public class DefaultSemanticParser implements ISemanticParser {

	private ElementValueSet valueSet;

	HashMap<String, String> nullFlavorMethods = null;
	HashMap<String, String> nullFlavorValues = null;

	private void loadNullFlavors() {
		ObjectMapper mapper = new ObjectMapper();

		try {
			nullFlavorMethods = mapper.readValue(new File("nullflavormethods.json"), new TypeReference<HashMap<String, String>>() {
			});

			nullFlavorValues = mapper.readValue(new File("nullflavorvalues.json"), new TypeReference<HashMap<String, String>>() {
			});

		}
		catch( Exception e ) {
			// Continue to process as the run time does not necessarily require the null flavor files
			nullFlavorMethods = new HashMap<String, String>();
			nullFlavorValues = new HashMap<String, String>();
		}
	}

	@Override
	public void buildSemanticModel( MessageModel mdl, ISyntaxNode yroot, ElementValueSet eset, boolean nullFlavors ) {
		if( mdl == null || yroot == null || eset == null ) {
			throw new IllegalArgumentException("Null argument!");
		}
		valueSet = eset;
		Node root = mdl.getSyntaxModel().getRoot();
		if( root != yroot.getNode() ) {
			throw new MdmiException("Invalid root node, expected {0}, found {1}.", root.getName(), (yroot.getNode() == null ? "null" : yroot.getNode().getName()));
		}

		// Need to do this only in debug ui
		loadNullFlavors();

		// 1. create all XElementValues
		getElements((YNode) yroot);

		// 2. set relationships
		ArrayList<IElementValue> xes = valueSet.getAllElementValues();
		for( IElementValue xe : xes ) {
			setRelations((XElementValue) xe);
		}

		// 3. set computed SEs
		SemanticElementSet set = mdl.getElementSet();
		Collection<SemanticElement> ses = set.getSemanticElements();
		for( SemanticElement se : ses ) {
			if( se.isComputed() ) {
				setComputedValue(se);
			}
			else if( se.isComputedOut() ) {
				setComputedOutValue(se);
			}
			else if( nullFlavors ) {
				setNullFlavorOut(se);
			}

		}
	}

	@Override
	public ISyntaxNode createNewSyntacticModel( MessageModel mdl, ElementValueSet eset ) {
		Node root = mdl.getSyntaxModel().getRoot();
		if( root.getMinOccurs() != 1 || root.getMaxOccurs() != 1 ) {
			throw new MdmiException("Invalid mapping for node " + DefaultSyntacticParser.getNodePath(root));
		}
		YNode yroot = createYNode(root);
		updateSyntacticModel(mdl, eset, yroot);
		return yroot;
	}

	@Override
	public void updateSyntacticModel( MessageModel mdl, ElementValueSet eset, ISyntaxNode yr ) {
		if( mdl == null || eset == null || yr == null ) {
			throw new IllegalArgumentException("Null argument!");
		}
		YNode yroot = (YNode) yr;

		// 1. set computed SEs
		SemanticElementSet set = mdl.getElementSet();
		Collection<SemanticElement> ses = set.getSemanticElements();
		for( SemanticElement se : ses ) {
			if( se.isComputedIn() ) {
				setComputedInValue(se);
			}
		}

		// 2. Update the syntax model
		Node root = mdl.getSyntaxModel().getRoot();
		if( root.getMinOccurs() != 1 || root.getMaxOccurs() != 1 ) {
			throw new MdmiException("Invalid mapping for node " + DefaultSyntacticParser.getNodePath(root));
		}
		XElementValues values = new XElementValues(eset);
		// top level XElementValues
		for( XElementValues.XES xes : values.elementValues ) {
			YNode ynode = ensureAbsolutePath(yroot, xes.semanticElement);
			ArrayList<YNode> ynodes = new ArrayList<YNode>();
			int n = xes.elementValues.size();
			if( n == 1 ) {
				ynodes.add(ynode);
			}
			else {
				YNode parent = (YNode) ynode.getParent();
				if( parent != null ) {
					ynodes = ensureParentHasChildren(parent, ynode.getNode(), n);
				}
			}
			for( int i = 0; i < n; i++ ) {
				XElementValues.XE xe = xes.elementValues.get(i);
				ynode = ynodes.get(i);
				setYNodeValuesAndChildren(ynode, xe);
			}
		}
	}

	private void getElements( YNode yroot ) {
		Node node = yroot.getNode();
		System.out.println("AAAAAAAAA"+node.getName());
		SemanticElement me = node.getSemanticElement();
		if( me == null ) {
			// node is not mapped to an element or field (one of the top nodes)
			if( yroot instanceof YBag ) {
				YBag y = (YBag) yroot;
				ArrayList<YNode> yns = y.getYNodes();
				for( YNode yn : yns ) {
					getElements(yn);
				}
			}
			else if( yroot instanceof YChoice ) {
				YChoice y = (YChoice) yroot;
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
			getSimpleElement((YLeaf) yroot, owner);
		}
		else if( yroot.isChoice() ) {
			getChoiceElement((YChoice) yroot, owner);
		}
		else if( yroot.isBag() ) { // Bag
			getStructElement((YBag) yroot, owner);
		}
		else {
			throw new IllegalArgumentException("Invalid YNode!");
		}
	}

	// a leaf mapped to a simple type (must be simple)
	private void getSimpleElement( YLeaf yleaf, XElementValue owner ) {
		SemanticElement me = yleaf.getLeaf().getSemanticElement();
		if( me == null ) {
			throw new MdmiException("Null SE for node {0}", DefaultSyntacticParser.getNodePath(yleaf.getNode()));
		}
		XElementValue xe = new XElementValue(me, valueSet);
		if( owner != null ) {
			// set parent-child relationship
			owner.addChild(xe);
		}

		// set the value
		MdmiDatatype dt = me.getDatatype();
		if( !(dt.isSimple() || dt.isExternal()) ) {
			throw new MdmiException("Invalid mapping for node " + DefaultSyntacticParser.getNodePath(yleaf.getNode()));
		}

		try {
			String value = yleaf.getValue();
			String format = yleaf.getLeaf().getFormat();
			XDT xdt = XDT.fromString(format);
			if( dt.isPrimitive() ) {
				DTSPrimitive pdt = (DTSPrimitive) dt;
				if( xdt == null ) {
					xdt = XDT.fromPDT(pdt);
				}
				Object o = XDT.convertFromString(xdt, value, format, pdt);
				xe.getXValue().addValue(o);
			}
			else if( dt.isDerived() ) {
				DTSDerived ddt = (DTSDerived) dt;
				DTSPrimitive pdt = ddt.getPrimitiveBaseType();
				if( xdt == null ) {
					xdt = XDT.fromPDT(pdt);
				}
				Object o = XDT.convertFromString(xdt, value, format, pdt);
				xe.getXValue().addValue(o);
			}
			else if( dt.isExternal() ) {
				DTExternal dte = (DTExternal) dt;
				URI uri = dte.getTypeSpec();
				if( uri != null ) {
					Object o = Mdmi.INSTANCE.getExternalResolvers().getDictionaryValue(dte, value);
					xe.getXValue().addValue(o);
				}
				else {
					DTSPrimitive pdt = DTSPrimitive.STRING;
					if( xdt == null ) {
						xdt = XDT.fromPDT(pdt);
					}
					Object o = XDT.convertFromString(xdt, value, format, pdt);
					xe.getXValue().addValue(o);
				}
			}
			else {
				DTSEnumerated edt = (DTSEnumerated) dt;
				EnumerationLiteral el = edt.getLiteralByCode(value);
				xe.getXValue().addValue(el);
			}
		}
		catch( Throwable throwable ) {
			throw new MdmiException("Error proccessing node " + DefaultSyntacticParser.getNodePath(yleaf.getNode()), throwable);
		}
	}

	private void getChoiceElement( YChoice ychoice, XElementValue owner ) {
		SemanticElement me = ychoice.getChoice().getSemanticElement();
		if( me == null ) {
			throw new IllegalArgumentException("Missing Semantic Elements for " + DefaultSyntacticParser.getNodePath(ychoice.getNode()));
		}
		XElementValue xe = new XElementValue(me, valueSet);
		if( owner != null ) {
			// set parent-child relationship
			owner.addChild(xe);
		}

		// set the value
		MdmiDatatype dt = me.getDatatype();
		if( !dt.isChoice() ) {
			throw new MdmiException("Invalid mapping for node {0}, expected datatype choice, found {1}.", DefaultSyntacticParser.getNodePath(ychoice.getNode()),
			      dt.toString());
		}

		try {
			XDataChoice xc = new XDataChoice(xe.getXValue());
			xe.getXValue().addValue(xc);

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
					XValue xv = xc.setXValue(fieldName);
					for( YNode yn : yns ) {
						getValue(yn, xv, xe);
					}
				}
			}
		}
		catch( Throwable throwable ) {
			throw new MdmiException("Error proccessing node " + DefaultSyntacticParser.getNodePath(ychoice.getNode()), throwable);
		}
	}

	private void getStructElement( YBag ybag, XElementValue owner ) {
		
		if (owner != null && owner.getSemanticElement() != null) {
			System.out.println(" ------------>>>>>>> "+  owner.getSemanticElement().getName());
		} else {
			System.out.println("null");
		}
	
		SemanticElement me = ybag.getBag().getSemanticElement();
		if( me == null ) {
			throw new IllegalArgumentException("Missing Semantic Elements for " + DefaultSyntacticParser.getNodePath(ybag.getNode()));
		}
		XElementValue xe = new XElementValue(me, valueSet);
		if( owner != null ) {
			// set parent-child relationship
			owner.addChild(xe);
		}

		// set the value
		MdmiDatatype dt = me.getDatatype();
		if( dt == null || !dt.isStruct() ) {
			throw new MdmiException("Invalid mapping for node " + DefaultSyntacticParser.getNodePath(ybag.getNode()));
		}

		try {
			XDataStruct xs = new XDataStruct(xe.getXValue());
			xe.getXValue().addValue(xs);

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
					if( fieldName == null ) {
						throw new MdmiException("Field Name is Null " + DefaultSyntacticParser.getNodePath(n));
					}
					XValue xv = xs.getXValue(fieldName);
					if( xv == null ) {
						throw new MdmiException("Invalid mapping for node " + DefaultSyntacticParser.getNodePath(ybag.getNode()));
					}
					getValue(yn, xv, xe);
				}
			}

		}
		catch( Throwable throwable ) {
			throw new MdmiException("Error proccessing node " + DefaultSyntacticParser.getNodePath(ybag.getNode()), throwable);
		}
	}

	private void getValue( YNode yroot, XValue xv, XElementValue owner ) {
		if( yroot.isLeaf() ) {
			getValueLeaf((YLeaf) yroot, xv);
		}
		else if( yroot.isChoice() ) {
			getValueChoice((YChoice) yroot, xv, owner);
		}
		else if( yroot.isBag() ) { // Bag
			getValueStruct((YBag) yroot, xv, owner);
		}
		else {
			throw new IllegalArgumentException("Invalid YNode!");
		}
	}

	private void getValueLeaf( YLeaf yleaf, XValue xv ) {
		MdmiDatatype dt = xv.getDatatype();
		if( !(dt.isSimple() || dt.isExternal()) ) {
			throw new MdmiException("Invalid mapping for node " + DefaultSyntacticParser.getNodePath(yleaf.getNode()));
		}

		try {
			String value = yleaf.getValue();
			String format = yleaf.getLeaf().getFormat();
			XDT xdt = XDT.fromString(format);
			if( xdt != null ) {
				format = null; // null the format so it does not get into the
				// conversions
			}
			if( dt.isPrimitive() ) {
				DTSPrimitive pdt = (DTSPrimitive) dt;
				if( xdt == null ) {
					xdt = XDT.fromPDT(pdt);
				}
				Object o = XDT.convertFromString(xdt, value, format, pdt);
				xv.addValue(o);
			}
			else if( dt.isDerived() ) {
				DTSDerived ddt = (DTSDerived) dt;
				DTSPrimitive pdt = ddt.getPrimitiveBaseType();
				if( xdt == null ) {
					xdt = XDT.fromPDT(pdt);
				}
				Object o = XDT.convertFromString(xdt, value, format, pdt);
				xv.addValue(o);
			}
			else if( dt.isExternal() ) {
				DTExternal dte = (DTExternal) dt;
				URI uri = dte.getTypeSpec();
				if( uri != null ) {
					Object o = Mdmi.INSTANCE.getExternalResolvers().getDictionaryValue(dte, value);
					xv.addValue(o);
				}
				else {
					DTSPrimitive pdt = DTSPrimitive.STRING;
					if( xdt == null ) {
						xdt = XDT.fromPDT(pdt);
					}
					Object o = XDT.convertFromString(xdt, value, format, pdt);
					xv.addValue(o);
				}
			}
			else {
				DTSEnumerated edt = (DTSEnumerated) dt;
				EnumerationLiteral el = edt.getLiteralByCode(value);
				xv.addValue(el);
			}
		}
		catch( Throwable throwable ) {
			throw new MdmiException("Error proccessing node " + DefaultSyntacticParser.getNodePath(yleaf.getNode()), throwable);
		}
	}

	private void getValueChoice( YChoice ychoice, XValue xv, XElementValue owner ) {
		MdmiDatatype dt = xv.getDatatype();
		if( !dt.isChoice() ) {
			throw new MdmiException("Invalid mapping for node " + DefaultSyntacticParser.getNodePath(ychoice.getNode()));
		}
		XDataChoice xc = new XDataChoice(xv);
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
				XValue xvalue = xc.setXValue(fieldName);
				for( YNode yn : yns ) {
					getValue(yn, xvalue, owner);
				}
			}
		}
	}

	private void getValueStruct( YBag ybag, XValue xv, XElementValue owner ) {
		MdmiDatatype dt = xv.getDatatype();
		if( !dt.isStruct() ) {
			throw new MdmiException("Invalid mapping for node " + DefaultSyntacticParser.getNodePath(ybag.getNode()));
		}
		XDataStruct xs = new XDataStruct(xv);
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
				XValue xvalue = xs.getXValue(fieldName);
				if( xvalue == null ) {
					throw new MdmiException("Invalid mapping for node " + DefaultSyntacticParser.getNodePath(ybag.getNode()));
				}
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
	// create top-level root node
	private YNode createYNode( Node node ) {
		if( node instanceof Bag ) {
			return new YBag((Bag) node, null);
		}
		if( node instanceof Choice ) {
			return new YChoice((Choice) node, null);
		}
		return new YLeaf((LeafSyntaxTranslator) node, null);
	}

	/**
	 * Set the value(s) and then the children recursively for the specified YNode
	 * from the given element value wrapper.
	 * 
	 * @param ynode
	 * @param xe
	 */
	private void setYNodeValuesAndChildren( YNode ynode, XElementValues.XE xe ) {
		// 1. set the value of the ynode first
		Object value = xe.elementValue.getXValue().getValue();
		setYNodeValues(ynode, value);

		// 2. recursively go through its child nodes and set the values
		for( XElementValues.XES xes : xe.children ) {
			YNode ychild = ensureRelativePath(ynode, xes.semanticElement);
			ArrayList<YNode> ynodes = new ArrayList<YNode>();
			int n = xes.elementValues.size();
			if( n == 1 ) {
				ynodes.add(ychild);
			}
			else {
				ynodes = ensureParentHasChildren(ynode, ychild.getNode(), n);
			}
			for( int i = 0; i < n; i++ ) {
				XElementValues.XE xeChild = xes.elementValues.get(i);
				ychild = ynodes.get(i);
				setYNodeValuesAndChildren(ychild, xeChild);
			}
		}
	}

	// set the node value(s) for all fields
	private void setYNodeValues( YNode ynode, Object value ) {

		if( value == null ) {
			return;
		}

		Node node = ynode.getNode();
		if( node instanceof Bag ) {
			if( !(value instanceof XDataStruct) ) {
				throw new IllegalArgumentException("Invalid map: expected XDataStruct for node " + ynode.getNode().getName() + " : "
				      + DefaultSyntacticParser.getNodePath(ynode.getNode()));
			}
			setYNodeValuesForBag((YBag) ynode, (XDataStruct) value);
		}
		else if( node instanceof Choice ) {
			if( !(value instanceof XDataChoice) ) {
				throw new IllegalArgumentException("Invalid map: expected XDataChoice for node " + ynode.getNode().getName() + " : "
				      + DefaultSyntacticParser.getNodePath(ynode.getNode()));
			}
			setYNodeValuesForChoice((YChoice) ynode, (XDataChoice) value);
		}
		else { // Leaf
			setLeafValue((YLeaf) ynode, value);
		}
	}

	private void setYNodeValuesForBag( YBag ybag, XDataStruct xds ) {
		if( xds.getXValues().size() <= 0 ) {
			return; // nothing to set
		}

		Node node = ybag.getNode();
		Bag bag = (Bag) node;
		Collection<Node> nodes = bag.getNodes();
		for( Iterator<Node> it = nodes.iterator(); it.hasNext(); ) {
			Node n = it.next();
			String fieldName = n.getFieldName();
			if( fieldName == null ) {
				continue;
			}
			XValue xvalue = xds.getXValue(fieldName);
			if( xvalue == null || xvalue.size() <= 0 ) {
				continue;
			}
			for( int i = 0; i < xvalue.size(); i++ ) {
				Object xv = xvalue.getValue(i);
				YNode yn = ensureYNodeExists(ybag, n, i);
				setYNodeValues(yn, xv);
			}
		}
	}

	private void setYNodeValuesForChoice( YChoice ychoice, XDataChoice xdc ) {
		if( xdc.getXValue() == null ) {
			return;
		}
		XValue xvalue = xdc.getXValue();
		if( xvalue != null && xvalue.size() > 0 ) {
			Node n = ychoice.getChosenNode();
			for( int i = 0; i < xvalue.size(); i++ ) {
				Object xv = xvalue.getValue(i);
				YNode yn = ensureYNodeExists(ychoice, n, i);
				setYNodeValues(yn, xv);
			}
		}
	}

	private void setLeafValue( YLeaf yleaf, Object value ) {
		if( value == null ) {
			return;
		}
		Node node = yleaf.getNode();

		MdmiDatatype dt = getDatatype(node);
		if( !(dt.isSimple() || dt.isExternal()) ) {
			throw new MdmiException("Invalid mapping for node " + DefaultSyntacticParser.getNodePath(yleaf.getNode()));
		}

		try {

			String format = yleaf.getLeaf().getFormat();
			XDT xdt = XDT.fromString(format);

			String v = null;
			if( dt.isPrimitive() ) {
				DTSPrimitive pdt = (DTSPrimitive) dt;
				if( xdt == null ) {
					xdt = XDT.fromPDT(pdt);
				}
				v = XDT.convertToString(pdt, value, format, xdt);
			}
			else if( dt.isDerived() ) {
				DTSDerived ddt = (DTSDerived) dt;
				DTSPrimitive pdt = ddt.getPrimitiveBaseType();
				if( xdt == null ) {
					xdt = XDT.fromPDT(pdt);
				}
				v = XDT.convertToString(pdt, value, format, xdt);
			}
			else if( dt.isExternal() ) {
				DTExternal dte = (DTExternal) dt;
				URI uri = dte.getTypeSpec();
				if( uri != null ) {
					v = Mdmi.INSTANCE.getExternalResolvers().getModelValue(dte, value);
				}
				else {
					DTSPrimitive pdt = DTSPrimitive.STRING;
					if( xdt == null ) {
						xdt = XDT.fromPDT(pdt);
					}
					v = XDT.convertToString(pdt, value, format, xdt);
				}
			}
			else {
				DTSEnumerated edt = (DTSEnumerated) dt;
				if( value instanceof EnumerationLiteral ) {
					v = ((EnumerationLiteral) value).getCode();
				}
				else if( value instanceof String ) {
					v = (String) value;
				}
				else {
					throw new MdmiException("Invalid enum conversion for type {0} value {1}", edt.getTypeName(), value);
				}
			}
			yleaf.setValue(v);
		}
		catch( Throwable throwable ) {
			throw new MdmiException("Error proccessing node " + DefaultSyntacticParser.getNodePath(yleaf.getNode()), throwable);
		}
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
				DTCStructured dts = (DTCStructured) dt;
				f = dts.getField(fieldName);
			}
			else if( dt.isChoice() ) {
				DTCChoice dtc = (DTCChoice) dt;
				f = dtc.getField(fieldName);
			}
			if( f == null ) {
				throw new MdmiException("Invalid construct for node " + DefaultSyntacticParser.getNodePath(node));
			}
			dt = f.getDatatype();
		}
		return dt;
	}

	/**
	 * Ensure the child of the specified parent exists, at the given index.
	 * Return the ynode at the given index. Will create it if necessary.
	 * 
	 * @param parent
	 *           The parent ynode.
	 * @param node
	 *           The child type.
	 * @param index
	 *           The index of the child in the parent children of the same type.
	 * @return The ynode at the requested index (may be creating it if need be).
	 */
	private YNode ensureYNodeExists( YNode parent, Node node, int index ) {
		YNode ynode = null;
		if( parent instanceof YBag ) {
			YBag ybag = (YBag) parent;
			if( node instanceof Bag ) {
				Bag bag = (Bag) node;
				ArrayList<YNode> ynodes = ybag.getYNodesForNode(bag);
				while( ynodes.size() <= index ) {
					ynode = new YBag(bag, ybag);
					ybag.addYNode(ynode);
					ynodes = ybag.getYNodesForNode(bag);
				}
				return ynodes.get(index);
			}
			else if( node instanceof Choice ) {
				Choice choice = (Choice) node;
				ArrayList<YNode> ynodes = ybag.getYNodesForNode(choice);
				while( ynodes.size() <= index ) {
					ynode = new YChoice(choice, ybag);
					ybag.addYNode(ynode);
					ynodes = ybag.getYNodesForNode(choice);
				}
				return ynodes.get(index);
			}
			else {
				LeafSyntaxTranslator leaf = (LeafSyntaxTranslator) node;
				ArrayList<YNode> ynodes = ybag.getYNodesForNode(leaf);
				while( ynodes.size() <= index ) {
					ynode = new YLeaf(leaf, ybag);
					ybag.addYNode(ynode);
					ynodes = ybag.getYNodesForNode(leaf);
				}
				return ynodes.get(index);
			}

		}
		else if( parent instanceof YLeaf ) {
			YChoice ychoice = (YChoice) parent;
			if( node instanceof Bag ) {
				Bag bag = (Bag) node;
				if( ychoice.getChosenNode() != bag ) {
					ychoice.forceChoice(null);
				}
				ArrayList<YNode> ynodes = ychoice.getYNodes();
				while( ynodes.size() <= index ) {
					ynode = new YBag(bag, ychoice);
					ychoice.addYNode(ynode);
					ynodes = ychoice.getYNodes();
				}
				return ynodes.get(index);
			}
			else if( node instanceof Choice ) {
				Choice choice = (Choice) node;
				if( ychoice.getChosenNode() != choice ) {
					ychoice.forceChoice(null);
				}
				ArrayList<YNode> ynodes = ychoice.getYNodes();
				while( ynodes.size() <= index ) {
					ynode = new YChoice(choice, ychoice);
					ychoice.addYNode(ynode);
					ynodes = ychoice.getYNodes();
				}
				return ynodes.get(index);
			}
			else {
				LeafSyntaxTranslator leaf = (LeafSyntaxTranslator) node;
				if( ychoice.getChosenNode() != leaf ) {
					ychoice.forceChoice(null);
				}
				ArrayList<YNode> ynodes = ychoice.getYNodes();
				while( ynodes.size() <= index ) {
					ynode = new YLeaf(leaf, ychoice);
					ychoice.addYNode(ynode);
					ynodes = ychoice.getYNodes();
				}
				return ynodes.get(index);
			}

		}
		else {
			throw new IllegalArgumentException("Invalid state: parent is a leaf!");
		}
	}

	/**
	 * Get the relative path from the node the se given is mapped to to the
	 * specified node. If the given node is null, it will return the absolute
	 * path.
	 * 
	 * <pre>
	 * NodeA
	 *   NodeB
	 *     NodeC
	 * 
	 * getPath(NodeA, null) returns {NodeA}
	 * getPath(NodeC, null) returns {NodeA, NodeB, NodeC}
	 * getPath(NodeC, NodeA) return {NodeB, NodeC}
	 * </pre>
	 * 
	 * @param se
	 *           The semantic element mapped to the node we want an absolute
	 *           path.
	 * @param node
	 *           The node relative to which we want the path (excluding the node
	 *           given).
	 * @return The relative path from the node the se given is mapped to to the
	 *         specified node. If the given node is null, it will return the
	 *         absolute path.
	 */
	private ArrayList<Node> getPath( SemanticElement se, Node node ) {
		ArrayList<Node> path = new ArrayList<Node>();
		Node n = se.getSyntaxNode();
		while( n != null && n != node ) {
			path.add(0, n);
			n = n.getParentNode();
		}
		return path;
	}

	/**
	 * Get the ynode to which the semantic element passed in is mapped to.
	 * Assumes the path is absolute, i.e. relative to the root of the syntax
	 * tree. Will create the ynodes on the path, if required.
	 * 
	 * @param yroot
	 *           The root of the syntax tree.
	 * @param se
	 *           The semantic element to which the syntax node to look for is
	 *           mapped.
	 * @return The ynode corresponding to the SE passed in.
	 */
	private YNode ensureAbsolutePath( YNode yroot, SemanticElement se ) {
		ArrayList<Node> path = getPath(se, null);
		if( path.size() == 1 ) {
			return yroot;
		}
		int index = 1;
		YNode parent = yroot;
		do {
			Node current = path.get(index++);
			parent = ensureParentHasChild(parent, current);
		} while( index < path.size() );
		return parent;
	}

	/**
	 * Get the ynode to which the semantic element passed in is mapped to.
	 * Assumes the path is relative to the given ynode. Will create the ynodes on
	 * the path, if required.
	 * 
	 * @param ynode
	 *           The node relative to which we ensure we have a path
	 * @param se
	 *           The semantic element to which the syntax node to look for is
	 *           mapped.
	 * @return The ynode corresponding to the SE passed in.
	 */
	private YNode ensureRelativePath( YNode ynode, SemanticElement se ) {
		ArrayList<Node> path = getPath(se, ynode.getNode());
		int index = 0;
		YNode parent = ynode;
		do {
			Node current = path.get(index++);
			parent = ensureParentHasChild(parent, current);
		} while( index < path.size() );
		return parent;
	}

	/**
	 * Ensure parent has at least one child of the given type.
	 * 
	 * @param parent
	 *           The parent.
	 * @param childType
	 *           The child type.
	 * @return The existing node, either existing of newly created.
	 */
	private YNode ensureParentHasChild( YNode parent, Node childType ) {
		if( parent.getNode() instanceof LeafSyntaxTranslator ) {
			throw new MdmiException("Invalid parent, found leaf, expected a bag or a choice!");
		}
		if( parent.getNode() instanceof Bag ) {
			YBag ybag = (YBag) parent;
			ArrayList<YNode> nodes = ybag.getYNodesForNode(childType);
			if( 0 < nodes.size() ) {
				return nodes.get(0); // get the first if more than one
			}
			// need to add a node of that type
			if( childType instanceof Bag ) {
				YBag child = new YBag((Bag) childType, ybag);
				ybag.addYNode(child);
				return child;
			}
			else if( childType instanceof Choice ) {
				YChoice child = new YChoice((Choice) childType, ybag);
				ybag.addYNode(child);
				return child;
			}
			else {
				YLeaf child = new YLeaf((LeafSyntaxTranslator) childType, ybag);
				ybag.addYNode(child);
				return child;
			}
		}
		else {
			YChoice ychoice = (YChoice) parent;
			ArrayList<YNode> nodes = ychoice.getYNodes();
			if( 0 < nodes.size() ) {
				return nodes.get(0); // get the first if more than one
			}
			// need to add a node of that type
			if( childType instanceof Bag ) {
				YBag child = new YBag((Bag) childType, ychoice);
				ychoice.addYNode(child);
				return child;
			}
			else if( childType instanceof Choice ) {
				YChoice child = new YChoice((Choice) childType, ychoice);
				ychoice.addYNode(child);
				return child;
			}
			else {
				YLeaf child = new YLeaf((LeafSyntaxTranslator) childType, ychoice);
				ychoice.addYNode(child);
				return child;
			}
		}
	}

	private ArrayList<YNode> ensureParentHasChildren( YNode parent, Node childType, int n ) {
		if( parent == null || parent.getNode() instanceof LeafSyntaxTranslator ) {
			throw new MdmiException("Invalid parent, found leaf, expected a bag or a choice!");
		}
		if( parent.getNode() instanceof Bag ) {
			YBag ybag = (YBag) parent;
			ArrayList<YNode> nodes = ybag.getYNodesForNode(childType);
			if( n <= nodes.size() ) {
				while( n < nodes.size() ) {
					nodes.remove(nodes.size() - 1); // return exact number n of
					// references
				}
				return nodes;
			}
			// need to add nodes of that type
			for( int i = nodes.size(); i < n; i++ ) {
				if( childType instanceof Bag ) {
					YBag child = new YBag((Bag) childType, ybag);
					ybag.addYNode(child);
					nodes.add(child);
				}
				else if( childType instanceof Choice ) {
					YChoice child = new YChoice((Choice) childType, ybag);
					ybag.addYNode(child);
					nodes.add(child);
				}
				else {
					YLeaf child = new YLeaf((LeafSyntaxTranslator) childType, ybag);
					ybag.addYNode(child);
					nodes.add(child);
				}
			}
			return nodes;
		}
		else {
			YChoice ychoice = (YChoice) parent;
			ArrayList<YNode> nodes = ychoice.getYNodes();
			if( n <= nodes.size() ) {
				while( n < nodes.size() ) {
					nodes.remove(nodes.size() - 1); // return exact number n of
					// references
				}
				return nodes;
			}
			// need to add nodes of that type
			for( int i = nodes.size(); i < n; i++ ) {
				if( childType instanceof Bag ) {
					YBag child = new YBag((Bag) childType, ychoice);
					ychoice.addYNode(child);
					nodes.add(child);
				}
				else if( childType instanceof Choice ) {
					YChoice child = new YChoice((Choice) childType, ychoice);
					ychoice.addYNode(child);
					nodes.add(child);
				}
				else {
					YLeaf child = new YLeaf((LeafSyntaxTranslator) childType, ychoice);
					ychoice.addYNode(child);
					nodes.add(child);
				}
			}
			return nodes;
		}
	}

	@SuppressWarnings( "unused" )
	private String pathToString( ArrayList<Node> path, boolean isAbsolute ) {
		StringBuilder sb = new StringBuilder();
		if( isAbsolute ) {
			sb.append('/');
		}
		for( int i = 0; i < path.size(); i++ ) {
			if( 0 < i ) {
				sb.append('/');
			}
			sb.append(path.get(i).getName());
		}
		return sb.toString();
	}

	private void setComputedValue( SemanticElement se ) {
		String rule = se.getComputedValue().getExpression();
		String lang = se.getComputedValue().getLanguage();
		XElementValue xe = new XElementValue(se, valueSet);
		IExpressionInterpreter adapter = Mdmi.getInterpreter(lang, xe, "", null);
		adapter.evalAction(xe, rule);
	}

	private void setComputedOutValue( SemanticElement se ) {
		String rule = se.getComputedOutValue().getExpression();
		String lang = se.getComputedOutValue().getLanguage();
		XElementValue xe = new XElementValue(se, valueSet);
		IExpressionInterpreter adapter = Mdmi.getInterpreter(lang, xe, "", null);
		adapter.evalAction(xe, rule);
	}

	private void setNullFlavorOut( SemanticElement se ) {
		if( nullFlavorValues.containsKey(se.getName()) ) {
			if( nullFlavorMethods.containsKey(se.getDatatype().getName()) ) {
				for( ToMessageElement tme : se.getToMdmi() ) {
					tme.setRule(String.format(nullFlavorMethods.get(se.getDatatype().getName()), tme.getName(), nullFlavorValues.get(se.getName())));
					tme.setRuleExpressionLanguage("JS");
				}
			}
		}

	}

	private void setComputedInValue( SemanticElement se ) {
		String rule = se.getComputedInValue().getExpression();
		String lang = se.getComputedInValue().getLanguage();

		if( se.getParent() != null ) {
			for( IElementValue needsChildEV : getElementValuesWithoutChild(se) ) {
				IElementValue childEV = new XElementValue(se, valueSet);
				needsChildEV.addChild(childEV);
				childEV.setParent(needsChildEV);
			}
		}

		if( valueSet.getElementValuesByType(se).size() > 0 ) {
			for( int i = 0; i < valueSet.getElementValuesByType(se).size(); i++ ) {
				evalRule(lang, rule, (XElementValue) valueSet.getElementValuesByType(se).get(i));
			}
		}
		else {
			evalRule(lang, rule, new XElementValue(se, valueSet));
		}
	}

	private List<IElementValue> getElementValuesWithoutChild( SemanticElement semanticElement ) {
		List<IElementValue> parentElementValues = valueSet.getElementValuesByType(semanticElement.getParent());
		List<IElementValue> result = new ArrayList<IElementValue>(parentElementValues);

		for( IElementValue parentElementValue : parentElementValues ) {
			for( IElementValue childElementValue : valueSet.getElementValuesByType(semanticElement) ) {
				if( parentElementValue.getChildren().contains(childElementValue) ) {
					result.remove(parentElementValue);
					break;
				}
			}
		}
		return result;
	}

	private void evalRule( String lang, String rule, XElementValue xe ) {
		IExpressionInterpreter adapter = Mdmi.getInterpreter(lang, xe, "", null);
		adapter.evalAction(xe, rule);
	}
} // DefaultMdmiSemanticParser
