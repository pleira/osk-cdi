/*
 * PortHandler.java
 *
 * Created on 3. Juli 2007, 22:24
 *
 *  This is the implementation of the Class PortHandler.
 *
 *-----------------------------------------------------------------------------
 *  Modification History
 *
 *  2004-12-05
 *      File created  J. Eickhoff:
 *
 *      Class architecture is a derivative from ObjectSim 2.0.3.,
 *      a simulation program published in:
 *
 *        Eickhoff, J.:
 *        Modulare Programmarchitektur fuer ein wissensbasiertes
 *        Simulationssystem mit erweiterter Anwendbarkeit in der
 *        Entwicklung und Betriebsberwachung verfahrenstechnischer
 *        Anlagen.
 *        PhD thesis in Department Process Engineering of
 *        TU Hamburg-Harburg, 1996.
 *
 *      See also file history cited there and see historic relation of
 *      this OpenSimKit class to a.m. ObjectSim explained in
 *      OpenSimKit Documentation.
 *
 *      File under GPL  see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 *
 *  2005-09
 *      OpenSimKit V 2.2
 *      Modifications enterd for XML input file parsing by
 *      Peter Heinrich  peterhe@student.ethz.ch
 *
 *  2008-05
 *      OpenSimKit V 2.4
 *      Ported from C++ to Java
 *      A. Brandt  alexander.brandt@gmail.com
 *
 *  2009-01
 *      OpenSimKit V 2.5.1
 *      Some clean up and javadoc improvements.
 *      A.Brandt
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
 */
package org.opensimkit;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.opensimkit.manipulation.Manipulator;
import org.opensimkit.ports.Port;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the implementation of the Class PortHandler.
 *
 * @author J. Eickhoff
 * @author P. Heinrich
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.2
 * @since 2.4.0
 */
@ApplicationScoped
public class PortHandler {
    private static final Logger LOG
            = LoggerFactory.getLogger(PortHandler.class);
    private static final String NULL = "null";
    @Inject Manipulator manipulator;
    /** Map of all ports inside the port handler. The key is the port name and
     *  the value is a reference to the port object. */
    private final SortedMap<String, Port>  items = new TreeMap<String, Port>();
    /** The name of the port handler. */
    private String  name = "Port-Collection";
    /** A comment attached to the port handler. Do we need this? */
    private String  comment = "none";


    /**
     * Initializes a port.
     *
     * @param portName    The name of the port object.
     * @param comHandler  A reference to the component handler.
     * @param fromModelID The name of the model from which the port starts.
     * @param toModelID   The name of the model on which the ports ends.
     * @param fromPortID  The ID of the port from which this port object starts.
     * @param toPortID    The ID of the port on which this port object ends.
     */
    public void initPort(final String portName, final ComHandler comHandler,
            final String fromModelID, final String toModelID,
            final String fromPortID, final String toPortID)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {
        Model fromModel;
        Model toModel;

        Port port = getPort(portName);

        if (port == null) {  // Port doesn't exist
            LOG.info("Invalid port in /System-Connection.");
            throw new RuntimeException("Invalid port (" + portName
                    + ") in System-Connection");
    }

        if (fromModelID.equals(NULL)) {
            fromModel = null;
        } else {
            fromModel = comHandler.getItemKey(fromModelID);
            if (fromModel == null) {  // Model-0 doesn't exist
                LOG.info("Invalid model in /System-Connection.");
            }
        }

        if (toModelID.equals(NULL)) {
            toModel = null;
        } else {
            toModel = comHandler.getItemKey(toModelID);
            if (toModel == null) {  // Model-1 doesn't exist
                LOG.info("Invalid model in /System-Connection.");
            }
        }

        port.connectWith(fromModel, toModel);
        if (fromModel != null) {
            manipulator.setPort(fromModel, fromPortID, port);
            /* After we injected the reference to the port into the model, we
             * need to update the manipulator's registry. */
            manipulator.updateInstance(fromModel);
        }
        if (toModel != null) {
            manipulator.setPort(toModel, toPortID, port);
            /* After we injected the reference to the port into the model, we
             * need to update the manipulator's registry. */
            manipulator.updateInstance(toModel);
        }
    }

    /**
     *
     */
    public void showLinks() {
        Iterator<String> it = items.keySet().iterator();
        while (it.hasNext()) {
            Port port = items.get(it.next());
            //port.showLinkedComps();
        }
    }

    /**
     * Adds a port to the port handler.
     *
     * @param port   The reference to the Port object.
     */
    public void addPort(final Port port) {
        if (port.getName() != null) {
            items.put(port.getName(), port);
        } else {
            LOG.error("Port name cannot be null (Port {}). ", port.toString());
        }
    }

    /**
     * Get a specific port from the port handler.
     *
     * @param key   The name of the port.
     * @return   The corresponding port.
     */
    public Port getPort(final String key) {
        return items.get(key);
    }

    /**
     * Removes a port from the port handler.
     *
     * @param key   The name of the port.
     */
    public void removePort(final String key) {
        items.remove(key);
        LOG.info("Removed '{}' from {}", key, name);
    }

    /**
     * Removes a port from the port handler. This is done by passing a reference
     * to the port object.
     *
     * @param reference   The reference to the port object.
     */
    public void removePortByReference(final Port reference) {
        Iterator<String> it = items.keySet().iterator();
        while (it.hasNext()) {
            String temporary = it.next();
            if (items.get(temporary) == reference) {
                items.remove(temporary);
            }
        }
    }
}
