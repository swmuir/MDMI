/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.openhealthtools.mdht.mdmi;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Conversion Rule</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>ConversionRule is an abstract class that defines a rule used to convert values.</p>
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openhealthtools.mdht.mdmi.ConversionRule#getName <em>Name</em>}</li>
 *   <li>{@link org.openhealthtools.mdht.mdmi.ConversionRule#getDescription <em>Description</em>}</li>
 *   <li>{@link org.openhealthtools.mdht.mdmi.ConversionRule#getRuleExpressionLanguage <em>Rule Expression Language</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openhealthtools.mdht.mdmi.MDMIPackage#getConversionRule()
 * @model abstract="true"
 * @generated
 */
public interface ConversionRule extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * The default value is <code>""</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>A "name" property of type String that names the ConversionRule.</p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.openhealthtools.mdht.mdmi.MDMIPackage#getConversionRule_Name()
	 * @model default="" required="true" ordered="false"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.openhealthtools.mdht.mdmi.ConversionRule#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>The optional "description" property of type String describes the ToBusinessElement.</p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see org.openhealthtools.mdht.mdmi.MDMIPackage#getConversionRule_Description()
	 * @model ordered="false"
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.openhealthtools.mdht.mdmi.ConversionRule#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Rule Expression Language</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>A "ruleExpressionLanguage" property whose value is a reference to the expression language used to define the rule. This scope of the language allowed in conversions should be limited so that only very straightforward transformations are possible. This is because these ConversionRules can be used to define the semantic distance between business elements in a central dictionary by identifying "near synonyms". It is important that the "near synonyms do not turn out to be far synonyms.</p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Rule Expression Language</em>' attribute.
	 * @see #setRuleExpressionLanguage(String)
	 * @see org.openhealthtools.mdht.mdmi.MDMIPackage#getConversionRule_RuleExpressionLanguage()
	 * @model ordered="false"
	 * @generated
	 */
	String getRuleExpressionLanguage();

	/**
	 * Sets the value of the '{@link org.openhealthtools.mdht.mdmi.ConversionRule#getRuleExpressionLanguage <em>Rule Expression Language</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Rule Expression Language</em>' attribute.
	 * @see #getRuleExpressionLanguage()
	 * @generated
	 */
	void setRuleExpressionLanguage(String value);

} // ConversionRule
