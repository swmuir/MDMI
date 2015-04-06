/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.openhealthtools.mdht.mdmi.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.openhealthtools.mdht.mdmi.MDMIBusinessElementReference;
import org.openhealthtools.mdht.mdmi.MDMIPackage;
import org.openhealthtools.mdht.mdmi.ToBusinessElement;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>To Business Element</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openhealthtools.mdht.mdmi.impl.ToBusinessElementImpl#getBusinessElement <em>Business Element</em>}</li>
 *   <li>{@link org.openhealthtools.mdht.mdmi.impl.ToBusinessElementImpl#getRule <em>Rule</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ToBusinessElementImpl extends ConversionRuleImpl implements ToBusinessElement {
	/**
	 * The cached value of the '{@link #getBusinessElement() <em>Business Element</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBusinessElement()
	 * @generated
	 * @ordered
	 */
	protected MDMIBusinessElementReference businessElement;

	/**
	 * The default value of the '{@link #getRule() <em>Rule</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRule()
	 * @generated
	 * @ordered
	 */
	protected static final String RULE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getRule() <em>Rule</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRule()
	 * @generated
	 * @ordered
	 */
	protected String rule = RULE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ToBusinessElementImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MDMIPackage.Literals.TO_BUSINESS_ELEMENT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MDMIBusinessElementReference getBusinessElement() {
		return businessElement;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setBusinessElement(MDMIBusinessElementReference newBusinessElement) {
		MDMIBusinessElementReference oldBusinessElement = businessElement;
		businessElement = newBusinessElement;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MDMIPackage.TO_BUSINESS_ELEMENT__BUSINESS_ELEMENT, oldBusinessElement, businessElement));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getRule() {
		return rule;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRule(String newRule) {
		String oldRule = rule;
		rule = newRule;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MDMIPackage.TO_BUSINESS_ELEMENT__RULE, oldRule, rule));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case MDMIPackage.TO_BUSINESS_ELEMENT__BUSINESS_ELEMENT:
				return getBusinessElement();
			case MDMIPackage.TO_BUSINESS_ELEMENT__RULE:
				return getRule();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case MDMIPackage.TO_BUSINESS_ELEMENT__BUSINESS_ELEMENT:
				setBusinessElement((MDMIBusinessElementReference)newValue);
				return;
			case MDMIPackage.TO_BUSINESS_ELEMENT__RULE:
				setRule((String)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case MDMIPackage.TO_BUSINESS_ELEMENT__BUSINESS_ELEMENT:
				setBusinessElement((MDMIBusinessElementReference)null);
				return;
			case MDMIPackage.TO_BUSINESS_ELEMENT__RULE:
				setRule(RULE_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case MDMIPackage.TO_BUSINESS_ELEMENT__BUSINESS_ELEMENT:
				return businessElement != null;
			case MDMIPackage.TO_BUSINESS_ELEMENT__RULE:
				return RULE_EDEFAULT == null ? rule != null : !RULE_EDEFAULT.equals(rule);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (rule: ");
		result.append(rule);
		result.append(')');
		return result.toString();
	}

} //ToBusinessElementImpl
