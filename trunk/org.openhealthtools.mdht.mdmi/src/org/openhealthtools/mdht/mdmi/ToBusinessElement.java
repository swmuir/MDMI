/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.openhealthtools.mdht.mdmi;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>To Business Element</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>The ToBusinessElement associates a BusinessElementReference with a SemanticElement, describing the directed conversion rule for converting the value of the Semantic element to the reference value of the referenced business element. A SemanticElement may be related to more than one BusinessElementReference but will have a separate ToBusinessElement class with individual rules for each relationship.</p>
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openhealthtools.mdht.mdmi.ToBusinessElement#getBusinessElement <em>Business Element</em>}</li>
 *   <li>{@link org.openhealthtools.mdht.mdmi.ToBusinessElement#getRule <em>Rule</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openhealthtools.mdht.mdmi.MDMIPackage#getToBusinessElement()
 * @model
 * @generated
 */
public interface ToBusinessElement extends ConversionRule {
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
	 * @see org.openhealthtools.mdht.mdmi.MDMIPackage#getToBusinessElement_BusinessElement()
	 * @model resolveProxies="false" required="true" ordered="false"
	 *        extendedMetaData="kind='element'"
	 * @generated
	 */
	MDMIBusinessElementReference getBusinessElement();

	/**
	 * Sets the value of the '{@link org.openhealthtools.mdht.mdmi.ToBusinessElement#getBusinessElement <em>Business Element</em>}' reference.
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
	 * @see org.openhealthtools.mdht.mdmi.MDMIPackage#getToBusinessElement_Rule()
	 * @model ordered="false"
	 * @generated
	 */
	String getRule();

	/**
	 * Sets the value of the '{@link org.openhealthtools.mdht.mdmi.ToBusinessElement#getRule <em>Rule</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Rule</em>' attribute.
	 * @see #getRule()
	 * @generated
	 */
	void setRule(String value);

} // ToBusinessElement
