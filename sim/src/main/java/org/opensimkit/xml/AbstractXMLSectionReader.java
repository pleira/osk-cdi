/*
 * AbstractXMLSectionReader.java
 *
 * Created on 12. October 2008, 14:32
 *
 * Abstract base class with convienience methods for implementing own
 * XMLReaders.
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

import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * Abstract base class with convienience methods for implementing own
 * XMLReaders.
 *
 * @author A. Brandt
 */
public abstract class AbstractXMLSectionReader implements XMLSectionReader {
    protected static final int LEVEL_0 = 0;
    protected static final int LEVEL_1 = 1;
    protected static final int LEVEL_2 = 2;
    protected static final int LEVEL_3 = 3;
    protected static final int LEVEL_4 = 4;
    /** Constant for the name attribute, which is often used in the
     * OpensimKitFile. */
    protected static final String NAME = "name";
    /** Special constant for detecting one-dimensional arrays. */
    protected static final String LENGTH = "length";
    /** Special constant for simplifying handling with one-dimensional arrays.*/
    protected static final String SAME_VALUE = "same";
    /** Name of the root tag for which a XMLSectionReader will be responsible.*/
    protected String rootName;
    private final List<XMLEvent> hierarchy = new ArrayList<XMLEvent>();

    public AbstractXMLSectionReader() {
    	super();
    	System.out.println("AbstractXMLSectionReader");    	
    }
    /**
     *
     * @param rootName The name of the root tag.
     */
    public AbstractXMLSectionReader(final String rootName) {
        this.rootName = rootName;
    }

    public void process(final XMLEvent xmlEvent) throws XMLStreamException {
        /** It is mandatory that the two if-clauses for adding and removing an
         * element from the hierarchy is placed before the branching for the
         * different XML file parts (System, CompDefs, Tabgenerator, etc.) is
         * done. */
        if (xmlEvent.isStartElement()) {
            /** Each time we get a StartElement we add it to our class global
             * hierarchy list. */
            hierarchy.add(xmlEvent);
        } else if (xmlEvent.isEndElement()) {
            /** Each time we get a EndElement we remove the latest element from
             * our class global hierarchy list.
             * Additionally we need to take care of the last EndElement event we
             * receive, as it is the EndElement OpenSimKitConfigFile, which has
             * no StartElement. The StartElement is removed by the StaxInput and
             * not visible in the {@link XMLSectionReader}s! */
            if (hierarchy.size() != 0) {
                hierarchy.remove(hierarchy.size() - 1);
            }
        }
    }

    public String getRootName() {
        return rootName;
    }

    public int getHierarchyLevel() {
        return hierarchy.size();
    }

    /**
     * Returns the element name at the specific hierarchy level.
     * @param level The hierarchy level.
     * @return NAme of the element at this level.
     */
    public String getElementNameAt(final int level) {
        return hierarchy.get(level).asStartElement().getName().toString();

    }

    public XMLEvent getXMLEventAt(final int level) {
        return hierarchy.get(level);

    }
}
