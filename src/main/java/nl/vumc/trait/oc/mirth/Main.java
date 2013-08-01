/*

 Copyright 2012 VU Medical Center Amsterdam

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */
package nl.vumc.trait.oc.mirth;

import java.io.IOException;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import nl.vumc.trait.oc.odm.NSContext;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Skeleton for any program contained within this package. It provides and
 * initializes basic support data structures for xml handling and command line
 * parsing.
 *
 * @author Arjan van der Velde (a.vandervelde (at) xs4all.nl)
 */
public abstract class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);
    /**
     * Document Builder Factory, set to be namespace aware in default
     * constructor
     */
    protected DocumentBuilderFactory documentBuilderFactory;
    /**
     * Document Builder
     */
    protected DocumentBuilder documentBuilder;
    /**
     * Turn on/off debugging
     */
    protected boolean debug;
    /**
     * Transformer Factory
     */
    protected TransformerFactory transformerFactory;
    /**
     * xpath factory
     */
    protected XPathFactory xPathFactory;
    /**
     * xpath
     */
    protected XPath xPath;

    protected void splashScreen() throws IOException {
        Properties props = new Properties();
        props.load(Main.class.getResourceAsStream("/application.properties"));
        System.out.println("Starting OCWS version " + props.getProperty("application.version"));
        logger.debug("Starting OCWS version " + props.getProperty("application.version"));
    }

    /**
     * Initialize a Main() object
     *
     * @throws ParserConfigurationException
     */
    public Main() throws Exception {
        splashScreen();
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(false);
        documentBuilderFactory.setNamespaceAware(true); // <- important!
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        transformerFactory = TransformerFactory.newInstance();
        xPathFactory = XPathFactory.newInstance();
        xPath = xPathFactory.newXPath();
        xPath.setNamespaceContext(new NSContext()); // <- important too!
        debug = true;
    }
}
