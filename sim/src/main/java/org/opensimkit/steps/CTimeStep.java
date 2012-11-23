/*
 * CTimeStep.java
 *
 * Created on 6. Juli 2007, 01:37
 *
 * Implementation of a computation timestep class
 *
 *-----------------------------------------------------------------------------
 * Modification History:
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
 *  2009-06
 *     Integrated logging support.
 *     T. Pieper
 *
 */

package org.opensimkit.steps;

import java.io.IOException;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opensimkit.CalcStep;
import org.opensimkit.BaseModel;
import org.opensimkit.SimHeaders;

/**
 * Implementation of a computation timestep class.
 *
 * @author J. Eickhoff
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.1
 * @since 2.4.0
 */
public final class CTimeStep extends CalcStep {
    private static final Logger LOG = LoggerFactory.getLogger(CTimeStep.class);

  /** Creates a new instance of CTimeStep.
   * @param name
   */
    public CTimeStep(final String name) {
        super(name);
        LOG.debug(SimHeaders.DEBUG_SHORT, "Constructor");
    }

    // INITIALIZATION
    //-------------------------------------------------------------------------
    public int load() {
        LOG.debug(SimHeaders.DEBUG_SHORT, "load");
        return 0;
    }

    // COMPUTATION
    //-------------------------------------------------------------------------
    public int calc(final double time, final double tStepSize)
        throws IOException {

        String thestring1;
        String thestring2;

        LOG.debug(SimHeaders.DEBUG_SHORT, "compute");

        Iterator it = items.iterator();
        while (it.hasNext()) {
            BaseModel model = (BaseModel) it.next();
            if (model.timeStep(time, tStepSize) != 0) { // A model found
                                                            // an error in
                                                            // computation
                LOG.info("Model  '" + model.getName()
                    + "' - timestep error! time: " + time);
                thestring1 = model.getName();
                thestring2 = Double.toString(time);
                SimHeaders.negativeAckFlag = 1;
                return 1;
            }
        }
        return 0;
    }

}
