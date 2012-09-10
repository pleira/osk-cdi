/*
 * AbstractTemplateXMLSectionReader.java
 *
 * Created on 12. October 2008, 14:32
 *
 * Abstract class which serves as the base for the template pattern.
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
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class which serves as the base for the template pattern.
 *
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.1
 * @since 2.4.6
 */
public abstract class AbstractTemplateXMLSectionReader
        extends AbstractXMLSectionReader {
    private static final Logger LOG
            = LoggerFactory.getLogger(AbstractTemplateXMLSectionReader.class);
    protected StartElement currentStartElement;

    public AbstractTemplateXMLSectionReader() {
    	super();
    	System.out.println("AbstractTemplateXMLSectionReader");
    }

    public AbstractTemplateXMLSectionReader(final String rootName) {
        super(rootName);
    }

    @Override
    public  void process(final XMLEvent xmlEvent)
            throws XMLStreamException {
        /** Important, do not forget this! */
        super.process(xmlEvent);

        if (xmlEvent.isStartElement()) {
            currentStartElement = xmlEvent.asStartElement();
            foundStartElement(xmlEvent.asStartElement());
        } else if (xmlEvent.isCharacters()) {
            if (xmlEvent.asCharacters().isWhiteSpace() == false) {
                processData(xmlEvent.asCharacters());
            }
        } else if  (xmlEvent.isEndElement()) {
            foundEndElement(xmlEvent.asEndElement());
        }
    }

    /**
     * Processes the character data.
     * @param characters
     */
    private void processData(final Characters characters) {
        final Attribute lengthAttribute
                = currentStartElement.getAttributeByName(new QName(LENGTH));
        if (lengthAttribute != null) {
            String value = characters.getData();
            int length = Integer.valueOf(lengthAttribute.getValue());
            /** Check if there is the attribute "same" and react accordingly. */
            Attribute sameValueAttribute
                = currentStartElement.getAttributeByName(new QName(SAME_VALUE));
            if (sameValueAttribute != null) {
                value = duplicateValues(characters.getData(), length);
            }
            foundArrayValue(currentStartElement.getName().toString(), value,
                        length);
        } else {
            foundCharacterData(currentStartElement.getName().toString(),
                    characters.getData());
        }
    }

    /**
     * Duplicates the given value "times" times.
     * @param value The input string.
     * @param times How often the input string is duplicated.
     * @return A string in the form: "value value....".
     */
    private String duplicateValues(final String value, final int times) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < times; i++) {
            result.append(value);
            result.append(" ");
        }

        return result.toString();
    }

    public abstract void foundStartElement(StartElement startElement);

    public abstract void foundEndElement(EndElement endElement);

    public abstract void foundArrayValue(final String elementName,
            final String value, final int length);

    public abstract void foundCharacterData(final String elementName,
            final String value);

    protected String getMandatoryAttributeValue(final XMLEvent xmlEvent,
            final String name) {
        return StaxHelper.getMandatoryAttributeValue(xmlEvent, name);
    }

    protected String getOptionalAttributeValue(final XMLEvent xmlEvent,
            final String name) {
        return StaxHelper.getOptionalAttributeValue(xmlEvent, name);
    }

    protected void printStartElement(final StartElement startElement) {
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

    protected void printEndElement(final String endElement) {
        printElement("/" + endElement, getHierarchyLevel() + 1);
    }

    protected void printElement(final String element, final int depth) {
        printOutput("<" + element + ">", depth);
    }

    protected void printData(final String data) {
        printOutput(data, getHierarchyLevel() + 1);
    }

    protected void printOutput(final String string, final int depth) {
        final StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < depth; i++) {
            stringBuilder.append("  ");
        }
        stringBuilder.append(string);

        LOG.info(stringBuilder.toString());
    }

}
