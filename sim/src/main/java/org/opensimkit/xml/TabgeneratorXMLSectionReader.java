/*
 * TabgeneratorXMLSectionReader.java
 *
 * Created on 30. October 2008, 14:32
 *
 * Concrete implementation of the behaviour for reading the LogOutput section of
 * the XML input file.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2008-10-30
 *      File created - A. Brandt:
 *      Initial version.
 *
 *
 *  2011-01
 *     Fixed parsing typo in inputfile: delimeter -> delimiter.
 *     J. Eickhoff
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

import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import org.opensimkit.ComHandler;
import org.opensimkit.Kernel;
import org.opensimkit.TabGenerator;

/**
 * Concrete implementation of the behaviour for reading the NetList section of
 * the XML input file.
 *
 * @author A. Brandt
 * @author J. Eickhoff
 * @version 1.1
 * @since 2.5.0
 */
public final class TabgeneratorXMLSectionReader
        extends AbstractTemplateXMLSectionReader {
    private static final String LOGSTART = "start";
    private static final String LOGEND = "end";
    private static final String LOGFACTOR = "factor";
    private static final String LOGVARIABLE = "entry";
    private static final String CID = "model";
    private static final String VARIABLE = "variable";
    private static final String FORMAT = "format";
    private static final String HEADER = "header";
    private static final String ALIGN_HEADER = "alignHeader";
    private static final String DELIMITER = "delimiter";
    private static final String DELIMITER_REGEX = "delimiterRegex";
    private final TabGenerator tabGenerator;
    private final ComHandler comHandler;

    public TabgeneratorXMLSectionReader(final String rootName,
            final Kernel kernel) {
        super(rootName);
        tabGenerator = kernel.getTabGenerator();
        comHandler = kernel.getComHandler();
    }

    public void foundArrayValue(final String elementName, final String value,
            final int length) {
        /** Intentionally left empty. */
    }

    public void foundCharacterData(final String elementName,
            final String value) {
        /** Intentionally left empty. */
    }

    public void foundEndElement(final EndElement endElement) {
        if (getHierarchyLevel() == LEVEL_0) {
            tabGenerator.initStepcounter();
        }

    }

    public void foundStartElement(final StartElement startElement) {
        if (getHierarchyLevel() == LEVEL_1) {
            String logStart
                    = getMandatoryAttributeValue(startElement, LOGSTART);
            tabGenerator.setStart(Double.valueOf(logStart));
            String logEnd = getMandatoryAttributeValue(startElement, LOGEND);
            tabGenerator.setEnd(Double.valueOf(logEnd));
            String logFactor
                    = getMandatoryAttributeValue(startElement, LOGFACTOR);
            tabGenerator.setFactor(Integer.valueOf(logFactor));
            String delimiter
                    = getMandatoryAttributeValue(startElement, DELIMITER);
            tabGenerator.setDelimiter(delimiter);
            String headerIsAligned
                    = getOptionalAttributeValue(startElement, ALIGN_HEADER);
            tabGenerator.setHeaderAlignment(Boolean.valueOf(headerIsAligned));
        } else if (getHierarchyLevel() == LEVEL_2) {
            String elementName = startElement.getName().toString();
            if (elementName.equals(LOGVARIABLE)) {
                String cid = getMandatoryAttributeValue(startElement, CID);
                String variable
                        = getMandatoryAttributeValue(startElement, VARIABLE);
                String format = getOptionalAttributeValue(startElement, FORMAT);
                String header = getOptionalAttributeValue(startElement, HEADER);
                tabGenerator.addVariable(cid, variable, header, format);
            }
        }
    }

}
