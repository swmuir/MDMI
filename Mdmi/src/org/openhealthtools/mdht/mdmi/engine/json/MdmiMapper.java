package org.openhealthtools.mdht.mdmi.engine.json;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.module.SimpleModule;
import org.openhealthtools.mdht.mdmi.ElementValueSet;
import org.openhealthtools.mdht.mdmi.engine.Conversion.ConversionInfo;
import org.openhealthtools.mdht.mdmi.engine.XDataStruct;
import org.openhealthtools.mdht.mdmi.engine.XElementValue;
import org.openhealthtools.mdht.mdmi.engine.XValue;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.SemanticElementSerializer;
import org.openhealthtools.mdht.mdmi.model.ToBusinessElement;
import org.openhealthtools.mdht.mdmi.model.ToMessageElement;

public class MdmiMapper extends ObjectMapper {

	public MdmiMapper() {
		super();
//		SimpleModule startInfoSerializerModule = new SimpleModule("Start", new Version(1, 0, 0, null));

		
		

		
		SimpleModule nodeSerializerModule = new SimpleModule("Node", new Version(1, 0, 0, null));
		nodeSerializerModule.addSerializer(org.openhealthtools.mdht.mdmi.model.Node.class, new JsonSerializer<org.openhealthtools.mdht.mdmi.model.Node>() {

			@Override
			public void serialize(org.openhealthtools.mdht.mdmi.model.Node node, JsonGenerator jgen, SerializerProvider arg2) throws IOException, JsonProcessingException {
				jgen.writeStartObject();
//				jgen.writeObject(node.get);
				jgen.writeObjectField("FieldName", node.getFieldName());
				jgen.writeObjectField("Location", node.getLocation());
				jgen.writeObjectField("Name", node.getName());
//				jgen.writeObjectField("TargetBER", ci.trgBER != null ? ci.trgBER : "null");
				jgen.writeEndObject();
			}
		});
		registerModule(nodeSerializerModule);
		
		
		
		
		SimpleModule toMessageElementSerializerModule = new SimpleModule("ToMessageElement", new Version(1, 0, 0, null));
		toMessageElementSerializerModule.addSerializer(ToMessageElement.class, new JsonSerializer<ToMessageElement>() {

			@Override
			public void serialize(ToMessageElement toMessageElement, JsonGenerator jgen, SerializerProvider arg2) throws IOException, JsonProcessingException {
				jgen.writeStartObject();
				jgen.writeObjectField("toString", toMessageElement.toString());
//				jgen.writeObjectField("TargetBER", ci.trgBER != null ? ci.trgBER : "null");
				jgen.writeEndObject();
			}
		});
		registerModule(toMessageElementSerializerModule);
		
		
		
		
		SimpleModule toBusinessElementSerializerModule = new SimpleModule("ToBusinessElement", new Version(1, 0, 0, null));
		toBusinessElementSerializerModule.addSerializer(ToBusinessElement.class, new JsonSerializer<ToBusinessElement>() {

			@Override
			public void serialize(ToBusinessElement toBusinessElement, JsonGenerator jgen, SerializerProvider arg2) throws IOException, JsonProcessingException {
				jgen.writeStartObject();
				
				jgen.writeObjectField("toString", toBusinessElement.toString());
//				jgen.writeObjectField("TargetBER", ci.trgBER != null ? ci.trgBER : "null");
				jgen.writeEndObject();
			}
		});
		registerModule(toBusinessElementSerializerModule);
		

	
		
		SimpleModule xValueSerializerModule = new SimpleModule("XValue", new Version(1, 0, 0, null));
		xValueSerializerModule.addSerializer(XValue.class, new JsonSerializer<XValue>() {

			@Override
			public void serialize(XValue xValue, JsonGenerator jgen, SerializerProvider arg2) throws IOException, JsonProcessingException {
				jgen.writeStartObject();
				if (xValue.getValue()!=null)
				{
					jgen.writeObjectField("value", xValue.getValue());
				}
				
				if (xValue.getValues()!=null)
				{
					jgen.writeObjectField("values", xValue.getValues());
				}
				
//				jgen.writeObjectField("SourceBER", ci.srcBER != null ? ci.srcBER : "null");
//				jgen.writeObjectField("TargetBER", ci.trgBER != null ? ci.trgBER : "null");
				jgen.writeEndObject();
			}
		});
registerModule(xValueSerializerModule);
		
		
		
		SimpleModule conversionInfoSerializerModule = new SimpleModule("ConversionInfo", new Version(1, 0, 0, null));
		conversionInfoSerializerModule.addSerializer(ConversionInfo.class, new JsonSerializer<ConversionInfo>() {

			@Override
			public void serialize(ConversionInfo ci, JsonGenerator jgen, SerializerProvider arg2) throws IOException, JsonProcessingException {
				jgen.writeStartObject();
				jgen.writeObjectField("SourceBER", ci.srcBER != null ? ci.srcBER : "null");
				jgen.writeObjectField("TargetBER", ci.trgBER != null ? ci.trgBER : "null");
				jgen.writeObjectField("SemanticElement", ci.target != null ? ci.target : "null");
				
				jgen.writeEndObject();
			}
		});
		registerModule(conversionInfoSerializerModule);

		SimpleModule mdmiBusinessElementReferenceSerializerModule = new SimpleModule("MdmiBusinessElementReference", new Version(1, 0, 0, null));
		mdmiBusinessElementReferenceSerializerModule.addSerializer(MdmiBusinessElementReference.class, new JsonSerializer<MdmiBusinessElementReference>() {

			@Override
			public void serialize(MdmiBusinessElementReference mber, JsonGenerator jgen, SerializerProvider arg2) throws IOException, JsonProcessingException {
				jgen.writeStartObject();
				jgen.writeObjectField("name", mber.getName());
				jgen.writeObjectField("parent", (mber.getParent() != null ? mber.getParent().getName() : ""));
				// jgen.writeObjectField("name", mber.get);
				jgen.writeEndObject();

			}

		});
		registerModule(mdmiBusinessElementReferenceSerializerModule);

		SimpleModule dateTimeSerializerModule = new SimpleModule("DateTimeSerializerModule", new Version(1, 0, 0, null));
		dateTimeSerializerModule.addSerializer(ElementValueSet.class, new ElementValueSetSerializer());
		registerModule(dateTimeSerializerModule);

		SimpleModule xElementValueSerializerModule = new SimpleModule("XElementValueSerializer", new Version(1, 0, 0, null));
		xElementValueSerializerModule.addSerializer(XElementValue.class, new XElementValueSerializer());
		registerModule(xElementValueSerializerModule);

		SimpleModule semanticElementSerializerModule = new SimpleModule("SemanticElementSerializer", new Version(1, 0, 0, null));
		xElementValueSerializerModule.addSerializer(SemanticElement.class, new SemanticElementSerializer());
		registerModule(semanticElementSerializerModule);

		SimpleModule xDataStructSerializerrModule = new SimpleModule("XDataStructSerializer", new Version(1, 0, 0, null));
		xDataStructSerializerrModule.addSerializer(XDataStruct.class, new XDataStructSerializer());
		registerModule(xDataStructSerializerrModule);

	}

}
