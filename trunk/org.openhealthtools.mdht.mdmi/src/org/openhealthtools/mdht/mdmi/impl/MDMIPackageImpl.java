/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.openhealthtools.mdht.mdmi.impl;

import java.io.IOException;

import java.net.URL;

import org.eclipse.emf.common.util.WrappedException;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EValidator;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.emf.ecore.resource.Resource;

import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

import org.openhealthtools.mdht.mdmi.Bag;
import org.openhealthtools.mdht.mdmi.Choice;
import org.openhealthtools.mdht.mdmi.ConversionRule;
import org.openhealthtools.mdht.mdmi.DTCStructured;
import org.openhealthtools.mdht.mdmi.DTExternal;
import org.openhealthtools.mdht.mdmi.DTSDerived;
import org.openhealthtools.mdht.mdmi.DTSPrimitive;
import org.openhealthtools.mdht.mdmi.DataRule;
import org.openhealthtools.mdht.mdmi.Field;
import org.openhealthtools.mdht.mdmi.Keyword;
import org.openhealthtools.mdht.mdmi.LeafSyntaxTranslator;
import org.openhealthtools.mdht.mdmi.MDMIBusinessElementReference;
import org.openhealthtools.mdht.mdmi.MDMIBusinessElementRule;
import org.openhealthtools.mdht.mdmi.MDMIDatatype;
import org.openhealthtools.mdht.mdmi.MDMIDomainDictionaryReference;
import org.openhealthtools.mdht.mdmi.MDMIExpression;
import org.openhealthtools.mdht.mdmi.MDMIFactory;
import org.openhealthtools.mdht.mdmi.MDMIPackage;

import org.openhealthtools.mdht.mdmi.MessageComposite;
import org.openhealthtools.mdht.mdmi.MessageElementType;
import org.openhealthtools.mdht.mdmi.MessageGroup;
import org.openhealthtools.mdht.mdmi.MessageModel;
import org.openhealthtools.mdht.mdmi.MessageSyntaxModel;
import org.openhealthtools.mdht.mdmi.Node;
import org.openhealthtools.mdht.mdmi.SemanticElement;
import org.openhealthtools.mdht.mdmi.SemanticElementBusinessRule;
import org.openhealthtools.mdht.mdmi.SemanticElementRelationship;
import org.openhealthtools.mdht.mdmi.SemanticElementSet;
import org.openhealthtools.mdht.mdmi.SimpleMessageComposite;
import org.openhealthtools.mdht.mdmi.ToBusinessElement;
import org.openhealthtools.mdht.mdmi.ToSemanticElement;
import org.openhealthtools.mdht.mdmi.util.MDMIValidator;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class MDMIPackageImpl extends EPackageImpl implements MDMIPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass messageModelEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass messageSyntaxModelEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass nodeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass bagEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass choiceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass leafSyntaxTranslatorEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass messageGroupEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass dataRuleEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass semanticElementSetEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass semanticElementEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass simpleMessageCompositeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass messageCompositeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass semanticElementBusinessRuleEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass semanticElementRelationshipEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mdmiBusinessElementReferenceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mdmiBusinessElementRuleEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass toBusinessElementEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass toSemanticElementEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass conversionRuleEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mdmiDomainDictionaryReferenceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mdmiExpressionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass keywordEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mdmiDatatypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass dtsPrimitiveEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass dtcStructuredEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass fieldEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass dtExternalEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass dtsDerivedEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum messageElementTypeEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType uriEDataType = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.openhealthtools.mdht.mdmi.MDMIPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private MDMIPackageImpl() {
		super(eNS_URI, MDMIFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 * 
	 * <p>This method is used to initialize {@link MDMIPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static MDMIPackage init() {
		if (isInited) return (MDMIPackage)EPackage.Registry.INSTANCE.getEPackage(MDMIPackage.eNS_URI);

		// Obtain or create and register package
		MDMIPackageImpl theMDMIPackage = (MDMIPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof MDMIPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new MDMIPackageImpl());

		isInited = true;

		// Create package meta-data objects
		theMDMIPackage.createPackageContents();

		// Initialize created meta-data
		theMDMIPackage.initializePackageContents();

		// Register package validator
		EValidator.Registry.INSTANCE.put
			(theMDMIPackage, 
			 new EValidator.Descriptor() {
				 public EValidator getEValidator() {
					 return MDMIValidator.INSTANCE;
				 }
			 });

		// Mark meta-data to indicate it can't be changed
		theMDMIPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(MDMIPackage.eNS_URI, theMDMIPackage);
		return theMDMIPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMessageModel() {
		return messageModelEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMessageModel_MessageModelName() {
		return (EAttribute)messageModelEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMessageModel_SyntaxModel() {
		return (EReference)messageModelEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMessageModel_ElementSet() {
		return (EReference)messageModelEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMessageModel_Description() {
		return (EAttribute)messageModelEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMessageModel_Source() {
		return (EAttribute)messageModelEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMessageModel_Group() {
		return (EReference)messageModelEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMessageSyntaxModel() {
		return messageSyntaxModelEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMessageSyntaxModel_Name() {
		return (EAttribute)messageSyntaxModelEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMessageSyntaxModel_Model() {
		return (EReference)messageSyntaxModelEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMessageSyntaxModel_Root() {
		return (EReference)messageSyntaxModelEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMessageSyntaxModel_ElementSet() {
		return (EReference)messageSyntaxModelEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMessageSyntaxModel_Description() {
		return (EAttribute)messageSyntaxModelEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getNode() {
		return nodeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getNode_Name() {
		return (EAttribute)nodeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getNode_Description() {
		return (EAttribute)nodeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getNode_MinOccurs() {
		return (EAttribute)nodeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getNode_MaxOccurs() {
		return (EAttribute)nodeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getNode_Location() {
		return (EAttribute)nodeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getNode_LocationExpressionLanguage() {
		return (EAttribute)nodeEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getNode_SyntaxModel() {
		return (EReference)nodeEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getNode_SemanticElement() {
		return (EReference)nodeEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getNode_FieldName() {
		return (EAttribute)nodeEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getNode_IsSyntacticField() {
		return (EAttribute)nodeEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getBag() {
		return bagEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getBag_IsUnique() {
		return (EAttribute)bagEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getBag_IsOrdered() {
		return (EAttribute)bagEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBag_Nodes() {
		return (EReference)bagEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getChoice() {
		return choiceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getChoice_Constraint() {
		return (EAttribute)choiceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getChoice_ConstraintExpressionLanguage() {
		return (EAttribute)choiceEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getChoice_Nodes() {
		return (EReference)choiceEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getLeafSyntaxTranslator() {
		return leafSyntaxTranslatorEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getLeafSyntaxTranslator_Format() {
		return (EAttribute)leafSyntaxTranslatorEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getLeafSyntaxTranslator_FormatExpressionLanguage() {
		return (EAttribute)leafSyntaxTranslatorEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMessageGroup() {
		return messageGroupEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMessageGroup_Name() {
		return (EAttribute)messageGroupEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMessageGroup_DataRules() {
		return (EReference)messageGroupEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMessageGroup_Description() {
		return (EAttribute)messageGroupEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMessageGroup_DefaultLocationExprLang() {
		return (EAttribute)messageGroupEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMessageGroup_DefaultConstraintExprLang() {
		return (EAttribute)messageGroupEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMessageGroup_DefaultRuleExprLang() {
		return (EAttribute)messageGroupEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMessageGroup_DefaultFormatExpressionLanguage() {
		return (EAttribute)messageGroupEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMessageGroup_DefaultOrderingExpressionLanguage() {
		return (EAttribute)messageGroupEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMessageGroup_Models() {
		return (EReference)messageGroupEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMessageGroup_DomainDictionary() {
		return (EReference)messageGroupEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMessageGroup_DefaultMDMIExpresionLanguage() {
		return (EAttribute)messageGroupEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMessageGroup_Rules() {
		return (EReference)messageGroupEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMessageGroup_Datatypes() {
		return (EReference)messageGroupEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getDataRule() {
		return dataRuleEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDataRule_Name() {
		return (EAttribute)dataRuleEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDataRule_Description() {
		return (EAttribute)dataRuleEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDataRule_Rule() {
		return (EAttribute)dataRuleEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDataRule_RuleExpressionLanguage() {
		return (EAttribute)dataRuleEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDataRule_Scope() {
		return (EReference)dataRuleEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDataRule_Datatype() {
		return (EReference)dataRuleEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDataRule_SemanticElement() {
		return (EReference)dataRuleEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDataRule_Group() {
		return (EReference)dataRuleEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getSemanticElementSet() {
		return semanticElementSetEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSemanticElementSet_Name() {
		return (EAttribute)semanticElementSetEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSemanticElementSet_Description() {
		return (EAttribute)semanticElementSetEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSemanticElementSet_MessageModelName() {
		return (EAttribute)semanticElementSetEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSemanticElementSet_SyntaxModel() {
		return (EReference)semanticElementSetEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSemanticElementSet_Model() {
		return (EReference)semanticElementSetEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSemanticElementSet_SemanticElements() {
		return (EReference)semanticElementSetEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSemanticElementSet_Composite() {
		return (EReference)semanticElementSetEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getSemanticElement() {
		return semanticElementEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSemanticElement_Name() {
		return (EAttribute)semanticElementEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSemanticElement_Description() {
		return (EAttribute)semanticElementEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSemanticElement_ElementType() {
		return (EAttribute)semanticElementEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSemanticElement_Datatype() {
		return (EReference)semanticElementEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSemanticElement_PropertyQualifier() {
		return (EAttribute)semanticElementEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSemanticElement_Composite() {
		return (EReference)semanticElementEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSemanticElement_ElementSet() {
		return (EReference)semanticElementEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSemanticElement_BusinessRules() {
		return (EReference)semanticElementEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSemanticElement_DataRules() {
		return (EReference)semanticElementEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSemanticElement_Relationships() {
		return (EReference)semanticElementEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSemanticElement_MultipleInstances() {
		return (EAttribute)semanticElementEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSemanticElement_FromMdmi() {
		return (EReference)semanticElementEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSemanticElement_Ordering() {
		return (EAttribute)semanticElementEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSemanticElement_OrderingLanguage() {
		return (EAttribute)semanticElementEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSemanticElement_ComputedValue() {
		return (EReference)semanticElementEClass.getEStructuralFeatures().get(14);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSemanticElement_ComputedInValue() {
		return (EReference)semanticElementEClass.getEStructuralFeatures().get(15);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSemanticElement_ToMdmi() {
		return (EReference)semanticElementEClass.getEStructuralFeatures().get(16);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSemanticElement_Parent() {
		return (EReference)semanticElementEClass.getEStructuralFeatures().get(17);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSemanticElement_Children() {
		return (EReference)semanticElementEClass.getEStructuralFeatures().get(18);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSemanticElement_SyntaxNode() {
		return (EReference)semanticElementEClass.getEStructuralFeatures().get(19);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSemanticElement_ComputedOutValue() {
		return (EReference)semanticElementEClass.getEStructuralFeatures().get(20);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSemanticElement_Keywords() {
		return (EReference)semanticElementEClass.getEStructuralFeatures().get(21);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getSimpleMessageComposite() {
		return simpleMessageCompositeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSimpleMessageComposite_Name() {
		return (EAttribute)simpleMessageCompositeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSimpleMessageComposite_SemanticElements() {
		return (EReference)simpleMessageCompositeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSimpleMessageComposite_ElementSet() {
		return (EReference)simpleMessageCompositeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSimpleMessageComposite_Description() {
		return (EAttribute)simpleMessageCompositeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMessageComposite() {
		return messageCompositeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMessageComposite_Composites() {
		return (EReference)messageCompositeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMessageComposite_Owner() {
		return (EReference)messageCompositeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getSemanticElementBusinessRule() {
		return semanticElementBusinessRuleEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSemanticElementBusinessRule_Name() {
		return (EAttribute)semanticElementBusinessRuleEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSemanticElementBusinessRule_Description() {
		return (EAttribute)semanticElementBusinessRuleEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSemanticElementBusinessRule_Rule() {
		return (EAttribute)semanticElementBusinessRuleEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSemanticElementBusinessRule_RuleExpressionLanguage() {
		return (EAttribute)semanticElementBusinessRuleEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSemanticElementBusinessRule_SemanticElement() {
		return (EReference)semanticElementBusinessRuleEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getSemanticElementRelationship() {
		return semanticElementRelationshipEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSemanticElementRelationship_Name() {
		return (EAttribute)semanticElementRelationshipEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSemanticElementRelationship_Description() {
		return (EAttribute)semanticElementRelationshipEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSemanticElementRelationship_Rule() {
		return (EAttribute)semanticElementRelationshipEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSemanticElementRelationship_RuleExpressionLanguage() {
		return (EAttribute)semanticElementRelationshipEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getSemanticElementRelationship_Context() {
		return (EReference)semanticElementRelationshipEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSemanticElementRelationship_MinOccurs() {
		return (EAttribute)semanticElementRelationshipEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSemanticElementRelationship_MaxOccurs() {
		return (EAttribute)semanticElementRelationshipEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSemanticElementRelationship_SourceIsInstance() {
		return (EAttribute)semanticElementRelationshipEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSemanticElementRelationship_TargetIsInstance() {
		return (EAttribute)semanticElementRelationshipEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMDMIBusinessElementReference() {
		return mdmiBusinessElementReferenceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMDMIBusinessElementReference_Name() {
		return (EAttribute)mdmiBusinessElementReferenceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMDMIBusinessElementReference_Description() {
		return (EAttribute)mdmiBusinessElementReferenceEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMDMIBusinessElementReference_Reference() {
		return (EAttribute)mdmiBusinessElementReferenceEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMDMIBusinessElementReference_UniqueIdentifier() {
		return (EAttribute)mdmiBusinessElementReferenceEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMDMIBusinessElementReference_BusinessRules() {
		return (EReference)mdmiBusinessElementReferenceEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMDMIBusinessElementReference_DomainDictionaryReference() {
		return (EReference)mdmiBusinessElementReferenceEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMDMIBusinessElementReference_ReferenceDatatype() {
		return (EReference)mdmiBusinessElementReferenceEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMDMIBusinessElementReference_EnumValueSetField() {
		return (EAttribute)mdmiBusinessElementReferenceEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMDMIBusinessElementReference_EnumValueField() {
		return (EAttribute)mdmiBusinessElementReferenceEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMDMIBusinessElementReference_EnumValueDescrField() {
		return (EAttribute)mdmiBusinessElementReferenceEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMDMIBusinessElementReference_EnumValueSet() {
		return (EAttribute)mdmiBusinessElementReferenceEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMDMIBusinessElementRule() {
		return mdmiBusinessElementRuleEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMDMIBusinessElementRule_Name() {
		return (EAttribute)mdmiBusinessElementRuleEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMDMIBusinessElementRule_Description() {
		return (EAttribute)mdmiBusinessElementRuleEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMDMIBusinessElementRule_Rule() {
		return (EAttribute)mdmiBusinessElementRuleEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMDMIBusinessElementRule_RuleExpressionLanguage() {
		return (EAttribute)mdmiBusinessElementRuleEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMDMIBusinessElementRule_BusinessElement() {
		return (EReference)mdmiBusinessElementRuleEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getToBusinessElement() {
		return toBusinessElementEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getToBusinessElement_BusinessElement() {
		return (EReference)toBusinessElementEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getToBusinessElement_Rule() {
		return (EAttribute)toBusinessElementEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getToSemanticElement() {
		return toSemanticElementEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getToSemanticElement_BusinessElement() {
		return (EReference)toSemanticElementEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getToSemanticElement_Rule() {
		return (EAttribute)toSemanticElementEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getConversionRule() {
		return conversionRuleEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getConversionRule_Name() {
		return (EAttribute)conversionRuleEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getConversionRule_Description() {
		return (EAttribute)conversionRuleEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getConversionRule_RuleExpressionLanguage() {
		return (EAttribute)conversionRuleEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMDMIDomainDictionaryReference() {
		return mdmiDomainDictionaryReferenceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMDMIDomainDictionaryReference_Name() {
		return (EAttribute)mdmiDomainDictionaryReferenceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMDMIDomainDictionaryReference_Description() {
		return (EAttribute)mdmiDomainDictionaryReferenceEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMDMIDomainDictionaryReference_BusinessElements() {
		return (EReference)mdmiDomainDictionaryReferenceEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMDMIDomainDictionaryReference_Reference() {
		return (EAttribute)mdmiDomainDictionaryReferenceEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMDMIDomainDictionaryReference_Group() {
		return (EReference)mdmiDomainDictionaryReferenceEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMDMIExpression() {
		return mdmiExpressionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMDMIExpression_Expression() {
		return (EAttribute)mdmiExpressionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMDMIExpression_Language() {
		return (EAttribute)mdmiExpressionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getKeyword() {
		return keywordEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getKeyword_Description() {
		return (EAttribute)keywordEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getKeyword_Keyword() {
		return (EAttribute)keywordEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getKeyword_KeywordValue() {
		return (EAttribute)keywordEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getKeyword_Reference() {
		return (EAttribute)keywordEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getKeyword_Owner() {
		return (EReference)keywordEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMDMIDatatype() {
		return mdmiDatatypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMDMIDatatype_TypeName() {
		return (EAttribute)mdmiDatatypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMDMIDatatype_Description() {
		return (EAttribute)mdmiDatatypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMDMIDatatype_Reference() {
		return (EAttribute)mdmiDatatypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMDMIDatatype_IsReadonly() {
		return (EAttribute)mdmiDatatypeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMDMIDatatype_TypeSpec() {
		return (EAttribute)mdmiDatatypeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMDMIDatatype_Restriction() {
		return (EAttribute)mdmiDatatypeEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getDTSPrimitive() {
		return dtsPrimitiveEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getDTCStructured() {
		return dtcStructuredEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDTCStructured_Fields() {
		return (EReference)dtcStructuredEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getField() {
		return fieldEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getField_Name() {
		return (EAttribute)fieldEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getField_MinOccurs() {
		return (EAttribute)fieldEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getField_MaxOccurs() {
		return (EAttribute)fieldEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getField_Datatype() {
		return (EReference)fieldEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getField_Description() {
		return (EAttribute)fieldEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getDTExternal() {
		return dtExternalEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getDTSDerived() {
		return dtsDerivedEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDTSDerived_BaseType() {
		return (EReference)dtsDerivedEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getMessageElementType() {
		return messageElementTypeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EDataType getURI() {
		return uriEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MDMIFactory getMDMIFactory() {
		return (MDMIFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		messageModelEClass = createEClass(MESSAGE_MODEL);
		createEAttribute(messageModelEClass, MESSAGE_MODEL__MESSAGE_MODEL_NAME);
		createEReference(messageModelEClass, MESSAGE_MODEL__SYNTAX_MODEL);
		createEReference(messageModelEClass, MESSAGE_MODEL__ELEMENT_SET);
		createEAttribute(messageModelEClass, MESSAGE_MODEL__DESCRIPTION);
		createEAttribute(messageModelEClass, MESSAGE_MODEL__SOURCE);
		createEReference(messageModelEClass, MESSAGE_MODEL__GROUP);

		messageSyntaxModelEClass = createEClass(MESSAGE_SYNTAX_MODEL);
		createEAttribute(messageSyntaxModelEClass, MESSAGE_SYNTAX_MODEL__NAME);
		createEReference(messageSyntaxModelEClass, MESSAGE_SYNTAX_MODEL__MODEL);
		createEReference(messageSyntaxModelEClass, MESSAGE_SYNTAX_MODEL__ROOT);
		createEReference(messageSyntaxModelEClass, MESSAGE_SYNTAX_MODEL__ELEMENT_SET);
		createEAttribute(messageSyntaxModelEClass, MESSAGE_SYNTAX_MODEL__DESCRIPTION);

		nodeEClass = createEClass(NODE);
		createEAttribute(nodeEClass, NODE__NAME);
		createEAttribute(nodeEClass, NODE__DESCRIPTION);
		createEAttribute(nodeEClass, NODE__MIN_OCCURS);
		createEAttribute(nodeEClass, NODE__MAX_OCCURS);
		createEAttribute(nodeEClass, NODE__LOCATION);
		createEAttribute(nodeEClass, NODE__LOCATION_EXPRESSION_LANGUAGE);
		createEReference(nodeEClass, NODE__SYNTAX_MODEL);
		createEReference(nodeEClass, NODE__SEMANTIC_ELEMENT);
		createEAttribute(nodeEClass, NODE__FIELD_NAME);
		createEAttribute(nodeEClass, NODE__IS_SYNTACTIC_FIELD);

		bagEClass = createEClass(BAG);
		createEAttribute(bagEClass, BAG__IS_UNIQUE);
		createEAttribute(bagEClass, BAG__IS_ORDERED);
		createEReference(bagEClass, BAG__NODES);

		choiceEClass = createEClass(CHOICE);
		createEAttribute(choiceEClass, CHOICE__CONSTRAINT);
		createEAttribute(choiceEClass, CHOICE__CONSTRAINT_EXPRESSION_LANGUAGE);
		createEReference(choiceEClass, CHOICE__NODES);

		leafSyntaxTranslatorEClass = createEClass(LEAF_SYNTAX_TRANSLATOR);
		createEAttribute(leafSyntaxTranslatorEClass, LEAF_SYNTAX_TRANSLATOR__FORMAT);
		createEAttribute(leafSyntaxTranslatorEClass, LEAF_SYNTAX_TRANSLATOR__FORMAT_EXPRESSION_LANGUAGE);

		messageGroupEClass = createEClass(MESSAGE_GROUP);
		createEAttribute(messageGroupEClass, MESSAGE_GROUP__NAME);
		createEReference(messageGroupEClass, MESSAGE_GROUP__DATA_RULES);
		createEAttribute(messageGroupEClass, MESSAGE_GROUP__DESCRIPTION);
		createEAttribute(messageGroupEClass, MESSAGE_GROUP__DEFAULT_LOCATION_EXPR_LANG);
		createEAttribute(messageGroupEClass, MESSAGE_GROUP__DEFAULT_CONSTRAINT_EXPR_LANG);
		createEAttribute(messageGroupEClass, MESSAGE_GROUP__DEFAULT_RULE_EXPR_LANG);
		createEAttribute(messageGroupEClass, MESSAGE_GROUP__DEFAULT_FORMAT_EXPRESSION_LANGUAGE);
		createEAttribute(messageGroupEClass, MESSAGE_GROUP__DEFAULT_ORDERING_EXPRESSION_LANGUAGE);
		createEReference(messageGroupEClass, MESSAGE_GROUP__MODELS);
		createEReference(messageGroupEClass, MESSAGE_GROUP__DOMAIN_DICTIONARY);
		createEAttribute(messageGroupEClass, MESSAGE_GROUP__DEFAULT_MDMI_EXPRESION_LANGUAGE);
		createEReference(messageGroupEClass, MESSAGE_GROUP__RULES);
		createEReference(messageGroupEClass, MESSAGE_GROUP__DATATYPES);

		dataRuleEClass = createEClass(DATA_RULE);
		createEAttribute(dataRuleEClass, DATA_RULE__NAME);
		createEAttribute(dataRuleEClass, DATA_RULE__DESCRIPTION);
		createEAttribute(dataRuleEClass, DATA_RULE__RULE);
		createEAttribute(dataRuleEClass, DATA_RULE__RULE_EXPRESSION_LANGUAGE);
		createEReference(dataRuleEClass, DATA_RULE__SCOPE);
		createEReference(dataRuleEClass, DATA_RULE__DATATYPE);
		createEReference(dataRuleEClass, DATA_RULE__SEMANTIC_ELEMENT);
		createEReference(dataRuleEClass, DATA_RULE__GROUP);

		semanticElementSetEClass = createEClass(SEMANTIC_ELEMENT_SET);
		createEAttribute(semanticElementSetEClass, SEMANTIC_ELEMENT_SET__NAME);
		createEAttribute(semanticElementSetEClass, SEMANTIC_ELEMENT_SET__DESCRIPTION);
		createEAttribute(semanticElementSetEClass, SEMANTIC_ELEMENT_SET__MESSAGE_MODEL_NAME);
		createEReference(semanticElementSetEClass, SEMANTIC_ELEMENT_SET__SYNTAX_MODEL);
		createEReference(semanticElementSetEClass, SEMANTIC_ELEMENT_SET__MODEL);
		createEReference(semanticElementSetEClass, SEMANTIC_ELEMENT_SET__SEMANTIC_ELEMENTS);
		createEReference(semanticElementSetEClass, SEMANTIC_ELEMENT_SET__COMPOSITE);

		semanticElementEClass = createEClass(SEMANTIC_ELEMENT);
		createEAttribute(semanticElementEClass, SEMANTIC_ELEMENT__NAME);
		createEAttribute(semanticElementEClass, SEMANTIC_ELEMENT__DESCRIPTION);
		createEAttribute(semanticElementEClass, SEMANTIC_ELEMENT__ELEMENT_TYPE);
		createEReference(semanticElementEClass, SEMANTIC_ELEMENT__DATATYPE);
		createEAttribute(semanticElementEClass, SEMANTIC_ELEMENT__PROPERTY_QUALIFIER);
		createEReference(semanticElementEClass, SEMANTIC_ELEMENT__COMPOSITE);
		createEReference(semanticElementEClass, SEMANTIC_ELEMENT__ELEMENT_SET);
		createEReference(semanticElementEClass, SEMANTIC_ELEMENT__BUSINESS_RULES);
		createEReference(semanticElementEClass, SEMANTIC_ELEMENT__DATA_RULES);
		createEReference(semanticElementEClass, SEMANTIC_ELEMENT__RELATIONSHIPS);
		createEAttribute(semanticElementEClass, SEMANTIC_ELEMENT__MULTIPLE_INSTANCES);
		createEReference(semanticElementEClass, SEMANTIC_ELEMENT__FROM_MDMI);
		createEAttribute(semanticElementEClass, SEMANTIC_ELEMENT__ORDERING);
		createEAttribute(semanticElementEClass, SEMANTIC_ELEMENT__ORDERING_LANGUAGE);
		createEReference(semanticElementEClass, SEMANTIC_ELEMENT__COMPUTED_VALUE);
		createEReference(semanticElementEClass, SEMANTIC_ELEMENT__COMPUTED_IN_VALUE);
		createEReference(semanticElementEClass, SEMANTIC_ELEMENT__TO_MDMI);
		createEReference(semanticElementEClass, SEMANTIC_ELEMENT__PARENT);
		createEReference(semanticElementEClass, SEMANTIC_ELEMENT__CHILDREN);
		createEReference(semanticElementEClass, SEMANTIC_ELEMENT__SYNTAX_NODE);
		createEReference(semanticElementEClass, SEMANTIC_ELEMENT__COMPUTED_OUT_VALUE);
		createEReference(semanticElementEClass, SEMANTIC_ELEMENT__KEYWORDS);

		simpleMessageCompositeEClass = createEClass(SIMPLE_MESSAGE_COMPOSITE);
		createEAttribute(simpleMessageCompositeEClass, SIMPLE_MESSAGE_COMPOSITE__NAME);
		createEReference(simpleMessageCompositeEClass, SIMPLE_MESSAGE_COMPOSITE__SEMANTIC_ELEMENTS);
		createEReference(simpleMessageCompositeEClass, SIMPLE_MESSAGE_COMPOSITE__ELEMENT_SET);
		createEAttribute(simpleMessageCompositeEClass, SIMPLE_MESSAGE_COMPOSITE__DESCRIPTION);

		messageCompositeEClass = createEClass(MESSAGE_COMPOSITE);
		createEReference(messageCompositeEClass, MESSAGE_COMPOSITE__COMPOSITES);
		createEReference(messageCompositeEClass, MESSAGE_COMPOSITE__OWNER);

		semanticElementBusinessRuleEClass = createEClass(SEMANTIC_ELEMENT_BUSINESS_RULE);
		createEAttribute(semanticElementBusinessRuleEClass, SEMANTIC_ELEMENT_BUSINESS_RULE__NAME);
		createEAttribute(semanticElementBusinessRuleEClass, SEMANTIC_ELEMENT_BUSINESS_RULE__DESCRIPTION);
		createEAttribute(semanticElementBusinessRuleEClass, SEMANTIC_ELEMENT_BUSINESS_RULE__RULE);
		createEAttribute(semanticElementBusinessRuleEClass, SEMANTIC_ELEMENT_BUSINESS_RULE__RULE_EXPRESSION_LANGUAGE);
		createEReference(semanticElementBusinessRuleEClass, SEMANTIC_ELEMENT_BUSINESS_RULE__SEMANTIC_ELEMENT);

		semanticElementRelationshipEClass = createEClass(SEMANTIC_ELEMENT_RELATIONSHIP);
		createEAttribute(semanticElementRelationshipEClass, SEMANTIC_ELEMENT_RELATIONSHIP__NAME);
		createEAttribute(semanticElementRelationshipEClass, SEMANTIC_ELEMENT_RELATIONSHIP__DESCRIPTION);
		createEAttribute(semanticElementRelationshipEClass, SEMANTIC_ELEMENT_RELATIONSHIP__RULE);
		createEAttribute(semanticElementRelationshipEClass, SEMANTIC_ELEMENT_RELATIONSHIP__RULE_EXPRESSION_LANGUAGE);
		createEReference(semanticElementRelationshipEClass, SEMANTIC_ELEMENT_RELATIONSHIP__CONTEXT);
		createEAttribute(semanticElementRelationshipEClass, SEMANTIC_ELEMENT_RELATIONSHIP__MIN_OCCURS);
		createEAttribute(semanticElementRelationshipEClass, SEMANTIC_ELEMENT_RELATIONSHIP__MAX_OCCURS);
		createEAttribute(semanticElementRelationshipEClass, SEMANTIC_ELEMENT_RELATIONSHIP__SOURCE_IS_INSTANCE);
		createEAttribute(semanticElementRelationshipEClass, SEMANTIC_ELEMENT_RELATIONSHIP__TARGET_IS_INSTANCE);

		mdmiBusinessElementReferenceEClass = createEClass(MDMI_BUSINESS_ELEMENT_REFERENCE);
		createEAttribute(mdmiBusinessElementReferenceEClass, MDMI_BUSINESS_ELEMENT_REFERENCE__NAME);
		createEAttribute(mdmiBusinessElementReferenceEClass, MDMI_BUSINESS_ELEMENT_REFERENCE__DESCRIPTION);
		createEAttribute(mdmiBusinessElementReferenceEClass, MDMI_BUSINESS_ELEMENT_REFERENCE__REFERENCE);
		createEAttribute(mdmiBusinessElementReferenceEClass, MDMI_BUSINESS_ELEMENT_REFERENCE__UNIQUE_IDENTIFIER);
		createEReference(mdmiBusinessElementReferenceEClass, MDMI_BUSINESS_ELEMENT_REFERENCE__BUSINESS_RULES);
		createEReference(mdmiBusinessElementReferenceEClass, MDMI_BUSINESS_ELEMENT_REFERENCE__DOMAIN_DICTIONARY_REFERENCE);
		createEReference(mdmiBusinessElementReferenceEClass, MDMI_BUSINESS_ELEMENT_REFERENCE__REFERENCE_DATATYPE);
		createEAttribute(mdmiBusinessElementReferenceEClass, MDMI_BUSINESS_ELEMENT_REFERENCE__ENUM_VALUE_SET_FIELD);
		createEAttribute(mdmiBusinessElementReferenceEClass, MDMI_BUSINESS_ELEMENT_REFERENCE__ENUM_VALUE_FIELD);
		createEAttribute(mdmiBusinessElementReferenceEClass, MDMI_BUSINESS_ELEMENT_REFERENCE__ENUM_VALUE_DESCR_FIELD);
		createEAttribute(mdmiBusinessElementReferenceEClass, MDMI_BUSINESS_ELEMENT_REFERENCE__ENUM_VALUE_SET);

		mdmiBusinessElementRuleEClass = createEClass(MDMI_BUSINESS_ELEMENT_RULE);
		createEAttribute(mdmiBusinessElementRuleEClass, MDMI_BUSINESS_ELEMENT_RULE__NAME);
		createEAttribute(mdmiBusinessElementRuleEClass, MDMI_BUSINESS_ELEMENT_RULE__DESCRIPTION);
		createEAttribute(mdmiBusinessElementRuleEClass, MDMI_BUSINESS_ELEMENT_RULE__RULE);
		createEAttribute(mdmiBusinessElementRuleEClass, MDMI_BUSINESS_ELEMENT_RULE__RULE_EXPRESSION_LANGUAGE);
		createEReference(mdmiBusinessElementRuleEClass, MDMI_BUSINESS_ELEMENT_RULE__BUSINESS_ELEMENT);

		toBusinessElementEClass = createEClass(TO_BUSINESS_ELEMENT);
		createEReference(toBusinessElementEClass, TO_BUSINESS_ELEMENT__BUSINESS_ELEMENT);
		createEAttribute(toBusinessElementEClass, TO_BUSINESS_ELEMENT__RULE);

		toSemanticElementEClass = createEClass(TO_SEMANTIC_ELEMENT);
		createEReference(toSemanticElementEClass, TO_SEMANTIC_ELEMENT__BUSINESS_ELEMENT);
		createEAttribute(toSemanticElementEClass, TO_SEMANTIC_ELEMENT__RULE);

		conversionRuleEClass = createEClass(CONVERSION_RULE);
		createEAttribute(conversionRuleEClass, CONVERSION_RULE__NAME);
		createEAttribute(conversionRuleEClass, CONVERSION_RULE__DESCRIPTION);
		createEAttribute(conversionRuleEClass, CONVERSION_RULE__RULE_EXPRESSION_LANGUAGE);

		mdmiDomainDictionaryReferenceEClass = createEClass(MDMI_DOMAIN_DICTIONARY_REFERENCE);
		createEAttribute(mdmiDomainDictionaryReferenceEClass, MDMI_DOMAIN_DICTIONARY_REFERENCE__NAME);
		createEAttribute(mdmiDomainDictionaryReferenceEClass, MDMI_DOMAIN_DICTIONARY_REFERENCE__DESCRIPTION);
		createEReference(mdmiDomainDictionaryReferenceEClass, MDMI_DOMAIN_DICTIONARY_REFERENCE__BUSINESS_ELEMENTS);
		createEAttribute(mdmiDomainDictionaryReferenceEClass, MDMI_DOMAIN_DICTIONARY_REFERENCE__REFERENCE);
		createEReference(mdmiDomainDictionaryReferenceEClass, MDMI_DOMAIN_DICTIONARY_REFERENCE__GROUP);

		mdmiExpressionEClass = createEClass(MDMI_EXPRESSION);
		createEAttribute(mdmiExpressionEClass, MDMI_EXPRESSION__EXPRESSION);
		createEAttribute(mdmiExpressionEClass, MDMI_EXPRESSION__LANGUAGE);

		keywordEClass = createEClass(KEYWORD);
		createEAttribute(keywordEClass, KEYWORD__DESCRIPTION);
		createEAttribute(keywordEClass, KEYWORD__KEYWORD);
		createEAttribute(keywordEClass, KEYWORD__KEYWORD_VALUE);
		createEAttribute(keywordEClass, KEYWORD__REFERENCE);
		createEReference(keywordEClass, KEYWORD__OWNER);

		mdmiDatatypeEClass = createEClass(MDMI_DATATYPE);
		createEAttribute(mdmiDatatypeEClass, MDMI_DATATYPE__TYPE_NAME);
		createEAttribute(mdmiDatatypeEClass, MDMI_DATATYPE__DESCRIPTION);
		createEAttribute(mdmiDatatypeEClass, MDMI_DATATYPE__REFERENCE);
		createEAttribute(mdmiDatatypeEClass, MDMI_DATATYPE__IS_READONLY);
		createEAttribute(mdmiDatatypeEClass, MDMI_DATATYPE__TYPE_SPEC);
		createEAttribute(mdmiDatatypeEClass, MDMI_DATATYPE__RESTRICTION);

		dtsPrimitiveEClass = createEClass(DTS_PRIMITIVE);

		dtcStructuredEClass = createEClass(DTC_STRUCTURED);
		createEReference(dtcStructuredEClass, DTC_STRUCTURED__FIELDS);

		fieldEClass = createEClass(FIELD);
		createEAttribute(fieldEClass, FIELD__NAME);
		createEAttribute(fieldEClass, FIELD__MIN_OCCURS);
		createEAttribute(fieldEClass, FIELD__MAX_OCCURS);
		createEReference(fieldEClass, FIELD__DATATYPE);
		createEAttribute(fieldEClass, FIELD__DESCRIPTION);

		dtExternalEClass = createEClass(DT_EXTERNAL);

		dtsDerivedEClass = createEClass(DTS_DERIVED);
		createEReference(dtsDerivedEClass, DTS_DERIVED__BASE_TYPE);

		// Create enums
		messageElementTypeEEnum = createEEnum(MESSAGE_ELEMENT_TYPE);

		// Create data types
		uriEDataType = createEDataType(URI);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		bagEClass.getESuperTypes().add(this.getNode());
		choiceEClass.getESuperTypes().add(this.getNode());
		leafSyntaxTranslatorEClass.getESuperTypes().add(this.getNode());
		messageCompositeEClass.getESuperTypes().add(this.getSimpleMessageComposite());
		toBusinessElementEClass.getESuperTypes().add(this.getConversionRule());
		toSemanticElementEClass.getESuperTypes().add(this.getConversionRule());
		dtsPrimitiveEClass.getESuperTypes().add(this.getMDMIDatatype());
		dtcStructuredEClass.getESuperTypes().add(this.getMDMIDatatype());
		dtExternalEClass.getESuperTypes().add(this.getDTSPrimitive());
		dtsDerivedEClass.getESuperTypes().add(this.getDTSPrimitive());

		// Initialize classes and features; add operations and parameters
		initEClass(messageModelEClass, MessageModel.class, "MessageModel", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMessageModel_MessageModelName(), ecorePackage.getEString(), "messageModelName", null, 1, 1, MessageModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getMessageModel_SyntaxModel(), this.getMessageSyntaxModel(), this.getMessageSyntaxModel_Model(), "syntaxModel", null, 1, 1, MessageModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getMessageModel_ElementSet(), this.getSemanticElementSet(), this.getSemanticElementSet_Model(), "elementSet", null, 1, 1, MessageModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getMessageModel_Description(), ecorePackage.getEString(), "description", null, 0, 1, MessageModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getMessageModel_Source(), this.getURI(), "source", null, 0, 1, MessageModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getMessageModel_Group(), this.getMessageGroup(), this.getMessageGroup_Models(), "group", null, 1, 1, MessageModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);

		initEClass(messageSyntaxModelEClass, MessageSyntaxModel.class, "MessageSyntaxModel", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMessageSyntaxModel_Name(), ecorePackage.getEString(), "name", "", 1, 1, MessageSyntaxModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getMessageSyntaxModel_Model(), this.getMessageModel(), this.getMessageModel_SyntaxModel(), "model", null, 1, 1, MessageSyntaxModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getMessageSyntaxModel_Root(), this.getNode(), this.getNode_SyntaxModel(), "root", null, 1, 1, MessageSyntaxModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getMessageSyntaxModel_ElementSet(), this.getSemanticElementSet(), this.getSemanticElementSet_SyntaxModel(), "elementSet", null, 1, 1, MessageSyntaxModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getMessageSyntaxModel_Description(), ecorePackage.getEString(), "description", null, 0, 1, MessageSyntaxModel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);

		initEClass(nodeEClass, Node.class, "Node", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getNode_Name(), ecorePackage.getEString(), "name", null, 1, 1, Node.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getNode_Description(), ecorePackage.getEString(), "description", "", 0, 1, Node.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getNode_MinOccurs(), ecorePackage.getEInt(), "minOccurs", null, 1, 1, Node.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getNode_MaxOccurs(), ecorePackage.getEInt(), "maxOccurs", null, 1, 1, Node.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getNode_Location(), ecorePackage.getEString(), "location", null, 1, 1, Node.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getNode_LocationExpressionLanguage(), ecorePackage.getEString(), "locationExpressionLanguage", null, 0, 1, Node.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getNode_SyntaxModel(), this.getMessageSyntaxModel(), this.getMessageSyntaxModel_Root(), "syntaxModel", null, 0, 1, Node.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getNode_SemanticElement(), this.getSemanticElement(), this.getSemanticElement_SyntaxNode(), "semanticElement", null, 0, 1, Node.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getNode_FieldName(), ecorePackage.getEString(), "fieldName", null, 0, 1, Node.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getNode_IsSyntacticField(), ecorePackage.getEBoolean(), "isSyntacticField", null, 1, 1, Node.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED, !IS_ORDERED);

		initEClass(bagEClass, Bag.class, "Bag", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getBag_IsUnique(), ecorePackage.getEBoolean(), "isUnique", "true", 1, 1, Bag.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getBag_IsOrdered(), ecorePackage.getEBoolean(), "isOrdered", null, 1, 1, Bag.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getBag_Nodes(), this.getNode(), null, "nodes", null, 1, -1, Bag.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);

		initEClass(choiceEClass, Choice.class, "Choice", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getChoice_Constraint(), ecorePackage.getEString(), "constraint", "", 0, 1, Choice.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getChoice_ConstraintExpressionLanguage(), ecorePackage.getEString(), "constraintExpressionLanguage", null, 0, 1, Choice.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getChoice_Nodes(), this.getNode(), null, "nodes", null, 1, -1, Choice.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);

		initEClass(leafSyntaxTranslatorEClass, LeafSyntaxTranslator.class, "LeafSyntaxTranslator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getLeafSyntaxTranslator_Format(), ecorePackage.getEString(), "format", "", 1, 1, LeafSyntaxTranslator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getLeafSyntaxTranslator_FormatExpressionLanguage(), ecorePackage.getEString(), "formatExpressionLanguage", null, 0, 1, LeafSyntaxTranslator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);

		initEClass(messageGroupEClass, MessageGroup.class, "MessageGroup", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMessageGroup_Name(), ecorePackage.getEString(), "name", null, 1, 1, MessageGroup.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getMessageGroup_DataRules(), this.getDataRule(), this.getDataRule_Scope(), "dataRules", null, 0, -1, MessageGroup.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getMessageGroup_Description(), ecorePackage.getEString(), "description", null, 0, 1, MessageGroup.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getMessageGroup_DefaultLocationExprLang(), ecorePackage.getEString(), "defaultLocationExprLang", null, 1, 1, MessageGroup.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getMessageGroup_DefaultConstraintExprLang(), ecorePackage.getEString(), "defaultConstraintExprLang", null, 1, 1, MessageGroup.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getMessageGroup_DefaultRuleExprLang(), ecorePackage.getEString(), "defaultRuleExprLang", null, 1, 1, MessageGroup.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getMessageGroup_DefaultFormatExpressionLanguage(), ecorePackage.getEString(), "defaultFormatExpressionLanguage", null, 1, 1, MessageGroup.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getMessageGroup_DefaultOrderingExpressionLanguage(), ecorePackage.getEString(), "defaultOrderingExpressionLanguage", null, 1, 1, MessageGroup.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getMessageGroup_Models(), this.getMessageModel(), this.getMessageModel_Group(), "models", null, 1, -1, MessageGroup.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getMessageGroup_DomainDictionary(), this.getMDMIDomainDictionaryReference(), this.getMDMIDomainDictionaryReference_Group(), "domainDictionary", null, 1, 1, MessageGroup.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getMessageGroup_DefaultMDMIExpresionLanguage(), ecorePackage.getEString(), "defaultMDMIExpresionLanguage", null, 1, 1, MessageGroup.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getMessageGroup_Rules(), this.getDataRule(), this.getDataRule_Group(), "rules", null, 0, -1, MessageGroup.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getMessageGroup_Datatypes(), this.getMDMIDatatype(), null, "datatypes", null, 0, -1, MessageGroup.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(dataRuleEClass, DataRule.class, "DataRule", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDataRule_Name(), ecorePackage.getEString(), "name", null, 1, 1, DataRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getDataRule_Description(), ecorePackage.getEString(), "description", null, 0, 1, DataRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getDataRule_Rule(), ecorePackage.getEString(), "rule", null, 1, 1, DataRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getDataRule_RuleExpressionLanguage(), ecorePackage.getEString(), "ruleExpressionLanguage", null, 0, 1, DataRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getDataRule_Scope(), this.getMessageGroup(), this.getMessageGroup_DataRules(), "scope", null, 1, 1, DataRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getDataRule_Datatype(), this.getMDMIDatatype(), null, "datatype", null, 1, -1, DataRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getDataRule_SemanticElement(), this.getSemanticElement(), this.getSemanticElement_DataRules(), "semanticElement", null, 1, 1, DataRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getDataRule_Group(), this.getMessageGroup(), this.getMessageGroup_Rules(), "group", null, 1, 1, DataRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);

		initEClass(semanticElementSetEClass, SemanticElementSet.class, "SemanticElementSet", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSemanticElementSet_Name(), ecorePackage.getEString(), "name", "", 1, 1, SemanticElementSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getSemanticElementSet_Description(), ecorePackage.getEString(), "description", null, 0, 1, SemanticElementSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getSemanticElementSet_MessageModelName(), ecorePackage.getEString(), "messageModelName", null, 1, 1, SemanticElementSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED, !IS_ORDERED);
		initEReference(getSemanticElementSet_SyntaxModel(), this.getMessageSyntaxModel(), this.getMessageSyntaxModel_ElementSet(), "syntaxModel", null, 1, 1, SemanticElementSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getSemanticElementSet_Model(), this.getMessageModel(), this.getMessageModel_ElementSet(), "model", null, 1, 1, SemanticElementSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getSemanticElementSet_SemanticElements(), this.getSemanticElement(), this.getSemanticElement_ElementSet(), "semanticElements", null, 1, -1, SemanticElementSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getSemanticElementSet_Composite(), this.getSimpleMessageComposite(), this.getSimpleMessageComposite_ElementSet(), "composite", null, 0, -1, SemanticElementSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);

		initEClass(semanticElementEClass, SemanticElement.class, "SemanticElement", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSemanticElement_Name(), ecorePackage.getEString(), "name", "", 1, 1, SemanticElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getSemanticElement_Description(), ecorePackage.getEString(), "description", null, 0, 1, SemanticElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getSemanticElement_ElementType(), ecorePackage.getEString(), "elementType", null, 1, 1, SemanticElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getSemanticElement_Datatype(), this.getMDMIDatatype(), null, "datatype", null, 1, 1, SemanticElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getSemanticElement_PropertyQualifier(), ecorePackage.getEString(), "propertyQualifier", null, 0, -1, SemanticElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getSemanticElement_Composite(), this.getSimpleMessageComposite(), this.getSimpleMessageComposite_SemanticElements(), "composite", null, 0, 1, SemanticElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getSemanticElement_ElementSet(), this.getSemanticElementSet(), this.getSemanticElementSet_SemanticElements(), "elementSet", null, 1, 1, SemanticElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getSemanticElement_BusinessRules(), this.getSemanticElementBusinessRule(), this.getSemanticElementBusinessRule_SemanticElement(), "businessRules", null, 0, -1, SemanticElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getSemanticElement_DataRules(), this.getDataRule(), this.getDataRule_SemanticElement(), "dataRules", null, 0, -1, SemanticElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getSemanticElement_Relationships(), this.getSemanticElementRelationship(), null, "relationships", null, 0, -1, SemanticElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getSemanticElement_MultipleInstances(), ecorePackage.getEBoolean(), "multipleInstances", null, 1, 1, SemanticElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getSemanticElement_FromMdmi(), this.getToBusinessElement(), null, "fromMdmi", null, 1, -1, SemanticElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getSemanticElement_Ordering(), ecorePackage.getEString(), "ordering", null, 0, 1, SemanticElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getSemanticElement_OrderingLanguage(), ecorePackage.getEString(), "orderingLanguage", null, 0, 1, SemanticElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getSemanticElement_ComputedValue(), this.getMDMIExpression(), null, "computedValue", null, 0, 1, SemanticElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getSemanticElement_ComputedInValue(), this.getMDMIExpression(), null, "computedInValue", null, 0, 1, SemanticElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getSemanticElement_ToMdmi(), this.getToSemanticElement(), null, "toMdmi", null, 1, -1, SemanticElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getSemanticElement_Parent(), this.getSemanticElement(), this.getSemanticElement_Children(), "parent", null, 0, 1, SemanticElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getSemanticElement_Children(), this.getSemanticElement(), this.getSemanticElement_Parent(), "children", null, 0, -1, SemanticElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getSemanticElement_SyntaxNode(), this.getNode(), this.getNode_SemanticElement(), "syntaxNode", null, 0, 1, SemanticElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getSemanticElement_ComputedOutValue(), this.getMDMIExpression(), null, "computedOutValue", null, 0, 1, SemanticElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getSemanticElement_Keywords(), this.getKeyword(), this.getKeyword_Owner(), "keywords", null, 0, -1, SemanticElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);

		initEClass(simpleMessageCompositeEClass, SimpleMessageComposite.class, "SimpleMessageComposite", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSimpleMessageComposite_Name(), ecorePackage.getEString(), "name", null, 1, 1, SimpleMessageComposite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getSimpleMessageComposite_SemanticElements(), this.getSemanticElement(), this.getSemanticElement_Composite(), "semanticElements", null, 1, -1, SimpleMessageComposite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getSimpleMessageComposite_ElementSet(), this.getSemanticElementSet(), this.getSemanticElementSet_Composite(), "elementSet", null, 1, 1, SimpleMessageComposite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getSimpleMessageComposite_Description(), ecorePackage.getEString(), "description", null, 0, 1, SimpleMessageComposite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);

		initEClass(messageCompositeEClass, MessageComposite.class, "MessageComposite", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getMessageComposite_Composites(), this.getMessageComposite(), this.getMessageComposite_Owner(), "composites", null, 0, -1, MessageComposite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getMessageComposite_Owner(), this.getMessageComposite(), this.getMessageComposite_Composites(), "owner", null, 0, 1, MessageComposite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);

		initEClass(semanticElementBusinessRuleEClass, SemanticElementBusinessRule.class, "SemanticElementBusinessRule", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSemanticElementBusinessRule_Name(), ecorePackage.getEString(), "name", "", 1, 1, SemanticElementBusinessRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getSemanticElementBusinessRule_Description(), ecorePackage.getEString(), "description", null, 0, 1, SemanticElementBusinessRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getSemanticElementBusinessRule_Rule(), ecorePackage.getEString(), "rule", null, 1, 1, SemanticElementBusinessRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getSemanticElementBusinessRule_RuleExpressionLanguage(), ecorePackage.getEString(), "ruleExpressionLanguage", null, 0, 1, SemanticElementBusinessRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getSemanticElementBusinessRule_SemanticElement(), this.getSemanticElement(), this.getSemanticElement_BusinessRules(), "semanticElement", null, 1, 1, SemanticElementBusinessRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);

		initEClass(semanticElementRelationshipEClass, SemanticElementRelationship.class, "SemanticElementRelationship", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSemanticElementRelationship_Name(), ecorePackage.getEString(), "name", "", 1, 1, SemanticElementRelationship.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getSemanticElementRelationship_Description(), ecorePackage.getEString(), "description", null, 0, 1, SemanticElementRelationship.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getSemanticElementRelationship_Rule(), ecorePackage.getEString(), "rule", null, 1, 1, SemanticElementRelationship.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getSemanticElementRelationship_RuleExpressionLanguage(), ecorePackage.getEString(), "ruleExpressionLanguage", null, 0, 1, SemanticElementRelationship.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getSemanticElementRelationship_Context(), this.getSemanticElement(), null, "context", null, 1, 1, SemanticElementRelationship.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getSemanticElementRelationship_MinOccurs(), ecorePackage.getEInt(), "minOccurs", "1", 1, 1, SemanticElementRelationship.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getSemanticElementRelationship_MaxOccurs(), ecorePackage.getEInt(), "maxOccurs", "1", 1, 1, SemanticElementRelationship.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getSemanticElementRelationship_SourceIsInstance(), ecorePackage.getEBoolean(), "sourceIsInstance", "true", 1, 1, SemanticElementRelationship.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getSemanticElementRelationship_TargetIsInstance(), ecorePackage.getEBoolean(), "targetIsInstance", "true", 1, 1, SemanticElementRelationship.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);

		initEClass(mdmiBusinessElementReferenceEClass, MDMIBusinessElementReference.class, "MDMIBusinessElementReference", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMDMIBusinessElementReference_Name(), ecorePackage.getEString(), "name", "", 1, 1, MDMIBusinessElementReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getMDMIBusinessElementReference_Description(), ecorePackage.getEString(), "description", null, 0, 1, MDMIBusinessElementReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getMDMIBusinessElementReference_Reference(), this.getURI(), "reference", null, 1, 1, MDMIBusinessElementReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMDMIBusinessElementReference_UniqueIdentifier(), ecorePackage.getEString(), "uniqueIdentifier", null, 1, 1, MDMIBusinessElementReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getMDMIBusinessElementReference_BusinessRules(), this.getMDMIBusinessElementRule(), this.getMDMIBusinessElementRule_BusinessElement(), "businessRules", null, 0, -1, MDMIBusinessElementReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getMDMIBusinessElementReference_DomainDictionaryReference(), this.getMDMIDomainDictionaryReference(), this.getMDMIDomainDictionaryReference_BusinessElements(), "domainDictionaryReference", null, 1, 1, MDMIBusinessElementReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getMDMIBusinessElementReference_ReferenceDatatype(), this.getMDMIDatatype(), null, "referenceDatatype", null, 1, 1, MDMIBusinessElementReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getMDMIBusinessElementReference_EnumValueSetField(), ecorePackage.getEString(), "enumValueSetField", null, 0, 1, MDMIBusinessElementReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getMDMIBusinessElementReference_EnumValueField(), ecorePackage.getEString(), "enumValueField", null, 0, 1, MDMIBusinessElementReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getMDMIBusinessElementReference_EnumValueDescrField(), ecorePackage.getEString(), "enumValueDescrField", null, 0, 1, MDMIBusinessElementReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getMDMIBusinessElementReference_EnumValueSet(), ecorePackage.getEString(), "enumValueSet", null, 0, 1, MDMIBusinessElementReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);

		initEClass(mdmiBusinessElementRuleEClass, MDMIBusinessElementRule.class, "MDMIBusinessElementRule", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMDMIBusinessElementRule_Name(), ecorePackage.getEString(), "name", "", 1, 1, MDMIBusinessElementRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getMDMIBusinessElementRule_Description(), ecorePackage.getEString(), "description", null, 0, 1, MDMIBusinessElementRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getMDMIBusinessElementRule_Rule(), ecorePackage.getEString(), "rule", null, 1, 1, MDMIBusinessElementRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getMDMIBusinessElementRule_RuleExpressionLanguage(), ecorePackage.getEString(), "ruleExpressionLanguage", null, 0, 1, MDMIBusinessElementRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getMDMIBusinessElementRule_BusinessElement(), this.getMDMIBusinessElementReference(), this.getMDMIBusinessElementReference_BusinessRules(), "businessElement", null, 1, 1, MDMIBusinessElementRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);

		initEClass(toBusinessElementEClass, ToBusinessElement.class, "ToBusinessElement", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getToBusinessElement_BusinessElement(), this.getMDMIBusinessElementReference(), null, "businessElement", null, 1, 1, ToBusinessElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getToBusinessElement_Rule(), ecorePackage.getEString(), "rule", null, 0, 1, ToBusinessElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);

		initEClass(toSemanticElementEClass, ToSemanticElement.class, "ToSemanticElement", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getToSemanticElement_BusinessElement(), this.getMDMIBusinessElementReference(), null, "businessElement", null, 1, 1, ToSemanticElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getToSemanticElement_Rule(), ecorePackage.getEString(), "rule", null, 0, 1, ToSemanticElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);

		initEClass(conversionRuleEClass, ConversionRule.class, "ConversionRule", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getConversionRule_Name(), ecorePackage.getEString(), "name", "", 1, 1, ConversionRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getConversionRule_Description(), ecorePackage.getEString(), "description", null, 0, 1, ConversionRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getConversionRule_RuleExpressionLanguage(), ecorePackage.getEString(), "ruleExpressionLanguage", null, 0, 1, ConversionRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);

		initEClass(mdmiDomainDictionaryReferenceEClass, MDMIDomainDictionaryReference.class, "MDMIDomainDictionaryReference", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMDMIDomainDictionaryReference_Name(), ecorePackage.getEString(), "name", "", 1, 1, MDMIDomainDictionaryReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getMDMIDomainDictionaryReference_Description(), ecorePackage.getEString(), "description", null, 0, 1, MDMIDomainDictionaryReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getMDMIDomainDictionaryReference_BusinessElements(), this.getMDMIBusinessElementReference(), this.getMDMIBusinessElementReference_DomainDictionaryReference(), "businessElements", null, 1, -1, MDMIDomainDictionaryReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getMDMIDomainDictionaryReference_Reference(), this.getURI(), "reference", null, 1, 1, MDMIDomainDictionaryReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getMDMIDomainDictionaryReference_Group(), this.getMessageGroup(), this.getMessageGroup_DomainDictionary(), "group", null, 1, 1, MDMIDomainDictionaryReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);

		initEClass(mdmiExpressionEClass, MDMIExpression.class, "MDMIExpression", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMDMIExpression_Expression(), ecorePackage.getEString(), "expression", "", 1, 1, MDMIExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getMDMIExpression_Language(), ecorePackage.getEString(), "language", null, 1, 1, MDMIExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);

		initEClass(keywordEClass, Keyword.class, "Keyword", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getKeyword_Description(), ecorePackage.getEString(), "description", "", 1, 1, Keyword.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getKeyword_Keyword(), ecorePackage.getEString(), "keyword", null, 1, 1, Keyword.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getKeyword_KeywordValue(), ecorePackage.getEString(), "keywordValue", null, 0, 1, Keyword.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getKeyword_Reference(), ecorePackage.getEString(), "reference", null, 1, 1, Keyword.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getKeyword_Owner(), this.getSemanticElement(), this.getSemanticElement_Keywords(), "owner", null, 1, 1, Keyword.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);

		initEClass(mdmiDatatypeEClass, MDMIDatatype.class, "MDMIDatatype", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMDMIDatatype_TypeName(), ecorePackage.getEString(), "typeName", "", 1, 1, MDMIDatatype.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getMDMIDatatype_Description(), ecorePackage.getEString(), "description", null, 0, 1, MDMIDatatype.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getMDMIDatatype_Reference(), this.getURI(), "reference", null, 1, 1, MDMIDatatype.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMDMIDatatype_IsReadonly(), ecorePackage.getEBoolean(), "isReadonly", null, 0, 1, MDMIDatatype.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMDMIDatatype_TypeSpec(), ecorePackage.getEString(), "typeSpec", null, 0, 1, MDMIDatatype.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMDMIDatatype_Restriction(), ecorePackage.getEString(), "restriction", null, 0, 1, MDMIDatatype.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(dtsPrimitiveEClass, DTSPrimitive.class, "DTSPrimitive", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(dtcStructuredEClass, DTCStructured.class, "DTCStructured", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getDTCStructured_Fields(), this.getField(), null, "fields", null, 0, -1, DTCStructured.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(fieldEClass, Field.class, "Field", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getField_Name(), ecorePackage.getEString(), "name", "", 0, 1, Field.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getField_MinOccurs(), ecorePackage.getEInt(), "minOccurs", null, 0, 1, Field.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getField_MaxOccurs(), ecorePackage.getEInt(), "maxOccurs", null, 0, 1, Field.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getField_Datatype(), this.getMDMIDatatype(), null, "datatype", null, 1, 1, Field.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getField_Description(), ecorePackage.getEString(), "description", null, 0, 1, Field.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);

		initEClass(dtExternalEClass, DTExternal.class, "DTExternal", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(dtsDerivedEClass, DTSDerived.class, "DTSDerived", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getDTSDerived_BaseType(), this.getMDMIDatatype(), null, "baseType", null, 0, 1, DTSDerived.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(messageElementTypeEEnum, MessageElementType.class, "MessageElementType");
		addEEnumLiteral(messageElementTypeEEnum, MessageElementType.NORMAL);
		addEEnumLiteral(messageElementTypeEEnum, MessageElementType.COMPUTED);
		addEEnumLiteral(messageElementTypeEEnum, MessageElementType.LOCAL);

		// Initialize data types
		initEDataType(uriEDataType, String.class, "URI", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// http:///org/eclipse/emf/ecore/util/ExtendedMetaData
		createExtendedMetaDataAnnotations();
	}

	/**
	 * Initializes the annotations for <b>http:///org/eclipse/emf/ecore/util/ExtendedMetaData</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createExtendedMetaDataAnnotations() {
		String source = "http:///org/eclipse/emf/ecore/util/ExtendedMetaData";																
		addAnnotation
		  (getNode_SemanticElement(), 
		   source, 
		   new String[] {
			 "kind", "element"
		   });																																			
		addAnnotation
		  (getSemanticElement_Datatype(), 
		   source, 
		   new String[] {
			 "kind", "element"
		   });								
		addAnnotation
		  (getSemanticElement_Children(), 
		   source, 
		   new String[] {
			 "kind", "element"
		   });		
		addAnnotation
		  (getSemanticElement_SyntaxNode(), 
		   source, 
		   new String[] {
			 "kind", "element"
		   });																						
		addAnnotation
		  (getMDMIBusinessElementReference_BusinessRules(), 
		   source, 
		   new String[] {
			 "kind", "element"
		   });			
		addAnnotation
		  (getMDMIBusinessElementReference_ReferenceDatatype(), 
		   source, 
		   new String[] {
			 "kind", "element"
		   });												
		addAnnotation
		  (getToBusinessElement_BusinessElement(), 
		   source, 
		   new String[] {
			 "kind", "element"
		   });				
		addAnnotation
		  (getToSemanticElement_BusinessElement(), 
		   source, 
		   new String[] {
			 "kind", "element"
		   });																				
		addAnnotation
		  (uriEDataType, 
		   source, 
		   new String[] {
			 "pattern", "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?"
		   });			
		addAnnotation
		  (getField_Datatype(), 
		   source, 
		   new String[] {
			 "kind", "element"
		   });	
	}

} //MDMIPackageImpl
