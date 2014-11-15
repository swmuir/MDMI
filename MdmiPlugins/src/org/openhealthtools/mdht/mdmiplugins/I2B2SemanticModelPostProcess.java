package org.openhealthtools.mdht.mdmiplugins;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.openhealthtools.mdht.mdmi.ElementValueSet;
import org.openhealthtools.mdht.mdmi.IElementValue;
import org.openhealthtools.mdht.mdmi.ITargetSemanticModelPostProcessor;
import org.openhealthtools.mdht.mdmi.engine.XDataStruct;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * I2B2SemanticModelPostProcess uses the I2B2 rest services to populate the name
 * and path of the codes in the concept and modifer dimensions of the message
 * 
 * @author seanmuir
 * 
 */
public class I2B2SemanticModelPostProcess implements
		ITargetSemanticModelPostProcessor {

	private static class CodeInfo {

		private String name;
		private String path;

		public String getName() {
			return name;
		}

		public String getPath() {
			return path;
		}

		public CodeInfo(String name, String path) {
			super();
			this.name = name;
			this.path = path;
		}

	}

	private static final String I2B2RestURL = "http://services.i2b2.org:9090/i2b2/services/OntologyService/getCodeInfo";

	private HashMap<String, CodeInfo> codeInformationMap = new HashMap<String, CodeInfo>();

	@Override
	public String getName() {
		return "TargetSemanticModel";
	}

	
	private void processCodes(ElementValueSet semanticModel,String semanticName,String codeAttribute,String nameAttribute,String pathAttribute) {

		ArrayList<IElementValue> ievs = semanticModel
				.getElementValuesByName(semanticName);

		for (IElementValue iev : ievs) {
			if (iev.value() != null && iev.value() instanceof XDataStruct) {
				XDataStruct xds = (XDataStruct) iev.value();
				String code = (String) xds.getValue(codeAttribute);
				if (code != null) {
					CodeInfo codeInfo = invokeI2B2(code);
					if (codeInfo != null) {
					xds.setValue(nameAttribute, codeInfo.getName());
					xds.setValue(pathAttribute, codeInfo.getPath());
					} else {
						xds.setValue(nameAttribute, "Error "+ code + " not found!");
					}
				}
			}
		}
	}
	@Override
	public void processModel(ElementValueSet semanticModel) {
		
		processCodes( semanticModel,"concept_dimension","concept_set_concept_cd","concept_set_concept_name","concept_set_concept_path");
		
		processCodes( semanticModel,"modifier_dimension","modifier_set_modifier_cd","modifier_set_modifier_name","modifier_cd_modifier_path");
		
	}
	
	

	private CodeInfo invokeI2B2(String code) {

		if (codeInformationMap.containsKey(code)) {
			return codeInformationMap.get(code);
		} else {

			try {
				Client httpClient = Client.create();

				WebResource getCodeInfo = httpClient
						.resource(I2B2RestURL);

				final String request = readFile("i2b2GetCodeInfo.xml",
						StandardCharsets.UTF_8);

				ClientResponse response = getCodeInfo
						.accept(MediaType.APPLICATION_XML, MediaType.TEXT_XML)
						.type(MediaType.TEXT_XML)
						.post(ClientResponse.class, request.replace("XXXXXXXXXXXXXXXXXXXXXXXXXX", code));

				processResponse(response.getEntity(String.class));

			} catch (Exception e) {

			}

		}

		return codeInformationMap.get(code);

	}

	private void processResponse(String response) throws Exception {

		InputStream stream = new ByteArrayInputStream(
				response.getBytes(StandardCharsets.UTF_8));

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		factory.setNamespaceAware(false);

		DocumentBuilder builder;

		Document doc = null;

		XPathExpression expr = null;

		builder = factory.newDocumentBuilder();

		doc = builder.parse(new InputSource(stream));  

		XPathFactory xFactory = XPathFactory.newInstance();

		XPath xpath = xFactory.newXPath();

		expr = xpath.compile("//response/message_body/concepts/concept");

		Object result = expr.evaluate(doc, XPathConstants.NODESET);

		NodeList nodes = (NodeList) result;

		for (int i = 0; i < nodes.getLength(); i++) {

			String name = "";
			String path = "";
			String code = "";
			for (int j = 0; j < nodes.item(i).getChildNodes().getLength(); j++) {

				if ("name".equals(nodes.item(i).getChildNodes().item(j)
						.getNodeName())) {
					name = nodes.item(i).getChildNodes().item(j)
							.getTextContent();
				}

				if ("key".equals(nodes.item(i).getChildNodes().item(j)
						.getNodeName())) {
					path = nodes.item(i).getChildNodes().item(j)
							.getTextContent();
				}

				if ("basecode".equals(nodes.item(i).getChildNodes().item(j)
						.getNodeName())) {
					code = nodes.item(i).getChildNodes().item(j)
							.getTextContent();
				}
			}

			codeInformationMap.put(code, new CodeInfo( name, path));

		}
	}

	private static String readFile(String path, Charset encoding)
			throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

}
