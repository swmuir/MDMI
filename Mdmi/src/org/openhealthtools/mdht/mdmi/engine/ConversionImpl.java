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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.openhealthtools.mdht.mdmi.IEnumerationConverter;
import org.openhealthtools.mdht.mdmi.IExpressionInterpreter;
import org.openhealthtools.mdht.mdmi.Mdmi;
import org.openhealthtools.mdht.mdmi.MdmiResolver;
import org.openhealthtools.mdht.mdmi.engine.Conversion.ConversionInfo;
import org.openhealthtools.mdht.mdmi.engine.json.MdmiMapper;
import org.openhealthtools.mdht.mdmi.model.*;

/**
 * Implementation class for rule execution.
 */
class ConversionImpl {
	
	boolean logging = false;
	ObjectMapper mapper = null;
	FileOutputStream jsonFop = null;
	File jsonFile = null;

	boolean first = true;
	ObjectNode conversionNode = null;
	
	public static ConversionImpl Instance = new ConversionImpl();

	private void logToJson() throws Exception {
		if (logging) {
		if( first )
			first = false;
		else
			jsonFop.write(",".getBytes());
		
		mapper.writeValue(jsonFop, conversionNode);
		
		conversionNode = null;
		}
	}

	private void logObject(String name,Object object)
	{
		if (logging) {
			if (conversionNode == null) 
			{
				conversionNode = mapper.createObjectNode().putObject("conversion");;
			}
			conversionNode.putPOJO(name, object);
		}
	}
	private ConversionImpl() {
	}

	void convert( XElementValue src, ConversionInfo ci, XElementValue trg ) throws Exception {


		
		logObject("conversionInfo", ci);
		logObject("xElementValue", trg);

		ToBusinessElement toBE = Conversion.getToBE(src.getSemanticElement(), ci.srcBER);
		logObject("toBusinessElement", toBE);

		ToMessageElement toSE = Conversion.getToSE(trg.getSemanticElement(), ci.trgBER);
		logObject("toMessageElement", toSE);

		XValue v = new XValue("v", toBE.getBusinessElement().getReferenceDatatype());
		logObject("initialXValue", v);

		if( !hasTrgRule(toSE) )
			cloneValue(trg.getXValue(), v, false);
		execSrcRule(src, ci, trg, toBE, v);

		logObject("toXValue", v);

		execTrgRule(v, ci, trg, toSE);

		logObject("finalXValue", trg);

		logToJson();
	}

	boolean hasSrcRule( ToBusinessElement toBE ) {
		return toBE != null && toBE.getRule() != null && 0 < toBE.getRule().length();
	}

	boolean hasTrgRule( ToMessageElement toSE ) {
		return toSE != null && toSE.getRule() != null && 0 < toSE.getRule().length();
	}

	void execSrcRule( XElementValue src, ConversionInfo ci, XElementValue trg, ToBusinessElement toBE, XValue v ) {
		if( !hasSrcRule(toBE) ) {
			cloneValue(src.getXValue(), v, true);
		}
		else {
			IExpressionInterpreter adapter = Mdmi.getInterpreter(toBE, src, toBE.getName(), v);
			adapter.evalAction(src, toBE.getRule());
		}
	}

	void execTrgRule( XValue v, ConversionInfo ci, XElementValue trg, ToMessageElement toSE ) {
		if( !hasTrgRule(toSE) ) {
			cloneValue(v, trg.getXValue(), false);
		}
		else {
            if (trg.getXValue().getValues().size() == 0 && (trg.getXValue().getDatatype() instanceof DTCStructured)) {
                XDataStruct xs = new XDataStruct(trg.getXValue());
				trg.getXValue().addValue(xs);
			}
			IExpressionInterpreter adapter = Mdmi.getInterpreter(toSE, trg, toSE.getName(), v);
			adapter.evalAction(trg, toSE.getRule());
		}
	}

	private void cloneStruct( XDataStruct src, XDataStruct trg, boolean fromSrc ) {
		if( src == null || trg == null ) {
			throw new IllegalArgumentException("Null argument!");
		}
		Collection<XValue> values = trg.getXValues();
		for (XValue t : values) {
			XValue s = src.getXValue(t.getName());
			if( s != null ) {
				cloneValue(s, t, fromSrc);
			}
		}
		
//		for( int i = 0; i < values.size(); i++ ) {
//			XValue t = values.get(i);
//			XValue s = src.getXValue(t.getName());
//			if( s != null ) {
//				cloneValue(s, t, fromSrc);
//			}
//		}
	}

	private void cloneChoice( XDataChoice src, XDataChoice trg, boolean fromSrc ) {
		XValue s = src.getXValue();
		String fieldName = s.getName();
		XValue t = trg.setXValue(fieldName);
		cloneValue(s, t, fromSrc);
	}

	private void cloneValue( XValue src, XValue trg, boolean fromSrc ) {
		ArrayList<Object> values = src.getValues();
		if( values.size() <= 0 ) {
			return;
		}
		if( src.getDatatype().isStruct() ) {
			for( int i = 0; i < values.size(); i++ ) {
				XDataStruct srcXD = (XDataStruct) values.get(i);
				XDataStruct trgXD = new XDataStruct(trg);
				trg.setValue(trgXD);
				cloneStruct(srcXD, trgXD, fromSrc);
			}
		}
		else if( src.getDatatype().isChoice() ) {
			for( int i = 0; i < values.size(); i++ ) {
				XDataChoice srcXD = (XDataChoice) values.get(i);
				XDataChoice trgXD = new XDataChoice(trg);
				trg.setValue(trgXD);
				cloneChoice(srcXD, trgXD, fromSrc);
			}
		}
		else { // simple
			if( src.getDatatype().isPrimitive() || src.getDatatype().isDerived() || src.getDatatype().isExternal() ) {
				trg.cloneValues(src);
			}
			else {
				DTSEnumerated eds = (DTSEnumerated) src.getDatatype();
				DTSEnumerated edt = (DTSEnumerated) trg.getDatatype();
				MdmiResolver resolver = Mdmi.INSTANCE.getResolver();
				IEnumerationConverter ecv = null;
				if( fromSrc ) {
					MessageGroup srcMap = eds.getOwner();
					ecv = resolver.getEnumerationConverter(srcMap.getName());
				}
				else {
					MessageGroup trgMap = edt.getOwner();
					ecv = resolver.getEnumerationConverter(trgMap.getName());
				}
				trg.clear();

				if( ecv == null || !ecv.canConvert(eds, edt) ) {
					for( int i = 0; i < values.size(); i++ ) {
						EnumerationLiteral srcEL = (EnumerationLiteral) values.get(i);
						if( srcEL != null ) {
							EnumerationLiteral trgEL = edt.getLiteralByCode(srcEL.getCode());
							trg.setValue(trgEL, -1);
						}
					}
				}
				else {
					for( int i = 0; i < values.size(); i++ ) {
						EnumerationLiteral srcEL = (EnumerationLiteral) values.get(i);
						if( srcEL != null ) {
							EnumerationLiteral trgEL = ecv.convert(eds, edt, srcEL);
							trg.setValue(trgEL, -1);
						}
					}
				}
			}
		}
	}

	public void start(boolean logging) {
		
		this.logging = logging;
		
		if (this.logging) {
		jsonFile = new File("./logs/Conversion.json");
		try {
			jsonFop = new FileOutputStream(jsonFile);
			jsonFop.write("[".getBytes());
		}
		catch( FileNotFoundException e1 ) {
			e1.printStackTrace();
		}
		catch( IOException e ) {
			e.printStackTrace();
		}

		mapper = new MdmiMapper();

		mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		}
	}

	public void end() {
		if (this.logging) {
		try {
			jsonFop.write("]".getBytes());
			jsonFop.flush();
			jsonFop.close();
			jsonFop = null;
		}
		catch( IOException e ) {
			e.printStackTrace();
		}
		}
	}
}