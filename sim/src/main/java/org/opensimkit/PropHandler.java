/*
 * PropHandler.java
 *
 * Created on 8. Juli 2007, 21:33
 *
 *  This is the header file of the Class PropHandler.
 *  The instance PropertyHandler handles the property data of fluids in the
 *  simulation program.
 *
 *-----------------------------------------------------------------------------
 *  Modification History
 *
 *  2004-12-05
 *      File created - J. Eickhoff:
 *
 *      Class architecture is a derivative from ObjectSim 2.0.3.,
 *      a simulation program published in:
 *
 *        Eickhoff, J.:
 *        Modulare Programmarchitektur fuer ein wissensbasiertes
 *        Simulationssystem mit erweiterter Anwendbarkeit in der
 *        Entwicklung und Betriebsueberwachung verfahrenstechnischer
 *        Anlagen.
 *        PhD thesis in Department Process Engineering of
 *        TU Hamburg-Harburg, 1996.
 *
 *      See also file history cited there and see historic relation of
 *      this OpenSimKit class to a.m. ObjectSim explained in
 *      OpenSimKit Documentation.
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 *  2008-05
 *      OpenSimKit V 2.4
 *      Ported from C++ to Java
 *      A. Brandt  alexander.brandt@gmail.com
 *
 *
 *  2009-06
 *     Integrated logging support.
 *     T. Pieper
 *
 */

package org.opensimkit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the header file of the Class PropHandler.
 * The instance PropertyHandler handles the property data of fluids in the
 * simulation program.
 *
 * @author J. Eickhoff
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.1
 * @since 2.4.0
 */
public final class PropHandler {
    private static final Logger LOG
            = LoggerFactory.getLogger(PropHandler.class);
    private final String  name;

    public PropHandler(final String name) {
        this.name = name;
        LOG.debug("{}:\t{}", this.name, "constructor");
    }

    public int pureGas() { //  (MixFlag, Medium, P, T) {
        LOG.debug("{}:\t{}", this.name, "pureGas");
        LOG.error(
                "Property Handler: Pure gas computations not yet implemented!");
        return 0;
    }

    public int mixedGas() {
        LOG.debug("{}:\t{}", this.name, "mixedGas");
        LOG.error(
               "Property Handler: Mixed gas computations not yet implemented!");
        return 1;
    }

}
