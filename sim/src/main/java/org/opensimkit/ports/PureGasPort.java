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
public final class PureGasPort extends FluidPort {

    /**
     * This constructor is needed for the PureGasPort initialisation during
     * the XML file parsing.
     */
    public PureGasPort(final String name, final Kernel kernel) {
        super(name, kernel);
    }
}
