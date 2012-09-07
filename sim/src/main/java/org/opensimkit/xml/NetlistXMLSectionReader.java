/*
 * NetlistXMLSectionReader.java
 *
 * Created on 26. October 2008, 14:32
 *
 * Concrete implementation of the behaviour for reading the MeshDefs section of
 * the XML input file.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2008-10-26
 *      File created - A. Brandt:
 *      Initial version.
 *
 *  2009-01-06
 *      Changed bid to pid. - A. Brandt
 *
 *  2009-06
 *     Integrated logging support.
 *     T. Pieper
 *
 *  2009-07
 *      OpenSimKit V 2.8
 *      Upgraded for handling of new port classes.
 *      A. Brandt
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import org.opensimkit.ComHandler;
import org.opensimkit.Kernel;
import org.opensimkit.PortHandler;
import org.opensimkit.manipulation.Manipulator;
import org.opensimkit.ports.Port;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation of the behaviour for reading the NetList section of
 * the XML input file.
 *
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.1
 * @since 2.5.0
 */
public class NetlistXMLSectionReader
        extends AbstractTemplateXMLSectionReader {
    private static final Logger LOG
            = LoggerFactory.getLogger(NetlistXMLSectionReader.class);
    private static final String PID      = "name";
    private static final String MODEL    = "model";
    private static final String FROM     = "from";
    private static final String TO       = "to";
    private static final String PORTID   = "port";
    private static final String TYPE     = "class";
    private static final String VARIABLE = "variable";
    private final Kernel      kernel;
    private final PortHandler portHandler;
    private final ComHandler  comHandler;
    @Inject Manipulator manipulator;
    private       String  portName;
    private       String  fromCID;
    private       String  fromPortID;
    private       String  toCID;
    private       String  toPortID;
    private       String  portType;
    private       Port    currentPort;

    public NetlistXMLSectionReader(final String rootName, final Kernel kernel) {
        super(rootName);
        this.kernel      = kernel;
        this.comHandler  = kernel.getComHandler();
        this.portHandler = kernel.getPortHandler();
        // manipulator = kernel.getManipulator();
    }

    public void foundArrayValue(final String elementName, final String value,
            final int length) {
        /** Intentionally left empty. */
    }

    public void foundCharacterData(final String elementName,
            final String value) {

        if (getHierarchyLevel() == LEVEL_3) {
            if (elementName.equals(VARIABLE)) {
                StartElement startElement
                        = getXMLEventAt(LEVEL_2).asStartElement();
                String name = getMandatoryAttributeValue(startElement, NAME);
                try {
                    manipulator.setFromString(currentPort, name, value);
                    LOG.debug("Set variable '{}' of Port '{}' to '{}'",
                            new Object[]{name, currentPort.getName(), value});
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void foundEndElement(final EndElement endElement) {
        if (getHierarchyLevel() == LEVEL_1) {
            initPort();
        }
    }

    @SuppressWarnings("unchecked")
    public void foundStartElement(final StartElement startElement) {
        if (getHierarchyLevel() == LEVEL_2) {
            portName = getMandatoryAttributeValue(startElement, PID);
            portType = getMandatoryAttributeValue(startElement, TYPE);
            try {
                /** Retrieve the Class reference for the name of the class.
                 *  The name of the class is located in the variable "portType".
                 */
                Class<?> portClass = Class.forName(portType);
                /** Retrieve the constructor with the first argument of type
                 *  String and the second argument of type
                 *  org.opensimkit.Kernel. This must be done, because Port has
                 *  no default constructor.
                 */
                Constructor<?> constructor
                        = portClass.getConstructor(String.class, Kernel.class);
                /** Execute this constructor to instantiate a new object from
                 *  the originally given name. */
                currentPort = (Port) constructor.newInstance(portName, kernel);

                LOG.debug("Port {} was created.", portName);
            } catch (ClassNotFoundException ex) {
                LOG.error("Exception: ", ex);
            } catch (InstantiationException ex) {
                LOG.error("Exception: ", ex);
            } catch (IllegalAccessException ex) {
                LOG.error("Exception: ", ex);
            } catch (NoSuchMethodException ex) {
                LOG.error("Exception: ", ex);
            } catch (InvocationTargetException ex) {
                LOG.error("Exception: ", ex);
            }
            portHandler.addPort(currentPort);
        } else if (getHierarchyLevel() == LEVEL_3) {
            String elementName = startElement.getName().toString();

            if (elementName.equals(FROM)) {
                fromCID = getMandatoryAttributeValue(startElement, MODEL);
                fromPortID = getMandatoryAttributeValue(startElement, PORTID);
            } else if (elementName.equals(TO)) {
                toCID = getMandatoryAttributeValue(startElement, MODEL);
                toPortID = getMandatoryAttributeValue(startElement, PORTID);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void initPort() {
        try {

            portHandler.initPort(portName, comHandler, fromCID, toCID,
                    fromPortID, toPortID);
            Port port = portHandler.getPort(portName);

        } catch (IllegalAccessException ex) {
            LOG.error("Exception: ", ex);
        } catch (ClassNotFoundException ex) {
            LOG.error("Exception: ", ex);
        } catch (NoSuchFieldException ex) {
            LOG.error("Exception: ", ex);
        }
    }
}
