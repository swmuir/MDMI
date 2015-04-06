/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.openhealthtools.mdht.mdmi.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.openhealthtools.mdht.mdmi.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class MDMIFactoryImpl extends EFactoryImpl implements MDMIFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static MDMIFactory init() {
		try {
			MDMIFactory theMDMIFactory = (MDMIFactory)EPackage.Registry.INSTANCE.getEFactory(MDMIPackage.eNS_URI);
			if (theMDMIFactory != null) {
				return theMDMIFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new MDMIFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MDMIFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case MDMIPackage.MESSAGE_MODEL: return createMessageModel();
			case MDMIPackage.MESSAGE_SYNTAX_MODEL: return createMessageSyntaxModel();
			case MDMIPackage.BAG: return createBag();
			case MDMIPackage.CHOICE: return createChoice();
			case MDMIPackage.LEAF_SYNTAX_TRANSLATOR: return createLeafSyntaxTranslator();
			case MDMIPackage.MESSAGE_GROUP: return createMessageGroup();
			case MDMIPackage.DATA_RULE: return createDataRule();
			case MDMIPackage.SEMANTIC_ELEMENT_SET: return createSemanticElementSet();
			case MDMIPackage.SEMANTIC_ELEMENT: return createSemanticElement();
			case MDMIPackage.SIMPLE_MESSAGE_COMPOSITE: return createSimpleMessageComposite();
			case MDMIPackage.MESSAGE_COMPOSITE: return createMessageComposite();
			case MDMIPackage.SEMANTIC_ELEMENT_BUSINESS_RULE: return createSemanticElementBusinessRule();
			case MDMIPackage.SEMANTIC_ELEMENT_RELATIONSHIP: return createSemanticElementRelationship();
			case MDMIPackage.MDMI_BUSINESS_ELEMENT_REFERENCE: return createMDMIBusinessElementReference();
			case MDMIPackage.MDMI_BUSINESS_ELEMENT_RULE: return createMDMIBusinessElementRule();
			case MDMIPackage.TO_BUSINESS_ELEMENT: return createToBusinessElement();
			case MDMIPackage.TO_SEMANTIC_ELEMENT: return createToSemanticElement();
			case MDMIPackage.MDMI_DOMAIN_DICTIONARY_REFERENCE: return createMDMIDomainDictionaryReference();
			case MDMIPackage.MDMI_EXPRESSION: return createMDMIExpression();
			case MDMIPackage.KEYWORD: return createKeyword();
			case MDMIPackage.MDMI_DATATYPE: return createMDMIDatatype();
			case MDMIPackage.DTS_PRIMITIVE: return createDTSPrimitive();
			case MDMIPackage.DTC_STRUCTURED: return createDTCStructured();
			case MDMIPackage.FIELD: return createField();
			case MDMIPackage.DT_EXTERNAL: return createDTExternal();
			case MDMIPackage.DTS_DERIVED: return createDTSDerived();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
			case MDMIPackage.MESSAGE_ELEMENT_TYPE:
				return createMessageElementTypeFromString(eDataType, initialValue);
			case MDMIPackage.URI:
				return createURIFromString(eDataType, initialValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
			case MDMIPackage.MESSAGE_ELEMENT_TYPE:
				return convertMessageElementTypeToString(eDataType, instanceValue);
			case MDMIPackage.URI:
				return convertURIToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MessageModel createMessageModel() {
		MessageModelImpl messageModel = new MessageModelImpl();
		return messageModel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MessageSyntaxModel createMessageSyntaxModel() {
		MessageSyntaxModelImpl messageSyntaxModel = new MessageSyntaxModelImpl();
		return messageSyntaxModel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Bag createBag() {
		BagImpl bag = new BagImpl();
		return bag;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Choice createChoice() {
		ChoiceImpl choice = new ChoiceImpl();
		return choice;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public LeafSyntaxTranslator createLeafSyntaxTranslator() {
		LeafSyntaxTranslatorImpl leafSyntaxTranslator = new LeafSyntaxTranslatorImpl();
		return leafSyntaxTranslator;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MessageGroup createMessageGroup() {
		MessageGroupImpl messageGroup = new MessageGroupImpl();
		return messageGroup;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DataRule createDataRule() {
		DataRuleImpl dataRule = new DataRuleImpl();
		return dataRule;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SemanticElementSet createSemanticElementSet() {
		SemanticElementSetImpl semanticElementSet = new SemanticElementSetImpl();
		return semanticElementSet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SemanticElement createSemanticElement() {
		SemanticElementImpl semanticElement = new SemanticElementImpl();
		return semanticElement;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SimpleMessageComposite createSimpleMessageComposite() {
		SimpleMessageCompositeImpl simpleMessageComposite = new SimpleMessageCompositeImpl();
		return simpleMessageComposite;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MessageComposite createMessageComposite() {
		MessageCompositeImpl messageComposite = new MessageCompositeImpl();
		return messageComposite;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SemanticElementBusinessRule createSemanticElementBusinessRule() {
		SemanticElementBusinessRuleImpl semanticElementBusinessRule = new SemanticElementBusinessRuleImpl();
		return semanticElementBusinessRule;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SemanticElementRelationship createSemanticElementRelationship() {
		SemanticElementRelationshipImpl semanticElementRelationship = new SemanticElementRelationshipImpl();
		return semanticElementRelationship;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MDMIBusinessElementReference createMDMIBusinessElementReference() {
		MDMIBusinessElementReferenceImpl mdmiBusinessElementReference = new MDMIBusinessElementReferenceImpl();
		return mdmiBusinessElementReference;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MDMIBusinessElementRule createMDMIBusinessElementRule() {
		MDMIBusinessElementRuleImpl mdmiBusinessElementRule = new MDMIBusinessElementRuleImpl();
		return mdmiBusinessElementRule;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ToBusinessElement createToBusinessElement() {
		ToBusinessElementImpl toBusinessElement = new ToBusinessElementImpl();
		return toBusinessElement;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ToSemanticElement createToSemanticElement() {
		ToSemanticElementImpl toSemanticElement = new ToSemanticElementImpl();
		return toSemanticElement;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MDMIDomainDictionaryReference createMDMIDomainDictionaryReference() {
		MDMIDomainDictionaryReferenceImpl mdmiDomainDictionaryReference = new MDMIDomainDictionaryReferenceImpl();
		return mdmiDomainDictionaryReference;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MDMIExpression createMDMIExpression() {
		MDMIExpressionImpl mdmiExpression = new MDMIExpressionImpl();
		return mdmiExpression;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Keyword createKeyword() {
		KeywordImpl keyword = new KeywordImpl();
		return keyword;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MDMIDatatype createMDMIDatatype() {
		MDMIDatatypeImpl mdmiDatatype = new MDMIDatatypeImpl();
		return mdmiDatatype;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DTSPrimitive createDTSPrimitive() {
		DTSPrimitiveImpl dtsPrimitive = new DTSPrimitiveImpl();
		return dtsPrimitive;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DTCStructured createDTCStructured() {
		DTCStructuredImpl dtcStructured = new DTCStructuredImpl();
		return dtcStructured;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Field createField() {
		FieldImpl field = new FieldImpl();
		return field;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DTExternal createDTExternal() {
		DTExternalImpl dtExternal = new DTExternalImpl();
		return dtExternal;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DTSDerived createDTSDerived() {
		DTSDerivedImpl dtsDerived = new DTSDerivedImpl();
		return dtsDerived;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MessageElementType createMessageElementTypeFromString(EDataType eDataType, String initialValue) {
		MessageElementType result = MessageElementType.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertMessageElementTypeToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String createURIFromString(EDataType eDataType, String initialValue) {
		return (String)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertURIToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MDMIPackage getMDMIPackage() {
		return (MDMIPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static MDMIPackage getPackage() {
		return MDMIPackage.eINSTANCE;
	}

} //MDMIFactoryImpl
