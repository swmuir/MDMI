/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.openhealthtools.mdht.mdmi;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>To Semantic Element</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>The ToSemanticElement associates a BusinessElement to a SemanticElement, describing the directed conversion rule for converting the reference value of the BusinessElement to the value in the SemanticElement. A BusinessElementReference may be related to more than one SemanticElement but will have a separate ToSemanticElement class with individual rules for each relationship.</p>
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openhealthtools.mdht.mdmi.ToSemanticElement#getBusinessElement <em>Business Element</em>}</li>
 *   <li>{@link org.openhealthtools.mdht.mdmi.ToSemanticElement#getRule <em>Rule</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openhealthtools.mdht.mdmi.MDMIPackage#getToSemanticElement()
 * @model
 * @generated
 */
public interface ToSemanticElement extends ConversionRule {
	/**
	 * Returns the value of the '<em><b>Business Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Business Element</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Business Element</em>' reference.
	 * @see #setBusinessElement(MDMIBusinessElementReference)
	 * @see org.openhealthtools.mdht.mdmi.MDMIPackage#getToSemanticElement_BusinessElement()
	 * @model resolveProxies="false" required="true" ordered="false"
	 *        extendedMetaData="kind='element'"
	 * @generated
	 */
	MDMIBusinessElementReference getBusinessElement();

	/**
	 * Sets the value of the '{@link org.openhealthtools.mdht.mdmi.ToSemanticElement#getBusinessElement <em>Business Element</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Business Element</em>' reference.
	 * @see #getBusinessElement()
	 * @generated
	 */
	void setBusinessElement(MDMIBusinessElementReference value);

	/**
	 * Returns the value of the '<em><b>Rule</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>A "rule" property of type String that holds an expression for converting one value to another.</p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Rule</em>' attribute.
	 * @see #setRule(String)
	 * @see org.openhealthtools.mdht.mdmi.MDMIPackage#getToSemanticElement_Rule()
	 * @model ordered="false"
	 * @generated
	 */
	String getRule();

	/**
	 * Sets the value of the '{@link org.openhealthtools.mdht.mdmi.ToSemanticElement#getRule <em>Rule</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Rule</em>' attribute.
	 * @see #getRule()
	 * @generated
	 */
	void setRule(String value);

} // ToSemanticElement
