/*******************************************************************************
* Copyright (c) 2012 Firestar Software, Inc.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Firestar Software, Inc. - initial API and implementation
*
* Author:
*     Sally Conway
*
*******************************************************************************/
package org.openhealthtools.mdht.mdmi.editor.map.tools;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.openhealthtools.mdht.mdmi.editor.common.UniqueID;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.model.Bag;
import org.openhealthtools.mdht.mdmi.model.Choice;
import org.openhealthtools.mdht.mdmi.model.DTCChoice;
import org.openhealthtools.mdht.mdmi.model.DTCStructured;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.DTExternal;
import org.openhealthtools.mdht.mdmi.model.DTSDerived;
import org.openhealthtools.mdht.mdmi.model.DTSPrimitive;
import org.openhealthtools.mdht.mdmi.model.DataRule;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.LeafSyntaxTranslator;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MdmiDomainDictionaryReference;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.MessageModel;
import org.openhealthtools.mdht.mdmi.model.MessageSyntaxModel;
import org.openhealthtools.mdht.mdmi.model.Node;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.SemanticElementSet;
import org.openhealthtools.mdht.mdmi.model.ToBusinessElement;
import org.openhealthtools.mdht.mdmi.model.ToMessageElement;
import org.openhealthtools.mdht.mdmi.model.enums.SemanticElementType;
import org.openhealthtools.mdht.mdmi.model.validate.ModelValidationResults;

//
// Build a model from Mark's spread sheet. The spread sheet must be split
// into CSV files named:
//		datatypes.csv
//    SE_def.csv
//    SE_map.csv
//
public class SpreadSheetModelBuilder {
	private static final String REQUIRED_R = "R";
	private static final String REQUIRED_O = "O";
	private static final String REQUIRED_R2 = "R2";
	
	private static final String DATATYPE_CODESET = "codeset";
	private static final String DATATYPE_ENUM = "enum";
	private static final String DATATYPE_DERIVED = "derived";
	private static final String DATATYPE_DATE = "date";
	private static final String DATATYPE_COMPLEX = "complex";
	private static final String DATATYPE_CONTAINER = "container";
	private static final String DATATYPE_PRIMITIVE = "primitive";
	
	private static final String VALUE_SET_INHERITED = "inherited";

	private static final String TRUE = "True";
	private static final String YES = "yes";
	
	public static final String DATATYPES_FILE = "datatypes.csv";
	public static final String SE_DEF_FILE   = "SE_def.csv";
	public static final String SE_MAP_FILE   = "SE_map.csv";

	public static final String DOMAIN_DICTIONARY_NAME = "SSA Referent dictionary";
	public static final String DOMAIN_DICTIONARY_REF = "";	// TODO

	
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");
	
	private File m_dir = null;

	private DTCStructured m_containerDatatype = null;
	
	// keep a list of inherited data types - these will have their URI set when the SE is defined
	private List<DTExternal> m_inheritedDataTypes = null;
	
	// keep a list of Syntax Nodes - we'll build the tree after building the SEs
	private List<Node> m_syntaxNodeList = null;
	
	// error information - CSV File and Line number
	private String m_errorLine = null;
	private ModelValidationResults m_valResults;

	/** Build a model from the files in the provided directory.
	 * 
	 * @param dir	Directory containing CSV files
	 */
	public SpreadSheetModelBuilder(File dir) {
		m_dir = dir;
	}
	
	/**
	 * Check that the directory contains the files needed.
	 */
	public static boolean checkDirectory(File dir) {
		// are all files that we need in the list?
		List<String> filesInDirectory = Arrays.asList(dir.list());
		String[] requiredFiles = { DATATYPES_FILE, /*SE_DEF_FILE,*/ SE_MAP_FILE };
		for (String requiredFileName : requiredFiles) {
			boolean foundIt = false;
			for (String fileInDir : filesInDirectory) {
				if (fileInDir.equalsIgnoreCase(requiredFileName)) {
					foundIt = true;
					break;
				}
			}
			if (!foundIt) {
				return false;
			}
		}

		return true;
	}

	// Normalize an element name (first letter capitalized, blanks dropped)
	private static String normalizeName(String name) {
		StringBuilder newName = new StringBuilder();
		for (int i=0; i<name.length(); i++) {
			char c = name.charAt(i);
			// capitalize first letter
			if (i==0) {
				c = Character.toUpperCase(c);
			}
			if (Character.isWhitespace(c)) {
				continue;
			}
			newName.append(c);
		}
		
		return newName.toString();
	}
	
	// Make a field name from an element name (first letter lower-case)
	private static String normalizeFieldName(String name) {
		String newName;
		// make first letter lower case
		if (name.length() > 0) {
			char firstLetter = name.charAt(0);
			newName = new String();
			newName += Character.toLowerCase(firstLetter);
			if (name.length() > 1) {
				newName += name.substring(1);
			}
			name = newName;
		}
		return name;
	}
	
	// fix location by stripping off "<", ">", and replacing '&' with '@'
	private static String normalizeLocation(String name) {
		StringBuilder newName = new StringBuilder();
		for (int i=0; i<name.length(); i++) {
			char c = name.charAt(i);
			if (c == '>' || c == '<') {
				continue;
			} else if (c == '&') {
				c = '@';
			}
			newName.append(c);
		}
		
		return newName.toString();
	}
	
	// fix rule by replacing ';' with a '\r\n'
	private static String normalizeRule(String name) {
		StringBuilder newName = new StringBuilder();
		for (int i=0; i<name.length(); i++) {
			char c = name.charAt(i);
			if (c == '&') {
				newName.append('\r');
				c = '\n';
			}
			newName.append(c);
		}
		
		return newName.toString();
	}

	
	
   /**
    * Builds an object graph representing an MDMI map.
    * @param dir Directory containing CSV files which 
    *               define an MDMI map.
    * @param valResults
    * @return
    */
	public List<MessageGroup> build(ModelValidationResults valResults) {

		// read files with names "datatypes.csv", "SE_def.csv", "SE_map.csv"

		// Create top Message Group
		MessageGroup messageGroup = new MessageGroup();
		messageGroup.setName("Message Group");
		messageGroup.setDefaultConstraintExprLang("NRL");
		messageGroup.setDefaultLocationExprLang("XPath");
		messageGroup.setDefaultRuleExprLang("NRL");
		ArrayList<MessageGroup> list = null;

		// save results
		m_valResults = valResults;

		// are all files that we need in the list?
		if (checkDirectory(m_dir)) {
			// okay so far
		} else {
			return list;
		}

		// Add pre-defined Datatypes
		List<? extends MdmiDatatype> preDefinedTypes = Arrays
				.asList(DTSPrimitive.ALL_PRIMITIVES);
		for (MdmiDatatype dataType : preDefinedTypes) {
			// add to the message group if not already present
			messageGroup.addDatatype(dataType);
		}

		// Add a "Container" datatype
		m_containerDatatype = new DTCStructured();
		m_containerDatatype.setTypeName(normalizeName(DATATYPE_CONTAINER));
		messageGroup.addDatatype(m_containerDatatype);
		m_containerDatatype.setOwner(messageGroup);

		// Add the dictionary
		MdmiDomainDictionaryReference domainDictionary = new MdmiDomainDictionaryReference();
		domainDictionary.setName(DOMAIN_DICTIONARY_NAME);
		String ref = DOMAIN_DICTIONARY_REF;
		if (!ref.isEmpty()) {
			try {
				URI reference = URI.create(ref);
				domainDictionary.setReference(reference);
			} catch (IllegalArgumentException ex) {
				m_valResults.addError(null, "",
						"Unable to create URI reference from  \"" + ref + "\".");
			}
		}
		messageGroup.setDomainDictionary(domainDictionary);
		domainDictionary.setMessageGroup(messageGroup);

		m_syntaxNodeList = new ArrayList<Node>();
		m_inheritedDataTypes = new ArrayList<DTExternal>();

		// datatypes
		if (!loadDatatypes(new File(m_dir, DATATYPES_FILE), messageGroup)) {
			return list;
		}
		// SE Definition (optional) - adds Semantic elements, and creates corresponding
		// syntax nodes
		File seDefFile = new File(m_dir, SE_DEF_FILE);
		if (seDefFile.exists() && !loadSEDefinition(seDefFile, messageGroup)) {
			return list;
		}
		// SE Map (Business Elements)
		if (!loadSEMapFile(new File(m_dir, SE_MAP_FILE), messageGroup)) {
			return list;
		}

		list = new ArrayList<MessageGroup>();
		list.add(messageGroup);

		return list;
	}

   // Read "datatypes.csv" file. Creates DataType and Field objects,
   // with corresponding SyntaxNodes
	private boolean loadDatatypes(File file, MessageGroup messageGroup) {
		try {
			CSVFileReader reader = new CSVFileReader(file);

			// Read File Line By Line
			List<String> stringList = null;

			int lineNo = 0;
			// Fields
			String dataTypeName = null;
			String containerName = null;
			String fieldName = null;
			String fieldType = null;
			String required = null;
			String multiple = null;
			String HL7ValueSet = null;
			String relativeXpathTag = null;
			String format = null;
			String serverDomain = null;

			// First line is the header
			if ((stringList = reader.getNextLine()) == null) {
				lineNo++;

				m_errorLine = FileAndLine(file, lineNo);
				m_valResults.addError(null, "",
								m_errorLine + s_res.getString("SpreadSheetModelBuilder.headerExpected"));
				return false;
			}

			DTComplex dataType = null;
			DTComplex currentType = null;

			Node dataTypeSyntaxNode = null; // either a Choice or a Bag
			Node currentTypeSyntaxNode = null;

			while ((stringList = reader.getNextLine()) != null) {
				lineNo++;
				// skip empty lines
				if (CSVFileReader.isEmptyList(stringList)) {
					continue;
				}

				m_errorLine = FileAndLine(file, lineNo);

				// DataType | Container | Field Name | Field Type
				// | required | multiple | HL7 ValueSet | Xpath Tag | Format |
				// Symedical Domain

				int column = 0;
				dataTypeName = CSVFileReader.getString(stringList, column++);
				containerName = CSVFileReader.getString(stringList, column++);
				fieldName = CSVFileReader.getString(stringList, column++);
				fieldType = CSVFileReader.getString(stringList, column++);
				required = CSVFileReader.getString(stringList, column++);
				multiple = CSVFileReader.getString(stringList, column++);
				HL7ValueSet = CSVFileReader.getString(stringList, column++);
				relativeXpathTag = CSVFileReader.getString(stringList, column++);
				format = CSVFileReader.getString(stringList, column++);
				serverDomain = CSVFileReader.getString(stringList, column++);

				// ////////////////////////////////
				// get maximum from Multiple
				// ///////////////////////////////
				int maxValue = 1;
				// if multiple is an integer, use it's value for maxValue
				if (!multiple.isEmpty()) {
					if (multiple.equalsIgnoreCase("unbounded")) {
						maxValue = Integer.MAX_VALUE;
					} else {
						// check for numeric value
						try {
							maxValue = Integer.valueOf(multiple);
						} catch (NumberFormatException ex) {
							// not a number
						}
					}
				}

				// /////////////////////////////////////
				// Use Required to get minimum
				// /////////////////////////////////////
				int minValue = 0;
				// use required setting to initialize min
				// O, R2 --> min = 0
				// R --> min = 1
				if (REQUIRED_R2.equalsIgnoreCase(required)
						|| REQUIRED_O.equalsIgnoreCase(required)) {
					minValue = 0;
				} else if (REQUIRED_R.equalsIgnoreCase(required)) {
					minValue = 1;
				}

				// /////////////////////////////////////////////////////
				// DataType Name - if undefined, use previous datatype
				// /////////////////////////////////////////////////////

				if (dataTypeName.isEmpty()) {
					// need at least one
					if (dataType == null) {
						// No Data Type defined for <fieldName>
						String err;
						if (fieldName.isEmpty()) {
							err = s_res.getString("SpreadSheetModelBuilder.noDatatype");
						} else {
							err = MessageFormat.format(s_res.getString("SpreadSheetModelBuilder.noDatatypeForField"),
											fieldName);
						}
						m_valResults.addError(null, "", m_errorLine + err);
						continue;
					}
					dataTypeName = dataType.getName();

				} else {
					// new datatype
					dataTypeName = normalizeName(dataTypeName);

					// Create the datatype
					MdmiDatatype dt = createDatatype(DATATYPE_COMPLEX,
							dataTypeName, true, messageGroup);
					if (dt == null || !(dt instanceof DTComplex)) {
						// already logged, clear dataType
						dataType = null;
						continue;
					}
					dataType = (DTComplex) dt;

					// create a syntax node for the datatype. When the SE's are
					// defined, we will
					// change the name of the syntax node to match the SE
					dataTypeSyntaxNode = createSyntaxNode(dataTypeName,
							dataType, minValue, maxValue, relativeXpathTag);

					// make these the ones we're interested in
					currentType = dataType;
					currentTypeSyntaxNode = dataTypeSyntaxNode;
				}

				// /////////////////////////////////////
				// Container Name (a ComplexDataype)
				// /////////////////////////////////////
				containerName = normalizeName(containerName);

				if (containerName.isEmpty()
						|| containerName.equalsIgnoreCase(dataTypeName)) {
					// check name - if container name matches data type name,
					// don't create a container
					// currentType = dataType;

				} else {
					// new container data type

					// Create the container data type
					MdmiDatatype dt = createDatatype(DATATYPE_COMPLEX,
							containerName, true, messageGroup);

					if (dt == null || !(dt instanceof DTComplex)) {
						// already logged
						continue;
					}
					DTComplex containerDataType = (DTComplex) dt;

					// add a Field to the datatype using the container name and
					// type
					Field field = new Field();
					field.setName(normalizeFieldName(containerName));
					field.setDatatype(containerDataType);
					field.setMinOccurs(minValue);
					field.setMaxOccurs(maxValue);

					field.setOwnerType(dataType);
					dataType.getFields().add(field);

					// Create a syntax node for the container type
					Node containerSyntaxNode = createSyntaxNode(
							field.getName(), containerDataType, minValue,
							maxValue, relativeXpathTag);
					// set the field name on the node
					containerSyntaxNode.setFieldName(field.getName());

					// make this node a child of the data type's node
					if (!addSyntaxNodeToParent(containerSyntaxNode,
							dataTypeSyntaxNode)) {
						continue;
					}

					// make these the one we're interested in
					currentType = containerDataType;
					currentTypeSyntaxNode = containerSyntaxNode;
				}

				// /////////////////////////////////////
				// fieldName / fieldType
				// /////////////////////////////////////

				if (fieldName.isEmpty() && !fieldType.isEmpty()) {
					// issue a warning if the FieldType is not "Container"
					if (!DATATYPE_CONTAINER.equalsIgnoreCase(fieldType)) {
						m_valResults.addWarning(currentType,
								currentType.getName(), m_errorLine
										+ "Line contains a Field Type '"
										+ fieldType + "', but no Field Name");
					}
				} else if (!fieldName.isEmpty() && fieldType.isEmpty()) {
					m_valResults.addError(currentType, currentType.getName(),
							m_errorLine + "Line contains a Field Name '"
									+ fieldName + "', but no Field Type");

				} else if (!fieldName.isEmpty() && !fieldType.isEmpty()) {
					fieldName = normalizeFieldName(fieldName);
					fieldType = normalizeName(fieldType);

					// //////////////////////////////////////////////////////
					// Field's Data Type
					// ////////////////////////////////////////////////////
					// for its name use ParentName_FieldName
					String fieldDataTypeName = currentType.getName() + "_"
							+ normalizeName(fieldName);
					MdmiDatatype fieldDataType = createDatatype(fieldType,
							fieldDataTypeName, true, messageGroup);
					if (fieldDataType == null) {
						continue;
					}

					// check fieldDataType
					if (fieldDataTypeName.equalsIgnoreCase(currentType
							.getName())) {
						m_valResults.addError(currentType, currentType.getName(),
										m_errorLine+ "Field '" + fieldDataTypeName
										+ "' has same Data Type as container. Field information will be ignored.");
						continue;
					}

					// Set TypeSpec for External datatypes
					setTypeSpec(fieldDataType, HL7ValueSet, serverDomain);

					// Add a field to the parent datatype (either the DataType
					// or Container)
					Field field = new Field();
					field.setName(fieldName);
					field.setDatatype(fieldDataType);
					field.setMinOccurs(minValue);
					field.setMaxOccurs(maxValue);

					field.setOwnerType(currentType);
					currentType.getFields().add(field);

					// Create a syntax node of the appropriate type
					Node syntaxNode = createSyntaxNode(field.getName(),
							fieldDataType, minValue, maxValue, relativeXpathTag);
					// set the field name on the node
					syntaxNode.setFieldName(field.getName());

					// make this node a child of the current node
					if (!addSyntaxNodeToParent(syntaxNode,
							currentTypeSyntaxNode)) {
						continue;
					}

					// use format
					if (!format.isEmpty()) {
						if (syntaxNode instanceof LeafSyntaxTranslator) {
							((LeafSyntaxTranslator) syntaxNode).setFormat(format);
						} else {
							// format only applies to Leafs
							m_valResults.addError(fieldDataType,fieldDataType.getName(),
									m_errorLine + "Format '" + format + "' ignored for Field datatype '"
											+ fieldDataType.getName() + "'. "
											+ "Format only applies to Leaf nodes; this element has a "
											+ ClassUtil .beautifyName(syntaxNode .getClass())
											+ " syntax node.");
						}
					}

				}

			}
			// Close the input stream
			reader.close();

			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Check if string is "Yes", or "Y", or "True" or "T"
	 * @param string
	 * @return
	 */
	private static boolean isTrue(String string) {
		return YES.equalsIgnoreCase(string)
		 			|| "Y".equalsIgnoreCase(string)
		 			|| TRUE.equalsIgnoreCase(string)
		 			|| "T".equalsIgnoreCase(string);
	}

	
	// is it a primitive
	private static MdmiDatatype getPrimitiveType(String name) {
		List<? extends MdmiDatatype> preDefinedTypes = Arrays.asList(DTSPrimitive.ALL_PRIMITIVES);
		for (MdmiDatatype primitive : preDefinedTypes) {
			if (name.equalsIgnoreCase(primitive.getTypeName())) {
				return primitive;
			}
		}
		return null;
	}
	
	/** Create SemanticElements (and associated DataTypes) from 
	 * the SE_Def file
	 * @param file		The CSV file containing the Semantic Element definitions
	 * @param messageGroup
	 * @return
	 */
	private boolean loadSEDefinition(File file, MessageGroup messageGroup) {
		
		try {
			CSVFileReader reader = new CSVFileReader(file);
			
			// Read File Line By Line
			List <String> stringList = null;
			
			int lineNo = 0;
			// Fields
			String modelName = null;
			String elementName = null;
			String SEType = null;
			String maximum = null;
			String required = null;
			String parentName = null;
			String dataTypeName = null;
			String datatypeCategory = null;
			String dataRuleText = null;
			String HL7ValueSet = null;
			String relativeXpathTag = null;
			String format = null;
			String serverDomain = null;
			
			MessageModel messageModel = null;

			// First line is the header
			if ( (stringList = reader.getNextLine()) == null) {
				lineNo++;

				m_errorLine = FileAndLine(file, lineNo);
				m_valResults.addError(null, "", m_errorLine +
						s_res.getString("SpreadSheetModelBuilder.headerExpected"));
				return false;
			}
			
			while ( (stringList = reader.getNextLine()) != null) {
				lineNo++;
				// skip empty lines
				if (CSVFileReader.isEmptyList(stringList)) {
					continue;
				}

				m_errorLine = FileAndLine(file, lineNo);

				int column = 0;

				// Message Model | Semantic Element | SE type | maximum |
				//     Required | Parent | SE (HL7) Data Type | Datatype Category |
				//     Data Rule | HL7 Value Set |Relative Xpath tag | Format | Symantic Server Domain
				
				modelName = CSVFileReader.getString(stringList, column++);
				
				// if there's a model name only - use name as message group name
				if (CSVFileReader.isEmptyList(stringList, column)) {
					messageGroup.setName(modelName);
					continue;
				}
				
				elementName = CSVFileReader.getString(stringList, column++);
				SEType = CSVFileReader.getString(stringList, column++);
				maximum = CSVFileReader.getString(stringList, column++);
				required = CSVFileReader.getString(stringList, column++);
				parentName = CSVFileReader.getString(stringList, column++);
				dataTypeName = CSVFileReader.getString(stringList, column++);
				datatypeCategory = CSVFileReader.getString(stringList, column++);
				dataRuleText = CSVFileReader.getString(stringList, column++);
				HL7ValueSet = CSVFileReader.getString(stringList, column++);
				relativeXpathTag = CSVFileReader.getString(stringList, column++);
				format = CSVFileReader.getString(stringList, column++);
				serverDomain = CSVFileReader.getString(stringList, column++);

				///////////////////////////////////////
				// Message Model name - use previous if blank
				///////////////////////////////////////
				if (modelName.isEmpty()) {
					// need at least one
					if (messageModel == null) {
						m_valResults.addError(null, "", m_errorLine +
								"A Message Model name is required");
						continue;
					}
				} else if (messageModel != null) {
					// Don't support multiple models
					m_valResults.addError(null, "", m_errorLine +
							"Multiple Message Models are not supported. Model Name '" + modelName +
							"' will be ignored.");
					continue;
					
				} else {
					modelName = normalizeName(modelName);
					messageModel = messageGroup.getModel(modelName);
					if (messageModel == null) {
						// create one
						messageModel = new MessageModel();
						messageModel.setGroup(messageGroup);
						messageModel.setMessageModelName(modelName);
						
						// For the name of the SemanticElementSet, use the name of the Message Model
						// and append "_SEset".
						SemanticElementSet seSet = new SemanticElementSet();
						seSet.setModel(messageModel);
						messageModel.setElementSet(seSet);
						seSet.setName(modelName + "_SEset");

						// For the name of the MessageSyntaxModel, use the name of the Message Model
						// and append "_syntax"
						MessageSyntaxModel msgSyntaxModel = new MessageSyntaxModel();
						messageModel.setSyntaxModel(msgSyntaxModel);
						msgSyntaxModel.setName(modelName + "_syntax");
						
						messageGroup.addModel(messageModel);
					}
				}

				///////////////////////////////////////
				// semanticElement - required
				///////////////////////////////////////
				if (elementName.isEmpty()) {
					m_valResults.addError(null, "", m_errorLine +
							"A Semantic Element name is required");
					continue;
				}
				
				// check if it exists already - it may have been created as a parent.
				// This will create one if it doesn't exist
				elementName = normalizeName(elementName);
				SemanticElement semanticElement = findSemanticElement(elementName, messageModel, true);
				SemanticElement parentElement = null;
				
				
				//////////////////////////////////////
				// SEType
				///////////////////////////////////////
				if (SEType.equalsIgnoreCase("COMPUTED")) {
					semanticElement.setSemanticElementType(SemanticElementType.COMPUTED);
				} else if (SEType.equalsIgnoreCase("LOCAL")) {
					semanticElement.setSemanticElementType(SemanticElementType.LOCAL);
				} else {
					// default
					semanticElement.setSemanticElementType(SemanticElementType.NORMAL);
				}

				///////////////////////////////////////
				// maximum
				///////////////////////////////////////
				
				int maxValue = 1;
				// if maximum is an integer, use it's value for maxValue
				if (!maximum.isEmpty()) {
					if (maximum.equalsIgnoreCase("unbounded")) {
						maxValue = Integer.MAX_VALUE;
					} else {
						// check for numeric value
						try {
							maxValue = Integer.valueOf(maximum);
						} catch (NumberFormatException ex) {
							// not a number
						}
					}
				}

				///////////////////////////////////////
				// required 
				///////////////////////////////////////
				int minValue = 0;
				// use required setting to initialize min
				// O, R2 --> min = 0
				// R     --> min = 1
				if (REQUIRED_R2.equalsIgnoreCase(required)
						 || REQUIRED_O.equalsIgnoreCase(required)) {
					minValue = 0;
				} else if (REQUIRED_R.equalsIgnoreCase(required)) {
					minValue = 1;
				}
				

				///////////////////////////////////////
				// parent
				///////////////////////////////////////
				if (!parentName.isEmpty()) {
					parentName = normalizeName(parentName);
					// find/create parent
					parentElement = findSemanticElement(parentName, messageModel, true);
					
					// add SE to parent SE
					parentElement.addChild(semanticElement);
					semanticElement.setParent(parentElement);
				}

				/////////////////////////////////////////////
				// SE (HL7) dataType and datatype Category
				/////////////////////////////////////////////
				MdmiDatatype datatype = null;


				// special case - Container - use no-value
				
				if (dataTypeName.isEmpty()) {
					// undefined datatype - allowed if category is "Container",
					// or a primitive
					if (DATATYPE_CONTAINER.equalsIgnoreCase(datatypeCategory)) {
						datatype = m_containerDatatype;
					} else {
						datatype = getPrimitiveType(datatypeCategory);
					}
					if (datatype == null) {
						// undefined datatype - error
						m_valResults.addError(semanticElement, semanticElement.getName(),
										m_errorLine + "No Data Type defined for Semantic Element '"
										+ elementName + "'" + " (category '" + datatypeCategory + "').");
						continue;
					}
				} else {
					// create the datatype if one doesn't already exist
					dataTypeName = normalizeName(dataTypeName);
					datatype = createDatatype(datatypeCategory, dataTypeName,
							true, messageGroup);

					if (datatype == null) {
						continue;
					}

					// Set TypeSpec (for external datatypes)
					setTypeSpec(datatype, HL7ValueSet, serverDomain);
				}
				
				
				semanticElement.setDatatype(datatype);
				

				///////////////////////////////////////
				// dataRule 
				///////////////////////////////////////
				if (!dataRuleText.isEmpty()) {
					// add a data rule to the SE - 
					DataRule dataRule = new DataRule();
					dataRule.setName(elementName);
					dataRule.setRule(dataRuleText);
					dataRule.setSemanticElement(semanticElement);
					if (datatype != null) {
						dataRule.addDatatype(datatype);
					}
					semanticElement.addDataRule(dataRule);
					
					// add to message group
					messageGroup.addDataRule(dataRule);
					dataRule.setScope( messageGroup );
				}
				
				//////////////////////////////////////////////////////////////////////////
				// Syntax node
				// create syntax node (with SE's name) according to the datatype -
				//////////////////////////////////////////////////////////////////////////
				Node syntaxNode = createSyntaxNode(elementName, datatype,
						minValue, maxValue, relativeXpathTag);
				syntaxNode.setSemanticElement(semanticElement);
				semanticElement.setSyntaxNode(syntaxNode);
				// set multiples
				if (syntaxNode.getMaxOccurs() > 1) {
					semanticElement.setMultipleInstances(true);
				}
				
				// check for an un-parented syntax node with the datatype name instead of the SE name
				for (Node nodeInList : m_syntaxNodeList) {
					if (nodeInList.getName().equalsIgnoreCase(datatype.getName())) {
						if (nodeInList.getParentNode() != null) {
							continue;
						}
						// take the children of this node, and copy them to the syntaxNode
						if (nodeInList instanceof Bag) {
							copyNodes(((Bag)nodeInList).getNodes(), syntaxNode);
						} else if (nodeInList instanceof Choice) {
							copyNodes(((Choice)nodeInList).getNodes(), syntaxNode);
						}
						break;
					}
				}
				
				// use format
				if (!format.isEmpty()) {
					if (syntaxNode instanceof LeafSyntaxTranslator) {
						((LeafSyntaxTranslator)syntaxNode).setFormat(format);
					} else {
						// format only applies to Leafs
   					m_valResults.addError(semanticElement, semanticElement.getName(), m_errorLine +
   							"Format '" + format + "' ignored for Semantic Element '" + elementName + "'. " +
   							"Format only applies to Leaf nodes; this element has a " + 
   							ClassUtil.beautifyName(syntaxNode.getClass()) + " syntax node.");
					}
				}
				
			}
			// Close the input stream
			reader.close();
			
			// check for null (empty file)
			if (messageModel == null) {
				return false;
			}

			// Now, build syntax tree
			MessageSyntaxModel syntaxModel = messageModel.getSyntaxModel();
			Bag root = null;
			
			// look at all SEs
			for (SemanticElement se : messageModel.getElementSet().getSemanticElements()) {
				Node syntaxNode = se.getSyntaxNode();
				// we have a node, but the node doesn't have a parent
				if (syntaxNode != null && syntaxNode.getParentNode() == null)
				{
	   				SemanticElement parentElement = se.getParent();
	   				// see if we can set the syntax node's parent
	   				if (parentElement != null) {
	   					// set SE's node to be a child of SE's parent's node
	   					Node parentSyntaxNode = parentElement.getSyntaxNode();
	   					if (parentSyntaxNode != null) {
	   						addSyntaxNodeToParent(syntaxNode, parentSyntaxNode);
	   					}
	   				}
				}
			}
			
			// check for an un-parented syntax nodes - add to root
			for (Node nodeInList : m_syntaxNodeList) {
				if (nodeInList.getParentNode() == null && nodeInList.getSemanticElement() != null) {
					if (root == null && nodeInList instanceof Bag) {
	   					// make it the root
	   					root = (Bag)nodeInList;
	   					syntaxModel.setRoot(root);
	   					root.setSyntaxModel(syntaxModel);
	   				} else {
	   					addSyntaxNodeToParent(nodeInList, root);
	   				}
				}
			}

			
			// warn on un-defined datatypes
			for (DTExternal inheritedType : m_inheritedDataTypes) {
				m_valResults.addError(inheritedType, inheritedType.getName(),
						"No valueSet/domain can be identified for this data type.");
			}
			
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	/** Create a new SyntaxNode using the supplied data. The SyntaxNode is not
	 * added to the tree at this point. It is added to the m_syntaxNodeList for
	 * later use.
	 * @param elementName
	 * @param datatype
	 * @param minValue
	 * @param maxValue
	 * @param location
	 * @return
	 */
	private Node createSyntaxNode(String nodeName, MdmiDatatype datatype,
			int minValue, int maxValue, String location) {
		// DTCChoice     -> Choice
		// DTCStructured -> Bag
		// anything else -> LeafSyntaxTranslator
		Node syntaxNode = null;
		if (datatype instanceof DTCChoice) {
			syntaxNode = new Choice();
		} else if (datatype instanceof DTCStructured) {
			syntaxNode = new Bag();
		} else {
			syntaxNode = new LeafSyntaxTranslator();
		}

		syntaxNode.setName(nodeName);
		syntaxNode.setMinOccurs(minValue);
		syntaxNode.setMaxOccurs(maxValue);
		
		// fix location 
		location = normalizeLocation(location);
		syntaxNode.setLocation(location);
		
		m_syntaxNodeList.add(syntaxNode);
		return syntaxNode;
	}
	
	// add the syntax node to the parent. The parent must be a Bag or Choice
	private boolean addSyntaxNodeToParent(Node childNode, Node parentNode) {
		if (parentNode instanceof Bag) {
			((Bag)parentNode).addNode(childNode);
			childNode.setParentNode(parentNode);
		} else if (parentNode instanceof Choice) {
			((Choice)parentNode).addNode(childNode);
			childNode.setParentNode(parentNode);
		} else {
			m_valResults.addError(parentNode, parentNode.getName(), m_errorLine +
					"Syntax Node '" + parentNode.getName() + "', is a " +
					ClassUtil.beautifyName(parentNode.getClass()) + 
					". It should be a " + ClassUtil.beautifyName(Bag.class) + " or a " +
					ClassUtil.beautifyName(Choice.class));
			return false;
		}
		return true;
	}
	
	/** Create a new MdmiDataype if one doesn't exist.
	 * 
	 * @param category	one of COMPLEX, DERIVED, DERIVED_DATE, ENUM, CODESET or a primitive
	 * @param dataTypeName	name of datatype
	 * @param multiple
	 * @param messageGroup
	 * @return
	 */
	private MdmiDatatype createDatatype(String category, String dataTypeName, boolean multiple,
			MessageGroup messageGroup) {
		MdmiDatatype datatype = null;
		
		// either category or name is CONTAINER, and the other is empty
		if ( (category.isEmpty() && DATATYPE_CONTAINER.equalsIgnoreCase(dataTypeName)) ||
				(dataTypeName.isEmpty() && DATATYPE_CONTAINER.equalsIgnoreCase(category)) ) {
			datatype = m_containerDatatype;
			return datatype;
			
		} else if (category.isEmpty()) {
			// If no category is specified, the name must be a primitive
			datatype = getPrimitiveType(dataTypeName);
				
			if (datatype == null) {
				m_valResults.addError(null, "", m_errorLine +
					"No Datatype Category defined for datatype '" + dataTypeName + "'");
				return null;
			}

		} else if ((datatype=getPrimitiveType(category)) != null) {
			// is category a primitive
			return datatype;
			
		// create a datatype based on the category
		} else {
			if ( DATATYPE_PRIMITIVE.equalsIgnoreCase(category)) {
				datatype = getPrimitiveType(dataTypeName);
				if (datatype == null) {
					m_valResults.addError(null, "", m_errorLine +
						"There is no " + DATATYPE_PRIMITIVE + " data type named '" + dataTypeName + "'");
					return null;
				}
			} else if (category.equalsIgnoreCase(DATATYPE_ENUM) ||
					category.equalsIgnoreCase(DATATYPE_CODESET)) {
				// was external, now String
				datatype = new DTExternal();
				//return  DTSPrimitive.STRING;
				
			} else if (category.toLowerCase().startsWith(DATATYPE_DERIVED)) {
				// derived 
				datatype = new DTSDerived();
				
				if (category.toLowerCase().endsWith(DATATYPE_DATE)) {
					// derived from DateTime
					((DTSDerived) datatype).setBaseType(DTSPrimitive.DATETIME);
				} else {
					// default is String
					((DTSDerived) datatype).setBaseType(DTSPrimitive.STRING);
				}
				((DTSDerived)datatype).setRestriction("\"" + dataTypeName + "\"");

			} else if (category.equalsIgnoreCase(DATATYPE_COMPLEX)){
				//  look at multiple/min/max/required
				// DTCChoice - one choice from two or more possible fields.
				//  or
				// DTCStructured - one or more named fields.
				if (multiple) {
					datatype = new DTCStructured();
				} else {
					datatype = new DTCChoice();
				}
			} else if (category.equalsIgnoreCase(DATATYPE_CONTAINER)) {
				// DTCStructured
				datatype = new DTCStructured();
				
			} else {
				// punt - create a derived type
				datatype = new DTSDerived();
				((DTSDerived)datatype).setBaseType(DTSPrimitive.STRING);
				((DTSDerived)datatype).setRestriction("\"" + dataTypeName + "\"");
			}

			
			// add/check
			MdmiDatatype existingType = findDataType(dataTypeName, messageGroup);
			if (existingType != null) {
				// if datatype already exists, make sure it's the same type
				if (!existingType.getClass().equals(datatype.getClass())) {
					m_valResults.addError(existingType, dataTypeName, m_errorLine +
							"WARNING: A DataType named '" + dataTypeName + "' already exists, but is a " +
							ClassUtil.beautifyName(existingType.getClass()) + ", instead of a " +
							ClassUtil.beautifyName(datatype.getClass()) );
				}
				return existingType;
				
			} else {
				// if it doesn't exist, add it
				datatype.setTypeName(dataTypeName);
				messageGroup.addDatatype(datatype);
				datatype.setOwner(messageGroup);
				
			}
		}
		
		return datatype;
	}

	/** Find a SemanticElement in the model - create if it doesn't exist
	 * @param elementName - case insensitive
	 * @param messageModel
	 * @return
	 */
	private SemanticElement findSemanticElement(String elementName, MessageModel messageModel, boolean create) {
		SemanticElement semanticElement = null;
		
		SemanticElementSet set = messageModel.getElementSet();
		if (set == null) {
			if (create) {
	   			set = new SemanticElementSet();
	   			messageModel.setElementSet(set);
			} else {
				return null;
			}
		}
		for (SemanticElement se : set.getSemanticElements()) {
			if (elementName.equalsIgnoreCase(se.getName())) {
				semanticElement = se;
				break;
			}
		}

		// add if it doesn't exist
		if (semanticElement == null && create) {
			semanticElement = new SemanticElement();
			semanticElement.setMultipleInstances(true);	// should be on by default
			semanticElement.setName(elementName);
			set.addSemanticElement(semanticElement);
			semanticElement.setElementSet(set);
		}
		return semanticElement;
	}


	/** Find a Dataype in the message group
	 * @param typeName - case insensitive
	 * @param group	MessageGroup
	 * @return
	 */
	private MdmiDatatype findDataType(String typeName, MessageGroup group) {
		for (MdmiDatatype datatype : group.getDatatypes()) {
			if (datatype.getTypeName().equals(typeName)) {
				return datatype;
			}
		}
		return null;
	}
	
	/** Find a BusinessElementReference. Create a new one if one doesn't exist
	 * @param beRefName 
	 * @param messageGroup 
	 * @param datatypeName 
	 * @param datatypeCategory 
	 * 
	 * @return
	 */
	private MdmiBusinessElementReference findBusinessElementRef(String beRefName, String datatypeName, 
			String datatypeCategory, MessageGroup messageGroup, String HL7ValueSet, String serverDomain) {

		// find one
		MdmiDomainDictionaryReference domainDictionary = messageGroup.getDomainDictionary();
		MdmiBusinessElementReference businessElement = domainDictionary.getBusinessElement(beRefName);
		
		if (businessElement == null)
		{
			// create one
			businessElement = new MdmiBusinessElementReference();
			businessElement.setName(beRefName);
			businessElement.setUniqueIdentifier(beRefName);
			businessElement.setDomainDictionaryReference(domainDictionary);
			domainDictionary.addBusinessElement(businessElement);
		}
		

		///////////////////////////////////
		// BER Datatype
		//////////////////////////////////
		if (!datatypeName.isEmpty()) {
			datatypeName = normalizeName(datatypeName);

			if (datatypeCategory.isEmpty() && !DATATYPE_CONTAINER.equalsIgnoreCase(datatypeName)) {
				m_valResults.addError(businessElement, businessElement.getName(), m_errorLine +
						"No Datatype Category has been specified for dataype '" + datatypeName + "'");
				return businessElement;
			}
			
			// check type
			MdmiDatatype existingType = businessElement.getReferenceDatatype();
			if (existingType == null) {
				MdmiDatatype datatype = createDatatype(datatypeCategory, datatypeName, true,
						messageGroup);
				
				if (datatype != null) {
					// Set TypeSpec for External datatypes
					setTypeSpec(datatype, HL7ValueSet, serverDomain);
					
					businessElement.setReferenceDatatype(datatype);
				}
			} else {
				if (!datatypeName.equals(existingType.getName())) {
					m_valResults.addError(businessElement, businessElement.getName(), m_errorLine +
							"WARNING: BusinessElementReference already has a dataype named '" + existingType.getName() +
							"'. Dataype '" + datatypeName + "' will be ignored.");
				}
			}
		}
		
		return businessElement;
	}

	// used for error handling
	public static String FileAndLine(File file, int lineNo)
	{
		String fileAndLine = file.getName() + " : " + lineNo + " - ";
		return fileAndLine;
	}
	
	// load the SE_Map file - creates BusinessElementReferences 
	private boolean loadSEMapFile(File file, MessageGroup messageGroup) {
		try {
			CSVFileReader reader = new CSVFileReader(file);
			
			// Read File Line By Line
			List <String> stringList = null;
			
			int lineNo = 0;

			String elementName = null;
			String beRefName = null;
			String datatypeName = null;
			String datatypeCategory = null;
			String iso = null;
			String SEtoBER = null;
			String SEfromBER = null;
			String HL7ValueSet = null;
			String serverDomain = null;
			String description = null;
			String uidString = null;

			// First line is the header
			if ( (stringList = reader.getNextLine()) == null) {
				lineNo++;

				m_errorLine = FileAndLine(file, lineNo);
				m_valResults.addError(null, "", m_errorLine +
						s_res.getString("SpreadSheetModelBuilder.headerExpected"));
				return false;
			}

			while ( (stringList = reader.getNextLine()) != null) {
				lineNo++;
				// skip empty lines
				if (CSVFileReader.isEmptyList(stringList)) {
					continue;
				}
				
				m_errorLine = FileAndLine(file, lineNo);
				
				// Semantic Element | Business Element Ref | BER Datatype Name | BER Datatype Category
				//      | Iso | SE to BER | SE From BER | HL7 Value Set | Symantic Server Domain
				//      | Description | UID 

				int column = 0;
				elementName = CSVFileReader.getString(stringList, column++);
				beRefName = CSVFileReader.getString(stringList, column++);
				datatypeName = CSVFileReader.getString(stringList, column++);
				datatypeCategory = CSVFileReader.getString(stringList, column++);
				iso = CSVFileReader.getString(stringList, column++);
				SEtoBER = CSVFileReader.getString(stringList, column++);
				SEfromBER = CSVFileReader.getString(stringList, column++);
				HL7ValueSet = CSVFileReader.getString(stringList, column++);
				serverDomain = CSVFileReader.getString(stringList, column++);
				description = CSVFileReader.getString(stringList, column++);
				uidString = CSVFileReader.getString(stringList, column++);
				
				/////////////////////////////////////
				// Semantic Element Name
				/////////////////////////////////////
				SemanticElement semanticElement = null;
				if (!elementName.isEmpty()) {
					elementName = normalizeName(elementName);
					// find it
					for (MessageModel messageModel : messageGroup.getModels()) {
						semanticElement = findSemanticElement(elementName, messageModel, false);
						if (semanticElement != null) {
							break;
						}
					}
					if (semanticElement == null) {
						// non-fatal
						m_valResults.addError(null, "", m_errorLine +
   							"Semantic Element '" + elementName + "' not defined in " + SE_DEF_FILE
   							+ ". Check spelling and/or capitalizations.");
					}
				}

				/////////////////////////////////////
				// Business Element Reference
				/////////////////////////////////////
				if (beRefName.isEmpty()) {
					m_valResults.addError(null, "", m_errorLine +
							"No Business Element name has been specified for '" + elementName + "'");
					continue;
				}
				
				beRefName = normalizeName(beRefName);
				MdmiBusinessElementReference businessElement = findBusinessElementRef(beRefName, datatypeName,
						datatypeCategory, messageGroup, HL7ValueSet, serverDomain);
				

				if (businessElement == null) {
					continue;
				}
				
				////////////////////////////////////
				// iso
				////////////////////////////////////
				boolean isIso = isTrue(iso);
				
				////////////////////////////////////
				// SEtoBER, SEfromBER 
				// if ISO, then these are both true
				////////////////////////////////////
				boolean createSEtoBER = isIso || !SEtoBER.isEmpty();
				boolean createSEfromBER = isIso || !SEfromBER.isEmpty();
				
				// create a From or To element
				if (createSEtoBER) {
				   ToBusinessElement conversionRule = new ToBusinessElement();
				   conversionRule.setName("To_" + beRefName);
				   conversionRule.setBusinessElement(businessElement);
				   if (!SEtoBER.isEmpty()) {
					   conversionRule.setRule(normalizeRule(SEtoBER));
				   }
				   if (semanticElement != null) {
					   conversionRule.setOwner(semanticElement);
					   semanticElement.addFromMdmi(conversionRule);
				   }
				}
				if (createSEfromBER) {
					ToMessageElement conversionRule = new ToMessageElement();
					conversionRule.setName("From_" + beRefName);
					conversionRule.setBusinessElement(businessElement);
					   if (!SEfromBER.isEmpty()) {
						   conversionRule.setRule(normalizeRule(SEfromBER));
					   }
					if (semanticElement != null) {
						conversionRule.setOwner(semanticElement);
						semanticElement.addToMdmi(conversionRule);
					}
				}
				
				// non-LOCAL SE's require a To and/or a From
				if (semanticElement != null && semanticElement.getSemanticElementType() != SemanticElementType.LOCAL)
				{
					if (!createSEtoBER && !createSEfromBER) {
						m_valResults.addError(semanticElement,semanticElement.getName(),
										m_errorLine+ "WARNING: Neither 'SE to BER', nor 'SE From BER' are defined for a "
											+ semanticElement.getSemanticElementType()+ " element type.");
					}
				}
				
				///////////////////////////////////////////////////////
				// Description and UID
				///////////////////////////////////////////////////////
				if (!description.isEmpty()) {
					businessElement.setDescription(description);
				}
				if (!uidString.isEmpty()) {
					// check form
					if (UniqueID.isUUID(uidString)) {
						businessElement.setUniqueIdentifier(uidString);
					} else {
						m_valResults.addError(businessElement, "", m_errorLine +
								"Invalid UUID, '" + uidString + "' for '" + beRefName + "'");
					}
				}
			}
			// Close the input stream
			reader.close();
			
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}
	
	// Use the valueSet and/or domain to define the TypeSpec for an external
	// dataype.
	// This will log an error if the datatype is not DTExternal
	public boolean setTypeSpec(MdmiDatatype datatype, String valueSet,
			String domainName) {
		boolean worked = true;

		if (valueSet.isEmpty() && domainName.isEmpty()) {
			// nothing to do

		} else if (datatype instanceof DTExternal) {
			if (VALUE_SET_INHERITED.equalsIgnoreCase(valueSet)) {
				// save these, we'll need to look them up later
				m_inheritedDataTypes.add((DTExternal) datatype);

			} else {

				// create URI
				// mdmi://datatypes/deferredEnum?type=<dataType>&valueSet=<valueSet>&domain=<domain>
				String typeSpecString = GenerateTypeSpecDialog.createTypeSpecString(datatype.getName(), valueSet, domainName);
				try {
					URI typeSpec = URI.create(typeSpecString);
					((DTExternal) datatype).setTypeSpec(typeSpec);
				} catch (IllegalArgumentException ex) {
					worked = false;
					m_valResults.addError(datatype, datatype.getName(),
							m_errorLine + "Unable to create URI  \""
									+ typeSpecString + "\".");
				}
			}

		} else if (datatype instanceof DTComplex) {
			// Not an External dataype - use the valueSet and domainName to set
			// the fields' datatypes
			for (Field field : ((DTComplex) datatype).getFields()) {
				MdmiDatatype fieldDatatype = field.getDatatype();
				if (!(fieldDatatype instanceof DTExternal)) {
					continue;
				}
				// If the field's type was inherited, remove it from the
				// inherited list,
				// and set its TypeSpec using the Value Set and Domain Name
				if (m_inheritedDataTypes.remove(fieldDatatype)) {
					setTypeSpec(fieldDatatype, valueSet, domainName);
				}
			}

		} else {
			// value set and/or domain name unused
			StringBuilder buf = new StringBuilder(m_errorLine);
			if (!valueSet.isEmpty()) {
				buf.append("An unused Value Set (").append(valueSet)
						.append(") is defined. ");
			}
			if (!domainName.isEmpty()) {
				buf.append("An unused Domain Name (").append(domainName)
						.append(") is defined. ");
			}
			m_valResults.addError(datatype, datatype.getName(), buf.toString());
		}

		return worked;
	}
	
	// Make a deep-copy of all nodes in the list, and add them to the new parent.
	private void copyNodes(List<Node> nodeList, Node newParent) {
		for (Node child : nodeList) {
			try {
				Node copy = (Node) ClassUtil.clone(child); // this is a shallow
															// copy
				addSyntaxNodeToParent(copy, newParent);

				// copy all children as well
				if (child instanceof Bag) {
					copyNodes(((Bag) child).getNodes(), copy);
				} else if (copy instanceof Choice) {
					copyNodes(((Choice) child).getNodes(), copy);
				}
			} catch (Exception e) {
				e.printStackTrace();
				m_valResults.addError(null, "", "Error copying syntax node: "
						+ e.getLocalizedMessage());
			}
		}
	}

}
