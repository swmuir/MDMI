/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.openhealthtools.mdht.mdmi;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Message Composite</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>The MessageComposite class inherits from the SimpleMessageComposite class, allowing the construction of a complex object tree. MessageComposite are an informative artifact that can be useful when there is a desire to associated SemanticElements with a complex object model.</p>
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openhealthtools.mdht.mdmi.MessageComposite#getComposites <em>Composites</em>}</li>
 *   <li>{@link org.openhealthtools.mdht.mdmi.MessageComposite#getOwner <em>Owner</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openhealthtools.mdht.mdmi.MDMIPackage#getMessageComposite()
 * @model
 * @generated
 */
public interface MessageComposite extends SimpleMessageComposite {
	/**
	 * Returns the value of the '<em><b>Composites</b></em>' reference list.
	 * The list contents are of type {@link org.openhealthtools.mdht.mdmi.MessageComposite}.
	 * It is bidirectional and its opposite is '{@link org.openhealthtools.mdht.mdmi.MessageComposite#getOwner <em>Owner</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Composites</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Composites</em>' reference list.
	 * @see org.openhealthtools.mdht.mdmi.MDMIPackage#getMessageComposite_Composites()
	 * @see org.openhealthtools.mdht.mdmi.MessageComposite#getOwner
	 * @model opposite="owner" ordered="false"
	 * @generated
	 */
	EList<MessageComposite> getComposites();

	/**
	 * Returns the value of the '<em><b>Owner</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link org.openhealthtools.mdht.mdmi.MessageComposite#getComposites <em>Composites</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Owner</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Owner</em>' reference.
	 * @see #setOwner(MessageComposite)
	 * @see org.openhealthtools.mdht.mdmi.MDMIPackage#getMessageComposite_Owner()
	 * @see org.openhealthtools.mdht.mdmi.MessageComposite#getComposites
	 * @model opposite="composites" ordered="false"
	 * @generated
	 */
	MessageComposite getOwner();

	/**
	 * Sets the value of the '{@link org.openhealthtools.mdht.mdmi.MessageComposite#getOwner <em>Owner</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Owner</em>' reference.
	 * @see #getOwner()
	 * @generated
	 */
	void setOwner(MessageComposite value);

} // MessageComposite
