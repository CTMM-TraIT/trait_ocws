/**
 *
 * Copyright (c) CTMM-TraIT / Vrije Universiteit medical center (VUmc)
 * 
*/
package nl.vumc.trait.oc.odm;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utils for dealing with XML
 *
 * @author Jacob Rousseau
 */
public class XMLUtils {
	
	private static final Logger logger = LogManager.getLogger(XMLUtils.class);

	/**
	 * Document Builder Factory, set to be namespace aware in default
	 * constructor
	 */
	private static final DocumentBuilderFactory documentBuilderFactory;
	/**
	 * Document Builder
	 */
	private static final DocumentBuilder documentBuilder;

	static {
		try {
			System.setProperty("jaxp.debug", "1");

			documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setValidating(true);
			documentBuilderFactory.setNamespaceAware(true); // <- important!
			documentBuilderFactory.setXIncludeAware(true);
			documentBuilderFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");

			documentBuilder = documentBuilderFactory.newDocumentBuilder();

		} catch (ParserConfigurationException pce) {
			throw new IllegalStateException(pce);
		}
	}

	public static String nodeToString(Node node) {
		StringWriter sw = new StringWriter();
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.transform(new DOMSource(node), new StreamResult(sw));
		} catch (TransformerException te) {
			logger.error("Transformer Exception " + te.getMessage());
		}
		return sw.toString();
	}

	/**
	 * Parses a string and converts it to a {@link Document}
	 *
	 * @param xmlInput the xml to parse
	 * @return a Document
	 * @throws SAXException if a problem occurs parsing the input
	 * @throws IOException if a problem occurs creating the return document
	 */
	public static Document buildDocument(String xmlInput) throws SAXException, IOException {

		return documentBuilder.parse(new InputSource(new StringReader(xmlInput)));
	}

}
