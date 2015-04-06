/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.openhealthtools.mdht.mdmi;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>DTS Derived</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openhealthtools.mdht.mdmi.DTSDerived#getBaseType <em>Base Type</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openhealthtools.mdht.mdmi.MDMIPackage#getDTSDerived()
 * @model
 * @generated
 */
public interface DTSDerived extends DTSPrimitive {
	/**
	 * Returns the value of the '<em><b>Base Type</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Base Type</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Base Type</em>' reference.
	 * @see #setBaseType(MDMIDatatype)
	 * @see org.openhealthtools.mdht.mdmi.MDMIPackage#getDTSDerived_BaseType()
	 * @model
	 * @generated
	 */
	MDMIDatatype getBaseType();

	/**
	 * Sets the value of the '{@link org.openhealthtools.mdht.mdmi.DTSDerived#getBaseType <em>Base Type</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Base Type</em>' reference.
	 * @see #getBaseType()
	 * @generated
	 */
	void setBaseType(MDMIDatatype value);

} // DTSDerived
