/*
 * SystemXMLSectionReader.java
 *
 * Created on 12. October 2008, 14:32
 *
 * Concrete implementation of the behaviour for reading the System section of
 * the XML input file.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2008-10-12
 *      File created - A. Brandt:
 *      Initial version.
 *
 *  2008-05
 *      Changed SystemXMLSectionReader to use the name attribute instead of the
 *      element name. Numorous XML element names simlified. - A. Brandt
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
 *-----------------------------------------------------------------------------
 */
package org.opensimkit.xml;

import javax.inject.Inject;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import org.opensimkit.Kernel;
import org.opensimkit.SeqModSim;
import org.opensimkit.TimeHandler;
import org.opensimkit.manipulation.Manipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation of the behaviour for reading the System section of
 * the XML input file.
 *
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.2
 * @since 2.5.0
 */
public class SystemXMLSectionReader
        extends AbstractTemplateXMLSectionReader {
    private static final Logger LOG
            = LoggerFactory.getLogger(SystemXMLSectionReader.class);
    private static final String SYSDESC = "systemDescription";
    private static final String SYMULATIONCONTROL = "simulationControl";
    private static final String TIMEHANDLER = "timeHandler";
    private final Kernel      kernel;
    private final SeqModSim   seqModSim;
    private final TimeHandler timeHandler;
     Manipulator manipulator;

    public SystemXMLSectionReader(final String rootName, final Kernel kernel) {
        super(rootName);
        this.kernel      = kernel;
        this.seqModSim   = kernel.getSeqModSim();
        this.timeHandler = kernel.getTimeHandler();
        manipulator = kernel.getManipulator();
    }

    public void foundArrayValue(final String elementName, final String value,
            final int length) {

        try {
            manipulator.setArray(kernel, elementName, value, length);
        } catch (IllegalAccessException ex) {
            LOG.error("Exception: ", ex);
        } catch (ClassNotFoundException ex) {
            LOG.error("Exception: ", ex);
        } catch (NoSuchFieldException ex) {
            LOG.error("Exception: ", ex);
        }
    }

    public void foundCharacterData(final String elementName,
            final String value) {

        String variableName = StaxHelper.getOptionalAttributeValue(
                getXMLEventAt(LEVEL_2), NAME);

        if (getHierarchyLevel() == LEVEL_3) {
            String subsection = getElementNameAt(LEVEL_1);
            if (subsection.equals(SYSDESC)) {
                setData(kernel, variableName, value);
            } else if (subsection.equals(SYMULATIONCONTROL)) {
                setData(kernel, variableName, value);
            } else if (subsection.equals(TIMEHANDLER)) {
                setData(timeHandler, variableName, value);
            }
        }
    }

    private void setData(final Object systemPointer, final String elementName,
            final String value) {

        try {
            manipulator.setFromString(systemPointer, elementName, value);
        } catch (IllegalAccessException ex) {
            LOG.error("Exception: ", ex);
        } catch (ClassNotFoundException ex) {
            LOG.error("Exception: ", ex);
        } catch (NoSuchFieldException ex) {
            LOG.error("Exception: ", ex);
        } catch (IllegalArgumentException ex) {
            LOG.error("Exception: ", ex);
        }
    }

    public void foundEndElement(final EndElement endElement) {
        if (getHierarchyLevel() == LEVEL_0) {
            seqModSim.printSimSettings();
            timeHandler.init();
        }
    }

    public void foundStartElement(final StartElement startElement) {
        /** Intentionally left empty. */
    }

}
