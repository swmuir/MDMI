package org.openhealthtools.mdht.mdmiplugins.resolvers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openhealthtools.mdht.mdmi.IPostProcessor;
import org.openhealthtools.mdht.mdmi.MdmiMessage;
import org.openhealthtools.mdht.mdmi.MdmiModelRef;
import org.openhealthtools.mdht.mdmi.util.XmlParser;
import org.openhealthtools.mdht.mdmi.util.XmlUtil;
import org.openhealthtools.mdht.mdmi.util.XmlWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

public class CcdaPostProcessor implements IPostProcessor {
	private static final String MESSAGE_GROUP = "CCDMessageGroup";
	private static final String MESSAGE_MODEL = "CCD";

	@Override
	public String getName() {
		return "CcdaPostProcessor";
	}

	@Override
	public ArrayList<String> getHandledQualifiedMessageNames() {
		ArrayList<String> a = new ArrayList<String>();
		a.add(MESSAGE_MODEL);
		return a;
	}

	@Override
	public boolean canProcess(String messageGroupName, String messageModelName) {
		if (messageGroupName.equalsIgnoreCase(MESSAGE_GROUP)
				&& messageModelName.equalsIgnoreCase(MESSAGE_MODEL))
			return true;
		return false;
	}

	@Override
	public void processMessage(MdmiMessage message, MdmiModelRef model) {
		// if not the relevant mesage type return with no changes - this should
		// never happen
		if (!model.getGroupName().equalsIgnoreCase(MESSAGE_GROUP)
				&& !model.getModelName().equalsIgnoreCase(MESSAGE_MODEL))
			return;

		// Parse the target message
		XmlParser p = new XmlParser();
		Document doc = p.parse(new ByteArrayInputStream(message.getData()));

		NodeList sections = doc.getElementsByTagNameNS("*", "section");

		for (int sectionCtr = 0; sectionCtr < sections.getLength(); sectionCtr++) {

			addSectionNarrative((Element) sections.item(sectionCtr), doc);
		}

		// Serialize it to a string and put it back
		StringWriter sw = new StringWriter();
		XmlWriter w = new XmlWriter(sw);
		w.write(doc);
		message.setData(sw.toString());
	}

	private void addSectionNarrative(Element section, Document doc) {

		try {

			DOMImplementationRegistry registry = DOMImplementationRegistry
					.newInstance();
			DOMImplementationLS lsImpl = (DOMImplementationLS) registry
					.getDOMImplementation("LS");

			LSSerializer serializer = lsImpl.createLSSerializer();

			LSOutput lsOutput = lsImpl.createLSOutput();
			lsOutput.setEncoding("UTF-8");
			Writer stringWriter = new StringWriter();
			lsOutput.setCharacterStream(stringWriter);
			serializer.write(section, lsOutput);

			String result = stringWriter.toString();

			javax.xml.transform.TransformerFactory tFactory = javax.xml.transform.TransformerFactory
					.newInstance();

			ByteArrayInputStream bis = new ByteArrayInputStream(
					result.getBytes());

			ByteArrayOutputStream bos = new ByteArrayOutputStream();

			javax.xml.transform.Transformer transformer = tFactory
					.newTransformer(new javax.xml.transform.stream.StreamSource(
							"section.xsl"));

			System.out.println(result);
			transformer.transform(new javax.xml.transform.stream.StreamSource(
					bis), new javax.xml.transform.stream.StreamResult(bos));

			Element narrativeText = XmlUtil.getElement(section, "text");

			if (narrativeText == null) {
				narrativeText = doc.createElement("text");
				NodeList nl = section.getElementsByTagNameNS("*", "entry");

				if (nl.getLength() == 0) {
					narrativeText = (Element) section.insertBefore(
							narrativeText, null);
				} else {
					narrativeText = (Element) section.insertBefore(
							narrativeText, nl.item(0));
				}
			}

			// Add the result of the style sheet to the document as raw html
			DocumentBuilder documentBuilder = DocumentBuilderFactory
					.newInstance().newDocumentBuilder();

			Document narrativeDocument = documentBuilder.parse(new InputSource(
					new StringReader(bos.toString())));
			Element narrativeContent = narrativeDocument.getDocumentElement();
			narrativeContent = (Element) doc.importNode(narrativeContent, true);
			narrativeText.appendChild(narrativeContent);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

	}
} // CcdaPostProcessor
