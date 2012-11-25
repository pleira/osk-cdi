/*
 * AnalogPort.java
 *
 * Definition of analog line port connection dataset.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2009-04-21
 *      File created - A. Brandt:
 *      Initial version.
 *
 *
 *  2009-04-21
 *      Boundary condition handling cleaned - A. Brandt:
 *
 *
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 */
package org.opensimkit.ports;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Definition of analog line port connection dataset.
 *
 * @author A. Brandt
 * @author J. Eickhoff
 * @version 1.2
 * @since 2.6.7
 */

public class AnalogPort extends BasePort {
    private static final Logger LOG = LoggerFactory.getLogger(AnalogPort.class);
     private double analogValue;

    /**
     * This constructor is needed for the AnalogPort initialisation during
     * the XML file parsing.
     */
    public AnalogPort(final String name) {
        super(name);
    }

    public void setAnalogValue(final double analogValue) {
        this.analogValue = analogValue;
        LOG.info("% Setting Analog Value: '{}'", this.analogValue);
    }

    public double getAnalogValue() {
    LOG.info("% Getting Value: '{}'", this.analogValue);
        return analogValue;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append(this.getClass().getName());
        result.append(": analogValue == ");
        result.append(analogValue);
        result.append(".");

        return result.toString();
    }
}
