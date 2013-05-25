package org.openhealthtools.mdht.mdmi.editor.be_editor.tools;

import java.util.Collection;

import javax.swing.JOptionPane;

import org.openhealthtools.mdht.mdmi.editor.be_editor.BEDisplayPanel;
import org.openhealthtools.mdht.mdmi.editor.be_editor.BEEditor;
import org.openhealthtools.mdht.mdmi.editor.be_editor.DataTypeDisplayPanel;
import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.DTSDerived;
import org.openhealthtools.mdht.mdmi.model.DTSimple;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;

/** A utility to replace all occurrences of a datatype with another */
public class DatatypeReplacement {


	// replace all occurrences of the old datatype  with the new one
	public static boolean replaceDataType(MdmiDatatype oldDatatype, MdmiDatatype newDatatype) {

		// Data Types Panel
		DataTypeDisplayPanel datatypePanel = BEEditor.getInstance().getDataTypeDisplayPanel();
		Collection<MdmiDatatype> allDatatypes = datatypePanel.getAllDatatypes();
		DatatypeReplacement.replaceAllDatatypes(allDatatypes, oldDatatype, newDatatype);
		
		// Business Elements Panel
		BEDisplayPanel bePanel = BEEditor.getInstance().getBEDisplayPanel();
		Collection<MdmiBusinessElementReference> allBERs = bePanel.getAllBusinessElementReferences();
		DatatypeReplacement.replaceAllBusinessElements(allBERs, oldDatatype, newDatatype);
		return true;
	}
	
	// replace all references to "oldDatatype" in the collection with "newDatatype"
	public static boolean replaceAllDatatypes(Collection<MdmiDatatype> datatypes, 
			MdmiDatatype oldDatatype, MdmiDatatype newDatatype) {
		
		StringBuilder errMsg = new StringBuilder();
		errMsg.append("Unable to replace datatype '").append(oldDatatype.getTypeName()).append("': ");
		
		for (MdmiDatatype datatype : datatypes) {
			
			if (datatype instanceof DTSDerived) {
				// replace BaseType
				if (!replaceBaseType((DTSDerived)datatype, oldDatatype, newDatatype, errMsg)) {
					return false;
				}
			} else if (datatype instanceof DTComplex) {
				// replace Fields
				if (!replaceFieldTypes( (DTComplex)datatype, oldDatatype, newDatatype, errMsg)) {
					return false;
				}
			}
		}
		
		return true;
	}


	// replace all references to "oldDatatype" in the collection with "newDatatype"
	public static boolean replaceAllBusinessElements(Collection<MdmiBusinessElementReference> bers, 
			MdmiDatatype oldDatatype, MdmiDatatype newDatatype) {
			
		for (MdmiBusinessElementReference ber : bers) {
			if (ber.getReferenceDatatype() == oldDatatype) {
				ber.setReferenceDatatype(newDatatype);
			}
		}
		
		return true;
	}

	// replace the old BaseType with the new datatype
	private static boolean replaceBaseType(DTSDerived derivedType,
			MdmiDatatype oldDatatype, MdmiDatatype newDatatype, StringBuilder errMsg) {

		if (derivedType.getBaseType() == oldDatatype) {
			// verify it's a Simple type
			if (!(newDatatype instanceof DTSimple)) {
				errMsg.append("the Derived datatype '").append(derivedType.getTypeName());
				errMsg.append("' has a '").append(oldDatatype.getTypeName()).append("' for the Base type.");
				errMsg.append(" The replacement is a ").append(ClassUtil.beautifyName(newDatatype.getClass()));
				errMsg.append("; only a Simple datatype is allowed for a Base type.");
				
				showWarningMessage(errMsg.toString());
				
				return false;
			}
			
			derivedType.setBaseType((DTSimple)newDatatype);
		}
		
		return true;
	}
	
	// replace the datatypes of the Fields with the new type
	private static boolean replaceFieldTypes(DTComplex complexType,
			MdmiDatatype oldDatatype, MdmiDatatype newDatatype, StringBuilder errMsg) {

		for (Field field : complexType.getFields()) {
			if (field.getDatatype() == oldDatatype) {
				field.setDatatype(newDatatype);
			}
		}
		
		return true;
	}
	
	// display the error message
	private static void showWarningMessage(String message) {
		JOptionPane.showMessageDialog(SystemContext.getApplicationFrame(), 
				message, "Invalid Data", JOptionPane.WARNING_MESSAGE);
	}
}
