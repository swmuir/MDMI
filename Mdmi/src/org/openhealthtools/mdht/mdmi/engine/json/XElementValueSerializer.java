package org.openhealthtools.mdht.mdmi.engine.json;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.openhealthtools.mdht.mdmi.engine.XElementValue;

public class XElementValueSerializer extends JsonSerializer<XElementValue> {

	@Override
	public void serialize(XElementValue xev, JsonGenerator jgen,
			SerializerProvider arg2) throws IOException,
			JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeObjectField("dddname", xev.getName());
		if (xev.value() != null) {
		jgen.writeObjectField("type", xev.value().getClass().getCanonicalName());
		jgen.writeObjectField("value", xev.value());
		}
//		jgen.writeObjectField("value", xev.value());
		
//		jgen.writeObjectField("type", xev.getClass().getName());
//		jgen.writeObjectField("value", elementValue.toString());
		jgen.writeEndObject();

	}

}
