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
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.openhealthtools.mdht.mdmi.MDMIDatatype;
import org.openhealthtools.mdht.mdmi.MDMIPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Datatype</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openhealthtools.mdht.mdmi.impl.MDMIDatatypeImpl#getTypeName <em>Type Name</em>}</li>
 *   <li>{@link org.openhealthtools.mdht.mdmi.impl.MDMIDatatypeImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.openhealthtools.mdht.mdmi.impl.MDMIDatatypeImpl#getReference <em>Reference</em>}</li>
 *   <li>{@link org.openhealthtools.mdht.mdmi.impl.MDMIDatatypeImpl#isIsReadonly <em>Is Readonly</em>}</li>
 *   <li>{@link org.openhealthtools.mdht.mdmi.impl.MDMIDatatypeImpl#getTypeSpec <em>Type Spec</em>}</li>
 *   <li>{@link org.openhealthtools.mdht.mdmi.impl.MDMIDatatypeImpl#getRestriction <em>Restriction</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class MDMIDatatypeImpl extends EObjectImpl implements MDMIDatatype {
	/**
	 * The default value of the '{@link #getTypeName() <em>Type Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTypeName()
	 * @generated
	 * @ordered
	 */
	protected static final String TYPE_NAME_EDEFAULT = "";

	/**
	 * The cached value of the '{@link #getTypeName() <em>Type Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTypeName()
	 * @generated
	 * @ordered
	 */
	protected String typeName = TYPE_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected static final String DESCRIPTION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected String description = DESCRIPTION_EDEFAULT;

	/**
	 * The default value of the '{@link #getReference() <em>Reference</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReference()
	 * @generated
	 * @ordered
	 */
	protected static final String REFERENCE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getReference() <em>Reference</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReference()
	 * @generated
	 * @ordered
	 */
	protected String reference = REFERENCE_EDEFAULT;

	/**
	 * The default value of the '{@link #isIsReadonly() <em>Is Readonly</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIsReadonly()
	 * @generated
	 * @ordered
	 */
	protected static final boolean IS_READONLY_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isIsReadonly() <em>Is Readonly</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIsReadonly()
	 * @generated
	 * @ordered
	 */
	protected boolean isReadonly = IS_READONLY_EDEFAULT;

	/**
	 * The default value of the '{@link #getTypeSpec() <em>Type Spec</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTypeSpec()
	 * @generated
	 * @ordered
	 */
	protected static final String TYPE_SPEC_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTypeSpec() <em>Type Spec</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTypeSpec()
	 * @generated
	 * @ordered
	 */
	protected String typeSpec = TYPE_SPEC_EDEFAULT;

	/**
	 * The default value of the '{@link #getRestriction() <em>Restriction</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRestriction()
	 * @generated
	 * @ordered
	 */
	protected static final String RESTRICTION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getRestriction() <em>Restriction</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRestriction()
	 * @generated
	 * @ordered
	 */
	protected String restriction = RESTRICTION_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected MDMIDatatypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MDMIPackage.Literals.MDMI_DATATYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getTypeName() {
		return typeName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTypeName(String newTypeName) {
		String oldTypeName = typeName;
		typeName = newTypeName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MDMIPackage.MDMI_DATATYPE__TYPE_NAME, oldTypeName, typeName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDescription(String newDescription) {
		String oldDescription = description;
		description = newDescription;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MDMIPackage.MDMI_DATATYPE__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getReference() {
		return reference;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setReference(String newReference) {
		String oldReference = reference;
		reference = newReference;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MDMIPackage.MDMI_DATATYPE__REFERENCE, oldReference, reference));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isIsReadonly() {
		return isReadonly;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setIsReadonly(boolean newIsReadonly) {
		boolean oldIsReadonly = isReadonly;
		isReadonly = newIsReadonly;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MDMIPackage.MDMI_DATATYPE__IS_READONLY, oldIsReadonly, isReadonly));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getTypeSpec() {
		return typeSpec;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTypeSpec(String newTypeSpec) {
		String oldTypeSpec = typeSpec;
		typeSpec = newTypeSpec;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MDMIPackage.MDMI_DATATYPE__TYPE_SPEC, oldTypeSpec, typeSpec));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getRestriction() {
		return restriction;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRestriction(String newRestriction) {
		String oldRestriction = restriction;
		restriction = newRestriction;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MDMIPackage.MDMI_DATATYPE__RESTRICTION, oldRestriction, restriction));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case MDMIPackage.MDMI_DATATYPE__TYPE_NAME:
				return getTypeName();
			case MDMIPackage.MDMI_DATATYPE__DESCRIPTION:
				return getDescription();
			case MDMIPackage.MDMI_DATATYPE__REFERENCE:
				return getReference();
			case MDMIPackage.MDMI_DATATYPE__IS_READONLY:
				return isIsReadonly();
			case MDMIPackage.MDMI_DATATYPE__TYPE_SPEC:
				return getTypeSpec();
			case MDMIPackage.MDMI_DATATYPE__RESTRICTION:
				return getRestriction();
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
			case MDMIPackage.MDMI_DATATYPE__TYPE_NAME:
				setTypeName((String)newValue);
				return;
			case MDMIPackage.MDMI_DATATYPE__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case MDMIPackage.MDMI_DATATYPE__REFERENCE:
				setReference((String)newValue);
				return;
			case MDMIPackage.MDMI_DATATYPE__IS_READONLY:
				setIsReadonly((Boolean)newValue);
				return;
			case MDMIPackage.MDMI_DATATYPE__TYPE_SPEC:
				setTypeSpec((String)newValue);
				return;
			case MDMIPackage.MDMI_DATATYPE__RESTRICTION:
				setRestriction((String)newValue);
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
			case MDMIPackage.MDMI_DATATYPE__TYPE_NAME:
				setTypeName(TYPE_NAME_EDEFAULT);
				return;
			case MDMIPackage.MDMI_DATATYPE__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case MDMIPackage.MDMI_DATATYPE__REFERENCE:
				setReference(REFERENCE_EDEFAULT);
				return;
			case MDMIPackage.MDMI_DATATYPE__IS_READONLY:
				setIsReadonly(IS_READONLY_EDEFAULT);
				return;
			case MDMIPackage.MDMI_DATATYPE__TYPE_SPEC:
				setTypeSpec(TYPE_SPEC_EDEFAULT);
				return;
			case MDMIPackage.MDMI_DATATYPE__RESTRICTION:
				setRestriction(RESTRICTION_EDEFAULT);
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
			case MDMIPackage.MDMI_DATATYPE__TYPE_NAME:
				return TYPE_NAME_EDEFAULT == null ? typeName != null : !TYPE_NAME_EDEFAULT.equals(typeName);
			case MDMIPackage.MDMI_DATATYPE__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case MDMIPackage.MDMI_DATATYPE__REFERENCE:
				return REFERENCE_EDEFAULT == null ? reference != null : !REFERENCE_EDEFAULT.equals(reference);
			case MDMIPackage.MDMI_DATATYPE__IS_READONLY:
				return isReadonly != IS_READONLY_EDEFAULT;
			case MDMIPackage.MDMI_DATATYPE__TYPE_SPEC:
				return TYPE_SPEC_EDEFAULT == null ? typeSpec != null : !TYPE_SPEC_EDEFAULT.equals(typeSpec);
			case MDMIPackage.MDMI_DATATYPE__RESTRICTION:
				return RESTRICTION_EDEFAULT == null ? restriction != null : !RESTRICTION_EDEFAULT.equals(restriction);
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
		result.append(" (typeName: ");
		result.append(typeName);
		result.append(", description: ");
		result.append(description);
		result.append(", reference: ");
		result.append(reference);
		result.append(", isReadonly: ");
		result.append(isReadonly);
		result.append(", typeSpec: ");
		result.append(typeSpec);
		result.append(", restriction: ");
		result.append(restriction);
		result.append(')');
		return result.toString();
	}

} //MDMIDatatypeImpl
