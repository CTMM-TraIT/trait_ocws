/**
*
* Copyright (c) CTMM-TraIT / Vrije Universiteit medical center (VUmc)
*
*/

package nl.vumc.trait.oc.odm;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Jacob Rousseau
 */
public class OCWSEntityResolver implements EntityResolver {

    public InputSource resolveEntity(String publicID, String systemID)
        throws SAXException {
        if (systemID.equals("http://www.cdisc.org/ns/odm/v1.3")) {
			return new InputSource(OCWSEntityResolver.class.getClassLoader().getResourceAsStream("OpenClinica-ODM1-3-0-OC2-0.xsd"));            
        }       
        return null;
    }
}
