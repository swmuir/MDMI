package org.openhealthtools.mdht.mdmi.editor.be_editor;

import java.net.URI;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MdmiDomainDictionaryReference;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.service.MdmiBusinessElementProxy;
import org.openhealthtools.mdht.mdmi.service.MdmiDatatypeProxy;

/* Interface to service */
public class ServerInterface {
	/* Maximum number of elements that will be returned from the getAll methods */
	public static int MAX_SEARCH = 100;

	private boolean m_connected;
	
	private MessageGroup m_messageGroup;
	private MdmiDatatypeProxy m_datatypeProxy;
	private MdmiBusinessElementProxy m_businessElementProxy;
	
	private static ServerInterface s_instance = null;
	
	public static ServerInterface getInstance() {
		if (s_instance == null) {
			s_instance = new ServerInterface();
		}
		return s_instance;
	}
	
	
	private ServerInterface() {
		
	}

	public boolean connect(URI uri, String token) {
		m_messageGroup = new MessageGroup();
		m_messageGroup.setName("Message Group");
		m_messageGroup.setDomainDictionary(new MdmiDomainDictionaryReference());
		
        m_datatypeProxy = new MdmiDatatypeProxy(uri, token, m_messageGroup);
        m_businessElementProxy = new MdmiBusinessElementProxy(uri, token, m_messageGroup, m_datatypeProxy);

		m_connected = true;
		return m_connected;
	}

	public boolean disconnect() {
		// TODO - do it
		m_connected = false;
		
		return true;
	}
	
	public boolean isConnected() {
		return m_connected;
	}
	
	/** Get the message group */
	public MessageGroup getMessageGroup() {
		return m_messageGroup;
	}
	
	/** Get first 100 datatypes. The Position object will be set up for the next batch */
	public MdmiDatatype[] getAllDatatypes(RetrievePosition pos,
			String searchExpr) {
		pos.currPos = pos.nextPos;
		MdmiDatatype[] datatypes = m_datatypeProxy.getAll(pos.currPos);
		int numFound = datatypes.length;
		// increment position
		pos.nextPos += numFound;
		
		// test each
		if (numFound > 0 && searchExpr != null && !searchExpr.isEmpty()) {
			Pattern pattern = Pattern.compile(toRegex(searchExpr), Pattern.CASE_INSENSITIVE);
			
			ArrayList<MdmiDatatype> matching = new ArrayList<MdmiDatatype>();

			for (MdmiDatatype datatype : datatypes) {
				String name = datatype.getName();
				if (name != null && pattern.matcher(name).matches()) {
					// set owner
					datatype.setOwner(m_messageGroup);
					matching.add(datatype);
				}
			}
			
			// TODO: look for more
			//if (matching.size() < 100) 
			
			// convert to array
			datatypes = matching.toArray(new MdmiDatatype[0]);
			
		}
		
		return datatypes;
	}
	
	/** Get a single */
	public MdmiDatatype  getDatatype(String value) {
		try {
			MdmiDatatype datatype = m_datatypeProxy.get(value);
			return datatype;
		} catch( Exception ignoreDeleteFails ) {}
		return null;
	}
	
	/** add*/
	public MdmiDatatype  addDatatype(MdmiDatatype datatype) {
		MdmiDatatype added = m_datatypeProxy.add(datatype);
		return added;
	}
	/** modify*/
	public MdmiDatatype  updateDatatype(MdmiDatatype datatype) {
		MdmiDatatype added = m_datatypeProxy.update(datatype);
		return added;
	}
	/** delete*/
	public void  deleteDatatype(MdmiDatatype datatype) {
		m_datatypeProxy.delete(datatype);
	}

	
	/** Get 100 business element references. The Position object will be set up for the next batch */
	public MdmiBusinessElementReference[] getAllBusinessElementReferences(RetrievePosition pos,
			String searchExpr) {
		pos.currPos = pos.nextPos;
		MdmiBusinessElementReference[] bers = m_businessElementProxy.getAll(pos.currPos);
		int numFound = bers.length;
		// increment position
		pos.nextPos += numFound;
		
		// test each
		if (numFound > 0 && searchExpr != null && !searchExpr.isEmpty()) {
			Pattern pattern = Pattern.compile(toRegex(searchExpr), Pattern.CASE_INSENSITIVE);
			
			ArrayList<MdmiBusinessElementReference> matching = new ArrayList<MdmiBusinessElementReference>();

			for (MdmiBusinessElementReference ber : bers) {
				String name = ber.getName();
				if (name != null && pattern.matcher(name).matches()) {
					ber.setDomainDictionaryReference(m_messageGroup.getDomainDictionary());
					matching.add(ber);
				}
			}
			
			// TODO: look for more
			//if (matching.size() < 100) 
			
			// convert to array
			bers = matching.toArray(new MdmiBusinessElementReference[0]);
			
		}
		return bers;
	}
	
	/** Get a single */
	public MdmiBusinessElementReference  getBusinessElementReference(String value) {
		try {
			MdmiBusinessElementReference ber = m_businessElementProxy.get(value);
			return ber;
		} catch( Exception ignoreDeleteFails ) {}
		return null;
	}
	
	/** add*/
	public MdmiBusinessElementReference  addBusinessElementReference(MdmiBusinessElementReference ber) {
		MdmiBusinessElementReference added = m_businessElementProxy.add(ber);
		return added;
	}
	/** update*/
	public MdmiBusinessElementReference  updateBusinessElementReference(MdmiBusinessElementReference ber) {
		MdmiBusinessElementReference added = m_businessElementProxy.update(ber);
		return added;
	}
	/** delete*/
	public void  deleteBusinessElementReference(MdmiBusinessElementReference ber) {
		m_businessElementProxy.delete(ber);
	}

	/** Convert the user-entered text into a regular expression */
	public static String toRegex(String text) {
		StringBuilder buf = new StringBuilder();

		// convert "*" to ".*"
		for (int i=0; i<text.length(); i++) {
			char c = text.charAt(i);
			if (c == '*') {
				// avoid multiple '*'s
				if (i > 0 && text.charAt(i-1) == '*') {
					continue;
				}
				buf.append('.');
			}
			buf.append(c);
		}

		//		// begin and end with ".*",
		//		if (!buf.toString().startsWith(".*")) {
		//			buf.insert(0, ".*");
		//		}
		//		if (!buf.toString().endsWith(".*")) {
		//			buf.append(".*");
		//		}
		return buf.toString();
	}

	//
	// Used to manage retrieval
	//
	public static class RetrievePosition {
		int currPos = 0;
		int nextPos = 0;
		
		int numFound() { return nextPos - currPos; }
	}
}
