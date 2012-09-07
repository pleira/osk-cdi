/*
 * PrintToConsoleXMLSectionReader.java
 *
 * Created on 12. October 2008, 14:32
 *
 * A simple XMLReader which prints all read tags to the console. It can be used
 * to understand the XMLReaders in OSK.
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
 *-----------------------------------------------------------------------------
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 */
package org.opensimkit.xml;

import java.util.Iterator;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple XMLReader which prints all read tags to the console. It can be used
 * to understand the XMLReaders in OSK.
 *
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.1
 * @since 2.4.6
 */
public class PrintToConsoleXMLSectionReader extends AbstractXMLSectionReader {
    private static final Logger LOG
            = LoggerFactory.getLogger(PrintToConsoleXMLSectionReader.class);

    public PrintToConsoleXMLSectionReader(final String rootName) {
        super(rootName);
    }

    @Override
    public void process(final XMLEvent xmlEvent) throws XMLStreamException {
        //Important, do not forget this!
        super.process(xmlEvent);

        if (xmlEvent.isStartElement()) {
            printStartElement(xmlEvent.asStartElement());
        } else if (xmlEvent.isEndElement()) {
            printEndElement(xmlEvent.asEndElement().getName().toString());
        } else if (xmlEvent.isCharacters()) {
            if (xmlEvent.asCharacters().isWhiteSpace() == false) {
                printData(xmlEvent.asCharacters().getData());
            }
        }
    }

    public void printStartElement(final StartElement startElement) {
        final StringBuilder stringBuilder = new StringBuilder();

        final Iterator<?> iterator = startElement.getAttributes();
        int numberOfAttributes = 0;
        while (iterator.hasNext()) {
            numberOfAttributes++;
            final Attribute attribute = (Attribute) iterator.next();
            stringBuilder.append(attribute.getName() + "=\""
                    + attribute.getValue());
            stringBuilder.append("\", ");
        }
        if (numberOfAttributes != 0) {
            stringBuilder.delete(stringBuilder.length() - 2,
                    stringBuilder.length());
            stringBuilder.insert(0, " ");
        }
        printElement(startElement.getName().toString()
                + stringBuilder.toString(), getHierarchyLevel());
    }

    public void printEndElement(final String endElement) {
        printElement("/" + endElement, getHierarchyLevel() + 1);
    }

    private void printElement(final String element, final int depth) {
        printOutput("<" + element + ">", depth);
    }

    private void printData(final String data) {
        printOutput(data, getHierarchyLevel() + 1);
    }

    private void printOutput(final String string, final int depth) {
        final StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < depth; i++) {
            stringBuilder.append("  ");
        }
        stringBuilder.append(string);

        LOG.info(stringBuilder.toString());
    }

}
