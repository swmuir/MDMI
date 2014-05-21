package org.openhealthtools.mdht.mdmiplugins.resolvers;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openhealthtools.mdht.mdmi.IPreProcessor;
import org.openhealthtools.mdht.mdmi.MdmiMessage;
import org.openhealthtools.mdht.mdmi.MdmiModelRef;
import org.openhealthtools.mdht.mdmi.util.XmlWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CcdaPreProcessor implements IPreProcessor {

	private static final String MESSAGE_GROUP = "CCDMessageGroup";
	private static final String MESSAGE_MODEL = "CCD";

	@Override
	public String getName() {
		return "CcdaPreProcessor";
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
	public void processMessage(MdmiMessage message, MdmiModelRef model,
			boolean isSource) {
		
		if (!model.getGroupName().equalsIgnoreCase(MESSAGE_GROUP)
				&& !model.getModelName().equalsIgnoreCase(MESSAGE_MODEL))
			return;

try {
		
		DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		
		org.w3c.dom.Document e1 = b.parse(new ByteArrayInputStream(message.getData()));
		
		
		
	
		
	 
		
		
		sectionNarrativeContent.clear();
		
	
		
		
		NodeList contents = e1.getDocumentElement().getElementsByTagName("content");
		for (int sectionCtr = 0; sectionCtr < contents.getLength(); sectionCtr++) {
			addSectionNarrativeContent((Element) contents.item(sectionCtr), e1);
		}

		
	
		
		
		NodeList references = e1.getDocumentElement().getElementsByTagName("reference"); //e1.getElementsByTagNameNS("*", "reference");
		
		ArrayList<Element> nodes = new ArrayList<Element>();
		for (int sectionCtr = 0; sectionCtr < references.getLength(); sectionCtr++) {
			nodes.add((Element) references.item(sectionCtr));
		}
		
	
		for (Element element : nodes) {			
				setReferencedValue(element, e1);
		}
		
 
		// Serialize it to a string and put it back
		StringWriter sw = new StringWriter();
		XmlWriter w = new XmlWriter(sw);
		w.write(e1);
		System.out.println(sw.toString());
		message.setData(sw.toString());
}
		catch (Exception exception ) {
			exception.printStackTrace();
		}
	}
	
	 

	private void setReferencedValue(Element item, Document doc) {
		
		HashMap<String,String> sectionValues = getSectionHashMap(item); 
		String key = item.getAttribute("value");
		System.out.println(key);
		if (sectionValues != null) {
			if (sectionValues.containsKey(key)) {
				String value = sectionValues.get(key);
				if (item.getParentNode() != null) {
					Node parentNode = item.getParentNode();
 					parentNode.setTextContent(value);
				} else {
//					System.out.println("aaaa");
				}
			} else {
//				System.out.println("bbbb   "+key);
			}		
		} else {
//			System.out.println("cccc");
		}
	}


	private HashMap<String,String>  getSectionHashMap(Node element) {
		
		while (element != null) {
			if ("section".equals(element.getNodeName())) {
				if (!sectionNarrativeContent.containsKey(element)) {
					sectionNarrativeContent.put(element, new HashMap<String,String>() );
				}
				return sectionNarrativeContent.get(element);
			}
			element = element.getParentNode();
		}
		
		return null;
		
	}
	
	HashMap <Node, HashMap<String,String>  > sectionNarrativeContent = new HashMap <Node, HashMap<String,String>  >();;
	
private void addSectionNarrativeContent(Element item, Document e1) {

	HashMap<String,String> sectionValues = getSectionHashMap(item); 

	if (sectionValues != null) {
			StringBuffer sb = new StringBuffer();
			NodeList nl = item.getChildNodes();
			for (int nlCtr= 0; nlCtr < nl.getLength(); nlCtr++){
				System.out.println(nl.item(nlCtr).getTextContent());
				sb.append(nl.item(nlCtr).getTextContent());				
			}		
			String key = "#" + item.getAttribute("ID");
			if (!sectionValues.containsKey(key)) {
				System.out.println(key + " :  "+ sb.toString());
				sectionValues.put(key, sb.toString());
			} else {
//				System.out.println("Error in refences");
			}
			
		}		
	}

	 
}
