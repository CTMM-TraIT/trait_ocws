/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.vumc.trait.oc.odm;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author j.rousseau
 */
public class ClasspathResourceURIResolver implements URIResolver {

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        return new StreamSource(ClasspathResourceURIResolver.class.getClassLoader().getResourceAsStream(href));
    }
}
