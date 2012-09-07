/*
 * XMLSectionReader.java
 *
 * Created on 12. October 2008, 14:32
 *
 * Interface for the XMLReaders used by StaxInput. Take a look at
 * AbstractXMLReader for a base implementation.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2008-10-12
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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * Interface for the XMLSectionReaders used by StaxInput. Take a look at
 * AbstractXMLSectionReader for a base implementation.
 *
 * @author A. Brandt
 */
public interface XMLSectionReader {
    /** Main method invoked by StaxInput.
     * @param xmlEvent Event to process.
     * @throws XMLStreamException
     */
    void process(final XMLEvent xmlEvent) throws XMLStreamException;
    /** Returns the root name which is needed by StaxInput to decide which
     * XMLSectionReader should be invoked.
     * @return The name of the root tag on which this XMLSectionReader will be
     *         invoked.
     */
    String getRootName();
}
