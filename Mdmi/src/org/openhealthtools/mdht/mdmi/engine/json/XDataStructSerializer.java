package org.openhealthtools.mdht.mdmi.engine.json;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.openhealthtools.mdht.mdmi.engine.XDataStruct;
import org.openhealthtools.mdht.mdmi.engine.XValue;

public class XDataStructSerializer extends JsonSerializer<XDataStruct> {

	@Override
	public void serialize(XDataStruct xds, JsonGenerator jgen,
			SerializerProvider arg2) throws IOException,
			JsonProcessingException {
		
		
		jgen.writeStartObject();
		jgen.writeObjectField("Data Type", xds.getDatatype().getName());
		
		
		
		jgen.writeArrayFieldStart("values");
		
		for (XValue xv : xds.getXValues()) {
			
			
			jgen.writeStartObject();
			jgen.writeObjectField("name", xv.getName());

			jgen.writeObjectField("value", xv.getValue());
			jgen.writeEndObject();
			
			
			
			
		}
		
		jgen.writeEndArray();
		

		
		jgen.writeEndObject();

	}

}
