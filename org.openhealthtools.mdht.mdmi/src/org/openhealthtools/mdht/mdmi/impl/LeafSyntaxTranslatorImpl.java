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

import org.openhealthtools.mdht.mdmi.LeafSyntaxTranslator;
import org.openhealthtools.mdht.mdmi.MDMIPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Leaf Syntax Translator</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openhealthtools.mdht.mdmi.impl.LeafSyntaxTranslatorImpl#getFormat <em>Format</em>}</li>
 *   <li>{@link org.openhealthtools.mdht.mdmi.impl.LeafSyntaxTranslatorImpl#getFormatExpressionLanguage <em>Format Expression Language</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class LeafSyntaxTranslatorImpl extends NodeImpl implements LeafSyntaxTranslator {
	/**
	 * The default value of the '{@link #getFormat() <em>Format</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFormat()
	 * @generated
	 * @ordered
	 */
	protected static final String FORMAT_EDEFAULT = "";

	/**
	 * The cached value of the '{@link #getFormat() <em>Format</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFormat()
	 * @generated
	 * @ordered
	 */
	protected String format = FORMAT_EDEFAULT;

	/**
	 * The default value of the '{@link #getFormatExpressionLanguage() <em>Format Expression Language</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFormatExpressionLanguage()
	 * @generated
	 * @ordered
	 */
	protected static final String FORMAT_EXPRESSION_LANGUAGE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFormatExpressionLanguage() <em>Format Expression Language</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFormatExpressionLanguage()
	 * @generated
	 * @ordered
	 */
	protected String formatExpressionLanguage = FORMAT_EXPRESSION_LANGUAGE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected LeafSyntaxTranslatorImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MDMIPackage.Literals.LEAF_SYNTAX_TRANSLATOR;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFormat(String newFormat) {
		String oldFormat = format;
		format = newFormat;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MDMIPackage.LEAF_SYNTAX_TRANSLATOR__FORMAT, oldFormat, format));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getFormatExpressionLanguage() {
		return formatExpressionLanguage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFormatExpressionLanguage(String newFormatExpressionLanguage) {
		String oldFormatExpressionLanguage = formatExpressionLanguage;
		formatExpressionLanguage = newFormatExpressionLanguage;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MDMIPackage.LEAF_SYNTAX_TRANSLATOR__FORMAT_EXPRESSION_LANGUAGE, oldFormatExpressionLanguage, formatExpressionLanguage));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case MDMIPackage.LEAF_SYNTAX_TRANSLATOR__FORMAT:
				return getFormat();
			case MDMIPackage.LEAF_SYNTAX_TRANSLATOR__FORMAT_EXPRESSION_LANGUAGE:
				return getFormatExpressionLanguage();
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
			case MDMIPackage.LEAF_SYNTAX_TRANSLATOR__FORMAT:
				setFormat((String)newValue);
				return;
			case MDMIPackage.LEAF_SYNTAX_TRANSLATOR__FORMAT_EXPRESSION_LANGUAGE:
				setFormatExpressionLanguage((String)newValue);
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
			case MDMIPackage.LEAF_SYNTAX_TRANSLATOR__FORMAT:
				setFormat(FORMAT_EDEFAULT);
				return;
			case MDMIPackage.LEAF_SYNTAX_TRANSLATOR__FORMAT_EXPRESSION_LANGUAGE:
				setFormatExpressionLanguage(FORMAT_EXPRESSION_LANGUAGE_EDEFAULT);
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
			case MDMIPackage.LEAF_SYNTAX_TRANSLATOR__FORMAT:
				return FORMAT_EDEFAULT == null ? format != null : !FORMAT_EDEFAULT.equals(format);
			case MDMIPackage.LEAF_SYNTAX_TRANSLATOR__FORMAT_EXPRESSION_LANGUAGE:
				return FORMAT_EXPRESSION_LANGUAGE_EDEFAULT == null ? formatExpressionLanguage != null : !FORMAT_EXPRESSION_LANGUAGE_EDEFAULT.equals(formatExpressionLanguage);
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
		result.append(" (format: ");
		result.append(format);
		result.append(", formatExpressionLanguage: ");
		result.append(formatExpressionLanguage);
		result.append(')');
		return result.toString();
	}

} //LeafSyntaxTranslatorImpl
