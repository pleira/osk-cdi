/*
 * XMLTemplateManager.java
 *
 * Created on 24. February 2009
 *
 * Provides a template mechanismn for the OSK input file.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2009-02-24
 *      File created - A. Brandt:
 *      Initial version.
 *
 *-----------------------------------------------------------------------------
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 */
package org.opensimkit.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * Provides a template mechanismn for the OSK input file.
 *
 * @author A. Brandt
 * @version 1.0
 * @since 2.5.1
 */
public class XMLTemplateManager extends AbstractXMLSectionReader {
    private final Map<String, List<XMLEvent>> templates
            = new HashMap<String, List<XMLEvent>>();
    private  String currentTemplate;


    public XMLTemplateManager(final String rootName) {
        super(rootName);
    }

    @Override
    public void process(final XMLEvent xmlEvent) throws XMLStreamException {
        /** Important, do not forget this! */
        super.process(xmlEvent);

        if ((getHierarchyLevel() == LEVEL_2) && (xmlEvent.isStartElement())) {
            String templateName
                    = StaxHelper.getMandatoryAttributeValue(xmlEvent, NAME);
            if (templateName !=  null) {
                currentTemplate = templateName;
                createNewTemplate(templateName);
            } else {
                throw new RuntimeException("Template name expected!"
                        + "At line " + xmlEvent.getLocation().getLineNumber()
                        + ".");
            }
        } else if (getHierarchyLevel() > LEVEL_1) {
            addToTemplate(currentTemplate, xmlEvent);
        }
    }

    private void createNewTemplate(final String name) {
        /** Check here for name collisions and throw appropriate exception. */
        if (templates.containsKey(name)) {
            throw new RuntimeException("A template with the name \""
                    + name + "\" already exists!");
        } else {
            List<XMLEvent> xmlEvents = new ArrayList<XMLEvent>();
            templates.put(name, xmlEvents);
        }
    }

    private void addToTemplate(final String name, final XMLEvent xmlEvent) {
        templates.get(name).add(xmlEvent);
    }

    public List<XMLEvent> getTemplate(final String name) {
        return templates.get(name);
    }
}
