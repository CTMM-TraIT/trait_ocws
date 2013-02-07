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
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;


/**
 *
 * @author j.rousseau
 */
public class ImportODMTest {
    
    
    private String odmDocument;
    
    private ImportODM instance;
    
    public ImportODMTest() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(false);
        documentBuilderFactory.setNamespaceAware(true); // <- important!
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        
    }
    
    
    public void initialize(String fileName) throws Exception {
        instance = new ImportODM();
        InputStream is = this.getClass().getResourceAsStream(fileName);                        
        odmDocument = IOUtils.toString(is, "UTF-8");
        is.close();
        instance.setupBatch(fileName, "https://wp1vm3.ehv.campus.philips.com/OpenClinica-ws-dev", "mirthconnect",  "mirthconnect");        
    }
    
    /**
     * Test of process method, of class ImportODM.
     */
    @Test
    public void testLoad() throws Exception {
        String fileName = "PREHDICT-upload.xml";
        initialize(fileName);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            String baseReplacement = "Test_" + RandomStringUtils.randomAlphanumeric(25);
            baseReplacement = baseReplacement.toUpperCase();
            String parameterReplacedString = StringUtils.replace(odmDocument, "_UNIQUE_ID_", baseReplacement);
            System.out.println("Uploading i "  + i + " subject : " + baseReplacement);
            parameterReplacedString = StringUtils.replace(parameterReplacedString, "_SUBJECT_KEY_", baseReplacement);
            instance.process(fileName, parameterReplacedString);
        }
        long duration = System.currentTimeMillis() - start;
        System.out.println("Duration of run: " + duration / 1000 + " seconds");
    }
}
