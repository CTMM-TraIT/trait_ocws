/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.vumc.trait.oc.odm;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

/**
 * Inspired by the code found at:
 * http://docstore.mik.ua/orelly/xml/jxslt/ch05_04.htm
 *
 */
public class TransformerCache {

    private static Map<String, Transformer> cache = new HashMap<String, Transformer>();

    /**
     * Flush all cached stylesheets from memory, emptying the cache.
     */
    public static synchronized void flushAll() {
        cache.clear();
    }

    /**
     * Flush a specific cached stylesheet from memory.
     *
     * @param xsltFileName the file name of the stylesheet to remove.
     */
    public static synchronized void flush(String xsltFileName) {
        cache.remove(xsltFileName);
    }

    /**
     * Creates a new transformer and adds it to the cache
     *
     * @param xsltFileName the file name of an XSLT stylesheet.
     * @return a transformation context for the given stylesheet.
     */
    public static synchronized Transformer newTransformer(String xsltFileName)
            throws TransformerConfigurationException {
        Transformer transformer = cache.get(xsltFileName);
        // create and put a new transformer in the cache
        if (transformer == null) {
            InputStream xslInput = TransformerCache.class.getResourceAsStream(xsltFileName);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setURIResolver(new ClasspathResourceURIResolver());
            Templates templateCompiler = transformerFactory.newTemplates(new StreamSource(xslInput));
            transformer = templateCompiler.newTransformer();
            cache.put(xsltFileName, transformer);
        }
        return transformer;
    }

    private TransformerCache() {
    }
}
