package org.openhealthtools.mdht.mdmi.model;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

public class SemanticElementSerializer extends JsonSerializer<SemanticElement> {

	@Override
	public void serialize(SemanticElement se, JsonGenerator jgen,
			SerializerProvider arg2) throws IOException,
			JsonProcessingException {

		jgen.writeStartObject();
	
		jgen.writeObjectField("name", se.getName());
		jgen.writeObjectField("node", se.getSyntaxNode());
		
	
		jgen.writeEndObject();
		
		
	}

	
}
