/*
 * StaxHelper.java
 *
 * Created on 24. February 2009
 *
 * Utility class for use with Stax.
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

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Utility class for use with Stax.
 *
 * @author A. Brandt
 */
public final class StaxHelper {

    /**
     * Private constructor to prohibit instancing this utility class.
     */
    private StaxHelper() {

    }

     public static String getOptionalAttributeValue(final XMLEvent xmlEvent,
            final String name) {
        String result;

        Attribute attribute
                = getOptionalAttribute(xmlEvent.asStartElement(), name);

        if (attribute != null) {
            result = attribute.getValue();
        } else {
            result = null;
        }

        return result;
    }

     public static Attribute getOptionalAttribute(
            final StartElement startElement, final String name) {
        Attribute result;

        result = startElement.getAttributeByName(new QName(name));

        return result;
    }

     public static Attribute getMandatoryAttribute(
             final StartElement startElement, final String name) {
        Attribute result;

        result = getOptionalAttribute(startElement, name);
        if (result == null) {
            throw new RuntimeException("Attribute name \"" + name
                    + "\" not found (at line "
                    + startElement.getLocation().getLineNumber()
                    + ")!");
        }
        return result;
    }

     public static String getMandatoryAttributeValue(final XMLEvent xmlEvent,
            final String name) {
        String result;

        Attribute attribute
                = getMandatoryAttribute(xmlEvent.asStartElement(), name);

        if (attribute != null) {
            result = attribute.getValue();
        } else {
            result = "";
        }

        return result;
    }
}
