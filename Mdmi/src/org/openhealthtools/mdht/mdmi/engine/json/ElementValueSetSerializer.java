package org.openhealthtools.mdht.mdmi.engine.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.openhealthtools.mdht.mdmi.ElementValueSet;
import org.openhealthtools.mdht.mdmi.IElementValue;
import org.openhealthtools.mdht.mdmi.ElementValueSet.SEVS;

public class ElementValueSetSerializer extends JsonSerializer<ElementValueSet> {

	@Override
	public void serialize(ElementValueSet evs, JsonGenerator jgen,
			SerializerProvider arg2) throws IOException,
			JsonProcessingException {
		
		jgen.writeStartArray();
		
		List<String> sortedKeys=new ArrayList<String>(evs.getM_xelements().keySet());
		Collections.sort(sortedKeys);
		
		for (String key : sortedKeys)
		{
			jgen.writeStartObject();
			SEVS sevs =evs.getM_xelements().get(key);
			jgen.writeObjectField("Semantic Element", sevs.semanticElement.getName());
			
			jgen.writeArrayFieldStart("values");
			for (IElementValue v : sevs.xelements.values()) {
				jgen.writeObject(v.value());
			}
			jgen.writeEndArray();
			

			
			jgen.writeEndObject();
		}
		
		
//		for (IElementValue elementValue : evs.getAllElementValues()) {
//			jgen.writeStartObject();
//			System.out.println(elementValue.getClass().getName());
//			jgen.writeObjectField("name", elementValue.getName());
//			jgen.writeObjectField("type", elementValue.getClass().getName());
//			
//			jgen.writeObjectField("value", elementValue);
//			jgen.writeEndObject();
//		}
		
		jgen.writeEndArray();

	}

}
