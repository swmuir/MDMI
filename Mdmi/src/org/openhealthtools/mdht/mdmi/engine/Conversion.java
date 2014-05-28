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
 *     Gabriel Oancea
 *
 *******************************************************************************/
package org.openhealthtools.mdht.mdmi.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

import org.openhealthtools.mdht.mdmi.IElementValue;
import org.openhealthtools.mdht.mdmi.MdmiException;
import org.openhealthtools.mdht.mdmi.MdmiTransferInfo;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDomainDictionaryReference;
import org.openhealthtools.mdht.mdmi.model.MessageModel;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.ToBusinessElement;
import org.openhealthtools.mdht.mdmi.model.ToMessageElement;
import org.openhealthtools.mdht.mdmi.model.enums.SemanticElementType;
import org.openhealthtools.mdht.mdmi.util.ICloneable;

/**
 * Internal class, defines a conversion for a transfer request, within a unit of
 * work.
 *
 * @author goancea
 */
public class Conversion {
	MdmiUow m_owner;
	MdmiTransferInfo m_transferInfo;
	ArrayList<ConversionInfo> m_conversionInfos = new ArrayList<ConversionInfo>();

	/**
	 * Construct an instance from the given unit of work.
	 *
	 * @param owner
	 *           The UoW owner.
	 */
	Conversion( MdmiUow owner ) {
		if( owner == null || owner.transferInfo == null || owner.transferInfo.targetElements == null )
			throw new IllegalArgumentException("Null or invalid argument!");
		m_owner = owner;
		m_transferInfo = m_owner.transferInfo;

		ArrayList<String> elements = m_transferInfo.targetElements;
		if( elements.size() <= 0 ) {
			System.out.println("WARNING: no transfer targets specified, nothing to do!");
			return;
		}
		if( m_transferInfo.useDictionary ) {
			initFromDictionaryElements(elements);
		}
		else {
			initFromTargetElements(elements);
		}
	}

	/**
	 * Construct a ConversionInfo for each element in the
	 * m_transferInfo.targetElements. The names in the
	 * m_transferInfo.targetElements are target BusinessElementRefecence names.
	 *
	 * @param elements
	 *           The elements to use, a list of strings.
	 */
	private void initFromDictionaryElements( ArrayList<String> elements ) {
		MessageModel trgModel = m_transferInfo.targetModel.getModel();
		MessageModel srcModel = m_transferInfo.sourceModel.getModel();

		MdmiDomainDictionaryReference trgDict = trgModel.getGroup().getDomainDictionary();
		MdmiDomainDictionaryReference srcDict = srcModel.getGroup().getDomainDictionary();

		for( int i = 0; i < elements.size(); i++ ) {
			String name = elements.get(i);
			MdmiBusinessElementReference trgBER = trgDict.getBusinessElement(name);
			if( trgBER == null )
				throw new MdmiException("Conversion: invalid target BER " + name);

			String uid = trgBER.getUniqueIdentifier();
			MdmiBusinessElementReference srcBER = srcDict.getBusinessElementByUniqueID(uid);
			if( srcBER == null )
				throw new MdmiException("Conversion: invalid source BER for unique ID " + uid);

			ArrayList<SemanticElement> trgSes = getTargetSESforBER(trgModel, trgBER);
			if( trgSes.size() <= 0 )
				throw new MdmiException("Conversion: invalid mapping, missing target SEs, target BER is " + trgBER.getName());

			ArrayList<ConversionInfo> cis = new ArrayList<ConversionInfo>();
			for( int j = 0; j < trgSes.size(); j++ ) {
				SemanticElement target = trgSes.get(j);
				ConversionInfo ci = new ConversionInfo(target, trgBER, srcBER);
				ArrayList<SemanticElement> srcSes = getSourceSESforBER(srcModel, srcBER);
				if( srcSes.size() <= 0 ) {
					throw new MdmiException("Conversion: invalid mapping, missing source SEs, source BER is " + ci.srcBER.getName());
				}
				for( int k = 0; k < srcSes.size(); k++ ) {
					SemanticElement ses = srcSes.get(k);
					ci.source.add(ses);
				}
				cis.add(ci);
			}
			m_conversionInfos.addAll(cis);
		}
	}

	/**
	 * Construct a ConversionInfo for each element in the
	 * m_transferInfo.targetElements. The names in the
	 * m_transferInfo.targetElements are target SemanticElement names.
	 *
	 * @param elements
	 *           The elements to use, a list of strings.
	 */
	private void initFromTargetElements( ArrayList<String> elements ) {
		MessageModel trgModel = m_transferInfo.targetModel.getModel();
		MessageModel srcModel = m_transferInfo.sourceModel.getModel();

		MdmiDomainDictionaryReference srcDict = srcModel.getGroup().getDomainDictionary();

		for( int i = 0; i < elements.size(); i++ ) {
			String name = elements.get(i);
			SemanticElement target = trgModel.getElementSet().getSemanticElement(name);
			if( target == null )
				throw new MdmiException("Conversion: invalid target SE " + name);

			ArrayList<MdmiBusinessElementReference> trgBers = getTargetBERSforSE(trgModel, target);
			// JGK: I believe it is OK to have semantic elements that are not mapped to business elements
			if( trgBers.size() <= 0 )
				continue;

			ArrayList<ConversionInfo> cis = new ArrayList<ConversionInfo>();
			for( int j = 0; j < trgBers.size(); j++ ) {
				MdmiBusinessElementReference trgBER = trgBers.get(j);
				String uid = trgBER.getUniqueIdentifier();
				MdmiBusinessElementReference srcBER = srcDict.getBusinessElementByUniqueID(uid);
				if( srcBER == null )
					throw new MdmiException("Conversion: invalid source BER element for unique ID " + uid);
				ConversionInfo ci = new ConversionInfo(target, trgBER, srcBER);
				ArrayList<SemanticElement> srcSes = getSourceSESforBER(srcModel, srcBER);
				if( srcSes.size() <= 0 )
					throw new MdmiException("Conversion: no source SEs found, source BER is " + ci.srcBER.getName());
				for( int k = 0; k < srcSes.size(); k++ ) {
					SemanticElement ses = srcSes.get(k);
					ci.source.add(ses);
				}
				cis.add(ci);
			}
			m_conversionInfos.addAll(cis);
		}
	}

	/**
	 * Execute all the conversions for this unit of work.
	 */
	void execute() {
		try {
			ConversionImpl impl = ConversionImpl.Instance;
			impl.start(m_owner.owner.getOwner().getConfig().getLogInfo().logLevel.intValue() <= Level.FINE.intValue());

			ArrayList<ConversionInfo> cis = getTopLevelCis();
			for( int i = 0; i < cis.size(); i++ ) {
				ConversionInfo ci = cis.get(i);
				for( int j = 0; j < ci.source.size(); j++ ) {
					SemanticElement source = ci.source.get(j);
					ArrayList<IElementValue> srcs = m_owner.srcSemanticModel.getElementValuesByType(source);
					ArrayList<IElementValue> trgs = m_owner.trgSemanticModel.getElementValuesByType(ci.target);
					if( ci.target.isMultipleInstances() ) {
						for( int k = 0; k < srcs.size(); k++ ) {
							XElementValue src = (XElementValue) srcs.get(k);
							XElementValue trg = null;
							if( k < trgs.size() ) {
								trg = (XElementValue) trgs.get(k);
							}
							else {
								trg = new XElementValue(ci.target, m_owner.trgSemanticModel);
								if( trgs.size() > 0 ) {
									IElementValue parentContainer = trgs.get(0).getParent();
									generateTargetValue(trg, parentContainer);
								}
							}
							impl.convert(src, ci, trg);
							execute(src, ci, trg);
						}
					}
					else {
						XElementValue trg = null;
						if( trgs.size() <= 0 ) {
							trg = new XElementValue(ci.target, m_owner.trgSemanticModel);
						}
						else {
							trg = (XElementValue) trgs.get(0);
						}
						for( int k = 0; k < srcs.size(); k++ ) {
							XElementValue src = (XElementValue) srcs.get(k);
							impl.convert(src, ci, trg);
							execute(src, ci, trg);
						}
					}
				}
			}
			impl.end();
		}
		catch( Exception e ) {
			e.printStackTrace();
			throw new MdmiException(e.getMessage());
		}
	}
	
	HashMap <String,ArrayList<IElementValue>> globalSources = new HashMap <String,ArrayList<IElementValue>> ();

	private void execute( XElementValue srcOwner, ConversionInfo parent, XElementValue trgOwner ) {
		try {
			ConversionImpl impl = ConversionImpl.Instance;

			impl.logging = m_owner.owner.getOwner().getConfig().getLogInfo().logLevel.intValue() <= Level.FINE.intValue();
		 
			ArrayList<ConversionInfo> cis = getCisForSE(parent.target);
			for( int i = 0; i < cis.size(); i++ ) {
				ConversionInfo ci = cis.get(i);
				for( int j = 0; j < ci.source.size(); j++ ) {
					SemanticElement source = ci.source.get(j);
					SemanticElement seParent = source.getParent();
					SemanticElement seParentOwner = srcOwner.getSemanticElement();
					ArrayList<IElementValue> srcs = null;
					// if the owner SE is not the same as the source parent SE, use all elements, otherwise get only children
					if( seParent != seParentOwner ) {			
						if (!globalSources.containsKey(source.getName())) {
							globalSources.put(source.getName(), m_owner.srcSemanticModel.getElementValuesByType(source));
						}
						srcs = globalSources.get(source.getName());
					}
					else {
						srcs = m_owner.srcSemanticModel.getDirectChildValuesByType(source, srcOwner);
					}

					ArrayList<IElementValue> trgs = m_owner.trgSemanticModel.getDirectChildValuesByType(ci.target, trgOwner);

					if( ci.target.isMultipleInstances() ) {
						for( int k = 0; k < srcs.size(); k++ ) {
							XElementValue src = (XElementValue) srcs.get(k);
							XElementValue trg = null;
							if( k < trgs.size() ) {
								trg = (XElementValue) trgs.get(k);
							}
							else {
								trg = new XElementValue(ci.target, m_owner.trgSemanticModel);
								trgOwner.addChild(trg);
							}
							impl.convert(src, ci, trg);
							execute(src, ci, trg);
						}
					}
					else {
						XElementValue trg = null;
						if( trgs.size() <= 0 ) {
							trg = new XElementValue(ci.target, m_owner.trgSemanticModel);
							trgOwner.addChild(trg);
						}
						else {
							trg = (XElementValue) trgs.get(0);
						}
						for( int k = 0; k < srcs.size(); k++ ) {
							XElementValue src = (XElementValue) srcs.get(k);
							impl.convert(src, ci, trg);
							execute(src, ci, trg);
						}
					}
				}
			}

		}
		catch( Exception e ) {
			e.printStackTrace();
			throw new MdmiException(e.getMessage());
		}

	}

	// generates target ElementValue tree
	private void generateTargetValue( IElementValue targetValue, IElementValue parentContainer ) {
		if( parentContainer == null )
			return;
		if( !targetValue.getSemanticElement().getSyntaxNode().isSingle() ) {
			parentContainer.addChild(targetValue);
		}
		else {
			IElementValue parentTargetValue = new XElementValue(targetValue.getSemanticElement().getParent(), m_owner.trgSemanticModel);
			((XValue) parentTargetValue.getXValue()).intializeStructs();
			parentTargetValue.addChild(targetValue);

			generateRequiredSEValues(parentTargetValue.getSemanticElement(), parentTargetValue);
			generateTargetValue(parentTargetValue, parentContainer.getParent());
		}
	}

	// generates target element values for required SNs
	private void generateRequiredSEValues( SemanticElement sourceSE, IElementValue sourceEV ) {
		for( SemanticElement childSE : sourceSE.getChildren() ) {
			if( !hasChildElementValue(sourceEV, childSE) && childSE.getSyntaxNode().isRequired() ) {
				XElementValue childTargetValue = new XElementValue(childSE, m_owner.trgSemanticModel);
				childTargetValue.getXValue().intializeStructs();
				sourceEV.addChild(childTargetValue);
				childTargetValue.setParent(sourceEV);
			}

			if( childSE.getChildren().size() > 0 )
				generateRequiredSEValues(childSE, getChildElementValue(sourceEV, childSE));
		}
	}

	private boolean hasChildElementValue( IElementValue elementValue, SemanticElement childSemanticElement ) {
		return getChildElementValue(elementValue, childSemanticElement) != null;
	}

	private IElementValue getChildElementValue( IElementValue elementValue, SemanticElement childSemanticElement ) {
		for( IElementValue childEV : elementValue.getChildren() ) {
			if( childEV.getSemanticElement() == childSemanticElement ) {
				return childEV;
			}
		}
		return null;
	}

	// get the CIs for which the target SE's are top level, i.e. have no parent
	// or have a LOCAL parent
	private ArrayList<ConversionInfo> getTopLevelCis() {
		ArrayList<ConversionInfo> cis = new ArrayList<ConversionInfo>();
		for( int i = 0; i < m_conversionInfos.size(); i++ ) {
			ConversionInfo ci = m_conversionInfos.get(i);
			SemanticElement parent = ci.target.getParent();
			if( parent == null || parent.getSemanticElementType() == SemanticElementType.LOCAL ) {
				cis.add(ci);
			}
		}
		return cis;
	}

	// get the CIs for which the target SE is a child of the given SE
	private ArrayList<ConversionInfo> getCisForSE( SemanticElement parent ) {
		ArrayList<ConversionInfo> cis = new ArrayList<ConversionInfo>();
		for( int i = 0; i < m_conversionInfos.size(); i++ ) {
			ConversionInfo ci = m_conversionInfos.get(i);
			if( parent.hasChild(ci.target) )
				cis.add(ci);
		}
		return cis;
	}

	// given a BER get a list of all SEs that have a ToBusinessElement rule for
	// it
	// and if none get a name match
	private ArrayList<SemanticElement> getSourceSESforBER( MessageModel model, MdmiBusinessElementReference ber ) {
		ArrayList<SemanticElement> a = new ArrayList<SemanticElement>();
		Collection<SemanticElement> srcSEs = model.getElementSet().getSemanticElements();
		for( Iterator<SemanticElement> itSE = srcSEs.iterator(); itSE.hasNext(); ) {
			SemanticElement se = itSE.next();
			Collection<ToBusinessElement> toBEs = se.getFromMdmi();
			for( Iterator<ToBusinessElement> itBE = toBEs.iterator(); itBE.hasNext(); ) {
				ToBusinessElement tbe = itBE.next();
				if( tbe != null && tbe.getBusinessElement() == ber ) {
					a.add(se);
					break;
				}
			}
		}
		if( a.size() <= 0 ) {
			SemanticElement se = model.getElementSet().getSemanticElement(ber.getName());
			if( se != null )
				a.add(se);
		}
		return a;
	}

	// given a BER get a list of all SEs that have a ToMessageElement rule for it and if none get a name match
	private ArrayList<SemanticElement> getTargetSESforBER( MessageModel model, MdmiBusinessElementReference ber ) {
		ArrayList<SemanticElement> a = new ArrayList<SemanticElement>();
		Collection<SemanticElement> srcSEs = model.getElementSet().getSemanticElements();
		for( Iterator<SemanticElement> itSE = srcSEs.iterator(); itSE.hasNext(); ) {
			SemanticElement se = itSE.next();
			Collection<ToMessageElement> toMEs = se.getToMdmi();
			for( Iterator<ToMessageElement> itME = toMEs.iterator(); itME.hasNext(); ) {
				ToMessageElement tme = itME.next();
				if( tme != null && tme.getBusinessElement() == ber ) {
					a.add(se);
					break;
				}
			}
		}
		if( a.size() <= 0 ) {
			SemanticElement se = model.getElementSet().getSemanticElement(ber.getName());
			if( se != null )
				a.add(se);
		}
		return a;
	}

	@SuppressWarnings( "unused" )
	private ArrayList<MdmiBusinessElementReference> getSourceBERSforSE( MessageModel model, SemanticElement se ) {
		ArrayList<MdmiBusinessElementReference> a = new ArrayList<MdmiBusinessElementReference>();
		Collection<ToBusinessElement> toBEs = se.getFromMdmi();
		for( Iterator<ToBusinessElement> itBE = toBEs.iterator(); itBE.hasNext(); ) {
			ToBusinessElement tbe = itBE.next();
			if( tbe != null ) {
				a.add(tbe.getBusinessElement());
				break;
			}
		}
		if( a.size() <= 0 ) {
			MdmiBusinessElementReference ber = model.getGroup().getDomainDictionary().getBusinessElement(se.getName());
			if( ber != null ) {
				a.add(ber);
			}
		}
		return a;
	}

	private ArrayList<MdmiBusinessElementReference> getTargetBERSforSE( MessageModel model, SemanticElement se ) {
		ArrayList<MdmiBusinessElementReference> a = new ArrayList<MdmiBusinessElementReference>();
		Collection<ToMessageElement> toMEs = se.getToMdmi();
		for( Iterator<ToMessageElement> itME = toMEs.iterator(); itME.hasNext(); ) {
			ToMessageElement tme = itME.next();
			if( tme != null ) {
				a.add(tme.getBusinessElement());
				// JGK: This line must be a bug because one can map multiple BEs to an SE.
				// break;
			}
		}
		if( a.size() <= 0 ) {
			MdmiBusinessElementReference ber = model.getGroup().getDomainDictionary().getBusinessElement(se.getName());
			if( ber != null ) {
				a.add(ber);
			}
		}
		return a;
	}

	static ToBusinessElement getToBE( SemanticElement src, MdmiBusinessElementReference ber ) {
		Collection<ToBusinessElement> toBEs = src.getFromMdmi();
		for( Iterator<ToBusinessElement> it = toBEs.iterator(); it.hasNext(); ) {
			ToBusinessElement t = it.next();
			if( t.getBusinessElement() == ber ) {
				return t;
			}
		}
		return null;
	}

	static ToMessageElement getToSE( SemanticElement trg, MdmiBusinessElementReference ber ) {
		Collection<ToMessageElement> toSEs = trg.getToMdmi();
		for( Iterator<ToMessageElement> it = toSEs.iterator(); it.hasNext(); ) {
			ToMessageElement t = it.next();
			if( t.getBusinessElement() == ber ) {
				return t;
			}
		}
		return null;
	}

	/**
	 * Each instance of this class wraps one conversion, from one source SE to
	 * one or more target SEs, through a source and target BER.
	 *
	 * @author goancea
	 */
	static public class ConversionInfo implements ICloneable<ConversionInfo> {
		public SemanticElement              target; // target message element
		public MdmiBusinessElementReference trgBER; // target business element reference
		public MdmiBusinessElementReference srcBER; // source business element reference (same unique ID as target)
		ArrayList<SemanticElement>          source; // source message elements

		public ConversionInfo( SemanticElement target, MdmiBusinessElementReference trgBER, MdmiBusinessElementReference srcBER ) {
			this.target = target;
			this.trgBER = trgBER;
			this.srcBER = srcBER;
			this.source = new ArrayList<SemanticElement>();
		}

		@SuppressWarnings( "unchecked" )
		ConversionInfo( ConversionInfo src ) {
			target = src.target;
			trgBER = src.trgBER;
			srcBER = src.srcBER;
			source = (ArrayList<SemanticElement>) src.source.clone();
		}

		@Override
		public ConversionInfo clone() {
			return new ConversionInfo(this);
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			if( source.size() == 1 ) {
				sb.append(source.get(0).getName() + " -> ");
			}
			else {
				sb.append("[ ");
				for( int i = 0; i < source.size(); i++ ) {
					if( i > 0 ) {
						sb.append(", ");
					}
					sb.append(source.get(i).getName());
				}
				sb.append(" ] -> ");
			}
			sb.append((trgBER == null ? "null" : trgBER.getName()) + " -> ");
			sb.append((target == null ? "null" : target.getName()));
			return sb.toString();
		}
	} // Conversion$ConversionInfo
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for( int i = 0; i < m_conversionInfos.size(); i++ ) {
	      ConversionInfo ci = m_conversionInfos.get(i);
	      sb.append(ci.toString()).append("\r\n");
      }
		return sb.toString();
	}
} // Conversion
