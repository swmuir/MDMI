/*******************************************************************************
* Copyright (c) 2012 Firestar Software, Inc.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Firestar Software, Inc. - initial API and implementation
*
* Author:
*     Sally Conway
*
*******************************************************************************/
package org.openhealthtools.mdht.mdmi.editor.map.tree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.tree.TreeNode;

import org.openhealthtools.mdht.mdmi.Mdmi;
import org.openhealthtools.mdht.mdmi.MdmiResolver;
import org.openhealthtools.mdht.mdmi.MdmiValueSetsHandler;
import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.map.editor.AbstractComponentEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.BooleanField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.BusinessElementReferenceField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.DataEntryFieldInfo;
import org.openhealthtools.mdht.mdmi.editor.map.editor.DataFormatException;
import org.openhealthtools.mdht.mdmi.editor.map.editor.GenericEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.IEditorField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.MdmiDatatypeField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.RuleField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.StringField;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ValueSetMapEditor;
import org.openhealthtools.mdht.mdmi.model.ConversionRule;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.ToBusinessElement;
import org.openhealthtools.mdht.mdmi.model.ToMessageElement;
import org.openhealthtools.mdht.mdmi.model.validate.ModelInfo;

/** Node for a ConversionRule */
public abstract class ConversionRuleNode extends EditableObjectNode {

	protected ConversionRuleNode(ConversionRule convRule) {
		super(convRule);
		setNodeIcon(TreeNodeIcon.ConversionRuleIcon);
	}

	@Override
	public String getDisplayName(Object userObject) {
		ConversionRule conversionRule = (ConversionRule)userObject;
		String name = conversionRule.getName();
		if (name != null && name.length() > 0) {
			return name;
		}
		// use rule
		String rule = conversionRule.getRule();
		if (rule == null) {
			rule = s_res.getString("EditableObjectNode.unNamedNodeDisplay");
		}
		if (rule.length() > 33) {
			rule = rule.substring(0,30) + "...";
		}
		return rule;
	}

	@Override
	public String getToolTipText() {
		return ((ConversionRule)getUserObject()).getDescription();
	}


//	@Override
//	public String getDisplayName(Object userObject) {
//		return ((ConversionRule)userObject).getName();
//	}

	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public boolean isRemovable() {
		return true;
	}
	
	@Override
	public AbstractComponentEditor getEditorForNode() {
		return new CustomEditor(getMessageGroup(), getUserObject().getClass());
	}

	/** Get the parent SemanticElement */
	public SemanticElement getSemanticElement() {
		// older model doesn't have parent - so we need to search the tree
		SemanticElement semanticElement = ((ConversionRule)getUserObject()).getOwner();
		if (semanticElement != null) {
			return semanticElement;
		}
		TreeNode parentNode = getParent();
		while (parentNode != null) {
			if (parentNode instanceof SemanticElementNode) {
				return (SemanticElement)((SemanticElementNode)parentNode).getUserObject();
			}
			parentNode = parentNode.getParent();
		}
		return null;
	}
	
	@Override
	public String getRulesExpressionLanguage() {
		String language = ((ConversionRule)userObject).getRuleExpressionLanguage();
		if (language == null || language.length() == 0) {
			language = getDefaultRulesExpressionLanguage();
		}
		return language;
	}

	public String getDefaultRulesExpressionLanguage() {
		String language = super.getRulesExpressionLanguage();
		return language;
	}

	/** Isomorphic is defined as the semantic element and the business element reference
	 * having the same datatype
	 * @return
	 */
	public boolean isIsomorphic() {
		boolean isIso = false;
		
		ConversionRule conversionRule = (ConversionRule)userObject;
		
		SemanticElement semanticElement = getSemanticElement();
		MdmiDatatype seDatatype = semanticElement.getDatatype();
		
		if (seDatatype != null) {
			MdmiBusinessElementReference ber = null;
			if (conversionRule instanceof ToBusinessElement) {
				ber = ((ToBusinessElement)conversionRule).getBusinessElement();
			} else if (conversionRule instanceof ToMessageElement) {
				ber = ((ToMessageElement)conversionRule).getBusinessElement();
			}
			if (ber != null) {
				isIso = (ber.getReferenceDatatype() == seDatatype);
			}
		}
//		for (ToMessageElement toMdmi : semanticElement.getToMdmi()) {
//			for (ToBusinessElement toBER : semanticElement.getFromMdmi()) {
//				
//				if (toMdmi == conversionRule || toBER == conversionRule) {
//					if (toMdmi.getBusinessElement() == toBER.getBusinessElement()) {
//						return true;
//					}
//				}
//				
//			}
//		}
		
		return isIso;
	}


	//////////////////////////////////////////////////////////////////
	//    Custom Classes
	//////////////////////////////////////////////////////////////
	public class CustomEditor extends AbstractRuleEditor {
		private BooleanField m_isomorphicField;
		private MdmiDatatypeField m_semanticElementDatatypeField;
		
		public CustomEditor(MessageGroup group, Class<?> objectClass) {
			super(group, objectClass);
		}

		/** Determine if this field should be shown read-only */
		@Override
		public boolean isReadOnlyFields(String fieldName) {
			// Owner is read-only
			return "Owner".equalsIgnoreCase(fieldName);
		}
		
		@Override
		protected IEditorField createEditorField(DataEntryFieldInfo fieldInfo) {
			if ("EnumExtResolverUri".equalsIgnoreCase(fieldInfo.getFieldName())) {
				// we want to add a button to show the mapping
				return new ViewMapField(this);
			}
			return super.createEditorField(fieldInfo);
		}

		
		@Override
		protected void createDataEntryFields(List<Method[]> methodPairList) {
			
			// Add a field to show if Isomorphic
			m_isomorphicField = new BooleanField(this, "Isomorphic");
			try {
				boolean isomorphic = isIsomorphic();
				if (isomorphic) {
					// make it stand out
					m_isomorphicField.setForeground(Color.red);
					Font font = m_isomorphicField.getFont();
					font = font.deriveFont(Font.BOLD);
					m_isomorphicField.setFont(font);
				}
				m_isomorphicField.setDisplayValue(Boolean.valueOf(isomorphic));
			} catch (DataFormatException e) {
				// don't care
			}
			m_isomorphicField.setReadOnly();
			addLabeledField(null, m_isomorphicField, 0.0, GridBagConstraints.NONE);

			super.createDataEntryFields(methodPairList);
			
			// Add a field to show Semantic Element's datatype
			m_semanticElementDatatypeField = new MdmiDatatypeField(this, MdmiDatatype.class);
			m_semanticElementDatatypeField.setReadOnly();
			addLabeledField("Owner Data Type",
					m_semanticElementDatatypeField, 0.0, GridBagConstraints.HORIZONTAL);
			if ( ((ConversionRule)getUserObject()).getOwner() != null) {
				MdmiDatatype datatype = ((ConversionRule)getUserObject()).getOwner().getDatatype();
				if (datatype != null) {
					m_semanticElementDatatypeField.setDisplayValue(datatype);
				}
			}
		}

		@Override
		public String getDefaultRuleLanguage() {
			return getDefaultRulesExpressionLanguage();
		}

		@Override
		public String getRuleLanguage() {
			return getConstraintExpressionLanguage();
		}

		@Override
		protected List<ModelInfo> validateRuleField(RuleField ruleField) {
			List<ModelInfo> errors = new ArrayList<ModelInfo>();
			SemanticElement semanticElement = getSemanticElement();
			if (semanticElement != null) {
			   ConversionRule cr = (ConversionRule)userObject;
				errors = ruleField.validateActionRule(cr.getActualRuleExpressionLanguage(), semanticElement);
			}
			return errors;
		}

	}



	// A StringField, with a button to view the map
	protected class ViewMapField extends StringField implements ActionListener {
		private JButton m_viewBtn;

		ViewMapField(GenericEditor parentEditor) {
			super(parentEditor, 1, 10);
			m_viewBtn = new JButton("View Map...");
			m_viewBtn.setIcon(AbstractComponentEditor.getIcon(this.getClass(),
					SemanticElementNode.s_res.getString("ViewMapField.icon")));
			JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, Standards.LEFT_INSET, 0));
			btnPanel.add(m_viewBtn);
			add(BorderLayout.EAST, btnPanel);
			
			// disable button if not mapped
		}


		@Override
		public void setReadOnly() {
			m_viewBtn.setEnabled(false);
			super.setReadOnly();
		}

		@Override
		public void addNotify() {
			super.addNotify();
			m_viewBtn.addActionListener(this);
			m_viewBtn.setToolTipText(DataTypeNode.s_res.getString("ViewMapField.toolTip"));
		}

		@Override
		public void removeNotify() {
			m_viewBtn.removeActionListener(this);
			m_viewBtn.setToolTipText(null);
			super.removeNotify();
		}

		/** Display the value set map between the SE and BER */
		private void showValueSetMap() {
			String srcValueSetName = null;
			String targetValueSetName = null;
			boolean hasBERValueSet = false;
			boolean hasSEValueSet = false;
			
			ConversionRule rule  = (ConversionRule)getUserObject();
			SemanticElement se = rule.getOwner();

			MessageGroup group = se.getElementSet().getModel().getGroup();

			// get the Business Element from the dialog, since the user can change it
			MdmiBusinessElementReference ber = getSelectedMdmiBusinessElementReference();
			
			
			if (rule instanceof ToBusinessElement) {
				// Source is the SE, Target is the BER
				srcValueSetName = se.getEnumValueSet();
				if (srcValueSetName != null && !srcValueSetName.isEmpty()) {
					hasSEValueSet = true;
				}
				
				if (ber != null) {
					targetValueSetName = ber.getEnumValueSet();
					if (targetValueSetName != null && !targetValueSetName.isEmpty()) {
						hasBERValueSet = true;
					}
				}
			} else if (rule instanceof ToMessageElement) {
				// Source is the BER, Target is the SE
				if (ber != null) {
					srcValueSetName = ber.getEnumValueSet();
					if (srcValueSetName != null && !srcValueSetName.isEmpty()) {
						hasBERValueSet = true;
					}
				}
				
				targetValueSetName = se.getEnumValueSet();
				if (targetValueSetName != null && !targetValueSetName.isEmpty()) {
					hasSEValueSet = true;
				}
			}
			
			// show map
			if (hasBERValueSet && hasSEValueSet) {
				
				MdmiResolver resolver = Mdmi.INSTANCE.getResolver();
				if (resolver != null) {
					MdmiValueSetsHandler handler = resolver.getValueSetsHandler(group.getName());
					ValueSetMapEditor dlg = new ValueSetMapEditor((JFrame)this.getTopLevelAncestor(), 
							false, handler, srcValueSetName, targetValueSetName);
					dlg.display();
				} else {
					JOptionPane.showMessageDialog(this, "The MDMI Resolver cannot be identified", 
							"Error", JOptionPane.ERROR_MESSAGE);
				}
				
			} else {
				// don't have the data we need - show an error message
				StringBuilder buf = new StringBuilder();
				if (!hasBERValueSet && !hasSEValueSet) {
					buf.append("Neither the Business Element Reference, nor the");
					buf.append("Semantic Element,");
					buf.append("have value sets defined");
				} else if (!hasBERValueSet) {
					buf.append("The Business Element Reference does not have a value set defined");
				} else if (!hasSEValueSet) {
					buf.append("The Semantic Element does not have a value set defined");
				}
				JOptionPane.showMessageDialog(this, buf.toString(), "Value Set Undefined", JOptionPane.WARNING_MESSAGE);
				
			}
		}


		/** get the currently selected BER */
		private MdmiBusinessElementReference getSelectedMdmiBusinessElementReference() {
			MdmiBusinessElementReference ber = null;
			BusinessElementReferenceField beField = findBusinessElementReferenceField();
			if (beField != null) {
				Object selected = beField.getValue();
				if (selected instanceof MdmiBusinessElementReference) {
					ber = (MdmiBusinessElementReference)selected;
				}
			}
			return ber;
		}

		
		/** Find the edit field that supports editing the business element */
		private BusinessElementReferenceField findBusinessElementReferenceField() {

			for (DataEntryFieldInfo fieldInfo : getParentEditor().getDataEntryFieldList()) {
				if (fieldInfo.getEditComponent() instanceof BusinessElementReferenceField) {
					return (BusinessElementReferenceField)fieldInfo.getEditComponent();
				}
			}
			return null;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == m_viewBtn) {
				showValueSetMap();
			}
		}

	}
}
