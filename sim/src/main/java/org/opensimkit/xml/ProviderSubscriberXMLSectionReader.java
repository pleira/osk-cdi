/*
 * SystemXMLSectionReader.java
 *
 *
 * Concrete implementation of the behaviour for reading the provider subscriber
 * section of the XML input file.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2010-02-09
 *      File created - A. Brandt:
 *      Initial version.
 *
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
import org.opensimkit.providerSubscriber.ProviderSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation of the behaviour for reading the provider subscriber
 * section of the XML input file.
 *
 * @author A. Brandt
 * @version 1.0
 * @since 3.5.0
 */
public class ProviderSubscriberXMLSectionReader
        extends AbstractTemplateXMLSectionReader {
    private static final Logger LOG
            = LoggerFactory.getLogger(ProviderSubscriberXMLSectionReader.class);
    private static final String ENTRY = "entry";
    private static final String PROVIDER = "provider";
    private static final String SUBSCRIBER = "subscriber";
    private static final String MODEL = "model";
    private static final String VARIABLE = "variable";
    
    private static final String SYSDESC = "systemDescription";
    private static final String SYMULATIONCONTROL = "simulationControl";
    private static final String TIMEHANDLER = "timeHandler";
    private final Kernel      kernel;
    private final SeqModSim   seqModSim;
    private final TimeHandler timeHandler;
    @Inject Manipulator manipulator;
    private ProviderSubscriber providerSubscriber;
    private String entryName;
    private String providerModel;
    private String providerVariable;
    private String subscriberModel;
    private String subscriberVariable;


    public ProviderSubscriberXMLSectionReader(final String rootName, final Kernel kernel) {
        super(rootName);
        this.kernel      = kernel;
        this.seqModSim   = kernel.getSeqModSim();
        this.timeHandler = kernel.getTimeHandler();
        // manipulator = kernel.getManipulator();
        this.providerSubscriber = kernel.getProviderSubscriber();
    }

    public void foundArrayValue(final String elementName, final String value,
            final int length) {
        /** Intentionally left empty. */
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

    }

    public void foundStartElement(final StartElement startElement) {
        if (getHierarchyLevel() == LEVEL_2) {
            String entry = startElement.getName().getLocalPart();
            LOG.debug("Entry: " + entry);

            if (entry.equals(ENTRY)) {
                entryName
                    = StaxHelper.getMandatoryAttributeValue(startElement, NAME);
                LOG.debug("Entry name: " + entryName);
            }
        } else if (getHierarchyLevel() == LEVEL_3) {
            String name = startElement.getName().getLocalPart();
            LOG.debug("Name: " + name);
            if (name.equals(PROVIDER)) {
                providerModel
                    = StaxHelper.getMandatoryAttributeValue(startElement, MODEL);
                providerVariable
                    = StaxHelper.getMandatoryAttributeValue(startElement, VARIABLE);
            } else if (name.equals(SUBSCRIBER)) {
                subscriberModel
                    = StaxHelper.getMandatoryAttributeValue(startElement, MODEL);
                subscriberVariable
                    = StaxHelper.getMandatoryAttributeValue(startElement, VARIABLE);
                providerSubscriber.add(entryName, providerModel,
                        providerVariable, subscriberModel, subscriberVariable);
            }
        }
    }
}
