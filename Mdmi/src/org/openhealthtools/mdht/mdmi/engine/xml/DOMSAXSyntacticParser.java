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
package org.openhealthtools.mdht.mdmi.engine.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.openhealthtools.mdht.mdmi.ISyntacticParser;
import org.openhealthtools.mdht.mdmi.ISyntaxNode;
import org.openhealthtools.mdht.mdmi.MdmiException;
import org.openhealthtools.mdht.mdmi.MdmiMessage;
import org.openhealthtools.mdht.mdmi.engine.YBag;
import org.openhealthtools.mdht.mdmi.engine.YChoice;
import org.openhealthtools.mdht.mdmi.engine.YLeaf;
import org.openhealthtools.mdht.mdmi.engine.YNode;
import org.openhealthtools.mdht.mdmi.engine.xml.XPathParser.AbbreviatedStepContext;
import org.openhealthtools.mdht.mdmi.engine.xml.XPathParser.AxisSpecifierContext;
import org.openhealthtools.mdht.mdmi.engine.xml.XPathParser.NodeTestContext;
import org.openhealthtools.mdht.mdmi.engine.xml.XPathParser.PredicateContext;
import org.openhealthtools.mdht.mdmi.model.Bag;
import org.openhealthtools.mdht.mdmi.model.Choice;
import org.openhealthtools.mdht.mdmi.model.LeafSyntaxTranslator;
import org.openhealthtools.mdht.mdmi.model.MessageModel;
import org.openhealthtools.mdht.mdmi.model.MessageSyntaxModel;
import org.openhealthtools.mdht.mdmi.model.Node;
import org.openhealthtools.mdht.mdmi.util.XmlParser;
import org.openhealthtools.mdht.mdmi.util.XmlUtil;
import org.openhealthtools.mdht.mdmi.util.XmlWriter;
import org.openhealthtools.mdht.mdmi.util.XslUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class DOMSAXSyntacticParser implements ISyntacticParser {

	private static class XPathExtractor extends XPathBaseListener {

		private static final String DOTDOT = "..";
		boolean inPredicate = false;
		boolean isAttribute = false;
		boolean isContainer = false;

		org.w3c.dom.Node node;
		Document document;

		public XPathExtractor( Element element, boolean isContainer ) {
	     this.node = element;
	     this.isContainer = isContainer;
	     document = this.node.getOwnerDocument();
      }

	

		@Override
      public void exitAbbreviatedStep( AbbreviatedStepContext ctx ) {
			// If xpath has .. move node upto parent
			if (DOTDOT.equals(ctx.getText())) {
				node = node.getParentNode();	
			}
	      super.exitAbbreviatedStep(ctx);
      }

	

		@Override
		public void exitNodeTest( NodeTestContext ctx ) {
			if( !inPredicate ) {
				if( isAttribute ) {
//					sb.append(ctx.getText());
					Attr attribute = document.createAttribute(ctx.getText());
					((Element) node).setAttributeNode(attribute);
					node = attribute;
				}
				else {
					Element childElement = null;
					if (isContainer) {
						childElement = document.createElement(ctx.getText());
						node.appendChild (childElement );
					} else {
						Element currentElement = (Element) node;
						NodeList nodeList = currentElement.getElementsByTagName(ctx.getText());
						
						
						if( nodeList.getLength() == 1 ) {
							childElement = (Element) nodeList.item(0);
						}
						if( childElement == null ) {
							childElement = document.createElement(ctx.getText());
							node.appendChild (childElement );
						}
					}
	
					node = childElement;
				}
				isAttribute = false;
			}
			super.exitNodeTest(ctx);
		}

		@Override
		public void enterPredicate( PredicateContext ctx ) {
			inPredicate = true;
			super.enterPredicate(ctx);
		}

		@Override
		public void exitPredicate( PredicateContext ctx ) {
			inPredicate = false;
			super.exitPredicate(ctx);
		}

		@Override
		public void exitAxisSpecifier( AxisSpecifierContext ctx ) {
			if( !inPredicate && "@".equals(ctx.getText()) ) {
				isAttribute = true;
			}
			super.exitAxisSpecifier(ctx);
		}

	}
	
 
	
	 
	private org.w3c.dom.Node createElement(Element elemement, String xPath, boolean container ) {
		 XPathLexer lexer = new XPathLexer(new ANTLRInputStream(xPath));
		 
		 CommonTokenStream tokens = new CommonTokenStream(lexer);
		 
		 XPathParser parser = new XPathParser(tokens);
	  	 
		 ParseTreeWalker walker = new ParseTreeWalker();
	
	
		XPathExtractor extractor = new XPathExtractor(elemement,container);
		walker.walk(extractor, parser.main());
		return extractor.node;
	}

	protected NamespaceContext context;

	/*
	 * Extend DOM/SAX Tree Walker which tracks the current dom node so we can
	 * perform XPATH queries
	 */
	private class MDMITreeWalker extends org.apache.xml.utils.TreeWalker {

		public MDMITreeWalker( ContentHandler contentHandler ) {
			super(contentHandler);
		}

		@Override
		protected void startNode( org.w3c.dom.Node node ) throws SAXException {
			domNodes.push(node);
			super.startNode(node);
		}

		@Override
		protected void endNode( org.w3c.dom.Node node ) throws SAXException {
			domNodes.pop();
			super.endNode(node);
		}

	}

	private Stack<org.w3c.dom.Node> domNodes = new Stack<org.w3c.dom.Node>();

	private void saxParse( final YBag yroot, final byte[] data ) throws ParserConfigurationException, SAXException {
		
		
		
	

         DefaultHandler2 mdmiHandler = new DefaultHandler2() {

			private Stack<EndTagProcessor> endTags;

			private Stack<Bag> syntaxNodes;

			private Stack<YBag> yBags;

			boolean isParsing = false;

			StringBuilder builder = null;

			YLeaf currentYLeaf = null;

			@Override
         public void startCDATA() throws SAXException {
				if( isParsing && builder != null ) {
					builder.append("<![CDATA[");
				} else {
					isParsing = true;
					builder = new StringBuilder();
					builder.append("<![CDATA[");
				}
         }

			@Override
         public void endCDATA() throws SAXException {
				if( isParsing && builder != null ) {
					builder.append("]]>");
				}
         }

			@Override
			public void startDocument() throws SAXException {
				endTags = new Stack<EndTagProcessor>();
				syntaxNodes = new Stack<Bag>();
				syntaxNodes.push((Bag) yroot.getNode());
				yBags = new Stack<YBag>();
				yBags.push(yroot);
			}

			@Override
			public void characters( char[] ch, int start, int length ) throws SAXException {
				if( isParsing && builder != null ) {
					builder.append(new String(ch, start, length));
				} else {
				}
			}

			/**
			 * Construct the XPath expression
			 */
			private String getCurrentXPath() {
				String str = "//";
				boolean first = true;
				for( Node node : syntaxNodes ) {
					if( first )
						str = node.getLocation();
					else
						str = str + "/" + node.getLocation();
					first = false;
				}
				return str;
			}

			/**
			 * Construct the XPath expression
			 */
			private String getCurrentRelativePath( Bag bag ) {
				Stack<String> currentPath = getRelativePath(bag);

				if( currentPath != null && !currentPath.isEmpty()) {

					String str = "";

					boolean first = true;
					for( String node : currentPath ) {

						if( first ) {
							str = node;
							first = false;
						}
						else {
							str = str + "/" + node;
						}

					}

					return str = str + "/";
				}
				else {
					return "";
				}
			}

			HashMap<Bag, Stack<String>> relativePaths = new HashMap<Bag, Stack<String>>();

			void pushXPath( Bag bag, String path ) {
				if( !relativePaths.containsKey(bag) ) {
					relativePaths.put(bag, new Stack<String>());
				}
				relativePaths.get(bag).push(path);
			}

			void popXPath( Bag bag ) {
				if( relativePaths.containsKey(bag) ) {
					relativePaths.get(bag).pop();
				}
			}

			Stack<String> getRelativePath( Bag bag ) {
				if( relativePaths.containsKey(bag) ) {
					return relativePaths.get(bag);
				}
				else {
					return null;
				}
			}

			private boolean isMatch( String currentRelativeXPath, String nodeXPathLocation ) {

			
				if( currentRelativeXPath.equals(nodeXPathLocation) ) {
					return true;
				}

				if( nodeXPathLocation.equals("@" + currentRelativeXPath) ) {
					return true;
				}

				String[] locationSegments = nodeXPathLocation.split("/@");

				if( currentRelativeXPath.equals(locationSegments[0]) ) {
					return true;
				}

				String[] path = nodeXPathLocation.split("\\[");

				if( currentRelativeXPath.equals(path[0]) ) {
					return true;
				}
				return false;

			}

			BagEndTagProcessor bagEndTagProcessor = new BagEndTagProcessor();
			LeafEndTagProcessor leafEndTagProcessor = new LeafEndTagProcessor();
			NotFoundEndTagProcessor notFoundEndTagProcessor = new NotFoundEndTagProcessor();

			class NodePredicate implements Predicate<Node> {

				public NodePredicate( Bag bag, final String qName ) {
					sb.append(getCurrentRelativePath(bag));
					sb.append(qName);
				}

				StringBuilder sb = new StringBuilder();

				@Override
				public boolean apply( Node node ) {
					return isMatch(sb.toString(), node.getLocation());
				}

				void pushNode( Node node ) {
					sb.insert(0, node.getLocation() + "/");
				}
			}

			private Node lookForMatch( final String qName ) {

				NodePredicate matches = null;
				
				for( Bag currentBag : Lists.reverse(syntaxNodes) ) {

					if (matches==null) {
						matches = new NodePredicate(currentBag, qName);
					}
					Iterable<Node> matchingNodes = Iterables.filter(currentBag.getNodes(), matches);
						Iterator<Node> iterator = matchingNodes.iterator();
						XPath xPath = XPathFactory.newInstance().newXPath();
						/*
						 * Loop over the nodes in the syntax model 
						 * If the node has a xpath  - use that xpath evaluation to determine the correct node
						 * If the node does not have a xpath - assume a match to the first node
						 */
						while( iterator.hasNext() ) {
							Node node = iterator.next();
							try {
								String path = node.getLocation();
								int start = path.indexOf("[");
								int end = node.getLocation().lastIndexOf("]");
								if( start > -1 && end > -1 ) {
									NodeList nodes = (NodeList) xPath.evaluate(path.substring(start + 1, end), domNodes.peek(), XPathConstants.NODESET);
									if( nodes.getLength() > 0 ) {
										return node;
									}
								} else {
									return node;
								}
							}
							catch( XPathExpressionException ex ) {
								ex.printStackTrace();
							}

						}
					matches.pushNode(currentBag);
				}
				return null;
			}

			private String getAttributeValue( Attributes attributes, String attribute ) {

				for( int i = 0; i < attributes.getLength(); i++ ) {
					if( attribute.equals(attributes.getQName(i)) ) {
						return attributes.getValue(i);
					}
				}

				return null;

			}

			private YBag getYBagForParentNode( Node matchingSyntaxNode ) {
				int index = 0;
				for( Node syntaxNode : syntaxNodes ) {
					if( matchingSyntaxNode.getParentNode().equals(syntaxNode) ) {
						break;
					}
					index++;
				}
				return yBags.get(index);
			}

			/*
			 * Processing Steps Syntax node should always be a bag (or a choice) If
			 * current syntax node is a bag and matching node is a bag and semantic
			 * element is a container - push corresponding node If current syntax
			 * node is a bag and matching node is a LST, create YNode and parse
			 */

			@Override
			public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException {

				// Skip the root 
				if( yBags.peek().getParent() == null && qName.equals(yBags.peek().getNode().getLocation()) ) {
					return;
				}

				Node matchingSyntaxNode = lookForMatch(qName);

				if( matchingSyntaxNode != null ) {
					YBag parentYBag = getYBagForParentNode(matchingSyntaxNode);

					if( matchingSyntaxNode instanceof Bag ) {
						YBag y = new YBag((Bag) matchingSyntaxNode, parentYBag);
						parentYBag.addYNode(y);
						syntaxNodes.push((Bag) matchingSyntaxNode);
						endTags.push(bagEndTagProcessor);
						for( Node n : ((Bag) matchingSyntaxNode).getNodes() ) {
							if( n.getLocation().startsWith("@") ) {
								String attributeValue = getAttributeValue(attributes, n.getLocation().substring(1));
								if( n instanceof LeafSyntaxTranslator && attributeValue != null ) {
									YLeaf aLeaf = new YLeaf((LeafSyntaxTranslator) n, y);
									aLeaf.setValue(attributeValue);
									y.addYNode(aLeaf);
								}

							}

						}
						yBags.push(y);
					}
					if( matchingSyntaxNode instanceof LeafSyntaxTranslator ) {
						if( matchingSyntaxNode.getLocation().contains("@") ) {
							String[] locationSegments = matchingSyntaxNode.getLocation().split("/@");
							if (locationSegments.length > 1) {
							String attributeValue = getAttributeValue(attributes, locationSegments[1]);
							if( attributeValue != null ) {
								YLeaf aLeaf = new YLeaf((LeafSyntaxTranslator) matchingSyntaxNode, parentYBag);
								aLeaf.setValue(attributeValue);
								parentYBag.addYNode(aLeaf);
							}
							}
							
					
								pushXPath(syntaxNodes.peek(), qName);
								endTags.push(notFoundEndTagProcessor);
						
						
						}
						else {
							currentYLeaf = new YLeaf((LeafSyntaxTranslator) matchingSyntaxNode, parentYBag);
							parentYBag.addYNode(currentYLeaf);
							
					
							isParsing = true;
							if (builder == null) {
								builder = new StringBuilder();		
							}
						
							endTags.push(leafEndTagProcessor);
						}
					
					}

				}
				else {
					pushXPath(syntaxNodes.peek(), qName);
					// TODO Log this versus System.out
					System.out.println("Not processing " + getCurrentXPath() + "/" + getCurrentRelativePath(syntaxNodes.peek()));
					endTags.push(notFoundEndTagProcessor);
				}

			}

			/**
			 * EndTagProcessor is function object definition to support end tag functionality
			 * 
			 * 
			 *
			 */
			abstract class EndTagProcessor {
				abstract void process();
			}

			class BagEndTagProcessor extends EndTagProcessor {

				@Override
				void process() {
					syntaxNodes.pop();
					yBags.pop();

				}

			}

			class NotFoundEndTagProcessor extends EndTagProcessor {

				@Override
				void process() {
					popXPath(syntaxNodes.peek());
				}

			}

			class LeafEndTagProcessor extends EndTagProcessor {

				@Override
				void process() {
					if( isParsing && currentYLeaf != null ) {
						currentYLeaf.setValue(builder.toString());
						builder = null;
						isParsing = false;

					}

				}

			}

			@Override
			public void endElement( String uri, String localName, String qName ) throws SAXException {
				if( !endTags.isEmpty() ) {
					EndTagProcessor etp = endTags.pop();
					etp.process();
				}

			}

		};

		DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		org.w3c.dom.Document doc;
		try {
			doc = b.parse(new ByteArrayInputStream(data));
			org.apache.xml.utils.TreeWalker treeWalker = new MDMITreeWalker(mdmiHandler);
			treeWalker.traverse(doc.getDocumentElement());
		}
		catch( IOException ex ) {
			ex.printStackTrace();
		}

	}

	public String getXPath( org.w3c.dom.Node node ) {
		return getXPath(node, "");
	}

	public String getXPath( org.w3c.dom.Node node, String xpath ) {
		if( node == null ) {
			return "";
		}
		String elementName = "";
		if( node instanceof Element ) {
			elementName = ((Element) node).getLocalName();
		}
		org.w3c.dom.Node parent = node.getParentNode();
		if( parent == null ) {
			return xpath;
		}
		return getXPath(parent, "/" + elementName + xpath);
	}

	@Override
	public ISyntaxNode parse( MessageModel mdl, MdmiMessage msg ) {
		if( mdl == null || msg == null )
			throw new IllegalArgumentException("Null argument!");
		byte[] data = msg.getData();
		if( data == null )
			return null; // <---- NOTE message can be empty

		YNode yroot = null;
		try {

			MessageSyntaxModel syn = mdl.getSyntaxModel();
			Node node = syn.getRoot();
			yroot = new YBag((Bag) node, null);
			long l = System.currentTimeMillis();
			this.saxParse((YBag) yroot, data);
			System.out.println("------ Parse Inbound Message took " + (System.currentTimeMillis() - l) / 1000 + " Seconds ----");
		}
		catch( MdmiException ex ) {
			throw ex;
		}
		catch( Exception ex ) {
			throw new MdmiException(ex, "Syntax.parse(): unexpected exception");
		}

		return yroot;
	}

	@Override
	public void serialize( MessageModel mdl, MdmiMessage msg, ISyntaxNode yroot ) {
		if( mdl == null || msg == null || yroot == null )
			throw new IllegalArgumentException("Null argument!");
		try {
			MessageSyntaxModel syn = mdl.getSyntaxModel();
			Node node = syn.getRoot();
			if( node != yroot.getNode() )
				throw new MdmiException("Invalid serialization attempt, expected node {0} forund node {1}", node.getName(), yroot.getNode().getName());
			String nodeName = location(node); // for the root node it is its name
			byte[] data = msg.getData();
			XmlParser p = new XmlParser();
			Document doc = null;
			Element root = null;
			if( data == null ) {
				doc = p.newDocument();
				root = doc.createElement(nodeName);
				doc.appendChild(root);
			}
			else {
				doc = p.parse(new ByteArrayInputStream(data));
				root = doc.getDocumentElement();
				String rootName = root.getNodeName();
				if( !nodeName.equals(rootName) )
					throw new MdmiException("Root node mismatch, found {0}, expected {1}", rootName, nodeName);
			}

			if( node instanceof Bag ) {
				serializeBag((YBag) yroot, root);
			}
			else if( node instanceof Choice ) {
				serializeChoice((YChoice) yroot, root);
			}
			else {
				serializeLeaf((YLeaf) yroot, root);
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream(0xFFFF);
			XmlWriter w = new XmlWriter(baos);
			w.write(doc);
			w = null;
			msg.setData(baos.toByteArray());
		}
		catch( MdmiException ex ) {
			throw ex;
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			throw new MdmiException(ex, "Syntax.serialize(): unexpected exception");
		}
	}

	
	
	/**
	 * Serialize a YBag to the given root element.
	 * 
	 * @param yroot
	 *           The bag node to serialize.
	 * @param root
	 *           The node to store it into.
	 */
	int indent=0;
	private void serializeBag( YBag yroot, Element root ) {	
		serialize(yroot,root);
	}
	
	private void serialize( YBag bag, Element element ) {
		// TODO - This loop maintains the physical order of serialization of the syntax mode
		// Replace with more effective loop
		for (Node node : bag.getBag().getNodes()){
			for( YNode ynode : bag.getYNodes() ) {
				if( ynode.getNode().equals(node) ) {
					if( ynode.isBag() ) {
						boolean isContainer = false;
						if (node.getSemanticElement() != null && node.getSemanticElement().getDatatype() != null ) {
							if ("Container".equals(node.getSemanticElement().getDatatype().getName())) {
								 isContainer = true;
							}
						}
							Element childElement = (Element) createElement(element, ynode.getNode().getLocation(), isContainer);					 
							serialize((YBag) ynode, childElement);
					}
					else if( ynode.isLeaf() ) {

						YLeaf yleaf = (YLeaf) ynode;
						String value = yleaf.getValue();
					
					/*
					 * TODO FIX!!! Passing in a default of true - ynode.getNode().getMaxOccurs() != 1 is not working
					 * All data type attributes are returning 0..1 regardless of the model
					 * The createElement handles attributes but could have other unforseen issues
					 * 
					 */
						
						/*
						 *  treatAsContainer - If the leaf node has a "/" in its definition, combine all "/" of similiar types
						 *  we set treat as container to false - this will cause the createElement process to search for existing elements before creating new ones
						 *  SWM
						 *  
						 */
						boolean treatAsContainer = true;
						
						if (ynode.getNode().getLocation().contains("/")) {
							treatAsContainer = false;
						}
						
						
						org.w3c.dom.Node xmlNode = createElement(element, ynode.getNode().getLocation(), treatAsContainer);
						
						if( xmlNode.getNodeType() == org.w3c.dom.Node.ATTRIBUTE_NODE ) {
							Attr o = (Attr) xmlNode;
							o.setTextContent(value);
						}
						else if( xmlNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE ) {
							Element o = (Element) xmlNode;
							if( value != null ) {
								XmlUtil.setText(o, value);
							}
						}
						else if( xmlNode.getNodeType() == org.w3c.dom.Node.TEXT_NODE ) {
							Text o = (Text) xmlNode;
							o.setTextContent(value);
						}
					}
				}
			}
		
		}
		
		
	 

	
	}
	

	/**
	 * Serialize a YChoice to the given root element.
	 * 
	 * @param yroot
	 *           The choice node to serialize.
	 * @param root
	 *           The node to store it into.
	 */
	private void serializeChoice( YChoice yroot, Element root ) {
		Node node = yroot.getChosenNode();
		String xpath = location(node);
		ArrayList<YNode> ynodes = yroot.getYNodes();
		for( int j = 0; j < ynodes.size(); j++ ) {
			if( node instanceof LeafSyntaxTranslator ) {
				YLeaf y = (YLeaf) ynodes.get(j);
				setValue(root, xpath, y, j);
			}
			else if( node instanceof Bag ) {
				YBag y = (YBag) ynodes.get(j);
				setBag(root, xpath, y, j);
			}
			else if( node instanceof Choice ) {
				YChoice y = (YChoice) ynodes.get(j);
				setChoice(root, xpath, y, j);
			}
		}
	}

	/**
	 * Serialize a YLeaf to the given root element. Only called for the root leaf
	 * (boundary case).
	 * 
	 * @param yroot
	 *           The leaf node to serialize.
	 * @param root
	 *           The node to store it into.
	 */
	private void serializeLeaf( YLeaf yroot, Element root ) {
		LeafSyntaxTranslator rootLeaf = yroot.getLeaf();
		String xpath = location(rootLeaf);
		setValue(root, xpath, yroot, 0);
	}

	/**
	 * Set the values from the given YBag into the specified xpath relative to
	 * the given node.
	 * 
	 * @param root
	 *           The root XML element relative to which we serialize.
	 * @param xpath
	 *           The XPath to store it at, relative root the root element.
	 * @param ybag
	 *           The YBag containing the values to store.
	 * @param yindex
	 *           The index of the ynode to store, used when having multiple
	 *           instances of ybag.
	 */
	private void setBag( Element root, String xpath, YBag ybag, int yindex ) {
		if( xpath == null || xpath.length() <= 0 )
			serializeBag(ybag, root); // mapped to content

		org.w3c.dom.Node xmlNode = XslUtil.createNodeForPath(root, xpath, yindex);
		if( xmlNode == null || xmlNode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE )
			throw new MdmiException("Invalid xpath expression {0} for element {1}, ybag {2}", xpath, root.getNodeName(), ybag.toString());
		Element e = (Element) xmlNode;
		serializeBag(ybag, e);
	}

	/**
	 * Set the values from the given YChoice into the specified xpath relative to
	 * the given node.
	 * 
	 * @param root
	 *           The root XML element relative to which we serialize.
	 * @param xpath
	 *           The XPath to store it at, relative root the root element.
	 * @param ychoice
	 *           The YChoice containing the values to store.
	 * @param yindex
	 *           The index of the ynode to store
	 */
	private void setChoice( Element root, String xpath, YChoice ychoice, int yindex ) {
		if( xpath == null || xpath.length() <= 0 )
			serializeChoice(ychoice, root); // mapped to content

		org.w3c.dom.Node xmlNode = XslUtil.createNodeForPath(root, xpath, yindex);
		if( xmlNode == null || xmlNode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE )
			throw new MdmiException("Invalid xpath expression {0} for element {1}, ychoice {2}", xpath, root.getNodeName(), ychoice.toString());
		Element e = (Element) xmlNode;
		serializeChoice(ychoice, e);
	}

	/**
	 * Set one value (from the given YLeaf) into the specified xpath relative to
	 * the given node. The xpath can be: text()
	 * 
	 * @attrName elementName elementName/text() elementName[2]
	 *           elementName[2]/text() elementName@attrName elementName[3]@attrName
	 *           element1/element2...
	 * 
	 * @param root
	 *           The root XML element relative to which we serialize.
	 * @param xpath
	 *           The XPath to store it at, relative root the root element.
	 * @param yleaf
	 *           The YLeaf containing the value to store.
	 * @param yindex
	 *           The index of the ynode to store
	 */
	private void setValue( Element root, String xpath, YLeaf yleaf, int yindex ) {
		String value = yleaf.getValue();
		System.out.println("Create for "+root.getNodeName() + "  --- "+xpath );
		org.w3c.dom.Node xmlNode = XslUtil.createNodeForPath(root, xpath, yindex);

		if( xmlNode.getNodeType() == org.w3c.dom.Node.ATTRIBUTE_NODE ) {
			Attr o = (Attr) xmlNode;
			o.setTextContent(value);
		}
		else if( xmlNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE ) {
			Element o = (Element) xmlNode;
			XmlUtil.setText(o, value);
		}
		else if( xmlNode.getNodeType() == org.w3c.dom.Node.TEXT_NODE ) {
			Text o = (Text) xmlNode;
			o.setTextContent(value);
		}
		else
			throw new MdmiException("Invalid XML node type for xpath expression {0} for element {1}, yleaf {2}: {3}", xpath, root.getNodeName(), yleaf.toString(),
			      xmlNode.getNodeType());
	}

	/**
	 * Get the node location (XPath) name based on the Node.location in the
	 * model. This field can be empty, and it is normally an XPath expression,
	 * such as "elem@attr", etc.
	 * 
	 * @param node
	 *           The node.
	 * @return The node location as an XPath expression.
	 */
	static String location( Node node ) {
		String location = node.getLocation();
		if( location == null || location.trim().length() <= 0 )
			return null;
		return location.trim();
	}

	/**
	 * Get the model path to the given node.
	 * 
	 * @param node
	 *           The node.
	 * @return The path (XPath) to this node.
	 */
	static String getNodePath( Node node ) {
		Node parent = node.getParentNode();
		String name = node.getLocation();
		if( parent == null )
			return name;
		String p = getNodePath(parent);
		if( name.startsWith("@") )
			return p + name;
		return p + '/' + name;
	}
} // DefaultMdmiSyntaxParser
