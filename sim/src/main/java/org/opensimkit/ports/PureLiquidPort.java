/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.opensimkit.ports;

import org.opensimkit.Kernel;

/**
 *
 * @author A. Brandt
 */
public final class PureLiquidPort extends FluidPort {

    /**
     * This constructor is needed for the PureLiquidPort initialisation during
     * the XML file parsing.
     */
    public PureLiquidPort(final String name, final Kernel kernel) {
        super(name, kernel);
    }
}
