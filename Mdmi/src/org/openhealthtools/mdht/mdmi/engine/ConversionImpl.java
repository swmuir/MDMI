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

import java.io.*;
import java.util.*;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.engine.Conversion.ConversionInfo;
import org.openhealthtools.mdht.mdmi.engine.json.MdmiMapper;
import org.openhealthtools.mdht.mdmi.model.*;

/**
 * Implementation class for rule execution.
 */
class ConversionImpl {
	boolean          logging;
	ObjectMapper     mapper;
	FileOutputStream jsonFop;
	File             jsonFile;
	boolean          first = true;
	ObjectNode       conversionNode;

	public static ConversionImpl Instance = new ConversionImpl();

	private void logToJson() throws Exception {
		if( logging ) {
			if( first )
				first = false;
			else
				jsonFop.write(",".getBytes());

			mapper.writeValue(jsonFop, conversionNode);

			conversionNode = null;
		}
	}

	private void logObject( String name, Object object ) {
		if( logging ) {
			if( conversionNode == null ) {
				conversionNode = mapper.createObjectNode().putObject("conversion");
				;
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
		execSrcRule(src, trg, toBE, v);

		logObject("toXValue", v);
		execTrgRule(v, trg, toSE);
		logObject("finalXValue", trg);
		logToJson();
	}

	boolean hasSrcRule( ToBusinessElement toBE ) {
		return toBE != null && toBE.getRule() != null && 0 < toBE.getRule().length();
	}

	boolean hasTrgRule( ToMessageElement toSE ) {
		return toSE != null && toSE.getRule() != null && 0 < toSE.getRule().length();
	}

	void execSrcRule( XElementValue src, XElementValue trg, ToBusinessElement toBE, XValue v ) {
		if( !hasSrcRule(toBE) ) {
			cloneValue(src.getXValue(), v, true, toBE, null);
		}
		else {
			IExpressionInterpreter adapter = Mdmi.getInterpreter(toBE, src, toBE.getName(), v);
			adapter.evalAction(src, toBE.getRule());
		}
	}

	void execTrgRule( XValue v, XElementValue trg, ToMessageElement toSE ) {
		if( !hasTrgRule(toSE) ) {
			cloneValue(v, trg.getXValue(), false, null, toSE);
		}
		else {
			if( trg.getXValue().getValues().size() == 0 && (trg.getXValue().getDatatype() instanceof DTCStructured) ) {
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
		for( XValue t : values ) {
			XValue s = src.getXValue(t.getName());
			if( s != null ) {
				cloneValue(s, t, fromSrc);
			}
		}
	}

	private void cloneChoice( XDataChoice src, XDataChoice trg, boolean fromSrc ) {
		XValue s = src.getXValue();
		String fieldName = s.getName();
		XValue t = trg.setXValue(fieldName);
		cloneValue(s, t, fromSrc);
	}

	private void cloneValue( XValue src, XValue trg, boolean fromSrc ) {
		cloneValue(src, trg, fromSrc, null, null);
	}

	private void cloneValue( XValue src, XValue trg, boolean fromSrc, ToBusinessElement toBE, ToMessageElement toSE ) {
		ArrayList<Object> values = src.getValues();
		if( values.size() <= 0 )
			return;

		if( usesVSR(fromSrc, toBE, toSE) ) {
			vsConvertWithResolver(src, trg, fromSrc, toBE, toSE);
			return;
		}
		else if( usesVS(fromSrc, toBE, toSE) ) {
			vsConvert(src, trg, fromSrc, toBE, toSE);
			return;
		}
		else if( src.getDatatype().isStruct() ) {
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
				DTSEnumerated edt = (DTSEnumerated) trg.getDatatype();
				trg.clear();
				for( int i = 0; i < values.size(); i++ ) {
					EnumerationLiteral srcEL = (EnumerationLiteral)values.get(i);
					if( srcEL != null ) {
						EnumerationLiteral trgEL = edt.getLiteralByCode(srcEL.getCode());
						trg.setValue(trgEL, -1);
					}
				}
			}
		}
	}

	private void vsConvertWithResolver( XValue src, XValue trg, boolean fromSrc, ToBusinessElement toBE, ToMessageElement toSE ) {
		MdmiExternalResolvers rs = Mdmi.INSTANCE.getExternalResolvers();
		DTExternal dt = null;
		Field srcField = null;
		Field trgField = null;
		if( fromSrc ) {
			SemanticElement se = toBE.getOwner(); // source
			MdmiBusinessElementReference ber = toBE.getBusinessElement(); // target
			DTCStructured srcDT = (DTCStructured)se.getDatatype();
			srcField = srcDT.getField(se.getEnumValueField());
			dt = (DTExternal)srcField.getDatatype();
			DTCStructured trgDT = (DTCStructured)ber.getReferenceDatatype();
			trgField = trgDT.getField(ber.getEnumValueField());
		}
		else {
			SemanticElement se = toSE.getOwner(); // target
			MdmiBusinessElementReference ber = toSE.getBusinessElement(); // source
			DTCStructured srcDT = (DTCStructured)ber.getReferenceDatatype();
			srcField = srcDT.getField(ber.getEnumValueField());
			dt = (DTExternal)srcField.getDatatype();
			DTCStructured trgDT = (DTCStructured)se.getDatatype();
			trgField = trgDT.getField(se.getEnumValueField());
		}
		ArrayList<Object> values = src.getValues();
		for( int i = 0; i < values.size(); i++ ) {
			XDataStruct srcXD = (XDataStruct)values.get(i);
			XDataStruct trgXD = new XDataStruct(trg);
			trg.setValue(trgXD);
			XValue sfv = srcXD.getXValue(srcField);
			XValue tfv = trgXD.getXValue(trgField);
			if( fromSrc ) {
				Object o = rs.getDictionaryValue(dt, sfv.getValue().toString());
				tfv.addValue(o);
			}
			else {
				Object o = rs.getModelValue(dt, sfv.getValue().toString());
				tfv.addValue(o);
			}
		}
	}

	private boolean vsConvert( XValue src, XValue trg, boolean fromSrc, ToBusinessElement toBE, ToMessageElement toSE ) {
		MdmiValueSetsHandler handler = null;
		String vsMapName = null; 
		MdmiDatatype srcDT = null;
		MdmiDatatype trgDT = null;
		Field srcField = null;
		Field trgField = null;
		Field trgFieldDescr = null;
		if( fromSrc ) {
			SemanticElement se = toBE.getOwner(); // source
			MdmiBusinessElementReference ber = toBE.getBusinessElement(); // target
			handler = Mdmi.INSTANCE.getResolver().getValueSetsHandler(se.getElementSet().getModel().getGroup().getName());
			vsMapName = MdmiValueSetMap.getMapName(se.getEnumValueSet(), ber.getEnumValueSet());
			srcDT = se.getDatatype();
			trgDT = ber.getReferenceDatatype();
			if( srcDT instanceof DTCStructured ) {
				DTCStructured xdt = (DTCStructured)se.getDatatype();
				srcField = xdt.getField(se.getEnumValueField());
			}
			if( trgDT instanceof DTCStructured ) {
				DTCStructured xdt = (DTCStructured)ber.getReferenceDatatype();
				trgField = xdt.getField(ber.getEnumValueField());
				if( null != ber.getEnumValueDescrField() )
					trgFieldDescr = xdt.getField(ber.getEnumValueDescrField());
			}
		}
		else {
			SemanticElement se = toSE.getOwner(); // target
			MdmiBusinessElementReference ber = toSE.getBusinessElement(); // source
			handler = Mdmi.INSTANCE.getResolver().getValueSetsHandler(se.getElementSet().getModel().getGroup().getName());
			vsMapName = MdmiValueSetMap.getMapName(ber.getEnumValueSet(), se.getEnumValueSet());
			srcDT = ber.getReferenceDatatype();
			trgDT = se.getDatatype();
			if( srcDT instanceof DTCStructured ) {
				DTCStructured xdt = (DTCStructured)ber.getReferenceDatatype();
				srcField = xdt.getField(ber.getEnumValueField());
			}
			if( trgDT instanceof DTCStructured ) {
				DTCStructured xdt = (DTCStructured)se.getDatatype();
				trgField = xdt.getField(se.getEnumValueField());
				if( null != se.getEnumValueDescrField() )
					trgFieldDescr = xdt.getField(se.getEnumValueDescrField());
			}
		}
		if( null == handler )
			return false;
		MdmiValueSetMap vsMap = handler.getValueSetMap(vsMapName);
		if( null == vsMap )
			return false;
		
		ArrayList<Object> values = src.getValues();
		for( int i = 0; i < values.size(); i++ ) {
			Object source = values.get(i); // XDataStruct or EnumerationLiteral
			
			if( null == srcField && null == trgField ) {
				// 1. Enum - > Enum
				EnumerationLiteral srcEL = (EnumerationLiteral)source;
				MdmiValueSetMap.Mapping map = vsMap.getMappingBySource(srcEL.getName());
				if( null != map ) {
					MdmiValueSet.Value target = map.getTarget();
					DTSEnumerated srcDTSE = (DTSEnumerated)srcDT;
					trg.setValue(srcDTSE.getLiteralByName(target.getCode()));
				}
			}
			else if( null != srcField && null == trgField ) {
				// 2. VS - > Enum
				XDataStruct srcXD = (XDataStruct)values.get(i);
				XValue sfv = srcXD.getXValue(srcField);
				MdmiValueSetMap.Mapping map = vsMap.getMappingBySource((String)sfv.getValue());
				if( null != map ) {
					MdmiValueSet.Value target = map.getTarget();
					DTSEnumerated srcDTSE = (DTSEnumerated)srcDT;
					trg.setValue(srcDTSE.getLiteralByName(target.getCode()));
				}
			}
			else if( null == srcField && null != trgField ) {
				// 3. Enum -> VS
				EnumerationLiteral srcEL = (EnumerationLiteral)source;
				XDataStruct trgXD = new XDataStruct(trg);
				trg.setValue(trgXD);
				XValue tfv = trgXD.getXValue(trgField);
				XValue tfvd = null;
				if( null != trgFieldDescr )
					tfvd = trgXD.getXValue(trgFieldDescr);
				MdmiValueSetMap.Mapping map = vsMap.getMappingBySource(srcEL.getName());
				if( null != map ) {
					MdmiValueSet.Value target = map.getTarget();
					tfv.setValue(target.getCode());
					if( null != tfvd )
						tfvd.setValue(target.getDescription());
				}
			}
			else {
				// 4. VS -> VS
				XDataStruct srcXD = (XDataStruct)values.get(i);
				XDataStruct trgXD = new XDataStruct(trg);
				trg.setValue(trgXD);
				XValue sfv = srcXD.getXValue(srcField);
				XValue tfv = trgXD.getXValue(trgField);
				XValue tfvd = null;
				if( null != trgFieldDescr )
					tfvd = trgXD.getXValue(trgFieldDescr);
				MdmiValueSetMap.Mapping map = vsMap.getMappingBySource((String)sfv.getValue());
				if( null != map ) {
					MdmiValueSet.Value target = map.getTarget();
					tfv.setValue(target.getCode());
					if( null != tfvd )
						tfvd.setValue(target.getDescription());
				}
			}
		}
		return true;
	}
	
	public void start( boolean logging ) {
		this.logging = logging;
		if( this.logging ) {
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
			first = true;
			mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		}
	}

	public void end() {
		if( this.logging ) {
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
	
	boolean usesVS( boolean fromSrc, ToBusinessElement toBE, ToMessageElement toSE ) {
		if( null == toBE && null == toSE )
			return false;
		if( fromSrc ) {
			// toBE is not null, toSE is null, executing conversion from SE -> BER
			if( null != toBE.getEnumExtResolverUri() )
				return true;
			MdmiBusinessElementReference ber = toBE.getBusinessElement();
			SemanticElement se = toBE.getOwner();
			if( ber.usesValueSet() && se.usesValueSet() )
				return true;
		}
		else {
			// toBE is null, toSE is not null, executing conversion from BER -> SE
			if( null != toSE.getEnumExtResolverUri() )
				return true;
			MdmiBusinessElementReference ber = toSE.getBusinessElement();
			SemanticElement se = toSE.getOwner();
			if( ber.usesValueSet() && se.usesValueSet() )
				return true;
		}
		return false;
	}
	
	boolean usesVSR( boolean fromSrc, ToBusinessElement toBE, ToMessageElement toSE ) {
		if( null == toBE && null == toSE )
			return false;
		if( fromSrc ) {
			// toBE is not null, toSE is null, executing conversion from SE -> BER
			if( null != toBE.getEnumExtResolverUri() )
				return true;
		}
		else {
			// toBE is null, toSE is not null, executing conversion from BER -> SE
			if( null != toSE.getEnumExtResolverUri() )
				return true;
		}
		return false;
	}
} // ConversionImpl