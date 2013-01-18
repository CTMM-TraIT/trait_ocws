/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.vumc.trait.oc.mirth;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import nl.vumc.trait.oc.connect.ConnectInfo;
import org.apache.commons.io.IOUtils;
import org.junit.Test;


/**
 *
 * @author j.rousseau
 */
public class ImportODMTest {
    
    
    private String odmDocument;
    
    private ImportODM instance = new ImportODM();
    
    public ImportODMTest() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(false);
        documentBuilderFactory.setNamespaceAware(true); // <- important!
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        InputStream is = this.getClass().getResourceAsStream("ODM_test_import.xml");
                
        
        odmDocument = IOUtils.toString(is, "UTF-8");
        
        instance.setupBatch("ODM_test_import.xml", "https://wp1vm3.ehv.campus.philips.com/OpenClinica-ws-dev", "mirthuser", "mirthuser");
    }

    /**
     * Test of process method, of class ImportODM.
     */
    @Test
    public void testProcess() throws Exception {
        
        String expResult = "";
        String result = instance.process("ODM_test_import.xml", odmDocument);
        
    }
}
