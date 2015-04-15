package org.openhealthtools.mdht.mdmi.service;

import java.util.*;
import java.io.*;
import java.net.URI;

import org.w3c.dom.*;
import org.openhealthtools.mdht.mdmi.MdmiException;
import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.util.*;
import org.openhealthtools.mdht.mdmi.service.entities.*;

public class MdmiImportExportUtility {
	public static void Export( Collection<MdmiBusinessElementReference> bers, String fileName ) {
		if( null == bers || bers.size() <= 0 )
			return;
		if( null == fileName || fileName.length() <= 0 )
			throw new IllegalArgumentException("Null or empty file name!");
		File xmlFile = new File(fileName);
		if( xmlFile.exists() && !xmlFile.isFile() )
			throw new IllegalArgumentException("Invalid file name - probably a directory!");
		BufferData bd = new BufferData();
		for( MdmiBusinessElementReference ber : bers ) {
	      AddOneBer(bd, ber);
      }
		// serialize bd
		Document doc = XmlParser.newDomDocument();
		Element root = doc.createElement("ReferentIndex");
		doc.appendChild(root);
		Element berRoot = XmlUtil.addElement(root, "MdmiNetBusinessElements");
		for( MdmiNetBusinessElement nber : bd.bers ) {
	      toXml(nber, berRoot);
      }
		Element dtRoot = XmlUtil.addElement(root, "MdmiNetDatatypes");
		Collection<MdmiNetDatatype> dts = bd.datatypes.values();
		for( Iterator<MdmiNetDatatype> it = dts.iterator(); it.hasNext(); ) {
	      MdmiNetDatatype ndt = (MdmiNetDatatype)it.next();
	      toXml(ndt, dtRoot);
      }
		
		XmlWriter xw = new XmlWriter(fileName);
		xw.write(doc, 2, 180, true);
	}
	
	public static Data Import( MessageGroup messageGroup, String fileName, int mdmiNetBerIndex ) {
		if( null == fileName || fileName.length() <= 0 )
			throw new IllegalArgumentException("Null or empty file name!");
		File file = new File(fileName);
		if( !file.exists() || !file.isFile() )
			throw new IllegalArgumentException("Missing or invalid file name!");
		Data data = new Data(messageGroup);
		XmlParser p = new XmlParser();
		Document doc = p.parse(file);
		Element root = doc.getDocumentElement();
		Element berRoot = XmlUtil.getElement(root, "MdmiNetBusinessElements");
		if( null == berRoot || XmlUtil.getElements(berRoot).size() <= 0 )
			return data;
		
		BufferData bd = new BufferData(); 
		Element dtRoot = XmlUtil.getElement(root, "MdmiNetDatatypes");
		ArrayList<Element> xmlDts = XmlUtil.getElements(dtRoot, "MdmiNetDatatype");
		for( Element xmlDt : xmlDts ) {
			MdmiNetDatatype ndt = fromXmlDt(xmlDt);
			bd.datatypes.put(ndt.getName(), ndt);
      }
		ArrayList<Element> xmlBers = XmlUtil.getElements(berRoot, "MdmiNetBusinessElement");
		for( Element xmlBer : xmlBers ) {
	      bd.bers.add(fromXmlBer(xmlBer));
      }
		
		Collection<MdmiNetDatatype> ndts = bd.datatypes.values();
		for( Iterator<MdmiNetDatatype> iterator = ndts.iterator(); iterator.hasNext(); ) {
	      MdmiNetDatatype ndt = (MdmiNetDatatype)iterator.next();
	      MdmiDatatype dt = MdmiDatatypeProxy.toModel(ndt);
	      data.datatypes.put(dt.getName(), dt);
      }
		for( Iterator<MdmiNetDatatype> iterator = ndts.iterator(); iterator.hasNext(); ) {
	      MdmiNetDatatype ndt = (MdmiNetDatatype)iterator.next();
	      MdmiDatatype dt = data.datatypes.get(ndt.getName());
	      resolveToModel(data, ndt, dt);
	      data.messageGroup.addDatatype(dt);
      }
		for( MdmiNetBusinessElement mnbe : bd.bers ) {
	      MdmiBusinessElementReference ber = toModel(data, mnbe, mdmiNetBerIndex);
	      data.bers.add(ber);
	      data.messageGroup.getDomainDictionary().addBusinessElement(ber);
      }
		return data;
	}
	
	private static void AddOneBer( BufferData bd, MdmiBusinessElementReference ber ) {
		MdmiNetBusinessElement nber = MdmiBusinessElementProxy.fromModel(ber);
		MdmiDatatype dt = ber.getReferenceDatatype();
		AddDatatype(bd, dt);
		bd.bers.add(nber);
	}

	private static void AddDatatype( BufferData bd, MdmiDatatype dt ) {
		if( bd.datatypes.containsKey(dt.getName()) || dt.isPrimitive() )
			return; // already in
		MdmiNetDatatype ndt = MdmiDatatypeProxy.fromModel(dt);
		// resolve referenced data types
		if( dt instanceof DTSDerived ) {
         DTSDerived t = (DTSDerived)dt;
         MdmiDatatype rdt = t.getBaseType();
         AddDatatype(bd, rdt);
      }
      else if( dt instanceof DTCStructured ) {
         DTCStructured t = (DTCStructured)dt;
         for( Field f : t.getFields() ) {
            MdmiDatatype rdt = f.getDatatype();
            AddDatatype(bd, rdt);
         }
      }
      else if( dt instanceof DTCChoice ) {
         DTCChoice t = (DTCChoice)dt;
         for( Field f : t.getFields() ) {
            MdmiDatatype rdt = f.getDatatype();
            AddDatatype(bd, rdt);
         }
      }
		bd.datatypes.put(ndt.getName(), ndt);
	}
	
	private static void toXml( MdmiNetBusinessElement nber, Element owner ) {
		Element root = XmlUtil.addElement(owner, "MdmiNetBusinessElement");
		Element bns = XmlUtil.addElement(root, "BerNames");
		for( MdmiNetBerName nbn : nber.getNames() ) {
	      Element n = XmlUtil.addElement(bns, "BerName");
	      n.setAttribute("name", nbn.getName());
	      if( null != nbn.getDescription() && 0 < nbn.getDescription().length() )
	      	XmlUtil.setText(n, nbn.getDescription());
      }
   	if( null != nber.getUri() && 0 < nber.getUri().length() )
   		XmlUtil.addElement(root, "uri", nber.getUri());
   	XmlUtil.addElement(root, "uniqueId", nber.getUniqueId());
   	XmlUtil.addElement(root, "datatype", nber.getDataType());
      if( null != nber.getEnumValueDescrField() && 0 < nber.getEnumValueDescrField().length() )
      	XmlUtil.addElement(root, "enumValueDescrField", nber.getEnumValueDescrField());
      if( null != nber.getEnumValueField() && 0 < nber.getEnumValueField().length() )
      	XmlUtil.addElement(root, "enumValueField", nber.getEnumValueField());
      if( null != nber.getEnumValueSet() && 0 < nber.getEnumValueSet().length() )
      	XmlUtil.addElement(root, "enumValueSet", nber.getEnumValueSet());
      if( null != nber.getEnumValueSetField() && 0 < nber.getEnumValueSetField().length() )
      	XmlUtil.addElement(root, "enumValueSetField", nber.getEnumValueSetField());
	}
	
	private static void toXml( MdmiNetDatatype ndt, Element owner ) {
		Element root = XmlUtil.addElement(owner, "MdmiNetDatatype");
		root.setAttribute("name", ndt.getName());
		XmlUtil.addElement(root, "type", ndt.getType().toString());
      if( null != ndt.getReferenceUri() && 0 < ndt.getReferenceUri().length() )
      	XmlUtil.addElement(root, "referenceUri", ndt.getReferenceUri());
      if( null != ndt.getBaseType() && 0 < ndt.getBaseType().length() )
      	XmlUtil.addElement(root, "baseType", ndt.getBaseType());
      if( null != ndt.getRestriction() && 0 < ndt.getRestriction().length() )
      	XmlUtil.addElement(root, "restriction", ndt.getRestriction());

      ArrayList<MdmiNetField> fields = ndt.getFields();
      if( null != fields && 0 < fields.size() ) {
   		Element fs = XmlUtil.addElement(root, "fields");
   		for( MdmiNetField f : fields ) {
   	      Element n = XmlUtil.addElement(fs, "field");
   	      n.setAttribute("name", f.getName());
   	      if( null != f.getDescription() && 0 < f.getDescription().length() )
   	      	XmlUtil.addElement(n, "description", f.getDescription());
   	      XmlUtil.addElement(n, "datatype", f.getDataType());
   	      if( 1 != f.getMinOccurs() )
   	      	XmlUtil.addElement(n, "minOccurs", new Integer(f.getMinOccurs()).toString() );
   	      if( 1 != f.getMaxOccurs() )
   	      	XmlUtil.addElement(n, "maxOccurs", new Integer(f.getMaxOccurs()).toString() );
         }
      }

      ArrayList<MdmiNetEnumLiteral> literals = ndt.getEnumLiterals();
      if( null != literals && 0 < literals.size() ) {
   		Element fs = XmlUtil.addElement(root, "enumLiterals");
   		for( MdmiNetEnumLiteral l : literals ) {
   	      Element n = XmlUtil.addElement(fs, "enumLiteral");
   	      n.setAttribute("name", l.getName());
   	      if( null != l.getDescription() && 0 < l.getDescription().length() )
   	      	XmlUtil.addElement(n, "description", l.getDescription());
   	      XmlUtil.addElement(n, "code", l.getCode());
         }
      }
	}
	
	private static MdmiNetBusinessElement fromXmlBer( Element root ) {
		MdmiNetBusinessElement mnbe = new MdmiNetBusinessElement();
		Element xmlBns = XmlUtil.getElement(root, "BerNames");
		ArrayList<Element> bns = XmlUtil.getElements(xmlBns, "BerName");
		for( Element bn : bns ) {
	      String name = bn.getAttribute("name");
	      Element e = XmlUtil.getElement(bn, "description");
	      String description = null;
	      if( null != e && 0 < XmlUtil.getText(e).length() )
	      	description = XmlUtil.getText(e);
	      mnbe.getNames().add(new MdmiNetBerName(name, description));
      }
		Element e = XmlUtil.getElement(root, "uri");
		if( null != e ) {
			String se = XmlUtil.getText(e);
			if( null != se && 0 < se.length() )
		   	mnbe.setUri(se);
		}
   	mnbe.setUniqueId(XmlUtil.getText(XmlUtil.getElement(root, "uniqueId")));
   	mnbe.setDataType(XmlUtil.getText(XmlUtil.getElement(root, "datatype")));
   	Element x = XmlUtil.getElement(root, "enumValueDescrField");
   	if( null != x )
   		mnbe.setEnumValueDescrField(XmlUtil.getText(x));
   	x = XmlUtil.getElement(root, "enumValueField");
   	if( null != x )
   		mnbe.setEnumValueField(XmlUtil.getText(x));
   	x = XmlUtil.getElement(root, "enumValueSet");
   	if( null != x )
   		mnbe.setEnumValueSet(XmlUtil.getText(x));
   	x = XmlUtil.getElement(root, "enumValueSetField");
   	if( null != x )
   		mnbe.setEnumValueSetField(XmlUtil.getText(x));
   	return mnbe;
	}
	
	private static MdmiNetDatatype fromXmlDt( Element root ) {
		MdmiNetDatatype dt = new MdmiNetDatatype();
		dt.setName(root.getAttribute("name"));
		Element e = XmlUtil.getElement(root, "type");
		MdmiNetDatatypeCategory t = MdmiNetDatatypeCategory.convert(XmlUtil.getText(e));
		dt.setType(t);
		if( t == MdmiNetDatatypeCategory.ENUMERATED ) {
			Element xmlLiterals = XmlUtil.getElement(root, "enumLiterals");
			if( null != xmlLiterals ) {
				ArrayList<Element> literals = XmlUtil.getElements(xmlLiterals, "enumLiteral");
				for( Element literal : literals ) {
	            MdmiNetEnumLiteral x = new MdmiNetEnumLiteral();
	            x.setName(literal.getAttribute("name"));
	            e = XmlUtil.getElement(literal, "description");
	            if( null != e && 0 < XmlUtil.getText(e).length() )
	            	x.setDescription(XmlUtil.getText(e));
	            e = XmlUtil.getElement(literal, "code");
	            if( null != e && 0 < XmlUtil.getText(e).length() )
	            	x.setCode(XmlUtil.getText(e));
	            dt.getEnumLiterals().add(x);
            }
			}
		}
		else if( t == MdmiNetDatatypeCategory.STRUCTURE || t == MdmiNetDatatypeCategory.CHOICE ) {
			Element xmlFields = XmlUtil.getElement(root, "fields");
			if( null != xmlFields ) {
				ArrayList<Element> fields = XmlUtil.getElements(xmlFields, "field");
				for( Element field : fields ) {
	            MdmiNetField x = new MdmiNetField();
	            x.setName(field.getAttribute("name"));
	            e = XmlUtil.getElement(field, "description");
	            if( null != e && 0 < XmlUtil.getText(e).length() )
	            	x.setDescription(XmlUtil.getText(e));
	            e = XmlUtil.getElement(field, "datatype");
	            x.setDataType(XmlUtil.getText(e));
	            e = XmlUtil.getElement(field, "minOccurs");
	            if( null != e && 0 < XmlUtil.getText(e).length() )
	            	x.setMinOccurs(Integer.parseInt(XmlUtil.getText(e)));
	            e = XmlUtil.getElement(field, "maxOccurs");
	            if( null != e && 0 < XmlUtil.getText(e).length() )
	            	x.setMaxOccurs(Integer.parseInt(XmlUtil.getText(e)));
	            dt.getFields().add(x);
            }
			}
		}
		else if( t == MdmiNetDatatypeCategory.DERIVED ) {
			e = XmlUtil.getElement(root, "baseType");
			dt.setBaseType(XmlUtil.getText(e));
			e = XmlUtil.getElement(root, "restriction");
			if( null != e )
				dt.setRestriction(XmlUtil.getText(e));
		}
		else if( t == MdmiNetDatatypeCategory.EXTERNAL ) {
			e = XmlUtil.getElement(root, "referenceUri");
			dt.setReferenceUri(XmlUtil.getText(e));
		}
		return dt;
	}

	private static MdmiBusinessElementReference toModel( Data data, MdmiNetBusinessElement nbr, int nameIndex ) {
   	if( nameIndex < 0 || nbr.getNames().size() <= nameIndex )
   		nameIndex = 0; // default for all that do not have multiple names
      MdmiBusinessElementReference br = new MdmiBusinessElementReference();
      String name = nbr.getNames().get(nameIndex).getName();
      String description = nbr.getNames().get(nameIndex).getDescription();
      br.setName(name);
      br.setDescription(description);
      try {
      	if( null != nbr.getUri() && 0 < nbr.getUri().length() )
      		br.setReference(new URI(nbr.getUri()));
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Invalid reference URI {0} for BER {1}.", nbr.getUri(), name);
      }
      br.setUniqueIdentifier(nbr.getUniqueId());
      br.setReadonly(true);
      br.setEnumValueDescrField(nbr.getEnumValueDescrField());
      br.setEnumValueField(nbr.getEnumValueField());
      br.setEnumValueSet(nbr.getEnumValueSet());
      br.setEnumValueSetField(nbr.getEnumValueSetField());
      MdmiDatatype t = getDatatype(data, nbr.getDataType());
      br.setReferenceDatatype(t);
      return br;
   }
	
	private static MdmiDatatype getDatatype( Data data, String typeName ) {
		DTSPrimitive p = DTSPrimitive.getByName(typeName);
		if( null != p )
			return p;
		MdmiDatatype dt = data.datatypes.get(typeName);
		if( null == dt ) {
			dt = data.messageGroup.getDatatype(typeName);
			if( null == dt )
	         throw new MdmiException("Referenced data type {0} not found!", typeName);
		}
		return dt;
	}
	
   private static void resolveToModel( Data data, MdmiNetDatatype ndt, MdmiDatatype dt ) {
      switch( ndt.getType() ) {
         case DERIVED: {
            String tn = null;
            try {
               DTSDerived t = (DTSDerived)dt;
               tn = ndt.getBaseType();
               t.setBaseType((DTSimple)getDatatype(data, tn));
               return;
            }
            catch( Exception ex ) {
               throw new MdmiException(ex, "Invalid type {0} (not DTSDerived) for data type {1}", tn, ndt.getName());
            }
         }
         case STRUCTURE: {
            String tn = null;
            Field f = null;
            try {
               DTCStructured t = (DTCStructured)dt;
               ArrayList<MdmiNetField> fields = ndt.getFields();
               for( MdmiNetField field : fields ) {
                  f = t.getField(field.getName());
                  tn = field.getDataType();
                  f.setDatatype(getDatatype(data, tn));
               }
               return;
            }
            catch( Exception ex ) {
               throw new MdmiException("Cannot find data type {0} for field {1} or data type {2}", tn
                     , f.getName(), ndt.getName());
            }
         }
         case CHOICE: {
            String tn = null;
            Field f = null;
            try {
               DTCChoice t = (DTCChoice)dt;
               ArrayList<MdmiNetField> fields = ndt.getFields();
               for( MdmiNetField field : fields ) {
                  f = t.getField(field.getName());
                  tn = field.getDataType();
                  f.setDatatype(getDatatype(data, tn));
               }
               return;
            }
            catch( Exception ex ) {
               throw new MdmiException("Can't find {0} for field {1} or data type {2}", tn, f.getName(), ndt.getName());
            }
         }
         case EXTERNAL:
         case ENUMERATED:
            return;
         default:
            throw new MdmiException("Invalid data type category found: NONE and PRIMITIVE not allowed!");
      }
   }
	
	public static class Data {
		public MessageGroup                            messageGroup;
		public ArrayList<MdmiBusinessElementReference> bers;
		public HashMap<String, MdmiDatatype>           datatypes;
		
		public Data( MessageGroup messageGroup ) {
			if( null == messageGroup )
				throw new IllegalArgumentException("Null MessageGroup!");
			this.messageGroup = messageGroup;
			bers = new ArrayList<MdmiBusinessElementReference>();
			datatypes = new HashMap<String, MdmiDatatype>();
		}
	} // Data
	
	private static class BufferData {
		public ArrayList<MdmiNetBusinessElement> bers;
		public HashMap<String, MdmiNetDatatype>  datatypes;
		
		public BufferData() {
			bers = new ArrayList<MdmiNetBusinessElement>();
			datatypes = new HashMap<String, MdmiNetDatatype>();
		}
	} // BufferData
} // MdmiImportExportUtility
