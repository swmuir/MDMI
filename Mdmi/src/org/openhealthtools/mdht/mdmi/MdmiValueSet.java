package org.openhealthtools.mdht.mdmi;

import java.util.*;
import org.w3c.dom.*;
import org.openhealthtools.mdht.mdmi.util.*;

public class MdmiValueSet {
	public static final String  TAG        = "valueSet";
	private static final String VALUES_TAG = "values";

	public static class Value {
		public static final String TAG = "value";

		private String code;
		private String description;
		
		public Value( String code, String description ) {
			if( null == code || code.trim().length() <= 0 )
				throw new IllegalArgumentException("Code cannot be null or empty!");
			this.code = code;
			this.description = description;
		}
		
		protected Value( Element root ) {
			fromXml(root);
		}

		public String getCode() {
			return code;
		}

		public void setCode( String code ) {
			if( null == code || code.trim().length() <= 0 )
				throw new IllegalArgumentException("Code cannot be null or empty!");
			this.code = code;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription( String description ) {
			this.description = description;
		}
		
		protected void fromXml( Element root ) {
			code = root.getAttribute("code");
//			description = XmlUtil.getText(root);
			description = root.getAttribute("description");
		}
		
		protected void toXml( Element owner ) {
			Element root = XmlUtil.addElement(owner, TAG);
			root.setAttribute("code", code);
			if (description != null) {
				root.setAttribute("description", description);
			}
		}
	} // Value
	
	private MdmiValueSetsHandler   owner;
	private String                 name;
	private HashMap<String, Value> values;
	
	public MdmiValueSet( MdmiValueSetsHandler owner, String name ) {
		if( null == owner )
			throw new IllegalArgumentException("Owner cannot be null!");
		if( null == name || name.trim().length() <= 0 )
			throw new IllegalArgumentException("Name must be non-empty of null!");
		this.owner = owner;
		this.name = name;
		values = new HashMap<String, Value>();
	}
	
	protected MdmiValueSet( MdmiValueSetsHandler owner, Element root ) {
		if( null == owner )
			throw new IllegalArgumentException("Owner cannot be null!");
		this.owner = owner;
		values = new HashMap<String, Value>();
		fromXml(root);
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		if( null == name || name.trim().length() <= 0 )
			throw new IllegalArgumentException("Name must be non-empty of null!");
		this.name = name;
	}

	public MdmiValueSetsHandler getOwner() {
		return owner;
	}

	public ArrayList<Value> getValues() {
		return new ArrayList<Value>(values.values());
	}

	public Value getValue( String code ) {
		return values.get(code);
	}

	public boolean containsValue( String code ) {
		return null != values.get(code);
	}
	
	public void addValue( String code, String description ) {
		if( null == code || code.trim().length() <= 0 )
			throw new IllegalArgumentException("Value code cannot be null");
		Value v = values.get(code);
		if( null == v ) {
			v = new Value(code, description);
			values.put(code, v);
		}
		else {
			v.setDescription(description);
		}
	}

	public void removeValue( String name ) {
		if( null == name || name.trim().length() <= 0 )
			throw new IllegalArgumentException("Value name cannot be null");
		values.remove(name);
	}
	
	protected void fromXml( Element root ) {
		name = root.getAttribute("name");
		Element vs = XmlUtil.getElement(root, VALUES_TAG);
		if( null != vs ) {
			ArrayList<Element> evs = XmlUtil.getElements(vs, Value.TAG);
			for( int i = 0; i < evs.size(); i++ ) {
	         Element e = evs.get(i);
	         Value v = new Value(e);
	         values.put(v.getCode(), v);
         }
		}
	}
	
	protected void toXml( Element owner ) {
		Element root = XmlUtil.addElement(owner, TAG);
		root.setAttribute("name", name);
		Element vs = XmlUtil.addElement(root, VALUES_TAG);
		for( Value v : values.values() ) {
			v.toXml(vs);
		}
	}
} // MdmiValueSet
