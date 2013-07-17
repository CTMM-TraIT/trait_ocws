/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.vumc.trait.oc.odm;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.Assert;
import nl.vumc.trait.oc.connect.ConnectInfo;
import nl.vumc.trait.oc.connect.OCConnectorException;
import nl.vumc.trait.oc.connect.OCWebServices;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 *
 * @author j.rousseau
 */
public class ClinicalODMResolverTest {

    private Document odmDocument;

    public ClinicalODMResolverTest() throws Exception {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(false);
        documentBuilderFactory.setNamespaceAware(true); // <- important!
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        InputStream is = this.getClass().getResourceAsStream("ODM_test.xml");

        InputSource reader = new InputSource(is);
        odmDocument = documentBuilder.parse(reader);
    }

    @Test
    @Ignore
    public void testNoPrelimanaryConsistancyCheck() throws Exception {
        ConnectInfo connectInfo = new ConnectInfo("https://wp1vm3.ehv.campus.philips.com/OpenClinica-ws-dev", "mirthconnect", ConnectInfo.toSHA1("mirthconnect"));
        OCWebServices connector = OCWebServices.getInstance(connectInfo, true, false);
        Assert.assertNotNull(connector);

        ClinicalODMResolver resolverUnderTest =
                new ClinicalODMResolver(odmDocument, connector);
        Assert.assertEquals(resolverUnderTest.resolveOdmDocument(), 2);
    }
}
