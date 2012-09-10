/*
 * StaxInput.java
 *
 * Created on 12. October 2008, 14:32
 *
 * A class for XML file reading by the use of a Stax (Streaming API for XML)
 * parser.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2008-10-12
 *      File created - A. Brandt:
 *      Initial version.
 *
 *  2009-06
 *     Integrated logging support.
 *     T. Pieper
 *
 *  2011-01
 *     Included reading of Provider-Subscriber-Table.
 *     A. Brandt
 *
 *-----------------------------------------------------------------------------
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 *-----------------------------------------------------------------------------
 */
package org.opensimkit.xml;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.jboss.weld.environment.se.beans.ParametersFactory;
import org.opensimkit.Kernel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for XML file reading by the use of a Stax (Streaming API for XML)
 * parser.
 *
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.2
 * @since 2.4.6
 */
//@ApplicationScoped
public class StaxInput {
    private static final Logger LOG
            = LoggerFactory.getLogger(StaxInput.class);
    private static final String TEMPLATES = "templates";
    private static final String TEMPLATE  = "template";
    private final XMLTemplateManager xmlTemplateManager;
    
    private final SortedMap<String, XMLSectionReader> items;
    private int    element;
    private String currentSection;
    
    @Inject ParametersFactory pf;
	@Inject ModelsXMLSectionReader modelsXMLSectionReader;

//	@Inject HPBottleT1 bottle; // only for classpath
	
    @Inject
    public StaxInput(Kernel kernel) {
    	System.out.println("injection" + pf.getArgs().get(0));
        xmlTemplateManager = new XMLTemplateManager(TEMPLATES);
        items = new TreeMap<String, XMLSectionReader>();

        registerXMLReader(xmlTemplateManager);
        registerXMLReader(new SystemXMLSectionReader("system", kernel));
        registerXMLReader(
                modelsXMLSectionReader);
        registerXMLReader(
                new MeshXMLSectionReader("meshes", kernel));
        registerXMLReader(new NetlistXMLSectionReader("connections",
                kernel));
        registerXMLReader(
                new TabgeneratorXMLSectionReader("logOutput", kernel));
        registerXMLReader(
        		new ProviderSubscriberXMLSectionReader("provider", kernel));
    }

    
    @Inject
    public void registerXMLReader(final XMLSectionReader xmlReader) {
        if (!items.containsKey(xmlReader.getRootName())) {
            items.put(xmlReader.getRootName(), xmlReader);
        } else {
            throw new RuntimeException("A XMLSectionReader is already"
                    + " registered with this name \"" + xmlReader.getRootName()
                    + "\"!");
        }
    }

    public void unregisterXMLReader(final XMLSectionReader xmlReader) {
        items.remove(xmlReader.getRootName());
    }

    public void process(FileReader reader) throws FileNotFoundException, XMLStreamException {
        // Create the XML input factory
        XMLInputFactory factory = XMLInputFactory.newInstance();

       XMLEventReader xmlEventReader = factory.createXMLEventReader(reader);
       // Loop over XML input stream and process events
       while (xmlEventReader.hasNext()) {
         XMLEvent xmlEvent = (XMLEvent) xmlEventReader.next();
         processEvent(xmlEvent);
       }
    }

    private void processEvent(final XMLEvent xmlEvent)
            throws XMLStreamException {
        /** It is mandatory that the two if-clauses for adding and removing an
         * element from the hierarchy is placed before the branching for the
         * different XML file parts (System, CompDefs, Tabgenerator, etc.) is
         * done. */
        if (xmlEvent.isStartElement()) {
            /** Each time we get a StartElement we add it to our class global
             * hierarchy list. */
            element++;

            /** This is for template handling! */
            String templateName = null;
            templateName
                    = StaxHelper.getOptionalAttributeValue(xmlEvent, TEMPLATE);

            if (templateName != null) {
                /** We have found a template attribute. Now it is time to fire
                 * our saved XMLEvents. */
                injectTemplate(templateName);
                LOG.debug("Injecting template: " + templateName);
            }


            if (element == 2) {
                currentSection
                        = xmlEvent.asStartElement().getName().getLocalPart();
            }
        }

        if (xmlEvent.isEndElement()) {
            /** Each time we get a EndElement we remove the latest element from
             * our class global hierarchy list. */
            element--;

            if (element == 0) {
                /** Take care that the last EndElement will not go to the
                 *  XMLReaders and confuse them, as they get an EndElement
                 *  without a starting StartElement. See {@AbstractXMLReader}!
                 */
                currentSection = null;
            }
        }

        fireEvent(xmlEvent);
    }

    private void fireEvent(final XMLEvent xmlEvent) throws XMLStreamException {
        for (String key: items.keySet()) {
            if (key.equalsIgnoreCase(currentSection)) {
                items.get(key).process(xmlEvent);
            }
        }
    }

    private void injectTemplate(final String name) throws XMLStreamException {
        List<XMLEvent> events = xmlTemplateManager.getTemplate(name);

        if (events != null) {
            for (XMLEvent event : events) {
                fireEvent(event);
            }
        } else {
            LOG.info("A template with the name {} does not exist!", name);
        }
    }

}